package com.platform.modules.mall.service;

/** 订单支付结果服务 / Order payment result service. */
public interface OrderPaymentService {
  /** 原子处理支付成功结果 / Atomically handles a successful payment. */
  void markPaid(Integer orderId, Integer paidAmountInCents);
}
