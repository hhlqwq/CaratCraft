package com.platform.modules.custom.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.platform.modules.custom.dao.ComponentStockDao;
import com.platform.modules.custom.dao.CustomDesignSnapshotDao;
import com.platform.modules.custom.dao.CustomOrderCustomizationDao;
import com.platform.modules.custom.dao.ProductionWorkOrderDao;
import com.platform.modules.custom.dto.CustomQuoteRequest;
import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import com.platform.modules.custom.entity.CustomDesignSnapshotEntity;
import com.platform.modules.custom.entity.CustomOrderCustomizationEntity;
import com.platform.modules.custom.service.impl.CustomDesignServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** 校验规则、草稿并发与冻结BOM恢复, 不依赖材料当前可售配置. */
class MaterialHistoryTest {
    private CustomDesignSnapshotDao snapshots;
    private CustomOrderCustomizationDao bindings;
    private ComponentStockDao stock;
    private CustomQuoteService quotes;
    private CustomDesignServiceImpl designs;
    private CustomDesignSnapshotEntity draft;
    private CustomQuoteRequest request;

    /** 准备本人草稿版本与可复用的报价替身. */
    @BeforeEach
    void setUp() {
        snapshots = mock(CustomDesignSnapshotDao.class);
        bindings = mock(CustomOrderCustomizationDao.class);
        stock = mock(ComponentStockDao.class);
        quotes = mock(CustomQuoteService.class);
        designs =
                new CustomDesignServiceImpl(
                        snapshots,
                        bindings,
                        mock(ProductionWorkOrderDao.class),
                        quotes,
                        stock,
                        org.mockito.Mockito.mock(
                                com.platform.modules.custom.dao.WorkOrderLogDao.class));
        draft = new CustomDesignSnapshotEntity();
        draft.setId(88L);
        draft.setVersion(1L);
        draft.setUserId(7L);
        draft.setGoodsId(10);
        draft.setStatus("DRAFT");
        when(snapshots.selectById(88L)).thenReturn(draft);
        when(quotes.quote(any())).thenReturn(Map.of("totalPrice", new BigDecimal("99.00")));
        request = new CustomQuoteRequest();
        request.setGoodsId(10);
        request.setDraftId(88L);
        request.setVersion(1L);
    }

    /** 编辑现有草稿不插入副本, 新版本由条件更新产生. */
    @Test
    void editUpdatesOwnedVersion() {
        when(snapshots.update(any(), any())).thenReturn(1);
        when(quotes.quote(any()))
                .thenReturn(
                        Map.of("components", List.of(Map.of("assetUrl", "/upload/preview.png"))));
        var result = designs.saveDraft(7L, request);
        assertEquals("/upload/preview.png", draft.getPreviewUrl());
        verify(snapshots)
                .update(
                        any(),
                        org.mockito.ArgumentMatchers.argThat(
                                wrapper -> {
                                    var update =
                                            (com.baomidou.mybatisplus.core.conditions.update
                                                                    .UpdateWrapper<
                                                            CustomDesignSnapshotEntity>)
                                                    wrapper;
                                    update.getSqlSet();
                                    return update.getParamNameValuePairs()
                                            .containsValue("/upload/preview.png");
                                }));
        assertEquals(88L, result.get("snapshotId"));
        assertEquals(2L, result.get("version"));
        verify(snapshots, never()).insert(any(CustomDesignSnapshotEntity.class));
    }

    /** 旧版本、他人草稿及已下单快照均不能覆盖. */
    @Test
    void rejectsStaleForeignAndOrderedDrafts() {
        request.setVersion(0L);
        assertThrows(IllegalStateException.class, () -> designs.saveDraft(7L, request));
        request.setVersion(1L);
        assertThrows(IllegalArgumentException.class, () -> designs.saveDraft(9L, request));
        draft.setStatus("ORDERED");
        assertThrows(IllegalStateException.class, () -> designs.saveDraft(7L, request));
        verify(snapshots, never()).update(any(), any());
    }

    /** 库存回补使用当时冻结的3颗, 不按当前已停用材料的默认1件恢复. */
    @Test
    void cancellationRestoresFrozenQuantity() {
        draft.setStatus("ORDERED");
        draft.setConfigurationJson("{\"goodsId\":10,\"optionIds\":[1]}");
        draft.setPriceBreakdownJson(
                "{\"components\":[{\"componentType\":\"pearl\",\"optionCode\":\"old-pearl\",\"quantity\":3}]}");
        CustomOrderCustomizationEntity binding = new CustomOrderCustomizationEntity();
        binding.setId(1L);
        binding.setSnapshotId(88L);
        binding.setStatus("PAID");
        when(bindings.selectOne(any())).thenReturn(binding);
        when(bindings.update(any(), any())).thenReturn(1);
        when(stock.restoreStock(10, "pearl", "old-pearl", 3)).thenReturn(1);
        when(stock.logMovement(
                        eq(10), eq("pearl"), eq("old-pearl"), eq(3), anyString(), anyString()))
                .thenReturn(1);
        designs.cancelOrder(100);
        verify(stock).restoreStock(10, "pearl", "old-pearl", 3);
    }

    /** 清单缺失或损坏时拒绝旧ID回退和隐式数量转换, 不触发任何库存写入. */
    @ParameterizedTest
    @ValueSource(
            strings = {
                "null",
                "{}",
                "{\"components\":[]}",
                "{\"components\":\"[]\"}",
                "{\"components\":[null]}",
                "{\"components\":[{\"componentType\":\"pearl\",\"optionCode\":\"p\"}]}",
                "{\"components\":[{\"componentType\":\"pearl\",\"optionCode\":\"p\",\"quantity\":1.5}]}",
                "{\"components\":[{\"componentType\":\"pearl\",\"optionCode\":\"p\",\"quantity\":\"3\"}]}",
                "{\"components\":[{\"componentType\":\"pearl\",\"optionCode\":\"p\",\"quantity\":-1}]}",
                "{\"components\":[{\"componentType\":\"pearl\",\"optionCode\":\"p\",\"quantity\":10001}]}",
                "{\"components\":[{\"componentType\":\"pearl\",\"optionCode\":\""
                        + " \",\"quantity\":1}]}",
                "{\"components\":[{\"componentType\":\"pearl\",\"optionCode\":\"p\",\"quantity\":1},{\"componentType\":\"pearl\",\"optionCode\":\"p\",\"quantity\":1}]}",
                "{\"components\":[{\"componentType\":\"pearl\",\"optionCode\":\"valid\",\"quantity\":1},{\"componentType\":\"pearl\",\"optionCode\":\"invalid\",\"quantity\":false}]}",
                "invalid-json"
            })
    void invalidFrozenComponentsNeverMoveStock(String frozen) {
        draft.setStatus("LOCKED");
        draft.setConfigurationJson("{\"goodsId\":10,\"optionIds\":[1]}");
        draft.setPriceBreakdownJson(frozen);
        assertThrows(IllegalStateException.class, () -> designs.bindOrder(88L, 7L, 100, 101));
        verifyNoInteractions(stock, bindings);
    }

    /** 商家互斥与依赖规则实际在服务端执行, 不依赖UI禁用按钮. */
    @Test
    void rulesEnforceCrossComponentConstraints() {
        CustomComponentOptionEntity pearl = new CustomComponentOptionEntity();
        pearl.setComponentType("pearl");
        pearl.setOptionCode("large");
        pearl.setName("大珠");
        pearl.setMaterialParams(
                "{\"stockQuantity\":3,\"stockUnit\":\"颗\",\"requires\":[\"chain:strong\"]}");
        assertEquals(3, ComponentRules.parse(pearl.getMaterialParams()).quantity());
        assertThrows(IllegalArgumentException.class, () -> ComponentRules.validate(List.of(pearl)));
        CustomComponentOptionEntity chain = new CustomComponentOptionEntity();
        chain.setComponentType("chain");
        chain.setOptionCode("strong");
        ComponentRules.validate(List.of(pearl, chain));
        chain.setMaterialParams("{\"incompatibleWith\":[\"pearl:large\"]}");
        assertThrows(
                IllegalArgumentException.class,
                () -> ComponentRules.validate(List.of(pearl, chain)));
        assertThrows(
                IllegalArgumentException.class,
                () -> ComponentRules.parse("{\"stockQuantity\":-1}"));
    }
}
