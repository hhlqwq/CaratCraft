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
        loading: false,
        loadError: '',
    },
    /** 初始化结算类型和定制快照 / Initializes checkout type and customization snapshot. */
    onLoad(options) {
        this._unloaded = false;
        this._checkoutVersion = 0;
        const isBuy = options.isBuy === true || options.isBuy === 'true';
        const querySnapshotId = Number(options.designSnapshotId || 0);
        this.setData({
            checkoutId: options.checkoutId || '',
            requestKey:
                'checkout_' + Date.now().toString(36) + Math.random().toString(36).slice(2, 12),
            isBuy: isBuy,
            buyType: isBuy ? 'buy' : 'cart',
            designSnapshotId: isBuy ? querySnapshotId || null : null,
        });
        //每次重新加载界面，清空数据
        app.globalData.userCoupon = 'NO_USE_COUPON';
        app.globalData.selectedCoupon = {};
    },

    /** 加载服务端购买上下文, 定制单再按同一快照获取最新报价. */
    getCheckoutInfo() {
        if (this._unloaded) {
            return Promise.resolve(false);
        }
        const token = wx.getStorageSync('token') || '';
        const version = (this._checkoutVersion = (this._checkoutVersion || 0) + 1);
        const current = () =>
            !this._unloaded &&
            version === this._checkoutVersion &&
            token === (wx.getStorageSync('token') || '');
        const buyType = this.data.isBuy ? 'buy' : 'cart';
        this.setData({loading: true, loadError: '', designQuote: null});
        return util
            .request(api.CartCheckout, {
                addressId: this.data.addressId,
                couponId: this.data.couponId,
                type: buyType,
                checkoutId: this.data.checkoutId,
            })
            .then((res) => {
                if (!current()) {
                    return false;
                }
                if (res.code !== 0) {
                    throw new Error(res.msg || '结算信息加载失败');
                }
                const contextSnapshotId = Number(res.data.designSnapshotId || 0);
                const currentSnapshotId = Number(this.data.designSnapshotId || 0);
                if (
                    contextSnapshotId &&
                    currentSnapshotId &&
                    contextSnapshotId !== currentSnapshotId
                ) {
                    throw new Error('定制设计与当前结算信息不一致，请重新结算');
                }
                const designSnapshotId = contextSnapshotId || currentSnapshotId || null;
                if (designSnapshotId !== this.data.designSnapshotId) {
                    this.setData({designSnapshotId: designSnapshotId});
                }
                if (!designSnapshotId) {
                    return {checkout: res, quote: null};
                }
                return util
                    .request(api.CustomizationCheckoutQuote, {snapshotId: designSnapshotId})
                    .then((quoteRes) => {
                        if (!current()) {
                            return false;
                        }
                        if (quoteRes.code !== 0) {
                            throw new Error(quoteRes.msg || '定制报价已失效');
                        }
                        return {checkout: res, quote: quoteRes.data};
                    });
            })
            .then((result) => {
                if (!current() || !result) {
                    return false;
                }
                const res = result.checkout;
                const designQuote = result.quote;
                const displayGoodsTotal = designQuote
                    ? this.roundMoney(designQuote.totalPrice)
                    : this.roundMoney(res.data.goodsTotalPrice);
                const displayOrderTotal = designQuote
                    ? this.roundMoney(displayGoodsTotal + Number(res.data.freightPrice || 0))
                    : this.roundMoney(res.data.orderTotalPrice);
                const displayActual = designQuote
                    ? this.roundMoney(
                          Math.max(0, displayOrderTotal - Number(res.data.couponPrice || 0))
                      )
                    : this.roundMoney(res.data.actualPrice);
                this.setData({
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
                //设置默认收货地址
                if (this.data.checkedAddress.id) {
                    this.setData({addressId: this.data.checkedAddress.id});
                }
                return true;
            })
            .catch((error) => {
                if (current()) {
                    this.setData({loadError: error.message || '结算信息加载失败, 请重试'});
                    util.showErrorToast(error.message || '结算信息加载失败');
                }
                return false;
            })
            .finally(() => {
                if (current()) {
                    this.setData({loading: false});
                }
            });
    },
    /** 将金额四舍五入到分, 避免浮点尾差导致服务端价格比对失败. */
    roundMoney(value) {
        const number = Number(value);
        return Number.isFinite(number) ? Math.round(number * 100) / 100 : 0;
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

    /** 页面卸载后拒绝结算加载的迟到结果. */
    onUnload() {
        this._unloaded = true;
        this._checkoutVersion = (this._checkoutVersion || 0) + 1;
    },

    /** 页面再次展示时刷新订单结算状态. */
    onShow() {
        this.getCouponData();
        const addressId = wx.getStorageSync('addressId');
        if (addressId) {
            this.setData({addressId: addressId});
        }
        this.getCheckoutInfo();
    },

    /**
     * 获取优惠券
     */
    getCouponData() {
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
    tapCoupon() {
        wx.navigateTo({
            url: '../selCoupon/selCoupon?buyType=' + this.data.buyType,
        });
    },

    /** 提交期间锁定按钮, 已创建订单仅继续支付而不重复下单. */
    submitOrder() {
        if (this.data.submitting) {
            return false;
        }
        if (this.data.loading || this.data.loadError) {
            util.showErrorToast('结算信息未就绪, 请重试');
            return false;
        }
        if (this.data.addressId <= 0) {
            util.showErrorToast('请选择收货地址');
            return false;
        }
        const expectedActualPrice = Number(this.data.actualPrice);
        if (!Number.isFinite(expectedActualPrice) || expectedActualPrice < 0) {
            util.showErrorToast('结算金额异常, 请重新加载');
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
                    expectedActualPrice: expectedActualPrice,
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
