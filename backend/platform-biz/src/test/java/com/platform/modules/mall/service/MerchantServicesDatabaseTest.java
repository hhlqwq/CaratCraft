package com.platform.modules.mall.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.platform.modules.custom.service.CustomDesignService;
import com.platform.support.TransactionTestSupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** 仅在随机独立库验证客服版本、审计回滚及本人售后查询, 不连接业务库或支付服务. */
@EnabledIfEnvironmentVariable(named = "CARATCRAFT_DATABASE_TEST", matches = "true")
class MerchantServicesDatabaseTest {
    private JdbcTemplate jdbc;
    private DataSourceTransactionManager manager;
    private MerchantContactService contacts;

    /** 复用隔离初始化库, 创建真实Spring事务代理, 测试数据按事务回滚. */
    @BeforeEach
    void setUp() {
        String url = System.getenv("CARATCRAFT_DATABASE_TEST_URL");
        if (url == null
                || !url.matches(
                        "jdbc:mariadb://127\\.0\\.0\\.1:[0-9]+/caratcraft_init_audit_[0-9]{8}_[a-f0-9]{6}(\\?.*)?")) {
            throw new IllegalStateException("仅允许连接随机独立初始化测试库");
        }
        var source =
                new DriverManagerDataSource(
                        url,
                        System.getenv().getOrDefault("CARATCRAFT_DATABASE_TEST_USER", "root"),
                        System.getenv("CARATCRAFT_DATABASE_TEST_PASSWORD"));
        jdbc = new JdbcTemplate(source);
        manager = new DataSourceTransactionManager(source);
        ProxyFactory proxy = new ProxyFactory(new MerchantContactService(jdbc));
        proxy.setProxyTargetClass(true);
        proxy.addAdvice(TransactionTestSupport.interceptor(manager));
        contacts = (MerchantContactService) proxy.getProxy();
    }

    /** 全新环境不自带开发者个人联系方式或参考商家的二维码. */
    @Test
    void contactStartsUnconfigured() {
        var contact = contacts.current();
        assertEquals("", contact.merchantName());
        assertEquals("", contact.phone());
        assertEquals("", contact.wechatId());
        assertEquals("", contact.qrUrl());
    }

    /** 保存与记录使用同一版本, 迟到编辑不能覆盖已保存资料. */
    @Test
    void optimisticVersionPreservesAuditSnapshot() {
        withRollback(
                () -> {
                    var saved = contacts.save(contact(0, ""), "test-operator");
                    assertEquals(1, saved.version());
                    assertEquals("测试商家", contacts.current().merchantName());
                    assertEquals(1, contacts.history().size());
                    assertTrue(
                            String.valueOf(contacts.history().get(0).get("snapshot_json"))
                                    .contains("owned_wechat"));
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> contacts.save(contact(0, ""), "late-operator"));
                    assertEquals(1, contacts.current().version());
                    assertEquals(1, contacts.history().size());
                });
    }

    /** 新二维码须有当前操作者的精确URL记录, 已有二维码允许授权同事保留. */
    @Test
    void verifiesNewQrOwnershipWithoutBlockingUnchangedQr() {
        withRollback(
                () -> {
                    String qr = "/upload/owned-" + UUID.randomUUID() + ".png";
                    jdbc.update(
                            "INSERT INTO SYS_OSS(ID,URL,CREATE_USER_ID,CREATE_DATE)"
                                    + " VALUES(?,?,?,NOW())",
                            UUID.randomUUID().toString().replace("-", ""),
                            qr,
                            "first-operator");
                    contacts.save(contact(0, qr), "first-operator");
                    var updated = contacts.save(contact(1, qr), "other-operator");
                    assertEquals(2, updated.version());
                    assertEquals(qr, updated.qrUrl());
                    assertThrows(
                            IllegalArgumentException.class,
                            () ->
                                    contacts.save(
                                            contact(2, "https://example.invalid/unowned.png"),
                                            "other-operator"));
                    assertThrows(
                            IllegalArgumentException.class,
                            () ->
                                    contacts.save(
                                            contact(2, qr.replace("owned-", "OWNED-")),
                                            "first-operator"));
                    assertEquals(2, contacts.current().version());
                });
    }

    /** 参数和活动协议在保存前拒绝, 不能生成带有不安全二维码的新版本. */
    @Test
    void rejectsMalformedPublicContactData() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        contacts.save(
                                new MerchantContactService.Contact("", "+++++", "", "", 0),
                                "operator"));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        contacts.save(
                                new MerchantContactService.Contact(
                                        "", "", "https://wrong.invalid", "", 0),
                                "operator"));
        assertThrows(
                IllegalArgumentException.class,
                () -> contacts.save(contact(0, "javascript:invalid.png"), "operator"));
        assertEquals(0, contacts.current().version());
    }

    /** 日志必填操作者失败时主配置也必须回滚, 不产生无法追溯的变更. */
    @Test
    void failedAuditWriteRollsBackContactUpdate() {
        assertThrows(RuntimeException.class, () -> contacts.save(contact(0, ""), null));
        assertEquals(0, contacts.current().version());
        assertEquals("", contacts.current().merchantName());
        assertTrue(contacts.history().isEmpty());
    }

    /** 售后中心只返回本人记录与有限公开字段, 读取时不调用支付侧查询. */
    @Test
    @SuppressWarnings("unchecked")
    void refundListEnforcesOwnershipAndHidesProviderFields() {
        withRollback(
                () -> {
                    ObjectProvider<WechatPaymentGateway> gateway = mock(ObjectProvider.class);
                    MallOrderService orders = mock(MallOrderService.class);
                    CustomDesignService designs = mock(CustomDesignService.class);
                    var refunds = new OrderRefundService(orders, designs, jdbc, gateway, manager);
                    int owned = insertRefund(777101L, "PROCESSING");
                    insertRefund(777102L, "SUCCESS");
                    Map<String, Object> result = refunds.mine(777101L, 1, 1);
                    assertEquals(1L, result.get("total"));
                    List<Map<String, Object>> rows =
                            (List<Map<String, Object>>) result.get("records");
                    assertEquals(1, rows.size());
                    assertEquals(owned, ((Number) rows.get(0).get("orderId")).intValue());
                    assertEquals("PROCESSING", rows.get(0).get("status"));
                    for (String field :
                            List.of(
                                    "refund_no",
                                    "operator_name",
                                    "provider_status",
                                    "attempts",
                                    "next_check_time",
                                    "user_id")) {
                        assertFalse(rows.get(0).containsKey(field));
                    }
                    assertTrue(((List<?>) refunds.mine(777101L, 2, 1).get("records")).isEmpty());
                    verifyNoInteractions(gateway, orders, designs);
                });
    }

    /** 非法分页或缺失身份不能触发无边界售后查询. */
    @Test
    @SuppressWarnings("unchecked")
    void refundListRejectsMissingIdentityAndInvalidPage() {
        var refunds =
                new OrderRefundService(
                        mock(MallOrderService.class),
                        mock(CustomDesignService.class),
                        jdbc,
                        mock(ObjectProvider.class),
                        manager);
        assertThrows(IllegalArgumentException.class, () -> refunds.mine(null, 1, 20));
        assertThrows(IllegalArgumentException.class, () -> refunds.mine(777101L, 0, 20));
        assertThrows(IllegalArgumentException.class, () -> refunds.mine(777101L, 1, 51));
    }

    /** 模拟经营者表单, 测试资料不会写入业务库或实际发布为客服. */
    private MerchantContactService.Contact contact(int version, String qr) {
        return new MerchantContactService.Contact(
                "测试商家", "01012345678", "owned_wechat", qr, version);
    }

    /** 在同一测试事务插入独立订单及售后记录, 使用数据库生成的标识避免冲突. */
    private int insertRefund(Long owner, String status) {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        jdbc.update(
                "INSERT INTO"
                    + " mall_order(order_sn,user_id,order_status,pay_status,shipping_status,actual_price,order_price)"
                    + " VALUES(?,?,201,3,0,199,199)",
                "IT-" + suffix.substring(0, 24),
                owner);
        Integer id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        jdbc.update(
                "INSERT INTO CC_REFUND(order_id,refund_no,status,original_status,amount_fen,reason)"
                        + " VALUES(?,?,?,201,19900,'独立售后测试')",
                id,
                "IT-REFUND-" + suffix,
                status);
        return id;
    }

    /** 用例数据随外层事务回滚, 不清空表或删除其他测试创建的记录. */
    private void withRollback(Runnable assertions) {
        new TransactionTemplate(manager)
                .executeWithoutResult(
                        status -> {
                            try {
                                assertions.run();
                            } finally {
                                status.setRollbackOnly();
                            }
                        });
    }
}
