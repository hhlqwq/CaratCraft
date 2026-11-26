package com.platform.modules.mall.service;

import com.platform.modules.mall.dao.MallCouponDao;
import com.platform.modules.mall.dao.MallProductDao;
import com.platform.modules.mall.entity.MallCouponEntity;
import com.platform.modules.mall.entity.MallProductEntity;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/** 结算预览与订单提交共享相同的优惠券和金额规则. */
@Service
public class CheckoutPricingService {
    private final MallCouponDao coupons;
    private final MallProductDao products;
    private final MallGoodsService goods;

    /** 注入包含用户归属的优惠券查询. */
    public CheckoutPricingService(
            MallCouponDao coupons, MallProductDao products, MallGoodsService goods) {
        this.coupons = coupons;
        this.products = products;
        this.goods = goods;
    }

    /** 预览和提交均读取当前SKU价格、商品状态及可售库存. */
    public MallProductEntity requireProduct(Integer productId, Integer goodsId, Integer number) {
        if (productId == null || goodsId == null || number == null || number < 1 || number > 9999) {
            throw new IllegalArgumentException("商品购买参数不正确");
        }
        MallProductEntity product = products.selectById(productId);
        var item = goods.getById(goodsId);
        if (product == null
                || !goodsId.equals(product.getGoodsId())
                || item == null
                || Integer.valueOf(1).equals(item.getIsDelete())
                || !Integer.valueOf(1).equals(item.getIsOnSale())
                || product.getGoodsNumber() == null
                || product.getGoodsNumber() < number
                || product.getRetailPrice() == null
                || product.getRetailPrice().signum() <= 0) {
            throw new IllegalArgumentException("商品已下架、规格失效或库存/价格不可用");
        }
        return product;
    }

    /** 校验归属、有效期、门槛和状态, 最大优惠保留一分实际付款. */
    public Map<String, BigDecimal> calculate(
            Long userId, Integer couponId, BigDecimal goods, BigDecimal freight) {
        if (goods == null || goods.signum() <= 0 || freight == null || freight.signum() < 0) {
            throw new IllegalArgumentException("商品价格或运费配置不正确");
        }
        BigDecimal discount = BigDecimal.ZERO;
        if (couponId != null && couponId != 0) {
            MallCouponEntity coupon = coupons.getUserCoupon(couponId, userId);
            Date now = new Date();
            boolean welcome = coupon != null && WelcomeCoupon.SOURCE.equals(coupon.getSourceKey());
            Date start = coupon == null ? null : welcome ? coupon.getGrantedAt() : coupon.getUseStartDate();
            Date end = coupon == null ? null : welcome ? WelcomeCoupon.expiresAt(start) : coupon.getUseEndDate();
            BigDecimal minimum = coupon == null ? null : welcome ? WelcomeCoupon.MINIMUM : coupon.getMinGoodsAmount();
            if (coupon == null
                    || !Integer.valueOf(1).equals(coupon.getCouponStatus())
                    || (welcome && end == null)
                    || (start != null && now.before(start))
                    || (end != null && !now.before(end))
                    || (minimum != null && goods.compareTo(minimum) < 0)) {
                throw new IllegalArgumentException("优惠券不可用、已过期或未达到使用门槛");
            }
            discount =
                    welcome ? WelcomeCoupon.DISCOUNT :
                            Objects.requireNonNullElse(coupon.getTypeMoney(), BigDecimal.ZERO).max(BigDecimal.ZERO);
        }
        BigDecimal total = goods.add(freight);
        discount = discount.min(total.subtract(new BigDecimal("0.01")).max(BigDecimal.ZERO));
        return Map.of(
                "goodsTotalPrice",
                goods,
                "freightPrice",
                freight,
                "couponPrice",
                discount,
                "orderTotalPrice",
                total,
                "actualPrice",
                total.subtract(discount));
    }
}
