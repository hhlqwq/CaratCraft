package com.platform.modules.mall.dto;

import lombok.Data;

@Data
public class ApiCartAddRequest {
  private Integer goodsId;
  private Integer productId;
  private Integer number;
}
