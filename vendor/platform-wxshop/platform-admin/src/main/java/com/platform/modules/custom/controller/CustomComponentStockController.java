package com.platform.modules.custom.controller;

import com.platform.common.utils.RestResponse;
import com.platform.modules.custom.dto.ComponentStockView;
import com.platform.modules.custom.service.CustomStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 组件选项库存维护 / Component option stock administration.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("custom/component-stock")
@Tag(name = "CustomComponentStockController|组件库存")
public class CustomComponentStockController {
    private final CustomStockService customStockService;

    /** 查询组件选项库存列表 / Lists component option stock. */
    @Operation(summary = "查询组件库存")
    @GetMapping("list")
    @RequiresPermissions("custom:stock:list")
    public RestResponse<List<ComponentStockView>> list(
            @RequestParam(required = false) Integer goodsId) {
        return RestResponse.ok(customStockService.listStock(goodsId));
    }

    /** 更新组件选项库存 / Updates stock for a component option. */
    @Operation(summary = "更新组件库存")
    @PostMapping("update")
    @RequiresPermissions("custom:stock:update")
    public RestResponse<String> update(Long optionId, Integer stock) {
        try {
            customStockService.updateStock(optionId, stock);
            return RestResponse.ok("更新成功");
        } catch (IllegalArgumentException e) {
            return RestResponse.fail(e.getMessage());
        }
    }
}
