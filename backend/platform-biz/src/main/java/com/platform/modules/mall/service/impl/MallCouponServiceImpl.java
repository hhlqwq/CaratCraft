/*
 *
 *      Copyright (c) 2018-2099, hehailong All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the openwtai.com developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: hehailong(845367567@qq.com)
 *
 */
package com.platform.modules.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.common.utils.Query;
import com.platform.modules.mall.dao.MallCouponDao;
import com.platform.modules.mall.entity.MallCouponEntity;
import com.platform.modules.mall.service.MallCouponService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Service实现类
 *
 * @author hehailong
 * @since 2026-02-24 10:20:41
 */
@Service("mallCouponService")
public class MallCouponServiceImpl extends ServiceImpl<MallCouponDao, MallCouponEntity>
        implements MallCouponService {

    @Override
    public List<MallCouponEntity> queryAll(Map<String, Object> params) {
        return baseMapper.queryAll(params);
    }

    @Override
    public List<MallCouponEntity> queryAllByWrapper(QueryWrapper<MallCouponEntity> queryWrapper) {
        return this.list(queryWrapper);
    }

    @Override
    public Page<MallCouponEntity> queryPage(Map<String, Object> params) {
        // 排序
        params.put("sidx", "T.id");
        params.put("asc", false);
        Page<MallCouponEntity> page = new Query<MallCouponEntity>(params).getPage();
        return page.setRecords(baseMapper.selectMallCouponPage(page, params));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(MallCouponEntity mallCoupon) {
        applyWelcomeRules(mallCoupon);
        boolean saved = this.save(mallCoupon);
        if (saved && isWelcome(mallCoupon) && mallCoupon.getId() > 255) {
            throw new IllegalArgumentException("优惠券编号超出现有领取记录容量");
        }
        return saved;
    }

    @Override
    public boolean update(MallCouponEntity mallCoupon) {
        var existing = this.getById(mallCoupon.getId());
        if (isWelcome(existing) != isWelcome(mallCoupon)) {
            throw new IllegalArgumentException("已经建立的券不可切换为新人券或其他类型");
        }
        applyWelcomeRules(mallCoupon);
        return this.updateById(mallCoupon);
    }

    @Override
    public boolean delete(Integer id) {
        return deleteBatch(new Integer[] {id});
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBatch(Integer[] ids) {
        var templates = this.listByIds(Arrays.asList(ids));
        if (templates.stream().anyMatch(MallCouponServiceImpl::isWelcome)) {
            throw new IllegalArgumentException("请通过结束发放时间停止新人活动, 已发放优惠券仍按原有效期使用");
        }
        return this.removeByIds(Arrays.asList(ids));
    }

    /** 区分独立新人活动和旧版注册领券. */
    private static boolean isWelcome(MallCouponEntity value) {
        return value != null && Integer.valueOf(
                com.platform.modules.mall.service.WelcomeCoupon.SEND_TYPE).equals(value.getSendType());
    }

    /** 管理后台只能调整新人活动的发放窗口, 消费者用券规则固定. */
    private static void applyWelcomeRules(MallCouponEntity value) {
        if (!isWelcome(value)) return;
        if (value.getSendStartDate() != null && value.getSendEndDate() != null
                && !value.getSendEndDate().after(value.getSendStartDate())) {
            throw new IllegalArgumentException("结束发放时间必须晚于开始时间");
        }
        value.setTypeMoney(com.platform.modules.mall.service.WelcomeCoupon.DISCOUNT);
        value.setMinGoodsAmount(com.platform.modules.mall.service.WelcomeCoupon.MINIMUM);
    }

    /** 查询当前用户在给定条件下可用的最大优惠券. */
    public MallCouponEntity queryMaxUserEnableCoupon(Map<String, Object> map) {
        return baseMapper.queryMaxUserEnableCoupon(map);
    }

    @Override
    public List<MallCouponEntity> queryList(Map<String, Object> couponParam) {
        return baseMapper.queryList(couponParam);
    }
}
