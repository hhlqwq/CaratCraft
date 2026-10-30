const {readPage, withPageReads} = require('../../services/page-read.js');
const util = require('../../utils/util.js');
const api = require('../../config/api.js');

Page(
    withPageReads({
        data: {
            id: 0,
            topicDetailHtml: '',
            topic: {},
            topicList: [],
            commentCount: 0,
            commentList: [],
        },
        /** 提供当前页面的微信好友分享信息. */
        onShareAppMessage() {
            //自定义函数 用户点击右上角分享给好友,要先在分享好友这里设置menus的两个参数,才可以分享朋友圈
            wx.showShareMenu({
                withShareTicket: true,
                menus: ['shareAppMessage', 'shareTimeline'],
            });
            return {
                title: this.data.topic.title,
                desc: this.data.topic.content,
                imageUrl: this.data.topic.scenePicUrl,
                path: '/pages/topicDetail/topicDetail?id=' + this.data.id,
            };
        },
        /** 提供当前页面的朋友圈分享信息. */
        onShareTimeline() {
            //用户点击右上角分享朋友圈
            return {
                title: this.data.topic.title,
                query: {
                    id: this.data.id,
                },
                imageUrl: this.data.topic.scenePicUrl,
            };
        },
        /** 初始化专题详情状态并读取页面所需数据. */
        onLoad(options) {
            this.setData({
                id: parseInt(options.id),
            });

            readPage(
                this,
                'TopicDetail',
                () => util.request(api.TopicDetail, {id: this.data.id}),
                (res) => {
                    if (res.code === 0) {
                        this.setData({
                            topic: res.data,
                        });

                        /* 使用微信原生富文本组件显示HTML, 不再分发旧解析库. */
                        this.setData({topicDetailHtml: res.data.content || ''});
                    }
                }
            );

            readPage(
                this,
                'TopicRelated',
                () => util.request(api.TopicRelated, {id: this.data.id}),
                (res) => {
                    if (res.code === 0) {
                        this.setData({
                            topicList: res.data,
                        });
                    }
                }
            );
        },
    })
);
