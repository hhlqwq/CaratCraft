const util = require('../utils/util.js');

/** 按明确接口字段提供列表分页, 总数始终表示记录数, 不猜测旧响应格式. */
function createPagedFeed({url, listField, responseField, replace = false, refreshOnShow = false}) {
    return {
        data: {
            [listField]: [],
            page: 1,
            size: 10,
            count: 0,
            totalPages: 0,
            loading: false,
            error: '',
            hasMore: true,
            nomore: false,
            showPage: false,
            scrollTop: 0,
            loadmoreText: '正在加载更多数据',
            nomoreText: '全部加载完成',
        },
        /** 首次读取第一页, 展示阶段不重复发起同一个请求. */
        onLoad() {
            this._alive = true;
            this._sessionToken = wx.getStorageSync('token') || '';
            this._firstShow = true;
            return this.resetFeed();
        },
        /** 私人列表返回时更新状态, 账号变化先清空旧内容. */
        onShow() {
            const token = wx.getStorageSync('token') || '';
            const changed = this._sessionToken !== undefined && token !== this._sessionToken;
            this._sessionToken = token;
            if (this._firstShow && !changed) {
                this._firstShow = false;
                return;
            }
            this._firstShow = false;
            if (changed || refreshOnShow) {
                return this.resetFeed();
            }
        },
        /** 页面销毁后废弃在途读取, 不覆盖其他页面状态. */
        onUnload() {
            this._alive = false;
            this._feedVersion = (this._feedVersion || 0) + 1;
        },
        /** 清空旧列表并读取第一页, 下拉及账号切换共用相同逻辑. */
        resetFeed() {
            if (this._alive === false) {
                return Promise.resolve(false);
            }
            this._feedVersion = (this._feedVersion || 0) + 1;
            this._loadedPage = 0;
            this.setData({
                [listField]: [],
                page: 1,
                loading: false,
                error: '',
                hasMore: true,
                nomore: false,
                count: 0,
                showPage: false,
            });
            return this.readFeed(1);
        },
        /** 读取成功后才推进页码, 失败保留原列表及可重试页. */
        readFeed(page) {
            if (
                this._alive === false ||
                this.data.loading ||
                !Number.isSafeInteger(page) ||
                page < 1
            ) {
                return Promise.resolve(false);
            }
            const token = wx.getStorageSync('token') || '';
            const version = (this._feedVersion = (this._feedVersion || 0) + 1);
            const current = () =>
                this._alive !== false &&
                version === this._feedVersion &&
                token === (wx.getStorageSync('token') || '');
            this._requestedPage = page;
            this.setData({loading: true, error: ''});
            return util
                .request(url, {page, size: this.data.size})
                .then((res) => {
                    if (!current()) {
                        return false;
                    }
                    const data = res.data;
                    if (
                        res.code !== 0 ||
                        !data ||
                        !Array.isArray(data[responseField]) ||
                        !Number.isSafeInteger(data.total) ||
                        data.total < 0
                    ) {
                        throw new Error(res.msg || '列表数据格式无效, 请重试');
                    }
                    const records = data[responseField];
                    const hasMore = records.length > 0 && page * this.data.size < data.total;
                    this._loadedPage = page;
                    this.setData({
                        [listField]:
                            replace || page === 1 ? records : this.data[listField].concat(records),
                        page,
                        count: data.total,
                        totalPages: Math.ceil(data.total / this.data.size),
                        hasMore,
                        nomore: !hasMore,
                        showPage: data.total > 0,
                        scrollTop: 0,
                    });
                    return true;
                })
                .catch((error) => {
                    if (current()) {
                        this.setData({error: error.message || '列表读取失败, 请重试'});
                    }
                    return false;
                })
                .finally(() => {
                    if (current()) {
                        this.setData({loading: false});
                        wx.stopPullDownRefresh();
                    }
                });
        },
        /** 明确重试失败页, 不把失败当作空列表或已到末页. */
        retryLoad() {
            return this.readFeed(this._requestedPage || 1);
        },
        /** 连续列表仅在还有记录时请求下一页. */
        onReachBottom() {
            if (!replace && this.data.hasMore) {
                return this.readFeed((this._loadedPage || 0) + 1);
            }
        },
        /** 下拉刷新重新读取当前账号第一页. */
        onPullDownRefresh() {
            return this.resetFeed();
        },
        /** 专题分页允许最后不足一页的内容, 不对总页数做向下截断. */
        nextPage() {
            if (this.data.hasMore) {
                return this.readFeed(this.data.page + 1);
            }
        },
        /** 分页切换只在成功后替换当前内容. */
        prevPage() {
            if (this.data.page > 1) {
                return this.readFeed(this.data.page - 1);
            }
        },
    };
}

module.exports = {createPagedFeed};
