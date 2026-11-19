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
 * @author hehailong
 * @since 2024-07-19 20:53:33
 */
@Data
@TableName("mall_collect")
public class MallCollectEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 用户Id */
    private Long userId;

    /** 产品Id */
    private Integer valueId;

    /** 添加时间 */
    private Long addTime;

    /** 是否提醒 */
    private Integer isAttention;

    private Integer typeId;

    /** 微信昵称 */
    @TableField(exist = false)
    private String nickname;

    @TableField(exist = false)
    private String name;

    @TableField(exist = false)
    private String listPicUrl;

    @TableField(exist = false)
    private String goodsBrief;

    @TableField(exist = false)
    private String retailPrice;
}
