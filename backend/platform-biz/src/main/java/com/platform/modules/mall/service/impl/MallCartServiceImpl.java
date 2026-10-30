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
import com.platform.common.utils.Query;
import com.platform.modules.mall.dao.MallCartDao;
import com.platform.modules.mall.entity.MallCartEntity;
import com.platform.modules.mall.service.MallCartService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 购物车持久化适配, 批量勾选和删除始终携带所属用户条件.
 *
 * @author hehailong
 * @since 2024-07-19 20:53:40
 */
@Service("mallCartService")
public class MallCartServiceImpl extends ServiceImpl<MallCartDao, MallCartEntity>
        implements MallCartService {

    /** 按后台筛选条件查询购物车, 数据权限由后台入口控制. */
    @Override
    public List<MallCartEntity> queryAll(Map<String, Object> params) {
        return baseMapper.queryAll(params);
    }

    /** 接受受信任调用方构造的查询条件, 不拼接客户端SQL. */
    @Override
    public List<MallCartEntity> queryAllByWrapper(QueryWrapper<MallCartEntity> queryWrapper) {
        return this.list(queryWrapper);
    }

    /** 后台列表固定使用购物车主键倒序分页. */
    @Override
    public Page<MallCartEntity> queryPage(Map<String, Object> params) {
        // 排序
        params.put("sidx", "T.id");
        params.put("asc", false);
        Page<MallCartEntity> page = new Query<MallCartEntity>(params).getPage();
        return page.setRecords(baseMapper.selectMallCartPage(page, params));
    }

    /** 保存经过应用层身份、规格及库存校验的新购物车行. */
    @Override
    public boolean add(MallCartEntity mallCart) {
        return this.save(mallCart);
    }

    /** 更新应用层已经校验归属的购物车行. */
    @Override
    public boolean update(MallCartEntity mallCart) {
        return this.updateById(mallCart);
    }

    /** 删除应用层已经校验归属的单行记录. */
    @Override
    public boolean delete(Integer id) {
        return this.removeById(id);
    }

    /** 后台批量删除在同一事务内完成, 权限由管理入口控制. */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBatch(Integer[] ids) {
        return this.removeByIds(Arrays.asList(ids));
    }

    /** 一条带用户条件的SQL完成勾选, 不回写其他行的旧数量或价格快照. */
    @Override
    public void updateCheck(String[] productIds, Integer isChecked, Long userId) {
        baseMapper.updateCheck(productIds, isChecked, userId);
    }

    /** 用户和规格集合同时匹配才删除, 不允许跨账号操作. */
    @Override
    public void deleteByUserAndProductIds(Long userId, String[] productIdsArray) {
        baseMapper.deleteByUserAndProductIds(userId, productIdsArray);
    }

    /** 按应用层传入的用户、商品和规格条件读取购物车行. */
    @Override
    public List<MallCartEntity> queryList(Map<String, Object> param) {
        return baseMapper.queryList(param);
    }
}
