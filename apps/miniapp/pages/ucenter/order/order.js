const api = require('../../../config/api.js');
const {createPagedFeed} = require('../../../services/paged-feed.js');

Page({
    ...createPagedFeed({
        url: api.OrderList,
        listField: 'orderList',
        responseField: 'records',
        refreshOnShow: true,
    }),
    /** 只从当前已读取的可支付订单进入支付页, 金额由支付页重新核对. */
    payOrder(event) {
        const index = Number(event.currentTarget.dataset.orderIndex);
        const order = Number.isSafeInteger(index) && this.data.orderList[index];
        if (this.data.loading || this.data.error || !order || !order.handleOption?.pay) {
            return;
        }
        wx.navigateTo({url: '/pages/pay/pay?orderId=' + order.id});
    },
});
