/* 客服缓存、页面入口和本人售后状态展示回归, 不调用微信或业务接口. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');
const root = path.resolve(__dirname, '../../apps/miniapp');

/** 在独立上下文装载模块, 依赖由测试明确提供. */
function moduleAt(relative, dependencies, extras = {}) {
    const module = {exports: {}};
    vm.runInNewContext(fs.readFileSync(path.join(root, relative), 'utf8'), {
        module,
        require: (name) => {
            if (!(name in dependencies)) {
                throw new Error('测试缺少依赖: ' + name);
            }
            return dependencies[name];
        },
        ...extras,
    });
    return module.exports;
}

/** 客服并发读取只发一个请求, 缓存到期后读取商家新配置. */
test('客服请求合并并在缓存到期后更新', async () => {
    let now = 0;
    const calls = [];
    const util = {
        request(url, params, method) {
            assert.equal(method, 'GET', '公开客服资料只允许GET');
            return new Promise((resolve, reject) => calls.push({resolve, reject}));
        },
    };
    const contacts = moduleAt(
        'services/merchant-contact.js',
        {'../utils/util.js': util, '../config/api.js': {}},
        {Date: {now: () => now}}
    );
    const first = contacts.load();
    const second = contacts.load();
    assert.equal(calls.length, 1);
    calls[0].resolve({code: 0, data: {wechatId: 'owned-account', version: 1}});
    assert.equal((await first).version, 1);
    assert.equal((await second).version, 1);
    await contacts.load();
    assert.equal(calls.length, 1);
    now += 6 * 60 * 1000;
    const refresh = contacts.load();
    calls[1].resolve({code: 0, data: {wechatId: '', version: 2}});
    assert.equal((await refresh).version, 2);
});

/** 网络失败不能把失败Promise永久缓存, 下一次点击仍可重试. */
test('客服加载失败后可以再次请求', async () => {
    let count = 0;
    const util = {
        request: () => {
            count++;
            return count === 1
                ? Promise.reject(new Error('offline'))
                : Promise.resolve({code: 0, data: {version: 0}});
        },
    };
    const contacts = moduleAt('services/merchant-contact.js', {
        '../utils/util.js': util,
        '../config/api.js': {},
    });
    await assert.rejects(contacts.load(), /offline/);
    assert.equal((await contacts.load()).version, 0);
    assert.equal(count, 2);
});

/** 组件卸载后的异步结果不能再次写页面, 空客服资料不能伪造可添加账号. */
test('客服组件保留未配置状态并忽略卸载后的响应', async () => {
    let definition;
    let complete;
    moduleAt(
        'components/merchant-contact/index.js',
        {
            '../../services/merchant-contact.js': {
                load: () =>
                    new Promise((resolve) => {
                        complete = resolve;
                    }),
            },
        },
        {
            Component: (value) => {
                definition = value;
            },
        }
    );
    const component = {...definition.methods, data: JSON.parse(JSON.stringify(definition.data))};
    let updates = 0;
    component.setData = (values) => {
        updates++;
        Object.assign(component.data, values);
    };
    definition.lifetimes.attached.call(component);
    const opened = component.open();
    complete({version: 0, wechatId: '', phone: '', qrUrl: '', wxChatEnabled: false});
    await opened;
    assert.equal(component.data.configured, false);
    const next = component.open();
    definition.lifetimes.detached.call(component);
    const before = updates;
    complete({version: 1, wechatId: 'not-for-destroyed-page'});
    await next;
    assert.equal(updates, before);
});

/** 所有已注册页面都保留显眼客服入口, 包括独立售后中心. */
test('全部注册页面包含统一客服组件', () => {
    const app = JSON.parse(fs.readFileSync(path.join(root, 'app.json'), 'utf8'));
    assert.equal(app.usingComponents['merchant-contact'], '/components/merchant-contact/index');
    assert.ok(app.pages.includes('pages/ucenter/aftersales/index'));
    for (const route of app.pages) {
        for (const extension of ['js', 'json', 'wxml', 'wxss']) {
            assert.ok(
                fs.existsSync(path.join(root, route + '.' + extension)),
                route + '.' + extension
            );
        }
        assert.match(
            fs.readFileSync(path.join(root, route + '.wxml'), 'utf8'),
            /<merchant-contact\s*\/>/,
            route
        );
    }
});

/** 构造可控制响应顺序的售后页面, 不使用真实登录令牌或订单. */
function refundsFixture() {
    let page;
    const calls = [];
    const util = {
        request: (url, params) =>
            new Promise((resolve, reject) => calls.push({params, resolve, reject})),
    };
    moduleAt(
        'pages/ucenter/aftersales/index.js',
        {'../../../utils/util.js': util, '../../../config/api.js': {}},
        {
            Page: (value) => {
                page = value;
            },
            wx: {stopPullDownRefresh() {}},
        }
    );
    page.data = JSON.parse(JSON.stringify(page.data));
    page.setData = (values) => Object.assign(page.data, values);
    page.onLoad();
    return {page, calls};
}

/** 支付渠道受理不能被界面显示为退款成功, 状态只按服务端结果区分. */
test('售后中心区分处理中与确认成功', async () => {
    const {page, calls} = refundsFixture();
    page.onShow();
    calls[0].resolve({
        code: 0,
        data: {
            records: [
                {orderId: 1, status: 'PROCESSING', amountFen: 12345},
                {orderId: 2, status: 'SUCCESS', amountFen: 500},
            ],
            total: 2,
        },
    });
    await new Promise(setImmediate);
    assert.equal(page.data.records[0].statusText, '支付渠道处理中');
    assert.equal(page.data.records[0].amountText, '123.45');
    assert.equal(page.data.records[1].statusText, '退款成功');
    assert.equal(page.data.hasMore, false);
});

/** 返回页面时的新加载必须覆盖旧请求, 分页失败不跳过未加载的数据. */
test('售后请求隔离迟到响应并支持原页重试', async () => {
    const {page, calls} = refundsFixture();
    page.onShow();
    const current = page.reload();
    calls[1].reject(new Error('offline'));
    await current;
    assert.equal(page.data.page, 1);
    const retry = page.loadMore();
    assert.equal(calls[2].params.page, 1);
    calls[2].resolve({
        code: 0,
        data: {records: [{orderId: 3, status: 'RETRY', amountFen: 100}], total: 1},
    });
    await retry;
    calls[0].resolve({
        code: 0,
        data: {records: [{orderId: 4, status: 'SUCCESS', amountFen: 100}], total: 1},
    });
    await new Promise(setImmediate);
    assert.equal(page.data.records[0].orderId, 3);
    assert.equal(page.data.error, '');
});

/** 离开私人页面立即清空缓存, 旧账号请求完成后也不能恢复原来的订单资料. */
test('离开售后页面清除私人列表并隔离迟到响应', async () => {
    const {page, calls} = refundsFixture();
    page.onShow();
    page.data.records = [{orderId: 99}];
    page.onHide();
    calls[0].resolve({
        code: 0,
        data: {records: [{orderId: 99, status: 'SUCCESS', amountFen: 100}], total: 1},
    });
    await new Promise(setImmediate);
    assert.equal(page.data.records.length, 0);
    assert.equal(page.data.total, 0);
});
