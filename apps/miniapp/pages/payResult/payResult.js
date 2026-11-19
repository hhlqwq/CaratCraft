const util = require('../../utils/util.js');
const api = require('../../config/api.js');
const pay = require('../../services/pay.js');

Page({
    data: {status: 'unknown', orderId: 0, checking: false, paying: false, error: ''},
    /** 忽略路由中的成功标记, 只查询明确的本人订单. */
    onLoad(options = {}) {
        this._unloaded = false;
        this._sessionToken = wx.getStorageSync('token') || '';
        const id = Number(options.orderId);
        if (!Number.isSafeInteger(id) || id <= 0) {
            this.setData({error: '订单标识无效, 请在本人订单中查看支付结果'});
            return;
        }
        this.setData({orderId: id});
        return this.updateSuccess();
    },
    /** 切换账号后清除上一账号订单展示并废弃未完成响应. */
    onShow() {
        const token = wx.getStorageSync('token') || '';
        if (this._sessionToken !== undefined && token !== this._sessionToken) {
            this._readVersion = (this._readVersion || 0) + 1;
            this._queryVersion = (this._queryVersion || 0) + 1;
            this.setData({
                orderId: 0,
                status: 'unknown',
                checking: false,
                paying: false,
                error: '账号已切换, 请在本人订单中查看',
            });
        }
        this._sessionToken = token;
    },
    /** 成功仅由服务端查单确认, 网络失败保持待确认而不推断付款结果. */
    updateSuccess() {
        if (!this.data.orderId || this.data.checking) {
            return Promise.resolve(false);
        }
        const token = wx.getStorageSync('token') || '';
        const version = (this._queryVersion = (this._queryVersion || 0) + 1);
        const current = () =>
            !this._unloaded &&
            version === this._queryVersion &&
            token === (wx.getStorageSync('token') || '');
        this.setData({checking: true, error: ''});
        return util
            .request(api.OrderQuery, {orderId: this.data.orderId})
            .then((res) => {
                if (!current()) {
                    return false;
                }
                if (res.code !== 0) {
                    throw new Error(res.msg || '支付结果待确认, 请稍后查询');
                }
                this.setData({status: 'paid'});
                return true;
            })
            .catch((error) => {
                if (current()) {
                    this.setData({
                        status: 'unknown',
                        error: error.message || '支付结果待确认, 请稍后查询',
                    });
                }
                return false;
            })
            .finally(() => {
                if (current()) {
                    this.setData({checking: false});
                }
            });
    },
    /** 重新支付须由用户主动触发, 共用服务端预下单及查单, 不自动重放. */
    payOrder() {
        if (
            !this.data.orderId ||
            this.data.paying ||
            this.data.checking ||
            this.data.status === 'paid'
        ) {
            return Promise.resolve(false);
        }
        const token = wx.getStorageSync('token') || '';
        const current = () => !this._unloaded && token === (wx.getStorageSync('token') || '');
        this.setData({paying: true, error: ''});
        return pay
            .payOrder(this.data.orderId)
            .then(() => {
                if (current()) {
                    this.setData({status: 'paid'});
                }
                return current();
            })
            .catch((error) => {
                if (current()) {
                    this.setData({
                        error: error.message || error.errMsg || '支付未完成, 请在订单中核对',
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
    /** 页面卸载后使待定查单结果失效. */
    onUnload() {
        this._unloaded = true;
        this._queryVersion = (this._queryVersion || 0) + 1;
    },
});
