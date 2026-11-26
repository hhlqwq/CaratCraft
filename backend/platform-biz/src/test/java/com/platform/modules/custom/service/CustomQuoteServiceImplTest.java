package com.platform.modules.custom.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.platform.modules.custom.dao.ComponentStockDao;
import com.platform.modules.custom.dao.CustomComponentOptionDao;
import com.platform.modules.custom.dao.CustomProductDao;
import com.platform.modules.custom.dto.CustomQuoteRequest;
import com.platform.modules.custom.entity.ComponentStockEntity;
import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import com.platform.modules.custom.entity.CustomProductEntity;
import com.platform.modules.custom.service.impl.CustomQuoteServiceImpl;
import com.platform.modules.mall.entity.MallGoodsEntity;
import com.platform.modules.mall.service.MallGoodsService;
import com.platform.support.ResponseAssertions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

/** 定制报价服务测试 / Custom quote service tests. */
class CustomQuoteServiceImplTest {
    private MallGoodsService goodsService;
    private CustomComponentOptionDao optionDao;
    private CustomProductDao productDao;
    private ComponentStockDao stockDao;
    private CustomQuoteServiceImpl service;

    /** 初始化报价依赖 / Initializes quote dependencies. */
    @BeforeEach
    void setUp() {
        goodsService = mock(MallGoodsService.class);
        optionDao = mock(CustomComponentOptionDao.class);
        productDao = mock(CustomProductDao.class);
        stockDao = mock(ComponentStockDao.class);
        service = new CustomQuoteServiceImpl(goodsService, optionDao, productDao, stockDao);

        MallGoodsEntity goods = new MallGoodsEntity();
        goods.setId(10);
        goods.setIsOnSale(1);
        goods.setRetailPrice(new BigDecimal("199.00"));
        when(goodsService.getById(10)).thenReturn(goods);
        CustomProductEntity product = new CustomProductEntity();
        product.setGoodsId(10);
        product.setEnabled(1);
        product.setRequiredComponentTypes("chain,accessory");
        when(productDao.selectOne(any())).thenReturn(product);
        when(optionDao.selectList(any()))
                .thenReturn(
                        Arrays.asList(
                                option(1L, "chain", "20.00"),
                                option(2L, "accessory", "35.00"),
                                option(3L, "chain", "50.00")));
        // 组件库存 mock / Mock component stock
        ComponentStockEntity stock = new ComponentStockEntity();
        stock.setStock(100);
        when(stockDao.selectOne(any())).thenReturn(stock);
    }

    /** 验证基础价和组件加价 / Verifies base and option prices. */
    @Test
    void quoteCalculatesServerPrice() {
        CustomQuoteRequest request = request(1L, 2L);
        Map<String, Object> quote = service.quote(request);
        assertEquals(new BigDecimal("254.00"), quote.get("totalPrice"));
    }

    /** 验证同类组件不可重复选择 / Verifies exclusive component groups. */
    @Test
    void quoteRejectsTwoOptionsFromSameGroup() {
        CustomQuoteRequest request = request(1L, 3L);
        assertThrows(IllegalArgumentException.class, () -> service.quote(request));
    }

    /** 缺少必选材料允许展示草稿报价, 但服务端明确禁止结算. */
    @Test
    void quoteKeepsIncompleteDesignDraftOnly() {
        CustomQuoteRequest request = request(1L);
        assertEquals(false, service.quote(request).get("checkoutReady"));
    }

    /** 重复材料按累计实例收费并合并BOM, 同类多选只在新排列模型开放. */
    @Test
    void arrangementAggregatesRepeatedMaterialsAndFreezesOrder() {
        CustomQuoteRequest request = request();
        request.setArrangementOptionIds(Arrays.asList(2L, 1L, 2L, 3L));
        Map<String, Object> quote = service.quote(request);
        assertEquals(new BigDecimal("339.00"), quote.get("totalPrice"));
        var components = ResponseAssertions.records(quote.get("components"));
        assertEquals(3, components.size());
        assertEquals(2, components.get(0).get("quantity"));
        assertEquals(new BigDecimal("70.00"), components.get(0).get("linePrice"));
        var arranged = ResponseAssertions.records(quote.get("arrangement"));
        assertEquals(
                Arrays.asList(2L, 1L, 2L, 3L),
                arranged.stream().map(item -> item.get("optionId")).toList());
        assertEquals(false, quote.get("lengthComplete"));
        assertEquals(null, quote.get("estimatedLengthMm"));
    }

    /** 库存校验累计耗用量, 不能因分成多个实例绕过库存不足. */
    @Test
    void arrangementChecksCombinedStockQuantity() {
        var first = option(1L, "chain", "20.00");
        first.setMaterialParams("{\"stockQuantity\":60,\"stockUnit\":\"毫米\"}");
        when(optionDao.selectList(any()))
                .thenReturn(Arrays.asList(first, option(2L, "accessory", "35.00")));
        CustomQuoteRequest request = request();
        request.setArrangementOptionIds(Arrays.asList(1L, 2L, 1L));
        assertThrows(IllegalArgumentException.class, () -> service.quote(request));
    }

    /** 空排列允许保存草稿, 拒绝过大实例列表以及同时提交两种配置模型. */
    @Test
    void arrangementRejectsAmbiguousAndUnboundedRequests() {
        CustomQuoteRequest request = request();
        request.setArrangementOptionIds(java.util.List.of());
        assertEquals(false, service.quote(request).get("checkoutReady"));
        request.setArrangementOptionIds(java.util.Collections.nCopies(2049, 1L));
        assertThrows(IllegalArgumentException.class, () -> service.quote(request));
        request.setArrangementOptionIds(Arrays.asList(1L, 2L));
        request.setOptionIds(Arrays.asList(1L, 2L));
        assertThrows(IllegalArgumentException.class, () -> service.quote(request));
    }

    /** 服务端长度仅使用商家实测值, 串珠与同一路径链材不重复加长. */
    @Test
    void arrangementLengthUsesMerchantMeasurementsAndFrozenDefaults() {
        var pearl = option(1L, "pearl", "10.00");
        pearl.setMaterialParams("{\"diameterMm\":8.5,\"stockUnit\":\"颗\"}");
        var chain = option(2L, "chain", "35.00");
        chain.setMaterialParams("{\"lengthMm\":400}");
        CustomProductEntity product = new CustomProductEntity();
        product.setRequiredComponentTypes("pearl,chain");
        when(productDao.selectOne(any())).thenReturn(product);
        when(optionDao.selectList(any())).thenReturn(Arrays.asList(pearl, chain));
        CustomQuoteRequest request = request();
        request.setArrangementOptionIds(Arrays.asList(1L, 2L, 1L));
        Map<String, Object> quote = service.quote(request);
        assertEquals(true, quote.get("lengthComplete"));
        assertEquals(new BigDecimal("400"), quote.get("estimatedLengthMm"));
        var arranged = ResponseAssertions.records(quote.get("arrangement"));
        assertEquals("近正圆", arranged.get(0).get("shape"));
        assertEquals("极光", arranged.get(0).get("luster"));
        assertEquals("微瑕", arranged.get(0).get("blemish"));
    }

    /** 无珠草稿可继续编辑, 重复固定部件不能报价或另存. */
    @Test
    void unfinishedNecklaceAndDuplicateFixedSlots() {
        var goods = goodsService.getById(10);
        goods.setJewelryType("NECKLACE");
        var product = new CustomProductEntity();
        product.setRequiredComponentTypes("chain");
        product.setLengthRules("{\"mode\":\"OVERLAY_MAX\",\"claspMm\":10,\"connectionMm\":0,"
                + "\"lossMm\":0,\"toleranceMm\":5,\"minLengthMm\":300,\"maxLengthMm\":600}");
        when(productDao.selectOne(any())).thenReturn(product);
        var chain = option(1L, "chain", "20.00");
        chain.setMaterialParams("{\"lengthMm\":440}");
        var pearl = option(2L, "pearl", "10.00");
        pearl.setMaterialParams("{\"diameterMm\":8}");
        var clasp = option(3L, "clasp", "5.00");
        clasp.setMaterialParams("{\"spanMm\":10}");
        when(optionDao.selectList(any())).thenReturn(java.util.List.of(chain, pearl, clasp));
        var request = request();
        request.setTargetLengthMm(new BigDecimal("450"));
        request.setArrangementOptionIds(java.util.List.of(1L, 3L));
        assertEquals(false, service.quote(request).get("checkoutReady"));
        request.setArrangementOptionIds(java.util.List.of(1L, 3L, 2L));
        assertEquals(true, service.quote(request).get("checkoutReady"));
        request.setArrangementOptionIds(java.util.List.of(1L, 3L, 2L, 3L));
        assertThrows(IllegalArgumentException.class, () -> service.quoteDraft(request));
        request.setArrangementOptionIds(java.util.List.of(1L, 1L, 2L, 3L));
        assertThrows(IllegalArgumentException.class, () -> service.quote(request));
    }

    /** 目录参考品允许组成草稿, 无法生成成交报价; 兼容延长链可以与主扣同用. */
    @Test
    void referenceMaterialsNeverBecomePurchasable() {
        var reference = option(8L, "pearl", "0");
        reference.setOptionCode("client-test-reference");
        reference.setEnabled(0);
        reference.setMaterialParams("{\"candidateOnly\":true}");
        when(optionDao.selectList(any())).thenReturn(java.util.List.of(reference));
        var request = request();
        request.setArrangementOptionIds(java.util.List.of(8L));
        assertEquals(true, service.quoteDraft(request).get("referencePreview"));
        assertEquals(false, service.quote(request).get("checkoutReady"));
        assertEquals(null, service.quote(request).get("totalPrice"));
        reference.setOptionCode("merchant-disabled");
        assertThrows(IllegalArgumentException.class, () -> service.quoteDraft(request));
    }

    /** 创建报价请求 / Creates a quote request. */
    private CustomQuoteRequest request(Long... optionIds) {
        CustomQuoteRequest request = new CustomQuoteRequest();
        request.setGoodsId(10);
        request.setOptionIds(Arrays.asList(optionIds));
        return request;
    }

    /** 创建组件选项 / Creates a component option. */
    private CustomComponentOptionEntity option(Long id, String type, String delta) {
        CustomComponentOptionEntity option = new CustomComponentOptionEntity();
        option.setId(id);
        option.setEnabled(1);
        option.setComponentType(type);
        option.setPriceDelta(new BigDecimal(delta));
        return option;
    }
}
