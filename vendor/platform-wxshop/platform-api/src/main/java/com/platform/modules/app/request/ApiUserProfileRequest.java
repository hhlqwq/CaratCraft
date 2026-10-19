package com.platform.modules.app.request;

import lombok.Data;

/** 小程序用户资料更新请求 / Mini-program user profile update request. */
@Data
public class ApiUserProfileRequest {
  private String nickname;
  private String avatarUrl;
}
