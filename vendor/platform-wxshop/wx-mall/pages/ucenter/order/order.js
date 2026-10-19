const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

Page({
  data: {
    orderList: [],
    page: 1,
    size: 10,
    loadmoreText: '正在加载更多数据',
    nomoreText: '全部加载完成',
    nomore: false,
    totalPages: 1,
  },
  /** 初始化个人订单状态并读取页面所需数据. */
  onLoad: function (options) {
    wx.showLoading({
      title: '加载中...',
      success: function () {},
    });
    this.getOrderList();
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom: function () {
    this.getOrderList();
  },

  /** 按当前订单分类读取本人订单列表. */
  getOrderList() {
    const that = this;

    if (that.data.totalPages <= that.data.page - 1) {
      that.setData({
        nomore: true,
      });
      return;
    }

    util.request(api.OrderList, {page: that.data.page, size: that.data.size}).then(function (res) {
      if (res.code === 0) {
        that.setData({
          orderList: that.data.orderList.concat(res.data.list),
          page: res.data.page + 1,
          totalPages: res.data.total,
        });
        wx.hideLoading();
      }
    });
  },
  /** 发起当前订单的微信支付并处理结果提示. */
  payOrder(event) {
    const that = this;
    const orderIndex = event.currentTarget.dataset.orderIndex;
    const order = that.data.orderList[orderIndex];
    wx.redirectTo({
      url: '/pages/pay/pay?orderId=' + order.id + '&actualPrice=' + order.actualPrice,
    });
  },
});
