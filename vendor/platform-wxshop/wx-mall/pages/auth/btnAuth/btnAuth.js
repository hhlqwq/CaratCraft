const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

//获取应用实例

Page({
  data: {
    canIUseGetUserProfile: false,
    navUrl: '',
    code: '',
  },

  /** 初始化登录授权状态并读取页面所需数据. */
  onLoad: function (options) {
    const that = this;
    if (wx.getUserProfile) {
      this.setData({
        canIUseGetUserProfile: true,
      });
    }
    if (wx.getStorageSync('navUrl')) {
      that.setData({
        navUrl: wx.getStorageSync('navUrl'),
      });
    } else {
      that.setData({
        navUrl: '/pages/index/index',
      });
    }

    wx.login({
      success: function (res) {
        if (res.code) {
          that.setData({
            code: res.code,
          });
        }
      },
    });
  },
  /** 请求用户主动授权的资料并交给登录流程处理. */
  getUserProfile() {
    // 用户主动触发资料授权; 登录会话仍由后端验证微信临时凭证.
    // 开发者妥善保管用户快速填写的头像昵称，避免重复弹窗
    wx.getUserProfile({
      desc: '用于完善会员资料', // 声明获取用户个人信息后的用途，后续会展示在弹窗中，请谨慎填写
      success: (resp) => {
        //登录远程服务器
        this.loginByWeixin(resp);
      },
    });
  },
  /** 处理资料授权回调, 拒绝授权时不继续提交资料. */
  bindGetUserInfo: function (e) {
    //登录远程服务器
    this.loginByWeixin(e.detail);
  },
  /** 使用微信临时登录凭证换取本项目会话并恢复用户信息. */
  loginByWeixin: function (userInfo) {
    const that = this;
    util
      .login()
      .then((loginResult) => {
        if (!loginResult.code) {
          throw new Error('微信登录凭证为空');
        }
        util
          .request(
            api.AuthLoginByWeixin,
            {
              code: loginResult.code,
              userInfo: userInfo,
            },
            'POST',
            'application/json'
          )
          .then((res) => {
            if (res.code === 0) {
              // 登录成功后存储用户信息, Store user information after login succeeds.
              wx.setStorageSync('userInfo', res.data.userInfo);
              wx.setStorageSync('token', res.data.token);
              wx.setStorageSync('userId', res.data.userId);
              that.navigateAfterLogin();
            } else {
              wx.showModal({
                title: '提示',
                content: res.msg,
                showCancel: false,
              });
            }
          })
          .catch(() => {
            wx.showModal({
              title: '提示',
              content: '登录请求失败，请检查网络后重试',
              showCancel: false,
            });
          });
      })
      .catch(() => {
        wx.showModal({
          title: '提示',
          content: '获取微信登录凭证失败，请重试',
          showCancel: false,
        });
      });
  },
  /**
   * 登录成功后返回用户原本要访问的页面, Navigate only after successful login.
   */
  navigateAfterLogin: function () {
    const navUrl = this.data.navUrl;
    if (navUrl && navUrl == '/pages/index/index') {
      wx.switchTab({
        url: navUrl,
      });
    } else if (navUrl) {
      wx.redirectTo({
        url: navUrl,
      });
    }
  },
});
