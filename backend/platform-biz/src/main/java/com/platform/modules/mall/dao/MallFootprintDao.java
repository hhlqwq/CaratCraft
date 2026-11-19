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
import com.platform.modules.mall.entity.MallFootprintEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Dao
 *
 * @author hehailong
 * @since 2024-07-19 20:53:33
 */
@Mapper
public interface MallFootprintDao extends BaseMapper<MallFootprintEntity> {

    /**
     * 查询所有列表
     *
     * @param params 查询参数
     * @return List
     */
    List<MallFootprintEntity> queryAll(@Param("params") Map<String, Object> params);

    /**
     * 自定义分页查询
     *
     * @param page 分页参数
     * @param params 查询参数
     * @return List
     */
    List<MallFootprintEntity> selectMallFootprintPage(
            Page<MallFootprintEntity> page, @Param("params") Map<String, Object> params);

    /** 按条件读取可展示的分享列表. */
    List<MallFootprintEntity> shareList(Map<String, Object> params);

    /** 按给定条件查询当前领域记录列表. */
    List<MallFootprintEntity> queryList(Map<String, Object> param);

    /** 统计本人每商品最新且商品未删除的足迹数量. */
    long countOwned(@Param("userId") Long userId);

    /** 按稳定ID倒序读取本人去重足迹的有界当前页. */
    List<MallFootprintEntity> listOwned(
            @Param("userId") Long userId, @Param("offset") long offset, @Param("size") int size);

    /** 所选记录须属于本人, 原子删除本人同一商品的全部浏览记录. */
    int deleteOwnedGoods(@Param("userId") Long userId, @Param("footprintId") Integer footprintId);
}
