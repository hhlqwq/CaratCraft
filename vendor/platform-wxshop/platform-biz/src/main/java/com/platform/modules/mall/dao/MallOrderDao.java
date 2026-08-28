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
import com.platform.modules.mall.entity.MallAttributeCategoryEntity;
import com.platform.modules.mall.entity.MallOrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 订单数据访问接口。
 * <p>
 * 提供订单列表查询和分页查询能力，供 Service 层调用。
 * </p>
 *
 * @author 何海龙
 * @since 2026-03-15 14:30:00
 */
@Mapper
public interface MallOrderDao extends BaseMapper<MallOrderEntity> {

    /**
     * 查询订单列表。
     *
     * @param params 查询参数
     * @return 订单集合
     */
    List<MallOrderEntity> queryAll(@Param("params") Map<String, Object> params);

    /**
     * 分页查询订单列表。
     *
     * @param page   分页对象
     * @param params 查询参数
     * @return 订单集合
     */
    List<MallOrderEntity> selectMallOrderPage(Page<MallOrderEntity> page, @Param("params") Map<String, Object> params);

    List<MallOrderEntity> queryList(Map<String, Object> param);

    /** 原子取消待支付订单 / Atomically cancels an unpaid order. */
    @Update("UPDATE mall_order SET order_status = 101 "
            + "WHERE id = #{orderId} AND user_id = #{userId} AND pay_status = 0 AND order_status = 0")
    int cancelUnpaid(@Param("orderId") Integer orderId, @Param("userId") Long userId);

    /** 原子标记退款完成 / Atomically marks an order as refunded. */
    @Update("UPDATE mall_order SET pay_status = 4, order_status = #{orderStatus} "
            + "WHERE id = #{orderId} AND pay_status = 2 AND "
            + "((#{orderStatus} = 401 AND order_status = 201) "
            + "OR (#{orderStatus} = 402 AND order_status = 300))")
    int markRefunded(@Param("orderId") Integer orderId,
                     @Param("orderStatus") Integer orderStatus);
}
