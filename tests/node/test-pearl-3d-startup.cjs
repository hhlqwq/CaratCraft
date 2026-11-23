const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');

const pageSource = fs.readFileSync(
    path.resolve(__dirname, '../../apps/pearl-3d-poc/pages/necklace/index.js'),
    'utf8'
);

/** 托管原生画布、查询和计时器, 用于验证回调丢失与迟到. */
function fixture() {
    const queries = [];
    const timers = new Map();
    const frames = [];
    let id = 0;
    let draws = 0;
    let disposed = 0;
    let drawError;
    let page;
    const canvas = {
        getContext: () => ({}),
        requestAnimationFrame: (fn) => {
            frames.push(fn);
            return frames.length;
        },
        cancelAnimationFrame: () => {},
    };
    const renderer = {
        fit: () => {},
        draw: () => {
            if (drawError) {
                throw drawError;
            }
            draws++;
        },
        info: () => ({}),
        dispose: () => {
            disposed++;
        },
    };
    vm.runInNewContext(pageSource, {
        require: (name) => (name.endsWith('renderer.js') ? {createRenderer: () => renderer} : {}),
        Page: (value) => {
            page = {...value, data: {...value.data}};
            page.setData = (patch) => Object.assign(page.data, patch);
        },
        wx: {
            createSelectorQuery: () => ({
                select: () => ({node: () => {}, boundingClientRect: () => {}}),
                exec: (callback) => queries.push(callback),
            }),
            getWindowInfo: () => ({pixelRatio: 2}),
        },
        setTimeout: (fn, delay) => {
            timers.set(++id, {fn, delay});
            return id;
        },
        clearTimeout: (key) => timers.delete(key),
        console: {info: () => {}, error: () => {}, warn: () => {}},
    });
    page.onLoad();
    page.onShow();
    page.onReady();

    /** 手动触发指定延迟的定时器. */
    function fire(delay) {
        const entry = [...timers].find(([, timer]) => timer.delay === delay);
        assert.ok(entry, '应存在待完成计时器');
        timers.delete(entry[0]);
        entry[1].fn();
    }

    /** 将正常的微信节点与矩形送回给查询回调. */
    function respond(index) {
        queries[index]([{node: canvas}, {left: 0, top: 80, width: 360, height: 460}]);
    }

    return {
        page,
        frames,
        fire,
        respond,
        timers,
        draws: () => draws,
        disposed: () => disposed,
        rejectDraw: (error) => {
            drawError = error;
        },
    };
}

test('微信原生帧不回调，首帧仍立即完成，后续绘制只补一次', () => {
    const t = fixture();
    t.respond(0);
    assert.equal(t.draws(), 1);
    assert.equal(t.page.data.status, '');
    assert.equal(t.frames.length, 0);
    assert.equal(t.timers.size, 0);
    t.page.queue();
    t.page.queue();
    assert.equal(t.frames.length, 1);
    t.fire(80);
    assert.equal(t.draws(), 2);
    t.frames[0]();
    assert.equal(t.draws(), 2);
});

test('画布查询无回调时超时，迟到回调不可覆盖重试', () => {
    const t = fixture();
    t.fire(5000);
    assert.equal(t.page.data.status, '画布查询失败');
    assert.equal(t.page.data.retryable, true);
    t.page.retry();
    t.respond(0);
    assert.equal(t.draws(), 0);
    t.respond(1);
    assert.equal(t.draws(), 1);
    assert.equal(t.page.data.status, '');
});

test('首帧抛错时释放渲染器，重试可以恢复', () => {
    const t = fixture();
    t.rejectDraw(new Error('模拟首帧失败'));
    t.respond(0);
    assert.equal(t.disposed(), 1);
    assert.equal(t.page.data.status, 'WebGL 首帧失败');
    assert.equal(t.timers.size, 0);
    t.rejectDraw(null);
    t.page.retry();
    t.respond(1);
    assert.equal(t.draws(), 1);
    assert.equal(t.page.data.retryable, false);
});

test('退到后台或离开页面后，待处理的回调不得重绘', () => {
    const t = fixture();
    t.page.onHide();
    t.respond(0);
    assert.equal(t.draws(), 0);
    t.page.onShow();
    t.respond(1);
    t.page.queue();
    t.page.onUnload();
    t.frames[0]();
    assert.equal(t.draws(), 1);
    assert.equal(t.disposed(), 1);
    assert.equal(t.timers.size, 0);
});
