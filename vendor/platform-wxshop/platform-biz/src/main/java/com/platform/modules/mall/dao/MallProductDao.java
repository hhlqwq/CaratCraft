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
package com.platform.modules.mall.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.modules.mall.entity.MallProductEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 商品SKU
 *
 * @author pengjun
 */
@Mapper
public interface MallProductDao extends BaseMapper<MallProductEntity> {
  List<MallProductEntity> queryAll(@Param("params") Map<String, Object> params);

  List<MallProductEntity> queryList(Map<String, Object> param);

  /** 原子扣减库存 / Atomically deducts stock. */
  @Update(
      "UPDATE mall_product SET goods_number = goods_number - #{number} "
          + "WHERE id = #{productId} AND goods_number >= #{number}")
  int deductStock(@Param("productId") Integer productId, @Param("number") Integer number);

  /** 恢复库存 / Restores stock. */
  @Update("UPDATE mall_product SET goods_number = goods_number + #{number} WHERE id = #{productId}")
  int restoreStock(@Param("productId") Integer productId, @Param("number") Integer number);
}
