package com.platform.modules.mall.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson.JSONObject;
import com.platform.common.exception.BusinessException;
import com.platform.modules.custom.service.CustomDesignService;
import com.platform.modules.mall.dao.MallAddressDao;
import com.platform.modules.mall.dao.MallCartDao;
import com.platform.modules.mall.dao.MallCouponDao;
import com.platform.modules.mall.dao.MallOrderDao;
import com.platform.modules.mall.dao.MallOrderGoodsDao;
import com.platform.modules.mall.dao.MallProductDao;
import com.platform.modules.mall.dao.MallUserCouponDao;
import com.platform.modules.mall.entity.MallAddressEntity;
import com.platform.modules.mall.entity.MallCartEntity;
import com.platform.modules.mall.entity.MallCouponEntity;
import com.platform.modules.mall.entity.MallGoodsEntity;
import com.platform.modules.mall.entity.MallOrderEntity;
import com.platform.modules.mall.entity.MallProductEntity;
import com.platform.modules.mall.service.impl.MallOrderServiceImpl;
import com.platform.modules.wx.dao.WxUserDao;
import com.platform.modules.wx.entity.WxUserEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/** 验证普通订单重试、意图冲突和优惠金额边界, 不连接真实支付. */
class CheckoutIntegrityTest {
    private MallOrderServiceImpl orders;
    private MallOrderDao orderDao;
    private MallProductDao products;
    private MallCouponDao coupons;
    private CheckoutPricingService pricing;
    private WxUserEntity user;
    private JSONObject request;

    /** 使用真实计价逻辑和可观察的持久化替身准备一次购物车结算. */
    @BeforeEach
    void setUp() {
        orders = new MallOrderServiceImpl();
        ReflectionTestUtils.setField(orders, "legalDocuments", mock(LegalDocumentService.class));
        orderDao = mock(MallOrderDao.class);
        products = mock(MallProductDao.class);
        coupons = mock(MallCouponDao.class);
        MallGoodsService goods = mock(MallGoodsService.class);
        pricing = new CheckoutPricingService(coupons, products, goods);
        MallProductEntity product = new MallProductEntity();
        product.setId(20);
        product.setGoodsId(10);
        product.setRetailPrice(new BigDecimal("99.00"));
        product.setGoodsNumber(10);
        MallGoodsEntity item = new MallGoodsEntity();
        item.setId(10);
        item.setIsOnSale(1);
        when(products.selectById(20)).thenReturn(product);
        when(products.deductStock(20, 1)).thenReturn(1);
        when(goods.getById(10)).thenReturn(item);
        user = new WxUserEntity();
        user.setId(7L);
        WxUserDao users = mock(WxUserDao.class);
        when(users.selectOne(any())).thenReturn(user);
        MallAddressEntity address = new MallAddressEntity();
        address.setUserId(7L);
        MallAddressDao addresses = mock(MallAddressDao.class);
        when(addresses.selectById(1)).thenReturn(address);
        MallCartEntity cart = new MallCartEntity();
        cart.setGoodsId(10);
        cart.setProductId(20);
        cart.setNumber(1);
        MallCartDao carts = mock(MallCartDao.class);
        when(carts.selectByMap(any())).thenReturn(List.of(cart));
        AtomicReference<MallOrderEntity> stored = new AtomicReference<>();
        when(orderDao.selectOne(any())).thenAnswer(call -> stored.get());
        doAnswer(
                        call -> {
                            MallOrderEntity order = call.getArgument(0);
                            order.setId(100);
                            stored.set(order);
                            return 1;
                        })
                .when(orderDao)
                .insert(any(MallOrderEntity.class));
        Map<String, Object> dependencies =
                Map.of(
                        "baseMapper",
                        orderDao,
                        "productDao",
                        products,
                        "wxUserDao",
                        users,
                        "addressDao",
                        addresses,
                        "cartDao",
                        carts,
                        "checkoutPricing",
                        pricing,
                        "orderGoodsDao",
                        mock(MallOrderGoodsDao.class),
                        "userCouponDao",
                        mock(MallUserCouponDao.class),
                        "customDesignService",
                        mock(CustomDesignService.class));
        dependencies.forEach((name, value) -> ReflectionTestUtils.setField(orders, name, value));
        request = new JSONObject();
        request.put("requestKey", "checkout_same_request_01");
        request.put("type", "cart");
        request.put("addressId", 1);
        request.put("expectedActualPrice", new BigDecimal("99.00"));
    }

    /** 新协议尚未确认时不创建订单, 也不扣减商品库存. */
    @Test
    void changedPolicyStopsNewOrderBeforeStockOrOrderWrite() {
        LegalDocumentService documents = mock(LegalDocumentService.class);
        org.mockito.Mockito.doThrow(new BusinessException("需要确认", 428))
                .when(documents)
                .requireAccepted(7L);
        ReflectionTestUtils.setField(orders, "legalDocuments", documents);
        assertThrows(BusinessException.class, () -> orders.submit(request, user));
        verify(orderDao, never()).insert(any(MallOrderEntity.class));
        verify(products, never()).deductStock(anyInt(), anyInt());
    }

    /** 网络重试返回原订单且不二次扣库存. */
    @Test
    void retryReturnsPersistedOrderOnce() {
        assertEquals(0, orders.submit(request, user).get("code"));
        assertEquals(0, orders.submit(request, user).get("code"));
        verify(orderDao, times(1)).insert(any(MallOrderEntity.class));
        verify(products, times(1)).deductStock(20, 1);
    }

    /** 同一键不能悄悄变成另一张订单. */
    @Test
    void changedIntentCannotReuseKey() {
        orders.submit(request, user);
        request.put("addressId", 2);
        assertThrows(IllegalArgumentException.class, () -> orders.submit(request, user));
        verify(products, times(1)).deductStock(20, 1);
    }

    /** 价格变化需用户重新确认, 不创建订单或扣减库存. */
    @Test
    void changedPriceRequiresConfirmation() {
        request.put("expectedActualPrice", new BigDecimal("98.00"));
        assertThrows(IllegalArgumentException.class, () -> orders.submit(request, user));
        verify(orderDao, never()).insert(any(MallOrderEntity.class));
        verify(products, never()).deductStock(anyInt(), anyInt());
    }

    /** 超额券最多减到一分, 无券和不可用券在预览与提交使用同一逻辑. */
    @Test
    void couponFloorAndInvalidCoupon() {
        MallCouponEntity coupon = new MallCouponEntity();
        coupon.setCouponStatus(1);
        coupon.setTypeMoney(new BigDecimal("200"));
        when(coupons.getUserCoupon(1, 7L)).thenReturn(coupon);
        assertEquals(
                new BigDecimal("0.01"),
                pricing.calculate(7L, 1, new BigDecimal("99.00"), BigDecimal.ZERO)
                        .get("actualPrice"));
        coupon.setMinGoodsAmount(new BigDecimal("100"));
        assertThrows(
                IllegalArgumentException.class,
                () -> pricing.calculate(7L, 1, new BigDecimal("99"), BigDecimal.ZERO));
        assertThrows(
                IllegalArgumentException.class,
                () -> pricing.calculate(7L, 9, new BigDecimal("99"), BigDecimal.ZERO));
    }
}
