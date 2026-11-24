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
import com.platform.modules.mall.entity.MallCommentEntity;
import com.platform.modules.mall.service.MallCommentService;
import com.platform.modules.sys.controller.AbstractController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.platform.datascope.StaffAuthority;
import com.platform.datascope.StaffAuthority.Resource;

import lombok.RequiredArgsConstructor;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论Controller
 *
 * @author hehailong
 * @email 939961241@qq.com
 * @date 2026-03-15 13:48:40
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("mall/comment")
@Tag(name = "MallCommentController|评论")
public class MallCommentController extends AbstractController {

    private final MallCommentService mallCommentService;
    private final StaffAuthority authority;

    /** 按给定条件读取当前领域全部匹配记录. */
    @Operation(summary = "查看所有列表", description = "查看所有列表")
    @GetMapping("/queryAll")
    @RequiresPermissions("mall:comment:list")
    public RestResponse<List<MallCommentEntity>> queryAll(
            @RequestParam Map<String, Object> params) {
        return RestResponse.ok(mallCommentService.queryAll(scoped(params, "mall:comment:list")));
    }

    /** 按当前接口参数查询对应的业务列表. */
    @Operation(summary = "分页查询", description = "分页查询")
    @GetMapping("/list")
    @RequiresPermissions("mall:comment:list")
    public RestResponse<Page<MallCommentEntity>> list(@RequestParam Map<String, Object> params) {
        return RestResponse.ok(mallCommentService.queryPage(scoped(params, "mall:comment:list")));
    }

    /** 按标识读取当前接口对应的记录详情. */
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
    @RequiresPermissions("mall:comment:info")
    public RestResponse<MallCommentEntity> info(@PathVariable("id") Integer id) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", String.valueOf(id));
        return RestResponse.ok(findInScope(id, "mall:comment:info"));
    }

    /** 接收当前接口的新增请求并委托业务服务保存. */
    @Operation(summary = "新增", description = "新增")
    @SysLog("新增评论")
    @PostMapping("/save")
    @RequiresPermissions("mall:comment:save")
    public RestResponse<String> save(@RequestBody MallCommentEntity mallComment) {
        requireProduct(mallComment, "mall:comment:save");
        mallCommentService.add(mallComment);
        return RestResponse.ok();
    }

    /** 接收当前接口的修改请求并委托业务服务更新. */
    @Operation(summary = "修改", description = "修改")
    @SysLog("修改评论")
    @PostMapping("/update")
    @RequiresPermissions("mall:comment:update")
    public RestResponse<String> update(@RequestBody MallCommentEntity mallComment) {
        findInScope(mallComment.getId(), "mall:comment:update");
        requireProduct(mallComment, "mall:comment:update");
        mallCommentService.update(mallComment);
        return RestResponse.ok();
    }

    /** 按当前接口身份和参数删除目标业务记录. */
    @Operation(summary = "删除", description = "删除")
    @SysLog("删除评论")
    @PostMapping("/delete")
    @RequiresPermissions("mall:comment:delete")
    public RestResponse<String> delete(@RequestBody Integer[] ids) {
        for (Integer id : ids) {
            findInScope(id, "mall:comment:delete");
        }
        mallCommentService.deleteBatch(ids);
        return RestResponse.ok();
    }
    /** 使用服务端已授权的商品归属范围, 消费者侧查询无关. */
    private Map<String, Object> scoped(Map<String, Object> params, String action) {
        params.put("commentOrgNos", authority.scope(action).organizations());
        return params;
    }

    /** 旧专题评论和无归属评价不进入商家数据. */
    private MallCommentEntity findInScope(Integer id, String action) {
        if (id == null || id <= 0) {
            throw new UnauthorizedException("评价不在当前工作室范围内");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        return mallCommentService.queryAll(scoped(params, action)).stream().findFirst()
                .orElseThrow(() -> new UnauthorizedException("评价不在当前工作室范围内"));
    }

    /** 写入时校验目标商品和评价类型, 不允许越权重关联. */
    private void requireProduct(MallCommentEntity comment, String action) {
        if (!Integer.valueOf(0).equals(comment.getTypeId()) || comment.getValueId() == null) {
            throw new UnauthorizedException("只能管理本工作室商品的评价");
        }
        authority.require(action, Resource.GOODS, comment.getValueId());
    }
}
