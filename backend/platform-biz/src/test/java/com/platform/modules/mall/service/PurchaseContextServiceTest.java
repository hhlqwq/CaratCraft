package com.platform.modules.mall.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.modules.mall.dto.BuyGoodsRequest;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/** 验证多结算上下文隔离、归属和有效期. */
class PurchaseContextServiceTest {
    /** 模拟两个页面分别购买, 另一用户或过期上下文不能读取. */
    @Test
    @SuppressWarnings("unchecked")
    void contextsDoNotOverwriteAndExpire() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        Map<String, String> records = new HashMap<>();
        doAnswer(
                        call -> {
                            assertEquals(Duration.ofMinutes(30), call.getArgument(2));
                            records.put(call.getArgument(0), call.getArgument(1));
                            return null;
                        })
                .when(values)
                .set(anyString(), anyString(), any(Duration.class));
        when(values.get(anyString())).thenAnswer(call -> records.get(call.getArgument(0)));
        PurchaseContextService contexts = new PurchaseContextService(redis, new ObjectMapper());
        BuyGoodsRequest first = new BuyGoodsRequest();
        first.setGoodsId(10);
        String one = contexts.create(7L, first);
        first.setGoodsId(20);
        String two = contexts.create(7L, first);
        assertNotEquals(one, two);
        assertEquals(10, contexts.require(7L, one).getGoodsId());
        assertEquals(20, contexts.require(7L, two).getGoodsId());
        assertThrows(IllegalArgumentException.class, () -> contexts.require(8L, one));
        records.clear();
        assertThrows(IllegalArgumentException.class, () -> contexts.require(7L, one));
    }
}
