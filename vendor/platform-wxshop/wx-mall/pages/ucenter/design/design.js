var util = require('../../../utils/util.js');
var api = require('../../../config/api.js');

/** 我的设计页面 / My designs page. */
Page({
  data: {
    designList: [],
    loading: false
  },

  /** 页面显示时读取当前用户的设计快照 / Loads the current user's design snapshots. */
  onShow: function () {
    this.loadDesigns();
  },

  /** 加载设计列表并转换为展示字段 / Loads designs and prepares display fields. */
  loadDesigns: function () {
    var that = this;
    that.setData({ loading: true });
    util.request(api.CustomizationList, {}).then(function (res) {
      if (res.code !== 0) {
        wx.showToast({ title: res.msg || '设计列表加载失败', icon: 'none' });
        return;
      }
      var designList = (res.data || []).map(function (item) {
        return {
          id: item.id,
          goodsId: item.goodsId,
          previewUrl: item.previewUrl || '/static/images/logo.png',
          canContinue: item.status === 'DRAFT',
          statusText: that.getStatusText(item.status),
          createTime: item.createTime || '',
          priceText: that.getPriceText(item.priceBreakdownJson)
        };
      });
      that.setData({ designList: designList });
    }).catch(function () {
      wx.showToast({ title: '设计列表加载失败', icon: 'none' });
    }).finally(function () {
      that.setData({ loading: false });
    });
  },

  /** 获取设计状态文案 / Gets design status text. */
  getStatusText: function (status) {
    return { DRAFT: '可继续编辑', LOCKED: '已锁定', ORDERED: '已下单' }[status] || '已保存';
  },

  /** 从报价 JSON 提取总价 / Extracts total price from quote JSON. */
  getPriceText: function (priceBreakdownJson) {
    try {
      var breakdown = JSON.parse(priceBreakdownJson || '{}');
      return breakdown.totalPrice === undefined ? '--' : '￥' + breakdown.totalPrice;
    } catch (error) {
      return '--';
    }
  },

  /** 跳转到设计器继续编辑 / Opens the designer for editing. */
  continueDesign: function (event) {
    var goodsId = Number(event.currentTarget.dataset.goodsId || 0);
    var snapshotId = Number(event.currentTarget.dataset.snapshotId || 0);
    if (!goodsId || !snapshotId) {
      wx.showToast({ title: '缺少商品信息', icon: 'none' });
      return;
    }
    wx.setStorageSync('designerGoodsId', goodsId);
    wx.setStorageSync('designerSnapshotId', snapshotId);
    wx.switchTab({ url: '/pages/designer/designer' });
  }
});
