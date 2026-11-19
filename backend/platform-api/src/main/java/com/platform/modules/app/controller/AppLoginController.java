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

package com.platform.modules.app.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.platform.annotation.IgnoreAuth;
import com.platform.annotation.LoginUser;
import com.platform.common.utils.RestResponse;
import com.platform.common.utils.StringUtils;
import com.platform.common.validator.AbstractAssert;
import com.platform.modules.app.request.LoginAppRequest;
import com.platform.modules.app.request.LoginWxMpRequest;
import com.platform.modules.app.request.LoginWxPhoneRequest;
import com.platform.modules.app.request.LoginWxRequest;
import com.platform.modules.app.vo.LoginUserInfo;
import com.platform.modules.app.vo.LoginVo;
import com.platform.modules.mall.service.AccountLoginService;
import com.platform.modules.mall.service.LegalDocumentService;
import com.platform.modules.wx.entity.WxUserEntity;
import com.platform.modules.wx.service.WxUserService;
import com.platform.utils.JwtUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * APP登录授权
 *
 * @author hehailong
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/auth")
@Tag(name = "AppLoginController|APP登录接口")
public class AppLoginController extends AppBaseController {
    private final WxUserService userService;
    private final JwtUtils jwtUtils;
    private final WxMaService wxMaService;
    private final WxMpService wxMpService;
    private final LegalDocumentService legalDocuments;
    private final AccountLoginService accounts;

    /**
     * 微信小程序登录
     *
     * @param request JSON格式参数
     * @return RestResponse
     */
    @IgnoreAuth
    @PostMapping("LoginByMa")
    @Operation(summary = "微信小程序登录", description = "wx.login()每次返回的code只能使用一次")
    public RestResponse<LoginVo> loginByMa(@RequestBody LoginWxRequest request) {
        legalDocuments.requireCurrent(request.getLegalAcceptance());
        String code = request.getCode();
        AbstractAssert.isBlank(code, "登录失败: code为空");
        try {
            WxMaJscode2SessionResult session = wxMaService.getUserService().getSessionInfo(code);
            return loginResult(
                    accounts.login(
                            true,
                            session.getOpenid(),
                            session.getUnionid(),
                            request.getLegalAcceptance()));
        } catch (WxErrorException error) {
            log.warn("微信身份交换失败");
            return RestResponse.fail("登录失败, 请重试");
        }
    }

    /**
     * 微信小程序手机号授权
     *
     * @param request JSON格式参数
     * @return RestResponse
     */
    @PostMapping("LoginByMaPhone")
    @Operation(summary = "微信小程序手机号授权", description = "wx.login()每次返回的code只能使用一次")
    public RestResponse<String> loginByMaPhone(
            @LoginUser WxUserEntity loginUser, @RequestBody LoginWxPhoneRequest request) {
        legalDocuments.requireAccepted(loginUser.getId());
        String code = request.getCode();
        AbstractAssert.isBlank(code, "手机号授权失败：code为空");
        Long userId = loginUser.getId();

        try {
            // 解密用户信息
            WxMaPhoneNumberInfo wxMpUser = wxMaService.getUserService().getPhoneNoInfo(code);

            WxUserEntity user = userService.getById(userId);
            user.setPhone(wxMpUser.getPurePhoneNumber());
            userService.update(
                    null,
                    new UpdateWrapper<WxUserEntity>()
                            .eq("ID", userId)
                            .set("PHONE", wxMpUser.getPurePhoneNumber()));
            return RestResponse.ok("手机号授权成功");
        } catch (WxErrorException e) {
            log.warn("手机号授权失败");
            return RestResponse.fail("手机号授权失败");
        }
    }

    /**
     * 微信公众号登录
     *
     * @param request JSON格式参数
     * @return RestResponse
     */
    @IgnoreAuth
    @PostMapping("loginByMp")
    @Operation(summary = "微信公众号登录", description = "根据微信code登录，每一个code只能使用一次")
    public RestResponse<LoginVo> loginByMp(@RequestBody LoginWxMpRequest request) {
        legalDocuments.requireCurrent(request.getLegalAcceptance());
        String code = request.getCode();
        AbstractAssert.isBlank(code, "登录失败：code为空");

        String mpAppId = request.getMpAppId();
        AbstractAssert.isBlank(mpAppId, "登录失败：mpAppId为空");

        if (!wxMpService.switchover(mpAppId)) {
            throw new IllegalArgumentException(String.format("未找到对应appid=[%s]的配置，请核实！", mpAppId));
        }

        try {
            WxOAuth2AccessToken access = wxMpService.getOAuth2Service().getAccessToken(code);
            WxOAuth2UserInfo identity = wxMpService.getOAuth2Service().getUserInfo(access, null);
            return loginResult(
                    accounts.login(
                            false,
                            identity.getOpenid(),
                            identity.getUnionId(),
                            request.getLegalAcceptance()));
        } catch (WxErrorException error) {
            log.warn("公众号身份交换失败");
            return RestResponse.fail("登录失败, 请重试");
        }
    }

    /**
     * APP端微信登录
     *
     * @param request JSON格式参数
     * @return RestResponse
     */
    @IgnoreAuth
    @PostMapping("AppLoginByWx")
    @Operation(summary = "APP端微信登录", description = "APP端微信登录")
    public RestResponse<LoginVo> appLoginByWx(@RequestBody LoginAppRequest request) {
        legalDocuments.requireCurrent(request.getLegalAcceptance());
        WxOAuth2AccessToken auth2AccessToken = request.getUserInfo();

        AbstractAssert.isNull(auth2AccessToken, "登录失败：userInfo为空");

        try {
            WxOAuth2UserInfo identity =
                    wxMpService.getOAuth2Service().getUserInfo(auth2AccessToken, null);
            return loginResult(
                    accounts.login(
                            false,
                            identity.getOpenid(),
                            identity.getUnionId(),
                            request.getLegalAcceptance()));
        } catch (WxErrorException error) {
            log.warn("App微信身份交换失败");
            return RestResponse.fail("登录失败, 请重试");
        }
    }

    /** 账户事务成功返回后签发令牌, 通过白名单返回本人界面资料. */
    private RestResponse<LoginVo> loginResult(WxUserEntity user) {
        String token = jwtUtils.generateToken(user.getId());
        if (StringUtils.isNullOrEmpty(token)) {
            return RestResponse.fail("登录失败, 请重试");
        }
        LoginVo result = new LoginVo();
        result.setToken(token);
        result.setUserId(user.getId());
        result.setUserInfo(LoginUserInfo.from(user));
        return RestResponse.ok(result);
    }

    /**
     * 创建调用jsapi时所需要的签名
     *
     * @param url url
     * @return RestResponse
     */
    @IgnoreAuth
    @GetMapping("/createJsapiSignature/{appid}")
    @Operation(
            summary = "创建调用jsapi时所需要的签名",
            description = "创建调用jsapi时所需要的签名",
            parameters = {
                @Parameter(
                        required = true,
                        in = ParameterIn.QUERY,
                        name = "url",
                        description = "url",
                        example = "https://example.com")
            })
    public RestResponse<WxJsapiSignature> createJsapiSignature(
            @PathVariable String appid, @RequestParam String url) {
        WxJsapiSignature data = null;
        try {
            data = wxMpService.switchoverTo(appid).createJsapiSignature(url);
        } catch (WxErrorException e) {
            log.warn("微信签名创建失败");
        }

        return RestResponse.ok(data);
    }

    /**
     * 保留旧路由的明确失败提示, 不再签发绕过协议确认的静默登录令牌.
     *
     * @param code code
     * @return RestResponse
     */
    @IgnoreAuth
    @GetMapping("/{code}")
    @Operation(
            summary = "旧静默登录入口已停用",
            description = "请使用包含主动协议确认的授权登录入口",
            parameters = {
                @Parameter(
                        in = ParameterIn.PATH,
                        required = true,
                        name = "code",
                        description = "code",
                        example = "oxaA11ulr9134oBL9Xscon5at_Gc")
            })
    public RestResponse<LoginVo> loginByCode(@PathVariable String code) {
        return RestResponse.fail("请通过授权登录页面阅读并主动确认用户协议及隐私政策");
    }
}
