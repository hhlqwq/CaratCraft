package com.platform.modules.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.platform.modules.app.request.ApiBuyAddRequest;
import com.platform.modules.custom.entity.CustomDesignSnapshotEntity;
import com.platform.modules.custom.service.CustomDesignService;
import com.platform.modules.mall.dao.MallCouponDao;
import com.platform.modules.mall.dao.MallProductDao;
import com.platform.modules.mall.dto.BuyGoodsRequest;
import com.platform.modules.mall.entity.MallGoodsEntity;
import com.platform.modules.mall.entity.MallProductEntity;
import com.platform.modules.mall.service.CheckoutPricingService;
import com.platform.modules.mall.service.MallGoodsService;
import com.platform.modules.mall.service.PurchaseContextService;
import com.platform.modules.wx.entity.WxUserEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

/** 使用真实商品复核逻辑验证上下文入口, 不访问Redis、支付或业务库. */
class AppBuyControllerTest {
    private AppBuyController controller;
    private PurchaseContextService contexts;
    private CustomDesignService designs;
    private MallProductEntity product;
    private MallGoodsEntity goods;
    private ApiBuyAddRequest request;
    private WxUserEntity user;

    /** 准备一件在售商品及本人请求, 缓存仅观察是否发生写入. */
    @BeforeEach
    void setUp() {
        contexts = mock(PurchaseContextService.class);
        designs = mock(CustomDesignService.class);
        MallProductDao products = mock(MallProductDao.class);
        MallGoodsService catalog = mock(MallGoodsService.class);
        product = new MallProductEntity();
        product.setGoodsId(10);
        product.setGoodsNumber(2);
        product.setRetailPrice(new BigDecimal("80.00"));
        goods = new MallGoodsEntity();
        goods.setIsDelete(0);
        goods.setIsOnSale(1);
        when(products.selectById(20)).thenReturn(product);
        when(catalog.getById(10)).thenReturn(goods);
        CheckoutPricingService pricing =
                new CheckoutPricingService(mock(MallCouponDao.class), products, catalog);
        controller = new AppBuyController(contexts, designs, pricing);
        request = new ApiBuyAddRequest();
        request.setGoodsId(10);
        request.setProductId(20);
        request.setNumber(1);
        user = new WxUserEntity();
        user.setId(7L);
    }

    /** 参数缺失及数量越界在缓存写入前拒绝. */
    @Test
    void invalidParametersNeverCreateContext() {
        for (Integer number : new Integer[] {null, 0, -1, 10000}) {
            request.setNumber(number);
            assertNotEquals(0, controller.addBuy(user, request).getCode());
        }
        request.setNumber(1);
        request.setProductId(null);
        assertNotEquals(0, controller.addBuy(user, request).getCode());
        verifyNoInteractions(contexts, designs);
    }

    /** 缺货、未知或不属于当前商品的SKU不能创建有效购买上下文. */
    @Test
    void invalidOrForeignProductNeverCreatesContext() {
        request.setNumber(3);
        assertNotEquals(0, controller.addBuy(user, request).getCode());
        request.setNumber(1);
        product.setGoodsId(11);
        assertNotEquals(0, controller.addBuy(user, request).getCode());
        request.setProductId(999);
        assertNotEquals(0, controller.addBuy(user, request).getCode());
        verifyNoInteractions(contexts, designs);
    }

    /** 商品下架、删除、库存未知或价格无效时均由公共校验拒绝. */
    @Test
    void unavailableGoodsNeverCreateContext() {
        goods.setIsOnSale(0);
        assertNotEquals(0, controller.addBuy(user, request).getCode());
        goods.setIsOnSale(1);
        goods.setIsDelete(1);
        assertNotEquals(0, controller.addBuy(user, request).getCode());
        goods.setIsDelete(0);
        product.setGoodsNumber(null);
        assertNotEquals(0, controller.addBuy(user, request).getCode());
        product.setGoodsNumber(2);
        product.setRetailPrice(BigDecimal.ZERO);
        assertNotEquals(0, controller.addBuy(user, request).getCode());
        verifyNoInteractions(contexts, designs);
    }

    /** 快照须归本人且对应当前商品及可编辑状态, 拒绝不改变缓存. */
    @Test
    void invalidDesignNeverCreatesContext() {
        request.setDesignSnapshotId(9L);
        when(designs.getOwned(9L, 7L)).thenThrow(new IllegalArgumentException("设计不属于本人"));
        assertNotEquals(0, controller.addBuy(user, request).getCode());
        CustomDesignSnapshotEntity design = new CustomDesignSnapshotEntity();
        design.setGoodsId(11);
        design.setStatus("DRAFT");
        doReturn(design).when(designs).getOwned(9L, 7L);
        assertNotEquals(0, controller.addBuy(user, request).getCode());
        design.setGoodsId(10);
        design.setStatus("ORDERED");
        assertNotEquals(0, controller.addBuy(user, request).getCode());
        verifyNoInteractions(contexts);
    }

    /** 合法请求仅保存本人已验证的商品、数量和设计, 不信任客户端金额. */
    @Test
    void validPurchaseCreatesOwnedContext() {
        when(contexts.create(eq(7L), any())).thenReturn("verified-context");
        assertEquals(0, controller.addBuy(user, request).getCode());
        ArgumentCaptor<BuyGoodsRequest> captured = ArgumentCaptor.forClass(BuyGoodsRequest.class);
        verify(contexts).create(eq(7L), captured.capture());
        assertEquals(10, captured.getValue().getGoodsId());
        assertEquals(20, captured.getValue().getProductId());
        assertEquals(1, captured.getValue().getNumber());
        verifyNoInteractions(designs);
    }
}
