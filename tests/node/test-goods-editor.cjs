/* 商品编辑真实组件脚本回归, 使用受控Promise验证加载与保存生命周期. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');

const file = path.resolve(
    __dirname,
    '../../apps/admin-ui/src/views/modules/mall/goods-add-or-update.vue'
);
const script = fs.readFileSync(file, 'utf8').match(/<script>([\s\S]*?)<\/script>/)[1];
const moduleBox = {exports: {}};
vm.runInNewContext(
    script.replace(/^import .*;$/gm, '').replace('export default', 'module.exports ='),
    {
        module: moduleBox,
        h: (tag, props) => ({tag, props}),
        DOMPurify: {sanitize: () => '已清洗详情'},
    }
);
const component = moduleBox.exports;

/** 使验证和网络结果可按任意顺序完成, 不访问真实业务数据. */
function deferred() {
    let resolve;
    let reject;
    const promise = new Promise((yes, no) => {
        resolve = yes;
        reject = no;
    });
    return {promise, resolve, reject};
}

/** 绑定实际组件方法并记录请求、通知和刷新事件. */
function fixture() {
    const calls = [];
    const messages = [];
    const events = [];
    const editor = {};
    for (const [name, method] of Object.entries(component.methods)) {
        editor[name] = method.bind(editor);
    }
    Object.assign(editor, component.data.call(editor), {
        visible: true,
        _editorVersion: 1,
        $refs: {dataForm: {validate: () => Promise.resolve(true), resetFields() {}}},
        $nextTick: (callback) => callback(),
        $http: (request) => {
            const result = deferred();
            calls.push({...request, ...result});
            return result.promise;
        },
        $message: Object.assign((message) => messages.push(message.message), {
            error: (message) => messages.push(message),
        }),
        $route: {query: {name: '珍珠', page: '2'}},
        $router: {
            push: (location) => {
                events.push(location);
                return Promise.resolve();
            },
        },
        sanitizePayload: () => ({goods: {id: 10, name: '测试商品'}, specs: [], skuList: []}),
    });
    return {editor, calls, messages, events};
}

/** 读取失败不能提交默认表单, 原地重试成功后恢复保存能力. */
test('商品详情失败阻止覆盖且可重试', async () => {
    const {editor, calls} = fixture();
    editor.form.goods.id = 10;
    const loading = editor.loadAggregate(10);
    await editor.dataFormSubmit();
    assert.equal(calls.length, 1);
    calls[0].reject(new Error('断网'));
    await loading;
    assert.ok(editor.loadError);
    await editor.dataFormSubmit();
    assert.equal(calls.length, 1);
    const retry = editor.loadAggregate(10);
    calls[1].resolve({data: {code: 0, data: {goods: {id: 10, name: '恢复商品'}}}});
    await retry;
    assert.equal(editor.loadError, '');
    assert.equal(editor.form.goods.name, '恢复商品');
    assert.equal(editor.loading, false);
});

/** 商品归属错误和业务失败同样不得被当作读取成功. */
test('商品详情拒绝不匹配的商品编号', async () => {
    const {editor, calls} = fixture();
    const loading = editor.loadAggregate(10);
    calls[0].resolve({data: {code: 0, data: {goods: {id: 20, name: '其他商品'}}}});
    await loading;
    assert.ok(editor.loadError);
    assert.equal(editor.form.goods.name, '');
});

/** 连续打开不同商品, 旧详情成功或失败均不能覆盖当前商品. */
test('切换商品隔离旧读取结果', async () => {
    for (const fails of [false, true]) {
        const {editor, calls} = fixture();
        const old = editor.loadAggregate(10);
        editor._editorVersion++;
        editor.form.goods.name = '新商品';
        editor.loading = false;
        if (fails) {
            calls[0].reject(new Error('旧请求失败'));
        } else {
            calls[0].resolve({data: {code: 0, data: {goods: {id: 10, name: '旧商品'}}}});
        }
        await old;
        assert.equal(editor.form.goods.name, '新商品');
        assert.equal(editor.loadError, '');
    }
});

/** 选项失败保留正文和错误状态, 重新读取全部成功后解除保存限制. */
test('商品选项失败保留输入并支持恢复', async () => {
    const {editor, calls} = fixture();
    editor.form.goods.name = '未保存输入';
    const loading = editor.loadOptions();
    calls[0].reject(new Error('断网'));
    await loading;
    assert.equal(editor.form.goods.name, '未保存输入');
    assert.ok(editor.optionsError);
    await editor.dataFormSubmit();
    assert.equal(calls.length, 4);
    const retry = editor.loadOptions();
    calls.slice(4).forEach((call) => call.resolve({data: {code: 0, data: []}}));
    await retry;
    assert.equal(editor.optionsError, '');
    assert.equal(editor.optionsLoading, false);
});

/** 异步表单校验尚未结束时也只能有一次保存, 失败后允许显式重试. */
test('商品保存从校验起去重且网络失败可重试', async () => {
    const {editor, calls, events} = fixture();
    const validation = deferred();
    editor.$refs.dataForm.validate = () => validation.promise;
    const saving = editor.dataFormSubmit();
    await editor.dataFormSubmit();
    validation.resolve(true);
    await new Promise(setImmediate);
    assert.equal(calls.length, 1);
    calls[0].reject(new Error('断网'));
    await saving;
    assert.equal(editor.visible, true);
    assert.equal(editor.saving, false);
    const retry = editor.dataFormSubmit();
    await new Promise(setImmediate);
    calls[1].resolve({data: {code: 0}});
    await retry;
    assert.equal(editor.visible, false);
    assert.equal(events[0].name, 'mall-goods');
    assert.equal(events[0].query.name, '珍珠');
    assert.equal(events[0].query.page, '2');
});

/** 业务校验拒绝时显示原因并保持表单, 不发出列表刷新事件. */
test('商品保存业务失败不关闭表单', async () => {
    const {editor, calls, messages, events} = fixture();
    const saving = editor.dataFormSubmit();
    await new Promise(setImmediate);
    calls[0].resolve({data: {code: 400, msg: '规格组合不完整'}});
    await saving;
    assert.equal(editor.visible, true);
    assert.equal(editor.saving, false);
    assert.ok(messages.includes('规格组合不完整'));
    assert.equal(events.length, 0);
});

/** 已保存旧商品的迟到响应不能关闭新商品或解除新会话的锁. */
test('切换商品隔离旧保存结果', async () => {
    const {editor, calls, events} = fixture();
    const saving = editor.dataFormSubmit();
    await new Promise(setImmediate);
    editor._editorVersion++;
    editor.saving = true;
    calls[0].resolve({data: {code: 0}});
    await saving;
    assert.equal(editor.visible, true);
    assert.equal(editor.saving, true);
    assert.equal(events.length, 0);
});

/** 校验失败解除锁, 只读或已关闭表单均不能执行写请求. */
test('非法及只读表单不提交', async () => {
    const {editor, calls} = fixture();
    editor.$refs.dataForm.validate = () => Promise.reject(new Error('字段不完整'));
    await editor.dataFormSubmit();
    assert.equal(editor.saving, false);
    editor.$refs.dataForm.validate = () => Promise.resolve(true);
    editor.disabled = true;
    await editor.dataFormSubmit();
    editor.disabled = false;
    editor.visible = false;
    await editor.dataFormSubmit();
    assert.equal(calls.length, 0);
});

/** 预览调用Vue3渲染函数并继续经过清洗入口, 不依赖已移除的Vue2实例方法. */
test('商品详情预览使用Vue3公开接口', () => {
    const {editor} = fixture();
    let preview;
    editor.$alert = (content) => {
        preview = content;
    };
    editor.openDetail();
    assert.equal(preview.tag, 'div');
    assert.equal(preview.props.innerHTML, '已清洗详情');
    assert.equal(preview.props.domProps, undefined);
});
