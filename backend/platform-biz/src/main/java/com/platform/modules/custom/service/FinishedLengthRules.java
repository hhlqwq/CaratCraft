package com.platform.modules.custom.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.platform.common.utils.JsonContract;
import com.platform.modules.custom.dto.CustomQuoteRequest;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 明确区分实测颈围、目标成品长度和商家工艺估算, 不将AI建议作为实测值. */
public final class FinishedLengthRules {
    /** 工具类不允许实例化. */
    private FinishedLengthRules() {}

    /** 保存时校验完整规则, 空规则允许暂存但不能据此下单. */
    public static JsonNode parse(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        JsonNode rules;
        try {
            rules = JsonContract.object(json);
        } catch (RuntimeException error) {
            throw new IllegalArgumentException("成品长度规则须为JSON对象");
        }
        if (rules == null
                || (!"SUM".equals(rules.path("mode").asText())
                        && !"OVERLAY_MAX".equals(rules.path("mode").asText()))) {
            throw new IllegalArgumentException("长度计算方式须为SUM或OVERLAY_MAX");
        }
        for (String field : List.of("claspMm", "connectionMm", "lossMm", "toleranceMm")) {
            decimal(rules.get(field), field, true);
        }
        if (rules.has("spacingMm")) {
            decimal(rules.get("spacingMm"), "材料间距", true);
        }
        BigDecimal minimum = decimal(rules.get("minLengthMm"), "minLengthMm", false);
        BigDecimal maximum = decimal(rules.get("maxLengthMm"), "maxLengthMm", false);
        if (minimum.compareTo(maximum) > 0) {
            throw new IllegalArgumentException("成品长度下限不能大于上限");
        }
        return rules;
    }

    /** 报价和下单共用同一套成品长度及链身容量校验. */
    public static Map<String, Object> evaluate(
            String json, CustomQuoteRequest request, Map<String, Object> arrangement) {
        Map<String, Object> result = new LinkedHashMap<>();
        BigDecimal neck = request.getNeckCircumferenceMm();
        BigDecimal target = request.getTargetLengthMm();
        if (neck != null) {
            neck = decimal(neck, "实测颈围", false);
        }
        if (target != null) {
            target = decimal(target, "目标成品长度", false);
        }
        result.put("neckCircumferenceMm", neck);
        result.put("targetLengthMm", target);
        JsonNode rules = parse(json);
        result.put("rules", rules);
        result.put("ready", false);
        if (rules == null) {
            result.put("notice", "请商家补齐成品长度工艺规则");
            return result;
        }

        BigDecimal minimumSpacing = rules.path("spacingMm").decimalValue();
        BigDecimal spacing = request.getLayoutSpacingMm() == null
                ? minimumSpacing
                : decimal(request.getLayoutSpacingMm(), "材料间距", true).max(minimumSpacing);
        BigDecimal offset = request.getLayoutOffsetMm() == null ? BigDecimal.ZERO : request.getLayoutOffsetMm();
        decimal(offset.abs(), "搭配偏移", true);
        MaterialUsage usage = measure(arrangement, spacing);
        BigDecimal min = rules.path("minLengthMm").decimalValue();
        BigDecimal max = rules.path("maxLengthMm").decimalValue();
        BigDecimal tolerance = rules.path("toleranceMm").decimalValue();
        BigDecimal hardware =
                rules.path("claspMm")
                        .decimalValue()
                        .max(usage.clasp())
                        .add(usage.extender())
                        .add(rules.path("connectionMm").decimalValue())
                        .subtract(rules.path("lossMm").decimalValue());
        boolean overlay = "OVERLAY_MAX".equals(rules.path("mode").asText());
        BigDecimal material =
                overlay ? usage.inline().max(usage.chain()) : usage.inline().add(usage.chain());
        BigDecimal finished = material.add(hardware);
        BigDecimal limit = (target == null ? max : target.add(tolerance).min(max)).subtract(hardware);
        if (overlay && usage.chain().signum() > 0) {
            limit = limit.min(usage.chain());
        }
        BigDecimal remaining = limit.subtract(usage.occupied());
        if (usage.placedCount() > 0) {
            remaining = remaining.subtract(offset.abs().multiply(BigDecimal.valueOf(2)));
        }
        if (!overlay) {
            // 垂挂件复用链身挂点, 串接件才从串接预算扣减链材长度.
            BigDecimal inlineRemaining = limit.subtract(usage.chain()).subtract(usage.inline());
            remaining = remaining.min(inlineRemaining);
        }
        boolean capacityReady = usage.complete() && remaining.signum() >= 0;
        result.put("capacityReady", capacityReady);
        result.put("remainingCapacityMm", usage.complete() ? remaining.max(BigDecimal.ZERO) : null);
        result.put("placedCount", usage.placedCount());
        result.put("spacingMm", spacing);
        result.put("layoutOffsetMm", offset);
        if (usage.complete() && material.signum() > 0) {
            result.put("materialLengthMm", material);
            result.put("estimatedFinishedLengthMm", finished);
            if (neck != null) {
                result.put("wearingClearanceMm", finished.subtract(neck));
            }
        }
        String reason;
        if (!usage.complete()) {
            reason = "部分材料缺少穿孔尺寸、挂点或工艺配置，请联系商家补齐";
        } else if (!capacityReady) {
            reason = "材料、间距或位置超出链身可用范围，请调整或换链";
        } else if (target == null) {
            reason = "请填写目标成品长度";
        } else if (neck != null && target.compareTo(neck) <= 0) {
            reason = "目标成品长度须大于实测颈围";
        } else if (target.compareTo(min) < 0 || target.compareTo(max) > 0) {
            reason = "目标长度超出商家可制作范围";
        } else if (material.signum() <= 0 || finished.compareTo(min) < 0
                || finished.compareTo(max) > 0 || finished.subtract(target).abs().compareTo(tolerance) > 0) {
            reason = "当前成品估算与目标长度不符，请调整材料或联系商家";
        } else {
            result.put("ready", true);
            reason = "长度符合商家配置的制作容差";
        }
        result.put("notice", reason);
        return result;
    }

    /** 汇总生产长度与物理占位, 悬挂件只占挂点及作业空间. */
    private static MaterialUsage measure(Map<String, Object> arrangement, BigDecimal spacing) {
        BigDecimal chain = BigDecimal.ZERO;
        BigDecimal inline = BigDecimal.ZERO;
        BigDecimal hanging = BigDecimal.ZERO;
        BigDecimal clasp = BigDecimal.ZERO;
        BigDecimal extender = BigDecimal.ZERO;
        int inlineCount = 0;
        int hangingCount = 0;
        boolean complete = true;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items =
                (List<Map<String, Object>>) arrangement.get("arrangement");
        for (var item : items == null ? List.<Map<String, Object>>of() : items) {
            String type = String.valueOf(item.get("componentType")).toLowerCase(java.util.Locale.ROOT);
            Object role = item.get("lengthRole");
            BigDecimal size = (BigDecimal) item.get("sizeMm");
            BigDecimal span = (BigDecimal) item.getOrDefault("axialMm", size);
            if ("clasp".equals(type)) {
                if ("EXTENDER".equals(item.get("claspRole"))) {
                    if (size == null) complete = false;
                    else extender = extender.add(size);
                } else if (size != null) {
                    clasp = clasp.add(size);
                }
            } else if ("HANGING".equals(role)) {
                BigDecimal mount = (BigDecimal) item.get("mountSpanMm");
                if (mount == null) {
                    // 既有吊坠的实测跨度保守计入挂点空间, 不加入成品周长.
                    mount = size;
                }
                if (mount == null) {
                    complete = false;
                } else {
                    hanging = hanging.add(mount);
                }
                hangingCount++;
            } else if (span == null || ("accessory".equals(type) && !"INLINE".equals(role))) {
                complete = false;
            } else if ("chain".equals(type)) {
                chain = chain.add(span);
            } else {
                inline = inline.add(span);
                inlineCount++;
            }
        }
        BigDecimal occupied = inline.add(hanging)
                .add(spacing.multiply(BigDecimal.valueOf(Math.max(0, inlineCount + hangingCount - 1))));
        inline = inline.add(spacing.multiply(BigDecimal.valueOf(Math.max(0, inlineCount - 1))));
        return new MaterialUsage(chain, inline, occupied, clasp, extender, inlineCount + hangingCount, complete);
    }

    /** 制作周长与链上占位分别保留, 便于容量查询和最终交易复验. */
    private record MaterialUsage(
            BigDecimal chain, BigDecimal inline, BigDecimal occupied, BigDecimal clasp, BigDecimal extender,
            int placedCount, boolean complete) {}


    /** 统一毫米精度与范围校验, 空值和非数字不能被隐式当成零. */
    private static BigDecimal decimal(Object raw, String label, boolean zeroAllowed) {
        try {
            BigDecimal value =
                    raw instanceof JsonNode node && node.isNumber()
                            ? node.decimalValue()
                            : raw instanceof BigDecimal number ? number : null;
            if (value == null
                    || value.signum() < (zeroAllowed ? 0 : 1)
                    || value.compareTo(new BigDecimal("3000")) > 0
                    || value.scale() > 3) {
                throw new IllegalArgumentException();
            }
            return value;
        } catch (RuntimeException error) {
            throw new IllegalArgumentException(label + "须为有效毫米数值, 最多三位小数, 最大3000");
        }
    }
}
