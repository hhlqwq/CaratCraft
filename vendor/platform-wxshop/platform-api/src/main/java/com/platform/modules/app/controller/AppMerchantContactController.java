package com.platform.modules.app.controller;

import com.platform.annotation.IgnoreAuth;
import com.platform.common.utils.RestResponse;
import com.platform.modules.mall.service.MerchantContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 访客也能联系商家, 不要求先提供个人资料或登录. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/merchant/contact")
public class AppMerchantContactController {
  private final MerchantContactService contacts;

  /** 仅提供当前公开资料, 不开放管理员身份和历史配置. */
  @IgnoreAuth
  @GetMapping
  public RestResponse<Object> current() {
    return RestResponse.ok(contacts.current());
  }
}
