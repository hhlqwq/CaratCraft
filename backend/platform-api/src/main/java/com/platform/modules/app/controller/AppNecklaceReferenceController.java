package com.platform.modules.app.controller;

import com.platform.annotation.LoginUser;
import com.platform.common.utils.RestResponse;
import com.platform.modules.custom.dto.NecklaceReferenceRequest;
import com.platform.modules.custom.service.NecklaceReferenceService;
import com.platform.modules.wx.entity.WxUserEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 首版规则参考接口, 身份由认证上下文提供, 业务规则集中在platform-biz. */
@RestController
@RequestMapping("/app/customization")
public class AppNecklaceReferenceController {
    private final NecklaceReferenceService service;

    /** 注入唯一规则参考服务. */
    public AppNecklaceReferenceController(NecklaceReferenceService service) {
        this.service = service;
    }

    /** 参考不改变设计或库存, 输入异常交由统一异常处理返回业务码及HTTP状态. */
    @PostMapping("reference")
    public RestResponse<NecklaceReferenceService.Reference> reference(
            @LoginUser WxUserEntity user, @RequestBody NecklaceReferenceRequest request) {
        return RestResponse.ok(service.reference(user.getId(), request));
    }
}
