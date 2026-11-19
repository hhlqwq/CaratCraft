package com.platform.modules.mall.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.platform.modules.custom.service.CustomDesignService;
import com.platform.modules.mall.entity.MallOrderEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 使用支付替身验证受理与完成区别, 不触发真实退款. */
class RefundReconciliationTest {
    private OrderRefundService refunds;
    private MallOrderService orders;
    private JdbcTemplate jdbc;
    private WechatPaymentGateway pay;

    /** 准备已收货订单和商家批准的退款流水. */
    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        orders = mock(MallOrderService.class);
        jdbc = mock(JdbcTemplate.class);
        pay = mock(WechatPaymentGateway.class);
        ObjectProvider<WechatPaymentGateway> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(pay);
        PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
        when(manager.getTransaction(any())).thenAnswer(call -> new SimpleTransactionStatus());
        refunds =
                new OrderRefundService(
                        orders, mock(CustomDesignService.class), jdbc, provider, manager);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        when(jdbc.queryForList(
                        contains("SELECT r.order_id FROM CC_REFUND"),
                        eq(Integer.class),
                        any(Object[].class)))
                .thenReturn(List.of(7));
        when(jdbc.queryForMap(anyString(), eq(7)))
                .thenReturn(
                        Map.of(
                                "refund_no",
                                "CCRF-TEST",
                                "amount_fen",
                                19900,
                                "original_status",
                                301,
                                "status",
                                "APPROVED"));
        MallOrderEntity order = new MallOrderEntity();
        order.setId(7);
        order.setUserId(8L);
        when(orders.lockById(7)).thenReturn(order);
        order.setOrderSn("CC-ORDER-7");
        when(orders.getById(7)).thenReturn(order);
    }

    /** 模拟微信返回原订单和退款号, 金额由用例决定. */
    private void channelRefund(String state, int cents) {
        when(pay.findRefund("CCRF-TEST"))
                .thenReturn(
                        Optional.of(
                                new WechatPaymentGateway.RefundRecord(
                                        "CCRF-TEST", "CC-ORDER-7", cents, state)));
    }

    /** 已受理但仍处理中不能更新本地退款完成. */
    @Test
    void processingNeverCompletesOrder() {
        channelRefund("PROCESSING", 19900);
        refunds.reconcile();
        verify(orders, never()).completeRefund(any(), any());
    }

    /** 查单成功允许已收货订单进入402, 金额及退款号均须匹配. */
    @Test
    void confirmedSuccessCompletesReceivedOrder() {
        channelRefund("SUCCESS", 19900);
        refunds.reconcile();
        verify(orders).completeRefund(7, 402);
    }

    /** 异常金额不能推动订单退款完成. */
    @Test
    void mismatchedAmountQueuesRetry() {
        channelRefund("SUCCESS", 1);
        refunds.reconcile();
        verify(orders, never()).completeRefund(any(), any());
        verify(jdbc).update(contains("status='RETRY'"), eq(7));
    }

    /** 其他实例已领取租约时不调用网关. */
    @Test
    void leasePreventsConcurrentGatewayCall() throws Exception {
        when(jdbc.update(contains("attempts=attempts+1"), eq(7))).thenReturn(0);
        refunds.reconcile();
        verify(pay, never()).findRefund(anyString());
    }
}
