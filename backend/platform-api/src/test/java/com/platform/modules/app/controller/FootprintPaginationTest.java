package com.platform.modules.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.platform.modules.mall.entity.MallFootprintEntity;
import com.platform.modules.mall.service.MallFootprintService;
import com.platform.modules.wx.entity.WxUserEntity;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

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
                                                && query.get("offset").equals(5)
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

    /** 本人足迹列表使用真实页码偏移并通过总数判断是否还有下一页. */
    @Test
    void listsOwnedFootprintsWithPageOffset() {
        MallFootprintService footprints = mock(MallFootprintService.class);
        MallFootprintEntity row = new MallFootprintEntity();
        row.setId(2);
        row.setGoodsId(9);
        row.setAddTime(1762560000L);
        when(footprints.queryListFootprint("7", 100, 100)).thenReturn(List.of(row));
        when(footprints.countListFootprint("7")).thenReturn(250);
        var response = controller(footprints).list(user(), Map.of("page", "2", "limit", "100"));
        assertEquals(0, response.getCode());
        assertEquals(250, ((Number) response.getData().get("count")).intValue());
        assertEquals(Boolean.TRUE, response.getData().get("hasMore"));
        assertEquals(1, ((List<?>) response.getData().get("data")).size());
        verify(footprints).queryListFootprint("7", 100, 100);
    }

    /** 非法页码和超过上限的每页条数在查询前拒绝. */
    @Test
    void rejectsInvalidOwnedListBounds() {
        MallFootprintService footprints = mock(MallFootprintService.class);
        AppFootprintController controller = controller(footprints);
        assertEquals(400, controller.list(user(), Map.of("page", "0")).getCode());
        assertEquals(400, controller.list(user(), Map.of("limit", "101")).getCode());
        assertEquals(400, controller.list(user(), Map.of("page", "abc", "limit", "20")).getCode());
        verifyNoInteractions(footprints);
    }

    /** 删除他人足迹不读取或删除同商品本人记录, 明确返回不存在. */
    @Test
    void refusesForeignFootprintDeletion() {
        MallFootprintService footprints = mock(MallFootprintService.class);
        MallFootprintEntity foreign = new MallFootprintEntity();
        foreign.setId(12);
        foreign.setUserId(8L);
        foreign.setGoodsId(9);
        when(footprints.getById(12)).thenReturn(foreign);
        assertEquals(404, controller(footprints).delete(user(), 12).getCode());
        verify(footprints, never()).remove(any());
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
