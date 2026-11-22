package com.platform.modules.sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.utils.RestResponse;
import com.platform.common.validator.ValidatorUtils;
import com.platform.modules.sys.entity.SysUserEntity;
import com.platform.modules.sys.service.BackOfficePermissions;
import com.platform.modules.sys.service.MerchantAccountService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 提供平台超管管理商家管理员的最小接口集. */
@RestController
@RequestMapping("/platform/merchant")
@RequiresPermissions(BackOfficePermissions.MANAGE_MERCHANTS)
@RequiredArgsConstructor
public class MerchantAccountController {
    private final MerchantAccountService accounts;

    /** 分页获取商家管理员基本资料. */
    @GetMapping("/list")
    public RestResponse<Page<SysUserEntity>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return RestResponse.ok(accounts.list(page, size));
    }

    /** 创建商家管理员并交付限时凭据. */
    @PostMapping("/create")
    public RestResponse<MerchantAccountService.Issued> create(
            @RequestBody MerchantAccountService.Create request, HttpServletResponse response) {
        ValidatorUtils.validateEntity(request);
        response.setHeader("Cache-Control", "no-store");
        return RestResponse.ok(accounts.create(request));
    }

    /** 停用或恢复指定商家管理员. */
    @PostMapping("/status")
    public RestResponse<String> status(@RequestBody Status request) {
        ValidatorUtils.validateEntity(request);
        accounts.status(request.userId(), request.active());
        return RestResponse.ok();
    }

    /** 为指定商家管理员更换限时凭据. */
    @PostMapping("/reset")
    public RestResponse<MerchantAccountService.Issued> reset(
            @RequestBody Target request, HttpServletResponse response) {
        ValidatorUtils.validateEntity(request);
        response.setHeader("Cache-Control", "no-store");
        return RestResponse.ok(accounts.reset(request.userId()));
    }

    /** 标识状态操作的目标及明确的新状态. */
    public record Status(@NotBlank String userId, @NotNull Boolean active) {}

    /** 标识凭据重置的目标. */
    public record Target(@NotBlank String userId) {}
}
