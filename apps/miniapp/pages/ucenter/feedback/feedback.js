const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');
const {withPageReads} = require('../../../services/page-read.js');
const {performPageAction} = require('../../../services/page-action.js');

Page(
    withPageReads({
        data: {
            array: [
                '请选择反馈类型',
                '商品相关',
                '物流状况',
                '客户服务',
                '优惠活动',
                '功能异常',
                '产品建议',
                '其他',
            ],
            index: 0,
            content: '',
            contentLength: 0,
            mobile: '',
        },
        /** 提交期间保持本次类型和正文不变. */
        selectFeedbackType(event) {
            const index = Number(event.currentTarget.dataset.type);
            if (!this.data.actionBusy && Number.isInteger(index) && index > 0 && index < this.data.array.length) {
                this.setData({index});
            }
        },
        /** 保存当前联系方式, 写入期间不修改正在提交的输入. */
        mobileInput(event) {
            if (!this.data.actionBusy) {
                this.setData({mobile: event.detail.value});
            }
        },
        /** 字数按实际正文计算, 移动光标不改变计数. */
        contentInput(event) {
            if (!this.data.actionBusy) {
                this.setData({
                    content: event.detail.value,
                    contentLength: event.detail.value.length,
                });
            }
        },
        /** 校验本次输入并防重提交, 失败保留内容且不自动重放. */
        submitFeedback() {
            const index = Number(this.data.index);
            const content = this.data.content.trim();
            const mobile = this.data.mobile.trim();
            if (!Number.isInteger(index) || index < 1 || index > 7) {
                util.showErrorToast('请选择反馈类型');
                return Promise.resolve(false);
            }
            if (!content || content.length > 500) {
                util.showErrorToast('请输入 1 到 500 字的反馈内容');
                return Promise.resolve(false);
            }
            if (mobile && !/^1[3-9]\d{9}$/.test(mobile)) {
                util.showErrorToast('请输入有效的中国大陆手机号');
                return Promise.resolve(false);
            }
            return performPageAction(this, {
                canRun: () => true,
                request: () =>
                    util.request(
                        api.FeedbackAdd,
                        {index, content, mobile},
                        'POST',
                        'application/json'
                    ),
                onSuccess: () => {
                    this.setData({index: 0, content: '', contentLength: 0, mobile: ''});
                    wx.showToast({title: '感谢你的反馈', icon: 'success'});
                },
            });
        },
    })
);
