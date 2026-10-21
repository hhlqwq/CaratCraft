package com.platform.modules.app.vo;

import com.platform.modules.wx.entity.WxUserEntity;

/** 登录仅返回本人界面必需字段, 平台身份、IP及运营资料不下发. */
public record LoginUserInfo(
    Long id,
    String nickname,
    String headImgUrl,
    Integer integral,
    Integer allIntegral,
    String phone) {
  /** 登录和资料入口共用白名单, 防止后续刷新重新下发内部字段. */
  public static LoginUserInfo from(WxUserEntity user) {
    return new LoginUserInfo(
        user.getId(),
        user.getNickname(),
        user.getHeadImgUrl(),
        user.getIntegral(),
        user.getAllIntegral(),
        user.getPhone());
  }
}
