/* 小程序订单详情实际脚本回归, 全部网络、确认框和支付使用隔离边界. */
const {test} = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');

/** 建立可控制账号、确认框和请求顺序的订单详情页面. */
function fixture() {
    let page;
    let token = 'A';
    const calls = [];
    const confirmations = [];
    let request = (url, data) =>
        Promise.resolve({
            code: 0,
            data:
                url === 'detail'
                    ? {
                          orderInfo: {id: data.orderId},
                          orderGoods: [],
                          handleOption: {cancel: true, confirm: true, pay: true},
                      }
                    : {},
        });
    const context = {
        Page: (value) => {
            page = value;
        },
        wx: {
            getStorageSync: () => token,
            showModal: (value) => confirmations.push(value),
            showToast() {},
        },
        require: (name) =>
            name.includes('services/pay')
                ? {payOrder: () => Promise.resolve({code: 0})}
                : name.includes('util')
                  ? {
                        request: (...args) => {
                            calls.push(args);
                            return request(...args);
                        },
                        showErrorToast() {},
                    }
                  : {
                        OrderDetail: 'detail',
                        RefundStatus: 'refund',
                        RefundRequest: 'refund-request',
                        OrderCancel: 'cancel',
                        OrderConfirm: 'confirm',
                    },
    };
    vm.runInNewContext(
        fs.readFileSync('apps/miniapp/pages/ucenter/orderDetail/orderDetail.js', 'utf8'),
        context
    );
    page.setData = (values) => Object.assign(page.data, values);
    return {
        page,
        calls,
        confirmations,
        setToken: (value) => {
            token = value;
        },
        setRequest: (value) => {
            request = value;
        },
    };
}

/** 读取失败结束加载并保留明确重试入口. */
test('订单详情两路读取失败均明确收尾', async () => {
    const f = fixture();
    f.setRequest(() => Promise.reject(new Error('offline')));
    await f.page.onLoad({id: 8});
    assert.equal(f.page.data.loading, false);
    assert.match(f.page.data.error, /offline/);
    assert.match(f.page.data.refundError, /offline/);
    f.setRequest((url) =>
        Promise.resolve({code: 0, data: url === 'detail' ? {orderInfo: {id: 8}} : null})
    );
    await f.page.getOrderDetail();
    assert.equal(f.page.data.error, '');
    assert.equal(f.page.data.refundError, '');
});

/** 切换账号会清空私人内容且拒绝前账号迟到结果. */
test('订单详情隔离账号切换', async () => {
    const f = fixture();
    const pending = [];
    f.setRequest(() => new Promise((resolve) => pending.push(resolve)));
    const read = f.page.onLoad({id: 8});
    f.setToken('B');
    f.page.onShow();
    for (const resolve of pending) {
        resolve({code: 0, data: {orderInfo: {id: 8}}});
    }
    await read;
    assert.equal(f.page.data.orderId, 0);
    assert.equal(f.page.data.orderInfo.id, undefined);
});

/** 从确认框开始持有同一写入锁, 连续点击不能发起第二个操作. */
test('订单操作从确认阶段防重', async () => {
    const f = fixture();
    await f.page.onLoad({id: 8});
    const first = f.page.cancelOrder();
    await f.page.cancelOrder();
    assert.equal(f.confirmations.length, 1);
    f.confirmations[0].success({confirm: true});
    assert.equal(await first, true);
    assert.equal(f.calls.filter(([url]) => url === 'cancel').length, 1);
    assert.equal(f.page.data.writing, false);
});

/** 确认期间订单动作变化时不能按旧状态提交. */
test('订单确认后重新核对合法动作', async () => {
    const f = fixture();
    await f.page.onLoad({id: 8});
    const pending = f.page.confirmOrder();
    f.page.data.handleOption.confirm = false;
    f.confirmations[0].success({confirm: true});
    assert.equal(await pending, false);
    assert.equal(f.calls.filter(([url]) => url === 'confirm').length, 0);
    assert.match(f.page.data.error, /状态/);
});

/** 离页后确认回调不得触发后续网络写入. */
test('订单操作隔离离页确认', async () => {
    const f = fixture();
    await f.page.onLoad({id: 8});
    const pending = f.page.cancelOrder();
    f.page.onUnload();
    f.confirmations[0].success({confirm: true});
    assert.equal(await pending, false);
    assert.equal(f.calls.filter(([url]) => url === 'cancel').length, 0);
});

/** 业务写入失败保留当前内容并要求重读, 不自动重复请求. */
test('订单操作失败不自动重放', async () => {
    const f = fixture();
    await f.page.onLoad({id: 8});
    f.setRequest(() => Promise.resolve({code: 409, msg: '隔离冲突'}));
    const pending = f.page.cancelOrder();
    f.confirmations[0].success({confirm: true});
    assert.equal(await pending, false);
    assert.equal(f.page.data.writing, false);
    assert.match(f.page.data.error, /冲突/);
    assert.equal(f.calls.filter(([url]) => url === 'cancel').length, 1);
});

/** 售后请求仅允许已支付的合法状态, 失败保持理由并要求先核对结果. */
test('售后申请验证状态并隔离失败重试', async () => {
    const f = fixture();
    await f.page.onLoad({id: 8});
    f.page.data.refundReason = '隔离测试理由';
    await f.page.requestRefund();
    assert.equal(f.calls.filter(([url]) => url === 'refund-request').length, 0);
    f.page.data.orderInfo = {id: 8, payStatus: 2, orderStatus: 201};
    f.setRequest(() => Promise.reject(new Error('offline')));
    await f.page.requestRefund();
    assert.equal(f.page.data.refundSubmitting, false);
    assert.equal(f.page.data.refundReason, '隔离测试理由');
    assert.match(f.page.data.refundError, /offline/);
    await f.page.requestRefund();
    assert.equal(f.calls.filter(([url]) => url === 'refund-request').length, 1);
});

/** 售后提交期间换账号或离页, 迟到结果均不能提示成功或重读私人订单. */
test('售后申请丢弃离页及账号变化的迟到结果', async () => {
    for (const mode of ['account', 'unload']) {
        const f = fixture();
        await f.page.onLoad({id: 8});
        f.page.data.orderInfo = {id: 8, payStatus: 2, orderStatus: 201};
        f.page.data.refundReason = '隔离测试理由';
        let resolve;
        f.setRequest(
            () =>
                new Promise((complete) => {
                    resolve = complete;
                })
        );
        const pending = f.page.requestRefund();
        if (mode === 'account') {
            f.setToken('B');
            f.page.onShow();
        } else {
            f.page.onUnload();
            f.page.setData = () => {
                throw new Error('unloaded');
            };
        }
        resolve({code: 0});
        await pending;
        assert.equal(f.calls.length, 3);
    }
});
