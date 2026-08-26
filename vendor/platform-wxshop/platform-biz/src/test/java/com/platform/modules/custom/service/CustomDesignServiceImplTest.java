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
        CustomOrderCustomizationEntity binding = new CustomOrderCustomizationEntity();
        binding.setOrderId(100);
        binding.setOrderGoodsId(101);
        binding.setSnapshotId(88L);
        when(bindingDao.selectOne(any())).thenReturn(binding);
        when(workOrderDao.selectCount(any())).thenReturn(0L, 1L);
        CustomDesignSnapshotEntity snapshot = new CustomDesignSnapshotEntity();
        snapshot.setConfigurationJson("{\"goodsId\":10}");
        when(snapshotDao.selectById(88L)).thenReturn(snapshot);

        service.markOrderPaid(100);
        service.markOrderPaid(100);

        verify(workOrderDao, times(1)).insert(any(ProductionWorkOrderEntity.class));
        assertEquals("PAID", binding.getStatus());
    }
}
