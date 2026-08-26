package com.platform.modules.custom.service;

import com.platform.modules.custom.dto.CustomQuoteRequest;

import java.util.Map;

/**
 * 定制报价服务 / Custom quote service.
 */
public interface CustomQuoteService {
    /**
     * 根据服务端商品与组件数据计算报价 / Calculates a quote from server-side data.
     *
     * @param request 用户选择 / customer selection
     * @return 可展示并可冻结的报价明细 / quote breakdown
     */
    Map<String, Object> quote(CustomQuoteRequest request);
}
