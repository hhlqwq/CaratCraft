package com.platform.modules.job.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.modules.mall.entity.MallOrderEntity;
import com.platform.modules.mall.service.MallOrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 订单超时自动关闭任务 / Auto-close expired unpaid orders task.
 *
 * <p>Bean 名称为 orderTimeoutTask，在定时任务管理页面配置。 建议 cron: 0 0/5 星号星号 星号？(即每5分钟执行一次, 表达式见 base.sql 种子数据)
 */
@Slf4j
@Component("orderTimeoutTask")
@RequiredArgsConstructor
public class OrderTimeoutTask {
    private final MallOrderService orderService;

    /** 默认超时时间（小时）/ Default timeout in hours. */
    private static final int DEFAULT_TIMEOUT_HOURS = 2;

    /** 关闭超时未支付的订单 / Closes unpaid orders that exceeded the timeout. */
    @SuppressWarnings("unused")
    public void closeExpiredOrders() {
        log.info("开始执行订单超时关闭任务");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -DEFAULT_TIMEOUT_HOURS);
        Date expireTime = calendar.getTime();

        int closedCount = 0;
        int cursor = 0;
        // 每批100条且最多20批, 失败订单也推进游标, 防止一次任务无限扫描.
        for (int batch = 1; batch <= 20; batch++) {
            List<MallOrderEntity> expiredOrders =
                    orderService.list(
                            new LambdaQueryWrapper<MallOrderEntity>()
                                    .gt(MallOrderEntity::getId, cursor)
                                    .eq(MallOrderEntity::getOrderStatus, 0)
                                    .in(MallOrderEntity::getPayStatus, 0, 1)
                                    .lt(MallOrderEntity::getAddTime, expireTime)
                                    .orderByAsc(MallOrderEntity::getId)
                                    .last("LIMIT 100"));
            if (expiredOrders.isEmpty()) {
                break;
            }
            for (MallOrderEntity order : expiredOrders) {
                cursor = order.getId();
                try {
                    if (orderService.cancelUnpaid(order.getId(), order.getUserId())) {
                        closedCount++;
                    }
                } catch (Exception e) {
                    log.warn("关闭超时订单失败, 订单ID: {}, 下次任务重试", order.getId());
                }
            }
            log.info("订单超时关闭进度: 批次{}, 游标{}, 已关闭{}", batch, cursor, closedCount);
            if (expiredOrders.size() < 100) {
                break;
            }
        }
        log.info("订单超时关闭任务完成，共关闭 {} 个订单", closedCount);
    }
}
