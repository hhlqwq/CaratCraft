package com.platform.modules.app.controller;

import com.platform.annotation.LoginUser;
import com.platform.common.utils.RestResponse;
import com.platform.modules.mall.dto.*;
import com.platform.modules.mall.entity.MallCouponEntity;
import com.platform.modules.mall.service.CartApplicationService;
import com.platform.modules.wx.entity.WxUserEntity;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

/** 购物车HTTP适配层, 业务校验与事务由应用服务统一处理. */
@RestController
@RequestMapping("/app/cart")
public class AppCartController {
  private final CartApplicationService carts;

  /** 注入购物车应用服务. */
  public AppCartController(CartApplicationService carts) {
    this.carts = carts;
  }

  /** 读取本人getCart数据. */
  @PostMapping("getCart")
  public Map<String, Object> getCart(@LoginUser WxUserEntity user) {
    return carts.getCart(user);
  }

  /** 读取本人index数据. */
  @PostMapping("index")
  public RestResponse<Map<String, Object>> index(@LoginUser WxUserEntity user) {
    return carts.index(user);
  }

  /** 读取本人goodscount数据. */
  @PostMapping("goodscount")
  public RestResponse<Map<String, Object>> goodscount(@LoginUser WxUserEntity user) {
    return carts.goodscount(user);
  }

  /** 读取本人checkedCouponList数据. */
  @PostMapping("checkedCouponList")
  public RestResponse<List<MallCouponEntity>> checkedCouponList(@LoginUser WxUserEntity user) {
    return carts.checkedCouponList(user);
  }

  /** 执行本人购物车add操作. */
  @PostMapping("add")
  public RestResponse<Map<String, Object>> add(
      @LoginUser WxUserEntity user, @RequestBody ApiCartAddRequest request) {
    return carts.add(user, request);
  }

  /** 执行本人购物车minus操作. */
  @PostMapping("minus")
  public RestResponse<Integer> minus(
      @LoginUser WxUserEntity user, @RequestBody ApiCartMinusRequest request) {
    return carts.minus(user, request);
  }

  /** 执行本人购物车update操作. */
  @PostMapping("update")
  public RestResponse<Map<String, Object>> update(
      @LoginUser WxUserEntity user, @RequestBody ApiCartUpdateRequest request) {
    return carts.update(user, request);
  }

  /** 执行本人购物车checked操作. */
  @PostMapping("checked")
  public RestResponse<Map<String, Object>> checked(
      @LoginUser WxUserEntity user, @RequestBody ApiCartCheckedRequest request) {
    return carts.checked(user, request);
  }

  /** 执行本人购物车delete操作. */
  @PostMapping("delete")
  public RestResponse<Map<String, Object>> delete(
      @LoginUser WxUserEntity user, @RequestBody ApiCartDeleteRequest request) {
    return carts.delete(user, request);
  }

  /** 统一计算立即购买和购物车结算价格. */
  @PostMapping("checkout")
  public RestResponse<Map<String, Object>> checkout(
      @LoginUser WxUserEntity user,
      Integer couponId,
      @RequestParam(defaultValue = "cart") String type,
      String checkoutId,
      Integer addressId) {
    return carts.checkout(user, couponId, type, checkoutId, addressId);
  }
}
