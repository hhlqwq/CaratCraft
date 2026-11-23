const {createRenderer} = require('../../lib/renderer.js');
const {BASE_SIZES} = require('../../lib/geometry.js');
const version = require('../../version.js');

const STARTUP_TIMEOUT = 5000;
const FRAME_FALLBACK = 80;

/** 取 Canvas 输入的本地坐标, 兼容只报告 x/y 的基础库. */
function point(touch, bounds) {
    if (Number.isFinite(touch.clientX) && Number.isFinite(touch.clientY)) {
        return {x: touch.clientX - bounds.left, y: touch.clientY - bounds.top};
    }
    return {x: touch.x, y: touch.y};
}

/** 计算手势像素距离. */
function separation(a, b) {
    return Math.hypot(b.x - a.x, b.y - a.y);
}

Page({
    data: {
        version,
        selected: -1,
        diameter: 0,
        sizes: BASE_SIZES,
        status: '正在载入 3D 画布…',
        retryable: false,
    },

    /** 在页面计时起点准备本地生命周期状态. */
    onLoad() {
        this.loadedAt = Date.now();
        this.visible = true;
    },

    /** 等待原生 Canvas 节点进入页面后建立渲染器. */
    onReady() {
        this.ready = true;
        this.measure();
    },

    /** 恢复展示时重新核对 Canvas 尺寸. */
    onShow() {
        this.visible = true;
        if (this.ready) {
            this.measure();
        } else {
            this.waitFor('页面就绪');
        }
    },

    /** 页面尺寸变化时仍按实际绘图区拾取. */
    onResize() {
        if (this.ready && this.visible) {
            this.measure();
        }
    },

    /** 页面隐藏不继续消耗帧, 并结束未完成的手势. */
    onHide() {
        this.visible = false;
        this.gesture = null;
        this.stopStartup();
        this.stopFrame();
        this.reportGesture();
    },

    /** 页面退出时显式释放所有画布 GPU 对象. */
    onUnload() {
        this.visible = false;
        this.ended = true;
        this.stopStartup();
        this.stopFrame();
        if (this.renderer) {
            this.renderer.dispose();
            this.renderer = null;
        }
        this.canvas = null;
    },

    /** 为页面就绪和画布查询设置有限等待, 逾期后允许重新初始化. */
    waitFor(stage) {
        this.stopStartup();
        const attempt = this.attempt;
        this.startup = setTimeout(() => {
            if (this.visible && this.attempt === attempt) {
                this.fail(stage, new Error('等待超过 ' + STARTUP_TIMEOUT + ' ms'));
            }
        }, STARTUP_TIMEOUT);
        return attempt;
    },

    /** 使以前的查询或等待回调失效. */
    stopStartup() {
        clearTimeout(this.startup);
        this.startup = null;
        this.attempt = (this.attempt || 0) + 1;
    },

    /** 使用页面级节点查询, 将 WebGL 节点和屏幕矩形分别读取. */
    measure() {
        if (this.ended || !this.visible) {
            return;
        }
        this.stopFrame();
        if (!this.renderer) {
            this.setData({status: '正在读取 3D 画布…', retryable: false});
        }
        const attempt = this.waitFor('画布查询');
        try {
            const query = wx.createSelectorQuery();
            query.select('#jewel').node();
            query.select('#jewel').boundingClientRect();
            query.exec((result) => {
                if (this.attempt !== attempt || !this.visible) {
                    return;
                }
                const canvas = result && result[0] && result[0].node;
                const bounds = result && result[1];
                if (!canvas || typeof canvas.getContext !== 'function') {
                    this.fail('画布节点', new Error('未返回原生 WebGL 节点'));
                    return;
                }
                if (!bounds || !(bounds.width > 0 && bounds.height > 0)) {
                    this.fail('画布尺寸', new Error('未取得可用的画布区域'));
                    return;
                }
                this.startRenderer(canvas, bounds);
            });
        } catch (error) {
            this.fail('画布查询', error);
        }
    },

    /** 直接提交首帧, 规避画布 rAF 在初次显示时未回调的情况. */
    startRenderer(canvas, bounds) {
        let stage = 'WebGL 初始化';
        try {
            if (!this.renderer) {
                this.canvas = canvas;
                this.renderer = createRenderer(canvas);
            }
            this.bounds = bounds;
            const device =
                typeof wx.getWindowInfo === 'function'
                    ? wx.getWindowInfo()
                    : wx.getSystemInfoSync();
            this.renderer.fit(bounds.width, bounds.height, device.pixelRatio);
            stage = 'WebGL 首帧';
            this.drawFrame();
            this.stopStartup();
            this.setData({status: '', retryable: false});
            if (!this.firstDrawAt) {
                this.firstDrawAt = Date.now();
                this.reportFirstFrame();
            }
        } catch (error) {
            this.fail(stage, error);
        }
    },

    /** 记录辅助指标, 探测失败时保留已绘制画面. */
    reportFirstFrame() {
        try {
            const device = typeof wx.getDeviceInfo === 'function' ? wx.getDeviceInfo() : {};
            console.info('[CaratCraft 3D] 首帧提交', {
                version,
                elapsedMs: this.firstDrawAt - this.loadedAt,
                platform: device.platform,
                model: device.model,
                system: device.system,
                ...this.renderer.info(),
            });
        } catch (error) {
            console.warn('[CaratCraft 3D] 首帧诊断不可用', error.message);
        }
    },

    /** 结束故障路径, 完整清理状态后向用户展示失败阶段及重试入口. */
    fail(stage, error) {
        this.stopStartup();
        this.stopFrame();
        if (this.renderer) {
            try {
                this.renderer.dispose();
            } catch (cleanupError) {
                console.warn('[CaratCraft 3D] 资源清理失败', cleanupError.message);
            }
            this.renderer = null;
        }
        this.canvas = null;
        this.firstDrawAt = null;
        this.gesture = null;
        this.reportGesture();
        console.error('[CaratCraft 3D] ' + stage, error.message);
        this.setData({status: stage + '失败', retryable: true, selected: -1, diameter: 0});
    },

    /** 用户重试时重置诊断计时并重新查询画布. */
    retry() {
        this.loadedAt = Date.now();
        this.measure();
    },

    /** 绘制并采集有效手势帧. */
    drawFrame() {
        this.renderer.draw();
        if (this.gestureFrames) {
            this.gestureFrames.push(Date.now());
        }
    },

    /** 合并重绘请求, 原生帧延迟时以一次定时帧接管. */
    queue() {
        if (!this.renderer || !this.visible || this.frame) {
            return;
        }
        const hasNative = typeof this.canvas.requestAnimationFrame === 'function';
        const frame = {};
        this.frame = frame;
        const tick = () => {
            if (this.frame !== frame) {
                return;
            }
            this.stopFrame();
            if (this.visible && !this.ended && this.renderer) {
                try {
                    this.drawFrame();
                } catch (error) {
                    this.fail('WebGL 绘制', error);
                }
            }
        };
        frame.timer = setTimeout(tick, hasNative ? FRAME_FALLBACK : 16);
        if (hasNative) {
            try {
                frame.native = this.canvas.requestAnimationFrame(tick);
            } catch (error) {
                console.warn('[CaratCraft 3D] 切换定时帧', error.message);
            }
        }
    },

    /** 撤销备用定时器与原生帧, 已离开的帧不得继续绘制. */
    stopFrame() {
        const frame = this.frame;
        if (!frame) {
            return;
        }
        this.frame = null;
        clearTimeout(frame.timer);
        if (frame.native != null && typeof this.canvas.cancelAnimationFrame === 'function') {
            try {
                this.canvas.cancelAnimationFrame(frame.native);
            } catch (error) {
                console.warn('[CaratCraft 3D] 取消原生帧失败', error.message);
            }
        }
    },

    /** 在控制台记录实际发生的手势帧, 静止时间不计入帧率. */
    reportGesture() {
        const frames = this.gestureFrames;
        this.gestureFrames = null;
        if (!frames || frames.length < 3) {
            return;
        }
        const deltas = frames
            .slice(1)
            .map((time, index) => time - frames[index])
            .filter((dt) => dt > 0)
            .sort((a, b) => a - b);
        const sum = deltas.reduce((total, value) => total + value, 0);
        if (deltas.length >= 2 && sum > 0) {
            console.info('[CaratCraft 3D] 连续手势帧', {
                samples: deltas.length,
                fps: Math.round((10000 * deltas.length) / sum) / 10,
                p95Ms: deltas[Math.ceil(deltas.length * 0.95) - 1],
            });
        }
    },

    /** 建立单指拖动或双指缩放手势, 双指后抬起不会被当作点选. */
    begin(event) {
        if (!this.renderer || !event.touches.length) {
            return;
        }
        if (!this.gesture) {
            const start = point(event.touches[0], this.bounds);
            this.gesture = {
                start,
                last: start,
                moved: false,
                pinched: false,
                startedAt: Date.now(),
            };
            this.gestureFrames = [];
        }
        if (event.touches.length >= 2) {
            const points = event.touches.slice(0, 2).map((touch) => point(touch, this.bounds));
            this.gesture.span = separation(...points);
            this.gesture.pinched = true;
        }
    },

    /** 接收平移或两指距离变化, 手指切换不造成旋转跳变. */
    move(event) {
        const g = this.gesture;
        if (!this.renderer || !g || !event.touches.length) {
            return;
        }
        const first = point(event.touches[0], this.bounds);
        if (event.touches.length >= 2) {
            const second = point(event.touches[1], this.bounds);
            const nextSpan = separation(first, second);
            if (g.span > 8 && nextSpan > 8) {
                const focusX = (first.x + second.x) / this.bounds.width - 1;
                const focusY = 1 - (first.y + second.y) / this.bounds.height;
                this.renderer.scale(nextSpan / g.span, focusX, focusY);
                this.queue();
            }
            g.span = nextSpan;
            g.pinched = true;
            g.last = null;
            return;
        }
        if (!g.last) {
            g.last = first;
            return;
        }
        if (g.moved || g.pinched || separation(first, g.start) >= 5) {
            this.renderer.rotate(first.x - g.last.x, first.y - g.last.y);
            g.moved = true;
            g.last = first;
            this.queue();
        }
    },

    /** 只在短触摸抬起时进行光线拾取. */
    end(event) {
        const g = this.gesture;
        if (!g || !this.renderer) {
            return;
        }
        if (event.touches.length) {
            g.last = point(event.touches[0], this.bounds);
            g.span = null;
            return;
        }
        this.gesture = null;
        this.reportGesture();
        if (
            g.moved ||
            g.pinched ||
            Date.now() - g.startedAt > 380 ||
            !event.changedTouches.length
        ) {
            return;
        }
        const cursor = point(event.changedTouches[0], this.bounds);
        if (separation(cursor, g.start) > 6) {
            return;
        }
        const x = (2 * cursor.x) / this.bounds.width - 1;
        const y = 1 - (2 * cursor.y) / this.bounds.height;
        const selected = this.renderer.select(x, y);
        this.setData({selected: selected.index, diameter: selected.diameter});
        this.queue();
    },

    /** 被系统取消的手势不触发点击. */
    cancel() {
        this.gesture = null;
        this.reportGesture();
    },

    /** 变更当前珠子的真实几何直径, 选中状态随排布更新保留. */
    resizePearl(event) {
        const diameter = Number(event.currentTarget.dataset.size);
        if (this.renderer && this.renderer.setDiameter(diameter)) {
            this.setData({diameter});
            this.queue();
        }
    },
});
