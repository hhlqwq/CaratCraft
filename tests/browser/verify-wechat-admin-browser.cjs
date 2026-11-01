/* 使用真实生产包验证公众号组件, 仅提供隔离读取数据, 不访问微信或业务服务. */
const assert = require('node:assert/strict');
const {start} = require('./serve-admin-ui-fixture.cjs');

/** 按真实关闭按钮收起当前弹窗, 避免直接导航遗留模态遮罩. */
async function closeDialogs(page, confirmUnsaved = false) {
    while (await page.locator('[role="dialog"]:visible').count()) {
        const dialog = page.locator('[role="dialog"]:visible').last();
        const element = await dialog.elementHandle();
        const draft = confirmUnsaved && (await dialog.getAttribute('aria-label')) === '新增';
        await dialog.locator('.el-dialog__headerbtn').first().click();
        if (draft) {
            await page.getByRole('button', {name: '离开', exact: true}).click();
        }
        await page.waitForFunction(
            (node) => !node.isConnected || node.getClientRects().length === 0,
            element
        );
    }
}

/** 覆盖组件注册、素材分页/切换、菜单选择器、消息转义和草稿弹窗. */
async function main() {
    const packages = process.env.CARATCRAFT_NODE_MODULES;
    const {chromium} = require(
        require.resolve('playwright', {paths: packages ? [packages] : [process.cwd()]})
    );
    const server = await start(0);
    const browser = await chromium.launch({channel: 'msedge', headless: true});
    const errors = [];
    const image = '/static/img/avatar.png';
    const unsafeText =
        '<img data-untrusted-message src="/missing-image" onerror="window.messageExecuted=true">隔离消息';
    const article = {title: '隔离图文', author: '隔离作者', content: '正文', thumbUrl: image};
    const filePages = [];
    const routes = {
        '/manage/wxMsg/list': {
            records: [
                {
                    openid: 'fixture-user',
                    msgType: 'text',
                    inOut: false,
                    detail: {content: unsafeText},
                    createTime: new Date().toISOString(),
                },
            ],
            total: 1,
        },
        '/manage/wxUser/listByIds': [
            {mpOpenId: 'fixture-user', nickname: '隔离消息用户', headImgUrl: image},
        ],
        '/manage/wxAssets/materialCount': {
            imageCount: 40,
            voiceCount: 0,
            videoCount: 0,
            newsCount: 1,
        },
        '/manage/wxAssets/materialNewsBatchGet': {
            items: [
                {
                    mediaId: 'fixture-news',
                    content: {articles: [article]},
                    updateTime: '2026-10-30 10:00:00',
                },
            ],
            totalCount: 1,
        },
        '/manage/wxMenu/getMenu': {
            menu: {buttons: [{name: '隔离菜单', type: 'media_id', mediaId: '', subButtons: []}]},
        },
        '/manage/msgReplyRule/list': {records: [], total: 0},
        '/manage/draft/listDraft': {
            items: [
                {
                    mediaId: 'fixture-draft',
                    content: {newsItem: [article]},
                    updateTime: '2026-10-30 10:00:00',
                },
            ],
            totalCount: 1,
        },
        '/manage/draft/getDraft/fixture-draft': {newsItem: [article]},
    };
    try {
        const page = await browser.newPage({viewport: {width: 1280, height: 900}});
        page.on('pageerror', (error) => errors.push(error.message));
        await page.route('**/fixture-api/manage/**', async (route) => {
            const url = new URL(route.request().url());
            const endpoint = url.pathname.replace('/fixture-api', '');
            assert.ok(
                route.request().method() === 'GET' ||
                    (endpoint === '/manage/wxUser/listByIds' &&
                        route.request().method() === 'POST'),
                '浏览器回归拒绝业务写入: ' + endpoint
            );
            let data = routes[endpoint];
            if (endpoint === '/manage/wxAssets/materialFileBatchGet') {
                const index = Number(url.searchParams.get('page'));
                filePages.push(index);
                data = {
                    items: [
                        {
                            mediaId: 'fixture-image-' + index,
                            name: '隔离图片第' + index + '页',
                            url: image,
                            updateTime: '2026-10-30 10:00:00',
                        },
                    ],
                    totalCount: 40,
                };
            }
            assert.notEqual(data, undefined, '缺少明确读取夹具: ' + endpoint);
            await route.fulfill({json: {code: 0, data}});
        });
        const base = 'http://127.0.0.1:' + server.address().port;
        await page.goto(base + '/#/wx-wx-msg');
        await page.locator('.panel-content').getByText(unsafeText, {exact: true}).waitFor();
        assert.equal(await page.locator('[data-untrusted-message]').count(), 0);
        assert.equal(await page.evaluate(() => globalThis.messageExecuted), undefined);
        await page.getByText('回复', {exact: true}).click();
        await page.getByRole('dialog', {name: '消息回复'}).waitFor();
        await page.getByRole('button', {name: '取消', exact: true}).click();
        await page.goto(base + '/#/wx-wx-assets');
        await page.getByText('隔离图片第1页', {exact: true}).waitFor();
        assert.equal(
            (await page.locator('.el-pagination .number.is-active').textContent()).trim(),
            '1'
        );
        await page.locator('.el-pagination .btn-next').click();
        await page.getByText('隔离图片第2页', {exact: true}).waitFor();
        assert.deepEqual(filePages.slice(0, 2), [1, 2]);
        await page.getByRole('tab', {name: /图文素材/}).click();
        await page.getByText('隔离图文', {exact: true}).waitFor();
        await page.getByRole('tab', {name: /图片素材/}).click();
        await page.getByText('隔离图片第2页', {exact: true}).waitFor();
        await page.goto(base + '/#/wx-wx-menu');
        await page.locator('.menu-item-title').getByText('隔离菜单', {exact: true}).click();
        await page.getByText('从图片库中选择', {exact: true}).click();
        await page.getByRole('dialog').getByText('隔离图片第1页', {exact: true}).waitFor();
        await closeDialogs(page);
        await page.goto(base + '/#/wx-msg-reply-rule');
        await page.getByRole('button', {name: '新增', exact: true}).click();
        await page
            .getByRole('dialog', {name: '新增', exact: true})
            .getByPlaceholder('规则名称')
            .waitFor();
        await closeDialogs(page);
        await page.goto(base + '/#/wx-wx-draft');
        await page.getByText('隔离图文', {exact: true}).waitFor();
        await page.getByRole('button', {name: '新增', exact: true}).click();
        await page.getByRole('dialog', {name: '新增', exact: true}).waitFor();
        await page
            .getByRole('dialog', {name: '新增', exact: true})
            .getByText('从素材库中选择', {exact: true})
            .click();
        await page.getByRole('dialog').getByText('隔离图片第1页', {exact: true}).waitFor();
        await closeDialogs(page, true);
        await page.goto(base + '/#/wx-wx-draft');
        await page.getByRole('button', {name: '修改', exact: true}).click();
        await page.getByRole('dialog', {name: '修改', exact: true}).waitFor();
        assert.deepEqual(errors, []);
        console.log(
            '公众号管理端浏览器验证通过: 消息转义和回复、图片分页、图文切换、菜单素材选择、规则及草稿新增/修改; 使用隔离读取夹具, 不代表微信平台验收.'
        );
    } finally {
        await browser.close();
        await new Promise((resolve) => server.close(resolve));
    }
}

main().catch((error) => {
    console.error(error);
    process.exitCode = 1;
});
