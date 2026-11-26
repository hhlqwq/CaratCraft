const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');
const {withPageReads, readPage} = require('../../../services/page-read.js');
const {performPageAction} = require('../../../services/page-action.js');
const app = getApp();

Page(
    withPageReads({
        data: {
            checkedGoodsList: [],
            checkedAddress: {},
            checkedCoupon: [],
            couponList: [],
            goodsTotalPrice: null,
            freightPrice: null,
            couponPrice: null,
            orderTotalPrice: null,
            actualPrice: null,
            addressId: 0,
            couponId: 0,
            couponDesc: '',
            couponCode: '',
            isBuy: false,
            buyType: 'cart',
            checkoutId: '',
            requestKey: '',
            designSnapshotId: null,
            checkoutReady: false,
            createdOrderId: null,
            designQuote: null,
        },
        /** 每次新结算建立独立提交键, 不复用其他页面的购买意图. */
        onLoad(options) {
            const isBuy = options.isBuy === true || options.isBuy === 'true';
            this.setData({
                isBuy,
                buyType: isBuy ? 'buy' : 'cart',
                checkoutId: options.checkoutId || '',
                designSnapshotId: isBuy ? Number(options.designSnapshotId || 0) || null : null,
                requestKey:
                    'checkout_' + Date.now().toString(36) + Math.random().toString(36).slice(2, 12),
            });
            app.globalData.userCoupon = 'NO_USE_COUPON';
            app.globalData.selectedCoupon = {};
        },
        /** 返回结算页重读选择, 已创建订单则保留继续付款入口. */
        onShow() {
            if (this.data.createdOrderId) {
                return;
            }
            this.getCouponData();
            const selected = Number(wx.getStorageSync('addressId'));
            this.setData({
                addressId: Number.isSafeInteger(selected) && selected > 0 ? selected : 0,
            });
            return this.getCheckoutInfo();
        },
        /** 只接受本次服务端结算结果, 加载与失败时立即废弃旧价格. */
        getCheckoutInfo() {
            if (this.data.createdOrderId) {
                return Promise.resolve(false);
            }
            this.setData({
                checkoutReady: false,
                actualPrice: null,
                checkedAddress: {},
                checkedGoodsList: [],
            });
            return readPage(
                this,
                'checkout',
                () =>
                    util.request(api.CartCheckout, {
                        addressId: this.data.addressId,
                        couponId: this.data.couponId,
                        type: this.data.buyType,
                        checkoutId: this.data.checkoutId,
                    }),
                (res) => {
                    const data = res.data;
                    if (
                        !data ||
                        !Array.isArray(data.checkedGoodsList) ||
                        data.checkedGoodsList.length === 0 ||
                        !data.checkedAddress ||
                        !/^\d+(\.\d{1,2})?$/.test(String(data.actualPrice))
                    ) {
                        throw new Error('结算信息无效, 请重新选择商品');
                    }
                    const snapshot = Number(data.designSnapshotId || 0) || null;
                    if (
                        snapshot &&
                        this.data.designSnapshotId &&
                        snapshot !== this.data.designSnapshotId
                    ) {
                        throw new Error('设计与购买信息不一致, 请重新结算');
                    }
                    this.setData({
                        checkedGoodsList: data.checkedGoodsList,
                        checkedAddress: data.checkedAddress,
                        checkedCoupon: data.checkedCoupon || [],
                        couponList: data.couponList || [],
                        goodsTotalPrice: data.goodsTotalPrice,
                        freightPrice: data.freightPrice,
                        couponPrice: data.couponPrice,
                        orderTotalPrice: data.orderTotalPrice,
                        actualPrice: data.actualPrice,
                        addressId: Number(data.checkedAddress.id) || 0,
                        designSnapshotId: snapshot || this.data.designSnapshotId,
                        designQuote: data.designQuote || null,
                        checkoutReady: true,
                    });
                }
            );
        },
        /** 同步本次主动选择的优惠券, 不把空字符串当作数字提交. */
        getCouponData() {
            const selected = app.globalData.selectedCoupon || {};
            if (app.globalData.userCoupon === 'USE_COUPON') {
                const id = Number(selected.user_coupon_id);
                this.setData({
                    couponDesc: selected.name || '',
                    couponId: Number.isSafeInteger(id) && id > 0 ? id : 0,
                });
            } else {
                this.setData({couponDesc: '不使用优惠券', couponId: 0});
            }
        },
        /** 只在可编辑状态选择地址, 不干扰已提交的购买意图. */
        selectAddress() {
            if (!this.data.actionBusy && !this.data.createdOrderId) {
                wx.navigateTo({url: '/pages/shopping/address/address'});
            }
        },
        /** 在结算前补充本人地址. */
        addAddress() {
            if (!this.data.actionBusy && !this.data.createdOrderId) {
                wx.navigateTo({url: '/pages/shopping/addressAdd/addressAdd'});
            }
        },
        /** 仅在提交前进入优惠券选择页. */
        tapCoupon() {
            if (!this.data.actionBusy && !this.data.createdOrderId) {
                wx.navigateTo({
                    url: '../selCoupon/selCoupon?buyType=' + encodeURIComponent(this.data.buyType) +
                        '&checkoutId=' + encodeURIComponent(this.data.checkoutId || ''),
                });
            }
        },
        /** 已创建订单只进入统一支付页, 导航失败也不能再次下单. */
        continuePayment() {
            if (
                this.data.actionBusy ||
                !this.data.createdOrderId ||
                !this._pageReads?.alive ||
                this._readToken !== (wx.getStorageSync('token') || '')
            ) {
                return false;
            }
            const context = this._pageReads;
            const token = this._readToken;
            this.setData({actionBusy: true});
            wx.redirectTo({
                url: '/pages/pay/pay?orderId=' + this.data.createdOrderId,
                fail: () => {
                    if (
                        this._pageReads === context &&
                        context.alive &&
                        token === (wx.getStorageSync('token') || '')
                    ) {
                        util.showErrorToast('订单已创建, 可重试进入付款或从我的订单继续');
                    }
                },
                complete: () => {
                    if (
                        this._pageReads === context &&
                        context.alive &&
                        token === (wx.getStorageSync('token') || '')
                    ) {
                        this.setData({actionBusy: false});
                    }
                },
            });
            return true;
        },
        /** 提交冻结商品明细并从确认阶段防重, 所有迟到写入结果均核对账号与页面. */
        async submitOrder() {
            if (this.data.createdOrderId) {
                return this.continuePayment();
            }
            if (
                !this.data.checkoutReady ||
                !this.data.addressId ||
                this.data.readLoading ||
                this.data.readError
            ) {
                util.showErrorToast('请先加载有效结算信息并选择地址');
                return false;
            }
            const succeeded = await performPageAction(this, {
                canRun: () =>
                    this.data.checkoutReady && this.data.addressId > 0 && !this.data.createdOrderId,
                request: () =>
                    util.request(
                        api.OrderSubmit,
                        {
                            addressId: this.data.addressId,
                            couponId: this.data.couponId,
                            type: this.data.buyType,
                            checkoutId: this.data.checkoutId,
                            requestKey: this.data.requestKey,
                            expectedActualPrice: this.data.actualPrice,
                            designSnapshotId: this.data.designSnapshotId,
                            expectedItems: this.data.checkedGoodsList.map((item) => ({
                                goodsId: item.goodsId,
                                productId: item.productId,
                                number: item.number,
                            })),
                        },
                        'POST',
                        'application/json'
                    ),
                onSuccess: (res) => {
                    const id = Number(res.data?.orderInfo?.id);
                    if (!Number.isSafeInteger(id) || id < 1) {
                        throw new Error('订单返回信息异常, 请先从我的订单核对');
                    }
                    this.setData({createdOrderId: id, checkoutReady: false});
                    wx.removeStorageSync('designSnapshotId');
                    wx.removeStorageSync('designQuote');
                },
            });
            if (succeeded) {
                this.continuePayment();
            }
            return succeeded;
        },
    })
);
