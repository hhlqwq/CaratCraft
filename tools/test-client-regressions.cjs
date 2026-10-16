/* 小程序和管理端关键异步路径回归, 不访问网络或真实账号. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');
const root = path.resolve(__dirname, '../vendor/platform-wxshop');

/** 在独立上下文加载CommonJS模块, 依赖由测试注入. */
function load(relative, dependencies = {}, extras = {}) {
  const sandbox = {module: {exports: {}}, console, ...extras};
  sandbox.require = name => {
    if (!(name in dependencies)) throw new Error('测试未声明依赖: ' + name);
    return dependencies[name];
  };
  vm.runInNewContext(fs.readFileSync(path.join(root, relative), 'utf8'), sandbox);
  return sandbox.module.exports;
}

/** 提供可控的请求队列和会话存储, 观察加载及导航次数. */
function requestFixture() {
  const storage = {token: 'old-token'};
  const calls = {requests: [], shown: 0, hidden: 0, navigation: 0};
  const app = {globalData: {}};
  const wx = {
    getStorageSync: key => storage[key],
    removeStorageSync: key => { delete storage[key]; },
    showLoading: () => { calls.shown++; },
    hideLoading: () => { calls.hidden++; },
    request: request => calls.requests.push(request),
    navigateTo: () => { calls.navigation++; },
  };
  const util = load('wx-mall/utils/util.js', {'../config/api.js': {}},
      {wx, getApp: () => app, getCurrentPages: () => []});
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

/** 预下单网络拒绝必须到达调用方, 不触发支付弹窗. */
test('支付预下单网络失败不会留下待定Promise', async () => {
  let paymentCalls = 0;
  const pay = load('wx-mall/services/pay.js', {
    '../utils/util.js': {request: () => Promise.reject(new Error('offline'))},
    '../config/api.js': {PayPrepayId: '/prepay'},
  }, {wx: {requestPayment: () => { paymentCalls++; }}});
  await assert.rejects(pay.payOrder(1), /offline/);
  assert.equal(paymentCalls, 0);
});

/** 支付客户端成功也必须查询服务端的可信状态. */
test('支付成功后查询服务端状态', async () => {
  const urls = [];
  const pay = load('wx-mall/services/pay.js', {
    '../utils/util.js': {request: url => {
      urls.push(url);
      return Promise.resolve({code: 0, data: {}});
    }},
    '../config/api.js': {PayPrepayId: '/prepay', OrderQuery: '/query'},
  }, {wx: {requestPayment: options => options.success({})}});
  await pay.payOrder(1);
  assert.deepEqual(urls, ['/prepay', '/query']);
});

/** 微信Page定义使用内存setData, 避免依赖开发者工具. */
function loadCheckout(request, storage, extraWx = {}) {
  let page;
  const app = {globalData: {userCoupon: 'NO_USE_COUPON'}};
  load('wx-mall/pages/shopping/checkout/checkout.js', {
    '../../../utils/util.js': {request, showErrorToast: () => {}},
    '../../../config/api.js': {CartCheckout: '/checkout', OrderSubmit: '/submit'},
    '../../../services/pay.js': {},
  }, {
    Page: definition => { page = definition; },
    getApp: () => app,
    wx: {getStorageSync: key => storage[key], ...extraWx},
  });
  page.setData = values => Object.assign(page.data, values);
  return page;
}

/** 所选地址必须在请求前同步, 连点提交只产生一个请求. */
test('结算先读取所选地址, 提交按钮阻止重复请求', async () => {
  const calls = [];
  let complete;
  const page = loadCheckout((url, data) => {
    calls.push({url, data});
    return new Promise(resolve => { complete = resolve; });
  }, {addressId: 12});
  page.onShow();
  assert.equal(calls[0].data.addressId, 12);
  calls.length = 0;
  const submitted = page.submitOrder();
  page.submitOrder();
  assert.equal(calls.length, 1);
  complete({code: 400, msg: '测试失败'});
  await submitted;
  assert.equal(page.data.submitting, false);
});

/** 使用当前Vue编译器读取组件, 导入依赖替换为占位对象, 不依赖旧Babel工具链. */
function loadVue(relative) {
  const compiler = require(path.join(root, 'platform-admin-ui/node_modules/@vue/compiler-sfc'));
  const source = compiler.parse(fs.readFileSync(path.join(root, relative), 'utf8')).descriptor.script.content;
  let transformed = compiler.rewriteDefault(source, '_component');
  const imports = compiler.babelParse(transformed, {sourceType: 'module'}).program.body
      .filter(node => node.type === 'ImportDeclaration');
  for (const node of imports.reverse()) {
    transformed = transformed.slice(0, node.start) + node.specifiers.map(item =>
      'const ' + item.local.name + ' = {};').join('\n') + transformed.slice(node.end);
  }
  transformed += '\nmodule.exports.default = _component;';
  const module = {exports: {}};
  vm.runInNewContext(transformed, {module, exports: module.exports,
    require: () => ({}), FileReader: class {
      /** 模拟浏览器读取本地JSON, 不写入实际文件. */
      readAsText() { this.onload({target: {result: JSON.stringify({goods: {name: '测试商品', categoryId: 1}})}}); }
    }});
  return module.exports.default;
}

/** 合法商品JSON必须进入确认和保存, 不再报未定义变量. */
test('合法商品JSON可以进入确认与保存', async () => {
  const component = loadVue('platform-admin-ui/src/views/modules/mall/goods.vue');
  let saves = 0;
  const context = {
    $confirm: () => Promise.resolve(),
    $http: () => { saves++; return Promise.resolve({data: {code: 0, data: 1}}); },
    $message: {success: () => {}, error: message => { throw new Error(message); }},
    getDataList: () => {},
  };
  component.methods.importGoodsJson.call(context, {raw: {}});
  await new Promise(setImmediate);
  assert.equal(saves, 1);
});

/** 创建可控制返回顺序的定制页, 不接入真实微信接口. */
function designerFixture() {
  let page;
  const requests = [];
  load('wx-mall/pages/designer/designer.js', {
    '../../utils/util.js': {request: (url, data) => new Promise((resolve, reject) => {
      requests.push({url, data, resolve, reject});
    })},
    '../../config/api.js': {CustomizationConfig: '/config', CustomizationQuote: '/quote',
      AiRecommendation: '/ai/recommend', AiRecommendationStatus: '/ai/status'},
    '../../services/ai-recommendation.js': {
      availability: () => '',
      recommend: data => new Promise((resolve, reject) => {
        requests.push({url: '/ai/recommend', data, resolve, reject});
      }),
    },
  }, {Page: definition => { page = definition; }, wx: {}, setTimeout, clearTimeout});
  page.setData = values => Object.assign(page.data, values);
  return {page, requests};
}

/** 未同意不发送身体数据, 请求失败仍可手动选择材料. */
test('AI推荐必须主动同意, 失败正确收尾且不覆盖设计', async () => {
  const {page, requests} = designerFixture();
  page.data.selectedOptionIds = [7];
  await page.requestAiRecommendation();
  assert.equal(requests.length, 0);
  Object.assign(page.data, {aiConsent: true, aiHeight: '165', aiWeight: '55', goodsId: 10});
  const pending = page.requestAiRecommendation();
  assert.equal(page.data.aiLoading, true);
  assert.equal(requests[0].data.neckCm, null);
  assert.equal(requests[0].data.consent, true);
  requests[0].reject(new Error('模型超时'));
  await pending;
  assert.equal(page.data.aiLoading, false);
  assert.equal(page.data.aiError, '模型超时');
  assert.deepEqual(page.data.selectedOptionIds, [7]);
});

/** 输入或商品变更后迟到的AI结果不得重新出现. */
test('AI参数变更废弃在途结果', async () => {
  const {page, requests} = designerFixture();
  Object.assign(page.data, {aiConsent: true, aiHeight: '165', aiWeight: '55', goodsId: 10});
  const pending = page.requestAiRecommendation();
  page.changeAiInput({currentTarget: {dataset: {field: 'aiHeight'}}, detail: {value: '170'}});
  requests[0].resolve({code: 0, data: {goodsId: 10, lengthCm: 45}});
  await pending;
  assert.equal(page.data.aiResult, null);
  assert.equal(page.data.aiHeight, '170');
});

/** 采用AI材料必须重新报价, 不信任结果中附带的旧价格或跨商品材料. */
test('采用AI搭配后重新报价并拒绝跨商品结果', async () => {
  const {page, requests} = designerFixture();
  Object.assign(page.data, {goodsId: 10, quote: {totalPrice: 500}, componentGroups: [
    {type: 'pearl', required: true, options: [{id: 1, name: '澳白', assetUrl: '/static/p.png'}]}
  ], aiResult: {goodsId: 11, optionIds: [1]}});
  page.applyAiRecommendation();
  assert.equal(requests.length, 0);
  page.data.aiResult = {goodsId: 10, optionIds: [1], quote: {totalPrice: 1}};
  const pending = page.applyAiRecommendation();
  assert.equal(requests[0].url, '/quote');
  assert.equal(page.data.quote.totalPrice, undefined);
  assert.deepEqual(Array.from(page.data.selectedOptionIds), [1]);
  requests[0].resolve({code: 0, data: {totalPrice: 99}});
  await pending;
  assert.equal(page.data.quote.totalPrice, 99);
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
  requests[1].resolve({code: 0, data: {
    product: {goodsId: 2, requiredComponentTypes: 'length', previewWidth: 500, previewHeight: 750},
    primaryProductId: 22,
    options: [{id: 2, componentType: 'length', name: '链长', assetUrl: '/merchant-layer.png', zIndex: 4}],
  }});
  await second;
  requests[0].resolve({code: 0, data: {
    product: {goodsId: 1}, primaryProductId: 11, options: [],
  }});
  await first;
  assert.equal(page.data.productConfig.goodsId, 2);
  assert.equal(page.data.primaryProductId, 22);
  assert.equal(page.data.componentGroups[0].required, true);
  assert.equal(page.data.previewLayers.layers[0].url, '/merchant-layer.png');
  assert.equal(page.data.previewLayers.ratio, 150);
});

/** 只有开发环境可以覆盖API, 正式版本忽略本机残留覆盖值. */
test('小程序正式环境强制使用配置的HTTPS域名', () => {
  const environment = {
    releaseBaseUrl: 'https://merchant.example/platform-framework-api/app/',
  };
  const wx = {
    getStorageSync: () => 'http://localhost:9999/app/',
    getAccountInfoSync: () => ({miniProgram: {envVersion: 'release'}}),
  };
  const api = load('wx-mall/config/api.js', {'./environment.js': environment}, {wx});
  assert.equal(api.ApiOrigin, 'https://merchant.example');
  environment.releaseBaseUrl = 'http://localhost:8888/app/';
  assert.throws(() => load('wx-mall/config/api.js', {'./environment.js': environment}, {wx}), /HTTPS/);
});

/** 上传图片补齐API来源, 包内静态资源和普通文案保持原内容. */
test('上传图片同源地址可以在小程序显示', () => {
  const util = load('wx-mall/utils/util.js', {
    '../config/api.js': {ApiOrigin: 'https://merchant.example'},
  }, {wx: {}});
  const data = {data: {headImgUrl: '/upload/avatar.png', listPicUrl: '/static/images/logo.png',
    name: '/upload/这是商品名称'}};
  util.normalizeAssetUrls(data);
  assert.equal(data.data.headImgUrl, 'https://merchant.example/upload/avatar.png');
  assert.equal(data.data.listPicUrl, '/static/images/logo.png');
  assert.equal(data.data.name, '/upload/这是商品名称');
});
