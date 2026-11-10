const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');
const pay = require('../../../services/pay.js');

Page({
    data: {
        orderId: 0,
        paying: false,
        writing: false,
        loading: false,
        error: '',
        refundError: '',
        orderInfo: {},
        legalDocuments: [],
        orderGoods: [],
        handleOption: {},
        customization: null,
        refund: {},
        refundReason: '',
        refundSubmitting: false,
    },
    /** 初始化订单详情状态并读取页面所需数据. */
    onLoad(options = {}) {
        this._unloaded = false;
        this._sessionToken = wx.getStorageSync('token') || '';
        const id = Number(options.id);
        if (!Number.isSafeInteger(id) || id <= 0) {
            this.setData({error: '订单标识无效, 请从本人订单重新进入'});
            return;
        }
        this.setData({orderId: id});
        return this.getOrderDetail();
    },
    /** 并行读取本人订单和售后, 独立记录失败且隔离账号、离页及乱序响应. */
    getOrderDetail() {
        if (!this.data.orderId) {
            return Promise.resolve(false);
        }
        const id = this.data.orderId;
        const token = wx.getStorageSync('token') || '';
        const version = (this._detailVersion = (this._detailVersion || 0) + 1);
        const current = () =>
            !this._unloaded &&
            version === this._detailVersion &&
            id === this.data.orderId &&
            token === (wx.getStorageSync('token') || '');
        this.setData({loading: true, error: '', refundError: ''});
        const order = util
            .request(api.OrderDetail, {orderId: id})
            .then((res) => {
                if (!current()) {
                    return;
                }
                if (
                    res.code !== 0 ||
                    !res.data ||
                    !res.data.orderInfo ||
                    Number(res.data.orderInfo.id) !== id
                ) {
                    throw new Error(res.msg || '订单读取失败');
                }
                this.setData({
                    orderInfo: res.data.orderInfo,
                    legalDocuments: res.data.legalDocuments || [],
                    orderGoods: res.data.orderGoods || [],
                    handleOption: res.data.handleOption || {},
                    customization: this.formatCustomization(res.data.customization),
                });
            })
            .catch((error) => {
                if (current()) {
                    this.setData({error: error.message || '订单读取失败, 请重试'});
                }
            });
        const refund = util
            .request(api.RefundStatus, {orderId: id})
            .then((res) => {
                if (!current()) {
                    return;
                }
                if (res.code !== 0) {
                    throw new Error(res.msg || '售后读取失败');
                }
                const names = {
                    PENDING_REVIEW: '待商家审核',
                    APPROVED: '已通过审核, 等待退款',
                    PROCESSING: '退款处理中',
                    RETRY: '退款对账中',
                    SUCCEEDED: '退款已完成',
                    REJECTED: '审核未通过',
                    MANUAL_REVIEW: '需商家处理退款异常',
                };
                const value = res.data || {};
                this.setData({refund: {...value, label: names[value.status] || ''}});
            })
            .catch((error) => {
                if (current()) {
                    this.setData({refundError: error.message || '售后读取失败, 请重试'});
                }
            });
        return Promise.all([order, refund]).finally(() => {
            if (current()) {
                this.setData({loading: false});
            }
        });
    },
    /** 账号变化时清空私人订单内容, 同账号从评价页返回时刷新评价资格. */
    onShow() {
        const token = wx.getStorageSync('token') || '';
        const refreshReview = this._reviewPageOpened;
        this._reviewPageOpened = false;
        if (this._sessionToken !== undefined && token !== this._sessionToken) {
            this._detailVersion = (this._detailVersion || 0) + 1;
            this.setData({
                orderId: 0,
                orderInfo: {},
                orderGoods: [],
                legalDocuments: [],
                customization: null,
                handleOption: {},
                refund: {},
                refundReason: '',
                refundSubmitting: false,
                paying: false,
                writing: false,
                loading: false,
                error: '账号已切换, 请从本人订单重新进入',
            });
        } else if (refreshReview) {
            return this.getOrderDetail();
        }
        this._sessionToken = token;
    },
    /** 仅为当前订单可评价商品打开发布页, 返回时重新读取订单. */
    openReview(event) {
        const goodsId = Number(event.currentTarget.dataset.goodsId);
        if (
            this._reviewPageOpened ||
            this._sessionToken !== (wx.getStorageSync('token') || '') ||
            this.data.loading ||
            this.data.error ||
            !this.data.handleOption.comment ||
            !this.data.orderGoods.some(
                (item) => item.goodsId === goodsId && item.reviewed === false
            )
        ) {
            return;
        }
        this._reviewPageOpened = true;
        wx.navigateTo({
            url: '/pages/commentPost/commentPost?typeId=0&valueId=' + goodsId,
            fail: () => {
                this._reviewPageOpened = false;
                util.showErrorToast('评价页面打开失败, 请重试');
            },
        });
    },
    /** 离页后废弃详情、支付及后续状态回调. */
    onUnload() {
        this._unloaded = true;
        this._detailVersion = (this._detailVersion || 0) + 1;
    },
    /** 售后理由只随本人订单申请提交. */
    changeRefundReason(event) {
        this.setData({refundReason: event.detail.value});
    },
    /** 申请审核不显示为资金已退回, 防止误导用户. */
    requestRefund() {
        if (
            this._unloaded ||
            this._sessionToken !== (wx.getStorageSync('token') || '') ||
            !this.data.orderId ||
            this.data.refundSubmitting ||
            this.data.writing ||
            this.data.paying ||
            this.data.loading ||
            this.data.error ||
            this.data.refundError ||
            Number(this.data.orderInfo.payStatus) !== 2 ||
            ![201, 300, 301].includes(Number(this.data.orderInfo.orderStatus)) ||
            (this.data.refund.status && this.data.refund.status !== 'NONE')
        ) {
            return;
        }
        const reason = this.data.refundReason.trim();
        if (!reason || reason.length > 200) {
            return util.showErrorToast('请填写1到200字的售后理由');
        }
        const id = this.data.orderId;
        const token = wx.getStorageSync('token') || '';
        const current = () =>
            !this._unloaded &&
            this.data.orderId === id &&
            token === (wx.getStorageSync('token') || '');
        this.setData({refundSubmitting: true});
        return util
            .request(api.RefundRequest, {orderId: id, reason: reason}, 'POST', 'application/json')
            .then((res) => {
                if (!current()) {
                    return false;
                }
                if (res.code !== 0) {
                    throw new Error(res.msg || '申请失败');
                }
                wx.showToast({title: '申请已提交', icon: 'none'});
                return this.getOrderDetail();
            })
            .catch((error) => {
                if (current()) {
                    this.setData({
                        refundError: error.message || '申请结果未确认, 请重新读取售后状态',
                    });
                }
                return false;
            })
            .finally(() => {
                if (current()) {
                    this.setData({refundSubmitting: false});
                }
            });
    },
    /** 将后端快照转换为订单展示字段 / Formats customization snapshot for display. */
    formatCustomization(customization) {
        if (!customization) {
            return null;
        }
        let engraving;
        let quote = {};
        try {
            engraving = (
                JSON.parse(customization.configurationJson || '{}').engraving || ''
            ).trim();
            quote = JSON.parse(customization.priceBreakdownJson || '{}');
        } catch (error) {
            engraving = '';
        }
        return {
            previewUrl: customization.previewUrl || '/static/images/logo.png',
            status: customization.designStatus || customization.status || '已保存',
            engraving: engraving || '未填写',
            arrangement: quote.arrangement || [],
            components: quote.components || [],
            sizing: quote.sizing || null,
            lengthText:
                quote.sizing && quote.sizing.estimatedFinishedLengthMm != null
                    ? quote.sizing.estimatedFinishedLengthMm + 'mm (下单冻结工艺估算)'
                    : quote.lengthComplete
                      ? quote.estimatedLengthMm + 'mm (历史材料估算, 成品以商家确认为准)'
                      : '历史尺寸未配置完整',
        };
    },
    /** 取消和确认收货共用确认阶段写入锁, 结果不明确时要求重新读取订单. */
    performOrderAction(kind) {
        const actions = {
            cancel: {url: api.OrderCancel, question: '确定要取消此订单？'},
            confirm: {url: api.OrderConfirm, question: '确认已经收到商品？'},
        };
        const action = actions[kind];
        if (
            !action ||
            this._unloaded ||
            this.data.loading ||
            this.data.error ||
            this.data.writing ||
            this.data.paying ||
            this.data.refundSubmitting ||
            !this.data.orderId ||
            !this.data.handleOption[kind]
        ) {
            return Promise.resolve(false);
        }
        const id = this.data.orderId;
        const token = wx.getStorageSync('token') || '';
        const current = () =>
            !this._unloaded &&
            id === this.data.orderId &&
            token === (wx.getStorageSync('token') || '');
        this.setData({writing: true});
        return new Promise((resolve) =>
            wx.showModal({
                title: '订单操作',
                content: action.question,
                success: (result) => resolve(Boolean(result.confirm)),
                fail: () => resolve(false),
            })
        )
            .then(async (confirmed) => {
                if (!confirmed || !current()) {
                    return false;
                }
                if (!this.data.handleOption[kind]) {
                    throw new Error('订单状态已变化, 请重新读取');
                }
                const res = await util.request(action.url, {orderId: id});
                if (!current()) {
                    return false;
                }
                if (res.code !== 0) {
                    throw new Error(res.msg || '操作结果未确认, 请重新读取订单');
                }
                await this.getOrderDetail();
                return true;
            })
            .catch((error) => {
                if (current()) {
                    this.setData({error: error.message || '操作结果未确认, 请重新读取订单'});
                }
                return false;
            })
            .finally(() => {
                if (current()) {
                    this.setData({writing: false});
                }
            });
    },
    /** 用户确认后取消当前本人订单, 不自动重放写入. */
    cancelOrder() {
        return this.performOrderAction('cancel');
    },
    /** 发起当前订单的微信支付并处理结果提示. */
    payOrder() {
        const id = Number(this.data.orderId);
        if (
            !Number.isSafeInteger(id) ||
            id <= 0 ||
            this.data.paying ||
            this.data.writing ||
            this.data.loading ||
            this.data.error ||
            this.data.refundSubmitting ||
            !this.data.handleOption.pay
        ) {
            return Promise.resolve(false);
        }
        const token = wx.getStorageSync('token') || '';
        const current = () => !this._unloaded && token === (wx.getStorageSync('token') || '');
        this.setData({paying: true});
        return pay
            .payOrder(id)
            .then(() => {
                if (current()) {
                    this.getOrderDetail();
                }
                return current();
            })
            .catch((error) => {
                if (current()) {
                    util.showErrorToast(error.message || error.errMsg || '支付未完成, 请核对订单');
                }
                return false;
            })
            .finally(() => {
                if (current()) {
                    this.setData({paying: false});
                }
            });
    },
    /** 用户确认后提交本人收货确认, 共用订单操作保护. */
    confirmOrder() {
        return this.performOrderAction('confirm');
    },
});
