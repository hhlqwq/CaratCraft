package com.platform.modules.mall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.platform.modules.custom.dao.CustomOrderCustomizationDao;
import com.platform.modules.custom.dao.ProductionWorkOrderDao;
import com.platform.modules.mall.dao.MallOrderDao;
import com.platform.modules.mall.dao.OrderPriceLogDao;
import com.platform.modules.mall.service.impl.MallOrderServiceImpl;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/** 显式启用的真实数据库回归, 仅允许连接独立测试库, 不加载业务应用和外部支付配置. */
@EnabledIfEnvironmentVariable(named = "CARATCRAFT_DATABASE_TEST", matches = "true")
class OrderTransactionDatabaseTest {
  private DriverManagerDataSource source;
  private JdbcTemplate jdbc;
  private OrderLifecycleService service;
  private OrderPriceLogDao logs;
  private int orderId;
  private SqlSessionTemplate sessions;

  /** 建立真实MyBatis和Spring事务代理, 测试行使用随机ID和独立订单号. */
  @BeforeEach
  void setUp() throws Exception {
    String url = System.getenv("CARATCRAFT_DATABASE_TEST_URL");
    if (url == null
        || !url.matches(
            "jdbc:mariadb://127\\.0\\.0\\.1:[0-9]+/caratcraft_(refactor_test_[0-9]{8}|init_audit_[0-9]{8}_[a-f0-9]{6})(\\?.*)?")) {
      throw new IllegalStateException("只允许连接本机caratcraft_refactor_test_日期测试库");
    }
    source =
        new DriverManagerDataSource(
            url,
            System.getenv().getOrDefault("CARATCRAFT_DATABASE_TEST_USER", "root"),
            System.getenv("CARATCRAFT_DATABASE_TEST_PASSWORD"));
    jdbc = new JdbcTemplate(source);
    MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
    MybatisConfiguration configuration = new MybatisConfiguration();
    configuration.setMapUnderscoreToCamelCase(true);
    factory.setConfiguration(configuration);
    factory.setDataSource(source);
    factory.afterPropertiesSet();
    for (Class<?> mapper :
        new Class<?>[] {
          MallOrderDao.class,
          CustomOrderCustomizationDao.class,
          ProductionWorkOrderDao.class,
          com.platform.modules.custom.dao.ComponentStockDao.class,
          com.platform.modules.custom.dao.CustomComponentOptionDao.class
        }) {
      factory.getObject().getConfiguration().addMapper(mapper);
    }
    sessions = new SqlSessionTemplate(factory.getObject());
    MallOrderServiceImpl orders = new MallOrderServiceImpl();
    ReflectionTestUtils.setField(orders, "baseMapper", sessions.getMapper(MallOrderDao.class));
    logs = mock(OrderPriceLogDao.class);
    OrderLifecycleService target =
        new OrderLifecycleService(
            orders,
            sessions.getMapper(CustomOrderCustomizationDao.class),
            sessions.getMapper(ProductionWorkOrderDao.class),
            logs);
    ProxyFactory proxy = new ProxyFactory(target);
    proxy.setProxyTargetClass(true);
    proxy.addAdvice(
        new TransactionInterceptor(
            new DataSourceTransactionManager(source), new AnnotationTransactionAttributeSource()));
    service = (OrderLifecycleService) proxy.getProxy();
    orderId = new java.security.SecureRandom().nextInt(10_000_000, 15_000_000);
    jdbc.update(
        "INSERT INTO mall_order"
            + " (id,order_sn,user_id,order_status,pay_status,shipping_status,actual_price,order_price)"
            + " VALUES (?,?,7,0,0,0,199.00,199.00)",
        orderId,
        "IT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 24));
  }

  /** 仅删除本用例随机测试行, 不清空库或其他测试数据. */
  @AfterEach
  void cleanUp() {
    if (jdbc != null && orderId != 0) {
      jdbc.update("DELETE FROM CC_ORDER_PRICE_LOG WHERE order_id=?", orderId);
      jdbc.update("DELETE FROM CC_STOCK_LOG WHERE goods_id=?", orderId);
      jdbc.update("DELETE FROM CC_COMPONENT_STOCK WHERE goods_id=?", orderId);
      jdbc.update("DELETE FROM CC_COMPONENT_OPTION WHERE goods_id=?", orderId);
      jdbc.update("DELETE FROM mall_order WHERE id=?", orderId);
    }
  }

  /** 库存写入后流水失败时, Spring事务必须同时回滚余额. */
  @Test
  void failedStockLedgerActuallyRollsBackBalance() {
    long optionId = insertStockFixture();
    var real = sessions.getMapper(com.platform.modules.custom.dao.ComponentStockDao.class);
    var failing =
        mock(
            com.platform.modules.custom.dao.ComponentStockDao.class,
            org.mockito.AdditionalAnswers.delegatesTo(real));
    doReturn(0).when(failing).logMovement(any(), any(), any(), anyInt(), any(), any());
    var stocks = stockService(failing);
    assertThrows(
        IllegalStateException.class, () -> stocks.updateStock(optionId, 20, 10, "隔离测试", "流水失败回滚"));
    assertEquals(
        10,
        jdbc.queryForObject(
            "SELECT stock FROM CC_COMPONENT_STOCK WHERE goods_id=?", Integer.class, orderId));
  }

  /** 两个相同旧余额的并发盘点只能有一个提交, 流水与最终余额保持一致. */
  @Test
  void concurrentStockAdjustmentsCannotLoseUpdates() throws Exception {
    long optionId = insertStockFixture();
    var stocks =
        stockService(sessions.getMapper(com.platform.modules.custom.dao.ComponentStockDao.class));
    var executor = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);
    java.util.concurrent.atomic.AtomicInteger changed =
        new java.util.concurrent.atomic.AtomicInteger();
    try {
      var futures =
          java.util.stream.IntStream.of(20, 30)
              .mapToObj(
                  balance ->
                      executor.submit(
                          () -> {
                            try {
                              start.await(5, TimeUnit.SECONDS);
                              stocks.updateStock(optionId, balance, 10, "隔离测试", "并发盘点");
                              changed.incrementAndGet();
                            } catch (IllegalArgumentException expected) {
                              // 旧余额已失效, 另一请求提交成功后本次必须明确拒绝.
                            } catch (InterruptedException interrupted) {
                              Thread.currentThread().interrupt();
                              throw new IllegalStateException(interrupted);
                            }
                          }))
              .toList();
      start.countDown();
      for (var future : futures) future.get(10, TimeUnit.SECONDS);
      assertEquals(1, changed.get());
      assertEquals(
          1,
          jdbc.queryForObject(
              "SELECT COUNT(*) FROM CC_STOCK_LOG WHERE goods_id=?", Integer.class, orderId));
      assertEquals(
          jdbc.queryForObject(
              "SELECT stock FROM CC_COMPONENT_STOCK WHERE goods_id=?", Integer.class, orderId),
          jdbc.queryForObject(
              "SELECT balance FROM CC_STOCK_LOG WHERE goods_id=?", Integer.class, orderId));
    } finally {
      executor.shutdown();
    }
  }

  /** 创建仅属于本测试随机商品ID的材料与余额. */
  private long insertStockFixture() {
    jdbc.update(
        "INSERT INTO"
            + " CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,enabled)"
            + " VALUES(?,'pearl','test-stock','隔离测试材料',0,1)",
        orderId);
    jdbc.update(
        "INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)"
            + " VALUES(?,'pearl','test-stock',10)",
        orderId);
    return jdbc.queryForObject(
        "SELECT id FROM CC_COMPONENT_OPTION WHERE goods_id=?", Long.class, orderId);
  }

  /** 为真实材料服务创建事务代理, 与应用运行时采用相同事务拦截器. */
  private com.platform.modules.custom.service.CustomStockService stockService(
      com.platform.modules.custom.dao.ComponentStockDao stocks) {
    var target =
        new com.platform.modules.custom.service.impl.CustomStockServiceImpl(
            sessions.getMapper(com.platform.modules.custom.dao.CustomComponentOptionDao.class),
            stocks);
    ProxyFactory proxy = new ProxyFactory(target);
    proxy.setProxyTargetClass(true);
    proxy.addAdvice(
        new TransactionInterceptor(
            new DataSourceTransactionManager(source), new AnnotationTransactionAttributeSource()));
    return (com.platform.modules.custom.service.CustomStockService) proxy.getProxy();
  }

  /** 日志落库失败后订单实付金额必须真实回滚. */
  @Test
  void failedAuditActuallyRollsBackAmount() {
    assertThrows(
        IllegalStateException.class,
        () -> service.adjustPrice(orderId, new BigDecimal("188.00"), "test", "隔离事务回归"));
    assertEquals(
        new BigDecimal("199.00"),
        jdbc.queryForObject(
            "SELECT actual_price FROM mall_order WHERE id=?", BigDecimal.class, orderId));
  }

  /** 支付状态与改价并发时, 改价必须读取锁释放后的最新状态并拒绝覆盖. */
  @Test
  void concurrentPrepaymentPreventsPriceOverwrite() throws Exception {
    var executor = Executors.newSingleThreadExecutor();
    CountDownLatch started = new CountDownLatch(1);
    try (Connection connection = source.getConnection()) {
      connection.setAutoCommit(false);
      try (var select =
          connection.prepareStatement("SELECT id FROM mall_order WHERE id=? FOR UPDATE")) {
        select.setInt(1, orderId);
        try (var row = select.executeQuery()) {
          assertTrue(row.next());
        }
      }
      var result =
          executor.submit(
              () -> {
                started.countDown();
                assertThrows(
                    IllegalArgumentException.class,
                    () -> service.adjustPrice(orderId, new BigDecimal("188.00"), "test", "并发回归"));
              });
      assertTrue(started.await(5, TimeUnit.SECONDS));
      try (var update =
          connection.prepareStatement("UPDATE mall_order SET pay_status=1 WHERE id=?")) {
        update.setInt(1, orderId);
        assertEquals(1, update.executeUpdate());
      }
      connection.commit();
      result.get(10, TimeUnit.SECONDS);
      assertEquals(
          new BigDecimal("199.00"),
          jdbc.queryForObject(
              "SELECT actual_price FROM mall_order WHERE id=?", BigDecimal.class, orderId));
    } finally {
      executor.shutdown();
    }
  }

  /** 两次并发确认最终只形成同一个完成状态, 重复请求保持幂等. */
  @Test
  void concurrentReceiptIsIdempotent() throws Exception {
    jdbc.update(
        "UPDATE mall_order SET order_status=300,pay_status=2,shipping_status=1 WHERE id=?",
        orderId);
    var executor = Executors.newFixedThreadPool(2);
    try {
      var first = executor.submit(() -> service.confirmReceipt(orderId, 7L));
      var second = executor.submit(() -> service.confirmReceipt(orderId, 7L));
      first.get(10, TimeUnit.SECONDS);
      second.get(10, TimeUnit.SECONDS);
      assertEquals(
          301,
          jdbc.queryForObject(
              "SELECT order_status FROM mall_order WHERE id=?", Integer.class, orderId));
      assertEquals(
          2,
          jdbc.queryForObject(
              "SELECT shipping_status FROM mall_order WHERE id=?", Integer.class, orderId));
    } finally {
      executor.shutdown();
    }
  }
}
