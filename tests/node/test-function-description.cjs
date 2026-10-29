/* 验证注释门禁覆盖嵌套组件和服务方法, 不把外部API短回调误判为公开方法. */
const {test} = require('node:test');
const assert = require('node:assert/strict');
const path = require('node:path');
const {ESLint} = require('../../apps/admin-ui/node_modules/eslint');

/** 使用真实ESLint配置解析合成小程序代码, 不只断言正则文本. */
async function descriptions(source) {
    const lint = new ESLint({
        overrideConfigFile: path.resolve('tools/quality/eslint-miniprogram.config.mjs'),
    });
    const [result] = await lint.lintText(source, {
        filePath: 'apps/miniapp/components/fixture/index.js',
    });
    return result.messages.filter((message) => message.ruleId === 'project/function-description');
}

/** 嵌套组件方法及导出的服务方法必须单独说明职责. */
test('注释门禁拒绝嵌套组件和服务的无说明方法', async () => {
    assert.equal((await descriptions('Component({methods:{save(){return 1;}}});')).length, 1);
    assert.equal((await descriptions('module.exports={save(){return 1;}};')).length, 1);
});

/** 方法已有职责时允许API短回调, 不要求逐条注释事件结果处理. */
test('注释门禁接受有职责说明的方法和所属API回调', async () => {
    const source =
        'Component({methods:{\n/** 保存当前输入. */\nsave(){wx.request({success(){return 1;}});}}});';
    assert.equal((await descriptions(source)).length, 0);
});
