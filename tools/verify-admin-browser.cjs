/* 使用真实浏览器检查生产包和Vue3双向绑定, 仅连接隔离回环测试服务. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const {start} = require('./serve-admin-ui-fixture.cjs');
const root = path.resolve(__dirname, '..');

/** 允许通过环境变量选择浏览器测试库, 不将本机绝对依赖路径写入项目配置. */
function browserTools() {
  const packages = process.env.CARATCRAFT_NODE_MODULES;
  return require(require.resolve('playwright', {paths: packages ? [packages] : [root]}));
}

/** 加载实际业务组件的脚本和模板, 基础控件用原生输入替身观察绑定契约. */
function subject(relative) {
  const ui = path.join(root, 'vendor/platform-wxshop/platform-admin-ui');
  const {parse} = require(path.join(ui, 'node_modules/@vue/compiler-sfc'));
  const {descriptor} = parse(fs.readFileSync(path.join(ui, 'src/components', relative), 'utf8'));
  const script = descriptor.script.content
    .replace(/^import .*;$/gm, '')
    .replace('export function sanitizeHtml', 'function sanitizeHtml')
    .replace('export default {', 'const Subject = {');
  return {script, template: descriptor.template.content};
}

/** 核对发布按钮、窄屏表单、表格详情及四个公共组件的真实Vue3父子同步. */
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
        console.error('隔离页面正文:', (await page.locator('body').innerText()).slice(0, 2000));
        console.error('页面异常:', errors);
        await page.screenshot({path: path.join(output, 'failure.png'), fullPage: true});
        throw error;
      });
    const publish = page.getByRole('button', {name: '发布已保存草稿'});
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
    for (const field of await lengthForm.locator('.length-rule-field').all()) {
      const label = await field.locator('span').first().boundingBox();
      const input = await field.getByRole('spinbutton').boundingBox();
      assert.ok(label && input && label.y + label.height <= input.y + 1);
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
        path: path.join(
          root,
          'vendor/platform-wxshop/platform-admin-ui/node_modules/vue/dist/vue.global.prod.js'
        ),
      });
      await page.evaluate(
        ({script, template, relative}) => {
          const create = new Function(
            'Vue',
            'DOMPurify',
            'Upload',
            script + '\nSubject.template=' + JSON.stringify(template) + '; return Subject;'
          );
          const Subject = create(window.Vue, {sanitize: (value) => value}, {});
          const app = window.Vue.createApp({
            data: () => ({value: relative.includes('tags') ? 'first,second' : 'initial'}),
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
        await page.evaluate(() => window.bindingRoot.$refs.subject.selectImg('/owned-test.png'));
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
      '浏览器验证通过: 协议发布条件、长度规则空值及保存校验、624/1280布局与标签无重叠、真实表格与描述详情、4个Vue3公共组件双向绑定; 无页面运行异常.'
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
