package com.platform.modules.mall.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.modules.mall.entity.MallGoodsEntity;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 检查双维度查询条件与公开详情边界, 不连接数据库. */
class PearlCatalogServiceTest {
    /** 来源、品类、可售及未删除条件应同时存在, 分页参数不被服务端默认值覆盖. */
    @Test
    void combinesBothDimensionsWithVisibilityAndPagination() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), "test"),
                MallGoodsEntity.class);
        MallGoodsService goods = mock(MallGoodsService.class);
        MallGoodsEntity item = new MallGoodsEntity();
        Page<MallGoodsEntity> response = new Page<>(3, 7);
        response.setRecords(List.of(item));
        response.setTotal(15);
        when(goods.page(org.mockito.ArgumentMatchers.<Page<MallGoodsEntity>>notNull(), notNull()))
                .thenReturn(response);
        var result = new PearlCatalogService(goods).page("FRESHWATER", "NECKLACE", 3, 7);
        ArgumentCaptor<Page<MallGoodsEntity>> page = ArgumentCaptor.captor();
        ArgumentCaptor<LambdaQueryWrapper<MallGoodsEntity>> filter = ArgumentCaptor.captor();
        verify(goods).page(page.capture(), filter.capture());
        assertEquals(3, page.getValue().getCurrent());
        assertEquals(7, page.getValue().getSize());
        String sql = filter.getValue().getSqlSegment();
        assertTrue(sql.contains("is_delete"));
        assertTrue(sql.contains("is_on_sale"));
        assertTrue(sql.contains("ORDER BY id DESC"));
        var wrapper = filter.getValue();
        var values = new ArrayList<>(wrapper.getParamNameValuePairs().values());
        assertEquals(2, Collections.frequency(values, "FRESHWATER"));
        assertEquals(2, Collections.frequency(values, "NECKLACE"));
        assertEquals(List.of(item), result.get("records"));
        assertEquals(15L, result.get("total"));
    }

    /** 不能通过未知分类或过大的分页参数绕过公开目录限制. */
    @Test
    void rejectsInvalidDimensionsBeforeQuerying() {
        MallGoodsService goods = mock(MallGoodsService.class);
        var catalog = new PearlCatalogService(goods);
        assertThrows(IllegalArgumentException.class, () -> catalog.page("UNKNOWN", "", 1, 20));
        assertThrows(IllegalArgumentException.class, () -> catalog.page("", "UNKNOWN", 1, 20));
        assertThrows(IllegalArgumentException.class, () -> catalog.page("", "", 0, 20));
        assertThrows(IllegalArgumentException.class, () -> catalog.page("", "", 1, 51));
        verifyNoInteractions(goods);
    }

    /** 缺失、下架和已删除商品在查询其他详情依赖前拒绝访问. */
    @Test
    void publicDetailRejectsUnavailableGoods() {
        MallGoodsService goods = mock(MallGoodsService.class);
        var service = new CatalogApplicationService();
        ReflectionTestUtils.setField(service, "goodsService", goods);
        assertThrows(IllegalArgumentException.class, () -> service.detail(null, 12, null));
        MallGoodsEntity item = new MallGoodsEntity();
        item.setIsDelete(0);
        item.setIsOnSale(0);
        when(goods.getById(12)).thenReturn(item);
        assertThrows(IllegalArgumentException.class, () -> service.detail(null, 12, null));
        item.setIsDelete(1);
        item.setIsOnSale(1);
        assertThrows(IllegalArgumentException.class, () -> service.detail(null, 12, null));
    }
}
