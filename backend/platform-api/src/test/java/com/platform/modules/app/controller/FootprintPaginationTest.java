package com.platform.modules.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.platform.modules.mall.service.MallFootprintService;
import com.platform.modules.wx.entity.WxUserEntity;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** 分享足迹分页不得重复读取第一页, 不允许越界或偏移溢出. */
class FootprintPaginationTest {
    /** 实际Query必须保留当前页、条数、偏移和认证归属. */
    @Test
    void propagatesPageAndOwner() {
        MallFootprintService footprints = mock(MallFootprintService.class);
        AppFootprintController controller = controller(footprints);
        WxUserEntity user = user();
        assertEquals(0, controller.sharelist(user, 2, 5).getCode());
        verify(footprints)
                .shareList(
                        argThat(
                                query ->
                                        query.get("page").equals(2)
                                                && query.get("limit").equals(5)
                                                && query.get("offset") instanceof Number offset
                                                && offset.longValue() == 5L
                                                && query.get("referrer").equals(7L)));
    }

    /** 无效或无限制分页在查询前失败. */
    @Test
    void rejectsInvalidBounds() {
        MallFootprintService footprints = mock(MallFootprintService.class);
        AppFootprintController controller = controller(footprints);
        assertEquals(400, controller.sharelist(user(), null, 10).getCode());
        assertEquals(400, controller.sharelist(user(), 0, 10).getCode());
        assertEquals(400, controller.sharelist(user(), 1, 101).getCode());
        verifyNoInteractions(footprints);
    }

    /** 整数最大页码不得形成负偏移. */
    @Test
    void rejectsOffsetOverflow() {
        MallFootprintService footprints = mock(MallFootprintService.class);
        assertEquals(
                400, controller(footprints).sharelist(user(), Integer.MAX_VALUE, 100).getCode());
        verifyNoInteractions(footprints);
    }

    /** 构造只使用隔离持久化边界的入口. */
    private AppFootprintController controller(MallFootprintService footprints) {
        var controller = new AppFootprintController();
        ReflectionTestUtils.setField(controller, "footprintService", footprints);
        return controller;
    }

    /** 使用明确认证ID, 不读取真实用户资料. */
    private WxUserEntity user() {
        var user = new WxUserEntity();
        user.setId(7L);
        return user;
    }
}
