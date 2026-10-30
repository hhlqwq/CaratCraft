package com.platform.modules.mall.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.platform.common.exception.BusinessException;
import com.platform.modules.mall.dao.MallAttributeDao;
import com.platform.modules.mall.dao.MallGoodsAttributeDao;
import com.platform.modules.mall.dao.MallGoodsDao;
import com.platform.modules.mall.dao.MallGoodsGalleryDao;
import com.platform.modules.mall.dao.MallGoodsSpecificationDao;
import com.platform.modules.mall.dao.MallProductDao;
import com.platform.modules.mall.dao.MallSpecificationDao;
import com.platform.modules.mall.dto.GoodsAggregateDTO;
import com.platform.modules.mall.entity.MallGoodsEntity;
import com.platform.modules.mall.service.impl.MallAttributeServiceImpl;
import com.platform.modules.mall.service.impl.MallGoodsAggregateServiceImpl;
import com.platform.modules.mall.service.impl.MallGoodsAttributeServiceImpl;
import com.platform.modules.mall.service.impl.MallGoodsGalleryServiceImpl;
import com.platform.modules.mall.service.impl.MallGoodsServiceImpl;
import com.platform.modules.mall.service.impl.MallGoodsSpecificationServiceImpl;
import com.platform.modules.mall.service.impl.MallProductServiceImpl;
import com.platform.modules.mall.service.impl.MallSpecificationServiceImpl;
import com.platform.support.TransactionTestSupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** 使用真实MyBatis服务和Spring事务验证商品聚合, 仅接受随机独立测试库. */
@EnabledIfEnvironmentVariable(named = "CARATCRAFT_DATABASE_TEST", matches = "true")
class GoodsAggregateDatabaseTest {
    private JdbcTemplate jdbc;
    private SqlSessionTemplate sessions;
    private MallGoodsAggregateService aggregate;
    private CheckoutPricingService pricing;
    private TransactionTemplate transactions;

    /** 装配真实映射器、子项服务和事务代理, 不加载支付或业务应用. */
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
        var global = new GlobalConfig();
        global.setDbConfig(new GlobalConfig.DbConfig());
        global.setIdentifierGenerator(new DefaultIdentifierGenerator(1L, 1L));
        factory.setGlobalConfig(global);
        factory.setDataSource(source);
        factory.afterPropertiesSet();
        for (Class<?> mapper :
                List.of(
                        MallGoodsDao.class,
                        MallGoodsAttributeDao.class,
                        MallGoodsGalleryDao.class,
                        MallGoodsSpecificationDao.class,
                        MallProductDao.class,
                        MallAttributeDao.class,
                        MallSpecificationDao.class)) {
            factory.getObject().getConfiguration().addMapper(mapper);
        }
        sessions = new SqlSessionTemplate(factory.getObject());
        var goods = mapped(new MallGoodsServiceImpl(), MallGoodsDao.class);
        ReflectionTestUtils.setField(
                goods, "pearlProductValidator", new PearlProductValidator(jdbc));
        var target =
                new MallGoodsAggregateServiceImpl(
                        goods,
                        mapped(new MallGoodsAttributeServiceImpl(), MallGoodsAttributeDao.class),
                        mapped(new MallGoodsGalleryServiceImpl(), MallGoodsGalleryDao.class),
                        mapped(
                                new MallGoodsSpecificationServiceImpl(),
                                MallGoodsSpecificationDao.class),
                        mapped(new MallProductServiceImpl(), MallProductDao.class),
                        mapped(new MallAttributeServiceImpl(), MallAttributeDao.class),
                        mapped(new MallSpecificationServiceImpl(), MallSpecificationDao.class));
        var manager = new DataSourceTransactionManager(source);
        transactions = new TransactionTemplate(manager);
        var proxy = new ProxyFactory(target);
        proxy.addAdvice(TransactionTestSupport.interceptor(manager));
        aggregate = (MallGoodsAggregateService) proxy.getProxy();
        pricing = new CheckoutPricingService(null, sessions.getMapper(MallProductDao.class), goods);
    }

    /** 将实际MyBatis映射器注入仓库真实服务, 不替换持久化行为. */
    private <T> T mapped(T service, Class<?> mapper) {
        ReflectionTestUtils.setField(service, "baseMapper", sessions.getMapper(mapper));
        return service;
    }

    /** 每个用例结束回滚独立事务, 不清空其他用例的数据. */
    private void rollback(Runnable work) {
        transactions.executeWithoutResult(
                status -> {
                    try {
                        work.run();
                    } finally {
                        status.setRollbackOnly();
                    }
                });
    }

    /** 构造可售的完整珍珠商品, 不使用初始化示例的身份. */
    private GoodsAggregateDTO item() {
        var dto = new GoodsAggregateDTO();
        var goods = new MallGoodsEntity();
        goods.setName("隔离商品聚合");
        goods.setCategoryId(1);
        goods.setRetailPrice(new BigDecimal("80.00"));
        goods.setGoodsNumber(8);
        goods.setIsOnSale(1);
        goods.setIsDelete(0);
        goods.setPearlSource("FRESHWATER");
        goods.setJewelryType("NECKLACE");
        dto.setGoods(goods);
        return dto;
    }

    /** 保存的默认SKU能被购买服务读取, 不接受越库存数量或跨商品身份. */
    @Test
    void defaultSkuIsPersistedAndCheckedAcrossLayers() {
        rollback(
                () -> {
                    var dto = item();
                    int id = aggregate.saveAggregate(dto);
                    var saved = aggregate.getAggregate(id);
                    assertEquals(1, saved.getSkuList().size());
                    var sku = saved.getSkuList().get(0);
                    assertEquals("", sku.getGoodsSpecificationIds());
                    assertEquals(sku.getId(), saved.getGoods().getPrimaryProductId());
                    assertEquals(8, pricing.requireProduct(sku.getId(), id, 8).getGoodsNumber());
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> pricing.requireProduct(sku.getId(), id, 9));
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> pricing.requireProduct(sku.getId(), id + 1, 1));
                    jdbc.update("UPDATE mall_goods SET is_on_sale=0 WHERE id=?", id);
                    sessions.clearCache();
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> pricing.requireProduct(sku.getId(), id, 1));
                });
    }

    /** 移除组合后旧SKU失效, 保留组合的规格行及SKU身份保持不变. */
    @Test
    void replacingCombinationsPreservesOnlyUnchangedIdentities() {
        rollback(
                () -> {
                    var dto = item();
                    var group = new GoodsAggregateDTO.SpecGroup();
                    group.setSpecificationId(7);
                    var white = new GoodsAggregateDTO.SpecValue();
                    white.setValue("白色");
                    var gold = new GoodsAggregateDTO.SpecValue();
                    gold.setValue("金色");
                    group.setValues(List.of(white, gold));
                    dto.setSpecs(List.of(group));
                    dto.setSkuList(List.of(sku("7:白色"), sku("7:金色")));
                    int id = aggregate.saveAggregate(dto);
                    var saved = aggregate.getAggregate(id);
                    int whiteId =
                            saved.getSkuList().stream()
                                    .filter(s -> s.getGoodsSpecificationIds().equals("7:白色"))
                                    .findFirst()
                                    .orElseThrow()
                                    .getId();
                    int goldId =
                            saved.getSkuList().stream()
                                    .filter(s -> s.getGoodsSpecificationIds().equals("7:金色"))
                                    .findFirst()
                                    .orElseThrow()
                                    .getId();
                    int specificationId =
                            jdbc.queryForObject(
                                    "SELECT id FROM mall_goods_specification WHERE goods_id=? AND"
                                            + " value=?",
                                    Integer.class,
                                    id,
                                    "白色");
                    group.setValues(List.of(white));
                    dto.setSkuList(List.of(sku("7:白色")));
                    aggregate.updateAggregate(dto);
                    var current = aggregate.getAggregate(id);
                    assertEquals(1, current.getSkuList().size());
                    assertEquals(whiteId, current.getSkuList().get(0).getId());
                    assertNotEquals(goldId, current.getGoods().getPrimaryProductId());
                    assertEquals(
                            specificationId,
                            jdbc.queryForObject(
                                    "SELECT id FROM mall_goods_specification WHERE goods_id=?",
                                    Integer.class,
                                    id));
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> pricing.requireProduct(goldId, id, 1));
                });
    }

    /** 新建SKU继承商品初始价格和库存, 不接收客户端持久化身份. */
    private GoodsAggregateDTO.SkuItem sku(String key) {
        var sku = new GoodsAggregateDTO.SkuItem();
        sku.setGoodsSpecificationIds(key);
        return sku;
    }

    /** 编辑读取的SKU库存缺省时保留成交后的余额和身份, 伪造表单ID无效. */
    @Test
    void editingPreservesIdentityAndLatestStock() {
        rollback(
                () -> {
                    int id = aggregate.saveAggregate(item());
                    var dto = aggregate.getAggregate(id);
                    int skuId = dto.getSkuList().get(0).getId();
                    jdbc.update("UPDATE mall_product SET goods_number=3 WHERE id=?", skuId);
                    dto.getSkuList().get(0).setId(skuId + 10000);
                    dto.getSkuList().get(0).setGoodsNumber(null);
                    dto.getSkuList().get(0).setRetailPrice(new BigDecimal("95.00"));
                    aggregate.updateAggregate(dto);
                    var current = aggregate.getAggregate(id).getSkuList().get(0);
                    assertEquals(skuId, current.getId());
                    assertEquals(3, current.getGoodsNumber());
                    assertEquals(
                            new BigDecimal("95.00"),
                            pricing.requireProduct(skuId, id, 3).getRetailPrice());
                });
    }

    /** 两笔并发扣减不能都消耗同一余额, 余量不足时保持非负且只成功一次. */
    @Test
    void concurrentPurchasesCannotOversellPersistedSku() throws Exception {
        int id = aggregate.saveAggregate(item());
        int productId = aggregate.getAggregate(id).getSkuList().get(0).getId();
        var executor = Executors.newFixedThreadPool(2);
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try {
            Callable<Integer> purchase =
                    () -> {
                        ready.countDown();
                        if (!start.await(10, TimeUnit.SECONDS)) {
                            throw new IllegalStateException("并发测试未能同时开始");
                        }
                        return transactions.execute(
                                status ->
                                        sessions.getMapper(MallProductDao.class)
                                                .deductStock(productId, 6));
                    };
            var first = executor.submit(purchase);
            var second = executor.submit(purchase);
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            assertEquals(1, first.get(15, TimeUnit.SECONDS) + second.get(15, TimeUnit.SECONDS));
            assertEquals(
                    2,
                    jdbc.queryForObject(
                            "SELECT goods_number FROM mall_product WHERE id=?",
                            Integer.class,
                            productId));
        } finally {
            start.countDown();
            executor.shutdownNow();
            executor.awaitTermination(10, TimeUnit.SECONDS);
            jdbc.update("DELETE FROM mall_product WHERE goods_id=?", id);
            jdbc.update("DELETE FROM mall_goods WHERE id=?", id);
        }
    }

    /** 更新不存在的主商品必须拒绝, 不得在没有外键的子表中留下孤儿SKU. */
    @Test
    void missingGoodsNeverCreatesOrphanChildren() {
        rollback(
                () -> {
                    var dto = item();
                    dto.getGoods().setId(2_000_000_000);
                    assertThrows(BusinessException.class, () -> aggregate.updateAggregate(dto));
                    assertEquals(
                            0,
                            jdbc.queryForObject(
                                    "SELECT COUNT(*) FROM mall_product WHERE goods_id=?",
                                    Integer.class,
                                    dto.getGoods().getId()));
                });
    }

    /** 子项写入受到数据库约束拒绝时, 主商品和已删除的旧相册均回滚. */
    @Test
    void childWriteFailureRollsBackWholeAggregate() {
        var dto = item();
        var originalImage = new GoodsAggregateDTO.GalleryItem();
        originalImage.setImgUrl("/upload/original.png");
        originalImage.setImgDesc("原始相册");
        dto.setGallery(List.of(originalImage));
        int id = aggregate.saveAggregate(dto);
        try {
            var edit = aggregate.getAggregate(id);
            edit.getGoods().setName("不应保存的名称");
            var image = new GoodsAggregateDTO.GalleryItem();
            image.setImgUrl("/upload/test.png");
            image.setImgDesc("长".repeat(256));
            edit.setGallery(List.of(image));
            assertThrows(RuntimeException.class, () -> aggregate.updateAggregate(edit));
            assertEquals("隔离商品聚合", aggregate.getAggregate(id).getGoods().getName());
            assertEquals(1, aggregate.getAggregate(id).getSkuList().size());
            assertEquals("原始相册", aggregate.getAggregate(id).getGallery().get(0).getImgDesc());
        } finally {
            jdbc.update("DELETE FROM mall_goods_gallery WHERE goods_id=?", id);
            jdbc.update("DELETE FROM mall_product WHERE goods_id=?", id);
            jdbc.update("DELETE FROM mall_goods WHERE id=?", id);
        }
    }
}
