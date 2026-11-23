/* 使用真实ESLint语法树验证中文职责门禁, 不以字符串正则替代解析. */
const assert = require('node:assert/strict');
const {test} = require('node:test');
const path = require('node:path');
const dependencies =
    process.env.CARATCRAFT_NODE_MODULES ||
    path.resolve(__dirname, '../../apps/admin-ui/node_modules');
const {Linter} = require(require.resolve('eslint', {paths: [dependencies]}));

/** 导出服务及嵌套组件方法都必须独立说明职责. */
test('注释门禁保留导出服务和嵌套组件覆盖', async () => {
    assert.equal((await inspect('module.exports={save(){return 1;}};')).length, 1);
    assert.equal((await inspect('Component({methods:{save(){return 1;}}});')).length, 1);
});

/** 为代码样本建立真实ESLint解析和共享规则实例. */
async function inspect(code) {
    const {default: rule} = await import('../../tools/quality/function-description-rule.mjs');
    return new Linter().verify(code, [
        {
            plugins: {project: {rules: {description: rule}}},
            rules: {'project/description': 'error'},
        },
    ]);
}

/** 工厂返回的页面方法与直接Page入口均不能绕过职责说明. */
test('注释门禁覆盖共享页面工厂和直接页面入口', async () => {
    assert.equal(
        (await inspect('/** 创建页面. */ function create() { return {onShow() {}}; }')).length,
        1
    );
    assert.equal((await inspect('Page({onShow() {}});')).length, 1);
    assert.equal(
        (
            await inspect(
                '/** 创建页面. */ function create() { return {/** 读取本人信息. */ onShow() {}}; }'
            )
        ).length,
        0
    );
});

/** 不将文件头或过远的注释借给没有说明的函数. */
test('注释门禁拒绝空白英文和远处职责说明', async () => {
    assert.equal((await inspect('/** English only. */ function load() {}')).length, 1);
    assert.equal(
        (await inspect('/** 其他字段. */\nconst value=1;\n\nfunction load() {}')).length,
        1
    );
});

/** 类构造函数及方法需各自说明用途, 具名顶层箭头函数同样检查. */
test('注释门禁覆盖构造函数方法及顶层箭头函数', async () => {
    assert.equal((await inspect('class Store { constructor() {} load() {} }')).length, 2);
    assert.equal((await inspect('const load = () => {};')).length, 1);
    assert.equal((await inspect('/** 读取当前配置. */ const load = () => {};')).length, 0);
});

/** export声明前的中文说明属于被导出的函数, 不误报为缺失. */
test('注释门禁识别导出函数的职责说明', async () => {
    assert.equal((await inspect('/** 读取当前配置. */ export function load() {}')).length, 0);
    assert.equal((await inspect('/** 读取当前配置. */ export const load = () => {};')).length, 0);
});

/** 测试用例需要可读的行为注释, 不能仅靠夹具注释通过. */
test('注释门禁覆盖测试用例', async () => {
    assert.equal((await inspect("test('失败恢复', () => {});")).length, 1);
    assert.equal(
        (await inspect("/** 网络失败后保留输入. */ test('失败恢复', () => {});")).length,
        0
    );
});

/** 匿名短回调沿用所属函数的职责说明, 不强制生成重复说明. */
test('注释门禁保留匿名短回调边界', async () => {
    assert.equal(
        (
            await inspect(
                '/** 保存并返回. */ function save() { wx.request({success: () => done()}); }'
            )
        ).length,
        0
    );
});

/** 管理端导出对象中的具名方法需要说明, 懒加载组件回调沿用注册上下文. */
test('注释门禁覆盖Vue选项及保留组件懒加载边界', async () => {
    assert.equal(
        (await inspect('export default {data() {return {};}, methods: {save() {}}};')).length,
        2
    );
    assert.equal(
        (await inspect("export default {components: {Editor: () => import('./editor.js')}};"))
            .length,
        0
    );
});
