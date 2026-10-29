/* 地址编辑器实际脚本回归, 控制网络顺序而不使用真实用户资料. */
const {test} = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');

/** 返回可控异步结果, 模拟请求乱序和失败. */
function deferred() {
    let resolve;
    let reject;
    const promise = new Promise((yes, no) => {
        resolve = yes;
        reject = no;
    });
    return {promise, resolve, reject};
}

/** 加载实际共享脚本, 仅替换请求和微信导航边界. */
function fixture() {
    const storage = {token: 'A'};
    const calls = [];
    let handler = () => Promise.resolve({code: 0, data: []});
    let navigations = 0;
    const context = {
        module: {exports: {}},
        wx: {
            getStorageSync: (key) => storage[key],
            navigateBack: () => navigations++,
        },
        require: (name) =>
            name.includes('util')
                ? {
                      request: (...args) => {
                          calls.push(args);
                          return handler(...args);
                      },
                  }
                : {AddressDetail: 'detail', AddressSave: 'save', RegionList: 'region'},
    };
    vm.runInNewContext(fs.readFileSync('apps/miniapp/services/address-editor.js', 'utf8'), context);
    const create = context.module.exports.createAddressEditor;
    const page = create();
    page.setData = (values) => Object.assign(page.data, values);
    return {
        page,
        calls,
        storage,
        create,
        setHandler: (next) => {
            handler = next;
        },
        navigationCount: () => navigations,
    };
}

/** 合成当前唯一地址字段, 不依赖省市区内部ID恢复. */
function address() {
    return {
        id: 8,
        userName: '隔离用户',
        provinceName: '隔离省',
        cityName: '隔离市',
        countyName: '隔离区',
        detailInfo: '隔离地址',
        telNumber: '13800000000',
        isDefault: 1,
    };
}

/** 无参数进入新增页不请求未定义地址标识. */
test('地址新增初始化与实例数据独立', () => {
    const f = fixture();
    f.page.onLoad({});
    assert.equal(f.calls.filter(([url]) => url === 'detail').length, 0);
    f.page.data.address.userName = '本实例';
    assert.equal(f.create().data.address.userName, '');
});

/** 已有地址按规范字段保存, 不需要重新选择地区或发送旧字段. */
test('已有地址读取后可以按唯一字段再次保存', async () => {
    const f = fixture();
    f.setHandler((url) => Promise.resolve({code: 0, data: url === 'detail' ? address() : []}));
    f.page.data.addressId = 8;
    await f.page.getAddressDetail();
    assert.equal(await f.page.saveAddress(), true);
    const body = f.calls.find(([url]) => url === 'save')[1];
    assert.equal(body.provinceName, '隔离省');
    assert.equal(body.isDefault, true);
    assert.equal('province_name' in body, false);
    assert.equal('districtId' in body, false);
    assert.equal(f.navigationCount(), 1);
});

/** 写入去重, 网络失败保留输入并释放锁供显式重试. */
test('地址保存防重及失败恢复', async () => {
    const f = fixture();
    f.page.data.address = address();
    const pending = deferred();
    f.setHandler(() => pending.promise);
    const first = f.page.saveAddress();
    await f.page.saveAddress();
    assert.equal(f.calls.length, 1);
    pending.reject(new Error('隔离失败'));
    await first;
    assert.equal(f.page.data.saving, false);
    assert.equal(f.page.data.address.detailInfo, '隔离地址');
    assert.match(f.page.data.saveError, /隔离失败/);
    f.setHandler(() => Promise.resolve({code: 0}));
    assert.equal(await f.page.saveAddress(), true);
});

/** 超过真实字段上限的输入不发送网络写入. */
test('地址校验与持久化长度上限一致', async () => {
    const f = fixture();
    f.page.data.address = {...address(), detailInfo: '测'.repeat(121)};
    assert.equal(await f.page.saveAddress(), false);
    assert.equal(f.calls.length, 0);
});

/** 账号切换后废弃旧地址读取, 不留下前一账号私人输入. */
test('地址读取隔离账号切换', async () => {
    const f = fixture();
    f.page._sessionToken = 'A';
    f.page.data.addressId = 8;
    const pending = deferred();
    f.setHandler(() => pending.promise);
    const read = f.page.getAddressDetail();
    f.storage.token = 'B';
    f.page.onShow();
    pending.resolve({code: 0, data: address()});
    await read;
    assert.equal(f.page.data.address.userName, '');
});

/** 离开页面后旧保存成功不能触发新的页面导航. */
test('地址保存隔离离页回调', async () => {
    const f = fixture();
    f.page.data.address = address();
    const pending = deferred();
    f.setHandler(() => pending.promise);
    const save = f.page.saveAddress();
    f.page.onUnload();
    pending.resolve({code: 0});
    await save;
    assert.equal(f.navigationCount(), 0);
});

/** 地区快速切换时旧响应不能覆盖新层级内容. */
test('地区读取拒绝乱序覆盖', async () => {
    const f = fixture();
    const old = deferred();
    const latest = deferred();
    f.setHandler((url, data) => (data.parentId === 1 ? old.promise : latest.promise));
    const a = f.page.getRegionList(1);
    const b = f.page.getRegionList(2);
    latest.resolve({code: 0, data: [{id: 2, type: 1, name: '新地区'}]});
    await b;
    old.resolve({code: 0, data: [{id: 1, type: 1, name: '旧地区'}]});
    await a;
    assert.equal(f.page.data.regionList[0].name, '新地区');
});

/** 详情读取失败时禁止保存空表单, 显式重试成功才解除保护. */
test('地址读取失败必须明确重试', async () => {
    const f = fixture();
    f.page.data.addressId = 8;
    f.setHandler(() => Promise.reject(new Error('读取失败')));
    await f.page.getAddressDetail();
    assert.equal(await f.page.saveAddress(), false);
    f.setHandler(() => Promise.resolve({code: 0, data: address()}));
    await f.page.getAddressDetail();
    assert.equal(f.page.data.detailError, '');
    assert.equal(f.page.data.address.userName, '隔离用户');
});
