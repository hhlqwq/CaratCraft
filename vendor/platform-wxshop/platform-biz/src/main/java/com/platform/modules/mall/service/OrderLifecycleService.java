package com.platform.modules.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.platform.modules.custom.dao.CustomOrderCustomizationDao;
import com.platform.modules.custom.dao.ProductionWorkOrderDao;
import com.platform.modules.custom.entity.CustomOrderCustomizationEntity;
import com.platform.modules.custom.entity.ProductionWorkOrderEntity;
import com.platform.modules.mall.dao.OrderPriceLogDao;
import com.platform.modules.mall.entity.MallOrderEntity;
import java.math.BigDecimal;
import java.util.Date;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 订单履约和改价统一状态边界, 与支付/退款共享订单行锁. */
@Service
public class OrderLifecycleService {
  private final MallOrderService orders;
  private final CustomOrderCustomizationDao customizations;
  private final ProductionWorkOrderDao workOrders;
  private final OrderPriceLogDao priceLogs;

  /** 注入订单、定制绑定、工单和改价审计访问接口. */
  public OrderLifecycleService(MallOrderService orders, CustomOrderCustomizationDao customizations,
      ProductionWorkOrderDao workOrders, OrderPriceLogDao priceLogs) {
    this.orders = orders;
    this.customizations = customizations;
    this.workOrders = workOrders;
    this.priceLogs = priceLogs;
  }

  /** 只允许当前用户确认已付款且已发货的订单, 重复确认幂等返回. */
  @Transactional(rollbackFor = Exception.class)
  public void confirmReceipt(Integer orderId, Long userId) {
    MallOrderEntity order = requireOrder(orderId);
    if (!userId.equals(order.getUserId())) {
      throw new IllegalArgumentException("订单不存在或无权访问");
    }
    if (Integer.valueOf(301).equals(order.getOrderStatus())
        && Integer.valueOf(2).equals(order.getPayStatus())
        && Integer.valueOf(2).equals(order.getShippingStatus())) {
      return;
    }
    if (!Integer.valueOf(300).equals(order.getOrderStatus())
        || !Integer.valueOf(2).equals(order.getPayStatus())
        || !Integer.valueOf(1).equals(order.getShippingStatus())) {
      throw new IllegalArgumentException("仅已支付并已发货订单可确认收货");
    }
    requireUpdate(new UpdateWrapper<MallOrderEntity>().eq("id", orderId)
        .eq("order_status", 300).eq("pay_status", 2).eq("shipping_status", 1)
        .set("order_status", 301).set("shipping_status", 2).set("confirm_time", new Date()));
  }

  /** 定制工单完成后才允许发货, 普通商品同样须处于待发货状态. */
  @Transactional(rollbackFor = Exception.class)
  public void ship(Integer orderId, String company, String tracking) {
    if (company == null || company.isBlank() || company.length() > 100
        || tracking == null || tracking.isBlank() || tracking.length() > 32) {
      throw new IllegalArgumentException("请填写有效的快递公司和物流单号");
    }
    MallOrderEntity order = requireOrder(orderId);
    if (!Integer.valueOf(201).equals(order.getOrderStatus())
        || !Integer.valueOf(2).equals(order.getPayStatus())
        || !Integer.valueOf(0).equals(order.getShippingStatus())) {
      throw new IllegalArgumentException("仅已支付且待发货订单可发货");
    }
    Long customCount = customizations.selectCount(new LambdaQueryWrapper<CustomOrderCustomizationEntity>()
        .eq(CustomOrderCustomizationEntity::getOrderId, orderId));
    if (customCount > 0) {
      ProductionWorkOrderEntity work = workOrders.selectOne(
          new LambdaQueryWrapper<ProductionWorkOrderEntity>()
              .eq(ProductionWorkOrderEntity::getOrderId, orderId).last("FOR UPDATE"));
      if (work == null || !"COMPLETED".equals(work.getStatus())) {
        throw new IllegalArgumentException("定制工单完成质检后才可发货");
      }
    }
    requireUpdate(new UpdateWrapper<MallOrderEntity>().eq("id", orderId)
        .eq("order_status", 201).eq("pay_status", 2).eq("shipping_status", 0)
        .set("shipping_status", 1).set("order_status", 300)
        .set("shipping_name", company.trim()).set("shipping_no", tracking.trim()));
  }

  /** 修改待付款实付金额并记录差额, 原商品价和优惠券账面金额保持可追溯. */
  @Transactional(rollbackFor = Exception.class)
  public void adjustPrice(Integer orderId, BigDecimal price, String operator, String reason) {
    if (price == null || price.scale() > 2 || price.compareTo(new BigDecimal("0.01")) < 0
        || price.compareTo(new BigDecimal("9999999.99")) > 0
        || reason == null || reason.isBlank() || reason.length() > 200) {
      throw new IllegalArgumentException("金额须为0.01到9999999.99且最多两位小数, 请填写改价理由");
    }
    MallOrderEntity order = requireOrder(orderId);
    if (!Integer.valueOf(0).equals(order.getOrderStatus())
        || !Integer.valueOf(0).equals(order.getPayStatus())
        || (order.getPayId() != null && !order.getPayId().isBlank() && !"0".equals(order.getPayId()))) {
      throw new IllegalArgumentException("仅尚未预支付的待付款订单可改价");
    }
    BigDecimal old = order.getActualPrice();
    requireUpdate(new UpdateWrapper<MallOrderEntity>().eq("id", orderId)
        .eq("order_status", 0).eq("pay_status", 0).eq("actual_price", old)
        .set("actual_price", price));
    if (priceLogs.insert(orderId, old, price, price.subtract(old), operator, reason.trim()) != 1) {
      throw new IllegalStateException("改价流水保存失败, 本次改价回滚");
    }
  }

  /** 加锁读取订单并检查存在性, 统一保持先订单后工单的加锁顺序. */
  private MallOrderEntity requireOrder(Integer id) {
    MallOrderEntity order = orders.lockById(id);
    if (order == null) {
      throw new IllegalArgumentException("订单不存在");
    }
    return order;
  }

  /** 条件更新未命中视为并发冲突, 不向调用方报告假成功. */
  private void requireUpdate(UpdateWrapper<MallOrderEntity> change) {
    if (!orders.update(change)) {
      throw new IllegalStateException("订单状态已变化, 请刷新重试");
    }
  }
}
