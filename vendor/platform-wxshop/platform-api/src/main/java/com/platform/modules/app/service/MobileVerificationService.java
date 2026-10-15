package com.platform.modules.app.service;

import com.platform.common.exception.BusinessException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

/** 手机验证上下文, 限流、失败次数和一次性消费均由Redis原子执行. */
@Service
public class MobileVerificationService {
  private final StringRedisTemplate redis;
  private final SecureRandom random = new SecureRandom();
  private static final DefaultRedisScript<Long> VERIFY = new DefaultRedisScript<>(
      "local v=redis.call('GET',KEYS[1]); if not v then return 0 end; "
          + "local n=redis.call('INCR',KEYS[2]); redis.call('EXPIRE',KEYS[2],300); "
          + "if n>5 then redis.call('DEL',KEYS[1]); return 0 end; "
          + "if v~=ARGV[1] then return 0 end; redis.call('DEL',KEYS[1],KEYS[2]); return 1",
      Long.class);
  private static final DefaultRedisScript<Long> LIMIT = new DefaultRedisScript<>(
      "local n=redis.call('INCR',KEYS[1]); if n==1 then redis.call('EXPIRE',KEYS[1],3600) end; return n",
      Long.class);

  /** 注入使用字符串序列化的Redis连接, 避免验证码依赖通用对象反序列化. */
  public MobileVerificationService(StringRedisTemplate redis) {
    this.redis = redis;
  }

  /** 校验手机号并限制每用户每分钟一次、每手机号每小时五次. */
  public String begin(Long userId, String phone) {
    validatePhone(phone);
    if (!Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(
        "verify:mobile:rate:user:" + userId, "1", Duration.ofSeconds(60)))) {
      throw new BusinessException("发送过于频繁, 请稍后重试");
    }
    Long count = redis.execute(LIMIT, List.of("verify:mobile:rate:phone:" + digest(phone)));
    if (count == null || count > 5) {
      throw new BusinessException("该手机号发送次数过多, 请稍后重试");
    }
    return String.valueOf(100000 + random.nextInt(900000));
  }

  /** 短信平台成功后保存五分钟验证上下文, 不保存明文验证码. */
  public void issued(Long userId, String phone, String code) {
    String key = "verify:mobile:challenge:" + userId;
    redis.delete(key + ":attempts");
    redis.opsForValue().set(key, digest(userId + ":mobile:" + phone + ":" + code),
        Duration.ofMinutes(5));
  }

  /** 匹配用户、用途、手机号和验证码后原子删除, 失败最多允许五次. */
  public void consume(Long userId, String phone, String code) {
    validatePhone(phone);
    if (code == null || !code.matches("[0-9]{6}")) {
      throw new BusinessException("验证码错误或已失效");
    }
    String key = "verify:mobile:challenge:" + userId;
    Long result = redis.execute(VERIFY, List.of(key, key + ":attempts"),
        digest(userId + ":mobile:" + phone + ":" + code));
    if (!Long.valueOf(1).equals(result)) {
      throw new BusinessException("验证码错误或已失效");
    }
  }

  /** 仅接受中国大陆手机号. */
  private static void validatePhone(String phone) {
    if (phone == null || !phone.matches("1[3-9][0-9]{9}")) {
      throw new BusinessException("手机号格式不正确");
    }
  }

  /** 使用摘要构造验证上下文和限流键, 不在键名中暴露手机号. */
  private static String digest(String value) {
    try {
      return HexFormat.of().formatHex(
          MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException error) {
      throw new IllegalStateException("运行环境缺少SHA-256", error);
    }
  }
}
