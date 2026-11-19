package com.platform.modules.mall.dto;

import lombok.Data;

/** 收货地址输入契约, 不接收用户身份字段. */
@Data
public class AddressSaveRequest {
    private Integer id;
    private String userName;
    private String postalCode;
    private String provinceName;
    private String cityName;
    private String countyName;
    private String detailInfo;
    private String nationalCode;
    private String telNumber;
    private Boolean isDefault;
}
