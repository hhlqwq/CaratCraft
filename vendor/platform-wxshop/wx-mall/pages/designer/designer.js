var util = require('../../utils/util.js');
var api = require('../../config/api.js');
var model3d = require('../../utils/model3d.js');

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
    quote: {},
    primaryProductId: 0,
    saving: false,
    productConfig: {},
    previewMode: '2d',
    xrSupported: false,
    xrFailed: false,
    models3d: []
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
      that.setData({
        componentGroups: Object.keys(groups).map(function (key) { return groups[key]; }),
        primaryProductId: res.data.primaryProductId,
        productConfig: res.data.product,
        xrSupported: Boolean(wx.canIUse && wx.canIUse('xr-frame'))
      });
      that.refresh3dPreview();
    }).catch(function () {
      wx.showToast({ title: '定制配置加载失败', icon: 'none' });
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
    var groups = this.data.componentGroups.map(function (group) {
      group.options = group.options.map(function (option) {
        option.selected = selected.indexOf(option.id) >= 0;
        return option;
      });
      return group;
    });
    this.setData({ selectedOptionIds: selected, componentGroups: groups });
    this.refreshPreview();
    this.refresh3dPreview();
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

  /** 刷新当前选择对应的三维模型 / Refreshes 3D models for the current selection. */
  refresh3dPreview: function () {
    var models = [];
    var product = this.data.productConfig;
    if (product && product.modelUrl) {
      models.push(model3d.createXrModel('base', product.modelUrl, null));
    }
    var selected = this.data.selectedOptionIds;
    this.data.componentGroups.forEach(function (group) {
      group.options.forEach(function (option) {
        if (selected.indexOf(option.id) >= 0 && option.modelUrl) {
          models.push(model3d.createXrModel(option.id, option.modelUrl, option.materialParams));
        }
      });
    });
    this.setData({ models3d: models });
  },

  /** 切换二维或三维预览 / Switches between 2D and 3D previews. */
  switchPreview: function (event) {
    var mode = event.currentTarget.dataset.mode;
    if (mode === '3d' && (!this.data.xrSupported || !this.data.models3d.length)) {
      wx.showToast({ title: '当前设备或商品暂不支持 3D', icon: 'none' });
      return;
    }
    this.setData({ previewMode: mode, xrFailed: false });
  },

  /** 三维加载失败时回退二维预览 / Falls back to 2D when 3D loading fails. */
  handleXrError: function () {
    this.setData({ previewMode: '2d', xrFailed: true });
    wx.showToast({ title: '3D 加载失败，已切换 2D', icon: 'none' });
  },

  /** 更新刻字 / Updates engraving text. */
  changeEngraving: function (event) {
    this.setData({ engraving: event.detail.value });
  },

  /** 请求服务端报价 / Requests server-side quote. */
  refreshQuote: function () {
    var that = this;
    // 递增序号丢弃过期响应, 避免快速切换选项时旧报价覆盖新报价 /
    // Bump a sequence to drop stale responses racing with newer selections.
    var seq = (this._quoteSeq = (this._quoteSeq || 0) + 1);
    util.request(api.CustomizationQuote, {
      goodsId: that.data.goodsId,
      optionIds: that.data.selectedOptionIds,
      engraving: that.data.engraving
    }, 'POST', 'application/json').then(function (res) {
      if (seq !== that._quoteSeq) return;
      if (res.code === 0) that.setData({ quote: res.data });
    });
  },

  /** 保存设计草稿 / Saves a design draft. */
  saveDraft: function () {
    this.persistDesign(false).catch(function () {});
  },

  /** 保存设计，可选进入结算 / Saves a design and optionally starts checkout. */
  persistDesign: function (startCheckout) {
    var that = this;
    if (that.data.saving) return Promise.reject(new Error('正在保存'));
    that.setData({ saving: true });
    return util.request(api.CustomizationSave, {
      goodsId: that.data.goodsId,
      optionIds: that.data.selectedOptionIds,
      engraving: that.data.engraving
    }, 'POST', 'application/json').then(function (res) {
      that.setData({ saving: false });
      if (res.code !== 0) throw new Error(res.msg || '保存失败');
      if (!startCheckout) {
        wx.setStorageSync('lastSavedDesignSnapshotId', res.data.snapshotId);
        wx.showToast({ title: '设计已保存', icon: 'success' });
        return res.data.snapshotId;
      }
      wx.setStorageSync('designSnapshotId', res.data.snapshotId);
      wx.setStorageSync('designQuote', res.data.quote);
      return util.request(api.BuyAdd, {
        goodsId: that.data.goodsId,
        number: 1,
        productId: that.data.primaryProductId
      }, 'POST', 'application/json').then(function (buyRes) {
        if (buyRes.code !== 0) throw new Error(buyRes.msg || '创建购买信息失败');
        wx.navigateTo({ url: '/pages/shopping/checkout/checkout?isBuy=true' });
        return res.data.snapshotId;
      });
    }).catch(function (error) {
      that.setData({ saving: false });
      wx.showToast({ title: error.message || '操作失败', icon: 'none' });
      throw error;
    });
  },

  /** 确认设计并进入结算 / Confirms a design and starts checkout. */
  submitDesign: function () {
    if (!this.data.primaryProductId) {
      wx.showToast({ title: '商品规格未配置', icon: 'none' });
      return;
    }
    this.persistDesign(true).catch(function () {});
  },

  /** 初始化页面 / Initializes page state. */
  onLoad: function (options) {
    this.setData({ goodsId: Number(options.goodsId || 0) });
    if (this.data.goodsId) this.loadConfig();
  },

  /** 页面卸载时释放三维数据引用 / Releases 3D data references on unload. */
  onUnload: function () {
    this.setData({ models3d: [], previewMode: '2d' });
  }
});
