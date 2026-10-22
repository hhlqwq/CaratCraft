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
package com.platform.modules.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.io.Serializable;

/**
 * 实体
 *
 * @author 何海龙
 * @since 2025-06-29 18:44:03
 */
@Data
@TableName("mall_search_history")
public class MallSearchHistoryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 关键字 */
    private String keyword;

    /** 搜索来源，如PC、小程序、APP等 */
    @TableField("`from`")
    private String from;

    /** 搜索时间 */
    private Long addTime;

    /** 会员Id */
    private String userId;

    /** 会员昵称 */
    @TableField(exist = false)
    private String nickname;
}
