const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

Page({
    data: {
        orderId: 0,
        orderInfo: {},
        legalDocuments: [],
        orderGoods: [],
        handleOption: {},
        customization: null,
        refund: {},
        refundReason: '',
        refundSubmitting: false,
    },
    /** 初始化订单详情状态并读取页面所需数据. */
    onLoad(options) {
        this.setData({
            orderId: options.id,
        });
        this.getOrderDetail();
    },
    /** 读取本人订单、定制快照及可执行的订单操作. */
    getOrderDetail() {
        util.request(api.OrderDetail, {
            orderId: this.data.orderId,
        }).then((res) => {
            if (res.code === 0) {
                this.setData({
                    orderInfo: res.data.orderInfo,
                    legalDocuments: res.data.legalDocuments || [],
                    orderGoods: res.data.orderGoods,
                    handleOption: res.data.handleOption,
                    customization: this.formatCustomization(res.data.customization),
                });
                //that.payTimer();
            }
        });
        util.request(api.RefundStatus, {orderId: this.data.orderId})
            .then((res) => {
                if (res.code === 0) {
                    const names = {
                        PENDING_REVIEW: '待商家审核',
                        APPROVED: '已通过审核, 等待退款',
                        PROCESSING: '退款处理中',
                        RETRY: '退款对账中',
                        SUCCEEDED: '退款已完成',
                        REJECTED: '审核未通过',
                        MANUAL_REVIEW: '需商家处理退款异常',
                    };
                    res.data.label = names[res.data.status] || '';
                    this.setData({refund: res.data});
                }
            })
            .catch(function () {});
    },
    /** 售后理由只随本人订单申请提交. */
    changeRefundReason(event) {
        this.setData({refundReason: event.detail.value});
    },
    /** 申请审核不显示为资金已退回, 防止误导用户. */
    requestRefund() {
        if (this.data.refundSubmitting) {
            return;
        }
        const reason = this.data.refundReason.trim();
        if (!reason) {
            return util.showErrorToast('请填写售后理由');
        }

        this.setData({refundSubmitting: true});
        return util
            .request(
                api.RefundRequest,
                {orderId: this.data.orderId, reason: reason},
                'POST',
                'application/json'
            )
            .then((res) => {
                if (res.code !== 0) {
                    throw new Error(res.msg || '申请失败');
                }
                wx.showToast({title: '申请已提交', icon: 'none'});
                this.getOrderDetail();
            })
            .catch(function (error) {
                util.showErrorToast(error.message || '申请失败');
            })
            .then(() => {
                this.setData({refundSubmitting: false});
            });
    },
    /** 将后端快照转换为订单展示字段 / Formats customization snapshot for display. */
    formatCustomization(customization) {
        if (!customization) {
            return null;
        }
        let engraving;
        let quote = {};
        try {
            engraving = (
                JSON.parse(customization.configurationJson || '{}').engraving || ''
            ).trim();
            quote = JSON.parse(customization.priceBreakdownJson || '{}');
        } catch (error) {
            engraving = '';
        }
        return {
            previewUrl: customization.previewUrl || '/static/images/logo.png',
            status: customization.designStatus || customization.status || '已保存',
            engraving: engraving || '未填写',
            arrangement: quote.arrangement || [],
            components: quote.components || [],
            sizing: quote.sizing || null,
            lengthText:
                quote.sizing && quote.sizing.estimatedFinishedLengthMm != null
                    ? quote.sizing.estimatedFinishedLengthMm + 'mm (下单冻结工艺估算)'
                    : quote.lengthComplete
                      ? quote.estimatedLengthMm + 'mm (历史材料估算, 成品以商家确认为准)'
                      : '历史尺寸未配置完整',
        };
    },
    /** 更新待支付剩余时间, 超时后停止倒计时并刷新状态. */
    payTimer() {
        const orderInfo = this.data.orderInfo;

        setInterval(() => {
            orderInfo.addTime -= 1;
            this.setData({
                orderInfo: orderInfo,
            });
        }, 1000);
    },
    /** 确认后申请取消当前订单并刷新订单状态. */
    cancelOrder() {
        const orderInfo = this.data.orderInfo;

        const orderStatus = orderInfo.orderStatus;

        let errorMessage = '';
        switch (orderStatus) {
            case 300: {
                errorMessage = '订单已发货';
                break;
            }
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
            content: '确定要取消此订单？',
            success(res) {
                if (res.confirm) {
                    util.request(api.OrderCancel, {
                        orderId: orderInfo.id,
                    }).then(function (res) {
                        if (res.code === 0) {
                            wx.showModal({
                                title: '提示',
                                content: res.data,
                                showCancel: false,
                                confirmText: '继续',
                                success(res) {
                                    //  util.redirect('/pages/ucenter/order/order');
                                    wx.navigateBack({
                                        url: 'pages/ucenter/order/order',
                                    });
                                },
                            });
                        }
                    });
                }
            },
        });
    },
    /** 发起当前订单的微信支付并处理结果提示. */
    payOrder() {
        util.request(api.PayPrepayId, {
            orderId: this.data.orderId || 15,
        }).then(function (res) {
            if (res.code === 0) {
                const payParam = res.data;
                wx.requestPayment({
                    timeStamp: payParam.timeStamp,
                    nonceStr: payParam.nonceStr,
                    package: payParam.package,
                    signType: payParam.signType,
                    paySign: payParam.paySign,
                    success(res) {
                        console.log(res);
                    },
                    fail(res) {
                        console.log(res);
                    },
                });
            }
        });
    },
    /** 确认本人订单已收货并刷新订单详情. */
    confirmOrder() {
        //确认收货

        const orderInfo = this.data.orderInfo;

        const orderStatus = orderInfo.orderStatus;

        let errorMessage = '';
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
            success(res) {
                if (res.confirm) {
                    util.request(api.OrderConfirm, {
                        orderId: orderInfo.id,
                    }).then(function (res) {
                        if (res.code === 0) {
                            wx.showModal({
                                title: '提示',
                                content: res.data,
                                showCancel: false,
                                confirmText: '继续',
                                success(res) {
                                    //  util.redirect('/pages/ucenter/order/order');
                                    wx.navigateBack({
                                        url: 'pages/ucenter/order/order',
                                    });
                                },
                            });
                        }
                    });
                }
            },
        });
    },
});
