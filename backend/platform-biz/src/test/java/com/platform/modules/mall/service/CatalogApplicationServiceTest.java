package com.platform.modules.mall.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.platform.modules.mall.entity.MallGoodsSpecificationEntity;
import com.platform.modules.mall.entity.MallRelatedGoodsEntity;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

/** 覆盖规格分组响应兼容及显式关联商品优先级, 不访问业务数据库. */
class CatalogApplicationServiceTest {
    /** 重复规格只合并值列表, 保留首次出现顺序和原响应字段. */
    @Test
    void groupsSpecificationsWithoutRawMapCasts() {
        var first = specification(2, "白色");
        var second = specification(1, "大号");
        var third = specification(2, "金色");
        var groups =
                new CatalogApplicationService().groupSpecifications(List.of(first, second, third));
        assertEquals(
                List.of(2, 1), groups.stream().map(group -> group.get("specificationId")).toList());
        assertEquals("白色", groups.get(0).get("name"));
        assertEquals(List.of(first, third), groups.get(0).get("valueList"));
    }

    /** 商家已配置关联商品时使用指定ID, 不误走同分类兜底查询. */
    @Test
    void usesConfiguredRelatedGoodsBeforeCategoryFallback() {
        var service = new CatalogApplicationService();
        var related = mock(MallRelatedGoodsService.class);
        var goods = mock(MallGoodsService.class);
        ReflectionTestUtils.setField(service, "relatedGoodsService", related);
        ReflectionTestUtils.setField(service, "goodsService", goods);
        var link = new MallRelatedGoodsEntity();
        link.setRelatedGoodsId(42);
        when(related.listByMap(anyMap())).thenReturn(List.of(link));
        when(goods.queryList(anyMap())).thenReturn(List.of());
        service.related(10);
        verify(goods)
                .queryList(
                        org.mockito.ArgumentMatchers.argThat(
                                params ->
                                        List.of(42).equals(params.get("goodsIds"))
                                                && Integer.valueOf(0).equals(params.get("isDelete"))
                                                && Integer.valueOf(1)
                                                        .equals(params.get("isOnSale"))));
    }

    /** 不存在的分类应返回明确业务错误, 而不是空指针. */
    @Test
    void rejectsUnknownCategoryWithoutNullPointer() {
        var service = new CatalogApplicationService();
        var categories = mock(MallCategoryService.class);
        ReflectionTestUtils.setField(service, "categoryService", categories);
        when(categories.getById(404)).thenReturn(null);

        var error = assertThrows(IllegalArgumentException.class, () -> service.category(404));
        assertEquals("分类不存在", error.getMessage());
    }

    /** 公开商品总数必须同时排除下架和已删除商品. */
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void countsOnlyVisibleGoods() {
        var service = new CatalogApplicationService();
        var goods = mock(MallGoodsService.class);
        ReflectionTestUtils.setField(service, "goodsService", goods);
        when(goods.count(any())).thenReturn(0L);

        service.count();

        var wrapper = org.mockito.ArgumentCaptor.forClass(QueryWrapper.class);
        verify(goods).count(wrapper.capture());
        String sql = wrapper.getValue().getSqlSegment();
        assertTrue(sql.contains("is_on_sale"));
        assertTrue(sql.contains("is_delete"));
    }

    /** 构建最小规格样本, 不引入无关商品或价格依赖. */
    private MallGoodsSpecificationEntity specification(int id, String value) {
        var specification = new MallGoodsSpecificationEntity();
        specification.setSpecificationId(id);
        specification.setValue(value);
        return specification;
    }
}
