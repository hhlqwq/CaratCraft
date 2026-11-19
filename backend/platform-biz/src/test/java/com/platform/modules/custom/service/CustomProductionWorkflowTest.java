package com.platform.modules.custom.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.platform.modules.custom.dao.ProductionWorkOrderDao;
import com.platform.modules.custom.dao.WorkOrderLogDao;
import com.platform.modules.custom.entity.ProductionWorkOrderEntity;
import com.platform.modules.custom.entity.WorkOrderLogEntity;
import com.platform.modules.mall.entity.MallOrderEntity;
import com.platform.modules.mall.service.MallOrderService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** 生产状态变更必须服从订单状态、合法流转和审计落库约束. */
class CustomProductionWorkflowTest {
    private MallOrderService orders;
    private ProductionWorkOrderDao workOrders;
    private WorkOrderLogDao logs;
    private ProductionWorkflowService service;
    private MallOrderEntity order;
    private ProductionWorkOrderEntity work;

    /** 列表动作与实际流转共享状态图, 退款冻结后立即隐藏制作入口. */
    @Test
    void allowedActionsFollowOrderFreezeAndSharedGraph() {
        order.setId(10);
        when(orders.listByIds(any())).thenReturn(java.util.List.of(order));
        service.describeAllowedActions(java.util.List.of(work));
        org.junit.jupiter.api.Assertions.assertEquals(
                java.util.List.of("REVIEWED"), work.getAllowedActions());
        order.setPayStatus(3);
        service.describeAllowedActions(java.util.List.of(work));
        org.junit.jupiter.api.Assertions.assertTrue(work.getAllowedActions().isEmpty());
    }

    /** 初始化已支付待履约订单和待审核工单. */
    @BeforeEach
    void setUp() {
        orders = mock(MallOrderService.class);
        workOrders = mock(ProductionWorkOrderDao.class);
        logs = mock(WorkOrderLogDao.class);
        service = new ProductionWorkflowService(orders, workOrders, logs);
        work = new ProductionWorkOrderEntity();
        work.setId(1L);
        work.setOrderId(10);
        work.setStatus("PENDING_REVIEW");
        when(workOrders.selectById(1L)).thenReturn(work);
        when(workOrders.selectOne(any())).thenReturn(work);
        order = new MallOrderEntity();
        order.setPayStatus(2);
        order.setOrderStatus(201);
        when(orders.lockById(10)).thenReturn(order);
    }

    /** 已进入退款流程的订单不能继续生产. */
    @Test
    void refundBlocksProduction() {
        order.setPayStatus(3);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.transition(1L, "REVIEWED", "审核通过", 7L, "测试商家"));
        verify(workOrders, never()).update(any(), notNull());
    }

    /** 跳过审核开工或直接取消工单都应拒绝. */
    @Test
    void invalidJumpAndDirectCancellationAreRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.transition(1L, "PRODUCING", "跳步", 7L, "测试商家"));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.transition(1L, "CANCELLED", "直接取消", 7L, "测试商家"));
    }

    /** 状态条件更新失败时不得写入操作成功日志. */
    @Test
    void concurrentStateChangeDoesNotWriteLog() {
        assertThrows(
                IllegalStateException.class,
                () -> service.transition(1L, "REVIEWED", "审核通过", 7L, "测试商家"));
        verify(logs, never()).insert(any(WorkOrderLogEntity.class));
    }

    /** 状态已更新但日志失败时抛出异常, 事务不得提交. */
    @Test
    void failedLogPreventsSuccessfulTransition() {
        when(workOrders.update(any(), notNull())).thenReturn(1);
        assertThrows(
                IllegalStateException.class,
                () -> service.transition(1L, "REVIEWED", "审核通过", 7L, "测试商家"));
    }
}
