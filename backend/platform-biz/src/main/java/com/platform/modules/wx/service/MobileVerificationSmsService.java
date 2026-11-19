package com.platform.modules.wx.service;

import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.platform.common.utils.Constant;
import com.platform.common.utils.SmsUtil;
import com.platform.common.utils.StringUtils;
import com.platform.modules.sys.entity.SmsConfig;
import com.platform.modules.sys.service.SysConfigService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.Map;

/** 向商家配置的短信平台发送绑定手机号验证码. */
@Service
@RequiredArgsConstructor
@Slf4j
public class MobileVerificationSmsService {
    private final SysConfigService settings;

    /** 读取当前商家短信设置, 每次请求使用同一份配置校验及发送. */
    public SmsConfig configuration() {
        return settings.getConfigObject(Constant.SMS_CONFIG_KEY, SmsConfig.class);
    }

    /** 校验当前短信平台配置, 有缺失时返回可展示说明. */
    public String configurationError(SmsConfig config) {
        if (config == null) {
            return "请先配置短信平台信息";
        }
        if (StringUtils.isNullOrEmpty(config.getSign())) {
            return "请先配置短信平台签名";
        }
        if (Integer.valueOf(2).equals(config.getType())) {
            if (StringUtils.isNullOrEmpty(config.getAccessKeyId())) {
                return "请先配置短信平台AccessKeyId";
            }
            if (StringUtils.isNullOrEmpty(config.getAccessSecret())) {
                return "请先配置短信平台AccessSecret";
            }
            if (StringUtils.isNullOrEmpty(config.getTemplateCode())) {
                return "请先配置短信平台模板CODE";
            }
            return null;
        }
        if (Integer.valueOf(1).equals(config.getType())) {
            if (config.getAppid() <= 0) {
                return "请先配置短信平台APPID";
            }
            if (StringUtils.isNullOrEmpty(config.getAppkey())) {
                return "请先配置短信平台KEY";
            }
            if (config.getTemplateId() == null || config.getTemplateId() <= 0) {
                return "请先配置短信平台模板ID";
            }
            return null;
        }
        return "请选择短信平台类型";
    }

    /** 按配置的腾讯云或阿里云模板发送六位验证码, 成功返回null. */
    public String send(SmsConfig config, String phone, String code) {
        try {
            if (Integer.valueOf(2).equals(config.getType())) {
                var response =
                        SmsUtil.aliSendSms(
                                config.getAccessKeyId(),
                                config.getAccessSecret(),
                                phone,
                                config.getTemplateCode(),
                                Map.of("code", code),
                                config.getSign());
                return response != null
                                && response.getBody() != null
                                && "OK".equals(response.getBody().getCode())
                        ? null
                        : "短信发送失败";
            }
            SmsSingleSenderResult result =
                    new SmsSingleSender(config.getAppid(), config.getAppkey())
                            .sendWithParam(
                                    "86",
                                    phone,
                                    config.getTemplateId(),
                                    new String[] {code},
                                    config.getSign(),
                                    "",
                                    "");
            return result != null && result.result == 0 ? null : "短信发送失败";
        } catch (Exception error) {
            log.warn(
                    "短信平台调用失败, type={}, errorType={}",
                    config.getType(),
                    error.getClass().getSimpleName());
            return "短信发送失败";
        }
    }
}
