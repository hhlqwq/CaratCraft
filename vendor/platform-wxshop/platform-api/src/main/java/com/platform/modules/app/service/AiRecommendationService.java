package com.platform.modules.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.platform.modules.custom.dao.ComponentStockDao;
import com.platform.modules.custom.dto.CustomQuoteRequest;
import com.platform.modules.custom.entity.ComponentStockEntity;
import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import com.platform.modules.custom.entity.CustomProductEntity;
import com.platform.modules.custom.service.CustomProductService;
import com.platform.modules.custom.service.CustomQuoteService;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** AI建议仅作为编辑输入, 服务端仍独立校验商家选项、必选组、报价与库存. */
@Service
public class AiRecommendationService {
  private final CustomProductService products;
  private final CustomQuoteService quotes;
  private final ComponentStockDao stocks;
  private final StringRedisTemplate redis;
  private static final DefaultRedisScript<Long> HOURLY =
      new DefaultRedisScript<>(
          "local n=redis.call('INCR',KEYS[1]); "
              + "if n==1 then redis.call('EXPIRE',KEYS[1],3600) end; return n",
          Long.class);
  private static final String NOTICE =
      "AI建议仅供搭配参考, 身高体重不能准确推算颈围. 请以实测和商家确认的成品长度为准; 价格、库存及工艺以下单时校验为准.";

  /** 注入可信商品数据与跨实例限流存储. */
  public AiRecommendationService(
      CustomProductService products,
      CustomQuoteService quotes,
      ComponentStockDao stocks,
      StringRedisTemplate redis) {
    this.products = products;
    this.quotes = quotes;
    this.stocks = stocks;
    this.redis = redis;
  }

  /** 只公开功能状态和隐私说明, 不输出供应商凭证. */
  public Map<String, Object> status() {
    return Map.of(
        "enabled",
        true,
        "notice",
        NOTICE,
        "privacyNotice",
        "使用时将身高、体重、选填颈围和佩戴偏好提交至微信云开发AI服务, 用于本次推荐; 项目后端不持久保存AI请求的身体参数, 不发送姓名、手机号、地址或订单至模型."
            + " 您在设计中另行填写的实测颈围和成品尺寸会随草稿、订单及工单保存, 用于定制制作.");
  }

  /** 校验同意与参数后准备云模型候选与票据, 限流键不含身体数据. */
  public Map<String, Object> prepare(Long userId, AiRecommendationRequest request) {
    validate(request);
    if (userId == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
    }
    products.getConfig(request.goodsId());
    List<CustomComponentOptionEntity> options = availableOptions(request.goodsId());
    if (options.isEmpty() || options.size() > 100) {
      throw new IllegalArgumentException("当前可用材料为空或超过单次AI推荐范围, 请手动设计");
    }
    limit(userId);
    CustomProductEntity product = products.getByGoodsId(request.goodsId());
    if (product == null) {
      throw new IllegalArgumentException("该商品已关闭定制");
    }
    Map<String, Object> context = new LinkedHashMap<>();
    context.put("heightCm", request.heightCm());
    context.put("weightKg", request.weightKg());
    context.put("neckCm", request.neckCm());
    context.put("preference", request.preference());
    context.put("requiredTypes", product.getRequiredComponentTypes());
    context.put(
        "catalog",
        options.stream()
            .map(
                option ->
                    Map.of(
                        "id",
                        option.getId(),
                        "componentType",
                        option.getComponentType(),
                        "name",
                        option.getName() == null ? "" : option.getName()))
            .toList());
    String ticket = UUID.randomUUID().toString();
    redis
        .opsForValue()
        .set(ticketKey(userId, ticket), digest(ticket, request), Duration.ofMinutes(2));
    return Map.of("ticket", ticket, "context", context);
  }

  /** 消费当前用户的一次性票据, 将客户端云模型输出作为不可信数据重新检查. */
  public Map<String, Object> validateResult(Long userId, AiRecommendationValidation submission) {
    if (userId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
    if (submission == null
        || submission.ticket() == null
        || !submission.ticket().matches("[a-f0-9]{8}(-[a-f0-9]{4}){3}-[a-f0-9]{12}")) {
      throw new IllegalArgumentException("AI推荐票据无效");
    }
    validate(submission.request());
    String expected = redis.opsForValue().getAndDelete(ticketKey(userId, submission.ticket()));
    if (expected == null
        || !MessageDigest.isEqual(
            expected.getBytes(StandardCharsets.UTF_8),
            digest(submission.ticket(), submission.request()).getBytes(StandardCharsets.UTF_8))) {
      throw new IllegalArgumentException("AI推荐已过期、参数已变化或已使用, 请重新推荐");
    }
    products.getConfig(submission.request().goodsId());
    return verify(
        submission.request(),
        submission.result(),
        availableOptions(submission.request().goodsId()));
  }

  /** 票据绑定认证用户, 不允许跨用户提交云模型结果. */
  private String ticketKey(Long userId, String ticket) {
    return "caratcraft:ai:ticket:" + userId + ":" + ticket;
  }

  /** 使用随机票据加盐的摘要绑定输入, Redis不保存原始身体参数. */
  private String digest(String ticket, AiRecommendationRequest request) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256")
                  .digest((ticket + ":" + request).getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException error) {
      throw new IllegalStateException("运行环境缺少SHA-256", error);
    }
  }

  /** 只将当前商品已启用且有库存的材料提供给模型. */
  private List<CustomComponentOptionEntity> availableOptions(Integer goodsId) {
    Map<String, Integer> available = new java.util.HashMap<>();
    for (ComponentStockEntity stock :
        stocks.selectList(
            new LambdaQueryWrapper<ComponentStockEntity>()
                .eq(ComponentStockEntity::getGoodsId, goodsId))) {
      if (stock.getStock() != null && stock.getStock() > 0) {
        available.put(stock.getComponentType() + ":" + stock.getOptionCode(), stock.getStock());
      }
    }
    return products.getEnabledOptions(goodsId).stream()
        .filter(
            option -> {
              int quantity =
                  com.platform.modules.custom.service.ComponentRules.parse(
                          option.getMaterialParams())
                      .quantity();
              return quantity == 0
                  || available.getOrDefault(
                          option.getComponentType() + ":" + option.getOptionCode(), 0)
                      >= quantity;
            })
        .toList();
  }

  /** 每用户30秒一次、每小时最多20次, Redis异常时不绕过限流调用付费接口. */
  private void limit(Long userId) {
    if (!Boolean.TRUE.equals(
        redis
            .opsForValue()
            .setIfAbsent("caratcraft:ai:cooldown:" + userId, "1", Duration.ofSeconds(30)))) {
      throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "请间隔30秒后再推荐");
    }
    Long count = redis.execute(HOURLY, List.of("caratcraft:ai:hour:" + userId));
    if (count == null || count > 20) {
      throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "本小时AI推荐次数已达上限");
    }
  }

  /** 模型输出必须为完整数字、有效范围及候选选项, 不信任模型生成的价格和身份. */
  private Map<String, Object> verify(
      AiRecommendationRequest request, JsonNode result, List<CustomComponentOptionEntity> options) {
    try {
      BigDecimal length = number(result, "lengthCm");
      BigDecimal min = number(result, "rangeMinCm");
      BigDecimal max = number(result, "rangeMaxCm");
      if (min.compareTo(length) > 0
          || max.compareTo(length) < 0
          || (request.neckCm() != null && min.compareTo(request.neckCm()) < 0)) {
        throw new IllegalArgumentException();
      }
      JsonNode ids = result.path("optionIds");
      if (!ids.isArray() || ids.isEmpty() || ids.size() > 20) {
        throw new IllegalArgumentException();
      }
      List<Long> selected = new ArrayList<>();
      for (JsonNode id : ids) {
        if (!id.isIntegralNumber()
            || !id.canConvertToLong()
            || options.stream().noneMatch(option -> option.getId().equals(id.longValue()))
            || selected.contains(id.longValue())) {
          throw new IllegalArgumentException();
        }
        selected.add(id.longValue());
      }
      String reason = result.path("reason").isTextual() ? result.path("reason").asText() : "";
      if (reason.isBlank() || reason.length() > 600) {
        throw new IllegalArgumentException();
      }
      CustomQuoteRequest quoteRequest = new CustomQuoteRequest();
      quoteRequest.setGoodsId(request.goodsId());
      quoteRequest.setOptionIds(selected);
      Map<String, Object> quote = quotes.quote(quoteRequest);
      Map<String, Object> verified = new LinkedHashMap<>();
      verified.put("goodsId", request.goodsId());
      verified.put("lengthCm", length);
      verified.put("rangeMinCm", min);
      verified.put("rangeMaxCm", max);
      verified.put("optionIds", selected);
      verified.put(
          "optionNames",
          selected.stream()
              .map(
                  id ->
                      options.stream()
                          .filter(option -> option.getId().equals(id))
                          .findFirst()
                          .orElseThrow()
                          .getName())
              .toList());
      verified.put("reason", reason);
      verified.put("notice", NOTICE);
      verified.put("lengthNotice", "推荐长度是参考建议, 采用搭配仅选择材料; 成品长度需在商家长度选项中另行确认.");
      verified.put("quote", quote);
      return verified;
    } catch (RuntimeException error) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI建议未通过材料、尺寸或报价校验, 请重新推荐或手动设计");
    }
  }

  /** 将AI数值限制在通用项链建议区间, 不将其作为商家生产规格. */
  private BigDecimal number(JsonNode result, String name) {
    JsonNode value = result.path(name);
    if (!value.isNumber()) {
      throw new IllegalArgumentException();
    }
    BigDecimal number = value.decimalValue();
    if (number.compareTo(new BigDecimal("25")) < 0
        || number.compareTo(new BigDecimal("120")) > 0
        || number.scale() > 1) {
      throw new IllegalArgumentException();
    }
    return number;
  }

  /** 身体参数只用于本次调用, 不允许任意文本注入提示词或绕过同意. */
  private void validate(AiRecommendationRequest request) {
    if (request == null || request.goodsId() == null || !Boolean.TRUE.equals(request.consent())) {
      throw new IllegalArgumentException("请先阅读并同意AI推荐的数据使用说明");
    }
    if (!inRange(request.heightCm(), "80", "250")
        || !inRange(request.weightKg(), "20", "300")
        || (request.neckCm() != null && !inRange(request.neckCm(), "20", "80"))
        || !Set.of("贴颈", "锁骨位置", "稍长")
            .contains(request.preference() == null ? "" : request.preference())) {
      throw new IllegalArgumentException("请检查身高、体重、颈围和佩戴偏好");
    }
  }

  /** 拒绝缺失、超范围和过高精度输入. */
  private boolean inRange(BigDecimal value, String min, String max) {
    return value != null
        && value.scale() <= 1
        && value.compareTo(new BigDecimal(min)) >= 0
        && value.compareTo(new BigDecimal(max)) <= 0;
  }
}
