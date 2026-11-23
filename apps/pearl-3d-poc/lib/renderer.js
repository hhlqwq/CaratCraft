const geometry = require('./geometry.js');
const shaders = require('./materials.js');

const SPREAD = 0.382;
const INITIAL_YAW = -0.1;
const INITIAL_PITCH = 0.26;
const QUAD = [-1, -1, 1, -1, -1, 1, -1, 1, 1, -1, 1, 1];
const MAX_DPR = 2.25;
const PIXEL_BUDGET = 1050000;

/** 创建单一 Canvas 的原生 WebGL 1 渲染器, 管理其全部 GPU 资源. */
function createRenderer(canvas) {
    const gl = canvas.getContext('webgl', {
        alpha: false,
        antialias: true,
        preserveDrawingBuffer: false,
    });
    if (!gl) {
        throw new Error('当前微信环境未提供 WebGL Canvas');
    }

    const buffers = [];
    const programs = [];
    const bufferSizes = new Map();
    const high =
        typeof gl.getShaderPrecisionFormat === 'function'
            ? gl.getShaderPrecisionFormat(gl.FRAGMENT_SHADER, gl.HIGH_FLOAT)
            : null;
    const precision = high && high.precision > 0 ? 'highp' : 'mediump';
    let viewportWidth = 0;
    let viewportHeight = 0;
    let viewportRatio = 1;
    let distance = 0;
    let zoom = 1;
    let centerX = 0;
    let centerY = 0;
    let yaw = INITIAL_YAW;
    let pitch = INITIAL_PITCH;
    let selection = -1;
    let model;
    let shadowData;
    let disposed = false;
    let firstFrame = true;
    let scene;
    const projection = new Float32Array(16);
    const sizes = geometry.initialDiameters();

    /** 编译并校验着色器, 失败时回收单个对象. */
    function shader(kind, source) {
        const value = gl.createShader(kind);
        if (!value) {
            throw new Error('无法创建 GPU 着色器');
        }
        gl.shaderSource(value, source);
        gl.compileShader(value);
        if (!gl.getShaderParameter(value, gl.COMPILE_STATUS)) {
            const error = gl.getShaderInfoLog(value);
            gl.deleteShader(value);
            throw new Error('着色器编译失败: ' + error);
        }
        return value;
    }

    /** 固定特性位置并连接着色器, 避免跨 pass 使用残留数组. */
    function program(vert, frag, attributes, uniformNames) {
        let vertex;
        let fragment;
        let linked;
        try {
            vertex = shader(gl.VERTEX_SHADER, 'precision highp float;\n' + vert);
            fragment = shader(gl.FRAGMENT_SHADER, 'precision ' + precision + ' float;\n' + frag);
            linked = gl.createProgram();
            if (!linked) {
                throw new Error('无法创建 GPU 程序');
            }
            programs.push(linked);
            gl.attachShader(linked, vertex);
            gl.attachShader(linked, fragment);
            attributes.forEach((name, index) => gl.bindAttribLocation(linked, index, name));
            gl.linkProgram(linked);
            if (!gl.getProgramParameter(linked, gl.LINK_STATUS)) {
                throw new Error('着色器连接失败: ' + gl.getProgramInfoLog(linked));
            }
            const uniforms = {};
            uniformNames.forEach((name) => {
                uniforms[name] = gl.getUniformLocation(linked, name);
            });
            return {linked, uniforms};
        } finally {
            if (vertex) {
                gl.deleteShader(vertex);
            }
            if (fragment) {
                gl.deleteShader(fragment);
            }
        }
    }

    /** 建立一个由渲染器所有的缓冲. */
    function buffer() {
        const result = gl.createBuffer();
        if (!result) {
            throw new Error('无法创建 GPU 缓冲');
        }
        buffers.push(result);
        return result;
    }

    /** 上传数据并记录几何缓冲的实际分配字节数. */
    function upload(target, handle, data, usage) {
        gl.bindBuffer(target, handle);
        gl.bufferData(target, data, usage);
        bufferSizes.set(handle, data.byteLength);
    }

    /** 复用 GPU 缓冲上传完整索引网格. */
    function uploadMesh(mesh, target, usage) {
        upload(gl.ARRAY_BUFFER, target.vertex, new Float32Array(mesh.vertices), usage);
        upload(gl.ELEMENT_ARRAY_BUFFER, target.index, new Uint16Array(mesh.indices), usage);
        target.count = mesh.indices.length;
    }

    /** 绘制道具网格时使用交错的位置及法线. */
    function bindMesh(mesh) {
        gl.bindBuffer(gl.ARRAY_BUFFER, mesh.vertex);
        gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, mesh.index);
        gl.vertexAttribPointer(0, 3, gl.FLOAT, false, 24, 0);
        gl.vertexAttribPointer(1, 3, gl.FLOAT, false, 24, 12);
        gl.enableVertexAttribArray(0);
        gl.enableVertexAttribArray(1);
        gl.disableVertexAttribArray(2);
    }

    /** 切换到矩形覆盖层, 与珠体顶点数组独立. */
    function bindQuad() {
        gl.bindBuffer(gl.ARRAY_BUFFER, scene.quad);
        gl.vertexAttribPointer(0, 2, gl.FLOAT, false, 0, 0);
        gl.enableVertexAttribArray(0);
        gl.disableVertexAttribArray(1);
        gl.disableVertexAttribArray(2);
    }

    /** 更新链条排布的可变 GPU 数据, 珠体始终共享一份几何. */
    function rebuild() {
        model = geometry.layout(sizes);
        uploadMesh(model.clasp, scene.clasp, gl.DYNAMIC_DRAW);
        uploadMesh(model.thread, scene.thread, gl.DYNAMIC_DRAW);
    }

    /** 按真实绘图区和像素预算重置投影, 默认完整取景. */
    function fit(width, height, pixelRatio) {
        if (!(width > 0 && height > 0) || disposed) {
            return;
        }
        const ratio = Math.min(
            pixelRatio || 1,
            MAX_DPR,
            Math.sqrt(PIXEL_BUDGET / (width * height))
        );
        const w = Math.max(1, Math.round(width * ratio));
        const h = Math.max(1, Math.round(height * ratio));
        if (w !== canvas.width || h !== canvas.height) {
            canvas.width = w;
            canvas.height = h;
        }
        viewportWidth = w;
        viewportHeight = h;
        viewportRatio = w / h;
        const near = 8;
        const far = 1800;
        projection[0] = 1 / (SPREAD * viewportRatio);
        projection[5] = 1 / SPREAD;
        projection[10] = (far + near) / (near - far);
        projection[11] = -1;
        projection[14] = (2 * far * near) / (near - far);
        distance = 73.8 / Math.sin(Math.atan(Math.min(1, viewportRatio) * SPREAD));
        gl.viewport(0, 0, w, h);
    }

    /** 将珠体软接触阴影批量写到单次绘制的屏幕空间缓存. */
    function shadows(matrix, depth) {
        let offset = 0;
        const yAxis = Math.max(0.3, Math.abs(matrix[8]));
        model.pearls.forEach((pearl) => {
            const p = geometry.viewPoint(pearl.point, matrix, depth);
            const denom = -p[2] * SPREAD;
            const projected = pearl.radius / denom;
            const width = projected / viewportRatio;
            const plane = projected * (1.28 + yAxis * 0.41);
            const sizeX = width * (1.62 + 0.46 * yAxis);
            const x = p[0] / denom / viewportRatio + width * 0.37 + centerX;
            const y = p[1] / denom - projected * 0.59 + centerY;
            const opacity = 0.26 + yAxis * 0.17;
            for (let index = 0; index < QUAD.length; index += 2) {
                const j = QUAD[index];
                const k = QUAD[index + 1];
                shadowData[offset++] = x + j * sizeX;
                shadowData[offset++] = y + k * plane;
                shadowData[offset++] = j;
                shadowData[offset++] = k;
                shadowData[offset++] = opacity;
            }
        });
        gl.bindBuffer(gl.ARRAY_BUFFER, scene.shadows);
        gl.bufferSubData(gl.ARRAY_BUFFER, 0, shadowData);
        gl.vertexAttribPointer(0, 2, gl.FLOAT, false, 20, 0);
        gl.vertexAttribPointer(1, 2, gl.FLOAT, false, 20, 8);
        gl.vertexAttribPointer(2, 1, gl.FLOAT, false, 20, 16);
        gl.enableVertexAttribArray(0);
        gl.enableVertexAttribArray(1);
        gl.enableVertexAttribArray(2);
        gl.useProgram(scene.shadowShader.linked);
        gl.drawArrays(gl.TRIANGLES, 0, offset / 5);
    }

    /** 绘制完整造型、软投影、扣件和遮挡正确的选择轮廓. */
    function draw() {
        if (disposed || !viewportWidth) {
            return;
        }
        if (typeof gl.isContextLost === 'function' && gl.isContextLost()) {
            throw new Error('WebGL 上下文已丢失, 请重新打开样机');
        }
        const matrix = geometry.rotation(yaw, pitch);
        const depth = distance / zoom;
        gl.depthMask(true);
        gl.clear(gl.DEPTH_BUFFER_BIT);
        gl.disable(gl.DEPTH_TEST);
        gl.disable(gl.BLEND);
        gl.useProgram(scene.backdrop.linked);
        bindQuad();
        gl.drawArrays(gl.TRIANGLES, 0, 6);
        gl.enable(gl.BLEND);
        gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA);
        shadows(matrix, depth);
        gl.disable(gl.BLEND);
        gl.enable(gl.DEPTH_TEST);
        const {linked, uniforms} = scene.jewel;
        gl.useProgram(linked);
        gl.uniformMatrix4fv(uniforms.uProjection, false, projection);
        gl.uniformMatrix3fv(uniforms.uRotation, false, matrix);
        gl.uniform1f(uniforms.uDistance, depth);
        gl.uniform3f(uniforms.uCenter, 0, 0, 0);
        gl.uniform1f(uniforms.uRadius, 1);
        gl.uniform1f(uniforms.uSeed, 0);
        gl.uniform1f(uniforms.uMaterial, 2);
        bindMesh(scene.thread);
        gl.drawElements(gl.TRIANGLES, scene.thread.count, gl.UNSIGNED_SHORT, 0);
        gl.uniform1f(uniforms.uMaterial, 1);
        bindMesh(scene.clasp);
        gl.drawElements(gl.TRIANGLES, scene.clasp.count, gl.UNSIGNED_SHORT, 0);

        gl.uniform1f(uniforms.uMaterial, 0);
        bindMesh(scene.pearl);
        model.pearls.forEach((pearl, index) => {
            gl.uniform3f(uniforms.uCenter, ...pearl.point);
            gl.uniform1f(uniforms.uRadius, pearl.radius);
            gl.uniform1f(uniforms.uSeed, index * 0.037);
            gl.drawElements(gl.TRIANGLES, scene.pearl.count, gl.UNSIGNED_SHORT, 0);
        });
        if (selection >= 0) {
            const pearl = model.pearls[selection];
            const point = geometry.viewPoint(pearl.point, matrix, depth);
            const halo = scene.halo.uniforms;
            gl.useProgram(scene.halo.linked);
            gl.uniformMatrix4fv(halo.uProjection, false, projection);
            gl.uniform3f(halo.uCenter, point[0], point[1], point[2] - 0.3);
            gl.uniform1f(halo.uRadius, pearl.radius * 1.29);
            gl.depthMask(false);
            gl.enable(gl.BLEND);
            bindQuad();
            gl.drawArrays(gl.TRIANGLES, 0, 6);
            gl.disable(gl.BLEND);
            gl.depthMask(true);
        }
        if (firstFrame) {
            if (typeof gl.flush === 'function') {
                gl.flush();
            }
            firstFrame = false;
            const error = gl.getError();
            if (error !== gl.NO_ERROR) {
                throw new Error('WebGL 首帧错误: ' + error);
            }
        }
    }

    /** 旋转模型, 俯仰限幅保证连续拖动方向易预判. */
    function rotate(dx, dy) {
        yaw += dx * 0.01;
        pitch = Math.max(-1.26, Math.min(1.26, pitch + dy * 0.009));
    }

    /** 围绕两指中点缩放, 回到全景比例时恢复居中. */
    function scale(factor, focusX = 0, focusY = 0) {
        const next = Math.max(0.78, Math.min(2.75, zoom * factor));
        const ratio = next / zoom;
        const limit = Math.max(0, next - 1);
        centerX = Math.max(-limit, Math.min(limit, centerX * ratio + focusX * (1 - ratio)));
        centerY = Math.max(-limit, Math.min(limit, centerY * ratio + focusY * (1 - ratio)));
        zoom = next;
        projection[8] = -centerX;
        projection[9] = -centerY;
    }

    /** 以归一化设备坐标执行前向三维拾取. */
    function select(x, y) {
        selection = geometry.pick(
            model.pearls,
            geometry.rotation(yaw, pitch),
            distance / zoom,
            x - centerX,
            y - centerY,
            SPREAD,
            viewportRatio
        );
        return {index: selection, diameter: selection < 0 ? 0 : sizes[selection]};
    }

    /** 切换所选珠径, 保留同一颗珠子的选择索引. */
    function setDiameter(diameter) {
        if (
            selection < 0 ||
            !geometry.BASE_SIZES.includes(diameter) ||
            sizes[selection] === diameter
        ) {
            return false;
        }
        sizes[selection] = diameter;
        rebuild();
        return true;
    }

    /** 读取性能说明所需的尺寸、上下文和已知几何资源. */
    function info() {
        const context = gl.getContextAttributes();
        return {
            width: viewportWidth,
            height: viewportHeight,
            api: gl.getParameter(gl.VERSION),
            renderer: gl.getParameter(gl.RENDERER),
            antialias: Boolean(context && context.antialias),
            bufferBytes: Array.from(bufferSizes.values()).reduce((sum, bytes) => sum + bytes, 0),
            pearls: sizes.length,
        };
    }

    /** 释放已分配的资源, 失败和正常退出共用此路径. */
    function dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        gl.useProgram(null);
        gl.bindBuffer(gl.ARRAY_BUFFER, null);
        gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, null);
        programs.forEach((value) => gl.deleteProgram(value));
        buffers.forEach((value) => gl.deleteBuffer(value));
        bufferSizes.clear();
    }

    try {
        scene = {
            quad: buffer(),
            shadows: buffer(),
            pearl: {vertex: buffer(), index: buffer()},
            clasp: {vertex: buffer(), index: buffer()},
            thread: {vertex: buffer(), index: buffer()},
            jewel: program(
                shaders.jewelVertex,
                shaders.jewelFragment,
                ['aPosition', 'aNormal'],
                [
                    'uProjection',
                    'uRotation',
                    'uCenter',
                    'uRadius',
                    'uDistance',
                    'uMaterial',
                    'uSeed',
                ]
            ),
            backdrop: program(shaders.backdropVertex, shaders.backdropFragment, ['aPosition'], []),
            shadowShader: program(
                shaders.shadowVertex,
                shaders.shadowFragment,
                ['aPosition', 'aProfile', 'aOpacity'],
                []
            ),
            halo: program(
                shaders.haloVertex,
                shaders.haloFragment,
                ['aPosition'],
                ['uProjection', 'uCenter', 'uRadius']
            ),
        };
        upload(gl.ARRAY_BUFFER, scene.quad, new Float32Array(QUAD), gl.STATIC_DRAW);
        shadowData = new Float32Array(sizes.length * 6 * 5);
        upload(gl.ARRAY_BUFFER, scene.shadows, shadowData, gl.DYNAMIC_DRAW);
        uploadMesh(geometry.pearlMesh(), scene.pearl, gl.STATIC_DRAW);
        rebuild();
    } catch (error) {
        dispose();
        throw error;
    }
    return {fit, draw, rotate, scale, select, setDiameter, info, dispose};
}

module.exports = {createRenderer};
