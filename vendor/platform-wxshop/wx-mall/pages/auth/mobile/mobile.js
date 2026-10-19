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
  },

  /** 初始化手机号绑定状态并读取页面所需数据. */
  onLoad: function () {
    const that = this;
    that.setData({userInfo: app.globalData.userInfo});

    if (!app.globalData.token) {
      const token = wx.getStorageSync('userToken');
      if (token) {
        app.globalData.token = token;
      }
    }
  },

  /** 校验国内手机号格式, 不合法时提示并阻止后续提交. */
  bindCheckMobile: function (mobile) {
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

  /** 暂时禁用验证码按钮, 避免重复点击. */
  bindGetPassCode: function (e) {
    const that = this;
    that.setData({disableGetMobileCode: true});
  },

  /** 将手机号输入同步到页面状态. */
  bindInputMobile: function (e) {
    this.setData({
      mobile: e.detail.value,
    });
  },

  /** 请求短信验证码并更新重发倒计时. */
  countDownPassCode: function () {
    if (!this.bindCheckMobile(this.data.mobile)) {
      return;
    }
    util
      .request(api.SmsCode, {phone: this.data.mobile}, 'POST', 'application/json')
      .then(function (res) {
        if (res.code == 0) {
          wx.showToast({
            title: '发送成功',
            icon: 'success',
            duration: 1000,
          });
          const pages = getCurrentPages();
          let i = 60;
          const intervalId = setInterval(function () {
            i--;
            if (i <= 0) {
              pages[pages.length - 1].setData({
                disableGetMobileCode: false,
                disableSubmitMobileCode: false,
                getCodeButtonText: '获取验证码',
              });
              clearInterval(intervalId);
            } else {
              pages[pages.length - 1].setData({
                getCodeButtonText: i,
                disableGetMobileCode: true,
                disableSubmitMobileCode: false,
              });
            }
          }, 1000);
        } else {
          wx.showToast({
            title: res.msg || '发送失败',
            icon: 'none',
            duration: 1000,
          });
        }
      });
  },

  /** 提交当前手机号和验证码完成绑定. */
  bindLoginMobilecode: function (e) {
    const mobile = this.data.mobile;
    if (!this.bindCheckMobile(mobile)) {
      return;
    }
    if (!(e.detail.value.code && e.detail.value.code.length === 4)) {
      return;
    }
    wx.showToast({
      title: '操作中...',
      icon: 'loading',
      duration: 5000,
    });
    util
      .request(
        api.BindMobile,
        {mobileCode: e.detail.value.code, mobile: mobile},
        'POST',
        'application/json'
      )
      .then(function (res) {
        if (res.code === 0) {
          wx.showModal({
            title: '提示',
            content: '操作成功',
            showCancel: false,
          });
          wx.switchTab({
            url: '/pages/ucenter/index/index',
          });
        } else {
          wx.showModal({
            title: '提示',
            content: res.msg || '绑定失败',
            showCancel: false,
          });
        }
      });
  },
});
