package com.platform.modules.mall.service;

import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.CloseOrderRequest;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.QueryByOutRefundNoRequest;
import com.wechat.pay.java.service.refund.model.Refund;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/** 使用微信支付官方 APIv3 SDK, 服务层只接收校验后的业务字段. */
public final class WechatV3PaymentGateway implements WechatPaymentGateway {
    private final String appId;
    private final String merchantId;
    private final JsapiServiceExtension payments;
    private final RefundService refunds;
    private final NotificationParser notifications;

    /** 绑定直连商户、应用和同一套支付验签配置. */
    public WechatV3PaymentGateway(
            String appId,
            String merchantId,
            JsapiServiceExtension payments,
            RefundService refunds,
            NotificationParser notifications) {
        this.appId = appId;
        this.merchantId = merchantId;
        this.payments = payments;
        this.refunds = refunds;
        this.notifications = notifications;
    }

    @Override
    public String appId() {
        return appId;
    }

    @Override
    public ClientPayment prepay(String orderNo, String openId, int cents, String notifyUrl) {
        var request = new PrepayRequest();
        request.setAppid(appId);
        request.setMchid(merchantId);
        request.setDescription("CaratCraft-" + orderNo);
        request.setOutTradeNo(orderNo);
        request.setNotifyUrl(notifyUrl);
        var amount = new Amount();
        amount.setTotal(cents);
        amount.setCurrency("CNY");
        request.setAmount(amount);
        var payer = new Payer();
        payer.setOpenid(openId);
        request.setPayer(payer);
        var result = call(() -> payments.prepayWithRequestPayment(request));
        return new ClientPayment(
                result.getTimeStamp(),
                result.getNonceStr(),
                result.getPackageVal(),
                result.getSignType(),
                result.getPaySign());
    }

    @Override
    public Optional<PaymentOrder> findOrder(String orderNo) {
        var request = new QueryOrderByOutTradeNoRequest();
        request.setMchid(merchantId);
        request.setOutTradeNo(orderNo);
        return find(() -> payments.queryOrderByOutTradeNo(request), "ORDER_NOT_EXIST")
                .map(result -> paymentOrder(result, orderNo));
    }

    @Override
    public void closeOrder(String orderNo) {
        var request = new CloseOrderRequest();
        request.setMchid(merchantId);
        request.setOutTradeNo(orderNo);
        call(
                () -> {
                    payments.closeOrder(request);
                    return null;
                });
    }

    @Override
    public PaymentOrder parsePaymentNotice(PaymentNotice notice) {
        var params =
                new RequestParam.Builder()
                        .serialNumber(notice.serial())
                        .nonce(notice.nonce())
                        .signature(notice.signature())
                        .timestamp(notice.timestamp())
                        .body(notice.body())
                        .build();
        return paymentOrder(call(() -> notifications.parse(params, Transaction.class)), null);
    }

    @Override
    public Optional<RefundRecord> findRefund(String refundNo) {
        var request = new QueryByOutRefundNoRequest();
        request.setOutRefundNo(refundNo);
        return find(() -> refunds.queryByOutRefundNo(request), "RESOURCE_NOT_EXISTS")
                .map(result -> refundRecord(result, refundNo));
    }

    @Override
    public void submitRefund(String refundNo, String orderNo, int cents) {
        var request = new CreateRequest();
        request.setOutRefundNo(refundNo);
        request.setOutTradeNo(orderNo);
        request.setReason("商家审核通过的退款申请");
        var amount = new AmountReq();
        amount.setTotal((long) cents);
        amount.setRefund((long) cents);
        amount.setCurrency("CNY");
        request.setAmount(amount);
        var result = refundRecord(call(() -> refunds.create(request)), refundNo);
        if (!orderNo.equals(result.orderNo()) || cents != result.cents()) {
            throw new ChannelFailure();
        }
    }

    /** 确认支付结果属于此商户和应用, 缺少金额的未付款结果只供状态判断. */
    private PaymentOrder paymentOrder(Transaction transaction, String expectedOrder) {
        if (transaction == null
                || !merchantId.equals(transaction.getMchid())
                || !appId.equals(transaction.getAppid())
                || transaction.getTradeState() == null
                || transaction.getOutTradeNo() == null
                || (expectedOrder != null && !expectedOrder.equals(transaction.getOutTradeNo()))) {
            throw new ChannelFailure();
        }
        var amount = transaction.getAmount();
        if ("SUCCESS".equals(transaction.getTradeState().name())
                && (amount == null
                        || amount.getTotal() == null
                        || !"CNY".equals(amount.getCurrency()))) {
            throw new ChannelFailure();
        }
        return new PaymentOrder(
                transaction.getOutTradeNo(),
                transaction.getTradeState().name(),
                amount == null ? null : amount.getTotal());
    }

    /** 拒绝错单或不完整的退款结果, 避免错误写回或误判缺单. */
    private RefundRecord refundRecord(Refund refund, String refundNo) {
        if (refund == null
                || !refundNo.equals(refund.getOutRefundNo())
                || refund.getOutTradeNo() == null
                || refund.getStatus() == null
                || refund.getAmount() == null
                || refund.getAmount().getRefund() == null
                || !"CNY".equals(refund.getAmount().getCurrency())) {
            throw new ChannelFailure();
        }
        return new RefundRecord(
                refundNo,
                refund.getOutTradeNo(),
                Math.toIntExact(refund.getAmount().getRefund()),
                refund.getStatus().name());
    }

    /** 只有官方 SDK 返回明确缺单才返回空, 超时及其他 HTTP 404 仍是渠道失败. */
    private <T> Optional<T> find(Supplier<T> query, String missingCode) {
        try {
            return Optional.of(Objects.requireNonNull(query.get()));
        } catch (ServiceException error) {
            if (error.getHttpStatusCode() == 404 && missingCode.equals(error.getErrorCode())) {
                return Optional.empty();
            }
            throw new ChannelFailure();
        } catch (RuntimeException error) {
            throw new ChannelFailure();
        }
    }

    /** 对外报告稳定失败, 不附带 SDK 的请求、签名或响应正文. */
    private <T> T call(Supplier<T> operation) {
        try {
            return operation.get();
        } catch (RuntimeException error) {
            throw new ChannelFailure();
        }
    }
}
