package com.platform.modules.custom.dto;

import lombok.Data;

import java.util.List;

/**
 * DIY报价请求 / DIY quote request.
 */
@Data
public class CustomQuoteRequest {
    private Integer goodsId;
    private List<Long> optionIds;
    private String engraving;
}
