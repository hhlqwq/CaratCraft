const util = require('../../utils/util.js');
const api = require('../../config/api.js');

Page({
  data: {
    id: 0,
    brand: {},
    goodsList: [],
    page: 1,
    size: 1000,
  },
  /** 初始化品牌详情状态并读取页面所需数据. */
  onLoad: function (options) {
    const that = this;
    that.setData({
      id: parseInt(options.id),
    });
    this.getBrand();
  },
  /** 读取当前品牌介绍和展示信息. */
  getBrand: function () {
    const that = this;
    util.request(api.BrandDetail, {id: that.data.id}).then(function (res) {
      if (res.code === 0) {
        that.setData({
          brand: res.data.brand,
        });

        that.getGoodsList();
      }
    });
  },
  /** 按当前条件分页读取品牌详情商品列表. */
  getGoodsList() {
    const that = this;

    util
      .request(api.GoodsList, {brandId: that.data.id, page: that.data.page, size: that.data.size})
      .then(function (res) {
        if (res.code === 0) {
          that.setData({
            goodsList: res.data.goodsList.records,
          });
        }
      });
  },
});
