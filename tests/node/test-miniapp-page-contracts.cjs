/* 全部注册页面的源码契约检查, 不将注册和语法检查冒充微信渲染或功能验收. */
const {test} = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const root = path.resolve('apps/miniapp');
const app = JSON.parse(fs.readFileSync(path.join(root, 'app.json'), 'utf8'));

/** 在独立小程序环境注册实际页面及其共享依赖, 禁止任何网络或支付调用. */
function definitionFor(page) {
    let definition;
    const storage = {};
    const wx = {
        getStorageSync: (key) => storage[key],
        getAccountInfoSync: () => ({miniProgram: {envVersion: 'develop'}}),
        getSystemInfoSync: () => ({windowWidth: 375, windowHeight: 812}),
        canIUse: () => false,
        request: () => {
            throw new Error('页面注册阶段不应发送请求');
        },
        requestPayment: () => {
            throw new Error('源码契约检查禁止真实支付');
        },
    };
    const context = vm.createContext({
        wx,
        console,
        setTimeout,
        clearTimeout,
        getApp: () => ({globalData: {userInfo: {}, selectedCoupon: {}}}),
        getCurrentPages: () => [],
        Page: (value) => {
            definition = value;
        },
    });
    const cache = new Map();

    /** 仅装载项目内真实CommonJS依赖, 拒绝路径越界及外部模块执行. */
    function load(file) {
        let absolute = path.resolve(file);
        if (!path.extname(absolute)) {
            absolute += '.js';
        }
        assert.ok(absolute.startsWith(root + path.sep), '依赖越界: ' + absolute);
        if (cache.has(absolute)) {
            return cache.get(absolute).exports;
        }
        const module = {exports: {}};
        cache.set(absolute, module);
        const source = fs.readFileSync(absolute, 'utf8');
        const execute = vm.runInContext(
            '(function(require,module,exports){\n' + source + '\n})',
            context,
            {filename: absolute}
        );
        execute(
            (name) => {
                assert.ok(name.startsWith('.'), '不允许执行外部依赖: ' + name);
                return load(path.resolve(path.dirname(absolute), name));
            },
            module,
            module.exports
        );
        return module.exports;
    }

    load(path.join(root, page + '.js'));
    assert.ok(definition && definition.data, '页面必须注册独立状态');
    return definition;
}

for (const page of app.pages) {
    /** 每个页面独立检查文件、注册、模板表达式及事件处理函数. */
    test('小程序页面源码契约: ' + page, () => {
        for (const extension of ['.js', '.json', '.wxml', '.wxss']) {
            assert.ok(
                fs.existsSync(path.join(root, page + extension)),
                '缺少页面文件: ' + extension
            );
        }
        JSON.parse(fs.readFileSync(path.join(root, page + '.json'), 'utf8'));
        const definition = definitionFor(page);
        const template = fs.readFileSync(path.join(root, page + '.wxml'), 'utf8');
        for (const match of template.matchAll(/\{\{([\s\S]*?)\}\}/g)) {
            assert.doesNotThrow(
                () => new vm.Script('(' + match[1] + ')'),
                '绑定表达式语法无效: ' + match[1]
            );
        }
        for (const match of template.matchAll(/\b(?:bind|catch):?[\w-]*\s*=\s*["']([\w]+)["']/g)) {
            assert.equal(typeof definition[match[1]], 'function', '缺少事件处理函数: ' + match[1]);
        }
    });
}
