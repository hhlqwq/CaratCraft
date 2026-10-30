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

import com.platform.common.utils.Constant;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 过滤器异常处理
 *
 * @author 何海龙
 */
@RestController
public class FilterExceptionController extends BasicErrorController {
    /** 使用默认错误属性初始化过滤器异常处理入口. */
    public FilterExceptionController() {
        super(new DefaultErrorAttributes(), new ErrorProperties());
    }

    @Override
    @RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        Map<String, Object> body =
                getErrorAttributes(
                        request,
                        ErrorAttributeOptions.of(ErrorAttributeOptions.Include.STACK_TRACE));
        String errMsg = "未知异常，请联系管理员";
        try {
            errMsg =
                    body.get("trace")
                            .toString()
                            .split("\r\n")[0]
                            .replace(
                                    "com.platform.common.exception.BusinessException: ",
                                    Constant.BLANK);
        } catch (Exception ignored) {
        }
        Map<String, Object> map = new HashMap<>(4);
        map.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        map.put("msg", errMsg);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }
}
