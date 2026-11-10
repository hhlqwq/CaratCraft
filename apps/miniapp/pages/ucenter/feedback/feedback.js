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
        submitting: false,
    },
    /** 同步当前选择的反馈类型. */
    bindPickerChange(event) {
        this.setData({index: Number(event.detail.value)});
    },
    /** 仅保留数字并同步反馈联系手机号输入. */
    mobileInput(event) {
        this.setData({
            mobile: String(event.detail.value || '')
                .replace(/[^0-9]/g, '')
                .slice(0, 11),
        });
    },
    /** 同步反馈正文和实际字符数, 超长输入不写入页面. */
    contentInput(event) {
        const value = String(event.detail.value || '');
        if (value.length > 500) {
            return;
        }
        this.setData({content: value, contentLength: value.length});
    },
    /** 校验反馈内容和联系方式后提交, 失败释放锁并保留输入. */
    sbmitFeedback() {
        if (this.data.submitting) {
            return false;
        }
        if (this.data.index === 0) {
            util.showErrorToast('请选择反馈类型');
            return false;
        }
        const content = this.data.content.trim();
        if (!content) {
            util.showErrorToast('请输入反馈内容');
            return false;
        }
        if (content.length > 500) {
            util.showErrorToast('反馈内容不能超过500字');
            return false;
        }
        if (!/^1[3-9][0-9]{9}$/.test(this.data.mobile)) {
            util.showErrorToast('请输入有效的手机号码');
            return false;
        }
        const token = wx.getStorageSync('token') || '';
        const current = () => !this._unloaded && token === (wx.getStorageSync('token') || '');
        this.setData({submitting: true});
        return util
            .request(
                api.FeedbackAdd,
                {mobile: this.data.mobile, index: this.data.index, content: content},
                'POST',
                'application/json'
            )
            .then((res) => {
                if (!current()) {
                    return false;
                }
                if (res.code !== 0) {
                    throw new Error(res.msg || '反馈提交失败');
                }
                this.setData({index: 0, content: '', contentLength: 0, mobile: ''});
                wx.showToast({title: '感谢反馈', icon: 'success', duration: 2000});
                return true;
            })
            .catch((error) => {
                if (current()) {
                    util.showErrorToast(error.message || '反馈提交失败, 请稍后重试');
                }
                return false;
            })
            .finally(() => {
                if (current()) {
                    this.setData({submitting: false});
                }
            });
    },
    /** 页面卸载后拒绝迟到反馈结果写入界面. */
    onUnload() {
        this._unloaded = true;
    },
});
