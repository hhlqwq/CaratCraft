const util = require('../../utils/util.js');
const api = require('../../config/api.js');
Page({
    data: {
        typeId: 0,
        valueId: 0,
        content: '',
    },
    /** 初始化评价发布状态并读取页面所需数据. */
    onLoad(options) {
        this.setData({
            typeId: parseInt(options.typeId),
            valueId: parseInt(options.valueId),
        });
    },
    /** 关闭评价编辑页并返回上一页. */
    onClose() {
        wx.navigateBack({
            delta: 1,
        });
    },
    /** 校验并提交当前评价内容和图片. */
    onPost() {
        if (!this.data.content) {
            util.showErrorToast('请填写评论');
            return false;
        }

        util.request(
            api.CommentPost,
            {
                typeId: this.data.typeId,
                valueId: this.data.valueId,
                content: this.data.content,
            },
            'POST',
            'application/json'
        ).then(function (res) {
            if (res.code === 0) {
                wx.showToast({
                    title: '已提交审核',
                    complete() {
                        wx.navigateBack({
                            delta: 1,
                        });
                    },
                });
            }
        });
    },
    /** 将评价文本输入同步到页面状态. */
    bindInpuntValue(event) {
        const value = event.detail.value;

        //判断是否超过140个字符
        if (value && value.length > 140) {
            return false;
        }

        this.setData({
            content: event.detail.value,
        });
    },
});
