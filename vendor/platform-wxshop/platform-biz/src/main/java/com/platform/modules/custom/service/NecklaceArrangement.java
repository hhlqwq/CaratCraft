package com.platform.modules.custom.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
      JSONObject params =
          option.getMaterialParams() == null || option.getMaterialParams().isBlank()
              ? new JSONObject()
              : JSON.parseObject(option.getMaterialParams());
      boolean pearl = "pearl".equalsIgnoreCase(option.getComponentType());
      boolean isChain = "chain".equalsIgnoreCase(option.getComponentType());
      BigDecimal size = dimension(params, pearl ? "diameterMm" : isChain ? "lengthMm" : "spanMm");
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("optionId", id);
      item.put("position", arrangement.size());
      item.put("componentType", option.getComponentType());
      item.put("optionCode", option.getOptionCode());
      item.put("name", option.getName());
      item.put("sizeMm", size);
      item.put("assetUrl", option.getAssetUrl());
      if (pearl) {
        item.put("shape", params.getString("shape") == null ? "近正圆" : params.getString("shape"));
        item.put("luster", params.getString("luster") == null ? "极光" : params.getString("luster"));
        item.put(
            "blemish", params.getString("blemish") == null ? "微瑕" : params.getString("blemish"));
      }
      arrangement.add(item);
      if (size == null) complete = false;
      else if (isChain) chain = chain.add(size);
      else occupied = occupied.add(size);
    }
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("arrangement", arrangement);
    result.put("lengthComplete", complete && !ids.isEmpty());
    result.put("estimatedLengthMm", complete && !ids.isEmpty() ? occupied.max(chain) : null);
    result.put("lengthNotice", "长度为材料尺寸估算, 链材与串珠重叠部分不重复相加, 实际成品长度须由商家确认");
    return result;
  }

  /** 只接受商家配置的正数尺寸, 不取规格区间中点或用户传入尺寸. */
  private static BigDecimal dimension(JSONObject params, String key) {
    if (!params.containsKey(key) || params.get(key) == null) return null;
    try {
      BigDecimal value = new BigDecimal(String.valueOf(params.get(key)));
      if (value.signum() <= 0 || value.compareTo(new BigDecimal("3000")) > 0 || value.scale() > 3) {
        throw new IllegalArgumentException();
      }
      return value;
    } catch (RuntimeException failure) {
      throw new IllegalArgumentException("材料尺寸无效, 请商家维护" + key);
    }
  }
}
