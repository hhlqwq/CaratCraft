package com.platform.modules.custom.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.platform.modules.custom.dao.ComponentStockDao;
import com.platform.modules.custom.dao.CustomDesignSnapshotDao;
import com.platform.modules.custom.dao.CustomOrderCustomizationDao;
import com.platform.modules.custom.dao.ProductionWorkOrderDao;
import com.platform.modules.custom.service.impl.CustomDesignServiceImpl;
import com.platform.support.TransactionTestSupport;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.security.SecureRandom;
import java.util.List;

/** 在随机独立库验证冻结材料的真实扣补、流水及事务回滚, 不访问业务实例. */
@EnabledIfEnvironmentVariable(named = "CARATCRAFT_DATABASE_TEST", matches = "true")
class CustomStockTransactionDatabaseTest {
    private JdbcTemplate jdbc;
    private CustomDesignService service;
    private int fixtureId;
    private static final String FROZEN =
            "{\"components\":[{\"componentType\":\"pearl\",\"optionCode\":\"frozen-pearl\",\"quantity\":3}]}";

    /** 创建真实MyBatis和事务服务, 独立随机行无需依赖当前可售材料配置. */
    @BeforeEach
    void setUp() throws Exception {
        String url = System.getenv("CARATCRAFT_DATABASE_TEST_URL");
        if (url == null
                || !url.matches(
                        "jdbc:mariadb://127\\.0\\.0\\.1:[0-9]+/caratcraft_init_audit_[0-9]{8}_[a-f0-9]{6}(\\?.*)?")) {
            throw new IllegalStateException("只允许连接随机独立初始化测试库");
        }
        var source =
                new DriverManagerDataSource(
                        url,
                        System.getenv().getOrDefault("CARATCRAFT_DATABASE_TEST_USER", "root"),
                        System.getenv("CARATCRAFT_DATABASE_TEST_PASSWORD"));
        jdbc = new JdbcTemplate(source);
        var factory = new MybatisSqlSessionFactoryBean();
        var configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        factory.setConfiguration(configuration);
        factory.setDataSource(source);
        var global = new GlobalConfig();
        global.setDbConfig(new GlobalConfig.DbConfig());
        global.setIdentifierGenerator(new DefaultIdentifierGenerator(2L, 1L));
        factory.setGlobalConfig(global);
        factory.afterPropertiesSet();
        for (Class<?> mapper :
                List.of(
                        CustomDesignSnapshotDao.class,
                        CustomOrderCustomizationDao.class,
                        ProductionWorkOrderDao.class,
                        ComponentStockDao.class)) {
            factory.getObject().getConfiguration().addMapper(mapper);
        }
        var sessions = new SqlSessionTemplate(factory.getObject());
        var target =
                new CustomDesignServiceImpl(
                        sessions.getMapper(CustomDesignSnapshotDao.class),
                        sessions.getMapper(CustomOrderCustomizationDao.class),
                        sessions.getMapper(ProductionWorkOrderDao.class),
                        null,
                        sessions.getMapper(ComponentStockDao.class),
                        org.mockito.Mockito.mock(
                                com.platform.modules.custom.dao.WorkOrderLogDao.class));
        var proxy = new ProxyFactory(target);
        proxy.addAdvice(
                TransactionTestSupport.interceptor(new DataSourceTransactionManager(source)));
        service = (CustomDesignService) proxy.getProxy();
        fixtureId = new SecureRandom().nextInt(30_000_000, 40_000_000);
        jdbc.update(
                "INSERT INTO"
                    + " CC_DESIGN_SNAPSHOT(id,user_id,goods_id,configuration_json,price_breakdown_json,status)"
                    + " VALUES(?,7,?,'{\"optionIds\":[999999]}',?,'LOCKED')",
                fixtureId,
                fixtureId,
                FROZEN);
        jdbc.update(
                "INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)"
                        + " VALUES(?,'pearl','frozen-pearl',8)",
                fixtureId);
    }

    /** 只清除本用例随机商品归属的行, 不清空表或其他用例数据. */
    @AfterEach
    void cleanUp() {
        if (jdbc != null && fixtureId != 0) {
            jdbc.update("DELETE FROM CC_ORDER_CUSTOMIZATION WHERE order_id=?", fixtureId);
            jdbc.update("DELETE FROM CC_DESIGN_SNAPSHOT WHERE goods_id=?", fixtureId);
            jdbc.update("DELETE FROM CC_STOCK_LOG WHERE goods_id=?", fixtureId);
            jdbc.update("DELETE FROM CC_COMPONENT_STOCK WHERE goods_id=?", fixtureId);
        }
    }

    /** 绑定按冻结三颗扣减, 重复取消只回补一次且原快照与新草稿分离. */
    @Test
    void bindAndRepeatedCancelRestoreExactlyFrozenQuantity() {
        service.bindOrder((long) fixtureId, 7L, fixtureId, fixtureId);
        assertEquals(5, balance());
        service.cancelOrder(fixtureId);
        service.cancelOrder(fixtureId);
        assertEquals(8, balance());
        assertEquals(
                2,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM CC_STOCK_LOG WHERE goods_id=?",
                        Integer.class,
                        fixtureId));
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT SUM(quantity_delta) FROM CC_STOCK_LOG WHERE goods_id=?",
                        Integer.class,
                        fixtureId));
        assertEquals(
                "CANCELLED",
                jdbc.queryForObject(
                        "SELECT status FROM CC_DESIGN_SNAPSHOT WHERE id=?",
                        String.class,
                        fixtureId));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM CC_DESIGN_SNAPSHOT WHERE goods_id=? AND"
                                + " status='DRAFT'",
                        Integer.class,
                        fixtureId));
    }

    /** 第二种材料不足时, 已扣第一种材料及流水和绑定状态全部回滚. */
    @Test
    void laterMaterialFailureRollsBackEarlierBalanceAndLedger() {
        String frozen =
                FROZEN.replace(
                        "]}",
                        ",{\"componentType\":\"chain\",\"optionCode\":\"missing-chain\",\"quantity\":1}]}");
        jdbc.update(
                "UPDATE CC_DESIGN_SNAPSHOT SET price_breakdown_json=? WHERE id=?",
                frozen,
                fixtureId);
        assertThrows(
                IllegalStateException.class,
                () -> service.bindOrder((long) fixtureId, 7L, fixtureId, fixtureId));
        assertEquals(8, balance());
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM CC_STOCK_LOG WHERE goods_id=?",
                        Integer.class,
                        fixtureId));
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM CC_ORDER_CUSTOMIZATION WHERE order_id=?",
                        Integer.class,
                        fixtureId));
        assertEquals(
                "LOCKED",
                jdbc.queryForObject(
                        "SELECT status FROM CC_DESIGN_SNAPSHOT WHERE id=?",
                        String.class,
                        fixtureId));
    }

    /** 取消遇损坏冻结清单时回滚绑定取消, 不按旧选项ID猜测恢复库存. */
    @Test
    void malformedSnapshotRollsBackCancellationState() {
        service.bindOrder((long) fixtureId, 7L, fixtureId, fixtureId);
        jdbc.update(
                "UPDATE CC_DESIGN_SNAPSHOT SET price_breakdown_json='{}' WHERE id=?", fixtureId);
        assertThrows(IllegalStateException.class, () -> service.cancelOrder(fixtureId));
        assertEquals(5, balance());
        assertEquals(
                "LOCKED",
                jdbc.queryForObject(
                        "SELECT status FROM CC_ORDER_CUSTOMIZATION WHERE order_id=?",
                        String.class,
                        fixtureId));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM CC_STOCK_LOG WHERE goods_id=?",
                        Integer.class,
                        fixtureId));
    }

    /** 快照缺失不能悄悄完成取消而遗漏库存, 绑定状态必须回滚. */
    @Test
    void missingSnapshotRollsBackCancellationState() {
        service.bindOrder((long) fixtureId, 7L, fixtureId, fixtureId);
        jdbc.update("DELETE FROM CC_DESIGN_SNAPSHOT WHERE id=?", fixtureId);
        assertThrows(IllegalStateException.class, () -> service.cancelOrder(fixtureId));
        assertEquals(5, balance());
        assertEquals(
                "LOCKED",
                jdbc.queryForObject(
                        "SELECT status FROM CC_ORDER_CUSTOMIZATION WHERE order_id=?",
                        String.class,
                        fixtureId));
    }

    /** 直接读取已提交余额, 避免服务对象缓存掩盖事务结果. */
    private int balance() {
        return jdbc.queryForObject(
                "SELECT stock FROM CC_COMPONENT_STOCK WHERE goods_id=? AND"
                        + " option_code='frozen-pearl'",
                Integer.class,
                fixtureId);
    }
}
