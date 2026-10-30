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

import com.platform.common.exception.BusinessException;
import com.platform.modules.sys.entity.SysUserEntity;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

/**
 * Shiro工具类
 *
 * @author hehailong
 */
public class ShiroUtils {

    /** 读取当前认证主体的会话. */
    public static Session getSession() {
        return SecurityUtils.getSubject().getSession();
    }

    /** 取得Shiro管理的当前认证主体. */
    public static Subject getSubject() {
        return SecurityUtils.getSubject();
    }

    /** 读取当前认证主体关联的系统用户. */
    public static SysUserEntity getUserEntity() {
        return (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
    }

    /** 从当前认证主体读取用户标识. */
    public static String getUserId() {
        return getUserEntity().getUserId();
    }

    /** 在当前认证会话中保存指定属性. */
    public static void setSessionAttribute(Object key, Object value) {
        getSession().setAttribute(key, value);
    }

    /** 读取当前认证会话中的指定属性. */
    public static Object getSessionAttribute(Object key) {
        return getSession().getAttribute(key);
    }

    /** 判断当前主体是否已通过认证. */
    public static boolean isLogin() {
        return SecurityUtils.getSubject().getPrincipal() != null;
    }

    /** 读取并移除当前会话中的验证码以避免重放. */
    public static String getKaptcha(String key) {
        Object kaptcha = getSessionAttribute(key);
        if (kaptcha == null) {
            throw new BusinessException("验证码已失效");
        }
        getSession().removeAttribute(key);
        return kaptcha.toString();
    }
}
