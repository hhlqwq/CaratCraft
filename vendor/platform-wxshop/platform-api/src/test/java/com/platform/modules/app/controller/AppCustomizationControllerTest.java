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
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

    ResponseEntity<RestResponse<Object>> response = controller.quote(new CustomQuoteRequest());

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getCode());
    assertEquals("请选择必选组件：style", response.getBody().getMsg());
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

    ResponseEntity<RestResponse<Object>> response = controller.detail(user, 88L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(snapshot, response.getBody().getData());
  }

  /** 验证商品详情接口允许匿名访问 / Verifies anonymous access annotation. */
  @Test
  void goodsDetailAllowsAnonymousAccess() throws NoSuchMethodException {
    Method detail =
        AppGoodsController.class.getMethod("detail", WxUserEntity.class, Integer.class, Long.class);

    assertTrue(detail.isAnnotationPresent(IgnoreAuth.class));
  }
}
