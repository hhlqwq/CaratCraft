const geometry = require('./geometry.js');
const shaders = require('./materials.js');

const SPREAD = 0.382;
const VIEW_RADIUS = 75;
const INITIAL_YAW = 0;
const INITIAL_PITCH = 0;
const QUAD = [-1, -1, 1, -1, -1, 1, -1, 1, 1, -1, 1, 1];
const MAX_DPR = 1.75;
const PIXEL_BUDGET = 620000;

const MESHES = {
    pearl: geometry.pearlMesh,
    smallPearl: geometry.smallPearlMesh,
    decoration: geometry.charmMesh,
    clasp: geometry.genericClaspMesh,
    chain: geometry.linkMesh,
    box: () => geometry.styledLinkMesh('box'),
    chopin: () => geometry.styledLinkMesh('chopin'),
    cartier: () => geometry.styledLinkMesh('cartier'),
    crown: () => geometry.accessoryMesh('crown'),
    clover: () => geometry.accessoryMesh('clover'),
    marquise: () => geometry.accessoryMesh('marquise'),
    baroque: geometry.baroqueMesh,
    thread: geometry.threadMesh,
};

/** 创建单一 Canvas 的原生 WebGL 1 渲染器, 管理其全部 GPU 资源. */
function createRenderer(canvas, preserveDrawingBuffer = false) {
    const gl = canvas.getContext('webgl', {
        alpha: false,
        antialias: true,
        preserveDrawingBuffer,
    });
    if (!gl) {
        throw new Error('当前微信环境未提供 WebGL Canvas');
    }

    const buffers = [];
    const programs = [];
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
    let pickedPart = null;
    let parts = [];
    let shadowData;
    let disposed = false;
    let firstFrame = true;
    let scene;
    const projection = new Float32Array(16);

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

    /** 将已分配的几何数据上传到现有 GPU 缓冲. */
    function upload(target, handle, data, usage) {
        gl.bindBuffer(target, handle);
        gl.bufferData(target, data, usage);
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

    /** 接收当前设计的显示投影, 复用所有 GPU 网格. */
    function setScene(next) {
        const required = next.parts.length * QUAD.length * 5 / 2;
        if (required > shadowData.length) {
            shadowData = new Float32Array(required);
            upload(gl.ARRAY_BUFFER, scene.shadows, shadowData, gl.DYNAMIC_DRAW);
        }
        const unitScale = VIEW_RADIUS / next.extent;
        parts = next.parts.map((part) => ({
            ...part,
            point: part.point.map((coordinate) => coordinate * unitScale),
            radius: part.radius * unitScale,
            pickRadius: part.pickRadius ? part.pickRadius * unitScale : undefined,
        }));
        pickedPart = null;
    }

    /** 由页面选中的实例索引确定高亮对象. */
    function setSelection(index) {
        selection = index;
        if (pickedPart && pickedPart.index !== index) {
            pickedPart = null;
        }
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
        distance = VIEW_RADIUS / Math.sin(Math.atan(Math.min(1, viewportRatio) * SPREAD));
        gl.viewport(0, 0, w, h);
    }

    /** 将珠体软接触阴影批量写到单次绘制的屏幕空间缓存. */
    function shadows(matrix, depth) {
        let offset = 0;
        const yAxis = Math.max(0.3, Math.abs(matrix[8]));
        parts.forEach((pearl) => {
            if (pearl.mesh === 'chain' || pearl.mesh === 'thread') {
                return;
            }
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
        gl.bufferSubData(gl.ARRAY_BUFFER, 0, shadowData.subarray(0, offset));
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
            throw new Error('WebGL 上下文已丢失, 请重试');
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
        const dense = parts.filter((part) => part.mesh === 'pearl').length > 48;
        let boundMesh;
        parts.forEach((part) => {
            const mesh = dense && part.mesh === 'pearl' ? 'smallPearl' : part.mesh;
            if (mesh !== boundMesh) {
                bindMesh(scene.meshes[mesh]);
                boundMesh = mesh;
            }
            const turn = part.rotation || 0;
            const twist = part.twist || 0;
            gl.uniform2f(uniforms.uTurn, Math.cos(turn), Math.sin(turn));
            gl.uniform2f(uniforms.uTwist, Math.cos(twist), Math.sin(twist));
            gl.uniform3f(uniforms.uScale, ...(part.stretch || [1, 1, 1]));
            gl.uniform1f(uniforms.uMaterial, part.material);
            gl.uniform3f(uniforms.uTint, ...(part.tint || [1, 1, 1]));
            gl.uniform3f(uniforms.uCenter, ...part.point);
            gl.uniform1f(uniforms.uRadius, part.radius);
            gl.uniform1f(uniforms.uSeed, part.index * 0.037);
            gl.drawElements(gl.TRIANGLES, scene.meshes[mesh].count, gl.UNSIGNED_SHORT, 0);
        });
        const pearl =
            pickedPart?.index === selection
                ? pickedPart
                : parts.find((part) => part.index === selection);
        if (pearl) {
            const point = geometry.viewPoint(pearl.point, matrix, depth);
            const halo = scene.halo.uniforms;
            gl.useProgram(scene.halo.linked);
            gl.uniformMatrix4fv(halo.uProjection, false, projection);
            gl.uniform3f(halo.uCenter, point[0], point[1], point[2] - 0.3);
            gl.uniform1f(halo.uRadius, (pearl.pickRadius || pearl.radius) * 1.31);
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

    /** 在真实观察空间拾取, 由父页决定最终选中项. */
    function select(x, y) {
        pickedPart = geometry.pick(
            parts,
            geometry.rotation(yaw, pitch),
            distance / zoom,
            x - centerX,
            y - centerY,
            SPREAD,
            viewportRatio
        );
        return pickedPart ? pickedPart.index : -1;
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
    }

    try {
        scene = {
            quad: buffer(),
            shadows: buffer(),
            meshes: {},
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
                    'uTint',
                    'uSeed',
                    'uTurn',
                    'uTwist',
                    'uScale',
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
        shadowData = new Float32Array(128 * 6 * 5);
        upload(gl.ARRAY_BUFFER, scene.shadows, shadowData, gl.DYNAMIC_DRAW);
        Object.entries(MESHES).forEach(([name, factory]) => {
            const mesh = {vertex: buffer(), index: buffer()};
            uploadMesh(factory(), mesh, gl.STATIC_DRAW);
            scene.meshes[name] = mesh;
        });
    } catch (error) {
        dispose();
        throw error;
    }
    /** 恢复完整正面取景, 保留当前材料选中项. */
    function resetView() {
        yaw = INITIAL_YAW;
        pitch = INITIAL_PITCH;
        zoom = 1;
        centerX = centerY = 0;
        projection[8] = projection[9] = 0;
    }

    return {fit, draw, rotate, scale, select, setScene, setSelection, resetView, dispose};
}

module.exports = {createRenderer};
