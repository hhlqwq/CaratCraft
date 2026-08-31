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
    selectedOptionNames: [],
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
      var groupNames = {
        pearl: '珍珠种类与规格',
        material: '贵金属材质',
        gem: '钻石与宝石',
        fitting: '耳饰与连接配件',
        style: '成品款式',
        chain: '链条材质',
        length: '项链长度',
        pendant: '吊坠款式',
        engraving: '刻字工艺'
      };
      var groupDescriptions = {
        pearl: '淡水与海水珍珠按品种、品质、尺寸分别报价。',
        material: '选择 18K 金、铂金等主体或连接件材质。',
        gem: '选择钻石、莫桑石、锆石等镶嵌方案。',
        fitting: '选择耳钉、耳钩、耳夹或项链扣等配件。',
        style: '确定珠宝的整体结构和佩戴方式。',
        chain: '选择链条的金属和工艺。',
        length: '选择适合佩戴场景的成品长度。',
        pendant: '选择吊坠造型。',
        engraving: '选择刻字位置或工艺。'
      };
      res.data.options.forEach(function (option) {
        if (!groups[option.componentType]) {
          groups[option.componentType] = {
            type: option.componentType,
            name: groupNames[option.componentType] || option.componentType,
            description: groupDescriptions[option.componentType] || '请选择一个适合当前设计的选项。',
            options: []
          };
        }
        groups[option.componentType].options.push(option);
      });
      var componentGroups = Object.keys(groups).map(function (key, index) {
        groups[key].step = index + 1;
        return groups[key];
      });
      var selectedOptionIds = [];
      componentGroups.forEach(function (group) {
        if (group.options.length > 0) {
          group.options[0].selected = true;
          selectedOptionIds.push(group.options[0].id);
        }
      });
      that.setData({
        componentGroups: componentGroups,
        selectedOptionIds: selectedOptionIds,
        selectedOptionNames: that.getSelectedOptionNames(componentGroups, selectedOptionIds),
        primaryProductId: res.data.primaryProductId,
        productConfig: res.data.product,
        xrSupported: Boolean(wx.canIUse && wx.canIUse('xr-frame'))
      });
      that.refreshPreview();
      that.refresh3dPreview();
      that.refreshQuote();
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
    this.setData({
      selectedOptionIds: selected,
      componentGroups: groups,
      selectedOptionNames: this.getSelectedOptionNames(groups, selected)
    });
    this.refreshPreview();
    this.refresh3dPreview();
    this.refreshQuote();
  },

  /** 获取当前已选组件名称 / Gets the names of selected component options. */
  getSelectedOptionNames: function (groups, selectedOptionIds) {
    var names = [];
    groups.forEach(function (group) {
      group.options.forEach(function (option) {
        if (selectedOptionIds.indexOf(option.id) >= 0) names.push(option.name);
      });
    });
    return names;
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

  /** 更新刻字并刷新报价（防抖）/ Updates engraving and refreshes quote with debounce. */
  changeEngraving: function (event) {
    var that = this;
    that.setData({ engraving: event.detail.value });
    if (that._engravingTimer) clearTimeout(that._engravingTimer);
    that._engravingTimer = setTimeout(function () {
      that.refreshQuote();
    }, 300);
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

  /** 默认定制商品 / Default custom goods id. */
  DEFAULT_GOODS_ID: 1006002,

  /** 初始化页面 / Initializes page state. */
  onLoad: function (options) {
    var goodsId = Number(options && options.goodsId || 0);
    // tabBar 入口不带参数, 优先取商品页传入的 ID, 再回退默认演示商品 /
    // Tab entry carries no query; prefer the id stashed by the goods page, then the default.
    if (!goodsId) goodsId = Number(wx.getStorageSync('designerGoodsId') || 0);
    if (!goodsId) goodsId = this.DEFAULT_GOODS_ID;
    wx.removeStorageSync('designerGoodsId');
    this.setData({ goodsId: goodsId });
    this.loadConfig();
  },

  /** 每次 tab 展示时重载配置, 保证从商品页带入新 ID / Reloads config on each tab show. */
  onShow: function () {
    var goodsId = Number(wx.getStorageSync('designerGoodsId') || 0);
    if (goodsId && goodsId !== this.data.goodsId) {
      wx.removeStorageSync('designerGoodsId');
      this.setData({ goodsId: goodsId, selectedOptionIds: [], selectedLayers: [], engraving: '', quote: {} });
      this.loadConfig();
    }
  },

  /** 页面卸载时释放三维数据引用 / Releases 3D data references on unload. */
  onUnload: function () {
    this.setData({ models3d: [], previewMode: '2d' });
  }
});
