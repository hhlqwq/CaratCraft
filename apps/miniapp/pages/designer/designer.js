const util = require('../../utils/util.js');
const api = require('../../config/api.js');
const arrangement = require('../../services/necklace-arrangement.js');

const DEFAULT_GOODS_ID = 1999105;

/**
 * 项链 DIY 页面 / Necklace DIY page.
 */
/** 为账号切换创建全新的编辑状态, 不迁移其他账号的私人输入. */
function initialDesignerState() {
    return {
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
        neckCircumferenceMm: '',
        targetLengthMm: '',
        quote: {},
        quoting: false,
        quoteError: '',
        primaryProductId: 0,
        saving: false,
        productConfig: {},
        loadingConfig: false,
        configError: '',
        showOptional: false,
        referencePreferences: ['较短', '中等', '较长'],
        referencePreferenceIndex: 1,
        referenceLoading: false,
        referenceError: '',
        referenceResult: null,
    };
}

Page({
    data: initialDesignerState(),

    /** 偏好仅映射固定比较组, 不将用户文本拼成提示词或公式. */
    changeReferencePreference(event) {
        const index = Number(event.detail.value);
        if (!Number.isInteger(index) || index < 0 || index > 2) {
            return;
        }
        this.invalidateReference();
        this.setData({referencePreferenceIndex: index});
    },

    /** 参数、商品或账号变化时废弃参考, 保留用户正在编辑的设计. */
    invalidateReference() {
        this._referenceSeq = (this._referenceSeq || 0) + 1;
        this.setData({referenceResult: null, referenceError: '', referenceLoading: false});
    },

    /** 请求第一方规则参考, 不发送身高体重, 不自动采用长度或材料. */
    requestReference() {
        if (
            this.data.referenceLoading ||
            this.data.loadingConfig ||
            this.data.saving ||
            this.data.configError
        ) {
            return Promise.resolve();
        }
        const text = String(this.data.neckCircumferenceMm).trim();
        const neck = text === '' ? null : Number(text);
        if (
            neck !== null &&
            (!/^\d+(\.\d)?$/.test(text) || !Number.isFinite(neck) || neck < 200 || neck > 800)
        ) {
            this.setData({referenceError: '实测颈围须为200到800mm, 最多1位小数; 未知请留空'});
            return Promise.resolve();
        }
        const seq = (this._referenceSeq = (this._referenceSeq || 0) + 1);
        const goodsId = this.data.goodsId;
        const token = wx.getStorageSync('token') || '';
        const current = () =>
            seq === this._referenceSeq &&
            goodsId === this.data.goodsId &&
            !this._unloaded &&
            token === (wx.getStorageSync('token') || '');
        this.setData({referenceLoading: true, referenceError: '', referenceResult: null});
        return util
            .request(
                api.CustomizationReference,
                {
                    goodsId,
                    neckCircumferenceMm: neck,
                    preference: ['SHORT', 'MEDIUM', 'LONG'][this.data.referencePreferenceIndex],
                },
                'POST',
                'application/json'
            )
            .then((res) => {
                if (!current()) {
                    return;
                }
                if (
                    res.code !== 0 ||
                    !res.data ||
                    res.data.goodsId !== goodsId ||
                    !Array.isArray(res.data.lengthsMm)
                ) {
                    throw new Error(res.msg || '参考结果不可用, 请重试');
                }
                this.setData({
                    referenceLoading: false,
                    referenceResult: {
                        ...res.data,
                        lengthsLabel: res.data.lengthsMm
                            .map((length) => Number(length) / 10)
                            .join(' / '),
                        materialNames: (res.data.materials || [])
                            .slice(0, 12)
                            .map((material) => material.name)
                            .join('、'),
                        materialCount: (res.data.materials || []).length,
                    },
                });
            })
            .catch((error) => {
                if (current()) {
                    this.setData({
                        referenceLoading: false,
                        referenceError: error.message || '参考暂不可用, 可继续手动设计',
                    });
                }
            });
    },

    /** 加载定制商品配置 / Loads custom product configuration. */
    loadConfig(restoredConfiguration) {
        this._snapshotSeq = (this._snapshotSeq || 0) + 1;
        this.invalidateReference();

        const configSeq = (this._configSeq = (this._configSeq || 0) + 1);
        const requestedGoodsId = this.data.goodsId;
        this._quoteSeq = (this._quoteSeq || 0) + 1;
        this.setData({
            loadingConfig: true,
            configError: '',
            componentGroups: [],
            quote: {},
            primaryProductId: 0,
        });
        return util
            .request(api.CustomizationConfig, {goodsId: requestedGoodsId}, 'GET')
            .then((res) => {
                if (configSeq !== this._configSeq || requestedGoodsId !== this.data.goodsId) {
                    return;
                }
                if (res.code !== 0) {
                    this.setData({
                        loadingConfig: false,
                        configError: res.msg || '该商品暂不可定制',
                    });
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
                    this.setData({loadingConfig: false, configError: '商家尚未配置可选组件'});
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
                        ? (
                              restoredConfiguration.arrangementOptionIds ||
                              restoredConfiguration.optionIds
                          ).map(function (id) {
                              return Number(id);
                          })
                        : [];
                if (
                    restoredOptionIds.some(function (id) {
                        return !options.some(function (option) {
                            return Number(option.id) === id;
                        });
                    })
                ) {
                    this.setData({
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
                this.setData({
                    componentGroups: componentGroups,
                    selectedOptionIds: selectedOptionIds,
                    selectedOptionNames: this.getSelectedOptionNames(
                        componentGroups,
                        selectedOptionIds
                    ),
                    engraving:
                        restoredConfiguration && restoredConfiguration.engraving
                            ? restoredConfiguration.engraving
                            : '',
                    neckCircumferenceMm:
                        restoredConfiguration && restoredConfiguration.neckCircumferenceMm != null
                            ? String(restoredConfiguration.neckCircumferenceMm)
                            : '',
                    targetLengthMm:
                        restoredConfiguration && restoredConfiguration.targetLengthMm != null
                            ? String(restoredConfiguration.targetLengthMm)
                            : '',
                    primaryProductId: res.data.primaryProductId,
                    productConfig: res.data.product,
                    loadingConfig: false,
                    configError: '',
                    activeIndex: -1,
                });
                this._history = [];
                this.updateArrangement(
                    restoredConfiguration ? restoredOptionIds : selectedOptionIds,
                    false
                );
            })
            .catch((error) => {
                if (configSeq !== this._configSeq || requestedGoodsId !== this.data.goodsId) {
                    return;
                }
                this.setData({
                    loadingConfig: false,
                    configError:
                        error && error.message ? error.message : '定制配置加载失败，请稍后重试',
                });
            });
    },

    /** 加载本人设计快照并恢复选项 / Loads an owned snapshot and restores its choices. */
    loadDesignSnapshot(snapshotId) {
        const seq = (this._snapshotSeq = (this._snapshotSeq || 0) + 1);
        this._configSeq = (this._configSeq || 0) + 1;
        this._quoteSeq = (this._quoteSeq || 0) + 1;
        this.setData({
            loadingConfig: true,
            configError: '',
            quote: {},
            draftId: snapshotId,
            draftVersion: null,
        });
        return util
            .request(api.CustomizationDetail, {snapshotId: snapshotId})
            .then((res) => {
                if (seq !== this._snapshotSeq || this._unloaded) {
                    return;
                }
                if (res.code !== 0) {
                    this.setData({loadingConfig: false, configError: res.msg || '设计加载失败'});
                    return;
                }
                if (!res.data || res.data.status !== 'DRAFT') {
                    this.setData({
                        loadingConfig: false,
                        configError: '该设计已锁定或下单，无法继续编辑',
                    });
                    return;
                }
                try {
                    const configuration = JSON.parse(res.data.configurationJson || '{}');
                    const goodsId = Number(res.data.goodsId || configuration.goodsId || 0);
                    if (!goodsId) {
                        throw new Error('设计缺少商品信息');
                    }
                    this.setData({
                        goodsId: goodsId,
                        draftId: res.data.id,
                        draftVersion: res.data.version,
                    });
                    this.loadConfig(configuration);
                } catch (error) {
                    this.setData({
                        loadingConfig: false,
                        configError: error && error.message ? error.message : '设计配置解析失败',
                    });
                }
            })
            .catch((error) => {
                if (seq !== this._snapshotSeq || this._unloaded) {
                    return;
                }
                this.setData({
                    loadingConfig: false,
                    configError:
                        error && error.message ? error.message : '设计加载失败，请稍后重试',
                });
            });
    },

    /** 重新加载定制配置 / Reloads customizer configuration. */
    retryLoadConfig() {
        if (this.data.draftId) {
            this.loadDesignSnapshot(this.data.draftId);
        } else {
            this.loadConfig();
        }
    },

    /** 点击材料库时在当前实例之后插入一项, 同类材料可以多次添加. */
    selectOption(event) {
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
                activeIndex: Math.min(
                    this.data.arrangementOptionIds.length - 1,
                    this.data.activeIndex + 1
                ),
            });
        } catch (error) {
            wx.showToast({title: error.message, icon: 'none'});
        }
    },

    /** 选中实例以插入、复制、删除或调序, 重复材料保持独立位置. */
    selectInstance(event) {
        this.setData({activeIndex: Number(event.currentTarget.dataset.index)});
    },

    /** 批量复制数量仅影响当前选中材料, 超限时整次操作不执行. */
    changeCopyCount(event) {
        this.setData({copyCount: event.detail.value});
    },

    /** 统一记录排列历史, 每次变更立即废弃旧报价. */
    updateArrangement(ids, remember) {
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
    undoArrangement() {
        if (!this._history || !this._history.length || this.data.saving) {
            return;
        }
        this.updateArrangement(this._history.pop(), false);
    },

    /** 按实例索引操作, 清空经确认后仍可撤回. */
    editInstance(event) {
        if (this.data.saving) {
            return;
        }
        const action = event.currentTarget.dataset.action;

        if (action === 'clear') {
            wx.showModal({
                title: '清空排列',
                content: '清空当前材料, 可通过撤回恢复。',
                success: (res) => {
                    if (res.confirm) {
                        this.updateArrangement([], true);
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
    clearOptional(event) {
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
    getSelectedOptionNames(groups, selectedOptionIds) {
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
    toggleOptional() {
        this.setData({showOptional: !this.data.showOptional});
    },

    /** 更新刻字并刷新报价（防抖）/ Updates engraving and refreshes quote with debounce. */
    changeEngraving(event) {
        if (this.data.saving) {
            return;
        }

        this.setData({engraving: event.detail.value, quote: {}, quoting: true, quoteError: ''});
        this._quoteSeq = (this._quoteSeq || 0) + 1;
        if (this._engravingTimer) {
            clearTimeout(this._engravingTimer);
        }
        this._engravingTimer = setTimeout(() => {
            this.refreshQuote();
        }, 300);
    },

    /** 实测输入仅由用户填写, 每次改动废弃旧参考及报价. */
    changeSizing(event) {
        if (this.data.saving) {
            return;
        }
        const field = event.currentTarget.dataset.field;
        if (!['neckCircumferenceMm', 'targetLengthMm'].includes(field)) {
            return;
        }
        this.invalidateReference();
        this.setData({[field]: event.detail.value});
        this.refreshQuote();
    },

    /** 请求服务端报价 / Requests server-side quote. */
    refreshQuote() {
        // 递增序号丢弃过期响应, 避免快速切换选项时旧报价覆盖新报价 /
        // Bump a sequence to drop stale responses racing with newer selections.
        const seq = (this._quoteSeq = (this._quoteSeq || 0) + 1);
        this.setData({quote: {}, quoting: true, quoteError: ''});
        return util
            .request(
                api.CustomizationQuote,
                {
                    goodsId: this.data.goodsId,
                    arrangementOptionIds: this.data.arrangementOptionIds,
                    engraving: this.data.engraving,
                    neckCircumferenceMm:
                        this.data.neckCircumferenceMm === ''
                            ? null
                            : Number(this.data.neckCircumferenceMm),
                    targetLengthMm:
                        this.data.targetLengthMm === '' ? null : Number(this.data.targetLengthMm),
                },
                'POST',
                'application/json'
            )
            .then((res) => {
                if (seq !== this._quoteSeq) {
                    return;
                }
                if (res.code !== 0) {
                    throw new Error(res.msg || '报价失败');
                }
                this.setData({quote: res.data, quoting: false, quoteError: ''});
            })
            .catch((error) => {
                if (seq !== this._quoteSeq) {
                    return;
                }
                this.setData({
                    quote: {},
                    quoting: false,
                    quoteError: error.message || '报价失败, 请重新选择',
                });
            });
    },

    /** 保存设计草稿 / Saves a design draft. */
    saveDraft() {
        this.persistDesign(false).catch(function () {});
    },

    /** 保存设计，可选进入结算 / Saves a design and optionally starts checkout. */
    persistDesign(startCheckout) {
        if (this.data.saving) {
            return Promise.reject(new Error('正在保存'));
        }
        if (this.data.loadingConfig || this.data.configError) {
            return Promise.reject(new Error('请先加载有效的材料配置'));
        }
        if (this.data.quoting || this.data.quoteError || !this.data.quote.totalPrice) {
            return Promise.reject(new Error('请等待有效报价后再保存或结算'));
        }
        if (startCheckout && this.data.quote.checkoutReady !== true) {
            wx.showToast({title: '请先完成成品长度校验', icon: 'none'});
            return Promise.reject(new Error('成品长度尚未校验通过'));
        }
        const token = wx.getStorageSync('token') || '';
        const current = () => !this._unloaded && token === (wx.getStorageSync('token') || '');
        this.setData({saving: true});
        return util
            .request(
                api.CustomizationSave,
                {
                    goodsId: this.data.goodsId,
                    draftId: this.data.draftId,
                    version: this.data.draftVersion,
                    arrangementOptionIds: this.data.arrangementOptionIds,
                    engraving: this.data.engraving,
                    neckCircumferenceMm:
                        this.data.neckCircumferenceMm === ''
                            ? null
                            : Number(this.data.neckCircumferenceMm),
                    targetLengthMm:
                        this.data.targetLengthMm === '' ? null : Number(this.data.targetLengthMm),
                },
                'POST',
                'application/json'
            )
            .then((res) => {
                if (!current()) {
                    return;
                }
                this.setData({saving: false});
                if (res.code !== 0) {
                    throw new Error(res.msg || '保存失败');
                }
                this.setData({draftId: res.data.snapshotId, draftVersion: res.data.version});
                if (!startCheckout) {
                    wx.setStorageSync('lastSavedDesignSnapshotId', res.data.snapshotId);
                    wx.showToast({title: '设计已保存', icon: 'success'});
                    return res.data.snapshotId;
                }
                return util
                    .request(
                        api.BuyAdd,
                        {
                            goodsId: this.data.goodsId,
                            number: 1,
                            productId: this.data.primaryProductId,
                            designSnapshotId: res.data.snapshotId,
                        },
                        'POST',
                        'application/json'
                    )
                    .then(function (buyRes) {
                        if (!current()) {
                            return;
                        }
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
            .catch((error) => {
                if (!current()) {
                    return;
                }
                this.setData({saving: false});
                wx.showToast({title: error.message || '操作失败', icon: 'none'});
                throw error;
            });
    },

    /** 确认设计并进入结算 / Confirms a design and starts checkout. */
    submitDesign() {
        if (!this.data.primaryProductId) {
            wx.showToast({title: '商品规格未配置', icon: 'none'});
            return;
        }
        this.persistDesign(true).catch(function () {});
    },

    /** 初始化页面 / Initializes page state. */
    onLoad(options) {
        this._unloaded = false;
        this._sessionToken = wx.getStorageSync('token') || '';
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
    onShow() {
        const token = wx.getStorageSync('token') || '';
        if (this._sessionToken !== undefined && token !== this._sessionToken) {
            this.invalidateReference();
            this._configSeq = (this._configSeq || 0) + 1;
            this._snapshotSeq = (this._snapshotSeq || 0) + 1;
            this._quoteSeq = (this._quoteSeq || 0) + 1;
            this._history = [];
            if (this._engravingTimer) {
                clearTimeout(this._engravingTimer);
            }
            this.setData({...initialDesignerState(), goodsId: DEFAULT_GOODS_ID});
            this.loadConfig();
        }
        this._sessionToken = token;
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
    onUnload() {
        this._unloaded = true;
        this.invalidateReference();
        this._configSeq = (this._configSeq || 0) + 1;
        this._snapshotSeq = (this._snapshotSeq || 0) + 1;
        this._quoteSeq = (this._quoteSeq || 0) + 1;
        if (this._engravingTimer) {
            clearTimeout(this._engravingTimer);
        }
    },
});
