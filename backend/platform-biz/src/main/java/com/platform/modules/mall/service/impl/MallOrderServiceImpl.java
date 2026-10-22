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
import com.platform.common.utils.Query;
import com.platform.common.utils.StringUtils;
import com.platform.config.RedisTemplateUtil;
import com.platform.modules.custom.entity.CustomDesignSnapshotEntity;
import com.platform.modules.custom.service.CustomDesignService;
import com.platform.modules.mall.dao.MallAddressDao;
import com.platform.modules.mall.dao.MallCartDao;
import com.platform.modules.mall.dao.MallCouponDao;
import com.platform.modules.mall.dao.MallOrderDao;
import com.platform.modules.mall.dao.MallOrderGoodsDao;
import com.platform.modules.mall.dao.MallProductDao;
import com.platform.modules.mall.dao.MallUserCouponDao;
import com.platform.modules.mall.dto.BuyGoodsRequest;
import com.platform.modules.mall.entity.MallAddressEntity;
import com.platform.modules.mall.entity.MallCartEntity;
import com.platform.modules.mall.entity.MallOrderEntity;
import com.platform.modules.mall.entity.MallOrderGoodsEntity;
import com.platform.modules.mall.entity.MallProductEntity;
import com.platform.modules.mall.service.LegalDocumentService;
import com.platform.modules.mall.service.MallGoodsService;
import com.platform.modules.mall.service.MallOrderService;
import com.platform.modules.wx.entity.WxUserEntity;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 订单服务实现。
 *
 * <p>基于 MyBatis-Plus 的 ServiceImpl 提供订单模块的 CRUD 与分页查询实现。
 *
 * @author 何海龙
 * @since 2026-03-15 14:30:00
 */
@Service("mallOrderService")
public class MallOrderServiceImpl extends ServiceImpl<MallOrderDao, MallOrderEntity>
        implements MallOrderService {
    @Autowired private MallAddressDao addressDao;
    @Autowired private MallCartDao cartDao;
    @Autowired private MallProductDao productDao;
    @Autowired private MallOrderGoodsDao orderGoodsDao;
    @Autowired private MallCouponDao couponDao;
    @Autowired private MallUserCouponDao userCouponDao;
    @Autowired private MallGoodsService goodsService;
    @Autowired private RedisTemplateUtil redisTemplateUtil;
    @Autowired private CustomDesignService customDesignService;
    @Autowired private LegalDocumentService legalDocuments;

    @Autowired
    private org.springframework.beans.factory.ObjectProvider<
                    com.github.binarywang.wxpay.service.WxPayService>
            paymentGateway;

    @Autowired
    private org.springframework.beans.factory.ObjectProvider<
                    com.platform.modules.mall.service.OrderPaymentService>
            paymentResults;

    @Autowired private com.platform.modules.mall.service.PurchaseContextService purchaseContexts;
    @Autowired private com.platform.modules.mall.service.CheckoutPricingService checkoutPricing;
    @Autowired private com.platform.modules.wx.dao.WxUserDao wxUserDao;

    /** 查询订单列表。 */
    @Override
    public List<MallOrderEntity> queryAll(Map<String, Object> params) {
        return baseMapper.queryAll(params);
    }

    /** 按 QueryWrapper 查询订单列表。 */
    @Override
    public List<MallOrderEntity> queryAllByWrapper(QueryWrapper<MallOrderEntity> queryWrapper) {
        return this.list(queryWrapper);
    }

    /** 分页查询订单，并按 ID 倒序。 */
    @Override
    public Page<MallOrderEntity> queryPage(Map<String, Object> params) {
        params.put("sidx", "T.id");
        params.put("asc", false);
        Page<MallOrderEntity> page = new Query<MallOrderEntity>(params).getPage();
        return page.setRecords(baseMapper.selectMallOrderPage(page, params));
    }

    /** 新增订单。 */
    @Override
    public boolean add(MallOrderEntity mallOrder) {
        return this.save(mallOrder);
    }

    /** 修改订单。 */
    @Override
    public boolean update(MallOrderEntity mallOrder) {
        return this.updateById(mallOrder);
    }

    /** 订单行锁必须由外层业务事务持有至提交, 禁止先读后无条件覆盖. */
    @Override
    public MallOrderEntity lockById(Integer orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("订单ID不能为空");
        }
        return getOne(new QueryWrapper<MallOrderEntity>().eq("id", orderId).last("FOR UPDATE"));
    }

    /** 删除订单。 */
    @Override
    public boolean delete(Integer id) {
        return this.removeById(id);
    }

    /** 批量删除订单。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBatch(Integer[] ids) {
        return this.removeByIds(Arrays.asList(ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submit(JSONObject jsonParam, WxUserEntity loginUser) {
        Map<String, Object> resultObj = new HashMap<String, Object>();
        // 同一用户提交串行化, 幂等记录与订单、扣库存处于同一数据库事务.
        if (wxUserDao.selectOne(
                        new QueryWrapper<WxUserEntity>()
                                .eq("id", loginUser.getId())
                                .last("FOR UPDATE"))
                == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        String requestKey = jsonParam.getString("requestKey");
        if (requestKey == null || !requestKey.matches("[A-Za-z0-9_-]{16,80}")) {
            throw new IllegalArgumentException("缺少有效的订单提交键, 请重新结算");
        }
        if ("buy".equals(jsonParam.getString("type"))) {
            requestKey = "buy_" + jsonParam.getString("checkoutId");
        }
        java.util.Map<String, Object> intent = new java.util.TreeMap<>();
        for (String name :
                java.util.List.of(
                        "addressId",
                        "couponId",
                        "type",
                        "postscript",
                        "designSnapshotId",
                        "checkoutId",
                        "expectedActualPrice")) {
            intent.put(name, jsonParam.get(name));
        }
        String digest =
                org.apache.commons.codec.digest.DigestUtils.sha256Hex(
                        JSONObject.toJSONString(intent));
        MallOrderEntity previous =
                baseMapper.selectOne(
                        new QueryWrapper<MallOrderEntity>()
                                .eq("user_id", loginUser.getId())
                                .eq("request_key", requestKey));
        if (previous != null) {
            if (!digest.equals(previous.getRequestDigest())) {
                throw new IllegalArgumentException("本次结算已经提交, 请从订单列表查看原订单或重新购买");
            }
            return java.util.Map.of("code", 0, "data", java.util.Map.of("orderInfo", previous));
        }

        legalDocuments.requireAccepted(loginUser.getId());
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
            // 统计商品总价
            goodsTotalPrice = new BigDecimal(0.00);
            for (MallCartEntity cartItem : checkedGoodsList) {
                MallProductEntity currentProduct =
                        validateProduct(
                                cartItem.getProductId(),
                                cartItem.getGoodsId(),
                                cartItem.getNumber());
                cartItem.setRetailPrice(currentProduct.getRetailPrice());
                goodsTotalPrice =
                        goodsTotalPrice.add(
                                currentProduct
                                        .getRetailPrice()
                                        .multiply(BigDecimal.valueOf(cartItem.getNumber())));
            }
        } else {
            BuyGoodsRequest goodsVo =
                    purchaseContexts.require(loginUser.getId(), jsonParam.getString("checkoutId"));
            if (goodsVo == null) {
                throw new IllegalArgumentException("购买信息已失效，请重新选择商品");
            }
            Long contextDesignSnapshotId = goodsVo.getDesignSnapshotId();
            if (requestedDesignSnapshotId != null
                    && contextDesignSnapshotId != null
                    && !requestedDesignSnapshotId.equals(contextDesignSnapshotId)) {
                throw new IllegalArgumentException("定制设计与当前结算信息不一致，请重新结算");
            }
            designSnapshotId =
                    contextDesignSnapshotId != null
                            ? contextDesignSnapshotId
                            : requestedDesignSnapshotId;
            MallProductEntity productInfo =
                    validateProduct(
                            goodsVo.getProductId(), goodsVo.getGoodsId(), goodsVo.getNumber());
            // 计算订单的费用
            // 商品总价
            goodsTotalPrice =
                    productInfo.getRetailPrice().multiply(BigDecimal.valueOf(goodsVo.getNumber()));
            if (designSnapshotId != null) {
                if (goodsVo.getNumber() != 1) {
                    throw new IllegalArgumentException("每个定制设计只能购买一件");
                }
                CustomDesignSnapshotEntity snapshot =
                        customDesignService.getOwned(designSnapshotId, loginUser.getId());
                if (!goodsVo.getGoodsId().equals(snapshot.getGoodsId())) {
                    throw new IllegalArgumentException("设计与购买商品不匹配");
                }
                goodsTotalPrice =
                        customDesignService.lockAndGetPrice(designSnapshotId, loginUser.getId());
            }

            MallCartEntity MallCartEntity = new MallCartEntity();
            BeanUtils.copyProperties(productInfo, MallCartEntity);
            MallCartEntity.setNumber(goodsVo.getNumber());
            MallCartEntity.setProductId(goodsVo.getProductId());
            checkedGoodsList.add(MallCartEntity);
        }

        Map<String, BigDecimal> prices =
                checkoutPricing.calculate(
                        loginUser.getId(),
                        couponId,
                        goodsTotalPrice,
                        BigDecimal.valueOf(freightPrice));
        BigDecimal couponPrice = prices.get("couponPrice");
        BigDecimal orderTotalPrice = prices.get("orderTotalPrice");
        BigDecimal actualPrice = prices.get("actualPrice");
        BigDecimal expected = jsonParam.getBigDecimal("expectedActualPrice");
        if (expected == null || expected.compareTo(actualPrice) != 0) {
            throw new IllegalArgumentException("价格或优惠已变化, 请刷新结算页面后确认");
        }
        boolean useCoupon = couponId != null && couponId != 0;

        //
        MallOrderEntity orderInfo = new MallOrderEntity();
        orderInfo.setOrderSn(StringUtils.generateOrderNumber());
        orderInfo.setRequestKey(requestKey);
        orderInfo.setRequestDigest(digest);
        orderInfo.setUserId(loginUser.getId());
        // 收货地址和运费
        orderInfo.setConsignee(addressVo.getUserName());
        orderInfo.setMobile(addressVo.getTelNumber());
        orderInfo.setCountry(addressVo.getNationalCode());
        orderInfo.setProvince(addressVo.getProvinceName());
        orderInfo.setCity(addressVo.getCityName());
        orderInfo.setDistrict(addressVo.getCountyName());
        orderInfo.setAddress(addressVo.getDetailInfo());
        //
        orderInfo.setFreightPrice(freightPrice);
        // 留言
        orderInfo.setPostscript(postscript);
        // 使用的优惠券
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

        // 开启事务，插入订单信息和订单商品
        baseMapper.insert(orderInfo);
        if (null == orderInfo.getId()) {
            resultObj.put("code", 1);
            resultObj.put("msg", "订单提交失败");
            return resultObj;
        }
        legalDocuments.freezeForOrder(orderInfo.getId(), loginUser.getId());
        if (useCoupon
                && userCouponDao.claim(couponId, loginUser.getId(), orderInfo.getId(), new Date())
                        != 1) {
            throw new IllegalArgumentException("优惠券已被使用，请重新结算");
        }
        // 统计商品总价
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
            customDesignService.bindOrder(
                    designSnapshotId,
                    loginUser.getId(),
                    orderInfo.getId(),
                    orderGoodsData.get(0).getId());
        }

        // 清理本次购买上下文 / Clears the current purchase context.
        if ("cart".equals(type)) {
            cartDao.deleteByCart(loginUser.getId(), 1, 1);
        }
        // 购买上下文保留至TTL到期, 网络重试由持久化提交键返回原订单.
        resultObj.put("code", 0);
        resultObj.put("msg", "订单提交成功");
        //
        Map<String, MallOrderEntity> orderInfoMap = new HashMap<>();
        orderInfoMap.put("orderInfo", orderInfo);
        //
        resultObj.put("data", orderInfoMap);
        return resultObj;
    }

    /** 预览和提交共享商品校验, 不使用客户端或购物车里的历史价格. */
    private MallProductEntity validateProduct(Integer productId, Integer goodsId, Integer number) {
        return checkoutPricing.requireProduct(productId, goodsId, number);
    }

    @Override
    public List<MallOrderEntity> queryList(Map<String, Object> params) {
        return baseMapper.queryList(params);
    }

    /** 取消未支付订单并恢复库存和优惠券 / Cancels an unpaid order and restores resources. */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelUnpaid(Integer orderId, Long userId) {
        MallOrderEntity order = lockById(orderId);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new IllegalArgumentException("订单不存在或无权访问");
        }
        if (Integer.valueOf(101).equals(order.getOrderStatus())) {
            return true;
        }
        if (!Set.of(0, 1).contains(order.getPayStatus())
                || !Integer.valueOf(0).equals(order.getOrderStatus())) {
            throw new IllegalArgumentException("订单状态不允许取消");
        }
        com.github.binarywang.wxpay.service.WxPayService gateway = paymentGateway.getIfAvailable();
        if (gateway == null
                && (Integer.valueOf(1).equals(order.getPayStatus())
                        || (order.getPayId() != null
                                && !order.getPayId().isBlank()
                                && !"0".equals(order.getPayId())))) {
            throw new IllegalStateException("支付服务未配置, 无法核对预支付订单");
        }
        if (gateway != null) {
            try {
                com.github.binarywang.wxpay.bean.result.WxPayOrderQueryResult result =
                        gateway.queryOrder("", order.getOrderSn());
                if ("SUCCESS".equals(result.getTradeState())) {
                    paymentResults.getObject().markPaid(orderId, result.getTotalFee());
                    return false;
                }
                if ("USERPAYING".equals(result.getTradeState())) {
                    return false;
                }
                if ("NOTPAY".equals(result.getTradeState())) {
                    gateway.closeOrder(order.getOrderSn());
                } else if (!Set.of("CLOSED", "REVOKED", "PAYERROR")
                        .contains(result.getTradeState())) {
                    throw new IllegalStateException("支付状态待核对, 暂不能关闭订单");
                }
            } catch (com.github.binarywang.wxpay.exception.WxPayException error) {
                if (!"ORDERNOTEXIST".equals(error.getErrCode())) {
                    throw new IllegalStateException("支付平台核对或关单失败, 请稍后重试");
                }
            }
        }
        if (baseMapper.cancelUnpaid(orderId, userId) != 1) {
            throw new IllegalArgumentException("订单状态已变化，请刷新后重试");
        }
        restoreOrderResources(orderId);
        customDesignService.cancelOrder(orderId);
        return true;
    }

    /** 完成退款并确保资源只恢复一次 / Completes a refund and restores resources once. */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeRefund(Integer orderId, Integer refundedOrderStatus) {
        MallOrderEntity order = lockById(orderId);
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
        if (Integer.valueOf(201).equals(order.getOrderStatus())) {
            restoreOrderResources(orderId);
            customDesignService.cancelOrder(orderId);
        }
        // 已发货/已收货退款不等于实物退回, 不自动增加成品和材料库存.
    }

    /** 恢复订单库存并释放优惠券 / Restores order stock and releases its coupon. */
    private void restoreOrderResources(Integer orderId) {
        List<MallOrderGoodsEntity> goods =
                orderGoodsDao.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<
                                        MallOrderGoodsEntity>()
                                .eq(MallOrderGoodsEntity::getOrderId, orderId));
        for (MallOrderGoodsEntity item : goods) {
            productDao.restoreStock(item.getProductId(), item.getNumber());
        }
        userCouponDao.releaseByOrder(orderId);
    }
}
