const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

Page({
    data: {
        typeId: 0,
        collectList: [],
    },
    /** 读取本人收藏的商品列表. */
    getCollectList() {
        util.request(api.CollectList, {typeId: this.data.typeId}).then((res) => {
            if (res.code === 0) {
                this.setData({
                    collectList: res.data,
                });
            }
        });
    },

    /** 页面再次展示时刷新商品收藏状态. */
    onShow() {
        this.getCollectList();
    },

    /** 进入所选收藏商品的详情页面. */
    openGoods(event) {
        const goodsId = this.data.collectList[event.currentTarget.dataset.index].valueId;

        //触摸时间距离页面打开的毫秒数
        const touchTime = this.data.touch_end - this.data.touch_start;
        //如果按下时间大于350为长按
        if (touchTime > 350) {
            wx.showModal({
                title: '',
                content: '确定删除收藏吗？',
                success: (res) => {
                    if (res.confirm) {
                        util.request(
                            api.CollectAddOrDelete,
                            {typeId: this.data.typeId, valueId: goodsId},
                            'POST',
                            'application/json'
                        ).then((res) => {
                            if (res.code === 0) {
                                wx.showToast({
                                    title: '删除成功',
                                    icon: 'success',
                                    duration: 2000,
                                });
                                this.getCollectList();
                            }
                        });
                    }
                },
            });
        } else {
            wx.navigateTo({
                url: '/pages/goods/goods?id=' + goodsId,
            });
        }
    },
    //按下事件开始
    touchStart(e) {
        this.setData({
            touch_start: e.timeStamp,
        });
    },
    //按下事件结束
    touchEnd(e) {
        this.setData({
            touch_end: e.timeStamp,
        });
    },
});
