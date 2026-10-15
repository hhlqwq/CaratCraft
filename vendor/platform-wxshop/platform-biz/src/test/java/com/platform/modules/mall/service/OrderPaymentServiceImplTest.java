package com.platform.modules.mall.service;

import com.platform.modules.custom.service.CustomDesignService;
import com.platform.modules.mall.entity.MallOrderEntity;
import com.platform.modules.mall.service.impl.OrderPaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 订单支付落库服务测试 / Order payment persistence service tests.
 */
class OrderPaymentServiceImplTest {
    private MallOrderService orderService;
    private CustomDesignService customDesignService;
    private OrderPaymentServiceImpl service;

    /** 初始化支付服务依赖 / Initializes payment service dependencies. */
    @BeforeEach
    void setUp() {
        orderService = mock(MallOrderService.class);
        customDesignService = mock(CustomDesignService.class);
        service = new OrderPaymentServiceImpl(orderService, customDesignService);
    }

    /** 验证支付金额不匹配时拒绝落库 / Rejects a mismatched payment amount. */
    @Test
    void markPaidRejectsMismatchedAmount() {
        MallOrderEntity order = unpaidOrder();
        when(orderService.lockById(10)).thenReturn(order);

        assertThrows(IllegalArgumentException.class, () -> service.markPaid(10, 19899));
        verify(orderService, never()).update(order);
        verify(customDesignService, never()).markOrderPaid(10);
    }

    /** 验证合法支付更新订单并触发工单 / Persists a valid payment and triggers work order creation. */
    @Test
    void markPaidPersistsOrderAndCustomization() {
        MallOrderEntity order = unpaidOrder();
        when(orderService.lockById(10)).thenReturn(order);

        when(orderService.update(any(MallOrderEntity.class), any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(true);
        service.markPaid(10, 19900);

        assertEquals(2, order.getPayStatus());
        assertEquals(201, order.getOrderStatus());
        verify(orderService).update(any(MallOrderEntity.class), any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
        verify(customDesignService).markOrderPaid(10);
    }

    /** 条件更新失败必须抛出异常, 不得生成工单. */
    @Test
    void failedPaymentUpdateDoesNotCreateWorkOrder() {
        when(orderService.lockById(10)).thenReturn(unpaidOrder());
        assertThrows(IllegalStateException.class, () -> service.markPaid(10, 19900));
        verify(customDesignService, never()).markOrderPaid(10);
    }

    /** 缺少支付平台确认的金额时不能将订单标记已支付. */
    @Test
    void missingPaidAmountIsRejected() {
        when(orderService.lockById(10)).thenReturn(unpaidOrder());
        assertThrows(IllegalArgumentException.class, () -> service.markPaid(10, null));
        verify(customDesignService, never()).markOrderPaid(10);
    }

    /** 创建待支付订单 / Creates an unpaid order. */
    private MallOrderEntity unpaidOrder() {
        MallOrderEntity order = new MallOrderEntity();
        order.setId(10);
        order.setActualPrice(new BigDecimal("199.00"));
        order.setPayStatus(0);
        order.setOrderStatus(0);
        return order;
    }
}
