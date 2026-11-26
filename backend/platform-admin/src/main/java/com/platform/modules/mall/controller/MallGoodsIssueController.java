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
import com.platform.modules.mall.entity.MallGoodsIssueEntity;
import com.platform.modules.mall.service.MallGoodsIssueService;
import com.platform.modules.sys.controller.AbstractController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.platform.datascope.StaffAuthority;

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
 * @since 2025-06-29 21:09:17
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("mall/goodsissue")
@Tag(name = "MallGoodsIssueController|")
public class MallGoodsIssueController extends AbstractController {

    private final MallGoodsIssueService mallGoodsIssueService;
    private final com.platform.modules.mall.service.GoodsQuestionService questions;

    public record BuyerQuestionDecision(Long id, String action, String answer) {}

    /** 查询本动作授权工作室的商品提问. */
    @GetMapping("/buyer-questions")
    @RequiresPermissions("mall:goodsissue:list")
    public RestResponse<Map<String, Object>> buyerQuestions(
            @RequestParam(defaultValue = "1") int page) {
        return RestResponse.ok(questions.manage(authority.scope("mall:goodsissue:list"), page));
    }

    /** 审核当前商品的回复或隐藏不宜公开的问答. */
    @PostMapping("/buyer-questions/review")
    @RequiresPermissions("mall:goodsissue:update")
    @SysLog("审核商品提问")
    public RestResponse<String> reviewBuyerQuestion(@RequestBody BuyerQuestionDecision decision) {
        questions.moderate(authority.scope("mall:goodsissue:update"), decision.id(), decision.action(), decision.answer());
        return RestResponse.ok();
    }
    private final StaffAuthority authority;

    /**
     * 查看所有列表
     *
     * @param params 查询参数
     * @return RestResponse
     */
    @Operation(summary = "查看所有列表", description = "查看所有列表")
    @GetMapping("/queryAll")
    @RequiresPermissions("mall:goodsissue:list")
    public RestResponse<List<MallGoodsIssueEntity>> queryAll(
            @RequestParam Map<String, Object> params) {
        authority.requireWorkspace("mall:goodsissue:list");
        List<MallGoodsIssueEntity> list = mallGoodsIssueService.queryAll(params);

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
    @RequiresPermissions("mall:goodsissue:list")
    public RestResponse<Page<MallGoodsIssueEntity>> list(@RequestParam Map<String, Object> params) {
        authority.requireWorkspace("mall:goodsissue:list");
        Page<MallGoodsIssueEntity> page = mallGoodsIssueService.queryPage(params);

        return RestResponse.ok(page);
    }

    /**
     * 根据主键查询详情
     *
     * @param id 主键
     * @return RestResponse
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
    @RequiresPermissions("mall:goodsissue:info")
    public RestResponse<MallGoodsIssueEntity> info(@PathVariable("id") Integer id) {
        authority.requireWorkspace("mall:goodsissue:info");
        MallGoodsIssueEntity mallGoodsIssue = mallGoodsIssueService.getById(id);

        return RestResponse.ok(mallGoodsIssue);
    }

    /**
     * 新增
     *
     * @param mallGoodsIssue mallGoodsIssue
     * @return RestResponse
     */
    @Operation(summary = "新增", description = "新增")
    @SysLog("新增")
    @PostMapping("/save")
    @RequiresPermissions("mall:goodsissue:save")
    public RestResponse<String> save(@RequestBody MallGoodsIssueEntity mallGoodsIssue) {

        authority.requireWorkspace("mall:goodsissue:save");
        mallGoodsIssueService.add(mallGoodsIssue);

        return RestResponse.ok();
    }

    /**
     * 修改
     *
     * @param mallGoodsIssue mallGoodsIssue
     * @return RestResponse
     */
    @Operation(summary = "修改", description = "修改")
    @SysLog("修改")
    @PostMapping("/update")
    @RequiresPermissions("mall:goodsissue:update")
    public RestResponse<String> update(@RequestBody MallGoodsIssueEntity mallGoodsIssue) {

        authority.requireWorkspace("mall:goodsissue:update");
        mallGoodsIssueService.update(mallGoodsIssue);

        return RestResponse.ok();
    }

    /**
     * 根据主键删除
     *
     * @param ids ids
     * @return RestResponse
     */
    @Operation(summary = "删除", description = "删除")
    @SysLog("删除")
    @PostMapping("/delete")
    @RequiresPermissions("mall:goodsissue:delete")
    public RestResponse<String> delete(@RequestBody Integer[] ids) {
        authority.requireWorkspace("mall:goodsissue:delete");
        mallGoodsIssueService.deleteBatch(ids);

        return RestResponse.ok();
    }
}
