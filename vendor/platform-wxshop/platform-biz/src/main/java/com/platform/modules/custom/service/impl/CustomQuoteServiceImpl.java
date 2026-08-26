package com.platform.modules.custom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.modules.custom.dao.CustomComponentOptionDao;
import com.platform.modules.custom.dto.CustomQuoteRequest;
import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import com.platform.modules.custom.service.CustomQuoteService;
import com.platform.modules.mall.entity.MallGoodsEntity;
import com.platform.modules.mall.service.MallGoodsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 服务端定制报价实现 / Server-side custom quote implementation.
 */
@Service
public class CustomQuoteServiceImpl implements CustomQuoteService {
    private final MallGoodsService mallGoodsService;
    private final CustomComponentOptionDao componentOptionDao;

    public CustomQuoteServiceImpl(MallGoodsService mallGoodsService,
                                  CustomComponentOptionDao componentOptionDao) {
        this.mallGoodsService = mallGoodsService;
        this.componentOptionDao = componentOptionDao;
    }

    /**
     * 根据选项计算最终价格 / Calculates final price from selected options.
     *
     * @param request 报价请求 / quote request
     * @return 报价结果 / quote result
     */
    @Override
    public Map<String, Object> quote(CustomQuoteRequest request) {
        if (request == null || request.getGoodsId() == null) {
            throw new IllegalArgumentException("请选择定制商品");
        }
        MallGoodsEntity goods = mallGoodsService.getById(request.getGoodsId());
        if (goods == null || !Integer.valueOf(1).equals(goods.getIsOnSale())) {
            throw new IllegalArgumentException("定制商品不可售");
        }
        BigDecimal basePrice = Objects.requireNonNullElse(goods.getRetailPrice(), BigDecimal.ZERO);
        List<CustomComponentOptionEntity> options = componentOptionDao.selectList(
                new LambdaQueryWrapper<CustomComponentOptionEntity>()
                        .eq(CustomComponentOptionEntity::getGoodsId, request.getGoodsId())
                        .eq(CustomComponentOptionEntity::getEnabled, 1));
        BigDecimal optionTotal = BigDecimal.ZERO;
        if (request.getOptionIds() != null) {
            for (Long optionId : request.getOptionIds()) {
                CustomComponentOptionEntity option = options.stream()
                        .filter(item -> item.getId().equals(optionId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("包含无效的定制选项"));
                optionTotal = optionTotal.add(Objects.requireNonNullElse(option.getPriceDelta(), BigDecimal.ZERO));
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("goodsId", goods.getId());
        result.put("basePrice", basePrice);
        result.put("optionTotal", optionTotal);
        result.put("totalPrice", basePrice.add(optionTotal));
        result.put("selectedOptionIds", request.getOptionIds());
        result.put("engraving", request.getEngraving());
        return result;
    }
}
