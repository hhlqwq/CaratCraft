package com.platform.modules.app.controller;

import com.platform.annotation.IgnoreAuth;
import com.platform.common.utils.RestResponse;
import com.platform.modules.mall.service.PearlCatalogService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 珍珠商城双维度公共目录. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/pearl-catalog")
public class AppPearlCatalogController {
    private final PearlCatalogService catalog;

    /** 无需登录即可按来源和成品品类浏览可售商品. */
    @IgnoreAuth
    @GetMapping("list")
    public RestResponse<Object> list(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return RestResponse.ok(catalog.page(source, type, page, limit));
    }
}
