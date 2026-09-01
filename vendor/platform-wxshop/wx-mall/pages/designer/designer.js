var util = require('../../utils/util.js');
var api = require('../../config/api.js');

var DEFAULT_GOODS_ID = 1999105;
var LAYER_BASE_PATH = '/static/images/designer/layers/';
var DEFAULT_PREVIEW = {
  chainUrl: LAYER_BASE_PATH + 'chain-white-gold.png',
  pearlUrl: LAYER_BASE_PATH + 'pearl-akoya-white.png',
  pearlSize: 'medium',
  gemUrl: '',
  doublePearl: false,
  description: 'Akoya 白珍珠 · 18K 白金 · 经典单珠'
};

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
    previewLayers: DEFAULT_PREVIEW,
    loadingConfig: false,
    configError: '',
    showOptional: false
  },

  /** 加载定制商品配置 / Loads custom product configuration. */
  loadConfig: function (restoredConfiguration) {
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
        fitting: '搭配配件',
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
        fitting: '可按需增加耳钉、耳钩、耳坠或项链扣，不选也可以。',
        style: '确定珠宝的整体结构和佩戴方式。',
        chain: '选择链条的金属和工艺。',
        length: '选择适合佩戴场景的成品长度。',
        pendant: '选择吊坠造型。',
        engraving: '选择刻字位置或工艺。'
      };
      var primaryTypes = ['pearl', 'material', 'style'];
      var options = res.data && res.data.options ? res.data.options : [];
      if (!options.length) {
        that.setData({ loadingConfig: false, configError: '商家尚未配置可选组件' });
        return;
      }
      options.forEach(function (option) {
        option.displayName = (option.name || '').replace(/^演示\s*[·・]\s*/, '');
        if (!groups[option.componentType]) {
          groups[option.componentType] = {
            type: option.componentType,
            name: groupNames[option.componentType] || option.componentType,
            description: groupDescriptions[option.componentType] || '请选择一个适合当前设计的选项。',
            required: primaryTypes.indexOf(option.componentType) >= 0,
            primary: primaryTypes.indexOf(option.componentType) >= 0,
            options: []
          };
        }
        groups[option.componentType].options.push(option);
      });
      var componentGroups = Object.keys(groups).map(function (key, index) {
        return groups[key];
      });
      var groupOrder = ['pearl', 'material', 'style', 'gem', 'length', 'fitting'];
      componentGroups.sort(function (left, right) {
        return groupOrder.indexOf(left.type) - groupOrder.indexOf(right.type);
      });
      var step = 0;
      componentGroups.forEach(function (group) {
        if (group.primary) group.step = ++step;
      });
      var restoredOptionIds = restoredConfiguration && restoredConfiguration.optionIds
        ? restoredConfiguration.optionIds.map(function (id) { return Number(id); })
        : [];
      var selectedOptionIds = [];
      componentGroups.forEach(function (group) {
        group.options.forEach(function (option) {
          option.selected = restoredOptionIds.indexOf(Number(option.id)) >= 0;
          if (option.selected) selectedOptionIds.push(Number(option.id));
        });
        if (group.required && !group.options.some(function (option) { return option.selected; })
            && group.options.length > 0) {
          group.options[0].selected = true;
          selectedOptionIds.push(Number(group.options[0].id));
        }
        group.hasSelection = group.options.some(function (option) { return option.selected; });
      });
      that.setData({
        componentGroups: componentGroups,
        selectedOptionIds: selectedOptionIds,
        selectedOptionNames: that.getSelectedOptionNames(componentGroups, selectedOptionIds),
        engraving: restoredConfiguration && restoredConfiguration.engraving
          ? restoredConfiguration.engraving : '',
        primaryProductId: res.data.primaryProductId,
        productConfig: res.data.product,
        loadingConfig: false,
        configError: ''
      });
      that.refreshPreview();
      that.refreshQuote();
    }).catch(function (error) {
      that.setData({
        loadingConfig: false,
        configError: error && error.message ? error.message : '定制配置加载失败，请稍后重试'
      });
    });
  },

  /** 加载本人设计快照并恢复选项 / Loads an owned snapshot and restores its choices. */
  loadDesignSnapshot: function (snapshotId) {
    var that = this;
    that.setData({ loadingConfig: true, configError: '' });
    util.request(api.CustomizationDetail, { snapshotId: snapshotId }).then(function (res) {
      if (res.code !== 0) {
        that.setData({ loadingConfig: false, configError: res.msg || '设计加载失败' });
        return;
      }
      if (!res.data || res.data.status !== 'DRAFT') {
        that.setData({ loadingConfig: false, configError: '该设计已锁定或下单，无法继续编辑' });
        return;
      }
      try {
        var configuration = JSON.parse(res.data.configurationJson || '{}');
        var goodsId = Number(res.data.goodsId || configuration.goodsId || 0);
        if (!goodsId) throw new Error('设计缺少商品信息');
        if (goodsId === 1999106) throw new Error('旧版耳饰设计仅供查看，当前定制页只支持项链');
        that.setData({ goodsId: goodsId });
        that.loadConfig(configuration);
      } catch (error) {
        that.setData({
          loadingConfig: false,
          configError: error && error.message ? error.message : '设计配置解析失败'
        });
      }
    }).catch(function (error) {
      that.setData({
        loadingConfig: false,
        configError: error && error.message ? error.message : '设计加载失败，请稍后重试'
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

  /** 刷新分层效果预览 / Refreshes the layered preview. */
  refreshPreview: function () {
    this.setData({ previewLayers: this.resolvePreviewLayers() });
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

  /** 根据选择解析透明预览图层 / Resolves transparent layers from selected options. */
  resolvePreviewLayers: function () {
    var selected = this.getSelectedOptionsByType();
    var codes = Object.keys(selected).map(function (type) {
      return selected[type].optionCode || '';
    }).join(' ');
    var materialCode = selected.material ? selected.material.optionCode : '';
    var chainUrl = LAYER_BASE_PATH + 'chain-white-gold.png';
    if (materialCode.indexOf('pt950') >= 0) chainUrl = LAYER_BASE_PATH + 'chain-platinum.png';
    else if (materialCode.indexOf('rose') >= 0) chainUrl = LAYER_BASE_PATH + 'chain-rose-gold.png';
    else if (materialCode.indexOf('yellow') >= 0 || materialCode === 'material-18k') {
      chainUrl = LAYER_BASE_PATH + 'chain-yellow-gold.png';
    }
    var pearlUrl = LAYER_BASE_PATH + 'pearl-akoya-white.png';
    if (codes.indexOf('tahiti') >= 0) pearlUrl = LAYER_BASE_PATH + 'pearl-tahiti-black.png';
    else if (codes.indexOf('southsea-gold') >= 0) {
      pearlUrl = LAYER_BASE_PATH + 'pearl-southsea-gold.png';
    } else if (codes.indexOf('fresh-purple') >= 0 || codes.indexOf('edison') >= 0) {
      pearlUrl = LAYER_BASE_PATH + 'pearl-fresh-purple.png';
    } else if (codes.indexOf('baroque') >= 0) {
      pearlUrl = LAYER_BASE_PATH + 'pearl-baroque-white.png';
    }
    var pearlCode = selected.pearl ? selected.pearl.optionCode || '' : '';
    var pearlSize = /-(6|7)(?:$|-)/.test(pearlCode) ? 'small' : 'medium';
    if (/-(10|11)(?:$|-)/.test(pearlCode)) pearlSize = 'large';
    var gemUrl = '';
    if (selected.gem && selected.gem.optionCode !== 'gem-none') {
      var gemText = (selected.gem.optionCode + ' ' + selected.gem.name).toLowerCase();
      gemUrl = gemText.indexOf('sapphire') >= 0 || gemText.indexOf('蓝宝石') >= 0
        ? LAYER_BASE_PATH + 'gem-sapphire.png'
        : LAYER_BASE_PATH + 'gem-diamond.png';
    }
    return {
      chainUrl: chainUrl,
      pearlUrl: pearlUrl,
      pearlSize: pearlSize,
      gemUrl: gemUrl,
      doublePearl: codes.indexOf('double') >= 0,
      description: this.getSelectedOptionNames(this.data.componentGroups, this.data.selectedOptionIds)
        .map(function (name) { return name.replace(/^演示\s*[·・]\s*/, ''); })
        .join('、')
    };
  },

  /** 展开或收起可选搭配 / Toggles optional customization groups. */
  toggleOptional: function () {
    this.setData({ showOptional: !this.data.showOptional });
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
      return util.request(api.BuyAdd, {
        goodsId: that.data.goodsId,
        number: 1,
        productId: that.data.primaryProductId,
        designSnapshotId: res.data.snapshotId
      }, 'POST', 'application/json').then(function (buyRes) {
        if (buyRes.code !== 0) throw new Error(buyRes.msg || '创建购买信息失败');
        wx.navigateTo({
          url: '/pages/shopping/checkout/checkout?isBuy=true&designSnapshotId=' + res.data.snapshotId
        });
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
    var goodsId = Number(options && options.goodsId || 0);
    var snapshotId = Number(options && options.snapshotId || 0);
    // tabBar 入口不带参数, 优先取商品页传入的 ID, 再回退默认演示商品 /
    // Tab entry carries no query; prefer the id stashed by the goods page, then the default.
    if (!goodsId) goodsId = Number(wx.getStorageSync('designerGoodsId') || 0);
    if (!snapshotId) snapshotId = Number(wx.getStorageSync('designerSnapshotId') || 0);
    if (!goodsId) goodsId = DEFAULT_GOODS_ID;
    if (goodsId === 1999106) goodsId = DEFAULT_GOODS_ID;
    wx.removeStorageSync('designerGoodsId');
    wx.removeStorageSync('designerSnapshotId');
    this.setData({ goodsId: goodsId });
    if (snapshotId) {
      this.loadDesignSnapshot(snapshotId);
    } else {
      this.loadConfig();
    }
  },

  /** 每次 tab 展示时重载配置, 保证从商品页带入新 ID / Reloads config on each tab show. */
  onShow: function () {
    var goodsId = Number(wx.getStorageSync('designerGoodsId') || 0);
    var snapshotId = Number(wx.getStorageSync('designerSnapshotId') || 0);
    if (snapshotId) {
      wx.removeStorageSync('designerGoodsId');
      wx.removeStorageSync('designerSnapshotId');
      this.loadDesignSnapshot(snapshotId);
      return;
    }
    if (goodsId && goodsId !== this.data.goodsId) {
      wx.removeStorageSync('designerGoodsId');
      if (goodsId === 1999106) goodsId = DEFAULT_GOODS_ID;
      this.setData({
        goodsId: goodsId,
        selectedOptionIds: [],
        engraving: '',
        quote: {}
      });
      this.loadConfig();
    }
  }
});
