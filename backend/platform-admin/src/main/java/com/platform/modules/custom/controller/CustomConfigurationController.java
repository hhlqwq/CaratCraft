package com.platform.modules.custom.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.common.utils.RestResponse;
import com.platform.modules.custom.dao.CustomComponentOptionDao;
import com.platform.modules.custom.dao.CustomProductDao;
import com.platform.modules.custom.dto.CustomConfigurationImportRequest;
import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import com.platform.modules.custom.entity.CustomProductEntity;
import com.platform.modules.mall.entity.MallGoodsEntity;
import com.platform.modules.mall.service.MallGoodsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** 定制商品及组件配置管理 / Custom product and component configuration administration. */
@RestController
@RequiredArgsConstructor
@RequestMapping("custom/configuration")
@Tag(name = "CustomConfigurationController|定制配置")
public class CustomConfigurationController {
    private final CustomProductDao customProductDao;
    private final CustomComponentOptionDao componentOptionDao;
    private final MallGoodsService mallGoodsService;

    private final com.platform.modules.custom.service.CustomConfigurationService configuration;

    /** 下载同源客户候选目录, 所有候选默认停用且不包含实测尺寸或虚构库存. */
    @GetMapping("catalog-template")
    @RequiresPermissions("custom:config:list")
    public RestResponse<Map<String, Object>> catalogTemplate(@RequestParam Integer goodsId) {
        return RestResponse.ok(
                com.platform.modules.custom.service.CustomerMaterialCatalog.template(goodsId));
    }

    /** 查询已配置的定制商品 / Lists configured custom products. */
    @Operation(summary = "查询定制商品配置")
    @GetMapping("products")
    @RequiresPermissions("custom:config:list")
    public RestResponse<List<CustomProductEntity>> products() {
        return RestResponse.ok(
                customProductDao.selectList(
                        new LambdaQueryWrapper<CustomProductEntity>()
                                .orderByAsc(CustomProductEntity::getGoodsId)));
    }

    /** 查询可配置的商城商品 / Lists mall products available for configuration. */
    @Operation(summary = "查询可配置商城商品")
    @GetMapping("goods")
    @RequiresPermissions("custom:config:list")
    public RestResponse<List<MallGoodsEntity>> goods() {
        return RestResponse.ok(
                mallGoodsService.list(
                        new LambdaQueryWrapper<MallGoodsEntity>()
                                .eq(MallGoodsEntity::getIsDelete, 0)
                                .orderByDesc(MallGoodsEntity::getId)));
    }

    /** 保存定制商品配置 / Saves a custom product configuration. */
    @Operation(summary = "保存定制商品配置")
    @PostMapping("product/save")
    @RequiresPermissions("custom:config:update")
    public RestResponse<String> saveProduct(@RequestBody CustomProductEntity product) {
        configuration.saveProduct(product);
        return RestResponse.ok();
    }

    /** 查询指定商品的组件选项 / Lists component options for a product. */
    @Operation(summary = "查询定制组件选项")
    @GetMapping("options")
    @RequiresPermissions("custom:config:list")
    public RestResponse<List<CustomComponentOptionEntity>> options(@RequestParam Integer goodsId) {
        return RestResponse.ok(
                componentOptionDao.selectList(
                        new LambdaQueryWrapper<CustomComponentOptionEntity>()
                                .eq(CustomComponentOptionEntity::getGoodsId, goodsId)
                                .orderByAsc(CustomComponentOptionEntity::getZIndex)
                                .orderByAsc(CustomComponentOptionEntity::getId)));
    }

    /** 保存组件选项 / Saves a component option. */
    @Operation(summary = "保存定制组件选项")
    @PostMapping("option/save")
    @RequiresPermissions("custom:config:update")
    public RestResponse<String> saveOption(@RequestBody CustomComponentOptionEntity option) {
        configuration.saveOption(option);
        return RestResponse.ok();
    }

    /** 删除组件选项及对应库存 / Deletes a component option and its stock row. */
    @Operation(summary = "删除定制组件选项")
    @PostMapping("option/delete")
    @RequiresPermissions("custom:config:update")
    public RestResponse<String> deleteOption(@RequestParam Long id) {
        configuration.disable(id);
        return RestResponse.ok();
    }

    /** 原子导入定制商品和全部配件 / Atomically imports a custom product and all components. */
    @Operation(summary = "导入完整定制商品与配件")
    @PostMapping("import")
    @RequiresPermissions("custom:config:update")
    public RestResponse<Map<String, Object>> importConfiguration(
            @RequestBody CustomConfigurationImportRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("缺少导入配置");
        }
        configuration.importConfiguration(
                request.getProduct(),
                request.getOptions(),
                !Boolean.FALSE.equals(request.getReplaceOptions()));
        return RestResponse.ok(
                Map.of(
                        "goodsId",
                        request.getProduct().getGoodsId(),
                        "optionCount",
                        request.getOptions().size()));
    }
}
