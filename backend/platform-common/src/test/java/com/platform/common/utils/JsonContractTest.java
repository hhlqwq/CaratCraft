package com.platform.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/** 使用真实Jackson验证严格业务JSON, 不允许歧义及隐式数值转换. */
class JsonContractTest {
    /** 固定类型数量用于验证反序列化边界. */
    record Quantity(Integer quantity) {}

    /** 拒绝数组、空根、重复键及尾随文档. */
    @Test
    void rejectsAmbiguousJson() {
        for (String json : java.util.List.of("null", "[]", "{}{}", "{\"n\":1,\"n\":2}")) {
            assertThrows(IllegalArgumentException.class, () -> JsonContract.object(json));
        }
    }

    /** 小数和字符串不能成为整数用量, 合法小数金额保持精确. */
    @Test
    void preservesNumericTypes() {
        assertThrows(
                IllegalArgumentException.class,
                () -> JsonContract.read("{\"quantity\":1.5}", Quantity.class));
        assertThrows(
                IllegalArgumentException.class,
                () -> JsonContract.read("{\"quantity\":\"1\"}", Quantity.class));
        assertEquals(
                new BigDecimal("0.125"),
                JsonContract.object("{\"price\":0.125}").path("price").decimalValue());
    }
}
