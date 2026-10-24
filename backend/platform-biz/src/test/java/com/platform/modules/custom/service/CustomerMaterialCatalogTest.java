package com.platform.modules.custom.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alibaba.fastjson.JSON;
import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import com.platform.modules.custom.entity.CustomProductEntity;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

/** 对照客户逐项目录检查候选覆盖、未知值及分类边界. */
class CustomerMaterialCatalogTest {
    /** 客户67项珍珠、4项尾扣、14项配饰和27项链材均可下载且代码不重复. */
    @Test
    @SuppressWarnings("unchecked")
    void templateCoversCustomerCatalogWithoutPublishingOrMeasuredGuesses() {
        var template = CustomerMaterialCatalog.template(10);
        var options = (List<CustomComponentOptionEntity>) template.get("options");
        assertEquals(112, options.size());
        var counts =
                options.stream()
                        .collect(
                                Collectors.groupingBy(
                                        CustomComponentOptionEntity::getComponentType,
                                        Collectors.counting()));
        assertEquals(
                java.util.Map.of("pearl", 67L, "clasp", 4L, "accessory", 14L, "chain", 27L),
                counts);
        assertEquals(112, options.stream().map(ComponentRules::key).distinct().count());
        for (var option : options) {
            assertEquals(10, option.getGoodsId());
            assertEquals(0, option.getEnabled());
            assertEquals(0, option.getPriceDelta().signum());
            assertNull(option.getId());
            assertNull(option.getAssetUrl());
            var params = JSON.parseObject(option.getMaterialParams());
            assertTrue(params.getBooleanValue("candidateOnly"));
            assertFalse(params.containsKey("diameterMm"));
            assertFalse(params.containsKey("lengthMm"));
            ComponentRules.parse(option.getMaterialParams());
        }
        var product = (CustomProductEntity) template.get("product");
        assertEquals(0, product.getEnabled());
        assertEquals("pearl,clasp,chain", product.getRequiredComponentTypes());
        assertEquals(false, template.get("replaceOptions"));
        assertTrue(template.get("accessorySourceText").toString().contains("戴妃皇冠"));
        assertEquals(1, options.stream().filter(row -> "戴妃皇冠".equals(row.getName())).count());
        assertEquals(1, options.stream().filter(row -> "皇冠".equals(row.getName())).count());
    }

    /** 生成新商品模板不会共享可变选项身份或依赖前次编辑状态. */
    @Test
    @SuppressWarnings("unchecked")
    void templatesDoNotShareMutableOptionsBetweenProducts() {
        var first =
                (List<CustomComponentOptionEntity>)
                        CustomerMaterialCatalog.template(10).get("options");
        first.get(0).setName("已编辑");
        var second =
                (List<CustomComponentOptionEntity>)
                        CustomerMaterialCatalog.template(20).get("options");
        assertEquals(20, second.get(0).getGoodsId());
        assertFalse("已编辑".equals(second.get(0).getName()));
        assertThrows(IllegalArgumentException.class, () -> CustomerMaterialCatalog.template(0));
    }

    /** 必选组只有四类且顺序稳定, 旧类、重复和空项不静默归一化. */
    @Test
    void requiredTypesRejectLegacyAmbiguityAndDuplicates() {
        assertThrows(IllegalArgumentException.class, () -> MaterialTypes.require(null));
        assertEquals("pearl,clasp,chain", MaterialTypes.required("chain, pearl,clasp"));
        for (String invalid :
                List.of("material", "style,pearl", "PEARL", "pearl,pearl", "pearl,", "unknown")) {
            assertThrows(IllegalArgumentException.class, () -> MaterialTypes.required(invalid));
        }
    }
}
