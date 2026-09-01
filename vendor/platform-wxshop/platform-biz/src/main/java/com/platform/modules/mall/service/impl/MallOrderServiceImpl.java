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

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.common.utils.Constant;
import com.platform.common.utils.Query;
import com.platform.common.utils.StringUtils;
import com.platform.config.RedisTemplateUtil;
import com.platform.modules.mall.dao.*;
import com.platform.modules.mall.dto.BuyGoodsRequest;
import com.platform.modules.mall.entity.*;
import com.platform.modules.mall.service.MallOrderService;
import com.platform.modules.mall.service.MallGoodsService;
import com.platform.modules.custom.service.CustomDesignService;
import com.platform.modules.custom.entity.CustomDesignSnapshotEntity;
import com.platform.modules.wx.entity.WxUserEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 订单服务实现。
 * <p>
 * 基于 MyBatis-Plus 的 ServiceImpl 提供订单模块的 CRUD 与分页查询实现。
 * </p>
 *
 * @author 何海龙
 * @since 2026-03-15 14:30:00
 */
@Service("mallOrderService")
public class MallOrderServiceImpl extends ServiceImpl<MallOrderDao, MallOrderEntity> implements MallOrderService {
    @Autowired
    private MallAddressDao addressDao;
    @Autowired
    private MallCartDao cartDao;
    @Autowired
    private MallProductDao productDao;
    @Autowired
    private MallOrderGoodsDao orderGoodsDao;
    @Autowired
    private MallCouponDao couponDao;
    @Autowired
    private MallUserCouponDao userCouponDao;
    @Autowired
    private MallGoodsService goodsService;
    @Autowired
    private RedisTemplateUtil redisTemplateUtil;
    @Autowired
    private CustomDesignService customDesignService;

    /**
     * 查询订单列表。
     */
    @Override
    public List<MallOrderEntity> queryAll(Map<String, Object> params) {
        return baseMapper.queryAll(params);
    }

    /**
     * 按 QueryWrapper 查询订单列表。
     */
    @Override
    public List<MallOrderEntity> queryAllByWrapper(QueryWrapper<MallOrderEntity> queryWrapper) {
        return this.list(queryWrapper);
    }

    /**
     * 分页查询订单，并按 ID 倒序。
     */
    @Override
    public Page<MallOrderEntity> queryPage(Map<String, Object> params) {
        params.put("sidx", "T.id");
        params.put("asc", false);
        Page<MallOrderEntity> page = new Query<MallOrderEntity>(params).getPage();
        return page.setRecords(baseMapper.selectMallOrderPage(page, params));
    }

    /**
     * 新增订单。
     */
    @Override
    public boolean add(MallOrderEntity mallOrder) {
        return this.save(mallOrder);
    }

    /**
     * 修改订单。
     */
    @Override
    public boolean update(MallOrderEntity mallOrder) {
        return this.updateById(mallOrder);
    }

    /**
     * 删除订单。
     */
    @Override
    public boolean delete(Integer id) {
        return this.removeById(id);
    }

    /**
     * 批量删除订单。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBatch(Integer[] ids) {
        return this.removeByIds(Arrays.asList(ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submit(JSONObject jsonParam, WxUserEntity loginUser) {
        Map<String, Object> resultObj = new HashMap<String, Object>();

        Integer couponId = jsonParam.getInteger("couponId");
        String type = jsonParam.getString("type");
        String postscript = jsonParam.getString("postscript");
        Long requestedDesignSnapshotId = jsonParam.getLong("designSnapshotId");
        Long designSnapshotId = requestedDesignSnapshotId;
        MallAddressEntity addressVo = addressDao.selectById(jsonParam.getInteger("addressId"));
        if (addressVo == null || !loginUser.getId().equals(addressVo.getUserId())) {
            throw new IllegalArgumentException("收货地址不存在或无权使用");
        }
        if (!"cart".equals(type) && !"buy".equals(type)) {
            throw new IllegalArgumentException("不支持的购买类型");
        }
        if (requestedDesignSnapshotId != null && "cart".equals(type)) {
            throw new IllegalArgumentException("定制商品仅支持立即购买");
        }

        Integer freightPrice = 0;

        // * 获取要购买的商品
        List<MallCartEntity> checkedGoodsList = new ArrayList<>();
        BigDecimal goodsTotalPrice;
        if ("cart".equals(type)) {
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("USER_ID", loginUser.getId());
            param.put("SESSION_ID", 1);
            param.put("CHECKED", 1);
            checkedGoodsList = cartDao.selectByMap(param);
            if (checkedGoodsList == null || checkedGoodsList.isEmpty()) {
                resultObj.put("code", 400);
                resultObj.put("msg", "请选择商品");
                return resultObj;
            }
            //统计商品总价
            goodsTotalPrice = new BigDecimal(0.00);
            for (MallCartEntity cartItem : checkedGoodsList) {
                MallProductEntity currentProduct = validateProduct(
                        cartItem.getProductId(), cartItem.getGoodsId(), cartItem.getNumber());
                cartItem.setRetailPrice(currentProduct.getRetailPrice());
                goodsTotalPrice = goodsTotalPrice.add(
                        currentProduct.getRetailPrice().multiply(BigDecimal.valueOf(cartItem.getNumber())));
            }
        } else {
            BuyGoodsRequest goodsVo = (BuyGoodsRequest) redisTemplateUtil.get(Constant.MTM_CACHE + "goods" + loginUser.getId());
            if (goodsVo == null) {
                throw new IllegalArgumentException("购买信息已失效，请重新选择商品");
            }
            Long contextDesignSnapshotId = goodsVo.getDesignSnapshotId();
            if (requestedDesignSnapshotId != null && contextDesignSnapshotId != null
                    && !requestedDesignSnapshotId.equals(contextDesignSnapshotId)) {
                throw new IllegalArgumentException("定制设计与当前结算信息不一致，请重新结算");
            }
            designSnapshotId = contextDesignSnapshotId != null
                    ? contextDesignSnapshotId : requestedDesignSnapshotId;
            MallProductEntity productInfo = validateProduct(
                    goodsVo.getProductId(), goodsVo.getGoodsId(), goodsVo.getNumber());
            //计算订单的费用
            //商品总价
            goodsTotalPrice = productInfo.getRetailPrice().multiply(BigDecimal.valueOf(goodsVo.getNumber()));
            if (designSnapshotId != null) {
                if (goodsVo.getNumber() != 1) {
                    throw new IllegalArgumentException("每个定制设计只能购买一件");
                }
                CustomDesignSnapshotEntity snapshot = customDesignService.getOwned(
                        designSnapshotId, loginUser.getId());
                if (!goodsVo.getGoodsId().equals(snapshot.getGoodsId())) {
                    throw new IllegalArgumentException("设计与购买商品不匹配");
                }
                goodsTotalPrice = customDesignService.lockAndGetPrice(designSnapshotId, loginUser.getId());
            }

            MallCartEntity MallCartEntity = new MallCartEntity();
            BeanUtils.copyProperties(productInfo, MallCartEntity);
            MallCartEntity.setNumber(goodsVo.getNumber());
            MallCartEntity.setProductId(goodsVo.getProductId());
            checkedGoodsList.add(MallCartEntity);
        }


        //获取订单使用的优惠券
        BigDecimal couponPrice = new BigDecimal("0.00");
        MallCouponEntity couponVo = null;
        if (couponId != null && couponId != 0) {
            couponVo = couponDao.getUserCoupon(couponId, loginUser.getId());
            couponPrice = validateCoupon(couponVo, goodsTotalPrice);
        }

        //订单价格计算
        BigDecimal orderTotalPrice = goodsTotalPrice.add(new BigDecimal(freightPrice));

        BigDecimal maximumDiscount = orderTotalPrice.subtract(new BigDecimal("0.01"));
        couponPrice = couponPrice.min(maximumDiscount.max(BigDecimal.ZERO));
        BigDecimal actualPrice = orderTotalPrice.subtract(couponPrice);

        //
        MallOrderEntity orderInfo = new MallOrderEntity();
        orderInfo.setOrderSn(StringUtils.generateOrderNumber());
        orderInfo.setUserId(loginUser.getId());
        //收货地址和运费
        orderInfo.setConsignee(addressVo.getUserName());
        orderInfo.setMobile(addressVo.getTelNumber());
        orderInfo.setCountry(addressVo.getNationalCode());
        orderInfo.setProvince(addressVo.getProvinceName());
        orderInfo.setCity(addressVo.getCityName());
        orderInfo.setDistrict(addressVo.getCountyName());
        orderInfo.setAddress(addressVo.getDetailInfo());
        //
        orderInfo.setFreightPrice(freightPrice);
        //留言
        orderInfo.setPostscript(postscript);
        //使用的优惠券
        orderInfo.setCouponId(couponId);
        orderInfo.setCouponPrice(couponPrice);
        orderInfo.setAddTime(new Date());
        orderInfo.setGoodsPrice(goodsTotalPrice);
        orderInfo.setOrderPrice(orderTotalPrice);
        orderInfo.setActualPrice(actualPrice);
        // 待付款
        orderInfo.setOrderStatus(0);
        orderInfo.setShippingStatus(0);
        orderInfo.setPayStatus(0);
        orderInfo.setShippingId(0);
        orderInfo.setShippingFee(new BigDecimal(0));
        orderInfo.setIntegral(0);
        orderInfo.setIntegralMoney(new BigDecimal(0));
        if ("cart".equals(type)) {
            orderInfo.setOrderType("1");
        } else {
            orderInfo.setOrderType("4");
        }

        //开启事务，插入订单信息和订单商品
        baseMapper.insert(orderInfo);
        if (null == orderInfo.getId()) {
            resultObj.put("code", 1);
            resultObj.put("msg", "订单提交失败");
            return resultObj;
        }
        if (couponVo != null && userCouponDao.claim(
                couponId, loginUser.getId(), orderInfo.getId(), new Date()) != 1) {
            throw new IllegalArgumentException("优惠券已被使用，请重新结算");
        }
        //统计商品总价
        List<MallOrderGoodsEntity> orderGoodsData = new ArrayList<MallOrderGoodsEntity>();
        for (MallCartEntity goodsItem : checkedGoodsList) {
            if (productDao.deductStock(goodsItem.getProductId(), goodsItem.getNumber()) != 1) {
                throw new IllegalArgumentException("商品库存不足，请重新结算");
            }
            MallOrderGoodsEntity orderGoodsVo = new MallOrderGoodsEntity();
            orderGoodsVo.setOrderId(orderInfo.getId());
            orderGoodsVo.setGoodsId(goodsItem.getGoodsId());
            orderGoodsVo.setGoodsSn(goodsItem.getGoodsSn());
            orderGoodsVo.setProductId(goodsItem.getProductId());
            orderGoodsVo.setGoodsName(goodsItem.getGoodsName());
            orderGoodsVo.setListPicUrl(goodsItem.getListPicUrl());
            orderGoodsVo.setMarketPrice(goodsItem.getMarketPrice());
            orderGoodsVo.setRetailPrice(goodsItem.getRetailPrice());
            if (designSnapshotId != null) {
                orderGoodsVo.setRetailPrice(goodsTotalPrice);
            }
            orderGoodsVo.setNumber(goodsItem.getNumber());
            orderGoodsVo.setGoodsSpecifitionNameValue(goodsItem.getGoodsSpecifitionNameValue());
            orderGoodsVo.setGoodsSpecifitionIds(goodsItem.getGoodsSpecifitionIds());
            orderGoodsData.add(orderGoodsVo);
            orderGoodsDao.insert(orderGoodsVo);
        }
        if (designSnapshotId != null) {
            customDesignService.bindOrder(designSnapshotId, loginUser.getId(), orderInfo.getId(),
                    orderGoodsData.get(0).getId());
        }

        // 清理本次购买上下文 / Clears the current purchase context.
        if ("cart".equals(type)) {
            cartDao.deleteByCart(loginUser.getId(), 1, 1);
        } else {
            redisTemplateUtil.del(Constant.MTM_CACHE + "goods" + loginUser.getId());
        }
        resultObj.put("code", 0);
        resultObj.put("msg", "订单提交成功");
        //
        Map<String, MallOrderEntity> orderInfoMap = new HashMap<>();
        orderInfoMap.put("orderInfo", orderInfo);
        //
        resultObj.put("data", orderInfoMap);
        return resultObj;
    }

    /** 校验商品、规格和库存 / Validates goods, SKU, and stock. */
    private MallProductEntity validateProduct(Integer productId, Integer goodsId, Integer number) {
        if (productId == null || goodsId == null || number == null || number <= 0) {
            throw new IllegalArgumentException("商品购买参数不正确");
        }
        MallProductEntity product = productDao.selectById(productId);
        MallGoodsEntity goods = goodsService.getById(goodsId);
        if (product == null || !goodsId.equals(product.getGoodsId())) {
            throw new IllegalArgumentException("商品规格不存在");
        }
        if (goods == null || !Integer.valueOf(1).equals(goods.getIsOnSale())) {
            throw new IllegalArgumentException("商品已下架");
        }
        if (product.getGoodsNumber() == null || product.getGoodsNumber() < number) {
            throw new IllegalArgumentException("商品库存不足");
        }
        return product;
    }

    /** 校验优惠券使用条件 / Validates coupon usage conditions. */
    private BigDecimal validateCoupon(MallCouponEntity coupon, BigDecimal goodsTotalPrice) {
        if (coupon == null || !Integer.valueOf(1).equals(coupon.getCouponStatus())) {
            throw new IllegalArgumentException("优惠券不存在或已使用");
        }
        Date now = new Date();
        if ((coupon.getUseStartDate() != null && now.before(coupon.getUseStartDate()))
                || (coupon.getUseEndDate() != null && now.after(coupon.getUseEndDate()))) {
            throw new IllegalArgumentException("优惠券不在有效期内");
        }
        if (coupon.getMinGoodsAmount() != null
                && goodsTotalPrice.compareTo(coupon.getMinGoodsAmount()) < 0) {
            throw new IllegalArgumentException("订单金额未达到优惠券使用门槛");
        }
        return Objects.requireNonNullElse(coupon.getTypeMoney(), BigDecimal.ZERO).max(BigDecimal.ZERO);
    }

    @Override
    public List<MallOrderEntity> queryList(Map<String, Object> params) {
        return baseMapper.queryList(params);
    }

    /** 取消未支付订单并恢复库存和优惠券 / Cancels an unpaid order and restores resources. */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelUnpaid(Integer orderId, Long userId) {
        MallOrderEntity order = baseMapper.selectById(orderId);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new IllegalArgumentException("订单不存在或无权访问");
        }
        if (!Integer.valueOf(0).equals(order.getPayStatus())
                || !Integer.valueOf(0).equals(order.getOrderStatus())) {
            throw new IllegalArgumentException("订单状态不允许取消");
        }
        if (baseMapper.cancelUnpaid(orderId, userId) != 1) {
            throw new IllegalArgumentException("订单状态已变化，请刷新后重试");
        }
        restoreOrderResources(orderId);
        customDesignService.cancelOrder(orderId);
    }

    /** 完成退款并确保资源只恢复一次 / Completes a refund and restores resources once. */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeRefund(Integer orderId, Integer refundedOrderStatus) {
        MallOrderEntity order = baseMapper.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (Integer.valueOf(4).equals(order.getPayStatus())) {
            return;
        }
        if (!Set.of(401, 402).contains(refundedOrderStatus)) {
            throw new IllegalArgumentException("退款订单状态不正确");
        }
        if (baseMapper.markRefunded(orderId, refundedOrderStatus) != 1) {
            throw new IllegalArgumentException("订单状态不允许退款");
        }
        restoreOrderResources(orderId);
        customDesignService.cancelOrder(orderId);
    }

    /** 恢复订单库存并释放优惠券 / Restores order stock and releases its coupon. */
    private void restoreOrderResources(Integer orderId) {
        List<MallOrderGoodsEntity> goods = orderGoodsDao.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MallOrderGoodsEntity>()
                        .eq(MallOrderGoodsEntity::getOrderId, orderId));
        for (MallOrderGoodsEntity item : goods) {
            productDao.restoreStock(item.getProductId(), item.getNumber());
        }
        userCouponDao.releaseByOrder(orderId);
    }
}
