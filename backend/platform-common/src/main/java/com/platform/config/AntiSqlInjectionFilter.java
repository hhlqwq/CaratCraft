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
package com.platform.config;

import com.platform.common.exception.BusinessException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;

/**
 * sql 注入过滤器
 *
 * @author hehailong
 */
public class AntiSqlInjectionFilter implements Filter {
    @Override
    public void doFilter(ServletRequest args0, ServletResponse args1, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) args0;
        // 获得所有请求参数名
        Enumeration<String> params = req.getParameterNames();
        StringBuilder sql = new StringBuilder();
        while (params.hasMoreElements()) {
            // 得到参数名
            String name = params.nextElement();
            // 得到参数对应值
            String[] value = req.getParameterValues(name);
            for (String s : value) {
                sql.append(s);
            }
        }
        if (sqlValidate(sql.toString())) {
            String ip = req.getRemoteAddr();
            throw new BusinessException("您发送请求中的参数中含有非法字符！请求IP：" + ip);
        } else {
            chain.doFilter(args0, args1);
        }
    }

    /**
     * 按固定区域识别既有关键字, 不记录可能包含身份或联系方式的请求参数.
     *
     * @param str 待校验参数文本
     * @return 是否命中既有拦截规则
     */
    protected static boolean sqlValidate(String str) {
        // 统一转为小写
        str = str.toLowerCase(Locale.ROOT);
        // 过滤掉的sql关键字，可以手动添加
        String badStr =
                "'|exec|execute|insert|select|delete|update|drop|master|truncate|"
                        + "declare|sitename|net user|xp_cmdshell'|exec|execute|insert|create|drop|"
                        + "grant|group_concat|column_name|"
                        + "information_schema.columns|table_schema|select|delete|update|"
                        + "master|truncate|declare";
        String[] badStrs = badStr.split("\\|");
        for (String s : badStrs) {
            if (str.contains(s)) {
                return true;
            }
        }
        return false;
    }
}
