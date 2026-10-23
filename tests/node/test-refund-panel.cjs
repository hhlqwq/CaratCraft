/* 售后真实组件脚本回归, 不调用真实支付审核或退款对账. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');

/** 控制确认框和网络响应的完成顺序. */
function deferred() {
    let resolve;
    let reject;
    const promise = new Promise((yes, no) => {
        resolve = yes;
        reject = no;
    });
    return {promise, resolve, reject};
}

/** 执行实际售后组件, 使用受控响应记录写入次数和通知. */
function fixture() {
    const box = {exports: {}};
    const source = fs.readFileSync(
        path.resolve(__dirname, '../../apps/admin-ui/src/views/modules/mall/refund-panel.vue'),
        'utf8'
    );
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
    const prompts = [];
    const messages = [];
    page.visible = true;
    page.rows = [{order_id: 10, status: 'PENDING_REVIEW'}];
    page.$http = (request) => {
        const response = deferred();
        calls.push({...request, ...response});
        return response.promise;
    };
    page.$prompt = (...args) => {
        const prompt = deferred();
        prompts.push({...prompt, args});
        return prompt.promise;
    };
    page.$message = {success: (text) => messages.push(text), error: (text) => messages.push(text)};
    return {page, calls, prompts, messages, component};
}

/** 等待受控确认框结果推进到网络阶段. */
async function tick() {
    await new Promise(setImmediate);
}

/** 列表乱序只展示最新页, 失败保留当前页并提供重试. */
test('售后列表隔离乱序与分页失败', async () => {
    const {page, calls} = fixture();
    const old = page.load(1);
    const latest = page.load(2);
    calls[1].resolve({data: {code: 0, data: {records: [{order_id: 20}], total: 30}}});
    await latest;
    calls[0].resolve({data: {code: 0, data: {records: [{order_id: 10}], total: 1}}});
    await old;
    assert.equal(page.page, 2);
    assert.equal(page.rows[0].order_id, 20);
    const failure = page.load(3);
    calls[2].reject(new Error('读取失败'));
    await failure;
    assert.equal(page.page, 2);
    assert.equal(page.rows[0].order_id, 20);
    assert.ok(page.listError);
});

/** 确认期间重复审核只出现一次提示, 对账同时被阻断. */
test('售后审核从确认框起共用写入锁', async () => {
    const {page, calls, prompts} = fixture();
    const pending = page.decide(page.rows[0], true);
    await page.decide(page.rows[0], false);
    await page.reconcile();
    assert.equal(prompts.length, 1);
    assert.equal(calls.length, 0);
    prompts[0].resolve({value: '已确认退货'});
    await tick();
    assert.equal(calls.length, 1);
    calls[0].reject(new Error('网络失败'));
    await pending;
    assert.equal(page.writing, false);
    assert.equal(page.decisionDraft.note, '已确认退货');
    assert.equal(page.needsReload, true);
});

/** 用户确认后仍须处于待审核, 状态变化不能继续提交旧动作. */
test('售后审核确认后复核当前状态', async () => {
    const {page, calls, prompts} = fixture();
    const pending = page.decide(page.rows[0], true);
    page.rows[0].status = 'PROCESSING';
    prompts[0].resolve({value: '已确认退货'});
    await pending;
    assert.equal(calls.length, 0);
    assert.equal(page.writing, false);
});

/** 关闭后重新打开时旧审核不得发请求或覆盖新面板. */
test('关闭重开售后面板废弃旧确认结果', async () => {
    const {page, calls, prompts} = fixture();
    const pending = page.decide(page.rows[0], true);
    page.close();
    const reopening = page.open();
    calls[0].resolve({data: {code: 0, data: {records: [{order_id: 20}], total: 1}}});
    await reopening;
    prompts[0].resolve({value: '旧审核说明'});
    await pending;
    assert.equal(calls.length, 1);
    assert.equal(page.rows[0].order_id, 20);
    assert.equal(page.visible, true);
});

/** 对账请求只有一个, 成功仅报告实际处理数而非退款完成. */
test('售后对账防重并读取实际状态', async () => {
    const {page, calls, messages} = fixture();
    const pending = page.reconcile();
    await page.reconcile();
    assert.equal(calls.length, 1);
    calls[0].resolve({data: {code: 0, data: {processed: 2}}});
    await tick();
    calls[1].resolve({
        data: {code: 0, data: {records: [{order_id: 10, status: 'PROCESSING'}], total: 1}},
    });
    await pending;
    assert.equal(page.statusName(page.rows[0].status), '退款处理中');
    assert.equal(page.writing, false);
    assert.ok(messages.some((text) => text.includes('2')));
    assert.ok(messages.every((text) => !text.includes('退款成功')));
});

/** 审核成功不等于退款成功, 仍展示服务端待提交状态. */
test('售后审核成功后展示实际待退款状态', async () => {
    const {page, calls, prompts, messages} = fixture();
    const pending = page.decide(page.rows[0], true);
    prompts[0].resolve({value: '已确认退货'});
    await tick();
    calls[0].resolve({data: {code: 0}});
    await tick();
    calls[1].resolve({
        data: {code: 0, data: {records: [{order_id: 10, status: 'APPROVED'}], total: 1}},
    });
    await pending;
    assert.equal(page.statusName(page.rows[0].status), '待提交退款');
    assert.ok(messages.every((text) => !text.includes('退款成功')));
});

/** 确认框返回异常说明也不能绕过前端边界, 用户取消不提示错误. */
test('售后审核校验理由并正确收尾取消', async () => {
    const {page, calls, prompts, messages} = fixture();
    for (const value of ['', '   ', '字'.repeat(201)]) {
        const pending = page.decide(page.rows[0], false);
        prompts.at(-1).resolve({value});
        await pending;
    }
    assert.equal(calls.length, 0);
    const count = messages.length;
    const cancelled = page.decide(page.rows[0], false);
    prompts.at(-1).reject('cancel');
    await cancelled;
    assert.equal(messages.length, count);
    assert.equal(page.writing, false);
});

/** 写入失败前的在途查询不能解除必须重查的保护. */
test('售后写入失败废弃旧读取并保持输入', async () => {
    const {page, calls} = fixture();
    const writing = page.reconcile();
    const reading = page.load(1);
    calls[0].resolve({data: {code: 500, msg: '支付通道暂不可用'}});
    await writing;
    calls[1].resolve({data: {code: 0, data: {records: page.rows, total: 1}}});
    await reading;
    assert.equal(page.needsReload, true);
    await page.reconcile();
    assert.equal(calls.length, 2);
});
