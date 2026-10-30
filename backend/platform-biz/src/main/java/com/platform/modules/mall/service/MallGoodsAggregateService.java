package com.platform.modules.mall.service;

import com.platform.modules.mall.dto.GoodsAggregateDTO;

/**
 * 商品聚合服务
 *
 * @author pengjun
 */
public interface MallGoodsAggregateService {

    /** 读取商品及属性、相册、规格和SKU的完整编辑数据. */
    GoodsAggregateDTO getAggregate(Integer goodsId);

    /** 在事务中创建商品及其完整关联数据. */
    Integer saveAggregate(GoodsAggregateDTO aggregateDTO);

    /** 在事务中按稳定身份更新商品及其完整关联数据. */
    Integer updateAggregate(GoodsAggregateDTO aggregateDTO);
}
