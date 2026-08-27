package com.platform.modules.custom.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 定制报价服务测试 / Custom quote service tests.
 */
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
        product.setRequiredComponentTypes("CHAIN,PENDANT");
        when(productDao.selectOne(any())).thenReturn(product);
        when(optionDao.selectList(any())).thenReturn(Arrays.asList(
                option(1L, "CHAIN", "20.00"), option(2L, "PENDANT", "35.00"),
                option(3L, "CHAIN", "50.00")));
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

    /** 验证缺少必选组件时拒绝报价 / Verifies required component validation. */
    @Test
    void quoteRejectsMissingRequiredGroup() {
        CustomQuoteRequest request = request(1L);
        assertThrows(IllegalArgumentException.class, () -> service.quote(request));
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
        option.setComponentType(type);
        option.setPriceDelta(new BigDecimal(delta));
        return option;
    }
}
