package com.platform.modules.custom.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import org.apache.ibatis.annotations.Mapper;

/** 定制组件数据访问 / Custom component data access. */
@Mapper
public interface CustomComponentOptionDao extends BaseMapper<CustomComponentOptionEntity> {}
