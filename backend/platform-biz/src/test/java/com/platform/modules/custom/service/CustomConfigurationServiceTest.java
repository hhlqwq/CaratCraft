package com.platform.modules.custom.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.platform.modules.custom.dao.ComponentStockDao;
import com.platform.modules.custom.dao.CustomComponentOptionDao;
import com.platform.modules.custom.dao.CustomProductDao;
import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import com.platform.modules.custom.entity.CustomProductEntity;
import com.platform.modules.mall.entity.MallGoodsEntity;
import com.platform.modules.mall.service.MallGoodsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

/** 导入保持历史标识、拒绝重复材料及不完整发布配置. */
class CustomConfigurationServiceTest {
    private CustomComponentOptionDao options;
    private ComponentStockDao stocks;
    private CustomProductDao products;
    private CustomConfigurationService service;

    /** 使用隔离DAO模拟商家维护商品, 不操作业务数据. */
    @BeforeEach
    void setUp() {
        options = mock(CustomComponentOptionDao.class);
        stocks = mock(ComponentStockDao.class);
        products = mock(CustomProductDao.class);
        MallGoodsService goods = mock(MallGoodsService.class);
        when(goods.getOne(any())).thenReturn(new MallGoodsEntity());
        service = new CustomConfigurationService(products, options, stocks, goods);
    }

    /** 替换导入沿用旧ID并只初始化缺失库存, 不调用物理删除. */
    @Test
    void replacementRetainsStableIdsAndStock() {
        var old = option("p1");
        old.setId(42L);
        when(options.selectOne(any())).thenReturn(old);
        var incoming = option("p1");
        service.importConfiguration(product(), List.of(incoming), true);
        assertEquals(42L, incoming.getId());
        verify(options).updateById(incoming);
        verify(options, never()).delete(notNull());
        verify(stocks).initialize(10, "pearl", "p1");
        verify(stocks, never()).update(any(), notNull());
    }

    /** 大小写不同的同一代码在停用旧配置前即被拒绝. */
    @Test
    void duplicateKeysDoNotDisableExistingConfiguration() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.importConfiguration(
                                product(), List.of(option("p1"), option("P1")), true));
        verify(options, never()).update(any(), notNull());
        verify(stocks, never()).initialize(any(), any(), any());
    }

    /** 导入模板可先保存停用状态, 未补齐必选库存时不能直接发布. */
    @Test
    void missingRequiredMaterialsPreventPublishing() {
        var product = product();
        product.setEnabled(1);
        product.setRequiredComponentTypes("pearl,chain,clasp");
        when(options.selectList(any())).thenReturn(List.of());
        assertThrows(IllegalArgumentException.class, () -> service.saveProduct(product));
        verify(products, never()).insert(any(CustomProductEntity.class));
    }

    /** 旧类在任何持久化之前被拒绝, 包括停用材料导入和必选组配置. */
    @Test
    void legacyTypesCannotReenterNewConfiguration() {
        var legacy = option("legacy");
        legacy.setComponentType("material");
        assertThrows(IllegalArgumentException.class, () -> service.saveOption(legacy));
        verify(options, never()).insert(any(CustomComponentOptionEntity.class));
        verify(stocks, never()).initialize(any(), any(), any());
        var invalid = product();
        invalid.setRequiredComponentTypes("pearl,style");
        assertThrows(IllegalArgumentException.class, () -> service.saveProduct(invalid));
        verify(products, never()).insert(any(CustomProductEntity.class));
    }

    /** 配饰启用前必须说明计长位置, 默认为启用的请求也不能绕过确认. */
    @Test
    void accessoryPublicationRequiresExplicitLengthRole() {
        var accessory = option("charm");
        accessory.setComponentType("accessory");
        assertThrows(IllegalArgumentException.class, () -> service.saveOption(accessory));
        verify(options, never()).insert(any(CustomComponentOptionEntity.class));
        accessory.setMaterialParams("{\"lengthRole\":\"HANGING\"}");
        service.saveOption(accessory);
        verify(options).insert(accessory);
        verify(stocks).initialize(10, "accessory", "charm");
    }

    /** 创建与下载模板一致的停用商品配置. */
    private CustomProductEntity product() {
        var product = new CustomProductEntity();
        product.setGoodsId(10);
        product.setTemplateCode("necklace-v1");
        product.setEnabled(0);
        return product;
    }

    /** 创建合法稳定材料代码和单件耗用量. */
    private CustomComponentOptionEntity option(String code) {
        var option = new CustomComponentOptionEntity();
        option.setGoodsId(10);
        option.setComponentType("pearl");
        option.setOptionCode(code);
        option.setName("测试珍珠");
        option.setMaterialParams("{\"stockQuantity\":2,\"stockUnit\":\"颗\"}");
        return option;
    }
}
