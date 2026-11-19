/*
 *
 *      Copyright (c) 2018-2099, hehailong All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the openwtai.com developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: hehailong(845367567@qq.com)
 *
 */
package com.platform.config;

import com.platform.modules.mall.service.WechatPaymentGateway;
import com.platform.modules.mall.service.WechatV3PaymentGateway;
import com.wechat.pay.java.core.RSAPublicKeyConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.refund.RefundService;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * 装配直连商户 APIv3, 使用微信支付公钥验签.
 *
 * @author Binary Wang
 */
@Configuration
@Conditional(WxPayConfiguredCondition.class)
@EnableConfigurationProperties(WxPayProperties.class)
public class WxPayConfiguration {
    /** 明确配置的商户密钥和微信支付公钥缺失时不签发支付请求. */
    @Bean
    public WechatPaymentGateway wxService(WxPayProperties properties) {
        var config =
                new RSAPublicKeyConfig.Builder()
                        .merchantId(properties.getMchId())
                        .merchantSerialNumber(properties.getMerchantSerialNumber())
                        .privateKeyFromPath(properties.getPrivateKeyPath())
                        .apiV3Key(properties.getApiV3Key())
                        .publicKeyFromPath(properties.getPublicKeyPath())
                        .publicKeyId(properties.getPublicKeyId())
                        .build();
        return new WechatV3PaymentGateway(
                properties.getAppId(),
                properties.getMchId(),
                new JsapiServiceExtension.Builder().config(config).build(),
                new RefundService.Builder().config(config).build(),
                new NotificationParser(config));
    }
}
