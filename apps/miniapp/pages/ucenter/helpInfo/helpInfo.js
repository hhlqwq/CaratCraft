const {readPage, withPageReads} = require('../../../services/page-read.js');
const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

// pages/ucenter/help/help.js
Page(
    withPageReads({
        data: {
            helpList: [],
        },
        /** 读取当前帮助页面所需的帮助信息. */
        getHelpList(id) {
            readPage(
                this,
                'HelpIssueList',
                () => util.request(api.HelpIssueList, {typeId: id}),
                (res) => {
                    if (res.code === 0) {
                        this.setData({
                            helpList: res.data,
                        });
                    }
                }
            );
        },
        /** 初始化帮助详情状态并读取页面所需数据. */
        onLoad(options) {
            this.getHelpList(options.id);
        },
    })
);
