const util = require('../../utils/util.js');
const api = require('../../config/api.js');
const arrangement = require('../../services/necklace-arrangement.js');


/** 读取商家规则, 保留缺失或不可解析状态. */
function object(text) {
    try {
        const value = JSON.parse(text || '{}');
        return value && typeof value === 'object' && !Array.isArray(value) ? value : {};
    } catch (error) {
        return {};
    }
}

/** 在页面展示时从毫米转为厘米, 草稿和交易仍使用毫米. */
function inCm(value) {
    return value === null || value === '' || value === undefined ? '' : String(Number(value) / 10);
}

/** 从厘米输入转回服务端承认的毫米精度, 保留未填写状态. */
function inMm(value) {
    return value.trim() === '' ? '' : String(Number((Number(value) * 10).toFixed(3)));
}

/** 按真实穿孔或挂点尺寸读取页面交互所需的规格. */
function optionSize(type, params) {
    return type === 'chain' ? params.lengthMm : type === 'pearl' ? params.diameterMm : params.spanMm;
}

/** 根据服务端剩余空间和配置时的库存计算可追加件数. */
function additionalCapacity(option, type, rules, sizing, used) {
    const params = object(option.materialParams);
    const shownSize = optionSize(type, params);
    const span = params.lengthRole === 'HANGING' ? params.mountSpanMm || shownSize :
        type === 'pearl' ? params.axialMm || shownSize : shownSize;
    const spacing = Number(sizing?.spacingMm ?? rules.spacingMm) || 0;
    const remaining = sizing?.remainingCapacityMm;
    const lengthLimited = (type === 'pearl' || type === 'accessory') &&
        Number.isFinite(remaining) && Number.isFinite(span) && span > 0;
    let space = null;
    if (lengthLimited) {
        const first = sizing.placedCount ? 0 : spacing;
        space = sizing.capacityReady === false ? 0 :
            Math.max(0, Math.floor((remaining + first + 1e-9) / (span + spacing)));
    }
    const available = option.availableInstances;
    const stock = Number.isInteger(available) ? Math.max(0, available - used) : null;
    return {
        sizeLabel: Number.isFinite(shownSize) ?
            (type === 'chain' ? inCm(shownSize) + ' cm' : shownSize + ' mm') :
            (Number.isFinite(params.candidateDiameterMinMm) && Number.isFinite(params.candidateDiameterMaxMm)
                ? params.candidateDiameterMinMm + '–' + params.candidateDiameterMaxMm + ' mm' : '尺寸待商家确认'),
        addable: [space, stock].filter((count) => count !== null).reduce((a, b) => a === null ? b : Math.min(a, b), null),
        limitReason: stock !== null && (space === null || stock <= space) ? '库存不足' : '超出当前链身可用长度',
    };
}

/** 为账号切换创建全新的编辑状态, 不迁移其他账号的私人输入. */
function initialDesignerState() {
    return {
        goodsId: 0,
        styles: [],
        loadingStyles: false,
        stylesError: '',
        editMode: 'insert',
        activeType: '',
        draftId: null,
        draftVersion: null,
        componentGroups: [],
        selectedOptionIds: [],
        selectedOptionNames: [],
        arrangementOptionIds: [],
        arrangementNodes: [],
        lengthRules: {},
        layoutSpacingMm: 0,
        layoutOffsetMm: 0,
        pearlCount: 0,
        estimatedLengthCm: '',
        wearingClearanceCm: '',
        neckCm: '',
        targetCm: '',
        addCount: 1,
        activeIndex: -1,
        copyCount: 1,
        canUndo: false,

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
        activeGroupType: '',
        activeGroupName: '',
        activeGroupRequired: false,
        activeGroupHasSelection: false,
        activeOptions: [],
        materialVariants: [],
        activeMaterialVariant: '',

        activeName: '',
        showSizing: false,
        showDetail: false,
        referencePreferences: ['贴颈', '锁骨', '宽松'],
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
            this.setData({referenceError: '实测颈围须为 20 到 80 cm，最多两位小数'});
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

    /** 采用用户主动选定的参考长度, 重新报价且保留现有材料. */
    applyReference(event) {
        const mm = Number(event.currentTarget.dataset.mm);
        if (!this.data.saving && this.data.referenceResult?.lengthsMm?.includes(mm)) {
            this.setData({targetCm: inCm(mm), targetLengthMm: String(mm)});
            this.refreshQuote();
        }
    },

    /** 加载当前店铺已开放的项链款式, 过期响应不得盖过已选择款式. */
    loadStyles() {
        const sequence = (this._stylesSeq = (this._stylesSeq || 0) + 1);
        this.setData({loadingStyles: true, stylesError: ''});
        const current = () => sequence === this._stylesSeq && !this._unloaded;
        return util
            .request(api.CustomizationStyles)
            .then((res) => {
                if (!current()) {
                    return;
                }
                if (res.code !== 0 || !Array.isArray(res.data)) {
                    throw new Error(res.msg || '项链款式暂时无法加载');
                }
                this.setData({styles: res.data, loadingStyles: false});
            })
            .catch((error) => {
                if (current()) {
                    this.setData({
                        styles: [],
                        loadingStyles: false,
                        stylesError: error.message || '项链款式暂时无法加载',
                    });
                }
            });
    },

    /** 只用当前款式列表中的商品编号进入已有编辑器. */
    chooseStyle(event) {
        const goodsId = Number(event.currentTarget.dataset.id);
        if (this.data.goodsId || !this.data.styles.some((style) => style.goodsId === goodsId)) {
            return;
        }
        this.setData({goodsId});
        return this.loadConfig();
    },

    /** 用原始输入比较未保存变更, 不把异步报价当作用户编辑. */
    designFingerprint() {
        return JSON.stringify([
            this.data.arrangementOptionIds,
            this.data.layoutSpacingMm,
            this.data.layoutOffsetMm,
            this.data.engraving,
            this.data.neckCircumferenceMm,
            this.data.targetLengthMm,
        ]);
    },

    /** 返回款式列表, 有未保存设计时先让用户选择是否离开. */
    changeStyle() {
        if (this.data.saving) {
            return;
        }
        const returnToStyles = () => {
            this._snapshotSeq = (this._snapshotSeq || 0) + 1;
            this._configSeq = (this._configSeq || 0) + 1;
            this._quoteSeq = (this._quoteSeq || 0) + 1;
            this.invalidateReference();
            clearTimeout(this._engravingTimer);
            this._history = [];
            this._savedDesign = null;
            this.setData(initialDesignerState());
            this.loadStyles();
        };
        if (this._savedDesign === this.designFingerprint()) {
            returnToStyles();
            return;
        }
        wx.showModal({
            title: '返回选款',
            content: '当前改动尚未保存，是否返回选款？',
            confirmText: '返回选款',
            success: ({confirm}) => {
                if (confirm) {
                    returnToStyles();
                }
            },
        });
    },

    /** 打开本人设计列表, 由列表入口恢复选中的草稿. */
    openDesigns() {
        wx.navigateTo({url: '/pages/ucenter/design/design'});
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
            saving: false,
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
                    pearl: '珍珠',
                    clasp: '尾扣',
                    accessory: '配饰',
                    chain: '链条',
                };
                const primaryTypes = String((res.data.product || {}).requiredComponentTypes || '')
                    .split(',')
                    .map(function (type) {
                        return type.trim();
                    })
                    .filter(Boolean);
                const live = res.data?.options || [];
                const reference = (res.data?.referenceOptions || []).map((item) => ({...item, referenceOnly: true}));
                const options = live.concat(reference);
                if (!options.length) {
                    this.setData({loadingConfig: false, configError: '商家尚未配置可选组件'});
                    return;
                }
                if (
                    primaryTypes.some((type) => Object.keys(groupNames).indexOf(type) < 0) ||
                    options.some(
                        (option) => Object.keys(groupNames).indexOf(option.componentType) < 0
                    )
                ) {
                    this.setData({
                        loadingConfig: false,
                        configError: '材料分类尚未升级, 请商家按四类材料重新配置',
                    });
                    return;
                }
                options.forEach(function (option) {
                    const params = object(option.materialParams);
                    option.materialVariant = params.variety || params.chainStyle?.replace(/^o字链$/i, 'O字链') ||
                        ((option.componentType === 'clasp' || option.componentType === 'accessory') ? option.name : '');
                    if (option.referenceOnly && !option.assetUrl) {
                        const photos = res.data.referenceImages || [];
                        const photo = photos.find((item) => item.material === params.variety) ||
                            photos.find((item) => item.material === params.pearlSource);
                        if (photo) {
                            option.assetUrl = photo.asset;
                            option.photoCredit = photo.author;
                            option.photoSource = photo.source;
                            option.photoLicense = photo.license;
                        }
                    }
                    option.displayName = option.name || '';
                    if (option.componentType === 'clasp' && /^\/static\/images\/designer\/layers\/chain-/i.test(option.assetUrl || '')) {
                        option.assetUrl = '';
                    }
                    option.availableInstances = res.data.availableInstances?.[option.id];
                    if (!groups[option.componentType]) {
                        groups[option.componentType] = {
                            type: option.componentType,
                            name: groupNames[option.componentType] || option.componentType,
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
                const groupOrder = ['pearl', 'clasp', 'accessory', 'chain'];
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
                        (group.type === 'clasp' && group.required || group.type === 'chain' && group.options.filter((item) => !item.referenceOnly).length === 1) &&
                        !group.options.some(function (option) {
                            return option.selected;
                        }) &&
                        group.options.length > 0
                    ) {
                        const option = group.options.find((item) => !item.referenceOnly && !arrangement.isExtender(item));
                        if (option) {
                            option.selected = true;
                            selectedOptionIds.push(Number(option.id));
                        }
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
                    lengthRules: object(res.data.product?.lengthRules),
                    layoutSpacingMm: Number(restoredConfiguration?.layoutSpacingMm) || 0,
                    layoutOffsetMm: Number(restoredConfiguration?.layoutOffsetMm) || 0,
                    neckCm: inCm(restoredConfiguration?.neckCircumferenceMm),
                    targetCm: inCm(restoredConfiguration?.targetLengthMm),
                    loadingConfig: false,
                    configError: '',
                    activeIndex: -1,
                    activeMaterialVariant: '',
                    editMode: 'insert',
                    activeGroupType: !restoredConfiguration && groups.chain && groups.chain.options.length > 1
                        ? 'chain' : (componentGroups[0]?.type || ''),
                });
                this.refreshActiveGroup(componentGroups);
                this._history = [];
                this.updateArrangement(
                    restoredConfiguration ? restoredOptionIds : selectedOptionIds,
                    false
                );
                this._savedDesign = this.designFingerprint();
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
            saving: false,
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
                    return this.loadConfig(configuration);
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

    /** 选择替换或添加, 材料点击前明确当前动作. */
    setEditMode(event) {
        const mode = event.currentTarget.dataset.mode;
        if (
            this.data.saving ||
            !['replace', 'insert'].includes(mode) ||
            (mode === 'replace' && this.data.activeIndex < 0)
        ) {
            return;
        }
        this.setData({editMode: mode});
        if (mode === 'replace') {
            this.setData({activeGroupType: this.data.activeType, activeMaterialVariant: ''});
            this.refreshActiveGroup();
        }
    },

    /** 链条与尾扣直接切换, 其他材料按当前动作添加或替换. */
    selectOption(event) {
        if (this.data.loadingConfig || this.data.saving) return;
        const id = Number(event.currentTarget.dataset.id);
        const option = this.data.activeOptions.find((item) => Number(item.id) === id);
        if (!option) return;
        const {activeIndex, activeType, activeGroupType, arrangementNodes, arrangementOptionIds} = this.data;
        const dedicated = activeGroupType === 'chain' || activeGroupType === 'clasp';
        const extender = arrangement.isExtender(option);
        const existing = dedicated ? arrangementNodes.findIndex(
            (node) => node.type === activeGroupType && (activeGroupType !== 'clasp' || node.extender === extender)
        ) : -1;
        const replacing = dedicated ? existing >= 0 : this.data.editMode === 'replace' && activeGroupType === activeType && activeIndex >= 0;
        const index = dedicated ? existing : activeIndex;
        if (replacing && arrangementOptionIds[index] === id) return;
        const count = dedicated || replacing ? 1 : Number(this.data.addCount);
        if (!Number.isSafeInteger(count) || count < 1) {
            wx.showToast({title: '请输入正整数数量', icon: 'none'});
            return;
        }
        if (!replacing && option.addable != null && count > option.addable) {
            wx.showToast({title: option.limitReason + '，可添加 ' + option.addable + ' 件', icon: 'none'});
            return;
        }
        try {
            const next = arrangement.edit(arrangementOptionIds, replacing ? 'replace' : 'insert', index, id, count);
            this.updateArrangement(next, true);
            const nextIndex = replacing ? index : index + count;
            const node = this.data.arrangementNodes[nextIndex];
            this.setData({activeIndex: nextIndex, activeName: node.name, activeType: node.type});
        } catch (error) {
            wx.showToast({title: error.message, icon: 'none'});
        }
    },

    /** 对照当前排列核验 3D 拾取, 避免连续编辑后选错材料. */
    pickPreview(event) {
        const {index, optionId} = event.detail;
        if (this.data.arrangementOptionIds[index] === optionId) {
            this.selectInstance({currentTarget: {dataset: {index}}});
        }
    },

    /** 点击预览实例即进入同类材料替换, 可另选添加、删除或移动. */
    selectInstance(event) {
        const index = Number(event.currentTarget.dataset.index);
        const node = this.data.arrangementNodes[index];
        if (!node || this.data.saving) {
            return;
        }
        this.setData({
            activeIndex: index,
            activeName: node.name,
            activeType: node.type,
            editMode: 'replace',
            activeGroupType: node.type,
            activeMaterialVariant: '',
        });
        this.refreshActiveGroup();
    },

    /** 批量复制数量仅影响当前选中材料, 超限时整次操作不执行. */
    changeCopyCount(event) {
        this.setData({copyCount: event.detail.value});
    },

    /** 保存批量添加的输入, 具体约束在提交操作时校验. */
    changeAddCount(event) {
        this.setData({addCount: event.detail.value});
    },

    /** 统一记录排列历史, 每次变更立即废弃旧报价. */
    updateArrangement(ids, remember, placement = {}) {
        if (remember) {
            this._history = this._history || [];
            this._history.push({ids: this.data.arrangementOptionIds.slice(), layoutSpacingMm: this.data.layoutSpacingMm, layoutOffsetMm: this.data.layoutOffsetMm});
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
        const nodes = arrangement.describe(ids, options);
        const nextActiveIndex = Math.min(this.data.activeIndex, ids.length - 1);
        this.setData({
            ...placement,
            arrangementOptionIds: ids.slice(),
            arrangementNodes: nodes,
            pearlCount: nodes.filter((node) => node.type === 'pearl').length,
            selectedOptionIds: Array.from(new Set(ids)),
            componentGroups: groups,
            canUndo: Boolean(this._history && this._history.length),
            activeIndex: nextActiveIndex,
            activeType: nodes[nextActiveIndex] ? nodes[nextActiveIndex].type : '',
            editMode: nextActiveIndex < 0 ? 'insert' : this.data.editMode,
            activeName:
                nextActiveIndex >= 0 && nodes[nextActiveIndex] ? nodes[nextActiveIndex].name : '',
            selectedOptionNames: nodes.map(function (node) {
                return node.name;
            }),
        });
        this.refreshActiveGroup(groups);
        return this.refreshQuote();
    },

    /** 同步当前材料分组的选项到独立字段, 避免模板直接依赖嵌套下标. */
    refreshActiveGroup(groups) {
        const list = groups || this.data.componentGroups;
        const current = list.filter((group) => group.type === this.data.activeGroupType)[0];
        const options = current ? current.options : [];
        const variants = [...new Set(options.map((option) => option.materialVariant).filter(Boolean))];
        const variant = variants.includes(this.data.activeMaterialVariant) ? this.data.activeMaterialVariant : '';
        const counts = new Map();
        for (const id of this.data.arrangementOptionIds) {
            counts.set(id, (counts.get(id) || 0) + 1);
        }
        this.setData({
            activeGroupName: current ? current.name : '',
            activeGroupRequired: current ? current.required : false,
            activeGroupHasSelection: current ? Boolean(current.hasSelection) : false,
            activeMaterialVariant: variant,
            materialVariants: variants,
            activeOptions: options.filter((option) => !variant || option.materialVariant === variant)
                .map((option) => ({
                    ...option,
                    ...additionalCapacity(option, current.type, this.data.lengthRules, this.data.quote.sizing,
                        counts.get(Number(option.id)) || 0),
                })),
        });
    },

    /** 保留参考摄影的出处和许可证, 不将著作权信息当作商品描述. */
    referenceCredit(event) {
        const {id} = event.currentTarget.dataset;
        const option = this.data.activeOptions.find((item) => Number(item.id) === Number(id));
        if (option?.photoCredit) {
            wx.showModal({
                title: '参考图片出处',
                content: option.photoCredit + '\n' + option.photoSource + '\n' + option.photoLicense,
                showCancel: false,
            });
        }
    },

    /** 筛选当前材料组内的品种或款式, 保留已选材料和编辑位置. */
    selectMaterialVariant(event) {
        const variant = event.currentTarget.dataset.variant || '';
        this.setData({activeMaterialVariant: variant});
        this.refreshActiveGroup();
    },

    /** 切换底部材料分组, 插入操作仍落在当前选中实例之后. */
    switchGroup(event) {
        const type = event.currentTarget.dataset.type;
        if (!type || type === this.data.activeGroupType) {
            return;
        }
        this.setData({activeGroupType: type, activeMaterialVariant: '', editMode: 'insert'});
        this.refreshActiveGroup();
    },

    /** 调整使用链身的搭配间距和整体位置, 撤回与材料排列共享历史. */
    adjustPlacement(event) {
        if (this.data.saving || this.data.quoting) return;
        const {action} = event.currentTarget.dataset;
        const {layoutSpacingMm, layoutOffsetMm, lengthRules, quote} = this.data;
        const available = quote.sizing?.remainingCapacityMm;
        const count = quote.sizing?.placedCount || 0;
        let spacing = layoutSpacingMm;
        let offset = layoutOffsetMm;
        if (action === 'space') {
            const next = Math.max(Number(lengthRules.spacingMm) || 0, spacing) + 1;
            if (count < 2 || !Number.isFinite(available) || available < count - 1) {
                wx.showToast({title: '当前设计没有更多可用间距', icon: 'none'});
                return;
            }
            spacing = next;
        } else if (action === 'left' || action === 'right') {
            const next = offset + (action === 'left' ? -2 : 2);
            if (!count || !Number.isFinite(available) || available < (Math.abs(next) - Math.abs(offset)) * 2) {
                wx.showToast({title: '当前设计无法移至该位置', icon: 'none'});
                return;
            }
            offset = next;
        } else if (action === 'reset') {
            spacing = offset = 0;
        } else {
            return;
        }
        if (spacing === layoutSpacingMm && offset === layoutOffsetMm) return;
        this.updateArrangement(this.data.arrangementOptionIds, true, {
            layoutSpacingMm: spacing,
            layoutOffsetMm: offset,
        });
    },

    /** 展开或收起尺寸与规则参考面板. */
    toggleSizing() {
        this.setData({showSizing: !this.data.showSizing});
    },

    /** 展开或收起报价明细. */
    toggleDetail() {
        this.setData({showDetail: !this.data.showDetail});
    },

    /** 撤回最后一次排列操作, 不修改已下单快照. */
    undoArrangement() {
        if (!this._history || !this._history.length || this.data.saving) {
            return;
        }
        const {ids, ...placement} = this._history.pop();
        this.setData(placement);
        this.updateArrangement(ids, false);
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
                content: '清空珍珠和配饰，保留基础链条和尾扣。',
                success: (res) => {
                    if (res.confirm) {
                        this.updateArrangement(this.data.arrangementNodes.filter((node) => node.type === 'chain' || node.type === 'clasp').map((node) => node.optionId), true);
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
            const node = this.data.arrangementNodes[index];
            if (action === 'copy') {
                if (node.type === 'clasp' || node.type === 'chain') {
                    throw new Error('请在材料栏切换链条或尾扣');
                }
                const group = this.data.componentGroups.find((item) => item.type === node.type);
                const option = group.options.find((item) => Number(item.id) === node.optionId);
                const used = this.data.arrangementOptionIds.filter((id) => id === node.optionId).length;
                const capacity = additionalCapacity(option, node.type, this.data.lengthRules, this.data.quote.sizing, used);
                if (capacity.addable != null && count > capacity.addable) {
                    throw new Error(capacity.limitReason + '，可复制 ' + capacity.addable + ' 件');
                }
            }
            const next = arrangement.edit(this.data.arrangementOptionIds, action, index, null, count);
            this.updateArrangement(next, true);
            if (action === 'left' || action === 'right') {
                const moved = Math.max(
                    0,
                    Math.min(
                        this.data.arrangementOptionIds.length - 1,
                        index + (action === 'left' ? -1 : 1)
                    )
                );
                const node = this.data.arrangementNodes[moved];
                this.setData({
                    activeIndex: moved,
                    activeName: node ? node.name : '',
                    activeType: node ? node.type : '',
                });
            }
            if (action === 'copy') {
                const copied = Math.min(index + count, this.data.arrangementOptionIds.length - 1);
                const node = this.data.arrangementNodes[copied];
                this.setData({activeIndex: copied, activeName: node ? node.name : ''});
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
        if (!['neckCm', 'targetCm'].includes(field)) {
            return;
        }
        this.invalidateReference();
        this.setData({[field]: event.detail.value, [field === 'neckCm' ? 'neckCircumferenceMm' : 'targetLengthMm']: inMm(event.detail.value)});
        this.refreshQuote();
    },

    /** 请求服务端报价 / Requests server-side quote. */
    refreshQuote() {
        // 递增序号丢弃过期响应, 避免快速切换选项时旧报价覆盖新报价 /
        // Bump a sequence to drop stale responses racing with newer selections.
        const seq = (this._quoteSeq = (this._quoteSeq || 0) + 1);
        this.setData({quote: {}, quoting: true, quoteError: '', estimatedLengthCm: '', wearingClearanceCm: ''});
        this.refreshActiveGroup();
        return util
            .request(
                api.CustomizationQuote,
                {
                    goodsId: this.data.goodsId,
                    arrangementOptionIds: this.data.arrangementOptionIds,
                    layoutSpacingMm: this.data.layoutSpacingMm || null,
                    layoutOffsetMm: this.data.layoutOffsetMm || null,
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
                this.setData({quote: res.data, quoting: false, quoteError: '', estimatedLengthCm: inCm(res.data.sizing?.estimatedFinishedLengthMm), wearingClearanceCm: inCm(res.data.sizing?.wearingClearanceMm)});
                this.refreshActiveGroup();
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

    /** 公开分享先保存本人珍珠排列, 不从渲染状态或示意尺寸直接生成订单. */
    shareDesign() {
        if (this.data.saving || !this.data.pearlCount || this.data.quote.referencePreview) return;
        this.persistDesign(false).then((id) => {
            if (id) wx.navigateTo({url: '/pages/community/publish?designId=' + encodeURIComponent(id)});
        }).catch(() => {});
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
        if (!this.data.goodsId || this.data.loadingConfig || this.data.configError) {
            return Promise.reject(new Error('请先加载有效的材料配置'));
        }
        if (
            startCheckout &&
            (this.data.quoting || this.data.quoteError || !this.data.quote.totalPrice)
        ) {
            return Promise.reject(new Error('请等待有效报价后再结算'));
        }
        if (startCheckout && (this.data.quote.checkoutReady !== true || !this.data.pearlCount)) {
            wx.showToast({title: '请先完成成品长度校验', icon: 'none'});
            return Promise.reject(new Error('成品长度尚未校验通过'));
        }
        const token = wx.getStorageSync('token') || '';
        const sequence = this._configSeq;
        const current = () =>
            !this._unloaded &&
            sequence === this._configSeq &&
            token === (wx.getStorageSync('token') || '');
        this.setData({saving: true});
        return util
            .request(
                api.CustomizationSave,
                {
                    goodsId: this.data.goodsId,
                    draftId: this.data.draftId,
                    version: this.data.draftVersion,
                    arrangementOptionIds: this.data.arrangementOptionIds,
                    layoutSpacingMm: this.data.layoutSpacingMm || null,
                    layoutOffsetMm: this.data.layoutOffsetMm || null,
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
                if (res.code !== 0) {
                    throw new Error(res.msg || '保存失败');
                }
                const savedQuote = res.data.quote || {};
                this._savedDesign = this.designFingerprint();
                wx.setStorageSync('lastSavedDesignSnapshotId', res.data.snapshotId);
                this.setData({
                    draftId: res.data.snapshotId,
                    draftVersion: res.data.version,
                });
                if (!startCheckout) {
                    wx.showToast({
                        title:
                            savedQuote.checkoutReady === true
                                ? '设计已保存'
                                : '草稿已保存，尚不能下单',
                        icon: 'none',
                    });
                    return res.data.snapshotId;
                }
                if (savedQuote.checkoutReady !== true) {
                    const notice = (savedQuote.sizing || {}).notice || '当前设计尚不能下单';
                    this._quoteSeq = (this._quoteSeq || 0) + 1;
                    this.setData({quote: savedQuote, quoting: false, quoteError: notice});
                    throw new Error(notice);
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
                wx.showToast({title: error.message || '操作失败', icon: 'none'});
                throw error;
            })
            .finally(() => {
                if (current()) {
                    this.setData({saving: false});
                }
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
        // 商品页与草稿可直达, 底部导航入口先选款式.
        if (!goodsId) {
            goodsId = Number(wx.getStorageSync('designerGoodsId') || 0);
        }
        if (!snapshotId) {
            snapshotId = Number(wx.getStorageSync('designerSnapshotId') || 0);
        }
        wx.removeStorageSync('designerGoodsId');
        wx.removeStorageSync('designerSnapshotId');
        this.setData({goodsId: goodsId});
        if (snapshotId) {
            this.loadDesignSnapshot(snapshotId);
        } else if (goodsId) {
            this.loadConfig();
        } else {
            this.loadStyles();
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
            this.setData(initialDesignerState());
            this.loadStyles();
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
        if (goodsId) {
            wx.removeStorageSync('designerGoodsId');
            if (goodsId === this.data.goodsId && !this.data.draftId) {
                return;
            }
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
