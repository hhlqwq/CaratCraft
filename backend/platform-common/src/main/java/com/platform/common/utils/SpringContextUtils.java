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
package com.platform.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring Context 工具类
 *
 * @author hehailong
 */
@Component
public class SpringContextUtils implements ApplicationContextAware {
    public static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtils.applicationContext = applicationContext;
    }

    /** 读取当前对象的Bean属性. */
    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    /** 读取当前对象的Bean属性. */
    public static <T> T getBean(String name, Class<T> requiredType) {
        return applicationContext.getBean(name, requiredType);
    }

    /** 判断Spring容器是否包含指定名称的组件. */
    public static boolean containsBean(String name) {
        return applicationContext.containsBean(name);
    }

    /** 查询指定Spring组件是否采用单例作用域. */
    public static boolean isSingleton(String name) {
        return applicationContext.isSingleton(name);
    }

    /** 读取当前对象的Type属性. */
    public static Class<? extends Object> getType(String name) {
        return applicationContext.getType(name);
    }
}
