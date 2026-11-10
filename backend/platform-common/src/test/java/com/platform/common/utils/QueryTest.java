package com.platform.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/** 通用分页参数回归, 不连接数据库. */
class QueryTest {
    /** 合法分页保留当前页、条数和偏移. */
    @Test
    void keepsValidPageAndLimit() {
        Map<String, Object> params = new HashMap<>();
        params.put("page", "2");
        params.put("limit", "20");
        Query<Object> query = new Query<>(params);
        assertEquals(2, query.getCurrPage());
        assertEquals(20, query.getLimit());
        assertEquals(20, query.get("offset"));
        assertEquals(2L, query.getPage().getCurrent());
        assertEquals(20L, query.getPage().getSize());
    }

    /** 缺省分页使用第一页和十条. */
    @Test
    void usesDefaultsWhenMissing() {
        Query<Object> query = new Query<>(new HashMap<>());
        assertEquals(1, query.getCurrPage());
        assertEquals(10, query.getLimit());
        assertEquals(0, query.get("offset"));
    }

    /** 非法数字、非正数和超过上限的每页条数明确拒绝. */
    @Test
    void rejectsInvalidBounds() {
        assertThrows(IllegalArgumentException.class, () -> new Query<>(Map.of("page", "abc")));
        assertThrows(IllegalArgumentException.class, () -> new Query<>(Map.of("page", "0")));
        assertThrows(IllegalArgumentException.class, () -> new Query<>(Map.of("limit", "-1")));
        assertThrows(IllegalArgumentException.class, () -> new Query<>(Map.of("limit", "101")));
    }

    /** 页码与条数乘积超出整数范围时拒绝负数偏移. */
    @Test
    void rejectsOffsetOverflow() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new Query<>(
                                Map.of("page", String.valueOf(Integer.MAX_VALUE), "limit", "100")));
    }
}
