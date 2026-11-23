const SIDES = 26;
const RINGS = 18;
const TAU = Math.PI * 2;
const TRACK_SAMPLES = 768;
const CLEARANCE = 0.28;
const OPEN_ANGLE = 0.19;
const BASE_SIZES = [6, 7.5, 9, 10.5];

/** 求三维向量长度. */
function length(x, y, z) {
    return Math.sqrt(x * x + y * y + z * z);
}

/** 求两点间直线距离. */
function distance(a, b) {
    return length(b[0] - a[0], b[1] - a[1], b[2] - a[2]);
}

/** 向量长度归一化, 保护软管局部退化处. */
function unit(x, y, z) {
    const scale = 1 / (length(x, y, z) || 1);
    return [x * scale, y * scale, z * scale];
}

/** 构造略微收肩的开放项链曲线, 顶端开口留给搭扣. */
function curve(t) {
    const angle = OPEN_ANGLE + t * (TAU - OPEN_ANGLE * 2);
    const cos = Math.cos(angle);
    return [
        56 * Math.sin(angle) * (1 - 0.13 * cos),
        68 * cos,
        1.05 * Math.cos(2 * angle) + 0.52 * Math.sin(3 * angle),
    ];
}

/** 按弦长确定全曲线抽样的累计弧长. */
function createTrack() {
    const points = [curve(0)];
    const sums = [0];
    for (let index = 1; index <= TRACK_SAMPLES; index++) {
        const point = curve(index / TRACK_SAMPLES);
        sums.push(sums[index - 1] + distance(point, points[index - 1]));
        points.push(point);
    }
    return {points, sums, total: sums[TRACK_SAMPLES]};
}

const track = createTrack();

/** 按弧长找取样点并插值, 缩放尺寸与项链毫米坐标一致. */
function along(s, scale) {
    const value = Math.min(track.total, Math.max(0, s));
    let low = 0;
    let high = TRACK_SAMPLES;
    while (high - low > 1) {
        const mid = (low + high) >> 1;
        if (track.sums[mid] <= value) {
            low = mid;
        } else {
            high = mid;
        }
    }
    const mix = (value - track.sums[low]) / (track.sums[high] - track.sums[low]);
    return track.points[low].map(
        (component, axis) => (component + (track.points[high][axis] - component) * mix) * scale
    );
}

/** 使实际相邻珠心的直线距离等于两半径之和加留隙. */
function pack(diameters, scale) {
    let position = (diameters[0] * 0.5 + 1.1) / scale;
    let previous = along(position, scale);
    const centers = [previous];
    for (let index = 1; index < diameters.length; index++) {
        const span = (diameters[index - 1] + diameters[index]) * 0.5 + CLEARANCE;
        const step = span / scale;
        let low = position + step * 0.998;
        let high = position + step * 1.04;
        if (position + step > track.total || distance(previous, along(high, scale)) < span) {
            return null;
        }
        for (let iteration = 0; iteration < 14; iteration++) {
            const mid = (low + high) * 0.5;
            if (distance(previous, along(mid, scale)) < span) {
                low = mid;
            } else {
                high = mid;
            }
        }
        position = (low + high) * 0.5;
        previous = along(position, scale);
        centers.push(previous);
    }
    const tail = (track.total - position) * scale;
    return tail >= diameters[diameters.length - 1] * 0.5 + 1.1 ? centers : null;
}

/** 计算刚好可容纳所有珠子的最小曲线比例. */
function packScale(diameters) {
    const estimate =
        (diameters.reduce((sum, value) => sum + value, 0) +
            (diameters.length - 1) * CLEARANCE +
            2.2) /
        track.total;
    let low = estimate * 0.98;
    let high = estimate * 1.1;
    for (let index = 0; index < 13; index++) {
        const scale = (low + high) * 0.5;
        if (pack(diameters, scale)) {
            high = scale;
        } else {
            low = scale;
        }
    }
    return high;
}

/** 生成从扣件向中心逐步变大的四档演示珠径. */
function initialDiameters() {
    const count = 44;
    return Array.from({length: count}, (_, index) => {
        const fromEdge = Math.min(index, count - 1 - index);
        return BASE_SIZES[fromEdge < 7 ? 0 : fromEdge < 15 ? 1 : 2];
    });
}

/** 创建分段网格容器. */
function meshData() {
    return {vertices: [], indices: []};
}

/** 添加入射方向平滑的球体, 可缩放为金属铆钉. */
function appendSphere(mesh, center, scale, rings, sides) {
    const start = mesh.vertices.length / 6;
    for (let row = 0; row <= rings; row++) {
        const phi = (Math.PI * row) / rings;
        for (let column = 0; column <= sides; column++) {
            const theta = (TAU * column) / sides;
            const x = Math.sin(phi) * Math.cos(theta);
            const y = Math.cos(phi);
            const z = Math.sin(phi) * Math.sin(theta);
            const normal = unit(x / scale[0], y / scale[1], z / scale[2]);
            mesh.vertices.push(
                center[0] + x * scale[0],
                center[1] + y * scale[1],
                center[2] + z * scale[2],
                ...normal
            );
        }
    }
    appendFaces(mesh, start, rings, sides);
}

/** 追加两三角形构成的网格面. */
function appendFaces(mesh, base, rows, sides) {
    const stride = sides + 1;
    for (let row = 0; row < rows; row++) {
        for (let column = 0; column < sides; column++) {
            const index = base + row * stride + column;
            mesh.indices.push(
                index,
                index + stride,
                index + 1,
                index + 1,
                index + stride,
                index + stride + 1
            );
        }
    }
}

/** 生成共享珠体网格. */
function pearlMesh() {
    const mesh = meshData();
    appendSphere(mesh, [0, 0, 0], [1, 1, 1], RINGS, SIDES);
    return mesh;
}

/** 将有厚度的线材沿连续曲线扫掠成椭圆截面软管. */
function appendTube(mesh, points, widths, heights, sides, closed = false) {
    const first = mesh.vertices.length / 6;
    const last = points.length - 1;
    points.forEach((point, row) => {
        const a = points[row > 0 ? row - 1 : closed ? last - 1 : 0];
        const b = points[row < last ? row + 1 : closed ? 1 : last];
        const tx = b[0] - a[0];
        const ty = b[1] - a[1];
        const tz = b[2] - a[2];
        const side = unit(ty, -tx, 0);
        const up = unit(side[1] * tz, -side[0] * tz, side[0] * ty - side[1] * tx);
        const w = typeof widths === 'number' ? widths : widths[row];
        const h = typeof heights === 'number' ? heights : heights[row];
        for (let column = 0; column <= sides; column++) {
            const angle = (TAU * column) / sides;
            const j = Math.cos(angle);
            const k = Math.sin(angle);
            const normal = unit(
                (side[0] * j) / w + (up[0] * k) / h,
                (side[1] * j) / w + (up[1] * k) / h,
                (up[2] * k) / h
            );
            mesh.vertices.push(
                point[0] + side[0] * j * w + up[0] * k * h,
                point[1] + side[1] * j * w + up[1] * k * h,
                point[2] + up[2] * k * h,
                ...normal
            );
        }
    });
    appendFaces(mesh, first, last, sides);
}

/** 用夹扣局部坐标绘制金属连接件. */
function claspMesh(a, b) {
    const mesh = meshData();
    const tangent = unit(b[0] - a[0], b[1] - a[1], 0);
    const gap = distance(a, b);
    const center = a.map((value, axis) => (value + b[axis]) * 0.5);
    /** 从扣件局部坐标映射回项链平面. */
    function local(x, y, z) {
        return [
            center[0] + x * tangent[0] - y * tangent[1],
            center[1] + x * tangent[1] + y * tangent[0],
            center[2] + z,
        ];
    }
    /** 生成连接环, 两端环略微斜转以提供深度反差. */
    function ring(x, y, rad, lean, wire) {
        const points = Array.from({length: 33}, (_, index) => {
            const theta = (index * TAU) / 32;
            return local(
                x + rad * Math.cos(theta),
                y + rad * Math.sin(theta),
                lean * Math.sin(theta)
            );
        });
        appendTube(mesh, points, wire, wire, 9, true);
    }

    const left = -gap / 2;
    const right = gap / 2;
    ring(left + 1.05, 0, 1.37, 0.43, 0.38);
    ring(right - 1.05, 0, 1.37, -0.53, 0.38);
    ring(left + 2.96, 0.24, 1.15, -0.43, 0.3);
    ring(right - 2.96, -0.06, 1.17, 0.41, 0.32);

    const half = gap / 2 - 3.3;
    const body = [];
    const width = [];
    const height = [];
    for (let index = 0; index <= 55; index++) {
        const angle = 0.29 + (index * 5.52) / 55;
        const c = Math.cos(angle);
        const s = Math.sin(angle);
        body.push(local(half * c, (1.65 + 0.4 * c) * s + 0.3, 0.26 * c));
        width.push(0.55 + 0.26 * (1 - c) + 0.1 * Math.abs(s));
        height.push(0.48 + 0.18 * (1 - c));
    }
    appendTube(mesh, body, width, height, 12);
    appendSphere(mesh, body[0], [0.62, 0.62, 0.58], 10, 14);
    appendSphere(mesh, body[body.length - 1], [0.58, 0.61, 0.55], 10, 14);

    const gate = Array.from({length: 18}, (_, index) => {
        const t = index / 17;
        return local(half * (0.19 + 0.8 * t), -1.08 + 1.58 * t * t, 0.42);
    });
    appendTube(mesh, gate, 0.42, 0.46, 10);
    const lever = [
        local(-half * 0.55, 1.5, 0.0),
        local(-half * 0.37, 2.55, 0.02),
        local(-half * 0.09, 2.9, 0.02),
        local(half * 0.12, 2.64, 0.05),
    ];
    appendTube(mesh, lever, 0.42, 0.5, 10);
    appendSphere(mesh, lever[3], [0.62, 0.57, 0.61], 10, 14);
    appendSphere(mesh, local(-half * 0.5, 0.95, 0.62), [0.83, 0.82, 0.26], 12, 18);
    appendSphere(mesh, a, [0.92, 0.97, 0.91], 10, 14);
    appendSphere(mesh, b, [0.92, 0.97, 0.91], 10, 14);
    return mesh;
}

/** 用当前珠径重新铺排珠子、穿线和扣件. */
function layout(diameters) {
    const scale = packScale(diameters);
    const centers = pack(diameters, scale);
    const start = along(0, scale);
    const end = along(track.total, scale);
    const thread = meshData();
    const points = Array.from({length: 153}, (_, index) =>
        along((track.total * index) / 152, scale)
    );
    appendTube(thread, points, 0.37, 0.34, 7);
    return {
        pearls: centers.map((point, index) => ({point, radius: diameters[index] / 2})),
        clasp: claspMesh(start, end),
        thread,
    };
}

/** 构造供 GPU 及射线命中共同使用的列主序旋转. */
function rotation(yaw, pitch) {
    const cy = Math.cos(yaw);
    const sy = Math.sin(yaw);
    const cx = Math.cos(pitch);
    const sx = Math.sin(pitch);
    return new Float32Array([cy, 0, -sy, sy * sx, cx, cy * sx, sy * cx, -sx, cy * cx]);
}

/** 将局部珠心变换到观察空间. */
function viewPoint(point, matrix, depth) {
    const [x, y, z] = point;
    return [
        matrix[0] * x + matrix[3] * y + matrix[6] * z,
        matrix[1] * x + matrix[4] * y + matrix[7] * z,
        matrix[2] * x + matrix[5] * y + matrix[8] * z - depth,
    ];
}

/** 求与屏幕发出的射线最接近的可见珠体, 按交点深度处理前后重叠. */
function pick(pearls, matrix, depth, x, y, spread, aspect) {
    const ray = unit(x * spread * aspect, y * spread, -1);
    let nearest = Infinity;
    let selected = -1;
    pearls.forEach((pearl, index) => {
        const point = viewPoint(pearl.point, matrix, depth);
        const projection = point[0] * ray[0] + point[1] * ray[1] + point[2] * ray[2];
        const square = point[0] ** 2 + point[1] ** 2 + point[2] ** 2;
        const discriminant = projection ** 2 - square + pearl.radius ** 2;
        if (discriminant >= 0 && projection > 0) {
            const hit = projection - Math.sqrt(discriminant);
            if (hit >= 0 && hit < nearest) {
                selected = index;
                nearest = hit;
            }
        }
    });
    return selected;
}

module.exports = {
    BASE_SIZES,
    CLEARANCE,
    initialDiameters,
    pearlMesh,
    layout,
    rotation,
    viewPoint,
    pick,
};
