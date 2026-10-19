const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

Page({
  data: {
    array: [
      '请选择反馈类型',
      '商品相关',
      '物流状况',
      '客户服务',
      '优惠活动',
      '功能异常',
      '产品建议',
      '其他',
    ],
    index: 0,
    content: '',
    contentLength: 0,
    mobile: '',
  },
  /** 同步当前选择的反馈类型. */
  bindPickerChange: function (e) {
    this.setData({
      index: e.detail.value,
    });
  },
  /** 同步反馈联系手机号输入. */
  mobileInput: function (e) {
    this.setData({
      mobile: e.detail.value,
    });
  },
  /** 同步反馈正文输入. */
  contentInput: function (e) {
    this.setData({
      contentLength: e.detail.cursor,
      content: e.detail.value,
    });
  },
  /** 清空反馈联系手机号. */
  cleanMobile: function () {},
  /** 校验反馈内容和联系方式后提交反馈. */
  sbmitFeedback: function (e) {
    const that = this;
    if (that.data.index == 0) {
      util.showErrorToast('请选择反馈类型');
      return false;
    }

    if (that.data.content == '') {
      util.showErrorToast('请输入反馈内容');
      return false;
    }

    if (that.data.mobile == '') {
      util.showErrorToast('请输入手机号码');
      return false;
    }
    wx.showLoading({
      title: '提交中...',
      mask: true,
      success: function () {},
    });

    util
      .request(
        api.FeedbackAdd,
        {mobile: that.data.mobile, index: that.data.index, content: that.data.content},
        'POST',
        'application/json'
      )
      .then(function (res) {
        if (res.code === 0) {
          wx.hideLoading();

          wx.showToast({
            title: res.data,
            icon: 'success',
            duration: 2000,
            complete: function () {
              that.setData({
                index: 0,
                content: '',
                contentLength: 0,
                mobile: '',
              });
            },
          });
        } else {
          util.showErrorToast(res.msg);
        }
      });
  },
});
