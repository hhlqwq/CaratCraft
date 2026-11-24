const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');
const directory = path.resolve(__dirname, '../../apps/miniapp/components/necklace-3d');
const {previewScene} = require(path.join(directory, 'scene.js'));

/** 仅模拟微信节点查询、页面生命周期及绘制提交. */
function fixture() {
    let definition;
    let draws = 0;
    let releases = 0;
    let scene;
    let pick = 1;
    const queries = [];
    const timers = new Map();
    const events = [];
    const renderer = {
        fit() {},
        draw: () => draws++,
        dispose: () => releases++,
        setScene: (value) => {
            scene = value;
        },
        setSelection() {},
        select: () => pick,
    };
    const canvas = {requestAnimationFrame: () => 0, cancelAnimationFrame() {}};
    let token = 0;
    vm.runInNewContext(fs.readFileSync(path.join(directory, 'index.js'), 'utf8'), {
        require: (name) =>
            name === './renderer.js' ? {createRenderer: () => renderer} : {previewScene},
        Component: (value) => {
            definition = value;
        },
        wx: {getWindowInfo: () => ({pixelRatio: 2})},
        setTimeout: (callback, delay) => {
            timers.set(++token, {callback, delay});
            return token;
        },
        clearTimeout: (id) => timers.delete(id),
        console: {warn() {}},
    });
    const instance = {
        ...definition.methods,
        properties: {
            nodes: [
                {optionId: 8, type: 'pearl', x: 30, y: 70, diameter: 10},
                {optionId: 8, type: 'pearl', x: 60, y: 70, diameter: 10},
            ],
            selected: -1,
        },
        setData() {},
        triggerEvent: (name, detail) => events.push({name, detail}),
        createSelectorQuery: () => ({
            select: () => ({node() {}, boundingClientRect() {}}),
            exec: (callback) => queries.push(callback),
        }),
    };
    definition.lifetimes.ready.call(instance);

    /** 模拟原生画布及其真实显示矩形. */
    function accept(at) {
        queries[at]([{node: canvas}, {left: 20, top: 20, width: 320, height: 300}]);
    }

    /** 触发一个没有自然返回的原生异步步骤. */
    function expire(delay) {
        const match = [...timers].find(([, timer]) => timer.delay === delay);
        assert.ok(match);
        timers.delete(match[0]);
        match[1].callback();
    }

    return {
        instance,
        definition,
        accept,
        expire,
        events,
        draws: () => draws,
        releases: () => releases,
        scene: () => scene,
        setPick: (value) => {
            pick = value;
        },
    };
}

/** 验证点击同料的第二个实例, 不按材料 ID 把它映射回第一个. */
test('3D 首帧不等动画回调且保留重复材料的原始位置', () => {
    const t = fixture();
    t.accept(0);
    assert.equal(t.draws(), 1);
    const touch = {clientX: 180, clientY: 160};
    t.instance.begin({touches: [touch]});
    t.instance.end({touches: [], changedTouches: [touch]});
    assert.equal(t.events[0].name, 'pick');
    assert.equal(t.events[0].detail.index, 1);
    assert.equal(t.events[0].detail.optionId, 8);
    t.definition.observers.selected.call(t.instance, 1);
    t.expire(80);
    assert.equal(t.draws(), 2);
});

/** 超时后仅通知回退一次, 旧画布结果失去有效性. */
test('3D 查询超时或离开后忽略迟到回调', () => {
    const t = fixture();
    t.expire(5000);
    t.accept(0);
    assert.equal(t.events[0].name, 'unavailable');
    assert.equal(t.draws(), 0);
    t.definition.pageLifetimes.show.call(t.instance);
    t.accept(1);
    t.definition.pageLifetimes.hide.call(t.instance);
    assert.equal(t.draws(), 1);
    assert.equal(t.releases(), 1);
    t.definition.pageLifetimes.show.call(t.instance);
    t.definition.lifetimes.detached.call(t.instance);
    t.accept(2);
    assert.equal(t.draws(), 1);
});
