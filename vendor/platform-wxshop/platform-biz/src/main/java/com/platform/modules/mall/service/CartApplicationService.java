package com.platform.modules.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.platform.common.exception.BusinessException;
import com.platform.common.utils.RestResponse;
import com.platform.config.RedisTemplateUtil;
import com.platform.modules.mall.dao.MallCouponDao;
import com.platform.modules.mall.dto.*;
import com.platform.modules.mall.dto.BuyGoodsRequest;
import com.platform.modules.mall.dto.CouponInfoVo;
import com.platform.modules.mall.entity.*;
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
 * 作者: @author Harmon <br>
 * 时间: 2017-08-11 08:32<br>
 *
 * @gitee https://gitee.com/hehailong/platform 描述: AppController <br>
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
  @Autowired private MallCouponDao apiCouponMapper;
  @Autowired private RedisTemplateUtil redisTemplateUtil;

  @Autowired private com.platform.modules.mall.service.PurchaseContextService purchaseContexts;
  @Autowired private com.platform.modules.mall.service.CheckoutPricingService checkoutPricing;
  @Autowired private com.platform.modules.custom.service.CustomDesignService customDesignService;

  /** 读取本人购物车和已选金额, 不将促销提示作为结算价格. */
  public Map<String, Object> getCart(WxUserEntity loginUser) {
    Map<String, Object> resultObj = new HashMap<>();
    // 查询列表数据
    Map<String, Object> param = new HashMap<>();
    param.put("userId", loginUser.getId());
    List<MallCartEntity> cartList = cartService.queryList(param);
    // 获取购物车统计信息
    Integer goodsCount = 0;
    BigDecimal goodsAmount = new BigDecimal("0.00");
    Integer checkedGoodsCount = 0;
    BigDecimal checkedGoodsAmount = new BigDecimal("0.00");
    for (MallCartEntity cartItem : cartList) {
      goodsCount += cartItem.getNumber();
      goodsAmount =
          goodsAmount.add(cartItem.getRetailPrice().multiply(new BigDecimal(cartItem.getNumber())));
      if (null != cartItem.getChecked() && 1 == cartItem.getChecked()) {
        checkedGoodsCount += cartItem.getNumber();
        checkedGoodsAmount =
            checkedGoodsAmount.add(
                cartItem.getRetailPrice().multiply(new BigDecimal(cartItem.getNumber())));
      }
    }
    // 获取优惠信息提示
    Map<String, Object> couponParam = new HashMap<>();
    couponParam.put("enabled", true);
    Integer[] sendTypes = new Integer[] {0, 7};
    couponParam.put("sendTypes", sendTypes);
    List<CouponInfoVo> couponInfoList = new ArrayList<>();
    List<MallCouponEntity> couponVos = apiCouponService.queryList(couponParam);
    if (null != couponVos && couponVos.size() > 0) {
      CouponInfoVo fullCutVo = new CouponInfoVo();
      BigDecimal fullCutDec = new BigDecimal(0);
      BigDecimal minAmount = new BigDecimal(100000);
      for (MallCouponEntity couponVo : couponVos) {
        if (couponVo.getMinGoodsAmount() == null || couponVo.getTypeMoney() == null) continue;
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
    resultObj.put("couponInfoList", couponInfoList);
    resultObj.put("cartList", cartList);
    //
    Map<String, Object> cartTotal = new HashMap<>();
    cartTotal.put("goodsCount", goodsCount);
    cartTotal.put("goodsAmount", goodsAmount);
    cartTotal.put("checkedGoodsCount", checkedGoodsCount);
    cartTotal.put("checkedGoodsAmount", checkedGoodsAmount);
    //
    resultObj.put("cartTotal", cartTotal);
    return resultObj;
  }

  /** 获取购物车信息，所有对购物车的增删改操作，都要重新返回购物车的信息 */
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

  /** 将已有规格ID拆分为查询参数. */
  private String[] getSpecificationIdsArray(String ids) {
    String[] idsArray = null;
    if (org.apache.commons.lang.StringUtils.isNotEmpty(ids)) {
      String[] tempArray = ids.split("_");
      if (tempArray.length > 0) {
        idsArray = tempArray;
      }
    }
    return idsArray;
  }

  /** 添加商品到购物车 */
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
        null != cartInfoList && cartInfoList.size() > 0 ? cartInfoList.get(0) : null;
    if (null == cartInfo) {
      // 添加操作
      // 添加规格名和值
      String[] goodsSepcifitionValue = null;
      if (null != productInfo.getGoodsSpecificationIds()
          && productInfo.getGoodsSpecificationIds().length() > 0) {
        Map<String, Object> specificationParam = new HashMap<>();
        String[] idsArray = getSpecificationIdsArray(productInfo.getGoodsSpecificationIds());
        specificationParam.put("ids", idsArray);
        specificationParam.put("goodsId", goodsId);
        List<MallGoodsSpecificationEntity> specificationEntities =
            goodsSpecificationService.queryList(specificationParam);
        goodsSepcifitionValue = new String[specificationEntities.size()];
        for (int i = 0; i < specificationEntities.size(); i++) {
          goodsSepcifitionValue[i] = specificationEntities.get(i).getValue();
        }
      }
      cartInfo = new MallCartEntity();

      cartInfo.setGoodsId(goodsId);
      cartInfo.setProductId(productId);
      cartInfo.setGoodsSn(productInfo.getGoodsSn());
      cartInfo.setGoodsName(goodsInfo.getName());
      cartInfo.setListPicUrl(goodsInfo.getListPicUrl());
      cartInfo.setNumber(number);
      cartInfo.setSessionId("1");
      cartInfo.setUserId(loginUser.getId());
      cartInfo.setRetailPrice(productInfo.getRetailPrice());
      cartInfo.setMarketPrice(productInfo.getMarketPrice());
      if (null != goodsSepcifitionValue) {
        cartInfo.setGoodsSpecifitionNameValue(StringUtils.join(goodsSepcifitionValue, ";"));
      }
      cartInfo.setGoodsSpecifitionIds(productInfo.getGoodsSpecificationIds());
      cartInfo.setChecked(1);
      cartService.save(cartInfo);
    } else {
      // 如果已经存在购物车中，则数量增加
      if (productInfo.getGoodsNumber() < (number + cartInfo.getNumber())) {
        return RestResponse.fail(400, "库存不足");
      }
      cartInfo.setNumber(cartInfo.getNumber() + number);
      cartService.update(cartInfo);
    }
    return RestResponse.ok(getCart(loginUser));
  }

  /** 减少商品到购物车 */
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
        null != cartInfoList && cartInfoList.size() > 0 ? cartInfoList.get(0) : null;
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

  /** 更新指定的购物车信息 */
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
    MallCartEntity newcartInfo =
        null != cartInfoList && cartInfoList.size() > 0 ? cartInfoList.get(0) : null;
    if (null == newcartInfo) {
      // 添加操作
      // 添加规格名和值
      String[] goodsSepcifitionValue = null;
      if (null != productInfo.getGoodsSpecificationIds()) {
        Map<String, Object> specificationParam = new HashMap<>();
        specificationParam.put("ids", productInfo.getGoodsSpecificationIds());
        specificationParam.put("goodsId", goodsId);
        List<MallGoodsSpecificationEntity> specificationEntities =
            goodsSpecificationService.queryList(specificationParam);
        goodsSepcifitionValue = new String[specificationEntities.size()];
        for (int i = 0; i < specificationEntities.size(); i++) {
          goodsSepcifitionValue[i] = specificationEntities.get(i).getValue();
        }
      }
      cartInfo.setProductId(productId);
      cartInfo.setGoodsSn(productInfo.getGoodsSn());
      cartInfo.setNumber(number);
      cartInfo.setRetailPrice(productInfo.getRetailPrice());
      cartInfo.setMarketPrice(productInfo.getRetailPrice());
      if (null != goodsSepcifitionValue) {
        cartInfo.setGoodsSpecifitionNameValue(StringUtils.join(goodsSepcifitionValue, ";"));
      }
      cartInfo.setGoodsSpecifitionIds(productInfo.getGoodsSpecificationIds());
      cartService.update(cartInfo);
    } else {
      // 合并购物车已有的product信息，删除已有的数据
      Integer newNumber = number + newcartInfo.getNumber();
      if (productInfo.getGoodsNumber() < newNumber) {
        return RestResponse.fail(400, "库存不足");
      }
      cartService.delete(newcartInfo.getId());
      // 添加规格名和值
      String[] goodsSepcifitionValue = null;
      if (null != productInfo.getGoodsSpecificationIds()) {
        Map<String, Object> specificationParam = new HashMap<>();
        specificationParam.put("ids", productInfo.getGoodsSpecificationIds());
        specificationParam.put("goodsId", goodsId);
        List<MallGoodsSpecificationEntity> specificationEntities =
            goodsSpecificationService.queryList(specificationParam);
        goodsSepcifitionValue = new String[specificationEntities.size()];
        for (int i = 0; i < specificationEntities.size(); i++) {
          goodsSepcifitionValue[i] = specificationEntities.get(i).getValue();
        }
      }
      cartInfo.setProductId(productId);
      cartInfo.setGoodsSn(productInfo.getGoodsSn());
      cartInfo.setNumber(newNumber);
      cartInfo.setRetailPrice(productInfo.getRetailPrice());
      cartInfo.setMarketPrice(productInfo.getRetailPrice());
      if (null != goodsSepcifitionValue) {
        cartInfo.setGoodsSpecifitionNameValue(StringUtils.join(goodsSepcifitionValue, ";"));
      }
      cartInfo.setGoodsSpecifitionIds(productInfo.getGoodsSpecificationIds());
      cartService.update(cartInfo);
    }
    return RestResponse.ok(getCart(loginUser));
  }

  /** 是否选择商品，如果已经选择，则取消选择，批量操作 */
  @Transactional(rollbackFor = Exception.class)
  public RestResponse<Map<String, Object>> checked(
      WxUserEntity loginUser, ApiCartCheckedRequest request) {
    lockCart(loginUser.getId());
    String productIds = request.getProductIds();
    Integer isChecked = request.getIsChecked();
    if (StringUtils.isNullOrEmpty(productIds)) {
      return RestResponse.fail("删除出错");
    }
    String[] productIdArray = productIds.split(",");
    cartService.updateCheck(productIdArray, isChecked, loginUser.getId());
    return RestResponse.ok(getCart(loginUser));
  }

  // 删除选中的购物车商品，批量删除
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

  //  获取购物车商品的总件件数
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
    Integer goodsCount = 0;
    for (MallCartEntity cartItem : cartList) {
      goodsCount += cartItem.getNumber();
    }
    resultObj.put("cartList", cartList);
    //
    Map<String, Object> cartTotal = new HashMap<>();
    cartTotal.put("goodsCount", goodsCount);
    //
    resultObj.put("cartTotal", cartTotal);
    return RestResponse.ok(resultObj);
  }

  /** 订单提交前的检验和填写相关订单信息 */
  public RestResponse<Map<String, Object>> checkout(
      WxUserEntity loginUser, Integer couponId, String type, String checkoutId, Integer addressId) {
    if (!"cart".equals(type) && !"buy".equals(type)) throw new IllegalArgumentException("购买方式无效");
    Map<String, Object> resultObj = new HashMap<>();
    // 根据收货地址计算运费

    BigDecimal freightPrice = new BigDecimal("0.00");
    // 默认收货地址
    Map<String, Object> param = new HashMap<>();
    param.put("userId", loginUser.getId());
    List addressEntities = addressService.queryList(param);

    if (null == addressEntities || addressEntities.isEmpty()) {
      resultObj.put("checkedAddress", new MallAddressEntity());
    } else {
      resultObj.put("checkedAddress", addressEntities.get(0));
    }
    // * 获取要购买的商品和总价
    ArrayList<Object> checkedGoodsList = new ArrayList<>();
    BigDecimal goodsTotalPrice;
    if ("cart".equals(type)) {
      Map<String, Object> cartData = (Map<String, Object>) this.getCart(loginUser);

      for (MallCartEntity cartEntity : (List<MallCartEntity>) cartData.get("cartList")) {
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
              .map(
                  value -> {
                    MallCartEntity line = (MallCartEntity) value;
                    return line.getRetailPrice().multiply(BigDecimal.valueOf(line.getNumber()));
                  })
              .reduce(BigDecimal.ZERO, BigDecimal::add);
    } else { // 是直接购买的
      BuyGoodsRequest goodsVO = purchaseContexts.require(loginUser.getId(), checkoutId);
      if (goodsVO == null) {
        return RestResponse.fail("购买信息已失效，请重新选择商品");
      }
      MallProductEntity productInfo =
          checkoutPricing.requireProduct(
              goodsVO.getProductId(), goodsVO.getGoodsId(), goodsVO.getNumber());
      // 计算订单的费用
      // 商品总价
      goodsTotalPrice = productInfo.getRetailPrice().multiply(new BigDecimal(goodsVO.getNumber()));

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
        ((MallCartEntity) checkedGoodsList.get(0)).setRetailPrice(goodsTotalPrice);
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

  /** 选择优惠券列表 */
  public RestResponse<List<MallCouponEntity>> checkedCouponList(WxUserEntity loginUser) {
    Map<String, Object> param = new HashMap<>();
    param.put("userId", loginUser.getId());
    List<MallCouponEntity> couponVos = apiCouponService.queryList(param);
    if (null != couponVos && !couponVos.isEmpty()) {
      // 获取要购买的商品
      Map<String, Object> cartData = this.getCart(loginUser);
      List<MallCartEntity> checkedGoodsList = new ArrayList<>();
      List<Integer> checkedGoodsIds = new ArrayList<>();
      for (MallCartEntity cartEntity : (List<MallCartEntity>) cartData.get("cartList")) {
        if (cartEntity.getChecked() == 1) {
          checkedGoodsList.add(cartEntity);
          checkedGoodsIds.add(cartEntity.getId());
        }
      }
    }
    return RestResponse.ok(couponVos);
  }
}
