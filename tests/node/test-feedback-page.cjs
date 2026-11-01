/* 反馈页面真实共享服务回归, 只替换网络和微信宿主边界. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');

/** 创建可控制完成顺序的反馈页面, 观察数据与提示. */
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
    const wx = {getStorageSync: () => token, showToast: (options) => notices.push(options.title)};
    /** 保持页面和读写公共服务为真实源码. */
    function load(file) {
        if (file.endsWith('/utils/util.js')) {
            return util;
        }
        if (file.endsWith('/config/api.js')) {
            return {FeedbackAdd: 'feedback'};
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
    load(path.resolve('apps/miniapp/pages/ucenter/feedback/feedback.js').replaceAll('\\', '/'));
    page.setData = (patch) => Object.assign(page.data, patch);
    page.onLoad({});
    page.onShow();
    /** 准备有效输入, 不包含任何用户归属参数. */
    function valid() {
        page.setData({index: 1, content: '  建议  ', mobile: '13800000000'});
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

/** 正文计数不依赖光标, 无效输入不会发送请求. */
test('反馈校验类型正文和手机号', async () => {
    const f = fixture();
    await f.page.submitFeedback();
    assert.equal(f.calls.length, 0);
    f.page.contentInput({detail: {value: '三个字', cursor: 0}});
    assert.equal(f.page.data.contentLength, 3);
    f.valid();
    f.page.setData({mobile: '123'});
    await f.page.submitFeedback();
    assert.equal(f.calls.length, 0);
});

/** 连点只发送一次, 失败保留输入且释放锁, 成功即清除当前输入. */
test('反馈防重及失败恢复', async () => {
    const f = fixture();
    f.valid();
    const first = f.page.submitFeedback();
    await f.page.submitFeedback();
    assert.equal(f.calls.length, 1);
    assert.equal(f.calls[0].data.content, '建议');
    f.calls[0].reject(new Error('断网'));
    await first;
    assert.equal(f.page.data.content, '  建议  ');
    assert.equal(f.page.data.actionBusy, false);
    const retry = f.page.submitFeedback();
    f.calls[1].resolve({code: 0, data: '感谢你的反馈'});
    await retry;
    assert.equal(f.page.data.content, '');
});

/** 切换后的新账号仍可主动提交, 不复用旧账号的写入上下文. */
test('反馈切换账号后可重新提交', async () => {
    const f = fixture();
    f.setToken('B');
    f.page.onShow();
    f.valid();
    const submit = f.page.submitFeedback();
    assert.equal(f.calls.length, 1);
    f.calls[0].resolve({code: 0});
    assert.equal(await submit, true);
});

/** 账号变化或离页后不清除新输入, 不展示旧反馈成功. */
test('反馈结果隔离账号和离页', async () => {
    for (const leave of [false, true]) {
        const f = fixture();
        f.valid();
        const submit = f.page.submitFeedback();
        if (leave) {
            f.page.onUnload();
        } else {
            f.setToken('B');
            f.page.onShow();
        }
        const current = f.page.data.content;
        f.calls[0].resolve({code: 0, data: '感谢你的反馈'});
        await submit;
        assert.equal(f.page.data.content, current);
        assert.equal(f.notices.length, 0);
    }
});
