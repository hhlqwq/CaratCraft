package com.platform.modules.mall.service;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Date;

/** 新人券使用独立来源和固定规则, 不改动历史注册领券活动. */
public final class WelcomeCoupon {
    public static final int SEND_TYPE = 8;
    public static final String SOURCE = "WELCOME_V1";
    public static final BigDecimal DISCOUNT = new BigDecimal("10.00");
    public static final BigDecimal MINIMUM = new BigDecimal("199.00");
    private static final ZoneId BUSINESS_TIME = ZoneId.of("Asia/Shanghai");

    /** 仅通过静态业务规则计算. */
    private WelcomeCoupon() {}

    /** 由发放时刻计算三个自然月, 月末按经营时区处理. */
    public static Date expiresAt(Date grantedAt) {
        return grantedAt == null ? null : Date.from(
                grantedAt.toInstant().atZone(BUSINESS_TIME).plusMonths(3).toInstant());
    }
}
