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
import com.platform.modules.mall.entity.MallCommentEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 商品评论表Dao
 *
 * @author hehailong
 * @email 939961241@qq.com
 * @date 2026-03-15 13:46:31
 */
@Mapper
public interface MallCommentDao extends BaseMapper<MallCommentEntity> {
    /** 按给定条件读取当前领域全部匹配记录. */
    List<MallCommentEntity> queryAll(@Param("params") Map<String, Object> params);

    /** 按查询条件分页读取商品评价. */
    List<MallCommentEntity> selectMallCommentPage(
            Page<MallCommentEntity> page, @Param("params") Map<String, Object> params);

    /** 统计符合当前查询条件的记录数量. */
    Integer queryTotal(Map<String, Object> param);

    /** 统计带有图片的匹配评价数量. */
    Integer queryhasPicTotal(Map<String, Object> param);

    /** 按给定条件查询当前领域记录列表. */
    List<MallCommentEntity> queryList(Map<String, Object> param);
}
