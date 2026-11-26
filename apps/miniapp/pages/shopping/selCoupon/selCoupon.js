const {readPage, withPageReads} = require('../../../services/page-read.js');
const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

const app = getApp();

Page(
    withPageReads({
        data: {
            couponList: [],
            buyType: '',
            checkoutId: '',
        },
        /** 初始化优惠券选择状态并读取页面所需数据. */
        onLoad(options) {
            this.setData({buyType: options.buyType, checkoutId: options.checkoutId || ''});
            this.loadListData();
        },

        /** 读取当前用户可查看的优惠券列表. */
        loadListData() {
            readPage(
                this,
                'GoodsCouponList',
                () => util.request(api.GoodsCouponList, {type: this.data.buyType, checkoutId: this.data.checkoutId}, 'POST'),
                (res) => {
                    if (res.code === 0) {
                        this.setData({
                            couponList: res.data,
                        });
                    }
                }
            );
        },

        /**
         * 点击不使用优惠券
         * 返回上一页
         */
        noUseCoupon() {
            if ((wx.getStorageSync('token') || '') !== this._readToken) {
                return;
            }
            app.globalData.userCoupon = 'NO_USE_COUPON';
            app.globalData.selectedCoupon = {};
            wx.navigateBack({});
        },

        /** 将当前选择的优惠券同步到结算上下文并返回. */
        tapCoupon(event) {
            const item = event.currentTarget.dataset.item;
            if (!item || !/^[1-9]\d*$/.test(String(item.user_coupon_id))) {
                return;
            }
            const selected = this.data.couponList.find(
                (coupon) => item && String(coupon.user_coupon_id) === String(item.user_coupon_id)
            );
            if (
                this.data.readLoading ||
                this.data.readError ||
                (wx.getStorageSync('token') || '') !== this._readToken ||
                !selected ||
                selected.enabled !== 1
            ) {
                return;
            }
            app.globalData.userCoupon = 'USE_COUPON';
            app.globalData.selectedCoupon = selected;
            wx.navigateBack({});
        },
    })
);
