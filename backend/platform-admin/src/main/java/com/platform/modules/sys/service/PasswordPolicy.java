package com.platform.modules.sys.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

/** 仅由服务端部署配置确定密码策略, 未声明开发环境时采用生产规则. */
@Service
public class PasswordPolicy {
    private static final Set<String> COMMON =
            Set.of("123456", "123456789012", "Password123!", "Admin123456!");
    private final boolean local;

    /** 接收受信任部署配置, 不读取域名、请求头或客户端策略参数. */
    public PasswordPolicy(@Value("${caratcraft.environment:production}") String environment) {
        local = Set.of("local", "lan").contains(environment);
    }

    /** 返回可公开的校验规则, 前端仅用于一致提示. */
    public Map<String, Object> describe() {
        return Map.of(
                "local",
                local,
                "minLength",
                local ? 6 : 12,
                "maxLength",
                32,
                "message",
                local ? "请输入6到32位密码, 不含空格" : "请输入12到32位密码, 包含大写、小写、数字、符号中的至少三类, 不使用常用密码");
    }

    /** 校验本人改密、找回及生产临时凭据的同一安全要求. */
    public void validate(String password) {
        if (password == null || !password.matches(local ? "[!-~]{6,32}" : "[!-~]{12,32}")) {
            throw new IllegalArgumentException((String) describe().get("message"));
        }
        if (local) {
            return;
        }
        int classes = 0;
        for (String expression :
                new String[] {".*[A-Z].*", ".*[a-z].*", ".*[0-9].*", ".*[^A-Za-z0-9].*"}) {
            if (password.matches(expression)) {
                classes++;
            }
        }
        if (classes < 3
                || COMMON.contains(password)
                || password.toLowerCase(java.util.Locale.ROOT).contains("password")) {
            throw new IllegalArgumentException((String) describe().get("message"));
        }
    }

    /** 为本地测试保留便捷初始密码, 生产始终独立随机生成. */
    public String temporary() {
        return local ? "123456" : "Aa7!" + AdminCredentials.randomPassword();
    }
}
