const {test} = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');

/** 装载真实商品与读取服务, 仅将网络边界替换为可控队列. */
function fixture() {
    const calls = [];
    const wx = {getStorageSync: () => 'A', stopPullDownRefresh() {}};
    const shared = {module: {exports: {}}, wx};
    vm.runInNewContext(fs.readFileSync('apps/miniapp/services/page-read.js', 'utf8'), shared);
    const context = {
        module: {exports: {}},
        wx,
        require: (name) =>
            name === './page-read.js'
                ? shared.module.exports
                : name.includes('api')
                  ? {GoodsList: '/goods/list'}
                  : {
                        request: (url, data) =>
                            new Promise((resolve, reject) =>
                                calls.push({url, data, resolve, reject})
                            ),
                    },
    };
    vm.runInNewContext(fs.readFileSync('apps/miniapp/services/goods-feed.js', 'utf8'), context);
    const page = shared.module.exports.withPageReads({data: {goodsList: []}});
    page.setData = (values) => Object.assign(page.data, values);
    page.onLoad({});
    return {
        page,
        calls,
        read: (filters = {}, reset = true) =>
            context.module.exports.readGoodsPage(page, filters, reset),
    };
}

/** 记录数与页大小决定末页, 不再一次请求1000条. */
test('商品列表固定20条且可加载不足一页的末页', async () => {
    const f = fixture();
    const first = f.read();
    assert.equal(f.calls[0].data.size, 20);
    f.calls[0].resolve({code: 0, data: {goodsList: {records: Array(20).fill({id: 1}), total: 21}}});
    await first;
    const next = f.read({}, false);
    assert.equal(f.calls[1].data.page, 2);
    f.calls[1].resolve({code: 0, data: {goodsList: {records: [{id: 21}], total: 21}}});
    await next;
    assert.equal(f.page.data.goodsList.length, 21);
    assert.equal(f.page.data.hasMore, false);
});

/** 读取中触底不重复请求, 失败重试仍为原页且可继续翻页. */
test('商品列表防重并在失败后重试同页', async () => {
    const f = fixture();
    const first = f.read();
    await f.read({}, false);
    assert.equal(f.calls.length, 1);
    f.calls[0].reject(new Error('offline'));
    await first;
    const retry = f.page.retryRead();
    assert.equal(f.calls[1].data.page, 1);
    f.calls[1].resolve({code: 0, data: {goodsList: {records: Array(20).fill({id: 1}), total: 21}}});
    await retry;
    const next = f.read({}, false);
    assert.equal(f.calls[2].data.page, 2);
    f.calls[2].resolve({code: 0, data: {goodsList: {records: [{id: 2}], total: 21}}});
    await next;
});

/** 切换分类重置分页并废弃在途旧分类数据. */
test('商品列表切换分类不混入旧请求', async () => {
    const f = fixture();
    const old = f.read({categoryId: 1});
    const current = f.read({categoryId: 2});
    f.calls[1].resolve({code: 0, data: {goodsList: {records: [{id: 2}], total: 1}}});
    await current;
    f.calls[0].resolve({code: 0, data: {goodsList: {records: [{id: 1}], total: 1}}});
    await old;
    assert.equal(f.page.data.goodsList.length, 1);
    assert.equal(f.page.data.goodsList[0].id, 2);
});

/** 损坏分页不能推进页码或被解释成正常空结果. */
test('商品列表拒绝损坏分页格式', async () => {
    const f = fixture();
    const first = f.read();
    f.calls[0].resolve({code: 0, data: {goodsList: {records: [], total: '21'}}});
    assert.equal(await first, false);
    assert.ok(f.page.data.readError);
    assert.equal(f.page._goodsFeed.page, 0);
});
