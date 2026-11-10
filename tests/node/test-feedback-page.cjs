/* 意见反馈页面真实脚本回归, 不访问账号或业务接口. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');

/** 注入受控请求、存储和提示, 保留真实页面方法. */
function fixture() {
    let page;
    const calls = [];
    const messages = [];
    const storage = {token: 'A'};
    vm.runInNewContext(
        fs.readFileSync(
            path.join(__dirname, '../../apps/miniapp/pages/ucenter/feedback/feedback.js'),
            'utf8'
        ),
        {
            Page: (definition) => {
                page = definition;
            },
            require: (name) =>
                name.includes('util')
                    ? {
                          request: (...args) =>
                              new Promise((resolve, reject) => calls.push({args, resolve, reject})),
                          showErrorToast: (text) => messages.push(text),
                      }
                    : {FeedbackAdd: 'feedback'},
            wx: {
                getStorageSync: (key) => storage[key],
                showToast: (options) => messages.push(options.title),
            },
        }
    );
    page.setData = (values) => Object.assign(page.data, values);
    return {page, calls, messages, storage};
}

/** 等待页面异步链完成. */
async function settle() {
    await new Promise((resolve) => setImmediate(resolve));
}

/** 合法反馈只提交一次, 成功后清空表单. */
test('反馈提交防重并在成功后清空表单', async () => {
    const {page, calls, messages} = fixture();
    page.setData({index: 2, content: '  希望增加礼盒  ', mobile: '13800000000'});
    const first = page.sbmitFeedback();
    const second = page.sbmitFeedback();
    assert.equal(calls.length, 1);
    assert.equal(page.data.submitting, true);
    calls[0].resolve({code: 0, data: '感谢你的反馈'});
    await Promise.all([first, second]);
    assert.equal(calls[0].args[1].content, '希望增加礼盒');
    assert.equal(page.data.index, 0);
    assert.equal(page.data.content, '');
    assert.equal(page.data.mobile, '');
    assert.equal(page.data.submitting, false);
    assert.ok(messages.includes('感谢反馈'));
    await settle();
});

/** 校验失败不发送请求, 网络失败释放锁并保留输入. */
test('反馈校验失败不提交且失败保留输入', async () => {
    const {page, calls, messages} = fixture();
    page.setData({index: 0, content: '建议', mobile: '13800000000'});
    await page.sbmitFeedback();
    assert.equal(calls.length, 0);
    assert.ok(messages.includes('请选择反馈类型'));
    page.setData({index: 1, content: '建议', mobile: '123'});
    await page.sbmitFeedback();
    assert.equal(calls.length, 0);
    assert.ok(messages.includes('请输入有效的手机号码'));
    page.setData({index: 1, content: '建议', mobile: '13800000000'});
    const submit = page.sbmitFeedback();
    calls[0].reject(new Error('offline'));
    await submit;
    assert.equal(page.data.submitting, false);
    assert.equal(page.data.content, '建议');
    assert.ok(messages.includes('offline'));
});

/** 手机号输入仅保留数字并限制十一位. */
test('反馈手机号输入只保留国内号码数字', () => {
    const {page} = fixture();
    page.mobileInput({detail: {value: '138-0000-0000abc'}});
    assert.equal(page.data.mobile, '13800000000');
    page.mobileInput({detail: {value: '138000000001234'}});
    assert.equal(page.data.mobile, '13800000000');
});
