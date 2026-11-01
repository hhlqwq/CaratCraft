/* 足迹页面真实共享服务回归, 只替换网络和微信宿主边界. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');

/** 创建可控制完成顺序的足迹页面, 观察数据与提示. */
function fixture() {
    let page;
    let token = 'A';
    const calls = [];
    const notices = [];
    const modals = [];
    const util = {
        showErrorToast: (text) => notices.push(text),
        request: (url, data) =>
            new Promise((resolve, reject) => calls.push({url, data, resolve, reject})),
    };
    const wx = {
        getStorageSync: () => token,
        showToast: (options) => notices.push(options.title),
        navigateTo: (options) => notices.push(options.url),
        stopPullDownRefresh() {},
        showModal: (options) => modals.push(options),
    };
    /** 保持页面和读写公共服务为真实源码. */
    function load(file) {
        if (file.endsWith('/utils/util.js')) {
            return util;
        }
        if (file.endsWith('/config/api.js')) {
            return {FootprintList: 'list', FootprintDelete: 'delete'};
        }
        const box = {exports: {}};
        vm.runInNewContext(fs.readFileSync(file, 'utf8'), {
            module: box,
            wx,
            Page: (definition) => {
                page = definition;
            },
            require: (name) => load(path.resolve(path.dirname(file), name).replaceAll('\\', '/')),
        });
        return box.exports;
    }
    load(path.resolve('apps/miniapp/pages/ucenter/footprint/footprint.js').replaceAll('\\', '/'));
    page.setData = (patch) => Object.assign(page.data, patch);
    const initial = page.onLoad({});
    page.onShow();
    return {
        page,
        calls,
        notices,
        initial,
        modals,
        setToken: (next) => {
            token = next;
        },
    };
}

/** 真实分页响应保持唯一字段, 日期由服务端明确提供. */
function response(total, records) {
    return {code: 0, data: {total, records}};
}

/** 首次只读一次, 分页后合并同日分组, 失败保留已读内容可重试. */
test('足迹分页分组及读取失败恢复', async () => {
    const f = fixture();
    assert.equal(f.calls.length, 1);
    f.page.setData({size: 1});
    f.calls[0].resolve(response(2, [{id: 2, goodsId: 5, date: '2026-11-01'}]));
    await f.initial;
    const second = f.page.onReachBottom();
    assert.equal(f.calls[1].data.page, 2);
    f.calls[1].reject(new Error('断网'));
    await second;
    assert.equal(f.page.data.records.length, 1);
    assert.ok(f.page.data.error);
    const retry = f.page.retryLoad();
    f.calls[2].resolve(response(2, [{id: 1, goodsId: 6, date: '2026-11-01'}]));
    await retry;
    assert.equal(f.page.data.footprintGroups.length, 1);
    assert.equal(f.page.data.footprintGroups[0].items.length, 2);
    assert.equal(f.page.data.nomore, true);
});

/** 从确认起锁定删除, 失败保持列表, 成功重新读取第一页. */
test('足迹删除确认防重及失败恢复', async () => {
    const f = fixture();
    f.calls[0].resolve(response(1, [{id: 2, goodsId: 5, date: '2026-11-01'}]));
    await f.initial;
    const event = {currentTarget: {dataset: {id: 2}}};
    const deletion = f.page.deleteItem(event);
    await f.page.deleteItem(event);
    assert.equal(f.modals.length, 1);
    f.modals[0].success({confirm: true});
    await Promise.resolve();
    assert.equal(f.calls[1].url, 'delete');
    f.calls[1].reject(new Error('失败'));
    await deletion;
    assert.equal(f.page.data.records.length, 1);
    const next = f.page.deleteItem(event);
    f.modals[1].success({confirm: true});
    await Promise.resolve();
    f.calls[2].resolve({code: 0});
    await next;
    assert.equal(f.calls[3].data.page, 1);
    f.calls[3].resolve(response(0, []));
    await new Promise((resolve) => setImmediate(resolve));
    assert.equal(f.page.data.footprintGroups.length, 0);
});

/** 账号切换清空旧日期分组, 在途旧写入和读取不影响新账号. */
test('足迹账号和离页隔离', async () => {
    const f = fixture();
    f.calls[0].resolve(response(1, [{id: 2, goodsId: 5, date: '2026-11-01'}]));
    await f.initial;
    const pending = f.page.deleteItem({currentTarget: {dataset: {id: 2}}});
    f.setToken('B');
    const show = f.page.onShow();
    f.modals[0].success({confirm: true});
    assert.equal(await pending, false);
    assert.equal(f.calls.length, 2);
    assert.equal(f.page.data.footprintGroups.length, 0);
    f.page.onUnload();
    f.calls[1].resolve(response(1, [{id: 9, date: '2026-11-02'}]));
    await show;
    await new Promise((resolve) => setImmediate(resolve));
    assert.equal(f.page.data.records.length, 0);
});
