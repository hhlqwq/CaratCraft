const util = require('../../utils/util.js');
const api = require('../../config/api.js');

Page({
  data: {
    // text:"这是一个页面"
    navList: [],
    goodsList: [],
    id: 0,
    currentCategory: {},
    scrollLeft: 0,
    scrollTop: 0,
    scrollHeight: 0,
    page: 1,
    size: 10,
    loadmoreText: '正在加载更多数据',
    nomoreText: '全部加载完成',
    nomore: false,
    totalPages: 1,
  },
  /** 初始化分类商品状态并读取页面所需数据. */
  onLoad: function (options) {
    const that = this;
    if (options.id) {
      that.setData({
        id: parseInt(options.id),
      });
    }

    wx.getSystemInfo({
      success: function (res) {
        that.setData({
          scrollHeight: res.windowHeight,
        });
      },
    });

    this.getCategoryInfo();
  },
  /** 读取当前分类详情与可切换的同级分类. */
  getCategoryInfo: function () {
    const that = this;
    util.request(api.GoodsCategory, {id: this.data.id}).then(function (res) {
      if (res.code == 0) {
        that.setData({
          navList: res.data.brotherCategory,
          currentCategory: res.data.currentCategory,
        });

        //nav位置
        let currentIndex = 0;
        const navListCount = that.data.navList.length;
        for (let i = 0; i < navListCount; i++) {
          currentIndex += 1;
          if (that.data.navList[i].id == that.data.id) {
            break;
          }
        }
        if (currentIndex > navListCount / 2 && navListCount > 5) {
          that.setData({
            scrollLeft: currentIndex * 60,
          });
        }
        that.getGoodsList();
      } else {
        //显示错误信息
      }
    });
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom: function () {
    this.getGoodsList();
  },

  /** 按当前条件分页读取分类商品商品列表. */
  getGoodsList: function () {
    const that = this;

    if (that.data.totalPages <= that.data.page - 1) {
      that.setData({
        nomore: true,
      });
      return;
    }

    util
      .request(api.GoodsList, {
        categoryId: that.data.id,
        page: that.data.page,
        size: that.data.size,
      })
      .then(function (res) {
        that.setData({
          goodsList: that.data.goodsList.concat(res.data.goodsList.records),
          page: res.data.goodsList.current + 1,
          totalPages: res.data.goodsList.pages,
        });
      });
  },

  /** 根据点击的分类标识切换商品展示范围. */
  switchCate: function (event) {
    if (this.data.id == event.currentTarget.dataset.id) {
      return false;
    }
    const that = this;
    const clientX = event.detail.x;
    const currentTarget = event.currentTarget;
    if (clientX < 60) {
      that.setData({
        scrollLeft: currentTarget.offsetLeft - 60,
      });
    } else if (clientX > 330) {
      that.setData({
        scrollLeft: currentTarget.offsetLeft,
      });
    }
    this.setData({
      id: event.currentTarget.dataset.id,
      page: 1,
      totalPages: 1,
      goodsList: [],
      nomore: false,
    });

    this.getCategoryInfo();
  },
});
