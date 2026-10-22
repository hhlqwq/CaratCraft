const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

Page({
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
    /** 同步当前选择的反馈类型. */
    bindPickerChange(e) {
        this.setData({
            index: e.detail.value,
        });
    },
    /** 同步反馈联系手机号输入. */
    mobileInput(e) {
        this.setData({
            mobile: e.detail.value,
        });
    },
    /** 同步反馈正文输入. */
    contentInput(e) {
        this.setData({
            contentLength: e.detail.cursor,
            content: e.detail.value,
        });
    },
    /** 清空反馈联系手机号. */
    cleanMobile() {},
    /** 校验反馈内容和联系方式后提交反馈. */
    sbmitFeedback(e) {
        if (this.data.index == 0) {
            util.showErrorToast('请选择反馈类型');
            return false;
        }

        if (this.data.content == '') {
            util.showErrorToast('请输入反馈内容');
            return false;
        }

        if (this.data.mobile == '') {
            util.showErrorToast('请输入手机号码');
            return false;
        }
        wx.showLoading({
            title: '提交中...',
            mask: true,
            success() {},
        });

        util.request(
            api.FeedbackAdd,
            {mobile: this.data.mobile, index: this.data.index, content: this.data.content},
            'POST',
            'application/json'
        ).then((res) => {
            if (res.code === 0) {
                wx.hideLoading();

                wx.showToast({
                    title: res.data,
                    icon: 'success',
                    duration: 2000,
                    complete: () => {
                        this.setData({
                            index: 0,
                            content: '',
                            contentLength: 0,
                            mobile: '',
                        });
                    },
                });
            } else {
                util.showErrorToast(res.msg);
            }
        });
    },
});
