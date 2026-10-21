package com.platform.modules.app.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.binarywang.wxpay.service.WxPayService;
import com.platform.annotation.IgnoreAuth;
import com.platform.annotation.LoginUser;
import com.platform.common.utils.Query;
import com.platform.common.utils.RestResponse;
import com.platform.modules.app.request.ApiOrderSubmitRequest;
import com.platform.modules.custom.dao.CustomDesignSnapshotDao;
import com.platform.modules.custom.dao.CustomOrderCustomizationDao;
import com.platform.modules.custom.entity.CustomDesignSnapshotEntity;
import com.platform.modules.custom.entity.CustomOrderCustomizationEntity;
import com.platform.modules.custom.service.CustomDesignService;
import com.platform.modules.mall.entity.MallOrderEntity;
import com.platform.modules.mall.entity.MallOrderGoodsEntity;
import com.platform.modules.mall.service.LegalDocumentService;
import com.platform.modules.mall.service.MallOrderGoodsService;
import com.platform.modules.mall.service.MallOrderService;
import com.platform.modules.wx.entity.WxUserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.*;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 作者: @author Harmon <br>
 * 时间: 2017-08-11 08:32<br>
 * 描述: AppController <br>
 */
@Tag(name = "订单-AppOrderController")
@RestController
@RequestMapping("/app/order")
public class AppOrderController extends AppBaseController {
  @Autowired private LegalDocumentService legalDocuments;

  @Autowired private com.platform.modules.mall.service.OrderRefundService refundService;
  @Autowired private MallOrderService orderService;
  @Autowired private com.platform.modules.mall.service.OrderLifecycleService lifecycleService;
  @Autowired private MallOrderGoodsService orderGoodsService;

  @Autowired(required = false)
  private WxPayService wxPayService;

  @Autowired private CustomDesignService customDesignService;
  @Autowired private CustomOrderCustomizationDao customOrderCustomizationDao;
  @Autowired private CustomDesignSnapshotDao customDesignSnapshotDao;

  /** */
  @Operation(summary = "订单首页")
  @IgnoreAuth
  @PostMapping("index")
  public RestResponse<Object> index() {
    //
    return RestResponse.ok("");
  }

  /** 获取订单列表 */
  @Operation(summary = "获取订单列表")
  @PostMapping("list")
  public RestResponse<Map<String, Object>> list(
      @LoginUser WxUserEntity loginUser,
      @RequestParam(value = "page") Integer page,
      @RequestParam(value = "size") Integer size) {
    //
    Map<String, Object> params = new HashMap<>();
    params.put("userId", loginUser.getId());
    params.put("page", page);
    params.put("limit", size);
    params.put("sidx", "add_time");
    params.put("order", "desc");
    // 查询列表数据
    Query<MallOrderEntity> query = new Query<>(params);
    List<MallOrderEntity> orderEntityList = orderService.queryList(query);
    long total =
        orderService.count(
            new LambdaQueryWrapper<MallOrderEntity>()
                .eq(MallOrderEntity::getUserId, loginUser.getId()));
    Map<String, Object> pageData = new HashMap<>();
    List<Integer> orderIds = new ArrayList<>();
    for (MallOrderEntity order : orderEntityList) {
      orderIds.add(order.getId());
    }
    Map<Integer, List<MallOrderGoodsEntity>> goodsByOrderId = new HashMap<>();
    if (!orderIds.isEmpty()) {
      List<MallOrderGoodsEntity> goodsList =
          orderGoodsService.list(
              new LambdaQueryWrapper<MallOrderGoodsEntity>()
                  .in(MallOrderGoodsEntity::getOrderId, orderIds));
      for (MallOrderGoodsEntity goods : goodsList) {
        goodsByOrderId.computeIfAbsent(goods.getOrderId(), ignored -> new ArrayList<>()).add(goods);
      }
    }
    // 批量装配商品，避免每个订单单独查询 / Batch assemble goods to avoid N+1 queries.
    for (MallOrderEntity order : orderEntityList) {
      List<MallOrderGoodsEntity> goodsList =
          goodsByOrderId.getOrDefault(order.getId(), Collections.emptyList());
      int goodsCount = 0;
      for (MallOrderGoodsEntity goods : goodsList) {
        goodsCount += goods.getNumber();
      }
      order.setGoodsCount(goodsCount);
      order.setGoodsList(goodsList);
    }
    pageData.put("list", orderEntityList);
    pageData.put("total", total);
    pageData.put("limit", size);
    pageData.put("page", page);
    return RestResponse.ok(pageData);
  }

  /** 获取订单详情 */
  @Operation(summary = "获取订单详情")
  @PostMapping("detail")
  public RestResponse<Map<String, Object>> detail(
      Integer orderId, @LoginUser WxUserEntity loginUser) {
    Map<String, Object> resultObj = new HashMap<>();
    //
    MallOrderEntity orderInfo = orderService.getById(orderId);
    if (null == orderInfo) {
      return RestResponse.fail("订单不存在");
    }

    if (!loginUser.getId().equals(orderInfo.getUserId())) {
      return RestResponse.fail("越权操作！");
    }
    Map<String, Object> orderGoodsParam = new HashMap<>();
    orderGoodsParam.put("orderId", orderId);
    // 订单的商品
    List<MallOrderGoodsEntity> orderGoods = orderGoodsService.queryList(orderGoodsParam);
    // 订单最后支付时间
    if (orderInfo.getOrderStatus() == 0) {
      // if (moment().subtract(60, 'minutes') < moment(orderInfo.addTime)) {
      //            orderInfo.final_pay_time = moment("001234", "Hmmss").format("mm:ss")
      // } else {
      //     //超过时间不支付，更新订单状态为取消
      // }
    }

    // 订单可操作的选择,删除，支付，收货，评论，退换货
    Map<String, Object> handleOption = orderInfo.getHandleOption();
    //
    resultObj.put("orderInfo", orderInfo);
    resultObj.put("legalDocuments", legalDocuments.orderDocuments(orderId, loginUser.getId()));
    resultObj.put("orderGoods", orderGoods);
    resultObj.put("handleOption", handleOption);
    resultObj.put("customization", getOrderCustomization(orderId));
    resultObj.put("shippingList", new ArrayList<>());
    return RestResponse.ok(resultObj);
  }

  /**
   * 查询订单绑定的定制快照，订单已在调用方完成归属校验。
   *
   * @param orderId 订单 ID
   * @return 面向小程序展示的定制信息；普通订单返回 null
   */
  private Map<String, Object> getOrderCustomization(Integer orderId) {
    CustomOrderCustomizationEntity binding =
        customOrderCustomizationDao.selectOne(
            new LambdaQueryWrapper<CustomOrderCustomizationEntity>()
                .eq(CustomOrderCustomizationEntity::getOrderId, orderId));
    if (binding == null) {
      return null;
    }
    CustomDesignSnapshotEntity snapshot =
        customDesignSnapshotDao.selectById(binding.getSnapshotId());
    if (snapshot == null) {
      return null;
    }
    Map<String, Object> customization = new HashMap<>();
    customization.put("snapshotId", snapshot.getId());
    customization.put("status", binding.getStatus());
    customization.put("designStatus", snapshot.getStatus());
    customization.put("configurationJson", snapshot.getConfigurationJson());
    customization.put("priceBreakdownJson", snapshot.getPriceBreakdownJson());
    customization.put("previewUrl", snapshot.getPreviewUrl());
    return customization;
  }

  @Operation(summary = "修改订单")
  @PostMapping("updateSuccess")
  public RestResponse<Object> updateSuccess(Integer orderId, @LoginUser WxUserEntity loginUser) {
    return RestResponse.fail("支付状态只能通过微信支付查询或签名回调更新");
  }

  /** 获取订单列表 */
  @Operation(summary = "订单提交")
  @PostMapping("submit")
  public RestResponse<Object> submit(
      @LoginUser WxUserEntity loginUser, @RequestBody ApiOrderSubmitRequest request) {
    Map<String, Object> resultObj = null;
    try {
      JSONObject payload = new JSONObject();
      if (request.getAddressId() != null) {
        payload.put("addressId", request.getAddressId());
      }
      if (request.getCouponId() != null) {
        payload.put("couponId", request.getCouponId());
      }
      if (request.getType() != null) {
        payload.put("type", request.getType());
      }
      if (request.getPostscript() != null) {
        payload.put("postscript", request.getPostscript());
      }
      if (request.getDesignSnapshotId() != null) {
        payload.put("designSnapshotId", request.getDesignSnapshotId());
      }
      payload.put("checkoutId", request.getCheckoutId());
      payload.put("requestKey", request.getRequestKey());
      payload.put("expectedActualPrice", request.getExpectedActualPrice());
      resultObj = orderService.submit(payload, loginUser);
      if (null != resultObj) {
        Integer code = MapUtils.getInteger(resultObj, "code");
        if (code != null && code == 0) {
          return RestResponse.ok(resultObj.get("data"));
        }
        return RestResponse.fail(MapUtils.getString(resultObj, "msg", "提交失败"));
      }
      return RestResponse.fail("提交失败");
    } catch (IllegalArgumentException e) {
      return RestResponse.fail(e.getMessage());
    } catch (IllegalStateException e) {
      return RestResponse.fail(e.getMessage());
    } catch (Exception e) {
      return RestResponse.fail("提交失败, 请稍后重试或检查订单列表");
    }
  }

  /** 取消订单 */
  @Operation(summary = "取消订单")
  @PostMapping("cancelOrder")
  public RestResponse<String> cancelOrder(Integer orderId, @LoginUser WxUserEntity loginUser) {

    MallOrderEntity orderVo = orderService.getById(orderId);
    if (null == orderVo) {
      return RestResponse.fail("订单不存在！");
    }
    if (!loginUser.getId().equals(orderVo.getUserId())) {
      return RestResponse.fail("越权操作！");
    }
    if (orderVo.getOrderStatus() == 300) {
      return RestResponse.fail("已发货，不能取消");
    } else if (orderVo.getOrderStatus() == 301) {
      return RestResponse.fail("已收货，不能取消");
    }
    // 需要退款
    if (orderVo.getPayStatus() == 2) {
      refundService.request(orderId, loginUser.getId(), "用户申请取消未发货订单");
      return RestResponse.ok("退款申请已提交, 等待商家审核");
    } else {
      boolean closed = orderService.cancelUnpaid(orderId, loginUser.getId());
      return closed ? RestResponse.ok("取消成功") : RestResponse.fail("订单已支付或支付处理中, 请刷新订单");
    }
  }

  /** 确认收货 */
  @Operation(summary = "确认收货")
  @PostMapping("confirmOrder")
  public RestResponse<String> confirmOrder(Integer orderId, @LoginUser WxUserEntity loginUser) {
    try {
      lifecycleService.confirmReceipt(orderId, loginUser.getId());
      return RestResponse.ok("确认收货成功");
    } catch (IllegalArgumentException | IllegalStateException e) {
      return RestResponse.fail(e.getMessage());
    } catch (Exception e) {
      return RestResponse.fail("确认收货失败：" + e.getMessage());
    }
  }
}
