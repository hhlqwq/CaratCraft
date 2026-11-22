/* 小程序和管理端关键异步路径回归, 不访问网络或真实账号. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');
const root = path.resolve(__dirname, '../..');

/** 在独立上下文加载CommonJS模块, 依赖由测试注入. */
function load(relative, dependencies = {}, extras = {}) {
    const sandbox = {module: {exports: {}}, console, ...extras};
    sandbox.require = (name) => {
        if (!(name in dependencies)) {
            throw new Error('测试未声明依赖: ' + name);
        }
        return dependencies[name];
    };
    vm.runInNewContext(fs.readFileSync(path.join(root, relative), 'utf8'), sandbox);
    return sandbox.module.exports;
}

/** 提供可控的请求队列和会话存储, 观察加载及导航次数. */
function requestFixture() {
    const storage = {token: 'old-token'};
    const calls = {requests: [], navigations: [], shown: 0, hidden: 0, navigation: 0};
    const app = {globalData: {}};
    const wx = {
        getStorageSync: (key) => storage[key],
        removeStorageSync: (key) => {
            delete storage[key];
        },
        showLoading: () => {
            calls.shown++;
        },
        hideLoading: () => {
            calls.hidden++;
        },
        request: (request) => calls.requests.push(request),
        navigateTo: (options) => {
            calls.navigation++;
            calls.navigations.push(options);
        },
    };
    const util = load(
        'apps/miniapp/utils/util.js',
        {'../config/api.js': {}},
        {wx, getApp: () => app, getCurrentPages: () => []}
    );
    return {util, wx, calls, storage};
}

/** 网络失败及并发完成都必须正确释放全局加载状态. */
test('网络失败和并发请求的加载提示正确收尾', async () => {
    const {util, calls} = requestFixture();
    const first = util.request('/first');
    const second = util.request('/second');
    const result = Promise.allSettled([first, second]);
    calls.requests[0].fail({errMsg: 'offline'});
    calls.requests[0].complete();
    assert.equal(calls.shown, 1);
    assert.equal(calls.hidden, 0);
    calls.requests[1].success({statusCode: 200, data: {code: 0}});
    calls.requests[1].complete();
    assert.equal(calls.hidden, 1);
    assert.equal((await result)[0].status, 'rejected');
});

/** 同一失效会话只跳转一次, 旧请求不能清除新令牌. */
test('401导航去重并隔离旧令牌的迟到响应', async () => {
    const {util, calls, storage} = requestFixture();
    const results = Promise.allSettled([util.request('/a'), util.request('/b')]);
    for (const request of calls.requests) {
        request.success({statusCode: 401, data: {code: 401}});
        request.complete();
    }
    await results;
    assert.equal(calls.navigation, 1);
    assert.equal(storage.token, undefined);
    const next = util.request('/stale');
    const nextResult = Promise.allSettled([next]);
    storage.token = 'new-token';
    calls.requests[2].success({statusCode: 401, data: {code: 401}});
    calls.requests[2].complete();
    await nextResult;
    assert.equal(storage.token, 'new-token');
    assert.equal(calls.navigation, 1);
});

/** 登录页跳转完成后释放导航锁, 后续独立失效会话仍可进入登录页. */
test('401登录导航完成后允许后续跳转', async () => {
    const {util, calls} = requestFixture();
    const first = Promise.allSettled([util.request('/first')]);
    calls.requests[0].success({statusCode: 401, data: {code: 401}});
    calls.requests[0].complete();
    await first;
    assert.equal(calls.navigation, 1);
    calls.navigations[0].complete();
    const second = Promise.allSettled([util.request('/second')]);
    calls.requests[1].success({statusCode: 401, data: {code: 401}});
    calls.requests[1].complete();
    await second;
    assert.equal(calls.navigation, 2);
});

/** 预下单网络拒绝必须到达调用方, 不触发支付弹窗. */
test('支付预下单网络失败不会留下待定Promise', async () => {
    let paymentCalls = 0;
    const pay = load(
        'apps/miniapp/services/pay.js',
        {
            '../utils/util.js': {request: () => Promise.reject(new Error('offline'))},
            '../config/api.js': {PayPrepayId: '/prepay'},
        },
        {
            wx: {
                requestPayment: () => {
                    paymentCalls++;
                },
            },
        }
    );
    await assert.rejects(pay.payOrder(1), /offline/);
    assert.equal(paymentCalls, 0);
});

/** 支付客户端成功也必须查询服务端的可信状态. */
test('支付成功后查询服务端状态', async () => {
    const urls = [];
    const pay = load(
        'apps/miniapp/services/pay.js',
        {
            '../utils/util.js': {
                request: (url) => {
                    urls.push(url);
                    return Promise.resolve({code: 0, data: {}});
                },
            },
            '../config/api.js': {PayPrepayId: '/prepay', OrderQuery: '/query'},
        },
        {wx: {requestPayment: (options) => options.success({})}}
    );
    await pay.payOrder(1);
    assert.deepEqual(urls, ['/prepay', '/query']);
});

/** 微信Page定义使用内存setData, 避免依赖开发者工具. */
function loadCheckout(request, storage, extraWx = {}) {
    let page;
    const app = {globalData: {userCoupon: 'NO_USE_COUPON'}};
    const wx = {
        getStorageSync: (key) => storage[key],
        stopPullDownRefresh() {},
        removeStorageSync() {},
        ...extraWx,
    };
    const reads = load('apps/miniapp/services/page-read.js', {}, {wx});
    const actions = load(
        'apps/miniapp/services/page-action.js',
        {'../utils/util.js': {showErrorToast() {}}},
        {wx}
    );
    load(
        'apps/miniapp/pages/shopping/checkout/checkout.js',
        {
            '../../../utils/util.js': {request, showErrorToast: () => {}},
            '../../../config/api.js': {CartCheckout: '/checkout', OrderSubmit: '/submit'},
            '../../../services/page-read.js': reads,
            '../../../services/page-action.js': actions,
        },
        {
            Page: (definition) => {
                page = definition;
            },
            getApp: () => app,
            wx,
        }
    );
    page.setData = (values) => Object.assign(page.data, values);
    return page;
}

/** 所选地址必须在请求前同步, 连点提交只产生一个请求. */
test('结算先读取所选地址, 提交按钮阻止重复请求', async () => {
    const calls = [];
    let complete;
    const page = loadCheckout(
        (url, data) => {
            calls.push({url, data});
            return new Promise((resolve) => {
                complete = resolve;
            });
        },
        {addressId: 12}
    );
    page.onLoad({});
    const reading = page.onShow();
    assert.equal(calls[0].data.addressId, 12);
    await page.submitOrder();
    assert.equal(calls.length, 1);
    complete({
        code: 0,
        data: {
            checkedGoodsList: [{goodsId: 1, productId: 2, number: 1}],
            checkedAddress: {id: 12},
            actualPrice: '10.00',
        },
    });
    await reading;
    calls.length = 0;
    const submitted = page.submitOrder();
    page.submitOrder();
    assert.equal(calls.length, 1);
    complete({code: 400, msg: '测试失败'});
    await submitted;
    assert.equal(page.data.actionBusy, false);
});

/** 使用当前Vue编译器读取组件, 导入依赖替换为占位对象, 不依赖旧Babel工具链. */
function loadVue(relative) {
    const compiler = require(path.join(root, 'apps/admin-ui/node_modules/@vue/compiler-sfc'));
    const source = compiler.parse(fs.readFileSync(path.join(root, relative), 'utf8')).descriptor
        .script.content;
    let transformed = compiler.rewriteDefault(source, '_component');
    const imports = compiler
        .babelParse(transformed, {sourceType: 'module'})
        .program.body.filter((node) => node.type === 'ImportDeclaration');
    for (const node of imports.reverse()) {
        transformed =
            transformed.slice(0, node.start) +
            node.specifiers.map((item) => 'const ' + item.local.name + ' = {};').join('\n') +
            transformed.slice(node.end);
    }
    transformed += '\nmodule.exports.default = _component;';
    const module = {exports: {}};
    vm.runInNewContext(transformed, {
        module,
        exports: module.exports,
        require: () => ({}),
        FileReader: class {
            /** 模拟浏览器读取本地JSON, 不写入实际文件. */
            readAsText() {
                this.onload({
                    target: {result: JSON.stringify({goods: {name: '测试商品', categoryId: 1}})},
                });
            }
        },
    });
    return module.exports.default;
}

/** 合法商品JSON必须进入确认和保存, 不再报未定义变量. */
test('合法商品JSON可以进入确认与保存', async () => {
    const component = loadVue('apps/admin-ui/src/views/modules/mall/goods.vue');
    let saves = 0;
    const context = {
        $confirm: () => Promise.resolve(),
        $http: () => {
            saves++;
            return Promise.resolve({data: {code: 0, data: 1}});
        },
        $message: {
            success: () => {},
            error: (message) => {
                throw new Error(message);
            },
        },
        getDataList: () => {},
    };
    component.methods.importGoodsJson.call(context, {raw: {}});
    await new Promise(setImmediate);
    assert.equal(saves, 1);
});

/** 未知尺寸不得被自动写成最小值或零, 小数精度和范围需在提交前校验. */
test('材料尺寸留空保持未知并拒绝无效测量值', () => {
    const component = loadVue('apps/admin-ui/src/views/modules/custom/configuration.vue');
    const parse = component.methods.measuredSize;
    assert.equal(parse(null), null);
    assert.equal(parse(''), null);
    assert.equal(parse('  '), null);
    assert.equal(parse('8.125'), 8.125);
    for (const value of ['0', '-1', '3001', '8.1234', '1e2', '8-9', '未知']) {
        assert.throws(() => parse(value));
    }
});

/** 创建可控制返回顺序的定制页, 不接入真实微信接口. */
function designerFixture() {
    let page;
    const requests = [];
    let token = 'A';
    load(
        'apps/miniapp/pages/designer/designer.js',
        {
            '../../utils/util.js': {
                request: (url, data) =>
                    new Promise((resolve, reject) => {
                        requests.push({url, data, resolve, reject});
                    }),
            },
            '../../config/api.js': {
                CustomizationConfig: '/config',
                CustomizationQuote: '/quote',
                CustomizationReference: '/reference',
            },
            '../../services/necklace-arrangement.js': require('../../apps/miniapp/services/necklace-arrangement.js'),
        },
        {
            Page: (definition) => {
                page = definition;
            },
            wx: {
                showModal: (options) => options.success({confirm: true}),
                getStorageSync: () => token,
            },
            setTimeout,
            clearTimeout,
        }
    );
    page.setData = (values) => Object.assign(page.data, values);
    return {
        page,
        requests,
        setToken: (value) => {
            token = value;
        },
    };
}

/** 规则参考仅发送毫米测量和偏好, 并发点击防重, 返回值不得修改设计或生产尺寸. */
test('规则参考复用实测输入且不自动采用长度和材料', async () => {
    const {page, requests} = designerFixture();
    Object.assign(page.data, {goodsId: 10, selectedOptionIds: [7], targetLengthMm: '450'});
    const pending = page.requestReference();
    await page.requestReference();
    assert.equal(requests.length, 1);
    assert.deepEqual(JSON.parse(JSON.stringify(requests[0].data)), {
        goodsId: 10,
        neckCircumferenceMm: null,
        preference: 'MEDIUM',
    });
    requests[0].resolve({
        code: 0,
        data: {goodsId: 10, lengthsMm: [457.5, 507.5], materials: [{name: '珍珠'}], sources: []},
    });
    await pending;
    assert.equal(page.data.referenceResult.lengthsLabel, '45.75 / 50.75');
    assert.equal(page.data.referenceLoading, false);
    assert.deepEqual(page.data.selectedOptionIds, [7]);
    assert.equal(page.data.targetLengthMm, '450');
});

/** 无效测量值在请求前拒绝, 空值不猜测为0. */
test('参考输入拒绝非法精度及范围', async () => {
    const {page, requests} = designerFixture();
    for (const value of ['0', '199', '801', '350.12', '3e2', '未知']) {
        page.data.neckCircumferenceMm = value;
        await page.requestReference();
        assert.ok(page.data.referenceError);
    }
    assert.equal(requests.length, 0);
});

/** 失败释放读取锁并保留设计和输入, 可由用户重试. */
test('规则参考失败正确收尾', async () => {
    const {page, requests} = designerFixture();
    Object.assign(page.data, {goodsId: 10, neckCircumferenceMm: '350', selectedOptionIds: [7]});
    const pending = page.requestReference();
    requests[0].reject(new Error('服务暂不可用'));
    await pending;
    assert.equal(page.data.referenceLoading, false);
    assert.equal(page.data.referenceError, '服务暂不可用');
    assert.equal(page.data.neckCircumferenceMm, '350');
    assert.deepEqual(page.data.selectedOptionIds, [7]);
});

/** 修改偏好后旧结果不得覆盖最新参考, 不再依赖云环境状态请求. */
test('参考参数变更隔离乱序响应', async () => {
    const {page, requests} = designerFixture();
    page.data.goodsId = 10;
    const old = page.requestReference();
    page.changeReferencePreference({detail: {value: '2'}});
    const fresh = page.requestReference();
    requests[1].resolve({code: 0, data: {goodsId: 10, lengthsMm: [610], materials: []}});
    await fresh;
    requests[0].resolve({code: 0, data: {goodsId: 10, lengthsMm: [405], materials: []}});
    await old;
    assert.equal(page.data.referenceResult.lengthsLabel, '61');
});

/** 账号、商品或页面变化后废弃旧参考, 不复用其他用户的身体输入结果. */
test('参考响应核对账号商品及页面生命周期', async () => {
    for (const change of ['account', 'goods', 'unload']) {
        const {page, requests, setToken} = designerFixture();
        page.data.goodsId = 10;
        const pending = page.requestReference();
        if (change === 'account') {
            setToken('B');
        }
        if (change === 'goods') {
            page.data.goodsId = 11;
        }
        if (change === 'unload') {
            page.onUnload();
        }
        requests[0].resolve({code: 0, data: {goodsId: 10, lengthsMm: [405], materials: []}});
        await pending;
        assert.equal(page.data.referenceResult, null);
    }
});

/** 同一实测字段用于参考和报价, 尺寸编辑立即废弃在途参考. */
test('实测颈围变更立即废弃参考', async () => {
    const {page, requests} = designerFixture();
    page.data.goodsId = 10;
    const pending = page.requestReference();
    page.refreshQuote = () => Promise.resolve();
    page.changeSizing({
        currentTarget: {dataset: {field: 'neckCircumferenceMm'}},
        detail: {value: '360'},
    });
    requests[0].resolve({code: 0, data: {goodsId: 10, lengthsMm: [405], materials: []}});
    await pending;
    assert.equal(page.data.referenceResult, null);
    assert.equal(page.data.neckCircumferenceMm, '360');
});

/** 报价失败时旧价格立即失效, 后续不能保存或结算. */
test('定制报价失败清除旧价格并禁止保存', async () => {
    const {page, requests} = designerFixture();
    page.data.quote = {totalPrice: 199};
    const refresh = page.refreshQuote();
    assert.equal(page.data.quote.totalPrice, undefined);
    assert.equal(page.data.quoting, true);
    requests[0].reject(new Error('库存不足'));
    await refresh;
    assert.equal(page.data.quoteError, '库存不足');
    assert.equal(page.data.quoting, false);
    await assert.rejects(page.persistDesign(true), /有效报价/);
});

/** 配置乱序返回时仅采纳当前商品, 必选类型读取商家配置. */
test('定制配置拒绝过期响应并采用商家必选项', async () => {
    const {page, requests} = designerFixture();
    page.data.goodsId = 1;
    const first = page.loadConfig();
    page.data.goodsId = 2;
    const second = page.loadConfig();
    requests[1].resolve({
        code: 0,
        data: {
            product: {
                goodsId: 2,
                requiredComponentTypes: 'chain',
                previewWidth: 500,
                previewHeight: 750,
            },
            primaryProductId: 22,
            options: [
                {
                    id: 2,
                    componentType: 'chain',
                    name: '链长',
                    assetUrl: '/merchant-layer.png',
                    zIndex: 4,
                },
            ],
        },
    });
    await second;
    requests[0].resolve({
        code: 0,
        data: {
            product: {goodsId: 1},
            primaryProductId: 11,
            options: [],
        },
    });
    await first;
    assert.equal(page.data.productConfig.goodsId, 2);
    assert.equal(page.data.primaryProductId, 22);
    assert.equal(page.data.componentGroups[0].required, true);
    assert.equal(page.data.arrangementNodes[0].name, '链长');
    assert.equal(page.data.arrangementNodes[0].known, false);
    assert.deepEqual(Array.from(page.data.arrangementOptionIds), [2]);
});

/** 所有版本忽略设备残留地址, 正式版必须匹配已配置的 HTTPS 域名. */
test('小程序正式环境强制使用配置的HTTPS域名', () => {
    const environment = {
        releaseBaseUrl: 'https://merchant.example/platform-framework-api/app/',
    };
    const wx = {
        getStorageSync: () => 'http://localhost:9999/app/',
        getAccountInfoSync: () => ({miniProgram: {envVersion: 'release'}}),
    };
    const api = load('apps/miniapp/config/api.js', {'./environment.js': environment}, {wx});
    assert.equal(api.ApiOrigin, 'https://merchant.example');
    wx.getAccountInfoSync = () => ({miniProgram: {envVersion: 'develop'}});
    environment.developBaseUrl = 'http://192.168.100.5:8888/platform-framework-api/app/';
    assert.equal(
        load('apps/miniapp/config/api.js', {'./environment.js': environment}, {wx}).ApiOrigin,
        'http://192.168.100.5:8888'
    );
    wx.getAccountInfoSync = () => ({miniProgram: {envVersion: 'release'}});
    environment.releaseBaseUrl = 'http://localhost:8888/app/';
    assert.throws(
        () => load('apps/miniapp/config/api.js', {'./environment.js': environment}, {wx}),
        /HTTPS/
    );
});

/** 上传图片补齐API来源, 包内静态资源和普通文案保持原内容. */
test('上传图片同源地址可以在小程序显示', () => {
    const util = load(
        'apps/miniapp/utils/util.js',
        {
            '../config/api.js': {ApiOrigin: 'https://merchant.example'},
        },
        {wx: {}}
    );
    const data = {
        data: {
            headImgUrl: '/upload/avatar.png',
            listPicUrl: '/static/images/logo.png',
            name: '/upload/这是商品名称',
        },
    };
    util.normalizeAssetUrls(data);
    assert.equal(data.data.headImgUrl, 'https://merchant.example/upload/avatar.png');
    assert.equal(data.data.listPicUrl, '/static/images/logo.png');
    assert.equal(data.data.name, '/upload/这是商品名称');
});
