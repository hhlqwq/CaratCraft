/* 组件库存真实页面脚本回归, 不连接业务库存. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');

/** 控制网络完成顺序, 复现迟到响应及失败. */
function deferred() {
    let resolve;
    let reject;
    const promise = new Promise((yes, no) => {
        resolve = yes;
        reject = no;
    });
    return {promise, resolve, reject};
}

/** 绑定实际组件的方法, 记录请求和界面提示. */
function fixture() {
    const source = fs.readFileSync(
        path.resolve(__dirname, '../../apps/admin-ui/src/views/modules/custom/component-stock.vue'),
        'utf8'
    );
    const box = {exports: {}};
    vm.runInNewContext(
        source
            .match(/<script>([\s\S]*?)<\/script>/)[1]
            .replace('export default', 'module.exports ='),
        {module: box}
    );
    const component = box.exports;
    const page = component.data();
    for (const [name, method] of Object.entries(component.methods)) {
        page[name] = method.bind(page);
    }
    const calls = [];
    const messages = [];
    page.$http = (request) => {
        const response = deferred();
        calls.push({...request, ...response});
        return response.promise;
    };
    page.$message = {success: (text) => messages.push(text), error: (text) => messages.push(text)};
    page.stockList = [{optionId: 11, goodsId: 5, stock: 20, name: '珍珠'}];
    return {page, calls, messages, component};
}

/** 当前列表只接收最新成功结果, 读取失败保留已展示的页和数据. */
test('库存分页隔离乱序并可重试失败页', async () => {
    const {page, calls} = fixture();
    const old = page.loadStock(1);
    const latest = page.loadStock(2);
    calls[1].resolve({data: {code: 0, data: {records: [{optionId: 22}], total: 30}}});
    await latest;
    calls[0].resolve({data: {code: 0, data: {records: [{optionId: 11}], total: 1}}});
    await old;
    assert.equal(page.stockList[0].optionId, 22);
    assert.equal(page.page, 2);
    const failed = page.changePage(3);
    calls[2].reject(new Error('读取失败'));
    await failed;
    assert.equal(page.page, 2);
    assert.equal(page.stockList[0].optionId, 22);
    assert.equal(page.loading, false);
    assert.ok(page.listError);
});

/** 非法库存及空理由不得触发任何写入, 上限与服务端保持一致. */
test('库存调整严格验证整数及理由', async () => {
    const {page, calls} = fixture();
    page.openEdit(page.stockList[0]);
    for (const stock of [-1, 1.5, true, '', null, 1000000001]) {
        page.editStock = stock;
        page.editReason = '盘点';
        await page.saveStock();
    }
    page.editStock = 30;
    for (const reason of ['', '   ', '字'.repeat(201)]) {
        page.editReason = reason;
        await page.saveStock();
    }
    assert.equal(calls.length, 0);
});

/** 并发点击仅提交一次, 请求绑定打开弹窗时的库存而非可变列表行. */
test('库存写入防重并保留原始并发校验值', async () => {
    const {page, calls} = fixture();
    const row = page.stockList[0];
    page.openEdit(row);
    row.stock = 99;
    page.editStock = 30;
    page.editReason = '  盘点  ';
    const pending = page.saveStock();
    await page.saveStock();
    assert.equal(calls.length, 1);
    assert.equal(calls[0].params.expectedStock, 20);
    assert.equal(calls[0].params.reason, '盘点');
    calls[0].reject(new Error('连接失败'));
    await pending;
    assert.equal(page.saving, false);
    assert.equal(page.editStock, 30);
    assert.equal(page.editReason, '  盘点  ');
    assert.equal(page.editNeedsReload, true);
    await page.saveStock();
    assert.equal(calls.length, 1);
});

/** 并发冲突后显式重读才更新expectedStock, 理由及调整目标保留. */
test('库存冲突后重读并发基线再允许调整', async () => {
    const {page, calls} = fixture();
    page.openEdit(page.stockList[0]);
    page.editReason = '盘点';
    page.editStock = 30;
    const save = page.saveStock();
    calls[0].resolve({data: {code: 500, msg: '库存已变化'}});
    await save;
    assert.equal(page.editVisible, true);
    assert.equal(page.editNeedsReload, true);
    const reload = page.reloadEdit();
    calls[1].resolve({data: {code: 0, data: {records: [{optionId: 11, stock: 18}], total: 1}}});
    await reload;
    assert.equal(page.editRow.stock, 18);
    assert.equal(page.editStock, 30);
    assert.equal(page.editReason, '盘点');
    assert.equal(page.editNeedsReload, false);
});

/** 旧保存回调不得关闭新弹窗或显示旧错误. */
test('切换编辑对象后忽略旧库存写入回调', async () => {
    const {page, calls, messages} = fixture();
    page.openEdit(page.stockList[0]);
    page.editReason = '盘点';
    const pending = page.saveStock();
    page.editVisible = false;
    page.openEdit({optionId: 22, stock: 8});
    calls[0].resolve({data: {code: 0}});
    await pending;
    assert.equal(page.editVisible, true);
    assert.equal(page.editRow.optionId, 22);
    assert.equal(messages.length, 0);
    assert.equal(calls.length, 1);
});

/** 离页使读取和写入响应失效, 激活后不继承旧加载状态. */
test('库存页面离页后不再回填或刷新', async () => {
    const {page, calls, component, messages} = fixture();
    const reading = page.loadStock();
    component.deactivated.call(page);
    calls[0].reject(new Error('迟到失败'));
    await reading;
    assert.equal(page.listError, '');
    assert.equal(messages.length, 0);
    assert.equal(page.loading, false);
});

/** 当前编辑保存成功后只刷新一次, 锁在刷新结束后释放. */
test('库存保存成功关闭当前弹窗并刷新列表', async () => {
    const {page, calls, messages} = fixture();
    page.openEdit(page.stockList[0]);
    page.editStock = 0;
    page.editReason = '盘点清零';
    const pending = page.saveStock();
    calls[0].resolve({data: {code: 0}});
    await new Promise(setImmediate);
    assert.equal(page.editVisible, false);
    assert.equal(calls.length, 2);
    assert.equal(page.saving, true);
    calls[1].resolve({data: {code: 0, data: {records: [{optionId: 11, stock: 0}], total: 1}}});
    await pending;
    assert.equal(page.saving, false);
    assert.equal(page.stockList[0].stock, 0);
    assert.equal(messages[0], '库存已更新');
});

/** 重读找不到原选项时不能用其他选项的库存覆盖并发基线. */
test('库存选项移出当前页后要求重新查询', async () => {
    const {page, calls} = fixture();
    page.openEdit(page.stockList[0]);
    page.editNeedsReload = true;
    const pending = page.reloadEdit();
    calls[0].resolve({data: {code: 0, data: {records: [{optionId: 22, stock: 8}], total: 1}}});
    await pending;
    assert.equal(page.editNeedsReload, true);
    assert.equal(page.editRow.stock, 20);
    assert.ok(page.editError);
});
