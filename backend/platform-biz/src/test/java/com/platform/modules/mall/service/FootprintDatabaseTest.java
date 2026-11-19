package com.platform.modules.mall.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.platform.common.exception.BusinessException;
import com.platform.modules.mall.dao.MallFootprintDao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.time.Instant;

/** 真实MySQL与Mapper验证足迹去重、分页、归属和同商品清理. */
@EnabledIfEnvironmentVariable(named = "CARATCRAFT_DATABASE_TEST", matches = "true")
class FootprintDatabaseTest {
    private JdbcTemplate jdbc;
    private FootprintApplicationService service;

    /** 只接受本轮随机临时库, 装配实际XML映射和业务服务. */
    @BeforeEach
    void setUp() throws Exception {
        String url = System.getenv("CARATCRAFT_DATABASE_TEST_URL");
        if (url == null
                || !url.matches(
                        "jdbc:mariadb://127\\.0\\.0\\.1:[0-9]+/caratcraft_init_audit_[0-9]{8}_[a-f0-9]{6}(\\?.*)?")) {
            throw new IllegalStateException("只允许随机独立测试库");
        }
        var source =
                new DriverManagerDataSource(
                        url, "root", System.getenv("CARATCRAFT_DATABASE_TEST_PASSWORD"));
        jdbc = new JdbcTemplate(source);
        var factory = new MybatisSqlSessionFactoryBean();
        var configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        factory.setConfiguration(configuration);
        factory.setDataSource(source);
        factory.setMapperLocations(
                new ClassPathResource("mapper/common/SafeOrderByMapper.xml"),
                new ClassPathResource("mapper/mall/MallFootprintDao.xml"));
        factory.afterPropertiesSet();
        service =
                new FootprintApplicationService(
                        new SqlSessionTemplate(factory.getObject())
                                .getMapper(MallFootprintDao.class));
        clean();
        jdbc.update(
                "INSERT INTO mall_goods(id,name,is_delete)"
                        + " VALUES(1991701,'足迹甲',0),(1991702,'足迹乙',0),(1991703,'已删除',1)");
        long seconds = Instant.parse("2025-01-01T16:30:00Z").getEpochSecond();
        jdbc.update(
                "INSERT INTO mall_footprint(id,user_id,goods_id,add_time) VALUES"
                    + " (1991701,9900701,1991701,?),(1991702,9900701,1991701,?),(1991703,9900701,1991702,?),(1991704,9900702,1991702,?),(1991705,9900701,1991703,?)",
                seconds,
                seconds,
                seconds,
                seconds,
                seconds);
    }

    /** 仅删除本测试明确身份的夹具, 不清理其他测试数据. */
    @AfterEach
    void clean() {
        if (jdbc != null) {
            jdbc.update("DELETE FROM mall_footprint WHERE id BETWEEN 1991701 AND 1991705");
            jdbc.update("DELETE FROM mall_goods WHERE id BETWEEN 1991701 AND 1991703");
        }
    }

    /** 两页各返回一个最新商品, 不重复、不泄漏他人或已删除商品, 日期固定中国时区. */
    @Test
    void pagesLatestOwnedGoods() {
        var first = service.list(9900701L, 1, 1);
        var second = service.list(9900701L, 2, 1);
        assertEquals(2, first.total());
        assertEquals(1991703, first.records().get(0).id());
        assertEquals(1991702, second.records().get(0).id());
        assertEquals("2025-01-02", first.records().get(0).date());
        assertEquals(0, service.list(9900701L, 3, 1).records().size());
    }

    /** 外部记录不能驱动本人同商品删除, 本人选中后旧记录一并清理. */
    @Test
    void deleteChecksChosenOwnerAndRemovesOlderVisits() {
        assertThrows(BusinessException.class, () -> service.delete(9900701L, 1991704));
        assertEquals(2, service.list(9900701L, 1, 10).total());
        service.delete(9900701L, 1991702);
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM mall_footprint WHERE user_id=9900701 AND"
                                + " goods_id=1991701",
                        Integer.class));
        assertEquals(1, service.list(9900701L, 1, 10).total());
        assertEquals(1, service.list(9900702L, 1, 10).total());
    }

    /** 无界分页和非法身份在查询前拒绝. */
    @Test
    void rejectsInvalidPagingAndOwner() {
        assertThrows(IllegalArgumentException.class, () -> service.list(9900701L, 1, 101));
        assertThrows(IllegalArgumentException.class, () -> service.list(9900701L, 0, 10));
        assertThrows(BusinessException.class, () -> service.list(null, 1, 10));
    }
}
