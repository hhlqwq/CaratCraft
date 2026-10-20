package com.platform.modules.mall.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionTemplate;

/** 仅在随机独立库测试发布不可变、版本冲突及本人确认, 数据随事务回滚. */
@EnabledIfEnvironmentVariable(named = "CARATCRAFT_DATABASE_TEST", matches = "true")
class LegalDocumentDatabaseTest {
  private JdbcTemplate jdbc;
  private DataSourceTransactionManager transactions;
  private LegalDocumentService service;

  /** 对隔离库创建真实事务代理, 拒绝任何业务数据库连接. */
  @BeforeEach
  void setUp() {
    String url = System.getenv("CARATCRAFT_DATABASE_TEST_URL");
    if (url == null
        || !url.matches(
            "jdbc:mariadb://127\\.0\\.0\\.1:[0-9]+/caratcraft_init_audit_[0-9]{8}_[a-f0-9]{6}")) {
      throw new IllegalStateException("仅允许连接随机独立初始化测试库");
    }
    var source =
        new DriverManagerDataSource(
            url, "root", System.getenv("CARATCRAFT_DATABASE_TEST_PASSWORD"));
    jdbc = new JdbcTemplate(source);
    transactions = new DataSourceTransactionManager(source);
    var proxy = new ProxyFactory(new LegalDocumentService(jdbc));
    proxy.setProxyTargetClass(true);
    proxy.addAdvice(
        new TransactionInterceptor(transactions, new AnnotationTransactionAttributeSource()));
    service = (LegalDocumentService) proxy.getProxy();
  }

  /** 空环境无示例正文, 未发布草稿不能被公开历史接口读取. */
  @Test
  void draftNeverLeaksIntoPublicDocuments() {
    rollback(
        () -> {
          assertTrue(service.published().isEmpty());
          save("TERMS", 0, "仅用于测试的草稿");
          assertTrue(service.published().isEmpty());
          assertThrows(IllegalArgumentException.class, () -> service.publishedVersion("TERMS", 1));
        });
  }

  /** 新草稿不改变已发布正文, 迟到保存及发布必须拒绝. */
  @Test
  void publishedTextIsImmutableAndStaleEditorsFail() {
    rollback(
        () -> {
          save("TERMS", 0, "第一版");
          service.publish("TERMS", 1, "publisher");
          save("TERMS", 1, "第二版草稿");
          assertEquals("第一版", service.publishedVersion("TERMS", 1).get("content"));
          assertEquals(1, ((Number) service.published().get(0).get("version")).intValue());
          assertThrows(IllegalArgumentException.class, () -> save("TERMS", 1, "迟到覆盖"));
          assertThrows(
              IllegalArgumentException.class, () -> service.publish("TERMS", 1, "publisher"));
          service.publish("TERMS", 2, "publisher");
          assertEquals("第一版", service.publishedVersion("TERMS", 1).get("content"));
        });
  }

  /** 确认幂等且只接受当前两份发布版本, 本人历史不包含他人记录. */
  @Test
  void acceptanceRequiresExactCurrentVersionsAndIsIdempotent() {
    rollback(
        () -> {
          for (String kind : new String[] {"TERMS", "PRIVACY"}) {
            save(kind, 0, "测试正文");
            service.publish(kind, 1, "publisher");
          }
          var acceptance = new LegalDocumentService.Acceptance(1, 1);
          service.accept(100L, acceptance);
          service.accept(100L, acceptance);
          assertEquals(2, service.history(100L).size());
          assertTrue(service.history(101L).isEmpty());
          save("PRIVACY", 1, "新政策");
          service.publish("PRIVACY", 2, "publisher");
          assertThrows(IllegalArgumentException.class, () -> service.accept(101L, acceptance));
          assertTrue(service.history(101L).isEmpty());
          assertThrows(IllegalArgumentException.class, () -> service.accept(101L, null));
          assertEquals("测试正文", service.publishedVersion("PRIVACY", 1).get("content"));
        });
  }

  /** 失败写入不推进修订号, 非法类别和过长正文在持久化前拒绝. */
  @Test
  void invalidDraftDoesNotAdvanceRevision() {
    rollback(
        () -> {
          assertThrows(IllegalArgumentException.class, () -> save("UNKNOWN", 0, "正文"));
          assertThrows(IllegalArgumentException.class, () -> save("TERMS", 0, ""));
          assertThrows(IllegalArgumentException.class, () -> save("TERMS", 0, "a".repeat(50001)));
          var head = (Map<?, ?>) service.editor("TERMS").get("head");
          assertEquals(0, ((Number) head.get("revision")).intValue());
          assertEquals(
              0, jdbc.queryForObject("SELECT COUNT(*) FROM CC_LEGAL_DOCUMENT", Integer.class));
        });
  }

  /** 测试正文显式标记用途, 不在业务库生成实际经营者协议. */
  private void save(String kind, int revision, String content) {
    service.save(new LegalDocumentService.Draft(kind, revision, "隔离测试协议", content), "editor");
  }

  /** 每项测试的全部写入都在同一外层事务中回滚. */
  private void rollback(Runnable assertions) {
    new TransactionTemplate(transactions)
        .executeWithoutResult(
            status -> {
              try {
                assertions.run();
              } finally {
                status.setRollbackOnly();
              }
            });
  }
}
