package com.platform.modules.custom.service;

import java.util.LinkedHashSet;
import java.util.List;

/** 项链材料仅使用四类稳定身份, 属性和历史分类不能作为新的材料组. */
public final class MaterialTypes {
    public static final List<String> ALL = List.of("pearl", "clasp", "accessory", "chain");

    /** 工具类不允许实例化. */
    private MaterialTypes() {}

    /** 拒绝未知及旧分类, 不在交易时猜测映射材料身份. */
    public static String require(String type) {
        if (type == null || !ALL.contains(type)) {
            throw new IllegalArgumentException("材料类型仅支持pearl、clasp、accessory、chain, 旧材料请停用后重新配置");
        }
        return type;
    }

    /** 按统一顺序保存必选组, 拒绝重复、空项和旧分类. */
    public static String required(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        var selected = new LinkedHashSet<String>();
        for (String type : value.split(",", -1)) {
            if (!selected.add(require(type.trim()))) {
                throw new IllegalArgumentException("必选材料组不能重复");
            }
        }
        return String.join(",", ALL.stream().filter(selected::contains).toList());
    }
}
