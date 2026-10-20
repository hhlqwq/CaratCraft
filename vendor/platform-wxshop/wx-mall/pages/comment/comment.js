const util = require('../../utils/util.js');
const api = require('../../config/api.js');

Page({
  data: {
    comments: [],
    allCommentList: [],
    picCommentList: [],
    typeId: 0,
    valueId: 0,
    showType: 0,
    allCount: 0,
    hasPicCount: 0,
    allPage: 1,
    picPage: 1,
    size: 20,
  },
  /** 读取全部评价与图片评价的数量. */
  getCommentCount() {
    const that = this;
    util
      .request(api.CommentCount, {valueId: that.data.valueId, typeId: that.data.typeId})
      .then(function (res) {
        if (res.code === 0) {
          that.setData({
            allCount: res.data.allCount,
            hasPicCount: res.data.hasPicCount,
          });
        }
      });
  },
  /** 按当前评价类型分页读取列表. */
  getCommentList() {
    const that = this;
    util
      .request(api.CommentList, {
        valueId: that.data.valueId,
        typeId: that.data.typeId,
        size: that.data.size,
        page: that.data.showType == 0 ? that.data.allPage : that.data.picPage,
        showType: that.data.showType,
      })
      .then(function (res) {
        if (res.code === 0) {
          if (that.data.showType == 0) {
            that.setData({
              allCommentList: that.data.allCommentList.concat(res.data.data),
              allPage: res.data.currentPage,
              comments: that.data.allCommentList.concat(res.data.data),
            });
          } else {
            that.setData({
              picCommentList: that.data.picCommentList.concat(res.data.data),
              picPage: res.data.currentPage,
              comments: that.data.picCommentList.concat(res.data.data),
            });
          }
        }
      });
  },
  /** 初始化商品评价状态并读取页面所需数据. */
  onLoad(options) {
    this.setData({
      typeId: options.typeId,
      valueId: options.valueId,
    });
    this.getCommentCount();
    this.getCommentList();
  },

  /** 切换评价类型并重新读取对应列表. */
  switchTab() {
    this.setData({
      showType: this.data.showType == 1 ? 0 : 1,
    });

    this.getCommentList();
  },
  /** 触底时继续加载商品评价的下一页内容. */
  onReachBottom() {
    if (this.data.showType == 0) {
      if (this.data.allCount / this.data.size < this.data.allPage) {
        return false;
      }

      this.setData({
        allPage: this.data.allPage + 1,
      });
    } else {
      if (this.data.hasPicCount / this.data.size < this.data.picPage) {
        return false;
      }

      this.setData({
        picPage: this.data.picPage + 1,
      });
    }

    this.getCommentList();
  },
});
