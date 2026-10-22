package com.platform.modules.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.platform.common.exception.BusinessException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/** 显式启用的真实Redis回归, 使用负数随机用户ID隔离, 不调用短信供应商. */
@EnabledIfEnvironmentVariable(named = "CARATCRAFT_REDIS_TEST", matches = "true")
class MobileVerificationRedisTest {
    /** 验证手机号上下文、重放保护及五次失败后销毁, 全程不发送真实短信. */
    @Test
    void contextReplayAndAttemptLimitAreAtomic() {
        RedisStandaloneConfiguration config =
                new RedisStandaloneConfiguration(
                        "127.0.0.1",
                        Integer.parseInt(
                                System.getenv()
                                        .getOrDefault("CARATCRAFT_REDIS_TEST_PORT", "6379")));
        String password = System.getenv("CARATCRAFT_REDIS_TEST_PASSWORD");
        if (password != null && !password.isBlank()) {
            config.setPassword(password);
        }
        JedisConnectionFactory connection = new JedisConnectionFactory(config);
        connection.afterPropertiesSet();
        StringRedisTemplate redis = new StringRedisTemplate(connection);
        long userId = -Math.abs(new java.security.SecureRandom().nextLong(1, Long.MAX_VALUE));
        String key = "verify:mobile:challenge:" + userId;
        java.util.List<String> keys = new java.util.ArrayList<>(List.of(key, key + ":attempts"));
        MobileVerificationService service = new MobileVerificationService(redis);
        try {
            var contexts =
                    new com.platform.modules.mall.service.PurchaseContextService(
                            redis, new com.fasterxml.jackson.databind.ObjectMapper());
            var goods = new com.platform.modules.mall.dto.BuyGoodsRequest();
            goods.setGoodsId(10);
            goods.setProductId(11);
            goods.setNumber(1);
            String first = contexts.create(userId, goods);
            keys.add("caratcraft:purchase:" + userId + ":" + first);
            goods.setGoodsId(20);
            String second = contexts.create(userId, goods);
            keys.add("caratcraft:purchase:" + userId + ":" + second);
            assertEquals(10, contexts.require(userId, first).getGoodsId());
            assertEquals(20, contexts.require(userId, second).getGoodsId());
            assertTrue(redis.getExpire(keys.get(2)) > 0 && redis.getExpire(keys.get(2)) <= 1800);
            assertThrows(IllegalArgumentException.class, () -> contexts.require(userId - 1, first));
            redis.expire(keys.get(2), java.time.Duration.ZERO);
            assertThrows(IllegalArgumentException.class, () -> contexts.require(userId, first));
            service.issued(userId, "13800000000", "123456");
            assertTrue(redis.getExpire(key) > 0 && redis.getExpire(key) <= 300);
            assertFalse(redis.opsForValue().get(key).contains("123456"));
            assertThrows(
                    BusinessException.class,
                    () -> service.consume(userId, "13900000000", "123456"));
            service.consume(userId, "13800000000", "123456");
            assertThrows(
                    BusinessException.class,
                    () -> service.consume(userId, "13800000000", "123456"));
            service.issued(userId, "13800000000", "234567");
            for (int attempt = 0; attempt < 5; attempt++) {
                assertThrows(
                        BusinessException.class,
                        () -> service.consume(userId, "13800000000", "999999"));
            }
            assertThrows(
                    BusinessException.class,
                    () -> service.consume(userId, "13800000000", "234567"));
        } finally {
            redis.delete(keys);
            connection.destroy();
        }
    }
}
