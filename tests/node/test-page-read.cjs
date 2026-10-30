/* 页面读取隔离测试仅执行真实共享脚本, 不调用微信或业务环境. */
const {test} = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');

/** 加载真实读取服务, 使用可控队列模拟乱序和失败响应. */
function fixture() {
    let token = 'A';
    const calls = [];
    const context = {
        module: {exports: {}},
        wx: {getStorageSync: () => token, stopPullDownRefresh() {}},
    };
    vm.runInNewContext(fs.readFileSync('apps/miniapp/services/page-read.js', 'utf8'), context);
    const {readPage, withPageReads} = context.module.exports;
    const page = withPageReads({
        data: {rows: []},
        /** 初始化列表, 实际请求由测试控制. */
        onLoad() {},
    });
    page.setData = (values) => Object.assign(page.data, values);
    page.onLoad({id: '11'});
    return {
        page,
        calls,
        setToken: (value) => {
            token = value;
        },
        read: (key = 'list') =>
            readPage(
                page,
                key,
                () => new Promise((resolve, reject) => calls.push({resolve, reject})),
                (response) => page.setData({rows: response.data})
            ),
    };
}

/** 失败可见且只重试失败区块, 不重复成功读取. */
test('页面读取独立区块失败后显式重试', async () => {
    const f = fixture();
    const first = f.read('first');
    const second = f.read('second');
    f.calls[0].resolve({code: 0, data: ['ok']});
    f.calls[1].reject(new Error('offline'));
    await Promise.all([first, second]);
    assert.equal(f.page.data.readLoading, false);
    assert.ok(f.page.data.readError);
    const retry = f.page.retryRead();
    assert.equal(f.calls.length, 3);
    f.calls[2].resolve({code: 0, data: ['retry']});
    await retry;
    assert.equal(f.page.data.readError, '');
    assert.equal(f.page.data.rows[0], 'retry');
});

/** 同接口较早的响应不能覆盖更新的筛选结果. */
test('页面读取丢弃同接口旧响应', async () => {
    const f = fixture();
    const first = f.read();
    const second = f.read();
    f.calls[1].resolve({code: 0, data: ['new']});
    await second;
    f.calls[0].resolve({code: 0, data: ['old']});
    await first;
    assert.equal(f.page.data.rows[0], 'new');
});

/** 账号改变后迟到响应失效, 展示时清理原账号数据. */
test('页面读取隔离账号变化及旧内容', async () => {
    const f = fixture();
    f.page.setData({rows: ['private']});
    const pending = f.read();
    f.setToken('B');
    f.calls[0].resolve({code: 0, data: ['old-account']});
    await pending;
    assert.equal(f.page.data.rows[0], 'private');
    f.page.onShow();
    assert.equal(f.page.data.rows.length, 0);
    assert.equal(f.page.data.readLoading, false);
});

/** 页面销毁后成功和失败均不得回写状态. */
test('页面读取卸载后不更新视图', async () => {
    const f = fixture();
    const pending = f.read();
    f.page.onUnload();
    f.page.setData = () => {
        throw new Error('unloaded');
    };
    f.calls[0].reject(new Error('offline'));
    assert.equal(await pending, false);
});

/** 业务失败不被当成空数据或成功内容. */
test('页面读取拒绝业务错误与损坏响应', async () => {
    const f = fixture();
    const pending = f.read();
    f.calls[0].resolve({code: 500, data: ['wrong']});
    assert.equal(await pending, false);
    assert.ok(f.page.data.readError);
    assert.equal(f.page.data.rows.length, 0);
});

/** 正在读取和账号切换未展示前都禁止重试旧上下文. */
test('页面读取重试防重且不跨账号', async () => {
    const f = fixture();
    const pending = f.read();
    await f.page.retryRead();
    assert.equal(f.calls.length, 1);
    f.calls[0].reject(new Error('offline'));
    await pending;
    f.setToken('B');
    await f.page.retryRead();
    assert.equal(f.calls.length, 1);
});
