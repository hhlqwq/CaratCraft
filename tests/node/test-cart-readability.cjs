/* 购物车重构的可观察行为回归, 不访问微信、网络或业务数据库. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');

/** 提供可控制响应顺序的页面, 只实现微信setData同步修改数据的行为. */
function fixture() {
    let definition;
    let token = 'first-account';
    const requests = [];
    const navigation = [];
    const api = {
        CartList: '/list',
        CartChecked: '/checked',
        CartUpdate: '/update',
        CartDelete: '/delete',
    };
    const util = {
        request: (...args) =>
            new Promise((resolve, reject) => requests.push({args, resolve, reject})),
    };
    vm.runInNewContext(
        fs.readFileSync(path.join(__dirname, '../../apps/miniapp/pages/cart/cart.js'), 'utf8'),
        {
            require: (name) => {
                if (name === '../../config/api.js') {
                    return api;
                }
                if (name === '../../utils/util.js') {
                    return util;
                }
                throw new Error('测试缺少依赖: ' + name);
            },
            Page: (value) => {
                definition = value;
            },
            wx: {
                getStorageSync: () => token,
                navigateTo: (options) => navigation.push(options.url),
            },
        }
    );
    const page = {...definition, data: structuredClone(definition.data)};
    page.setData = (values) => Object.assign(page.data, values);
    return {
        page,
        requests,
        navigation,
        changeAccount: () => {
            token = 'second-account';
        },
    };
}

/** 返回两行结算状态不同的合成商品, 数字checked与真实接口一致. */
function response(
    rows = [
        {id: 1, productId: 21, goodsId: 11, number: 2, checked: 1},
        {id: 2, productId: 22, goodsId: 12, number: 1, checked: 0},
    ]
) {
    return {
        code: 0,
        data: {cartList: rows, cartTotal: {checkedGoodsCount: 2, checkedGoodsAmount: '20.00'}},
    };
}

/** 打开页面并完成首次列表, 后续测试只观察用户动作. */
async function ready() {
    const context = fixture();
    const loaded = context.page.onShow();
    context.requests[0].resolve(response());
    await loaded;
    return context;
}

/** currentTarget必须取自绑定控件, 内部子元素不负责携带索引. */
function event(index) {
    return {currentTarget: {dataset: {itemIndex: index}}, target: {dataset: {}}};
}

/** 删除选择不改变结算勾选, 数量和全选状态由当前模式统一计算. */
test('编辑选择与服务端结算选择独立', async () => {
    const {page, requests, navigation} = await ready();
    assert.equal(page.data.selectedCount, 2);
    page.editCart();
    assert.equal(page.data.selectedCount, 0);
    page.checkedItem(event(1));
    assert.equal(page.data.selectedCount, 1);
    assert.equal(page.data.cartGoods[0].checked, true);
    assert.equal(page.data.cartGoods[1].checked, false);
    page.checkoutOrder();
    assert.equal(navigation.length, 0);
    assert.equal(requests.length, 1);
    page.editCart();
    assert.equal(page.data.selectedCount, 2);
    page.checkoutOrder();
    assert.equal(navigation.length, 1);
});

/** 数量修改必须使用JSON, 等待服务端成功且保持已有删除选择. */
test('数量写入串行且使用成功响应刷新统计', async () => {
    const {page, requests} = await ready();
    page.editCart();
    page.checkedItem(event(0));
    const updated = page.addNumber(event(0));
    await page.addNumber(event(0));
    assert.equal(requests.length, 2);
    assert.deepEqual(Array.from(requests[1].args).slice(2), ['POST', 'application/json']);
    assert.deepEqual(JSON.parse(JSON.stringify(requests[1].args[1])), {
        productId: 21,
        goodsId: 11,
        number: 3,
        id: 1,
    });
    assert.equal(page.data.cartGoods[0].number, 2);
    requests[1].resolve(response([{id: 1, productId: 21, goodsId: 11, number: 3, checked: 1}]));
    await updated;
    assert.equal(page.data.cartGoods[0].number, 3);
    assert.equal(page.data.cartGoods[0].editChecked, true);
    assert.equal(page.data.selectedCount, 3);
    assert.equal(page.data.checkedAllStatus, true);
    assert.equal(page.data.saving, false);
});

/** 库存失败不伪造本地成功, 拒绝Promise也必须结束忙碌状态. */
test('库存或网络失败保留原数量并允许重试', async () => {
    const {page, requests} = await ready();
    page.editCart();
    const failed = page.addNumber(event(0));
    requests[1].resolve({code: 400, msg: '库存不足'});
    await failed;
    assert.equal(page.data.cartGoods[0].number, 2);
    assert.equal(page.data.error, '库存不足');
    assert.equal(page.data.saving, false);
    const offline = page.addNumber(event(0));
    requests[2].reject(new Error('offline'));
    await offline;
    assert.equal(page.data.cartGoods[0].number, 2);
    assert.equal(page.data.error, 'offline');
    assert.equal(page.data.saving, false);
});

/** 最小和最大数量不发请求, 绑定索引越界也不误改其他行. */
test('数量边界和无效事件不发请求', async () => {
    const {page, requests} = await ready();
    page.editCart();
    page.cutNumber(event(1));
    page.addNumber(event(-1));
    page.addNumber(event(9));
    page.applyCart(response([{id: 1, productId: 21, number: 9999, checked: 1}]).data);
    page.addNumber(event(0));
    assert.equal(requests.length, 1);
});

/** 只发送删除选择对应的规格, 未选择时不将空集合发送服务端. */
test('删除仅包含编辑选中项并使用剩余列表', async () => {
    const {page, requests} = await ready();
    page.editCart();
    page.deleteCart();
    assert.equal(requests.length, 1);
    page.checkedItem(event(1));
    const deleted = page.deleteCart();
    assert.equal(requests[1].args[0], '/delete');
    assert.equal(requests[1].args[1].productIds, '22');
    requests[1].resolve(response([response().data.cartList[0]]));
    await deleted;
    assert.equal(page.data.cartGoods.length, 1);
    assert.equal(page.data.selectedCount, 0);
    page.editCart();
    assert.equal(page.data.selectedCount, 2);
});

/** 离开与重入产生独立代次, 旧响应不回填敏感列表或结束新请求的加载提示. */
test('离页后迟到响应不覆盖新一轮请求', async () => {
    const {page, requests, changeAccount} = fixture();
    const old = page.onShow();
    page.onHide();
    changeAccount();
    assert.equal(page.data.cartGoods.length, 0);
    const fresh = page.onShow();
    requests[0].resolve(response());
    await old;
    assert.equal(page.data.cartGoods.length, 0);
    assert.equal(page.data.loading, true);
    requests[1].resolve(response([]));
    await fresh;
    assert.equal(page.data.cartGoods.length, 0);
    assert.equal(page.data.checkedAllStatus, false);
    assert.equal(page.data.loading, false);
});

/** 账号变化即使没有先触发离页, 旧令牌响应也不能写入页面. */
test('令牌变化使旧账号响应失效', async () => {
    const {page, requests, changeAccount} = fixture();
    const loaded = page.onShow();
    changeAccount();
    requests[0].resolve(response());
    await loaded;
    assert.equal(page.data.cartGoods.length, 0);
});

/** 服务端勾选使用数字1/0, 空购物车不显示为全选状态. */
test('普通全选使用服务端状态并可恢复', async () => {
    const {page, requests} = await ready();
    const selected = page.checkedAll();
    assert.equal(requests[1].args[1].isChecked, 1);
    requests[1].resolve(response(response().data.cartList.map((row) => ({...row, checked: 1}))));
    await selected;
    assert.equal(page.data.checkedAllStatus, true);
    const unselected = page.checkedAll();
    assert.equal(requests[2].args[1].isChecked, 0);
    requests[2].resolve(response(response().data.cartList.map((row) => ({...row, checked: 0}))));
    await unselected;
    assert.equal(page.data.selectedCount, 0);
    assert.equal(page.data.checkedAllStatus, false);
});
