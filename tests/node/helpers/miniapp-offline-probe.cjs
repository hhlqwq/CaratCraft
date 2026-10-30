/* 独立进程探测实际页面生命周期的断网处理, 不联网、不渲染、不付款. */
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {setInterval, clearInterval} = require('node:timers');
const root = path.resolve('apps/miniapp');
let page;
let requests = 0;
const errors = [];
const cache = new Map();
process.on('unhandledRejection', (error) => errors.push(error?.message || String(error)));
const wx = new Proxy(
    {
        getAccountInfoSync: () => ({miniProgram: {envVersion: 'develop'}}),
        getStorageSync: (key) =>
            key === 'token'
                ? 'synthetic-session'
                : key === 'userInfo'
                  ? {nickname: '隔离用户'}
                  : undefined,
        getSystemInfoSync: () => ({windowWidth: 375, windowHeight: 812}),
        canIUse: () => false,
        /** 所有实际网络入口均强制失败, 只检查失败收尾. */
        request(options) {
            requests++;
            options.fail?.({errMsg: 'isolated-network-failure'});
            options.complete?.();
        },
        /** 探测不允许执行真实支付或意外进入支付阶段. */
        requestPayment() {
            throw new Error('断网探测禁止付款');
        },
    },
    {
        /** 只提供无副作用宿主边界, 业务代码及依赖使用仓库原件. */
        get(target, key) {
            return key in target
                ? target[key]
                : (options) => {
                      if (key === 'getSystemInfo') {
                          options?.success?.({windowWidth: 375, windowHeight: 812});
                      }
                  };
        },
    }
);
const app = {globalData: {token: 'synthetic-session', userInfo: {}, selectedCoupon: {}}};
const context = vm.createContext({
    wx,
    console: {log() {}, error() {}, warn() {}},
    setTimeout,
    clearTimeout,
    setInterval,
    clearInterval,
    getApp: () => app,
    getCurrentPages: () => [{route: 'pages/index/index'}, page],
    Page: (definition) => {
        page = definition;
    },
});

/** 递归装载真实本地脚本, 禁止依赖越过小程序源码目录. */
function load(file) {
    let absolute = path.resolve(file);
    if (!path.extname(absolute)) {
        absolute += '.js';
    }
    if (!absolute.startsWith(root + path.sep)) {
        throw new Error('依赖越界');
    }
    if (cache.has(absolute)) {
        return cache.get(absolute).exports;
    }
    const module = {exports: {}};
    cache.set(absolute, module);
    vm.runInContext(
        '(function(require,module,exports){' + fs.readFileSync(absolute, 'utf8') + '\n})',
        context,
        {filename: absolute}
    )((name) => load(path.resolve(path.dirname(absolute), name)), module, module.exports);
    return module.exports;
}

try {
    load(path.join(root, process.argv[2] + '.js'));
    page.data = structuredClone(page.data);
    page.setData = (values) => Object.assign(page.data, values);
    const options = {
        id: '1999105',
        orderId: '1999105',
        valueId: '1999105',
        typeId: '1',
        keyword: '隔离测试',
    };
    for (const key of ['onLoad', 'onShow', 'onReady']) {
        try {
            page[key]?.(options)?.catch?.((error) => errors.push(error?.message || String(error)));
        } catch (error) {
            errors.push(error.message);
        }
    }
} catch (error) {
    errors.push(error.message);
}
setTimeout(() => {
    try {
        page?.onUnload?.();
    } catch (error) {
        errors.push(error.message);
    }
    console.log(
        JSON.stringify({
            page: process.argv[2],
            requests,
            errors,
            readError: page?.data?.readError || '',
        })
    );
    process.exit(0);
}, 80);
