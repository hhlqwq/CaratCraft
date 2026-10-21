/* 验证实测输入、草稿保存和结算拦截, 不调用业务服务或AI. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');

/** 构造设计页及可控报价队列, 服务端响应由用例指定. */
function fixture() {
  let page;
  const calls = [];
  const sandbox = {Page: value => { page = value; }, wx: {showToast() {}, setStorageSync() {}, getStorageSync() { return ''; }},
    require: name => {
      if (name.includes('/utils/')) return {request: (url, data) => new Promise(resolve => calls.push({url, data, resolve}))};
      if (name.includes('/config/')) return {CustomizationQuote: '/quote', CustomizationSave: '/save'};
      return {};
    }};
  vm.runInNewContext(fs.readFileSync(path.resolve(__dirname,
      '../vendor/platform-wxshop/wx-mall/pages/designer/designer.js'), 'utf8'), sandbox);
  page.setData = values => Object.assign(page.data, values);
  Object.assign(page.data, {goodsId: 10, neckCircumferenceMm: '350', targetLengthMm: '450', arrangementOptionIds: [1, 1]});
  return {page, calls};
}

/** 报价和保存均传递两个独立毫米字段, 未就绪设计仍可保存草稿. */
test('实测颈围和目标长度随报价及草稿保存独立提交', async () => {
  const {page, calls} = fixture();
  const quoting = page.refreshQuote();
  assert.equal(calls[0].data.neckCircumferenceMm, 350);
  assert.equal(calls[0].data.targetLengthMm, 450);
  calls[0].resolve({code: 0, data: {totalPrice: 100, checkoutReady: false}});
  await quoting;
  const saving = page.persistDesign(false);
  assert.equal(calls[1].data.neckCircumferenceMm, 350);
  assert.equal(calls[1].data.targetLengthMm, 450);
  calls[1].resolve({code: 0, data: {snapshotId: 7, version: 0}});
  assert.equal(await saving, 7);
});

/** 缺少或未通过服务端就绪标记时禁止进入结算, 不先创建买单上下文. */
test('长度不完整或就绪标记缺失时禁止下一步', async () => {
  for (const ready of [false, undefined]) {
    const {page, calls} = fixture();
    page.data.quote = {totalPrice: 100, checkoutReady: ready};
    await assert.rejects(page.persistDesign(true), /尚未校验通过/);
    assert.equal(calls.length, 0);
  }
});

/** 实测输入变化立即清除旧价格, 旧响应不能覆盖新尺寸的报价. */
test('尺寸变化隔离迟到报价并保留空值含义', async () => {
  const {page, calls} = fixture();
  const first = page.refreshQuote();
  page.changeSizing({currentTarget: {dataset: {field: 'neckCircumferenceMm'}}, detail: {value: ''}});
  assert.equal(calls[1].data.neckCircumferenceMm, null);
  calls[0].resolve({code: 0, data: {totalPrice: 999, checkoutReady: true}});
  await first;
  assert.equal(page.data.quote.totalPrice, undefined);
  calls[1].resolve({code: 0, data: {totalPrice: 100, checkoutReady: false}});
  await new Promise(setImmediate);
  assert.equal(page.data.quote.totalPrice, 100);
  assert.equal(page.data.quote.checkoutReady, false);
});
