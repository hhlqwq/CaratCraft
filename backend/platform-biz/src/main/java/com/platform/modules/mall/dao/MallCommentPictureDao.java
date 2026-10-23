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
import com.platform.modules.mall.entity.MallCommentPictureEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 商品评论图片表Dao
 *
 * @author hehailong
 * @email 939961241@qq.com
 * @date 2026-03-15 13:47:04
 */
@Mapper
public interface MallCommentPictureDao extends BaseMapper<MallCommentPictureEntity> {
    /** 按给定条件读取当前领域全部匹配记录. */
    List<MallCommentPictureEntity> queryAll(@Param("params") Map<String, Object> params);

    /** 按查询条件分页读取评价图片. */
    List<MallCommentPictureEntity> selectMallCommentPicturePage(
            Page<MallCommentPictureEntity> page, @Param("params") Map<String, Object> params);

    /** 按给定条件查询当前领域记录列表. */
    List<MallCommentPictureEntity> queryList(Map<String, Object> param);
}
