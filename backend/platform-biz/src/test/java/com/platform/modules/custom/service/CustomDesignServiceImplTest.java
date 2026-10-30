package com.platform.modules.custom.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.platform.modules.custom.dao.ComponentStockDao;
import com.platform.modules.custom.dao.CustomComponentOptionDao;
import com.platform.modules.custom.dao.CustomDesignSnapshotDao;
import com.platform.modules.custom.dao.CustomOrderCustomizationDao;
import com.platform.modules.custom.dao.CustomProductDao;
import com.platform.modules.custom.dao.ProductionWorkOrderDao;
import com.platform.modules.custom.dto.CustomQuoteRequest;
import com.platform.modules.custom.entity.ComponentStockEntity;
import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import com.platform.modules.custom.entity.CustomDesignSnapshotEntity;
import com.platform.modules.custom.entity.CustomOrderCustomizationEntity;
import com.platform.modules.custom.entity.CustomProductEntity;
import com.platform.modules.custom.entity.ProductionWorkOrderEntity;
import com.platform.modules.custom.service.impl.CustomDesignServiceImpl;
import com.platform.modules.custom.service.impl.CustomQuoteServiceImpl;
import com.platform.modules.mall.entity.MallGoodsEntity;
import com.platform.modules.mall.service.MallGoodsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 定制设计流程测试 / Custom design workflow tests. */
class CustomDesignServiceImplTest {
    private CustomDesignSnapshotDao snapshotDao;
    private CustomOrderCustomizationDao bindingDao;
    private ProductionWorkOrderDao workOrderDao;
    private CustomQuoteService quoteService;
    private ComponentStockDao stockDao;
    private CustomDesignServiceImpl service;

    /** 初始化设计服务依赖 / Initializes design service dependencies. */
    @BeforeEach
    void setUp() {
        snapshotDao = mock(CustomDesignSnapshotDao.class);
        bindingDao = mock(CustomOrderCustomizationDao.class);
        workOrderDao = mock(ProductionWorkOrderDao.class);
        quoteService = mock(CustomQuoteService.class);
        stockDao = mock(ComponentStockDao.class);
        service =
                new CustomDesignServiceImpl(
                        snapshotDao, bindingDao, workOrderDao, quoteService, stockDao);
        when(stockDao.restoreStock(any(), any(), any(), anyInt())).thenReturn(1);
        when(stockDao.deductStock(any(), any(), any(), anyInt())).thenReturn(1);
        when(stockDao.logMovement(any(), any(), any(), anyInt(), any(), any())).thenReturn(1);
        when(snapshotDao.insert(any(CustomDesignSnapshotEntity.class))).thenReturn(1);
    }

    /** 验证设计保存、锁价和订单绑定 / Verifies save, price lock, and order binding. */
    @Test
    void designCanBeSavedLockedAndBound() {
        CustomQuoteRequest request = new CustomQuoteRequest();
        request.setGoodsId(10);
        request.setOptionIds(Collections.singletonList(1L));
        Map<String, Object> quote = new LinkedHashMap<>();
        quote.put("totalPrice", new BigDecimal("219.00"));
        quote.put(
                "components",
                List.of(
                        Map.of(
                                "componentType",
                                "pearl",
                                "optionCode",
                                "test-pearl",
                                "quantity",
                                3)));
        quote.put("checkoutReady", true);
        when(quoteService.quote(any())).thenReturn(quote);
        org.mockito.Mockito.doAnswer(
                        invocation -> {
                            CustomDesignSnapshotEntity snapshot = invocation.getArgument(0);
                            snapshot.setId(88L);
                            return 1;
                        })
                .when(snapshotDao)
                .insert(any(CustomDesignSnapshotEntity.class));

        Map<String, Object> saved = service.saveDraft(7L, request);
        assertEquals(88L, saved.get("snapshotId"));

        ArgumentCaptor<CustomDesignSnapshotEntity> snapshotCaptor =
                ArgumentCaptor.forClass(CustomDesignSnapshotEntity.class);
        verify(snapshotDao).insert(snapshotCaptor.capture());
        CustomDesignSnapshotEntity snapshot = snapshotCaptor.getValue();
        when(snapshotDao.selectById(88L)).thenReturn(snapshot);
        when(snapshotDao.update(any(), notNull())).thenReturn(1);

        assertEquals(new BigDecimal("219.00"), service.lockAndGetPrice(88L, 7L));
        snapshot.setStatus("LOCKED");
        service.bindOrder(88L, 7L, 100, 101);
        verify(bindingDao).insert(any(CustomOrderCustomizationEntity.class));
        assertEquals("ORDERED", snapshot.getStatus());
    }

    /** 排列订单扣减及退款恢复冻结累计用量, 工单保留逐颗顺序而不重读材料配置. */
    @Test
    void arrangementUsesFrozenAggregateForStockAndProduction() {
        var snapshot = new CustomDesignSnapshotEntity();
        snapshot.setId(88L);
        snapshot.setUserId(7L);
        snapshot.setGoodsId(10);
        snapshot.setStatus("LOCKED");
        String frozen =
                "{\"components\":[{\"componentType\":\"pearl\",\"optionCode\":\"akoya\",\"quantity\":6,\"instances\":3}],\"arrangement\":[{\"position\":0,\"name\":\"原珍珠\"},{\"position\":1,\"name\":\"原珍珠\"},{\"position\":2,\"name\":\"原珍珠\"}]}";
        snapshot.setPriceBreakdownJson(frozen);
        when(snapshotDao.selectById(88L)).thenReturn(snapshot);
        service.bindOrder(88L, 7L, 100, 101);
        verify(stockDao).deductStock(10, "pearl", "akoya", 6);
        var binding = unpaidBinding();
        when(bindingDao.selectOne(any())).thenReturn(binding);
        when(bindingDao.update(any(), notNull())).thenReturn(1);
        when(workOrderDao.selectCount(any())).thenReturn(0L);
        service.markOrderPaid(100);
        var work = ArgumentCaptor.forClass(ProductionWorkOrderEntity.class);
        verify(workOrderDao).insert(work.capture());
        assertEquals(frozen, work.getValue().getCraftJson());
        binding.setStatus("PAID");
        when(stockDao.restoreStock(10, "pearl", "akoya", 6)).thenReturn(1);
        service.cancelOrder(100);
        verify(stockDao).restoreStock(10, "pearl", "akoya", 6);
        verify(quoteService, times(0)).quote(any());
    }

    /** 验证支付成功只生成一次工单 / Verifies idempotent work order creation. */
    @Test
    void paidOrderCreatesOneWorkOrder() {
        CustomOrderCustomizationEntity binding = unpaidBinding();
        when(bindingDao.selectOne(any())).thenReturn(binding);
        when(bindingDao.update(any(), notNull())).thenReturn(1, 0);
        when(workOrderDao.selectCount(any())).thenReturn(0L, 1L);
        CustomDesignSnapshotEntity snapshot = new CustomDesignSnapshotEntity();
        snapshot.setConfigurationJson("{\"goodsId\":10}");
        when(snapshotDao.selectById(88L)).thenReturn(snapshot);

        service.markOrderPaid(100);
        service.markOrderPaid(100);

        verify(workOrderDao, times(1)).insert(any(ProductionWorkOrderEntity.class));
    }

    /** 验证报价、保存、锁价、下单绑定和支付工单完整链路 / Verifies the full custom order workflow. */
    @Test
    void quoteSaveLockBindAndPaymentCreatesWorkOrder() {
        MallGoodsService goodsService = mock(MallGoodsService.class);
        CustomComponentOptionDao optionDao = mock(CustomComponentOptionDao.class);
        CustomProductDao productDao = mock(CustomProductDao.class);
        MallGoodsEntity goods = new MallGoodsEntity();
        goods.setId(1999105);
        goods.setIsOnSale(1);
        goods.setRetailPrice(new BigDecimal("399.00"));
        when(goodsService.getById(1999105)).thenReturn(goods);
        CustomProductEntity product = new CustomProductEntity();
        product.setGoodsId(1999105);
        product.setEnabled(1);
        product.setRequiredComponentTypes("accessory,pearl");
        product.setLengthRules(
                "{\"mode\":\"SUM\",\"claspMm\":0,\"connectionMm\":0,\"lossMm\":0,"
                        + "\"toleranceMm\":1,\"minLengthMm\":300,\"maxLengthMm\":700}");
        when(productDao.selectOne(any())).thenReturn(product);
        CustomComponentOptionEntity accessory =
                workflowOption(1L, "accessory", "inline-charm", "0.00");
        accessory.setMaterialParams("{\"spanMm\":2,\"lengthRole\":\"INLINE\"}");
        CustomComponentOptionEntity pearl = workflowOption(2L, "pearl", "pearl-akoya", "660.00");
        pearl.setMaterialParams("{\"diameterMm\":10}");
        CustomComponentOptionEntity chain = workflowOption(3L, "chain", "chain-test", "0.00");
        chain.setMaterialParams("{\"lengthMm\":438}");
        when(optionDao.selectList(any())).thenReturn(Arrays.asList(accessory, pearl, chain));
        ComponentStockEntity stock = new ComponentStockEntity();
        stock.setStock(100);
        when(stockDao.selectOne(any())).thenReturn(stock);
        CustomQuoteServiceImpl realQuoteService =
                new CustomQuoteServiceImpl(goodsService, optionDao, productDao, stockDao);
        CustomDesignServiceImpl workflowService =
                new CustomDesignServiceImpl(
                        snapshotDao, bindingDao, workOrderDao, realQuoteService, stockDao);

        CustomQuoteRequest request = new CustomQuoteRequest();
        request.setGoodsId(1999105);
        request.setOptionIds(Arrays.asList(1L, 2L, 3L));
        request.setNeckCircumferenceMm(new BigDecimal("350"));
        request.setTargetLengthMm(new BigDecimal("450"));
        org.mockito.Mockito.doAnswer(
                        invocation -> {
                            CustomDesignSnapshotEntity savedSnapshot = invocation.getArgument(0);
                            savedSnapshot.setId(88L);
                            return 1;
                        })
                .when(snapshotDao)
                .insert(any(CustomDesignSnapshotEntity.class));

        Map<String, Object> saved = workflowService.saveDraft(7L, request);
        ArgumentCaptor<CustomDesignSnapshotEntity> snapshotCaptor =
                ArgumentCaptor.forClass(CustomDesignSnapshotEntity.class);
        verify(snapshotDao).insert(snapshotCaptor.capture());
        CustomDesignSnapshotEntity snapshot = snapshotCaptor.getValue();
        when(snapshotDao.selectById(88L)).thenReturn(snapshot);
        when(snapshotDao.update(any(), notNull())).thenReturn(1);

        assertEquals(88L, saved.get("snapshotId"));
        assertEquals(new BigDecimal("1059.00"), workflowService.lockAndGetPrice(88L, 7L));
        snapshot.setStatus("LOCKED");
        workflowService.bindOrder(88L, 7L, 100, 101);

        CustomOrderCustomizationEntity binding = unpaidBinding();
        when(bindingDao.selectOne(any())).thenReturn(binding);
        when(bindingDao.update(any(), notNull())).thenReturn(1);
        when(workOrderDao.selectCount(any())).thenReturn(0L);
        workflowService.markOrderPaid(100);

        verify(bindingDao).insert(any(CustomOrderCustomizationEntity.class));
        verify(workOrderDao).insert(any(ProductionWorkOrderEntity.class));
        assertEquals("ORDERED", snapshot.getStatus());
    }

    /** 创建完整链路使用的组件选项 / Creates an option for the workflow test. */
    private CustomComponentOptionEntity workflowOption(
            Long id, String type, String code, String priceDelta) {
        CustomComponentOptionEntity option = new CustomComponentOptionEntity();
        option.setId(id);
        option.setComponentType(type);
        option.setOptionCode(code);
        option.setName(code);
        option.setPriceDelta(new BigDecimal(priceDelta));
        option.setEnabled(1);
        return option;
    }

    /** 验证已取消绑定不会被迟到支付回调复活 / Verifies cancelled bindings stay cancelled. */
    @Test
    void cancelledBindingIsNotRevivedByLateCallback() {
        when(bindingDao.selectOne(any())).thenReturn(unpaidBinding());
        // 条件更新未命中说明绑定已不是 LOCKED / Miss means the binding is no longer LOCKED.
        when(bindingDao.update(any(), notNull())).thenReturn(0);

        service.markOrderPaid(100);

        verify(workOrderDao, times(0)).insert(any(ProductionWorkOrderEntity.class));
    }

    /** 验证未支付取消后设计恢复为草稿 / Verifies unpaid cancellation releases the design. */
    @Test
    void cancelUnpaidRestoresSnapshotToDraft() {
        CustomOrderCustomizationEntity binding = unpaidBinding();
        when(bindingDao.selectOne(any())).thenReturn(binding);
        when(bindingDao.update(any(), notNull())).thenReturn(1);
        CustomDesignSnapshotEntity snapshot = orderedSnapshot();
        when(snapshotDao.selectById(88L)).thenReturn(snapshot);
        when(workOrderDao.selectOne(any())).thenReturn(null);

        service.cancelOrder(100);

        assertEquals("CANCELLED", snapshot.getStatus());
        verify(snapshotDao).updateById(snapshot);
        ArgumentCaptor<CustomDesignSnapshotEntity> draft =
                ArgumentCaptor.forClass(CustomDesignSnapshotEntity.class);
        verify(snapshotDao).insert(draft.capture());
        assertEquals("DRAFT", draft.getValue().getStatus());
        org.junit.jupiter.api.Assertions.assertNull(draft.getValue().getId());
        // 取消时应恢复组件库存 / Component stock must be restored on cancellation.
        verify(stockDao).restoreStock(10, "pearl", "test-pearl", 3);
    }

    /** 验证已支付退款取消不释放设计 / Verifies paid refunds do not release the design. */
    @Test
    void cancelPaidKeepsSnapshotOrdered() {
        CustomOrderCustomizationEntity binding = unpaidBinding();
        binding.setStatus("PAID");
        when(bindingDao.selectOne(any())).thenReturn(binding);
        // 条件更新命中说明取消成功 / Hit means the cancellation succeeded.
        when(bindingDao.update(any(), notNull())).thenReturn(1);
        CustomDesignSnapshotEntity snapshot = orderedSnapshot();
        when(snapshotDao.selectById(88L)).thenReturn(snapshot);
        when(workOrderDao.selectOne(any())).thenReturn(null);

        service.cancelOrder(100);

        assertEquals("ORDERED", snapshot.getStatus());
        verify(snapshotDao, times(0)).updateById(snapshot);
        // 已支付退款同样恢复组件库存 / Paid refunds restore component stock too.
        verify(stockDao).restoreStock(10, "pearl", "test-pearl", 3);
    }

    /** 验证重复取消不会二次恢复库存 / Verifies repeated cancellation does not restore stock twice. */
    @Test
    void repeatedCancellationDoesNotRestoreStockTwice() {
        CustomOrderCustomizationEntity binding = unpaidBinding();
        when(bindingDao.selectOne(any())).thenReturn(binding);
        // 第二次调用条件更新未命中 / Second conditional update misses.
        when(bindingDao.update(any(), notNull())).thenReturn(1, 0);
        when(snapshotDao.selectById(88L)).thenReturn(orderedSnapshot());
        when(workOrderDao.selectOne(any())).thenReturn(null);

        service.cancelOrder(100);
        service.cancelOrder(100);

        verify(stockDao, times(1)).restoreStock(10, "pearl", "test-pearl", 3);
    }

    /** 已取消绑定即使数据库更新返回成功也不得再次恢复库存. */
    @Test
    void alreadyCancelledBindingNeverRestoresStock() {
        CustomOrderCustomizationEntity binding = unpaidBinding();
        binding.setStatus("CANCELLED");
        when(bindingDao.selectOne(any())).thenReturn(binding);
        when(bindingDao.update(any(), notNull())).thenReturn(1);
        service.cancelOrder(100);
        verify(bindingDao, times(0)).update(any(), notNull());
        verify(stockDao, times(0)).restoreStock(any(), any(), any(), anyInt());
    }

    /** 验证组件库存不足时绑定失败并抛异常 / Verifies binding fails when component stock is insufficient. */
    @Test
    void bindOrderFailsWhenComponentStockInsufficient() {
        CustomQuoteRequest request = new CustomQuoteRequest();
        request.setGoodsId(10);
        request.setOptionIds(Collections.singletonList(1L));
        Map<String, Object> quote = new LinkedHashMap<>();
        quote.put("totalPrice", new BigDecimal("219.00"));
        quote.put(
                "components",
                List.of(
                        Map.of(
                                "componentType",
                                "pearl",
                                "optionCode",
                                "test-pearl",
                                "quantity",
                                3)));
        when(quoteService.quote(any())).thenReturn(quote);
        org.mockito.Mockito.doAnswer(
                        invocation -> {
                            CustomDesignSnapshotEntity snapshot = invocation.getArgument(0);
                            snapshot.setId(88L);
                            return 1;
                        })
                .when(snapshotDao)
                .insert(any(CustomDesignSnapshotEntity.class));
        service.saveDraft(7L, request);
        ArgumentCaptor<CustomDesignSnapshotEntity> snapshotCaptor =
                ArgumentCaptor.forClass(CustomDesignSnapshotEntity.class);
        verify(snapshotDao).insert(snapshotCaptor.capture());
        CustomDesignSnapshotEntity snapshot = snapshotCaptor.getValue();
        snapshot.setStatus("LOCKED");
        when(snapshotDao.selectById(88L)).thenReturn(snapshot);
        when(stockDao.deductStock(10, "pearl", "test-pearl", 3)).thenReturn(0);

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class, () -> service.bindOrder(88L, 7L, 100, 101));
        // 库存不足时不允许绑定订单 / No binding allowed when stock is insufficient.
        verify(bindingDao, times(0)).insert(any(CustomOrderCustomizationEntity.class));
    }

    /** 创建已下单状态的设计快照 / Creates a snapshot in ORDERED status. */
    private CustomDesignSnapshotEntity orderedSnapshot() {
        CustomDesignSnapshotEntity snapshot = new CustomDesignSnapshotEntity();
        snapshot.setId(88L);
        snapshot.setGoodsId(10);
        snapshot.setConfigurationJson("{\"goodsId\":10,\"optionIds\":[1]}");
        snapshot.setPriceBreakdownJson(
                "{\"components\":[{\"componentType\":\"pearl\",\"optionCode\":\"test-pearl\",\"quantity\":3}]}");
        snapshot.setStatus("ORDERED");
        return snapshot;
    }

    /** 创建未支付定制绑定 / Creates an unpaid customization binding. */
    private CustomOrderCustomizationEntity unpaidBinding() {
        CustomOrderCustomizationEntity binding = new CustomOrderCustomizationEntity();
        binding.setId(1L);
        binding.setOrderId(100);
        binding.setOrderGoodsId(101);
        binding.setSnapshotId(88L);
        binding.setStatus("LOCKED");
        return binding;
    }
}
