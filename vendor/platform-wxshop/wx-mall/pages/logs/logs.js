//logs.js
const util = require('../../utils/util.js');
Page({
  data: {
    logs: [],
  },
  /** 初始化本地访问日志状态并读取页面所需数据. */
  onLoad: function () {
    this.setData({
      logs: (wx.getStorageSync('logs') || []).map(function (log) {
        return util.formatTime(new Date(log));
      }),
    });
  },
});
