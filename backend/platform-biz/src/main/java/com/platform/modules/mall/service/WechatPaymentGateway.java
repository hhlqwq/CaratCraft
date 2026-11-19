package com.platform.modules.mall.service;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

/** 商城与微信支付之间的契约, 金额为人民币分. */
public interface WechatPaymentGateway {
    /** 返回网关配置的小程序标识. */
    String appId();

    /** 创建同号预支付并返回原生小程序调起参数. */
    ClientPayment prepay(String orderNo, String openId, int cents, String notifyUrl);

    /** 查询原商户订单, 仅官方明确查无订单时返回空. */
    Optional<PaymentOrder> findOrder(String orderNo);

    /** 关闭未付款的渠道订单. */
    void closeOrder(String orderNo);

    /** 验签并解密支付通知, 同时校验商户及应用身份. */
    PaymentOrder parsePaymentNotice(PaymentNotice notice);

    /** 查询原商户退款号, 仅官方明确查无退款时返回空. */
    Optional<RefundRecord> findRefund(String refundNo);

    /** 提交原退款号及原订单金额, 同步响应仅作为受理依据. */
    void submitRefund(String refundNo, String orderNo, int cents);

    /** 保持小程序 wx.requestPayment 的参数契约. */
    record ClientPayment(
            String timeStamp,
            String nonceStr,
            @JsonProperty("package") String packageValue,
            String signType,
            String paySign) {}

    /** 保存已校验商户和应用的交易结果. */
    record PaymentOrder(String orderNo, String state, Integer totalCents) {}

    /** 保存平台签名需要的原始通知正文及请求头. */
    record PaymentNotice(
            String serial, String nonce, String signature, String timestamp, String body) {}

    /** 保存已验签退款查询结果中的身份、金额及状态. */
    record RefundRecord(String refundNo, String orderNo, int cents, String state) {}

    /** 统一渠道失败, 不把 SDK 响应与密钥传给业务调用方. */
    final class ChannelFailure extends RuntimeException {
        /** 提供对外可用的稳定错误类型. */
        public ChannelFailure() {
            super("微信支付请求或验签失败");
        }
    }
}
