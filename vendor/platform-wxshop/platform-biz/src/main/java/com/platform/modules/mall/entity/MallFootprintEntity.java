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
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.platform.common.utils.JsonDateSerializer;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 实体
 *
 * @author 何海龙
 * @since 2024-07-19 20:53:33
 */
@Data
@TableName("mall_footprint")
public class MallFootprintEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  /** 主键 */
  @TableId(type = IdType.AUTO)
  private Integer id;

  /** 会员Id */
  private Long userId;

  /** 商品id */
  private Integer goodsId;

  /** 记录时间 */
  @JsonSerialize(using = JsonDateSerializer.class)
  private Long addTime;

  /** 转发人 */
  private Long referrer;

  /** 微信昵称 */
  @TableField(exist = false)
  private String nickname;

  @TableField(exist = false)
  private String name;

  @TableField(exist = false)
  private String listPicUrl;

  @TableField(exist = false)
  private String goodsBrief;

  //
  @TableField(exist = false)
  private BigDecimal retailPrice;

  @TableField(exist = false)
  private String avatar;
}
