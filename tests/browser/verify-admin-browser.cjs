/* 使用真实浏览器检查生产包和Vue3双向绑定, 仅连接隔离回环测试服务. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const {start} = require('./serve-admin-ui-fixture.cjs');
const root = path.resolve(__dirname, '../..');

/** 允许通过环境变量选择浏览器测试库, 不将本机绝对依赖路径写入项目配置. */
function browserTools() {
    const packages = process.env.CARATCRAFT_NODE_MODULES;
    return require(require.resolve('playwright', {paths: packages ? [packages] : [root]}));
}

/** 加载实际业务组件的脚本和模板, 基础控件用原生输入替身观察绑定契约. */
function subject(relative) {
    const ui = path.join(root, 'apps/admin-ui');
    const {parse} = require(path.join(ui, 'node_modules/@vue/compiler-sfc'));
    const {descriptor} = parse(fs.readFileSync(path.join(ui, 'src/components', relative), 'utf8'));
    const script = descriptor.script.content
        .replace(/^import .*;$/gm, '')
        .replace('export function sanitizeHtml', 'function sanitizeHtml')
        .replace('export default {', 'const Subject = {');
    return {script, template: descriptor.template.content};
}

/** 校验截图中的深色菜单可读性和真实折叠行为, 不连接业务菜单接口. */
async function verifySidebar(page, output) {
    const title = page.locator('.site-sidebar .el-sub-menu__title').filter({hasText: '系统设置'});
    await title.waitFor();
    const contrast = await title.evaluate((element) => {
        /** 按sRGB相对亮度计算前景与深色背景的对比度. */
        function luminance(color) {
            const values = color
                .match(/\d+/g)
                .slice(0, 3)
                .map((value) => {
                    const channel = Number(value) / 255;
                    return channel <= 0.04045
                        ? channel / 12.92
                        : ((channel + 0.055) / 1.055) ** 2.4;
                });
            return values[0] * 0.2126 + values[1] * 0.7152 + values[2] * 0.0722;
        }
        const foreground = luminance(getComputedStyle(element).color);
        const background = luminance(
            getComputedStyle(document.querySelector('.site-sidebar')).backgroundColor
        );
        return (
            (Math.max(foreground, background) + 0.05) / (Math.min(foreground, background) + 0.05)
        );
    });
    assert.ok(contrast >= 4.5, '侧栏文字对比度不足: ' + contrast);
    await title.click();
    await page.locator('.site-sidebar').getByText('组织管理', {exact: true}).waitFor();
    const toggle = page.getByRole('menuitem', {name: '展开或折叠导航'});
    await toggle.click();
    await page.waitForFunction(
        () => document.querySelector('.site-sidebar').getBoundingClientRect().width <= 65
    );
    assert.equal(await title.locator('span').first().isVisible(), false);
    await toggle.click();
    await page.waitForFunction(
        () => document.querySelector('.site-sidebar').getBoundingClientRect().width >= 230
    );
    await page.screenshot({path: path.join(output, 'sidebar-corrected.png'), fullPage: true});
}

/** 在实际业务页面验证树表格、树选择弹窗、日期选择和图片类型拒绝. */
async function verifyNativeControls(page, base) {
    await page.goto(base + '/#/sys-org');
    const table = page.locator('.mod-org .el-table');
    await table.getByText('隔离总部', {exact: true}).waitFor();
    assert.equal(await table.getByText('隔离门店', {exact: true}).isVisible(), false);
    await table.locator('.el-table__expand-icon').first().click();
    await table.getByText('隔离门店', {exact: true}).waitFor();
    await table.locator('.el-table__expand-icon').first().click();
    await table.getByText('隔离门店', {exact: true}).waitFor({state: 'hidden'});
    await page.getByRole('button', {name: '新增', exact: true}).click();
    await page.getByRole('button', {name: '点击选择上级机构'}).click();
    await page.getByRole('dialog', {name: '选择区域'}).getByText('隔离总部', {exact: true}).click();
    await page
        .getByRole('dialog', {name: '新增', exact: true})
        .getByRole('button', {name: '隔离总部', exact: true})
        .waitFor();
    await page
        .getByRole('dialog', {name: '新增', exact: true})
        .getByRole('button', {name: '取消', exact: true})
        .click();
    await page.goto(base + '/#/sys-menu');
    const menus = page.locator('.mod-menu .el-table');
    await menus.getByText('隔离目录', {exact: true}).waitFor();
    await menus.locator('.el-table__expand-icon').first().click();
    await menus.getByText('隔离子菜单', {exact: true}).waitFor();
    await page.goto(base + '/#/mall-ad');
    await page.getByRole('button', {name: '新增', exact: true}).click();
    const date = page.locator('.el-dialog .el-date-editor input');
    await date.click();
    await page.locator('.el-date-picker .el-date-table td.available').first().click();
    assert.ok((await date.inputValue()).length > 0);
    await page
        .getByRole('dialog', {name: '新增', exact: true})
        .getByRole('button', {name: '取消', exact: true})
        .click();
    await page.goto(base + '/#/oss-oss');
    await page.getByRole('button', {name: '上传文件', exact: true}).click();
    await page.locator('.el-upload input[type="file"]').setInputFiles({
        name: 'invalid.txt',
        mimeType: 'text/plain',
        buffer: Buffer.from('not an image'),
    });
    await page.getByText('只支持 JPG、PNG、GIF、WebP 格式的图片！', {exact: true}).waitFor();
    await page
        .getByRole('dialog', {name: '上传文件', exact: true})
        .locator('.el-dialog__headerbtn')
        .click();
}

/** 使用独立浏览器页验证无写权限时隐藏操作, 不复用上一个管理员的会话缓存. */
async function verifyReadOnlyPermissions(browser, base, errors) {
    const page = await browser.newPage();
    page.on('pageerror', (error) => errors.push(error.stack || error.message));
    await page.route('**/fixture-api/sys/menu/nav*', async (route) => {
        const response = await route.fetch();
        const data = await response.json();
        data.data.permissions = ['sys:org:list'];
        await route.fulfill({response, json: data});
    });
    try {
        await page.goto(base + '/#/sys-org');
        await page.locator('.mod-org .el-table').getByText('隔离总部', {exact: true}).waitFor();
        assert.equal(await page.getByRole('button', {name: '新增', exact: true}).count(), 0);
        assert.equal(await page.getByRole('button', {name: '修改', exact: true}).count(), 0);
        assert.equal(await page.getByRole('button', {name: '删除', exact: true}).count(), 0);
    } finally {
        await page.close();
    }
}

/** 核对实际页面及公共组件, 所有数据和写入入口均使用隔离替身. */
async function main() {
    const {chromium} = browserTools();
    const server = await start(0);
    const output = path.join(root, '.tools/copyright/browser-tests');
    fs.mkdirSync(output, {recursive: true});
    const browser = await chromium.launch({
        channel: process.env.CARATCRAFT_BROWSER || 'msedge',
        headless: true,
    });
    const errors = [];
    try {
        const page = await browser.newPage({viewport: {width: 1280, height: 900}});
        page.on('pageerror', (error) => errors.push(error.stack || error.message));
        await page.goto('http://127.0.0.1:' + server.address().port + '/#/merchant-legal');
        await page
            .getByText('最新修订 1')
            .waitFor({timeout: 10000})
            .catch(async (error) => {
                console.error(
                    '隔离页面正文:',
                    (await page.locator('body').innerText()).slice(0, 2000)
                );
                console.error('页面异常:', errors);
                await page.screenshot({path: path.join(output, 'failure.png'), fullPage: true});
                throw error;
            });
        const publish = page.getByRole('button', {name: '发布已保存草稿'});
        await verifySidebar(page, output);
        assert.equal(await publish.isEnabled(), true);
        await page.getByRole('textbox').first().fill('本地修改尚未保存');
        assert.equal(await publish.isEnabled(), false);
        await page.getByRole('button', {name: '重新加载', exact: true}).click();
        await page.waitForFunction(
            () => document.querySelector('.legal-admin .el-form input').value === '隔离测试用户协议'
        );
        await page.evaluate(() => {
            document.activeElement.blur();
            window.scrollTo(0, 0);
        });
        await page.screenshot({path: path.join(output, 'legal-desktop.png'), fullPage: true});
        await page.setViewportSize({width: 624, height: 900});
        await page.waitForFunction(
            () => document.querySelector('.site-sidebar').getBoundingClientRect().width <= 65
        );
        await page.evaluate(() => window.scrollTo(0, 0));
        await page.screenshot({path: path.join(output, 'legal-narrow.png'), fullPage: true});
        assert.equal(
            await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth),
            true
        );
        await page.setViewportSize({width: 1280, height: 900});
        await page.goto('http://127.0.0.1:' + server.address().port + '/#/custom-configuration');
        const lengthForm = page.getByText('成品长度规则', {exact: true}).locator('..');
        await lengthForm.locator('.el-switch__core').click();
        await lengthForm.getByText('请选择实际制作方式', {exact: true}).waitFor();
        const sizes = lengthForm.getByRole('spinbutton');
        await sizes.first().waitFor();
        assert.equal(await sizes.count(), 6);
        assert.equal(await sizes.nth(4).inputValue(), '');
        await sizes.nth(4).fill('350');
        await sizes.nth(4).blur();
        assert.equal(Number(await sizes.nth(4).inputValue()), 350);
        await page.getByRole('button', {name: '保存商品配置'}).click();
        await page.getByText('请完整填写制作规则, 最小长度不能大于最大长度').waitFor();
        await page.setViewportSize({width: 624, height: 900});
        await page.waitForFunction(
            () => document.querySelector('.site-sidebar').getBoundingClientRect().width <= 65
        );
        for (const field of await lengthForm.locator('.length-rule-field').all()) {
            const label = await field.locator('span').first().boundingBox();
            const input = await field.getByRole('spinbutton').boundingBox();
            assert.ok(
                label && input && label.y + label.height <= input.y + 1,
                '长度字段标签不得压住输入框: ' + JSON.stringify({label, input})
            );
        }
        await page.locator('.el-message').waitFor({state: 'hidden'});
        assert.equal(
            await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth),
            true
        );
        await page.screenshot({path: path.join(output, 'length-rules-narrow.png'), fullPage: true});
        await page.setViewportSize({width: 1280, height: 900});
        await page.goto('http://127.0.0.1:' + server.address().port + '/#/mall-address');
        await page.locator('.el-table').getByText('隔离收货人', {exact: true}).waitFor();
        await page.getByRole('button', {name: '查看', exact: true}).click();
        await page
            .locator('.el-descriptions')
            .getByText('隔离收货人', {exact: true})
            .waitFor({timeout: 10000});
        await page.locator('.el-dialog').getByRole('button', {name: '取消', exact: true}).click();
        const base = 'http://127.0.0.1:' + server.address().port;
        await verifyNativeControls(page, base);
        await verifyReadOnlyPermissions(browser, base, errors);
        for (const relative of [
            'tags-editor.vue',
            'el-dict/index.vue',
            'el-img/index.vue',
            'rich-text-editor/index.vue',
        ]) {
            const fixture = subject(relative);
            await page.goto('http://127.0.0.1:' + server.address().port + '/empty-binding-fixture');
            await page.setContent('<div id="binding-root"></div>');
            await page.addScriptTag({
                path: path.join(root, 'apps/admin-ui/node_modules/vue/dist/vue.global.prod.js'),
            });
            await page.evaluate(
                ({script, template, relative}) => {
                    const create = new Function(
                        'Vue',
                        'DOMPurify',
                        'Upload',
                        script +
                            '\nSubject.template=' +
                            JSON.stringify(template) +
                            '; return Subject;'
                    );
                    const Subject = create(window.Vue, {sanitize: (value) => value}, {});
                    const app = window.Vue.createApp({
                        data: () => ({
                            value: relative.includes('tags') ? 'first,second' : 'initial',
                        }),
                        template:
                            '<subject ref="subject" v-model="value" /><pre id="model-result">{{value}}</pre>',
                    });
                    app.component('subject', Subject);
                    app.component('el-select', {
                        props: ['modelValue'],
                        emits: ['update:modelValue', 'change'],
                        template:
                            '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"><slot />',
                    });
                    for (const name of [
                        'el-option',
                        'el-button',
                        'el-input',
                        'el-tag',
                        'el-dialog',
                        'el-image',
                        'el-form',
                        'el-form-item',
                        'el-row',
                        'el-col',
                        'el-card',
                        'el-pagination',
                    ]) {
                        app.component(name, {template: '<div><slot /></div>'});
                    }
                    app.config.globalProperties.$http = async () => ({
                        data: {code: 0, data: {list: [], records: [], total: 0}},
                    });
                    app.config.globalProperties.isAuth = () => true;
                    app.config.globalProperties.openImg = () => {};
                    window.bindingRoot = app.mount('#binding-root');
                },
                {...fixture, relative}
            );
            if (relative.includes('tags')) {
                await page.evaluate(() => window.bindingRoot.$refs.subject.handleClose('first'));
                assert.equal(await page.locator('#model-result').textContent(), 'second');
            } else if (relative.includes('el-dict')) {
                await page.locator('input').fill('changed');
                assert.equal(await page.locator('#model-result').textContent(), 'changed');
            } else if (relative.includes('el-img')) {
                await page.evaluate(() =>
                    window.bindingRoot.$refs.subject.selectImg('/owned-test.png')
                );
                assert.equal(await page.locator('#model-result').textContent(), '/owned-test.png');
            } else {
                await page.locator('[contenteditable="true"]').fill('changed HTML');
                assert.equal(await page.locator('#model-result').textContent(), 'changed HTML');
            }
            await page.evaluate(() => {
                window.bindingRoot.value = 'parent update';
            });
            await page.waitForFunction(
                () => document.querySelector('#model-result').textContent === 'parent update'
            );
        }
        assert.deepEqual(errors, []);
        console.log(
            '浏览器验证通过: 菜单可读性与折叠、原生树表格与树选择、日期及图片类型拒绝、只读权限、协议发布条件、长度表单、624/1280布局、真实表格与描述详情、4个Vue3公共组件绑定; 无页面运行异常.'
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
