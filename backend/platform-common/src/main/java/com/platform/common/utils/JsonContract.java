package com.platform.common.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

/** 固定业务JSON契约, 不启用多态且拒绝隐式数值转换及歧义输入. */
public final class JsonContract {
    private static final JsonMapper MAPPER =
            JsonMapper.builder()
                    .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                    .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                    .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
                    .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
                    .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
                    .build();

    /** 工具类不允许实例化. */
    private JsonContract() {}

    /** 读取明确类型, 输入错误由业务入口映射到相应状态. */
    public static <T> T read(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException error) {
            throw new IllegalArgumentException("JSON格式无效", error);
        }
    }

    /** 读取对象树并保留数值类型, 拒绝空根和数组. */
    public static JsonNode object(String json) {
        var root = read(json, JsonNode.class);
        if (root == null || !root.isObject()) {
            throw new IllegalArgumentException("JSON须为对象");
        }
        return root;
    }

    /** 序列化服务端确认的业务值, 不输出类型元数据. */
    public static String write(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("业务数据无法序列化", error);
        }
    }
}
