const util = require('../../utils/util.js');
const api = require('../../config/api.js');

Page({
  data: {
    brandList: [],
    page: 1,
    size: 10,
    totalPages: 1,
  },
  /** 初始化品牌列表状态并读取页面所需数据. */
  onLoad: function (options) {
    this.getBrandList();
  },
  /** 分页读取品牌列表并追加到当前展示集合. */
  getBrandList: function () {
    wx.showLoading({
      title: '加载中...',
    });
    const that = this;
    util.request(api.BrandList, {page: that.data.page, size: that.data.size}).then(function (res) {
      if (res.code === 0) {
        that.setData({
          brandList: that.data.brandList.concat(res.data.records),
          totalPages: res.data.total,
        });
      }
      wx.hideLoading();
    });
  },
  /** 触底时继续加载品牌列表的下一页内容. */
  onReachBottom() {
    if (this.data.totalPages > this.data.page) {
      this.setData({
        page: this.data.page + 1,
      });
    } else {
      return false;
    }

    this.getBrandList();
  },
});
