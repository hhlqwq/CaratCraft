const user = require('../../../services/user.js');

Page({
  data: {navUrl: '', legalAcceptance: null, saving: false},
  /** 只初始化能力和返回位置, 用户主动授权前不请求微信登录凭证. */
  onLoad() {
    this.setData({
      navUrl: wx.getStorageSync('navUrl') || '/pages/index/index',
    });
  },
  /** 接收本次主动勾选, 取消勾选立即撤销本次登录的协议参数. */
  consentChanged(event) {
    this.setData({legalAcceptance: event.detail});
  },
  /** 先确认协议, 再由用户手势请求身份登录, 昵称头像另行自愿填写. */
  confirmLogin() {
    if (!this.data.legalAcceptance || this.data.saving) {
      return;
    }
    this.loginByWeixin();
  },
  /** 共用登录服务, 失败只显示一条提示并保留协议查看入口. */
  async loginByWeixin(profile) {
    if (!this.data.legalAcceptance || this.data.saving) {
      return;
    }
    this.setData({saving: true});
    try {
      await user.loginByWeixin(profile, this.data.legalAcceptance);
      this.navigateAfterLogin();
    } catch (error) {
      wx.showModal({
        title: '登录提示',
        content: error.message || error.msg || '登录失败, 请重试',
        showCancel: false,
      });
    } finally {
      this.setData({saving: false});
    }
  },
  /** 根据实际页签区分导航方式, 防止登录后跳转到旧购物车页签失败. */
  navigateAfterLogin() {
    const url = this.data.navUrl;
    const tabs = [
      '/pages/index/index',
      '/pages/designer/designer',
      '/pages/community/index',
      '/pages/ucenter/index/index',
    ];
    if (tabs.includes(url)) {
      wx.switchTab({url});
    } else {
      wx.redirectTo({url});
    }
  },
});
