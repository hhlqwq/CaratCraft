const {test} = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');

/** 使用真实优惠券页面及读取服务, 仅替换网络和微信边界. */
async function fixture() {
    let page;
    let token = 'A';
    let backs = 0;
    const app = {globalData: {selectedCoupon: {user_coupon_id: 99}}};
    const wx = {
        getStorageSync: () => token,
        stopPullDownRefresh() {},
        navigateBack: () => {
            backs++;
        },
    };
    const shared = {module: {exports: {}}, wx};
    vm.runInNewContext(fs.readFileSync('apps/miniapp/services/page-read.js', 'utf8'), shared);
    const rows = [
        {id: 5, user_coupon_id: 11, enabled: 0, name: '不可用'},
        {id: 5, user_coupon_id: 12, enabled: 1, name: '可用'},
    ];
    const context = {
        wx,
        getApp: () => app,
        Page: (definition) => {
            page = definition;
        },
        require: (name) =>
            name.includes('page-read')
                ? shared.module.exports
                : name.includes('util')
                  ? {request: () => Promise.resolve({code: 0, data: rows})}
                  : {},
    };
    vm.runInNewContext(
        fs.readFileSync('apps/miniapp/pages/shopping/selCoupon/selCoupon.js', 'utf8'),
        context
    );
    page.setData = (values) => Object.assign(page.data, values);
    page.onLoad({buyType: 'cart'});
    await Promise.resolve();
    await Promise.resolve();
    return {
        page,
        app,
        rows,
        backs: () => backs,
        setToken: (value) => {
            token = value;
        },
    };
}

/** 同一券模板下以本人券身份选择, 不信任事件携带的金额和名称. */
test('优惠券选择以本人券ID定位服务端原件', async () => {
    const f = await fixture();
    f.page.tapCoupon({currentTarget: {dataset: {item: {id: 5, user_coupon_id: 12, name: '篡改'}}}});
    assert.equal(f.app.globalData.selectedCoupon, f.rows[1]);
    assert.equal(f.backs(), 1);
});

/** 伪造启用状态、未知身份及已换账号均不能更改结算选择. */
test('优惠券选择拒绝不可用券和旧账号事件', async () => {
    const f = await fixture();
    for (const item of [{user_coupon_id: 11, enabled: 1}, {user_coupon_id: 999}, {}]) {
        f.page.tapCoupon({currentTarget: {dataset: {item}}});
    }
    f.setToken('B');
    f.page.tapCoupon({currentTarget: {dataset: {item: f.rows[1]}}});
    assert.equal(f.backs(), 0);
    assert.equal(f.app.globalData.selectedCoupon.user_coupon_id, 99);
});

/** 主动不使用优惠券同时清空原券, 防止结算上下文残留. */
test('不使用优惠券清空原选择', async () => {
    const f = await fixture();
    f.page.noUseCoupon();
    assert.equal(f.app.globalData.userCoupon, 'NO_USE_COUPON');
    assert.equal(Object.keys(f.app.globalData.selectedCoupon).length, 0);
    assert.equal(f.backs(), 1);
});
