package com.platform.modules.app.controller;

import com.platform.annotation.IgnoreAuth;
import com.platform.annotation.LoginUser;
import com.platform.common.utils.RestResponse;
import com.platform.common.utils.SafeImageUpload;
import com.platform.modules.community.CommunityPostRequest;
import com.platform.modules.community.CommunityService;
import com.platform.modules.oss.cloud.UploadFactory;
import com.platform.modules.wx.entity.WxUserEntity;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 小程序社区入口, 没有评论、私信与跨用户资料接口. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/community")
public class AppCommunityController {
  private final CommunityService community;

  /** 允许匿名浏览审核通过的分享. */
  @IgnoreAuth
  @GetMapping("list")
  public RestResponse<Object> list(
      @RequestParam(required = false) String kind,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int limit) {
    return RestResponse.ok(community.page(kind, page, limit));
  }

  /** 公开详情不可读取待审核、隐藏或删除内容. */
  @IgnoreAuth
  @GetMapping("detail")
  public RestResponse<Object> detail(@RequestParam Long id) {
    return RestResponse.ok(community.detail(id));
  }

  /** 本人分享列表保留审核反馈. */
  @GetMapping("mine")
  public RestResponse<Object> mine(
      @LoginUser WxUserEntity user,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int limit) {
    return RestResponse.ok(community.mine(user.getId(), page, limit));
  }

  /** 图片解码重编码后登记归属, 不复用没有用户归属的通用图片地址. */
  @PostMapping("image")
  public RestResponse<Object> image(@LoginUser WxUserEntity user, @RequestParam MultipartFile file)
      throws IOException {
    byte[] png = SafeImageUpload.readPng(file);
    long mediaId = community.reserveImage(user.getId());
    String url = Objects.requireNonNull(UploadFactory.build()).uploadSuffix(png, ".png");
    return RestResponse.ok(community.registerImage(user.getId(), mediaId, url));
  }

  /** 发布纯文本和本人图片, 默认等待运营审核. */
  @PostMapping("publish")
  public RestResponse<Object> publish(
      @LoginUser WxUserEntity user, @RequestBody CommunityPostRequest request) {
    return RestResponse.ok(community.publish(user.getId(), request));
  }

  /** 批量获取本人的点赞状态, 不使用用户提交的身份. */
  @PostMapping("states")
  public RestResponse<Object> states(@LoginUser WxUserEntity user, @RequestBody List<Long> ids) {
    return RestResponse.ok(community.states(user.getId(), ids));
  }

  /** 接收目标状态而非增减数值, 使网络重试保持幂等. */
  @PostMapping("like")
  public RestResponse<Object> like(
      @LoginUser WxUserEntity user, @RequestParam Long id, @RequestParam boolean liked) {
    return RestResponse.ok(community.like(user.getId(), id, liked));
  }

  /** 用户只能移除自己的分享. */
  @PostMapping("remove")
  public RestResponse<Object> remove(@LoginUser WxUserEntity user, @RequestParam Long id) {
    community.remove(user.getId(), id);
    return RestResponse.ok();
  }
}
