package com.platform.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.SerializationException;

/** 缓存键空间和反序列化类型边界回归, 不连接真实Redis. */
class RedisBoundaryTest {
  /** 新键增加版本前缀且拒绝旧键, 不删除旧缓存. */
  @Test
  void keysUseVersionedNamespace() {
    VersionedRedisKeySerializer keys = new VersionedRedisKeySerializer();
    byte[] bytes = keys.serialize("user:7");
    assertEquals("caratcraft:v2:user:7", new String(bytes, StandardCharsets.UTF_8));
    assertEquals("user:7", keys.deserialize(bytes));
    assertThrows(
        SerializationException.class,
        () -> keys.deserialize("user:7".getBytes(StandardCharsets.UTF_8)));
  }

  /** 常见集合值可往返, 非允许范围的文件类不得实例化. */
  @Test
  void collectionsRoundTripButExternalTypesAreRejected() {
    RedisConfig config = new RedisConfig(mock(RedisConnectionFactory.class));
    var serializer = config.valueRedisSerializer();
    ArrayList<String> values = new ArrayList<>(List.of("中文", "safe"));
    assertEquals(values, serializer.deserialize(serializer.serialize(values)));
    assertThrows(
        SerializationException.class,
        () ->
            serializer.deserialize(
                "[\"java.io.File\",\"/tmp/untrusted\"]".getBytes(StandardCharsets.UTF_8)));
  }

  /** Boot原生属性可以创建应用缓存配置, 不再依赖spring.redis旧前缀. */
  @Test
  void bootNativePropertiesCreateConnectionFactory() {
    new org.springframework.boot.test.context.runner.ApplicationContextRunner()
        .withConfiguration(
            org.springframework.boot.autoconfigure.AutoConfigurations.of(
                org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class))
        .withUserConfiguration(RedisConfig.class)
        .withPropertyValues(
            "spring.data.redis.client-type=jedis",
            "spring.data.redis.host=127.0.0.1",
            "spring.data.redis.port=6379",
            "spring.data.redis.jedis.pool.min-idle=0")
        .run(
            context -> {
              org.junit.jupiter.api.Assertions.assertNull(context.getStartupFailure());
              org.junit.jupiter.api.Assertions.assertInstanceOf(
                  org.springframework.data.redis.connection.jedis.JedisConnectionFactory.class,
                  context.getBean(RedisConnectionFactory.class));
            });
  }
}
