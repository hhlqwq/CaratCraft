/* 商品选择与购买链路回归, 只使用合成商品、受控Promise及微信替身. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');
const sku = require('../vendor/platform-wxshop/wx-mall/services/goods-sku.js');
const root = path.resolve(__dirname, '../vendor/platform-wxshop/wx-mall');
const api = Object.fromEntries(
    [
        'GoodsDetail',
        'GoodsRelated',
        'CartGoodsCount',
        'CustomizationConfig',
        'CollectAddOrDelete',
        'BuyAdd',
        'CartAdd',
    ].map((name) => [name, name])
);

/** 两组同名前缀规格配合不同SKU价格和库存, 暴露子串误匹配及库存读取问题. */
function specifications() {
    return [
        {
            specificationId: 7,
            valueList: [
                {id: 1, value: '白色'},
                {id: 11, value: '金色'},
            ],
        },
        {
            specificationId: 8,
            valueList: [
                {id: 2, value: '8mm'},
                {id: 22, value: '9mm'},
            ],
        },
    ];
}

/** 读取页面真实代码, 网络请求排队以控制成功、失败与迟到顺序. */
function fixture(token = 'A') {
    let page;
    const storage = {token};
    const calls = [];
    const messages = [];
    const navigations = [];
    const util = {
        request: (url, data, method, header) =>
            new Promise((resolve, reject) => {
                calls.push({url, data, method, header, resolve, reject});
            }),
        showErrorToast: (message) => messages.push(message),
        showSuccessToast: (message) => messages.push(message),
    };
    const wx = {
        getStorageSync: (key) => storage[key],
        setStorageSync: (key, value) => {
            storage[key] = value;
        },
        switchTab() {},
        navigateTo: (options) => {
            navigations.push(options);
        },
    };
    vm.runInNewContext(fs.readFileSync(path.join(root, 'pages/goods/goods.js'), 'utf8'), {
        require: (name) =>
            ({
                '../../utils/util.js': util,
                '../../config/api.js': api,
                '../../services/pearl-catalog.js': {parameters: () => []},
                '../../services/goods-sku.js': sku,
            })[name],
        Page: (definition) => {
            page = definition;
        },
        wx,
    });
    page.data = structuredClone(page.data);
    page.setData = (update) => Object.assign(page.data, update);
    page._epoch = 1;
    page._token = token;
    Object.assign(page.data, {
        id: 10,
        goods: {id: 10, retailPrice: '100.00'},
        loading: false,
        productList: [
            {id: 31, goodsSpecificationIds: '7:金色|8:9mm', goodsNumber: 8, retailPrice: '200.00'},
            {id: 32, goodsSpecificationIds: '7:白色|8:8mm', goodsNumber: 2, retailPrice: '80.00'},
            {id: 33, goodsSpecificationIds: '7:白色|8:9mm', goodsNumber: 0, retailPrice: '90.00'},
        ],
    });
    page.setDefSpecInfo(specifications());
    return {page, calls, messages, navigations, storage};
}

/** 模拟用户点选真实规格控件的事件参数. */
function choose(page, nameId, valueId) {
    page.clickSkuValue({currentTarget: {dataset: {nameId, valueId}}});
}

/** 准备可购买组合, 保留规格面板打开便于直接验证提交. */
function ready(page) {
    choose(page, 7, 1);
    choose(page, 8, 2);
    page.switchAttrPop();
}

/** 仅匹配规范化的具名组合, 旧数字格式、前缀、缺项和重复记录均不回退. */
test('SKU使用唯一具名标准并拒绝旧格式和歧义', () => {
    const groups = specifications();
    const products = [
        {id: 1, goodsSpecificationIds: '7:金色|8:9mm'},
        {id: 2, goodsSpecificationIds: '7:白色|8:8mm', goodsNumber: 3},
    ];
    let index = sku.prepare(products, groups);
    assert.equal(sku.findProduct(index, ['1', '2']).id, 2);
    assert.equal(sku.findProduct(index, ['2', '1']).id, 2);
    assert.equal(sku.findProduct(index, ['1']), null);
    index = sku.prepare([...products, products[1]], groups);
    assert.equal(sku.findProduct(index, ['1', '2']), null);
    for (const key of ['1_2', '7:白|8:8mm', '8:8mm|7:白色', '7:白色|7:白色', '7:未知|8:8mm']) {
        assert.equal(
            sku.findProduct(sku.prepare([{goodsSpecificationIds: key}], groups), ['1', '2']),
            null
        );
    }
    const available = sku.updateAvailability(groups, sku.prepare([products[1]], groups));
    assert.equal(available[0].valueList[0].disabled, false);
    assert.equal(available[0].valueList[1].disabled, true);
});

/** 缺货和非整数库存不能误认为可购买, 有效库存限制在服务端上限以内. */
test('库存为空、负数、小数或异常时不可购买', () => {
    for (const value of [null, undefined, -1, 0, '错误', 1.5, Infinity]) {
        assert.equal(sku.availableStock({goodsNumber: value}), 0);
    }
    assert.equal(sku.availableStock({goodsNumber: '3'}), 3);
    assert.equal(sku.availableStock({goodsNumber: 20000}), 9999);
});

/** 选中一组会禁用缺货或无对应组合的另一组选项, 完整选择显示SKU价格. */
test('规格联动排除缺货组合并同步价格、库存和数量', () => {
    const {page} = fixture();
    choose(page, 7, 1);
    assert.equal(page.data.specificationList[1].valueList[1].disabled, true);
    choose(page, 8, 22);
    assert.equal(page.data.selectedProduct, null);
    choose(page, 8, 2);
    assert.equal(page.data.selectedProduct.id, 32);
    assert.equal(page.data.selectedProduct.retailPrice, '80.00');
    page.addNumber();
    page.addNumber();
    assert.equal(page.data.number, 2);
    choose(page, 8, 2);
    assert.equal(page.data.selectedProduct, null);
    assert.equal(page.data.number, 1);
});

/** 无规格商品只有唯一默认SKU可购买, 不能以空字符串误选第一条商品. */
test('默认SKU唯一识别且单值规格自动选择', () => {
    const {page} = fixture();
    page.data.productList = [{id: 5, goodsSpecificationIds: '', goodsNumber: 2}];
    page.setDefSpecInfo([]);
    assert.equal(page.data.selectedProduct.id, 5);
    assert.equal(page.data.checkedSpecText, '默认规格');
    page.data.productList.push({id: 6, goodsSpecificationIds: '', goodsNumber: 1});
    page.setDefSpecInfo([]);
    assert.equal(page.data.selectedProduct, null);
    page.data.productList = [{id: 9, goodsSpecificationIds: '7:白色|8:8mm', goodsNumber: 0}];
    page.setDefSpecInfo(
        specifications().map((group) => ({...group, valueList: group.valueList.slice(0, 1)}))
    );
    assert.equal(page.data.selectedProduct.id, 9);
    assert.equal(page.data.selectedStock, 0);
});

/** 初次购买打开面板, 不完整、缺货、非整数或超上限数量不能进入请求. */
test('购买校验对两个入口一致且不发送非法请求', async () => {
    for (const method of ['buyGoods', 'addToCart']) {
        const {page, calls} = fixture();
        await page[method]();
        assert.equal(page.data.openAttr, true);
        await page[method]();
        ready(page);
        for (const number of [0, -1, 1.5, NaN, 3, 10000]) {
            page.data.number = number;
            await page[method]();
        }
        assert.equal(calls.length, 0);
    }
});

/** 一次请求期间两个按钮均不能再次提交, 加购成功刷新数量和面板图标. */
test('连续点击仅发送一次加购且成功收尾', async () => {
    const {page, calls, messages} = fixture();
    ready(page);
    const pending = page.addToCart();
    await page.addToCart();
    await page.buyGoods();
    page.addNumber();
    choose(page, 7, 11);
    assert.equal(calls.length, 1);
    assert.deepEqual(JSON.parse(JSON.stringify(calls[0].data)), {
        goodsId: 10,
        productId: 32,
        number: 1,
    });
    calls[0].resolve({code: 0, data: {cartTotal: {goodsCount: 4}}});
    await pending;
    assert.equal(page.data.submitting, false);
    assert.equal(page.data.openAttr, false);
    assert.equal(page.data.cartGoodsCount, 4);
    assert.equal(page.data.collectBackImage, page.data.noCollectImage);
    assert.equal(messages.at(-1), '添加成功');
});

/** 失败不自动重试或导航, 原选择保留以供用户再次提交. */
test('加购网络及业务失败保留选择并释放提交锁', async () => {
    const {page, calls, messages, navigations} = fixture();
    ready(page);
    let pending = page.addToCart();
    calls[0].reject(new Error('网络断开'));
    await pending;
    assert.equal(page.data.openAttr, true);
    assert.equal(page.data.submitting, false);
    pending = page.addToCart();
    calls[1].resolve({code: 400, msg: '库存已变化'});
    await pending;
    assert.equal(page.data.selectedProduct.id, 32);
    assert.equal(page.data.submitting, false);
    assert.equal(messages.at(-1), '库存已变化');
    assert.equal(navigations.length, 0);
    assert.equal(calls.length, 2);
});

/** 结算只携带服务端上下文, 导航完成前冻结按钮, 导航失败可再次操作. */
test('立即购买使用上下文并处理结算导航失败', async () => {
    const {page, calls, navigations, messages} = fixture();
    ready(page);
    let pending = page.buyGoods();
    calls[0].resolve({code: 0, data: {checkoutId: 'context/1'}});
    await new Promise(setImmediate);
    assert.equal(page.data.submitting, true);
    assert.match(navigations[0].url, /checkoutId=context%2F1$/);
    navigations[0].fail();
    await pending;
    assert.equal(page.data.submitting, false);
    assert.equal(page.data.openAttr, true);
    assert.match(messages.at(-1), /结算页面打开失败/);
    pending = page.buyGoods();
    calls[1].resolve({code: 0, data: {checkoutId: 'next'}});
    await new Promise(setImmediate);
    navigations[1].success();
    await pending;
    assert.equal(page.data.openAttr, false);
});

/** 请求完成后若账号已变化, 不向新账号导航或显示旧账号操作结果. */
test('购买及收藏的迟到结果隔离账号切换和页面卸载', async () => {
    for (const action of ['addToCart', 'buyGoods', 'closeAttrOrCollect']) {
        for (const stale of ['account', 'unload']) {
            const {page, calls, messages, navigations, storage} = fixture();
            ready(page);
            if (action === 'closeAttrOrCollect') {
                page.data.openAttr = false;
            }
            const pending = page[action]();
            if (stale === 'account') {
                storage.token = 'B';
            } else {
                page.onUnload();
            }
            const snapshot = JSON.stringify(page.data);
            calls[0].resolve({
                code: 0,
                data: {cartTotal: {goodsCount: 99}, checkoutId: 'old', type: 'add'},
            });
            await pending;
            assert.equal(JSON.stringify(page.data), snapshot);
            assert.equal(messages.length, 0);
            assert.equal(navigations.length, 0);
        }
    }
});

/** 收藏状态必须同步更新以保持反复打开关闭面板的图标一致. */
test('收藏重复点击串行化且失败后可重试', async () => {
    const {page, calls, messages} = fixture();
    let pending = page.closeAttrOrCollect();
    await page.closeAttrOrCollect();
    assert.equal(calls.length, 1);
    calls[0].resolve({code: 0, data: {type: 'add'}});
    await pending;
    page.switchAttrPop();
    await page.closeAttrOrCollect();
    assert.equal(page.data.userHasCollect, 1);
    assert.equal(page.data.collectBackImage, page.data.hasCollectImage);
    pending = page.closeAttrOrCollect();
    calls[1].reject(new Error('收藏失败'));
    await pending;
    assert.equal(page.data.collecting, false);
    assert.equal(page.data.userHasCollect, 1);
    assert.equal(messages.at(-1), '收藏失败');
});

/** 游客加载详情不访问需要登录的购物车, 可选接口失败均正常结束. */
test('游客浏览及辅助请求失败不打断商品详情', async () => {
    const {page, calls} = fixture('');
    await page.loadCartCount();
    assert.equal(calls.length, 0);
    const customization = page.loadCustomization();
    calls[0].reject(new Error('offline'));
    await customization;
    assert.equal(page.data.isCustom, false);
    const related = page.getGoodsRelated();
    calls[1].reject(new Error('推荐失败'));
    await related;
    assert.equal(page.data.goodsError, '');
    assert.equal(page.data.relatedError, '推荐失败');
    const retry = page.getGoodsRelated();
    calls[2].resolve({code: 0, data: {goodsList: [{id: 30}]}});
    await retry;
    assert.equal(page.data.relatedError, '');
    assert.equal(page.data.relatedGoods[0].id, 30);
});

/** 主详情重试仅接收最新响应, 所有读取在页面卸载后均不得写入. */
test('详情代次和全部读取请求拒绝卸载后的响应', async () => {
    const {page, calls} = fixture();
    const first = page.getGoodsInfo();
    const second = page.getGoodsInfo();
    calls[0].reject(new Error('旧错误'));
    calls[1].resolve({
        code: 0,
        data: {
            info: {id: 10, name: '新结果'},
            specificationList: [],
            productList: [{id: 1, goodsSpecificationIds: '', goodsNumber: 1}],
        },
    });
    await Promise.all([first, second]);
    assert.equal(page.data.goods.name, '新结果');
    assert.equal(page.data.goodsError, '');
    calls[2].resolve({code: 0, data: {goodsList: []}});
    await new Promise(setImmediate);
    for (const method of [
        'getGoodsInfo',
        'getGoodsRelated',
        'loadCartCount',
        'loadCustomization',
    ]) {
        const current = fixture();
        const pending = current.page[method]();
        current.page.onUnload();
        const snapshot = JSON.stringify(current.page.data);
        current.calls[0].reject(new Error('迟到失败'));
        await pending;
        assert.equal(JSON.stringify(current.page.data), snapshot);
    }
});

/** 非法编号不发请求, 账号变化清理旧收藏和提交锁后重新加载当前账号数据. */
test('无效商品编号与账号切换状态清理', () => {
    const invalid = fixture();
    invalid.page.onLoad({id: '10x'});
    assert.equal(invalid.calls.length, 0);
    assert.match(invalid.page.data.goodsError, /编号无效/);
    const current = fixture();
    Object.assign(current.page.data, {
        submitting: true,
        collecting: true,
        userHasCollect: 1,
        cartGoodsCount: 12,
    });
    current.storage.token = 'B';
    current.page.onShow();
    assert.equal(current.page.data.submitting, false);
    assert.equal(current.page.data.collecting, false);
    assert.equal(current.page.data.cartGoodsCount, 0);
    assert.equal(current.page.data.userHasCollect, 0);
    assert.equal(current.calls.length, 3);
    current.page.onUnload();
    current.calls.forEach((call) => call.reject(new Error('结束测试')));
});

/** 组合在装载时解析一次, 控件联动复用索引而不重复读取512个SKU键. */
test('512组合的规格联动复用一次解析结果', () => {
    const groups = Array.from({length: 9}, (_, position) => ({
        specificationId: position + 1,
        valueList: [
            {id: position * 10 + 1, value: '白色'},
            {id: position * 10 + 2, value: '金色'},
        ],
    }));
    let reads = 0;
    const products = Array.from({length: 512}, (_, bits) => ({
        id: bits + 1,
        goodsNumber: 5,
        get goodsSpecificationIds() {
            reads++;
            return groups
                .map(
                    (group, position) =>
                        `${group.specificationId}:${bits & (1 << position) ? '金色' : '白色'}`
                )
                .join('|');
        },
    }));
    const index = sku.prepare(products, groups);
    assert.equal(reads, 512);
    for (let count = 0; count < 10; count++) {
        const result = sku.updateAvailability(groups, index);
        assert.equal(result[0].valueList[0].disabled, false);
    }
    assert.equal(reads, 512);
});
