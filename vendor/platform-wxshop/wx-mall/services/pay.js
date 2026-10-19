/** 微信支付服务, 预下单和支付失败均向调用方传播. */
const util = require('../utils/util.js');
const api = require('../config/api.js');

/** 获取预支付参数并支付, 客户端成功后再查询服务端结果. */
function payOrder(orderId) {
  return util
    .request(api.PayPrepayId, {orderId: orderId})
    .then(function (res) {
      if (res.code !== 0) {
        throw new Error(res.msg || '预支付失败');
      }
      return new Promise(function (resolve, reject) {
        wx.requestPayment(
          Object.assign({}, res.data, {
            success: resolve,
            fail: reject,
          })
        );
      });
    })
    .then(function () {
      return util.request(api.OrderQuery, {orderId: orderId});
    })
    .then(function (result) {
      if (result.code !== 0) {
        throw new Error(result.msg || '支付结果待确认, 请在订单中查询');
      }
      return result;
    });
}

module.exports = {payOrder};
