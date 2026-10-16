var util = require('../../../utils/util.js');
var api = require('../../../config/api.js');

Page({
  data: {
    orderId: 0,
    orderInfo: {},
    orderGoods: [],
    handleOption: {},
    customization: null,
    refund: {},
    refundReason: '',
    refundSubmitting: false
  },
  onLoad: function (options) {
    // 页面初始化 options为页面跳转所带来的参数
    this.setData({
      orderId: options.id
    });
    this.getOrderDetail();
  },
  getOrderDetail() {
    let that = this;
    util.request(api.OrderDetail, {
      orderId: that.data.orderId
    }).then(function (res) {
      if (res.code === 0) {
        that.setData({
          orderInfo: res.data.orderInfo,
          orderGoods: res.data.orderGoods,
          handleOption: res.data.handleOption,
          customization: that.formatCustomization(res.data.customization)
        });
        //that.payTimer();
      }
    });
    util.request(api.RefundStatus, {orderId: this.data.orderId}).then(function (res) {
      if (res.code === 0) {
        var names = {PENDING_REVIEW: '待商家审核', APPROVED: '已通过审核, 等待退款', PROCESSING: '退款处理中', RETRY: '退款对账中', SUCCEEDED: '退款已完成', REJECTED: '审核未通过', MANUAL_REVIEW: '需商家处理退款异常'};
        res.data.label = names[res.data.status] || '';
        that.setData({refund: res.data});
      }
    }).catch(function () {});
  },
  /** 售后理由只随本人订单申请提交. */
  changeRefundReason: function (event) {
    this.setData({refundReason: event.detail.value});
  },
  /** 申请审核不显示为资金已退回, 防止误导用户. */
  requestRefund: function () {
    if (this.data.refundSubmitting) return;
    var reason = this.data.refundReason.trim();
    if (!reason) return util.showErrorToast('请填写售后理由');
    var that = this;
    this.setData({refundSubmitting: true});
    return util.request(api.RefundRequest, {orderId: this.data.orderId, reason: reason}, 'POST', 'application/json')
      .then(function (res) {
        if (res.code !== 0) throw new Error(res.msg || '申请失败');
        wx.showToast({title: '申请已提交', icon: 'none'});
        that.getOrderDetail();
      }).catch(function (error) { util.showErrorToast(error.message || '申请失败'); })
      .then(function () { that.setData({refundSubmitting: false}); });
  },
  /** 将后端快照转换为订单展示字段 / Formats customization snapshot for display. */
  formatCustomization: function (customization) {
    if (!customization) return null;
    var engraving = '';
    try {
      engraving = (JSON.parse(customization.configurationJson || '{}').engraving || '').trim();
    } catch (error) {
      engraving = '';
    }
    return {
      previewUrl: customization.previewUrl || '/static/images/logo.png',
      status: customization.designStatus || customization.status || '已保存',
      engraving: engraving || '未填写'
    };
  },
  payTimer() {
    let that = this;
    let orderInfo = that.data.orderInfo;

    setInterval(() => {
      orderInfo.addTime -= 1;
      that.setData({
        orderInfo: orderInfo,
      });
    }, 1000);
  },
  cancelOrder(){
    let that = this;
    let orderInfo = that.data.orderInfo;

    var orderStatus = orderInfo.orderStatus;

    var errorMessage = '';
    switch (orderStatus){
      case 300: {
        errorMessage = '订单已发货';
        break;
      }
      case 301:{
        errorMessage = '订单已收货';
        break;
      }
      case 101:{
        errorMessage = '订单已取消';
        break;
      }
      case 102: {
        errorMessage = '订单已删除';
        break;
      }
      case 401: {
        errorMessage = '订单已退款';
        break;
      }
      case 402: {
        errorMessage = '订单已退货';
        break;
      }
    }

    if (errorMessage != '') {
      util.showErrorToast(errorMessage);
      return false;
    }

    wx.showModal({
      title: '',
      content: '确定要取消此订单？',
      success: function (res) {
        if (res.confirm) {

          util.request(api.OrderCancel,{
            orderId: orderInfo.id
          }).then(function (res) {
            if (res.code === 0) {
              wx.showModal({
                title:'提示',
                content: res.data,
                showCancel:false,
                confirmText:'继续',
                success: function (res) {
                //  util.redirect('/pages/ucenter/order/order');
                  wx.navigateBack({
                    url: 'pages/ucenter/order/order',
                  });
                }
              });
            }
          });

        }
      }
    });
  },
  payOrder() {
    let that = this;
    util.request(api.PayPrepayId, {
      orderId: that.data.orderId || 15
    }).then(function (res) {
      if (res.code === 0) {
        const payParam = res.data;
        wx.requestPayment({
          'timeStamp': payParam.timeStamp,
          'nonceStr': payParam.nonceStr,
          'package': payParam.package,
          'signType': payParam.signType,
          'paySign': payParam.paySign,
          'success': function (res) {
            console.log(res);
          },
          'fail': function (res) {
            console.log(res);
          }
        });
      }
    });

  },
  confirmOrder() {
//确认收货
      let that = this;
      let orderInfo = that.data.orderInfo;

      var orderStatus = orderInfo.orderStatus;

      var errorMessage = '';
      switch (orderStatus) {
          // case 300: {
          //   errorMessage = '订单已发货';
          //   break;
          // }
          case 301: {
              errorMessage = '订单已收货';
              break;
          }
          case 101: {
              errorMessage = '订单已取消';
              break;
          }
          case 102: {
              errorMessage = '订单已删除';
              break;
          }
          case 401: {
              errorMessage = '订单已退款';
              break;
          }
          case 402: {
              errorMessage = '订单已退货';
              break;
          }
      }

      if (errorMessage != '') {
          util.showErrorToast(errorMessage);
          return false;
      }

      wx.showModal({
          title: '',
          content: '确定已经收到商品？',
          success: function (res) {
              if (res.confirm) {

                  util.request(api.OrderConfirm, {
                      orderId: orderInfo.id
                  }).then(function (res) {
                      if (res.code === 0) {
                          wx.showModal({
                              title: '提示',
                              content: res.data,
                              showCancel: false,
                              confirmText: '继续',
                              success: function (res) {
                                  //  util.redirect('/pages/ucenter/order/order');
                                  wx.navigateBack({
                                      url: 'pages/ucenter/order/order',
                                  });
                              }
                          });
                      }
                  });

              }
          }
      });
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
