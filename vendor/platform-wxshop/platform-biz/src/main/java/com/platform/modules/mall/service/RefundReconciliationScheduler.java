package com.platform.modules.mall.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/** 每分钟补偿已审核退款, 每批有上限且数据库租约支持多实例. */
@Configuration
@EnableScheduling
@ConditionalOnProperty(
    name = "caratcraft.refund-reconciliation-enabled",
    havingValue = "true",
    matchIfMissing = true)
public class RefundReconciliationScheduler {
  private static final Logger LOG = LoggerFactory.getLogger(RefundReconciliationScheduler.class);
  private final OrderRefundService refunds;

  /** 注入带持久化租约的对账服务. */
  public RefundReconciliationScheduler(OrderRefundService refunds) {
    this.refunds = refunds;
  }

  /** 不扫描无退款的订单, 失败等待下一轮, 仅记录关键阶段数量. */
  @Scheduled(initialDelay = 60000, fixedDelay = 60000)
  public void reconcile() {
    try {
      int count = refunds.reconcile();
      if (count > 0) LOG.info("退款对账批次完成, 处理申请数: {}", count);
    } catch (Exception failure) {
      LOG.warn("退款对账暂未完成, 请检查迁移及支付配置, 下一轮重试");
    }
  }
}
