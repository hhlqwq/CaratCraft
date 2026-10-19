package com.platform.modules.app.controller;

import com.platform.annotation.IgnoreAuth;
import com.platform.annotation.LoginUser;
import com.platform.common.utils.RestResponse;
import com.platform.modules.app.service.AiRecommendationRequest;
import com.platform.modules.app.service.AiRecommendationService;
import com.platform.modules.app.service.AiRecommendationValidation;
import com.platform.modules.wx.entity.WxUserEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** 小程序AI推荐入口, 推荐请求必须通过已有登录认证. */
@RestController
@RequestMapping("/app/customization/ai")
public class AppAiRecommendationController {
  private final AiRecommendationService service;

  /** 注入推荐业务服务. */
  public AppAiRecommendationController(AiRecommendationService service) {
    this.service = service;
  }

  /** 匿名仅可读取开关及用途说明. */
  @IgnoreAuth
  @GetMapping("status")
  public RestResponse<Object> status() {
    return RestResponse.ok(service.status());
  }

  /** 从认证上下文取得用户ID, 使用真实HTTP状态返回可预期错误. */
  @PostMapping("prepare")
  public ResponseEntity<RestResponse<Object>> prepare(
      @LoginUser WxUserEntity user, @RequestBody AiRecommendationRequest request) {
    try {
      return ResponseEntity.ok(RestResponse.ok(service.prepare(user.getId(), request)));
    } catch (IllegalArgumentException error) {
      return ResponseEntity.badRequest().body(RestResponse.fail(400, error.getMessage()));
    } catch (ResponseStatusException error) {
      return ResponseEntity.status(error.getStatusCode())
          .body(RestResponse.fail(error.getStatusCode().value(), error.getReason()));
    }
  }

  /** 云模型建议通过独立校验后才能成为可采用的材料搭配. */
  @PostMapping("validate")
  public ResponseEntity<RestResponse<Object>> validate(
      @LoginUser WxUserEntity user, @RequestBody AiRecommendationValidation request) {
    try {
      return ResponseEntity.ok(RestResponse.ok(service.validateResult(user.getId(), request)));
    } catch (IllegalArgumentException error) {
      return ResponseEntity.badRequest().body(RestResponse.fail(400, error.getMessage()));
    } catch (ResponseStatusException error) {
      return ResponseEntity.status(error.getStatusCode())
          .body(RestResponse.fail(error.getStatusCode().value(), error.getReason()));
    }
  }
}
