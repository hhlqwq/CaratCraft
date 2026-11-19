package com.platform.modules.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.platform.common.xss.XssFilter;
import com.platform.modules.mall.entity.MallOrderEntity;
import com.platform.modules.mall.service.MallOrderService;
import com.platform.modules.mall.service.OrderPaymentService;
import com.platform.modules.mall.service.WechatPaymentGateway;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** 用虚构交易和签名替身验证 APIv3 回调的 HTTP 和写入边界. */
class WechatPaymentNotificationTest {
    /** 精确传递签名正文, 拒绝验签失败且仅确认已入账通知. */
    @Test
    void signedBodyRemainsUnchangedAndInvalidNoticeCannotWrite() throws Exception {
        var gateway = mock(WechatPaymentGateway.class);
        var orders = mock(MallOrderService.class);
        var payments = mock(OrderPaymentService.class);
        var controller = new AppPayController();
        ReflectionTestUtils.setField(controller, "weixinPayService", gateway);
        ReflectionTestUtils.setField(controller, "orderService", orders);
        ReflectionTestUtils.setField(controller, "orderPaymentService", payments);
        var http = MockMvcBuilders.standaloneSetup(controller).addFilters(new XssFilter()).build();
        String body = "{ \"id\": \"signed-notice\", \"resource\": {} }";
        var order = new MallOrderEntity();
        order.setId(14);
        when(orders.getOne(ArgumentMatchers.<Wrapper<MallOrderEntity>>any())).thenReturn(order);
        when(gateway.parsePaymentNotice(any()))
                .thenThrow(new WechatPaymentGateway.ChannelFailure());
        var request =
                post("/app/pay/notify")
                        .contentType("application/json")
                        .content(body)
                        .header("Wechatpay-Serial", "test-serial")
                        .header("Wechatpay-Nonce", "test-nonce")
                        .header("Wechatpay-Signature", "test-signature")
                        .header("Wechatpay-Timestamp", "1");
        http.perform(request).andExpect(status().isUnauthorized());
        verifyNoInteractions(payments);
        var exact =
                new WechatPaymentGateway.PaymentNotice(
                        "test-serial", "test-nonce", "test-signature", "1", body);
        org.mockito.Mockito.doReturn(
                        new WechatPaymentGateway.PaymentOrder("fixture-order", "SUCCESS", 175))
                .when(gateway)
                .parsePaymentNotice(exact);
        http.perform(request).andExpect(status().isNoContent());
        http.perform(request).andExpect(status().isNoContent());
        verify(payments, times(2)).markPaid(14, 175);
    }
}
