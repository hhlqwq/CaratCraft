const util = require('../utils/util.js');
const api = require('../config/api.js');

/** 用户主动确认协议后交换登录会话, 仅在完整成功响应后保存会话. */
async function loginByWeixin(ignoredProfile, legalAcceptance) {
    if (!legalAcceptance || !legalAcceptance.termsVersion || !legalAcceptance.privacyVersion) {
        throw new Error('请先阅读并主动同意用户协议及隐私政策');
    }
    const login = await util.login();
    if (!login.code) {
        throw new Error('微信登录凭证为空');
    }
    const response = await util.request(
        api.AuthLoginByWeixin,
        {code: login.code, legalAcceptance},
        'POST',
        'application/json'
    );
    if (response.code !== 0 || !response.data || !response.data.token) {
        throw new Error(response.msg || '登录失败');
    }
    util.clearSession();
    wx.setStorageSync('userInfo', response.data.userInfo);
    wx.setStorageSync('token', response.data.token);
    wx.setStorageSync('userId', response.data.userId);
    const app = typeof getApp === 'function' ? getApp() : null;
    if (app && app.globalData) {
        app.globalData.token = response.data.token;
        app.globalData.userInfo = response.data.userInfo;
        app.globalData.userId = response.data.userId;
    }
    return response;
}

/** 只有本地资料和令牌均存在时才核对微信会话有效性. */
async function checkLogin() {
    if (!wx.getStorageSync('userInfo') || !wx.getStorageSync('token')) {
        throw new Error('请先登录');
    }
    await util.checkSession();
    return true;
}

module.exports = {loginByWeixin, checkLogin};
