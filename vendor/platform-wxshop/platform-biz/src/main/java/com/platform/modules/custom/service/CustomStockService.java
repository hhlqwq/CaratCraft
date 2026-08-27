package com.platform.modules.custom.service;

import com.platform.modules.custom.dto.ComponentStockView;

import java.util.List;

/**
 * 组件库存维护服务 / Component stock administration service.
 */
public interface CustomStockService {

    /**
     * 查询组件选项及库存 / Lists component options with their stock.
     *
     * @param goodsId 商城商品 ID, 为空时查询全部 / mall goods id, null for all
     * @return 选项库存视图列表 / list of option stock views
     */
    List<ComponentStockView> listStock(Integer goodsId);

    /**
     * 按选项 ID 更新库存 / Updates stock by option id.
     *
     * @param optionId 组件选项 ID / component option id
     * @param stock 目标库存, 不可为负 / target stock, non-negative
     */
    void updateStock(Long optionId, Integer stock);
}
