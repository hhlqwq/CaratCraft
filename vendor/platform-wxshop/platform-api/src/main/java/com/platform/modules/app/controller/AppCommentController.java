package com.platform.modules.app.controller;

import com.platform.annotation.IgnoreAuth;
import com.platform.annotation.LoginUser;
import com.platform.common.utils.*;
import com.platform.modules.app.request.ApiCommentPostRequest;
import com.platform.modules.mall.entity.*;
import com.platform.modules.wx.entity.WxUserEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 作者: @author Harmon <br>
 * 时间: 2017-08-11 08:32<br>
 *
 * <p>描述: AppController <br>
 */
@Tag(name = "会员评论-AppCommentController")
@RestController
@RequestMapping("/app/comment")
public class AppCommentController extends AppBaseController {
  private final com.platform.modules.mall.service.CustomerReviewService reviews;

  /** 注入购买评价服务, 控制器不处理发券及用户资料装配. */
  public AppCommentController(com.platform.modules.mall.service.CustomerReviewService reviews) {
    this.reviews = reviews;
  }

  /** 只接受已购买商品的本人评价, 社区不开放评论. */
  @PostMapping("post")
  public RestResponse<Object> post(
      @LoginUser WxUserEntity user, @RequestBody ApiCommentPostRequest request) {
    return RestResponse.ok(
        reviews.post(
            user.getId(),
            request.getTypeId(),
            request.getValueId(),
            request.getContent(),
            request.getImagesList()));
  }

  /** 匿名读取与公开列表一致的审核通过计数. */
  @IgnoreAuth
  @PostMapping("count")
  public RestResponse<Object> count(Integer typeId, Integer valueId) {
    return RestResponse.ok(reviews.count(typeId, valueId));
  }

  /** 分页只输出昵称、头像、评价内容和图片, 不返回用户实体. */
  @IgnoreAuth
  @PostMapping("list")
  public RestResponse<Object> list(
      Integer typeId,
      Integer valueId,
      Integer showType,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size) {
    return RestResponse.ok(
        reviews.page(typeId, valueId, Integer.valueOf(1).equals(showType), page, size));
  }
}
