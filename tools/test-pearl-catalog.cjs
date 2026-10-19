/* 珍珠商城组合筛选与商品事实展示回归, 不访问网络和真实商家数据. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');
const root = path.resolve(__dirname, '../vendor/platform-wxshop/wx-mall');

/** 使用可控请求队列装载页面, 使新旧筛选请求能够按相反顺序完成. */
function fixture() {
  const calls = [];
  let page;
  const util = {
    request(url, params) {
      return new Promise((resolve, reject) => calls.push({params, resolve, reject}));
    },
  };
  vm.runInNewContext(fs.readFileSync(path.join(root, 'pages/catalog/catalog.js'), 'utf8'), {
    require: (name) => (name.endsWith('util.js') ? util : {PearlCatalog: '/test'}),
    Page: (definition) => {
      page = definition;
    },
    wx: {stopPullDownRefresh() {}},
  });
  page.data = JSON.parse(JSON.stringify(page.data));
  page.setData = (update) => Object.assign(page.data, update);
  return {page, calls};
}

/** 当前筛选的迟到响应只能更新自己的代次, 切换品类时保留来源维度. */
test('两个筛选维度组合且隔离迟到响应', async () => {
  const {page, calls} = fixture();
  page.onLoad({source: 'FRESHWATER', type: 'NECKLACE'});
  assert.equal(calls[0].params.source, 'FRESHWATER');
  page.selectFilter({currentTarget: {dataset: {field: 'type', id: 'RING'}}});
  assert.equal(calls[1].params.source, 'FRESHWATER');
  assert.equal(calls[1].params.type, 'RING');
  calls[1].resolve({code: 0, data: {records: [{id: 2}], total: 1}});
  await new Promise(setImmediate);
  calls[0].resolve({code: 0, data: {records: [{id: 1}], total: 1}});
  await new Promise(setImmediate);
  assert.equal(page.data.goods[0].id, 2);
  assert.equal(page.data.hasMore, false);
});

/** 网络失败不跳页, 用户重试仍请求相同页码且可以恢复列表. */
test('商品分页失败保留当前页以供重试', async () => {
  const {page, calls} = fixture();
  page.onLoad({});
  calls[0].reject(new Error('offline'));
  await new Promise(setImmediate);
  assert.equal(page.data.page, 1);
  const retry = page.loadMore();
  assert.equal(calls[1].params.page, 1);
  calls[1].resolve({code: 0, data: {records: [{id: 3}], total: 2}});
  await retry;
  assert.equal(page.data.page, 2);
  assert.equal(page.data.hasMore, true);
  assert.equal(page.data.error, '');
});

/** 页面卸载即使请求成功也不能写入已销毁的页面. */
test('商品页面卸载隔离未完成请求', async () => {
  const {page, calls} = fixture();
  page.onLoad({});
  page.onUnload();
  calls[0].resolve({code: 0, data: {records: [{id: 4}], total: 1}});
  await new Promise(setImmediate);
  assert.equal(page.data.goods.length, 0);
});

/** 只显示已填写的事实, 未知商品参数不被演示数据或默认尺寸补齐. */
test('商品参数保留未知状态并准确展示毫米区间', () => {
  const module = {exports: {}};
  vm.runInNewContext(fs.readFileSync(path.join(root, 'services/pearl-catalog.js'), 'utf8'), {
    module,
  });
  const parameters = module.exports.parameters;
  assert.equal(parameters({}).length, 0);
  const rows = parameters({
    pearlSource: 'SEAWATER',
    jewelryType: 'NECKLACE',
    pearlDiameterMin: '8.100',
    pearlDiameterMax: '9.200',
  });
  assert.equal(rows[0].value, '海水珍珠');
  assert.equal(rows[rows.length - 1].value, '8.1–9.2mm');
});
