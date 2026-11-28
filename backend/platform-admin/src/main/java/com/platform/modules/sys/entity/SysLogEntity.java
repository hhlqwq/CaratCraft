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
package com.platform.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统日志
 *
 * @author hehailong
 */
@Data
@TableName("SYS_LOG")
public class SysLogEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId private String id;

    /** 用户名 */
    private String userName;

    /** 用户操作 */
    private String operation;

    /** 审计事件发生时的操作人机构. */
    private String scopeOrgNo;

    /** 请求方法 */
    @com.fasterxml.jackson.annotation.JsonIgnore private String method;

    /** 请求参数 */
    @com.fasterxml.jackson.annotation.JsonIgnore private String params;

    /** 执行时长(毫秒) */
    private Long time;

    /** IP地址 */
    private String ip;

    /** 审计操作者稳定标识及当时身份. */
    private String actorId;

    private String actorType;

    /** 模块、类型及仅含标识的操作对象. */
    private String module;

    private String operationType;
    private String targetId;

    /** 明确结果、安全摘要及非敏感错误码. */
    private String result;

    private String summary;
    private String errorCode;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
