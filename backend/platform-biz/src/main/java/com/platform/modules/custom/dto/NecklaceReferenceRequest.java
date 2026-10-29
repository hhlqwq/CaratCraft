package com.platform.modules.custom.dto;

import java.math.BigDecimal;

/** 首版参考输入, 身份来自认证上下文, 不接收身高体重或模型结果. */
public record NecklaceReferenceRequest(
        Integer goodsId, BigDecimal neckCircumferenceMm, String preference) {}
