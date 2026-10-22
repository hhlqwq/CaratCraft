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
import com.platform.modules.mall.dto.GoodsAggregateDTO;
import com.platform.modules.mall.entity.MallGoodsEntity;
import com.platform.modules.mall.service.MallGoodsAggregateService;
import com.platform.modules.mall.service.MallGoodsService;
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
 * @author 何海龙
 * @since 2026-02-24 13:15:28
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("mall/goods")
@Tag(name = "MallGoodsController|")
public class MallGoodsController extends AbstractController {

    private final MallGoodsService mallGoodsService;
    private final MallGoodsAggregateService mallGoodsAggregateService;

    /**
     * 查看所有列表
     *
     * @param params 查询参数
     * @return RestResponse
     */
    @Operation(summary = "查看所有列表", description = "查看所有列表")
    @GetMapping("/queryAll")
    @RequiresPermissions("mall:goods:list")
    public RestResponse<List<MallGoodsEntity>> queryAll(@RequestParam Map<String, Object> params) {
        List<MallGoodsEntity> list = mallGoodsService.queryAll(params);

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
    @RequiresPermissions("mall:goods:list")
    public RestResponse<Page<MallGoodsEntity>> list(@RequestParam Map<String, Object> params) {
        Page<MallGoodsEntity> page = mallGoodsService.queryPage(params);

        return RestResponse.ok(page);
    }

    /**
     * 分页查询商品回收站。
     *
     * @param params 查询参数。
     * @return 回收站分页数据。
     */
    @Operation(summary = "商品回收站", description = "查询三十天内可恢复的商品")
    @GetMapping("/recycle/list")
    @RequiresPermissions("mall:goods:recycle:list")
    public RestResponse<Page<MallGoodsEntity>> recycleList(
            @RequestParam Map<String, Object> params) {
        return RestResponse.ok(mallGoodsService.queryRecyclePage(params));
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
    @RequiresPermissions("mall:goods:info")
    public RestResponse<MallGoodsEntity> info(@PathVariable("id") Integer id) {
        MallGoodsEntity mallGoods = mallGoodsService.getById(id);

        return RestResponse.ok(mallGoods);
    }

    /** 聚合详情 */
    @Operation(summary = "商品聚合详情", description = "商品聚合详情")
    @GetMapping("/aggregate/{id}")
    @RequiresPermissions("mall:goods:info")
    public RestResponse<GoodsAggregateDTO> aggregate(@PathVariable("id") Integer id) {
        return RestResponse.ok(mallGoodsAggregateService.getAggregate(id));
    }

    /**
     * 新增
     *
     * @param mallGoods mallGoods
     * @return RestResponse
     */
    @Operation(summary = "新增", description = "新增")
    @SysLog("新增")
    @PostMapping("/save")
    @RequiresPermissions("mall:goods:save")
    public RestResponse<String> save(@RequestBody MallGoodsEntity mallGoods) {

        mallGoodsService.add(mallGoods);

        return RestResponse.ok();
    }

    /** 新增聚合 */
    @Operation(summary = "新增聚合", description = "新增聚合")
    @SysLog("新增聚合")
    @PostMapping("/aggregate/save")
    @RequiresPermissions("mall:goods:save")
    public RestResponse<Integer> saveAggregate(@RequestBody GoodsAggregateDTO aggregateDTO) {
        return RestResponse.ok(mallGoodsAggregateService.saveAggregate(aggregateDTO));
    }

    /**
     * 修改
     *
     * @param mallGoods mallGoods
     * @return RestResponse
     */
    @Operation(summary = "修改", description = "修改")
    @SysLog("修改")
    @PostMapping("/update")
    @RequiresPermissions("mall:goods:update")
    public RestResponse<String> update(@RequestBody MallGoodsEntity mallGoods) {

        mallGoodsService.update(mallGoods);

        return RestResponse.ok();
    }

    /** 修改聚合 */
    @Operation(summary = "修改聚合", description = "修改聚合")
    @SysLog("修改聚合")
    @PostMapping("/aggregate/update")
    @RequiresPermissions("mall:goods:update")
    public RestResponse<Integer> updateAggregate(@RequestBody GoodsAggregateDTO aggregateDTO) {
        return RestResponse.ok(mallGoodsAggregateService.updateAggregate(aggregateDTO));
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
    @RequiresPermissions("mall:goods:delete")
    public RestResponse<String> delete(@RequestBody Integer[] ids) {
        mallGoodsService.deleteBatch(ids);

        return RestResponse.ok("商品已移入回收站，三十天内可恢复");
    }

    /**
     * 从回收站恢复商品，恢复后保持下架状态。
     *
     * @param ids 商品 ID。
     * @return 操作结果。
     */
    @Operation(summary = "恢复商品", description = "从回收站恢复商品并保持下架")
    @SysLog("恢复回收站商品")
    @PostMapping("/recycle/restore")
    @RequiresPermissions("mall:goods:recycle:restore")
    public RestResponse<String> restore(@RequestBody Integer[] ids) {
        mallGoodsService.restoreBatch(ids);
        return RestResponse.ok("商品已恢复，请检查后手动上架");
    }

    /**
     * 永久删除回收站商品。
     *
     * @param ids 商品 ID。
     * @return 操作结果。
     */
    @Operation(summary = "永久删除商品", description = "永久删除商品，操作不可撤销")
    @SysLog("永久删除回收站商品")
    @PostMapping("/recycle/delete")
    @RequiresPermissions("mall:goods:recycle:delete")
    public RestResponse<Integer> permanentlyDelete(@RequestBody Integer[] ids) {
        int deletedCount = mallGoodsService.permanentlyDeleteBatch(ids);
        return RestResponse.ok("已永久删除 " + deletedCount + " 个商品", deletedCount);
    }
}
