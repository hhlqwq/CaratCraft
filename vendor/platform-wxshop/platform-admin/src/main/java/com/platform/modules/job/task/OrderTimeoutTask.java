package com.platform.modules.job.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.modules.mall.entity.MallOrderEntity;
import com.platform.modules.mall.service.MallOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 订单超时自动关闭任务 / Auto-close expired unpaid orders task.
 * <p>
 * Bean 名称为 orderTimeoutTask，在定时任务管理页面配置。
 * 建议 cron: 0 0/5 星号星号 星号？(即每5分钟执行一次, 表达式见 base.sql 种子数据)
 * </p>
 */
@Slf4j
@Component("orderTimeoutTask")
@RequiredArgsConstructor
public class OrderTimeoutTask {
    private final MallOrderService orderService;

    /** 默认超时时间（小时）/ Default timeout in hours. */
    private static final int DEFAULT_TIMEOUT_HOURS = 2;

    /**
     * 关闭超时未支付的订单 / Closes unpaid orders that exceeded the timeout.
     */
    @SuppressWarnings("unused")
    @Transactional(rollbackFor = Exception.class)
    public void closeExpiredOrders() {
        log.info("开始执行订单超时关闭任务");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -DEFAULT_TIMEOUT_HOURS);
        Date expireTime = calendar.getTime();

        List<MallOrderEntity> expiredOrders = orderService.list(
                new LambdaQueryWrapper<MallOrderEntity>()
                        .eq(MallOrderEntity::getOrderStatus, 0)
                        .eq(MallOrderEntity::getPayStatus, 0)
                        .lt(MallOrderEntity::getAddTime, expireTime));

        int closedCount = 0;
        for (MallOrderEntity order : expiredOrders) {
            try {
                orderService.cancelUnpaid(order.getId(), order.getUserId());
                closedCount++;
            } catch (Exception e) {
                log.warn("关闭超时订单失败，订单ID：{}，原因：{}", order.getId(), e.getMessage());
            }
        }
        log.info("订单超时关闭任务完成，共关闭 {} 个订单", closedCount);
    }
}
