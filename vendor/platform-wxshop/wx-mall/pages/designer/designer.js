const util = require('../../utils/util.js');
const api = require('../../config/api.js');
const aiRecommendation = require('../../services/ai-recommendation.js');
const arrangement = require('../../services/necklace-arrangement.js');

const DEFAULT_GOODS_ID = 1999105;

/**
 * 项链 DIY 页面 / Necklace DIY page.
 */
Page({
  data: {
    goodsId: 0,
    draftId: null,
    draftVersion: null,
    componentGroups: [],
    selectedOptionIds: [],
    selectedOptionNames: [],
    arrangementOptionIds: [],
    arrangementNodes: [],
    activeIndex: -1,
    copyCount: 1,
    canUndo: false,
    dimensionNotice: '',
    engraving: '',
    quote: {},
    quoting: false,
    quoteError: '',
    primaryProductId: 0,
    saving: false,
    productConfig: {},
    loadingConfig: false,
    configError: '',
    showOptional: false,
    aiStatus: {},
    aiStatusError: '',
    aiHeight: '',
    aiWeight: '',
    aiNeck: '',
    aiPreferences: ['贴颈', '锁骨位置', '稍长'],
    aiPreferenceIndex: 1,
    aiConsent: false,
    aiLoading: false,
    aiError: '',
    aiResult: null,
  },

  /** 获取功能开关与数据用途, 配置缺失不影响手动设计. */
  loadAiStatus: function () {
    const that = this;
    return util
      .request(api.AiRecommendationStatus, {}, 'GET')
      .then(function (res) {
        if (that._unloaded) {
          return;
        }
        if (res.code !== 0) {
          throw new Error('AI功能状态加载失败');
        }
        const unavailable = aiRecommendation.availability();
        that.setData({
          aiStatus: Object.assign({}, res.data, {
            enabled: !unavailable,
            unavailableReason: unavailable,
          }),
          aiStatusError: '',
        });
      })
      .catch(function () {
        if (!that._unloaded) {
          that.setData({aiStatusError: 'AI功能状态加载失败, 可重试或手动设计'});
        }
      });
  },

  /** 更新身体参数时立即废弃旧建议, 不将身体参数写入本地存储. */
  changeAiInput: function (event) {
    const field = event.currentTarget.dataset.field;
    if (['aiHeight', 'aiWeight', 'aiNeck'].indexOf(field) < 0) {
      return;
    }
    this.invalidateAi();
    const values = {};
    values[field] = event.detail.value;
    this.setData(values);
  },

  /** 使用固定偏好选项, 不向模型发送任意提示词. */
  changeAiPreference: function (event) {
    this.invalidateAi();
    this.setData({aiPreferenceIndex: Number(event.detail.value)});
  },

  /** 同意状态变更时废弃在途结果, 不默认勾选数据提交同意. */
  changeAiConsent: function (event) {
    this.invalidateAi();
    this.setData({aiConsent: event.detail.value.indexOf('agree') >= 0});
  },

  /** 递增请求序号, 阻止参数修改或切换商品后迟到的建议覆盖页面. */
  invalidateAi: function () {
    this._aiSeq = (this._aiSeq || 0) + 1;
    this.setData({aiResult: null, aiError: '', aiLoading: false});
  },

  /** 用户主动同意后请求AI建议, 失败保留手动设计能力. */
  requestAiRecommendation: function () {
    if (this.data.aiLoading) {
      return Promise.resolve();
    }
    if (!this.data.aiConsent) {
      this.setData({aiError: '请先阅读并勾选AI推荐的数据使用说明'});
      return Promise.resolve();
    }
    const height = Number(this.data.aiHeight);
    const weight = Number(this.data.aiWeight);
    const neck = this.data.aiNeck === '' ? null : Number(this.data.aiNeck);
    if (
      !isFinite(height) ||
      height < 80 ||
      height > 250 ||
      !isFinite(weight) ||
      weight < 20 ||
      weight > 300 ||
      (neck !== null && (!isFinite(neck) || neck < 20 || neck > 80))
    ) {
      this.setData({aiError: '请填写有效的身高、体重和选填颈围'});
      return Promise.resolve();
    }
    const that = this;
    const seq = (this._aiSeq = (this._aiSeq || 0) + 1);
    const goodsId = this.data.goodsId;
    this.setData({aiLoading: true, aiError: '', aiResult: null});
    return aiRecommendation
      .recommend(
        {
          goodsId: goodsId,
          heightCm: height,
          weightKg: weight,
          neckCm: neck,
          preference: this.data.aiPreferences[this.data.aiPreferenceIndex],
          consent: true,
        },
        function () {
          return seq === that._aiSeq && goodsId === that.data.goodsId && !that._unloaded;
        }
      )
      .then(function (res) {
        if (seq !== that._aiSeq || goodsId !== that.data.goodsId || that._unloaded) {
          return;
        }
        if (res.code !== 0) {
          throw new Error(res.msg || 'AI推荐失败');
        }
        that.setData({aiResult: res.data, aiLoading: false});
      })
      .catch(function (error) {
        if (seq !== that._aiSeq || that._unloaded) {
          return;
        }
        that.setData({aiLoading: false, aiError: error.message || 'AI暂不可用, 可继续手动设计'});
      });
  },

  /** 采用仍属于当前商品的材料建议, 成品长度由商家选项确认, 重新获取可信报价. */
  applyAiRecommendation: function () {
    const result = this.data.aiResult;
    if (
      !result ||
      result.goodsId !== this.data.goodsId ||
      this.data.loadingConfig ||
      this.data.saving
    ) {
      return;
    }
    const selected = result.optionIds.map(Number);
    const allIds = [];
    const valid = this.data.componentGroups.every(function (group) {
      let count = 0;
      group.options.forEach(function (option) {
        allIds.push(Number(option.id));
        if (selected.indexOf(Number(option.id)) >= 0) {
          count++;
        }
      });
      return count <= 1 && (!group.required || count === 1);
    });
    if (
      !valid ||
      selected.some(function (id) {
        return allIds.indexOf(id) < 0;
      })
    ) {
      this.setData({aiError: '材料配置已变化, 请重新推荐', aiResult: null});
      return;
    }
    const that = this;
    return new Promise(function (resolve) {
      wx.showModal({
        title: '采用材料建议',
        content: '将用推荐材料替换当前排列, 之后可撤回。AI建议不会自动确定每种材料的颗数。',
        success: function (res) {
          resolve(res.confirm ? that.updateArrangement(selected, true) : undefined);
        },
        fail: function () {
          resolve();
        },
      });
    });
  },

  /** 加载定制商品配置 / Loads custom product configuration. */
  loadConfig: function (restoredConfiguration) {
    this._snapshotSeq = (this._snapshotSeq || 0) + 1;
    this.invalidateAi();
    const that = this;
    const configSeq = (this._configSeq = (this._configSeq || 0) + 1);
    const requestedGoodsId = that.data.goodsId;
    this._quoteSeq = (this._quoteSeq || 0) + 1;
    that.setData({
      loadingConfig: true,
      configError: '',
      componentGroups: [],
      quote: {},
      primaryProductId: 0,
    });
    return util
      .request(api.CustomizationConfig, {goodsId: requestedGoodsId}, 'GET')
      .then(function (res) {
        if (configSeq !== that._configSeq || requestedGoodsId !== that.data.goodsId) {
          return;
        }
        if (res.code !== 0) {
          that.setData({loadingConfig: false, configError: res.msg || '该商品暂不可定制'});
          return;
        }
        const groups = {};
        const groupNames = {
          pearl: '珍珠种类与规格',
          material: '贵金属材质',
          gem: '钻石与宝石',
          fitting: '配饰',
          accessory: '配饰',
          clasp: '尾扣',
          style: '成品款式',
          chain: '链条材质',
          length: '项链长度',
          pendant: '吊坠款式',
          engraving: '刻字工艺',
        };
        const primaryTypes = String((res.data.product || {}).requiredComponentTypes || '')
          .split(',')
          .map(function (type) {
            return type.trim();
          })
          .filter(Boolean);
        const options = res.data && res.data.options ? res.data.options : [];
        if (!options.length) {
          that.setData({loadingConfig: false, configError: '商家尚未配置可选组件'});
          return;
        }
        options.forEach(function (option) {
          option.displayName = (option.name || '').replace(/^演示\s*[·・]\s*/, '');
          if (!groups[option.componentType]) {
            groups[option.componentType] = {
              type: option.componentType,
              name: groupNames[option.componentType] || option.componentType,
              description: '点击材料, 插入当前选中位置之后; 可重复添加。',
              required: primaryTypes.indexOf(option.componentType) >= 0,
              primary: primaryTypes.indexOf(option.componentType) >= 0,
              options: [],
            };
          }
          groups[option.componentType].options.push(option);
        });
        const componentGroups = Object.keys(groups).map(function (key, index) {
          return groups[key];
        });
        const groupOrder = [
          'pearl',
          'clasp',
          'accessory',
          'fitting',
          'chain',
          'material',
          'style',
          'gem',
          'length',
        ];
        componentGroups.sort(function (left, right) {
          return groupOrder.indexOf(left.type) - groupOrder.indexOf(right.type);
        });
        let step = 0;
        componentGroups.forEach(function (group) {
          if (group.primary) {
            group.step = ++step;
          }
        });
        const restoredOptionIds =
          restoredConfiguration &&
          (restoredConfiguration.arrangementOptionIds || restoredConfiguration.optionIds)
            ? (restoredConfiguration.arrangementOptionIds || restoredConfiguration.optionIds).map(
                function (id) {
                  return Number(id);
                }
              )
            : [];
        if (
          restoredOptionIds.some(function (id) {
            return !options.some(function (option) {
              return Number(option.id) === id;
            });
          })
        ) {
          that.setData({
            loadingConfig: false,
            configError: '原草稿包含已停用材料, 已保留原设计, 请联系商家核对或新建设计',
          });
          return;
        }
        const selectedOptionIds = [];
        componentGroups.forEach(function (group) {
          group.options.forEach(function (option) {
            option.selected = restoredOptionIds.indexOf(Number(option.id)) >= 0;
            if (option.selected) {
              selectedOptionIds.push(Number(option.id));
            }
          });
          if (
            !restoredConfiguration &&
            group.required &&
            !group.options.some(function (option) {
              return option.selected;
            }) &&
            group.options.length > 0
          ) {
            group.options[0].selected = true;
            selectedOptionIds.push(Number(group.options[0].id));
          }
          group.hasSelection = group.options.some(function (option) {
            return option.selected;
          });
        });
        that.setData({
          componentGroups: componentGroups,
          selectedOptionIds: selectedOptionIds,
          selectedOptionNames: that.getSelectedOptionNames(componentGroups, selectedOptionIds),
          engraving:
            restoredConfiguration && restoredConfiguration.engraving
              ? restoredConfiguration.engraving
              : '',
          primaryProductId: res.data.primaryProductId,
          productConfig: res.data.product,
          loadingConfig: false,
          configError: '',
          activeIndex: -1,
        });
        that._history = [];
        that.updateArrangement(
          restoredConfiguration ? restoredOptionIds : selectedOptionIds,
          false
        );
      })
      .catch(function (error) {
        if (configSeq !== that._configSeq || requestedGoodsId !== that.data.goodsId) {
          return;
        }
        that.setData({
          loadingConfig: false,
          configError: error && error.message ? error.message : '定制配置加载失败，请稍后重试',
        });
      });
  },

  /** 加载本人设计快照并恢复选项 / Loads an owned snapshot and restores its choices. */
  loadDesignSnapshot: function (snapshotId) {
    const that = this;
    const seq = (this._snapshotSeq = (this._snapshotSeq || 0) + 1);
    this._configSeq = (this._configSeq || 0) + 1;
    this._quoteSeq = (this._quoteSeq || 0) + 1;
    that.setData({
      loadingConfig: true,
      configError: '',
      quote: {},
      draftId: snapshotId,
      draftVersion: null,
    });
    return util
      .request(api.CustomizationDetail, {snapshotId: snapshotId})
      .then(function (res) {
        if (seq !== that._snapshotSeq || that._unloaded) {
          return;
        }
        if (res.code !== 0) {
          that.setData({loadingConfig: false, configError: res.msg || '设计加载失败'});
          return;
        }
        if (!res.data || res.data.status !== 'DRAFT') {
          that.setData({loadingConfig: false, configError: '该设计已锁定或下单，无法继续编辑'});
          return;
        }
        try {
          const configuration = JSON.parse(res.data.configurationJson || '{}');
          const goodsId = Number(res.data.goodsId || configuration.goodsId || 0);
          if (!goodsId) {
            throw new Error('设计缺少商品信息');
          }
          that.setData({goodsId: goodsId, draftId: res.data.id, draftVersion: res.data.version});
          that.loadConfig(configuration);
        } catch (error) {
          that.setData({
            loadingConfig: false,
            configError: error && error.message ? error.message : '设计配置解析失败',
          });
        }
      })
      .catch(function (error) {
        if (seq !== that._snapshotSeq || that._unloaded) {
          return;
        }
        that.setData({
          loadingConfig: false,
          configError: error && error.message ? error.message : '设计加载失败，请稍后重试',
        });
      });
  },

  /** 重新加载定制配置 / Reloads customizer configuration. */
  retryLoadConfig: function () {
    if (this.data.draftId) {
      this.loadDesignSnapshot(this.data.draftId);
    } else {
      this.loadConfig();
    }
  },

  /** 点击材料库时在当前实例之后插入一项, 同类材料可以多次添加. */
  selectOption: function (event) {
    if (this.data.loadingConfig || this.data.saving) {
      return;
    }
    try {
      this.updateArrangement(
        arrangement.edit(
          this.data.arrangementOptionIds,
          'insert',
          this.data.activeIndex,
          event.currentTarget.dataset.id
        ),
        true
      );
      this.setData({
        activeIndex: Math.min(this.data.arrangementOptionIds.length - 1, this.data.activeIndex + 1),
      });
    } catch (error) {
      wx.showToast({title: error.message, icon: 'none'});
    }
  },

  /** 选中实例以插入、复制、删除或调序, 重复材料保持独立位置. */
  selectInstance: function (event) {
    this.setData({activeIndex: Number(event.currentTarget.dataset.index)});
  },

  /** 批量复制数量仅影响当前选中材料, 超限时整次操作不执行. */
  changeCopyCount: function (event) {
    this.setData({copyCount: event.detail.value});
  },

  /** 统一记录排列历史, 每次变更立即废弃旧报价. */
  updateArrangement: function (ids, remember) {
    if (remember) {
      this._history = this._history || [];
      this._history.push(this.data.arrangementOptionIds.slice());
      if (this._history.length > 50) {
        this._history.shift();
      }
    }
    const options = [];
    const groups = this.data.componentGroups.map(function (group) {
      group.options.forEach(function (option) {
        option.selected = ids.indexOf(Number(option.id)) >= 0;
        options.push(option);
      });
      group.hasSelection = group.options.some(function (option) {
        return option.selected;
      });
      return group;
    });
    const nodes = arrangement.preview(ids, options);
    this.setData({
      arrangementOptionIds: ids.slice(),
      arrangementNodes: nodes,
      selectedOptionIds: Array.from(new Set(ids)),
      componentGroups: groups,
      canUndo: Boolean(this._history && this._history.length),
      activeIndex: Math.min(this.data.activeIndex, ids.length - 1),
      dimensionNotice: nodes.some(function (node) {
        return !node.known;
      })
        ? '部分材料缺少实测尺寸, 以示意符号展示, 无法给出完整长度。'
        : '',
      selectedOptionNames: nodes.map(function (node) {
        return node.name;
      }),
    });
    return this.refreshQuote();
  },

  /** 撤回最后一次排列操作, 不修改已下单快照. */
  undoArrangement: function () {
    if (!this._history || !this._history.length || this.data.saving) {
      return;
    }
    this.updateArrangement(this._history.pop(), false);
  },

  /** 按实例索引操作, 清空经确认后仍可撤回. */
  editInstance: function (event) {
    if (this.data.saving) {
      return;
    }
    const action = event.currentTarget.dataset.action;
    const that = this;
    if (action === 'clear') {
      wx.showModal({
        title: '清空排列',
        content: '清空当前材料, 可通过撤回恢复。',
        success: function (res) {
          if (res.confirm) {
            that.updateArrangement([], true);
          }
        },
      });
      return;
    }
    try {
      const index = this.data.activeIndex;
      if (index < 0 || index >= this.data.arrangementOptionIds.length) {
        throw new Error('请先选中一个材料实例');
      }
      const count = action === 'copy' ? Number(this.data.copyCount) : 1;
      if (!Number.isInteger(count) || count < 1 || count > 300) {
        throw new Error('复制数量须为1到300的整数');
      }
      let next = this.data.arrangementOptionIds;
      for (let i = 0; i < count; i++) {
        next = arrangement.edit(next, action, index + (action === 'copy' ? i : 0));
      }
      this.updateArrangement(next, true);
      if (action === 'left' || action === 'right') {
        this.setData({
          activeIndex: Math.max(
            0,
            Math.min(
              this.data.arrangementOptionIds.length - 1,
              index + (action === 'left' ? -1 : 1)
            )
          ),
        });
      }
      if (action === 'copy') {
        this.setData({activeIndex: index + count});
      }
    } catch (error) {
      wx.showToast({title: error.message, icon: 'none'});
    }
  },

  /** 清除可选升级项 / Clears an optional upgrade selection. */
  clearOptional: function (event) {
    if (this.data.saving) {
      return;
    }
    const type = event.currentTarget.dataset.type;
    const removed = [];
    this.data.componentGroups.forEach(function (group) {
      if (group.type === type && !group.required) {
        group.options.forEach(function (option) {
          removed.push(Number(option.id));
        });
      }
    });
    this.updateArrangement(
      this.data.arrangementOptionIds.filter(function (id) {
        return removed.indexOf(id) < 0;
      }),
      true
    );
  },

  /** 获取当前已选组件名称 / Gets the names of selected component options. */
  getSelectedOptionNames: function (groups, selectedOptionIds) {
    const names = [];
    groups.forEach(function (group) {
      group.options.forEach(function (option) {
        if (selectedOptionIds.indexOf(option.id) >= 0) {
          names.push(option.name);
        }
      });
    });
    return names;
  },

  /** 展开或收起可选搭配 / Toggles optional customization groups. */
  toggleOptional: function () {
    this.setData({showOptional: !this.data.showOptional});
  },

  /** 更新刻字并刷新报价（防抖）/ Updates engraving and refreshes quote with debounce. */
  changeEngraving: function (event) {
    if (this.data.saving) {
      return;
    }
    const that = this;
    that.setData({engraving: event.detail.value, quote: {}, quoting: true, quoteError: ''});
    this._quoteSeq = (this._quoteSeq || 0) + 1;
    if (that._engravingTimer) {
      clearTimeout(that._engravingTimer);
    }
    that._engravingTimer = setTimeout(function () {
      that.refreshQuote();
    }, 300);
  },

  /** 请求服务端报价 / Requests server-side quote. */
  refreshQuote: function () {
    const that = this;
    // 递增序号丢弃过期响应, 避免快速切换选项时旧报价覆盖新报价 /
    // Bump a sequence to drop stale responses racing with newer selections.
    const seq = (this._quoteSeq = (this._quoteSeq || 0) + 1);
    that.setData({quote: {}, quoting: true, quoteError: ''});
    return util
      .request(
        api.CustomizationQuote,
        {
          goodsId: that.data.goodsId,
          arrangementOptionIds: that.data.arrangementOptionIds,
          engraving: that.data.engraving,
        },
        'POST',
        'application/json'
      )
      .then(function (res) {
        if (seq !== that._quoteSeq) {
          return;
        }
        if (res.code !== 0) {
          throw new Error(res.msg || '报价失败');
        }
        that.setData({quote: res.data, quoting: false, quoteError: ''});
      })
      .catch(function (error) {
        if (seq !== that._quoteSeq) {
          return;
        }
        that.setData({
          quote: {},
          quoting: false,
          quoteError: error.message || '报价失败, 请重新选择',
        });
      });
  },

  /** 保存设计草稿 / Saves a design draft. */
  saveDraft: function () {
    this.persistDesign(false).catch(function () {});
  },

  /** 保存设计，可选进入结算 / Saves a design and optionally starts checkout. */
  persistDesign: function (startCheckout) {
    const that = this;
    if (that.data.saving) {
      return Promise.reject(new Error('正在保存'));
    }
    if (that.data.loadingConfig || that.data.configError) {
      return Promise.reject(new Error('请先加载有效的材料配置'));
    }
    if (that.data.quoting || that.data.quoteError || !that.data.quote.totalPrice) {
      return Promise.reject(new Error('请等待有效报价后再保存或结算'));
    }
    that.setData({saving: true});
    return util
      .request(
        api.CustomizationSave,
        {
          goodsId: that.data.goodsId,
          draftId: that.data.draftId,
          version: that.data.draftVersion,
          arrangementOptionIds: that.data.arrangementOptionIds,
          engraving: that.data.engraving,
        },
        'POST',
        'application/json'
      )
      .then(function (res) {
        that.setData({saving: false});
        if (res.code !== 0) {
          throw new Error(res.msg || '保存失败');
        }
        that.setData({draftId: res.data.snapshotId, draftVersion: res.data.version});
        if (!startCheckout) {
          wx.setStorageSync('lastSavedDesignSnapshotId', res.data.snapshotId);
          wx.showToast({title: '设计已保存', icon: 'success'});
          return res.data.snapshotId;
        }
        return util
          .request(
            api.BuyAdd,
            {
              goodsId: that.data.goodsId,
              number: 1,
              productId: that.data.primaryProductId,
              designSnapshotId: res.data.snapshotId,
            },
            'POST',
            'application/json'
          )
          .then(function (buyRes) {
            if (buyRes.code !== 0) {
              throw new Error(buyRes.msg || '创建购买信息失败');
            }
            wx.navigateTo({
              url:
                '/pages/shopping/checkout/checkout?isBuy=true&checkoutId=' +
                encodeURIComponent(buyRes.data.checkoutId) +
                '&designSnapshotId=' +
                res.data.snapshotId,
            });
            return res.data.snapshotId;
          });
      })
      .catch(function (error) {
        that.setData({saving: false});
        wx.showToast({title: error.message || '操作失败', icon: 'none'});
        throw error;
      });
  },

  /** 确认设计并进入结算 / Confirms a design and starts checkout. */
  submitDesign: function () {
    if (!this.data.primaryProductId) {
      wx.showToast({title: '商品规格未配置', icon: 'none'});
      return;
    }
    this.persistDesign(true).catch(function () {});
  },

  /** 初始化页面 / Initializes page state. */
  onLoad: function (options) {
    this._unloaded = false;
    this.loadAiStatus();
    let goodsId = Number((options && options.goodsId) || 0);
    let snapshotId = Number((options && options.snapshotId) || 0);
    // tabBar 入口不带参数, 优先取商品页传入的 ID, 再回退默认演示商品 /
    // Tab entry carries no query; prefer the id stashed by the goods page, then the default.
    if (!goodsId) {
      goodsId = Number(wx.getStorageSync('designerGoodsId') || 0);
    }
    if (!snapshotId) {
      snapshotId = Number(wx.getStorageSync('designerSnapshotId') || 0);
    }
    if (!goodsId) {
      goodsId = DEFAULT_GOODS_ID;
    }
    wx.removeStorageSync('designerGoodsId');
    wx.removeStorageSync('designerSnapshotId');
    this.setData({goodsId: goodsId});
    if (snapshotId) {
      this.loadDesignSnapshot(snapshotId);
    } else {
      this.loadConfig();
    }
  },

  /** 每次 tab 展示时重载配置, 保证从商品页带入新 ID / Reloads config on each tab show. */
  onShow: function () {
    const goodsId = Number(wx.getStorageSync('designerGoodsId') || 0);
    const snapshotId = Number(wx.getStorageSync('designerSnapshotId') || 0);
    if (snapshotId) {
      wx.removeStorageSync('designerGoodsId');
      wx.removeStorageSync('designerSnapshotId');
      this.loadDesignSnapshot(snapshotId);
      return;
    }
    if (goodsId && goodsId !== this.data.goodsId) {
      wx.removeStorageSync('designerGoodsId');
      this.setData({
        goodsId: goodsId,
        draftId: null,
        draftVersion: null,
        selectedOptionIds: [],
        engraving: '',
        quote: {},
      });
      this.loadConfig();
    }
  },

  /** 页面卸载时使未完成请求失效并清理防抖计时器. */
  onUnload: function () {
    this._unloaded = true;
    this.invalidateAi();
    this._configSeq = (this._configSeq || 0) + 1;
    this._snapshotSeq = (this._snapshotSeq || 0) + 1;
    this._quoteSeq = (this._quoteSeq || 0) + 1;
    if (this._engravingTimer) {
      clearTimeout(this._engravingTimer);
    }
  },
});
