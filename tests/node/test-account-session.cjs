/* 账号隔离回归使用合成状态, 不访问微信或业务接口. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');
const root = path.join(__dirname, '../../apps/miniapp');

/** 给页面提供独立存储和可控制的请求, 不共享其他用例的会话. */
function fixture(file, dependencies = {}) {
    let page;
    const storage = {token: 'A', userInfo: {nickname: 'A'}};
    const app = {globalData: {token: 'A', userInfo: {nickname: 'A'}, selectedCoupon: {id: 7}}};
    const calls = [];
    const wx = {
        getStorageSync: (key) => storage[key],
        setStorageSync: (key, value) => {
            storage[key] = value;
        },
        removeStorageSync: (key) => {
            delete storage[key];
        },
        showLoading() {},
        hideLoading() {},
        showToast() {},
        switchTab() {},
        showModal: (options) => options.success({confirm: true}),
        request: (options) => calls.push(options),
    };
    const context = {
        module: {exports: {}},
        wx,
        getApp: () => app,
        getCurrentPages: () => [],
        setTimeout,
        clearTimeout,
        Page: (definition) => {
            page = {...definition, data: structuredClone(definition.data)};
        },
        require: (name) => {
            if (name === '../../services/page-read.js') {
                const shared = {module: {exports: {}}, wx};
                vm.runInNewContext(
                    fs.readFileSync(path.join(root, 'services/page-read.js'), 'utf8'),
                    shared
                );
                return shared.module.exports;
            }
            if (!(name in dependencies)) {
                throw new Error('测试未提供依赖: ' + name);
            }
            return dependencies[name];
        },
    };
    vm.runInNewContext(fs.readFileSync(path.join(root, file), 'utf8'), context);
    if (page) {
        page.setData = (values) => Object.assign(page.data, values);
    }
    return {page, storage, app, calls, exported: context.module.exports, wx};
}

/** 主动退出清空身份及购买上下文, 保留无关应用设置. */
test('会话清理覆盖全局令牌与本地购买上下文', () => {
    const {storage, app, exported} = fixture('utils/util.js', {'../config/api.js': {}});
    Object.assign(storage, {userId: 1, addressId: 9, designerSnapshotId: 12, theme: 'light'});
    exported.clearSession();
    assert.equal(storage.token, undefined);
    assert.equal(storage.userId, undefined);
    assert.equal(storage.designerSnapshotId, undefined);
    assert.equal(app.globalData.token, '');
    assert.equal(app.globalData.userCoupon, 'NO_USE_COUPON');
    assert.equal(Object.keys(app.globalData.selectedCoupon).length, 0);
    assert.equal(storage.theme, 'light');
});

/** 普通成功响应也必须核对令牌, 不能只对401做账号隔离. */
test('旧账号成功响应在令牌变化后被拒绝', async () => {
    const {storage, calls, exported} = fixture('utils/util.js', {'../config/api.js': {}});
    const pending = exported.request('/profile');
    storage.token = 'B';
    calls[0].success({statusCode: 200, data: {code: 0, data: {nickname: 'A'}}});
    calls[0].complete();
    await assert.rejects(pending, /账号已切换/);
    assert.equal(storage.token, 'B');
});

/** 协议更新只打开一个确认页, 不清除会话或自动重放交易请求. */
test('协议更新导航去重且保留现有会话', async () => {
    const {calls, exported, wx, storage} = fixture('utils/util.js', {'../config/api.js': {}});
    const navigation = [];
    wx.navigateTo = (options) => navigation.push(options);
    const results = Promise.allSettled([exported.request('/first'), exported.request('/second')]);
    for (const request of calls) {
        request.success({statusCode: 200, data: {code: 428}});
        request.complete();
    }
    const completed = await results;
    assert.equal(
        completed.every((item) => item.status === 'rejected'),
        true
    );
    assert.equal(navigation.length, 1);
    assert.equal(navigation[0].url, '/pages/ucenter/legal/index');
    assert.equal(storage.token, 'A');
    assert.equal(calls.length, 2);
});

/** 个人中心即使使用独立请求替身, 迟到资料也不能覆盖新账号存储. */
test('个人资料回调同时核对账号和页面代次', async () => {
    let complete;
    const util = {
        request: () =>
            new Promise((resolve) => {
                complete = resolve;
            }),
    };
    const {page, storage} = fixture('pages/ucenter/index/index.js', {
        '../../../utils/util.js': util,
        '../../../config/api.js': {},
    });
    page.onShow();
    storage.token = 'B';
    storage.userInfo = {nickname: 'B'};
    complete({code: 0, data: {nickname: 'A'}});
    await new Promise(setImmediate);
    assert.equal(storage.userInfo.nickname, 'B');
});

/** 常驻DIY在账号切换时清除私人输入, 同账号切页仍保留正在编辑的内容. */
test('DIY账号切换清除草稿和身体输入但同账号保留编辑', () => {
    const {page, storage} = fixture('pages/designer/designer.js', {
        '../../utils/util.js': {},
        '../../config/api.js': {},
        '../../services/necklace-arrangement.js': {},
    });
    page._sessionToken = 'A';
    page.invalidateReference = () => {};
    page.loadConfig = () => {};
    Object.assign(page.data, {
        draftId: 9,
        engraving: 'A的刻字',
        neckCircumferenceMm: 350,
        referenceResult: {goodsId: 10},
    });
    page.onShow();
    assert.equal(page.data.draftId, 9);
    storage.token = 'B';
    page.onShow();
    assert.equal(page.data.draftId, null);
    assert.equal(page.data.engraving, '');
    assert.equal(page.data.neckCircumferenceMm, '');
    assert.equal(page.data.referenceResult, null);
});

/** 首页游客进入只请求商品区块, 不交换微信身份或写会话. */
test('首页不再请求已停用的静默登录接口', () => {
    const {page} = fixture('pages/index/index.js', {
        '../../utils/util.js': {},
        '../../config/api.js': {},
    });
    let loaded = 0;
    page.getIndexData = () => {
        loaded++;
    };
    page.onLoad({});
    assert.equal(loaded, 1);
});
