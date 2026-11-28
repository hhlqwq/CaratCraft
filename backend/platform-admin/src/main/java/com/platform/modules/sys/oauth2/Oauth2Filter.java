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
package com.platform.modules.sys.oauth2;

import com.alibaba.fastjson.JSONObject;
import com.platform.common.utils.Constant;
import com.platform.common.utils.RestResponse;
import com.platform.modules.sys.entity.SysUserEntity;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;

/**
 * oauth2过滤器
 *
 * @author hehailong
 */
public class Oauth2Filter extends AuthenticatingFilter {

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        // 获取请求token
        String token = getRequestToken((HttpServletRequest) request);

        if (StringUtils.isBlank(token)) {
            return null;
        }

        return new Oauth2Token(token);
    }

    @Override
    protected boolean isAccessAllowed(
            ServletRequest request, ServletResponse response, Object mappedValue) {
        return ((HttpServletRequest) request).getMethod().equals(RequestMethod.OPTIONS.name());
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response)
            throws Exception {
        // 获取请求token，如果token不存在，直接返回401
        String token = getRequestToken((HttpServletRequest) request);
        if (StringUtils.isBlank(token)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json;charset=utf-8");
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
            httpResponse.setHeader("Access-Control-Allow-Origin", "*");

            String json = JSONObject.toJSONString(RestResponse.fail(401, "invalid token"));
            httpResponse.getWriter().print(json);

            return false;
        }

        return executeLogin(request, response);
    }

    /**
     * 执行登录, 会话已过期或停止时返回干净的 401, 避免堆栈逸出到页面 / Executes login; returns a clean 401 when the session is
     * expired or stopped.
     */
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response)
            throws Exception {
        try {
            if (!super.executeLogin(request, response)) {
                return false;
            }
            var user = (SysUserEntity) getSubject(request, response).getPrincipal();
            var httpRequest = (HttpServletRequest) request;
            String path = WebUtils.getPathWithinApplication(httpRequest);
            if (com.platform.modules.sys.entity.AccountTier.PLATFORM_ADMIN.equals(
                            user.getAccountType())
                    && !platformEndpoint(httpRequest.getMethod(), path)) {
                var denied = (HttpServletResponse) response;
                denied.setStatus(HttpServletResponse.SC_FORBIDDEN);
                denied.setContentType("application/json;charset=utf-8");
                denied.getWriter()
                        .print(JSONObject.toJSONString(RestResponse.fail(403, "平台超管无此接口权限")));
                return false;
            }
            if (!Boolean.TRUE.equals(user.getPasswordChangeRequired())
                    || ("POST".equals(httpRequest.getMethod())
                            && ("/sys/user/password".equals(path) || "/sys/logout".equals(path)))) {
                return true;
            }
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(428);
            httpResponse.setContentType("application/json;charset=utf-8");
            httpResponse
                    .getWriter()
                    .print(JSONObject.toJSONString(RestResponse.fail(428, "请先修改临时密码")));
            return false;
        } catch (SessionException e) {
            // 旧会话已过期(StoppedSessionException 等), 清理后要求重新登录 /
            // The stale session has expired; ask the client to re-authenticate.
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json;charset=utf-8");
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
            httpResponse.setHeader("Access-Control-Allow-Origin", "*");
            httpResponse
                    .getWriter()
                    .print(JSONObject.toJSONString(RestResponse.fail(401, "登录已过期，请重新登录")));
            return false;
        }
    }

    /** 超管请求仅能进入专用账号管理及本人会话的明确方法和路径. */
    private boolean platformEndpoint(String method, String path) {
        return switch (method) {
            case "GET" ->
                    java.util.Set.of(
                                    "/platform/merchant/list",
                                    "/sys/menu/nav",
                                    "/sys/user/info",
                                    "/platform/overview",
                                    "/platform/workspace",
                                    "/platform/monitor",
                                    "/platform/release",
                                    "/platform/jobs",
                                    "/platform/logs")
                            .contains(path);
            case "POST" ->
                    java.util.Set.of(
                                    "/platform/merchant/create",
                                    "/platform/merchant/status",
                                    "/platform/merchant/reset",
                                    "/platform/merchant/update",
                                    "/platform/workspace",
                                    "/platform/packages",
                                    "/platform/confirm",
                                    "/platform/restore",
                                    "/sys/user/password",
                                    "/sys/logout")
                            .contains(path);
            default -> false;
        };
    }

    @Override
    protected boolean onLoginFailure(
            AuthenticationToken token,
            AuthenticationException e,
            ServletRequest request,
            ServletResponse response) {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResponse.setContentType("application/json;charset=utf-8");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        try {
            // 处理登录失败的异常
            Throwable throwable = e.getCause() == null ? e : e.getCause();
            RestResponse<String> restResponse = RestResponse.fail(401, throwable.getMessage());

            String json = JSONObject.toJSONString(restResponse);
            httpResponse.getWriter().print(json);
        } catch (IOException ignored) {

        }

        return false;
    }

    /** 获取请求的token */
    private String getRequestToken(HttpServletRequest httpRequest) {
        // 从header中获取token
        String token = httpRequest.getHeader("token");

        // 如果header中不存在token，则从参数中获取token
        if (StringUtils.isBlank(token)) {
            token = httpRequest.getParameter("token");
        }
        if (Constant.NULL.equals(token)) {
            token = null;
        }
        return token;
    }
}
