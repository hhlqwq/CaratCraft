/* 地址页面使用可控制响应验证账号、生命周期及写入边界, 不调用微信. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');

/** 装载真实共享页面, 单独控制读取、确认框和账号状态. */
function fixture(checkout = false) {
    const calls = [];
    const modals = [];
    const storage = {token: 'A'};
    let navigation = 0;
    const module = {exports: {}};
    const util = {
        request: (...args) => new Promise((resolve, reject) => calls.push({args, resolve, reject})),
        showErrorToast() {},
    };
    vm.runInNewContext(
        fs.readFileSync(
            path.join(__dirname, '../../apps/miniapp/services/address-list-page.js'),
            'utf8'
        ),
        {
            module,
            require: (name) =>
                name.includes('util') ? util : {AddressList: 'list', AddressDelete: 'delete'},
            wx: {
                getStorageSync: (key) => storage[key],
                setStorageSync: (key, value) => {
                    storage[key] = value;
                },
                showModal: (options) => modals.push(options),
                navigateBack: () => navigation++,
                navigateTo: () => navigation++,
            },
        }
    );
    const page = module.exports.createAddressListPage(checkout);
    page.setData = (values) => Object.assign(page.data, values);
    return {page, calls, modals, storage, navigations: () => navigation};
}

/** 清空当前Promise回调队列, 保持响应次序可控. */
async function settle() {
    await new Promise((resolve) => setImmediate(resolve));
}

/** 构造当前节点事件, 子节点故意携带不同地址以验证取值来源. */
function event(id = 7) {
    return {currentTarget: {dataset: {addressId: id}}, target: {dataset: {addressId: 99}}};
}

for (const checkout of [false, true]) {
    /** 两个入口均拒绝旧读取覆盖新列表, 并可从失败恢复. */
    test(`地址列表乱序及失败重试: checkout=${checkout}`, async () => {
        const {page, calls} = fixture(checkout);
        page.onShow();
        const latest = page.getAddressList();
        calls[1].resolve({code: 0, data: [{id: 7}]});
        await latest;
        calls[0].resolve({code: 0, data: [{id: 99}]});
        await settle();
        assert.equal(page.data.addressList[0].id, 7);
        const failure = page.getAddressList();
        calls[2].reject(new Error('offline'));
        await failure;
        assert.ok(page.data.loadError);
        assert.equal(page.data.loading, false);
        assert.equal(page.data.addressList.length, 0);
        const retry = page.getAddressList();
        calls[3].resolve({code: 0, data: []});
        await retry;
        assert.equal(page.data.loadError, '');
    });

    /** 迟到响应不得泄露旧账号地址或在离页后更新状态. */
    test(`地址账号及离页隔离: checkout=${checkout}`, async () => {
        const {page, calls, storage} = fixture(checkout);
        page.onShow();
        storage.token = 'B';
        calls[0].resolve({code: 0, data: [{id: 7}]});
        await settle();
        assert.equal(page.data.addressList.length, 0);
        page.onShow();
        page.onUnload();
        calls[1].resolve({code: 0, data: [{id: 99}]});
        await settle();
        assert.equal(page.data.addressList.length, 0);
    });
}

/** 确认期间和写入期间均只执行一次删除, 网络不明确后只重读不重放. */
test('地址删除确认防重及失败恢复', async () => {
    const {page, calls, modals} = fixture();
    page.onShow();
    calls[0].resolve({code: 0, data: [{id: 7}]});
    await settle();
    page.deleteAddress(event());
    page.deleteAddress(event());
    assert.equal(modals.length, 1);
    const saving = modals[0].success({confirm: true});
    page.deleteAddress(event());
    assert.equal(calls.length, 2);
    assert.equal(calls[1].args[1].id, 7);
    calls[1].reject(new Error('offline'));
    await saving;
    assert.equal(page.data.deleting, false);
    assert.equal(calls.length, 3);
    assert.equal(calls[2].args[0], 'list');
    calls[2].resolve({code: 0, data: [{id: 7}]});
    await settle();
    assert.equal(page.addressAvailable(7), true);
});

/** 切换账号后确认旧弹窗不会发送原账号的写请求. */
test('地址删除确认后的账号复核及确认失败解锁', async () => {
    const {page, calls, modals, storage} = fixture();
    page.onShow();
    calls[0].resolve({code: 0, data: [{id: 7}]});
    await settle();
    page.deleteAddress(event());
    modals[0].fail();
    assert.equal(page.data.deleting, false);
    page.deleteAddress(event());
    storage.token = 'B';
    await modals[1].success({confirm: true});
    assert.equal(calls.length, 1);
});

/** 结算只能选择当前成功加载且属于原会话的地址. */
test('结算拒绝失效地址和切换账号后的节点', async () => {
    const {page, calls, storage, navigations} = fixture(true);
    page.onShow();
    page.selectAddress(event());
    assert.equal(navigations(), 0);
    calls[0].resolve({code: 0, data: [{id: 7}]});
    await settle();
    page.selectAddress(event(99));
    assert.equal(navigations(), 0);
    page.selectAddress(event());
    assert.equal(storage.addressId, 7);
    assert.equal(navigations(), 1);
    storage.token = 'B';
    page.selectAddress(event());
    assert.equal(navigations(), 1);
});
