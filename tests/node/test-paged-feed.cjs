/* 分页列表读取真实共享脚本, 不访问业务接口或微信窗口. */
const {test} = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');

/** 请求队列使网络顺序、账号切换和失败均可重复控制. */
function fixture(options = {}) {
    const calls = [];
    let token = 'A';
    const context = {
        module: {exports: {}},
        wx: {getStorageSync: () => token, stopPullDownRefresh() {}},
        require: () => ({
            request: (url, data) =>
                new Promise((resolve, reject) => calls.push({url, data, resolve, reject})),
        }),
    };
    vm.runInNewContext(fs.readFileSync('apps/miniapp/services/paged-feed.js', 'utf8'), context);
    const page = context.module.exports.createPagedFeed({
        url: '/list',
        listField: 'rows',
        responseField: 'records',
        ...options,
    });
    page.setData = (values) => Object.assign(page.data, values);
    return {
        page,
        calls,
        setToken: (value) => {
            token = value;
        },
    };
}

/** 构造明确的记录数分页格式, 最后一页允许少于每页条数. */
function response(total, count, id = 1) {
    return {
        code: 0,
        data: {total, records: Array.from({length: count}, (_, index) => ({id: id + index}))},
    };
}

/** 记录总数11只应请求两页, 不应误当11页. */
test('连续列表按记录总数判断末页', async () => {
    const f = fixture();
    const first = f.page.onLoad();
    f.calls[0].resolve(response(11, 10));
    await first;
    assert.equal(f.page.data.totalPages, 2);
    const next = f.page.onReachBottom();
    assert.equal(f.calls[1].data.page, 2);
    f.calls[1].resolve(response(11, 1, 11));
    await next;
    assert.equal(f.page.data.rows.length, 11);
    assert.equal(f.page.data.hasMore, false);
    f.page.onReachBottom();
    assert.equal(f.calls.length, 2);
});

/** 专题最后不足一页也可进入, 翻页成功后替换内容. */
test('专题分页保留最后不足一页的记录', async () => {
    const f = fixture({replace: true});
    const first = f.page.onLoad();
    f.calls[0].resolve(response(11, 10));
    await first;
    const next = f.page.nextPage();
    f.calls[1].resolve(response(11, 1, 11));
    await next;
    assert.equal(f.page.data.page, 2);
    assert.equal(f.page.data.rows.length, 1);
    assert.equal(f.page.data.rows[0].id, 11);
});

/** 网络失败不前进页码或丢弃当前内容, 重试仍读取失败页. */
test('列表分页失败保持内容并可重试同页', async () => {
    const f = fixture();
    const first = f.page.onLoad();
    f.calls[0].resolve(response(20, 10));
    await first;
    const next = f.page.onReachBottom();
    f.calls[1].reject(new Error('offline'));
    await next;
    assert.equal(f.page.data.loading, false);
    assert.equal(f.page.data.rows.length, 10);
    assert.equal(f.page.data.page, 1);
    const retry = f.page.retryLoad();
    assert.equal(f.calls[2].data.page, 2);
    f.calls[2].resolve(response(20, 10, 11));
    await retry;
    assert.equal(f.page.data.rows.length, 20);
});

/** 加载中重复触底不发请求, 损坏格式不被解释成空内容或成功. */
test('列表读取防重并拒绝损坏响应', async () => {
    const f = fixture();
    const pending = f.page.onLoad();
    await f.page.onReachBottom();
    assert.equal(f.calls.length, 1);
    f.calls[0].resolve({code: 0, data: {list: [], total: 0}});
    await pending;
    assert.match(f.page.data.error, /格式/);
    assert.equal(f.page.data.loading, false);
});

/** 前账号后续页不能追加到新账号已经重置的列表. */
test('私人列表隔离账号切换和迟到响应', async () => {
    const f = fixture({refreshOnShow: true});
    const first = f.page.onLoad();
    f.page.onShow();
    f.calls[0].resolve(response(20, 10));
    await first;
    const old = f.page.onReachBottom();
    f.setToken('B');
    const current = f.page.onShow();
    assert.equal(f.page.data.rows.length, 0);
    f.calls[2].resolve(response(1, 1, 100));
    await current;
    f.calls[1].resolve(response(20, 10, 11));
    await old;
    assert.equal(f.page.data.rows.length, 1);
    assert.equal(f.page.data.rows[0].id, 100);
});

/** 离页后不应用成功响应或触发下一次读取. */
test('列表离页后废弃响应', async () => {
    const f = fixture();
    const pending = f.page.onLoad();
    f.page.onUnload();
    f.calls[0].resolve(response(1, 1));
    await pending;
    assert.equal(f.page.data.rows.length, 0);
    assert.equal(await f.page.readFeed(2), false);
});
