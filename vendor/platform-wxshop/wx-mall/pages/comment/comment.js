const util = require('../../utils/util.js');
const api = require('../../config/api.js');

Page({
    data: {
        comments: [],
        typeId: 0,
        valueId: 0,
        showType: 0,
        allCount: 0,
        hasPicCount: 0,
        page: 1,
        size: 20,
        loading: false,
        hasMore: true,
        error: '',
    },
    /** 初始化合法商品的公开购买评价, 社区不借用此入口开放评论. */
    onLoad(options) {
        this._version = 1;
        this._unloaded = false;
        const valueId = Number(options.valueId);
        if (!Number.isSafeInteger(valueId) || valueId < 1 || Number(options.typeId || 0) !== 0) {
            this.setData({error: '商品编号无效或该内容不开放评价', hasMore: false});
            return;
        }
        this.setData({valueId});
        this.getCommentCount();
        this.getCommentList();
    },
    /** 读取审核通过的全部及有图计数, 辅助计数失败不阻断实际列表. */
    async getCommentCount() {
        const version = this._version;
        try {
            const res = await util.request(api.CommentCount, {
                valueId: this.data.valueId,
                typeId: 0,
            });
            if (!this._unloaded && version === this._version && res.code === 0) {
                this.setData({allCount: res.data.allCount, hasPicCount: res.data.hasPicCount});
            }
        } catch (error) {
            // 列表本身仍返回当前分类总数, 刷新可重取另一分类的辅助计数.
        }
    },
    /** 使用统一records分页契约, 仅成功后推进页码, 失败保持原页供重试. */
    async getCommentList() {
        if (this.data.loading || !this.data.hasMore || !this.data.valueId) {
            return;
        }
        const version = this._version;
        const type = this.data.showType;
        this.setData({loading: true, error: ''});
        try {
            const res = await util.request(api.CommentList, {
                valueId: this.data.valueId,
                typeId: 0,
                size: this.data.size,
                page: this.data.page,
                showType: type,
            });
            if (this._unloaded || version !== this._version) {
                return;
            }
            if (res.code !== 0) {
                throw new Error(res.msg || '评价加载失败');
            }
            const rows = res.data.records.map((row) => ({
                ...row,
                addTimeText: util.formatTime(new Date(row.addTime * 1000)),
            }));
            const comments = this.data.comments.concat(rows);
            this.setData({
                comments,
                page: this.data.page + 1,
                hasMore: rows.length > 0 && comments.length < res.data.total,
                [type === 0 ? 'allCount' : 'hasPicCount']: res.data.total,
            });
        } catch (error) {
            if (!this._unloaded && version === this._version) {
                this.setData({error: error.message || '评价加载失败, 请重试'});
            }
        } finally {
            if (!this._unloaded && version === this._version) {
                this.setData({loading: false});
            }
        }
    },
    /** 切换分类使旧响应失效, 不把旧页码或旧数组写入新分类. */
    switchTab(event) {
        const type = Number(event.currentTarget.dataset.type);
        if (![0, 1].includes(type) || type === this.data.showType) {
            return;
        }
        this._version++;
        this.setData({
            showType: type,
            comments: [],
            page: 1,
            loading: false,
            hasMore: true,
            error: '',
        });
        this.getCommentList();
        this.getCommentCount();
    },
    /** 触底只加载尚未读取的下一页, 不预先递增页码. */
    onReachBottom() {
        return this.getCommentList();
    },
    /** 离开页面即拒绝所有迟到成功和失败回填. */
    onUnload() {
        this._unloaded = true;
        this._version++;
    },
});
