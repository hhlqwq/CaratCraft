/*
 *
 *      Copyright (c) 2018-2099, hehailong All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the openwtai.com developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: hehailong(845367567@qq.com)
 *
 */
package com.platform.modules.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.annotation.SysLog;
import com.platform.common.utils.RestResponse;
import com.platform.datascope.StaffAuthority;
import com.platform.datascope.StaffAuthority.Resource;
import com.platform.modules.mall.entity.MallOrderEntity;
import com.platform.modules.mall.entity.MallOrderGoodsEntity;
import com.platform.modules.mall.service.MallOrderGoodsService;
import com.platform.modules.mall.service.MallOrderService;
import com.platform.modules.sys.controller.AbstractController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单管理控制器。
 *
 * <p>提供后台订单管理相关接口：列表、分页、详情、新增、修改、删除。
 *
 * @author hehailong
 * @since 2026-03-15 14:30:00
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("mall/order")
@Tag(name = "MallOrderController|订单管理")
public class MallOrderController extends AbstractController {

    private final MallOrderService mallOrderService;
    private final com.platform.modules.mall.service.OrderLifecycleService lifecycleService;
    private final MallOrderGoodsService mallOrderGoodsService;
    private final StaffAuthority authority;

    /**
     * 查询所有订单列表。
     *
     * @param params 查询参数
     * @return 订单列表
     */
    @Operation(summary = "查看所有列表", description = "查看所有列表")
    @GetMapping("/queryAll")
    @RequiresPermissions("mall:order:list")
    public RestResponse<List<MallOrderEntity>> queryAll(@RequestParam Map<String, Object> params) {
        applyScope(params);
        return RestResponse.ok(
                mallOrderService.queryAll(params).stream().map(this::listProjection).toList());
    }

    /**
     * 分页查询订单。
     *
     * @param params 查询参数
     * @return 分页数据
     */
    @Operation(summary = "分页查询", description = "分页查询")
    @GetMapping("/list")
    @RequiresPermissions("mall:order:list")
    public RestResponse<Page<MallOrderEntity>> list(@RequestParam Map<String, Object> params) {
        applyScope(params);
        Page<MallOrderEntity> result = mallOrderService.queryPage(params);
        var info = authority.scope("mall:order:info");
        var shipment = authority.scope("mall:order:ship");
        var price = authority.scope("mall:order:price:update");
        for (var row : result.getRecords()) {
            String org = row.getScopeOrgNo();
            row.setCanReadDetails(info.permits(org));
            row.setCanShip(shipment.permits(org));
            row.setCanAdjustPrice(
                    price.permits(org)
                            && (row.getPayId() == null
                                    || row.getPayId().isBlank()
                                    || "0".equals(row.getPayId())));
            listProjection(row);
        }
        return RestResponse.ok(result);
    }

    /**
     * 根据主键查询订单详情。
     *
     * @param id 主键
     * @return 订单详情
     */
    @Operation(
            summary = "根据主键查询详情",
            description = "根据主键查询详情",
            parameters = {
                @Parameter(
                        in = ParameterIn.PATH,
                        name = "id",
                        description = "主键",
                        example = "1",
                        required = true)
            })
    @GetMapping("/info/{id}")
    @RequiresPermissions("mall:order:info")
    public RestResponse<MallOrderEntity> info(@PathVariable("id") Integer id) {
        authority.require("mall:order:info", Resource.ORDER, id);
        Map<String, Object> params = new HashMap<>();
        params.put("id", String.valueOf(id));
        List<MallOrderEntity> list = mallOrderService.queryAll(params);
        if (list.isEmpty()) {
            return RestResponse.ok(detailProjection(mallOrderService.getById(id)));
        }
        return RestResponse.ok(detailProjection(list.get(0)));
    }

    /**
     * 查询订单商品明细列表。
     *
     * @param id 订单ID
     * @return 订单商品明细
     */
    @Operation(summary = "查询订单商品明细", description = "按订单ID查询订单商品明细")
    @GetMapping("/goods/{id}")
    @RequiresPermissions("mall:order:info")
    public RestResponse<List<MallOrderGoodsEntity>> goods(@PathVariable("id") Integer id) {
        Map<String, Object> params = new HashMap<>();
        authority.require("mall:order:info", Resource.ORDER, id);
        params.put("orderId", String.valueOf(id));
        return RestResponse.ok(mallOrderGoodsService.queryAll(params));
    }

    /**
     * 新增订单。
     *
     * @param mallOrder 订单实体
     * @return 处理结果
     */
    @Operation(summary = "新增", description = "新增")
    @SysLog("新增订单")
    @PostMapping("/save")
    @RequiresPermissions("mall:order:save")
    public RestResponse<String> save(@RequestBody MallOrderEntity mallOrder) {
        return RestResponse.fail("订单只能通过商城结算创建, 不支持通用新增");
    }

    /**
     * 修改订单。
     *
     * @param mallOrder 订单实体
     * @return 处理结果
     */
    @Operation(summary = "修改", description = "修改")
    @SysLog("修改订单")
    @PostMapping("/update")
    @RequiresPermissions("mall:order:price:update")
    public RestResponse<String> update(@RequestBody MallOrderEntity mallOrder) {
        return RestResponse.fail("订单不支持通用修改，请使用“发货”或“改价”操作");
    }

    /**
     * 订单发货。
     *
     * @param params 发货参数
     * @return 处理结果
     */
    @Operation(summary = "订单发货", description = "已支付且未发货订单执行发货")
    @SysLog("订单发货")
    @PostMapping("/ship")
    @RequiresPermissions("mall:order:ship")
    public RestResponse<String> ship(@RequestBody Map<String, Object> params) {
        Object idObj = params.get("id");
        Object shippingNoObj = params.get("shippingNo");
        Object shippingNameObj = params.get("shippingName");
        if (idObj == null) {
            return RestResponse.fail("订单ID不能为空");
        }
        if (shippingNoObj == null || String.valueOf(shippingNoObj).trim().isEmpty()) {
            return RestResponse.fail("物流单号不能为空");
        }
        if (shippingNameObj == null || String.valueOf(shippingNameObj).trim().isEmpty()) {
            return RestResponse.fail("快递公司不能为空");
        }
        Integer id;
        try {
            id = Integer.valueOf(String.valueOf(idObj));
        } catch (Exception e) {
            return RestResponse.fail("订单ID格式错误");
        }

        authority.require("mall:order:ship", Resource.ORDER, id);
        lifecycleService.ship(id, String.valueOf(shippingNameObj), String.valueOf(shippingNoObj));
        return RestResponse.ok("发货成功");
    }

    /**
     * 支付前修改订单价格。
     *
     * @param params 改价参数
     * @return 处理结果
     */
    @Operation(summary = "支付前改价", description = "仅允许未支付订单修改实付金额")
    @SysLog("支付前修改订单价格")
    @PostMapping("/adjustPrice")
    @RequiresPermissions("mall:order:price:update")
    public RestResponse<String> adjustPrice(@RequestBody Map<String, Object> params) {
        Object idObj = params.get("id");
        Object priceObj = params.get("actualPrice");
        if (idObj == null) {
            return RestResponse.fail("订单ID不能为空");
        }
        if (priceObj == null) {
            return RestResponse.fail("改价金额不能为空");
        }

        Integer id;
        BigDecimal actualPrice;
        try {
            id = Integer.valueOf(String.valueOf(idObj));
            actualPrice = new BigDecimal(String.valueOf(priceObj));
        } catch (Exception e) {
            return RestResponse.fail("参数格式错误");
        }

        authority.require("mall:order:price:update", Resource.ORDER, id);
        lifecycleService.adjustPrice(
                id,
                actualPrice,
                String.valueOf(getUserId()),
                String.valueOf(params.getOrDefault("reason", "")));
        return RestResponse.ok("改价成功");
    }

    /**
     * 删除订单。
     *
     * @param ids 主键数组
     * @return 处理结果
     */
    @Operation(summary = "删除", description = "删除")
    @SysLog("删除订单")
    @PostMapping("/delete")
    @RequiresPermissions("mall:order:delete")
    public RestResponse<String> delete(@RequestBody Integer[] ids) {
        return RestResponse.fail("订单属于交易凭证, 不支持物理删除");
    }

    /** 订单查询只使用当前查询动作的范围, 丢弃客户端携带的范围键. */
    private void applyScope(Map<String, Object> params) {
        params.remove("businessOrgNos");
        params.remove("readyToShip");
        var scope = authority.scope("mall:order:list");
        if ("toShip".equals(params.get("todo"))) {
            scope = scope.intersect(authority.scope("mall:order:ship"));
            params.put("readyToShip", true);
        }
        if (!scope.emergency()) {
            params.put("businessOrgNos", scope.organizations());
        }
    }

    /** 列表不返回完整收件资料和支付流水. */
    private MallOrderEntity listProjection(MallOrderEntity order) {
        order.setConsignee(null);
        order.setAddress(null);
        order.setMobile(null);
        order.setPayId(null);
        order.setPostscript(null);
        return order;
    }

    /** 仅有该订单的发货权限时可看到完整物流资料, 支付流水单独隐藏. */
    private MallOrderEntity detailProjection(MallOrderEntity order) {
        if (order == null) {
            return null;
        }
        order.setPayId(null);
        if (!authority.scope("mall:order:ship").permits(order.getScopeOrgNo())) {
            return listProjection(order);
        }
        return order;
    }
}
