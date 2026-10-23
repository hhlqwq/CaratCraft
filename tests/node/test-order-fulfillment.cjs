/* 订单履约真实组件脚本回归, 不发送发货或改价业务请求. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');

/** 允许网络、校验和确认框按任意顺序完成. */
function deferred() {
    let resolve;
    let reject;
    const promise = new Promise((yes, no) => {
        resolve = yes;
        reject = no;
    });
    return {promise, resolve, reject};
}

/** 绑定实际订单组件并使用受控网络, 不访问真实订单. */
function fixture(file = 'order.vue') {
    const source = fs.readFileSync(
        path.resolve(__dirname, '../../apps/admin-ui/src/views/modules/mall/' + file),
        'utf8'
    );
    const box = {exports: {}};
    vm.runInNewContext(
        source
            .match(/<script>([\s\S]*?)<\/script>/)[1]
            .replace(/^import .*;$/gm, '')
            .replace('export default', 'module.exports ='),
        {module: box, OrderDetail: {}, RefundPanel: {}}
    );
    const component = box.exports;
    const page = component.data();
    for (const [name, method] of Object.entries(component.methods)) {
        page[name] = method.bind(page);
    }
    const calls = [];
    const messages = [];
    const prompts = [];
    page.$http = (request) => {
        const response = deferred();
        calls.push({...request, ...response});
        return response.promise;
    };
    page.$message = Object.assign((message) => messages.push(message.message), {
        success: (message) => messages.push(message),
        error: (message) => messages.push(message),
    });
    page.$prompt = (...args) => {
        const prompt = deferred();
        prompts.push({...prompt, args});
        return prompt.promise;
    };
    page.$nextTick = (callback) => callback();
    page.$refs = {shipForm: {clearValidate() {}, validate: () => Promise.resolve(true)}};
    page.dataList = [{id: 10, orderStatus: 201, payStatus: 2, shippingStatus: 0}];
    return {page, component, calls, messages, prompts};
}

/** 推进确认框后的异步任务到下一个网络请求. */
async function tick() {
    await new Promise(setImmediate);
}

/** 失败分页保留原数据, 旧成功不能覆盖当前页. */
test('订单列表隔离乱序并保留失败页', async () => {
    const {page, calls} = fixture();
    const old = page.getDataList(1);
    const latest = page.getDataList(2);
    calls[1].resolve({data: {code: 0, data: {records: [{id: 20}], total: 30}}});
    await latest;
    calls[0].resolve({data: {code: 0, data: {records: [{id: 10}], total: 1}}});
    await old;
    assert.equal(page.dataList[0].id, 20);
    const failed = page.getDataList(3);
    calls[2].reject(new Error('读取失败'));
    await failed;
    assert.equal(page.pageIndex, 2);
    assert.equal(page.dataList[0].id, 20);
    assert.ok(page.listError);
    assert.equal(page.loading, false);
});

/** 校验尚未完成时重复点击也不能发出两次发货. */
test('发货锁覆盖异步表单校验并保留失败输入', async () => {
    const {page, calls} = fixture();
    page.shipOrder(page.dataList[0]);
    page.shipForm.shippingName = '顺丰';
    page.shipForm.shippingNo = 'SF001';
    const validation = deferred();
    let validations = 0;
    page.$refs.shipForm.validate = () => {
        validations++;
        return validation.promise;
    };
    const pending = page.submitShip();
    await page.submitShip();
    assert.equal(validations, 1);
    validation.resolve(true);
    await tick();
    assert.equal(calls.length, 1);
    calls[0].reject(new Error('连接失败'));
    await pending;
    assert.equal(page.writing, false);
    assert.equal(page.shipDialogVisible, true);
    assert.equal(page.shipForm.shippingNo, 'SF001');
    assert.equal(page.needsReload, true);
    await page.submitShip();
    assert.equal(calls.length, 1);
});

/** 校验期间关闭并切换订单, 旧校验完成不得提交新订单. */
test('发货校验绑定原弹窗及订单', async () => {
    const {page, calls} = fixture();
    page.shipOrder(page.dataList[0]);
    page.shipForm.shippingName = '顺丰';
    page.shipForm.shippingNo = 'SF001';
    const validation = deferred();
    page.$refs.shipForm.validate = () => validation.promise;
    const pending = page.submitShip();
    page.shipDialogVisible = false;
    page.shipOrder({id: 20, payStatus: 2, orderStatus: 201, shippingStatus: 0});
    validation.resolve(true);
    await pending;
    assert.equal(calls.length, 0);
    assert.equal(page.shipForm.id, 20);
});

/** 成功发货只关闭对应弹窗, 旧回调不关闭重新打开的弹窗. */
test('发货响应不影响新订单弹窗', async () => {
    const {page, calls, messages} = fixture();
    page.shipOrder(page.dataList[0]);
    page.shipForm.shippingName = '顺丰';
    page.shipForm.shippingNo = 'SF001';
    const pending = page.submitShip();
    await tick();
    page.shipDialogVisible = false;
    page.shipOrder({id: 20, payStatus: 2, orderStatus: 201, shippingStatus: 0});
    calls[0].resolve({data: {code: 0}});
    await pending;
    assert.equal(page.shipDialogVisible, true);
    assert.equal(messages.length, 0);
    assert.equal(calls.length, 1);
});

/** 改价从第一个确认框起持锁, 状态已变化则不提交. */
test('改价确认防重并复核支付状态', async () => {
    const {page, prompts, calls} = fixture();
    const row = {id: 10, orderStatus: 0, payStatus: 0, payId: '', actualPrice: '20.00'};
    page.dataList = [row];
    const pending = page.adjustPrice(row);
    await page.adjustPrice(row);
    assert.equal(prompts.length, 1);
    prompts[0].resolve({value: '18.00'});
    await tick();
    row.payStatus = 1;
    prompts[1].resolve({value: '协商优惠'});
    await pending;
    assert.equal(calls.length, 0);
    assert.equal(page.writing, false);
});

/** 改价失败明确提示且保留草稿, 不吞掉业务错误. */
test('改价失败保留金额理由并要求刷新', async () => {
    const {page, prompts, calls, messages} = fixture();
    const row = {id: 10, orderStatus: 0, payStatus: 0, payId: '', actualPrice: '20.00'};
    page.dataList = [row];
    const pending = page.adjustPrice(row);
    prompts[0].resolve({value: '18.00'});
    await tick();
    prompts[1].resolve({value: '协商优惠'});
    await tick();
    calls[0].resolve({data: {code: 409, msg: '订单状态已变化'}});
    await pending;
    assert.equal(page.priceDraft.price, '18.00');
    assert.equal(page.priceDraft.reason, '协商优惠');
    assert.equal(page.needsReload, true);
    assert.ok(messages.some((text) => text.includes('订单状态已变化')));
});

/** 离页期间确认框结束不能发出请求. */
test('订单离页后丢弃改价确认结果', async () => {
    const {page, component, prompts, calls} = fixture();
    const row = {id: 10, orderStatus: 0, payStatus: 0, payId: ''};
    page.dataList = [row];
    const pending = page.adjustPrice(row);
    component.deactivated.call(page);
    prompts[0].resolve({value: '18.00'});
    await pending;
    assert.equal(prompts.length, 1);
    assert.equal(calls.length, 0);
});

/** 不存在或异常状态不能打开履约操作, 旧新增和删除方法已移除. */
test('订单只保留合法履约操作', async () => {
    const {page, calls} = fixture();
    page.shipOrder({id: 30, payStatus: 0, orderStatus: 0});
    assert.equal(page.shipDialogVisible, false);
    await page.adjustPrice({id: 40, payStatus: 2, orderStatus: 201});
    assert.equal(calls.length, 0);
    assert.equal(page.deleteHandle, undefined);
    assert.equal(page.addOrUpdateHandle, undefined);
});

/** 订单详情只在两个读取均成功后展示, 切换时旧结果不能污染新订单. */
test('订单详情和商品绑定同一次打开请求', async () => {
    const {page, calls} = fixture('order-detail.vue');
    const old = page.init(10, true);
    const latest = page.init(20, true);
    calls[2].resolve({data: {code: 0, data: {id: 20, orderSn: 'NEW'}}});
    calls[3].resolve({data: {code: 0, data: [{id: 200}]}});
    await latest;
    calls[0].resolve({data: {code: 0, data: {id: 10, orderSn: 'OLD'}}});
    calls[1].resolve({data: {code: 0, data: [{id: 100}]}});
    await old;
    assert.equal(page.dataForm.id, 20);
    assert.equal(page.orderGoodsList[0].id, 200);
    assert.equal(page.dataFormSubmit, undefined);
    const source = fs.readFileSync(
        path.resolve(__dirname, '../../apps/admin-ui/src/views/modules/mall/order-detail.vue'),
        'utf8'
    );
    assert.doesNotMatch(source, /dataFormSubmit|\/mall\/order\/(save|update|delete)/);
});

/** 快递公司继续使用真实Controller已有接口, 不引入另一套协议. */
test('快递字典读取沿用既有接口并隔离迟到失败', async () => {
    const {page, calls} = fixture();
    const old = page.loadShippingCompanies();
    const latest = page.loadShippingCompanies();
    assert.equal(calls[0].url, '/mall/shipping/queryAll');
    calls[1].resolve({data: {code: 0, data: [{id: 1, name: '顺丰'}]}});
    await latest;
    calls[0].reject(new Error('旧失败'));
    await old;
    assert.equal(page.shippingCompanyOptions[0].name, '顺丰');
    assert.equal(page.companyError, '');
});

/** 表单校验成功仍执行服务端长度边界, 合法发货成功后刷新状态. */
test('发货长度边界与成功刷新', async () => {
    const {page, calls, messages} = fixture();
    page.shipOrder(page.dataList[0]);
    page.shipForm.shippingName = '顺丰';
    page.shipForm.shippingNo = 'S'.repeat(33);
    await page.submitShip();
    assert.equal(calls.length, 0);
    page.shipForm.shippingNo = 'SF001';
    const pending = page.submitShip();
    await tick();
    calls[0].resolve({data: {code: 0}});
    await tick();
    assert.equal(page.shipDialogVisible, false);
    calls[1].resolve({data: {code: 0, data: {records: [{id: 10, orderStatus: 300}], total: 1}}});
    await pending;
    assert.equal(page.dataList[0].orderStatus, 300);
    assert.equal(page.writing, false);
    assert.ok(messages.includes('发货成功'));
});

/** 详情缺少商品响应时不展示半份订单, 关闭后错误也不回填. */
test('订单详情失败可重试且关闭废弃旧请求', async () => {
    const {page, calls} = fixture('order-detail.vue');
    const pending = page.init(10);
    calls[0].resolve({data: {code: 0, data: {id: 10}}});
    calls[1].reject(new Error('商品明细读取失败'));
    await pending;
    assert.equal(page.dataForm.id, undefined);
    assert.equal(page.loading, false);
    assert.ok(page.readError);
    const retry = page.init(10);
    page.close();
    calls[2].resolve({data: {code: 0, data: {id: 10}}});
    calls[3].resolve({data: {code: 0, data: []}});
    await retry;
    assert.equal(page.visible, false);
    assert.equal(page.dataForm.id, undefined);
});

/** 写入失败前发出的列表查询不能解除结果未确认保护. */
test('发货失败废弃在途旧列表并要求明确重查', async () => {
    const {page, calls} = fixture();
    page.shipOrder(page.dataList[0]);
    page.shipForm.shippingName = '顺丰';
    page.shipForm.shippingNo = 'SF001';
    const saving = page.submitShip();
    await tick();
    const reading = page.getDataList();
    calls[0].reject(new Error('连接中断'));
    await saving;
    calls[1].resolve({data: {code: 0, data: {records: page.dataList, total: 1}}});
    await reading;
    assert.equal(page.needsReload, true);
    const refresh = page.getDataList();
    calls[2].resolve({data: {code: 0, data: {records: [{id: 10, orderStatus: 300}], total: 1}}});
    await refresh;
    assert.equal(page.needsReload, false);
    assert.equal(page.dataList[0].orderStatus, 300);
});
