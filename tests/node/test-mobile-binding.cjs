/* 手机号绑定页面真实脚本回归, 不发送短信或绑定真实账号. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');

/** 注入受控短信请求、确认框和计时器, 保留真实页面方法. */
function fixture() {
    let page;
    const calls = [];
    const messages = [];
    const modals = [];
    const switches = [];
    const storage = {token: 'A'};
    const app = {globalData: {token: '', userInfo: {nickname: '隔离用户'}}};
    vm.runInNewContext(
        fs.readFileSync(
            path.join(__dirname, '../../apps/miniapp/pages/auth/mobile/mobile.js'),
            'utf8'
        ),
        {
            Page: (definition) => {
                page = definition;
            },
            getApp: () => app,
            require: (name) =>
                name.includes('util')
                    ? {
                          request: (...args) =>
                              new Promise((resolve, reject) => calls.push({args, resolve, reject})),
                      }
                    : {SmsCode: 'smscode', BindMobile: 'bindmobile'},
            wx: {
                getStorageSync: (key) => storage[key],
                showModal: (options) => modals.push(options),
                showToast: (options) => messages.push(options.title),
                switchTab: (options) => switches.push(options.url),
            },
            setInterval: () => 1,
            clearInterval: () => {},
        }
    );
    page.setData = (values) => Object.assign(page.data, values);
    page.onLoad();
    return {page, calls, messages, modals, switches, storage, app};
}

/** 四位验证码被拒绝, 六位只提交一次并等待用户确认后跳转. */
test('手机号绑定校验六位验证码并防重', async () => {
    const {page, calls, messages, modals, switches} = fixture();
    assert.equal(page._unloaded, false);
    assert.equal(page.data.userInfo.nickname, '隔离用户');
    page.bindInputMobile({detail: {value: '138-0000-0000'}});
    page.setData({disableSubmitMobileCode: false});
    await page.bindLoginMobilecode({detail: {value: {code: '1234'}}});
    assert.equal(calls.length, 0);
    assert.ok(messages.includes('请输入六位验证码'));
    const first = page.bindLoginMobilecode({detail: {value: {code: '123456'}}});
    const second = page.bindLoginMobilecode({detail: {value: {code: '123456'}}});
    assert.equal(calls.length, 1);
    calls[0].resolve({code: 0, data: '手机绑定成功'});
    await Promise.all([first, second]);
    assert.equal(modals.length, 1);
    assert.equal(page.data.submitting, false);
    modals[0].success({confirm: true});
    assert.deepEqual(switches, ['/pages/ucenter/index/index']);
});

/** 短信发送失败立即恢复按钮, 成功才启动倒计时. */
test('手机号验证码失败恢复按钮且成功后倒计时', async () => {
    const {page, calls, messages} = fixture();
    page.setData({mobile: '13800000000'});
    const failed = page.countDownPassCode();
    assert.equal(page.data.sendingCode, true);
    calls[0].reject(new Error('offline'));
    await failed;
    assert.equal(page.data.sendingCode, false);
    assert.equal(page.data.disableGetMobileCode, false);
    assert.ok(messages.includes('offline'));
    const sent = page.countDownPassCode();
    calls[1].resolve({code: 0, data: '短信发送成功'});
    await sent;
    assert.equal(page.data.disableSubmitMobileCode, false);
    assert.match(page.data.getCodeButtonText, /^60s$/);
    page.onUnload();
    assert.equal(page._unloaded, true);
});

/** 账号切换后旧绑定响应不得提示成功或跳转. */
test('手机号绑定隔离账号切换响应', async () => {
    const {page, calls, modals, storage} = fixture();
    page.setData({mobile: '13800000000'});
    const submit = page.bindLoginMobilecode({detail: {value: {code: '123456'}}});
    storage.token = 'B';
    calls[0].resolve({code: 0, data: '手机绑定成功'});
    await submit;
    assert.equal(modals.length, 0);
});
