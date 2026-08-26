package com.platform.modules.custom.service;

import com.platform.modules.custom.dao.CustomDesignSnapshotDao;
import com.platform.modules.custom.dao.CustomOrderCustomizationDao;
import com.platform.modules.custom.dao.ProductionWorkOrderDao;
import com.platform.modules.custom.dto.CustomQuoteRequest;
import com.platform.modules.custom.entity.CustomDesignSnapshotEntity;
import com.platform.modules.custom.entity.CustomOrderCustomizationEntity;
import com.platform.modules.custom.entity.ProductionWorkOrderEntity;
import com.platform.modules.custom.service.impl.CustomDesignServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import com.baomidou.mybatisplus.core.conditions.Wrapper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 定制设计流程测试 / Custom design workflow tests.
 */
class CustomDesignServiceImplTest {
    private CustomDesignSnapshotDao snapshotDao;
    private CustomOrderCustomizationDao bindingDao;
    private ProductionWorkOrderDao workOrderDao;
    private CustomQuoteService quoteService;
    private CustomDesignServiceImpl service;

    /** 初始化设计服务依赖 / Initializes design service dependencies. */
    @BeforeEach
    void setUp() {
        snapshotDao = mock(CustomDesignSnapshotDao.class);
        bindingDao = mock(CustomOrderCustomizationDao.class);
        workOrderDao = mock(ProductionWorkOrderDao.class);
        quoteService = mock(CustomQuoteService.class);
        service = new CustomDesignServiceImpl(snapshotDao, bindingDao, workOrderDao, quoteService);
    }

    /** 验证设计保存、锁价和订单绑定 / Verifies save, price lock, and order binding. */
    @Test
    void designCanBeSavedLockedAndBound() {
        CustomQuoteRequest request = new CustomQuoteRequest();
        request.setGoodsId(10);
        request.setOptionIds(Collections.singletonList(1L));
        Map<String, Object> quote = new LinkedHashMap<>();
        quote.put("totalPrice", new BigDecimal("219.00"));
        when(quoteService.quote(any())).thenReturn(quote);
        org.mockito.Mockito.doAnswer(invocation -> {
            CustomDesignSnapshotEntity snapshot = invocation.getArgument(0);
            snapshot.setId(88L);
            return 1;
        }).when(snapshotDao).insert(any(CustomDesignSnapshotEntity.class));

        Map<String, Object> saved = service.saveDraft(7L, request);
        assertEquals(88L, saved.get("snapshotId"));

        ArgumentCaptor<CustomDesignSnapshotEntity> snapshotCaptor =
                ArgumentCaptor.forClass(CustomDesignSnapshotEntity.class);
        verify(snapshotDao).insert(snapshotCaptor.capture());
        CustomDesignSnapshotEntity snapshot = snapshotCaptor.getValue();
        when(snapshotDao.selectById(88L)).thenReturn(snapshot);
        when(snapshotDao.update(any(), any(Wrapper.class))).thenReturn(1);

        assertEquals(new BigDecimal("219.00"), service.lockAndGetPrice(88L, 7L));
        snapshot.setStatus("LOCKED");
        service.bindOrder(88L, 7L, 100, 101);
        verify(bindingDao).insert(any(CustomOrderCustomizationEntity.class));
        assertEquals("ORDERED", snapshot.getStatus());
    }

    /** 验证支付成功只生成一次工单 / Verifies idempotent work order creation. */
    @Test
    void paidOrderCreatesOneWorkOrder() {
        CustomOrderCustomizationEntity binding = unpaidBinding();
        when(bindingDao.selectOne(any())).thenReturn(binding);
        when(bindingDao.update(any(), any(Wrapper.class))).thenReturn(1, 0);
        when(workOrderDao.selectCount(any())).thenReturn(0L, 1L);
        CustomDesignSnapshotEntity snapshot = new CustomDesignSnapshotEntity();
        snapshot.setConfigurationJson("{\"goodsId\":10}");
        when(snapshotDao.selectById(88L)).thenReturn(snapshot);

        service.markOrderPaid(100);
        service.markOrderPaid(100);

        verify(workOrderDao, times(1)).insert(any(ProductionWorkOrderEntity.class));
    }

    /** 验证已取消绑定不会被迟到支付回调复活 / Verifies cancelled bindings stay cancelled. */
    @Test
    void cancelledBindingIsNotRevivedByLateCallback() {
        when(bindingDao.selectOne(any())).thenReturn(unpaidBinding());
        // 条件更新未命中说明绑定已不是 UNPAID / Miss means the binding is no longer UNPAID.
        when(bindingDao.update(any(), any(Wrapper.class))).thenReturn(0);

        service.markOrderPaid(100);

        verify(workOrderDao, times(0)).insert(any(ProductionWorkOrderEntity.class));
    }

    /** 验证未支付取消后设计恢复为草稿 / Verifies unpaid cancellation releases the design. */
    @Test
    void cancelUnpaidRestoresSnapshotToDraft() {
        CustomOrderCustomizationEntity binding = unpaidBinding();
        when(bindingDao.selectOne(any())).thenReturn(binding);
        when(bindingDao.update(any(), any(Wrapper.class))).thenReturn(1);
        CustomDesignSnapshotEntity snapshot = new CustomDesignSnapshotEntity();
        snapshot.setId(88L);
        snapshot.setStatus("ORDERED");
        when(snapshotDao.selectById(88L)).thenReturn(snapshot);
        when(workOrderDao.selectOne(any())).thenReturn(null);

        service.cancelOrder(100);

        assertEquals("DRAFT", snapshot.getStatus());
        verify(snapshotDao).updateById(snapshot);
    }

    /** 验证已支付退款取消不释放设计 / Verifies paid refunds do not release the design. */
    @Test
    void cancelPaidKeepsSnapshotOrdered() {
        CustomOrderCustomizationEntity binding = unpaidBinding();
        binding.setStatus("PAID");
        when(bindingDao.selectOne(any())).thenReturn(binding);
        CustomDesignSnapshotEntity snapshot = new CustomDesignSnapshotEntity();
        snapshot.setId(88L);
        snapshot.setStatus("ORDERED");
        when(snapshotDao.selectById(88L)).thenReturn(snapshot);
        when(workOrderDao.selectOne(any())).thenReturn(null);

        service.cancelOrder(100);

        assertEquals("ORDERED", snapshot.getStatus());
        verify(snapshotDao, times(0)).updateById(snapshot);
        org.mockito.Mockito.verify(bindingDao).updateById(binding);
    }

    /** 创建未支付定制绑定 / Creates an unpaid customization binding. */
    private CustomOrderCustomizationEntity unpaidBinding() {
        CustomOrderCustomizationEntity binding = new CustomOrderCustomizationEntity();
        binding.setId(1L);
        binding.setOrderId(100);
        binding.setOrderGoodsId(101);
        binding.setSnapshotId(88L);
        binding.setStatus("UNPAID");
        return binding;
    }
}
