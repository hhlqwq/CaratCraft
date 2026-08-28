const util = require('../../utils/util.js');
const api = require('../../config/api.js');
Page({
  data: {
    newGoods: [],
    hotGoods: []
  },
  onShareAppMessage: function () {
    wx.showShareMenu({
      withShareTicket: true,
      menus: ['shareAppMessage', 'shareTimeline']
    })
    return {
      title: '克拉匠造 | 手工克拉 · 珠宝匠心',
      desc: 'DIY 定制你的专属珠宝',
      path: '/pages/index/index'
    }
  },
  onShareTimeline: function () { //用户点击右上角分享朋友圈
    return {
      title: '克拉匠造 | 手工克拉 · 珠宝匠心',
      query: '',
      imageUrl: '/static/images/logo.png'
    }
  },
  onPullDownRefresh() {
    // 增加下拉刷新数据的功能
    this.getIndexData();
  },
  getIndexData: function () {
    let that = this;
    var data = new Object();
    util.request(api.IndexUrlNewGoods).then(function (res) {
      if (res.code === 0) {
        data.newGoods = res.data.newGoodsList
        that.setData(data);
      }
    });
    util.request(api.IndexUrlHotGoods).then(function (res) {

      if (res.code === 0) {
        data.hotGoods = res.data.hotGoodsList
        that.setData(data);
      }
    });
  },
  onLoad: function (options) {
    this.getIndexData();
    wx.login({
      success: (resp) => {
        util.request(api.Code + resp.code, {}, 'GET').then(function (res) {
          if (res.code === 0) {
            //存储用户信息
            wx.setStorageSync('userInfo', res.data.userInfo);
            wx.setStorageSync('token', res.data.token);
            wx.setStorageSync('userId', res.data.userId);
          } else {
            // util.showErrorToast(res.msg)
            wx.showModal({
              title: '提示',
              content: res.msg,
              showCancel: false
            });
          }
        });
      },
    })
  },
  onReady: function () {
    // 页面渲染完成
  },
  onShow: function () {
    // 页面显示
  },
  onHide: function () {
    // 页面隐藏
  },
  onUnload: function () {
    // 页面关闭
  },
})
