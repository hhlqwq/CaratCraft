package com.platform.modules.custom.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.platform.common.utils.JsonContract;
import com.platform.modules.custom.entity.CustomComponentOptionEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 冻结逐颗顺序和商家实测尺寸, 未知尺寸不推算为生产承诺. */
public final class NecklaceArrangement {
    /** 工具类不允许实例化. */
    private NecklaceArrangement() {}

    /** 按实例顺序复制公开制作参数, 价格和库存仍由报价服务统一计算. */
    public static Map<String, Object> describe(
            List<Long> ids, List<CustomComponentOptionEntity> options) {
        Map<Long, CustomComponentOptionEntity> indexed = new LinkedHashMap<>();
        options.forEach(option -> indexed.put(option.getId(), option));
        List<Map<String, Object>> arrangement = new ArrayList<>();
        BigDecimal occupied = BigDecimal.ZERO;
        BigDecimal chain = BigDecimal.ZERO;
        boolean complete = true;
        for (Long id : ids) {
            CustomComponentOptionEntity option = indexed.get(id);
            JsonNode params =
                    option.getMaterialParams() == null || option.getMaterialParams().isBlank()
                            ? JsonContract.object("{}")
                            : JsonContract.object(option.getMaterialParams());
            boolean pearl = "pearl".equalsIgnoreCase(option.getComponentType());
            boolean isChain = "chain".equalsIgnoreCase(option.getComponentType());
            BigDecimal size =
                    dimension(params, pearl ? "diameterMm" : isChain ? "lengthMm" : "spanMm");
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("optionId", id);
            item.put("position", arrangement.size());
            item.put("componentType", option.getComponentType());
            item.put("optionCode", option.getOptionCode());
            item.put("name", option.getName());
            item.put("sizeMm", size);
            item.put("assetUrl", option.getAssetUrl());
            if ("accessory".equals(option.getComponentType())) {
                item.put("lengthRole", params.path("lengthRole").asText(null));
            }
            if (pearl) {
                item.put(
                        "shape",
                        params.path("shape").asText(null) == null
                                ? "近正圆"
                                : params.path("shape").asText(null));
                item.put(
                        "luster",
                        params.path("luster").asText(null) == null
                                ? "极光"
                                : params.path("luster").asText(null));
                item.put(
                        "blemish",
                        params.path("blemish").asText(null) == null
                                ? "微瑕"
                                : params.path("blemish").asText(null));
            }
            arrangement.add(item);
            if (size == null) {
                complete = false;
            } else if (isChain) {
                chain = chain.add(size);
            } else {
                occupied = occupied.add(size);
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("arrangement", arrangement);
        result.put("lengthComplete", complete && !ids.isEmpty());
        result.put("estimatedLengthMm", complete && !ids.isEmpty() ? occupied.max(chain) : null);
        result.put("lengthNotice", "长度为材料尺寸估算, 链材与串珠重叠部分不重复相加, 实际成品长度须由商家确认");
        return result;
    }

    /** 只接受商家配置的正数尺寸, 不取规格区间中点或用户传入尺寸. */
    private static BigDecimal dimension(JsonNode params, String key) {
        if (!params.hasNonNull(key)) {
            return null;
        }
        try {
            if (!params.get(key).isNumber()) {
                throw new IllegalArgumentException();
            }
            BigDecimal value = params.get(key).decimalValue();
            if (value.signum() <= 0
                    || value.compareTo(new BigDecimal("3000")) > 0
                    || value.scale() > 3) {
                throw new IllegalArgumentException();
            }
            return value;
        } catch (RuntimeException failure) {
            throw new IllegalArgumentException("材料尺寸无效, 请商家维护" + key);
        }
    }
}
