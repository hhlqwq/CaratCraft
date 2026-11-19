package com.platform.modules.custom.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.platform.modules.custom.dao.ComponentStockDao;
import com.platform.modules.custom.dto.NecklaceReferenceRequest;
import com.platform.modules.custom.entity.ComponentStockEntity;
import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import com.platform.modules.custom.entity.CustomProductEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

/** 检验首版确定性参考、实测筛选及库存边界, 不依赖云模型、Redis或业务库. */
class NecklaceReferenceServiceTest {
    private CustomProductService products;
    private ComponentStockDao stocks;
    private NecklaceReferenceService service;
    private CustomProductEntity product;

    /** 使用可信商品接口替身, 每个用例按需要配置制作规则与材料. */
    @BeforeEach
    void setUp() {
        products = mock(CustomProductService.class);
        stocks = mock(ComponentStockDao.class);
        service = new NecklaceReferenceService(products, stocks);
        product = new CustomProductEntity();
        product.setGoodsId(10);
        when(products.getByGoodsId(10)).thenReturn(product);
        when(products.getEnabledOptions(10)).thenReturn(List.of());
        when(stocks.selectList(any())).thenReturn(List.of());
    }

    /** 未知颈围保留通用比较档位与出处, 不推算个人颈围或保证合身. */
    @Test
    void unknownMeasurementRemainsGenericAndTraceable() {
        var result = service.reference(7L, request(null, "MEDIUM"));
        assertEquals(List.of(new BigDecimal("457.5"), new BigDecimal("507.5")), result.lengthsMm());
        assertFalse(result.measured());
        assertFalse(result.merchantRulesConfigured());
        assertTrue(result.notice().contains("不推算颈围"));
        assertTrue(result.sources().get(0).url().startsWith("https://www.tiffany.com/"));
        assertEquals("2026-10-28", result.sources().get(0).reviewedAt());
    }

    /** 已知颈围仅排除不大于实测值的候选, 不套用固定加长公式. */
    @Test
    void measuredNeckFiltersWithoutInventingNewLengths() {
        var result = service.reference(7L, request("480", "MEDIUM"));
        assertEquals(List.of(new BigDecimal("507.5")), result.lengthsMm());
        assertTrue(result.measured());
        assertTrue(service.reference(7L, request("507.5", "MEDIUM")).lengthsMm().isEmpty());
    }

    /** 无交集不得偷偷改换偏好、取中点或扩大商家范围. */
    @Test
    void missingIntersectionDoesNotFallbackToMadeUpLength() {
        var result = service.reference(7L, request("800", "LONG"));
        assertTrue(result.lengthsMm().isEmpty());
        assertTrue(result.reason().contains("没有交集"));
    }

    /** 制作范围与实测条件共同筛选, 公开资料不能覆盖商家生产边界. */
    @Test
    void merchantRangeFurtherRestrictsCommonLengths() {
        product.setLengthRules(
                "{\"mode\":\"SUM\",\"claspMm\":0,\"connectionMm\":0,\"lossMm\":0,\"toleranceMm\":1,\"minLengthMm\":490,\"maxLengthMm\":510}");
        var result = service.reference(7L, request("350", "MEDIUM"));
        assertEquals(List.of(new BigDecimal("507.5")), result.lengthsMm());
        assertTrue(result.merchantRulesConfigured());
        assertTrue(service.reference(7L, request(null, "SHORT")).lengthsMm().isEmpty());
    }

    /** 非法身份、未知偏好和无效测量在访问配置及库存前拒绝. */
    @Test
    void invalidInputNeverReachesPersistence() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.reference(null, request(null, "MEDIUM")));
        assertThrows(IllegalArgumentException.class, () -> service.reference(7L, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.reference(7L, new NecklaceReferenceRequest(0, null, "MEDIUM")));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.reference(7L, request(null, "unknown")));
        for (String value : List.of("0", "199.9", "800.1", "350.12")) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> service.reference(7L, request(value, "MEDIUM")));
        }
        verifyNoInteractions(products, stocks);
    }

    /** 不可售或已关闭定制的商品不能被参考接口绕过配置校验. */
    @Test
    void unavailableGoodsNeverReadsStock() {
        when(products.getConfig(10)).thenThrow(new IllegalArgumentException("商品已下架"));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.reference(7L, request(null, "MEDIUM")));
        verifyNoInteractions(stocks);
    }

    /** 稳定类型和代码匹配实际用量, 虚拟材料无需余额, 缺货材料不冒充可用. */
    @Test
    void materialCandidatesUseActualStockQuantityAndStableIdentity() {
        var pearl = option(3L, "pearl", "p", "{\"stockQuantity\":3}");
        var virtual = option(1L, "style", "s", "{\"stockQuantity\":0}");
        var other = option(2L, "chain", "p", "{}");
        when(products.getEnabledOptions(10)).thenReturn(List.of(pearl, virtual, other));
        var balance = new ComponentStockEntity();
        balance.setComponentType("pearl");
        balance.setOptionCode("p");
        balance.setStock(2);
        when(stocks.selectList(any())).thenReturn(List.of(balance));
        assertEquals(
                List.of(1L),
                service.reference(7L, request(null, "MEDIUM")).materials().stream()
                        .map(NecklaceReferenceService.Material::optionId)
                        .toList());
        balance.setStock(3);
        var first = service.reference(7L, request(null, "MEDIUM"));
        assertEquals(
                List.of(1L, 3L),
                first.materials().stream()
                        .map(NecklaceReferenceService.Material::optionId)
                        .toList());
        assertEquals(first, service.reference(7L, request(null, "MEDIUM")));
    }

    /** 构造唯一毫米输入契约. */
    private NecklaceReferenceRequest request(String neck, String preference) {
        return new NecklaceReferenceRequest(
                10, neck == null ? null : new BigDecimal(neck), preference);
    }

    /** 构造带稳定身份和明确单项用量的启用材料. */
    private CustomComponentOptionEntity option(Long id, String type, String code, String params) {
        var option = new CustomComponentOptionEntity();
        option.setId(id);
        option.setGoodsId(10);
        option.setComponentType(type);
        option.setOptionCode(code);
        option.setName(code);
        option.setMaterialParams(params);
        return option;
    }
}
