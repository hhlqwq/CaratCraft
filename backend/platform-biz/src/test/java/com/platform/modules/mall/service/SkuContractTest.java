package com.platform.modules.mall.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.platform.modules.mall.dto.GoodsAggregateDTO;
import com.platform.modules.mall.service.support.SkuContract;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** 验证后台和购买链路的唯一SKU契约及有界组合生成. */
class SkuContractTest {
    /** 构造明确的规格组, 不借用真实商品配置. */
    private GoodsAggregateDTO.SpecGroup group(int id, String... values) {
        GoodsAggregateDTO.SpecGroup group = new GoodsAggregateDTO.SpecGroup();
        group.setSpecificationId(id);
        for (String text : values) {
            GoodsAggregateDTO.SpecValue value = new GoodsAggregateDTO.SpecValue();
            value.setValue(text);
            group.getValues().add(value);
        }
        return group;
    }

    /** 生成时按组排序, 同一规格值中冒号是正文而非额外分隔符. */
    @Test
    void canonicalKeysHaveOneFormatAndStableOrder() {
        Set<String> keys =
                SkuContract.expectedKeys(List.of(group(8, "8mm", "9:mm"), group(7, "白色")));
        assertEquals(Set.of("7:白色|8:8mm", "7:白色|8:9:mm"), keys);
        assertEquals(
                List.of("白色", "9:mm"), new ArrayList<>(SkuContract.parse("7:白色|8:9:mm").values()));
        assertEquals(Set.of(""), SkuContract.expectedKeys(List.of()));
    }

    /** 运行时拒绝旧格式、重复组、非规范顺序和空白值. */
    @Test
    void legacyAndAmbiguousKeysAreRejected() {
        for (String value :
                new String[] {"1_2", "8:8mm|7:白色", "7:白色|7:金色", "7:", "7: 白色", "0:白色", "7:白色|"}) {
            assertThrows(IllegalArgumentException.class, () -> SkuContract.parse(value));
        }
        assertThrows(IllegalArgumentException.class, () -> SkuContract.parse(null));
    }

    /** 组合上限在分配组合集合前检查, 大量维度不能整型溢出绕过限制. */
    @Test
    void combinationLimitIsCheckedBeforeExpansion() {
        List<GoodsAggregateDTO.SpecGroup> groups = new ArrayList<>();
        for (int id = 1; id <= 9; id++) {
            groups.add(group(id, "白色", "金色"));
        }
        assertEquals(512, SkuContract.expectedKeys(groups).size());
        groups.add(group(10, "8mm", "9mm"));
        assertThrows(IllegalArgumentException.class, () -> SkuContract.expectedKeys(groups));
    }

    /** 重复或含分隔符的规格值会在任何数据库写入前被拒绝. */
    @Test
    void duplicateGroupsAndReservedValuesAreRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SkuContract.expectedKeys(List.of(group(7, "白色", "白色"))));
        assertThrows(
                IllegalArgumentException.class,
                () -> SkuContract.expectedKeys(List.of(group(7, "白|金"))));
        assertThrows(
                IllegalArgumentException.class,
                () -> SkuContract.expectedKeys(List.of(group(7, "白色"), group(7, "金色"))));
        assertThrows(
                IllegalArgumentException.class,
                () -> SkuContract.expectedKeys(List.of(group(7, "白".repeat(51)))));
    }

    /** 不能用相同数量的错误组合替代所需组合, 缺货可以保存但负库存不允许. */
    @Test
    void submittedKeysMustCoverExactlyTheDeclaredCombinations() {
        GoodsAggregateDTO aggregate = new GoodsAggregateDTO();
        aggregate.setSpecs(List.of(group(7, "白色")));
        GoodsAggregateDTO.SkuItem item = new GoodsAggregateDTO.SkuItem();
        item.setGoodsSpecificationIds("7:金色");
        aggregate.setSkuList(List.of(item));
        assertThrows(IllegalArgumentException.class, () -> SkuContract.validate(aggregate));
        item.setGoodsSpecificationIds("7:白色");
        item.setGoodsNumber(0);
        SkuContract.validate(aggregate);
        item.setGoodsNumber(-1);
        assertThrows(IllegalArgumentException.class, () -> SkuContract.validate(aggregate));
    }
}
