package com.platform.modules.custom.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.platform.modules.custom.dto.CustomQuoteRequest;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 验证工艺计算、缺失尺寸、输入边界及冻结规则不随编辑变化. */
class FinishedLengthRulesTest {
    private static final String RULES =
            "{\"mode\":\"SUM\",\"claspMm\":10,\"connectionMm\":2,"
                    + "\"lossMm\":2,\"toleranceMm\":1,\"minLengthMm\":300,\"maxLengthMm\":700}";

    /** 有实测颈围及目标长度时按工艺计算, 不重复累计扣件或悬挂件. */
    @Test
    void computesFinishedLengthFromProductionRules() {
        var result =
                FinishedLengthRules.evaluate(
                        RULES, request("350", "450"), arrangement("200", "240"));
        assertEquals(true, result.get("ready"));
        assertEquals(new BigDecimal("450"), result.get("estimatedFinishedLengthMm"));
        assertEquals(new BigDecimal("350"), result.get("neckCircumferenceMm"));
    }

    /** 承载链与串珠重叠时取较长部分, 不把二维预览估算直接当生产长度. */
    @Test
    void overlayModeDoesNotDoubleCountChain() {
        var result =
                FinishedLengthRules.evaluate(
                        RULES.replace("SUM", "OVERLAY_MAX"),
                        request("350", "450"),
                        arrangement("200", "440"));
        assertEquals(true, result.get("ready"));
        assertEquals(new BigDecimal("450"), result.get("estimatedFinishedLengthMm"));
    }

    /** 缺少实测或商家规则仍可形成草稿数据, 但不能下单. */
    @Test
    void incompleteMeasurementsAndRulesStayDraftOnly() {
        assertEquals(
                false,
                FinishedLengthRules.evaluate(null, request(null, null), arrangement("200", "240"))
                        .get("ready"));
        assertEquals(
                false,
                FinishedLengthRules.evaluate(RULES, request(null, "450"), arrangement("200", "240"))
                        .get("ready"));
        assertEquals(
                false,
                FinishedLengthRules.evaluate(RULES, request("350", "450"), arrangement(null, "240"))
                        .get("ready"));
    }

    /** 超出容差、短于颈围及超出制作范围都不能进入结算. */
    @Test
    void rejectsUnmakeableTargets() {
        for (String target : List.of("300", "460", "800")) {
            assertEquals(
                    false,
                    FinishedLengthRules.evaluate(
                                    RULES, request("350", target), arrangement("200", "240"))
                            .get("ready"));
        }
    }

    /** 商家规则不得省略零值项或使用负数、倒置区间及过多小数. */
    @Test
    void rejectsInvalidRulesAndMeasurements() {
        for (String rules :
                List.of(
                        "{}",
                        "null",
                        "[]",
                        RULES.replace("\"lossMm\":2", "\"lossMm\":-1"),
                        RULES.replace("\"maxLengthMm\":700", "\"maxLengthMm\":200"))) {
            assertThrows(IllegalArgumentException.class, () -> FinishedLengthRules.parse(rules));
        }
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        FinishedLengthRules.evaluate(
                                RULES, request("0", "450"), arrangement("200", "240")));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        FinishedLengthRules.evaluate(
                                RULES, request("350.0001", "450"), arrangement("200", "240")));
    }

    /** 四类中的配饰跨度必须有实测依据, 不再按旧款式类别静默忽略. */
    @Test
    void accessorySpanIsMeasuredAndUnknownSpanPreventsCheckout() {
        var measured =
                Map.<String, Object>of(
                        "arrangement",
                        List.of(
                                item("pearl", "200"),
                                item("chain", "235"),
                                item("accessory", "5")));
        assertEquals(
                true,
                FinishedLengthRules.evaluate(RULES, request("350", "450"), measured).get("ready"));
        var unknown =
                Map.<String, Object>of(
                        "arrangement",
                        List.of(
                                item("pearl", "200"),
                                item("chain", "235"),
                                item("accessory", null)));
        assertEquals(
                false,
                FinishedLengthRules.evaluate(RULES, request("350", "450"), unknown).get("ready"));
    }

    /** 悬挂配饰不占周长, 未确认位置则阻止生产, 不根据名称推断. */
    @Test
    void hangingAccessoryRequiresExplicitLengthRole() {
        var accessory = new LinkedHashMap<String, Object>();
        accessory.put("componentType", "accessory");
        accessory.put("lengthRole", "HANGING");
        var arrangement =
                Map.<String, Object>of(
                        "arrangement",
                        List.of(item("pearl", "200"), item("chain", "240"), accessory));
        assertEquals(
                true,
                FinishedLengthRules.evaluate(RULES, request("350", "450"), arrangement)
                        .get("ready"));
        accessory.remove("lengthRole");
        assertEquals(
                false,
                FinishedLengthRules.evaluate(RULES, request("350", "450"), arrangement)
                        .get("ready"));
        assertThrows(
                IllegalArgumentException.class,
                () -> ComponentRules.parse("{\"lengthRole\":\"guess\"}"));
    }

    /** 创建实测输入, 不使用任何AI估计字段. */
    private CustomQuoteRequest request(String neck, String target) {
        var request = new CustomQuoteRequest();
        request.setNeckCircumferenceMm(neck == null ? null : new BigDecimal(neck));
        request.setTargetLengthMm(target == null ? null : new BigDecimal(target));
        return request;
    }

    /** 测试明确包含未知跨度的扣件, 以验证由工艺规则统一计量. */
    private Map<String, Object> arrangement(String pearl, String chain) {
        return Map.of(
                "arrangement",
                List.of(item("pearl", pearl), item("chain", chain), item("clasp", null)));
    }

    /** 未知尺寸使用null保留原义, 不转换为零. */
    private Map<String, Object> item(String type, String size) {
        var item = new LinkedHashMap<String, Object>();
        item.put("componentType", type);
        item.put("sizeMm", size == null ? null : new BigDecimal(size));
        if ("accessory".equals(type)) {
            item.put("lengthRole", "INLINE");
        }
        return item;
    }
}
