package com.platform.modules.custom.controller;

import com.platform.common.utils.RestResponse;
import com.platform.datascope.StaffAuthority;
import com.platform.datascope.StaffAuthority.Resource;
import com.platform.modules.custom.dto.CustomConfigurationImportRequest;
import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import com.platform.modules.custom.entity.CustomProductEntity;

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

import java.util.Map;

/** 定制商品及组件配置管理 / Custom product and component configuration administration. */
@RestController
@RequiredArgsConstructor
@RequestMapping("custom/configuration")
@Tag(name = "CustomConfigurationController|定制配置")
public class CustomConfigurationController {
    private final StaffAuthority authority;
    private final com.platform.modules.custom.dao.CustomComponentOptionDao componentOptionDao;
    private final com.platform.modules.custom.service.ConfigurationQueryService queries;
    private final com.platform.modules.custom.service.CustomConfigurationService configuration;

    /** 下载同源客户候选目录, 所有候选默认停用且不包含实测尺寸或虚构库存. */
    @GetMapping("catalog-template")
    @RequiresPermissions("custom:config:list")
    public RestResponse<Map<String, Object>> catalogTemplate(@RequestParam Integer goodsId) {
        authority.require("custom:config:list", Resource.GOODS, goodsId);
        return RestResponse.ok(
                com.platform.modules.custom.service.CustomerMaterialCatalog.template(goodsId));
    }

    /** 查询已配置的定制商品 / Lists configured custom products. */
    @Operation(summary = "查询定制商品配置")
    @GetMapping("products")
    @RequiresPermissions("custom:config:list")
    public RestResponse<Map<String, Object>> products(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return RestResponse.ok(queries.products(page, size, authority.scope("custom:config:list")));
    }

    /** 查询可配置的商城商品 / Lists mall products available for configuration. */
    @Operation(summary = "查询可配置商城商品")
    @GetMapping("goods")
    @RequiresPermissions("custom:config:list")
    public RestResponse<Map<String, Object>> goods(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return RestResponse.ok(
                queries.goods(page, size, keyword, authority.scope("custom:config:list")));
    }

    /** 保存定制商品配置 / Saves a custom product configuration. */
    @Operation(summary = "保存定制商品配置")
    @PostMapping("product/save")
    @RequiresPermissions("custom:config:update")
    public RestResponse<String> saveProduct(@RequestBody CustomProductEntity product) {
        authority.require("custom:config:update", Resource.GOODS, product.getGoodsId());
        configuration.saveProduct(product);
        return RestResponse.ok();
    }

    /** 查询指定商品的组件选项 / Lists component options for a product. */
    @Operation(summary = "查询定制组件选项")
    @GetMapping("options")
    @RequiresPermissions("custom:config:list")
    public RestResponse<Map<String, Object>> options(
            @RequestParam Integer goodsId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        authority.require("custom:config:list", Resource.GOODS, goodsId);
        return RestResponse.ok(queries.options(goodsId, page, size, keyword));
    }

    /** 保存组件选项 / Saves a component option. */
    @Operation(summary = "保存定制组件选项")
    @PostMapping("option/save")
    @RequiresPermissions("custom:config:update")
    public RestResponse<String> saveOption(@RequestBody CustomComponentOptionEntity option) {
        authority.require("custom:config:update", Resource.GOODS, option.getGoodsId());
        if (option.getId() != null) {
            var existing = componentOptionDao.selectById(option.getId());
            authority.require(
                    "custom:config:update",
                    Resource.GOODS,
                    existing == null ? null : existing.getGoodsId());
        }
        configuration.saveOption(option);
        return RestResponse.ok();
    }

    /** 停用组件选项, 保留库存及历史引用. */
    @Operation(summary = "停用定制组件选项")
    @PostMapping("option/disable")
    @RequiresPermissions("custom:config:update")
    public RestResponse<String> disableOption(@RequestParam Long id) {
        var option = componentOptionDao.selectById(id);
        authority.require(
                "custom:config:update",
                Resource.GOODS,
                option == null ? null : option.getGoodsId());
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
        if (request.getProduct() == null) {
            throw new IllegalArgumentException("缺少定制商品");
        }
        authority.require(
                "custom:config:update", Resource.GOODS, request.getProduct().getGoodsId());
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
