package com.platform.modules.custom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.modules.custom.dao.CustomComponentOptionDao;
import com.platform.modules.custom.dao.CustomProductDao;
import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import com.platform.modules.custom.entity.CustomProductEntity;
import com.platform.modules.custom.service.CustomProductService;
import com.platform.modules.mall.entity.MallGoodsEntity;
import com.platform.modules.mall.entity.MallProductEntity;
import com.platform.modules.mall.service.MallGoodsService;
import com.platform.modules.mall.service.MallProductService;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 定制商品服务实现 / Custom product service implementation. */
@Service
public class CustomProductServiceImpl implements CustomProductService {
    private final CustomProductDao customProductDao;
    private final CustomComponentOptionDao componentOptionDao;
    private final MallGoodsService mallGoodsService;
    private final MallProductService mallProductService;

    /** 注入定制商品配置及其商品和库存查询依赖. */
    public CustomProductServiceImpl(
            CustomProductDao customProductDao,
            CustomComponentOptionDao componentOptionDao,
            MallGoodsService mallGoodsService,
            MallProductService mallProductService) {
        this.customProductDao = customProductDao;
        this.componentOptionDao = componentOptionDao;
        this.mallGoodsService = mallGoodsService;
        this.mallProductService = mallProductService;
    }

    /** 获取定制器配置 / Gets customizer configuration. */
    @Override
    public Map<String, Object> getConfig(Integer goodsId) {
        if (goodsId == null) {
            throw new IllegalArgumentException("定制商品ID不能为空");
        }
        MallGoodsEntity goods = mallGoodsService.getById(goodsId);
        if (goods == null || Integer.valueOf(1).equals(goods.getIsDelete())) {
            throw new IllegalArgumentException("定制商品不存在或已进入回收站");
        }
        if (!Integer.valueOf(1).equals(goods.getIsOnSale())) {
            throw new IllegalArgumentException("定制商品已下架，请联系商家");
        }
        CustomProductEntity product = getByGoodsId(goodsId);
        if (product == null) {
            throw new IllegalArgumentException("该商品暂未开放定制");
        }
        Integer primaryProductId = goods.getPrimaryProductId();
        MallProductEntity primaryProduct =
                primaryProductId == null ? null : mallProductService.getById(primaryProductId);
        if (primaryProduct == null || !goodsId.equals(primaryProduct.getGoodsId())) {
            throw new IllegalArgumentException("定制商品缺少有效SKU，请联系商家");
        }
        List<CustomComponentOptionEntity> options = getEnabledOptions(goodsId);
        if (options.isEmpty()) {
            throw new IllegalArgumentException("商家尚未配置可选组件");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("product", product);
        result.put("options", options);
        result.put("primaryProductId", primaryProductId);
        return result;
    }

    /** 根据商品ID获取定制商品 / Gets custom product by goods id. */
    @Override
    public CustomProductEntity getByGoodsId(Integer goodsId) {
        return customProductDao.selectOne(
                new LambdaQueryWrapper<CustomProductEntity>()
                        .eq(CustomProductEntity::getGoodsId, goodsId)
                        .eq(CustomProductEntity::getEnabled, 1));
    }

    /** 根据商品ID获取启用的组件选项 / Gets enabled options by goods id. */
    @Override
    public List<CustomComponentOptionEntity> getEnabledOptions(Integer goodsId) {
        return componentOptionDao.selectList(
                new LambdaQueryWrapper<CustomComponentOptionEntity>()
                        .eq(CustomComponentOptionEntity::getGoodsId, goodsId)
                        .eq(CustomComponentOptionEntity::getEnabled, 1)
                        .in(
                                CustomComponentOptionEntity::getComponentType,
                                com.platform.modules.custom.service.MaterialTypes.ALL)
                        .orderByAsc(CustomComponentOptionEntity::getZIndex));
    }
}
