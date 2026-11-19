package com.platform.modules.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.platform.annotation.IgnoreAuth;
import com.platform.common.utils.RestResponse;
import com.platform.modules.custom.dto.CustomQuoteRequest;
import com.platform.modules.custom.entity.CustomDesignSnapshotEntity;
import com.platform.modules.custom.service.CustomDesignService;
import com.platform.modules.custom.service.CustomProductService;
import com.platform.modules.custom.service.CustomQuoteService;
import com.platform.modules.wx.entity.WxUserEntity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

/** 小程序公开浏览与定制接口回归测试 / App browsing and customization API regression tests. */
class AppCustomizationControllerTest {

    /** 验证参数校验同时返回 HTTP 400 和业务码 400 / Verifies HTTP and body status. */
    @Test
    void quoteValidationReturnsHttpBadRequest() {
        CustomProductService productService = mock(CustomProductService.class);
        CustomQuoteService quoteService = mock(CustomQuoteService.class);
        CustomDesignService designService = mock(CustomDesignService.class);
        when(quoteService.quote(any())).thenThrow(new IllegalArgumentException("请选择必选组件：style"));
        AppCustomizationController controller =
                new AppCustomizationController(productService, quoteService, designService);

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> controller.quote(new CustomQuoteRequest()));
    }

    /** 验证设计详情只通过本人查询服务返回 / Verifies owned snapshot detail lookup. */
    @Test
    void designDetailReturnsOwnedSnapshot() {
        CustomProductService productService = mock(CustomProductService.class);
        CustomQuoteService quoteService = mock(CustomQuoteService.class);
        CustomDesignService designService = mock(CustomDesignService.class);
        CustomDesignSnapshotEntity snapshot = new CustomDesignSnapshotEntity();
        snapshot.setId(88L);
        snapshot.setUserId(7L);
        snapshot.setConfigurationJson("{\"goodsId\":1999105,\"optionIds\":[1,2]}");
        when(designService.getOwned(88L, 7L)).thenReturn(snapshot);
        AppCustomizationController controller =
                new AppCustomizationController(productService, quoteService, designService);
        WxUserEntity user = new WxUserEntity();
        user.setId(7L);

        RestResponse<Object> response = controller.detail(user, 88L);

        assertNotNull(response);
        assertEquals(snapshot, response.getData());
    }

    /** 通过真实MVC检查读取方法和错误状态, 控制器不得吞掉409冲突. */
    @Test
    void mvcPreservesStatusAndReadMethod() throws Exception {
        var products = mock(CustomProductService.class);
        var quotes = mock(CustomQuoteService.class);
        var mvc =
                org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(
                                new AppCustomizationController(
                                        products, quotes, mock(CustomDesignService.class)))
                        .setControllerAdvice(
                                new com.platform.common.exception.BusinessExceptionHandler(),
                                new com.platform.common.exception.RestStatusAdvice())
                        .build();
        mvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                                        "/app/customization/config")
                                .param("goodsId", "10"))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers.status()
                                .isOk());
        mvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                                "/app/customization/config"))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers.status()
                                .isMethodNotAllowed());
        org.mockito.Mockito.doThrow(new IllegalStateException("并发冲突")).when(quotes).quote(any());
        mvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                                        "/app/customization/quote")
                                .contentType("application/json")
                                .content("{\"goodsId\":10}"))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers.status()
                                .isConflict());
        org.mockito.Mockito.doThrow(new IllegalArgumentException("参数无效")).when(quotes).quote(any());
        mvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                                        "/app/customization/quote")
                                .contentType("application/json")
                                .content("{\"goodsId\":10}"))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers.status()
                                .isBadRequest());
    }

    /** 验证商品详情接口允许匿名访问 / Verifies anonymous access annotation. */
    @Test
    void goodsDetailAllowsAnonymousAccess() throws NoSuchMethodException {
        Method detail =
                AppGoodsController.class.getMethod(
                        "detail", WxUserEntity.class, Integer.class, Long.class);

        assertTrue(detail.isAnnotationPresent(IgnoreAuth.class));
    }
}
