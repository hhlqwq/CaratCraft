const util = require('../../utils/util.js');
const api = require('../../config/api.js');
const {withPageReads} = require('../../services/page-read.js');
const {performPageAction} = require('../../services/page-action.js');

Page(
    withPageReads({
        data: {typeId: 0, valueId: 0, content: '', posted: false},
        /** 只接受商品评价路由, 不按模糊数字前缀识别身份. */
        onLoad(options) {
            const typeId = Number(options.typeId);
            const valueId = Number(options.valueId);
            this.setData({typeId, valueId});
        },
        /** 写入完成或未开始时允许返回, 不在提交中丢弃结果. */
        onClose() {
            if (!this.data.actionBusy) {
                wx.navigateBack({delta: 1});
            }
        },
        /** 提交期间保持正文不变, 长度与服务端500字契约一致. */
        bindInputValue(event) {
            if (!this.data.actionBusy && !this.data.posted) {
                this.setData({content: event.detail.value});
            }
        },
        /** 防重发布本人已收货商品评价, 资格及奖励由服务端事务决定. */
        onPost() {
            const {typeId, valueId} = this.data;
            const content = this.data.content.trim();
            if (
                typeId !== 0 ||
                !Number.isSafeInteger(valueId) ||
                valueId < 1 ||
                !content ||
                content.length > 500
            ) {
                util.showErrorToast('请选择商品并填写1到500字评价');
                return Promise.resolve(false);
            }
            return performPageAction(this, {
                canRun: () => !this.data.posted,
                request: () =>
                    util.request(
                        api.CommentPost,
                        {typeId, valueId, content},
                        'POST',
                        'application/json'
                    ),
                onSuccess: () => {
                    this.setData({posted: true});
                    wx.showToast({title: '已提交审核', icon: 'success'});
                    wx.navigateBack({delta: 1});
                },
            });
        },
    })
);
