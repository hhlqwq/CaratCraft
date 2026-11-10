package com.platform.modules.mall.service;

import com.platform.common.utils.RestResponse;
import com.platform.modules.mall.dto.FeedbackSaveRequest;
import com.platform.modules.mall.entity.MallFeedbackEntity;
import com.platform.modules.wx.entity.WxUserEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/** 意见反馈应用编排, 输入校验、身份归属及初始状态不分散于Controller. */
@Service
public class FeedbackApplicationService {
    private static final int MIN_FEEDBACK_TYPE = 1;
    private static final int MAX_FEEDBACK_TYPE = 7;
    private static final int MAX_CONTENT_LENGTH = 500;
    private static final int MAX_USER_NAME_LENGTH = 60;

    @Autowired private MallFeedbackService feedbackService;

    /** 校验反馈类型、正文和联系方式, 使用认证用户写入待处理反馈. */
    public RestResponse<String> save(WxUserEntity loginUser, FeedbackSaveRequest request) {
        if (loginUser == null || loginUser.getId() == null) {
            return RestResponse.fail(401, "请先登录后再反馈");
        }
        if (request == null) {
            return RestResponse.fail("请填写反馈内容和联系方式");
        }
        String mobile = request.getMobile() == null ? "" : request.getMobile().trim();
        String content = request.getContent() == null ? "" : request.getContent().trim();
        Integer type = request.getIndex();
        if (type == null || type < MIN_FEEDBACK_TYPE || type > MAX_FEEDBACK_TYPE) {
            return RestResponse.fail("请选择有效的反馈类型");
        }
        if (content.isEmpty() || content.length() > MAX_CONTENT_LENGTH) {
            return RestResponse.fail("请输入1到500字反馈内容");
        }
        if (!mobile.matches("1[3-9][0-9]{9}")) {
            return RestResponse.fail("请输入有效的手机号码");
        }
        String nickname = loginUser.getNickname() == null ? "" : loginUser.getNickname().trim();
        if (nickname.length() > MAX_USER_NAME_LENGTH) {
            nickname = nickname.substring(0, MAX_USER_NAME_LENGTH);
        }
        MallFeedbackEntity entity = new MallFeedbackEntity();
        entity.setUserId(loginUser.getId().intValue());
        entity.setUserName(nickname);
        entity.setMobile(mobile);
        entity.setFeedType(type);
        entity.setStatus(0);
        entity.setContent(content);
        entity.setAddTime(new Date());
        if (!feedbackService.save(entity)) {
            return RestResponse.fail("反馈提交失败, 请稍后重试");
        }
        return RestResponse.ok("感谢你的反馈");
    }
}
