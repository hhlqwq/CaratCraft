package com.platform.modules.mall.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.platform.modules.mall.entity.MallOrderEntity;
import com.platform.modules.wx.entity.WxUserEntity;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.ObjectProvider;

import java.math.BigDecimal;

/** 小程序支付身份和预支付状态回归, 网关使用替身且不发送外部请求. */
class OrderPrepaymentServiceTest {
    /** JSAPI预支付必须使用小程序openid, 不得误用公众号身份. */
    @Test
    void usesMiniappOpenidAndRejectsApplicationMismatch() throws Exception {
        MallOrderService orders = mock(MallOrderService.class);
        ObjectProvider<WechatPaymentGateway> provider = mock();
        WechatPaymentGateway gateway = mock(WechatPaymentGateway.class);
        when(provider.getIfAvailable()).thenReturn(gateway);
        when(gateway.appId()).thenReturn("miniapp-test");
        OrderPrepaymentService service =
                new OrderPrepaymentService(
                        orders, provider, "miniapp-test", "https://merchant.example/pay/notify");
        MallOrderEntity order = new MallOrderEntity();
        order.setUserId(7L);
        order.setOrderStatus(0);
        order.setPayStatus(0);
        order.setOrderSn("ISOLATED-TEST");
        order.setActualPrice(new BigDecimal("199.00"));
        when(orders.lockById(10)).thenReturn(order);
        when(orders.update(ArgumentMatchers.<Wrapper<MallOrderEntity>>notNull())).thenReturn(true);
        WxUserEntity user = new WxUserEntity();
        user.setId(7L);
        user.setMaOpenid("miniapp-openid");
        user.setOpenid("public-account-openid");
        var result =
                new WechatPaymentGateway.ClientPayment(
                        "1", "nonce", "prepay_id=isolated", "RSA", "signature");
        when(gateway.prepay(
                        "ISOLATED-TEST",
                        "miniapp-openid",
                        19900,
                        "https://merchant.example/pay/notify"))
                .thenReturn(result);
        assertEquals(result, service.prepare(10, user));
        verify(gateway)
                .prepay(
                        "ISOLATED-TEST",
                        "miniapp-openid",
                        19900,
                        "https://merchant.example/pay/notify");
        when(gateway.appId()).thenReturn("another-application");
        assertThrows(IllegalArgumentException.class, () -> service.prepare(10, user));
    }
}
