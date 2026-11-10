/* 结算页面真实脚本回归, 不访问账号、商品或支付接口. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');

/** 注入受控结算、定制报价和订单请求, 保留真实页面方法. */
function fixture() {
    let page;
    const calls = [];
    const messages = [];
    const storage = {token: 'A', addressId: 3};
    const app = {globalData: {userCoupon: 'NO_USE_COUPON', selectedCoupon: {}}};
    vm.runInNewContext(
        fs.readFileSync(
            path.join(__dirname, '../../apps/miniapp/pages/shopping/checkout/checkout.js'),
            'utf8'
        ),
        {
            Page: (definition) => {
                page = definition;
            },
            getApp: () => app,
            require: (name) => {
                if (name.includes('util')) {
                    return {
                        request: (...args) =>
                            new Promise((resolve, reject) => calls.push({args, resolve, reject})),
                        showErrorToast: (text) => messages.push(text),
                        showSuccessToast: (text) => messages.push(text),
                    };
                }
                if (name.includes('pay')) {
                    return {payOrder: () => Promise.resolve({code: 0})};
                }
                return {
                    CartCheckout: 'checkout',
                    CustomizationCheckoutQuote: 'quote',
                    OrderSubmit: 'submit',
                };
            },
            wx: {
                getStorageSync: (key) => storage[key],
                removeStorageSync: (key) => delete storage[key],
                showToast: (options) => messages.push(options.title),
                redirectTo() {},
                navigateTo() {},
                showModal() {},
            },
        }
    );
    page.setData = (values) => Object.assign(page.data, values);
    return {page, calls, messages, storage, app};
}

/** 等待页面异步链完成. */
async function settle() {
    await new Promise((resolve) => setImmediate(resolve));
}

/** 定制结算读取同一快照报价并四舍五入到分. */
test('结算定制报价显示金额无浮点尾差', async () => {
    const {page, calls} = fixture();
    page.onLoad({isBuy: 'true', checkoutId: 'buy_1', designSnapshotId: '5'});
    const load = page.getCheckoutInfo();
    calls[0].resolve({
        code: 0,
        data: {
            designSnapshotId: 5,
            goodsTotalPrice: 95.25,
            orderTotalPrice: 95.25,
            actualPrice: 95.25,
            couponPrice: 0,
            freightPrice: 0,
            checkedAddress: {id: 3},
            checkedGoodsList: [],
        },
    });
    await settle();
    assert.equal(calls.length, 2);
    calls[1].resolve({
        code: 0,
        data: {totalPrice: '95.25', arrangement: [{id: 1}], components: [], lengthComplete: true},
    });
    await load;
    assert.equal(page.data.designQuote.totalPrice, '95.25');
    assert.equal(page.data.goodsTotalPrice, 95.25);
    assert.equal(page.data.actualPrice, 95.25);
    assert.equal(page.data.loading, false);
    assert.equal(page.data.loadError, '');
    assert.equal(page.roundMoney(0.1 + 0.2), 0.3);
});

/** 定制报价失败阻止提交并保留明确错误. */
test('结算报价失败禁止提交', async () => {
    const {page, calls, messages} = fixture();
    page.onLoad({isBuy: 'true', checkoutId: 'buy_1', designSnapshotId: '5'});
    const load = page.getCheckoutInfo();
    calls[0].resolve({
        code: 0,
        data: {designSnapshotId: 5, checkedAddress: {id: 3}, checkedGoodsList: []},
    });
    await settle();
    assert.equal(calls.length, 2);
    calls[1].reject(new Error('quote expired'));
    await load;
    assert.equal(page.data.loadError, 'quote expired');
    assert.equal(page.data.loading, false);
    assert.ok(messages.includes('quote expired'));
    assert.equal(page.submitOrder(), false);
    assert.equal(calls.length, 2);
});

/** 提交使用已展示的分金额, 防止浮点尾差被服务端拒绝. */
test('结算提交使用四舍五入后的实付金额', async () => {
    const {page, calls} = fixture();
    page.onLoad({isBuy: 'false', checkoutId: 'cart_1'});
    const load = page.getCheckoutInfo();
    calls[0].resolve({
        code: 0,
        data: {
            designSnapshotId: null,
            goodsTotalPrice: 0.1,
            orderTotalPrice: 0.3,
            actualPrice: 0.30000000000000004,
            couponPrice: 0.2,
            freightPrice: 0,
            checkedAddress: {id: 3},
            checkedGoodsList: [],
        },
    });
    await load;
    assert.equal(page.data.actualPrice, 0.3);
    const submit = page.submitOrder();
    assert.equal(calls[1].args[1].expectedActualPrice, 0.3);
    calls[1].resolve({code: 0, data: {orderInfo: {id: 11}}});
    await submit;
});
