package com.platform.modules.custom.dto;

import java.math.BigDecimal;
import lombok.Data;

/** 组件选项库存视图 / Component option stock view. */
@Data
public class ComponentStockView {
  /** 组件选项 ID / component option id */
  private Long optionId;

  /** 商城商品 ID / mall goods id */
  private Integer goodsId;

  /** 组件类型 / component type */
  private String componentType;

  /** 选项代码 / option code */
  private String optionCode;

  /** 选项名称 / option name */
  private String name;

  /** 加价金额 / price delta */
  private BigDecimal priceDelta;

  /** 当前库存, 无库存记录时为 0 / current stock, 0 when no record */
  private Integer stock;
}
