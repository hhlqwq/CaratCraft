App({
  /** 在支持更新机制的微信客户端提示升级, 用户确认后应用新版. */
  onLaunch: function () {
    if (wx.canIUse('getUpdateManager')) {
      const updateManager = wx.getUpdateManager();
      updateManager.onCheckForUpdate(function (res) {
        // 请求完新版本信息的回调
        if (res.hasUpdate) {
          updateManager.onUpdateReady(function () {
            wx.showModal({
              title: '更新提示',
              content: '新版本已经准备好，是否重启应用？',
              success: function (res) {
                if (res.confirm) {
                  // 新的版本已经下载好，调用 applyUpdate 应用新版本并重启
                  updateManager.applyUpdate();
                }
              },
            });
          });
          updateManager.onUpdateFailed(function () {
            // 新的版本下载失败
            wx.showModal({
              title: '已经有新版本了哟~',
              content: '新版本已经上线啦~，请您删除当前小程序，重新搜索打开哟~',
            });
          });
        }
      });
    } else {
      // 如果希望用户在最新版本的客户端上体验您的小程序，可以这样子提示
      wx.showModal({
        title: '提示',
        content: '当前微信版本过低，无法更好体验程序，请升级到最新微信版本后重试。',
      });
    }
  },
  globalData: {
    userInfo: {
      nickname: 'Hi,游客',
      userName: '点击去登录',
      avatar: '/static/images/defaultAvatar.png',
    },
    token: '',
    userCoupon: 'NO_USE_COUPON', //默认不适用优惠券
    selectedCoupon: {}, // 当前结算选择的优惠券信息.
  },
  /** 结束应用级刷新提示, 页面数据加载由各页面负责. */
  onPullDownRefresh: function () {
    // 显示顶部刷新图标
    wx.showNavigationBarLoading();
    // 隐藏导航栏加载框
    wx.hideNavigationBarLoading();
    // 停止下拉动作
    wx.stopPullDownRefresh();
  },
});
