package com.platform.modules.custom.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.modules.custom.entity.CustomDesignSnapshotEntity;
import org.apache.ibatis.annotations.Mapper;

/** 设计快照数据访问 / Design snapshot data access. */
@Mapper
public interface CustomDesignSnapshotDao extends BaseMapper<CustomDesignSnapshotEntity> {}
