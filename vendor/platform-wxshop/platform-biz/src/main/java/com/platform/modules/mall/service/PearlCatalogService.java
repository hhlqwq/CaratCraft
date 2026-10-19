package com.platform.modules.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.modules.mall.entity.MallGoodsEntity;
import java.util.Map;
import org.springframework.stereotype.Service;

/** 按商家填写的珍珠来源和成品品类提供可售商品目录. */
@Service
public class PearlCatalogService {
  private final MallGoodsService goods;

  /** 注入商品分页查询接口. */
  public PearlCatalogService(MallGoodsService goods) {
    this.goods = goods;
  }

  /** 两个分类维度可独立组合, 分页总数与可售过滤使用同一查询. */
  public Map<String, Object> page(String source, String type, int page, int limit) {
    if (page < 1 || page > 100000 || limit < 1 || limit > 50) {
      throw new IllegalArgumentException("分页参数无效");
    }
    if (source != null && !source.isBlank() && !PearlProductValidator.SOURCES.contains(source)) {
      throw new IllegalArgumentException("珍珠来源无效");
    }
    if (type != null && !type.isBlank() && !PearlProductValidator.TYPES.contains(type)) {
      throw new IllegalArgumentException("成品品类无效");
    }
    var filter =
        new LambdaQueryWrapper<MallGoodsEntity>()
            .eq(MallGoodsEntity::getIsDelete, 0)
            .eq(MallGoodsEntity::getIsOnSale, 1)
            .in(MallGoodsEntity::getPearlSource, PearlProductValidator.SOURCES)
            .in(MallGoodsEntity::getJewelryType, PearlProductValidator.TYPES)
            .eq(source != null && !source.isBlank(), MallGoodsEntity::getPearlSource, source)
            .eq(type != null && !type.isBlank(), MallGoodsEntity::getJewelryType, type)
            .orderByDesc(MallGoodsEntity::getId);
    var result = goods.page(new Page<MallGoodsEntity>(page, limit), filter);
    return Map.of("records", result.getRecords(), "total", result.getTotal());
  }
}
