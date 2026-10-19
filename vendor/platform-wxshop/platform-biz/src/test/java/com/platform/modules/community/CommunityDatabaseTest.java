package com.platform.modules.community;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.platform.modules.custom.entity.CustomDesignSnapshotEntity;
import com.platform.modules.custom.service.CustomDesignService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/** 使用独立MySQL库验证审核边界、并发唯一性与事务回滚, 不连接业务库. */
@EnabledIfEnvironmentVariable(named = "CARATCRAFT_DATABASE_TEST", matches = "true")
class CommunityDatabaseTest {
  private JdbcTemplate jdbc;
  private CommunityService service;
  private CustomDesignService designs;
  private long owner;
  private long other;

  /** 严格限制独立库URL, 为每例生成独立用户. */
  @BeforeEach
  void setup() {
    String url = System.getenv("CARATCRAFT_DATABASE_TEST_URL");
    if (url == null
        || !url.matches(
            "jdbc:mariadb://127\\.0\\.0\\.1:[0-9]+/caratcraft_init_audit_[0-9]{8}_[a-f0-9]{6}(\\?.*)?"))
      throw new IllegalStateException("只允许独立空库测试");
    var source =
        new DriverManagerDataSource(
            url,
            System.getenv().getOrDefault("CARATCRAFT_DATABASE_TEST_USER", "root"),
            System.getenv("CARATCRAFT_DATABASE_TEST_PASSWORD"));
    jdbc = new JdbcTemplate(source);
    designs = mock(CustomDesignService.class);
    ProxyFactory proxy = new ProxyFactory(new CommunityService(jdbc, designs));
    proxy.setProxyTargetClass(true);
    proxy.addAdvice(
        new TransactionInterceptor(
            new DataSourceTransactionManager(source), new AnnotationTransactionAttributeSource()));
    service = (CommunityService) proxy.getProxy();
    owner = new java.security.SecureRandom().nextInt(10_000_000, 15_000_000);
    other = owner + 1;
    jdbc.update("INSERT INTO WX_USER(id) VALUES(?),(?)", owner, other);
  }

  /** 仅删除当前随机用户的测试行, 不清空库. */
  @AfterEach
  void cleanup() {
    if (jdbc == null || owner == 0) return;
    jdbc.update(
        "DELETE l FROM CC_COMMUNITY_LIKE l JOIN CC_COMMUNITY_POST p ON p.id=l.post_id WHERE"
            + " p.user_id=?",
        owner);
    jdbc.update(
        "DELETE l FROM CC_COMMUNITY_LOG l JOIN CC_COMMUNITY_POST p ON p.id=l.post_id WHERE"
            + " p.user_id=?",
        owner);
    jdbc.update("DELETE FROM CC_COMMUNITY_POST WHERE user_id=?", owner);
    jdbc.update("DELETE FROM CC_COMMUNITY_MEDIA WHERE user_id IN (?,?)", owner, other);
    jdbc.update("DELETE FROM WX_USER WHERE id IN (?,?)", owner, other);
  }

  /** 创建最小合法建议, 所有请求由服务端从登录态取身份. */
  private CommunityPostRequest request() {
    var request = new CommunityPostRequest();
    request.setRequestKey(UUID.randomUUID().toString());
    request.setKind("SUGGESTION");
    request.setTitle("建议");
    request.setContent("希望增加可选材料");
    return request;
  }

  /** 从真实生成键获取帖子ID. */
  private long post(CommunityPostRequest request) {
    return ((Number) service.publish(owner, request).get("id")).longValue();
  }

  /** 模拟安全上传接口先占位、后登记地址的调用顺序. */
  private long image(long userId, String url) {
    long id = service.reserveImage(userId);
    service.registerImage(userId, id, url);
    return id;
  }

  /** 新帖待审核且不可公开, 审核后公开, 隐藏后旧地址立即失效. */
  @Test
  void moderationControlsAllPublicReadsAndWritesAuditLog() {
    long id = post(request());
    assertThrows(IllegalArgumentException.class, () -> service.detail(id));
    assertThrows(IllegalArgumentException.class, () -> service.like(other, id, true));
    service.moderate(id, 0L, "PUBLISHED", "文字图片核验通过", "测试运营");
    assertEquals("建议", service.detail(id).get("title"));
    service.moderate(id, 1L, "HIDDEN", "需要补充说明", "测试运营");
    assertThrows(IllegalArgumentException.class, () -> service.detail(id));
    assertEquals(2, service.logs(id).size());
    assertThrows(
        IllegalArgumentException.class,
        () -> service.moderate(id, 1L, "PUBLISHED", "过期请求", "测试运营"));
  }

  /** 并发重试同一发布键只能产生一个帖子. */
  @Test
  void concurrentPublishIsIdempotent() throws Exception {
    var request = request();
    try (var pool = Executors.newFixedThreadPool(2)) {
      var first = pool.submit(() -> post(request));
      var second = pool.submit(() -> post(request));
      assertEquals(first.get(), second.get());
    }
    assertEquals(
        1,
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM CC_COMMUNITY_POST WHERE user_id=?", Integer.class, owner));
    request.setContent("同键修改为其他内容");
    assertThrows(IllegalArgumentException.class, () -> service.publish(owner, request));
  }

  /** 点赞唯一键抵御并发重复, 取消重复也不会出现负数. */
  @Test
  void concurrentLikeAndRepeatedUnlikeKeepExactCount() throws Exception {
    long id = post(request());
    service.moderate(id, 0L, "PUBLISHED", "审核通过", "运营");
    try (var pool = Executors.newFixedThreadPool(4)) {
      var jobs =
          java.util.stream.IntStream.range(0, 8)
              .mapToObj(i -> pool.submit(() -> service.like(other, id, true)))
              .toList();
      for (var job : jobs) job.get();
    }
    assertEquals(1L, ((Number) service.detail(id).get("likes")).longValue());
    service.like(other, id, false);
    service.like(other, id, false);
    assertEquals(0L, ((Number) service.detail(id).get("likes")).longValue());
  }

  /** 他人不能删除或借用图片, 失败不产生残留帖子. */
  @Test
  void ownershipChecksProtectImagesAndDeletion() {
    long image = image(other, "/upload/test-owned.png");
    var request = request();
    request.setMediaIds(List.of(image));
    assertThrows(IllegalArgumentException.class, () -> service.publish(owner, request));
    long id = post(request());
    assertThrows(IllegalArgumentException.class, () -> service.remove(other, id));
    service.remove(owner, id);
    assertThrows(
        IllegalArgumentException.class,
        () -> service.moderate(id, 1L, "PUBLISHED", "不能恢复删除内容", "运营"));
  }

  /** 真实驱动的JSON列必须输出字符串, 本人图片在列表和详情均可解析. */
  @Test
  void ownedImagesAndDesignJsonAreReadableAcrossDriverTypes() {
    long image = image(owner, "/upload/owned-community.png");
    var request = request();
    request.setKind("FINISHED");
    request.setMediaIds(List.of(image));
    long id = post(request);
    var mine = (List<Map<String, Object>>) service.mine(owner, 1, 20).get("records");
    assertInstanceOf(String.class, mine.get(0).get("imagesJson"));
    service.moderate(id, 0L, "PUBLISHED", "图片核验通过", "运营");
    String json = (String) service.detail(id).get("imagesJson");
    assertTrue(json.contains("/upload/owned-community.png"));
    var publicRows = (List<Map<String, Object>>) service.page("FINISHED", 1, 20).get("records");
    assertTrue(publicRows.stream().anyMatch(row -> ((Number) row.get("id")).longValue() == id));
  }

  /** 设计公开副本严格按白名单复制, 商家后改材料与私人刻字都不进入帖子. */
  @Test
  void publicDesignIsFrozenAndExcludesPrivateFields() {
    var snapshot = new CustomDesignSnapshotEntity();
    snapshot.setStatus("DRAFT");
    snapshot.setConfigurationJson("{\"engraving\":\"私密刻字\",\"height\":165}");
    snapshot.setPriceBreakdownJson(
        "{\"arrangement\":[{\"position\":0,\"name\":\"澳白\",\"sizeMm\":9,\"optionId\":99,\"assetUrl\":\"/upload/private.png\"}],\"totalPrice\":999}");
    when(designs.getOwned(77L, owner)).thenReturn(snapshot);
    var request = request();
    request.setKind("DESIGN");
    request.setSnapshotId(77L);
    request.setDesignConsent(true);
    long id = post(request);
    snapshot.setPriceBreakdownJson("{}");
    service.moderate(id, 0L, "PUBLISHED", "核验通过", "运营");
    String json = String.valueOf(service.detail(id).get("designJson"));
    assertTrue(json.contains("澳白"));
    for (String privateKey :
        List.of(
            "optionId", "assetUrl", "engraving", "height", "totalPrice", "snapshotId", "userId"))
      assertFalse(json.contains(privateKey));
    assertFalse(service.detail(id).containsKey("user_id"));
  }

  /** 日志约束失败后帖子状态和版本一并回滚, 不出现无记录审核. */
  @Test
  void failedAuditInsertRollsBackModeration() {
    long id = post(request());
    assertThrows(RuntimeException.class, () -> service.moderate(id, 0L, "PUBLISHED", "通过", null));
    assertEquals(
        "PENDING",
        jdbc.queryForObject("SELECT status FROM CC_COMMUNITY_POST WHERE id=?", String.class, id));
    assertEquals(
        0,
        jdbc.queryForObject("SELECT version FROM CC_COMMUNITY_POST WHERE id=?", Integer.class, id));
    assertTrue(service.logs(id).isEmpty());
  }

  /** 未完成上传不能发布, 超额请求在存储写入前被阻断, 占位不能被他人冒用或重复覆盖. */
  @Test
  void uploadReservationsEnforceQuotaBeforeStorageAndCannotBeReused() {
    long id = service.reserveImage(owner);
    assertThrows(
        IllegalArgumentException.class,
        () -> service.registerImage(other, id, "/upload/not-owner.png"));
    var request = request();
    request.setMediaIds(List.of(id));
    assertThrows(IllegalArgumentException.class, () -> service.publish(owner, request));
    service.registerImage(owner, id, "/upload/first.png");
    assertThrows(
        IllegalArgumentException.class,
        () -> service.registerImage(owner, id, "/upload/replace.png"));
    for (int i = 1; i < 60; i++) service.reserveImage(owner);
    assertThrows(IllegalArgumentException.class, () -> service.reserveImage(owner));
    assertEquals(
        60,
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM CC_COMMUNITY_MEDIA WHERE user_id=?", Integer.class, owner));
  }
}
