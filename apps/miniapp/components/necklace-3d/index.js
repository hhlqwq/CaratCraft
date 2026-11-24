const {createRenderer} = require('./renderer.js');
const {previewScene} = require('./scene.js');

const STARTUP_TIMEOUT = 5000;
const FRAME_FALLBACK = 80;

/** 取 Canvas 输入的本地坐标, 兼容只报告 x/y 的基础库. */
function point(touch, bounds) {
    if (Number.isFinite(touch.x) && Number.isFinite(touch.y)) {
        return {x: touch.x, y: touch.y};
    }
    return {x: touch.clientX - bounds.left, y: touch.clientY - bounds.top};
}

/** 计算手势像素距离. */
function separation(a, b) {
    return Math.hypot(b.x - a.x, b.y - a.y);
}

Component({
    properties: {
        optionIds: {type: Array, value: []},
        groups: {type: Array, value: []},
        selected: {type: Number, value: -1},
    },

    data: {loading: true, limitation: ''},

    observers: {
        /** 材料改动只更新 3D 投影, 不触发业务请求. */
        'optionIds, groups'() {
            if (this.renderer) {
                try {
                    this.applyScene();
                    this.queue();
                } catch (error) {
                    this.unavailable(error);
                }
            }
        },

        /** 二维和材料面板选中后立即同步高亮. */
        selected(index) {
            if (this.renderer) {
                this.renderer.setSelection(index);
                this.queue();
            }
        },
    },

    lifetimes: {
        /** 等待真实组件布局后申请 WebGL. */
        ready() {
            this.isReady = true;
            if (!this.hidden) {
                this.start();
            }
        },

        /** 销毁组件时注销异步回调并释放 GPU. */
        detached() {
            this.isReady = false;
            this.release();
        },
    },

    pageLifetimes: {
        /** 从后台回到原页时重建被释放的画布. */
        show() {
            this.hidden = false;
            if (this.isReady && !this.renderer && !this.startup) {
                this.start();
            }
        },

        /** 切页后取消帧和初始化, 不占用 GPU. */
        hide() {
            this.hidden = true;
            this.release();
        },

        /** 旋转设备后重新读取尺寸和触摸坐标. */
        resize() {
            if (this.isReady && !this.hidden) {
                this.start();
            }
        },
    },

    methods: {
        /** 首帧直接绘制, 画布查询无响应时通知页面恢复二维. */
        start() {
            this.release();
            const attempt = {};
            this.attempt = attempt;
            this.setData({loading: true});
            this.startup = setTimeout(() => {
                if (this.attempt === attempt) {
                    this.unavailable(new Error('画布查询超时'));
                }
            }, STARTUP_TIMEOUT);
            try {
                const query = this.createSelectorQuery();
                query.select('#necklace-webgl').node();
                query.select('#necklace-webgl').boundingClientRect();
                query.exec((values) => {
                    if (this.attempt !== attempt) {
                        return;
                    }
                    try {
                        const canvas = values && values[0] && values[0].node;
                        const rect = values && values[1];
                        if (!canvas || !rect || !(rect.width > 0 && rect.height > 0)) {
                            throw new Error('画布不可用');
                        }
                        const device =
                            typeof wx.getWindowInfo === 'function'
                                ? wx.getWindowInfo()
                                : wx.getSystemInfoSync();
                        this.bounds = rect;
                        this.canvas = canvas;
                        this.renderer = createRenderer(canvas);
                        this.renderer.fit(rect.width, rect.height, device.pixelRatio);
                        this.applyScene();
                        this.renderer.draw();
                        clearTimeout(this.startup);
                        this.startup = null;
                        this.setData({loading: false});
                    } catch (error) {
                        this.unavailable(error);
                    }
                });
            } catch (error) {
                this.unavailable(error);
            }
        },

        /** 将画面与实例索引配对, 保证选中事件对应当前排列. */
        applyScene() {
            const result = previewScene(this.properties.optionIds, this.properties.groups);
            this.renderer.setScene(result);
            this.renderer.setSelection(this.properties.selected);
            this.renderedIds = result.optionIds;
            this.setData({limitation: result.limitation});
        },

        /** 把渲染失败限制在预览区域, 不修改设计. */
        unavailable(error) {
            this.release();
            console.warn('[CaratCraft DIY 3D]', error.message);
            this.triggerEvent('unavailable');
        },

        /** 每帧合并触摸和编辑, 原生帧未回调时兜底. */
        queue() {
            if (!this.renderer || this.hidden || this.frame) {
                return;
            }
            const frame = {};
            this.frame = frame;
            const native = typeof this.canvas.requestAnimationFrame === 'function';
            const paint = () => {
                if (this.frame !== frame) {
                    return;
                }
                this.stopFrame();
                if (this.renderer && !this.hidden) {
                    try {
                        this.renderer.draw();
                    } catch (error) {
                        this.unavailable(error);
                    }
                }
            };
            frame.timer = setTimeout(paint, native ? FRAME_FALLBACK : 16);
            if (native) {
                try {
                    frame.native = this.canvas.requestAnimationFrame(paint);
                } catch (error) {
                    console.warn('[CaratCraft DIY 3D] 采用定时重绘', error.message);
                }
            }
        },

        /** 取消当次原生帧及定时帧. */
        stopFrame() {
            const frame = this.frame;
            this.frame = null;
            if (frame) {
                clearTimeout(frame.timer);
                if (
                    frame.native != null &&
                    typeof this.canvas.cancelAnimationFrame === 'function'
                ) {
                    try {
                        this.canvas.cancelAnimationFrame(frame.native);
                    } catch (error) {
                        console.warn('[CaratCraft DIY 3D] 取消原生帧失败', error.message);
                    }
                }
            }
        },

        /** 回收实例所有资源, 迟到的回调通过对象身份失效. */
        release() {
            this.attempt = null;
            clearTimeout(this.startup);
            this.startup = null;
            this.stopFrame();
            this.gesture = null;
            if (this.renderer) {
                try {
                    this.renderer.dispose();
                } catch (error) {
                    console.warn('[CaratCraft DIY 3D] 回收失败', error.message);
                }
                this.renderer = null;
            }
            this.canvas = null;
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
            const index = this.renderer.select(x, y);
            const optionId = this.renderedIds[index];
            if (optionId != null) {
                this.triggerEvent('pick', {index, optionId});
                this.queue();
            }
        },

        /** 被系统取消的手势不触发点击. */
        cancel() {
            this.gesture = null;
        },
    },
});
