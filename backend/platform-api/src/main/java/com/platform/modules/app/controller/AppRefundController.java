package com.platform.modules.app.controller;

import com.platform.annotation.LoginUser;
import com.platform.common.utils.RestResponse;
import com.platform.modules.mall.service.OrderRefundService;
import com.platform.modules.wx.entity.WxUserEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 本人售后申请与进度查询, 不直接发起支付侧退款. */
@RestController
@RequestMapping("/app/refund")
public class AppRefundController {
    private final OrderRefundService refunds;

    /** 注入持久化退款服务. */
    public AppRefundController(OrderRefundService refunds) {
        this.refunds = refunds;
    }

    /** 申请必须明确理由, 已收货同样进入商家审核. */
    @PostMapping("request")
    public RestResponse<Object> request(
            @LoginUser WxUserEntity user, @RequestBody Application request) {
        return RestResponse.ok(refunds.request(request.orderId(), user.getId(), request.reason()));
    }

    /** 限定本人订单查看审核与支付侧对账进度. */
    @PostMapping("status")
    public RestResponse<Object> status(@LoginUser WxUserEntity user, Integer orderId) {
        return RestResponse.ok(refunds.owned(orderId, user.getId()));
    }

    /** 售后列表只按登录身份筛选, 不接收客户端传入的用户标识. */
    @GetMapping("list")
    public RestResponse<Object> list(
            @LoginUser WxUserEntity user,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "20") int limit) {
        return RestResponse.ok(refunds.mine(user.getId(), page, limit));
    }

    /** 用户不能自行传入退款金额和状态. */
    public record Application(Integer orderId, String reason) {}
}
