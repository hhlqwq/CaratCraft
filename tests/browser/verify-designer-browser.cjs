/* 使用官方 WXML 编译结果和实际页面脚本验证点击流程, 微信接口和服务端响应采用隔离替身. */
const assert = require('node:assert/strict');
const {execFileSync} = require('node:child_process');
const fs = require('node:fs');
const path = require('node:path');

const project = path.resolve(__dirname, '../../apps/miniapp');
const route = './pages/designer/designer.wxml';

/** 将官方编译的节点映射成可点击的页面, 不修改模板条件或事件绑定. */
function mountBrowserFixture({designer, arrangement, scripts}) {
    const {document} = globalThis;
    const status = document.querySelector('#status');
    const root = document.querySelector('#editor');
    const renderWxml = globalThis.$gwx('./pages/designer/designer.wxml');
    const values = new Map([['token', 'isolated-design-owner']]);
    const options = [
        {id: 1, componentType: 'pearl', name: '测试珍珠 6 mm', materialParams: '{"diameterMm":6}'},
        {
            id: 2,
            componentType: 'pearl',
            name: '测试珍珠 8 mm',
            priceDelta: 20,
            materialParams: '{"diameterMm":8}',
        },
        {
            id: 3,
            componentType: 'accessory',
            name: '测试配饰',
            priceDelta: 10,
            materialParams: '{"spanMm":2}',
        },
        {id: 4, componentType: 'chain', name: '测试链条', materialParams: '{"lengthMm":440}'},
    ];
    const loaded = new Map();
    /** 在浏览器装配正式 WebGL 源码, 微信 Canvas 接口由浏览器 Canvas 承载. */
    function loadRenderer(name) {
        if (!loaded.has(name)) {
            const module = {exports: {}};
            loaded.set(name, module);
            new Function('module', 'require', scripts[name])(module, loadRenderer);
        }
        return loaded.get(name).exports;
    }
    let renderer;
    const canvas = document.createElement('canvas');
    canvas.style.cssText = 'width:100%;height:265px;display:block;touch-action:none';
    let snapshot;
    let purchase;
    let page;

    /** 给编辑器提供固定测试目录, 缺货开关模拟报价失败后恢复. */
    function request(endpoint, payload) {
        switch (endpoint) {
            case 'CustomizationStyles':
                return Promise.resolve({code: 0, data: [{goodsId: 77, name: '隔离测试项链'}]});
            case 'CustomizationConfig':
                assertGoods(payload);
                return Promise.resolve({
                    code: 0,
                    data: {
                        product: {requiredComponentTypes: 'pearl,chain'},
                        options,
                        primaryProductId: 78,
                    },
                });
            case 'CustomizationQuote':
                if (values.get('outOfStock')) {
                    return Promise.resolve({code: 1, msg: '测试配饰库存不足'});
                }
                return Promise.resolve({code: 0, data: quote(payload)});
            case 'CustomizationSave':
                snapshot = {
                    id: 79,
                    goodsId: 77,
                    version: 0,
                    status: 'DRAFT',
                    configurationJson: JSON.stringify(payload),
                };
                return Promise.resolve({
                    code: 0,
                    data: {
                        snapshotId: snapshot.id,
                        version: snapshot.version,
                        quote: values.get('outOfStock') ? {checkoutReady: false} : quote(payload),
                    },
                });
            case 'CustomizationDetail':
                return Promise.resolve({code: 0, data: snapshot});
            case 'BuyAdd':
                purchase = payload;
                return Promise.resolve({code: 0, data: {checkoutId: 'isolated-checkout'}});
            default:
                throw new Error('未声明的测试接口 ' + endpoint);
        }
    }

    /** 记录本次测试商品, 避免旧的固定演示编号被无意使用. */
    function assertGoods(payload) {
        if (payload.goodsId !== 77) {
            throw new Error('错误的商品编号');
        }
    }

    /** 用固定尺寸和价格匹配测试任务, 服务端工艺由Java用例验证. */
    function quote(payload) {
        const ready = payload.targetLengthMm === 450 && payload.arrangementOptionIds.includes(2);
        const totalPrice = payload.arrangementOptionIds.reduce(
            (total, id) => total + (options.find((item) => item.id === id).priceDelta || 0),
            399
        );
        return {
            totalPrice,
            checkoutReady: ready,
            sizing: {ready, estimatedFinishedLengthMm: 450, notice: ready ? '' : '请填写尺寸'},
        };
    }

    const wx = {
        getStorageSync: (key) => values.get(key),
        setStorageSync: (key, value) => values.set(key, value),
        removeStorageSync: (key) => values.delete(key),
        showToast: ({title}) => (status.textContent = title),
        navigateTo: ({url}) => (status.textContent = url),
        showModal: ({title, success}) => {
            status.textContent = title;
            success({confirm: false});
        },
    };

    /** 以真实节点的 data-* 属性调用真实页面回调. */
    function invoke(element, attr, event, handler) {
        const dataset = {};
        for (const [key, value] of Object.entries(attr)) {
            if (key.startsWith('data-')) {
                dataset[key.slice(5)] = value;
            }
        }
        element.addEventListener(event, () => {
            page[handler]({currentTarget: {dataset}, detail: {value: element.value}});
        });
    }

    /** 挂载正式渲染器, 使用真实页面状态绘制并经真实拾取事件更新选中项. */
    function drawPreview() {
        if (!canvas.isConnected) {
            renderer?.dispose();
            renderer = null;
            return;
        }
        if (!renderer) {
            renderer = loadRenderer('./renderer.js').createRenderer(canvas);
        }
        renderer.fit(canvas.clientWidth, canvas.clientHeight, 1);
        const state = page.data;
        const materialScene = loadRenderer('./scene.js').previewScene(
            state.arrangementOptionIds, state.componentGroups, state.lengthRules,
            {spacingMm: state.layoutSpacingMm, offsetMm: state.layoutOffsetMm}
        );
        renderer.setScene(materialScene);
        renderer.setSelection(state.activeIndex);
        renderer.draw();
        canvas.onclick = (event) => {
            const index = renderer.select(2 * event.offsetX / canvas.clientWidth - 1,
                1 - 2 * event.offsetY / canvas.clientHeight);
            if (index >= 0) {
                page.pickPreview({detail: {index, optionId: state.arrangementOptionIds[index]}});
            }
        };
    }

    /** 适配浏览器的标签、表单状态和 rpx, 模板文字与显隐由 WCC 返回. */
    function renderNode(node) {
        if (typeof node !== 'object') {
            return document.createTextNode(String(node));
        }
        if (node.tag === 'wx-necklace-3d' || node.tag === 'necklace-3d') return canvas;
        const tags = {
            'wx-button': 'button',
            'wx-image': 'img',
            'wx-input': 'input',
            'wx-text': 'span',
        };
        const element = document.createElement(tags[node.tag] || 'div');
        const attr = node.attr || {};
        for (const [key, value] of Object.entries(attr)) {
            if (key === 'bindtap' || key === 'bindinput') {
                invoke(element, attr, key === 'bindtap' ? 'click' : 'input', value);
            } else if (key === 'disabled') {
                element.disabled = Boolean(value);
            } else if (
                ['class', 'style', 'placeholder', 'src', 'type', 'maxlength'].includes(key)
            ) {
                element.setAttribute(key, String(value));
            } else if (key === 'value') {
                element.value = value;
            }
        }
        for (const child of node.children || []) {
            element.append(renderNode(child));
        }
        return element;
    }

    /** 从 Page 定义创建新的页面实例, 重开草稿走原有生命周期. */
    function open(snapshotId) {
        status.textContent = '';
        if (page) {
            page.onUnload();
        }
        const shared = {exports: {}};
        new Function('module', arrangement)(shared);
        new Function('require', 'wx', 'Page', designer)(
            (name) => {
                if (name.endsWith('necklace-arrangement.js')) {
                    return shared.exports;
                }
                if (name.endsWith('api.js')) {
                    return new Proxy({}, {get: (unused, key) => key});
                }
                return {request};
            },
            wx,
            (definition) => {
                page = definition;
            }
        );
        let pending = false;
        page.setData = (changes) => {
            Object.assign(page.data, changes);
            if (!pending) {
                pending = true;
                globalThis.queueMicrotask(() => {
                    pending = false;
                    root.replaceChildren(renderNode(renderWxml(page.data, {})));
                    drawPreview();
                });
            }
        };
        page.onLoad(snapshotId ? {snapshotId} : {});
    }

    globalThis.designerFixture = {
        open,
        getOrder: () => page.data.arrangementOptionIds,
        setStockFailure: (failing) => {
            values.set('outOfStock', failing);
            return page.refreshQuote();
        },
        purchase: () => purchase,
    };
    open();
}

/** 在窄屏浏览器走完选择、替换、添加、撤回、保存、重开与下一步. */
async function main() {
    const tools = process.env.CARATCRAFT_WECHAT_DEVTOOLS;
    const evidence = process.env.CARATCRAFT_BROWSER_EVIDENCE;
    if (!tools || !evidence || !fs.statSync(evidence).isDirectory()) {
        throw new Error('请配置已安装的微信开发者工具和现有测试证据目录');
    }
    const {chromium} = require(
        require.resolve('playwright', {
            paths: [process.env.CARATCRAFT_NODE_MODULES || process.cwd()],
        })
    );
    const compiled = path.join(evidence, 'stage3-designer-wxml.js');
    execFileSync(
        path.join(tools, 'resources/app.asar.unpacked/node_modules/wcc-exec/wcc.exe'),
        ['-o', compiled, route],
        {cwd: project}
    );
    const browser = await chromium.launch({channel: 'msedge', headless: true, args: ['--enable-unsafe-swiftshader']});
    try {
        const page = await browser.newPage({viewport: {width: 430, height: 1050}});
        await page.setContent(
            '<aside>隔离交互验证 · 官方 WXML 编译产物与替身数据</aside><div id="editor"></div><output id="status"></output>'
        );
        const css = fs.readFileSync(path.join(project, 'pages/designer/designer.wxss'), 'utf8');
        await page.addStyleTag({
            content: css.replace(
                /(-?[\d.]+)rpx/g,
                (whole, value) => (Number(value) * 430) / 750 + 'px'
            ),
        });
        await page.addScriptTag({path: compiled});
        await page.evaluate(mountBrowserFixture, {
            designer: fs.readFileSync(path.join(project, 'pages/designer/designer.js'), 'utf8'),
            scripts: Object.fromEntries(['geometry', 'materials', 'scene', 'renderer'].map((name) => ["./" + name + ".js", fs.readFileSync(path.join(project, 'components/necklace-3d', name + '.js'), 'utf8')])),
            arrangement: fs.readFileSync(
                path.join(project, 'services/necklace-arrangement.js'),
                'utf8'
            ),
        });
        await page.getByRole('button', {name: /隔离测试项链/}).click();
        await page.locator('canvas').first().waitFor();
        await page.screenshot({path: path.join(evidence, 'stage2-empty-chain.png')});
        await page.getByText('测试珍珠 6 mm', {exact: true}).click();
        await page.getByRole('button', {name: '测试珍珠 6 mm', exact: true}).click();
        await page.getByText('测试珍珠 8 mm', {exact: true}).click();
        assert.deepEqual(await page.evaluate(() => globalThis.designerFixture.getOrder()), [2, 4]);
        await page.screenshot({path: path.join(evidence, 'stage2-single-pearl.png')});
        await page.getByRole('button', {name: '在选中项后添加'}).click();
        await page.locator('.group-tab').getByText('配饰', {exact: true}).click();
        await page.getByText('测试配饰', {exact: true}).click();
        assert.deepEqual(
            await page.evaluate(() => globalThis.designerFixture.getOrder()),
            [2, 3, 4]
        );
        await page.getByText('删除', {exact: true}).click();
        await page.getByText('撤回', {exact: true}).click();
        assert.deepEqual(
            await page.evaluate(() => globalThis.designerFixture.getOrder()),
            [2, 3, 4]
        );
        await page.getByText('换款', {exact: true}).click();
        await page.getByText('返回选款', {exact: true}).waitFor();
        assert.deepEqual(
            await page.evaluate(() => globalThis.designerFixture.getOrder()),
            [2, 3, 4]
        );
        await page.getByText('当前报价 ¥ 429', {exact: true}).waitFor();
        await page.screenshot({path: path.join(evidence, 'stage3-diy-arrangement.png')});
        await page.evaluate(() => globalThis.designerFixture.setStockFailure(true));
        await page.getByText('测试配饰库存不足', {exact: true}).waitFor();
        await page.getByRole('button', {name: '保存草稿'}).click();
        await page.getByText('草稿已保存，尚不能下单', {exact: true}).waitFor();
        assert.equal(await page.getByRole('button', {name: '下一步'}).isDisabled(), true);
        await page.evaluate(() => globalThis.designerFixture.open(79));
        await page.getByText('配饰', {exact: true}).waitFor();
        assert.deepEqual(
            await page.evaluate(() => globalThis.designerFixture.getOrder()),
            [2, 3, 4]
        );
        await page.evaluate(() => globalThis.designerFixture.setStockFailure(false));
        await page.getByText('佩戴尺寸', {exact: true}).click();
        await page.getByPlaceholder('选填, 未知请留空').fill('35');
        await page.getByPlaceholder('选填', {exact: true}).fill('45');
        await page.getByText('预计成品 45 cm', {exact: true}).waitFor();
        await page.locator('.length-summary').getByText('45 cm', {exact: true}).last().waitFor();
        await page.screenshot({path: path.join(evidence, 'stage3-diy-sizing.png'), fullPage: true});
        await page.getByRole('button', {name: '下一步'}).click();
        await page.getByText(/checkoutId=isolated-checkout/).waitFor();
        assert.equal(
            (await page.evaluate(() => globalThis.designerFixture.purchase())).designSnapshotId,
            79
        );
        await page.getByText('换款', {exact: true}).click();
        await page.getByRole('button', {name: /隔离测试项链/}).waitFor();
        console.log('DIY 编辑、草稿恢复和结算预入口的 Edge 隔离交互通过');
    } finally {
        await browser.close();
    }
}

main().catch((error) => {
    console.error(error);
    process.exitCode = 1;
});
