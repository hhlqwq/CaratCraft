package com.platform.modules.app.service;

import com.fasterxml.jackson.databind.JsonNode;

/** 小程序云模型返回值, 必须连同原始请求和一次性票据再次校验. */
public record AiRecommendationValidation(
    AiRecommendationRequest request, String ticket, JsonNode result) {}
