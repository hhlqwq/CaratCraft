var util = require('../../../utils/util.js');
var user = require('../../../services/user.js');
var api = require('../../../config/api.js');
var app = getApp();
var DEFAULT_AVATAR = '/static/images/logo.png';

Page({
  data: {
    userInfo: {},
    hasMobile: '',
    avatarUrl: DEFAULT_AVATAR,
    nicknameText: 'Hi, 游客',
    profileVisible: false,
    profileForm: {
      nickname: '',
      avatarUrl: ''
    },
    profileSaving: false,
    menuItems: [
      { title: '我的订单', url: '/pages/ucenter/order/order', badge: '单' },
      { title: '我的设计', url: '/pages/ucenter/design/design', badge: '设' },
      { title: '优惠券', url: '/pages/ucenter/coupon/coupon', badge: '券' },
      { title: '我的收藏', url: '/pages/ucenter/collect/collect', badge: '藏' },
      { title: '我的足迹', url: '/pages/ucenter/footprint/footprint', badge: '迹' },
      { title: '地址管理', url: '/pages/ucenter/address/address', badge: '址' },
      { title: '帮助中心', url: '/pages/ucenter/help/help', badge: '助' },
      { title: '意见反馈', url: '/pages/ucenter/feedback/feedback', badge: '评' }
    ]
  },
  onLoad: function (options) {
    // 页面初始化 options为页面跳转所带来的参数

  },
  onReady: function () {

  },
  onShow: function () {

    let userInfo = wx.getStorageSync('userInfo');
    let token = wx.getStorageSync('token');

    // 页面显示
    if (userInfo && token) {
      app.globalData.userInfo = this.normalizeUserInfo(userInfo);
      app.globalData.token = token;
    }

    this.refreshUserInfo();

  },
  onHide: function () {
    // 页面隐藏

  },
  onUnload: function () {
    // 页面关闭
  },
  getUserProfile() {
    let userInfo = wx.getStorageSync('userInfo');
    let token = wx.getStorageSync('token');
    if (userInfo && token) {
      app.globalData.userInfo = this.normalizeUserInfo(userInfo);
      app.globalData.token = token;
      this.openProfileEditor();
      return;
    }
    // 推荐使用wx.getUserProfile获取用户信息，开发者每次通过该接口获取用户个人信息均需用户确认
    wx.getUserProfile({
      desc: '用于完善会员资料', // 声明获取用户个人信息后的用途，后续会展示在弹窗中，请谨慎填写
      success: (resp) => {
        //登录远程服务器
        user.loginByWeixin(resp).then(res => {
          const userInfo = this.normalizeUserInfo(res.data.userInfo);
          this.applyUserInfo(userInfo);
          app.globalData.userInfo = userInfo;
          app.globalData.token = res.data.token;
        }).catch((err) => {
          console.log(err)
        });
      }
    })
  },
  bindGetUserInfo(e) {
    let userInfo = wx.getStorageSync('userInfo');
    let token = wx.getStorageSync('token');
    if (userInfo && token) {
      return;
    }
    //用户按了允许授权按钮
    user.loginByWeixin(e.detail).then(res => {
      const userInfo = this.normalizeUserInfo(res.data.userInfo);
      this.applyUserInfo(userInfo);
      app.globalData.userInfo = userInfo;
      app.globalData.token = res.data.token;
    }).catch((err) => {
      console.log(err)
    });
  },
  exitLogin: function () {
    wx.showModal({
      title: '',
      confirmColor: '#b4282d',
      content: '退出登录？',
      success: function (res) {
        if (res.confirm) {
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          app.globalData.userInfo = {
            nickname: 'Hi, 游客',
            userName: '点击去登录',
            avatar: DEFAULT_AVATAR
          };
          util.showSuccessToast('退出成功');
          wx.switchTab({
            url: '/pages/index/index'
          });
        }
      }
    })
  },
  refreshUserInfo: function () {
    var userInfo = this.normalizeUserInfo(app.globalData.userInfo || {});
    this.setData({
      userInfo: userInfo,
      avatarUrl: userInfo.avatar || DEFAULT_AVATAR,
      nicknameText: userInfo.nickname || 'Hi, 游客'
    });
    if (app.globalData.token || wx.getStorageSync('token')) {
      util.request(api.UserProfile).then((res) => {
        if (res.code === 0 && res.data) {
          this.applyUserInfo(this.normalizeUserInfo(res.data));
        }
      }).catch(() => {});
    }
  },
  // 统一服务端与小程序头像字段 / Normalizes server and mini-program avatar fields.
  normalizeUserInfo: function (userInfo) {
    const normalized = Object.assign({}, userInfo || {});
    normalized.avatar = normalized.headImgUrl || normalized.avatar || DEFAULT_AVATAR;
    normalized.headImgUrl = normalized.avatar;
    return normalized;
  },
  // 应用并持久化用户资料 / Applies and persists the user profile.
  applyUserInfo: function (userInfo) {
    const normalized = this.normalizeUserInfo(userInfo);
    app.globalData.userInfo = normalized;
    wx.setStorageSync('userInfo', normalized);
    this.setData({
      userInfo: normalized,
      avatarUrl: normalized.avatar,
      nicknameText: normalized.nickname || 'Hi, 游客'
    });
  },
  // 打开资料编辑层 / Opens the profile editor.
  openProfileEditor: function () {
    const userInfo = this.normalizeUserInfo(app.globalData.userInfo || {});
    this.setData({
      profileVisible: true,
      profileForm: {
        nickname: userInfo.nickname || '',
        avatarUrl: userInfo.avatar || DEFAULT_AVATAR
      }
    });
  },
  // 关闭资料编辑层 / Closes the profile editor.
  closeProfileEditor: function () {
    if (!this.data.profileSaving) {
      this.setData({profileVisible: false});
    }
  },
  // 接收微信头像选择结果 / Receives the avatar selected from WeChat.
  chooseAvatar: function (event) {
    this.setData({'profileForm.avatarUrl': event.detail.avatarUrl});
  },
  // 接收昵称输入 / Receives nickname input.
  inputNickname: function (event) {
    this.setData({'profileForm.nickname': event.detail.value});
  },
  // 经用户确认后同步微信昵称和头像 / Syncs WeChat profile after explicit user consent.
  syncWeChatProfile: function () {
    wx.getUserProfile({
      desc: '用于设置商城昵称和头像',
      success: (profile) => {
        this.setData({profileSaving: true});
        user.loginByWeixin(profile).then((res) => {
          const userInfo = this.normalizeUserInfo(res.data.userInfo);
          app.globalData.token = res.data.token;
          this.applyUserInfo(userInfo);
          this.setData({
            profileForm: {
              nickname: userInfo.nickname || '',
              avatarUrl: userInfo.avatar || DEFAULT_AVATAR
            }
          });
          wx.showToast({title: '已同步微信资料', icon: 'success'});
        }).catch(() => {
          wx.showToast({title: '微信资料同步失败', icon: 'none'});
        }).finally(() => {
          this.setData({profileSaving: false});
        });
      }
    });
  },
  // 保存昵称和头像 / Saves nickname and avatar.
  saveProfile: function () {
    const nickname = (this.data.profileForm.nickname || '').trim();
    if (!nickname || nickname.length > 32) {
      wx.showToast({title: '昵称应为1到32个字符', icon: 'none'});
      return;
    }
    this.setData({profileSaving: true});
    this.uploadAvatarIfNeeded(this.data.profileForm.avatarUrl)
      .then((avatarUrl) => util.request(api.UpdateUserProfile, {
        nickname: nickname,
        avatarUrl: avatarUrl
      }, 'POST', 'application/json'))
      .then((res) => {
        if (res.code !== 0) {
          throw new Error(res.msg || '保存失败');
        }
        this.applyUserInfo(this.normalizeUserInfo(res.data));
        this.setData({profileVisible: false});
        wx.showToast({title: '资料已保存', icon: 'success'});
      }).catch((error) => {
        wx.showToast({title: error.message || '资料保存失败', icon: 'none'});
      }).finally(() => {
        this.setData({profileSaving: false});
      });
  },
  // 仅上传本地选择的新头像 / Uploads only a newly selected local avatar.
  uploadAvatarIfNeeded: function (avatarUrl) {
    if (!avatarUrl || /^https?:\/\//.test(avatarUrl) || avatarUrl.indexOf('/static/') === 0 || avatarUrl.indexOf('/upload/') === 0) {
      return Promise.resolve(avatarUrl);
    }
    return new Promise((resolve, reject) => {
      wx.uploadFile({
        url: api.UploadFile,
        filePath: avatarUrl,
        name: 'file',
        header: {'token': wx.getStorageSync('token')},
        success: (response) => {
          try {
            const result = JSON.parse(response.data);
            if (response.statusCode === 200 && result.code === 0 && result.data) {
              resolve(util.normalizeAssetUrls(result).data);
              return;
            }
            reject(new Error(result.msg || '头像上传失败'));
          } catch (error) {
            reject(new Error('头像上传响应异常'));
          }
        },
        fail: () => reject(new Error('头像上传失败'))
      });
    });
  }
})
