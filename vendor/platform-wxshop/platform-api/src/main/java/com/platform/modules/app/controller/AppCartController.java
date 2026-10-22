package com.platform.modules.app.controller;

import com.platform.annotation.LoginUser;
import com.platform.common.utils.RestResponse;
import com.platform.modules.mall.dto.ApiCartAddRequest;
import com.platform.modules.mall.dto.ApiCartCheckedRequest;
import com.platform.modules.mall.dto.ApiCartDeleteRequest;
import com.platform.modules.mall.dto.ApiCartMinusRequest;
import com.platform.modules.mall.dto.ApiCartUpdateRequest;
import com.platform.modules.mall.entity.MallCouponEntity;
import com.platform.modules.mall.service.CartApplicationService;
import com.platform.modules.wx.entity.WxUserEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** 购物车HTTP适配层, 业务校验与事务由应用服务统一处理. */
@RestController
@RequestMapping("/app/cart")
public class AppCartController {
    private final CartApplicationService carts;

    /** 注入购物车应用服务. */
    public AppCartController(CartApplicationService carts) {
        this.carts = carts;
    }

    /** 兼容旧客户端的购物车原始数据入口, 仅查询当前登录用户. */
    @PostMapping("getCart")
    public Map<String, Object> getCart(@LoginUser WxUserEntity user) {
        return carts.getCart(user);
    }

    /** 返回购物车列表及总数量、已选金额等汇总数据. */
    @PostMapping("index")
    public RestResponse<Map<String, Object>> index(@LoginUser WxUserEntity user) {
        return carts.index(user);
    }

    /** 返回当前用户的购物车商品总件数. */
    @PostMapping("goodscount")
    public RestResponse<Map<String, Object>> goodscount(@LoginUser WxUserEntity user) {
        return carts.goodscount(user);
    }

    /** 查询当前用户持有的优惠券, 是否适用由结算服务重新校验. */
    @PostMapping("checkedCouponList")
    public RestResponse<List<MallCouponEntity>> checkedCouponList(@LoginUser WxUserEntity user) {
        return carts.checkedCouponList(user);
    }

    /** 增加购物车商品, 合并已有规格并校验可售状态和库存. */
    @PostMapping("add")
    public RestResponse<Map<String, Object>> add(
            @LoginUser WxUserEntity user, @RequestBody ApiCartAddRequest request) {
        return carts.add(user, request);
    }

    /** 减少指定规格的购买数量, 减至零时删除记录. */
    @PostMapping("minus")
    public RestResponse<Integer> minus(
            @LoginUser WxUserEntity user, @RequestBody ApiCartMinusRequest request) {
        return carts.minus(user, request);
    }

    /** 更新本人购物车行的规格或数量, 成功后返回新的完整统计. */
    @PostMapping("update")
    public RestResponse<Map<String, Object>> update(
            @LoginUser WxUserEntity user, @RequestBody ApiCartUpdateRequest request) {
        return carts.update(user, request);
    }

    /** 批量更新结算选择, 不接受其他用户的购物车记录. */
    @PostMapping("checked")
    public RestResponse<Map<String, Object>> checked(
            @LoginUser WxUserEntity user, @RequestBody ApiCartCheckedRequest request) {
        return carts.checked(user, request);
    }

    /** 按当前用户和规格集合批量删除, 成功后返回剩余购物车. */
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
