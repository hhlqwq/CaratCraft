package com.platform.modules.custom.service;

import com.platform.modules.custom.dto.CustomQuoteRequest;
import com.platform.modules.custom.entity.CustomDesignSnapshotEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 定制设计服务 / Custom design service.
 */
public interface CustomDesignService {
    /** 保存设计草稿 / Saves a design draft. */
    Map<String, Object> saveDraft(Long userId, CustomQuoteRequest request);

    /** 获取当前用户的设计 / Gets an owned design. */
    CustomDesignSnapshotEntity getOwned(Long snapshotId, Long userId);

    /** 查询当前用户的设计列表 / Lists owned designs. */
    List<CustomDesignSnapshotEntity> listOwned(Long userId);

    /** 重新报价并锁定设计 / Requotes and locks a design. */
    BigDecimal lockAndGetPrice(Long snapshotId, Long userId);

    /** 绑定设计与订单商品 / Binds a design to an order item. */
    void bindOrder(Long snapshotId, Long userId, Integer orderId, Integer orderGoodsId);

    /** 支付成功后生成生产工单 / Creates a work order after payment. */
    void markOrderPaid(Integer orderId);
}
