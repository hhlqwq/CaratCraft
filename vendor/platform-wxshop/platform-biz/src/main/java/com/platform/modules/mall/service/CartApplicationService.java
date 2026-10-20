package com.platform.modules.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.platform.common.exception.BusinessException;
import com.platform.common.utils.RestResponse;
import com.platform.modules.custom.service.CustomDesignService;
import com.platform.modules.mall.dto.ApiCartAddRequest;
import com.platform.modules.mall.dto.ApiCartCheckedRequest;
import com.platform.modules.mall.dto.ApiCartDeleteRequest;
import com.platform.modules.mall.dto.ApiCartMinusRequest;
import com.platform.modules.mall.dto.ApiCartUpdateRequest;
import com.platform.modules.mall.dto.BuyGoodsRequest;
import com.platform.modules.mall.dto.CouponInfoVo;
import com.platform.modules.mall.entity.MallAddressEntity;
import com.platform.modules.mall.entity.MallCartEntity;
import com.platform.modules.mall.entity.MallCouponEntity;
import com.platform.modules.mall.entity.MallGoodsEntity;
import com.platform.modules.mall.entity.MallGoodsSpecificationEntity;
import com.platform.modules.mall.entity.MallProductEntity;
import com.platform.modules.wx.entity.WxUserEntity;
import com.platform.modules.wx.service.WxUserService;
import com.qiniu.util.StringUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理本人购物车的读写与结算预览, 写操作先锁定用户行以避免并发合并丢失数量.
 *
 * <p>原始作者: Harmon, 原始时间: 2017-08-11 08:32. 原始项目: https://gitee.com/hehailong/platform.
 */
@Service
public class CartApplicationService {
  @Autowired private MallCartService cartService;
  @Autowired private WxUserService userService;
  @Autowired private MallGoodsService goodsService;
  @Autowired private MallProductService productService;
  @Autowired private MallGoodsSpecificationService goodsSpecificationService;
  @Autowired private MallAddressService addressService;
  @Autowired private MallCouponService apiCouponService;
  @Autowired private PurchaseContextService purchaseContexts;
  @Autowired private CheckoutPricingService checkoutPricing;
  @Autowired private CustomDesignService customDesignService;

  /** 读取本人购物车和已选金额, 不将促销提示作为结算价格. */
  public Map<String, Object> getCart(WxUserEntity loginUser) {
    Map<String, Object> resultObj = new HashMap<>();
    // 用户条件必须保留, 不允许查询其他账号的购物车.
    Map<String, Object> param = new HashMap<>();
    param.put("userId", loginUser.getId());
    List<MallCartEntity> cartList = cartService.queryList(param);
    // 展示金额来自购物车快照, 正式结算另行读取当前规格价格.
    int goodsCount = 0;
    BigDecimal goodsAmount = new BigDecimal("0.00");
    int checkedGoodsCount = 0;
    BigDecimal checkedGoodsAmount = new BigDecimal("0.00");
    for (MallCartEntity cartItem : cartList) {
      goodsCount += cartItem.getNumber();
      goodsAmount =
          goodsAmount.add(
              cartItem.getRetailPrice().multiply(BigDecimal.valueOf(cartItem.getNumber())));
      if (null != cartItem.getChecked() && 1 == cartItem.getChecked()) {
        checkedGoodsCount += cartItem.getNumber();
        checkedGoodsAmount =
            checkedGoodsAmount.add(
                cartItem.getRetailPrice().multiply(BigDecimal.valueOf(cartItem.getNumber())));
      }
    }
    Map<String, Object> cartTotal = new HashMap<>();
    cartTotal.put("goodsCount", goodsCount);
    cartTotal.put("goodsAmount", goodsAmount);
    cartTotal.put("checkedGoodsCount", checkedGoodsCount);
    cartTotal.put("checkedGoodsAmount", checkedGoodsAmount);
    resultObj.put("cartTotal", cartTotal);
    resultObj.put("cartList", cartList);
    resultObj.put("couponInfoList", couponHints(checkedGoodsAmount));
    return resultObj;
  }

  /** 优惠提示只用于展示, 不作为优惠券适用性及最终扣款依据. */
  private List<CouponInfoVo> couponHints(BigDecimal checkedGoodsAmount) {
    Map<String, Object> couponParam = new HashMap<>();
    couponParam.put("enabled", true);
    Integer[] sendTypes = new Integer[] {0, 7};
    couponParam.put("sendTypes", sendTypes);
    List<CouponInfoVo> couponInfoList = new ArrayList<>();
    List<MallCouponEntity> couponVos = apiCouponService.queryList(couponParam);
    if (null != couponVos && !couponVos.isEmpty()) {
      CouponInfoVo fullCutVo = new CouponInfoVo();
      BigDecimal fullCutDec = new BigDecimal(0);
      BigDecimal minAmount = new BigDecimal(100000);
      for (MallCouponEntity couponVo : couponVos) {
        if (couponVo.getMinGoodsAmount() == null || couponVo.getTypeMoney() == null) {
          continue;
        }
        BigDecimal difDec =
            couponVo
                .getMinGoodsAmount()
                .subtract(checkedGoodsAmount)
                .setScale(2, RoundingMode.HALF_UP);
        if (couponVo.getSendType() == 0
            && difDec.doubleValue() > 0.0
            && minAmount.compareTo(couponVo.getMinGoodsAmount()) > 0) {
          fullCutDec = couponVo.getTypeMoney();
          minAmount = couponVo.getMinGoodsAmount();
          fullCutVo.setType(1);
          fullCutVo.setMsg(couponVo.getName() + "，还差" + difDec + "元");
        } else if (couponVo.getSendType() == 0
            && difDec.doubleValue() < 0.0
            && fullCutDec.compareTo(couponVo.getTypeMoney()) < 0) {
          fullCutDec = couponVo.getTypeMoney();
          fullCutVo.setType(0);
          fullCutVo.setMsg("可使用满减券" + couponVo.getName());
        }
        if (couponVo.getSendType() == 7 && difDec.doubleValue() > 0.0) {
          CouponInfoVo cpVo = new CouponInfoVo();
          cpVo.setMsg("满￥" + couponVo.getMinAmount() + "元免配送费，还差" + difDec + "元");
          cpVo.setType(1);
          couponInfoList.add(cpVo);
        } else if (couponVo.getSendType() == 7) {
          CouponInfoVo cpVo = new CouponInfoVo();
          cpVo.setMsg("满￥" + couponVo.getMinAmount() + "元免配送费");
          couponInfoList.add(cpVo);
        }
      }
      if (!StringUtils.isNullOrEmpty(fullCutVo.getMsg())) {
        couponInfoList.add(fullCutVo);
      }
    }
    return couponInfoList;
  }

  /** 将本人购物车和统计包装为统一响应, 供列表及修改后的刷新使用. */
  public RestResponse<Map<String, Object>> index(WxUserEntity loginUser) {
    return RestResponse.ok(getCart(loginUser));
  }

  /** 同一用户购物车写入串行化, 防止合并时丢失数量或重复删除. */
  private void lockCart(Long userId) {
    if (userService.getOne(new QueryWrapper<WxUserEntity>().eq("ID", userId).last("FOR UPDATE"))
        == null) {
      throw new BusinessException("用户不存在");
    }
  }

  /** 统一生成规格展示文本, 缺少规格时明确清空而不遗留上一规格的名称. */
  private String specificationText(MallProductEntity product) {
    String ids = product.getGoodsSpecificationIds();
    if (ids == null || ids.isBlank()) {
      return "";
    }
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("ids", ids.split("_"));
    parameters.put("goodsId", product.getGoodsId());
    List<MallGoodsSpecificationEntity> specifications =
        goodsSpecificationService.queryList(parameters);
    return String.join(
        ";", specifications.stream().map(MallGoodsSpecificationEntity::getValue).toList());
  }

  /** 新增、换规格和合并使用同一份规格快照, 零售价与市场价分别保存. */
  private void applyProduct(MallCartEntity cart, MallProductEntity product, int number) {
    cart.setProductId(product.getId());
    cart.setGoodsSn(product.getGoodsSn());
    cart.setNumber(number);
    cart.setRetailPrice(product.getRetailPrice());
    cart.setMarketPrice(product.getMarketPrice());
    cart.setGoodsSpecifitionIds(
        product.getGoodsSpecificationIds() == null ? "" : product.getGoodsSpecificationIds());
    cart.setGoodsSpecifitionNameValue(specificationText(product));
  }

  /** 添加指定规格, 同一用户已有规格合并数量前重新检查库存. */
  @Transactional(rollbackFor = Exception.class)
  public RestResponse<Map<String, Object>> add(WxUserEntity loginUser, ApiCartAddRequest request) {
    lockCart(loginUser.getId());
    Integer goodsId = request.getGoodsId();
    Integer productId = request.getProductId();
    Integer number = request.getNumber();
    if (goodsId == null || productId == null || number == null || number < 1 || number > 9999) {
      return RestResponse.fail(400, "商品、规格和数量无效, 数量须为1到9999");
    }
    // 判断商品是否可以购买
    MallGoodsEntity goodsInfo = goodsService.getById(goodsId);
    if (null == goodsInfo || goodsInfo.getIsDelete() == 1 || goodsInfo.getIsOnSale() != 1) {
      return RestResponse.fail(400, "商品已下架");
    }
    // 取得规格的信息,判断规格库存
    MallProductEntity productInfo = productService.getById(productId);
    if (null == productInfo
        || !goodsId.equals(productInfo.getGoodsId())
        || productInfo.getGoodsNumber() < number) {
      return RestResponse.fail(400, "库存不足");
    }

    // 判断购物车中是否存在此规格商品
    Map<String, Object> cartParam = new HashMap<>();
    cartParam.put("goodsId", goodsId);
    cartParam.put("productId", productId);
    cartParam.put("userId", loginUser.getId());
    List<MallCartEntity> cartInfoList = cartService.queryList(cartParam);
    MallCartEntity cartInfo =
        null != cartInfoList && !cartInfoList.isEmpty() ? cartInfoList.get(0) : null;
    if (null == cartInfo) {
      cartInfo = new MallCartEntity();

      cartInfo.setGoodsId(goodsId);
      applyProduct(cartInfo, productInfo, number);
      cartInfo.setGoodsName(goodsInfo.getName());
      cartInfo.setListPicUrl(goodsInfo.getListPicUrl());
      cartInfo.setSessionId("1");
      cartInfo.setUserId(loginUser.getId());
      cartInfo.setChecked(1);
      cartService.save(cartInfo);
    } else {
      // 合并后的总量也必须满足接口上限, 而非只限制本次新增数量.
      int combinedNumber = number + cartInfo.getNumber();
      if (combinedNumber > 9999) {
        return RestResponse.fail(400, "合并后的购买数量不能超过9999");
      }
      if (productInfo.getGoodsNumber() < combinedNumber) {
        return RestResponse.fail(400, "库存不足");
      }
      cartInfo.setNumber(combinedNumber);
      cartService.update(cartInfo);
    }
    return RestResponse.ok(getCart(loginUser));
  }

  /** 减少本人指定规格的数量, 数量不足扣减时删除该购物车行. */
  @Transactional(rollbackFor = Exception.class)
  public RestResponse<Integer> minus(WxUserEntity loginUser, ApiCartMinusRequest request) {
    lockCart(loginUser.getId());
    Integer goodsId = request.getGoodsId();
    Integer productId = request.getProductId();
    Integer number = request.getNumber();
    if (goodsId == null || productId == null || number == null || number < 1 || number > 9999) {
      return RestResponse.fail(400, "商品、规格和数量无效, 数量须为1到9999");
    }
    // 判断购物车中是否存在此规格商品
    Map<String, Object> cartParam = new HashMap<>();
    cartParam.put("goodsId", goodsId);
    cartParam.put("productId", productId);
    cartParam.put("userId", loginUser.getId());
    List<MallCartEntity> cartInfoList = cartService.queryList(cartParam);
    MallCartEntity cartInfo =
        null != cartInfoList && !cartInfoList.isEmpty() ? cartInfoList.get(0) : null;
    int cartNum = 0;
    if (null != cartInfo) {
      if (cartInfo.getNumber() > number) {
        cartInfo.setNumber(cartInfo.getNumber() - number);
        cartService.update(cartInfo);
        cartNum = cartInfo.getNumber();
      } else {
        cartService.delete(cartInfo.getId());
      }
    }
    return RestResponse.ok(cartNum);
  }

  /** 更新本人购物车的数量或规格, 换规格时合并已有行并重新检查库存. */
  @Transactional(rollbackFor = Exception.class)
  public RestResponse<Map<String, Object>> update(
      WxUserEntity loginUser, ApiCartUpdateRequest request) {
    lockCart(loginUser.getId());
    Integer goodsId = request.getGoodsId();
    Integer productId = request.getProductId();
    Integer number = request.getNumber();
    if (goodsId == null || productId == null || number == null || number < 1 || number > 9999) {
      return RestResponse.fail(400, "商品、规格和数量无效, 数量须为1到9999");
    }
    Integer id = request.getId();
    // 取得规格的信息,判断规格库存
    MallProductEntity productInfo = productService.getById(productId);
    if (null == productInfo
        || !goodsId.equals(productInfo.getGoodsId())
        || productInfo.getGoodsNumber() < number) {
      return RestResponse.fail(400, "库存不足");
    }
    // 判断是否已经存在product_id购物车商品
    MallCartEntity cartInfo = cartService.getById(id);
    if (cartInfo == null
        || !loginUser.getId().equals(cartInfo.getUserId())
        || !goodsId.equals(cartInfo.getGoodsId())) {
      return RestResponse.fail(403, "购物车记录不存在或无权修改");
    }
    // 只是更新number
    if (cartInfo.getProductId().equals(productId)) {
      cartInfo.setNumber(number);
      cartService.update(cartInfo);
      return RestResponse.ok(getCart(loginUser));
    }

    Map<String, Object> cartParam = new HashMap<>();
    cartParam.put("goodsId", goodsId);
    cartParam.put("productId", productId);
    cartParam.put("userId", loginUser.getId());
    List<MallCartEntity> cartInfoList = cartService.queryList(cartParam);
    MallCartEntity existingProductCart =
        null != cartInfoList && !cartInfoList.isEmpty() ? cartInfoList.get(0) : null;
    int updatedNumber = number;
    if (existingProductCart != null) {
      updatedNumber += existingProductCart.getNumber();
      if (updatedNumber > 9999) {
        return RestResponse.fail(400, "合并后的购买数量不能超过9999");
      }
      if (productInfo.getGoodsNumber() < updatedNumber) {
        return RestResponse.fail(400, "库存不足");
      }
      cartService.delete(existingProductCart.getId());
    }
    applyProduct(cartInfo, productInfo, updatedNumber);
    cartService.update(cartInfo);
    return RestResponse.ok(getCart(loginUser));
  }

  /** 仅批量更新本人规格的结算选择, 不修改商品数量或金额. */
  @Transactional(rollbackFor = Exception.class)
  public RestResponse<Map<String, Object>> checked(
      WxUserEntity loginUser, ApiCartCheckedRequest request) {
    lockCart(loginUser.getId());
    String productIds = request.getProductIds();
    Integer isChecked = request.getIsChecked();
    if (StringUtils.isNullOrEmpty(productIds)
        || isChecked == null
        || (isChecked != 0 && isChecked != 1)) {
      return RestResponse.fail(400, "请选择商品并提供有效的勾选状态");
    }
    String[] productIdArray = productIds.split(",");
    cartService.updateCheck(productIdArray, isChecked, loginUser.getId());
    return RestResponse.ok(getCart(loginUser));
  }

  /** 按本人身份及规格集合批量删除, 不使用客户端提供的其他用户标识. */
  @Transactional(rollbackFor = Exception.class)
  public RestResponse<Map<String, Object>> delete(
      WxUserEntity loginUser, ApiCartDeleteRequest request) {
    lockCart(loginUser.getId());
    Long userId = loginUser.getId();
    String productIds = request.getProductIds();

    if (StringUtils.isNullOrEmpty(productIds)) {
      return RestResponse.fail("删除出错");
    }
    String[] productIdsArray = productIds.split(",");
    cartService.deleteByUserAndProductIds(userId, productIdsArray);

    return RestResponse.ok(getCart(loginUser));
  }

  /** 汇总本人购物车的总件数, 与商品种类数量区分. */
  public RestResponse<Map<String, Object>> goodscount(WxUserEntity loginUser) {
    if (null == loginUser || null == loginUser.getId()) {
      return RestResponse.fail("未登录");
    }
    Map<String, Object> resultObj = new HashMap<>();
    // 查询列表数据
    Map<String, Object> param = new HashMap<>();
    param.put("userId", loginUser.getId());
    List<MallCartEntity> cartList = cartService.queryList(param);
    // 获取购物车统计信息
    int goodsCount = 0;
    for (MallCartEntity cartItem : cartList) {
      goodsCount += cartItem.getNumber();
    }
    resultObj.put("cartList", cartList);
    Map<String, Object> cartTotal = new HashMap<>();
    cartTotal.put("goodsCount", goodsCount);
    resultObj.put("cartTotal", cartTotal);
    return RestResponse.ok(resultObj);
  }

  /** 按当前规格价格、本人地址与优惠券计算结算预览, 定制单重新校验设计报价. */
  public RestResponse<Map<String, Object>> checkout(
      WxUserEntity loginUser, Integer couponId, String type, String checkoutId, Integer addressId) {
    if (!"cart".equals(type) && !"buy".equals(type)) {
      throw new IllegalArgumentException("购买方式无效");
    }
    Map<String, Object> resultObj = new HashMap<>();
    // 当前沿用现有零运费策略, 不在展示层伪造商家尚未配置的运费规则.
    BigDecimal freightPrice = new BigDecimal("0.00");
    // 默认收货地址
    Map<String, Object> param = new HashMap<>();
    param.put("userId", loginUser.getId());
    List<MallAddressEntity> addressEntities = addressService.queryList(param);

    if (null == addressEntities || addressEntities.isEmpty()) {
      resultObj.put("checkedAddress", new MallAddressEntity());
    } else {
      resultObj.put("checkedAddress", addressEntities.get(0));
    }
    // 两种购买入口最终共用计价校验, 不信任购物车快照中的旧价格.
    List<MallCartEntity> checkedGoodsList = new ArrayList<>();
    BigDecimal goodsTotalPrice;
    if ("cart".equals(type)) {
      List<MallCartEntity> cartItems = cartService.queryList(Map.of("userId", loginUser.getId()));
      for (MallCartEntity cartEntity : cartItems) {
        if (cartEntity.getChecked() == 1) {
          MallProductEntity current =
              checkoutPricing.requireProduct(
                  cartEntity.getProductId(), cartEntity.getGoodsId(), cartEntity.getNumber());
          cartEntity.setRetailPrice(current.getRetailPrice());
          checkedGoodsList.add(cartEntity);
        }
      }
      goodsTotalPrice =
          checkedGoodsList.stream()
              .map(line -> line.getRetailPrice().multiply(BigDecimal.valueOf(line.getNumber())))
              .reduce(BigDecimal.ZERO, BigDecimal::add);
    } else {
      BuyGoodsRequest goodsVO = purchaseContexts.require(loginUser.getId(), checkoutId);
      if (goodsVO == null) {
        return RestResponse.fail("购买信息已失效，请重新选择商品");
      }
      MallProductEntity productInfo =
          checkoutPricing.requireProduct(
              goodsVO.getProductId(), goodsVO.getGoodsId(), goodsVO.getNumber());
      goodsTotalPrice =
          productInfo.getRetailPrice().multiply(BigDecimal.valueOf(goodsVO.getNumber()));

      MallCartEntity cartVo = new MallCartEntity();
      cartVo.setGoodsId(goodsVO.getGoodsId());
      cartVo.setProductId(goodsVO.getProductId());
      cartVo.setGoodsSn(productInfo.getGoodsSn());
      cartVo.setGoodsSpecifitionNameValue(goodsVO.getSelectedText());
      cartVo.setGoodsName(productInfo.getGoodsName());
      cartVo.setNumber(goodsVO.getNumber());
      cartVo.setRetailPrice(productInfo.getRetailPrice());
      cartVo.setListPicUrl(productInfo.getListPicUrl());
      checkedGoodsList.add(cartVo);
      resultObj.put("designSnapshotId", goodsVO.getDesignSnapshotId());
    }

    if (resultObj.get("designSnapshotId") != null) {
      Map<String, Object> quote =
          customDesignService.quoteOwned(
              ((Number) resultObj.get("designSnapshotId")).longValue(), loginUser.getId());
      goodsTotalPrice = new BigDecimal(quote.get("totalPrice").toString());
      if (!checkedGoodsList.isEmpty()) {
        checkedGoodsList.get(0).setRetailPrice(goodsTotalPrice);
      }
    }
    if (addressId != null && addressId > 0) {
      MallAddressEntity chosen = addressService.getById(addressId);
      if (chosen == null || !loginUser.getId().equals(chosen.getUserId())) {
        throw new IllegalArgumentException("收货地址不存在或无权访问");
      }
      resultObj.put("checkedAddress", chosen);
    }
    resultObj.putAll(
        checkoutPricing.calculate(loginUser.getId(), couponId, goodsTotalPrice, freightPrice));
    resultObj.put("checkedGoodsList", checkedGoodsList);
    resultObj.put("checkoutId", checkoutId);
    return RestResponse.ok(resultObj);
  }

  /** 返回本人持有的优惠券, 是否适用由结算计价服务重新校验. */
  public RestResponse<List<MallCouponEntity>> checkedCouponList(WxUserEntity loginUser) {
    Map<String, Object> param = new HashMap<>();
    param.put("userId", loginUser.getId());
    return RestResponse.ok(apiCouponService.queryList(param));
  }
}
