package com.platform.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 微信支付配置完整性条件 / Checks whether WeChat pay config is fully provided.
 * <p>
 * 未配置 appId/mchId/mchKey 时不装配支付 Bean, 允许应用在无支付配置的环境下启动 /
 * Pay beans are skipped when config is absent so the app can boot without them.
 * </p>
 */
public class WxPayConfiguredCondition implements Condition {

    /**
     * 判断支付配置是否完整 / Matches when all required pay properties are present.
     *
     * @param context 条件上下文 / condition context
     * @param metadata 注解元数据 / annotated type metadata
     * @return 配置是否完整 / true when fully configured
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String appId = context.getEnvironment().getProperty("wx.pay.appId");
        String mchId = context.getEnvironment().getProperty("wx.pay.mchId");
        String mchKey = context.getEnvironment().getProperty("wx.pay.mchKey");
        return StringUtils.isNotBlank(appId)
                && StringUtils.isNotBlank(mchId)
                && StringUtils.isNotBlank(mchKey);
    }
}
