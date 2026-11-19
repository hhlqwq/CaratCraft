package com.platform.modules.mall.dto;

import lombok.Data;

@Data
public class ApiCartUpdateRequest {
    private Integer goodsId;
    private Integer productId;
    private Integer number;
    private Integer id;
}
