package com.platform.modules.mall.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.platform.modules.mall.entity.MallGoodsSpecificationEntity;
import com.platform.modules.mall.entity.MallRelatedGoodsEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** 覆盖规格分组响应兼容及显式关联商品优先级, 不访问业务数据库. */
class CatalogApplicationServiceTest {
  /** 重复规格只合并值列表, 保留首次出现顺序和原响应字段. */
  @Test
  void groupsSpecificationsWithoutRawMapCasts() {
    var first = specification(2, "白色");
    var second = specification(1, "大号");
    var third = specification(2, "金色");
    var groups = new CatalogApplicationService().groupSpecifications(List.of(first, second, third));
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
                        && Integer.valueOf(1).equals(params.get("isOnSale"))));
  }

  /** 构建最小规格样本, 不引入无关商品或价格依赖. */
  private MallGoodsSpecificationEntity specification(int id, String value) {
    var specification = new MallGoodsSpecificationEntity();
    specification.setSpecificationId(id);
    specification.setValue(value);
    return specification;
  }
}
