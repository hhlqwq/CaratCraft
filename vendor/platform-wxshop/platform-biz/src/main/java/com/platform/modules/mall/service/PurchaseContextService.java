package com.platform.modules.mall.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.modules.mall.dto.BuyGoodsRequest;
import java.time.Duration;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/** 每次立即购买使用独立的本人上下文, 避免多页面互相覆盖. */
@Service
public class PurchaseContextService {
  private final StringRedisTemplate redis;
  private final ObjectMapper mapper;

  /** 注入字符串缓存与固定类型JSON编解码器. */
  public PurchaseContextService(StringRedisTemplate redis, ObjectMapper mapper) {
    this.redis = redis;
    this.mapper = mapper;
  }

  /** 创建30分钟有效上下文, 不把商品信息或用户身份放进客户端令牌. */
  public String create(Long userId, BuyGoodsRequest goods) {
    String id = UUID.randomUUID().toString();
    try {
      redis
          .opsForValue()
          .set(key(userId, id), mapper.writeValueAsString(goods), Duration.ofMinutes(30));
      return id;
    } catch (JsonProcessingException error) {
      throw new IllegalStateException("购买信息保存失败", error);
    }
  }

  /** 读取本人指定上下文, 禁止回退到旧的每用户共享键. */
  public BuyGoodsRequest require(Long userId, String id) {
    String value = redis.opsForValue().get(key(userId, id));
    if (value == null) {
      throw new IllegalArgumentException("购买信息已过期, 请重新选择商品");
    }
    try {
      return mapper.readValue(value, BuyGoodsRequest.class);
    } catch (JsonProcessingException error) {
      throw new IllegalStateException("购买信息不可用, 请重新选择商品", error);
    }
  }

  /** 强制上下文格式和认证用户命名空间. */
  private String key(Long userId, String id) {
    if (userId == null || id == null || !id.matches("[a-f0-9]{8}(-[a-f0-9]{4}){3}-[a-f0-9]{12}")) {
      throw new IllegalArgumentException("购买上下文无效, 请重新选择商品");
    }
    return "caratcraft:purchase:" + userId + ":" + id;
  }
}
