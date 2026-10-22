const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

Page({
    data: {
        orderList: [],
        page: 1,
        size: 10,
        loadmoreText: '正在加载更多数据',
        nomoreText: '全部加载完成',
        nomore: false,
        totalPages: 1,
    },
    /** 初始化个人订单状态并读取页面所需数据. */
    onLoad(options) {
        wx.showLoading({
            title: '加载中...',
            success() {},
        });
        this.getOrderList();
    },

    /**
     * 页面上拉触底事件的处理函数
     */
    onReachBottom() {
        this.getOrderList();
    },

    /** 按当前订单分类读取本人订单列表. */
    getOrderList() {
        if (this.data.totalPages <= this.data.page - 1) {
            this.setData({
                nomore: true,
            });
            return;
        }

        util.request(api.OrderList, {page: this.data.page, size: this.data.size}).then((res) => {
            if (res.code === 0) {
                this.setData({
                    orderList: this.data.orderList.concat(res.data.list),
                    page: res.data.page + 1,
                    totalPages: res.data.total,
                });
                wx.hideLoading();
            }
        });
    },
    /** 发起当前订单的微信支付并处理结果提示. */
    payOrder(event) {
        const orderIndex = event.currentTarget.dataset.orderIndex;
        const order = this.data.orderList[orderIndex];
        wx.redirectTo({
            url: '/pages/pay/pay?orderId=' + order.id + '&actualPrice=' + order.actualPrice,
        });
    },
});
