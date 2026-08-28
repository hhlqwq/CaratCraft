const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

//获取应用实例
const app = getApp()
Page({
  data: {
    canIUseGetUserProfile: false,
    navUrl: '',
    code: ''
  },

  onLoad: function (options) {
    let that = this;
    if (wx.getUserProfile) {
      this.setData({
        canIUseGetUserProfile: true
      })
    }
    if (wx.getStorageSync("navUrl")) {
      that.setData({
        navUrl: wx.getStorageSync("navUrl")
      })
    } else {
      that.setData({
        navUrl: '/pages/index/index'
      })
    }

    wx.login({
      success: function (res) {
        if (res.code) {
          that.setData({
            code: res.code
          })
        }
      }
    });
  },
  getUserProfile() {
    // 推荐使用wx.getUserProfile获取用户信息，开发者每次通过该接口获取用户个人信息均需用户确认
    // 开发者妥善保管用户快速填写的头像昵称，避免重复弹窗
    wx.getUserProfile({
      desc: '用于完善会员资料', // 声明获取用户个人信息后的用途，后续会展示在弹窗中，请谨慎填写
      success: (resp) => {
        //登录远程服务器
        this.loginByWeixin(resp)
      }
    })
  },
  bindGetUserInfo: function (e) {
    //登录远程服务器
    this.loginByWeixin(e.detail)
  },
  loginByWeixin: function (userInfo) {
    let that = this;
    util.login().then(loginResult => {
      if (!loginResult.code) {
        throw new Error('微信登录凭证为空');
      }
      util.request(api.AuthLoginByWeixin, {
        code: loginResult.code,
        userInfo: userInfo
      }, 'POST', 'application/json').then(res => {
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
            showCancel: false
          });
        }
      }).catch(() => {
        wx.showModal({
          title: '提示',
          content: '登录请求失败，请检查网络后重试',
          showCancel: false
        });
      });
    }).catch(() => {
      wx.showModal({
        title: '提示',
        content: '获取微信登录凭证失败，请重试',
        showCancel: false
      });
    });
  },
  /**
   * 登录成功后返回用户原本要访问的页面, Navigate only after successful login.
   */
  navigateAfterLogin: function () {
    let navUrl = this.data.navUrl;
    if (navUrl && navUrl == '/pages/index/index') {
      wx.switchTab({
        url: navUrl,
      })
    } else if (navUrl) {
      wx.redirectTo({
        url: navUrl,
      })
    }
  },
  onReady: function () {
    // 页面渲染完成
  },
  onShow: function () {
    // 页面显示
  },
  onHide: function () {
    // 页面隐藏
  },
  onUnload: function () {
    // 页面关闭
  }
})
