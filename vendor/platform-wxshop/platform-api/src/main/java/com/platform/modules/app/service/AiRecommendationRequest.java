package com.platform.modules.app.service;

import java.math.BigDecimal;

/** AI推荐输入, 不接收用户身份、自由提示词或模型密钥. */
public record AiRecommendationRequest(
    Integer goodsId,
    BigDecimal heightCm,
    BigDecimal weightKg,
    BigDecimal neckCm,
    String preference,
    Boolean consent) {}
