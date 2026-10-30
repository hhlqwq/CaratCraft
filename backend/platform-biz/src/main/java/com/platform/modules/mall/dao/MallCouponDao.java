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
import com.platform.modules.mall.entity.MallCouponEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Dao
 *
 * @author 何海龙
 * @since 2026-02-24 10:20:41
 */
@Mapper
public interface MallCouponDao extends BaseMapper<MallCouponEntity> {

    /**
     * 查询所有列表
     *
     * @param params 查询参数
     * @return List
     */
    List<MallCouponEntity> queryAll(@Param("params") Map<String, Object> params);

    /**
     * 自定义分页查询
     *
     * @param page 分页参数
     * @param params 查询参数
     * @return List
     */
    List<MallCouponEntity> selectMallCouponPage(
            Page<MallCouponEntity> page, @Param("params") Map<String, Object> params);

    /** 读取满足当前用户及优惠条件的领券记录. */
    MallCouponEntity getUserCoupon(@Param(value = "id") Integer id, @Param("userId") Long userId);

    /**
     * 按类型查询
     *
     * @param params
     * @return
     */
    MallCouponEntity queryMaxUserEnableCoupon(Map<String, Object> params);

    /** 按给定条件更新用户领券状态. */
    void updateUserCoupon(MallCouponEntity couponVo);

    /** 按给定条件查询当前领域记录列表. */
    List<MallCouponEntity> queryList(Map<String, Object> param);
}
