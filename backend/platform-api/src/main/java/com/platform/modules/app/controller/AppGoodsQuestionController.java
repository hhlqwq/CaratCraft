package com.platform.modules.app.controller;

import com.platform.common.utils.RestResponse;
import com.platform.annotation.IgnoreAuth;
import com.platform.annotation.LoginUser;
import com.platform.modules.mall.service.GoodsQuestionService;
import com.platform.modules.wx.entity.WxUserEntity;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 商城读公开问答, 登录用户提交问题待商家审核. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/goods/questions")
public class AppGoodsQuestionController {
    private final GoodsQuestionService questions;

    public record Question(Integer goodsId, String question) {}

    /** 游客也可浏览已审核问答. */
    @IgnoreAuth
    @GetMapping
    public RestResponse<Map<String, Object>> list(
            @RequestParam Integer goodsId, @RequestParam(defaultValue = "1") int page) {
        return RestResponse.ok(questions.published(goodsId, page));
    }

    /** 留言人只来自认证会话. */
    @PostMapping
    public RestResponse<Void> ask(@LoginUser WxUserEntity user, @RequestBody Question request) {
        questions.ask(user.getId(), request.goodsId(), request.question());
        return RestResponse.ok();
    }
}
