package com.platform.modules.app.controller;

import com.platform.annotation.IgnoreAuth;
import com.platform.annotation.LoginUser;
import com.platform.common.utils.RestResponse;
import com.platform.modules.mall.service.CatalogApplicationService;
import com.platform.modules.wx.entity.WxUserEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 商品目录HTTP适配层, 查询与用户足迹等事务交由应用服务. */
@RestController
@RequestMapping("/app/goods")
public class AppGoodsController {
  private final CatalogApplicationService catalog;

  /** 注入目录应用服务. */
  public AppGoodsController(CatalogApplicationService catalog) {
    this.catalog = catalog;
  }

  /** 读取商品目录index接口数据. */
  @IgnoreAuth
  @PostMapping("index")
  public RestResponse<Object> index() {
    return catalog.index();
  }

  /** 读取商品目录sku接口数据. */
  @IgnoreAuth
  @PostMapping("sku")
  public RestResponse<Object> sku(Integer id) {
    return catalog.sku(id);
  }

  /** 读取商品目录detail接口数据. */
  @IgnoreAuth
  @PostMapping("detail")
  public RestResponse<Object> detail(@LoginUser WxUserEntity loginUser, Integer id, Long referrer) {
    return catalog.detail(loginUser, id, referrer);
  }

  /** 读取商品目录category接口数据. */
  @IgnoreAuth
  @PostMapping("category")
  public RestResponse<Object> category(Integer id) {
    return catalog.category(id);
  }

  /** 读取商品目录list接口数据. */
  @IgnoreAuth
  @PostMapping("list")
  public RestResponse<Object> list(
      @LoginUser WxUserEntity loginUser,
      Integer categoryId,
      Integer brandId,
      String keyword,
      Integer isNew,
      Integer isHot,
      @RequestParam(value = "page", defaultValue = "1") Integer page,
      @RequestParam(value = "size", defaultValue = "10") Integer size,
      String sort,
      String order) {
    return catalog.list(
        loginUser, categoryId, brandId, keyword, isNew, isHot, page, size, sort, order);
  }

  /** 读取商品目录filter接口数据. */
  @IgnoreAuth
  @PostMapping("filter")
  public RestResponse<Object> filter(
      Integer categoryId, String keyword, Integer isNew, Integer isHot) {
    return catalog.filter(categoryId, keyword, isNew, isHot);
  }

  /** 读取商品目录new接口数据. */
  @IgnoreAuth
  @PostMapping("new")
  public RestResponse<Object> newAction() {
    return catalog.newAction();
  }

  /** 读取商品目录hot接口数据. */
  @IgnoreAuth
  @PostMapping("hot")
  public RestResponse<Object> hot() {
    return catalog.hot();
  }

  /** 读取商品目录related接口数据. */
  @IgnoreAuth
  @PostMapping("related")
  public RestResponse<Object> related(Integer id) {
    return catalog.related(id);
  }

  /** 读取商品目录count接口数据. */
  @IgnoreAuth
  @PostMapping("count")
  public RestResponse<Object> count() {
    return catalog.count();
  }

  /** 读取商品目录productlist接口数据. */
  @IgnoreAuth
  @PostMapping("productlist")
  public RestResponse<Object> productlist(
      @LoginUser WxUserEntity loginUser,
      Integer categoryId,
      Integer isNew,
      Integer discount,
      @RequestParam(value = "page", defaultValue = "1") Integer page,
      @RequestParam(value = "size", defaultValue = "10") Integer size,
      String sort,
      String order) {
    return catalog.productlist(loginUser, categoryId, isNew, discount, page, size, sort, order);
  }
}
