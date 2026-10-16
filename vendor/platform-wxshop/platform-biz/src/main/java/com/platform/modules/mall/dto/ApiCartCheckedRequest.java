package com.platform.modules.mall.dto;

import lombok.Data;

@Data
public class ApiCartCheckedRequest {
  private String productIds;
  private Integer isChecked;
}
