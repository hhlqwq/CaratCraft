package com.platform.support;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 校验动态响应的实际容器类型, 不以未检查泛型转换掩盖格式错误. */
public final class ResponseAssertions {
    /** 仅提供独立断言操作, 不创建共享可变测试状态. */
    private ResponseAssertions() {}

    /** 校验列表及每行Map, 保留原记录引用和业务断言检查的真实值. */
    public static List<Map<?, ?>> records(Object value) {
        List<?> values = assertInstanceOf(List.class, value);
        List<Map<?, ?>> records = new ArrayList<>();
        for (Object item : values) {
            Map<?, ?> record = assertInstanceOf(Map.class, item);
            records.add(record);
        }
        return List.copyOf(records);
    }
}
