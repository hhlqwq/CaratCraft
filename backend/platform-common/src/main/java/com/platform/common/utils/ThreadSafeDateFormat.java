package com.platform.common.utils;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 线程安全的日期格式器, 为每个线程复用独立实例, 避免并发请求共享 SimpleDateFormat.
 *
 * <p>Jackson 会跨请求复用自定义 DateFormat, 直接持有 SimpleDateFormat 会在并发序列化时产生错误日期.
 */
public class ThreadSafeDateFormat extends DateFormat {
    private final String pattern;
    private final ThreadLocal<SimpleDateFormat> formats;
    private volatile TimeZone timeZone = TimeZone.getDefault();

    /** 使用固定格式及根区域创建线程隔离的格式器. */
    public ThreadSafeDateFormat(String pattern) {
        this.pattern = pattern;
        this.formats = ThreadLocal.withInitial(this::createFormatter);
    }

    /** 为当前线程创建按当前时区配置的格式器. */
    private SimpleDateFormat createFormatter() {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.ROOT);
        formatter.setTimeZone(timeZone);
        return formatter;
    }

    /** 使用当前线程格式器输出日期. */
    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        return formats.get().format(date, toAppendTo, fieldPosition);
    }

    /** 使用当前线程格式器解析日期文本. */
    @Override
    public Date parse(String source, ParsePosition position) {
        return formats.get().parse(source, position);
    }

    /** 返回当前配置的时区. */
    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /** 更新时区后丢弃各线程旧格式器, 下次使用时按新时区重建. */
    @Override
    public void setTimeZone(TimeZone zone) {
        this.timeZone = zone;
        formats.remove();
    }

    /** 复制格式配置但不共享线程内的 SimpleDateFormat. */
    @Override
    public Object clone() {
        ThreadSafeDateFormat copy = new ThreadSafeDateFormat(pattern);
        copy.setTimeZone(timeZone);
        return copy;
    }
}
