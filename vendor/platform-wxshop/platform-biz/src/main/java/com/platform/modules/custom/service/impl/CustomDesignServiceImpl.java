package com.platform.modules.custom.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.platform.modules.custom.dao.ComponentStockDao;
import com.platform.modules.custom.dao.CustomDesignSnapshotDao;
import com.platform.modules.custom.dao.CustomOrderCustomizationDao;
import com.platform.modules.custom.dao.ProductionWorkOrderDao;
import com.platform.modules.custom.dto.CustomQuoteRequest;
import com.platform.modules.custom.entity.CustomDesignSnapshotEntity;
import com.platform.modules.custom.entity.CustomOrderCustomizationEntity;
import com.platform.modules.custom.entity.ProductionWorkOrderEntity;
import com.platform.modules.custom.service.CustomDesignService;
import com.platform.modules.custom.service.CustomQuoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 定制设计服务实现 / Custom design service implementation.
 */
@Service
public class CustomDesignServiceImpl implements CustomDesignService {
    private final CustomDesignSnapshotDao snapshotDao;
    private final CustomOrderCustomizationDao orderCustomizationDao;
    private final ProductionWorkOrderDao workOrderDao;
    private final CustomQuoteService quoteService;
    private final ComponentStockDao componentStockDao;

    public CustomDesignServiceImpl(CustomDesignSnapshotDao snapshotDao,
                                   CustomOrderCustomizationDao orderCustomizationDao,
                                   ProductionWorkOrderDao workOrderDao,
                                   CustomQuoteService quoteService,
                                   ComponentStockDao componentStockDao) {
        this.snapshotDao = snapshotDao;
        this.orderCustomizationDao = orderCustomizationDao;
        this.workOrderDao = workOrderDao;
        this.quoteService = quoteService;
        this.componentStockDao = componentStockDao;
    }

    /** 保存设计草稿，并固化当时的服务端报价 / Saves a draft and its server quote. */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> saveDraft(Long userId, CustomQuoteRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("请先登录");
        }
        Map<String, Object> quote = quoteService.quote(request);
        CustomDesignSnapshotEntity snapshot = new CustomDesignSnapshotEntity();
        snapshot.setUserId(userId);
        snapshot.setGoodsId(request.getGoodsId());
        snapshot.setConfigurationJson(JSON.toJSONString(request));
        snapshot.setPriceBreakdownJson(JSON.toJSONString(quote));
        snapshot.setPreviewUrl(request.getPreviewUrl());
        snapshot.setStatus("DRAFT");
        snapshot.setCreateTime(new Date());
        snapshot.setUpdateTime(new Date());
        snapshotDao.insert(snapshot);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("snapshotId", snapshot.getId());
        result.put("status", snapshot.getStatus());
        result.put("quote", quote);
        return result;
    }

    /** 获取当前用户拥有的设计 / Gets a design owned by the current user. */
    @Override
    public CustomDesignSnapshotEntity getOwned(Long snapshotId, Long userId) {
        CustomDesignSnapshotEntity snapshot = snapshotDao.selectById(snapshotId);
        if (snapshot == null || !userId.equals(snapshot.getUserId())) {
            throw new IllegalArgumentException("设计不存在或无权访问");
        }
        return snapshot;
    }

    /** 按创建时间倒序查询用户设计 / Lists user designs by creation time. */
    @Override
    public List<CustomDesignSnapshotEntity> listOwned(Long userId) {
        return snapshotDao.selectList(new LambdaQueryWrapper<CustomDesignSnapshotEntity>()
                .eq(CustomDesignSnapshotEntity::getUserId, userId)
                .orderByDesc(CustomDesignSnapshotEntity::getCreateTime));
    }

    /** 批量装配设计与订单关联, 避免列表逐条查询 / Batch assembles design-order relations. */
    @Override
    public List<Map<String, Object>> listOwnedWithOrders(Long userId) {
        List<CustomDesignSnapshotEntity> snapshots = listOwned(userId);
        Map<Long, Integer> orderIdsBySnapshot = new HashMap<>();
        if (!snapshots.isEmpty()) {
            List<Long> snapshotIds = snapshots.stream()
                    .map(CustomDesignSnapshotEntity::getId)
                    .toList();
            List<CustomOrderCustomizationEntity> bindings = orderCustomizationDao.selectList(
                    new LambdaQueryWrapper<CustomOrderCustomizationEntity>()
                            .in(CustomOrderCustomizationEntity::getSnapshotId, snapshotIds));
            for (CustomOrderCustomizationEntity binding : bindings) {
                orderIdsBySnapshot.put(binding.getSnapshotId(), binding.getOrderId());
            }
        }
        return snapshots.stream().map(snapshot -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", snapshot.getId());
            item.put("goodsId", snapshot.getGoodsId());
            item.put("configurationJson", snapshot.getConfigurationJson());
            item.put("priceBreakdownJson", snapshot.getPriceBreakdownJson());
            item.put("previewUrl", snapshot.getPreviewUrl());
            item.put("status", snapshot.getStatus());
            item.put("createTime", snapshot.getCreateTime());
            item.put("updateTime", snapshot.getUpdateTime());
            item.put("orderId", orderIdsBySnapshot.get(snapshot.getId()));
            return item;
        }).toList();
    }

    /** 解析快照并返回最新报价 / Parses a snapshot and returns its latest quote. */
    @Override
    public Map<String, Object> quoteOwned(Long snapshotId, Long userId) {
        CustomDesignSnapshotEntity snapshot = getOwned(snapshotId, userId);
        CustomQuoteRequest request = JSON.parseObject(
                snapshot.getConfigurationJson(), CustomQuoteRequest.class);
        return quoteService.quote(request);
    }

    /** 按最新商品和选项价格重新报价并锁定 / Requotes with current prices and locks the design. */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal lockAndGetPrice(Long snapshotId, Long userId) {
        CustomDesignSnapshotEntity snapshot = getOwned(snapshotId, userId);
        if (!"DRAFT".equals(snapshot.getStatus())) {
            throw new IllegalArgumentException("该设计正在结算或已提交订单");
        }
        CustomQuoteRequest request = JSON.parseObject(snapshot.getConfigurationJson(), CustomQuoteRequest.class);
        Map<String, Object> quote = quoteService.quote(request);
        BigDecimal totalPrice = new BigDecimal(String.valueOf(quote.get("totalPrice")));
        int updated = snapshotDao.update(null, new UpdateWrapper<CustomDesignSnapshotEntity>()
                .eq("id", snapshotId)
                .eq("user_id", userId)
                .eq("status", "DRAFT")
                .set("price_breakdown_json", JSON.toJSONString(quote))
                .set("status", "LOCKED")
                .set("update_time", new Date()));
        if (updated != 1) {
            throw new IllegalArgumentException("设计已被其他结算请求占用");
        }
        return totalPrice;
    }

    /** 将已锁定设计绑定到订单商品 / Binds a locked design to an order item. */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindOrder(Long snapshotId, Long userId, Integer orderId, Integer orderGoodsId) {
        CustomDesignSnapshotEntity snapshot = getOwned(snapshotId, userId);
        if (!"LOCKED".equals(snapshot.getStatus())) {
            throw new IllegalArgumentException("设计尚未锁定");
        }
        // 扣减组件库存 / Deduct component stock.
        deductComponentStock(snapshot);
        CustomOrderCustomizationEntity binding = new CustomOrderCustomizationEntity();
        binding.setOrderId(orderId);
        binding.setOrderGoodsId(orderGoodsId);
        binding.setSnapshotId(snapshotId);
        binding.setStatus("LOCKED");
        binding.setCreateTime(new Date());
        binding.setUpdateTime(new Date());
        orderCustomizationDao.insert(binding);
        snapshot.setStatus("ORDERED");
        snapshot.setUpdateTime(new Date());
        snapshotDao.updateById(snapshot);
    }

    /** 扣减设计所选组件库存, 条件更新未命中说明库存不足, 防止并发超售 /
     * Deducts stock for each selected option; a missed conditional update means oversold. */
    private void deductComponentStock(CustomDesignSnapshotEntity snapshot) {
        CustomQuoteRequest request = JSON.parseObject(
                snapshot.getConfigurationJson(), CustomQuoteRequest.class);
        if (request == null || request.getOptionIds() == null) {
            return;
        }
        for (Long optionId : request.getOptionIds()) {
            if (componentStockDao.deductByOptionId(snapshot.getGoodsId(), optionId, 1) != 1) {
                throw new IllegalArgumentException("组件库存不足，请重新选择设计");
            }
        }
    }

    /** 恢复设计所选组件库存 / Restores stock for each selected option. */
    private void restoreComponentStock(CustomDesignSnapshotEntity snapshot) {
        CustomQuoteRequest request = JSON.parseObject(
                snapshot.getConfigurationJson(), CustomQuoteRequest.class);
        if (request == null || request.getOptionIds() == null) {
            return;
        }
        for (Long optionId : request.getOptionIds()) {
            componentStockDao.restoreByOptionId(snapshot.getGoodsId(), optionId, 1);
        }
    }

    /** 幂等更新定制状态并创建待生产工单 / Idempotently marks paid and creates a work order. */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markOrderPaid(Integer orderId) {
        CustomOrderCustomizationEntity binding = orderCustomizationDao.selectOne(
                new LambdaQueryWrapper<CustomOrderCustomizationEntity>()
                        .eq(CustomOrderCustomizationEntity::getOrderId, orderId));
        if (binding == null) {
            return;
        }
        // 仅允许未支付绑定流转为已支付, 防止迟到回调复活已取消订单 /
        // Only LOCKED bindings may become PAID so late callbacks cannot revive cancelled orders.
        int updated = orderCustomizationDao.update(null, new UpdateWrapper<CustomOrderCustomizationEntity>()
                .eq("id", binding.getId())
                .eq("status", "LOCKED")
                .set("status", "PAID")
                .set("update_time", new Date()));
        if (updated != 1) {
            return;
        }
        Long count = workOrderDao.selectCount(new LambdaQueryWrapper<ProductionWorkOrderEntity>()
                .eq(ProductionWorkOrderEntity::getOrderId, orderId));
        if (count > 0) {
            return;
        }
        CustomDesignSnapshotEntity snapshot = snapshotDao.selectById(binding.getSnapshotId());
        if (snapshot == null) {
            throw new IllegalStateException("定制设计快照不存在");
        }
        ProductionWorkOrderEntity workOrder = new ProductionWorkOrderEntity();
        workOrder.setWorkOrderNo("CC-" + orderId);
        workOrder.setOrderId(orderId);
        workOrder.setOrderGoodsId(binding.getOrderGoodsId());
        workOrder.setSnapshotId(binding.getSnapshotId());
        workOrder.setStatus("PENDING_REVIEW");
        workOrder.setCraftJson(snapshot.getConfigurationJson());
        workOrder.setCreateTime(new Date());
        workOrder.setUpdateTime(new Date());
        workOrderDao.insert(workOrder);
    }

    /** 取消尚未完成的定制生产流程, 释放设计并恢复组件库存 /
     * Cancels the workflow, releases the design and restores component stock. */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Integer orderId) {
        CustomOrderCustomizationEntity binding = orderCustomizationDao.selectOne(
                new LambdaQueryWrapper<CustomOrderCustomizationEntity>()
                        .eq(CustomOrderCustomizationEntity::getOrderId, orderId));
        if (binding == null) {
            return;
        }
        boolean wasUnpaid = "LOCKED".equals(binding.getStatus());
        // 条件更新保证取消只执行一次, 防止与支付回调并发或重复调用导致重复恢复库存 /
        // Conditional update ensures cancellation runs once, avoiding double stock restoration.
        int updated = orderCustomizationDao.update(null, new UpdateWrapper<CustomOrderCustomizationEntity>()
                .eq("id", binding.getId())
                .eq("status", binding.getStatus())
                .set("status", "CANCELLED")
                .set("update_time", new Date()));
        if (updated != 1) {
            return;
        }
        if (binding.getSnapshotId() != null) {
            CustomDesignSnapshotEntity snapshot = snapshotDao.selectById(binding.getSnapshotId());
            if (snapshot != null) {
                // 恢复组件库存 / Restore component stock.
                restoreComponentStock(snapshot);
                // 未支付取消时将设计快照恢复为草稿, 允许用户重新下单 /
                // Restore an unpaid design back to DRAFT so the user can reorder it.
                if (wasUnpaid && "ORDERED".equals(snapshot.getStatus())) {
                    snapshot.setStatus("DRAFT");
                    snapshot.setUpdateTime(new Date());
                    snapshotDao.updateById(snapshot);
                }
            }
        }
        ProductionWorkOrderEntity workOrder = workOrderDao.selectOne(
                new LambdaQueryWrapper<ProductionWorkOrderEntity>()
                        .eq(ProductionWorkOrderEntity::getOrderId, orderId));
        if (workOrder != null && !"COMPLETED".equals(workOrder.getStatus())
                && !"CANCELLED".equals(workOrder.getStatus())) {
            workOrder.setStatus("CANCELLED");
            workOrder.setUpdateTime(new Date());
            workOrderDao.updateById(workOrder);
        }
    }

    /** 校验已开工的定制订单不可直接退款 / Prevents direct refunds after production starts. */
    @Override
    public void assertOrderCancellable(Integer orderId) {
        ProductionWorkOrderEntity workOrder = workOrderDao.selectOne(
                new LambdaQueryWrapper<ProductionWorkOrderEntity>()
                        .eq(ProductionWorkOrderEntity::getOrderId, orderId));
        if (workOrder == null) {
            return;
        }
        Set<String> nonCancellable = Set.of("PRODUCING", "QUALITY_CHECK", "COMPLETED");
        if (nonCancellable.contains(workOrder.getStatus())) {
            throw new IllegalStateException("定制商品已进入生产，请联系客服处理退款");
        }
    }
}
