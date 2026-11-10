const util = require('../../utils/util.js');
const api = require('../../config/api.js');

Page({
    data: {
        typeId: 0,
        valueId: 0,
        content: '',
        submitting: false,
        error: '',
    },
    /** 校验商品评价目标, 非法参数不发起提交. */
    onLoad(options) {
        this._unloaded = false;
        const typeId = Number(options.typeId);
        const valueId = Number(options.valueId);
        if (typeId !== 0 || !Number.isSafeInteger(valueId) || valueId < 1) {
            this.setData({error: '仅支持已购买商品的评价'});
            return;
        }
        this.setData({typeId, valueId});
    },
    /** 关闭评价编辑页并返回上一页. */
    onClose() {
        wx.navigateBack({
            delta: 1,
        });
    },
    /** 校验并提交当前评价内容, 提交期间阻止重复请求. */
    onPost() {
        if (this.data.submitting) {
            return Promise.resolve(false);
        }
        const content = this.data.content.trim();
        if (!content) {
            util.showErrorToast('请填写评论');
            return Promise.resolve(false);
        }
        if (content.length > 140) {
            util.showErrorToast('评论不能超过140字');
            return Promise.resolve(false);
        }
        if (!this.data.valueId) {
            util.showErrorToast('商品参数无效');
            return Promise.resolve(false);
        }
        const token = wx.getStorageSync('token') || '';
        const current = () => !this._unloaded && token === (wx.getStorageSync('token') || '');
        this.setData({submitting: true, error: ''});
        return util
            .request(
                api.CommentPost,
                {
                    typeId: this.data.typeId,
                    valueId: this.data.valueId,
                    content: content,
                },
                'POST',
                'application/json'
            )
            .then((res) => {
                if (!current()) {
                    return false;
                }
                if (res.code !== 0) {
                    throw new Error(res.msg || '评价提交失败');
                }
                wx.showToast({
                    title: '已提交审核',
                    complete() {
                        wx.navigateBack({
                            delta: 1,
                        });
                    },
                });
                return true;
            })
            .catch((error) => {
                if (current()) {
                    this.setData({error: error.message || '评价提交失败'});
                    util.showErrorToast(error.message || '评价提交失败');
                }
                return false;
            })
            .finally(() => {
                if (current()) {
                    this.setData({submitting: false});
                }
            });
    },
    /** 将评价文本输入同步到页面状态并限制140字. */
    bindInpuntValue(event) {
        const value = String(event.detail.value || '');

        //判断是否超过140个字符
        if (value.length > 140) {
            return false;
        }

        this.setData({
            content: value,
            error: '',
        });
        return true;
    },
    /** 离开页面后拒绝迟到提交结果写入界面. */
    onUnload() {
        this._unloaded = true;
    },
});
