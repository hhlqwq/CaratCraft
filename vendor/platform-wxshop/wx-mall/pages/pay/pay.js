const util = require('../../utils/util.js');
const api = require('../../config/api.js');

Page({
  data: {
    orderId: 0,
    actualPrice: 0.0,
  },
  /** 初始化订单支付状态并读取页面所需数据. */
  onLoad(options) {
    this.setData({
      orderId: options.orderId,
      actualPrice: options.actualPrice,
    });
  },

  //向服务请求支付参数
  requestPayParam() {
    const that = this;
    util.request(api.PayPrepayId, {orderId: that.data.orderId, payType: 1}).then(function (res) {
      if (res.code === 0) {
        const payParam = res.data;
        wx.requestPayment({
          timeStamp: payParam.timeStamp,
          nonceStr: payParam.timeStamp,
          package: payParam.nonceStr,
          signType: payParam.signType,
          paySign: payParam.paySign,
          success(res) {
            wx.redirectTo({
              url: '/pages/payResult/payResult?status=true',
            });
          },
          fail(res) {
            wx.redirectTo({
              url: '/pages/payResult/payResult?status=false',
            });
          },
        });
      }
    });
  },
  /** 为当前订单请求微信支付, 结果以服务端状态为准. */
  startPay() {
    this.requestPayParam();
  },
});
