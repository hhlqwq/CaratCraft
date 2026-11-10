package com.platform.common.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Date;

/**
 * 将秒或毫秒时间戳格式化为标准日期文本.
 *
 * <p>格式器按线程隔离, 多个请求并发序列化时不会共享 SimpleDateFormat.
 */
public class JsonDateSerializer extends JsonSerializer<Long> {
    private static final long MILLISECOND_THRESHOLD = 100_000_000_000L;
    private final ThreadSafeDateFormat dateFormat = new ThreadSafeDateFormat("yyyy-MM-dd");

    /** 输出日期文本, 空值保持为空而不是写入错误时间. */
    @Override
    public void serialize(Long value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        gen.writeString(dateFormat.format(new Date(toMillis(value))));
    }

    /** 按数值范围判断秒或毫秒, 不根据字符串长度猜测单位. */
    private long toMillis(long value) {
        return Math.abs(value) < MILLISECOND_THRESHOLD ? value * 1000L : value;
    }
}
