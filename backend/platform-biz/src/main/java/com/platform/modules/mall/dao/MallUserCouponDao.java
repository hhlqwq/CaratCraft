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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.modules.mall.entity.MallUserCouponEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Dao
 *
 * @author 何海龙
 * @since 2024-07-19 21:02:38
 */
@Mapper
public interface MallUserCouponDao extends BaseMapper<MallUserCouponEntity> {

    /**
     * 查询所有列表
     *
     * @param params 查询参数
     * @return List
     */
    List<MallUserCouponEntity> queryAll(@Param("params") Map<String, Object> params);

    /**
     * 自定义分页查询
     *
     * @param page 分页参数
     * @param params 查询参数
     * @return List
     */
    List<MallUserCouponEntity> selectMallUserCouponPage(
            Page<MallUserCouponEntity> page, @Param("params") Map<String, Object> params);

    /** 按给定条件查询当前领域记录列表. */
    List<MallUserCouponEntity> queryList(Map<String, Object> param);

    /** 原子占用用户优惠券 / Atomically claims a user coupon. */
    @Update(
            "UPDATE mall_user_coupon SET coupon_status = 2, used_time = #{usedTime}, order_id ="
                    + " #{orderId} WHERE id = #{id} AND user_id = #{userId} AND coupon_status = 1")
    int claim(
            @Param("id") Integer id,
            @Param("userId") Long userId,
            @Param("orderId") Integer orderId,
            @Param("usedTime") Date usedTime);

    /** 释放未支付订单占用的优惠券 / Releases a coupon claimed by an unpaid order. */
    @Update(
            "UPDATE mall_user_coupon SET coupon_status = 1, used_time = NULL, order_id = 0 "
                    + "WHERE order_id = #{orderId} AND coupon_status = 2")
    int releaseByOrder(@Param("orderId") Integer orderId);
}
