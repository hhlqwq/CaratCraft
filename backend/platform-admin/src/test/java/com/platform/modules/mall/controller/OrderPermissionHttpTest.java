package com.platform.modules.mall.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.binarywang.wxpay.bean.request.WxPayRefundQueryRequest;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.bean.result.WxPayRefundQueryResult;
import com.github.binarywang.wxpay.bean.result.WxPayRefundResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.platform.PlatformAdminApplication;
import com.platform.modules.job.utils.ScheduleRunnable;

import org.apache.shiro.authz.UnauthorizedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** 真实HTTP经Shiro和隔离数据库授权, 支付渠道全程使用替身. */
@EnabledIfEnvironmentVariable(named = "CARATCRAFT_DATABASE_TEST", matches = "true")
@ActiveProfiles("docker")
@SpringBootTest(
        classes = PlatformAdminApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"server.address=127.0.0.1", "caratcraft.refund-reconciliation-enabled=false"})
class OrderPermissionHttpTest {
    private static final int SHIP = 8700401;
    private static final int APPROVE = 8700402;
    private static final int RETRY = 8700403;
    private static final int WRITEBACK = 8700404;
    private static final int MISSING = 8700405;
    private static final String LEGACY_JOB = "cc-perm-legacy-refund";
    private static final String KNOWN_JOB = "orderTimeoutTask";
    private final Map<String, String> tokens = new HashMap<>();

    @Autowired private TestRestTemplate http;
    @Autowired private JdbcTemplate jdbc;
    @MockBean private WxPayService gateway;

    /** 只接受现有隔离回归工具生成的回环数据库, Redis也固定在回环. */
    @DynamicPropertySource
    static void configureIsolatedServices(DynamicPropertyRegistry registry) {
        String url = System.getenv("CARATCRAFT_DATABASE_TEST_URL");
        if (url == null
                || !url.matches(
                        "jdbc:mariadb://127\\.0\\.0\\.1:[0-9]+/caratcraft_init_audit_[0-9]{8}_[a-f0-9]{6}")) {
            throw new IllegalStateException("后台权限HTTP测试只允许工具生成的隔离库");
        }
        for (String name : List.of("master", "second")) {
            String prefix = "spring.datasource.dynamic.datasource." + name;
            registry.add(prefix + ".url", () -> url + "?sslMode=disable");
            registry.add(prefix + ".username", () -> "root");
            registry.add(
                    prefix + ".password", () -> System.getenv("CARATCRAFT_DATABASE_TEST_PASSWORD"));
        }
        registry.add("spring.data.redis.host", () -> "127.0.0.1");
        registry.add("spring.data.redis.port", () -> System.getenv("CARATCRAFT_REDIS_TEST_PORT"));
        registry.add("spring.data.redis.password", () -> "");
    }

    /** 使用种子菜单关联普通角色, 准备可以真实变更的独立订单和退款流水. */
    @BeforeEach
    void seedFixtures() {
        createAccount("ship", "1801", "180104");
        createAccount("approve", "1801", "180105");
        createAccount("retry", "1801", "180106");
        createAccount("writeback", "1801", "180107");
        createAccount("legacy", "180102");
        createAccount("schedule", "100703", "100706", "100707");
        jdbc.update(
                "INSERT INTO"
                    + " mall_order(id,order_sn,user_id,order_status,pay_status,shipping_status,actual_price)"
                    + " VALUES(8700401,'cc-perm-ship',1,201,2,0,1),"
                    + " (8700402,'cc-perm-approve',1,301,3,2,1),"
                    + " (8700403,'cc-perm-retry',1,301,3,2,1),"
                    + " (8700404,'cc-perm-writeback',1,301,3,2,1),"
                    + " (8700405,'cc-perm-missing',1,301,3,2,1)");
        jdbc.update(
                "INSERT INTO CC_REFUND(order_id,refund_no,status,original_status,amount_fen,reason)"
                        + " VALUES(8700402,'CC-PERM-APPROVE','PENDING_REVIEW',301,100,'隔离申请'),"
                        + " (8700403,'CC-PERM-RETRY','RETRY',301,100,'隔离申请'),"
                        + " (8700404,'CC-PERM-WRITEBACK','PROCESSING',301,100,'隔离申请'),"
                        + " (8700405,'CC-PERM-MISSING','PROCESSING',301,100,'隔离申请')");
        jdbc.update(
                "INSERT INTO"
                    + " SCHEDULE_JOB(JOB_ID,BEAN_NAME,METHOD_NAME,CRON_EXPRESSION,STATUS,CREATE_TIME)"
                    + " VALUES(?,'refundReconciliationScheduler','reconcile','0 0 0 1 1"
                    + " ?',1,NOW())",
                LEGACY_JOB);
    }

    /** 清理本用例写入的合成行, 隔离库中其他业务用例的数据不参与清理. */
    @AfterEach
    void removeFixtures() {
        jdbc.update("DELETE FROM SCHEDULE_JOB WHERE JOB_ID=?", LEGACY_JOB);
        jdbc.update("DELETE FROM CC_REFUND WHERE order_id BETWEEN ? AND ?", SHIP, MISSING);
        jdbc.update("DELETE FROM mall_order WHERE id BETWEEN ? AND ?", SHIP, MISSING);
        for (String account : tokens.keySet()) {
            String id = accountId(account);
            jdbc.update("DELETE FROM SYS_USER_TOKEN WHERE USER_ID=?", id);
            jdbc.update("DELETE FROM SYS_USER_ROLE WHERE USER_ID=?", id);
            jdbc.update("DELETE FROM SYS_ROLE_MENU WHERE ROLE_ID=?", id);
            jdbc.update("DELETE FROM SYS_ROLE WHERE ROLE_ID=?", id);
            jdbc.update("DELETE FROM SYS_USER WHERE USER_ID=?", id);
        }
        tokens.clear();
    }

    /** 正常发货写入物流字段, 同一账号可以查阅但三类退款写入一律403. */
    @Test
    void fulfillmentCanShipButCannotChangeRefunds() {
        var before = snapshot();
        send("ship", HttpMethod.GET, "/mall/shipping/queryAll", null, 200);
        send("ship", HttpMethod.GET, "/mall/refund/list", null, 200);
        assertRefundWritesDenied("ship");
        assertEquals(before, snapshot());
        verifyNoInteractions(gateway);
        send("ship", HttpMethod.POST, "/mall/order/ship", shipment(), 200);
        assertEquals(
                "300:1:PERM-TRACK",
                jdbc.queryForObject(
                        "SELECT CONCAT(order_status,':',shipping_status,':',shipping_no) FROM"
                                + " mall_order WHERE id=?",
                        String.class,
                        SHIP));
        assertEquals("PENDING_REVIEW", refundStatus(APPROVE));
    }

    /** 三项退款权限互不蕴含且均不能发货, 渠道提交只来自获准的单笔重试. */
    @Test
    void refundRolesHaveIndependentWritesAndCannotShip() throws Exception {
        var before = snapshot();
        for (String account : List.of("approve", "retry", "writeback")) {
            send(account, HttpMethod.POST, "/mall/order/ship", shipment(), 403);
        }
        send("approve", HttpMethod.POST, retryUrl(RETRY), null, 403);
        send("approve", HttpMethod.POST, "/mall/refund/reconcile", null, 403);
        send("retry", HttpMethod.POST, "/mall/refund/decide", decision(), 403);
        send("retry", HttpMethod.POST, "/mall/refund/reconcile", null, 403);
        send("writeback", HttpMethod.POST, "/mall/refund/decide", decision(), 403);
        send("writeback", HttpMethod.POST, retryUrl(RETRY), null, 403);
        assertEquals(before, snapshot());
        verifyNoInteractions(gateway);

        send("approve", HttpMethod.POST, "/mall/refund/decide", decision(), 200);
        assertEquals("APPROVED", refundStatus(APPROVE));
        verifyNoInteractions(gateway);
        WxPayException missing = mock(WxPayException.class);
        when(missing.getErrCode()).thenReturn("REFUNDNOTEXIST");
        when(gateway.refundQuery(any(WxPayRefundQueryRequest.class)))
                .thenAnswer(
                        call -> {
                            WxPayRefundQueryRequest query = call.getArgument(0);
                            if ("CC-PERM-WRITEBACK".equals(query.getOutRefundNo())) {
                                return successfulQuery();
                            }
                            throw missing;
                        });
        WxPayRefundResult accepted = new WxPayRefundResult();
        accepted.setResultCode("SUCCESS");
        when(gateway.refund(any(WxPayRefundRequest.class))).thenReturn(accepted);
        send("retry", HttpMethod.POST, retryUrl(APPROVE), null, 409);
        send("retry", HttpMethod.POST, retryUrl(RETRY), null, 200);
        send("retry", HttpMethod.POST, retryUrl(RETRY), null, 409);
        var refund = ArgumentCaptor.forClass(WxPayRefundRequest.class);
        verify(gateway).refund(refund.capture());
        assertEquals("CC-PERM-RETRY", refund.getValue().getOutRefundNo());
        assertEquals("PROCESSING", refundStatus(RETRY));
        clearInvocations(gateway);

        JsonNode written = send("writeback", HttpMethod.POST, "/mall/refund/reconcile", null, 200);
        assertEquals(2, written.path("data").path("processed").asInt());
        assertEquals("SUCCEEDED", refundStatus(WRITEBACK));
        assertEquals("PROCESSING", refundStatus(MISSING));
        assertEquals("APPROVED", refundStatus(APPROVE));
        assertEquals(
                4,
                jdbc.queryForObject(
                        "SELECT pay_status FROM mall_order WHERE id=?", Integer.class, WRITEBACK));
        verify(gateway, never()).refund(any(WxPayRefundRequest.class));
    }

    /** 旧订单修改角色无法使用敏感入口, 原订单通用更新和旧式别名无法变更数据. */
    @Test
    void legacyOrderUpdateDoesNotAuthorizeShippingOrRefundWrites() {
        var before = snapshot();
        send("legacy", HttpMethod.POST, "/mall/order/ship", shipment(), 403);
        assertRefundWritesDenied("legacy");
        send(
                "legacy",
                HttpMethod.POST,
                "/mall/order/update",
                Map.of("id", SHIP, "shippingStatus", 1, "payStatus", 4),
                400);
        send("legacy", HttpMethod.POST, "/mall/order/sendGoods", shipment(), 404);
        send("legacy", HttpMethod.GET, "/mall/refund/reconcile", null, 405);
        assertEquals(before, snapshot());
        verifyNoInteractions(gateway);
    }

    /** 通用任务新增、修改和批量触发均不能绕过退款专用授权. */
    @Test
    void configurableSchedulerCannotInvokeRefundService() {
        var before = snapshot();
        var definition =
                Map.of(
                        "jobId",
                        KNOWN_JOB,
                        "beanName",
                        "orderRefundService",
                        "methodName",
                        "reconcile",
                        "cronExpression",
                        "0 0 0 1 1 ?");
        send("schedule", HttpMethod.POST, "/sys/schedule/save", definition, 403);
        send("schedule", HttpMethod.POST, "/sys/schedule/update", definition, 403);
        send("schedule", HttpMethod.POST, "/sys/schedule/run", List.of(KNOWN_JOB, LEGACY_JOB), 403);
        assertThrows(
                UnauthorizedException.class,
                () -> new ScheduleRunnable("orderRefundService", "reconcile", null));
        assertEquals(
                "orderTimeoutTask",
                jdbc.queryForObject(
                        "SELECT BEAN_NAME FROM SCHEDULE_JOB WHERE JOB_ID=?",
                        String.class,
                        KNOWN_JOB));
        assertEquals(before, snapshot());
        verifyNoInteractions(gateway);
    }

    /** 通过真实角色菜单关系和真实令牌存储建立非超管账号. */
    private void createAccount(String account, String... menus) {
        String id = accountId(account);
        String token = UUID.randomUUID().toString();
        tokens.put(account, token);
        jdbc.update(
                "INSERT INTO SYS_USER(USER_ID,USER_NAME,REAL_NAME,SEX,STATUS,CREATE_TIME)"
                        + " VALUES(?,?,?,0,1,NOW())",
                id,
                id,
                id);
        jdbc.update(
                "INSERT INTO SYS_ROLE(ROLE_ID,ROLE_NAME,CREATE_TIME) VALUES(?,?,NOW())", id, id);
        jdbc.update("INSERT INTO SYS_USER_ROLE(ID,USER_ID,ROLE_ID) VALUES(?,?,?)", id, id, id);
        jdbc.update(
                "INSERT INTO SYS_USER_TOKEN(USER_ID,TOKEN,EXPIRE_TIME,UPDATE_TIME)"
                        + " VALUES(?,?,DATE_ADD(NOW(),INTERVAL 1 HOUR),NOW())",
                id,
                token);
        for (String menu : menus) {
            assertEquals(
                    1,
                    jdbc.update(
                            "INSERT INTO SYS_ROLE_MENU(ID,ROLE_ID,MENU_ID) SELECT"
                                + " REPLACE(UUID(),'-',''),?,MENU_ID FROM SYS_MENU WHERE MENU_ID=?",
                            id,
                            menu),
                    menu);
        }
    }

    /** 为夹具账户与角色生成与超管不同的稳定标识. */
    private String accountId(String account) {
        return "cc-perm-" + account;
    }

    /** 对同一数据库和网关重复检查前后快照, 包含退款租约、状态及成品库存. */
    private List<List<Map<String, Object>>> snapshot() {
        return List.of(
                jdbc.queryForList(
                        "SELECT * FROM mall_order WHERE id BETWEEN ? AND ? ORDER BY id",
                        SHIP,
                        MISSING),
                jdbc.queryForList(
                        "SELECT * FROM CC_REFUND WHERE order_id BETWEEN ? AND ? ORDER BY order_id",
                        SHIP,
                        MISSING),
                jdbc.queryForList("SELECT id,goods_number FROM mall_product ORDER BY id"));
    }

    /** 登录会话走真实HTTP过滤器, 同时校验传输状态和统一业务码. */
    private JsonNode send(String account, HttpMethod method, String path, Object body, int status) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("token", tokens.get(account));
        if (body != null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        var response = http.exchange(path, method, new HttpEntity<>(body, headers), JsonNode.class);
        var payload = response.getBody();
        assertEquals(
                status,
                response.getStatusCode().value(),
                () -> method + " " + path + " " + payload);
        assertNotNull(payload);
        assertEquals(status == 200 ? 0 : status, payload.path("code").asInt(-1), path);
        return payload;
    }

    /** 对可正常执行的退款申请连续检查三项写入口的403. */
    private void assertRefundWritesDenied(String account) {
        send(account, HttpMethod.POST, "/mall/refund/decide", decision(), 403);
        send(account, HttpMethod.POST, retryUrl(RETRY), null, 403);
        send(account, HttpMethod.POST, "/mall/refund/reconcile", null, 403);
    }

    /** 返回实际退款状态用于核对持久化写回. */
    private String refundStatus(int orderId) {
        return jdbc.queryForObject(
                "SELECT status FROM CC_REFUND WHERE order_id=?", String.class, orderId);
    }

    /** 返回指向已付款待发货订单的有效发货参数. */
    private Map<String, Object> shipment() {
        return Map.of("id", SHIP, "shippingName", "隔离物流", "shippingNo", "PERM-TRACK");
    }

    /** 返回对待审核申请执行通过所需的有效参数. */
    private Map<String, Object> decision() {
        return Map.of("orderId", APPROVE, "approved", true, "note", "隔离退款审核");
    }

    /** 按订单身份生成单笔重试地址. */
    private String retryUrl(int orderId) {
        return "/mall/refund/retry?orderId=" + orderId;
    }

    /** 返回同号同金额的渠道成功记录, 不进行网络调用. */
    private WxPayRefundQueryResult successfulQuery() {
        var record = new WxPayRefundQueryResult.RefundRecord();
        record.setOutRefundNo("CC-PERM-WRITEBACK");
        record.setRefundFee(100);
        record.setRefundStatus("SUCCESS");
        var query = new WxPayRefundQueryResult();
        query.setResultCode("SUCCESS");
        query.setRefundRecords(List.of(record));
        return query;
    }
}
