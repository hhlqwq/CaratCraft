package com.platform.modules.app.controller;

import com.platform.annotation.LoginUser;
import com.platform.common.utils.RestResponse;
import com.platform.modules.mall.dto.FeedbackSaveRequest;
import com.platform.modules.mall.service.FeedbackApplicationService;
import com.platform.modules.wx.entity.WxUserEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 作者: @author hehailong <br>
 * 时间: 2017-08-11 08:32<br>
 *
 * <p>描述: AppFeedbackController <br>
 */
@Tag(name = "会员反馈-AppFeedbackController")
@RestController
@RequestMapping("/app/feedback")
public class AppFeedbackController {
    private final FeedbackApplicationService feedbacks;

    /** 注入唯一反馈应用服务, 控制器只映射协议输入输出. */
    public AppFeedbackController(FeedbackApplicationService feedbacks) {
        this.feedbacks = feedbacks;
    }

    /** 提交本人意见反馈, 校验和初始状态由应用服务处理. */
    @Operation(summary = "添加反馈")
    @PostMapping("save")
    public RestResponse<String> save(
            @LoginUser WxUserEntity loginUser,
            @RequestBody(required = false) FeedbackSaveRequest request) {
        return feedbacks.save(loginUser, request);
    }
}
