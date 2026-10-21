package com.platform.modules.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.WxMaUserService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.modules.app.request.LoginWxRequest;
import com.platform.modules.mall.service.AccountLoginService;
import com.platform.modules.mall.service.LegalDocumentService;
import com.platform.modules.wx.entity.WxUserEntity;
import com.platform.modules.wx.service.WxUserService;
import com.platform.utils.JwtUtils;
import me.chanjar.weixin.mp.api.WxMpService;
import org.junit.jupiter.api.Test;

/** 身份登录不请求可选个人资料, 也不把运营字段暴露到客户端. */
class AccountLoginControllerTest {
  /** 模拟可信微信会话, 校验事务服务使用服务端标识及响应白名单. */
  @Test
  void codeOnlyLoginReturnsWhitelistedProfile() throws Exception {
    WxMaService mini = mock(WxMaService.class);
    WxMaUserService identity = mock(WxMaUserService.class);
    when(mini.getUserService()).thenReturn(identity);
    WxMaJscode2SessionResult session = new WxMaJscode2SessionResult();
    session.setOpenid("verified-openid");
    session.setUnionid("verified-union");
    when(identity.getSessionInfo("one-time-code")).thenReturn(session);
    AccountLoginService accounts = mock(AccountLoginService.class);
    LegalDocumentService documents = mock(LegalDocumentService.class);
    var consent = new LegalDocumentService.Acceptance(1, 2);
    WxUserEntity user = new WxUserEntity();
    user.setId(7L);
    user.setNickname("自填昵称");
    user.setMaOpenid("private-openid");
    user.setRemark("private-operation-note");
    user.setRegisterIp("private-ip");
    when(accounts.login(true, "verified-openid", "verified-union", consent)).thenReturn(user);
    JwtUtils tokens = mock(JwtUtils.class);
    when(tokens.generateToken(7L)).thenReturn("test-session");
    var controller =
        new AppLoginController(
            mock(WxUserService.class), tokens, mini, mock(WxMpService.class), documents, accounts);
    LoginWxRequest request = new LoginWxRequest();
    request.setCode("one-time-code");
    request.setLegalAcceptance(consent);
    var response = controller.loginByMa(request);
    assertEquals(0, response.getCode());
    assertEquals("自填昵称", response.getData().getUserInfo().nickname());
    String json = new ObjectMapper().writeValueAsString(response);
    assertFalse(json.contains("private-"));
    verify(documents).requireCurrent(consent);
    verify(identity).getSessionInfo("one-time-code");
    verifyNoMoreInteractions(identity);
  }
}
