/* 定制配置真实组件脚本回归, 覆盖商品切换、乱序读取及所有写入口. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {Blob} = require('node:buffer');
const {test} = require('node:test');

/** 创建可显式完成的异步操作, 不访问外部网络. */
function deferred() {
    let resolve;
    let reject;
    const promise = new Promise((yes, no) => {
        resolve = yes;
        reject = no;
    });
    return {promise, resolve, reject};
}

/** 按真实data与methods装配页面, 网络和文件读取由用例决定返回顺序. */
function fixture() {
    const readers = [];
    const downloads = [];
    const source = fs.readFileSync(
        path.resolve(__dirname, '../../apps/admin-ui/src/views/modules/custom/configuration.vue'),
        'utf8'
    );
    const box = {exports: {}};
    vm.runInNewContext(
        source
            .match(/<script>([\s\S]*?)<\/script>/)[1]
            .replace('export default', 'module.exports ='),
        {
            module: box,
            Blob,
            URL: {createObjectURL: () => 'blob:catalog', revokeObjectURL() {}},
            document: {
                createElement: () => ({
                    click() {
                        downloads.push(this.download);
                    },
                }),
            },
            FileReader: class {
                /** 只记录实例, 用例显式触发读取完成或失败. */
                constructor() {
                    readers.push(this);
                }
                /** 测试不读取实际文件. */
                readAsText() {}
            },
        }
    );
    const component = box.exports;
    const editor = component.data();
    for (const [name, method] of Object.entries(component.methods)) {
        editor[name] = method.bind(editor);
    }
    for (const [name, getter] of Object.entries(component.computed)) {
        Object.defineProperty(editor, name, {get: getter.bind(editor)});
    }
    const calls = [];
    const messages = [];
    editor.$http = (request) => {
        const operation = deferred();
        calls.push({...request, ...operation});
        return operation.promise;
    };
    editor.$message = {
        success: (message) => messages.push(message),
        error: (message) => messages.push(message),
    };
    editor.$confirm = () => Promise.resolve();
    editor.product.goodsId = 10;
    editor.selectedGoodsId = 10;
    return {editor, calls, messages, readers, component, downloads};
}

/** 新商品只接收自己的配件, 切换时立即清空旧列表. */
test('切换商品隔离迟到配件读取', async () => {
    const {editor, calls} = fixture();
    const old = editor.loadOptions();
    editor.selectProduct({goodsId: 20});
    assert.equal(editor.options.length, 0);
    calls[1].resolve({data: {code: 0, data: [{id: 200, goodsId: 20}]}});
    await new Promise(setImmediate);
    calls[0].resolve({data: {code: 0, data: [{id: 100, goodsId: 10}]}});
    await old;
    assert.equal(editor.options[0].goodsId, 20);
    assert.equal(editor.reading.options, false);
});

/** 同一商品连续刷新也必须只接收最新结果, 错误后允许重试读取. */
test('配件读取失败可恢复且旧失败不污染新结果', async () => {
    const {editor, calls} = fixture();
    const old = editor.loadOptions();
    const latest = editor.loadOptions();
    calls[1].resolve({data: {code: 0, data: [{id: 200}]}});
    await latest;
    calls[0].reject(new Error('旧请求失败'));
    await old;
    assert.equal(editor.options[0].id, 200);
    assert.equal(editor.readErrors.options, '');
    const failure = editor.loadOptions();
    calls[2].reject(new Error('断网'));
    await failure;
    assert.ok(editor.readErrors.options);
    editor.retryReads();
    calls[3].resolve({data: {code: 0, data: []}});
    await new Promise(setImmediate);
    assert.equal(editor.readErrors.options, '');
});

/** 保存锁覆盖所有写入口, 业务及网络失败均保留用户输入并解锁. */
test('定制保存去重且失败保留输入', async () => {
    const {editor, calls, messages} = fixture();
    editor.product.templateCode = '未保存模板';
    const saving = editor.saveProduct();
    editor.saveProduct();
    assert.equal(calls.length, 1);
    calls[0].reject(new Error('网络失败'));
    await saving;
    assert.equal(editor.product.templateCode, '未保存模板');
    assert.equal(editor.writing, false);
    const retry = editor.saveProduct();
    calls[1].resolve({data: {code: 400, msg: '制作规则不完整'}});
    await retry;
    assert.ok(messages.includes('制作规则不完整'));
    assert.equal(editor.writing, false);
});

/** 旧商品保存完成不能回切商品, 修改当前对象的商品ID也要隔离旧结果. */
test('旧保存不能改变当前选中商品', async () => {
    for (const replace of [false, true]) {
        const {editor, calls} = fixture();
        const saving = editor.saveProduct();
        if (replace) {
            editor.product = {...editor.product, goodsId: 20};
        } else {
            editor.product.goodsId = 20;
        }
        editor.selectedGoodsId = 20;
        calls[0].resolve({data: {code: 0}});
        await saving;
        assert.equal(editor.selectedGoodsId, 20);
        assert.equal(calls.length, 1);
        assert.equal(editor.writing, false);
    }
});

/** 编辑另一个配件后, 前一个配件保存成功不能清空新表单. */
test('配件保存隔离后续编辑', async () => {
    const {editor, calls} = fixture();
    editor.option.name = '旧配件';
    const saving = editor.saveOption();
    editor.resetOption();
    editor.option.name = '新配件';
    calls[0].resolve({data: {code: 0}});
    await saving;
    assert.equal(editor.option.name, '新配件');
    assert.equal(calls.length, 1);
});

/** 读取失败和未选商品时不接受配件保存. */
test('无有效配件读取上下文不能保存', async () => {
    const {editor, calls} = fixture();
    editor.readErrors.options = '读取失败';
    await editor.saveOption();
    editor.readErrors.options = '';
    editor.selectedGoodsId = null;
    await editor.saveOption();
    assert.equal(calls.length, 0);
});

/** 确认框等待期间切换商品, 不向旧目标发送停用操作. */
test('停用配件确认后重新检查商品上下文', async () => {
    const {editor, calls} = fixture();
    const confirmation = deferred();
    editor.$confirm = () => confirmation.promise;
    const removing = editor.deleteOption({id: 100, name: '测试配件'});
    editor.resetProduct();
    confirmation.resolve();
    await removing;
    assert.equal(calls.length, 0);
});

/** 文件读取和导入确认都要绑定原编辑会话, 不自动切回旧商品. */
test('导入隔离文件读取和确认期间的商品切换', async () => {
    const {editor, readers, calls} = fixture();
    editor.importConfiguration({raw: {}});
    editor.resetProduct();
    readers[0].onload({target: {result: '{"product":{"goodsId":10},"options":[]}'}});
    await new Promise(setImmediate);
    assert.equal(calls.length, 0);
    const confirmation = deferred();
    editor.$confirm = () => confirmation.promise;
    editor.importConfiguration({raw: {}});
    readers[1].onload({target: {result: '{"product":{"goodsId":10},"options":[]}'}});
    editor.resetProduct();
    confirmation.resolve();
    await new Promise(setImmediate);
    assert.equal(calls.length, 0);
});

/** 离页后成功读取不得回填列表, 写入成功不得重置表单. */
test('定制配置离页隔离全部在途结果', async () => {
    const {editor, calls, component} = fixture();
    const loading = editor.loadOptions();
    const saving = editor.saveProduct();
    component.deactivated.call(editor);
    calls[0].resolve({data: {code: 0, data: [{id: 200}]}});
    calls[1].resolve({data: {code: 0}});
    await Promise.all([loading, saving]);
    assert.equal(editor.options.length, 0);
    assert.equal(calls.length, 2);
});

/** 客户目录读取防重且只在原商品上下文触发下载, 不写业务配置. */
test('客户目录下载隔离商品切换并可失败重试', async () => {
    const {editor, calls, downloads} = fixture();
    const first = editor.downloadTemplate();
    await editor.downloadTemplate();
    assert.equal(calls.length, 1);
    assert.equal(calls[0].params.goodsId, 10);
    editor.resetProduct();
    calls[0].resolve({data: {code: 0, data: {options: []}}});
    await first;
    assert.equal(downloads.length, 0);
    editor.selectedGoodsId = 20;
    const failed = editor.downloadTemplate();
    calls[1].reject(new Error('offline'));
    await failed;
    assert.equal(editor.catalogDownloading, false);
    const retry = editor.downloadTemplate();
    calls[2].resolve({data: {code: 0, data: {options: [{enabled: 0}]}}});
    await retry;
    assert.equal(downloads.length, 1);
    assert.ok(calls.every((call) => call.method === 'get'));
});

/** 旧分类不能被后台编辑入口再次保存为当前材料, 不猜测其映射. */
test('四类材料与旧配置编辑边界一致', () => {
    const {editor, messages} = fixture();
    assert.deepEqual(
        Array.from(editor.componentTypes, (row) => row.value),
        ['pearl', 'clasp', 'accessory', 'chain']
    );
    const original = editor.option;
    editor.editOption({componentType: 'material', id: 7});
    assert.equal(editor.option, original);
    assert.match(messages[0], /旧分类/);
});

/** 配饰计长方式由商家明确修改, 保存时保留候选来源和其他规则. */
test('配饰计长选择随材料保存且不丢失原始元数据', async () => {
    const {editor, calls} = fixture();
    editor.editOption({
        id: 44,
        componentType: 'accessory',
        optionCode: 'charm',
        name: '待核对配饰',
        materialParams: JSON.stringify({lengthRole: 'HANGING', provisionalName: true}),
    });
    assert.equal(editor.ruleForm.lengthRole, 'HANGING');
    editor.ruleForm.lengthRole = 'INLINE';
    editor.ruleForm.spanMm = '2';
    const saving = editor.saveOption();
    const rules = JSON.parse(calls[0].data.materialParams);
    assert.equal(rules.lengthRole, 'INLINE');
    assert.equal(rules.spanMm, 2);
    assert.equal(rules.provisionalName, true);
    calls[0].resolve({data: {code: 0}});
    await saving;
});
