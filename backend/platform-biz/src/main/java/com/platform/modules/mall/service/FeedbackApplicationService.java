package com.platform.modules.mall.service;

import com.platform.modules.mall.dao.MallFeedbackDao;
import com.platform.modules.mall.dto.FeedbackSaveRequest;
import com.platform.modules.mall.entity.MallFeedbackEntity;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/** 反馈校验和保存使用认证归属, 不接受客户端用户身份. */
@Service
@RequiredArgsConstructor
public class FeedbackApplicationService {
    private final MallFeedbackDao feedback;

    /** 校验反馈并保存, 失败不报告成功或截断认证ID. */
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long userId, String nickname, FeedbackSaveRequest request) {
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("请先登录");
        }
        if (request == null
                || request.getIndex() == null
                || request.getIndex() < 1
                || request.getIndex() > 7
                || request.getContent() == null
                || request.getContent().isBlank()
                || request.getContent().trim().length() > 500
                || request.getMobile() == null
                || !request.getMobile().trim().matches("1[3-9][0-9]{9}")) {
            throw new IllegalArgumentException("请选择反馈类型, 填写1到500字正文和有效手机号");
        }
        var row = new MallFeedbackEntity();
        row.setUserId(userId);
        String name = nickname == null ? "" : nickname;
        row.setUserName(
                name.substring(
                        0,
                        name.offsetByCodePoints(
                                0, Math.min(60, name.codePointCount(0, name.length())))));
        row.setMobile(request.getMobile().trim());
        row.setContent(request.getContent().trim());
        row.setFeedType(request.getIndex());
        row.setStatus(0);
        row.setAddTime(new Date());
        if (feedback.insert(row) != 1) {
            throw new IllegalStateException("反馈保存失败, 请稍后核对");
        }
    }
}
