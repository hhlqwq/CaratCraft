package com.platform.modules.sys.controller;

import com.platform.common.utils.RestResponse;
import com.platform.modules.sys.service.PasswordPolicy;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 为登录前及强制改密界面公开非敏感的服务器密码规则. */
@RestController
@RequiredArgsConstructor
public class PasswordPolicyController {
    private final PasswordPolicy policy;

    /** 只返回当前服务端已生效的规则. */
    @GetMapping("/sys/password-policy")
    public RestResponse<Map<String, Object>> policy() {
        return RestResponse.ok(policy.describe());
    }
}
