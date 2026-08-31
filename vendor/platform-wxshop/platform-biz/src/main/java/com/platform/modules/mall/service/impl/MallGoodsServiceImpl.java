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
package com.platform.modules.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.common.exception.BusinessException;
import com.platform.common.utils.Query;
import com.platform.modules.mall.dao.MallGoodsDao;
import com.platform.modules.mall.entity.MallGoodsEntity;
import com.platform.modules.mall.service.MallGoodsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Service实现类
 *
 * @author 何海龙
 * @since 2026-02-24 13:15:28
 */
@Service("mallGoodsService")
public class MallGoodsServiceImpl extends ServiceImpl<MallGoodsDao, MallGoodsEntity> implements MallGoodsService {

    @Override
    public List<MallGoodsEntity> queryAll(Map<String, Object> params) {
        return baseMapper.queryAll(params);
    }

    @Override
    public List<MallGoodsEntity> queryAllByWrapper(QueryWrapper<MallGoodsEntity> queryWrapper) {
        return this.list(queryWrapper);
    }

    @Override
    public Page<MallGoodsEntity> queryPage(Map<String, Object> params) {
        //排序
        params.put("sidx", "T.id");
        params.put("asc", false);
        Page<MallGoodsEntity> page = new Query<MallGoodsEntity>(params).getPage();
        return page.setRecords(baseMapper.selectMallGoodsPage(page, params));
    }

    @Override
    public Page<MallGoodsEntity> queryRecyclePage(Map<String, Object> params) {
        params.put("sidx", "T.delete_time");
        params.put("asc", false);
        Page<MallGoodsEntity> page = new Query<MallGoodsEntity>(params).getPage();
        return page.setRecords(baseMapper.selectRecyclePage(page, params));
    }

    @Override
    public boolean add(MallGoodsEntity mallGoods) {
        return this.save(mallGoods);
    }

    @Override
    public boolean update(MallGoodsEntity mallGoods) {
        return this.updateById(mallGoods);
    }

    @Override
    public boolean delete(Integer id) {
        return deleteBatch(new Integer[]{id});
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBatch(Integer[] ids) {
        List<Integer> goodsIds = validateIds(ids);
        int affectedRows = baseMapper.moveToRecycleBin(goodsIds, new Date());
        if (affectedRows == 0) {
            throw new BusinessException("所选商品不存在或已在回收站中");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean restoreBatch(Integer[] ids) {
        List<Integer> goodsIds = validateIds(ids);
        int affectedRows = baseMapper.restoreFromRecycleBin(goodsIds);
        if (affectedRows == 0) {
            throw new BusinessException("所选商品不在回收站中或已经恢复");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int permanentlyDeleteBatch(Integer[] ids) {
        return permanentlyDelete(validateIds(ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int purgeExpiredRecycleBin(Date expireTime) {
        if (expireTime == null) {
            throw new BusinessException("回收站过期时间不能为空");
        }
        List<Integer> expiredIds = baseMapper.selectExpiredRecycleIds(expireTime);
        if (expiredIds == null || expiredIds.isEmpty()) {
            return 0;
        }
        return permanentlyDelete(expiredIds);
    }

    @Override
    public List<MallGoodsEntity> queryCatalogProductList(Map<String, Object> params) {
        return baseMapper.queryCatalogProductList(params);
    }

    @Override
    public List<MallGoodsEntity> queryList(Map<String, Object> param) {
        return baseMapper.queryList(param);
    }

    @Override
    public long queryTotal(Map<String, Object> params) {
        return baseMapper.queryTotal(params);
    }

    /**
     * 校验并规范商品 ID。
     *
     * @param ids 商品 ID。
     * @return 非空商品 ID 列表。
     */
    private List<Integer> validateIds(Integer[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("请选择商品");
        }
        List<Integer> goodsIds = Arrays.asList(ids);
        if (goodsIds.contains(null)) {
            throw new BusinessException("商品 ID 不能为空");
        }
        return goodsIds;
    }

    /**
     * 永久删除商品和可重建关联数据，保留订单及定制历史快照。
     *
     * @param goodsIds 商品 ID。
     * @return 实际删除商品数量。
     */
    private int permanentlyDelete(List<Integer> goodsIds) {
        List<Integer> safeIds = goodsIds == null ? Collections.emptyList() : goodsIds;
        if (safeIds.isEmpty()) {
            return 0;
        }
        baseMapper.deleteRecycleRelations(safeIds);
        int deletedRows = baseMapper.permanentlyDeleteGoods(safeIds);
        if (deletedRows == 0) {
            throw new BusinessException("所选商品不在回收站中，无法永久删除");
        }
        return deletedRows;
    }
}
