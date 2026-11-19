const {test} = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const {spawnSync} = require('node:child_process');
const pages = JSON.parse(fs.readFileSync('apps/miniapp/app.json', 'utf8')).pages;
const helper = path.join(__dirname, 'helpers/miniapp-offline-probe.cjs');
const repaired = new Set([
    'pages/index/index',
    'pages/newGoods/newGoods',
    'pages/hotGoods/hotGoods',
    'pages/ucenter/address/address',
    'pages/shopping/address/address',
    'pages/ucenter/coupon/coupon',
    'pages/ucenter/collect/collect',
    'pages/brandDetail/brandDetail',
    'pages/search/search',
    'pages/category/category',
    'pages/topicDetail/topicDetail',
    'pages/shopping/selCoupon/selCoupon',
    'pages/ucenter/help/help',
    'pages/ucenter/helpInfo/helpInfo',
]);

for (const page of pages) {
    /** 每页在独立进程模拟初始加载断网, 验证无未处理异常和已整改读取提示. */
    test('小程序初始加载断网: ' + page, () => {
        const result = spawnSync(process.execPath, [helper, page], {
            encoding: 'utf8',
            timeout: 10000,
        });
        assert.equal(result.status, 0, result.stderr || result.error?.message);
        const report = JSON.parse(result.stdout.trim());
        assert.deepEqual(report.errors, []);
        if (repaired.has(page)) {
            assert.ok(report.requests > 0, '必须实际触发只读请求');
            assert.ok(report.readError, '读取失败必须提供可见提示');
        }
    });
}
