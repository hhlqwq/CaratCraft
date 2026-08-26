package com.platform.modules.custom.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.modules.custom.entity.ProductionWorkOrderEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 生产工单数据访问 / Production work order data access.
 */
@Mapper
public interface ProductionWorkOrderDao extends BaseMapper<ProductionWorkOrderEntity> {
}
