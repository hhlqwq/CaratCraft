package com.platform.modules.custom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.modules.custom.dao.ComponentStockDao;
import com.platform.modules.custom.dao.CustomComponentOptionDao;
import com.platform.modules.custom.dao.CustomProductDao;
import com.platform.modules.custom.dto.CustomQuoteRequest;
import com.platform.modules.custom.entity.ComponentStockEntity;
import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import com.platform.modules.custom.entity.CustomProductEntity;
import com.platform.modules.custom.service.CustomQuoteService;
import com.platform.modules.mall.entity.MallGoodsEntity;
import com.platform.modules.mall.service.MallGoodsService;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** 服务端定制报价实现 / Server-side custom quote implementation. */
@Service
public class CustomQuoteServiceImpl implements CustomQuoteService {
    private final MallGoodsService mallGoodsService;
    private final CustomComponentOptionDao componentOptionDao;
    private final CustomProductDao customProductDao;
    private final ComponentStockDao componentStockDao;

    /** 注入可信商品、材料与库存来源, 报价不采用客户端传入的价格或尺寸. */
    public CustomQuoteServiceImpl(
            MallGoodsService mallGoodsService,
            CustomComponentOptionDao componentOptionDao,
            CustomProductDao customProductDao,
            ComponentStockDao componentStockDao) {
        this.mallGoodsService = mallGoodsService;
        this.componentOptionDao = componentOptionDao;
        this.customProductDao = customProductDao;
        this.componentStockDao = componentStockDao;
    }

    /**
     * 根据选项计算最终价格 / Calculates final price from selected options.
     *
     * @param request 报价请求 / quote request
     * @return 报价结果 / quote result
     */
    @Override
    public Map<String, Object> quote(CustomQuoteRequest request) {
        if (request == null || request.getGoodsId() == null) {
            throw new IllegalArgumentException("请选择定制商品");
        }
        MallGoodsEntity goods = mallGoodsService.getById(request.getGoodsId());
        if (goods == null
                || Integer.valueOf(1).equals(goods.getIsDelete())
                || !Integer.valueOf(1).equals(goods.getIsOnSale())) {
            throw new IllegalArgumentException("定制商品不可售");
        }
        if (request.getEngraving() != null && request.getEngraving().length() > 20) {
            throw new IllegalArgumentException("刻字不能超过20个字符");
        }
        BigDecimal basePrice = Objects.requireNonNullElse(goods.getRetailPrice(), BigDecimal.ZERO);
        List<CustomComponentOptionEntity> options =
                componentOptionDao.selectList(
                        new LambdaQueryWrapper<CustomComponentOptionEntity>()
                                .eq(CustomComponentOptionEntity::getGoodsId, request.getGoodsId())
                                .eq(CustomComponentOptionEntity::getEnabled, 1));
        BigDecimal optionTotal = BigDecimal.ZERO;
        java.util.List<CustomComponentOptionEntity> selectedOptions = new java.util.ArrayList<>();
        java.util.List<Map<String, Object>> components = new java.util.ArrayList<>();
        Set<String> selectedTypes = new HashSet<>();
        boolean arranged = request.getArrangementOptionIds() != null;
        List<Long> instanceIds =
                arranged ? request.getArrangementOptionIds() : request.getOptionIds();
        if (arranged && request.getOptionIds() != null && !request.getOptionIds().isEmpty()) {
            throw new IllegalArgumentException("不能同时提交排列和旧版选项");
        }
        if (instanceIds != null && instanceIds.size() > 300) {
            throw new IllegalArgumentException("单件设计最多300个材料实例");
        }
        Map<Long, Integer> counts = new LinkedHashMap<>();
        if (instanceIds != null) {
            for (Long id : instanceIds) {
                if (id == null || id <= 0) {
                    throw new IllegalArgumentException("材料ID无效");
                }
                counts.merge(id, 1, Integer::sum);
            }
        }
        if (instanceIds != null) {
            for (Long optionId : counts.keySet()) {
                if (!arranged && counts.get(optionId) > 1) {
                    throw new IllegalArgumentException("定制选项不能重复");
                }
                CustomComponentOptionEntity option =
                        options.stream()
                                .filter(item -> item.getId().equals(optionId))
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("包含无效的定制选项"));
                if (!arranged && !selectedTypes.add(option.getComponentType())) {
                    throw new IllegalArgumentException("同类组件只能选择一个");
                }
                selectedTypes.add(option.getComponentType());
                selectedOptions.add(option);
                var rules =
                        com.platform.modules.custom.service.ComponentRules.parse(
                                option.getMaterialParams());
                Map<String, Object> component = new LinkedHashMap<>();
                component.put("optionId", option.getId());
                component.put("componentType", option.getComponentType());
                component.put("optionCode", option.getOptionCode());
                component.put("name", option.getName());
                int instances = counts.get(optionId);
                int quantity = Math.multiplyExact(rules.quantity(), instances);
                if (quantity > 10000) {
                    throw new IllegalArgumentException("单种材料累计用量不能超过10000单位");
                }
                component.put("instances", instances);
                component.put("quantity", quantity);
                component.put("unit", rules.unit());
                BigDecimal unitPrice =
                        Objects.requireNonNullElse(option.getPriceDelta(), BigDecimal.ZERO);
                if (unitPrice.signum() < 0) {
                    throw new IllegalArgumentException("材料价格不能为负数");
                }
                component.put("unitPrice", unitPrice);
                component.put("linePrice", unitPrice.multiply(BigDecimal.valueOf(instances)));
                component.put("assetUrl", option.getAssetUrl());
                component.put("zIndex", option.getZIndex());
                component.put("materialParams", option.getMaterialParams());
                components.add(component);
                // 校验组件库存 / Validate component stock
                ComponentStockEntity stock =
                        componentStockDao.selectOne(
                                new LambdaQueryWrapper<ComponentStockEntity>()
                                        .eq(ComponentStockEntity::getGoodsId, request.getGoodsId())
                                        .eq(
                                                ComponentStockEntity::getComponentType,
                                                option.getComponentType())
                                        .eq(
                                                ComponentStockEntity::getOptionCode,
                                                option.getOptionCode()));
                if (quantity > 0
                        && (stock == null
                                || stock.getStock() == null
                                || stock.getStock() < quantity)) {
                    throw new IllegalArgumentException("组件 " + option.getName() + " 库存不足");
                }
                optionTotal = optionTotal.add(unitPrice.multiply(BigDecimal.valueOf(instances)));
            }
        }
        CustomProductEntity customProduct =
                customProductDao.selectOne(
                        new LambdaQueryWrapper<CustomProductEntity>()
                                .eq(CustomProductEntity::getGoodsId, request.getGoodsId())
                                .eq(CustomProductEntity::getEnabled, 1));
        boolean requiredComplete = hasRequiredTypes(customProduct, selectedTypes);
        com.platform.modules.custom.service.ComponentRules.validate(selectedOptions);
        BigDecimal totalPrice = basePrice.add(optionTotal);
        if (totalPrice.compareTo(new BigDecimal("0.01")) < 0) {
            throw new IllegalArgumentException("定制商品价格配置无效");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("goodsId", goods.getId());
        result.put("basePrice", basePrice);
        result.put("optionTotal", optionTotal);
        result.put("totalPrice", totalPrice);
        result.put("selectedOptionIds", request.getOptionIds());
        result.put("components", components);
        Map<String, Object> arrangement =
                com.platform.modules.custom.service.NecklaceArrangement.describe(
                        instanceIds == null ? List.of() : instanceIds, selectedOptions);
        if (arranged) {
            result.putAll(arrangement);
        }
        Map<String, Object> sizing =
                com.platform.modules.custom.service.FinishedLengthRules.evaluate(
                        customProduct.getLengthRules(), request, arrangement);
        if (!requiredComplete) {
            sizing.put("ready", false);
            sizing.put("notice", "必选材料尚未补齐, 可以保存草稿后继续编辑");
        }
        result.put("sizing", sizing);
        result.put("checkoutReady", sizing.get("ready"));
        result.put("previewWidth", customProduct.getPreviewWidth());
        result.put("previewHeight", customProduct.getPreviewHeight());
        result.put("engraving", request.getEngraving());
        return result;
    }

    /** 校验商品配置的必选组件组 / Validates required component groups. */
    private boolean hasRequiredTypes(CustomProductEntity product, Set<String> selectedTypes) {
        if (product == null) {
            throw new IllegalArgumentException("该商品未开放定制");
        }
        if (product.getRequiredComponentTypes() == null
                || product.getRequiredComponentTypes().isBlank()) {
            return true;
        }
        for (String requiredType : product.getRequiredComponentTypes().split(",")) {
            if (!requiredType.isBlank() && !selectedTypes.contains(requiredType.trim())) {
                return false;
            }
        }
        return true;
    }
}
