/* 在本地构建产物上验证后台交互, 所有业务请求由浏览器替身响应. */
const assert = require('node:assert/strict');

const origin = 'http://127.0.0.1:8888';
const catalog = [
    ['goods', null, '商品管理', 'mall/goods', 'mall:goods:list,mall:goods:info'],
    ['add', 'goods', '新增', null, 'mall:goods:save'],
    ['edit', 'goods', '修改', null, 'mall:goods:update'],
    ['delete', 'goods', '删除', null, 'mall:goods:delete'],
    ['order', null, '订单管理', 'mall/order', 'mall:order:list'],
    ['old', 'order', '修改', null, 'mall:order:update'],
    ['refund', null, '退款管理', 'mall/refund', 'mall:refund:list'],
    ['review', 'refund', '审核退款', null, 'mall:refund:review'],
    ['execute', 'refund', '执行退款', null, 'mall:refund:execute'],
    ['retry', 'refund', '重试退款', null, 'mall:refund:retry'],
    ['reconcile', 'refund', '退款对账写回', null, 'mall:refund:reconcile'],
    ['community', null, '社区管理', 'community/posts', 'community:post:list'],
    ['coupon', null, '优惠券管理', 'mall/coupon', 'mall:coupon:list'],
    ['platform', null, '商家管理员', 'platform/merchant-accounts', 'platform:merchant:manage'],
].map(([menuId, parentId, name, url, perms]) => ({menuId, parentId, name, url, perms}));
const role = {roleId: 'fixture', roleName: '隔离岗位', remark: '', menuIdList: ['edit', 'old']};

/** 为当前测试账号隔离所有后台请求, 保存仅记录参数. */
async function fixture(browser, identity = '') {
    const context = await browser.newContext({viewport: {width: 1280, height: 850}});
    if (identity) {
        await context.addCookies([{name: 'token', value: 'fixture-' + identity, url: origin}]);
    }
    const saves = [];
    const data = {
        '/sys/menu/nav': {
            menuList: [{menuId: 'role-menu', url: 'sys/role', name: '岗位与权限'}],
            permissions: ['sys:role:list', 'sys:role:save', 'sys:role:update', 'sys:role:info', 'sys:menu:list'],
        },
        '/sys/menu/list': catalog,
        '/sys/role/list': {records: [role], total: 1},
        '/sys/role/info/fixture': role,
        '/sys/user/info': {userId: 'manager', userName: '隔离商家管理员', accountType: 'MERCHANT_ADMIN'},
    };
    await context.route(origin + '/platform-framework/**', async (route) => {
        const path = new URL(route.request().url()).pathname.replace('/platform-framework', '');
        if (path === '/captcha.jpg') {
            await route.fulfill({status: 204, body: ''});
            return;
        }
        if (path === '/sys/role/save' || path === '/sys/role/update') {
            saves.push(route.request().postDataJSON());
        }
        await route.fulfill({
            json: {
                code: path === '/sys/menu/nav' && identity === 'temporary' ? 428 : 0,
                data: data[path] || {},
            },
        });
    });
    return {context, page: await context.newPage(), saves};
}

/** 验证密码默认隐藏, 开关保持原有输入. */
async function checkPasswords(container, count) {
    await container.locator('input[type=password]').first().waitFor();
    const fields = await container.locator('input[type=password]').all();
    assert.equal(fields.length, count);
    for (const [index, field] of fields.entries()) {
        await field.fill('FixturePassword' + index);
    }
    const toggles = await container.locator('.el-input__password').all();
    assert.equal(toggles.length, count);
    for (const [index, toggle] of toggles.entries()) {
        const input = toggle.locator('xpath=ancestor::div[contains(@class,"el-input__wrapper")]').locator('input');
        await toggle.click();
        assert.equal(await input.getAttribute('type'), 'text');
        assert.equal(await input.inputValue(), 'FixturePassword' + index);
        await toggle.click();
        assert.equal(await input.getAttribute('type'), 'password');
    }
}

/** 验证中文分组、退款单独勾选和既有授权原样提交. */
async function checkRoles(page, saves) {
    await page.goto(origin + '/#/sys-role');
    await page.getByRole('button', {name: '新增', exact: true}).click();
    const create = page.getByRole('dialog', {name: '新增岗位'});
    await create.getByText('已选权限（0 项）').waitFor();
    const groups = create.locator('.el-collapse-item__header');
    assert.deepEqual(
        (await groups.allTextContents()).map((text) => text.trim().replace(/\s*已选.*$/, '')),
        ['商品与材料', '订单与制作', '客服与售后', '社区']
    );
    assert.doesNotMatch(await create.innerText(), /mall:|platform:|优惠券|商家管理员/);
    assert.equal(await create.getByText('编辑商品', {exact: true}).isVisible(), true);
    assert.equal(await create.getByText('删除商品', {exact: true}).isVisible(), true);
    await create.getByText('新增商品', {exact: true}).click();
    const aftersales = groups.filter({hasText: '客服与售后'});
    await aftersales.click();
    await create.getByText('重试退款', {exact: true}).click();
    for (const name of ['审核退款', '执行退款', '对账并更新退款状态']) {
        assert.equal(await create.getByRole('checkbox', {name, exact: true}).isChecked(), false);
    }
    const summary = create.locator('.role-permission-picker__summary');
    await summary.getByText('已选权限（2 项）').waitFor();
    assert.match(await summary.innerText(), /商品与材料.*新增商品/);
    assert.match(await summary.innerText(), /客服与售后.*重试退款/);
    assert.match(await aftersales.innerText(), /已选 1 项/);
    await aftersales.click();
    await create.getByRole('checkbox', {name: '重试退款', exact: true}).waitFor({state: 'hidden'});
    await create.locator('input[type=text]').first().fill('隔离新增');
    await create.getByRole('button', {name: '保存', exact: true}).click();
    await create.waitFor({state: 'hidden'});
    assert.deepEqual(saves[0], {roleName: '隔离新增', remark: '', menuIdList: ['add', 'retry']});

    await page.getByRole('button', {name: '编辑', exact: true}).click();
    const edit = page.getByRole('dialog', {name: '编辑岗位'});
    await edit.getByText('另保留 1 项已有授权').waitFor();
    assert.equal(await edit.getByRole('checkbox', {name: '编辑商品', exact: true}).isChecked(), true);
    const deleteGoods = edit.getByRole('checkbox', {name: '删除商品', exact: true});
    await edit.getByText('删除商品', {exact: true}).click();
    assert.equal(await deleteGoods.isChecked(), true);
    await edit.getByText('删除商品', {exact: true}).click();
    assert.equal(await deleteGoods.isChecked(), false);
    await edit.locator('textarea').fill('只改备注');
    await edit.getByRole('button', {name: '保存', exact: true}).click();
    await edit.waitFor({state: 'hidden'});
    assert.deepEqual(saves[1], {...role, remark: '只改备注'});
}

/** 使用无界面 Edge 核对生产构建产物的四个密码入口和岗位页面. */
async function main() {
    const {chromium} = require(
        require.resolve('playwright', {paths: [process.env.CARATCRAFT_NODE_MODULES || process.cwd()]})
    );
    const browser = await chromium.launch({channel: 'msedge', headless: true});
    try {
        const anonymous = await fixture(browser);
        await anonymous.page.goto(origin + '/#/login');
        await checkPasswords(anonymous.page, 1);
        await anonymous.page.getByRole('button', {name: '忘记密码'}).click();
        await anonymous.page.waitForURL('**/#/password-recovery');
        await checkPasswords(anonymous.page.locator('.password-recovery'), 2);
        await anonymous.context.close();

        const temporary = await fixture(browser, 'temporary');
        await temporary.page.goto(origin + '/#/home');
        await temporary.page.waitForURL('**/#/password-change');
        await checkPasswords(temporary.page.getByRole('dialog', {name: '修改临时密码'}), 3);
        await temporary.context.close();

        const manager = await fixture(browser, 'manager');
        await checkRoles(manager.page, manager.saves);
        await manager.page.locator('.site-navbar__avatar .el-dropdown-link').hover();
        await manager.page.getByText('修改密码', {exact: true}).click();
        await checkPasswords(manager.page.getByRole('dialog', {name: '修改密码'}), 3);
        await manager.context.close();
        console.log('密码四入口、岗位分类、权限摘要、退款独立选择及旧授权保留验证通过.');
    } finally {
        await browser.close();
    }
}

main().catch((error) => {
    console.error(error);
    process.exitCode = 1;
});
