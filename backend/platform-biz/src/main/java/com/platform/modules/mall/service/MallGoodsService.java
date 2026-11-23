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
package com.platform.modules.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.platform.modules.mall.entity.MallGoodsEntity;

import java.util.List;
import java.util.Map;

/**
 * Service接口
 *
 * @author hehailong
 * @since 2026-02-24 13:15:28
 */
public interface MallGoodsService extends IService<MallGoodsEntity> {

    /** 单独设置商品销售状态. */
    void changeSale(Integer id, boolean onSale);

    /**
     * 查询所有列表
     *
     * @param params 查询参数
     * @return List
     */
    List<MallGoodsEntity> queryAll(Map<String, Object> params);

    /**
     * 查询所有列表
     *
     * @param queryWrapper 查询参数
     * @return List
     */
    List<MallGoodsEntity> queryAllByWrapper(QueryWrapper<MallGoodsEntity> queryWrapper);

    /**
     * 分页查询
     *
     * @param params 查询参数
     * @return Page
     */
    Page<MallGoodsEntity> queryPage(Map<String, Object> params);

    /**
     * 分页查询回收站商品。
     *
     * @param params 查询参数。
     * @return 回收站分页数据。
     */
    Page<MallGoodsEntity> queryRecyclePage(Map<String, Object> params);

    /**
     * 新增
     *
     * @param mallGoods
     * @return 新增结果
     */
    boolean add(MallGoodsEntity mallGoods);

    /**
     * 根据主键更新
     *
     * @param mallGoods
     * @return 更新结果
     */
    boolean update(MallGoodsEntity mallGoods);

    /**
     * 根据主键删除
     *
     * @param id id
     * @return 删除结果
     */
    boolean delete(Integer id);

    /**
     * 根据主键批量删除
     *
     * @param ids ids
     * @return 删除结果
     */
    boolean deleteBatch(Integer[] ids);

    /**
     * 批量恢复回收站商品，恢复后保持下架状态。
     *
     * @param ids 商品 ID。
     * @return 是否恢复成功。
     */
    boolean restoreBatch(Integer[] ids);

    /**
     * 批量永久删除回收站商品及可重建关联数据。
     *
     * @param ids 商品 ID。
     * @return 实际删除商品数量。
     */
    int permanentlyDeleteBatch(Integer[] ids);

    /**
     * 清理超过保留期的回收站商品。
     *
     * @param expireTime 最晚删除时间。
     * @return 实际删除商品数量。
     */
    int purgeExpiredRecycleBin(java.util.Date expireTime);

    /** 按目录筛选条件查询商品列表. */
    List<MallGoodsEntity> queryCatalogProductList(Map<String, Object> params);

    /** 按给定条件查询当前领域记录列表. */
    List<MallGoodsEntity> queryList(Map<String, Object> param);

    /** 统计符合当前查询条件的记录数量. */
    long queryTotal(Map<String, Object> params);
}
