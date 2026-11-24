/* 使用生产包与本地夹具验证工作室导航, 不访问业务环境. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const {start} = require('./serve-admin-ui-fixture.cjs');

/** 为非超管浏览器会话配置精确的后台返回菜单与动作权限. */
async function installIdentity(page, menus, permissions) {
    await page.route('**/fixture-api/sys/menu/nav', (route) =>
        route.fulfill({
            json: {
                code: 0,
                data: {
                    menuList: menus.map(([url, name], index) => ({menuId: index + 10, url, name})),
                    permissions,
                    dictList: [{code: 'SEX', value: '0', name: '保密'}],
                },
            },
        })
    );
    await page.route('**/fixture-api/sys/user/info', (route) =>
        route.fulfill({
            json: {code: 0, data: {userId: 'studio-fixture', userName: '隔离岗位员工'}},
        })
    );
}

/** 检查实际路由保护、无标签导航、列表参数恢复及免选机构开户页. */
async function main() {
    const {chromium} = require(
        require.resolve('playwright', {
            paths: [process.env.CARATCRAFT_NODE_MODULES || process.cwd()],
        })
    );
    const output = path.resolve('.tools/copyright/studio-ui');
    fs.mkdirSync(output, {recursive: true});
    const server = await start(0);
    const origin = 'http://127.0.0.1:' + server.address().port;
    let browser;
    try {
        browser = await chromium.launch({channel: 'msedge', headless: true});
        const page = await browser.newPage({viewport: {width: 1440, height: 960}});
        const errors = [];
        page.on('pageerror', (error) => errors.push(error.message));
        await installIdentity(
            page,
            [
                ['mall/order', '订单管理'],
                ['custom/work-order', '生产工单'],
                ['gen/generator', '代码生成'],
                ['mall/coupon', '优惠券管理'],
            ],
            ['mall:order:list', 'mall:order:info', 'mall:order:ship', 'custom:work-order:list']
        );
        const order = {
            id: 77,
            orderSn: 'CC-STUDIO-77',
            orderStatus: 201,
            payStatus: 2,
            shippingStatus: 0,
            canReadDetails: true,
            canShip: true,
            actualPrice: '8.00',
        };
        await page.route('**/fixture-api/mall/order/list*', (route) =>
            route.fulfill({
                json: {code: 0, data: {records: [order], total: 40}},
            })
        );
        await page.route('**/fixture-api/mall/order/info/77', (route) =>
            route.fulfill({json: {code: 0, data: order}})
        );
        await page.route('**/fixture-api/mall/order/goods/77', (route) =>
            route.fulfill({json: {code: 0, data: []}})
        );
        await page.route('**/fixture-api/mall/dashboard/summary', (route) =>
            route.fulfill({
                json: {code: 0, data: {toShip: 3, workMaking: 2}},
            })
        );
        await page.goto(origin + '/#/home');
        const workbench = page.locator('.merchant-home');
        await workbench.getByRole('button', {name: '待发货 3'}).waitFor();
        assert.equal(await workbench.getByRole('button', {name: /退款|员工/}).count(), 0);
        const nav = page.locator('.site-sidebar');
        const orders = nav.getByText('订单与制作', {exact: true});
        await orders.click();
        assert.equal(
            await nav.getByText(/系统设置|退款管理|代码生成|优惠券管理|商品与材料/).count(),
            0
        );
        await nav.getByText('生产工单', {exact: true}).click();
        await page.waitForURL('**/#/custom-work-order');
        await nav.getByText('订单列表', {exact: true}).click();
        await page.getByPlaceholder('订单编号').fill('CC-STUDIO');
        await page.getByRole('button', {name: '查询', exact: true}).click();
        await page.waitForURL(/orderSn=CC-STUDIO/);
        await page.locator('.el-pager').getByText('2', {exact: true}).click();
        await page.waitForURL(/page=2/);
        await page.getByRole('button', {name: '查看', exact: true}).click();
        await page.waitForURL(/mall-order\/77/);
        await page.getByText('订单详情', {exact: true}).waitFor();
        await page.reload();
        await page.getByText('CC-STUDIO-77', {exact: true}).first().waitFor();
        await page.screenshot({path: path.join(output, 'after.png')});
        await page.getByRole('button', {name: '返回列表'}).click();
        await page.locator('.el-pager .is-active').filter({hasText: '2'}).waitFor();
        assert.equal(await page.getByPlaceholder('订单编号').inputValue(), 'CC-STUDIO');
        assert.equal(await page.locator('.site-content__wrapper .el-tabs__nav').count(), 0);
        await page.goBack();
        await page.waitForURL(/mall-order\/77/);
        await page.goForward();
        await page.waitForURL(/mall-order\?[^#]*page=2/);
        const restricted = await browser.newPage();
        await installIdentity(restricted, [['mall/order', '订单管理']], ['mall:order:list']);
        await restricted.goto(origin + '/#/mall-refund');
        await restricted.waitForURL('**/#/404');
        assert.deepEqual(errors, []);

        const moderator = await browser.newPage();
        await installIdentity(moderator, [['mall/comment', '评论管理']], ['mall:comment:list']);
        await moderator.goto(origin + '/#/home');
        const communityNav = moderator.locator('.site-sidebar');
        await communityNav.getByText('社区', {exact: true}).click();
        await communityNav.getByText('商品评价', {exact: true}).click();
        await moderator.waitForURL('**/#/mall-comment');

        const manager = await browser.newPage({viewport: {width: 1440, height: 960}});
        await installIdentity(
            manager,
            [
                ['sys/user', '管理员列表'],
                ['sys/role', '角色管理'],
                ['sys/log', '系统日志'],
            ],
            ['sys:user:list', 'sys:user:save', 'sys:role:select']
        );
        await manager.route('**/fixture-api/sys/role/select', (route) =>
            route.fulfill({json: {code: 0, data: []}})
        );
        await manager.route('**/fixture-api/sys/user/list*', (route) =>
            route.fulfill({json: {code: 0, data: {records: [], total: 0}}})
        );
        await manager.goto(origin + '/#/sys-user');
        await manager.getByRole('button', {name: '新增', exact: true}).click();
        await manager.getByRole('heading', {name: '新建员工'}).waitFor();
        assert.equal(await manager.getByText('所属机构', {exact: true}).count(), 0);
        await manager.screenshot({path: path.join(output, 'staff-after.png')});
        await manager.getByRole('button', {name: '返回列表'}).click();
        await manager.waitForURL('**/#/sys-user');
        await manager.getByRole('button', {name: '新增', exact: true}).click();
        await manager.getByPlaceholder('登录帐号').fill('changed');
        await manager.getByRole('button', {name: '返回列表'}).click();
        await manager.getByText('员工资料有未保存的修改，是否离开？').waitFor();
        await manager.getByRole('button', {name: '继续编辑'}).click();
        assert.equal(await manager.getByPlaceholder('登录帐号').inputValue(), 'changed');
        console.log('单工作室分组、权限直达、单页切换、列表恢复及编辑保护通过.');
    } finally {
        await browser?.close();
        await new Promise((resolve) => server.close(resolve));
    }
}

main().catch((error) => {
    console.error(error);
    process.exitCode = 1;
});
