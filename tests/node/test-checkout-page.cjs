/* 真实结算页面和共享读写服务回归, 不访问微信或业务账号. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');

/** 装配真实CommonJS依赖并用可控网络观察页面状态和导航. */
function fixture() {
    let page;
    let token = 'A';
    const calls = [];
    const navigation = [];
    const messages = [];
    const app = {globalData: {}};
    const api = {CartCheckout: 'checkout', OrderSubmit: 'submit'};
    const util = {
        showErrorToast: (message) => messages.push(message),
        request: (url, data) =>
            new Promise((resolve, reject) => calls.push({url, data, resolve, reject})),
    };
    const wx = {
        getStorageSync: (key) => (key === 'token' ? token : key === 'addressId' ? 12 : undefined),
        stopPullDownRefresh() {},
        removeStorageSync() {},
        redirectTo: (options) => {
            navigation.push(options.url);
            options.fail?.({});
            options.complete?.();
        },
    };
    const cache = new Map();
    /** 仅替换传输和宿主边界, 页面与公共服务均加载仓库原件. */
    function load(file) {
        if (file.endsWith('/utils/util.js')) {
            return util;
        }
        if (file.endsWith('/config/api.js')) {
            return api;
        }
        if (cache.has(file)) {
            return cache.get(file);
        }
        const box = {exports: {}};
        vm.runInNewContext(fs.readFileSync(file, 'utf8'), {
            module: box,
            console,
            wx,
            Page: (definition) => {
                page = definition;
            },
            getApp: () => app,
            require: (name) => load(path.resolve(path.dirname(file), name).replaceAll('\\', '/')),
        });
        cache.set(file, box.exports);
        return box.exports;
    }
    load(path.resolve('apps/miniapp/pages/shopping/checkout/checkout.js').replaceAll('\\', '/'));
    page.setData = (patch) => Object.assign(page.data, patch);
    page.onLoad({});
    /** 提供一次明确的服务端结算成功响应. */
    function resolveCheckout(call, price = '10.00') {
        call.resolve({
            code: 0,
            data: {
                checkedGoodsList: [{goodsId: 1, productId: 2, number: 1}],
                checkedAddress: {id: 12},
                actualPrice: price,
            },
        });
    }
    /** 完成首次真实页面读取, 后续用例只操作提交边界. */
    async function ready() {
        const reading = page.onShow();
        resolveCheckout(calls.at(-1));
        await reading;
    }
    return {
        page,
        calls,
        navigation,
        messages,
        ready,
        resolveCheckout,
        setToken: (next) => {
            token = next;
        },
    };
}

/** 加载和失败期间不允许以旧价或默认零元提交. */
test('结算读取失败阻止下单并允许显式重试', async () => {
    const f = fixture();
    const reading = f.page.onShow();
    await f.page.submitOrder();
    assert.equal(f.calls.length, 1);
    f.calls[0].reject(new Error('断网'));
    await reading;
    assert.ok(f.page.data.readError);
    assert.equal(f.page.data.actualPrice, null);
    await f.page.submitOrder();
    assert.equal(f.calls.length, 1);
    const retry = f.page.retryRead();
    f.resolveCheckout(f.calls[1]);
    await retry;
    assert.equal(f.page.data.checkoutReady, true);
});

/** 后发读取优先, 旧报价不能覆盖新的金额. */
test('结算隔离乱序报价', async () => {
    const f = fixture();
    const first = f.page.onShow();
    const second = f.page.getCheckoutInfo();
    f.resolveCheckout(f.calls[1], '20.00');
    await second;
    f.resolveCheckout(f.calls[0], '10.00');
    await first;
    assert.equal(f.page.data.actualPrice, '20.00');
});

/** 连点只创建一次订单, 导航失败后仍只能继续已有订单付款. */
test('结算冻结明细防重并恢复已有订单付款', async () => {
    const f = fixture();
    await f.ready();
    const submit = f.page.submitOrder();
    await f.page.submitOrder();
    assert.equal(f.calls.length, 2);
    assert.equal(f.calls[1].url, 'submit');
    assert.equal(
        JSON.stringify(f.calls[1].data.expectedItems),
        '[{"goodsId":1,"productId":2,"number":1}]'
    );
    f.calls[1].resolve({code: 0, data: {orderInfo: {id: 98}}});
    await submit;
    assert.equal(f.page.data.createdOrderId, 98);
    f.page.onShow();
    await f.page.submitOrder();
    assert.equal(f.calls.length, 2);
    assert.deepEqual(f.navigation, ['/pages/pay/pay?orderId=98', '/pages/pay/pay?orderId=98']);
});

/** 账号切换和离页均不能接收旧下单结果或跳到上一账号支付页. */
test('结算写入结果核对账号与页面', async () => {
    for (const leave of [false, true]) {
        const f = fixture();
        await f.ready();
        const submit = f.page.submitOrder();
        if (leave) {
            f.page.onUnload();
        } else {
            f.setToken('B');
            f.page.onShow();
        }
        f.calls[1].resolve({code: 0, data: {orderInfo: {id: 98}}});
        await submit;
        assert.equal(f.navigation.length, 0);
        assert.equal(f.page.data.createdOrderId, null);
    }
});

/** 服务端设计身份与路由不一致时, 不能继续使用旧快照. */
test('结算拒绝错配设计和损坏响应', async () => {
    const f = fixture();
    f.page.setData({designSnapshotId: 88});
    const reading = f.page.onShow();
    f.calls[0].resolve({
        code: 0,
        data: {
            designSnapshotId: 99,
            checkedAddress: {id: 12},
            checkedGoodsList: [{goodsId: 1, productId: 2, number: 1}],
            actualPrice: '10.00',
        },
    });
    await reading;
    assert.ok(f.page.data.readError);
    assert.equal(f.page.data.checkoutReady, false);
});
