package com.platform.modules.app.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.platform.annotation.IgnoreAuth;
import com.platform.modules.app.request.ApiAddressSaveRequest;
import com.platform.modules.app.request.ApiCartUpdateRequest;
import com.platform.modules.app.request.LoginWxPhoneRequest;
import com.platform.modules.mall.entity.MallCartEntity;
import com.platform.modules.mall.entity.MallProductEntity;
import com.platform.modules.mall.service.MallAddressService;
import com.platform.modules.mall.service.MallCartService;
import com.platform.modules.mall.service.MallProductService;
import com.platform.modules.wx.entity.WxUserEntity;
import com.platform.modules.wx.service.WxUserService;
import com.platform.utils.JwtUtils;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** 用户归属、输入边界及认证配置回归, 全部外部依赖使用隔离替身. */
class AccountSecurityTest {
  /** 手机授权和上传入口不允许跳过认证. */
  @Test
  void protectedEndpointsRequireAuthentication() throws Exception {
    assertNull(AppLoginController.class.getMethod("loginByMaPhone", WxUserEntity.class,
        LoginWxPhoneRequest.class).getAnnotation(IgnoreAuth.class));
    assertNull(AppUploadController.class.getMethod("upload",
        org.springframework.web.multipart.MultipartFile.class).getAnnotation(IgnoreAuth.class));
    assertThrows(ClassNotFoundException.class,
        () -> Class.forName("com.platform.modules.app.controller.AppTestController"));
  }

  /** 默认地址字段缺失不触发拆箱异常, 用户ID始终取登录上下文. */
  @Test
  void addressWithoutDefaultFlagUsesAuthenticatedOwner() {
    MallAddressService addresses = mock(MallAddressService.class);
    AppAddressController controller = new AppAddressController();
    ReflectionTestUtils.setField(controller, "addressService", addresses);
    ApiAddressSaveRequest request = new ApiAddressSaveRequest();
    request.setUserName("测试用户");
    request.setProvinceName("广东省");
    request.setCityName("深圳市");
    request.setCountyName("南山区");
    request.setDetailInfo("隔离测试地址");
    request.setTelNumber("13800000000");
    WxUserEntity user = new WxUserEntity();
    user.setId(7L);
    controller.save(user, request);
    verify(addresses).saveOwned(argThat(address -> address.getUserId().equals(7L)
        && address.getIsDefault() == 0));
    verify(addresses, never()).update(any(com.platform.modules.mall.entity.MallAddressEntity.class));
  }

  /** 禁止跨用户更新购物车, 不调用任何写入或合并删除. */
  @Test
  void cartCannotUpdateAnotherOwner() {
    AppCartController controller = new AppCartController();
    MallCartService carts = mock(MallCartService.class);
    MallProductService products = mock(MallProductService.class);
    WxUserService users = mock(WxUserService.class);
    ReflectionTestUtils.setField(controller, "cartService", carts);
    ReflectionTestUtils.setField(controller, "productService", products);
    ReflectionTestUtils.setField(controller, "userService", users);
    WxUserEntity user = new WxUserEntity();
    user.setId(7L);
    when(users.getOne(any())).thenReturn(user);
    MallProductEntity product = new MallProductEntity();
    product.setId(11);
    product.setGoodsId(10);
    product.setGoodsNumber(20);
    when(products.getById(11)).thenReturn(product);
    MallCartEntity cart = new MallCartEntity();
    cart.setUserId(8L);
    cart.setGoodsId(10);
    cart.setProductId(11);
    when(carts.getById(99)).thenReturn(cart);
    ApiCartUpdateRequest request = new ApiCartUpdateRequest();
    request.setId(99);
    request.setGoodsId(10);
    request.setProductId(11);
    request.setNumber(2);
    assertEquals(403, controller.update(user, request).getCode());
    verify(carts, never()).update(any(MallCartEntity.class));
    verify(carts, never()).delete(anyInt());
    request.setNumber(-5);
    assertEquals(400, controller.update(user, request).getCode());
  }

  /** 密钥缺失和短密钥在启动时失败, 强密钥可以签发并验证令牌. */
  @Test
  void jwtConfigurationFailsClosedAndStrongKeyRoundTrips() {
    JwtUtils jwt = new JwtUtils();
    jwt.setExpire(60);
    jwt.setHeader("token");
    assertThrows(IllegalStateException.class, jwt::validateConfiguration);
    jwt.setSecret("c2hvcnQ=");
    assertThrows(IllegalStateException.class, jwt::validateConfiguration);
    byte[] key = new byte[32];
    new java.security.SecureRandom().nextBytes(key);
    jwt.setSecret(Base64.getEncoder().encodeToString(key));
    jwt.validateConfiguration();
    assertEquals("7", jwt.getClaimByToken(jwt.generateToken(7L)).getSubject());
    assertNull(jwt.getClaimByToken("invalid.token"));
  }
}
