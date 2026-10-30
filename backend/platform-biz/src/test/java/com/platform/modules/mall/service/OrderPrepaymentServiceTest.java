package com.platform.modules.mall.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.platform.modules.mall.entity.MallOrderEntity;
import com.platform.modules.wx.entity.WxUserEntity;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.ObjectProvider;

import java.math.BigDecimal;

/** 小程序支付身份和预支付状态回归, 网关使用替身且不发送外部请求. */
class OrderPrepaymentServiceTest {
    /** JSAPI预支付必须使用小程序openid, 不得误用公众号身份. */
    @Test
    void usesMiniappOpenidAndRejectsApplicationMismatch() throws Exception {
        MallOrderService orders = mock(MallOrderService.class);
        ObjectProvider<WxPayService> provider = mock();
        WxPayService gateway = mock(WxPayService.class);
        when(provider.getIfAvailable()).thenReturn(gateway);
        WxPayConfig config = new WxPayConfig();
        config.setAppId("miniapp-test");
        when(gateway.getConfig()).thenReturn(config);
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
        WxPayMpOrderResult result = new WxPayMpOrderResult();
        result.setPackageValue("prepay_id=isolated");
        when(gateway.createOrder(any(WxPayUnifiedOrderRequest.class))).thenReturn(result);
        service.prepare(10, user, "127.0.0.1");
        ArgumentCaptor<WxPayUnifiedOrderRequest> request =
                ArgumentCaptor.forClass(WxPayUnifiedOrderRequest.class);
        verify(gateway).createOrder(request.capture());
        assertEquals("miniapp-openid", request.getValue().getOpenid());
        assertEquals(19900, request.getValue().getTotalFee());
        config.setAppId("another-application");
        assertThrows(IllegalArgumentException.class, () -> service.prepare(10, user, "127.0.0.1"));
    }
}
