package com.platform.modules.custom.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.modules.custom.entity.CustomOrderCustomizationEntity;

import org.apache.ibatis.annotations.Mapper;

/** 定制订单数据访问 / Custom order data access. */
@Mapper
public interface CustomOrderCustomizationDao extends BaseMapper<CustomOrderCustomizationEntity> {}
