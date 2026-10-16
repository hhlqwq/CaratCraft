package com.platform.modules.custom.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.platform.modules.custom.dao.ProductionWorkOrderDao;
import com.platform.modules.custom.dao.WorkOrderLogDao;
import com.platform.modules.custom.entity.ProductionWorkOrderEntity;
import com.platform.modules.custom.entity.WorkOrderLogEntity;
import com.platform.modules.mall.entity.MallOrderEntity;
import com.platform.modules.mall.service.MallOrderService;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 生产工单流转服务, 状态和审计日志必须在同一事务内提交. */
@Service
public class ProductionWorkflowService {
  public static final Map<String, List<String>> TRANSITIONS =
      Map.of(
          "PENDING_REVIEW",
          List.of("REVIEWED"),
          "REVIEWED",
          List.of("PRODUCING"),
          "PRODUCING",
          List.of("QUALITY_CHECK"),
          "QUALITY_CHECK",
          List.of("COMPLETED"),
          "COMPLETED",
          List.of(),
          "CANCELLED",
          List.of());
  private final MallOrderService orders;
  private final ProductionWorkOrderDao workOrders;
  private final WorkOrderLogDao logs;

  /** 注入共享订单锁及工单、日志访问接口. */
  public ProductionWorkflowService(
      MallOrderService orders, ProductionWorkOrderDao workOrders, WorkOrderLogDao logs) {
    this.orders = orders;
    this.workOrders = workOrders;
    this.logs = logs;
  }

  /** 批量读取订单履约状态, 使用服务端共享状态图描述可执行动作, 避免逐行查询. */
  public void describeAllowedActions(List<ProductionWorkOrderEntity> rows) {
    if (rows.isEmpty()) return;
    Map<Integer, MallOrderEntity> byOrder = new java.util.HashMap<>();
    for (MallOrderEntity order :
        orders.listByIds(
            rows.stream().map(ProductionWorkOrderEntity::getOrderId).distinct().toList())) {
      byOrder.put(order.getId(), order);
    }
    for (ProductionWorkOrderEntity row : rows) {
      MallOrderEntity order = byOrder.get(row.getOrderId());
      row.setAllowedActions(
          order != null
                  && Integer.valueOf(2).equals(order.getPayStatus())
                  && Integer.valueOf(201).equals(order.getOrderStatus())
              ? TRANSITIONS.getOrDefault(row.getStatus(), List.of())
              : List.of());
    }
  }

  /** 先锁订单再锁工单, 拒绝与退款、取消并发的生产操作. */
  @Transactional(rollbackFor = Exception.class)
  public void transition(
      Long id, String target, String remark, Long operatorId, String operatorName) {
    if (id == null || target == null || target.isBlank()) {
      throw new IllegalArgumentException("工单和目标状态不能为空");
    }
    ProductionWorkOrderEntity reference = workOrders.selectById(id);
    if (reference == null) {
      throw new IllegalArgumentException("工单不存在");
    }
    MallOrderEntity order = orders.lockById(reference.getOrderId());
    if (order == null
        || !Integer.valueOf(2).equals(order.getPayStatus())
        || !Integer.valueOf(201).equals(order.getOrderStatus())) {
      throw new IllegalArgumentException("订单已取消、退款中或不处于待履约状态");
    }
    ProductionWorkOrderEntity work =
        workOrders.selectOne(
            new LambdaQueryWrapper<ProductionWorkOrderEntity>()
                .eq(ProductionWorkOrderEntity::getId, id)
                .last("FOR UPDATE"));
    if (work == null || !TRANSITIONS.getOrDefault(work.getStatus(), List.of()).contains(target)) {
      throw new IllegalArgumentException("工单状态已变化或不允许该操作, 请刷新");
    }
    if (remark == null || remark.isBlank() || remark.length() > 500) {
      throw new IllegalArgumentException("请填写1到500字的审核、制作或质检备注");
    }
    if (workOrders.update(
            null,
            new UpdateWrapper<ProductionWorkOrderEntity>()
                .eq("id", id)
                .eq("status", work.getStatus())
                .set("status", target)
                .set("update_time", new Date()))
        != 1) {
      throw new IllegalStateException("工单发生并发变更, 请重试");
    }
    WorkOrderLogEntity log = new WorkOrderLogEntity();
    log.setWorkOrderId(id);
    log.setOperatorId(operatorId);
    log.setOperatorName(operatorName);
    log.setFromStatus(work.getStatus());
    log.setToStatus(target);
    log.setRemark(remark.trim());
    log.setCreateTime(new Date());
    if (logs.insert(log) != 1) {
      throw new IllegalStateException("工单日志保存失败, 状态变更回滚");
    }
  }
}
