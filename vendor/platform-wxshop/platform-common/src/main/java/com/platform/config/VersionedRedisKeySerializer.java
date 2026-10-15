package com.platform.config;

import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/** 为受限缓存增加版本前缀, 升级不清空也不反序列化历史数据. */
public final class VersionedRedisKeySerializer extends StringRedisSerializer {
  private static final String PREFIX = "caratcraft:v2:";

  /** 写入新的键空间, 保留Redis空值约定. */
  @Override
  public byte[] serialize(String value) {
    return value == null ? null : super.serialize(PREFIX + value);
  }

  /** 只解码本版本键名, 不混入其他应用或旧版本的键. */
  @Override
  public String deserialize(byte[] bytes) {
    String value = super.deserialize(bytes);
    if (value == null) {
      return null;
    }
    if (!value.startsWith(PREFIX)) {
      throw new SerializationException("拒绝读取其他版本的缓存键");
    }
    return value.substring(PREFIX.length());
  }
}
