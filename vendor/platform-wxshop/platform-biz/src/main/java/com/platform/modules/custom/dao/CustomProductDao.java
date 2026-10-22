package com.platform.modules.custom.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.modules.custom.entity.CustomProductEntity;

import org.apache.ibatis.annotations.Mapper;

/** 定制商品数据访问 / Custom product data access. */
@Mapper
public interface CustomProductDao extends BaseMapper<CustomProductEntity> {}
