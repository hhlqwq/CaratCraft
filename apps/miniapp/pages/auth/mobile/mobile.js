const api = require('../../../config/api.js');
const util = require('../../../utils/util.js');
const app = getApp();

Page({
    data: {
        mobile: '',
        userInfo: {
            avatar: '',
            nickname: '',
        },
        disableGetMobileCode: false,
        disableSubmitMobileCode: true,
        getCodeButtonText: '获取验证码',
        sendingCode: false,
        submitting: false,
    },

    /** 初始化手机号绑定状态并读取会话中的本人资料. */
    onLoad() {
        this._unloaded = false;
        this.setData({userInfo: app.globalData.userInfo || {}});
        if (!app.globalData.token) {
            const token = wx.getStorageSync('token');
            if (token) {
                app.globalData.token = token;
            }
        }
    },

    /** 清理验证码倒计时, 避免离页后继续写入页面. */
    onUnload() {
        this._unloaded = true;
        if (this._timer) {
            clearInterval(this._timer);
            this._timer = null;
        }
    },

    /** 校验国内手机号格式, 不合法时提示并阻止后续提交. */
    bindCheckMobile(mobile) {
        if (!mobile) {
            wx.showModal({
                title: '错误',
                content: '请输入手机号码',
            });
            return false;
        }
        if (!/^1[3-9]\d{9}$/.test(mobile)) {
            wx.showModal({
                title: '错误',
                content: '手机号格式不正确，仅支持国内手机号码',
            });
            return false;
        }
        return true;
    },

    /** 仅保留数字并同步手机号输入. */
    bindInputMobile(event) {
        this.setData({
            mobile: String(event.detail.value || '')
                .replace(/[^0-9]/g, '')
                .slice(0, 11),
        });
    },

    /** 请求六位短信验证码并启动页面级倒计时, 失败立即恢复按钮. */
    countDownPassCode() {
        if (this.data.sendingCode || this.data.disableGetMobileCode) {
            return Promise.resolve(false);
        }
        if (!this.bindCheckMobile(this.data.mobile)) {
            return Promise.resolve(false);
        }
        this.setData({sendingCode: true, disableGetMobileCode: true});
        return util
            .request(api.SmsCode, {phone: this.data.mobile}, 'POST', 'application/json')
            .then((res) => {
                if (this._unloaded) {
                    return false;
                }
                if (res.code !== 0) {
                    throw new Error(res.msg || '验证码发送失败');
                }
                wx.showToast({title: '发送成功', icon: 'success', duration: 1000});
                this.setData({disableSubmitMobileCode: false});
                this.startCountdown();
                return true;
            })
            .catch((error) => {
                if (!this._unloaded) {
                    this.setData({disableGetMobileCode: false});
                    wx.showToast({
                        title: error.message || '验证码发送失败',
                        icon: 'none',
                        duration: 2000,
                    });
                }
                return false;
            })
            .finally(() => {
                if (!this._unloaded) {
                    this.setData({sendingCode: false});
                }
            });
    },

    /** 使用页面实例启动六十秒倒计时, 离页后停止. */
    startCountdown() {
        if (this._timer) {
            clearInterval(this._timer);
        }
        let remaining = 60;
        this.setData({disableGetMobileCode: true, getCodeButtonText: remaining + 's'});
        this._timer = setInterval(() => {
            if (this._unloaded) {
                clearInterval(this._timer);
                this._timer = null;
                return;
            }
            remaining -= 1;
            if (remaining <= 0) {
                clearInterval(this._timer);
                this._timer = null;
                this.setData({
                    disableGetMobileCode: false,
                    getCodeButtonText: '获取验证码',
                });
                return;
            }
            this.setData({getCodeButtonText: remaining + 's'});
        }, 1000);
    },

    /** 提交当前手机号和六位验证码完成绑定. */
    bindLoginMobilecode(event) {
        if (this.data.submitting) {
            return Promise.resolve(false);
        }
        const mobile = this.data.mobile;
        if (!this.bindCheckMobile(mobile)) {
            return Promise.resolve(false);
        }
        const code = event && event.detail && event.detail.value ? event.detail.value.code : '';
        if (!/^[0-9]{6}$/.test(String(code || ''))) {
            wx.showToast({title: '请输入六位验证码', icon: 'none'});
            return Promise.resolve(false);
        }
        const token = wx.getStorageSync('token') || '';
        const current = () => !this._unloaded && token === (wx.getStorageSync('token') || '');
        this.setData({submitting: true});
        return util
            .request(
                api.BindMobile,
                {mobileCode: String(code), mobile: mobile},
                'POST',
                'application/json'
            )
            .then((res) => {
                if (!current()) {
                    return false;
                }
                if (res.code !== 0) {
                    throw new Error(res.msg || '绑定失败');
                }
                wx.showModal({
                    title: '提示',
                    content: '手机号绑定成功',
                    showCancel: false,
                    success: () => {
                        if (current()) {
                            wx.switchTab({url: '/pages/ucenter/index/index'});
                        }
                    },
                });
                return true;
            })
            .catch((error) => {
                if (current()) {
                    wx.showModal({
                        title: '提示',
                        content: error.message || '绑定失败',
                        showCancel: false,
                    });
                }
                return false;
            })
            .finally(() => {
                if (current()) {
                    this.setData({submitting: false});
                }
            });
    },
});
