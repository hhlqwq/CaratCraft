package com.platform.modules.app.controller;

import com.platform.annotation.LoginUser;
import com.platform.common.utils.RestResponse;
import com.platform.modules.mall.dto.FeedbackSaveRequest;
import com.platform.modules.mall.service.FeedbackApplicationService;
import com.platform.modules.wx.entity.WxUserEntity;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 本人反馈HTTP适配层, 校验与保存交由业务服务.
 *
 * @author hehailong
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/feedback")
public class AppFeedbackController {
    private final FeedbackApplicationService feedback;

    /** 提交认证用户反馈, 不接收请求中的归属字段. */
    @PostMapping("save")
    public RestResponse<String> save(
            @LoginUser WxUserEntity user, @RequestBody FeedbackSaveRequest request) {
        feedback.submit(user.getId(), user.getNickname(), request);
        return RestResponse.ok("感谢你的反馈");
    }
}
