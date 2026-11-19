package com.platform.modules.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.platform.annotation.IgnoreAuth;
import com.platform.modules.community.CommunityService;
import com.platform.modules.wx.entity.WxUserEntity;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

/** 社区公开读取与本人操作的入口隔离回归. */
class AppCommunityControllerTest {
    /** 只有公共列表和详情放行匿名请求, 上传、发布、点赞和删除都保留认证. */
    @Test
    void onlyPublishedReadsAllowAnonymousAccess() {
        var publicMethods =
                Arrays.stream(AppCommunityController.class.getDeclaredMethods())
                        .filter(method -> method.isAnnotationPresent(IgnoreAuth.class))
                        .map(java.lang.reflect.Method::getName)
                        .sorted()
                        .toList();
        assertEquals(java.util.List.of("detail", "list"), publicMethods);
    }

    /** 删除身份只来自解析后的登录用户, 业务层再次执行归属校验. */
    @Test
    void deletionUsesAuthenticatedIdentity() {
        var service = mock(CommunityService.class);
        var controller = new AppCommunityController(service);
        var user = new WxUserEntity();
        user.setId(7L);
        controller.remove(user, 99L);
        verify(service).remove(7L, 99L);
    }
}
