/* 收藏页面真实脚本回归, 不访问实际账号和业务接口. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');

/** 注入可控制的请求和确认框, 保留真实页面方法. */
function fixture() {
    let page;
    const calls = [];
    const modals = [];
    const messages = [];
    const storage = {token: 'A'};
    vm.runInNewContext(
        fs.readFileSync(
            path.join(__dirname, '../../apps/miniapp/pages/ucenter/collect/collect.js'),
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
                      }
                    : {CollectList: 'list', CollectAddOrDelete: 'toggle'},
            wx: {
                getStorageSync: (key) => storage[key],
                showModal: (options) => modals.push(options),
                showToast: (options) => messages.push(options.title),
                navigateTo() {},
            },
        }
    );
    page.setData = (values) => Object.assign(page.data, values);
    page.onShow();
    return {page, calls, modals, storage, messages};
}

/** 等待页面自行发起的异步工作完成. */
async function settle() {
    await new Promise((resolve) => setImmediate(resolve));
}

/** 收藏列表失败不会显示为空列表且可重试, 旧读取不能覆盖新列表. */
test('收藏读取失败重试及乱序隔离', async () => {
    const {page, calls} = fixture();
    calls[0].reject(new Error('offline'));
    await settle();
    assert.ok(page.data.loadError);
    const retry = page.getCollectList();
    calls[1].resolve({code: 0, data: [{valueId: 7}]});
    await retry;
    assert.equal(page.data.loadError, '');
    const old = page.getCollectList();
    const latest = page.getCollectList();
    calls[3].resolve({code: 0, data: [{valueId: 8}]});
    await latest;
    calls[2].resolve({code: 0, data: [{valueId: 7}]});
    await old;
    assert.equal(page.data.collectList[0].valueId, 8);
});

/** 从确认阶段防重且按服务端实际add/delete结果提示, 不伪报删除成功. */
test('收藏确认防重及权威动作提示', async () => {
    const {page, calls, modals, messages} = fixture();
    calls[0].resolve({code: 0, data: [{valueId: 7}]});
    await settle();
    page.touchStart({timeStamp: 0});
    page.touchEnd({timeStamp: 500});
    const event = {currentTarget: {dataset: {index: 0}}};
    page.openGoods(event);
    page.openGoods(event);
    assert.equal(modals.length, 1);
    const toggle = modals[0].success({confirm: true});
    assert.equal(calls.length, 2);
    calls[1].resolve({code: 0, data: {type: 'add'}});
    await toggle;
    assert.match(messages[0], /已收藏/);
    assert.equal(page.data.changing, false);
    assert.equal(calls[2].args[0], 'list');
});

/** 旧账号响应和离页后的确认不得展示私人数据或执行写入. */
test('收藏账号及确认生命周期隔离', async () => {
    const {page, calls, storage, modals} = fixture();
    storage.token = 'B';
    calls[0].resolve({code: 0, data: [{valueId: 7}]});
    await settle();
    assert.equal(page.data.collectList.length, 0);
    page.onShow();
    calls[1].resolve({code: 0, data: [{valueId: 7}]});
    await settle();
    page.touchStart({timeStamp: 0});
    page.touchEnd({timeStamp: 500});
    page.openGoods({currentTarget: {dataset: {index: 0}}});
    page.onUnload();
    await modals[0].success({confirm: true});
    assert.equal(calls.length, 2);
    assert.equal(page.data.collectList.length, 0);
});

/** 结果不明时只查询列表, 不自动重放切换动作. */
test('收藏写入失败后释放锁且只重读', async () => {
    const {page, calls, modals} = fixture();
    calls[0].resolve({code: 0, data: [{valueId: 7}]});
    await settle();
    page.touchStart({timeStamp: 0});
    page.touchEnd({timeStamp: 500});
    page.openGoods({currentTarget: {dataset: {index: 0}}});
    const toggle = modals[0].success({confirm: true});
    calls[1].reject(new Error('timeout'));
    await toggle;
    assert.equal(page.data.changing, false);
    assert.equal(calls.length, 3);
    assert.equal(calls[2].args[0], 'list');
});
