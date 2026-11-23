package com.platform.modules.mall.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.platform.PlatformAdminApplication;
import com.platform.common.utils.AesUtil;
import com.platform.modules.job.utils.ScheduleRunnable;
import com.platform.modules.mall.service.WechatPaymentGateway;
import com.platform.modules.sys.service.MailService;
import com.platform.modules.sys.service.PlatformAccountBootstrap;
import com.platform.modules.sys.service.SysUserService;
import com.platform.modules.sys.service.SysUserTokenService;

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
import java.util.Optional;
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
    @Autowired private SysUserService users;
    @Autowired private SysUserTokenService userTokens;
    @MockBean private WechatPaymentGateway gateway;
    @MockBean private MailService mail;

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
        registry.add("caratcraft.default-business-org", () -> "QT-A");
        registry.add("caratcraft.staff-recovery.email-enabled", () -> true);
    }

    /** 使用种子菜单关联普通角色, 准备可以真实变更的独立订单和退款流水. */
    @BeforeEach
    void seedFixtures() {
        jdbc.update(
                "INSERT INTO SYS_ORG(ORG_NO,ORG_NAME,STATUS,CREATE_TIME)"
                        + " VALUES('QT-A','机构A',1,NOW()),('QT-B','机构B',1,NOW())");
        createAccount("ship");
        grantPreset("ship", "cc-job-fulfillment", "QT-A");
        createAccount("approve", "1802", "180201", "180105");
        createAccount("retry", "1802", "180106");
        createAccount("writeback", "1802", "180107");
        createAccount("legacy", "180102");
        createAccount("operator");
        createAccount("support");
        createAccount("manager");
        createAccount("empty");
        grantPreset("operator", "cc-job-goods", "QT-A");
        grantPreset("support", "cc-job-support", "QT-A");
        jdbc.update(
                "UPDATE SYS_USER SET ACCOUNT_TYPE='MERCHANT_ADMIN' WHERE USER_ID=?",
                accountId("manager"));
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
                "UPDATE mall_order SET scope_org_no='QT-A' WHERE id BETWEEN ? AND ?",
                SHIP,
                MISSING);
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
        jdbc.update("DELETE FROM SYS_OSS WHERE ID IN ('cc-perm-asset-a','cc-perm-asset-b')");
        jdbc.update(
                "DELETE FROM SYS_LOG WHERE SCOPE_ORG_NO IN ('QT-A','QT-B') OR"
                        + " ID='cc-perm-audit-unmapped'");
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
        jdbc.update("DELETE FROM SYS_ROLE_ORG WHERE ORG_NO IN ('QT-A','QT-B')");
        jdbc.update("DELETE FROM SYS_ORG WHERE ORG_NO IN ('QT-A','QT-B')");
        jdbc.update(
                "DELETE t FROM SYS_USER_TOKEN t JOIN SYS_USER u ON u.USER_ID=t.USER_ID"
                        + " WHERE u.USER_NAME='cc-created-by-test'");
        jdbc.update("DELETE FROM SYS_USER WHERE USER_NAME='cc-created-by-test'");
        jdbc.update("UPDATE SYS_ROLE SET DELEGABLE=0 WHERE ROLE_ID='cc-job-refunds'");
        tokens.clear();
    }

    /** 校验商品上下架只影响销售状态, 未授权账号和演示主图均不能发布. */
    @Test
    void goodsSaleRequiresUploadedPhotoAndPreservesInventory() {
        int id = 1999107;
        String photo = "/upload/fixtures-product-photo.png";
        var original =
                jdbc.queryForMap(
                        "SELECT scope_org_no, primary_pic_url, is_on_sale FROM mall_goods WHERE"
                            + " id=?",
                        id);
        try {
            jdbc.update("UPDATE mall_goods SET scope_org_no='QT-A',is_on_sale=0 WHERE id=?", id);
            var inventory =
                    jdbc.queryForList(
                            "SELECT id,goods_number FROM mall_product WHERE goods_id=?", id);
            var publish = Map.of("id", id, "onSale", true);
            send("empty", HttpMethod.POST, "/mall/goods/sale", publish, 403);
            send("operator", HttpMethod.POST, "/mall/goods/sale", publish, 400);
            jdbc.update("UPDATE mall_goods SET primary_pic_url=? WHERE id=?", photo, id);
            jdbc.update(
                    "INSERT INTO SYS_OSS(ID,URL,CREATE_DATE,CREATE_USER_ORG_NO)"
                            + " VALUES('cc-perm-asset-a',?,NOW(),'QT-A')",
                    photo);
            send("operator", HttpMethod.POST, "/mall/goods/sale", publish, 200);
            assertEquals(
                    1,
                    jdbc.queryForObject(
                            "SELECT is_on_sale FROM mall_goods WHERE id=?", Integer.class, id));
            send(
                    "operator",
                    HttpMethod.POST,
                    "/mall/goods/sale",
                    Map.of("id", id, "onSale", false),
                    200);
            assertEquals(
                    0,
                    jdbc.queryForObject(
                            "SELECT is_on_sale FROM mall_goods WHERE id=?", Integer.class, id));
            assertEquals(
                    inventory,
                    jdbc.queryForList(
                            "SELECT id,goods_number FROM mall_product WHERE goods_id=?", id));
        } finally {
            jdbc.update(
                    "UPDATE mall_goods SET scope_org_no=?,primary_pic_url=?,is_on_sale=? WHERE"
                        + " id=?",
                    original.get("scope_org_no"),
                    original.get("primary_pic_url"),
                    original.get("is_on_sale"),
                    id);
        }
    }

    /** 正常发货写入物流字段, 同一账号可以查阅但三类退款写入一律403. */
    @Test
    void fulfillmentCanShipButCannotChangeRefunds() {
        var before = snapshot();
        send("ship", HttpMethod.GET, "/mall/shipping/queryAll", null, 200);
        send("ship", HttpMethod.GET, "/mall/refund/list", null, 403);
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

        send("approve", HttpMethod.POST, "/mall/refund/decide", decision(), 400);
        send("approve", HttpMethod.POST, "/mall/refund/review", decision(), 200);
        assertEquals("PENDING_REVIEW", refundStatus(APPROVE));
        send("approve", HttpMethod.POST, "/mall/refund/decide", decision(), 200);
        assertEquals("APPROVED", refundStatus(APPROVE));
        verifyNoInteractions(gateway);
        when(gateway.findRefund(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(
                        call ->
                                "CC-PERM-WRITEBACK".equals(call.getArgument(0))
                                        ? Optional.of(
                                                new WechatPaymentGateway.RefundRecord(
                                                        "CC-PERM-WRITEBACK",
                                                        "cc-perm-writeback",
                                                        100,
                                                        "SUCCESS"))
                                        : Optional.empty());
        send("retry", HttpMethod.POST, retryUrl(APPROVE), null, 409);
        send("retry", HttpMethod.POST, retryUrl(RETRY), null, 200);
        send("retry", HttpMethod.POST, retryUrl(RETRY), null, 409);
        verify(gateway).submitRefund("CC-PERM-RETRY", "cc-perm-retry", 100);
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
        verify(gateway, never()).submitRefund(any(), any(), org.mockito.ArgumentMatchers.anyInt());
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
                403);
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

    /** 货品、客服和系统岗位仅获得各自工作台和后端接口能力. */
    @Test
    void jobPresetsOnlyExposeTheirAuthorizedActions() {
        send("operator", HttpMethod.GET, "/mall/goods/list", null, 200);
        for (String dictionary : List.of("category", "brand", "attribute", "specification")) {
            send("operator", HttpMethod.GET, "/mall/" + dictionary + "/queryAll", null, 200);
        }
        jdbc.update(
                "INSERT INTO SYS_OSS(ID,URL,CREATE_DATE,CREATE_USER_ORG_NO)"
                        + " VALUES('cc-perm-asset-a','/fixture/image-a.png',NOW(),'QT-A'),"
                        + " ('cc-perm-asset-b','/fixture/image-b.png',NOW(),'QT-B')");
        var assets =
                send("operator", HttpMethod.GET, "/sys/oss/list?url=/fixture/", null, 200)
                        .path("data");
        assertEquals(1, assets.path("total").asInt());
        assertEquals("/fixture/image-a.png", assets.path("records").get(0).path("url").asText());
        send("empty", HttpMethod.GET, "/sys/oss/list", null, 403);
        send("operator", HttpMethod.GET, "/custom/component-stock/list?todo=stockOut", null, 200);
        send("ship", HttpMethod.GET, "/custom/work-order/list?todo=MAKING", null, 200);
        send("operator", HttpMethod.POST, "/mall/order/ship", shipment(), 403);
        assertRefundWritesDenied("operator");
        send("support", HttpMethod.GET, "/mall/aftersale/list", null, 200);
        var supportCase =
                send(
                                "support",
                                HttpMethod.GET,
                                "/mall/aftersale/list?todo=supportPending",
                                null,
                                200)
                        .path("data")
                        .path("records")
                        .get(0);
        assertEquals(APPROVE, supportCase.path("order_id").asInt());
        assertTrue(supportCase.has("customer_id"));
        assertFalse(supportCase.has("amount_fen"));
        send("support", HttpMethod.POST, "/mall/aftersale/handle", decision(), 200);
        send("support", HttpMethod.GET, "/mall/refund/list", null, 403);
        assertRefundWritesDenied("support");
        assertFalse(
                send("support", HttpMethod.GET, "/mall/dashboard/summary", null, 200)
                        .path("data")
                        .has("refundReview"));
        send("manager", HttpMethod.GET, "/sys/user/list", null, 200);
        send("manager", HttpMethod.GET, "/sys/role/list", null, 200);
        send("manager", HttpMethod.GET, "/sys/config/info/2", null, 403);
        send("manager", HttpMethod.GET, "/mall/refund/list", null, 200);
        send(
                "manager",
                HttpMethod.POST,
                "/sys/user/save",
                Map.of(
                        "userName",
                        "cc-created-by-test",
                        "realName",
                        "独立新员工",
                        "sex",
                        0,
                        "orgNo",
                        "QT-A",
                        "email",
                        "fixture@example.test"),
                200);
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM SYS_USER_ROLE ur JOIN SYS_USER u"
                                + " ON u.USER_ID=ur.USER_ID WHERE u.USER_NAME='cc-created-by-test'",
                        Integer.class));
        send("manager", HttpMethod.GET, "/platform/merchant/list", null, 403);
        JsonNode dashboard =
                send("manager", HttpMethod.GET, "/mall/dashboard/summary", null, 200).path("data");
        assertTrue(dashboard.has("employees"));
        assertTrue(dashboard.has("refundReview"));
        send(null, HttpMethod.GET, "/mall/dashboard/summary", null, 401);
        send("empty", HttpMethod.GET, "/mall/refund/list", null, 403);
        verifyNoInteractions(gateway);
    }

    /** 岗位只授权本工作室, 同一员工的多岗位撤权后旧令牌立即受限. */
    @Test
    void multipleRolesKeepIndependentOrganizationsAndRevokeOnOldToken() {
        grantPreset("operator", "cc-job-refunds", "QT-B");
        send("operator", HttpMethod.GET, "/mall/refund/list", null, 403);
        jdbc.update(
                "UPDATE SYS_ROLE_ORG SET ORG_NO='QT-A' WHERE ROLE_ID='cc-job-refunds' AND"
                        + " ORG_NO='QT-B'");
        jdbc.update("UPDATE mall_order SET scope_org_no='QT-B' WHERE id=?", RETRY);
        send("operator", HttpMethod.GET, "/mall/goods/list", null, 200);
        JsonNode before =
                send("operator", HttpMethod.GET, "/mall/dashboard/summary", null, 200).path("data");
        assertTrue(before.has("goods"));
        assertEquals(1, before.path("refundReview").asInt());
        assertTrue(
                send("operator", HttpMethod.GET, "/mall/refund/list", null, 200)
                                .path("data")
                                .path("total")
                                .asInt()
                        >= 1);
        send("operator", HttpMethod.POST, retryUrl(RETRY), null, 403);
        send("operator", HttpMethod.POST, "/mall/refund/review", decision(), 200);
        jdbc.update(
                "DELETE FROM SYS_USER_ROLE WHERE USER_ID=? AND ROLE_ID='cc-job-refunds'",
                accountId("operator"));
        send("operator", HttpMethod.POST, "/mall/refund/decide", decision(), 403);
        assertFalse(
                send("operator", HttpMethod.GET, "/mall/dashboard/summary", null, 200)
                        .path("data")
                        .has("refundReview"));
        send("operator", HttpMethod.GET, "/mall/goods/list", null, 200);
        verifyNoInteractions(gateway);
    }

    /** 商家管理员能停用员工, 但不能修改自己的角色或以参数突破机构范围. */
    @Test
    void disablingEmployeeInvalidatesExistingToken() {
        jdbc.update("DELETE FROM SYS_USER_ROLE WHERE USER_ID=?", accountId("empty"));
        jdbc.update("UPDATE SYS_ROLE SET DELEGABLE=1 WHERE ROLE_ID='cc-job-refunds'");
        jdbc.update(
                "INSERT INTO SYS_ROLE_ORG(ID,ROLE_ID,ORG_NO)"
                        + " VALUES(REPLACE(UUID(),'-',''),'cc-job-refunds','QT-A')");
        var selected = send("manager", HttpMethod.GET, "/sys/role/select", null, 200).path("data");
        assertEquals(1, selected.size());
        assertEquals("cc-job-refunds", selected.get(0).path("roleId").asText());
        send(
                "manager",
                HttpMethod.POST,
                "/sys/role/update",
                Map.of(
                        "roleId",
                        "cc-job-refunds",
                        "roleName",
                        "越权修改",
                        "delegable",
                        false,
                        "menuIdList",
                        List.of("180107"),
                        "orgNoList",
                        List.of("QT-B")),
                403);
        send(
                "manager",
                HttpMethod.POST,
                "/sys/user/update",
                Map.of(
                        "userId",
                        accountId("empty"),
                        "userName",
                        accountId("empty"),
                        "realName",
                        "轮岗员工",
                        "sex",
                        0,
                        "orgNo",
                        "QT-A",
                        "roleIdList",
                        List.of("cc-job-refunds")),
                200);
        send("empty", HttpMethod.GET, "/mall/refund/list", null, 200);
        send(
                "manager",
                HttpMethod.POST,
                "/sys/user/update",
                Map.of(
                        "userId",
                        accountId("manager"),
                        "userName",
                        accountId("manager"),
                        "realName",
                        "管理员",
                        "sex",
                        0,
                        "orgNo",
                        "QT-A",
                        "roleIdList",
                        List.of("cc-job-refunds")),
                403);
        send(
                "manager",
                HttpMethod.POST,
                "/sys/user/update",
                Map.of(
                        "userId",
                        accountId("empty"),
                        "userName",
                        accountId("empty"),
                        "realName",
                        "已停用员工",
                        "sex",
                        0,
                        "orgNo",
                        "QT-A",
                        "status",
                        0),
                200);
        send("empty", HttpMethod.GET, "/mall/refund/list", null, 401);
        send("empty", HttpMethod.POST, "/mall/refund/decide", decision(), 401);
        verifyNoInteractions(gateway);
    }

    /** 新账号须登记联系渠道并先改临时密码, 更换密码后旧令牌失效. */
    @Test
    void employeeCanSignInThenResetAndChangePassword() {
        String username = "cc-created-by-test";
        String initial = "123456";
        String reset = "Isolated-Reset-02!";
        String changed = "Isolated-Change-03!";
        send(
                "manager",
                HttpMethod.POST,
                "/sys/user/save",
                Map.of(
                        "userName",
                        username,
                        "realName",
                        "隔离员工",
                        "sex",
                        0,
                        "orgNo",
                        "QT-B",
                        "email",
                        "fixture@example.test"),
                403);
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM SYS_USER WHERE USER_NAME=?",
                        Integer.class,
                        username));
        send(
                "manager",
                HttpMethod.POST,
                "/sys/user/save",
                Map.of("userName", username, "realName", "隔离员工", "sex", 0),
                400);
        send(
                "manager",
                HttpMethod.POST,
                "/sys/user/save",
                Map.of(
                        "userName",
                        username,
                        "realName",
                        "隔离员工",
                        "sex",
                        0,
                        "email",
                        "fixture@example.test"),
                200);
        String employeeId =
                jdbc.queryForObject(
                        "SELECT USER_ID FROM SYS_USER WHERE USER_NAME=?", String.class, username);
        assertNotNull(employeeId);
        send(
                "manager",
                HttpMethod.POST,
                "/sys/user/update",
                Map.of(
                        "userId",
                        employeeId,
                        "userName",
                        username,
                        "realName",
                        "隔离员工",
                        "sex",
                        0,
                        "orgNo",
                        "QT-B"),
                403);
        send(
                "manager",
                HttpMethod.POST,
                "/sys/user/update",
                Map.of("userId", employeeId, "userName", username, "realName", "隔离员工", "sex", 0),
                200);
        assertEquals(
                "QT-A",
                jdbc.queryForObject(
                        "SELECT ORG_NO FROM SYS_USER WHERE USER_ID=?", String.class, employeeId));
        String original = signIn(username, initial);
        tokens.put("new-employee", original);
        send("new-employee", HttpMethod.GET, "/sys/menu/nav", null, 428);
        send("new-employee", HttpMethod.GET, "/mall/dashboard/summary", null, 428);
        send("new-employee", HttpMethod.POST, "/mall/order/ship", shipment(), 428);
        send(
                "new-employee",
                HttpMethod.POST,
                "/sys/user/password",
                Map.of("password", initial, "newPassword", initial),
                400);
        send(
                "new-employee",
                HttpMethod.POST,
                "/sys/user/password",
                Map.of("password", initial, "newPassword", changed),
                200);
        send("new-employee", HttpMethod.GET, "/mall/dashboard/summary", null, 401);
        tokens.put("new-employee", signIn(username, changed));
        assertTrue(
                send("new-employee", HttpMethod.GET, "/mall/dashboard/summary", null, 200)
                        .path("data")
                        .isEmpty());
        send("new-employee", HttpMethod.GET, "/mall/refund/list", null, 403);
        assertNotNull(
                jdbc.queryForObject(
                        "SELECT LAST_LOGIN_AT FROM SYS_USER WHERE USER_ID=?",
                        java.sql.Timestamp.class,
                        employeeId));
        var accountInfo =
                send("manager", HttpMethod.GET, "/sys/user/info/" + employeeId, null, 200)
                        .path("data");
        assertEquals(employeeId, accountInfo.path("userId").asText());
        assertFalse(accountInfo.has("password"));
        assertFalse(accountInfo.has("salt"));
        jdbc.update(
                "INSERT INTO SYS_LOG(ID,USER_NAME,OPERATION,TIME,CREATE_TIME,SCOPE_ORG_NO)"
                        + " VALUES('cc-perm-audit-secret','隔离其他机构日志','其他机构记录',0,NOW(),'QT-B'),"
                        + " ('cc-perm-audit-unmapped','隔离旧日志','未映射记录',0,NOW(),NULL)");
        var auditRows =
                send("manager", HttpMethod.GET, "/sys/log/list", null, 200)
                        .path("data")
                        .path("records");
        assertTrue(auditRows.toString().contains("创建员工"));
        assertFalse(auditRows.toString().contains("其他机构记录"));
        assertFalse(auditRows.toString().contains("未映射记录"));
        send(
                "manager",
                HttpMethod.POST,
                "/sys/user/resetPw",
                Map.of("userId", employeeId, "password", reset),
                200);
        send("new-employee", HttpMethod.GET, "/mall/dashboard/summary", null, 401);
        tokens.put("new-employee", signIn(username, reset));
        send("new-employee", HttpMethod.GET, "/mall/dashboard/summary", null, 428);
        send(
                "new-employee",
                HttpMethod.POST,
                "/sys/user/password",
                Map.of("password", reset, "newPassword", changed),
                200);
        send("new-employee", HttpMethod.GET, "/mall/dashboard/summary", null, 401);
        tokens.put("new-employee", signIn(username, changed));
        send("new-employee", HttpMethod.GET, "/mall/dashboard/summary", null, 200);
    }

    /** 只向员工档案投递验证码, 错误和重复提交不能更改凭据, 成功重置撤销旧会话. */
    @Test
    void employeeCanRecoverWithOneTimeCode() {
        String username = accountId("empty");
        String contact = "employee@example.test";
        jdbc.update(
                "UPDATE SYS_USER SET EMAIL=?,PASSWORD=?,SALT=? WHERE USER_ID=?",
                contact,
                new org.apache.shiro.crypto.hash.Sha256Hash("Before7", "fixtures-staff").toHex(),
                "fixtures-staff",
                username);
        when(mail.sendMail(eq(contact), anyString(), anyString())).thenReturn(true);
        send(
                null,
                HttpMethod.POST,
                "/sys/password-recovery/send",
                Map.of(
                        "userName", username,
                        "channel", "email",
                        "uuid", issueCaptcha(),
                        "captcha", "A2B3",
                        "email", "other@example.test"),
                200);
        var message = ArgumentCaptor.forClass(String.class);
        verify(mail).sendMail(eq(contact), anyString(), message.capture());
        var code = java.util.regex.Pattern.compile("[0-9]{6}").matcher(message.getValue());
        assertTrue(code.find());
        String replacement = "Staff<654321";
        send(
                null,
                HttpMethod.POST,
                "/sys/password-recovery/reset",
                Map.of(
                        "userName",
                        username,
                        "channel",
                        "email",
                        "code",
                        "000000",
                        "newPassword",
                        replacement),
                400);
        send("empty", HttpMethod.GET, "/mall/dashboard/summary", null, 200);
        var recovery =
                Map.of(
                        "userName",
                        username,
                        "channel",
                        "email",
                        "code",
                        code.group(),
                        "newPassword",
                        replacement);
        send(null, HttpMethod.POST, "/sys/password-recovery/reset", recovery, 200);
        send("empty", HttpMethod.GET, "/mall/dashboard/summary", null, 401);
        send(null, HttpMethod.POST, "/sys/password-recovery/reset", recovery, 400);
        tokens.put("empty", signIn(username, replacement));
        send("empty", HttpMethod.GET, "/mall/dashboard/summary", null, 200);
    }

    /** 平台账号只能管理商家管理员, 即使遗留岗位含经营权限和通配项. */
    @Test
    void platformAccountCannotOperateBusiness(
            @org.junit.jupiter.api.io.TempDir java.nio.file.Path directory)
            throws java.io.IOException {
        String original = "FixtureBootstrap123456ABC";
        String replacement = "Fixture-Platform-Password";
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT STATUS FROM SYS_USER WHERE USER_ID='1'", Integer.class));
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM SYS_USER WHERE USER_ID='1' AND PASSWORD IS NOT NULL",
                        Integer.class));
        java.nio.file.Path bootstrapFile = directory.resolve("fixture-bootstrap.txt");
        java.nio.file.Files.writeString(bootstrapFile, original);
        new PlatformAccountBootstrap(users, userTokens, jdbc, bootstrapFile.toString()).run(null);
        try {
            tokens.put("platform", signIn("admin", original));
            send("platform", HttpMethod.GET, "/platform/merchant/list", null, 428);
            signIn("admin", original, 400);
            send(
                    "platform",
                    HttpMethod.POST,
                    "/sys/user/password",
                    Map.of("password", original, "newPassword", replacement),
                    200);
            tokens.put("platform", signIn("admin", replacement));
            jdbc.update(
                    "INSERT INTO SYS_USER_ROLE(ID,USER_ID,ROLE_ID)"
                            + " VALUES('cc-platform-fixture','1','cc-job-goods')");
            jdbc.update(
                    "INSERT INTO SYS_MENU(MENU_ID,PARENT_ID,NAME,PERMS,TYPE,ORDER_NUM)"
                            + " VALUES('cc-wild','0','Fixture','*',2,0)");
            jdbc.update(
                    "INSERT INTO SYS_ROLE_MENU(ID,ROLE_ID,MENU_ID)"
                            + " VALUES('cc-admin-wild','cc-job-goods','cc-wild')");
            send("platform", HttpMethod.GET, "/mall/goods/list", null, 403);
            send("platform", HttpMethod.GET, "/mall/order/list", null, 403);
            send("platform", HttpMethod.GET, "/mall/refund/list", null, 403);
            send("platform", HttpMethod.POST, "/mall/refund/reconcile", null, 403);
            send("platform", HttpMethod.GET, "/community/posts/list", null, 403);
            send("platform", HttpMethod.GET, "/sys/user/list", null, 403);
            send("platform", HttpMethod.GET, "/platform/merchant/list", null, 200);
            send("manager", HttpMethod.POST, "/platform/merchant/create", Map.of(), 403);
            send(
                    "platform",
                    HttpMethod.POST,
                    "/platform/merchant/reset",
                    Map.of("userId", "1"),
                    403);
            send(
                    "platform",
                    HttpMethod.POST,
                    "/platform/merchant/status",
                    Map.of("userId", accountId("empty"), "active", false),
                    403);
            var issued =
                    send(
                                    "platform",
                                    HttpMethod.POST,
                                    "/platform/merchant/create",
                                    Map.of(
                                            "userName",
                                            "cc-tier-merchant",
                                            "realName",
                                            "隔离商家",
                                            "email",
                                            "merchant@example.test"),
                                    200)
                            .path("data");
            String merchantId = issued.path("userId").asText();
            String temporary = issued.path("temporaryPassword").asText();
            assertEquals(24, temporary.length());
            tokens.put("tier-merchant", signIn("cc-tier-merchant", temporary));
            send("tier-merchant", HttpMethod.GET, "/mall/goods/list", null, 428);
            send(
                    "tier-merchant",
                    HttpMethod.POST,
                    "/sys/user/password",
                    Map.of("password", temporary, "newPassword", "Merchant-Changed-Password"),
                    200);
            tokens.put("tier-merchant", signIn("cc-tier-merchant", "Merchant-Changed-Password"));
            send("tier-merchant", HttpMethod.GET, "/mall/goods/list", null, 200);
            send("tier-merchant", HttpMethod.GET, "/mall/refund/list", null, 200);
            send("tier-merchant", HttpMethod.POST, "/platform/merchant/create", Map.of(), 403);
            var reset =
                    send(
                                    "platform",
                                    HttpMethod.POST,
                                    "/platform/merchant/reset",
                                    Map.of("userId", merchantId),
                                    200)
                            .path("data");
            assertFalse(temporary.equals(reset.path("temporaryPassword").asText()));
            send("tier-merchant", HttpMethod.GET, "/mall/goods/list", null, 401);
            send(
                    "platform",
                    HttpMethod.POST,
                    "/platform/merchant/status",
                    Map.of("userId", merchantId, "active", false),
                    200);
            signIn("cc-tier-merchant", reset.path("temporaryPassword").asText(), 400);
            send(
                    "platform",
                    HttpMethod.POST,
                    "/platform/merchant/status",
                    Map.of("userId", merchantId, "active", true),
                    200);
            tokens.put(
                    "tier-merchant",
                    signIn("cc-tier-merchant", reset.path("temporaryPassword").asText()));
            send("tier-merchant", HttpMethod.GET, "/mall/order/list", null, 428);
            assertThrows(
                    org.springframework.dao.DataAccessException.class,
                    () ->
                            jdbc.update(
                                    "UPDATE SYS_USER SET ACCOUNT_TYPE='PLATFORM_ADMIN' WHERE"
                                            + " USER_ID=?",
                                    merchantId));
        } finally {
            jdbc.update("DELETE FROM SYS_ROLE_MENU WHERE ID='cc-admin-wild'");
            jdbc.update("DELETE FROM SYS_MENU WHERE MENU_ID='cc-wild'");
            jdbc.update("DELETE FROM SYS_USER_ROLE WHERE ID='cc-platform-fixture'");
            jdbc.update(
                    "DELETE t FROM SYS_USER_TOKEN t JOIN SYS_USER u ON u.USER_ID=t.USER_ID WHERE"
                            + " u.USER_ID='1' OR u.USER_NAME='cc-tier-merchant'");
            jdbc.update("DELETE FROM SYS_USER WHERE USER_NAME='cc-tier-merchant'");
            jdbc.update(
                    "UPDATE SYS_USER SET"
                        + " STATUS=0,PASSWORD=NULL,SALT=NULL,PASSWORD_CHANGE_REQUIRED=1,TEMPORARY_PASSWORD_EXPIRES_AT=NULL"
                        + " WHERE USER_ID='1'");
            tokens.remove("platform");
            tokens.remove("tier-merchant");
        }
    }

    /** 商家以岗位和直接授权选择功能, 退款的每项写操作单独判定. */
    @Test
    void merchantGrantsStudioActionsThenRevokesOldSession() {
        String employee = accountId("empty");
        send(null, HttpMethod.GET, "/platform/merchant/list", null, 401);
        send(
                "manager",
                HttpMethod.POST,
                "/sys/role/save",
                Map.of("roleName", "禁止平台权", "menuIdList", List.of("110101")),
                403);
        send(
                "manager",
                HttpMethod.POST,
                "/sys/role/save",
                Map.of("roleName", "隔离退款审核岗", "menuIdList", List.of("1802", "180201")),
                200);
        String role =
                jdbc.queryForObject(
                        "SELECT ROLE_ID FROM SYS_ROLE WHERE ROLE_NAME='隔离退款审核岗'", String.class);
        try {
            send(
                    "manager",
                    HttpMethod.POST,
                    "/sys/user/update",
                    Map.of(
                            "userId",
                            employee,
                            "userName",
                            employee,
                            "realName",
                            "隔离员工",
                            "roleIdList",
                            List.of(role),
                            "permissionMenuIds",
                            List.of("180106")),
                    200);
            send("empty", HttpMethod.GET, "/mall/refund/list", null, 200);
            send("empty", HttpMethod.POST, "/mall/refund/review", decision(), 200);
            send("empty", HttpMethod.POST, "/mall/refund/decide", decision(), 403);
            send("empty", HttpMethod.POST, "/mall/refund/reconcile", null, 403);
            send("empty", HttpMethod.GET, "/sys/user/list", null, 403);
            send("empty", HttpMethod.POST, "/sys/role/save", Map.of(), 403);
            String provenance =
                    send("manager", HttpMethod.GET, "/sys/user/permissions/" + employee, null, 200)
                            .path("data")
                            .toString();
            assertTrue(provenance.contains("mall:refund:review"));
            assertTrue(provenance.contains("mall:refund:retry"));
            send(
                    "manager",
                    HttpMethod.POST,
                    "/sys/user/update",
                    Map.of(
                            "userId",
                            employee,
                            "userName",
                            employee,
                            "realName",
                            "隔离员工",
                            "roleIdList",
                            List.of(),
                            "permissionMenuIds",
                            List.of()),
                    200);
            send("empty", HttpMethod.POST, retryUrl(RETRY), null, 403);
            send("empty", HttpMethod.POST, "/mall/refund/review", decision(), 403);
        } finally {
            send("manager", HttpMethod.POST, "/sys/role/delete", List.of(role), 200);
        }
    }

    /** 使用隔离图片验证码数据走正式登录接口, 不访问外部发码渠道. */
    private String signIn(String username, String password) {
        return signIn(username, password, 200);
    }

    /** 使用同一登录入口检查拒绝状态, 不绕过图形验证码. */
    private String signIn(String username, String password, int status) {
        String uuid = issueCaptcha();
        return send(
                        null,
                        HttpMethod.POST,
                        "/sys/login",
                        Map.of(
                                "userName",
                                username,
                                "password",
                                AesUtil.encrypt(password),
                                "uuid",
                                uuid,
                                "captcha",
                                "A2B3"),
                        status)
                .path("data")
                .asText();
    }

    /** 在隔离库放置有效图片验证码, 不使用真实用户凭据. */
    private String issueCaptcha() {
        String uuid = UUID.randomUUID().toString();
        jdbc.update(
                "INSERT INTO SYS_CAPTCHA(UUID,CODE,EXPIRE_TIME)"
                        + " VALUES(?,'A2B3',DATE_ADD(NOW(),INTERVAL 1 MINUTE))",
                uuid);
        return uuid;
    }

    /** 订单列表、详情、工作台数量及写入统一检查所属机构. */
    @Test
    void scopedCountsAndLegacyListsCannotRevealOtherOrganizations() {
        jdbc.update("UPDATE mall_order SET scope_org_no='QT-B' WHERE id=?", SHIP);
        JsonNode dashboard =
                send("ship", HttpMethod.GET, "/mall/dashboard/summary", null, 200).path("data");
        assertEquals(0, dashboard.path("toShip").asInt(-1));
        assertEquals(
                0,
                send(
                                "ship",
                                HttpMethod.GET,
                                "/mall/order/list?todo=toShip&businessOrgNos=QT-B",
                                null,
                                200)
                        .path("data")
                        .path("total")
                        .asInt(-1));
        send("ship", HttpMethod.GET, "/mall/order/info/" + SHIP, null, 403);
        send("ship", HttpMethod.GET, "/mall/order/queryAll?id=" + SHIP, null, 200);
        send("ship", HttpMethod.GET, "/mall/ordergoods/queryAll", null, 403);
        send("ship", HttpMethod.POST, "/mall/order/ship", shipment(), 403);
        verifyNoInteractions(gateway);
    }

    /** 仅在隔离夹具给实际种子岗位分派机构和合成用户. */
    private void grantPreset(String account, String role, String org) {
        jdbc.update(
                "INSERT INTO SYS_ROLE_ORG(ID,ROLE_ID,ORG_NO) VALUES(REPLACE(UUID(),'-',''),?,?)",
                role,
                org);
        jdbc.update(
                "INSERT INTO SYS_USER_ROLE(ID,USER_ID,ROLE_ID) VALUES(REPLACE(UUID(),'-',''),?,?)",
                accountId(account),
                role);
    }

    /** 通过真实角色菜单关系和真实令牌存储建立非超管账号. */
    private void createAccount(String account, String... menus) {
        String id = accountId(account);
        String token = UUID.randomUUID().toString();
        tokens.put(account, token);
        jdbc.update(
                "INSERT INTO SYS_USER(USER_ID,USER_NAME,REAL_NAME,SEX,STATUS,CREATE_TIME,ORG_NO)"
                        + " VALUES(?,?,?,0,1,NOW(),'QT-A')",
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
        jdbc.update(
                "INSERT INTO SYS_ROLE_ORG(ID,ROLE_ID,ORG_NO)"
                        + " VALUES(REPLACE(UUID(),'-',''),?,'QT-A')",
                id);
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
        if (account != null) {
            headers.set("token", tokens.get(account));
        }
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
}
