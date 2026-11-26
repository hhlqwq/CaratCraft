package com.platform.modules.mall.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.platform.modules.mall.dao.MallFeedbackDao;
import com.platform.modules.mall.dto.FeedbackSaveRequest;
import com.platform.modules.mall.entity.MallFeedbackEntity;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** 反馈真实业务规则回归, 持久化使用可观察替身. */
class FeedbackApplicationServiceTest {
    /** 使用有效反馈构造独立请求, 避免多个用例共享可变输入. */
    private FeedbackSaveRequest request() {
        var request = new FeedbackSaveRequest();
        request.setIndex(1);
        request.setContent("  建议完善说明  ");
        request.setMobile("13800000000");
        return request;
    }

    /** 非法输入不落库, 不依赖小程序先行校验. */
    @Test
    void rejectsInvalidInputBeforeInsert() {
        var dao = mock(MallFeedbackDao.class);
        var service = new FeedbackApplicationService(dao);
        assertThrows(IllegalArgumentException.class, () -> service.submit(7L, "用户", null));
        for (int type : new int[] {0, 8}) {
            var request = request();
            request.setIndex(type);
            assertThrows(IllegalArgumentException.class, () -> service.submit(7L, "用户", request));
        }
        var request = request();
        request.setContent(" ");
        assertThrows(IllegalArgumentException.class, () -> service.submit(7L, "用户", request));
        request.setContent("文".repeat(501));
        assertThrows(IllegalArgumentException.class, () -> service.submit(7L, "用户", request));
        request.setContent("有效");
        request.setMobile("123");
        assertThrows(IllegalArgumentException.class, () -> service.submit(7L, "用户", request));
        verifyNoInteractions(dao);
    }

    /** 认证大ID不截断, 昵称截取保持完整Unicode字符, 正文按契约去除边界空白. */
    @Test
    void preservesLongOwnerAndUnicodeNickname() {
        var dao = mock(MallFeedbackDao.class);
        when(dao.insert(any(MallFeedbackEntity.class))).thenReturn(1);
        new FeedbackApplicationService(dao).submit(20_000_000L, "😀".repeat(70), request());
        var captured = ArgumentCaptor.forClass(MallFeedbackEntity.class);
        verify(dao).insert(captured.capture());
        var row = captured.getValue();
        assertEquals(20_000_000L, row.getUserId());
        assertEquals("😀".repeat(60), row.getUserName());
        assertEquals("建议完善说明", row.getContent());
        assertEquals(0, row.getStatus());
    }

    /** 未填写联系电话时使用数据库现有的空字符串表示. */
    @Test
    void acceptsFeedbackWithoutPhone() {
        var dao = mock(MallFeedbackDao.class);
        when(dao.insert(any(MallFeedbackEntity.class))).thenReturn(1);
        var request = request();
        request.setMobile(null);
        new FeedbackApplicationService(dao).submit(7L, "用户", request);
        var captured = ArgumentCaptor.forClass(MallFeedbackEntity.class);
        verify(dao).insert(captured.capture());
        assertEquals("", captured.getValue().getMobile());
    }

    /** 写入失败不向用户报告成功. */
    @Test
    void failedInsertDoesNotReportSuccess() {
        var dao = mock(MallFeedbackDao.class);
        assertThrows(
                IllegalStateException.class,
                () -> new FeedbackApplicationService(dao).submit(7L, null, request()));
    }
}
