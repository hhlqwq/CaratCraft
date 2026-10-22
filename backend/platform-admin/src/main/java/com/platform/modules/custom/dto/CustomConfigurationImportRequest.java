package com.platform.modules.custom.dto;

import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import com.platform.modules.custom.entity.CustomProductEntity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 定制商品与配件完整导入请求 / Complete custom product and component import request. */
@Data
public class CustomConfigurationImportRequest {
    private CustomProductEntity product;
    private List<CustomComponentOptionEntity> options = new ArrayList<>();
    private Boolean replaceOptions = true;
}
