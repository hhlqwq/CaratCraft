const api = require('../../../config/api.js');
const util = require('../../../utils/util.js');
const app = getApp();

Page({
    data: {
        mobile: '',
        mobileCode: '',
        formError: '',
        disableGetMobileCode: false,
        disableSubmitMobileCode: true,
        getCodeButtonText: '获取验证码',
        sendingCode: false,
        submitting: false,
    },

    /** 初始化手机号绑定状态并读取会话中的本人资料. */
    onLoad() {
        this._unloaded = false;
        const token = wx.getStorageSync('token') || '';
        this._session = {token};
        this.setData({formError: ''});
        if (!app.globalData.token && token) {
            app.globalData.token = token;
        }
    },

    /** 切换账号后清除旧手机号、验证码和倒计时, 迟到响应不得恢复这些内容. */
    onShow() {
        const token = wx.getStorageSync('token') || '';
        if (token !== this._session.token) {
            this.onUnload();
            this.onLoad();
            this._issuedMobile = '';
            this.setData({
                mobile: '',
                mobileCode: '',
                sendingCode: false,
                submitting: false,
                disableGetMobileCode: false,
                disableSubmitMobileCode: true,
                getCodeButtonText: '获取验证码',
            });
        }
    },

    /** 验证请求仍属于当前页面和发起请求时的账号. */
    isCurrentSession(session) {
        return (
            !this._unloaded &&
            this._session === session &&
            session.token === (wx.getStorageSync('token') || '')
        );
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
        const formError = !mobile ? '请输入手机号码' :
            (/^1[3-9]\d{9}$/.test(mobile) ? '' : '请输入有效的中国大陆手机号');
        this.setData({formError});
        return !formError;
    },

    /** 仅保留数字并同步手机号输入. */
    bindInputMobile(event) {
        const mobile = String(event.detail.value || '')
            .replace(/[^0-9]/g, '')
            .slice(0, 11);
        this.setData({
            mobile,
            formError: '',
            disableSubmitMobileCode: mobile !== this._issuedMobile,
        });
    },

    /** 同步验证码输入, 切换账号时可以清空输入框. */
    bindInputCode(event) {
        this.setData({mobileCode: event.detail.value, formError: ''});
    },

    /** 请求六位短信验证码并启动页面级倒计时, 失败立即恢复按钮. */
    countDownPassCode() {
        const session = this._session;
        const mobile = this.data.mobile;
        if (
            !this.isCurrentSession(session) ||
            this.data.sendingCode ||
            this.data.disableGetMobileCode ||
            this.data.submitting
        ) {
            return Promise.resolve(false);
        }
        if (!this.bindCheckMobile(mobile)) {
            return Promise.resolve(false);
        }
        this.setData({sendingCode: true, disableGetMobileCode: true, formError: ''});
        return util
            .request(api.SmsCode, {phone: mobile}, 'POST', 'application/json')
            .then((res) => {
                if (!this.isCurrentSession(session)) {
                    return false;
                }
                if (res.code !== 0) {
                    throw new Error(res.msg || '验证码发送失败');
                }
                wx.showToast({title: '发送成功', icon: 'success', duration: 1000});
                this._issuedMobile = mobile;
                this.setData({
                    mobileCode: '',
                    disableSubmitMobileCode: this.data.mobile !== mobile,
                });
                this.startCountdown();
                return true;
            })
            .catch((error) => {
                if (this.isCurrentSession(session)) {
                    this.setData({disableGetMobileCode: false, formError: error.message || '验证码发送失败'});
                }
                return false;
            })
            .finally(() => {
                if (this.isCurrentSession(session)) {
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
        const session = this._session;
        if (
            !this.isCurrentSession(session) ||
            this.data.submitting ||
            this.data.sendingCode ||
            this.data.disableSubmitMobileCode
        ) {
            return Promise.resolve(false);
        }
        const mobile = this.data.mobile;
        if (!this.bindCheckMobile(mobile)) {
            return Promise.resolve(false);
        }
        const code = event && event.detail && event.detail.value ? event.detail.value.code : '';
        if (!/^[0-9]{6}$/.test(String(code || ''))) {
            this.setData({formError: '请输入六位验证码'});
            return Promise.resolve(false);
        }
        const current = () => this.isCurrentSession(session);
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
                    this.setData({formError: error.message || '绑定失败'});
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
