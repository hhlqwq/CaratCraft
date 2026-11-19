package com.platform.modules.app.controller;

import com.platform.annotation.LoginUser;
import com.platform.common.utils.RestResponse;
import com.platform.modules.app.request.ApiBuyAddRequest;
import com.platform.modules.custom.entity.CustomDesignSnapshotEntity;
import com.platform.modules.custom.service.CustomDesignService;
import com.platform.modules.mall.dto.BuyGoodsRequest;
import com.platform.modules.mall.service.CheckoutPricingService;
import com.platform.modules.mall.service.PurchaseContextService;
import com.platform.modules.wx.entity.WxUserEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author hehailong
 */
@Tag(name = "商品购买-AppBuyController")
@RestController
@RequestMapping("/app/buy")
@RequiredArgsConstructor
public class AppBuyController extends AppBaseController {
    private final PurchaseContextService purchaseContexts;
    private final CustomDesignService customDesignService;
    private final CheckoutPricingService checkoutPricing;

    /**
     * 创建立即购买上下文, 并将定制快照与商品一起保存在服务端 / Creates a server-side buy context with its design snapshot.
     */
    @Operation(summary = "商品添加")
    @PostMapping("/add")
    public RestResponse<Object> addBuy(
            @LoginUser WxUserEntity loginUser, @RequestBody ApiBuyAddRequest request) {
        Integer goodsId = request.getGoodsId();
        Integer productId = request.getProductId();
        Integer number = request.getNumber();
        Long designSnapshotId = request.getDesignSnapshotId();
        try {
            checkoutPricing.requireProduct(productId, goodsId, number);
            if (designSnapshotId != null) {
                CustomDesignSnapshotEntity snapshot =
                        customDesignService.getOwned(designSnapshotId, loginUser.getId());
                if (!goodsId.equals(snapshot.getGoodsId())) {
                    return RestResponse.fail("设计与购买商品不匹配");
                }
                if (!"DRAFT".equals(snapshot.getStatus())) {
                    return RestResponse.fail("该设计正在结算或已提交订单");
                }
            }
        } catch (IllegalArgumentException e) {
            return RestResponse.fail(e.getMessage());
        }
        BuyGoodsRequest goodsVo = new BuyGoodsRequest();
        goodsVo.setGoodsId(goodsId);
        goodsVo.setProductId(productId);
        goodsVo.setNumber(number);
        goodsVo.setDesignSnapshotId(designSnapshotId);
        String checkoutId = purchaseContexts.create(loginUser.getId(), goodsVo);
        return RestResponse.ok(Map.of("checkoutId", checkoutId));
    }
}
