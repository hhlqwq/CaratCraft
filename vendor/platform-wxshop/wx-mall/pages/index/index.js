const util = require('../../utils/util.js');
const api = require('../../config/api.js');
Page({
  data: {
    newGoods: [],
    hotGoods: [],
    searchKeyword: '',
    functionalMenus: [
      {name: '珠宝成品', caption: '全部款式', mark: '珠', url: '/pages/catalog/catalog'},
      {name: '新品上架', caption: '本季新作', mark: '新', url: '/pages/newGoods/newGoods'},
      {name: '热门款式', caption: '人气之选', mark: '热', url: '/pages/hotGoods/hotGoods'},
      {
        name: '自由定制',
        caption: '从零设计',
        mark: '造',
        url: '/pages/designer/designer',
        tab: true,
      },
      {name: '我的设计', caption: '继续创作', mark: '稿', url: '/pages/ucenter/design/design'},
      {name: '我的收藏', caption: '心仪清单', mark: '藏', url: '/pages/ucenter/collect/collect'},
      {
        name: '浏览足迹',
        caption: '最近看过',
        mark: '迹',
        url: '/pages/ucenter/footprint/footprint',
      },
      {name: '全部订单', caption: '进度查询', mark: '单', url: '/pages/ucenter/order/order'},
    ],
    styleKeywords: ['珍珠项链', '18K 金', '通勤款', '礼赠', '可定制'],
  },
  /** 提供当前页面的微信好友分享信息. */
  onShareAppMessage: function () {
    wx.showShareMenu({
      withShareTicket: true,
      menus: ['shareAppMessage', 'shareTimeline'],
    });
    return {
      title: '克拉匠造 | 手工克拉 · 珠宝匠心',
      desc: 'DIY 定制你的专属珠宝',
      path: '/pages/index/index',
    };
  },
  /** 提供当前页面的朋友圈分享信息. */
  onShareTimeline: function () {
    //用户点击右上角分享朋友圈
    return {
      title: '克拉匠造 | 手工克拉 · 珠宝匠心',
      query: '',
      imageUrl: '/static/images/logo.png',
    };
  },
  /** 下拉时重新加载页面数据并结束刷新提示. */
  onPullDownRefresh() {
    // 增加下拉刷新数据的功能 / Refreshes home data.
    this.getIndexData();
  },
  /** 更新首页搜索词 / Updates the home search keyword. */
  onSearchInput: function (event) {
    this.setData({searchKeyword: event.detail.value});
  },
  /** 提交首页商品搜索 / Submits a product search from home. */
  submitSearch: function () {
    const keyword = (this.data.searchKeyword || '').trim();
    const query = keyword ? '?keyword=' + encodeURIComponent(keyword) : '';
    wx.navigateTo({url: '/pages/search/search' + query});
  },
  /** 使用快捷关键词检索 / Searches with a suggested keyword. */
  searchByKeyword: function (event) {
    const keyword = event.currentTarget.dataset.keyword;
    this.setData({searchKeyword: keyword});
    this.submitSearch();
  },
  /** 演示标签只跟随服务端商品编码, 不将真实商品统一标为演示. */
  markDemo: function (item) {
    item.isDemo = /^DEMO-/.test(item.goodsSn || '');
    return item;
  },
  /** 读取商城首页区块并更新页面展示. */
  getIndexData: function () {
    const that = this;
    const data = {};
    util.request(api.IndexUrlNewGoods).then(function (res) {
      if (res.code === 0) {
        data.newGoods = (res.data.newGoodsList || []).map(that.markDemo);
        that.setData(data);
      }
    });
    util.request(api.IndexUrlHotGoods).then(function (res) {
      if (res.code === 0) {
        data.hotGoods = (res.data.hotGoodsList || []).map(that.markDemo);
        that.setData(data);
      }
    });
    wx.stopPullDownRefresh();
  },
  /** 初始化商城首页状态并读取页面所需数据. */
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
              showCancel: false,
            });
          }
        });
      },
    });
  },
});
