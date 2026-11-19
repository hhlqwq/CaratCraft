const util = require('../../utils/util.js');
const api = require('../../config/api.js');
const pay = require('../../services/pay.js');

Page({
    data: {orderId: 0, actualPrice: '', loading: false, paying: false, payable: false, error: ''},
    /** 只采用有效订单标识, 金额从本人订单详情读取. */
    onLoad(options = {}) {
        this._unloaded = false;
        this._sessionToken = wx.getStorageSync('token') || '';
        const id = Number(options.orderId);
        if (!Number.isSafeInteger(id) || id <= 0) {
            this.setData({error: '订单标识无效, 请从本人订单重新进入'});
            return;
        }
        this.setData({orderId: id});
        return this.loadOrder();
    },
    /** 切换账号后清除上一账号订单展示并废弃未完成响应. */
    onShow() {
        const token = wx.getStorageSync('token') || '';
        if (this._sessionToken !== undefined && token !== this._sessionToken) {
            this._readVersion = (this._readVersion || 0) + 1;
            this._queryVersion = (this._queryVersion || 0) + 1;
            this.setData({
                orderId: 0,
                actualPrice: '',
                payable: false,
                loading: false,
                paying: false,
                error: '账号已切换, 请从本人订单重新进入',
            });
        }
        this._sessionToken = token;
    },
    /** 明确读取本人订单并核对可支付状态, 不信任路由金额. */
    loadOrder() {
        if (!this.data.orderId || this.data.paying) {
            return Promise.resolve(false);
        }
        const token = wx.getStorageSync('token') || '';
        const version = (this._readVersion = (this._readVersion || 0) + 1);
        const current = () =>
            !this._unloaded &&
            version === this._readVersion &&
            token === (wx.getStorageSync('token') || '');
        this.setData({loading: true, payable: false, error: ''});
        return util
            .request(api.OrderDetail, {orderId: this.data.orderId})
            .then((res) => {
                if (!current()) {
                    return false;
                }
                const order = res.data && res.data.orderInfo;
                if (res.code !== 0 || !order || Number(order.id) !== this.data.orderId) {
                    throw new Error(res.msg || '订单读取失败');
                }
                this.setData({
                    actualPrice: order.actualPrice,
                    payable: Number(order.payStatus) === 0 && Number(order.orderStatus) === 0,
                });
                return true;
            })
            .catch((error) => {
                if (current()) {
                    this.setData({error: error.message || '订单读取失败, 请重试'});
                }
                return false;
            })
            .finally(() => {
                if (current()) {
                    this.setData({loading: false});
                }
            });
    },
    /** 所有支付入口共用签名参数及服务端查单, 点击期间禁止重复提交. */
    startPay() {
        if (this.data.paying || this.data.loading || !this.data.payable) {
            return Promise.resolve(false);
        }
        const token = wx.getStorageSync('token') || '';
        const current = () => !this._unloaded && token === (wx.getStorageSync('token') || '');
        this.setData({paying: true, error: ''});
        return pay
            .payOrder(this.data.orderId)
            .then(() => {
                if (!current()) {
                    return false;
                }
                this.setData({payable: false});
                wx.redirectTo({
                    url: '/pages/payResult/payResult?orderId=' + this.data.orderId,
                    fail: () => {
                        if (current()) {
                            this.setData({error: '支付已确认, 结果页打开失败, 请在订单中查看'});
                        }
                    },
                });
                return true;
            })
            .catch((error) => {
                if (current()) {
                    this.setData({
                        error: error.message || error.errMsg || '支付未完成, 请核对订单后重试',
                    });
                }
                return false;
            })
            .finally(() => {
                if (current()) {
                    this.setData({paying: false});
                }
            });
    },
    /** 离页后不让旧读取或支付回调更新当前页面. */
    onUnload() {
        this._unloaded = true;
        this._readVersion = (this._readVersion || 0) + 1;
    },
});
