package com.platform.modules.mall.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.platform.modules.mall.dao.MallCartDao;
import com.platform.modules.mall.dto.ApiCartAddRequest;
import com.platform.modules.mall.dto.ApiCartCheckedRequest;
import com.platform.modules.mall.dto.ApiCartUpdateRequest;
import com.platform.modules.mall.entity.MallCartEntity;
import com.platform.modules.mall.entity.MallGoodsEntity;
import com.platform.modules.mall.entity.MallGoodsSpecificationEntity;
import com.platform.modules.mall.entity.MallProductEntity;
import com.platform.modules.mall.service.impl.MallCartServiceImpl;
import com.platform.modules.wx.entity.WxUserEntity;
import com.platform.modules.wx.service.WxUserService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

/** 验证规格更换、合并及用户隔离, 使用内存替身而不连接业务数据库. */
class CartApplicationServiceTest {
  private CartApplicationService service;
  private MallCartService carts;
  private MallProductService products;
  private MallGoodsSpecificationService specifications;
  private MallCouponService coupons;
  private WxUserEntity user;
  private MallCartEntity cart;
  private MallProductEntity product;
  private ApiCartUpdateRequest request;

  /** 构造两种规格及可观察的持久化接口, 返回购物车展示不影响写入断言. */
  @BeforeEach
  void setUp() {
    service = spy(new CartApplicationService());
    carts = mock(MallCartService.class);
    products = mock(MallProductService.class);
    specifications = mock(MallGoodsSpecificationService.class);
    coupons = mock(MallCouponService.class);
    WxUserService users = mock(WxUserService.class);
    user = new WxUserEntity();
    user.setId(7L);
    when(users.getOne(any())).thenReturn(user);
    Map.of(
            "cartService", carts,
            "productService", products,
            "goodsSpecificationService", specifications,
            "userService", users,
            "apiCouponService", coupons)
        .forEach((name, value) -> ReflectionTestUtils.setField(service, name, value));
    doReturn(Map.of()).when(service).getCart(user);
    cart = new MallCartEntity();
    cart.setId(100);
    cart.setUserId(7L);
    cart.setGoodsId(10);
    cart.setProductId(20);
    cart.setNumber(1);
    cart.setGoodsSpecifitionIds("old");
    cart.setGoodsSpecifitionNameValue("旧规格");
    when(carts.getById(100)).thenReturn(cart);
    product = new MallProductEntity();
    product.setId(21);
    product.setGoodsId(10);
    product.setGoodsNumber(20000);
    product.setRetailPrice(new BigDecimal("80.00"));
    product.setMarketPrice(new BigDecimal("120.00"));
    when(products.getById(21)).thenReturn(product);
    when(carts.queryList(any())).thenReturn(List.of());
    request = new ApiCartUpdateRequest();
    request.setId(100);
    request.setGoodsId(10);
    request.setProductId(21);
    request.setNumber(2);
  }

  /** 换至无规格产品时用非null空值清除旧规格, 两种价格各取对应产品字段. */
  @Test
  void changingProductClearsOldSpecificationAndKeepsDistinctPrices() {
    assertEquals(0, service.update(user, request).getCode());
    assertEquals(21, cart.getProductId());
    assertEquals(2, cart.getNumber());
    assertEquals("", cart.getGoodsSpecifitionIds());
    assertEquals("", cart.getGoodsSpecifitionNameValue());
    assertEquals(new BigDecimal("80.00"), cart.getRetailPrice());
    assertEquals(new BigDecimal("120.00"), cart.getMarketPrice());
    verify(carts).update(cart);
    verifyNoInteractions(specifications);
  }

  /** 多个规格ID必须以数组交给Mapper, 同时保留商品归属条件. */
  @Test
  void specificationLookupUsesArrayAndProductOwnership() {
    product.setGoodsSpecificationIds("31_32");
    MallGoodsSpecificationEntity first = new MallGoodsSpecificationEntity();
    first.setValue("白色");
    MallGoodsSpecificationEntity second = new MallGoodsSpecificationEntity();
    second.setValue("8mm");
    when(specifications.queryList(any())).thenReturn(List.of(first, second));
    service.update(user, request);
    assertEquals("白色;8mm", cart.getGoodsSpecifitionNameValue());
    ArgumentCaptor<Map<String, Object>> parameters = ArgumentCaptor.forClass(Map.class);
    verify(specifications).queryList(parameters.capture());
    assertArrayEquals(new String[] {"31", "32"}, (String[]) parameters.getValue().get("ids"));
    assertEquals(10, parameters.getValue().get("goodsId"));
  }

  /** 换规格合并已有行时先校验总数量, 成功才删除重复行并写入合并数量. */
  @Test
  void changingProductMergesQuantitiesAndRejectsOverflowBeforeWriting() {
    MallCartEntity existing = new MallCartEntity();
    existing.setId(101);
    existing.setNumber(9999);
    when(carts.queryList(any())).thenReturn(List.of(existing));
    assertEquals(400, service.update(user, request).getCode());
    verify(carts, never()).delete(any());
    verify(carts, never()).update(any(MallCartEntity.class));
    existing.setNumber(3);
    assertEquals(0, service.update(user, request).getCode());
    assertEquals(5, cart.getNumber());
    verify(carts).delete(101);
    verify(carts).update(cart);
  }

  /** 添加到已有规格时同样限制合并总量, 不只检查本次增加的数量. */
  @Test
  void addingExistingProductRejectsMergedQuantityOverLimit() {
    MallGoodsService goods = mock(MallGoodsService.class);
    ReflectionTestUtils.setField(service, "goodsService", goods);
    MallGoodsEntity item = new MallGoodsEntity();
    item.setIsDelete(0);
    item.setIsOnSale(1);
    when(goods.getById(10)).thenReturn(item);
    cart.setNumber(9999);
    when(carts.queryList(any())).thenReturn(List.of(cart));
    ApiCartAddRequest addition = new ApiCartAddRequest();
    addition.setGoodsId(10);
    addition.setProductId(21);
    addition.setNumber(1);
    assertEquals(400, service.add(user, addition).getCode());
    verify(carts, never()).update(any(MallCartEntity.class));
    verify(carts, never()).save(any(MallCartEntity.class));
  }

  /** 更换规格也必须校验原购物车归属, 无权访问不能借合并删除其他记录. */
  @Test
  void foreignCartCannotBeChangedOrMerged() {
    cart.setUserId(8L);
    assertEquals(403, service.update(user, request).getCode());
    verify(carts, never()).queryList(any());
    verify(carts, never()).update(any(MallCartEntity.class));
    verify(carts, never()).delete(any());
  }

  /** 查询本人优惠券无需再查询并丢弃购物车数据. */
  @Test
  void couponListingQueriesOnlyCurrentUserCoupons() {
    service.checkedCouponList(user);
    verify(coupons).queryList(Map.of("userId", 7L));
    verifyNoInteractions(carts);
  }

  /** 勾选只执行带用户条件的批量SQL, 不重新查询或回写无关购物车行. */
  @Test
  void checkingDoesNotRewriteOtherCartRows() {
    MallCartDao mapper = mock(MallCartDao.class);
    MallCartServiceImpl persistence = new MallCartServiceImpl();
    ReflectionTestUtils.setField(persistence, "baseMapper", mapper);
    String[] ids = {"21", "22"};
    persistence.updateCheck(ids, 1, 7L);
    verify(mapper).updateCheck(ids, 1, 7L);
    verifyNoMoreInteractions(mapper);
  }

  /** 非0/1勾选值不写入数据库, 避免脏状态导致前端与结算解释不同. */
  @Test
  void invalidCheckStateDoesNotReachPersistence() {
    ApiCartCheckedRequest checked = new ApiCartCheckedRequest();
    checked.setProductIds("21");
    checked.setIsChecked(2);
    assertEquals(400, service.checked(user, checked).getCode());
    checked.setIsChecked(null);
    assertEquals(400, service.checked(user, checked).getCode());
    verifyNoInteractions(carts);
  }
}
