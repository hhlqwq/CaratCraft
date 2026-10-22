package com.platform.modules.custom.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.modules.custom.entity.WorkOrderLogEntity;

import org.apache.ibatis.annotations.Mapper;

/** 工单操作日志数据访问 / Work order log data access. */
@Mapper
public interface WorkOrderLogDao extends BaseMapper<WorkOrderLogEntity> {}
