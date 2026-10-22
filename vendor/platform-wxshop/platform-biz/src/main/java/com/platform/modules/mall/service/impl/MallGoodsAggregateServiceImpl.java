/*
 *
 *      Copyright (c) 2018-2099, hehailong All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the openwtai.com developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: hehailong(845367567@qq.com)
 *
 */
package com.platform.modules.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.platform.common.exception.BusinessException;
import com.platform.modules.mall.dto.GoodsAggregateDTO;
import com.platform.modules.mall.entity.MallAttributeEntity;
import com.platform.modules.mall.entity.MallGoodsAttributeEntity;
import com.platform.modules.mall.entity.MallGoodsEntity;
import com.platform.modules.mall.entity.MallGoodsGalleryEntity;
import com.platform.modules.mall.entity.MallGoodsSpecificationEntity;
import com.platform.modules.mall.entity.MallProductEntity;
import com.platform.modules.mall.entity.MallSpecificationEntity;
import com.platform.modules.mall.service.MallAttributeService;
import com.platform.modules.mall.service.MallGoodsAggregateService;
import com.platform.modules.mall.service.MallGoodsAttributeService;
import com.platform.modules.mall.service.MallGoodsGalleryService;
import com.platform.modules.mall.service.MallGoodsService;
import com.platform.modules.mall.service.MallGoodsSpecificationService;
import com.platform.modules.mall.service.MallProductService;
import com.platform.modules.mall.service.MallSpecificationService;
import com.platform.modules.mall.service.support.SkuContract;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 商品聚合服务实现类
 *
 * @author 何海龙
 */
@Service
@RequiredArgsConstructor
public class MallGoodsAggregateServiceImpl implements MallGoodsAggregateService {

    private final MallGoodsService mallGoodsService;
    private final MallGoodsAttributeService mallGoodsAttributeService;
    private final MallGoodsGalleryService mallGoodsGalleryService;
    private final MallGoodsSpecificationService mallGoodsSpecificationService;
    private final MallProductService mallProductService;
    private final MallAttributeService mallAttributeService;
    private final MallSpecificationService mallSpecificationService;

    /** 查询商品聚合及同一标准下的规格和SKU. */
    @Override
    public GoodsAggregateDTO getAggregate(Integer goodsId) {
        MallGoodsEntity goods = mallGoodsService.getById(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }

        GoodsAggregateDTO dto = new GoodsAggregateDTO();
        dto.setGoods(goods);

        List<MallGoodsAttributeEntity> attributeEntities =
                mallGoodsAttributeService.list(
                        new QueryWrapper<MallGoodsAttributeEntity>().eq("goods_id", goodsId));
        Map<Integer, String> attributeNameMap = buildAttributeNameMap(attributeEntities);
        dto.setAttributes(
                attributeEntities.stream()
                        .map(
                                item -> {
                                    GoodsAggregateDTO.AttributeItem attr =
                                            new GoodsAggregateDTO.AttributeItem();
                                    attr.setId(item.getId());
                                    attr.setGoodsId(item.getGoodsId());
                                    attr.setAttributeId(item.getAttributeId());
                                    attr.setValue(item.getValue());
                                    attr.setAttributeName(
                                            attributeNameMap.get(item.getAttributeId()));
                                    return attr;
                                })
                        .collect(Collectors.toList()));

        List<MallGoodsGalleryEntity> galleryEntities =
                mallGoodsGalleryService.list(
                        new QueryWrapper<MallGoodsGalleryEntity>()
                                .eq("goods_id", goodsId)
                                .orderByAsc("sort_order", "id"));
        dto.setGallery(
                galleryEntities.stream()
                        .map(
                                item -> {
                                    GoodsAggregateDTO.GalleryItem gallery =
                                            new GoodsAggregateDTO.GalleryItem();
                                    gallery.setId(item.getId());
                                    gallery.setGoodsId(item.getGoodsId());
                                    gallery.setSortOrder(item.getSortOrder());
                                    gallery.setImgDesc(item.getImgDesc());
                                    gallery.setImgUrl(
                                            item.getImgUrl() == null
                                                    ? ""
                                                    : new String(
                                                            item.getImgUrl(),
                                                            StandardCharsets.UTF_8));
                                    return gallery;
                                })
                        .collect(Collectors.toList()));

        List<MallGoodsSpecificationEntity> specEntities =
                mallGoodsSpecificationService.list(
                        new QueryWrapper<MallGoodsSpecificationEntity>()
                                .eq("goods_id", goodsId)
                                .orderByAsc("specification_id", "id"));
        dto.setSpecs(buildSpecGroups(specEntities));

        List<MallProductEntity> productEntities =
                mallProductService.list(
                        new QueryWrapper<MallProductEntity>()
                                .eq("goods_id", goodsId)
                                .orderByAsc("id"));
        dto.setSkuList(
                productEntities.stream()
                        .map(
                                item -> {
                                    GoodsAggregateDTO.SkuItem sku = new GoodsAggregateDTO.SkuItem();
                                    sku.setId(item.getId());
                                    sku.setGoodsId(item.getGoodsId());
                                    sku.setGoodsSpecificationIds(item.getGoodsSpecificationIds());
                                    sku.setGoodsSn(item.getGoodsSn());
                                    sku.setGoodsNumber(item.getGoodsNumber());
                                    sku.setRetailPrice(item.getRetailPrice());
                                    sku.setMarketPrice(item.getMarketPrice());
                                    return sku;
                                })
                        .collect(Collectors.toList()));

        return dto;
    }

    /** 在事务中创建商品及完整子项, 不接受不完整组合. */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer saveAggregate(GoodsAggregateDTO aggregateDTO) {
        validateAggregate(aggregateDTO, false);

        MallGoodsEntity goods = aggregateDTO.getGoods();
        goods.setId(null);
        if (goods.getAddTime() == null) {
            goods.setAddTime(new Date());
        }
        mallGoodsService.add(goods);

        saveChildren(goods.getId(), aggregateDTO);
        refreshPrimaryProduct(goods.getId());

        return goods.getId();
    }

    /** 事务更新商品, 未变组合保留身份, 失败整体回滚. */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateAggregate(GoodsAggregateDTO aggregateDTO) {
        validateAggregate(aggregateDTO, true);

        MallGoodsEntity goods = aggregateDTO.getGoods();
        goods.setUpdateTime(new Date());
        mallGoodsService.update(goods);

        saveChildren(goods.getId(), aggregateDTO);
        refreshPrimaryProduct(goods.getId());

        return goods.getId();
    }

    /** 按商品归属协调属性、媒体、规格及SKU更新. */
    private void saveChildren(Integer goodsId, GoodsAggregateDTO aggregateDTO) {
        replaceAttributes(goodsId, aggregateDTO.getAttributes());
        replaceGallery(goodsId, aggregateDTO.getGallery());
        replaceSpecifications(goodsId, aggregateDTO.getSpecs());
        replaceSku(goodsId, aggregateDTO.getSkuList(), aggregateDTO.getGoods());
    }

    /** 替换当前商品的属性值, 不影响其他商品. */
    private void replaceAttributes(Integer goodsId, List<GoodsAggregateDTO.AttributeItem> items) {
        mallGoodsAttributeService.remove(
                new QueryWrapper<MallGoodsAttributeEntity>().eq("goods_id", goodsId));
        if (CollectionUtils.isEmpty(items)) {
            return;
        }
        List<MallGoodsAttributeEntity> entities =
                items.stream()
                        .filter(
                                item ->
                                        item.getAttributeId() != null
                                                && StringUtils.hasText(item.getValue()))
                        .map(
                                item -> {
                                    MallGoodsAttributeEntity entity =
                                            new MallGoodsAttributeEntity();
                                    entity.setGoodsId(goodsId);
                                    entity.setAttributeId(item.getAttributeId());
                                    entity.setValue(item.getValue());
                                    return entity;
                                })
                        .collect(Collectors.toList());
        if (!entities.isEmpty()) {
            mallGoodsAttributeService.saveBatch(entities);
        }
    }

    /** 保存当前商品的有序相册及UTF-8地址. */
    private void replaceGallery(Integer goodsId, List<GoodsAggregateDTO.GalleryItem> items) {
        mallGoodsGalleryService.remove(
                new QueryWrapper<MallGoodsGalleryEntity>().eq("goods_id", goodsId));
        if (CollectionUtils.isEmpty(items)) {
            return;
        }
        int fallbackSort = 1;
        List<MallGoodsGalleryEntity> entities = new ArrayList<>();
        for (GoodsAggregateDTO.GalleryItem item : items) {
            if (!StringUtils.hasText(item.getImgUrl())) {
                continue;
            }
            MallGoodsGalleryEntity entity = new MallGoodsGalleryEntity();
            entity.setGoodsId(goodsId);
            entity.setImgUrl(item.getImgUrl().getBytes(StandardCharsets.UTF_8));
            entity.setImgDesc(item.getImgDesc());
            entity.setSortOrder(item.getSortOrder() == null ? fallbackSort : item.getSortOrder());
            fallbackSort++;
            entities.add(entity);
        }
        if (!entities.isEmpty()) {
            mallGoodsGalleryService.saveBatch(entities);
        }
    }

    /** 保留未变规格值的行ID, 仅创建新增值并移除已取消的规格值. */
    private void replaceSpecifications(Integer goodsId, List<GoodsAggregateDTO.SpecGroup> groups) {
        List<MallGoodsSpecificationEntity> previous =
                mallGoodsSpecificationService.list(
                        new QueryWrapper<MallGoodsSpecificationEntity>()
                                .eq("goods_id", goodsId)
                                .last("FOR UPDATE"));
        Map<String, MallGoodsSpecificationEntity> existing = new HashMap<>();
        for (MallGoodsSpecificationEntity entity : previous) {
            String key = entity.getSpecificationId() + ":" + entity.getValue();
            if (existing.put(key, entity) != null) {
                throw new BusinessException("商品规格有重复记录, 请先核对数据");
            }
        }
        List<MallGoodsSpecificationEntity> inserts = new ArrayList<>();
        List<MallGoodsSpecificationEntity> updates = new ArrayList<>();
        for (GoodsAggregateDTO.SpecGroup group :
                groups == null ? List.<GoodsAggregateDTO.SpecGroup>of() : groups) {
            for (GoodsAggregateDTO.SpecValue value : group.getValues()) {
                MallGoodsSpecificationEntity entity =
                        existing.remove(group.getSpecificationId() + ":" + value.getValue());
                boolean added = entity == null;
                if (added) {
                    entity = new MallGoodsSpecificationEntity();
                    entity.setGoodsId(goodsId);
                    entity.setSpecificationId(group.getSpecificationId());
                    entity.setValue(value.getValue());
                }
                entity.setPicUrl(value.getPicUrl() == null ? "" : value.getPicUrl());
                (added ? inserts : updates).add(entity);
            }
        }
        if (!existing.isEmpty()) {
            mallGoodsSpecificationService.removeByIds(
                    existing.values().stream().map(MallGoodsSpecificationEntity::getId).toList());
        }
        if (!inserts.isEmpty()) {
            mallGoodsSpecificationService.saveBatch(inserts);
        }
        if (!updates.isEmpty()) {
            mallGoodsSpecificationService.updateBatchById(updates);
        }
    }

    /** 同一组合保持原SKU身份, 显式库存更新与新增库存缺省分开处理. */
    private void replaceSku(
            Integer goodsId, List<GoodsAggregateDTO.SkuItem> items, MallGoodsEntity goods) {
        Map<String, MallProductEntity> existing = new HashMap<>();
        for (MallProductEntity product :
                mallProductService.list(
                        new QueryWrapper<MallProductEntity>()
                                .eq("goods_id", goodsId)
                                .last("FOR UPDATE"))) {
            SkuContract.parse(product.getGoodsSpecificationIds());
            if (existing.put(product.getGoodsSpecificationIds(), product) != null) {
                throw new BusinessException("商品SKU重复, 请先核对数据");
            }
        }
        if (items == null || items.isEmpty()) {
            GoodsAggregateDTO.SkuItem item = new GoodsAggregateDTO.SkuItem();
            item.setGoodsSpecificationIds("");
            items = List.of(item);
        }
        List<MallProductEntity> inserts = new ArrayList<>();
        List<MallProductEntity> updates = new ArrayList<>();
        for (GoodsAggregateDTO.SkuItem item : items) {
            MallProductEntity product = existing.remove(item.getGoodsSpecificationIds());
            boolean added = product == null;
            if (added) {
                product = new MallProductEntity();
                product.setGoodsId(goodsId);
                product.setGoodsSpecificationIds(item.getGoodsSpecificationIds());
            }
            product.setGoodsSn(
                    StringUtils.hasText(item.getGoodsSn())
                            ? item.getGoodsSn()
                            : goods.getGoodsSn());
            if (item.getGoodsNumber() != null || added) {
                product.setGoodsNumber(
                        item.getGoodsNumber() == null
                                ? goods.getGoodsNumber()
                                : item.getGoodsNumber());
            }
            product.setRetailPrice(
                    item.getRetailPrice() == null
                            ? nvl(goods.getRetailPrice())
                            : item.getRetailPrice());
            product.setMarketPrice(
                    item.getMarketPrice() == null
                            ? nvl(goods.getMarketPrice())
                            : item.getMarketPrice());
            (added ? inserts : updates).add(product);
        }
        if (!existing.isEmpty()) {
            mallProductService.removeByIds(
                    existing.values().stream().map(MallProductEntity::getId).toList());
        }
        if (!inserts.isEmpty()) {
            mallProductService.saveBatch(inserts);
        }
        if (!updates.isEmpty()) {
            mallProductService.updateBatchById(updates);
        }
    }

    /** 从当前商品SKU中更新默认购买入口. */
    private void refreshPrimaryProduct(Integer goodsId) {
        MallProductEntity firstSku =
                mallProductService.getOne(
                        new QueryWrapper<MallProductEntity>()
                                .eq("goods_id", goodsId)
                                .orderByAsc("id")
                                .last("LIMIT 1"));
        if (firstSku == null) {
            return;
        }
        MallGoodsEntity goods = new MallGoodsEntity();
        goods.setId(goodsId);
        goods.setPrimaryProductId(firstSku.getId());
        mallGoodsService.updateById(goods);
    }

    /** 持久化前验证商品必需字段与唯一规格契约. */
    private void validateAggregate(GoodsAggregateDTO aggregateDTO, boolean checkId) {
        if (aggregateDTO == null || aggregateDTO.getGoods() == null) {
            throw new BusinessException("商品数据不能为空");
        }
        MallGoodsEntity goods = aggregateDTO.getGoods();

        if (checkId && goods.getId() == null) {
            throw new BusinessException("商品ID不能为空");
        }
        if (!StringUtils.hasText(goods.getName())) {
            throw new BusinessException("商品名称不能为空");
        }
        if (goods.getCategoryId() == null) {
            throw new BusinessException("商品分类不能为空");
        }
        if (goods.getRetailPrice() == null) {
            throw new BusinessException("商品零售价不能为空");
        }

        try {
            SkuContract.validate(aggregateDTO);
        } catch (IllegalArgumentException error) {
            throw new BusinessException(error.getMessage());
        }

        if (!CollectionUtils.isEmpty(aggregateDTO.getGallery())) {
            for (GoodsAggregateDTO.GalleryItem item : aggregateDTO.getGallery()) {
                if (!StringUtils.hasText(item.getImgUrl())) {
                    throw new BusinessException("商品相册图片不能为空");
                }
            }
        }
    }

    /** 按规格组聚合当前商品的值并附上组名. */
    private List<GoodsAggregateDTO.SpecGroup> buildSpecGroups(
            List<MallGoodsSpecificationEntity> specEntities) {
        if (CollectionUtils.isEmpty(specEntities)) {
            return Collections.emptyList();
        }
        Set<Integer> specIds =
                specEntities.stream()
                        .map(MallGoodsSpecificationEntity::getSpecificationId)
                        .collect(Collectors.toSet());
        Map<Integer, String> specNameMap =
                mallSpecificationService.listByIds(specIds).stream()
                        .collect(
                                Collectors.toMap(
                                        MallSpecificationEntity::getId,
                                        MallSpecificationEntity::getName,
                                        (a, b) -> a));

        Map<Integer, GoodsAggregateDTO.SpecGroup> groupMap = new LinkedHashMap<>();
        for (MallGoodsSpecificationEntity specEntity : specEntities) {
            GoodsAggregateDTO.SpecGroup group =
                    groupMap.computeIfAbsent(
                            specEntity.getSpecificationId(),
                            key -> {
                                GoodsAggregateDTO.SpecGroup g = new GoodsAggregateDTO.SpecGroup();
                                g.setSpecificationId(key);
                                g.setSpecificationName(specNameMap.get(key));
                                return g;
                            });
            GoodsAggregateDTO.SpecValue value = new GoodsAggregateDTO.SpecValue();
            value.setValue(specEntity.getValue());
            value.setPicUrl(specEntity.getPicUrl());
            group.getValues().add(value);
        }
        return new ArrayList<>(groupMap.values());
    }

    /** 批量读取已用属性名, 避免逐行查询. */
    private Map<Integer, String> buildAttributeNameMap(
            List<MallGoodsAttributeEntity> attributeEntities) {
        if (CollectionUtils.isEmpty(attributeEntities)) {
            return Collections.emptyMap();
        }
        Set<Integer> ids =
                attributeEntities.stream()
                        .map(MallGoodsAttributeEntity::getAttributeId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return mallAttributeService.listByIds(ids).stream()
                .collect(
                        Collectors.toMap(
                                MallAttributeEntity::getId,
                                MallAttributeEntity::getName,
                                (a, b) -> a));
    }

    /** 仅对可选金额提供零值, 不用于补齐未知工艺参数. */
    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
