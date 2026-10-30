package com.platform.modules.mall.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.platform.modules.custom.dao.CustomOrderCustomizationDao;
import com.platform.modules.custom.dao.ProductionWorkOrderDao;
import com.platform.modules.custom.entity.ProductionWorkOrderEntity;
import com.platform.modules.mall.dao.OrderPriceLogDao;
import com.platform.modules.mall.entity.MallOrderEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.math.BigDecimal;

/** 履约状态、用户归属和商家改价边界回归, 不调用外部支付接口. */
class OrderLifecycleServiceTest {
    private MallOrderService orders;
    private CustomOrderCustomizationDao bindings;
    private ProductionWorkOrderDao workOrders;
    private OrderPriceLogDao logs;
    private OrderLifecycleService service;
    private MallOrderEntity order;

    /** 初始化一张属于用户7的未支付订单. */
    @BeforeEach
    void setUp() {
        orders = mock(MallOrderService.class);
        bindings = mock(CustomOrderCustomizationDao.class);
        workOrders = mock(ProductionWorkOrderDao.class);
        logs = mock(OrderPriceLogDao.class);
        service = new OrderLifecycleService(orders, bindings, workOrders, logs);
        order = new MallOrderEntity();
        order.setId(10);
        order.setUserId(7L);
        order.setOrderStatus(0);
        order.setPayStatus(0);
        order.setShippingStatus(0);
        order.setActualPrice(new BigDecimal("199.00"));
        when(orders.lockById(10)).thenReturn(order);
    }

    /** 未支付、未发货和其他用户都不能确认收货. */
    @Test
    void receiptRejectsInvalidStateAndOwner() {
        assertThrows(IllegalArgumentException.class, () -> service.confirmReceipt(10, 7L));
        order.setOrderStatus(300);
        order.setPayStatus(2);
        order.setShippingStatus(1);
        assertThrows(IllegalArgumentException.class, () -> service.confirmReceipt(10, 8L));
        verify(orders, never()).update(ArgumentMatchers.<Wrapper<MallOrderEntity>>notNull());
    }

    /** 完成订单重复确认是幂等操作, 不产生重复更新. */
    @Test
    void receiptOfCompletedOrderIsIdempotent() {
        order.setOrderStatus(301);
        order.setPayStatus(2);
        order.setShippingStatus(2);
        service.confirmReceipt(10, 7L);
        verify(orders, never()).update(ArgumentMatchers.<Wrapper<MallOrderEntity>>notNull());
    }

    /** 定制工单缺失或未完成质检时禁止发货. */
    @Test
    void shipRequiresCompletedCustomizationWorkOrder() {
        order.setOrderStatus(201);
        order.setPayStatus(2);
        when(bindings.selectCount(any())).thenReturn(1L);
        assertThrows(IllegalArgumentException.class, () -> service.ship(10, "测试快递", "TEST123"));
        ProductionWorkOrderEntity work = new ProductionWorkOrderEntity();
        work.setStatus("PRODUCING");
        when(workOrders.selectOne(any())).thenReturn(work);
        assertThrows(IllegalArgumentException.class, () -> service.ship(10, "测试快递", "TEST123"));
        work.setStatus("COMPLETED");
        when(orders.update(ArgumentMatchers.<Wrapper<MallOrderEntity>>notNull())).thenReturn(true);
        service.ship(10, "测试快递", "TEST123");
        verify(orders).update(ArgumentMatchers.<Wrapper<MallOrderEntity>>notNull());
    }

    /** 零元、超精度和已预支付订单禁止改价. */
    @Test
    void priceRejectsZeroPrecisionAndPrepayment() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.adjustPrice(10, BigDecimal.ZERO, "operator", "测试"));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.adjustPrice(10, new BigDecimal("1.001"), "operator", "测试"));
        order.setPayStatus(1);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.adjustPrice(10, new BigDecimal("188.00"), "operator", "测试"));
    }

    /** 改价流水保存失败应抛出异常, 由事务统一回滚金额更新. */
    @Test
    void priceAuditFailureRollsBack() {
        when(orders.update(ArgumentMatchers.<Wrapper<MallOrderEntity>>notNull())).thenReturn(true);
        assertThrows(
                IllegalStateException.class,
                () -> service.adjustPrice(10, new BigDecimal("188.00"), "operator", "协商折扣"));
        verify(logs)
                .insert(
                        10,
                        new BigDecimal("199.00"),
                        new BigDecimal("188.00"),
                        new BigDecimal("-11.00"),
                        "operator",
                        "协商折扣");
    }
}
