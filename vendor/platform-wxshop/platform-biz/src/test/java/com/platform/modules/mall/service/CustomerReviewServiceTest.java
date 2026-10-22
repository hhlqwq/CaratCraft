package com.platform.modules.mall.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.common.utils.Base64Util;
import com.platform.modules.mall.dao.MallCommentDao;
import com.platform.modules.mall.dao.MallCommentPictureDao;
import com.platform.modules.mall.dao.MallUserCouponDao;
import com.platform.modules.mall.entity.MallCouponEntity;
import com.platform.modules.mall.entity.MallUserCouponEntity;
import com.platform.modules.wx.dao.WxUserDao;
import com.platform.modules.wx.entity.WxUserEntity;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/** 评价购买校验、首评奖励及公共资料裁剪回归. */
class CustomerReviewServiceTest {
    /** 先读取原有评价数量, 新评价插入后仍能发出一次奖励. */
    @Test
    void firstReviewReceivesRewardAfterPurchaseValidation() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        MallCommentDao comments = mock(MallCommentDao.class);
        WxUserDao users = mock(WxUserDao.class);
        MallCouponService coupons = mock(MallCouponService.class);
        MallUserCouponDao rewards = mock(MallUserCouponDao.class);
        when(users.selectOne(any())).thenReturn(new WxUserEntity());
        when(jdbc.queryForObject(anyString(), eq(Long.class), any(), any())).thenReturn(1L);
        when(comments.selectCount(any())).thenReturn(0L);
        when(comments.insert(any(com.platform.modules.mall.entity.MallCommentEntity.class)))
                .thenReturn(1);
        when(rewards.selectCount(any())).thenReturn(0L);
        when(rewards.insert(any(MallUserCouponEntity.class))).thenReturn(1);
        MallCouponEntity coupon = new MallCouponEntity();
        coupon.setId(12);
        when(coupons.queryMaxUserEnableCoupon(anyMap())).thenReturn(coupon);
        var service =
                new CustomerReviewService(
                        jdbc, comments, mock(MallCommentPictureDao.class), users, coupons, rewards);
        assertTrue(service.post(7L, 0, 10, "购买评价", List.of()).containsKey("coupon"));
        var sequence = inOrder(comments, rewards);
        sequence.verify(comments).selectCount(any());
        sequence.verify(comments)
                .insert(any(com.platform.modules.mall.entity.MallCommentEntity.class));
        sequence.verify(rewards).selectCount(any());
        sequence.verify(rewards)
                .insert(
                        argThat(
                                (MallUserCouponEntity value) ->
                                        value.getCouponStatus() == 1
                                                && "first-review:10".equals(value.getSourceKey())));
        when(jdbc.queryForObject(anyString(), eq(Long.class), any(), any())).thenReturn(0L);
        assertThrows(
                IllegalArgumentException.class, () -> service.post(7L, 0, 11, "未购买", List.of()));
    }

    /** 公共列表只返回昵称和头像, 列表及总数均使用审核通过过滤. */
    @Test
    void publicPageOmitsPrivateUserFields() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        WxUserDao users = mock(WxUserDao.class);
        MallCommentPictureDao pictures = mock(MallCommentPictureDao.class);
        when(jdbc.queryForList(anyString(), eq(10), eq(20), eq(0L)))
                .thenReturn(
                        List.of(
                                Map.of(
                                        "id",
                                        1,
                                        "content",
                                        Base64Util.encode("评价"),
                                        "add_time",
                                        1,
                                        "user_id",
                                        7L)));
        when(jdbc.queryForObject(anyString(), eq(Long.class), any())).thenReturn(1L);
        WxUserEntity user = new WxUserEntity();
        user.setId(7L);
        user.setNickname("买家");
        user.setHeadImgUrl("/upload/avatar.png");
        when(users.selectByIds(anyCollection())).thenReturn(List.of(user));
        when(pictures.selectList(any())).thenReturn(List.of());
        var service =
                new CustomerReviewService(
                        jdbc,
                        mock(MallCommentDao.class),
                        pictures,
                        users,
                        mock(MallCouponService.class),
                        mock(MallUserCouponDao.class));
        CustomerReviewService.ReviewPage page = service.page(0, 10, true, 1, 20);
        CustomerReviewService.PublicReview record = page.records().get(0);
        assertEquals(
                new CustomerReviewService.PublicProfile("买家", "/upload/avatar.png"),
                record.userInfo());
        JsonNode wire = new ObjectMapper().valueToTree(page);
        assertEquals(1, wire.path("total").asInt());
        assertEquals("买家", wire.path("records").get(0).path("userInfo").path("nickname").asText());
        assertEquals(2, wire.path("records").get(0).path("userInfo").size());
        verify(jdbc).queryForList(contains("c.status=1 AND EXISTS"), eq(10), eq(20), eq(0L));
        verify(jdbc).queryForObject(contains("c.status=1 AND EXISTS"), eq(Long.class), eq(10));
        assertThrows(IllegalArgumentException.class, () -> service.page(1, 10, false, 1, 20));
    }
}
