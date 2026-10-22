package com.platform.modules.mall.service.support;

import com.platform.modules.mall.dto.GoodsAggregateDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** 商品维护、购物车和小程序共同使用的唯一具名SKU契约. */
public final class SkuContract {
    public static final int MAX_COMBINATIONS = 512;
    public static final int MAX_KEY_LENGTH = 2048;

    /** 工具类不创建实例. */
    private SkuContract() {}

    /** 解析有序的完整具名组合, 不接收旧数字格式或自动排序损坏数据. */
    public static Map<Integer, String> parse(String key) {
        if (key == null || key.length() > MAX_KEY_LENGTH) {
            throw new IllegalArgumentException("SKU规格组合为空或超过长度上限");
        }
        Map<Integer, String> values = new LinkedHashMap<>();
        if (key.isEmpty()) {
            return values;
        }
        int previous = 0;
        for (String part : key.split("\\|", -1)) {
            int separator = part.indexOf(':');
            if (separator <= 0 || !part.substring(0, separator).matches("[1-9][0-9]*")) {
                throw new IllegalArgumentException("SKU须使用按规格组ID升序的具名组合, 旧数据须先迁移");
            }
            int group = Integer.parseInt(part.substring(0, separator));
            String value = part.substring(separator + 1);
            if (group <= previous || value.isBlank() || !value.equals(value.trim())) {
                throw new IllegalArgumentException("SKU规格组重复、顺序错误或规格值为空");
            }
            values.put(group, value);
            previous = group;
        }
        return values;
    }

    /** 在生成组合前限制数量, 校验规格组和值的唯一性及保留分隔符. */
    public static Set<String> expectedKeys(List<GoodsAggregateDTO.SpecGroup> specifications) {
        Map<Integer, List<String>> groups = new TreeMap<>();
        int count = 1;
        for (GoodsAggregateDTO.SpecGroup group :
                specifications == null ? List.<GoodsAggregateDTO.SpecGroup>of() : specifications) {
            if (group == null
                    || group.getSpecificationId() == null
                    || group.getSpecificationId() < 1
                    || groups.containsKey(group.getSpecificationId())
                    || group.getValues() == null
                    || group.getValues().isEmpty()) {
                throw new IllegalArgumentException("规格组必须唯一且至少包含一个有效值");
            }
            List<String> values = new ArrayList<>();
            for (GoodsAggregateDTO.SpecValue value : group.getValues()) {
                String text = value == null ? null : value.getValue();
                if (text == null
                        || text.isBlank()
                        || text.length() > 50
                        || !text.equals(text.trim())
                        || text.contains("|")
                        || values.contains(text)) {
                    throw new IllegalArgumentException("规格值须唯一且非空, 不含首尾空格或分隔符|");
                }
                values.add(text);
            }
            if (count > MAX_COMBINATIONS / values.size()) {
                throw new IllegalArgumentException("SKU组合数量不能超过" + MAX_COMBINATIONS);
            }
            count *= values.size();
            groups.put(group.getSpecificationId(), values);
        }
        Set<String> keys = Set.of("");
        for (Map.Entry<Integer, List<String>> group : groups.entrySet()) {
            Set<String> next = new HashSet<>();
            for (String prefix : keys) {
                for (String value : group.getValue()) {
                    String key =
                            (prefix.isEmpty() ? "" : prefix + "|") + group.getKey() + ":" + value;
                    if (key.length() > MAX_KEY_LENGTH) {
                        throw new IllegalArgumentException("SKU组合长度不能超过" + MAX_KEY_LENGTH);
                    }
                    next.add(key);
                }
            }
            keys = next;
        }
        return keys;
    }

    /** 后台提交必须覆盖全部且仅有的组合, 防止只核对数量却保存错误SKU. */
    public static void validate(GoodsAggregateDTO aggregate) {
        Set<String> expected = expectedKeys(aggregate.getSpecs());
        List<GoodsAggregateDTO.SkuItem> items = aggregate.getSkuList();
        if ((items == null || items.isEmpty()) && expected.equals(Set.of(""))) {
            return;
        }
        Set<String> supplied = new HashSet<>();
        for (GoodsAggregateDTO.SkuItem item :
                items == null ? List.<GoodsAggregateDTO.SkuItem>of() : items) {
            if (item == null) {
                throw new IllegalArgumentException("SKU记录不能为空");
            }
            parse(item.getGoodsSpecificationIds());
            if (!expected.contains(item.getGoodsSpecificationIds())
                    || !supplied.add(item.getGoodsSpecificationIds())) {
                throw new IllegalArgumentException("SKU组合不存在或重复");
            }
            if (item.getGoodsNumber() != null
                            && (item.getGoodsNumber() < 0 || item.getGoodsNumber() > 16777215)
                    || item.getRetailPrice() != null && item.getRetailPrice().signum() < 0
                    || item.getMarketPrice() != null && item.getMarketPrice().signum() < 0) {
                throw new IllegalArgumentException("SKU库存或价格超出允许范围");
            }
        }
        if (!supplied.equals(expected)) {
            throw new IllegalArgumentException("SKU列表须完整覆盖规格组合");
        }
    }
}
