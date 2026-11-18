package com.platform.modules.mall.controller;

import com.platform.common.annotation.SysLog;
import com.platform.common.utils.RestResponse;
import com.platform.datascope.StaffAuthority;
import com.platform.datascope.StaffAuthority.Resource;
import com.platform.modules.mall.service.MallOrderService;
import com.platform.modules.mall.service.OrderRefundService;

import lombok.RequiredArgsConstructor;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 客服登记及跟进售后, 资金审核与执行由其他入口授权. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/mall/aftersale")
public class MallAfterSaleController {
    private final StaffAuthority authority;
    private final OrderRefundService refunds;
    private final MallOrderService orders;
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;

    /** 读取客服必要的订单编号和售后说明. */
    @GetMapping("list")
    @RequiresPermissions("mall:aftersale:list")
    public RestResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String todo) {
        var scope = authority.scope("mall:aftersale:list");
        boolean pending = "supportPending".equals(todo);
        if (pending) {
            scope = scope.intersect(authority.scope("mall:aftersale:handle"));
        }
        return RestResponse.ok(refunds.supportPage(page, limit, scope, pending));
    }

    /** 客服登记时只获取授权的近期已付款订单编号, 省略无关客户隐私字段. */
    @GetMapping("orders")
    @RequiresPermissions("mall:aftersale:register")
    public RestResponse<List<Map<String, Object>>> eligibleOrders() {
        var args = new ArrayList<Object>();
        String scope = authority.scope("mall:aftersale:register").andWhere("o.scope_org_no", args);
        return RestResponse.ok(
                jdbc.queryForList(
                        "SELECT o.id,o.order_sn AS orderSn FROM mall_order o WHERE o.pay_status=2"
                                + " AND o.order_status IN (201,300,301)"
                                + scope
                                + " ORDER BY o.id DESC LIMIT 100",
                        args.toArray()));
    }

    /** 根据授权订单登记退款类售后, 仅创建待审核申请. */
    @SysLog("登记售后")
    @PostMapping("register")
    @RequiresPermissions("mall:aftersale:register")
    public RestResponse<Map<String, Object>> register(@RequestBody Note input) {
        authority.require("mall:aftersale:register", Resource.ORDER, input.orderId());
        var order = orders.getById(input.orderId());
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        return RestResponse.ok(refunds.request(order.getId(), order.getUserId(), input.note()));
    }

    /** 记录客服已处理的内容, 保留资金工作流状态. */
    @SysLog("处理售后")
    @PostMapping("handle")
    @RequiresPermissions("mall:aftersale:handle")
    public RestResponse<String> handle(@RequestBody Note input) {
        authority.require("mall:aftersale:handle", Resource.ORDER, input.orderId());
        refunds.handleSupport(input.orderId(), input.note());
        return RestResponse.ok();
    }

    /** 记录客服输入的订单和处理说明, 不接受资金字段. */
    public record Note(Integer orderId, String note) {}
}
