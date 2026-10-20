const util = require('../../utils/util.js');
const api = require('../../config/api.js');
const pay = require('../../services/pay.js');

Page({
  data: {
    status: false,
    orderId: 0,
  },
  /** 初始化支付结果状态并读取页面所需数据. */
  onLoad(options) {
    this.setData({
      orderId: options.orderId || 24,
      status: options.status,
    });
    this.updateSuccess();
  },

  /** 查询服务端支付状态并更新支付结果展示. */
  updateSuccess() {
    util.request(api.OrderQuery, {orderId: this.data.orderId}).then(function (res) {});
  },

  /** 发起当前订单的微信支付并处理结果提示. */
  payOrder() {
    pay
      .payOrder(parseInt(this.data.orderId))
      .then((res) => {
        this.setData({
          status: true,
        });
      })
      .catch((res) => {
        util.showErrorToast('支付失败');
      });
  },
});
