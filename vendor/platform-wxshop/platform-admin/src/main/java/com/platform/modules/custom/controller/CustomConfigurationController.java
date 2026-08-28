package com.platform.modules.custom.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.common.utils.RestResponse;
import com.platform.modules.custom.dto.CustomConfigurationImportRequest;
import com.platform.modules.custom.dao.CustomComponentOptionDao;
import com.platform.modules.custom.dao.CustomProductDao;
import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import com.platform.modules.custom.entity.CustomProductEntity;
import com.platform.modules.mall.service.MallGoodsService;
import com.platform.modules.mall.entity.MallGoodsEntity;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 定制商品及组件配置管理 / Custom product and component configuration administration.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("custom/configuration")
@Tag(name = "CustomConfigurationController|定制配置")
public class CustomConfigurationController {
    private final CustomProductDao customProductDao;
    private final CustomComponentOptionDao componentOptionDao;
    private final MallGoodsService mallGoodsService;

    /** 查询已配置的定制商品 / Lists configured custom products. */
    @Operation(summary = "查询定制商品配置")
    @GetMapping("products")
    @RequiresPermissions("custom:config:list")
    public RestResponse<List<CustomProductEntity>> products() {
        return RestResponse.ok(customProductDao.selectList(
                new LambdaQueryWrapper<CustomProductEntity>().orderByAsc(CustomProductEntity::getGoodsId)));
    }

    /** 查询可配置的商城商品 / Lists mall products available for configuration. */
    @Operation(summary = "查询可配置商城商品")
    @GetMapping("goods")
    @RequiresPermissions("custom:config:list")
    public RestResponse<List<MallGoodsEntity>> goods() {
        return RestResponse.ok(mallGoodsService.list(
                new LambdaQueryWrapper<MallGoodsEntity>()
                        .eq(MallGoodsEntity::getIsDelete, 0)
                        .orderByDesc(MallGoodsEntity::getId)));
    }

    /** 保存定制商品配置 / Saves a custom product configuration. */
    @Operation(summary = "保存定制商品配置")
    @PostMapping("product/save")
    @RequiresPermissions("custom:config:update")
    public RestResponse<String> saveProduct(@RequestBody CustomProductEntity product) {
        if (product == null || product.getGoodsId() == null || product.getGoodsId() <= 0) {
            return RestResponse.fail("请选择有效的商城商品");
        }
        if (mallGoodsService.getById(product.getGoodsId()) == null) {
            return RestResponse.fail("商城商品不存在");
        }
        if (isBlank(product.getTemplateCode())) {
            return RestResponse.fail("模板代码不能为空");
        }
        product.setPreviewWidth(defaultPositive(product.getPreviewWidth(), 750));
        product.setPreviewHeight(defaultPositive(product.getPreviewHeight(), 750));
        product.setEnabled(product.getEnabled() != null && product.getEnabled() == 0 ? 0 : 1);
        CustomProductEntity existed = customProductDao.selectOne(
                new LambdaQueryWrapper<CustomProductEntity>()
                        .eq(CustomProductEntity::getGoodsId, product.getGoodsId()));
        if (existed == null) {
            customProductDao.insert(product);
        } else {
            product.setId(existed.getId());
            customProductDao.updateById(product);
        }
        return RestResponse.ok();
    }

    /** 查询指定商品的组件选项 / Lists component options for a product. */
    @Operation(summary = "查询定制组件选项")
    @GetMapping("options")
    @RequiresPermissions("custom:config:list")
    public RestResponse<List<CustomComponentOptionEntity>> options(@RequestParam Integer goodsId) {
        return RestResponse.ok(componentOptionDao.selectList(
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
        if (option == null || option.getGoodsId() == null || option.getGoodsId() <= 0
                || isBlank(option.getComponentType()) || isBlank(option.getOptionCode()) || isBlank(option.getName())) {
            return RestResponse.fail("商品、组件类型、选项代码和名称不能为空");
        }
        if (mallGoodsService.getById(option.getGoodsId()) == null) {
            return RestResponse.fail("商城商品不存在");
        }
        option.setPriceDelta(option.getPriceDelta() == null ? BigDecimal.ZERO : option.getPriceDelta());
        option.setZIndex(option.getZIndex() == null ? 0 : option.getZIndex());
        option.setEnabled(option.getEnabled() != null && option.getEnabled() == 0 ? 0 : 1);
        if (option.getId() == null) {
            CustomComponentOptionEntity duplicated = componentOptionDao.selectOne(
                    new LambdaQueryWrapper<CustomComponentOptionEntity>()
                            .eq(CustomComponentOptionEntity::getGoodsId, option.getGoodsId())
                            .eq(CustomComponentOptionEntity::getComponentType, option.getComponentType())
                            .eq(CustomComponentOptionEntity::getOptionCode, option.getOptionCode()));
            if (duplicated != null) {
                return RestResponse.fail("同一商品的组件选项代码不能重复");
            }
            componentOptionDao.insert(option);
        } else if (componentOptionDao.selectById(option.getId()) == null) {
            return RestResponse.fail("组件选项不存在");
        } else {
            componentOptionDao.updateById(option);
        }
        return RestResponse.ok();
    }

    /**
     * 原子导入定制商品和全部配件 / Atomically imports a custom product and all components.
     */
    @Operation(summary = "导入完整定制商品与配件")
    @PostMapping("import")
    @RequiresPermissions("custom:config:update")
    @Transactional(rollbackFor = Exception.class)
    public RestResponse<Map<String, Object>> importConfiguration(
            @RequestBody CustomConfigurationImportRequest request) {
        if (request == null || request.getProduct() == null) {
            return RestResponse.fail("导入文件缺少 product 配置");
        }
        CustomProductEntity product = request.getProduct();
        if (product.getGoodsId() == null || product.getGoodsId() <= 0
                || mallGoodsService.getById(product.getGoodsId()) == null) {
            return RestResponse.fail("导入文件中的商城商品不存在");
        }
        if (isBlank(product.getTemplateCode())) {
            return RestResponse.fail("模板代码不能为空");
        }
        List<CustomComponentOptionEntity> options = request.getOptions();
        if (options == null || options.isEmpty()) {
            return RestResponse.fail("导入文件至少需要一个配件选项");
        }
        Set<String> uniqueCodes = new HashSet<>();
        for (CustomComponentOptionEntity option : options) {
            if (option == null || isBlank(option.getComponentType())
                    || isBlank(option.getOptionCode()) || isBlank(option.getName())) {
                return RestResponse.fail("每个配件必须包含 componentType、optionCode 和 name");
            }
            String uniqueCode = option.getComponentType().trim() + ":" + option.getOptionCode().trim();
            if (!uniqueCodes.add(uniqueCode)) {
                return RestResponse.fail("导入文件中存在重复配件代码：" + uniqueCode);
            }
        }
        normalizeAndSaveProduct(product);
        boolean replaceOptions = request.getReplaceOptions() == null || request.getReplaceOptions();
        if (replaceOptions) {
            componentOptionDao.delete(new LambdaQueryWrapper<CustomComponentOptionEntity>()
                    .eq(CustomComponentOptionEntity::getGoodsId, product.getGoodsId()));
        }
        for (CustomComponentOptionEntity option : options) {
            option.setId(null);
            option.setGoodsId(product.getGoodsId());
            normalizeOption(option);
            componentOptionDao.insert(option);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("goodsId", product.getGoodsId());
        result.put("optionCount", options.size());
        result.put("replaced", replaceOptions);
        return RestResponse.ok(result);
    }

    /** 规范化并保存商品配置 / Normalizes and saves a product configuration. */
    private void normalizeAndSaveProduct(CustomProductEntity product) {
        product.setPreviewWidth(defaultPositive(product.getPreviewWidth(), 750));
        product.setPreviewHeight(defaultPositive(product.getPreviewHeight(), 750));
        product.setEnabled(product.getEnabled() != null && product.getEnabled() == 0 ? 0 : 1);
        CustomProductEntity existed = customProductDao.selectOne(
                new LambdaQueryWrapper<CustomProductEntity>()
                        .eq(CustomProductEntity::getGoodsId, product.getGoodsId()));
        if (existed == null) {
            product.setId(null);
            customProductDao.insert(product);
        } else {
            product.setId(existed.getId());
            customProductDao.updateById(product);
        }
    }

    /** 规范化配件选项 / Normalizes a component option. */
    private void normalizeOption(CustomComponentOptionEntity option) {
        option.setComponentType(option.getComponentType().trim());
        option.setOptionCode(option.getOptionCode().trim());
        option.setName(option.getName().trim());
        option.setPriceDelta(option.getPriceDelta() == null ? BigDecimal.ZERO : option.getPriceDelta());
        option.setZIndex(option.getZIndex() == null ? 0 : option.getZIndex());
        option.setEnabled(option.getEnabled() != null && option.getEnabled() == 0 ? 0 : 1);
    }

    /** 判断文本是否为空 / Checks whether a text value is blank. */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** 返回合法的正整数或默认值 / Returns a positive value or the fallback. */
    private int defaultPositive(Integer value, int fallback) {
        return value == null || value <= 0 ? fallback : value;
    }
}
