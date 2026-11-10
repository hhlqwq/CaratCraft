package com.platform.modules.mall.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.platform.modules.mall.dto.FeedbackSaveRequest;
import com.platform.modules.mall.entity.MallFeedbackEntity;
import com.platform.modules.wx.entity.WxUserEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

/** 反馈应用服务校验、身份及初始状态回归, 不连接业务数据库. */
class FeedbackApplicationServiceTest {
    private FeedbackApplicationService service;
    private MallFeedbackService feedbacks;
    private WxUserEntity user;

    /** 每个用例独立建立持久化替身和认证身份. */
    @BeforeEach
    void setUp() {
        feedbacks = mock(MallFeedbackService.class);
        when(feedbacks.save(any())).thenReturn(true);
        service = new FeedbackApplicationService();
        ReflectionTestUtils.setField(service, "feedbackService", feedbacks);
        user = new WxUserEntity();
        user.setId(7L);
        user.setNickname("隔离用户");
    }

    /** 合法输入使用认证身份且创建为未读状态, 正文去除首尾空格. */
    @Test
    void savesAuthenticatedUnreadFeedback() {
        FeedbackSaveRequest request = valid();
        request.setContent("  希望增加礼盒包装  ");
        assertEquals(0, service.save(user, request).getCode());
        var saved = ArgumentCaptor.forClass(MallFeedbackEntity.class);
        verify(feedbacks).save(saved.capture());
        assertEquals(7, saved.getValue().getUserId());
        assertEquals("隔离用户", saved.getValue().getUserName());
        assertEquals("13800000000", saved.getValue().getMobile());
        assertEquals(3, saved.getValue().getFeedType());
        assertEquals(0, saved.getValue().getStatus());
        assertEquals("希望增加礼盒包装", saved.getValue().getContent());
    }

    /** 空请求、非法类型、非法手机号和超长正文均在持久化前拒绝. */
    @Test
    void rejectsInvalidInputBeforePersistence() {
        assertEquals(400, service.save(user, null).getCode());
        FeedbackSaveRequest request = valid();
        request.setIndex(0);
        assertEquals(400, service.save(user, request).getCode());
        request = valid();
        request.setIndex(8);
        assertEquals(400, service.save(user, request).getCode());
        request = valid();
        request.setMobile("123");
        assertEquals(400, service.save(user, request).getCode());
        request = valid();
        request.setContent(" ");
        assertEquals(400, service.save(user, request).getCode());
        request = valid();
        request.setContent("测".repeat(501));
        assertEquals(400, service.save(user, request).getCode());
        verify(feedbacks, never()).save(any());
        verifyNoInteractions(feedbacks);
    }

    /** 未认证身份不得写入反馈. */
    @Test
    void rejectsMissingIdentity() {
        assertEquals(401, service.save(null, valid()).getCode());
        assertEquals(401, service.save(new WxUserEntity(), valid()).getCode());
        verifyNoInteractions(feedbacks);
    }

    /** 持久化失败时返回可重试错误, 不伪造成功. */
    @Test
    void reportsPersistenceFailure() {
        when(feedbacks.save(any())).thenReturn(false);
        assertEquals(400, service.save(user, valid()).getCode());
    }

    /** 合成合法反馈输入, 不包含真实个人资料. */
    private FeedbackSaveRequest valid() {
        FeedbackSaveRequest request = new FeedbackSaveRequest();
        request.setMobile("13800000000");
        request.setIndex(3);
        request.setContent("希望增加礼盒包装");
        return request;
    }
}
