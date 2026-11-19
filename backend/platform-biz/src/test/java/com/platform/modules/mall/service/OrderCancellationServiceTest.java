package com.platform.modules.mall.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.platform.modules.custom.service.CustomDesignService;
import com.platform.modules.mall.dao.MallOrderDao;
import com.platform.modules.mall.dao.MallOrderGoodsDao;
import com.platform.modules.mall.dao.MallProductDao;
import com.platform.modules.mall.dao.MallUserCouponDao;
import com.platform.modules.mall.entity.MallOrderEntity;
import com.platform.modules.mall.entity.MallOrderGoodsEntity;
import com.platform.modules.mall.service.impl.MallOrderServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

/** 取消预支付订单前必须核对和关闭支付侧订单, 不得误恢复已付款资源. */
class OrderCancellationServiceTest {
    private MallOrderServiceImpl service;
    private MallOrderDao orders;
    private WechatPaymentGateway gateway;
    private OrderPaymentService payments;
    private ObjectProvider<WechatPaymentGateway> provider;
    private MallOrderEntity order;

    /** 设置可控支付网关和资源访问替身. */
    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        service =
                spy(
                        new MallOrderServiceImpl(
                                null, null, null, null, null, null, null, null, null, null, null,
                                null));
        orders = mock(MallOrderDao.class);
        gateway = mock(WechatPaymentGateway.class);
        payments = mock(OrderPaymentService.class);
        provider = mock(ObjectProvider.class);
        ObjectProvider<OrderPaymentService> paymentProvider = mock(ObjectProvider.class);
        when(paymentProvider.getObject()).thenReturn(payments);
        when(provider.getIfAvailable()).thenReturn(gateway);
        ReflectionTestUtils.setField(service, "baseMapper", orders);
        ReflectionTestUtils.setField(service, "paymentGateway", provider);
        ReflectionTestUtils.setField(service, "paymentResults", paymentProvider);
        ReflectionTestUtils.setField(
                service, "customDesignService", mock(CustomDesignService.class));
        MallOrderGoodsDao goods = mock(MallOrderGoodsDao.class);
        when(goods.selectList(any())).thenReturn(List.of());
        ReflectionTestUtils.setField(service, "orderGoodsDao", goods);
        ReflectionTestUtils.setField(service, "userCouponDao", mock(MallUserCouponDao.class));
        order = new MallOrderEntity();
        order.setUserId(7L);
        order.setOrderStatus(0);
        order.setPayStatus(1);
        order.setOrderSn("ISOLATED-CANCEL-TEST");
        doReturn(order).when(service).lockById(10);
        when(orders.cancelUnpaid(10, 7L)).thenReturn(1);
    }

    /** 支付平台已成功时只补记支付结果, 不取消本地订单. */
    @Test
    void paidGatewayOrderIsReconciledInsteadOfCancelled() throws Exception {
        when(gateway.findOrder(order.getOrderSn()))
                .thenReturn(
                        Optional.of(
                                new WechatPaymentGateway.PaymentOrder(
                                        order.getOrderSn(), "SUCCESS", 19900)));
        assertFalse(service.cancelUnpaid(10, 7L));
        verify(payments).markPaid(10, 19900);
        verify(orders, never()).cancelUnpaid(10, 7L);
    }

    /** 预支付尚未付款时先关闭支付侧订单再取消本地订单. */
    @Test
    void unpaidGatewayOrderIsClosedBeforeLocalCancellation() throws Exception {
        when(gateway.findOrder(order.getOrderSn()))
                .thenReturn(
                        Optional.of(
                                new WechatPaymentGateway.PaymentOrder(
                                        order.getOrderSn(), "NOTPAY", null)));
        assertTrue(service.cancelUnpaid(10, 7L));
        var sequence = org.mockito.Mockito.inOrder(gateway, orders);
        sequence.verify(gateway).closeOrder(order.getOrderSn());
        sequence.verify(orders).cancelUnpaid(10, 7L);
    }

    /** 没有预支付的默认payId=0订单可以在未配置支付时正常取消. */
    @Test
    void defaultZeroPaymentIdDoesNotBlockLocalCancellation() {
        when(provider.getIfAvailable()).thenReturn(null);
        order.setPayStatus(0);
        order.setPayId("0");
        assertTrue(service.cancelUnpaid(10, 7L));
    }

    /** 支付平台不可用时不得凭本地状态恢复库存. */
    @Test
    void unavailableGatewayPreventsLocalCancellation() throws Exception {
        when(gateway.findOrder(order.getOrderSn()))
                .thenThrow(new WechatPaymentGateway.ChannelFailure());
        assertThrows(IllegalStateException.class, () -> service.cancelUnpaid(10, 7L));
        verify(orders, never()).cancelUnpaid(10, 7L);
    }

    /** 商品已经不存在时必须中止取消, 由事务回滚订单状态. */
    @Test
    void missingProductPreventsCancellationFromCommitting() {
        MallOrderGoodsEntity item = new MallOrderGoodsEntity();
        item.setProductId(12);
        item.setNumber(3);
        MallOrderGoodsDao goods = mock(MallOrderGoodsDao.class);
        MallProductDao products = mock(MallProductDao.class);
        when(goods.selectList(any())).thenReturn(List.of(item));
        when(products.restoreStock(12, 3)).thenReturn(0);
        ReflectionTestUtils.setField(service, "orderGoodsDao", goods);
        ReflectionTestUtils.setField(service, "productDao", products);
        when(provider.getIfAvailable()).thenReturn(null);
        order.setPayStatus(0);
        order.setPayId("0");

        assertThrows(IllegalStateException.class, () -> service.cancelUnpaid(10, 7L));
    }

    /** 库存恢复成功时, 没有优惠券的订单也应正常取消. */
    @Test
    void restoredStockAllowsCancellationWithoutCoupon() {
        MallOrderGoodsEntity item = new MallOrderGoodsEntity();
        item.setProductId(12);
        item.setNumber(3);
        MallOrderGoodsDao goods = mock(MallOrderGoodsDao.class);
        MallProductDao products = mock(MallProductDao.class);
        when(goods.selectList(any())).thenReturn(List.of(item));
        when(products.restoreStock(12, 3)).thenReturn(1);
        ReflectionTestUtils.setField(service, "orderGoodsDao", goods);
        ReflectionTestUtils.setField(service, "productDao", products);
        when(provider.getIfAvailable()).thenReturn(null);
        order.setPayStatus(0);
        order.setPayId("0");

        assertTrue(service.cancelUnpaid(10, 7L));
        verify(products).restoreStock(12, 3);
    }
}
