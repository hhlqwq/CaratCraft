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
package com.platform.common.exception;

import com.platform.common.utils.RestResponse;

import lombok.extern.slf4j.Slf4j;

import me.chanjar.weixin.common.error.WxErrorException;

import org.apache.shiro.authz.AuthorizationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 异常处理器
 *
 * @author 何海龙
 */
@Slf4j
@RestControllerAdvice
public class BusinessExceptionHandler {

    /**
     * 处理自定义异常
     *
     * @param e 自定义异常
     * @return restResponse
     */
    @ExceptionHandler(BusinessException.class)
    public RestResponse<String> handleBusinessException(BusinessException e) {
        return RestResponse.fail(e.getCode(), e.getMessage());
    }

    /**
     * 参数格式化异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(NumberFormatException.class)
    public RestResponse<String> handleNumberFormatException(NumberFormatException e) {
        log.error(e.getMessage(), e);
        return RestResponse.fail("请求参数有误！");
    }

    /**
     * 路径不存在异常
     *
     * @param e 路径不存在异常
     * @return restResponse
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public RestResponse<String> handlerNoFoundException(Exception e) {
        log.error(e.getMessage(), e);
        return RestResponse.fail(HttpStatus.NOT_FOUND.value(), "路径不存在，请检查路径是否正确");
    }

    /** 将数据库唯一约束冲突映射为明确的业务错误. */
    @ExceptionHandler(DuplicateKeyException.class)
    public RestResponse<String> handleDuplicateKeyException(DuplicateKeyException e) {
        log.error(e.getMessage(), e);
        return RestResponse.fail("数据库中已存在该记录");
    }

    /** 将权限校验失败映射为协议错误响应. */
    @ExceptionHandler(AuthorizationException.class)
    public RestResponse<String> handleAuthorizationException(AuthorizationException e) {
        log.error(e.getMessage(), e);
        return RestResponse.fail(403, "没有权限，请联系管理员授权");
    }

    /** 参数错误返回400, 不在日志中输出请求体. */
    @ExceptionHandler({
        IllegalArgumentException.class,
        org.springframework.http.converter.HttpMessageNotReadableException.class,
        org.springframework.web.bind.MissingServletRequestParameterException.class,
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
    })
    public RestResponse<String> handleInvalidInput(Exception error) {
        return RestResponse.fail(
                400, error instanceof IllegalArgumentException ? error.getMessage() : "请求参数格式不正确");
    }

    /** 方法不支持保持HTTP405, 便于客户端区分错误请求与服务故障. */
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public RestResponse<String> handleMethodNotAllowed(Exception error) {
        return RestResponse.fail(405, "请求方法不支持");
    }

    /** 未匹配的静态资源与业务路由均保持404语义. */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public RestResponse<String> handleResourceNotFound(Exception error) {
        return RestResponse.fail(404, "请求资源不存在");
    }

    /** 业务状态冲突可由用户刷新后重试, 不误报为成功响应. */
    @ExceptionHandler(IllegalStateException.class)
    public RestResponse<String> handleConflict(IllegalStateException error) {
        return RestResponse.fail(409, error.getMessage());
    }

    /** 将未处理异常转换为统一错误响应, 不向客户端暴露堆栈. */
    @ExceptionHandler(Exception.class)
    public RestResponse<String> handleException(Exception e) {
        log.error("未处理异常: ", e);
        return RestResponse.fail(500, "服务器内部错误，请稍后再试");
    }

    /** 将微信SDK异常转换为当前接口的错误响应. */
    @ExceptionHandler(WxErrorException.class)
    public RestResponse<String> handleWxErrorException(WxErrorException e) {
        log.error(e.getMessage(), e);

        return RestResponse.fail(e.getError().getErrorCode(), e.getError().getErrorMsg());
    }
}
