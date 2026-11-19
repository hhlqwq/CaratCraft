package com.platform.common.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Date;

/**
 * 将秒或毫秒时间戳格式化为标准日期文本.
 *
 * <p>格式器同步保护内部日期状态, 保留旧调用方的时间戳识别范围.
 *
 * @author zhuliyun
 * @version 1.0.0
 */
public class JsonDateSerializer extends JsonSerializer<Long> {
    private static final long MIN_SECONDS = -99_999_999_999L;
    private static final long MAX_SECONDS = 999_999_999_999L;
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
        return value >= MIN_SECONDS && value <= MAX_SECONDS ? value * 1000L : value;
    }
}
