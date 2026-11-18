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
package com.platform.modules.job.utils;

import com.platform.common.exception.BusinessException;
import com.platform.common.utils.SpringContextUtils;
import com.platform.modules.mall.service.OrderRefundService;
import com.platform.modules.mall.service.RefundReconciliationScheduler;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * 执行定时任务
 *
 * @author hehailong
 */
public class ScheduleRunnable implements Runnable {
    private Object target;
    private Method method;
    private String params;

    /** 解析定时任务目标组件、方法及调用参数. */
    public ScheduleRunnable(String beanName, String methodName, String params)
            throws NoSuchMethodException, SecurityException {
        requireNonRefundTarget(beanName);
        this.target = SpringContextUtils.getBean(beanName);
        this.params = params;

        if (StringUtils.isNotBlank(params)) {
            this.method = target.getClass().getDeclaredMethod(methodName, String.class);
        } else {
            this.method = target.getClass().getDeclaredMethod(methodName);
        }
    }

    /** 禁止可配置任务借组件名称或别名调用退款业务与系统退款调度. */
    public static void requireNonRefundTarget(String beanName) {
        Class<?> type =
                SpringContextUtils.containsBean(beanName)
                        ? SpringContextUtils.getType(beanName)
                        : null;
        if ("refundReconciliationScheduler".equals(beanName)
                || (type != null
                        && (OrderRefundService.class.isAssignableFrom(type)
                                || RefundReconciliationScheduler.class.isAssignableFrom(type)))) {
            throw new UnauthorizedException("退款只允许通过专用入口或系统调度处理");
        }
    }

    @Override
    public void run() {
        try {
            ReflectionUtils.makeAccessible(method);
            if (StringUtils.isNotBlank(params)) {
                method.invoke(target, params);
            } else {
                method.invoke(target);
            }
        } catch (Exception e) {
            throw new BusinessException("执行定时任务失败", e);
        }
    }
}
