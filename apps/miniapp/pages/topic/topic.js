const util = require('../../utils/util.js');
const api = require('../../config/api.js');

Page({
    data: {
        // text:"这是一个页面"
        topicList: [],
        page: 1,
        size: 10,
        count: 0,
        scrollTop: 0,
        showPage: false,
    },
    /** 初始化专题列表状态并读取页面所需数据. */
    onLoad(options) {
        this.getTopic();
    },

    /** 在仍有后续内容时加载下一页专题. */
    nextPage(event) {
        if (this.data.page + 1 > this.data.count / this.data.size) {
            return true;
        }

        this.setData({
            page: parseInt(this.data.page) + 1,
        });

        this.getTopic();
    },
    /** 按当前页码读取专题列表及分页信息. */
    getTopic() {
        this.setData({
            scrollTop: 0,
            showPage: false,
            topicList: [],
        });
        // 页面渲染完成
        wx.showToast({
            title: '加载中...',
            icon: 'loading',
            duration: 2000,
        });

        util.request(api.TopicList, {
            page: this.data.page,
            size: this.data.size,
        }).then((res) => {
            if (res.code === 0) {
                this.setData({
                    scrollTop: 0,
                    topicList: res.data.records,
                    showPage: true,
                    count: res.data.total,
                });
            }
            wx.hideToast();
        });
    },
    /** 在当前页大于首页时加载上一页专题. */
    prevPage(event) {
        if (this.data.page <= 1) {
            return false;
        }

        this.setData({
            page: parseInt(this.data.page) - 1,
        });
        this.getTopic();
    },
});
