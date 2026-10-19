const util = require('../../utils/util.js');
const api = require('../../config/api.js');

Page({
  data: {
    bannerInfo: {
      imgUrl: '',
      name: '',
    },
    categoryFilter: false,
    filterCategory: [],
    goodsList: [],
    categoryId: 0,
    currentSortType: 'default',
    currentSortOrder: 'desc',
    page: 1,
    size: 1000,
  },
  /** 读取热门商品的筛选条件和页面初始信息. */
  getData: function () {
    const that = this;
    util.request(api.GoodsHot).then(function (res) {
      if (res.code === 0) {
        that.setData({
          bannerInfo: res.data.bannerInfo,
        });
        that.getGoodsList();
      }
    });
  },
  /** 按当前条件分页读取热门商品商品列表. */
  getGoodsList() {
    const that = this;

    util
      .request(api.GoodsList, {
        isHot: 1,
        page: that.data.page,
        size: that.data.size,
        order: that.data.currentSortOrder,
        sort: that.data.currentSortType,
        categoryId: that.data.categoryId,
      })
      .then(function (res) {
        if (res.code === 0) {
          that.setData({
            goodsList: res.data.goodsList.records,
            filterCategory: res.data.filterCategory,
          });
        }
      });
  },
  /** 初始化热门商品状态并读取页面所需数据. */
  onLoad: function (options) {
    this.getData();
  },

  /** 切换排序或筛选条件并重新加载商品列表. */
  openSortFilter: function (event) {
    const currentId = event.currentTarget.id;
    switch (currentId) {
      case 'categoryFilter':
        this.setData({
          categoryFilter: !this.data.categoryFilter,
          currentSortType: 'category',
          currentSortOrder: 'asc',
        });
        break;
      case 'priceSort': {
        let tmpSortOrder = 'asc';
        if (this.data.currentSortOrder == 'asc') {
          tmpSortOrder = 'desc';
        }
        this.setData({
          currentSortType: 'price',
          currentSortOrder: tmpSortOrder,
          categoryFilter: false,
        });

        this.getData();
        break;
      }
      default:
        //综合排序
        this.setData({
          currentSortType: 'default',
          currentSortOrder: 'desc',
          categoryFilter: false,
        });
        this.getData();
    }
  },
  /** 应用当前分类筛选并刷新商品列表. */
  selectCategory: function (event) {
    const currentIndex = event.target.dataset.categoryIndex;
    this.setData({
      categoryFilter: false,
      categoryId: this.data.filterCategory[currentIndex].id,
    });
    this.getData();
  },
});
