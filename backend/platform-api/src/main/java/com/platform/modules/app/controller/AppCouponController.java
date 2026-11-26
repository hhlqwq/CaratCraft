package com.platform.modules.app.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.platform.annotation.LoginUser;
import com.platform.common.utils.CharUtil;
import com.platform.common.utils.RestResponse;
import com.platform.common.utils.StringUtils;
import com.platform.modules.app.request.ApiCouponExchangeRequest;
import com.platform.modules.app.request.ApiCouponNewUserRequest;
import com.platform.modules.app.request.ApiCouponTransActivitRequest;
import com.platform.modules.app.service.MobileVerificationService;
import com.platform.modules.mall.entity.MallCouponEntity;
import com.platform.modules.mall.entity.MallUserCouponEntity;
import com.platform.modules.mall.service.MallCouponService;
import com.platform.modules.mall.service.MallUserCouponService;
import com.platform.modules.wx.entity.WxUserEntity;
import com.platform.modules.wx.service.WxUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * API优惠券管理
 *
 * @author hehailong
 * @email 939961241@qq.com
 * @date 2017-03-23 15:31
 */
@Tag(name = "优惠券-AppCouponController")
@RestController
@RequestMapping("/app/coupon")
public class AppCouponController extends AppBaseController {
    @Autowired private WxUserService apiUserService;
    @Autowired private MallCouponService apiCouponService;
    @Autowired private MallUserCouponService apiUserCouponService;
    @Autowired private MobileVerificationService verification;
    @Autowired private com.platform.modules.mall.service.CartApplicationService cartCheckout;

    /** 获取优惠券列表 */
    @Operation(summary = "获取优惠券列表")
    @PostMapping("/list")
    public RestResponse<List<Map<String, Object>>> list(@LoginUser WxUserEntity loginUser) {
        List<Map<String, Object>> couponList =
                queryUserCoupons(loginUser.getId(), null, null, null);
        return RestResponse.ok(couponList);
    }

    /** 根据商品获取可用优惠券列表 */
    @Operation(summary = "根据商品获取可用优惠券列表")
    @PostMapping("/listByGoods")
    public RestResponse<List<Map<String, Object>>> listByGoods(
            @RequestParam(defaultValue = "cart") String type,
            @RequestParam(required = false) String checkoutId,
            @LoginUser WxUserEntity loginUser) {
        var checkout = cartCheckout.checkout(loginUser, 0, type, checkoutId, null);
        if (checkout.getCode() != 0 || checkout.getData() == null) {
            return RestResponse.fail(checkout.getMsg());
        }
        BigDecimal goodsTotalPrice = (BigDecimal) checkout.getData().get("goodsTotalPrice");

        // 获取可用优惠券
        List<Map<String, Object>> couponVos = queryUserCoupons(loginUser.getId(), 1, null, null);
        List<Map<String, Object>> useCoupons = new ArrayList<>();
        List<Map<String, Object>> notUseCoupons = new ArrayList<>();
        for (Map<String, Object> couponVo : couponVos) {
            BigDecimal minGoodsAmount = (BigDecimal) couponVo.get("minGoodsAmount");
            if (minGoodsAmount == null) {
                minGoodsAmount = BigDecimal.ZERO;
            }
            if (goodsTotalPrice.compareTo(minGoodsAmount) >= 0) { // 可用优惠券
                couponVo.put("enabled", 1);
                useCoupons.add(couponVo);
            } else {
                couponVo.put("enabled", 0);
                notUseCoupons.add(couponVo);
            }
        }
        useCoupons.addAll(notUseCoupons);
        return RestResponse.ok(useCoupons);
    }

    /** 兑换优惠券 */
    @Operation(summary = "领券优惠券")
    @PostMapping("exchange")
    public RestResponse<MallUserCouponEntity> exchange(
            @LoginUser WxUserEntity loginUser, @RequestBody ApiCouponExchangeRequest request) {
        String couponNumber = request.getCouponNumber();
        if (StringUtils.isNullOrEmpty(couponNumber)) {
            return RestResponse.fail("当前优惠码无效");
        }
        QueryWrapper<MallUserCouponEntity> userCouponQueryWrapper = new QueryWrapper<>();
        userCouponQueryWrapper.eq("coupon_number", couponNumber);
        List<MallUserCouponEntity> couponVos = apiUserCouponService.list(userCouponQueryWrapper);
        if (couponVos == null || couponVos.isEmpty()) {
            return RestResponse.fail("当前优惠码无效");
        }
        MallUserCouponEntity userCouponVo = couponVos.get(0);
        if (null != userCouponVo.getUserId() && !userCouponVo.getUserId().equals(0L)) {
            return RestResponse.fail("当前优惠码已经兑换");
        }
        MallCouponEntity couponVo = apiCouponService.getById(userCouponVo.getCouponId());
        if (null == couponVo
                || null == couponVo.getUseEndDate()
                || couponVo.getUseEndDate().before(new Date())) {
            return RestResponse.fail("当前优惠码已经过期");
        }
        userCouponVo.setUserId(loginUser.getId());
        userCouponVo.setAddTime(new Date());
        userCouponVo.setCouponStatus(1);
        apiUserCouponService.updateById(userCouponVo);
        return RestResponse.ok(userCouponVo);
    }

    /** 　　填写手机号码，领券 */
    @Operation(summary = "领券优惠券")
    @PostMapping("newuser")
    @Transactional(rollbackFor = Exception.class)
    public RestResponse<MallUserCouponEntity> newuser(
            @LoginUser WxUserEntity loginUser, @RequestBody ApiCouponNewUserRequest request) {
        loginUser =
                apiUserService.getOne(
                        new QueryWrapper<WxUserEntity>()
                                .eq("ID", loginUser.getId())
                                .last("FOR UPDATE"));
        String phone = request.getPhone();
        Integer smscode = request.getSmscode();
        if (StringUtils.isNullOrEmpty(phone)) {
            return RestResponse.fail("手机号不能为空");
        }
        if (smscode == null) {
            return RestResponse.fail("短信校验失败");
        }
        // 判断是否是新用户
        if (!StringUtils.isNullOrEmpty(loginUser.getPhone())) {
            return RestResponse.fail("当前优惠券只能新用户领取");
        }
        verification.consume(loginUser.getId(), phone, String.valueOf(smscode));
        // 是否领取过了
        List<Map<String, Object>> couponVos = queryUserCoupons(loginUser.getId(), null, 4, null);
        if (couponVos != null && !couponVos.isEmpty()) {
            return RestResponse.fail("已经领取过，不能重复领取");
        }
        // 领取
        Map<String, Object> couponParam = new HashMap<>();
        couponParam.put("sendType", 4);
        MallCouponEntity newCouponConfig = apiCouponService.queryMaxUserEnableCoupon(couponParam);
        if (null != newCouponConfig) {
            loginUser.setPhone(phone);
            if (!apiUserService.updateById(loginUser)) {
                throw new IllegalStateException("用户资料更新失败");
            }
            MallUserCouponEntity userCouponVo = new MallUserCouponEntity();
            userCouponVo.setAddTime(new Date());
            userCouponVo.setCouponId(newCouponConfig.getId());
            userCouponVo.setCouponNumber(CharUtil.getRandomString(12));
            userCouponVo.setUserId(loginUser.getId());
            userCouponVo.setCouponStatus(1);
            if (!apiUserCouponService.save(userCouponVo)) {
                throw new IllegalStateException("领券失败");
            }
            return RestResponse.ok(userCouponVo);
        } else {
            return RestResponse.fail("领取失败");
        }
    }

    /** 　　转发领取红包 */
    @Operation(summary = "转发领取红包")
    @PostMapping("transActivit")
    public RestResponse<List<Map<String, Object>>> transActivit(
            @LoginUser WxUserEntity loginUser, @RequestBody ApiCouponTransActivitRequest request) {
        String sourceKey = request.getSourceKey();
        Long referrer = request.getReferrer();
        if (StringUtils.isNullOrEmpty(sourceKey)) {
            return RestResponse.fail("sourceKey不能为空");
        }
        // 是否领取过了
        List<Map<String, Object>> couponVos =
                queryUserCoupons(loginUser.getId(), null, 2, sourceKey);
        if (couponVos != null && !couponVos.isEmpty()) {
            return RestResponse.fail("已经领取过");
        }
        // 领取
        Map<String, Object> couponParam = new HashMap<>();
        couponParam.put("sendType", 2);
        MallCouponEntity newCouponConfig = apiCouponService.queryMaxUserEnableCoupon(couponParam);
        if (null != newCouponConfig) {
            MallUserCouponEntity userCouponVo = new MallUserCouponEntity();
            userCouponVo.setAddTime(new Date());
            userCouponVo.setCouponId(newCouponConfig.getId());
            userCouponVo.setCouponNumber(CharUtil.getRandomString(12));
            userCouponVo.setUserId(loginUser.getId());
            userCouponVo.setSourceKey(sourceKey);
            userCouponVo.setCouponStatus(1);
            if (referrer != null) {
                if (referrer > Integer.MAX_VALUE || referrer < Integer.MIN_VALUE) {
                    return RestResponse.fail("推荐人参数无效");
                }
                userCouponVo.setReferrer(referrer.intValue());
            }
            apiUserCouponService.save(userCouponVo);
            couponVos = queryUserCoupons(loginUser.getId(), null, 2, sourceKey);
            return RestResponse.ok(couponVos);
        } else {
            return RestResponse.fail("领取失败");
        }
    }

    /** 读取当前用户持有的优惠券. */
    private List<Map<String, Object>> queryUserCoupons(
            Long userId, Integer couponStatus, Integer sendType, String sourceKey) {
        QueryWrapper<MallUserCouponEntity> userCouponQueryWrapper = new QueryWrapper<>();
        userCouponQueryWrapper.eq("user_id", userId);
        if (couponStatus != null) {
            userCouponQueryWrapper.eq("coupon_status", couponStatus);
        }
        if (!StringUtils.isNullOrEmpty(sourceKey)) {
            userCouponQueryWrapper.eq("source_key", sourceKey);
        }
        userCouponQueryWrapper.orderByDesc("id");
        List<MallUserCouponEntity> userCouponList =
                apiUserCouponService.list(userCouponQueryWrapper);
        if (userCouponList == null || userCouponList.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Integer> couponIdSet = new HashSet<>();
        for (MallUserCouponEntity userCoupon : userCouponList) {
            if (userCoupon.getCouponId() != null) {
                couponIdSet.add(userCoupon.getCouponId());
            }
        }
        if (couponIdSet.isEmpty()) {
            return Collections.emptyList();
        }
        List<MallCouponEntity> couponList = apiCouponService.listByIds(couponIdSet);
        Map<Integer, MallCouponEntity> couponMap = new HashMap<>();
        for (MallCouponEntity coupon : couponList) {
            couponMap.put(coupon.getId(), coupon);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        Date now = new Date();
        for (MallUserCouponEntity userCoupon : userCouponList) {
            MallCouponEntity coupon = couponMap.get(userCoupon.getCouponId());
            if (coupon == null) {
                continue;
            }
            if (sendType != null && !sendType.equals(coupon.getSendType())) {
                continue;
            }
            boolean welcome = com.platform.modules.mall.service.WelcomeCoupon.SOURCE.equals(userCoupon.getSourceKey());
            Date start = welcome ? userCoupon.getAddTime() : coupon.getUseStartDate();
            Date end = welcome ? com.platform.modules.mall.service.WelcomeCoupon.expiresAt(start) : coupon.getUseEndDate();
            boolean expired = (welcome && end == null) || (end != null && !now.before(end));
            boolean usable = !expired && (start == null || !now.before(start));
            if (Integer.valueOf(1).equals(couponStatus) && !usable) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("id", coupon.getId());
            item.put("name", coupon.getName());
            item.put("typeMoney", welcome ? com.platform.modules.mall.service.WelcomeCoupon.DISCOUNT : coupon.getTypeMoney());
            item.put("sendType", coupon.getSendType());
            item.put("minAmount", coupon.getMinAmount());
            item.put("maxAmount", coupon.getMaxAmount());
            item.put("sendStartDate", coupon.getSendStartDate());
            item.put("sendEndDate", coupon.getSendEndDate());
            item.put("useStartDate", start);
            item.put("useEndDate", end);
            item.put("minGoodsAmount", welcome ? com.platform.modules.mall.service.WelcomeCoupon.MINIMUM : coupon.getMinGoodsAmount());
            item.put("minTransmitNum", coupon.getMinTransmitNum());
            item.put("couponId", userCoupon.getCouponId());
            item.put("couponNumber", userCoupon.getCouponNumber());
            item.put("couponStatus", expired && Integer.valueOf(1).equals(userCoupon.getCouponStatus())
                    ? 3 : userCoupon.getCouponStatus());
            item.put("sourceKey", userCoupon.getSourceKey());
            item.put("referrer", userCoupon.getReferrer());
            item.put("usedTime", userCoupon.getUsedTime());
            item.put("addTime", userCoupon.getAddTime());
            item.put("userCouponId", userCoupon.getId());
            item.put("user_coupon_id", userCoupon.getId());
            item.put("enabled", 1);
            result.add(item);
        }
        return result;
    }
}
