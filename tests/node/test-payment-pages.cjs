/* 支付页面回归不调用真实微信支付, 只验证页面与共享服务的可信状态契约. */
const {test} = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');

/** 以可控网络和支付边界加载实际页面, 不使用真实订单或账号. */
function fixture(name) {
    let page;
    let token = 'A';
    let payments = 0;
    const navigation = [];
    const calls = [];
    let request = () =>
        Promise.resolve({
            code: 0,
            data: {orderInfo: {id: 8, actualPrice: '10.00', payStatus: 0, orderStatus: 0}},
        });
    let payment = () => Promise.resolve({code: 0});
    const context = {
        Page: (definition) => {
            page = definition;
        },
        wx: {
            getStorageSync: () => token,
            redirectTo: (options) => navigation.push(options.url),
        },
        require: (path) =>
            path.includes('services/pay')
                ? {
                      payOrder: (...args) => {
                          payments++;
                          return payment(...args);
                      },
                  }
                : path.includes('util')
                  ? {
                        request: (...args) => {
                            calls.push(args);
                            return request(...args);
                        },
                    }
                  : {OrderDetail: 'detail', OrderQuery: 'query'},
    };
    vm.runInNewContext(
        fs.readFileSync('apps/miniapp/pages/' + name + '/' + name + '.js', 'utf8'),
        context
    );
    page.setData = (values) => Object.assign(page.data, values);
    return {
        page,
        calls,
        navigation,
        setToken: (next) => {
            token = next;
        },
        setRequest: (next) => {
            request = next;
        },
        setPayment: (next) => {
            payment = next;
        },
        paymentCount: () => payments,
    };
}

/** 未提供订单号时不猜测默认订单, 更不采用路由的成功标记. */
test('支付结果拒绝默认订单和伪造成功标记', async () => {
    const f = fixture('payResult');
    await f.page.onLoad({status: 'true'});
    assert.equal(f.calls.length, 0);
    assert.equal(f.page.data.orderId, 0);
    assert.equal(f.page.data.status, 'unknown');
});

/** 只有服务端查单成功后才能展示付款成功. */
test('支付结果由服务端响应确认', async () => {
    const f = fixture('payResult');
    f.setRequest(() => Promise.resolve({code: 400, msg: '支付中'}));
    await f.page.onLoad({orderId: '8', status: 'true'});
    assert.equal(f.page.data.status, 'unknown');
    assert.equal(f.page.data.checking, false);
    f.setRequest(() => Promise.resolve({code: 0, data: '支付成功'}));
    await f.page.updateSuccess();
    assert.equal(f.page.data.status, 'paid');
});

/** 查询失败和离页均不能产生假的成功状态. */
test('支付结果处理网络失败并隔离迟到响应', async () => {
    const f = fixture('payResult');
    f.setRequest(() => Promise.reject(new Error('offline')));
    await f.page.onLoad({orderId: 8});
    assert.equal(f.page.data.status, 'unknown');
    assert.equal(f.page.data.checking, false);
    let resolve;
    f.setRequest(
        () =>
            new Promise((done) => {
                resolve = done;
            })
    );
    const pending = f.page.updateSuccess();
    f.page.onUnload();
    resolve({code: 0});
    await pending;
    assert.equal(f.page.data.status, 'unknown');
});

/** 支付金额来自本人订单, 不能使用路由金额替换可信展示. */
test('支付页面读取服务端金额并统一支付入口', async () => {
    const f = fixture('pay');
    await f.page.onLoad({orderId: 8, actualPrice: '0.01'});
    assert.equal(f.page.data.actualPrice, '10.00');
    assert.equal(await f.page.startPay(), true);
    assert.equal(f.paymentCount(), 1);
    assert.equal(f.navigation[0], '/pages/payResult/payResult?orderId=8');
    assert.equal(f.page.data.payable, false);
});

/** 连续点击仅提交一次, 失败保留订单且释放锁供用户明确重试. */
test('支付页面防重并恢复失败状态', async () => {
    const f = fixture('pay');
    await f.page.onLoad({orderId: 8});
    let reject;
    f.setPayment(
        () =>
            new Promise((resolve, fail) => {
                reject = fail;
            })
    );
    const first = f.page.startPay();
    await f.page.startPay();
    assert.equal(f.paymentCount(), 1);
    reject(new Error('用户取消'));
    await first;
    assert.equal(f.page.data.paying, false);
    assert.equal(f.page.data.orderId, 8);
    assert.match(f.page.data.error, /取消/);
});

/** 已支付或不可售状态不再允许当前页触发新的支付. */
test('非待付款订单拒绝支付', async () => {
    const f = fixture('pay');
    f.setRequest(() =>
        Promise.resolve({
            code: 0,
            data: {orderInfo: {id: 8, actualPrice: '10.00', payStatus: 2, orderStatus: 201}},
        })
    );
    await f.page.onLoad({orderId: 8});
    await f.page.startPay();
    assert.equal(f.paymentCount(), 0);
});

/** 前账号的读取不能在身份变化后恢复支付按钮或显示私人金额. */
test('支付页面读取结果核对账号', async () => {
    const f = fixture('pay');
    let resolve;
    f.setRequest(
        () =>
            new Promise((done) => {
                resolve = done;
            })
    );
    const pending = f.page.onLoad({orderId: 8});
    f.setToken('B');
    resolve({
        code: 0,
        data: {orderInfo: {id: 8, actualPrice: '10.00', payStatus: 0, orderStatus: 0}},
    });
    await pending;
    assert.equal(f.page.data.actualPrice, '');
    assert.equal(f.page.data.payable, false);
});
