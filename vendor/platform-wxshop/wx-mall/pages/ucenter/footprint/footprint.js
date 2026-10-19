const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

Page({
  data: {
    footprintList: [],
  },
  /** 下拉时重新加载页面数据并结束刷新提示. */
  onPullDownRefresh() {
    // 增加下拉刷新数据的功能
    this.getFootprintList(true);
  },
  /** 读取本人浏览足迹并按日期更新展示集合. */
  getFootprintList(stopRefresh) {
    const that = this;
    let tmpFootPrint;
    util
      .request(api.FootprintList)
      .then(function (res) {
        if (res.code === 0) {
          if (res.data.data != undefined) {
            tmpFootPrint = res.data.data;
          } else {
            tmpFootPrint = [];
          }

          that.setData({
            footprintList: tmpFootPrint,
          });
        }
        if (stopRefresh) {
          wx.stopPullDownRefresh();
        }
      })
      .catch(function () {
        if (stopRefresh) {
          wx.stopPullDownRefresh();
        }
      });
  },
  /** 删除本人指定浏览足迹并更新列表. */
  deleteItem(event) {
    const that = this;
    const footprint = event.currentTarget.dataset.footprint;
    const touchTime = that.data.touch_end - that.data.touch_start;
    //如果按下时间大于350为长按
    if (touchTime > 350) {
      wx.showModal({
        title: '',
        content: '要删除所选足迹？',
        success: function (res) {
          if (res.confirm) {
            util.request(api.FootprintDelete, {footprintId: footprint.id}).then(function (res) {
              if (res.code === 0) {
                wx.showToast({
                  title: res.msg,
                  icon: 'success',
                  duration: 2000,
                  complete: function () {
                    that.getFootprintList();
                  },
                });
              } else {
                util.showErrorToast(res.msg);
              }
            });
          }
        },
      });
    } else {
      wx.navigateTo({
        url: '/pages/goods/goods?id=' + footprint.goodsId,
      });
    }
  },
  /** 初始化浏览足迹状态并读取页面所需数据. */
  onLoad: function (options) {
    this.getFootprintList();
  },

  /** 页面再次展示时刷新浏览足迹状态. */
  onShow: function () {
    this.getFootprintList();
  },

  //按下事件开始
  touchStart: function (e) {
    const that = this;
    that.setData({
      touch_start: e.timeStamp,
    });
  },
  //按下事件结束
  touchEnd: function (e) {
    const that = this;
    that.setData({
      touch_end: e.timeStamp,
    });
  },
});
