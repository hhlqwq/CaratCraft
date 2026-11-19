package com.platform.modules.mall.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** 订单应用服务接收固定意图, 身份仍由认证用户提供. */
@Data
public class OrderSubmitRequest {
    private Integer addressId;
    private Integer couponId;
    private String type;
    private String postscript;
    private Long designSnapshotId;
    private String checkoutId;
    private String requestKey;
    private BigDecimal expectedActualPrice;
    private List<CheckoutItem> expectedItems;

    /** 一条冻结的商品、SKU与购买数量, 不接收客户端成交价格. */
    public record CheckoutItem(Integer goodsId, Integer productId, Integer number) {}
}
