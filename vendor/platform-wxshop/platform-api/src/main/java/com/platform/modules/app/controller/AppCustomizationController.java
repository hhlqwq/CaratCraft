package com.platform.modules.app.controller;

import com.platform.annotation.IgnoreAuth;
import com.platform.annotation.LoginUser;
import com.platform.common.utils.RestResponse;
import com.platform.modules.custom.dto.CustomQuoteRequest;
import com.platform.modules.custom.service.CustomProductService;
import com.platform.modules.custom.service.CustomQuoteService;
import com.platform.modules.custom.service.CustomDesignService;
import com.platform.modules.wx.entity.WxUserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 小程序定制接口 / Mini-program customization endpoints.
 */
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
    @RequestMapping(value = "config", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<RestResponse<Object>> config(Integer goodsId) {
        try {
            return ResponseEntity.ok(RestResponse.ok(customProductService.getConfig(goodsId)));
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
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
    public ResponseEntity<RestResponse<Object>> quote(@RequestBody CustomQuoteRequest request) {
        try {
            return ResponseEntity.ok(RestResponse.ok(customQuoteService.quote(request)));
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
    }

    /** 保存当前用户的设计草稿 / Saves the current user's design draft. */
    @Operation(summary = "保存定制设计")
    @PostMapping("save")
    public ResponseEntity<RestResponse<Object>> save(@LoginUser WxUserEntity loginUser,
                                                     @RequestBody CustomQuoteRequest request) {
        try {
            return ResponseEntity.ok(RestResponse.ok(
                    customDesignService.saveDraft(loginUser.getId(), request)));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return badRequest(e.getMessage());
        }
    }

    /** 查询当前用户的设计列表 / Lists the current user's designs. */
    @Operation(summary = "查询我的定制设计")
    @PostMapping("list")
    public RestResponse<Object> list(@LoginUser WxUserEntity loginUser) {
        return RestResponse.ok(customDesignService.listOwned(loginUser.getId()));
    }

    /** 获取当前用户拥有的设计详情 / Gets an owned design snapshot. */
    @Operation(summary = "获取我的定制设计详情")
    @PostMapping("detail")
    public ResponseEntity<RestResponse<Object>> detail(@LoginUser WxUserEntity loginUser,
                                                       Long snapshotId) {
        try {
            return ResponseEntity.ok(RestResponse.ok(
                    customDesignService.getOwned(snapshotId, loginUser.getId())));
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
    }

    /** 获取结算前最新定制报价 / Gets the latest customization quote before checkout. */
    @Operation(summary = "获取定制结算报价")
    @PostMapping("checkout-quote")
    public ResponseEntity<RestResponse<Object>> checkoutQuote(
            @LoginUser WxUserEntity loginUser, Long snapshotId) {
        try {
            return ResponseEntity.ok(RestResponse.ok(
                    customDesignService.quoteOwned(snapshotId, loginUser.getId())));
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
    }

    /** 构建 HTTP 400 业务参数响应 / Builds an HTTP 400 validation response. */
    private ResponseEntity<RestResponse<Object>> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(RestResponse.fail(HttpStatus.BAD_REQUEST.value(), message));
    }
}
