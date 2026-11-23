const {createRenderer} = require('../../lib/renderer.js');
const {BASE_SIZES} = require('../../lib/geometry.js');
const version = require('../../version.js');

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
        this.stopFrame();
        this.reportGesture();
    },

    /** 页面退出时显式释放所有画布 GPU 对象. */
    onUnload() {
        this.visible = false;
        this.ended = true;
        this.stopFrame();
        if (this.renderer) {
            this.renderer.dispose();
            this.renderer = null;
        }
        this.canvas = null;
    },

    /** 读取真实原生画布节点与矩形, 使用微信设备像素比. */
    measure() {
        const token = (this.measurement || 0) + 1;
        this.measurement = token;
        wx.createSelectorQuery()
            .in(this)
            .select('#jewel')
            .fields({node: true, size: true, rect: true})
            .exec((values) => {
                if (this.ended || !this.visible || token !== this.measurement) {
                    return;
                }
                const result = values && values[0];
                if (!result || !result.node || !(result.width > 0 && result.height > 0)) {
                    this.setData({status: '当前窗口无法初始化 3D 画布'});
                    return;
                }
                try {
                    if (!this.renderer) {
                        this.canvas = result.node;
                        this.renderer = createRenderer(this.canvas);
                    }
                    this.bounds = {
                        left: result.left || 0,
                        top: result.top || 0,
                        width: result.width,
                        height: result.height,
                    };
                    const device =
                        typeof wx.getWindowInfo === 'function'
                            ? wx.getWindowInfo()
                            : wx.getSystemInfoSync();
                    this.renderer.fit(result.width, result.height, device.pixelRatio);
                    this.queue();
                } catch (error) {
                    this.fail(error);
                }
            });
    },

    /** 渲染失败时提供可读状态, 立即释放分配过的资源. */
    fail(error) {
        this.stopFrame();
        if (this.renderer) {
            this.renderer.dispose();
            this.renderer = null;
        }
        console.error('[CaratCraft 3D] 渲染失败', error.message);
        this.setData({status: 'WebGL 渲染失败，请检查开发者工具或微信版本'});
    },

    /** 合并多个触摸事件到同一帧, 静止时不持续绘制. */
    queue() {
        if (!this.renderer || !this.visible || this.frame != null) {
            return;
        }
        const tick = () => {
            this.frame = null;
            if (!this.visible || this.ended || !this.renderer) {
                return;
            }
            try {
                this.renderer.draw();
                const time = Date.now();
                if (!this.firstDrawAt) {
                    this.firstDrawAt = time;
                    this.setData({status: ''});
                    const device = typeof wx.getDeviceInfo === 'function' ? wx.getDeviceInfo() : {};
                    console.info('[CaratCraft 3D] 首帧提交', {
                        version,
                        elapsedMs: time - this.loadedAt,
                        platform: device.platform,
                        model: device.model,
                        system: device.system,
                        ...this.renderer.info(),
                    });
                }
                if (this.gestureFrames) {
                    this.gestureFrames.push(time);
                }
            } catch (error) {
                this.fail(error);
            }
        };
        this.canvasFrame = typeof this.canvas.requestAnimationFrame === 'function';
        this.frame = this.canvasFrame
            ? this.canvas.requestAnimationFrame(tick)
            : setTimeout(tick, 16);
    },

    /** 解除挂起的原生帧或兼容定时帧. */
    stopFrame() {
        if (this.frame == null) {
            return;
        }
        if (this.canvasFrame && typeof this.canvas.cancelAnimationFrame === 'function') {
            this.canvas.cancelAnimationFrame(this.frame);
        } else if (!this.canvasFrame) {
            clearTimeout(this.frame);
        }
        this.frame = null;
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
