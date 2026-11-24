const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');

const mini = path.resolve(__dirname, '../../apps/miniapp');
const {previewScene, readMaterials, INSTANCE_LIMIT} = require(
    path.join(mini, 'components/necklace-3d/scene.js')
);
const arrangement = require(path.join(mini, 'services/necklace-arrangement.js'));
const palette = [
    {
        type: 'pearl',
        options: [
            {id: 8, componentType: 'pearl', name: '六毫米', materialParams: '{"diameterMm":6}'},
            {id: 9, componentType: 'pearl', name: '十二毫米', materialParams: '{"diameterMm":12}'},
            {id: 10, componentType: 'pearl', name: '未测量', materialParams: '{"diameterMm":"8"}'},
        ],
    },
    {
        type: 'chain',
        options: [
            {id: 15, componentType: 'chain', name: '链条', materialParams: '{"lengthMm":400}'},
        ],
    },
    {
        type: 'clasp',
        options: [{id: 21, componentType: 'clasp', name: '扣件', materialParams: '{"spanMm":10}'}],
    },
    {
        type: 'accessory',
        options: [
            {
                id: 33,
                componentType: 'accessory',
                name: '隔珠',
                materialParams: '{"spanMm":3,"lengthRole":"INLINE"}',
            },
            {
                id: 34,
                componentType: 'accessory',
                name: '坠饰',
                materialParams: '{"spanMm":15,"lengthRole":"HANGING"}',
            },
        ],
    },
];

/** 混排保留重复实例, 真珠径按比例显示, 通用配饰采用独立结构. */
test('3D 真实配置保留材料数、相对比例、阵列顺序与不同形状', () => {
    const ids = [8, 15, 9, 33, 8, 21, 34, 10];
    const scene = previewScene(ids, palette);
    const item = (index) => scene.parts.find((part) => part.index === index);
    assert.deepEqual(scene.optionIds, ids);
    assert.deepEqual(
        [...new Set(scene.parts.map((part) => part.index))],
        ids.map((_, index) => index)
    );
    assert.equal(item(2).radius / item(0).radius, 2);
    assert.equal(item(0).radius, item(4).radius);
    assert(item(0).point[0] < item(2).point[0] && item(2).point[0] < item(3).point[0]);
    assert.equal(item(1).mesh, 'chain');
    assert(scene.parts.filter((part) => part.index === 1).length > 2);
    assert.equal(item(5).mesh, 'clasp');
    assert.equal(item(6).mesh, 'decoration');
    assert(item(6).point[1] < item(3).point[1]);
    assert.equal(item(7).mesh, 'decoration');
    assert.equal(readMaterials(ids, palette)[7].sizeMm, null);
    assert.match(scene.limitation, /1 项缺少尺寸/);
    assert(scene.parts.every((part) => scene.extent >= Math.hypot(...part.point) + part.radius));
});

/** 极端链长组合仍满足绘制预算, 超限设计可以完整使用二维. */
test('3D 几何上限没有静默裁剪业务实例', () => {
    const ids = Array.from({length: INSTANCE_LIMIT}, (_, index) => (index % 2 ? 15 : 8));
    const model = previewScene(ids, palette);
    assert.equal(new Set(model.parts.map((item) => item.index)).size, INSTANCE_LIMIT);
    assert(model.parts.length <= 144);
    assert.throws(() => previewScene([...ids, 9], palette), /二维/);
});

/** 加载现有页面方法, 只替换网络及微信事件. */
function pageFixture() {
    let page;
    const requests = [];
    const urls = {
        CustomizationQuote: '/quote',
        CustomizationSave: '/save',
        BuyAdd: '/buy',
    };
    const client = {
        request(url, payload) {
            requests.push({url, payload: JSON.parse(JSON.stringify(payload))});
            return Promise.resolve({
                code: 0,
                data:
                    url === '/quote'
                        ? {totalPrice: 200, checkoutReady: true}
                        : url === '/save'
                          ? {snapshotId: 22, version: 3, quote: {checkoutReady: true}}
                          : {checkoutId: 'fixture'},
            });
        },
    };
    vm.runInNewContext(fs.readFileSync(path.join(mini, 'pages/designer/designer.js'), 'utf8'), {
        require: (name) => {
            if (name.endsWith('/util.js')) {
                return client;
            }
            if (name.endsWith('/api.js')) {
                return urls;
            }
            return arrangement;
        },
        Page: (value) => {
            page = value;
        },
        wx: {
            getStorageSync: () => 'isolated',
            setStorageSync() {},
            showToast() {},
            navigateTo() {},
        },
    });
    page.setData = (delta) => Object.assign(page.data, delta);
    page.setData({goodsId: 50, primaryProductId: 51, componentGroups: structuredClone(palette)});
    return {page, requests};
}

/** 从真实页面路径验证 3D 无独立的报价、存储、顺序或结算分支. */
test('连续编辑和撤回后，3D 与二维顺序和保存结算输入一致', async () => {
    const {page, requests} = pageFixture();
    const original = [15, 8, 9, 8, 21];
    await page.updateArrangement(original, false);
    page.changePreview({currentTarget: {dataset: {mode: '3d'}}});
    page.pickPreview({detail: {index: 3, optionId: 33}});
    assert.equal(page.data.activeIndex, -1);
    page.pickPreview({detail: {index: 3, optionId: 8}});
    assert.equal(page.data.activeIndex, 3);
    page.selectOption({currentTarget: {dataset: {id: 9}}});
    page.setEditMode({currentTarget: {dataset: {mode: 'insert'}}});
    page.selectOption({currentTarget: {dataset: {id: 8}}});
    page.editInstance({currentTarget: {dataset: {action: 'copy'}}});
    page.editInstance({currentTarget: {dataset: {action: 'left'}}});
    page.editInstance({currentTarget: {dataset: {action: 'remove'}}});
    const arranged = Array.from(page.data.arrangementOptionIds);
    assert.equal(arranged.length, 6);
    page.undoArrangement();
    const active = Array.from(page.data.arrangementOptionIds);
    assert.equal(active.length, 7);
    assert.deepEqual(
        Array.from(page.data.arrangementNodes, (item) => item.optionId),
        active
    );
    assert.deepEqual(previewScene(active, page.data.componentGroups).optionIds, active);
    await page.refreshQuote();
    page.fallbackPreview();
    assert.equal(page.data.preview3d, false);
    await page.persistDesign(true);
    assert.deepEqual(
        requests.findLast(({url}) => url === '/quote').payload.arrangementOptionIds,
        active
    );
    assert.deepEqual(
        requests.find(({url}) => url === '/save').payload.arrangementOptionIds,
        active
    );
    assert.equal(requests.find(({url}) => url === '/buy').payload.designSnapshotId, 22);
});

/** 还原保存排列时, 重复实例及两个渲染模式的顺序保持一致. */
test('已保存的有序 ID 可直接恢复 2D 与 3D', () => {
    const stored = JSON.parse('{"arrangementOptionIds":[8,15,8,34,9,21,8]}').arrangementOptionIds;
    const nodes = arrangement.preview(
        stored,
        palette.flatMap((group) => group.options)
    );
    assert.deepEqual(
        nodes.map((node) => node.optionId),
        previewScene(stored, palette).optionIds
    );
});
