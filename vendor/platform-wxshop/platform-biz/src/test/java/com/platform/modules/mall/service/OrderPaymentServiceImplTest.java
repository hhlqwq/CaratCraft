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
        when(orderService.getById(10)).thenReturn(order);

        assertThrows(IllegalArgumentException.class, () -> service.markPaid(10, 19899));
        verify(orderService, never()).update(order);
        verify(customDesignService, never()).markOrderPaid(10);
    }

    /** 验证合法支付更新订单并触发工单 / Persists a valid payment and triggers work order creation. */
    @Test
    void markPaidPersistsOrderAndCustomization() {
        MallOrderEntity order = unpaidOrder();
        when(orderService.getById(10)).thenReturn(order);

        service.markPaid(10, 19900);

        assertEquals(2, order.getPayStatus());
        assertEquals(201, order.getOrderStatus());
        verify(orderService).update(order);
        verify(customDesignService).markOrderPaid(10);
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
