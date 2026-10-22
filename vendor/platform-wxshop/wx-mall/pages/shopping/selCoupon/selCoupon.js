const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

const app = getApp();

Page({
    data: {
        couponList: null,
        buyType: '',
    },
    /** 初始化优惠券选择状态并读取页面所需数据. */
    onLoad(options) {
        this.data.buyType = options.buyType;
        this.loadListData();
    },

    /** 读取当前用户可查看的优惠券列表. */
    loadListData() {
        util.request(api.GoodsCouponList, {type: this.data.buyType}).then((res) => {
            if (res.code === 0) {
                this.setData({
                    couponList: res.data,
                });
            }
        });
    },

    /**
     * 点击不使用优惠券
     * 返回上一页
     */
    noUseCoupon() {
        app.globalData.userCoupon = 'NO_USE_COUPON';
        wx.navigateBack({});
    },

    /** 将当前选择的优惠券同步到结算上下文并返回. */
    tapCoupon(event) {
        const item = event.currentTarget.dataset.item;
        if (item.enabled == 0) {
            return;
        }
        app.globalData.userCoupon = 'USE_COUPON';
        app.globalData.selectedCoupon = item;
        wx.navigateBack({});
    },
});
