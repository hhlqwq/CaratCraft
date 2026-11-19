const {test} = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');

/** 使用可控确认框和请求队列测试真实写入锁, 不执行业务写入. */
function fixture() {
    let token = 'A';
    let modal;
    const calls = [];
    const messages = [];
    const context = {
        module: {exports: {}},
        wx: {
            getStorageSync: () => token,
            showModal: (options) => {
                modal = options;
            },
        },
        require: () => ({showErrorToast: (message) => messages.push(message)}),
    };
    vm.runInNewContext(fs.readFileSync('apps/miniapp/services/page-action.js', 'utf8'), context);
    const page = {data: {actionBusy: false}, _pageReads: {alive: true}, _readToken: 'A'};
    page.setData = (values) => Object.assign(page.data, values);
    let successes = 0;
    return {
        page,
        calls,
        messages,
        setToken: (value) => {
            token = value;
        },
        confirm: (value) => modal.success({confirm: value}),
        successes: () => successes,
        run: () =>
            context.module.exports.performPageAction(page, {
                confirmation: '确认删除?',
                canRun: () => true,
                request: () => new Promise((resolve, reject) => calls.push({resolve, reject})),
                onSuccess: () => {
                    successes++;
                },
            }),
    };
}

/** 确认期间就锁定操作, 取消不发请求且释放锁. */
test('页面写入确认阶段防重且取消无副作用', async () => {
    const f = fixture();
    const pending = f.run();
    assert.equal(await f.run(), false);
    f.confirm(false);
    assert.equal(await pending, false);
    assert.equal(f.calls.length, 0);
    assert.equal(f.page.data.actionBusy, false);
});

/** 确认框期间换账号或卸载均不得向新账号提交旧操作. */
test('页面写入确认后复核账号及页面', async () => {
    for (const mode of ['account', 'unload']) {
        const f = fixture();
        const pending = f.run();
        if (mode === 'account') {
            f.setToken('B');
        } else {
            f.page._pageReads.alive = false;
        }
        f.confirm(true);
        assert.equal(await pending, false);
        assert.equal(f.calls.length, 0);
    }
});

/** 写入失败有提示且不会自动重放, 服务端成功后才显示成功. */
test('页面写入失败不重放或伪造成功', async () => {
    const f = fixture();
    const pending = f.run();
    f.confirm(true);
    await Promise.resolve();
    f.calls[0].reject(new Error('offline'));
    assert.equal(await pending, false);
    assert.equal(f.calls.length, 1);
    assert.equal(f.successes(), 0);
    assert.equal(f.messages.length, 1);
    assert.equal(f.page.data.actionBusy, false);
});
