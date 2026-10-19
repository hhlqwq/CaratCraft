const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');
const pay = require('../../../services/pay.js');

const app = getApp();

Page({
  data: {
    checkedGoodsList: [],
    checkedAddress: {},
    checkedCoupon: [],
    couponList: [],
    goodsTotalPrice: 0.0, //商品总价
    freightPrice: 0.0, //快递费
    couponPrice: 0.0, //优惠券的价格
    orderTotalPrice: 0.0, //订单总价
    actualPrice: 0.0, //实际需要支付的总价
    addressId: 0,
    couponId: 0,
    isBuy: false,
    couponDesc: '',
    couponCode: '',
    buyType: '',
    submitting: false,
    checkoutId: '',
    requestKey: '',
    designSnapshotId: null,
  },
  /** 初始化结算类型和定制快照 / Initializes checkout type and customization snapshot. */
  onLoad: function (options) {
    const isBuy = options.isBuy === true || options.isBuy === 'true';
    const querySnapshotId = Number(options.designSnapshotId || 0);
    this.setData({
      checkoutId: options.checkoutId || '',
      requestKey: 'checkout_' + Date.now().toString(36) + Math.random().toString(36).slice(2, 12),
      isBuy: isBuy,
      buyType: isBuy ? 'buy' : 'cart',
      designSnapshotId: isBuy ? querySnapshotId || null : null,
    });
    //每次重新加载界面，清空数据
    app.globalData.userCoupon = 'NO_USE_COUPON';
    app.globalData.selectedCoupon = {};
  },

  /** 加载服务端购买上下文, 再按其中的快照获取最新报价 / Loads server buy context before requoting its snapshot. */
  getCheckoutInfo: function () {
    const that = this;
    const url = api.CartCheckout;
    const buyType = this.data.isBuy ? 'buy' : 'cart';
    util
      .request(url, {
        addressId: that.data.addressId,
        couponId: that.data.couponId,
        type: buyType,
        checkoutId: that.data.checkoutId,
      })
      .then(function (res) {
        if (res.code !== 0) {
          return Promise.reject(new Error(res.msg || '结算信息加载失败'));
        }
        const contextSnapshotId = Number(res.data.designSnapshotId || 0);
        const currentSnapshotId = Number(that.data.designSnapshotId || 0);
        if (contextSnapshotId && currentSnapshotId && contextSnapshotId !== currentSnapshotId) {
          return Promise.reject(new Error('定制设计与当前结算信息不一致，请重新结算'));
        }
        const designSnapshotId = contextSnapshotId || currentSnapshotId || null;
        that.setData({designSnapshotId: designSnapshotId});
        return {checkout: res, quote: {code: 0, data: null}};
      })
      .then(function (result) {
        const res = result.checkout;
        const quoteRes = result.quote;
        if (quoteRes.code !== 0) {
          util.showErrorToast(quoteRes.msg || '定制报价已失效');
          return;
        }
        if (res.code === 0) {
          const designQuote = quoteRes.data;
          const displayGoodsTotal = designQuote
            ? Number(designQuote.totalPrice)
            : res.data.goodsTotalPrice;
          const displayOrderTotal = designQuote
            ? displayGoodsTotal + Number(res.data.freightPrice || 0)
            : res.data.orderTotalPrice;
          const displayActual = designQuote
            ? Math.max(0, displayOrderTotal - Number(res.data.couponPrice || 0))
            : res.data.actualPrice;
          that.setData({
            designQuote: designQuote || null,
            checkedGoodsList: res.data.checkedGoodsList,
            checkedAddress: res.data.checkedAddress,
            actualPrice: displayActual,
            checkedCoupon: res.data.checkedCoupon ? res.data.checkedCoupon : '',
            couponList: res.data.couponList ? res.data.couponList : '',
            couponPrice: res.data.couponPrice,
            freightPrice: res.data.freightPrice,
            goodsTotalPrice: displayGoodsTotal,
            orderTotalPrice: displayOrderTotal,
          });
          //设置默认收获地址
          if (that.data.checkedAddress.id) {
            const addressId = that.data.checkedAddress.id;
            if (addressId) {
              that.setData({addressId: addressId});
            }
          } else {
            wx.showModal({
              title: '',
              content: '请添加默认收货地址!',
              success: function (res) {
                if (res.confirm) {
                  that.selectAddress();
                }
              },
            });
          }
        }
      })
      .catch(function (error) {
        util.showErrorToast(error && error.message ? error.message : '结算信息加载失败');
      });
  },
  /** 记录当前选择的收货地址并返回结算流程. */
  selectAddress() {
    wx.navigateTo({
      url: '/pages/shopping/address/address',
    });
  },
  /** 从结算页面进入收货地址新增流程. */
  addAddress() {
    wx.navigateTo({
      url: '/pages/shopping/addressAdd/addressAdd',
    });
  },

  /** 页面再次展示时刷新订单结算状态. */
  onShow: function () {
    this.getCouponData();

    try {
      const addressId = wx.getStorageSync('addressId');
      if (addressId) {
        this.setData({
          addressId: addressId,
        });
      }
    } catch (e) {
      util.showErrorToast('地址读取失败, 请重新选择');
    }
    this.getCheckoutInfo();
  },

  /**
   * 获取优惠券
   */
  getCouponData: function () {
    if (app.globalData.userCoupon == 'USE_COUPON') {
      this.setData({
        couponDesc: app.globalData.selectedCoupon.name,
        couponId: app.globalData.selectedCoupon.user_coupon_id,
      });
    } else if (app.globalData.userCoupon == 'NO_USE_COUPON') {
      this.setData({
        couponDesc: '不使用优惠券',
        couponId: '',
      });
    }
  },

  /**
   * 选择可用优惠券
   */
  tapCoupon: function () {
    const that = this;

    wx.navigateTo({
      url: '../selCoupon/selCoupon?buyType=' + that.data.buyType,
    });
  },

  /** 提交期间锁定按钮, 已创建订单仅继续支付而不重复下单. */
  submitOrder: function () {
    if (this.data.submitting) {
      return;
    }
    if (this.data.addressId <= 0) {
      util.showErrorToast('请选择收货地址');
      return false;
    }
    this.setData({submitting: true});
    return util
      .request(
        api.OrderSubmit,
        {
          addressId: this.data.addressId,
          couponId: this.data.couponId,
          type: this.data.buyType,
          checkoutId: this.data.checkoutId,
          requestKey: this.data.requestKey,
          expectedActualPrice: this.data.actualPrice,
          designSnapshotId: this.data.designSnapshotId,
        },
        'POST',
        'application/json'
      )
      .then((res) => {
        if (res.code === 0) {
          const orderId = res.data.orderInfo.id;
          // 订单已绑定设计快照, 无论支付结果如何都不得再次提交同一设计 / The order now owns the snapshot and must not be resubmitted.
          wx.removeStorageSync('designSnapshotId');
          wx.removeStorageSync('designQuote');
          this.setData({designSnapshotId: null});
          return pay
            .payOrder(parseInt(orderId))
            .then((res) => {
              wx.redirectTo({
                url: '/pages/payResult/payResult?status=1&orderId=' + orderId,
              });
            })
            .catch((res) => {
              // 支付失败后通过已有订单重新支付, 不重新提交定制设计 / Retry payment from the existing order.
              wx.redirectTo({
                url: '/pages/payResult/payResult?status=0&orderId=' + orderId,
              });
            });
        } else {
          util.showErrorToast(res.msg || '下单失败');
        }
      })
      .catch((error) => {
        util.showErrorToast(error.message || '提交失败, 请稍后重试');
      })
      .then(() => {
        this.setData({submitting: false});
      });
  },
});
