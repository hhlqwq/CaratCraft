/* 地址编辑使用实际共享页面, 不调用业务接口或微信. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');

/** 创建独立账号及可控制网络响应的真实编辑页. */
function fixture(id = 0) {
    const calls = [];
    const storage = {token: 'A'};
    let navigations = 0;
    const module = {exports: {}};
    vm.runInNewContext(
        fs.readFileSync(
            path.join(__dirname, '../../apps/miniapp/services/address-editor-page.js'),
            'utf8'
        ),
        {
            module,
            require: (name) =>
                name.includes('util')
                    ? {
                          request: (...args) =>
                              new Promise((resolve, reject) => calls.push({args, resolve, reject})),
                          showErrorToast() {},
                      }
                    : {AddressDetail: 'detail', AddressSave: 'save', RegionList: 'region'},
            wx: {getStorageSync: (key) => storage[key], navigateBack: () => navigations++},
        }
    );
    const page = module.exports.createAddressEditorPage();
    page.setData = (values) => Object.assign(page.data, values);
    page.onLoad({id});
    return {page, calls, storage, navigations: () => navigations};
}

/** 生成符合服务端名称契约且没有旧地区ID的地址. */
function address(id = 0) {
    return {
        id,
        userName: '测试',
        telNumber: '13800138000',
        provinceName: '浙江省',
        cityName: '杭州市',
        countyName: '西湖区',
        detailInfo: '测试地址',
        isDefault: 1,
    };
}

/** 等待由生命周期发起的读取完成. */
async function settle() {
    await new Promise((resolve) => setImmediate(resolve));
}

/** 新建不读取不存在的详情, 未填完整时不提交. */
test('新建地址空白校验及零ID不读取', async () => {
    const {page, calls} = fixture();
    assert.equal(calls.length, 0);
    await page.saveAddress();
    assert.equal(calls.length, 0);
    assert.equal(page.data.saving, false);
});

/** 编辑直接恢复服务端字段, 保存无需不存在的districtId且只发送一次. */
test('已有地址名称契约及保存防重', async () => {
    const {page, calls, navigations} = fixture(7);
    calls[0].resolve({code: 0, data: address(7)});
    await settle();
    assert.equal(page.data.address.fullRegion, '浙江省杭州市西湖区');
    const save = page.saveAddress();
    await page.saveAddress();
    assert.equal(calls.length, 2);
    assert.equal(calls[1].args[1].provinceName, '浙江省');
    assert.equal(calls[1].args[1].isDefault, true);
    assert.equal('provinceId' in calls[1].args[1], false);
    calls[1].resolve({code: 0});
    await save;
    assert.equal(navigations(), 1);
});

/** 详情失败阻断空白覆盖, 显式重试后可编辑. */
test('详情加载失败后重试恢复', async () => {
    const {page, calls} = fixture(7);
    calls[0].reject(new Error('offline'));
    await settle();
    assert.ok(page.data.loadError);
    await page.saveAddress();
    assert.equal(calls.length, 1);
    const retry = page.getAddressDetail();
    calls[1].resolve({code: 0, data: address(7)});
    await retry;
    assert.equal(page.data.loadError, '');
    assert.equal(page.data.loading, false);
});

/** 未确认的创建保留输入并要求回列表核对, 不自动或重复提交. */
test('保存结果不明保留输入且禁止重复创建', async () => {
    const {page, calls} = fixture();
    page.setData({address: address()});
    const save = page.saveAddress();
    calls[0].reject(new Error('timeout'));
    await save;
    assert.equal(page.data.address.detailInfo, '测试地址');
    assert.equal(page.data.saveUncertain, true);
    assert.equal(page.data.saving, false);
    await page.saveAddress();
    assert.equal(calls.length, 1);
});

/** 旧账号或离页后的写入成功均不得跳转新页面. */
test('保存回调账号及生命周期隔离', async () => {
    for (const leave of [false, true]) {
        const {page, calls, storage, navigations} = fixture();
        page.setData({address: address()});
        const save = page.saveAddress();
        if (leave) {
            page.onHide();
            page.onShow();
        } else {
            storage.token = 'B';
        }
        calls[0].resolve({code: 0});
        await save;
        assert.equal(navigations(), 0);
        if (leave) {
            assert.equal(page.data.saveUncertain, true);
        }
    }
});

/** 地区响应乱序和取消不会覆盖当前列表, 失败支持当前父级重试. */
test('地区读取乱序取消及重试', async () => {
    const {page, calls} = fixture();
    page.chooseRegion();
    const newer = page.getRegionList(1);
    calls[1].resolve({code: 0, data: [{id: 3, name: '新省', type: 1}]});
    await newer;
    calls[0].resolve({code: 0, data: [{id: 2, name: '旧省', type: 1}]});
    await settle();
    assert.equal(page.data.regionList[0].id, 3);
    const failure = page.getRegionList(1);
    calls[2].reject(new Error('offline'));
    await failure;
    assert.ok(page.data.regionError);
    page.retryRegions();
    page.cancelSelectRegion();
    calls[3].resolve({code: 0, data: [{id: 4, name: '取消省', type: 1}]});
    await settle();
    assert.equal(page.data.regionList.length, 0);
    assert.equal(page.data.regionLoading, false);
});

/** 完整省市区选择一次应用名称, 切换父级使下级失效. */
test('地区层级选择和取消保留原地址', async () => {
    const {page, calls} = fixture();
    page.setData({address: address()});
    page.chooseRegion();
    for (let type = 1; type <= 3; type++) {
        calls[type - 1].resolve({code: 0, data: [{id: type + 10, type, name: '地区' + type}]});
        await settle();
        page.selectRegion({currentTarget: {dataset: {regionIndex: 0}}});
    }
    assert.equal(page.data.selectRegionDone, true);
    page.doneSelectRegion();
    assert.equal(page.data.address.provinceName, '地区1');
    assert.equal(page.data.address.countyName, '地区3');
    page.chooseRegion();
    page.cancelSelectRegion();
    assert.equal(page.data.address.provinceName, '地区1');
});
