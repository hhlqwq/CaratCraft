const community = require('../../services/community.js');

/** 社区公共信息流与本人审核进度, 不提供用户主页或评论入口. */
Page({
    data: {
        records: [],
        total: 0,
        page: 1,
        loading: false,
        error: '',
        kind: '',
        mine: false,
        kinds: [
            {key: '', name: '全部'},
            {key: 'DESIGN', name: '设计'},
            {key: 'FINISHED', name: '成品'},
            {key: 'SUGGESTION', name: '建议'},
        ],
    },

    /** 从详情返回后刷新审核状态和点赞数. */
    onShow() {
        this.load(true);
    },

    /** 切换分类后从第一页重新读取. */
    filter(event) {
        this.setData({kind: event.currentTarget.dataset.kind, mine: false});
        this.load(true);
    },

    /** 本人列表需登录, 展示审核原因并允许删除自己的分享. */
    toggleMine() {
        if (!community.loggedIn()) {
            return;
        }
        this.setData({mine: !this.data.mine});
        this.load(true);
    },

    /** 分页响应带请求序号, 分类切换时丢弃旧响应. */
    load(reset) {
        if (!reset && (this.data.loading || this.data.records.length >= this.data.total)) {
            return Promise.resolve();
        }
        const seq = (this._seq = (this._seq || 0) + 1);
        const page = reset ? 1 : this.data.page + 1;
        const mine = this.data.mine;
        this.setData({loading: true, error: ''});
        return community
            .request(mine ? 'mine' : 'list', {kind: this.data.kind, page: page, limit: 20})
            .then((data) => {
                if (seq !== this._seq) {
                    return;
                }
                const records = data.records.map(community.present);
                this.setData({
                    records: reset ? records : this.data.records.concat(records),
                    total: data.total,
                    page: page,
                });
                if (records.length && wx.getStorageSync('token')) {
                    return community
                        .request(
                            'states',
                            this.data.records
                                .map(function (row) {
                                    return row.id;
                                })
                                .slice(-100),
                            'POST',
                            true
                        )
                        .then((states) => {
                            if (seq !== this._seq) {
                                return;
                            }
                            this.setData({
                                records: this.data.records.map(function (row) {
                                    const state = states.find(function (value) {
                                        return value.id === row.id;
                                    });
                                    return state
                                        ? Object.assign({}, row, {
                                              liked: Boolean(Number(state.liked)),
                                              owned: Boolean(Number(state.owned)),
                                          })
                                        : row;
                                }),
                            });
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

    /** 页尾继续加载, 不跳过失败页. */
    onReachBottom() {
        this.load(false);
    },
    /** 下拉刷新同一信息流. */
    onPullDownRefresh() {
        this.load(true);
    },
    /** 重试保持当前分类并重新读取第一页. */
    retry() {
        this.load(true);
    },
    /** 发布前要求登录, 登录完成后用户主动选择内容. */
    compose() {
        if (community.loggedIn()) {
            wx.navigateTo({url: '/pages/community/publish'});
        }
    },
    /** 详情仅打开公开内容, 私人审核反馈直接显示在本人列表. */
    open(event) {
        const item = this.data.records.find(function (row) {
            return row.id === Number(event.currentTarget.dataset.id);
        });
        if (item && (!this.data.mine || item.status === 'PUBLISHED')) {
            wx.navigateTo({url: '/pages/community/detail?id=' + item.id});
        }
    },
    /** 目标点赞状态支持网络重试, 按帖子阻止在途重复点击. */
    like(event) {
        if (!community.loggedIn()) {
            return;
        }

        const id = Number(event.currentTarget.dataset.id);
        const item = this.data.records.find(function (row) {
            return row.id === id;
        });
        if (!item || item.busy) {
            return;
        }
        item.busy = true;
        community
            .request('like', {id: id, liked: !item.liked}, 'POST')
            .then((result) => {
                this.setData({
                    records: this.data.records.map(function (row) {
                        return row.id === id ? Object.assign({}, row, result, {busy: false}) : row;
                    }),
                });
            })
            .catch(function (error) {
                item.busy = false;
                wx.showToast({title: error.message, icon: 'none'});
            });
    },
    /** 删除前确认, 失败时保留当前内容和操作入口. */
    remove(event) {
        wx.showModal({
            title: '删除分享',
            content: '删除后不再公开, 此操作无法撤回。',
            success: (res) => {
                if (res.confirm) {
                    community
                        .request('remove', {id: event.currentTarget.dataset.id}, 'POST')
                        .then(() => {
                            this.load(true);
                        })
                        .catch(function (error) {
                            wx.showToast({title: error.message, icon: 'none'});
                        });
                }
            },
        });
    },
    /** 离开页面废弃在途响应. */
    onUnload() {
        this._seq = (this._seq || 0) + 1;
    },
});
