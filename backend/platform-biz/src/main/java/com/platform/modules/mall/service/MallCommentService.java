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
import com.platform.modules.mall.entity.MallCommentEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品评论表
 *
 * @author hehailong
 * @email 939961241@qq.com
 * @date 2026-03-15 13:46:21
 */
public interface MallCommentService extends IService<MallCommentEntity> {
    /** 按给定条件读取当前领域全部匹配记录. */
    List<MallCommentEntity> queryAll(Map<String, Object> params);

    /** 根据显式查询条件读取当前领域记录. */
    List<MallCommentEntity> queryAllByWrapper(QueryWrapper<MallCommentEntity> queryWrapper);

    /** 按查询条件和分页参数返回当前领域记录. */
    Page<MallCommentEntity> queryPage(Map<String, Object> params);

    /** 保存当前领域的新记录并返回处理结果. */
    boolean add(MallCommentEntity mallComment);

    /** 接收当前接口的修改请求并委托业务服务更新. */
    boolean update(MallCommentEntity mallComment);

    /** 按当前接口身份和参数删除目标业务记录. */
    boolean delete(Integer id);

    /** 在事务中批量删除给定标识对应的记录. */
    boolean deleteBatch(Integer[] ids);

    /** 统计符合当前查询条件的记录数量. */
    Integer queryTotal(Map<String, Object> param);

    /** 统计带有图片的匹配评价数量. */
    Integer queryhasPicTotal(Map<String, Object> param);

    /** 按给定条件查询当前领域记录列表. */
    List<MallCommentEntity> queryList(Map<String, Object> param);
}
