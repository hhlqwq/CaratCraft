/*
 *
 *      Copyright (c) 2018-2099, hehailong All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the openwtai.com developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: hehailong(845367567@qq.com)
 *
 */
package com.platform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import java.time.Duration;
import java.util.Arrays;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/** 缓存连接交给Boot的spring.data.redis类型化配置, 仅定制应用缓存边界. */
@Configuration
@EnableCaching
public class RedisConfig implements org.springframework.cache.annotation.CachingConfigurer {
  private final RedisConnectionFactory connectionFactory;

  /** 使用Boot自动配置的连接工厂, 避免重复维护字符串超时和连接池参数. */
  public RedisConfig(RedisConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  /** 应用缓存六小时过期, 新版本键空间不读取旧版宽泛类型缓存. */
  @Bean
  @Primary
  public CacheManager cacheManager() {
    RedisCacheConfiguration config =
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(6))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(keyRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(valueRedisSerializer()));
    return RedisCacheManager.builder(connectionFactory).cacheDefaults(config).build();
  }

  /** 方法参数仅以摘要参与缓存键, 不打印或暴露原参数内容. */
  @Bean
  @Primary
  public KeyGenerator keyGenerator() {
    return (target, method, params) ->
        target.getClass().getName()
            + ":"
            + method.getName()
            + ":"
            + DigestUtils.sha256Hex(Arrays.deepToString(params));
  }

  /** 只允许应用实体、购买DTO和集合值类型, 拒绝任意外部类反序列化. */
  @Bean
  public Jackson2JsonRedisSerializer<Object> valueRedisSerializer() {
    ObjectMapper mapper = new ObjectMapper();
    BasicPolymorphicTypeValidator types =
        BasicPolymorphicTypeValidator.builder()
            .allowIfSubType("com.platform.modules.mall.entity.")
            .allowIfSubType("com.platform.modules.wx.entity.")
            .allowIfSubType("com.platform.modules.sys.entity.")
            .allowIfSubType("com.platform.modules.custom.entity.")
            .allowIfSubType("com.platform.modules.mall.dto.BuyGoodsRequest")
            .allowIfSubType("java.util.")
            .allowIfSubType("java.math.")
            .build();
    mapper.activateDefaultTyping(types, ObjectMapper.DefaultTyping.NON_FINAL);
    return new Jackson2JsonRedisSerializer<>(mapper, Object.class);
  }

  /** 版本化键序列化器, 与旧缓存并存但不读取旧缓存. */
  @Bean
  public StringRedisSerializer keyRedisSerializer() {
    return new VersionedRedisKeySerializer();
  }

  /** 对象缓存统一使用受限类型序列化和版本化键. */
  @Bean
  public RedisTemplate<String, Object> redisTemplate() {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(keyRedisSerializer());
    template.setHashKeySerializer(keyRedisSerializer());
    template.setValueSerializer(valueRedisSerializer());
    template.setHashValueSerializer(valueRedisSerializer());
    template.afterPropertiesSet();
    return template;
  }
}
