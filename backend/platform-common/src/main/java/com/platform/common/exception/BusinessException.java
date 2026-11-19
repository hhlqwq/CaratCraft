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

import org.springframework.http.HttpStatus;

/**
 * 自定义异常
 *
 * @author hehailong
 */
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private String msg;
    private int code = HttpStatus.INTERNAL_SERVER_ERROR.value();

    /** 构建业务异常并保存当前错误说明、错误码及可选的异常原因. */
    public BusinessException(String msg) {
        super(msg);
        this.msg = msg;
    }

    /** 构建业务异常并保存当前错误说明、错误码及可选的异常原因. */
    public BusinessException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }

    /** 构建业务异常并保存当前错误说明、错误码及可选的异常原因. */
    public BusinessException(String msg, int code) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }

    /** 构建业务异常并保存当前错误说明、错误码及可选的异常原因. */
    public BusinessException(String msg, int code, Throwable e) {
        super(msg, e);
        this.msg = msg;
        this.code = code;
    }

    /** 读取当前对象的Msg属性. */
    public String getMsg() {
        return msg;
    }

    /** 更新当前对象的Msg属性. */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /** 读取当前对象的Code属性. */
    public int getCode() {
        return code;
    }

    /** 更新当前对象的Code属性. */
    public void setCode(int code) {
        this.code = code;
    }
}
