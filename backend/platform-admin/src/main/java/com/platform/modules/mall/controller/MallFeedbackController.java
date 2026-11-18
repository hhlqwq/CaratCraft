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
import com.platform.modules.mall.entity.MallFeedbackEntity;
import com.platform.modules.mall.service.MallFeedbackService;
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

import java.util.List;
import java.util.Map;

/**
 * Controller
 *
 * @author hehailong
 * @since 2024-07-19 20:53:33
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("mall/feedback")
@Tag(name = "MallFeedbackController|")
public class MallFeedbackController extends AbstractController {

    private final MallFeedbackService mallFeedbackService;
    private final StaffAuthority authority;
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;

    /**
     * 查看所有列表
     *
     * @param params 查询参数
     * @return RestResponse
     */
    @Operation(summary = "查看所有列表", description = "查看所有列表")
    @GetMapping("/queryAll")
    @RequiresPermissions("mall:feedback:list")
    public RestResponse<List<MallFeedbackEntity>> queryAll(
            @RequestParam Map<String, Object> params) {
        applyScope(params);
        List<MallFeedbackEntity> list = mallFeedbackService.queryAll(params);

        return RestResponse.ok(list);
    }

    /**
     * 分页查询
     *
     * @param params 查询参数
     * @return RestResponse
     */
    @Operation(summary = "分页查询", description = "分页查询")
    @GetMapping("/list")
    @RequiresPermissions("mall:feedback:list")
    public RestResponse<Page<MallFeedbackEntity>> list(@RequestParam Map<String, Object> params) {
        applyScope(params);
        Page<MallFeedbackEntity> page = mallFeedbackService.queryPage(params);

        return RestResponse.ok(page);
    }

    /**
     * 根据主键查询详情
     *
     * @param msgId 主键
     * @return RestResponse
     */
    @Operation(
            summary = "根据主键查询详情",
            description = "根据主键查询详情",
            parameters = {
                @Parameter(
                        in = ParameterIn.PATH,
                        name = "msgId",
                        description = "主键",
                        example = "1",
                        required = true)
            })
    @GetMapping("/info/{msgId}")
    @RequiresPermissions("mall:feedback:info")
    public RestResponse<MallFeedbackEntity> info(@PathVariable("msgId") Integer msgId) {
        authority.require("mall:feedback:info", Resource.FEEDBACK, msgId);
        MallFeedbackEntity mallFeedback = mallFeedbackService.getById(msgId);

        return RestResponse.ok(mallFeedback);
    }

    /**
     * 新增
     *
     * @param mallFeedback mallFeedback
     * @return RestResponse
     */
    @Operation(summary = "新增", description = "新增")
    @SysLog("新增")
    @PostMapping("/save")
    @RequiresPermissions("mall:feedback:save")
    public RestResponse<String> save(@RequestBody MallFeedbackEntity mallFeedback) {

        mallFeedback.setMsgId(null);
        mallFeedback.setScopeOrgNo(authority.organizationForCreation("mall:feedback:save", null));
        mallFeedbackService.add(mallFeedback);

        return RestResponse.ok();
    }

    /**
     * 修改
     *
     * @param mallFeedback mallFeedback
     * @return RestResponse
     */
    @Operation(summary = "修改", description = "修改")
    @SysLog("修改")
    @PostMapping("/update")
    @RequiresPermissions("mall:feedback:update")
    public RestResponse<String> update(@RequestBody MallFeedbackEntity mallFeedback) {

        authority.require("mall:feedback:update", Resource.FEEDBACK, mallFeedback.getMsgId());
        String reply = mallFeedback.getStaffReply();
        if (reply == null || reply.isBlank() || reply.trim().length() > 500) {
            throw new IllegalArgumentException("请填写1到500字的处理记录");
        }
        jdbc.update(
                "UPDATE mall_feedback SET staff_reply=?,status=1 WHERE msg_id=?",
                reply.trim(),
                mallFeedback.getMsgId());

        return RestResponse.ok();
    }

    /**
     * 根据主键删除
     *
     * @param msgIds msgIds
     * @return RestResponse
     */
    @Operation(summary = "删除", description = "删除")
    @SysLog("删除")
    @PostMapping("/delete")
    @RequiresPermissions("mall:feedback:delete")
    public RestResponse<String> delete(@RequestBody Integer[] msgIds) {
        authority.require(
                "mall:feedback:delete", Resource.FEEDBACK, java.util.Arrays.asList(msgIds));
        mallFeedbackService.deleteBatch(msgIds);

        return RestResponse.ok();
    }

    /** 使用客服读取范围并允许筛选待处理状态. */
    private void applyScope(Map<String, Object> params) {
        params.remove("businessOrgNos");
        var scope = authority.scope("mall:feedback:list");
        if ("feedbackPending".equals(params.get("todo"))) {
            scope = scope.intersect(authority.scope("mall:feedback:update"));
            params.put("status", "0");
        }
        if (!scope.emergency()) {
            params.put("businessOrgNos", scope.organizations());
        }
    }
}
