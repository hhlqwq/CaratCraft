package com.platform.modules.app.controller;

import com.platform.annotation.LoginUser;
import com.platform.common.utils.RestResponse;
import com.platform.modules.app.request.ApiCollectToggleRequest;
import com.platform.modules.mall.entity.MallCollectEntity;
import com.platform.modules.mall.service.MallCollectService;
import com.platform.modules.wx.entity.WxUserEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 作者: @author Harmon <br>
 * 时间: 2017-08-11 08:32<br>
 *
 * <p>描述: AppController <br>
 */
@Tag(name = "会员收藏-AppCollectController")
@RestController
@RequestMapping("/app/collect")
public class AppCollectController extends AppBaseController {
    @Autowired private MallCollectService collectService;

    /** 查询当前认证用户的商品收藏. */
    @Operation(summary = "获取用户收藏")
    @PostMapping("list")
    public RestResponse<List<MallCollectEntity>> list(
            @LoginUser WxUserEntity loginUser, Integer typeId) {

        return RestResponse.ok(collectService.listOwned(loginUser.getId(), typeId));
    }

    /** 切换当前认证用户的商品收藏, 事务及归属规则由业务服务执行. */
    @Operation(summary = "添加取消收藏")
    @PostMapping("addordelete")
    public RestResponse<Map<String, Object>> addordelete(
            @LoginUser WxUserEntity loginUser, @RequestBody ApiCollectToggleRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("收藏参数不能为空");
        }
        return RestResponse.ok(
                Map.of(
                        "type",
                        collectService.toggleOwned(
                                loginUser.getId(), request.getTypeId(), request.getValueId())));
    }
}
