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
package com.platform.common.aspect;

import com.platform.common.annotation.SysLog;
import com.platform.common.utils.HttpContextUtils;
import com.platform.common.utils.RestResponse;
import com.platform.modules.sys.form.SysLoginForm;
import com.platform.modules.sys.service.OperationAudit;

import lombok.RequiredArgsConstructor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/** 按实际返回或异常记录重要操作, 与事务内的手工审计去重. */
@RequiredArgsConstructor
@Aspect
@Component
public class SysLogAspect {
    private final OperationAudit audit;

    /** 正常响应的业务失败与抛出异常均明确记录失败, 不记录请求参数. */
    @Around("@annotation(com.platform.common.annotation.SysLog)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long start = System.currentTimeMillis();
        Object response;
        try {
            response = point.proceed();
        } catch (Throwable error) {
            try {
                record(
                        point,
                        start,
                        false,
                        error instanceof org.apache.shiro.authz.UnauthorizedException
                                ? "FORBIDDEN"
                                : "FAILED");
            } catch (RuntimeException auditFailure) {
                error.addSuppressed(auditFailure);
            }
            throw error;
        }
        boolean success = !(response instanceof RestResponse<?> value) || value.getCode() == 0;
        try {
            record(
                    point,
                    start,
                    success,
                    response instanceof RestResponse<?> value
                            ? String.valueOf(value.getCode())
                            : null);
        } catch (RuntimeException auditFailure) {
            org.slf4j.LoggerFactory.getLogger(SysLogAspect.class).error("操作审计持久化失败, 请检查数据库结构及可写状态");
        }
        return response;
    }

    /** 未被业务审计覆盖的重要系统异常和鉴权失败, 只记录固定摘要. */
    @org.aspectj.lang.annotation.AfterReturning(
            pointcut =
                    "execution(*"
                        + " com.platform.common.exception.BusinessExceptionHandler.handleException(..))"
                        + " || execution(*"
                        + " com.platform.common.exception.BusinessExceptionHandler.handleAuthorizationException(..))",
            returning = "response")
    public void recordSystemFailure(Object response) {
        var request = HttpContextUtils.getHttpServletRequest();
        if (request == null || Boolean.TRUE.equals(request.getAttribute(OperationAudit.RECORDED))) {
            return;
        }
        String code =
                response instanceof RestResponse<?> value ? String.valueOf(value.getCode()) : "500";
        try {
            audit.write(
                    "403".equals(code) ? "权限校验失败" : "系统请求异常",
                    "系统操作",
                    "SECURITY",
                    null,
                    "FAILED",
                    "请求未通过, 请核对账号权限或服务诊断",
                    code,
                    0,
                    null);
            request.setAttribute(OperationAudit.RECORDED, true);
        } catch (RuntimeException error) {
            org.slf4j.LoggerFactory.getLogger(SysLogAspect.class).error("系统异常审计持久化失败, 请检查数据库可写状态");
        }
    }

    /** 仅从明确的登录表单取尝试账号, 其他请求内容均不进入日志. */
    private void record(ProceedingJoinPoint point, long start, boolean success, String code) {
        var request = HttpContextUtils.getHttpServletRequest();
        if (success
                && request != null
                && Boolean.TRUE.equals(request.getAttribute(OperationAudit.RECORDED))) {
            return;
        }
        var method = ((MethodSignature) point.getSignature()).getMethod();
        String action = method.getAnnotation(SysLog.class).value();
        String attempted = null;
        for (Object argument : point.getArgs()) {
            if (argument instanceof SysLoginForm form) {
                attempted = form.getUserName();
            }
        }
        audit.write(
                action,
                attempted == null ? "系统操作" : "身份认证",
                attempted == null ? "CHANGE" : "LOGIN",
                null,
                success ? "SUCCESS" : "FAILED",
                success ? "操作完成" : "操作未完成",
                code,
                System.currentTimeMillis() - start,
                attempted);
        if (request != null) {
            request.setAttribute(OperationAudit.RECORDED, true);
        }
    }
}
