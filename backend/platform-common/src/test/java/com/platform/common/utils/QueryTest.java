package com.platform.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.util.Map;

/** 验证公共分页参数在构建查询前被限制. */
class QueryTest {
    /** 非法大小和页码不能绕过上限, 最大合法偏移使用长整数. */
    @Test
    void boundsPageAndSize() {
        for (int size : new int[] {-1, 0, 101, Integer.MAX_VALUE}) {
            assertThrows(IllegalArgumentException.class, () -> new Query<>(Map.of("limit", size)));
        }
        assertThrows(IllegalArgumentException.class, () -> new Query<>(Map.of("page", 0)));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Query<>(Map.of("page", Integer.MAX_VALUE)));
        assertEquals(9999900L, new Query<>(Map.of("page", 100000, "limit", 100)).get("offset"));
    }
}
