const util = require('../utils/util.js');
const api = require('../config/api.js');
const {readPage} = require('./page-read.js');

/** 按固定20条读取商品分页, 成功后推进页码, 筛选变化废弃原分页上下文. */
function readGoodsPage(page, filters, reset = true) {
    const signature = JSON.stringify(filters);
    const token = wx.getStorageSync('token') || '';
    let feed = page._goodsFeed;
    if (reset || !feed || feed.signature !== signature || feed.token !== token) {
        feed = {signature, token, page: 0, loading: false, hasMore: true};
        page._goodsFeed = feed;
        page.setData({goodsList: [], page: 1, hasMore: true, nomore: false});
    }
    if (feed.loading || !feed.hasMore) {
        return Promise.resolve(false);
    }
    feed.loading = true;
    const next = feed.page + 1;
    return readPage(
        page,
        'GoodsList',
        async () => {
            feed.loading = true;
            try {
                return await util.request(api.GoodsList, {...filters, page: next, size: 20});
            } finally {
                feed.loading = false;
            }
        },
        (response) => {
            const result = response.data?.goodsList;
            if (
                !result ||
                !Array.isArray(result.records) ||
                !Number.isSafeInteger(result.total) ||
                result.total < 0
            ) {
                throw new Error('商品分页响应格式错误');
            }
            feed.page = next;
            feed.hasMore = result.records.length > 0 && next * 20 < result.total;
            page.setData({
                goodsList: page.data.goodsList.concat(result.records),
                filterCategory: response.data.filterCategory || [],
                page: next,
                size: 20,
                hasMore: feed.hasMore,
                nomore: !feed.hasMore,
                searchStatus: true,
                categoryFilter: false,
            });
        }
    );
}

module.exports = {readGoodsPage};
