package com.platform.modules.mall.dto;

import lombok.Data;

@Data
public class ApiCartMinusRequest {
  private Integer goodsId;
  private Integer productId;
  private Integer number;
}
