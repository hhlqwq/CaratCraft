package com.platform.modules.mall.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.platform.modules.mall.entity.MallOrderEntity;
import com.platform.modules.wx.entity.WxUserEntity;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 小程序预支付创建, 使用所属应用的openid并与改价/取消共享行锁. */
@Service
public class OrderPrepaymentService {
  private final MallOrderService orders;
  private final ObjectProvider<WxPayService> gateway;
  private final String miniappId;
  private final String notifyUrl;

  /** 注入可选支付网关, 未配置支付时允许应用启动但拒绝预支付. */
  public OrderPrepaymentService(MallOrderService orders, ObjectProvider<WxPayService> gateway,
      @Value("${wx.miniapp.appid:}") String miniappId,
      @Value("${wx.pay.baseNotifyUrl:}") String notifyUrl) {
    this.orders = orders;
    this.gateway = gateway;
    this.miniappId = miniappId;
    this.notifyUrl = notifyUrl;
  }

  /** 锁定待付款订单后创建微信预支付, 不将预支付等同于支付完成. */
  @Transactional(rollbackFor = Exception.class)
  public WxPayMpOrderResult prepare(Integer orderId, WxUserEntity user, String clientIp) {
    MallOrderEntity order = orders.lockById(orderId);
    if (order == null || !user.getId().equals(order.getUserId())) {
      throw new IllegalArgumentException("订单不存在或无权访问");
    }
    if (!Integer.valueOf(0).equals(order.getOrderStatus())
        || !(Integer.valueOf(0).equals(order.getPayStatus()) || Integer.valueOf(1).equals(order.getPayStatus()))) {
      throw new IllegalArgumentException("当前订单状态不允许预支付");
    }
    WxPayService payment = gateway.getIfAvailable();
    if (payment == null || miniappId.isBlank()) {
      throw new IllegalStateException("微信支付或小程序应用未配置");
    }
    if (!miniappId.equals(payment.getConfig().getAppId())
        || user.getMaOpenid() == null || user.getMaOpenid().isBlank()) {
      throw new IllegalArgumentException("支付应用与小程序登录信息不匹配, 请重新登录或联系商家");
    }
    int cents = order.getActualPrice().movePointRight(2).intValueExact();
    if (cents <= 0) {
      throw new IllegalArgumentException("订单金额必须大于零");
    }
    WxPayUnifiedOrderRequest request = new WxPayUnifiedOrderRequest();
    request.setBody("克拉匠造-" + order.getOrderSn());
    request.setOutTradeNo(order.getOrderSn());
    request.setTotalFee(cents);
    request.setTradeType("JSAPI");
    request.setOpenid(user.getMaOpenid());
    request.setSpbillCreateIp(clientIp);
    request.setNotifyUrl(notifyUrl);
    try {
      WxPayMpOrderResult result = payment.createOrder(request);
      if (!orders.update(new UpdateWrapper<MallOrderEntity>().eq("id", orderId)
          .eq("order_status", 0).in("pay_status", 0, 1).eq("actual_price", order.getActualPrice())
          .set("pay_id", result.getPackageValue()).set("pay_status", 1))) {
        throw new IllegalStateException("订单状态已变化, 请重新查询支付结果");
      }
      return result;
    } catch (WxPayException error) {
      throw new IllegalStateException("微信预支付创建失败, 请稍后重试");
    }
  }
}
