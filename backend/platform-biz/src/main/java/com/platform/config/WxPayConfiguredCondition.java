package com.platform.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.stream.Stream;

/** 无商户配置时保持应用可启动, 仅停用微信支付. */
public class WxPayConfiguredCondition implements Condition {
    /** 只有直连商户所需 APIv3 凭据齐全才装配网关. */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        var settings = context.getEnvironment();
        return Stream.of(
                        "appId",
                        "mchId",
                        "apiV3Key",
                        "merchantSerialNumber",
                        "privateKeyPath",
                        "publicKeyPath",
                        "publicKeyId")
                .allMatch(key -> !settings.getProperty("wx.pay." + key, "").isBlank());
    }
}
