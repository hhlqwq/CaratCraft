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
package com.platform.modules.sys.controller;

import com.platform.common.annotation.SysLog;
import com.platform.common.utils.AesUtil;
import com.platform.common.utils.RestResponse;
import com.platform.modules.sys.entity.SysUserEntity;
import com.platform.modules.sys.form.SysLoginForm;
import com.platform.modules.sys.service.StaffPasswordRecovery;
import com.platform.modules.sys.service.SysCaptchaService;
import com.platform.modules.sys.service.SysUserService;
import com.platform.modules.sys.service.SysUserTokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.apache.commons.io.IOUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * 登录相关
 *
 * @author hehailong
 */
@Tag(name = "SysLoginController|登录相关")
@RequiredArgsConstructor
@RestController
public class SysLoginController extends AbstractController {

    private final SysUserService sysUserService;
    private final SysUserTokenService sysUserTokenService;
    private final SysCaptchaService sysCaptchaService;
    private final StaffPasswordRecovery recovery;

    /** 通过图形验证码后向员工档案中的联系方式发码. */
    @PostMapping("/sys/password-recovery/send")
    public RestResponse<String> sendRecoveryCode(
            @RequestBody RecoveryRequest form, HttpServletRequest request) {
        if (!sysCaptchaService.validate(form.uuid(), form.captcha())) {
            return RestResponse.fail("验证码不正确");
        }
        if (!recovery.send(form.userName(), form.channel(), request.getRemoteAddr())) {
            return RestResponse.fail("该找回渠道尚未启用，请联系管理员");
        }
        return RestResponse.ok("若该账号可自助找回，验证码将发到已登记的联系方式", null);
    }

    /** 校验一次性验证码并更新密码, 不为找回请求签发会话. */
    @PostMapping("/sys/password-recovery/reset")
    public RestResponse<String> recoverPassword(
            @RequestBody RecoveryRequest form, HttpServletRequest request) {
        return recovery.reset(
                        form.userName(),
                        form.channel(),
                        form.code(),
                        form.newPassword(),
                        request.getRemoteAddr())
                ? RestResponse.ok()
                : RestResponse.fail("验证码错误或已失效，请重新获取或联系管理员");
    }

    /** 找回请求以用户名和渠道定位现有档案. */
    public record RecoveryRequest(
            String userName,
            String channel,
            String uuid,
            String captcha,
            String code,
            String newPassword) {}

    /**
     * 验证码
     *
     * @param response response
     * @param uuid uuid
     */
    @Operation(summary = "验证码", description = "验证码")
    @GetMapping("captcha.jpg")
    public void captcha(HttpServletResponse response, String uuid) throws IOException {
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");

        // 获取图片验证码
        BufferedImage image = sysCaptchaService.getCaptcha(uuid);

        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "jpg", out);
        IOUtils.closeQuietly(out);
    }

    /**
     * 登录
     *
     * @param form 登录表单
     * @return RestResponse
     */
    @Operation(summary = "登录", description = "登录")
    @SysLog("登录")
    @PostMapping("/sys/login")
    public RestResponse<String> login(@RequestBody SysLoginForm form) {
        boolean captcha = sysCaptchaService.validate(form.getUuid(), form.getCaptcha());
        if (!captcha) {
            return RestResponse.fail("验证码不正确");
        }

        // 用户信息
        SysUserEntity user = sysUserService.queryByUserName(form.getUserName());
        String password;
        try {
            // AESUtil.desEncrypt解密后 password 为char[16], trim转为char[6]
            password = AesUtil.desEncrypt(form.getPassword()).trim();
        } catch (Exception e) {
            return RestResponse.fail("解密失败！");
        }

        // 账号不存在
        if (user == null) {
            return RestResponse.fail("账号或密码不正确");
        }

        // 账号锁定
        if (user.getStatus() == 0) {
            return RestResponse.fail("账号已被锁定,请联系管理员");
        }

        // 账号不存在
        if (user.getPassword() == null
                || user.getSalt() == null
                || !user.getPassword().equals(new Sha256Hash(password, user.getSalt()).toHex())) {
            return RestResponse.fail("账号或密码不正确");
        }

        // 生成token，并保存到数据库
        String token = sysUserTokenService.createToken(user.getUserId(), user.getPassword());
        if (token == null) {
            return RestResponse.fail("账号或密码已变更, 请重新登录");
        }

        return RestResponse.ok("登录成功", token);
    }

    /**
     * 退出系统
     *
     * @return RestResponse
     */
    @Operation(summary = "退出系统", description = "退出系统")
    @PostMapping("/sys/logout")
    public RestResponse<String> logout() {
        sysUserTokenService.logout(getUserId());
        return RestResponse.ok();
    }
}
