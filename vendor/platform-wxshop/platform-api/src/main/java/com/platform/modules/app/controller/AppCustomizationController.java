package com.platform.modules.app.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.annotation.IgnoreAuth;
import com.platform.common.utils.RestResponse;
import com.platform.modules.custom.dao.CustomComponentOptionDao;
import com.platform.modules.custom.dao.CustomProductDao;
import com.platform.modules.custom.dto.CustomQuoteRequest;
import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import com.platform.modules.custom.entity.CustomProductEntity;
import com.platform.modules.custom.service.CustomQuoteService;
import com.platform.modules.custom.service.CustomDesignService;
import com.platform.modules.mall.entity.MallGoodsEntity;
import com.platform.modules.mall.service.MallGoodsService;
import com.platform.annotation.LoginUser;
import com.platform.modules.wx.entity.WxUserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 小程序定制接口 / Mini-program customization endpoints.
 */
@Tag(name = "CaratCraft定制")
@RestController
@RequestMapping("/app/customization")
public class AppCustomizationController {
    private final CustomProductDao customProductDao;
    private final CustomComponentOptionDao componentOptionDao;
    private final CustomQuoteService customQuoteService;
    private final CustomDesignService customDesignService;
    private final MallGoodsService mallGoodsService;

    public AppCustomizationController(CustomProductDao customProductDao,
                                     CustomComponentOptionDao componentOptionDao,
                                     CustomQuoteService customQuoteService,
                                     CustomDesignService customDesignService,
                                     MallGoodsService mallGoodsService) {
        this.customProductDao = customProductDao;
        this.componentOptionDao = componentOptionDao;
        this.customQuoteService = customQuoteService;
        this.customDesignService = customDesignService;
        this.mallGoodsService = mallGoodsService;
    }

    /**
     * 获取定制器配置 / Gets customizer configuration.
     *
     * @param goodsId 商城商品ID / mall goods id
     * @return 模板和可选组件 / template and selectable components
     */
    @IgnoreAuth
    @Operation(summary = "获取定制器配置")
    @PostMapping("config")
    public RestResponse<Object> config(Integer goodsId) {
        CustomProductEntity product = customProductDao.selectOne(
                new LambdaQueryWrapper<CustomProductEntity>()
                        .eq(CustomProductEntity::getGoodsId, goodsId)
                        .eq(CustomProductEntity::getEnabled, 1));
        if (product == null) {
            return RestResponse.fail("该商品暂未开放定制");
        }
        List<CustomComponentOptionEntity> options = componentOptionDao.selectList(
                new LambdaQueryWrapper<CustomComponentOptionEntity>()
                        .eq(CustomComponentOptionEntity::getGoodsId, goodsId)
                        .eq(CustomComponentOptionEntity::getEnabled, 1)
                        .orderByAsc(CustomComponentOptionEntity::getZIndex));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("product", product);
        result.put("options", options);
        MallGoodsEntity goods = mallGoodsService.getById(goodsId);
        result.put("primaryProductId", goods == null ? null : goods.getPrimaryProductId());
        return RestResponse.ok(result);
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
        try {
            return RestResponse.ok(customQuoteService.quote(request));
        } catch (IllegalArgumentException exception) {
            return RestResponse.fail(exception.getMessage());
        }
    }

    /** 保存当前用户的设计草稿 / Saves the current user's design draft. */
    @Operation(summary = "保存定制设计")
    @PostMapping("save")
    public RestResponse<Object> save(@LoginUser WxUserEntity loginUser,
                                     @RequestBody CustomQuoteRequest request) {
        try {
            return RestResponse.ok(customDesignService.saveDraft(loginUser.getId(), request));
        } catch (IllegalArgumentException exception) {
            return RestResponse.fail(exception.getMessage());
        }
    }

    /** 查询当前用户的设计列表 / Lists the current user's designs. */
    @Operation(summary = "查询我的定制设计")
    @PostMapping("list")
    public RestResponse<Object> list(@LoginUser WxUserEntity loginUser) {
        return RestResponse.ok(customDesignService.listOwned(loginUser.getId()));
    }
}
