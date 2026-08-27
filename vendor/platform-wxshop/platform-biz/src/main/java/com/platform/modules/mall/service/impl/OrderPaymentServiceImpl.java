package com.platform.modules.mall.service.impl;

import com.platform.modules.custom.service.CustomDesignService;
import com.platform.modules.mall.entity.MallOrderEntity;
import com.platform.modules.mall.service.MallOrderService;
import com.platform.modules.mall.service.OrderPaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.Date;

/**
 * 订单支付结果服务实现 / Order payment result service implementation.
 */
@Service
public class OrderPaymentServiceImpl implements OrderPaymentService {
    private final MallOrderService orderService;
    private final CustomDesignService customDesignService;

    public OrderPaymentServiceImpl(MallOrderService orderService,
                                   CustomDesignService customDesignService) {
        this.orderService = orderService;
        this.customDesignService = customDesignService;
    }

    /** 在同一事务中更新订单并生成生产工单 / Updates order and work order in one transaction. */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markPaid(Integer orderId, Integer paidAmountInCents) {
        MallOrderEntity order = orderService.getById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("支付订单不存在");
        }
        int expectedAmount = order.getActualPrice().movePointRight(2)
                .setScale(0, RoundingMode.HAL_UP).intValueExact();
        if (paidAmountInCents != null && expectedAmount != paidAmountInCents) {
            throw new IllegalArgumentException("支付金额与订单金额不一致");
        }
        if (!Integer.valueOf(2).equals(order.getPayStatus())) {
            if (!Integer.valueOf(0).equals(order.getOrderStatus())) {
                throw new IllegalArgumentException("订单状态不允许支付");
            }
            order.setPayStatus(2);
            order.setOrderStatus(201);
            order.setShippingStatus(0);
            order.setPayTime(new Date());
            orderService.update(order);
        }
        customDesignService.markOrderPaid(orderId);
    }
}
