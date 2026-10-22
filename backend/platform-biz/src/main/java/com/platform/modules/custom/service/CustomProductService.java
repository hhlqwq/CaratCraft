package com.platform.modules.custom.service;

import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import com.platform.modules.custom.entity.CustomProductEntity;

import java.util.List;
import java.util.Map;

/** 定制商品服务 / Custom product service. */
public interface CustomProductService {

    /**
     * 获取定制器配置 / Gets customizer configuration.
     *
     * @param goodsId 商城商品ID / mall goods id
     * @return 模板、组件选项和主商品ID / template, options and primary product id
     */
    Map<String, Object> getConfig(Integer goodsId);

    /**
     * 根据商品ID获取定制商品 / Gets custom product by goods id.
     *
     * @param goodsId 商城商品ID / mall goods id
     * @return 定制商品实体 / custom product entity or null
     */
    CustomProductEntity getByGoodsId(Integer goodsId);

    /**
     * 根据商品ID获取启用的组件选项 / Gets enabled options by goods id.
     *
     * @param goodsId 商城商品ID / mall goods id
     * @return 组件选项列表 / list of component options
     */
    List<CustomComponentOptionEntity> getEnabledOptions(Integer goodsId);
}
