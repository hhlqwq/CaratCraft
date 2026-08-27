package com.platform.modules.custom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.modules.custom.dao.CustomComponentOptionDao;
import com.platform.modules.custom.dao.CustomProductDao;
import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import com.platform.modules.custom.entity.CustomProductEntity;
import com.platform.modules.custom.service.CustomProductService;
import com.platform.modules.mall.entity.MallGoodsEntity;
import com.platform.modules.mall.service.MallGoodsService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 定制商品服务实现 / Custom product service implementation.
 */
@Service
public class CustomProductServiceImpl implements CustomProductService {
    private final CustomProductDao customProductDao;
    private final CustomComponentOptionDao componentOptionDao;
    private final MallGoodsService mallGoodsService;

    public CustomProductServiceImpl(CustomProductDao customProductDao,
                                    CustomComponentOptionDao componentOptionDao,
                                    MallGoodsService mallGoodsService) {
        this.customProductDao = customProductDao;
        this.componentOptionDao = componentOptionDao;
        this.mallGoodsService = mallGoodsService;
    }

    /** 获取定制器配置 / Gets customizer configuration. */
    @Override
    public Map<String, Object> getConfig(Integer goodsId) {
        CustomProductEntity product = getByGoodsId(goodsId);
        if (product == null) {
            throw new IllegalArgumentException("该商品暂未开放定制");
        }
        List<CustomComponentOptionEntity> options = getEnabledOptions(goodsId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("product", product);
        result.put("options", options);
        MallGoodsEntity goods = mallGoodsService.getById(goodsId);
        result.put("primaryProductId", goods == null ? null : goods.getPrimaryProductId());
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
                        .orderByAsc(CustomComponentOptionEntity::getZIndex));
    }
}
