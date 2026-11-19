package com.platform.common.utils;

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** 配置完成后可并发使用的日期格式器, 同步保护解析及格式化时复用的内部日历. */
public final class ThreadSafeDateFormat extends SimpleDateFormat {
    private static final long serialVersionUID = 1L;

    /** 使用固定格式及根区域创建格式器, 时区在发布实例前完成配置. */
    public ThreadSafeDateFormat(String pattern) {
        super(pattern, Locale.ROOT);
    }

    /** 独占内部日历直至日期输出完毕. */
    @Override
    public synchronized StringBuffer format(
            Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        return super.format(date, toAppendTo, fieldPosition);
    }

    /** 独占内部日历直至文本解析完毕. */
    @Override
    public synchronized Date parse(String source, ParsePosition position) {
        return super.parse(source, position);
    }

    /** 在没有进行中的格式化操作时复制配置及内部日历. */
    @Override
    public synchronized Object clone() {
        return super.clone();
    }
}
