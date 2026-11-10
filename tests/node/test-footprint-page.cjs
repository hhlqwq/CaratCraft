/* 浏览足迹页面真实脚本回归, 不访问账号或业务接口. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');

/** 注入受控请求、确认框和存储, 保留真实页面方法. */
function fixture() {
    let page;
    const calls = [];
    const messages = [];
    const modals = [];
    const storage = {token: 'A'};
    vm.runInNewContext(
        fs.readFileSync(
            path.join(__dirname, '../../apps/miniapp/pages/ucenter/footprint/footprint.js'),
            'utf8'
        ),
        {
            Page: (definition) => {
                page = definition;
            },
            require: (name) =>
                name.includes('util')
                    ? {
                          request: (...args) =>
                              new Promise((resolve, reject) => calls.push({args, resolve, reject})),
                          showErrorToast: (text) => messages.push(text),
                          showSuccessToast: (text) => messages.push(text),
                      }
                    : {FootprintList: 'list', FootprintDelete: 'delete'},
            wx: {
                getStorageSync: (key) => storage[key],
                stopPullDownRefresh() {},
                navigateTo() {},
                showModal: (options) => modals.push(options),
            },
        }
    );
    page.setData = (values) => Object.assign(page.data, values);
    page.onLoad();
    return {page, calls, messages, modals, storage};
}

/** 等待页面异步链完成. */
async function settle() {
    await new Promise((resolve) => setImmediate(resolve));
}

/** 首屏失败明确提示并可重试, 成功后记录分页状态. */
test('足迹首屏失败可重试并保留分页结果', async () => {
    const {page, calls} = fixture();
    const first = page.onShow();
    calls[0].reject(new Error('offline'));
    await first;
    assert.match(page.data.error, /offline/);
    assert.equal(page.data.loading, false);
    const retry = page.loadFootprints(true);
    calls[1].resolve({
        code: 0,
        data: {data: [[{id: 2, addTime: '2026-11-09', goodsId: 9}]], hasMore: true, count: 3},
    });
    await retry;
    assert.equal(page.data.error, '');
    assert.equal(page.data.footprintList.length, 1);
    assert.equal(page.data.page, 2);
    assert.equal(page.data.hasMore, true);
});

/** 触底追加下一页并按日期合并, 不重复同一足迹. */
test('足迹分页追加合并同一天记录', async () => {
    const {page, calls} = fixture();
    const initial = page.onShow();
    calls[0].resolve({
        code: 0,
        data: {
            data: [[{id: 2, addTime: '2026-11-09'}], [{id: 1, addTime: '2026-11-08'}]],
            hasMore: true,
        },
    });
    await initial;
    const more = page.onReachBottom();
    calls[1].resolve({
        code: 0,
        data: {
            data: [
                [
                    {id: 2, addTime: '2026-11-09'},
                    {id: 3, addTime: '2026-11-09'},
                ],
                [{id: 4, addTime: '2026-11-07'}],
            ],
            hasMore: false,
        },
    });
    await more;
    assert.equal(page.data.footprintList.length, 3);
    assert.equal(page.data.footprintList[0].map((row) => row.id).join(','), '2,3');
    assert.equal(page.data.footprintList[1].map((row) => row.id).join(','), '1');
    assert.equal(page.data.footprintList[2].map((row) => row.id).join(','), '4');
    assert.equal(page.data.hasMore, false);
    assert.equal(calls[1].args[1].page, 2);
});

/** 账号切换后迟到的足迹响应不得写入新账号页面. */
test('足迹账号切换隔离迟到响应', async () => {
    const {page, calls, storage} = fixture();
    const load = page.onShow();
    assert.equal(calls.length, 1);
    storage.token = 'B';
    const fresh = page.onShow();
    assert.equal(calls.length, 2);
    calls[0].resolve({code: 0, data: {data: [[{id: 2, addTime: '2026-11-09'}]], hasMore: false}});
    await load;
    assert.equal(page.data.footprintList.length, 0);
    assert.equal(page.data.loading, true);
    calls[1].resolve({code: 0, data: {data: [], hasMore: false}});
    await fresh;
    assert.equal(page.data.loading, false);
});

/** 长按删除从确认阶段防重, 失败后释放锁并重读权威列表. */
test('足迹删除确认防重且失败重读', async () => {
    const {page, calls, modals, messages} = fixture();
    const initial = page.onShow();
    calls[0].resolve({
        code: 0,
        data: {data: [[{id: 2, addTime: '2026-11-09', goodsId: 9}]], hasMore: false},
    });
    await initial;
    page.touchStart({timeStamp: 0});
    page.touchEnd({timeStamp: 500});
    const event = {currentTarget: {dataset: {footprint: {id: 2, goodsId: 9}}}};
    page.deleteItem(event);
    const first = modals[0].success({confirm: true});
    const second = modals[0].success({confirm: true});
    assert.equal(calls.length, 2);
    calls[1].reject(new Error('offline'));
    await Promise.all([first, second]);
    await settle();
    assert.equal(page.data.deleting, false);
    assert.ok(messages.includes('offline'));
    assert.equal(calls.length, 3);
});

/** 短按进入商品详情, 不触发删除确认. */
test('足迹短按不触发删除确认', () => {
    const {page, modals} = fixture();
    page.touchStart({timeStamp: 0});
    page.touchEnd({timeStamp: 100});
    page.deleteItem({currentTarget: {dataset: {footprint: {id: 2, goodsId: 9}}}});
    assert.equal(modals.length, 0);
});
