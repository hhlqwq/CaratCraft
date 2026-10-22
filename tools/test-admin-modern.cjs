/* 管理端现代浏览器适配回归, 不访问业务接口. */
const {test} = require('node:test');
const assert = require('node:assert/strict');
const {createCipheriv} = require('node:crypto');
const fs = require('node:fs');
const path = require('node:path');
const root = path.resolve(__dirname, '../vendor/platform-wxshop/platform-admin-ui');

/** 在独立ES模块中加载无依赖的浏览器适配函数. */
async function moduleAt(file) {
    return import(
        'data:text/javascript;base64,' + fs.readFileSync(path.join(root, file)).toString('base64')
    );
}

/** 新WebCrypto实现必须与Java既有AES-CBC零填充协议逐字节一致. */
test('浏览器原生AES保持历史登录协议兼容', async () => {
    const {Encrypt} = await moduleAt('src/utils/AESUtils.js');
    for (const input of ['password123', '1234567890123456', '12345678901234567', '中文密码123']) {
        const value = Buffer.from(input);
        const padded = Buffer.alloc(Math.ceil(value.length / 16) * 16);
        value.copy(padded);
        const cipher = createCipheriv(
            'aes-128-cbc',
            Buffer.from('_aes_secret_key_'),
            Buffer.from('_aes_secret_iv__')
        );
        cipher.setAutoPadding(false);
        const expected = Buffer.concat([cipher.update(padded), cipher.final()]).toString('base64');
        assert.equal(await Encrypt(input), expected);
    }
});

/** 旧版本分包加载失败时只提示一次, 用户取消后保留当前编辑页面. */
test('分包更新提示去重且不自动丢弃编辑', async () => {
    const {watchBuildUpdates} = await moduleAt('src/utils/build-updates.js');
    let listener;
    let reloads = 0;
    let prompts = 0;
    let rejectPrompt;
    watchBuildUpdates(
        {
            confirm: () => {
                prompts++;
                return new Promise((resolve, reject) => {
                    rejectPrompt = reject;
                });
            },
        },
        {
            addEventListener: (name, fn) => {
                listener = fn;
            },
            location: {
                reload: () => {
                    reloads++;
                },
            },
        }
    );
    listener({preventDefault() {}});
    listener({preventDefault() {}});
    assert.equal(prompts, 1);
    rejectPrompt('cancel');
    await new Promise(setImmediate);
    assert.equal(reloads, 0);
});
