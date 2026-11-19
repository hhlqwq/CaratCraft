/* 协议确认、登录失败及账号隔离回归, 不访问真实微信或业务数据库. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');
const root = path.resolve(__dirname, '../../apps/miniapp');

/** 独立装载微信脚本, 外部依赖和平台能力由用例显式注入. */
function load(file, dependencies, extras = {}) {
    const sandbox = {module: {exports: {}}, ...extras};
    sandbox.require = (name) => {
        if (!(name in dependencies)) {
            throw new Error('未知依赖: ' + name);
        }
        return dependencies[name];
    };
    vm.runInNewContext(fs.readFileSync(path.join(root, file), 'utf8'), sandbox);
    return sandbox.module.exports;
}

/** 登录前未主动勾选不得调用微信或提交资料, 服务端失败不得缓存会话. */
test('未同意协议及登录失败不会生成本地登录状态', async () => {
    let logins = 0;
    const writes = [];
    const service = load(
        'services/user.js',
        {
            '../utils/util.js': {
                login: async () => {
                    logins++;
                    return {code: 'one-time'};
                },
                request: async () => ({code: 400, msg: '版本已更新'}),
            },
            '../config/api.js': {AuthLoginByWeixin: '/login'},
        },
        {wx: {setStorageSync: (...args) => writes.push(args)}}
    );
    await assert.rejects(service.loginByWeixin({}), /主动同意/);
    assert.equal(logins, 0);
    await assert.rejects(
        service.loginByWeixin({}, {termsVersion: 1, privacyVersion: 1}),
        /版本已更新/
    );
    assert.equal(logins, 1);
    assert.equal(writes.length, 0);
});

/** 有效登录必须传递用户实际确认版本, 成功后才写入身份和令牌. */
test('登录提交精确协议版本并仅缓存成功会话', async () => {
    const requests = [];
    const storage = {};
    const app = {globalData: {}};
    const service = load(
        'services/user.js',
        {
            '../utils/util.js': {
                clearSession: () => {},
                login: async () => ({code: 'one-time'}),
                request: async (...args) => {
                    requests.push(args);
                    return {code: 0, data: {token: 'test-token', userInfo: {id: 9}, userId: 9}};
                },
            },
            '../config/api.js': {AuthLoginByWeixin: '/login'},
        },
        {
            getApp: () => app,
            wx: {
                setStorageSync: (key, value) => {
                    storage[key] = value;
                },
            },
        }
    );
    await service.loginByWeixin({}, {termsVersion: 3, privacyVersion: 4});
    assert.equal(requests[0][1].legalAcceptance.privacyVersion, 4);
    assert.equal(requests[0][2], 'POST');
    assert.equal(storage.token, 'test-token');
    assert.equal(app.globalData.token, storage.token);
    assert.equal(app.globalData.userId, 9);
});

/** 提供可控请求、组件回调及生命周期, 观察未配置和卸载后的行为. */
function consentFixture() {
    let definition;
    const requests = [];
    load(
        'components/legal-consent/index.js',
        {
            '../../utils/util.js': {
                request(url, params, method) {
                    assert.equal(method, 'GET', '读取公开协议只允许GET');
                    return new Promise((resolve) => requests.push(resolve));
                },
            },
            '../../config/api.js': {LegalDocuments: '/legal'},
        },
        {
            Component: (value) => {
                definition = value;
            },
        }
    );
    const events = [];
    const component = {
        data: structuredClone(definition.data),
        ...definition.methods,
        setData(values) {
            Object.assign(this.data, values);
        },
        triggerEvent(name, value) {
            events.push({name, value});
        },
    };
    component.alive = true;
    return {component, definition, requests, events};
}

/** 两份正文未齐时不能勾选, 加载失败后允许重试且不恢复默认同意. */
test('协议未发布保持不可登录并允许重新读取', async () => {
    const {component, requests, events} = consentFixture();
    const loading = component.load();
    requests[0]({code: 0, data: [{kind: 'TERMS', version: 1}]});
    await loading;
    assert.match(component.data.error, /尚未发布/);
    assert.equal(events[0].value, null);
    assert.equal(component.data.checked, false);
    const retry = component.load();
    requests[1]({
        code: 0,
        data: [
            {kind: 'TERMS', version: 2},
            {kind: 'PRIVACY', version: 3},
        ],
    });
    await retry;
    component.change({detail: {value: ['agree']}});
    assert.equal(events.at(-1).value.termsVersion, 2);
    component.change({detail: {value: []}});
    assert.equal(events.at(-1).value, null);
});

/** 卸载后迟到正文不回填已离开的组件. */
test('协议组件卸载隔离迟到响应', async () => {
    const {component, definition, requests} = consentFixture();
    const pending = component.load();
    definition.lifetimes.detached.call(component);
    requests[0]({
        code: 0,
        data: [
            {kind: 'TERMS', version: 1},
            {kind: 'PRIVACY', version: 1},
        ],
    });
    await pending;
    assert.equal(component.data.documents.length, 0);
});

/** 本人确认记录只能回填请求发起时的页面及账号. */
test('协议历史在离页及账号切换后不回填', async () => {
    let page;
    let token = 'first';
    const requests = [];
    load(
        'pages/ucenter/legal/index.js',
        {
            '../../../utils/util.js': {
                request(url, params, method) {
                    assert.equal(method, 'GET', '读取协议和本人历史只允许GET');
                    return new Promise((resolve) => requests.push(resolve));
                },
            },
            '../../../config/api.js': {LegalDocuments: '/legal'},
        },
        {
            Page: (value) => {
                page = value;
            },
            wx: {getStorageSync: () => token},
        }
    );
    page.setData = (values) => Object.assign(page.data, values);
    page.onLoad({});
    const pending = page.load();
    requests[0]({code: 0, data: []});
    await new Promise(setImmediate);
    page.onHide();
    token = 'second';
    requests[1]({code: 0, data: [{kind: 'PRIVACY', version: 1}]});
    await pending;
    assert.equal(page.data.history.length, 0);
});
