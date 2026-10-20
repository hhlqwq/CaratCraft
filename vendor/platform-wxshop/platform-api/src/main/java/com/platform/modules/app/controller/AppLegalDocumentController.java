package com.platform.modules.app.controller;

import com.platform.annotation.IgnoreAuth;
import com.platform.annotation.LoginUser;
import com.platform.common.utils.RestResponse;
import com.platform.modules.mall.service.LegalDocumentService;
import com.platform.modules.wx.entity.WxUserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 访客查看已发布协议, 登录用户可主动确认并查询本人确认记录. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/legal")
public class AppLegalDocumentController {
  private final LegalDocumentService documents;

  /** 游客无需提供个人信息即可阅读当前正文. */
  @IgnoreAuth
  @GetMapping
  public RestResponse<Object> current() {
    return RestResponse.ok(documents.published());
  }

  /** 历史入口不返回任何未发布草稿. */
  @IgnoreAuth
  @GetMapping("/{kind}/{version}")
  public RestResponse<Object> version(@PathVariable String kind, @PathVariable int version) {
    return RestResponse.ok(documents.publishedVersion(kind, version));
  }

  /** 确认身份来自会话, 请求正文只允许指定已阅读的两个版本. */
  @PostMapping("accept")
  public RestResponse<Object> accept(
      @LoginUser WxUserEntity user, @RequestBody LegalDocumentService.Acceptance acceptance) {
    documents.accept(Long.valueOf(user.getId()), acceptance);
    return RestResponse.ok();
  }

  /** 只展示当前用户的确认历史. */
  @GetMapping("history")
  public RestResponse<Object> history(@LoginUser WxUserEntity user) {
    return RestResponse.ok(documents.history(Long.valueOf(user.getId())));
  }
}
