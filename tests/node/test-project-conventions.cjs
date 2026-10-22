/* 约定门禁回归使用合成目录, 不修改仓库文档或业务源码. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const {test} = require('node:test');
const {check} = require('../../tools/quality/check-project-conventions.cjs');

/** 创建本次独立样本并核对边界, 用例成功或失败均清理自己的目录. */
function fixture(context) {
    const day =
        process.env.CARATCRAFT_TEST_DATE ||
        new Date().toLocaleDateString('sv-SE', {timeZone: 'Asia/Shanghai'});
    const parent = path.join('D:/tmp/hailongcodex', day);
    fs.mkdirSync(parent, {recursive: true});
    const root = fs.mkdtempSync(path.join(parent, 'conventions-'));
    context.after(() => {
        assert.equal(path.dirname(path.resolve(root)), path.resolve(parent));
        fs.rmSync(root, {recursive: true});
    });
    fs.mkdirSync(path.join(root, 'docs'));
    for (const name of ['项目开发文档', '项目规范文档', '项目维护文档', '功能使用文档']) {
        fs.writeFileSync(path.join(root, 'docs', name + '.md'), '# ' + name + '\n');
    }
    fs.writeFileSync(path.join(root, '.prettierrc.json'), '{"tabWidth":4,"useTabs":false}');
    return root;
}

/** 固定中文入口及有效相对链接可以正常通过. */
test('规范门禁接受唯一中文文档和有效链接', (context) => {
    const root = fixture(context);
    fs.writeFileSync(path.join(root, 'README.md'), '[规范](docs/项目规范文档.md)');
    assert.deepEqual(check(root), []);
});

/** 不允许删掉必需入口或再添加日期报告. */
test('规范门禁拒绝遗漏和新增日期报告', (context) => {
    const root = fixture(context);
    fs.unlinkSync(path.join(root, 'docs/功能使用文档.md'));
    fs.writeFileSync(path.join(root, 'docs/整改报告-20261022.md'), '# 重复报告');
    assert.ok(check(root).some((message) => message.includes('四份')));
});

/** 原链接失效不能因文件整理而被静默忽略. */
test('规范门禁拒绝失效入口链接', (context) => {
    const root = fixture(context);
    fs.writeFileSync(path.join(root, 'README.md'), '[旧报告](docs/不存在.md)');
    assert.ok(check(root).some((message) => message.includes('链接不存在')));
});

/** 开发者不能把单个工具恢复到另一种缩进. */
test('规范门禁拒绝2空格或Tab配置', (context) => {
    const root = fixture(context);
    fs.writeFileSync(path.join(root, '.prettierrc.json'), '{"tabWidth":2,"useTabs":true}');
    assert.ok(check(root).some((message) => message.includes('4个空格')));
});

/** 子目录不能通过独立root配置重新覆盖统一缩进. */
test('规范门禁拒绝管理端子目录的2空格覆盖', (context) => {
    const root = fixture(context);
    const editor = path.join(root, 'apps/admin-ui/.editorconfig');
    fs.mkdirSync(path.dirname(editor), {recursive: true});
    fs.writeFileSync(editor, 'root = true\n[*]\nindent_size = 2\n');
    assert.ok(check(root).some((message) => message.includes('EditorConfig')));
});

/** 部署说明必须与主文档一起验证链接, 不能在子目录残留失效入口. */
test('规范门禁检查部署文档和源码旧目录引用', (context) => {
    const root = fixture(context);
    fs.mkdirSync(path.join(root, 'deploy'));
    fs.writeFileSync(path.join(root, 'deploy/README.md'), '[启动](../失效入口.sh)');
    const script = path.join(root, 'tools/legacy.cjs');
    fs.mkdirSync(path.dirname(script));
    fs.writeFileSync(script, 'const old = "vendor' + '/platform-wxshop";');
    const errors = check(root);
    assert.ok(errors.some((message) => message.includes('链接不存在')));
    assert.ok(errors.some((message) => message.includes('旧源码目录')));
});

/** 后续新增代码不能重新引入已清理的星号导入和作用域别名. */
test('规范门禁阻止星号导入和that别名回归', (context) => {
    const root = fixture(context);
    const java = path.join(root, 'backend/platform-biz/src/main/java/Example.java');
    const script = path.join(root, 'apps/miniapp/example.js');
    fs.mkdirSync(path.dirname(java), {recursive: true});
    fs.mkdirSync(path.dirname(script), {recursive: true});
    fs.writeFileSync(java, 'import java.util.*;');
    fs.writeFileSync(script, 'const that = this;');
    assert.equal(check(root).length, 2);
});

/** 已收敛的SQL、部署和管理端历史入口不得重新进入仓库. */
test('规范门禁拒绝恢复已删除的备用实现', (context) => {
    const root = fixture(context);
    fs.mkdirSync(path.join(root, 'env'));
    fs.mkdirSync(path.join(root, 'database/legacy'), {recursive: true});
    const errors = check(root);
    assert.ok(errors.some((message) => message.startsWith('env:')));
    assert.ok(errors.some((message) => message.startsWith('database/legacy:')));
});
