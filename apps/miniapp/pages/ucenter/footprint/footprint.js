const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

Page({
    data: {
        footprintList: [],
        loading: false,
        error: '',
        page: 1,
        size: 20,
        hasMore: true,
        deleting: false,
    },
    /** 初始化足迹页状态, 实际读取在页面展示时执行. */
    onLoad() {
        this._unloaded = false;
        this._version = 0;
        this._lastToken = wx.getStorageSync('token') || '';
    },
    /** 页面再次展示时从第一页刷新本人足迹, 账号变化先清除旧列表. */
    onShow() {
        const token = wx.getStorageSync('token') || '';
        if (token !== this._lastToken) {
            this._lastToken = token;
            this._version = (this._version || 0) + 1;
            this.setData({
                footprintList: [],
                error: '',
                page: 1,
                hasMore: true,
                deleting: false,
                loading: false,
            });
        }
        return this.loadFootprints(true);
    },
    /** 下拉时重新读取第一页并结束刷新提示. */
    onPullDownRefresh() {
        return this.loadFootprints(true);
    },
    /** 触底只追加尚未读取的下一页, 失败保留已有内容. */
    onReachBottom() {
        if (this.data.hasMore) {
            return this.loadFootprints(false);
        }
        return Promise.resolve(false);
    },
    /** 离开页面后拒绝迟到响应并清除私人列表上下文. */
    onUnload() {
        this._unloaded = true;
        this._version = (this._version || 0) + 1;
    },
    /** 读取本人足迹, reset为真时从第一页开始并清空旧内容. */
    loadFootprints(reset) {
        const token = wx.getStorageSync('token') || '';
        if (!token) {
            this.setData({
                loading: false,
                error: '请先登录后查看浏览足迹',
                footprintList: [],
                hasMore: false,
            });
            wx.stopPullDownRefresh();
            return Promise.resolve(false);
        }
        if (!reset && (this.data.loading || !this.data.hasMore)) {
            return Promise.resolve(false);
        }
        const version = (this._version = (this._version || 0) + 1);
        const page = reset ? 1 : this.data.page;
        const current = () =>
            !this._unloaded &&
            version === this._version &&
            token === (wx.getStorageSync('token') || '');
        if (reset) {
            this.setData({footprintList: [], error: '', hasMore: true, page: 1, loading: true});
        } else {
            this.setData({loading: true, error: ''});
        }
        return util
            .request(api.FootprintList, {page: page, limit: this.data.size})
            .then((res) => {
                if (!current()) {
                    return false;
                }
                if (res.code !== 0) {
                    throw new Error(res.msg || '足迹加载失败');
                }
                const payload = res.data || {};
                const incoming = Array.isArray(payload.data) ? payload.data : [];
                const merged = reset
                    ? incoming
                    : this.mergeGroups(this.data.footprintList, incoming);
                this.setData({
                    footprintList: merged,
                    page: page + 1,
                    hasMore: Boolean(payload.hasMore),
                });
                return true;
            })
            .catch((error) => {
                if (current()) {
                    this.setData({error: error.message || '足迹加载失败, 请重试'});
                }
                return false;
            })
            .finally(() => {
                if (version === this._version) {
                    this.setData({loading: false});
                }
                wx.stopPullDownRefresh();
            });
    },
    /** 将分页返回的日期分组合并到已有分组, 同一天保持倒序不重复. */
    mergeGroups(existing, incoming) {
        const byDate = new Map();
        (existing || []).concat(incoming || []).forEach((group) => {
            if (!Array.isArray(group) || group.length === 0) {
                return;
            }
            const date = String(group[0].addTime || '');
            const rows = byDate.get(date) || [];
            group.forEach((row) => {
                if (!rows.some((saved) => saved.id === row.id)) {
                    rows.push(row);
                }
            });
            byDate.set(date, rows);
        });
        return Array.from(byDate.entries())
            .sort((left, right) => right[0].localeCompare(left[0]))
            .map((entry) => entry[1]);
    },
    /** 长按确认后删除本人足迹, 结果不明时重新读取权威列表. */
    deleteItem(event) {
        const footprint = event.currentTarget.dataset.footprint;
        const touchTime = (this.data.touch_end || 0) - (this.data.touch_start || 0);
        if (touchTime <= 350) {
            wx.navigateTo({
                url: '/pages/goods/goods?id=' + footprint.goodsId,
            });
            return;
        }
        const id = Number(footprint.id);
        if (!Number.isSafeInteger(id) || id <= 0 || this.data.deleting) {
            return;
        }
        const token = wx.getStorageSync('token') || '';
        const current = () => !this._unloaded && token === (wx.getStorageSync('token') || '');
        wx.showModal({
            title: '',
            content: '要删除所选足迹？',
            success: (result) => {
                if (!result.confirm || !current() || this.data.deleting) {
                    return;
                }
                this.setData({deleting: true});
                util.request(api.FootprintDelete, {footprintId: id})
                    .then((res) => {
                        if (!current()) {
                            return;
                        }
                        if (res.code !== 0) {
                            throw new Error(res.msg || '删除失败');
                        }
                        util.showSuccessToast('删除成功');
                    })
                    .catch((error) => {
                        if (current()) {
                            util.showErrorToast(error.message || '删除结果未确认, 请刷新');
                        }
                    })
                    .finally(() => {
                        if (current()) {
                            this.setData({deleting: false});
                            this.loadFootprints(true);
                        }
                    });
            },
        });
    },
    /** 记录本次按压开始时间用于区分点击和长按. */
    touchStart(event) {
        this.setData({touch_start: event.timeStamp});
    },
    /** 记录本次按压结束时间用于区分点击和长按. */
    touchEnd(event) {
        this.setData({touch_end: event.timeStamp});
    },
});
