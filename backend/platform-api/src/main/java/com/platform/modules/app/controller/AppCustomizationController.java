package com.platform.modules.app.controller;

import com.platform.annotation.IgnoreAuth;
import com.platform.annotation.LoginUser;
import com.platform.common.utils.RestResponse;
import com.platform.modules.custom.dto.CustomQuoteRequest;
import com.platform.modules.custom.service.CustomDesignService;
import com.platform.modules.custom.service.CustomProductService;
import com.platform.modules.custom.service.CustomQuoteService;
import com.platform.modules.wx.entity.WxUserEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 小程序定制接口 / Mini-program customization endpoints. */
@Tag(name = "CaratCraft定制")
@RestController
@RequestMapping("/app/customization")
@RequiredArgsConstructor
public class AppCustomizationController {
    private final CustomProductService customProductService;
    private final CustomQuoteService customQuoteService;
    private final CustomDesignService customDesignService;

    /**
     * 获取定制器配置 / Gets customizer configuration.
     *
     * @param goodsId 商城商品ID / mall goods id
     * @return 模板和可选组件 / template and selectable components
     */
    @IgnoreAuth
    @Operation(summary = "获取定制器配置")
    @GetMapping("config")
    public RestResponse<Object> config(Integer goodsId) {
        return RestResponse.ok(customProductService.getConfig(goodsId));
    }

    /**
     * 服务端重新报价 / Requotes customization on the server.
     *
     * @param request 用户配置 / customer configuration
     * @return 可信报价 / trusted quote
     */
    @IgnoreAuth
    @Operation(summary = "定制报价")
    @PostMapping("quote")
    public RestResponse<Object> quote(@RequestBody CustomQuoteRequest request) {
        return RestResponse.ok(customQuoteService.quote(request));
    }

    /** 提供当前店铺可选的基础项链款式. */
    @IgnoreAuth
    @Operation(summary = "查看可定制项链款式")
    @GetMapping("styles")
    public RestResponse<Object> styles() {
        return RestResponse.ok(customProductService.listNecklaceStyles());
    }

    /** 保存当前用户的设计草稿 / Saves the current user's design draft. */
    @Operation(summary = "保存定制设计")
    @PostMapping("save")
    public RestResponse<Object> save(
            @LoginUser WxUserEntity loginUser, @RequestBody CustomQuoteRequest request) {
        return RestResponse.ok(customDesignService.saveDraft(loginUser.getId(), request));
    }

    /** 查询当前用户的设计列表 / Lists the current user's designs. */
    @Operation(summary = "查询我的定制设计")
    @PostMapping("list")
    public RestResponse<Object> list(
            @LoginUser WxUserEntity loginUser,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int limit) {
        return RestResponse.ok(
                customDesignService.listOwnedWithOrders(loginUser.getId(), page, limit));
    }

    /** 归档草稿须匹配本人及当前版本, 不删除下单历史. */
    @PostMapping("archive")
    public RestResponse<Object> archive(
            @LoginUser WxUserEntity user, Long snapshotId, Long version) {
        customDesignService.archive(snapshotId, user.getId(), version);
        return RestResponse.ok();
    }

    /** 获取当前用户拥有的设计详情 / Gets an owned design snapshot. */
    @Operation(summary = "获取我的定制设计详情")
    @PostMapping("detail")
    public RestResponse<Object> detail(@LoginUser WxUserEntity loginUser, Long snapshotId) {
        return RestResponse.ok(customDesignService.getOwned(snapshotId, loginUser.getId()));
    }

    /** 获取结算前最新定制报价 / Gets the latest customization quote before checkout. */
    @Operation(summary = "获取定制结算报价")
    @PostMapping("checkout-quote")
    public RestResponse<Object> checkoutQuote(@LoginUser WxUserEntity loginUser, Long snapshotId) {
        return RestResponse.ok(customDesignService.quoteOwned(snapshotId, loginUser.getId()));
    }
}
