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
import com.platform.modules.mall.dao.MallCollectDao;
import com.platform.modules.mall.entity.MallCollectEntity;
import com.platform.modules.mall.service.MallCollectService;
import com.platform.modules.mall.service.MallGoodsService;
import com.platform.modules.wx.entity.WxUserEntity;
import com.platform.modules.wx.service.WxUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Service实现类
 *
 * @author hehailong
 * @since 2024-07-19 20:53:33
 */
@Service("mallCollectService")
public class MallCollectServiceImpl extends ServiceImpl<MallCollectDao, MallCollectEntity>
        implements MallCollectService {
    @Autowired private WxUserService users;
    @Autowired private MallGoodsService goods;

    /** 本人查询只支持已确认的商品收藏类型0. */
    @Override
    public List<MallCollectEntity> listOwned(Long userId, Integer typeId) {
        validateOwnerAndType(userId, typeId);
        return queryList(Map.of("userId", userId, "typeId", typeId));
    }

    /** 用户行锁协调同一账号的并发收藏, 删除也始终携带本人归属条件. */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String toggleOwned(Long userId, Integer typeId, Integer goodsId) {
        validateOwnerAndType(userId, typeId);
        if (goodsId == null || goodsId <= 0) {
            throw new IllegalArgumentException("收藏商品标识无效");
        }
        if (users.getOne(new QueryWrapper<WxUserEntity>().eq("ID", userId).last("FOR UPDATE"))
                == null) {
            throw new BusinessException("用户不存在");
        }
        var owned =
                new QueryWrapper<MallCollectEntity>()
                        .eq("user_id", userId)
                        .eq("type_id", typeId)
                        .eq("value_id", goodsId);
        if (!list(owned).isEmpty()) {
            if (!remove(owned)) {
                throw new BusinessException("取消收藏失败, 请重新查询");
            }
            return "delete";
        }
        var item = goods.getById(goodsId);
        if (item == null
                || !Integer.valueOf(0).equals(item.getIsDelete())
                || !Integer.valueOf(1).equals(item.getIsOnSale())) {
            throw new BusinessException("商品已下架或不存在");
        }
        var favorite = new MallCollectEntity();
        favorite.setUserId(userId);
        favorite.setTypeId(typeId);
        favorite.setValueId(goodsId);
        favorite.setAddTime(System.currentTimeMillis() / 1000);
        favorite.setIsAttention(0);
        if (!save(favorite)) {
            throw new BusinessException("收藏保存失败, 请重新查询");
        }
        return "add";
    }

    /** 用户身份来自认证上下文, 类型范围由当前商品收藏需求确定. */
    private void validateOwnerAndType(Long userId, Integer typeId) {
        if (userId == null || userId <= 0 || !Integer.valueOf(0).equals(typeId)) {
            throw new IllegalArgumentException("用户身份或收藏类型无效");
        }
    }

    @Override
    public List<MallCollectEntity> queryAll(Map<String, Object> params) {
        return baseMapper.queryAll(params);
    }

    @Override
    public List<MallCollectEntity> queryAllByWrapper(QueryWrapper<MallCollectEntity> queryWrapper) {
        return this.list(queryWrapper);
    }

    @Override
    public Page<MallCollectEntity> queryPage(Map<String, Object> params) {
        // 排序
        params.put("sidx", "T.id");
        params.put("asc", false);
        Page<MallCollectEntity> page = new Query<MallCollectEntity>(params).getPage();
        return page.setRecords(baseMapper.selectMallCollectPage(page, params));
    }

    @Override
    public boolean add(MallCollectEntity mallCollect) {
        return this.save(mallCollect);
    }

    @Override
    public boolean update(MallCollectEntity mallCollect) {
        return this.updateById(mallCollect);
    }

    @Override
    public boolean delete(Integer id) {
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBatch(Integer[] ids) {
        return this.removeByIds(Arrays.asList(ids));
    }

    @Override
    public List<MallCollectEntity> queryList(Map<String, Object> param) {
        return baseMapper.queryList(param);
    }
}
