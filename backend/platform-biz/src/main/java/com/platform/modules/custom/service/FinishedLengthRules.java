package com.platform.modules.custom.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
    public static JSONObject parse(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        JSONObject rules;
        try {
            rules = JSON.parseObject(json);
        } catch (RuntimeException error) {
            throw new IllegalArgumentException("成品长度规则须为JSON对象");
        }
        if (rules == null
                || (!"SUM".equals(rules.getString("mode"))
                        && !"OVERLAY_MAX".equals(rules.getString("mode")))) {
            throw new IllegalArgumentException("长度计算方式须为SUM或OVERLAY_MAX");
        }
        for (String field : List.of("claspMm", "connectionMm", "lossMm", "toleranceMm")) {
            decimal(rules.get(field), field, true);
        }
        BigDecimal minimum = decimal(rules.get("minLengthMm"), "minLengthMm", false);
        BigDecimal maximum = decimal(rules.get("maxLengthMm"), "maxLengthMm", false);
        if (minimum.compareTo(maximum) > 0) {
            throw new IllegalArgumentException("成品长度下限不能大于上限");
        }
        return rules;
    }

    /** 只使用服务端材料尺寸及商家规则计算, 不接受客户端估算长度和就绪标记. */
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
        JSONObject rules = parse(json);
        result.put("rules", rules);
        result.put("ready", false);
        String reason = "请商家补齐成品长度工艺规则";
        if (rules != null) {
            if (neck == null || target == null) {
                reason = "请填写实测颈围及目标成品长度";
            } else if (target.compareTo(neck) < 0) {
                reason = "目标成品长度不能小于实测颈围";
            } else if (target.compareTo(rules.getBigDecimal("minLengthMm")) < 0
                    || target.compareTo(rules.getBigDecimal("maxLengthMm")) > 0) {
                reason = "目标长度超出商家可制作范围";
            } else if (materialLength(rules, arrangement) == null) {
                reason = "材料实测尺寸不完整, 请联系商家补齐";
            } else {
                BigDecimal material = materialLength(rules, arrangement);
                BigDecimal finished =
                        material.add(rules.getBigDecimal("claspMm"))
                                .add(rules.getBigDecimal("connectionMm"))
                                .subtract(rules.getBigDecimal("lossMm"));
                result.put("materialLengthMm", material);
                result.put("estimatedFinishedLengthMm", finished);
                boolean ready =
                        finished.signum() > 0
                                && finished.subtract(target)
                                                .abs()
                                                .compareTo(rules.getBigDecimal("toleranceMm"))
                                        <= 0;
                result.put("ready", ready);
                reason = ready ? "长度符合商家配置的制作容差" : "排列估算长度与目标长度不符, 请调整材料或联系商家";
            }
        }
        result.put("notice", reason);
        return result;
    }

    /** 扣件按工艺规则计入, 配饰明确串接或悬挂后再计算周长, 未知归属拒绝猜测. */
    private static BigDecimal materialLength(JSONObject rules, Map<String, Object> arrangement) {
        BigDecimal chain = BigDecimal.ZERO;
        BigDecimal beads = BigDecimal.ZERO;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items =
                (List<Map<String, Object>>) arrangement.get("arrangement");
        if (items == null || items.isEmpty()) {
            return null;
        }
        boolean measured = false;
        for (var item : items) {
            String type =
                    String.valueOf(item.get("componentType")).toLowerCase(java.util.Locale.ROOT);
            if ("clasp".equals(type)) {
                continue;
            }
            if ("accessory".equals(type)) {
                if ("HANGING".equals(item.get("lengthRole"))) {
                    continue;
                }
                if (!"INLINE".equals(item.get("lengthRole"))) {
                    return null;
                }
            }
            if (item.get("sizeMm") == null) {
                return null;
            }
            BigDecimal size = new BigDecimal(String.valueOf(item.get("sizeMm")));
            measured = true;
            if ("chain".equals(type)) {
                chain = chain.add(size);
            } else {
                beads = beads.add(size);
            }
        }
        if (!measured) {
            return null;
        }
        return "SUM".equals(rules.getString("mode")) ? beads.add(chain) : beads.max(chain);
    }

    /** 统一毫米精度与范围校验, 空值和非数字不能被隐式当成零. */
    private static BigDecimal decimal(Object raw, String label, boolean zeroAllowed) {
        try {
            BigDecimal value = new BigDecimal(String.valueOf(raw));
            if (value.signum() < (zeroAllowed ? 0 : 1)
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
