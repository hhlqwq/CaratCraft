package com.platform.modules.custom.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 商家可配置的单件用量及跨组件规则, 不虚构真实工艺限制. */
public record ComponentRules(
    int quantity, String unit, Set<String> incompatible, Set<String> required) {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  /** 默认一件用一单位, 支持虚拟选项用量0, 无规则时不增加臆测限制. */
  public static ComponentRules parse(String json) {
    if (json == null || json.isBlank()) return new ComponentRules(1, "件", Set.of(), Set.of());
    try {
      if (json.length() > 8192) throw new IllegalArgumentException();
      JsonNode root = MAPPER.readTree(json);
      if (!root.isObject()) throw new IllegalArgumentException();
      JsonNode count = root.path("stockQuantity");
      int quantity = count.isMissingNode() ? 1 : count.intValue();
      if ((!count.isMissingNode() && (!count.isIntegralNumber() || !count.canConvertToInt()))
          || quantity < 0
          || quantity > 10000) throw new IllegalArgumentException();
      String unit = root.path("stockUnit").asText("件");
      if (unit.isBlank() || unit.length() > 10) throw new IllegalArgumentException();
      for (String key : List.of("diameterMm", "spanMm", "lengthMm")) {
        JsonNode value = root.path(key);
        if (!value.isMissingNode() && !value.isNull()) {
          if (!value.isNumber()) throw new IllegalArgumentException();
          var size = value.decimalValue();
          if (size.signum() <= 0
              || size.compareTo(new java.math.BigDecimal("3000")) > 0
              || size.scale() > 3) throw new IllegalArgumentException();
        }
      }
      for (String key : List.of("shape", "luster", "blemish")) {
        JsonNode value = root.path(key);
        if (!value.isMissingNode()
            && (!value.isTextual() || value.asText().isBlank() || value.asText().length() > 30))
          throw new IllegalArgumentException();
      }
      return new ComponentRules(
          quantity, unit, keys(root.path("incompatibleWith")), keys(root.path("requires")));
    } catch (Exception failure) {
      throw new IllegalArgumentException("材料规则格式无效, 请检查单位、用量、尺寸、品质和搭配规则");
    }
  }

  /** 配件稳定标识用于工艺规则, 与数据库自增ID和展示名解耦. */
  public static String key(CustomComponentOptionEntity option) {
    return option.getComponentType() + ":" + option.getOptionCode();
  }

  /** 报价和下单共同执行互斥与依赖, 不只在页面隐藏不可选组合. */
  public static void validate(List<CustomComponentOptionEntity> selected) {
    Set<String> keys = new HashSet<>();
    selected.forEach(option -> keys.add(key(option)));
    for (CustomComponentOptionEntity option : selected) {
      ComponentRules rules = parse(option.getMaterialParams());
      if (rules.incompatible().stream().anyMatch(keys::contains)) {
        throw new IllegalArgumentException(option.getName() + "与所选材料不能组合");
      }
      if (!keys.containsAll(rules.required())) {
        throw new IllegalArgumentException(option.getName() + "缺少商家要求的搭配材料");
      }
    }
  }

  /** 每项规则限定为组件类型和代码, 拒绝任意脚本或无限数组. */
  private static Set<String> keys(JsonNode values) {
    if (values.isMissingNode()) return Set.of();
    if (!values.isArray() || values.size() > 100) throw new IllegalArgumentException();
    Set<String> result = new HashSet<>();
    for (JsonNode value : values) {
      if (!value.isTextual()
          || !value.asText().matches("[A-Za-z0-9_-]{1,32}:[A-Za-z0-9_.-]{1,64}")) {
        throw new IllegalArgumentException();
      }
      result.add(value.asText());
    }
    return Set.copyOf(result);
  }
}
