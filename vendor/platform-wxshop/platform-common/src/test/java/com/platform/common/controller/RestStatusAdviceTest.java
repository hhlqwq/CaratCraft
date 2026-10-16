package com.platform.common.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.platform.common.exception.BusinessExceptionHandler;
import com.platform.common.exception.RestStatusAdvice;
import com.platform.common.utils.RestResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** 通过真实MVC适配链验证业务JSON和HTTP状态一致, 不启动网络监听. */
class RestStatusAdviceTest {
  /** 参数错误、状态冲突及成功结果必须产生对应HTTP状态. */
  @Test
  void mvcUsesActualFailureStatus() throws Exception {
    var mvc =
        MockMvcBuilders.standaloneSetup(new Endpoint())
            .setControllerAdvice(new RestStatusAdvice(), new BusinessExceptionHandler())
            .build();
    assertEquals(400, mvc.perform(get("/invalid")).andReturn().getResponse().getStatus());
    assertEquals(409, mvc.perform(get("/conflict")).andReturn().getResponse().getStatus());
    assertEquals(403, mvc.perform(get("/forbidden")).andReturn().getResponse().getStatus());
    assertEquals(200, mvc.perform(get("/ok")).andReturn().getResponse().getStatus());
    assertEquals(405, mvc.perform(post("/ok")).andReturn().getResponse().getStatus());
  }

  /** 隔离端点仅用于响应适配验证. */
  @RestController
  static class Endpoint {
    /** 模拟校验失败. */
    @GetMapping("/invalid")
    public Object invalid() {
      throw new IllegalArgumentException("参数错误");
    }

    /** 模拟版本冲突. */
    @GetMapping("/conflict")
    public Object conflict() {
      throw new IllegalStateException("请刷新");
    }

    /** 模拟权限业务结果. */
    @GetMapping("/forbidden")
    public Object forbidden() {
      return RestResponse.fail(403, "无权访问");
    }

    /** 模拟正常业务结果. */
    @GetMapping("/ok")
    public Object ok() {
      return RestResponse.ok();
    }
  }
}
