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
import com.platform.modules.mall.entity.MallGoodsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * Dao
 *
 * @author 何海龙
 * @since 2026-02-24 13:15:28
 */
@Mapper
public interface MallGoodsDao extends BaseMapper<MallGoodsEntity> {

    /**
     * 查询所有列表
     *
     * @param params 查询参数
     * @return List
     */
    List<MallGoodsEntity> queryAll(@Param("params") Map<String, Object> params);

    /**
     * 自定义分页查询
     *
     * @param page   分页参数
     * @param params 查询参数
     * @return List
     */
    List<MallGoodsEntity> selectMallGoodsPage(Page<MallGoodsEntity> page, @Param("params") Map<String, Object> params);

    /**
     * 查询回收站商品。
     *
     * @param page 分页参数。
     * @param params 查询参数。
     * @return 回收站商品列表。
     */
    List<MallGoodsEntity> selectRecyclePage(Page<MallGoodsEntity> page,
                                            @Param("params") Map<String, Object> params);

    /**
     * 将商品移入回收站并强制下架。
     *
     * @param ids 商品 ID。
     * @param deleteTime 删除时间。
     * @return 更新行数。
     */
    int moveToRecycleBin(@Param("ids") List<Integer> ids, @Param("deleteTime") Date deleteTime);

    /**
     * 从回收站恢复商品。
     *
     * @param ids 商品 ID。
     * @return 更新行数。
     */
    int restoreFromRecycleBin(@Param("ids") List<Integer> ids);

    /**
     * 查询超过保留期的商品 ID。
     *
     * @param expireTime 最晚删除时间。
     * @return 商品 ID 列表。
     */
    List<Integer> selectExpiredRecycleIds(@Param("expireTime") Date expireTime);

    /**
     * 删除商品可重建关联数据。
     *
     * @param ids 商品 ID。
     * @return 受影响行数。
     */
    int deleteRecycleRelations(@Param("ids") List<Integer> ids);

    /**
     * 物理删除回收站商品。
     *
     * @param ids 商品 ID。
     * @return 删除行数。
     */
    int permanentlyDeleteGoods(@Param("ids") List<Integer> ids);

    List<MallGoodsEntity> queryCatalogProductList(Map<String, Object> params);

    List<MallGoodsEntity> queryList(Map<String, Object> param);

    int queryTotal(Map<String, Object> map);
}
