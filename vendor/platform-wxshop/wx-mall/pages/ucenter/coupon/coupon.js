const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

Page({
  data: {
    couponList: null,
  },
  /** 初始化个人优惠券状态并读取页面所需数据. */
  onLoad(options) {
    this.loadListData();
  },

  /** 读取当前用户可查看的优惠券列表. */
  loadListData() {
    const that = this;

    util.request(api.CouponList).then(function (res) {
      if (res.code === 0) {
        that.setData({
          couponList: res.data,
        });
      }
    });
  },
});
