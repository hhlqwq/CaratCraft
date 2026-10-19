const util = require('../../utils/util.js');
const api = require('../../config/api.js');

Page({
  data: {
    navList: [],
    categoryList: [],
    currentCategory: {},
    scrollLeft: 0,
    scrollTop: 0,
    goodsCount: 0,
    scrollHeight: 0,
  },
  /** 初始化商城目录状态并读取页面所需数据. */
  onLoad: function (options) {
    this.getCatalog();
  },
  /** 读取分类导航及商品数量供目录页面展示. */
  getCatalog: function () {
    const that = this;
    wx.showLoading({
      title: '加载中...',
    });
    util.request(api.CatalogList).then(function (res) {
      that.setData({
        navList: res.data.categoryList,
        currentCategory: res.data.currentCategory,
      });
      wx.hideLoading();
    });
    util.request(api.GoodsCount).then(function (res) {
      that.setData({
        goodsCount: res.data.goodsCount,
      });
    });
  },
  /** 读取指定分类的子分类信息并更新当前选择. */
  getCurrentCategory: function (id) {
    const that = this;
    util.request(api.CatalogCurrent, {id: id}).then(function (res) {
      that.setData({
        currentCategory: res.data.currentCategory,
      });
    });
  },

  /** 读取当前分类下的商品展示列表. */
  getList: function () {
    const that = this;
    util.request(api.Catalog + that.data.currentCategory.id).then(function (res) {
      that.setData({
        categoryList: res.data,
      });
    });
  },
  /** 根据点击的分类标识切换商品展示范围. */
  switchCate: function (event) {
    if (this.data.currentCategory.id == event.currentTarget.dataset.id) {
      return false;
    }

    this.getCurrentCategory(event.currentTarget.dataset.id);
  },
});
