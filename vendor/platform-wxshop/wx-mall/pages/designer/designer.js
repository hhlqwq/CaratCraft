var util = require('../../utils/util.js');
var api = require('../../config/api.js');

/**
 * 项链 DIY 页面 / Necklace DIY page.
 */
Page({
  data: {
    goodsId: 0,
    componentGroups: [],
    selectedOptionIds: [],
    selectedLayers: [],
    engraving: '',
    quote: {}
  },

  /** 加载定制商品配置 / Loads custom product configuration. */
  loadConfig: function () {
    var that = this;
    util.request(api.CustomizationConfig, { goodsId: that.data.goodsId }).then(function (res) {
      if (res.code !== 0) {
        wx.showToast({ title: res.msg || '暂不可定制', icon: 'none' });
        return;
      }
      var groups = {};
      res.data.options.forEach(function (option) {
        if (!groups[option.componentType]) {
          groups[option.componentType] = { type: option.componentType, name: option.componentType, options: [] };
        }
        groups[option.componentType].options.push(option);
      });
      that.setData({ componentGroups: Object.keys(groups).map(function (key) { return groups[key]; }) });
    });
  },

  /** 选择互斥组件 / Selects one option in a component group. */
  selectOption: function (event) {
    var optionId = Number(event.currentTarget.dataset.id);
    var componentType = event.currentTarget.dataset.type;
    var selected = this.data.selectedOptionIds.slice();
    this.data.componentGroups.forEach(function (group) {
      if (group.type === componentType) {
        group.options.forEach(function (option) {
          var index = selected.indexOf(option.id);
          if (index >= 0) selected.splice(index, 1);
        });
      }
    });
    selected.push(optionId);
    this.setData({ selectedOptionIds: selected });
    this.refreshPreview();
    this.refreshQuote();
  },

  /** 刷新叠层预览 / Refreshes layered preview. */
  refreshPreview: function () {
    var selected = this.data.selectedOptionIds;
    var layers = [];
    this.data.componentGroups.forEach(function (group) {
      group.options.forEach(function (option) {
        if (selected.indexOf(option.id) >= 0 && option.assetUrl) layers.push(option);
      });
    });
    layers.sort(function (left, right) { return left.zIndex - right.zIndex; });
    this.setData({ selectedLayers: layers });
  },

  /** 更新刻字 / Updates engraving text. */
  changeEngraving: function (event) {
    this.setData({ engraving: event.detail.value });
  },

  /** 请求服务端报价 / Requests server-side quote. */
  refreshQuote: function () {
    var that = this;
    util.request(api.CustomizationQuote, {
      goodsId: that.data.goodsId,
      optionIds: that.data.selectedOptionIds,
      engraving: that.data.engraving
    }, 'POST', 'application/json').then(function (res) {
      if (res.code === 0) that.setData({ quote: res.data });
    });
  },

  /** 暂存设计提示 / Shows draft-save guidance. */
  saveDraft: function () {
    wx.showToast({ title: '设计保存将在订单模块接入后启用', icon: 'none' });
  },

  /** 引导确认设计 / Guides customer to confirmation. */
  submitDesign: function () {
    wx.showToast({ title: '订单快照模块开发中', icon: 'none' });
  },

  /** 初始化页面 / Initializes page state. */
  onLoad: function (options) {
    this.setData({ goodsId: Number(options.goodsId || 0) });
    if (this.data.goodsId) this.loadConfig();
  }
});
