/* 工单真实组件脚本回归, 不执行真实生产流转. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');

/** 让网络和确认框可按任意顺序完成. */
function deferred() {
    let resolve;
    let reject;
    const promise = new Promise((yes, no) => {
        resolve = yes;
        reject = no;
    });
    return {promise, resolve, reject};
}

/** 加载实际工单组件, 记录接口请求及用户提示. */
function fixture() {
    const source = fs.readFileSync(
        path.resolve(__dirname, '../../apps/admin-ui/src/views/modules/custom/work-order.vue'),
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
    Object.entries(component.methods).forEach(([key, method]) => {
        page[key] = method.bind(page);
    });
    const calls = [];
    const messages = [];
    let prompts = 0;
    page.$http = (request) => {
        const result = deferred();
        calls.push({...request, ...result});
        return result.promise;
    };
    page.$prompt = () => {
        prompts++;
        return Promise.resolve({value: '已核对制作内容'});
    };
    page.$message = {
        success: (value) => messages.push(value),
        error: (value) => messages.push(value),
    };
    page.workOrders = [{id: 10, status: 'PENDING_REVIEW', allowedActions: ['REVIEWED']}];
    return {page, calls, messages, component, prompts: () => prompts};
}

/** 分页请求乱序不能回填旧页, 失败时保留原页和数据供重试. */
test('工单分页隔离乱序并保留失败页上下文', async () => {
    const {page, calls} = fixture();
    const old = page.loadWorkOrders(1);
    const latest = page.loadWorkOrders(2);
    calls[1].resolve({data: {code: 0, data: {records: [{id: 20}], total: 30}}});
    await latest;
    calls[0].reject(new Error('旧失败'));
    await old;
    assert.equal(page.page, 2);
    assert.equal(page.workOrders[0].id, 20);
    assert.equal(page.listError, '');
    const failure = page.changePage(3);
    calls[2].reject(new Error('读取失败'));
    await failure;
    assert.equal(page.page, 2);
    assert.equal(page.workOrders[0].id, 20);
    assert.equal(page.loading, false);
    assert.ok(page.listError);
});

/** 日志弹窗切换工单后不显示旧日志, 关闭后迟到结果不重开弹窗. */
test('工单日志绑定工单与弹窗生命周期', async () => {
    const {page, calls} = fixture();
    const old = page.showLogs({id: 10});
    const latest = page.showLogs({id: 20});
    calls[1].resolve({
        data: {
            code: 0,
            data: {
                records: [{remark: '当前日志'}],
                total: [{remark: '当前日志'}].length,
                current: 1,
                size: 20,
            },
        },
    });
    await latest;
    calls[0].resolve({
        data: {
            code: 0,
            data: {
                records: [{remark: '旧日志'}],
                total: [{remark: '旧日志'}].length,
                current: 1,
                size: 20,
            },
        },
    });
    await old;
    assert.equal(page.logs[0].remark, '当前日志');
    const closing = page.showLogs({id: 30});
    page.logVisible = false;
    calls[2].reject(new Error('迟到失败'));
    await closing;
    assert.equal(page.logVisible, false);
    assert.equal(page.logError, '');
});

/** 确认框弹出即锁定操作, 网络失败释放锁并说明原因. */
test('工单流转从确认起去重且失败可恢复', async () => {
    const {page, calls, prompts, messages} = fixture();
    const saving = page.updateStatus(10, 'REVIEWED');
    await page.updateStatus(10, 'REVIEWED');
    await new Promise(setImmediate);
    assert.equal(prompts(), 1);
    assert.equal(calls.length, 1);
    calls[0].reject(new Error('保存失败'));
    await saving;
    assert.equal(page.updating, false);
    assert.ok(messages.includes('保存失败'));
    assert.equal(page.workOrders[0].status, 'PENDING_REVIEW');
});

/** 只允许服务端声明的下一状态, 确认期间列表变化须重新选择. */
test('工单拒绝未知及已失效的状态动作', async () => {
    const {page, calls, prompts} = fixture();
    await page.updateStatus(10, 'COMPLETED');
    assert.equal(prompts(), 0);
    const confirmation = deferred();
    page.$prompt = () => confirmation.promise;
    const saving = page.updateStatus(10, 'REVIEWED');
    page.workOrders = [];
    confirmation.resolve({value: '审核备注'});
    await saving;
    assert.equal(calls.length, 0);
    assert.equal(page.updating, false);
});

/** 成功后读取服务端列表, 不由前端直接改写状态. */
test('工单成功流转后刷新权威列表', async () => {
    const {page, calls} = fixture();
    const saving = page.updateStatus(10, 'REVIEWED');
    await new Promise(setImmediate);
    calls[0].resolve({data: {code: 0}});
    await new Promise(setImmediate);
    assert.equal(calls[1].method, 'get');
    calls[1].resolve({data: {code: 0, data: {records: [{id: 10, status: 'REVIEWED'}], total: 1}}});
    await saving;
    assert.equal(page.workOrders[0].status, 'REVIEWED');
    assert.equal(page.updating, false);
});

/** 离页后的读取与确认不得再发写请求或改动列表. */
test('工单离页隔离列表与状态确认', async () => {
    const {page, calls, component} = fixture();
    const loading = page.loadWorkOrders();
    calls[0].resolve({data: {code: 0, data: {records: page.workOrders, total: 1}}});
    await loading;
    const confirmation = deferred();
    page.$prompt = () => confirmation.promise;
    const saving = page.updateStatus(10, 'REVIEWED');
    component.deactivated.call(page);
    confirmation.resolve({value: '审核备注'});
    await saving;
    assert.equal(calls.length, 1);
    assert.equal(page.updating, false);
});
