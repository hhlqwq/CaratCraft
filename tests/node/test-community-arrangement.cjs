const assert = require('node:assert/strict');
const {test} = require('node:test');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const arrangement = require('../../apps/miniapp/services/necklace-arrangement.js');
const root = path.resolve(__dirname, '../../apps/miniapp');

/** 隔离微信运行环境, 控制请求返回顺序和登录身份. */
function pageFixture(relative, dependencies, wx = {}) {
    let page;
    vm.runInNewContext(fs.readFileSync(path.join(root, relative), 'utf8'), {
        require: (name) => {
            if (!(name in dependencies)) {
                throw new Error(name);
            }
            return dependencies[name];
        },
        Page: (definition) => {
            page = definition;
        },
        wx,
    });
    page.setData = (values) => Object.assign(page.data, values);
    return page;
}

/** 重复材料实例独立编辑, 撤回所需原数组保持不变. */
test('重复材料插入复制调序删除保持实例顺序', () => {
    const original = [1, 2];
    const added = arrangement.edit(original, 'insert', 0, 1);
    assert.deepEqual(added, [1, 1, 2]);
    assert.deepEqual(original, [1, 2]);
    assert.deepEqual(arrangement.edit(added, 'replace', 1, 7), [1, 7, 2]);
    assert.deepEqual(added, [1, 1, 2]);
    assert.throws(() => arrangement.edit(added, 'replace', -1, 7), /选中/);
    assert.deepEqual(arrangement.edit(added, 'copy', 1), [1, 1, 1, 2]);
    assert.deepEqual(arrangement.edit(added, 'right', 1), [1, 2, 1]);
    assert.deepEqual(arrangement.edit(added, 'remove', 0), [1, 2]);
    assert.deepEqual(arrangement.edit(added, 'clear', 0), []);
    assert.throws(() => arrangement.edit(Array(300).fill(1), 'insert', 0, 1), /300/);
});

/** 相对直径由实测值决定, 不将缺失规格变成实际尺寸. */
test('不同尺寸珍珠按相同比例展示, 缺失尺寸明确未知', () => {
    const nodes = arrangement.preview(
        [1, 2, 3],
        [
            {id: 1, name: '小珠', componentType: 'pearl', materialParams: '{"diameterMm":5}'},
            {id: 2, name: '大珠', componentType: 'pearl', materialParams: '{"diameterMm":10}'},
            {id: 3, name: '规格未定', componentType: 'pearl', materialParams: '{}'},
        ]
    );
    assert.equal(nodes[1].diameter / nodes[0].diameter, 2);
    assert.equal(nodes[2].known, false);
    assert.equal(nodes[2].sizeMm, null);
    assert.ok(nodes.every((node) => Number.isFinite(node.x) && Number.isFinite(node.y)));
});

/** 清空可撤回, 报价请求包含重复实例而不退回单选模型. */
test('编辑器清空撤回恢复完整排列并发送有序报价', async () => {
    const requests = [];
    const page = pageFixture(
        'pages/designer/designer.js',
        {
            '../../utils/util.js': {
                request: (url, data) => {
                    requests.push(data);
                    return Promise.resolve({code: 0, data: {totalPrice: 10}});
                },
            },
            '../../config/api.js': {},
            '../../services/necklace-arrangement.js': arrangement,
        },
        {showModal: (options) => options.success({confirm: true})}
    );
    page.data.componentGroups = [{options: [{id: 1, name: '珍珠', componentType: 'pearl'}]}];
    await page.updateArrangement([1, 1], false);
    page.editInstance({currentTarget: {dataset: {action: 'clear'}}});
    assert.equal(page.data.arrangementOptionIds.length, 0);
    page.undoArrangement();
    assert.deepEqual(Array.from(page.data.arrangementOptionIds), [1, 1]);
    assert.deepEqual(Array.from(requests.at(-1).arrangementOptionIds), [1, 1]);
    assert.equal(requests.at(-1).optionIds, undefined);
    page.setData({activeIndex: 0, copyCount: 5});
    page.editInstance({currentTarget: {dataset: {action: 'copy'}}});
    assert.equal(page.data.arrangementOptionIds.length, 7);
    page.undoArrangement();
    assert.deepEqual(Array.from(page.data.arrangementOptionIds), [1, 1]);
});

/** 草稿详情切换与配置切换使用同一代次边界, 防止恢复旧设计覆盖新设计. */
test('连续打开草稿时迟到详情不能覆盖最新草稿', async () => {
    const requests = [];
    const page = pageFixture('pages/designer/designer.js', {
        '../../utils/util.js': {
            request: (url, data) => new Promise((resolve) => requests.push({url, data, resolve})),
        },
        '../../config/api.js': {
            CustomizationDetail: '/detail',
            CustomizationConfig: '/config',
            CustomizationQuote: '/quote',
        },
        '../../services/necklace-arrangement.js': arrangement,
    });
    const first = page.loadDesignSnapshot(1);
    const second = page.loadDesignSnapshot(2);
    requests[1].resolve({
        code: 0,
        data: {
            id: 2,
            goodsId: 20,
            version: 3,
            status: 'DRAFT',
            configurationJson: '{"arrangementOptionIds":[7,7]}',
        },
    });
    requests[0].resolve({
        code: 0,
        data: {id: 1, goodsId: 10, version: 0, status: 'DRAFT', configurationJson: '{}'},
    });
    await first;
    assert.equal(page.data.draftId, 2);
    assert.equal(page.data.goodsId, 20);
    requests[2].resolve({
        code: 0,
        data: {
            product: {requiredComponentTypes: 'pearl'},
            options: [{id: 7, name: '珍珠', componentType: 'pearl'}],
            primaryProductId: 22,
        },
    });
    await second;
    assert.deepEqual(Array.from(page.data.arrangementOptionIds), [7, 7]);
    assert.equal(page.data.draftVersion, 3);
    requests[3].resolve({code: 0, data: {totalPrice: 100}});
});

/** 社区分类切换后的旧响应不能污染新信息流. */
test('社区分类切换隔离迟到响应', async () => {
    const pending = [];
    const page = pageFixture(
        'pages/community/index.js',
        {
            '../../services/community.js': {
                request: () => new Promise((resolve) => pending.push(resolve)),
                present: (item) => item,
            },
        },
        {getStorageSync: () => '', stopPullDownRefresh: () => {}}
    );
    const first = page.load(true);
    page.data.kind = 'DESIGN';
    const second = page.load(true);
    pending[1]({records: [{id: 2}], total: 1});
    await second;
    pending[0]({records: [{id: 1}], total: 1});
    await first;
    assert.equal(page.data.records[0].id, 2);
});

/** 失败分页不前移页码, 下一次仍请求相同页. */
test('社区分页失败可以重试同一页', async () => {
    const pages = [];
    const page = pageFixture(
        'pages/community/index.js',
        {
            '../../services/community.js': {
                request: (route, data) => {
                    pages.push(data.page);
                    return Promise.reject(new Error('offline'));
                },
                present: (item) => item,
            },
        },
        {getStorageSync: () => '', stopPullDownRefresh: () => {}}
    );
    Object.assign(page.data, {page: 1, records: [{id: 1}], total: 21});
    await page.load(false);
    await page.load(false);
    assert.deepEqual(pages, [2, 2]);
    assert.equal(page.data.page, 1);
});

/** 网络失败后正文和图片不能丢失, 重试使用同一发布键. */
test('社区发布失败保留内容并沿用幂等键', async () => {
    const keys = [];
    const page = pageFixture('pages/community/publish.js', {
        '../../services/community.js': {
            loggedIn: () => true,
            request: (route, data) => {
                keys.push(data.requestKey);
                return Promise.reject(new Error('offline'));
            },
        },
        '../../utils/util.js': {},
        '../../config/api.js': {},
    });
    Object.assign(page.data, {
        kindIndex: 2,
        title: '建议',
        content: '希望添加新材料',
        images: [{id: 7}],
    });
    page._requestKey = 'stable_publish_request';
    page.submit();
    await new Promise((resolve) => setImmediate(resolve));
    page.submit();
    await new Promise((resolve) => setImmediate(resolve));
    assert.deepEqual(keys, ['stable_publish_request', 'stable_publish_request']);
    assert.equal(page.data.content, '希望添加新材料');
    assert.equal(page.data.images[0].id, 7);
    assert.equal(page.data.saving, false);
});

/** 社区导航替换后购物车仍可从个人中心和商品页普通导航打开. */
test('四板块导航和购物车路由保持一致', () => {
    const app = JSON.parse(fs.readFileSync(path.join(root, 'app.json'), 'utf8'));
    assert.deepEqual(
        app.tabBar.list.map((item) => item.text),
        ['商城', 'DIY', '社区', '我的']
    );
    assert.ok(app.pages.includes('pages/cart/cart'));
    assert.ok(
        fs
            .readFileSync(path.join(root, 'pages/ucenter/index/index.js'), 'utf8')
            .includes("url: '/pages/cart/cart'")
    );
    assert.match(
        fs.readFileSync(path.join(root, 'pages/goods/goods.js'), 'utf8'),
        /wx\.navigateTo\(\{\s*url: '\/pages\/cart\/cart'/
    );
});

/** 社区二维示意只从公开副本重建, 不需要原草稿或内部材料ID. */
test('公开设计副本可以独立生成尺寸比例示意', () => {
    const module = {exports: {}};
    vm.runInNewContext(fs.readFileSync(path.join(root, 'services/community.js'), 'utf8'), {
        module,
        require: (name) =>
            name === './necklace-arrangement.js'
                ? arrangement
                : name === '../config/api.js'
                  ? {ApiOrigin: 'https://merchant.example'}
                  : {},
    });
    const post = module.exports.present({
        designJson: JSON.stringify({
            arrangement: [
                {position: 0, componentType: 'pearl', name: '小珠', sizeMm: 5},
                {position: 1, componentType: 'pearl', name: '大珠', sizeMm: 10},
            ],
        }),
        imagesJson: '["/upload/owned.png"]',
    });
    assert.equal(post.publicNodes.length, 2);
    assert.equal(post.publicNodes[1].diameter / post.publicNodes[0].diameter, 2);
    assert.equal(post.images[0], 'https://merchant.example/upload/owned.png');
});
