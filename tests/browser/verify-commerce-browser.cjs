/* 用实际生产包验证库存及售后失败恢复, 所有接口均为隔离数据. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const {start} = require('./serve-admin-ui-fixture.cjs');

/** 运行真实Edge页面, 只拦截本次测试服务接口并在结束时关闭自建资源. */
async function main() {
    const packages = process.env.CARATCRAFT_NODE_MODULES;
    const {chromium} = require(
        require.resolve('playwright', {paths: packages ? [packages] : [process.cwd()]})
    );
    const output = path.resolve('.tools/restructure/commerce-browser');
    fs.mkdirSync(output, {recursive: true});
    const server = await start(0);
    const browser = await chromium.launch({channel: 'msedge', headless: true});
    const errors = [];
    const origin = 'http://127.0.0.1:' + server.address().port;
    let reads = 0;
    let writes = 0;
    try {
        const page = await browser.newPage({viewport: {width: 1280, height: 900}});
        page.on('pageerror', (error) => errors.push(error.message));
        await page.route('**/fixture-api/custom/component-stock/**', async (route) => {
            const update = new URL(route.request().url()).pathname.endsWith('/update');
            if (update) {
                writes++;
                await route.fulfill({json: {code: 409, msg: '隔离库存冲突, 请重新读取'}});
                return;
            }
            reads++;
            await route.fulfill({
                json: {
                    code: 0,
                    data: {
                        records: [
                            {
                                id: 1,
                                optionId: 1,
                                goodsId: 900001,
                                componentType: 'pearl',
                                optionCode: 'WHITE',
                                name: '隔离白珠',
                                stock: 8,
                                priceDelta: 0,
                            },
                        ],
                        total: 1,
                    },
                },
            });
        });
        await page.goto(origin + '/#/custom-component-stock');
        await page.getByRole('button', {name: '修改库存', exact: true}).click();
        const stock = page.getByRole('dialog', {name: '修改组件库存'});
        await stock.getByPlaceholder('补货、盘点等原因').fill('隔离盘点理由');
        await stock.getByRole('button', {name: '确定', exact: true}).click();
        await stock.getByText('隔离库存冲突, 请重新读取', {exact: false}).waitFor();
        assert.equal(writes, 1);
        assert.equal(await stock.getByPlaceholder('补货、盘点等原因').inputValue(), '隔离盘点理由');
        assert.equal(
            await stock.getByRole('button', {name: '确定', exact: true}).isDisabled(),
            true
        );
        const previous = reads;
        await stock.getByRole('button', {name: '重新读取库存'}).click();
        await stock.getByRole('button', {name: '确定', exact: true}).waitFor();
        await page.waitForFunction(
            () => !document.querySelector('.el-dialog__footer .el-button--primary')?.disabled
        );
        assert.ok(reads > previous);
        await page.screenshot({
            path: path.join(output, 'stock-conflict-recovery.png'),
            fullPage: true,
        });
        await stock.getByRole('button', {name: '取消', exact: true}).click();
        let refundReads = 0;
        await page.route('**/fixture-api/mall/order/list*', (route) =>
            route.fulfill({json: {code: 0, data: {records: [], total: 0}}})
        );
        await page.route('**/fixture-api/mall/shipping/queryAll*', (route) =>
            route.fulfill({json: {code: 0, data: []}})
        );
        await page.route('**/fixture-api/mall/refund/list*', (route) => {
            refundReads++;
            return route.fulfill({
                json:
                    refundReads === 1
                        ? {code: 503, msg: '隔离售后读取失败'}
                        : {
                              code: 0,
                              data: {
                                  records: [
                                      {
                                          id: 1,
                                          order_id: 1,
                                          order_sn: 'ISOLATED-REFUND',
                                          amount_fen: 1000,
                                          reason: '隔离申请',
                                          status: 'PROCESSING',
                                      },
                                  ],
                                  total: 1,
                              },
                          },
            });
        });
        await page.goto(origin + '/#/mall-order');
        await page.getByRole('button', {name: '退款售后', exact: true}).click();
        const refunds = page.getByRole('dialog', {name: '退款与售后审核'});
        await refunds.getByText('隔离售后读取失败', {exact: false}).waitFor();
        assert.equal(
            await refunds.getByRole('button', {name: '对账并更新状态'}).isDisabled(),
            true
        );
        await refunds.getByRole('button', {name: '重新读取售后列表'}).click();
        await refunds.getByText('ISOLATED-REFUND', {exact: true}).waitFor();
        assert.equal(await refunds.getByRole('button', {name: '通过', exact: true}).count(), 0);
        await page.setViewportSize({width: 624, height: 900});
        await page.screenshot({path: path.join(output, 'refund-retry-narrow.png'), fullPage: true});
        assert.deepEqual(errors, []);
        fs.writeFileSync(
            path.join(output, 'result.json'),
            JSON.stringify(
                {
                    passed: true,
                    scenarios: [
                        'stock_conflict_preserves_reason_and_requires_reload',
                        'refund_read_retry_and_processing_has_no_approve',
                    ],
                    realBackend: false,
                    productionBundle: true,
                },
                null,
                4
            )
        );
        console.log('库存冲突恢复、售后读取重试及处理中状态真实浏览器检查通过, 无页面运行异常.');
    } finally {
        await browser.close();
        await new Promise((resolve) => server.close(resolve));
    }
}

main().catch((error) => {
    console.error(error);
    process.exitCode = 1;
});
