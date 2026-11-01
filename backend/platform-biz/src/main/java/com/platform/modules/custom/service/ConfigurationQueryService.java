package com.platform.modules.custom.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.modules.custom.dao.CustomComponentOptionDao;
import com.platform.modules.custom.dao.CustomProductDao;
import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import com.platform.modules.custom.entity.CustomProductEntity;
import com.platform.modules.mall.entity.MallGoodsEntity;
import com.platform.modules.mall.service.MallGoodsService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/** 配置列表统一分页并明确编辑字段, 不暴露商城完整实体. */
@Service
@RequiredArgsConstructor
public class ConfigurationQueryService {
    private final CustomProductDao products;
    private final CustomComponentOptionDao options;
    private final MallGoodsService goods;

    /** 商品选择仅返回身份和名称. */
    public record GoodsView(Integer id, String name) {}

    /** 定制配置的稳定编辑契约. */
    public record ProductView(
            Long id,
            Integer goodsId,
            String templateCode,
            Integer previewWidth,
            Integer previewHeight,
            String modelUrl,
            String requiredComponentTypes,
            Integer enabled,
            String lengthRules) {}

    /** 材料价格表示每实例加价, 库存耗用量由规则另行定义. */
    public record OptionView(
            Long id,
            Integer goodsId,
            String componentType,
            String optionCode,
            String name,
            BigDecimal priceDelta,
            String assetUrl,
            String modelUrl,
            String materialParams,
            Integer zIndex,
            Integer enabled) {}

    /** 搜索未回收商城商品, 只加载当前页最小字段. */
    public Map<String, Object> goods(int page, int size, String keyword) {
        validate(page, size, keyword);
        Page<MallGoodsEntity> result =
                goods.page(
                        new Page<>(page, size),
                        new LambdaQueryWrapper<MallGoodsEntity>()
                                .select(MallGoodsEntity::getId, MallGoodsEntity::getName)
                                .eq(MallGoodsEntity::getIsDelete, 0)
                                .like(
                                        keyword != null && !keyword.isBlank(),
                                        MallGoodsEntity::getName,
                                        keyword)
                                .orderByDesc(MallGoodsEntity::getId));
        return response(
                result,
                result.getRecords().stream()
                        .map(row -> new GoodsView(row.getId(), row.getName()))
                        .toList());
    }

    /** 查询当前页定制配置, 同商品使用主键稳定排序. */
    public Map<String, Object> products(int page, int size) {
        validate(page, size, null);
        Page<CustomProductEntity> result =
                products.selectPage(
                        new Page<>(page, size),
                        new LambdaQueryWrapper<CustomProductEntity>()
                                .orderByAsc(CustomProductEntity::getGoodsId)
                                .orderByAsc(CustomProductEntity::getId));
        return response(
                result,
                result.getRecords().stream()
                        .map(
                                row ->
                                        new ProductView(
                                                row.getId(),
                                                row.getGoodsId(),
                                                row.getTemplateCode(),
                                                row.getPreviewWidth(),
                                                row.getPreviewHeight(),
                                                row.getModelUrl(),
                                                row.getRequiredComponentTypes(),
                                                row.getEnabled(),
                                                row.getLengthRules()))
                        .toList());
    }

    /** 仅查询指定商品材料, 支持名称及代码搜索. */
    public Map<String, Object> options(Integer goodsId, int page, int size, String keyword) {
        validate(page, size, keyword);
        if (goodsId == null || goodsId < 1) {
            throw new IllegalArgumentException("请选择商品");
        }
        var wrapper =
                new LambdaQueryWrapper<CustomComponentOptionEntity>()
                        .eq(CustomComponentOptionEntity::getGoodsId, goodsId)
                        .and(
                                keyword != null && !keyword.isBlank(),
                                query ->
                                        query.like(CustomComponentOptionEntity::getName, keyword)
                                                .or()
                                                .like(
                                                        CustomComponentOptionEntity::getOptionCode,
                                                        keyword))
                        .orderByAsc(CustomComponentOptionEntity::getZIndex)
                        .orderByAsc(CustomComponentOptionEntity::getId);
        Page<CustomComponentOptionEntity> result =
                options.selectPage(new Page<>(page, size), wrapper);
        return response(
                result,
                result.getRecords().stream()
                        .map(
                                row ->
                                        new OptionView(
                                                row.getId(),
                                                row.getGoodsId(),
                                                row.getComponentType(),
                                                row.getOptionCode(),
                                                row.getName(),
                                                row.getPriceDelta(),
                                                row.getAssetUrl(),
                                                row.getModelUrl(),
                                                row.getMaterialParams(),
                                                row.getZIndex(),
                                                row.getEnabled()))
                        .toList());
    }

    /** 拒绝无界分页和过长搜索条件. */
    private static void validate(int page, int size, String keyword) {
        if (page < 1
                || page > 100000
                || size < 1
                || size > 100
                || keyword != null && keyword.length() > 100) {
            throw new IllegalArgumentException("分页或搜索参数不正确");
        }
    }

    /** 返回标准分页结构, total始终为记录数. */
    private static Map<String, Object> response(Page<?> page, Object records) {
        return Map.of(
                "records",
                records,
                "total",
                page.getTotal(),
                "current",
                page.getCurrent(),
                "size",
                page.getSize());
    }
}
