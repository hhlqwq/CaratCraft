/* 评价页面真实共享服务回归, 只替换网络和微信宿主边界. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');

/** 创建可控制完成顺序的评价页面, 观察数据与提示. */
function fixture() {
    let page;
    let token = 'A';
    const calls = [];
    const notices = [];
    const util = {
        showErrorToast: (text) => notices.push(text),
        request: (url, data) =>
            new Promise((resolve, reject) => calls.push({url, data, resolve, reject})),
    };
    const wx = {
        getStorageSync: () => token,
        showToast: (options) => notices.push(options.title),
        navigateBack: () => notices.push('返回'),
    };
    /** 保持页面和读写公共服务为真实源码. */
    function load(file) {
        if (file.endsWith('/utils/util.js')) {
            return util;
        }
        if (file.endsWith('/config/api.js')) {
            return {CommentPost: 'comment'};
        }
        const box = {exports: {}};
        vm.runInNewContext(fs.readFileSync(file, 'utf8'), {
            module: box,
            wx,
            Page: (definition) => {
                page = definition;
            },
            require: (name) => load(path.resolve(path.dirname(file), name).replaceAll('\\', '/')),
        });
        return box.exports;
    }
    load(path.resolve('apps/miniapp/pages/commentPost/commentPost.js').replaceAll('\\', '/'));
    page.setData = (patch) => Object.assign(page.data, patch);
    page.onLoad({typeId: '0', valueId: '123'});
    page.onShow();
    /** 准备有效输入, 不包含任何用户归属参数. */
    function valid() {
        page.setData({content: '  建议  '});
    }
    return {
        page,
        calls,
        notices,
        valid,
        setToken: (next) => {
            token = next;
        },
    };
}

/** 空白、超长或无效身份不发送请求, 正常正文允许超过旧140字限制. */
test('评价校验目标和500字正文', async () => {
    const f = fixture();
    await f.page.onPost();
    assert.equal(f.calls.length, 0);
    f.valid();
    f.page.setData({valueId: 1.5});
    await f.page.onPost();
    assert.equal(f.calls.length, 0);
    f.page.setData({valueId: 123, content: '字'.repeat(501)});
    await f.page.onPost();
    assert.equal(f.calls.length, 0);
    f.page.setData({content: '字'.repeat(500)});
    const submit = f.page.onPost();
    assert.equal(f.calls[0].data.content.length, 500);
    f.calls[0].resolve({code: 0});
    assert.equal(await submit, true);
});

/** 连点只提交一次, 网络失败保留输入, 成功后不能再次发表. */
test('评价防重及失败恢复', async () => {
    const f = fixture();
    f.valid();
    const first = f.page.onPost();
    await f.page.onPost();
    f.page.onClose();
    assert.equal(f.calls.length, 1);
    assert.equal(f.notices.includes('返回'), false);
    f.calls[0].reject(new Error('断网'));
    await first;
    assert.equal(f.page.data.content, '  建议  ');
    const retry = f.page.onPost();
    f.calls[1].resolve({code: 0});
    await retry;
    assert.equal(f.page.data.posted, true);
    await f.page.onPost();
    assert.equal(f.calls.length, 2);
});

/** 旧账号和已卸载页面的结果不提示成功或执行返回. */
test('评价结果隔离账号和离页', async () => {
    for (const leave of [false, true]) {
        const f = fixture();
        f.valid();
        const submit = f.page.onPost();
        if (leave) {
            f.page.onUnload();
        } else {
            f.setToken('B');
            f.page.onShow();
        }
        f.calls[0].resolve({code: 0});
        assert.equal(await submit, false);
        assert.equal(f.notices.includes('返回'), false);
        assert.equal(f.notices.includes('已提交审核'), false);
    }
});
