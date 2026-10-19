const util = require('../../utils/util.js');
const api = require('../../config/api.js');

Page({
  data: {
    keywrod: '',
    searchStatus: false,
    goodsList: [],
    helpKeyword: [],
    historyKeyword: [],
    categoryFilter: false,
    filterCategory: [],
    defaultKeyword: {},
    hotKeyword: [],
    page: 1,
    size: 20,
    currentSortType: 'id',
    currentSortOrder: 'desc',
    categoryId: 0,
  },
  //事件处理函数
  closeSearch: function () {
    wx.navigateBack();
  },
  /** 清空关键词并恢复搜索建议状态. */
  clearKeyword: function () {
    this.setData({
      keyword: '',
      searchStatus: false,
    });
  },
  /** 初始化商品搜索状态并读取页面所需数据. */
  onLoad: function (options) {
    this.getSearchKeyword();
    if (options && options.keyword) {
      this.getSearchResult(options.keyword);
    }
  },

  /** 读取默认搜索词、热门关键词和搜索历史. */
  getSearchKeyword() {
    const that = this;
    util.request(api.SearchIndex).then(function (res) {
      if (res.code === 0) {
        that.setData({
          historyKeyword: res.data.historyKeywordList,
          defaultKeyword: res.data.defaultKeyword,
          hotKeyword: res.data.hotKeywordList,
        });
      }
    });
  },

  /** 更新搜索词并按输入刷新联想建议. */
  inputChange: function (e) {
    this.setData({
      keyword: e.detail.value,
      searchStatus: false,
    });
    this.getHelpKeyword();
  },
  /** 请求与当前输入匹配的搜索建议. */
  getHelpKeyword: function () {
    const that = this;
    util.request(api.SearchHelper, {keyword: that.data.keyword}).then(function (res) {
      if (res.code === 0) {
        that.setData({
          helpKeyword: res.data,
        });
      }
    });
  },
  /** 恢复搜索输入状态并显示搜索建议. */
  inputFocus: function () {
    this.setData({
      searchStatus: false,
      goodsList: [],
    });

    if (this.data.keyword) {
      this.getHelpKeyword();
    }
  },
  /** 清除当前用户搜索历史并刷新页面展示. */
  clearHistory: function () {
    this.setData({
      historyKeyword: [],
    });

    util.request(api.SearchClearHistory, {}).then(function (res) {});
  },
  /** 按当前条件分页读取商品搜索商品列表. */
  getGoodsList: function () {
    const that = this;
    util
      .request(api.GoodsList, {
        keyword: that.data.keyword,
        page: that.data.page,
        size: that.data.size,
        sort: that.data.currentSortType,
        order: that.data.currentSortOrder,
        categoryId: that.data.categoryId,
      })
      .then(function (res) {
        if (res.code === 0) {
          that.setData({
            searchStatus: true,
            categoryFilter: false,
            goodsList: res.data.goodsList.records,
            filterCategory: res.data.filterCategory,
            page: res.data.goodsList.current,
            size: res.data.goodsList.size,
          });
        }

        //重新获取关键词
        that.getSearchKeyword();
      });
  },
  /** 将点击的建议词设为当前搜索词并执行搜索. */
  onKeywordTap: function (event) {
    this.getSearchResult(event.target.dataset.keyword);
  },
  /** 按当前关键词发起商品搜索并更新列表状态. */
  getSearchResult(keyword) {
    this.setData({
      keyword: keyword,
      page: 1,
      categoryId: 0,
      goodsList: [],
    });

    this.getGoodsList();
  },
  /** 切换排序或筛选条件并重新加载商品列表. */
  openSortFilter: function (event) {
    const currentId = event.currentTarget.id;
    switch (currentId) {
      case 'categoryFilter':
        this.setData({
          categoryFilter: !this.data.categoryFilter,
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

        this.getGoodsList();
        break;
      }
      default:
        //综合排序
        this.setData({
          currentSortType: 'default',
          currentSortOrder: 'desc',
          categoryFilter: false,
        });
        this.getGoodsList();
    }
  },
  /** 应用当前分类筛选并刷新商品列表. */
  selectCategory: function (event) {
    const currentIndex = event.target.dataset.categoryIndex;
    const filterCategory = this.data.filterCategory;
    let currentCategory = null;
    for (const key in filterCategory) {
      if (key == currentIndex) {
        filterCategory[key].selected = true;
        currentCategory = filterCategory[key];
      } else {
        filterCategory[key].selected = false;
      }
    }
    this.setData({
      filterCategory: filterCategory,
      categoryFilter: false,
      categoryId: currentCategory.id,
      page: 1,
      goodsList: [],
    });
    this.getGoodsList();
  },
  /** 响应键盘确认并执行当前关键词搜索. */
  onKeywordConfirm(event) {
    this.getSearchResult(event.detail.value);
  },
});
