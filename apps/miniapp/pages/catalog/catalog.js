const util = require('../../utils/util.js');
const api = require('../../config/api.js');

Page({
    data: {
        sources: [
            {id: '', name: '全部来源'},
            {id: 'FRESHWATER', name: '淡水珍珠'},
            {id: 'SEAWATER', name: '海水珍珠'},
        ],
        types: [
            {id: '', name: '全部品类'},
            {id: 'NECKLACE', name: '项链'},
            {id: 'EARRINGS', name: '耳饰'},
            {id: 'BRACELET', name: '手链'},
            {id: 'RING', name: '戒指'},
        ],
        source: '',
        type: '',
        goods: [],
        total: 0,
        page: 1,
        loading: false,
        hasMore: true,
        error: '',
    },

    /** 读取入口筛选参数, 首次加载使用与列表分页一致的查询. */
    onLoad(options) {
        this.requestVersion = 0;
        const source = this.data.sources.some((item) => item.id === options.source)
            ? options.source
            : '';
        const type = this.data.types.some((item) => item.id === options.type) ? options.type : '';
        this.setData({source, type});
        this.reload();
    },

    /** 切换一个维度保留另一个维度, 不允许迟到请求覆盖新的筛选结果. */
    selectFilter(event) {
        const field = event.currentTarget.dataset.field;
        const value = event.currentTarget.dataset.id;
        if (!['source', 'type'].includes(field) || this.data[field] === value) {
            return;
        }
        this.setData({[field]: value});
        this.reload();
    },

    /** 创建新的请求代次, 清空旧分页并从第一页重新加载. */
    reload() {
        this.requestVersion++;
        this.setData({goods: [], page: 1, total: 0, loading: false, hasMore: true, error: ''});
        return this.loadMore();
    },

    /** 成功后才推进页码, 错误时保留当前页供用户重试. */
    async loadMore() {
        if (this.data.loading || !this.data.hasMore) {
            return;
        }
        const version = this.requestVersion;
        const page = this.data.page;
        this.setData({loading: true, error: ''});
        try {
            const response = await util.request(
                api.PearlCatalog,
                {
                    source: this.data.source,
                    type: this.data.type,
                    page,
                    limit: 20,
                },
                'GET'
            );
            if (version !== this.requestVersion) {
                return;
            }
            if (response.code !== 0) {
                throw new Error(response.msg || '商品加载失败');
            }
            const records = response.data.records || [];
            const goods = this.data.goods.concat(records);
            this.setData({
                goods,
                total: response.data.total,
                page: page + 1,
                hasMore: records.length > 0 && goods.length < response.data.total,
            });
        } catch (error) {
            if (version === this.requestVersion) {
                this.setData({error: error.message || '暂时无法加载商品, 请重试'});
            }
        } finally {
            if (version === this.requestVersion) {
                this.setData({loading: false});
            }
        }
    },

    /** 触底继续加载下一页, 加载中和到达末页时保持当前列表. */
    onReachBottom() {
        this.loadMore();
    },

    /** 重新请求当前筛选结果, 完成后结束微信下拉刷新提示. */
    async onPullDownRefresh() {
        try {
            await this.reload();
        } finally {
            wx.stopPullDownRefresh();
        }
    },

    /** 离开页面即废弃请求代次, 避免异步完成后再次更新销毁页面. */
    onUnload() {
        this.requestVersion++;
    },
});
