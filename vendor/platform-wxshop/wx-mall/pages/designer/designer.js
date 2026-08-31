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
    engraving: '',
    quote: {},
    primaryProductId: 0,
    saving: false,
    productConfig: {},
    previewMode: '2d',
    xrSupported: false,
    xrFailed: false,
    models3d: [],
    loadingConfig: false,
    configError: '',
    activeProductType: 'necklace',
    designerProducts: [
      { goodsId: 1999105, type: 'necklace', name: '定制项链', description: '珍珠、链型与可选升级' },
      { goodsId: 1999106, type: 'earring', name: '定制耳饰', description: '耳钉、耳钩与垂坠结构' }
    ]
  },

  /** 加载定制商品配置 / Loads custom product configuration. */
  loadConfig: function () {
    var that = this;
    that.setData({ loadingConfig: true, configError: '', componentGroups: [] });
    util.request(api.CustomizationConfig, { goodsId: that.data.goodsId }, 'GET').then(function (res) {
      if (res.code !== 0) {
        that.setData({ loadingConfig: false, configError: res.msg || '该商品暂不可定制' });
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
      var requiredTypes = (res.data.product.requiredComponentTypes || '')
        .split(',')
        .map(function (type) { return type.trim(); })
        .filter(function (type) { return Boolean(type); });
      var options = res.data && res.data.options ? res.data.options : [];
      if (!options.length) {
        that.setData({ loadingConfig: false, configError: '商家尚未配置可选组件' });
        return;
      }
      options.forEach(function (option) {
        if (!groups[option.componentType]) {
          groups[option.componentType] = {
            type: option.componentType,
            name: groupNames[option.componentType] || option.componentType,
            description: groupDescriptions[option.componentType] || '请选择一个适合当前设计的选项。',
            required: requiredTypes.indexOf(option.componentType) >= 0,
            options: []
          };
        }
        groups[option.componentType].options.push(option);
      });
      var componentGroups = Object.keys(groups).map(function (key, index) {
        return groups[key];
      });
      var groupOrder = that.data.activeProductType === 'earring'
        ? ['style', 'pearl', 'fitting', 'material', 'gem']
        : ['style', 'pearl', 'material', 'gem', 'length', 'fitting'];
      componentGroups.sort(function (left, right) {
        return groupOrder.indexOf(left.type) - groupOrder.indexOf(right.type);
      });
      componentGroups.forEach(function (group, index) { group.step = index + 1; });
      var selectedOptionIds = [];
      componentGroups.forEach(function (group) {
        if (group.required && group.options.length > 0) {
          group.options[0].selected = true;
          selectedOptionIds.push(group.options[0].id);
        }
        group.hasSelection = group.required && group.options.length > 0;
      });
      that.setData({
        componentGroups: componentGroups,
        selectedOptionIds: selectedOptionIds,
        selectedOptionNames: that.getSelectedOptionNames(componentGroups, selectedOptionIds),
        primaryProductId: res.data.primaryProductId,
        productConfig: res.data.product,
        xrSupported: Boolean(wx.canIUse && wx.canIUse('xr-frame')),
        loadingConfig: false,
        configError: ''
      });
      that.refreshPreview();
      that.refresh3dPreview();
      that.refreshQuote();
    }).catch(function (error) {
      that.setData({
        loadingConfig: false,
        configError: error && error.message ? error.message : '定制配置加载失败，请稍后重试'
      });
    });
  },

  /** 重新加载定制配置 / Reloads customizer configuration. */
  retryLoadConfig: function () {
    this.loadConfig();
  },

  /** 选择互斥组件 / Selects one option in a component group. */
  selectOption: function (event) {
    var optionId = Number(event.currentTarget.dataset.id);
    var componentType = event.currentTarget.dataset.type;
    var selected = this.data.selectedOptionIds.slice();
    var selectedGroup = null;
    this.data.componentGroups.forEach(function (group) {
      if (group.type === componentType) {
        selectedGroup = group;
        group.options.forEach(function (option) {
          var index = selected.indexOf(option.id);
          if (index >= 0) selected.splice(index, 1);
        });
      }
    });
    var isCurrentSelection = this.data.selectedOptionIds.indexOf(optionId) >= 0;
    if (!isCurrentSelection || (selectedGroup && selectedGroup.required)) selected.push(optionId);
    var groups = this.data.componentGroups.map(function (group) {
      group.options = group.options.map(function (option) {
        option.selected = selected.indexOf(option.id) >= 0;
        return option;
      });
      group.hasSelection = group.options.some(function (option) { return option.selected; });
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

  /** 清除可选升级项 / Clears an optional upgrade selection. */
  clearOptional: function (event) {
    var componentType = event.currentTarget.dataset.type;
    var selected = this.data.selectedOptionIds.slice();
    var groups = this.data.componentGroups.map(function (group) {
      if (group.type !== componentType || group.required) return group;
      group.options = group.options.map(function (option) {
        var index = selected.indexOf(option.id);
        if (index >= 0) selected.splice(index, 1);
        option.selected = false;
        return option;
      });
      group.hasSelection = false;
      return group;
    });
    this.setData({
      selectedOptionIds: selected,
      componentGroups: groups,
      selectedOptionNames: this.getSelectedOptionNames(groups, selected)
    });
    this.refreshPreview();
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

  /** 刷新二维预览 / Refreshes the 2D preview. */
  refreshPreview: function () {
    var that = this;
    setTimeout(function () { that.drawPreview(); }, 20);
  },

  /** 获取按组件类型索引的已选项 / Gets selected options indexed by component type. */
  getSelectedOptionsByType: function () {
    var selected = this.data.selectedOptionIds;
    var result = {};
    this.data.componentGroups.forEach(function (group) {
      group.options.forEach(function (option) {
        if (selected.indexOf(option.id) >= 0) result[group.type] = option;
      });
    });
    return result;
  },

  /** 绘制定制预览 / Draws the customization preview. */
  drawPreview: function () {
    var context = wx.createCanvasContext('designCanvas', this);
    var selected = this.getSelectedOptionsByType();
    context.setFillStyle('#f4f0e8');
    context.fillRect(0, 0, 300, 300);
    if (this.data.activeProductType === 'earring') {
      this.drawEarringPreview(context, selected);
    } else {
      this.drawNecklacePreview(context, selected);
    }
    context.draw();
  },

  /** 解析所选金属颜色 / Resolves the selected metal color. */
  getMetalColor: function (selected) {
    var code = selected.material ? selected.material.optionCode : '';
    if (code.indexOf('rose') >= 0) return '#c99386';
    if (code.indexOf('yellow') >= 0) return '#c6a25a';
    if (code.indexOf('18k-white') >= 0 || code.indexOf('pt950') >= 0) return '#aeb5b8';
    return '#b9aa96';
  },

  /** 解析所选珍珠颜色 / Resolves the selected pearl color. */
  getPearlColor: function (selected) {
    var code = selected.pearl ? selected.pearl.optionCode : '';
    if (code.indexOf('tahiti') >= 0) return '#52605d';
    if (code.indexOf('southsea-gold') >= 0) return '#d8b978';
    if (code.indexOf('purple') >= 0) return '#c7afc4';
    return '#f3eee4';
  },

  /** 绘制项链预览 / Draws a necklace preview. */
  drawNecklacePreview: function (context, selected) {
    var metalColor = this.getMetalColor(selected);
    var pearlColor = this.getPearlColor(selected);
    context.setStrokeStyle('#ded5c7');
    context.setLineWidth(2);
    context.beginPath();
    context.moveTo(32, 44);
    context.bezierCurveTo(58, 112, 72, 132, 94, 148);
    context.moveTo(268, 44);
    context.bezierCurveTo(242, 112, 228, 132, 206, 148);
    context.stroke();
    context.setStrokeStyle(metalColor);
    context.setLineWidth(3);
    context.beginPath();
    context.moveTo(62, 55);
    context.bezierCurveTo(68, 218, 232, 218, 238, 55);
    if (selected.style && selected.style.optionCode.indexOf('double') >= 0) {
      context.moveTo(72, 65);
      context.bezierCurveTo(84, 190, 216, 190, 228, 65);
    }
    context.stroke();
    context.setFillStyle(pearlColor);
    context.setStrokeStyle('#a99d8d');
    context.setLineWidth(1.5);
    context.beginPath();
    context.arc(150, 211, 17, 0, Math.PI * 2);
    context.fill();
    context.stroke();
    context.setFillStyle('rgba(255,255,255,.72)');
    context.beginPath();
    context.arc(144, 205, 5, 0, Math.PI * 2);
    context.fill();
    if (selected.style && selected.style.optionCode.indexOf('double') >= 0) {
      context.setFillStyle(pearlColor);
      context.beginPath();
      context.arc(180, 193, 11, 0, Math.PI * 2);
      context.fill();
      context.stroke();
    }
    this.drawSelectedGem(context, selected, 150, 242);
    context.setFillStyle('#71685c');
    context.setFontSize(11);
    context.setTextAlign('center');
    context.fillText('项链正面佩戴示意', 150, 278);
  },

  /** 绘制耳饰预览 / Draws an earring preview. */
  drawEarringPreview: function (context, selected) {
    var metalColor = this.getMetalColor(selected);
    var pearlColor = this.getPearlColor(selected);
    var isDrop = selected.style && selected.style.optionCode.indexOf('drop') >= 0;
    [92, 208].forEach(function (centerX) {
      context.setStrokeStyle('#d9cec0');
      context.setLineWidth(2);
      context.beginPath();
      context.arc(centerX, 116, 34, Math.PI * 0.2, Math.PI * 1.8);
      context.stroke();
      context.setStrokeStyle(metalColor);
      context.setLineWidth(3);
      context.beginPath();
      if (selected.fitting && selected.fitting.optionCode.indexOf('hook') >= 0) {
        context.arc(centerX, 142, 13, Math.PI, Math.PI * 2.4);
      } else if (selected.fitting && selected.fitting.optionCode.indexOf('hoop') >= 0) {
        context.arc(centerX, 152, 18, 0, Math.PI * 2);
      } else {
        context.moveTo(centerX, 135);
        context.lineTo(centerX, isDrop ? 184 : 158);
      }
      context.stroke();
      var pearlY = isDrop ? 205 : 166;
      context.setFillStyle(pearlColor);
      context.setStrokeStyle('#a99d8d');
      context.setLineWidth(1.5);
      context.beginPath();
      context.arc(centerX, pearlY, isDrop ? 16 : 14, 0, Math.PI * 2);
      context.fill();
      context.stroke();
      context.setFillStyle('rgba(255,255,255,.72)');
      context.beginPath();
      context.arc(centerX - 5, pearlY - 5, 4, 0, Math.PI * 2);
      context.fill();
    });
    this.drawSelectedGem(context, selected, 92, isDrop ? 175 : 142);
    this.drawSelectedGem(context, selected, 208, isDrop ? 175 : 142);
    context.setFillStyle('#71685c');
    context.setFontSize(11);
    context.setTextAlign('center');
    context.fillText('耳饰成对佩戴示意', 150, 278);
  },

  /** 绘制可选宝石 / Draws an optional gemstone. */
  drawSelectedGem: function (context, selected, centerX, centerY) {
    if (!selected.gem || selected.gem.optionCode === 'gem-none') return;
    var color = selected.gem.optionCode.indexOf('diamond') >= 0 ? '#b8dce8' : '#d4e5e4';
    context.setFillStyle(color);
    context.setStrokeStyle('#7796a0');
    context.setLineWidth(1);
    context.beginPath();
    context.moveTo(centerX, centerY - 7);
    context.lineTo(centerX + 7, centerY);
    context.lineTo(centerX, centerY + 7);
    context.lineTo(centerX - 7, centerY);
    context.closePath();
    context.fill();
    context.stroke();
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

  /** 切换项链或耳饰定制商品 / Switches between necklace and earring products. */
  switchProduct: function (event) {
    var goodsId = Number(event.currentTarget.dataset.goodsId);
    var productType = event.currentTarget.dataset.type;
    if (!goodsId || goodsId === this.data.goodsId) return;
    this.setData({
      goodsId: goodsId,
      activeProductType: productType,
      selectedOptionIds: [],
      selectedOptionNames: [],
      componentGroups: [],
      engraving: '',
      quote: {}
    });
    this.loadConfig();
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
  DEFAULT_GOODS_ID: 1999105,

  /** 初始化页面 / Initializes page state. */
  onLoad: function (options) {
    var goodsId = Number(options && options.goodsId || 0);
    // tabBar 入口不带参数, 优先取商品页传入的 ID, 再回退默认演示商品 /
    // Tab entry carries no query; prefer the id stashed by the goods page, then the default.
    if (!goodsId) goodsId = Number(wx.getStorageSync('designerGoodsId') || 0);
    if (!goodsId) goodsId = this.DEFAULT_GOODS_ID;
    wx.removeStorageSync('designerGoodsId');
    this.setData({ goodsId: goodsId, activeProductType: goodsId === 1999106 ? 'earring' : 'necklace' });
    this.loadConfig();
  },

  /** 每次 tab 展示时重载配置, 保证从商品页带入新 ID / Reloads config on each tab show. */
  onShow: function () {
    var goodsId = Number(wx.getStorageSync('designerGoodsId') || 0);
    if (goodsId && goodsId !== this.data.goodsId) {
      wx.removeStorageSync('designerGoodsId');
      this.setData({
        goodsId: goodsId,
        activeProductType: goodsId === 1999106 ? 'earring' : 'necklace',
        selectedOptionIds: [],
        engraving: '',
        quote: {}
      });
      this.loadConfig();
    }
  },

  /** 页面卸载时释放三维数据引用 / Releases 3D data references on unload. */
  onUnload: function () {
    this.setData({ models3d: [], previewMode: '2d' });
  }
});
