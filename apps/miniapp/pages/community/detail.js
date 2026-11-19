const community = require('../../services/community.js');

/** 分享详情仅展示审核后的公开副本. */
Page({
    data: {id: 0, post: null, loading: false, error: '', liked: false, owned: false, busy: false},
    /** 读取公开帖子ID, 非法参数交给服务端统一拒绝. */
    onLoad(options) {
        this.setData({id: Number(options.id || 0)});
    },
    /** 每次进入重新检查公开状态. */
    onShow() {
        this.load();
    },
    /** 不保留旧详情到新请求失败之后, 隐藏内容立即显示不可用. */
    load() {
        const seq = (this._seq = (this._seq || 0) + 1);
        this.setData({loading: true, error: '', post: null});
        return community
            .request('detail', {id: this.data.id})
            .then((post) => {
                if (seq !== this._seq) {
                    return;
                }
                this.setData({post: community.present(post)});
                if (wx.getStorageSync('token')) {
                    return community
                        .request('states', [this.data.id], 'POST', true)
                        .then((states) => {
                            if (seq === this._seq && states[0]) {
                                this.setData({
                                    liked: Boolean(Number(states[0].liked)),
                                    owned: Boolean(Number(states[0].owned)),
                                });
                            }
                        });
                }
            })
            .catch((error) => {
                if (seq === this._seq) {
                    this.setData({error: error.message});
                }
            })
            .finally(() => {
                if (seq === this._seq) {
                    this.setData({loading: false});
                }
                wx.stopPullDownRefresh();
            });
    },
    /** 图片点击使用平台预览, 不打开任意外部网页. */
    preview(event) {
        wx.previewImage({current: event.currentTarget.dataset.url, urls: this.data.post.images});
    },
    /** 发送目标点赞状态, 同一详情只允许一个在途操作. */
    like() {
        if (!community.loggedIn() || this.data.busy) {
            return;
        }

        this.setData({busy: true});
        community
            .request('like', {id: this.data.id, liked: !this.data.liked}, 'POST')
            .then((result) => {
                this.setData({liked: result.liked, 'post.likes': result.likes});
            })
            .catch(function (error) {
                wx.showToast({title: error.message, icon: 'none'});
            })
            .finally(() => {
                this.setData({busy: false});
            });
    },
    /** 删除本人帖子后返回列表. */
    remove() {
        wx.showModal({
            title: '删除分享',
            content: '删除后不可恢复。',
            success: (res) => {
                if (res.confirm) {
                    community
                        .request('remove', {id: this.data.id}, 'POST')
                        .then(function () {
                            wx.navigateBack();
                        })
                        .catch(function (error) {
                            wx.showToast({title: error.message, icon: 'none'});
                        });
                }
            },
        });
    },
    /** 下拉刷新公开状态. */
    onPullDownRefresh() {
        this.load();
    },
    /** 废弃离开后的在途详情请求. */
    onUnload() {
        this._seq = (this._seq || 0) + 1;
    },
});
