const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

Page({
    data: {
        footprintList: [],
    },
    /** 下拉时重新加载页面数据并结束刷新提示. */
    onPullDownRefresh() {
        // 增加下拉刷新数据的功能
        this.getFootprintList(true);
    },
    /** 读取本人浏览足迹并按日期更新展示集合. */
    getFootprintList(stopRefresh) {
        let tmpFootPrint;
        util.request(api.FootprintList)
            .then((res) => {
                if (res.code === 0) {
                    if (res.data.data != undefined) {
                        tmpFootPrint = res.data.data;
                    } else {
                        tmpFootPrint = [];
                    }

                    this.setData({
                        footprintList: tmpFootPrint,
                    });
                }
                if (stopRefresh) {
                    wx.stopPullDownRefresh();
                }
            })
            .catch(function () {
                if (stopRefresh) {
                    wx.stopPullDownRefresh();
                }
            });
    },
    /** 删除本人指定浏览足迹并更新列表. */
    deleteItem(event) {
        const footprint = event.currentTarget.dataset.footprint;
        const touchTime = this.data.touch_end - this.data.touch_start;
        //如果按下时间大于350为长按
        if (touchTime > 350) {
            wx.showModal({
                title: '',
                content: '要删除所选足迹？',
                success: (res) => {
                    if (res.confirm) {
                        util.request(api.FootprintDelete, {footprintId: footprint.id}).then(
                            (res) => {
                                if (res.code === 0) {
                                    wx.showToast({
                                        title: res.msg,
                                        icon: 'success',
                                        duration: 2000,
                                        complete: () => {
                                            this.getFootprintList();
                                        },
                                    });
                                } else {
                                    util.showErrorToast(res.msg);
                                }
                            }
                        );
                    }
                },
            });
        } else {
            wx.navigateTo({
                url: '/pages/goods/goods?id=' + footprint.goodsId,
            });
        }
    },
    /** 初始化浏览足迹状态并读取页面所需数据. */
    onLoad(options) {
        this.getFootprintList();
    },

    /** 页面再次展示时刷新浏览足迹状态. */
    onShow() {
        this.getFootprintList();
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
