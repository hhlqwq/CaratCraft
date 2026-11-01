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
package com.platform.common.xss;

import com.platform.common.utils.JsonContract;
import com.platform.common.utils.RestResponse;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 在进入MVC前验证JSON边界, 下游业务异常仍由全局异常处理器映射.
 *
 * @author hehailong
 */
public class XssFilter implements Filter {
    /** 仅预读取有界JSON, 不读取multipart、XML支付回调或其他二进制流. */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        var wrapped = new XssHttpServletRequestWrapper((HttpServletRequest) request);
        try {
            wrapped.prepareJsonBody();
        } catch (XssHttpServletRequestWrapper.JsonBodyTooLargeException error) {
            reject((HttpServletResponse) response, 413, error.getMessage());
            return;
        } catch (IllegalArgumentException error) {
            reject((HttpServletResponse) response, 400, "JSON格式、编码或层级无效");
            return;
        }
        chain.doFilter(wrapped, response);
    }

    /** 过滤阶段直接提供统一HTTP状态及RestResponse, 不输出请求正文或异常栈. */
    private void reject(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(JsonContract.write(RestResponse.fail(code, message)));
    }
}
