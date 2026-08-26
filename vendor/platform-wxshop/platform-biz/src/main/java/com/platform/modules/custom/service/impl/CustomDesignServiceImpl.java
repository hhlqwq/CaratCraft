package com.platform.modules.custom.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 定制设计服务实现 / Custom design service implementation.
 */
@Service
public class CustomDesignServiceImpl implements CustomDesignService {
    private final CustomDesignSnapshotDao snapshotDao;
    private final CustomOrderCustomizationDao orderCustomizationDao;
    private final ProductionWorkOrderDao workOrderDao;
    private final CustomQuoteService quoteService;

    public CustomDesignServiceImpl(CustomDesignSnapshotDao snapshotDao,
                                   CustomOrderCustomizationDao orderCustomizationDao,
                                   ProductionWorkOrderDao workOrderDao,
                                   CustomQuoteService quoteService) {
        this.snapshotDao = snapshotDao;
        this.orderCustomizationDao = orderCustomizationDao;
        this.workOrderDao = workOrderDao;
        this.quoteService = quoteService;
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

    /** 按最新商品和选项价格重新报价并锁定 / Requotes with current prices and locks the design. */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal lockAndGetPrice(Long snapshotId, Long userId) {
        CustomDesignSnapshotEntity snapshot = getOwned(snapshotId, userId);
        if ("ORDERED".equals(snapshot.getStatus())) {
            throw new IllegalArgumentException("该设计已提交订单");
        }
        CustomQuoteRequest request = JSON.parseObject(snapshot.getConfigurationJson(), CustomQuoteRequest.class);
        Map<String, Object> quote = quoteService.quote(request);
        BigDecimal totalPrice = new BigDecimal(String.valueOf(quote.get("totalPrice")));
        snapshot.setPriceBreakdownJson(JSON.toJSONString(quote));
        snapshot.setStatus("LOCKED");
        snapshotDao.updateById(snapshot);
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
        CustomOrderCustomizationEntity binding = new CustomOrderCustomizationEntity();
        binding.setOrderId(orderId);
        binding.setOrderGoodsId(orderGoodsId);
        binding.setSnapshotId(snapshotId);
        binding.setStatus("UNPAID");
        binding.setCreateTime(new Date());
        binding.setUpdateTime(new Date());
        orderCustomizationDao.insert(binding);
        snapshot.setStatus("ORDERED");
        snapshotDao.updateById(snapshot);
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
        binding.setStatus("PAID");
        binding.setUpdateTime(new Date());
        orderCustomizationDao.updateById(binding);
        Long count = workOrderDao.selectCount(new LambdaQueryWrapper<ProductionWorkOrderEntity>()
                .eq(ProductionWorkOrderEntity::getOrderId, orderId));
        if (count > 0) {
            return;
        }
        CustomDesignSnapshotEntity snapshot = snapshotDao.selectById(binding.getSnapshotId());
        ProductionWorkOrderEntity workOrder = new ProductionWorkOrderEntity();
        workOrder.setWorkOrderNo("CC-" + orderId);
        workOrder.setOrderId(orderId);
        workOrder.setOrderGoodsId(binding.getOrderGoodsId());
        workOrder.setSnapshotId(binding.getSnapshotId());
        workOrder.setStatus("PENDING");
        workOrder.setCraftJson(snapshot.getConfigurationJson());
        workOrder.setCreateTime(new Date());
        workOrder.setUpdateTime(new Date());
        workOrderDao.insert(workOrder);
    }
}
