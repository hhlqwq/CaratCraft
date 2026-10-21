package com.platform.modules.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.platform.common.utils.Base64Util;
import com.platform.modules.mall.dao.MallCommentDao;
import com.platform.modules.mall.dao.MallCommentPictureDao;
import com.platform.modules.mall.dao.MallUserCouponDao;
import com.platform.modules.mall.entity.MallCommentEntity;
import com.platform.modules.mall.entity.MallCommentPictureEntity;
import com.platform.modules.mall.entity.MallUserCouponEntity;
import com.platform.modules.wx.dao.WxUserDao;
import com.platform.modules.wx.entity.WxUserEntity;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 商品购买评价与社区分离, 公共展示仅输出审核通过的最小用户资料. */
@Service
public class CustomerReviewService {
  /** 公开资料只含昵称和头像, 不持有完整用户实体. */
  public record PublicProfile(String nickname, String avatar) {}

  /** 评价列表保持原接口字段, 使用明确类型传递公开资料和图片. */
  public record PublicReview(
      Integer id,
      String content,
      long addTime,
      PublicProfile userInfo,
      List<MallCommentPictureEntity> picList) {}

  /** 分页数据保持records、total、current和size响应契约. */
  public record ReviewPage(List<PublicReview> records, Long total, int current, int size) {}

  private final JdbcTemplate jdbc;
  private final MallCommentDao comments;
  private final MallCommentPictureDao pictures;
  private final WxUserDao users;
  private final MallCouponService coupons;
  private final MallUserCouponDao userCoupons;

  /** 注入评价与发券事务所需的访问接口. */
  public CustomerReviewService(
      JdbcTemplate jdbc,
      MallCommentDao comments,
      MallCommentPictureDao pictures,
      WxUserDao users,
      MallCouponService coupons,
      MallUserCouponDao userCoupons) {
    this.jdbc = jdbc;
    this.comments = comments;
    this.pictures = pictures;
    this.users = users;
    this.coupons = coupons;
    this.userCoupons = userCoupons;
  }

  /** 验证真实已收货购买, 先判断首评再插入, 与奖励在同一用户锁和事务中完成. */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> post(
      Long userId, Integer type, Integer goodsId, String content, List<String> images) {
    validateTarget(type, goodsId);
    if (content == null
        || content.isBlank()
        || content.length() > 500
        || (images != null && images.size() > 6)) {
      throw new IllegalArgumentException("请填写1到500字评价, 图片最多6张");
    }
    if (userId == null
        || users.selectOne(new QueryWrapper<WxUserEntity>().eq("id", userId).last("FOR UPDATE"))
            == null) {
      throw new IllegalArgumentException("请先登录");
    }
    Long bought =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM mall_order_goods g JOIN mall_order o ON o.id=g.order_id "
                + "WHERE o.user_id=? AND g.goods_id=? AND o.order_status=301 AND o.pay_status=2",
            Long.class,
            userId,
            goodsId);
    if (bought == null || bought == 0) {
      throw new IllegalArgumentException("仅本人已收货商品可以评价");
    }
    Long previous =
        comments.selectCount(
            new QueryWrapper<MallCommentEntity>()
                .eq("user_id", userId)
                .eq("type_id", 0)
                .eq("value_id", goodsId));
    if (previous != null && previous > 0) {
      throw new IllegalArgumentException("该商品已评价, 请勿重复提交");
    }
    MallCommentEntity comment = new MallCommentEntity();
    comment.setTypeId(0);
    comment.setValueId(goodsId);
    comment.setUserId(userId);
    comment.setContent(Base64Util.encode(content.trim()));
    comment.setStatus(0);
    comment.setAddTime(System.currentTimeMillis() / 1000);
    if (comments.insert(comment) != 1) {
      throw new IllegalStateException("评价保存失败");
    }
    int sort = 0;
    for (String url : images == null ? List.<String>of() : images) {
      if (url == null || !url.startsWith("/upload/") || url.length() > 1000 || url.contains("..")) {
        throw new IllegalArgumentException("评价图片须为已上传的站内图片");
      }
      MallCommentPictureEntity picture = new MallCommentPictureEntity();
      picture.setCommentId(comment.getId());
      picture.setPicUrl(url);
      picture.setSortOrder(++sort);
      if (pictures.insert(picture) != 1) {
        throw new IllegalStateException("评价图片保存失败");
      }
    }
    Map<String, Object> result = new LinkedHashMap<>();
    String source = "first-review:" + goodsId;
    if (userCoupons.selectCount(
            new QueryWrapper<MallUserCouponEntity>().eq("user_id", userId).eq("source_key", source))
        == 0) {
      var coupon = coupons.queryMaxUserEnableCoupon(Map.of("sendType", 6));
      Date now = new Date();
      if (coupon != null
          && (coupon.getSendStartDate() == null || !now.before(coupon.getSendStartDate()))
          && (coupon.getSendEndDate() == null || !now.after(coupon.getSendEndDate()))) {
        MallUserCouponEntity reward = new MallUserCouponEntity();
        reward.setCouponId(coupon.getId());
        reward.setUserId(userId);
        reward.setCouponStatus(1);
        reward.setSourceKey(source);
        reward.setCouponNumber(UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        reward.setAddTime(now);
        if (userCoupons.insert(reward) != 1) throw new IllegalStateException("评价奖励保存失败");
        result.put("coupon", coupon);
      }
    }
    result.put("message", "评价已提交, 审核通过后展示");
    return result;
  }

  /** 列表和总数使用完全相同的目标、审核状态和有图过滤. */
  public ReviewPage page(Integer type, Integer goodsId, boolean hasPic, int page, int size) {
    validateTarget(type, goodsId);
    if (page < 1 || size < 1 || size > 100) {
      throw new IllegalArgumentException("分页参数不正确");
    }
    String filter =
        " FROM mall_comment c WHERE c.type_id=0 AND c.value_id=? AND c.status=1"
            + (hasPic
                ? " AND EXISTS(SELECT 1 FROM mall_comment_picture p WHERE p.comment_id=c.id)"
                : "");
    List<Map<String, Object>> rows =
        jdbc.queryForList(
            "SELECT c.id,c.content,c.add_time,c.user_id"
                + filter
                + " ORDER BY c.id DESC LIMIT ? OFFSET ?",
            goodsId,
            size,
            (long) (page - 1) * size);
    Map<Long, WxUserEntity> profiles = new HashMap<>();
    if (!rows.isEmpty()) {
      List<Long> ids =
          rows.stream().map(row -> ((Number) row.get("user_id")).longValue()).distinct().toList();
      for (WxUserEntity user : users.selectByIds(ids)) {
        profiles.put(user.getId(), user);
      }
    }
    Map<Integer, List<MallCommentPictureEntity>> byComment = new HashMap<>();
    if (!rows.isEmpty()) {
      List<Object> ids = rows.stream().map(row -> row.get("id")).toList();
      for (MallCommentPictureEntity picture :
          pictures.selectList(
              new QueryWrapper<MallCommentPictureEntity>()
                  .in("comment_id", ids)
                  .orderByAsc("sort_order"))) {
        byComment
            .computeIfAbsent(picture.getCommentId(), ignored -> new ArrayList<>())
            .add(picture);
      }
    }
    List<PublicReview> records = new ArrayList<>();
    for (Map<String, Object> row : rows) {
      WxUserEntity profile = profiles.get(((Number) row.get("user_id")).longValue());
      int id = ((Number) row.get("id")).intValue();
      PublicProfile publicProfile =
          new PublicProfile(
              profile == null || profile.getNickname() == null ? "用户" : profile.getNickname(),
              profile == null || profile.getHeadImgUrl() == null ? "" : profile.getHeadImgUrl());
      records.add(
          new PublicReview(
              id,
              Base64Util.decode(row.get("content").toString()),
              ((Number) row.get("add_time")).longValue(),
              publicProfile,
              byComment.getOrDefault(id, List.of())));
    }
    return new ReviewPage(
        records, jdbc.queryForObject("SELECT COUNT(*)" + filter, Long.class, goodsId), page, size);
  }

  /** 公开计数只包含审核通过的数据. */
  public Map<String, Object> count(Integer type, Integer goodsId) {
    validateTarget(type, goodsId);
    return Map.of(
        "allCount",
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM mall_comment WHERE type_id=0 AND value_id=? AND status=1",
            Long.class,
            goodsId),
        "hasPicCount",
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM mall_comment c WHERE type_id=0 AND value_id=? AND status=1 "
                + "AND EXISTS(SELECT 1 FROM mall_comment_picture p WHERE p.comment_id=c.id)",
            Long.class,
            goodsId));
  }

  /** 社区与专题不开放评论, 只接受明确的商品评价目标. */
  private void validateTarget(Integer type, Integer goodsId) {
    if (!Integer.valueOf(0).equals(type) || goodsId == null || goodsId < 1) {
      throw new IllegalArgumentException("仅开放商品购买评价, 社区不提供评论功能");
    }
  }
}
