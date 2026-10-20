const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

// pages/ucenter/help/help.js
Page({
  data: {
    helpList: [],
  },
  /** 读取当前帮助页面所需的帮助信息. */
  getHelpList(id) {
    const that = this;
    util.request(api.HelpIssueList, {typeId: id}).then(function (res) {
      if (res.code === 0) {
        that.setData({
          helpList: res.data,
        });
      }
    });
  },
  /** 初始化帮助详情状态并读取页面所需数据. */
  onLoad(options) {
    this.getHelpList(options.id);
  },
});
