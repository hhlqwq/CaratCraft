package com.platform.modules.mall.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.platform.modules.wx.entity.WxUserEntity;
import com.platform.modules.wx.service.WxUserService;
import com.platform.support.TransactionTestSupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** 仅在随机隔离库验证账户事务和真实唯一约束, 微信身份使用合成值. */
@EnabledIfEnvironmentVariable(named = "CARATCRAFT_DATABASE_TEST", matches = "true")
class AccountLoginDatabaseTest {
    private JdbcTemplate jdbc;
    private DataSourceTransactionManager transactions;
    private WxUserService users;

    /** 数据源严格限制随机测试库, ORM边界用真实SQL适配以观察事务结果. */
    @BeforeEach
    void setUp() {
        String url = System.getenv("CARATCRAFT_DATABASE_TEST_URL");
        if (url == null
                || !url.matches(
                        "jdbc:mariadb://127\\.0\\.0\\.1:[0-9]+/caratcraft_init_audit_[0-9]{8}_[a-f0-9]{6}(\\?allowPublicKeyRetrieval=true)?")) {
            throw new IllegalStateException("仅允许随机隔离库");
        }
        var source =
                new DriverManagerDataSource(
                        url, "root", System.getenv("CARATCRAFT_DATABASE_TEST_PASSWORD"));
        jdbc = new JdbcTemplate(source);
        transactions = new DataSourceTransactionManager(source);
        users = mock(WxUserService.class);
        when(users.save(any(WxUserEntity.class)))
                .thenAnswer(
                        call -> {
                            WxUserEntity user = call.getArgument(0);
                            GeneratedKeyHolder key = new GeneratedKeyHolder();
                            jdbc.update(
                                    connection -> {
                                        var statement =
                                                connection.prepareStatement(
                                                        "INSERT INTO"
                                                            + " WX_USER(MA_OPENID,OPENID,UNION_ID,NICKNAME,HEAD_IMG_URL)"
                                                            + " VALUES(?,?,?,?,?)",
                                                        Statement.RETURN_GENERATED_KEYS);
                                        statement.setString(1, user.getMaOpenid());
                                        statement.setString(2, user.getOpenid());
                                        statement.setString(3, user.getUnionId());
                                        statement.setString(4, user.getNickname());
                                        statement.setString(5, user.getHeadImgUrl());
                                        return statement;
                                    },
                                    key);
                            user.setId(key.getKey().longValue());
                            return true;
                        });
        when(users.getById(any(Long.class)))
                .thenAnswer(
                        call ->
                                jdbc.queryForObject(
                                        "SELECT ID,NICKNAME,HEAD_IMG_URL FROM WX_USER WHERE ID=?",
                                        (row, index) -> {
                                            WxUserEntity user = new WxUserEntity();
                                            user.setId(row.getLong("ID"));
                                            user.setNickname(row.getString("NICKNAME"));
                                            user.setHeadImgUrl(row.getString("HEAD_IMG_URL"));
                                            return user;
                                        },
                                        call.getArgument(0, Long.class)));
    }

    /** 协议版本无效时即使账户已经插入, 事务仍须回滚该账户及锁记录. */
    @Test
    void failedConfirmationRollsBackNewAccount() {
        String openid = "rollback-" + UUID.randomUUID();
        AccountLoginService accounts = service(new LegalDocumentService(jdbc));
        assertThrows(
                IllegalArgumentException.class, () -> accounts.login(true, openid, null, null));
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM WX_USER WHERE MA_OPENID=?", Integer.class, openid));
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM CC_LOGIN_IDENTITY_LOCK WHERE"
                                + " identity_hash=SHA2(?,256)",
                        Integer.class,
                        "MA_OPENID:" + openid));
    }

    /** 登录不覆盖已有昵称和头像, 当前身份只更新会话需要的字段. */
    @Test
    void existingProfileSurvivesLogin() {
        rollback(
                () -> {
                    String openid = "profile-" + UUID.randomUUID();
                    jdbc.update(
                            "INSERT INTO WX_USER(MA_OPENID,NICKNAME,HEAD_IMG_URL) VALUES(?,?,?)",
                            openid,
                            "用户自填昵称",
                            "/upload/owned.png");
                    WxUserEntity user =
                            service(mock(LegalDocumentService.class))
                                    .login(true, openid, null, null);
                    assertEquals("用户自填昵称", user.getNickname());
                    assertEquals("/upload/owned.png", user.getHeadImgUrl());
                });
    }

    /** 无UnionID的同一身份并发登录, 两次请求必须返回同一账户. */
    @Test
    void concurrentRegistrationCreatesOneAccount() throws Exception {
        String openid = "parallel-" + UUID.randomUUID();
        AccountLoginService accounts = service(mock(LegalDocumentService.class));
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first =
                    executor.submit(
                            () -> {
                                start.await();
                                return accounts.login(true, openid, null, null).getId();
                            });
            var second =
                    executor.submit(
                            () -> {
                                start.await();
                                return accounts.login(true, openid, null, null).getId();
                            });
            start.countDown();
            Long userId = first.get(20, TimeUnit.SECONDS);
            assertEquals(userId, second.get(20, TimeUnit.SECONDS));
            assertEquals(
                    1,
                    jdbc.queryForObject(
                            "SELECT COUNT(*) FROM mall_user_coupon WHERE user_id=? AND source_key=?",
                            Integer.class, userId, WelcomeCoupon.SOURCE));
            assertEquals(
                    1,
                    jdbc.queryForObject(
                            "SELECT COUNT(*) FROM WX_USER WHERE MA_OPENID=?",
                            Integer.class,
                            openid));
        } finally {
            // 仅删除本用例随机身份, 不影响其他隔离用例的数据.
            jdbc.update("DELETE c FROM mall_user_coupon c JOIN WX_USER u ON c.user_id=u.id WHERE u.MA_OPENID=?", openid);
            jdbc.update("DELETE FROM WX_USER WHERE MA_OPENID=?", openid);
            jdbc.update(
                    "DELETE FROM CC_LOGIN_IDENTITY_LOCK WHERE identity_hash=SHA2(?,256)",
                    "MA_OPENID:" + openid);
        }
    }

    /** 共享可信UnionID的不同入口保留同一账户, 后续不返回UnionID时仍识别别名. */
    @Test
    void verifiedUnionKeepsDifferentEntryAliasesWithoutOverwritingLegacyIdentity() {
        rollback(
                () -> {
                    String suffix = UUID.randomUUID().toString();
                    String original = "old-" + suffix;
                    String another = "new-" + suffix;
                    String union = "union-" + suffix;
                    jdbc.update(
                            "INSERT INTO WX_USER(OPENID,UNION_ID,NICKNAME) VALUES(?,?,?)",
                            original,
                            union,
                            "原有用户");
                    AccountLoginService accounts = service(mock(LegalDocumentService.class));
                    Long id = accounts.login(false, another, union, null).getId();
                    assertEquals(id, accounts.login(false, another, null, null).getId());
                    assertEquals(id, accounts.login(false, original, null, null).getId());
                    assertEquals(
                            original,
                            jdbc.queryForObject(
                                    "SELECT OPENID FROM WX_USER WHERE ID=?", String.class, id));
                });
    }

    /** 不透明身份按大小写精确匹配, 两个不同标识不能被合并为同一用户. */
    @Test
    void identityComparisonIsCaseSensitive() {
        rollback(
                () -> {
                    String suffix = UUID.randomUUID().toString();
                    AccountLoginService accounts = service(mock(LegalDocumentService.class));
                    assertNotEquals(
                            accounts.login(true, "A-" + suffix, null, null).getId(),
                            accounts.login(true, "a-" + suffix, null, null).getId());
                });
    }

    /** 关联冲突拒绝登录, 不自动把旧账户的UnionID重定向到另一个账户. */
    @Test
    void conflictingUnionDoesNotRelinkAccounts() {
        rollback(
                () -> {
                    String suffix = UUID.randomUUID().toString();
                    jdbc.update(
                            "INSERT INTO WX_USER(MA_OPENID,UNION_ID) VALUES(?,?)",
                            "first-" + suffix,
                            "first-" + suffix);
                    jdbc.update(
                            "INSERT INTO WX_USER(MA_OPENID,UNION_ID) VALUES(?,?)",
                            "second-" + suffix,
                            "second-" + suffix);
                    assertThrows(
                            IllegalArgumentException.class,
                            () ->
                                    service(mock(LegalDocumentService.class))
                                            .login(
                                                    true,
                                                    "first-" + suffix,
                                                    "second-" + suffix,
                                                    null));
                });
    }

    /** 为被测服务安装真实事务代理, 覆盖跨服务调用失败时的数据库回滚. */
    private AccountLoginService service(LegalDocumentService documents) {
        var proxy = new ProxyFactory(new AccountLoginService(jdbc, users, documents));
        proxy.setProxyTargetClass(true);
        proxy.addAdvice(TransactionTestSupport.interceptor(transactions));
        return (AccountLoginService) proxy.getProxy();
    }

    /** 普通用例在外层事务结束时回滚全部合成记录. */
    private void rollback(Runnable work) {
        new TransactionTemplate(transactions)
                .executeWithoutResult(
                        status -> {
                            try {
                                work.run();
                            } finally {
                                status.setRollbackOnly();
                            }
                        });
    }
}
