const INSTANCE_LIMIT = 96;
const TRACK_STEPS = 192;
const GAP_MM = 0.35;
const TYPE_FIELDS = {pearl: 'diameterMm', chain: 'lengthMm', clasp: 'spanMm', accessory: 'spanMm'};

/** 保留商家提供的测量值, 缺失或无效时保留未知. */
function readMaterials(optionIds, groups) {
    const options = new Map();
    for (const group of groups) {
        for (const option of group.options || []) {
            options.set(Number(option.id), option);
        }
    }
    return optionIds.map((optionId, index) => {
        const option = options.get(optionId);
        const type = String(option?.componentType || '').toLowerCase();
        let params;
        try {
            params = JSON.parse(option?.materialParams || '{}') || {};
        } catch (error) {
            params = {};
        }
        const measured = params[TYPE_FIELDS[type]];
        const sizeMm =
            Number.isFinite(measured) && measured > 0 && measured <= 3000 ? measured : null;
        return {
            index,
            optionId,
            type,
            sizeMm,
            hanging: type === 'accessory' && params.lengthRole === 'HANGING',
        };
    });
}

/** 按现有二维语义把链身上的部件沿下弧从左到右排列. */
function pointAt(t, width, height, z = 0) {
    const angle = t * Math.PI * 2;
    return [-width * Math.sin(angle) * (1 - 0.1 * Math.cos(angle)), height * Math.cos(angle), z];
}

/** 采集下弧距离, 为不同直径保留实际显示间距. */
function arcTrack(width, height) {
    const points = [];
    const distances = [0];
    for (let index = 0; index <= TRACK_STEPS; index++) {
        const point = pointAt(0.28 + (0.44 * index) / TRACK_STEPS, width, height, 0.8);
        if (index > 0) {
            const [x, y] = points[index - 1];
            distances.push(distances[index - 1] + Math.hypot(point[0] - x, point[1] - y));
        }
        points.push(point);
    }
    return {points, distances, length: distances[TRACK_STEPS]};
}

/** 通过累计距离定位位置, 保留原始逐项顺序. */
function sample(track, distance) {
    let low = 0;
    let high = TRACK_STEPS;
    while (high - low > 1) {
        const mid = (low + high) >> 1;
        if (track.distances[mid] <= distance) {
            low = mid;
        } else {
            high = mid;
        }
    }
    const fraction =
        (distance - track.distances[low]) / (track.distances[high] - track.distances[low]);
    return track.points[low].map(
        (part, axis) => part + fraction * (track.points[high][axis] - part)
    );
}

/** 计算各部件所占宽度, 只服务几何布局. */
function occupied(items) {
    return items.reduce((sum, item) => sum + item.span + GAP_MM, 0) - (items.length ? GAP_MM : 0);
}

/** 构建绘制部分, 保持其对应的业务实例索引及材质局限. */
function makePart(item, mesh, point, radius, rotation = 0) {
    return {
        index: item.index,
        mesh,
        point,
        radius,
        rotation,
        material: mesh === 'pearl' ? 0 : item.sizeMm === null ? 2 : 1,
    };
}

/** 用同一实例的若干通用链环覆盖对应链段, 不创建额外材料用量. */
function chainParts(chains, width, height, opening) {
    const parts = [];
    const sum = chains.reduce((total, chain) => total + chain.span, 0);
    const extra = Math.max(0, 36 - chains.length);
    let start = opening;
    for (const chain of chains) {
        const portion = chain.span / sum;
        const length = (1 - 2 * opening) * portion;
        const count = 1 + Math.floor(extra * portion);
        for (let index = 0; index < count; index++) {
            const position = start + ((index + 0.5) / count) * length;
            const point = pointAt(position, width, height, -1.1);
            const offset = length / count / 4;
            const a = pointAt(position - offset, width, height);
            const b = pointAt(position + offset, width, height);
            const span = Math.hypot(b[0] - a[0], b[1] - a[1]) * 2;
            parts.push(
                makePart(chain, 'chain', point, span * 0.65, Math.atan2(b[1] - a[1], b[0] - a[0]))
            );
        }
        start += length;
    }
    return parts;
}

/** 由商家材料与唯一的排列 ID 生成只读预览, 无报价、库存和订单语义. */
function previewScene(optionIds, groups) {
    if (!optionIds.length || optionIds.length > INSTANCE_LIMIT) {
        throw new Error('当前排列使用二维预览');
    }
    const materials = readMaterials(optionIds, groups);
    const known = materials.filter((item) => item.sizeMm !== null && item.type !== 'chain');
    // 示例尺度仅供缺参符号绘制, 不写回材料业务属性.
    const symbol = known.length ? Math.min(10, Math.max(3, known[0].sizeMm * 0.75)) : 6;
    const withSpan = materials.map((item) => ({
        ...item,
        span: item.sizeMm === null ? (item.type === 'chain' ? symbol * 20 : symbol) : item.sizeMm,
    }));
    const chains = withSpan.filter((item) => item.type === 'chain');
    const clasps = withSpan.filter((item) => item.type === 'clasp');
    const pendants = withSpan.filter((item) => item.hanging);
    const inline = withSpan.filter(
        (item) => item.type !== 'chain' && item.type !== 'clasp' && !item.hanging
    );
    const inlineWidth = occupied(inline);
    const claspWidth = occupied(clasps);
    const pendantWidth = occupied(pendants);
    const chainLength = chains.reduce((total, chain) => total + chain.span, 0);
    // 环形场景半径只用于取景. 保留真实珠径比例, 不输出工艺或成品长度.
    const width = Math.max(
        symbol * 4.5,
        inlineWidth * 0.46,
        claspWidth * 0.9,
        pendantWidth * 0.53,
        chainLength / (2 * Math.PI * 1.06)
    );
    const height = width * 1.12;
    const track = arcTrack(width, height);
    const opening = claspWidth ? Math.min(0.13, claspWidth / (Math.PI * width * 4)) : 0;
    const parts = chainParts(chains, width, height, opening);

    let cursor = (track.length - inlineWidth) / 2;
    for (const item of inline) {
        const mesh = item.type === 'pearl' && item.sizeMm !== null ? 'pearl' : 'decoration';
        parts.push(makePart(item, mesh, sample(track, cursor + item.span / 2), item.span / 2));
        cursor += item.span + GAP_MM;
    }
    cursor = -claspWidth / 2;
    for (const item of clasps) {
        parts.push(makePart(item, 'clasp', [cursor + item.span / 2, height, 0], item.span / 2));
        cursor += item.span + GAP_MM;
    }
    const inlineRadius = inline.reduce((max, item) => Math.max(max, item.span / 2), 0);
    cursor = -pendantWidth / 2;
    for (const item of pendants) {
        parts.push(
            makePart(
                item,
                'decoration',
                [cursor + item.span / 2, -height - inlineRadius - item.span / 2 - GAP_MM, 0.8],
                item.span / 2
            )
        );
        cursor += item.span + GAP_MM;
    }

    parts.sort((a, b) => a.index - b.index);
    const extent =
        parts.reduce(
            (farthest, part) => Math.max(farthest, Math.hypot(...part.point) + part.radius),
            12
        ) * 1.06;
    const unknown = materials.filter((item) => item.sizeMm === null).length;
    return {
        parts,
        optionIds: optionIds.slice(),
        extent,
        limitation: unknown
            ? '有 ' + unknown + ' 项缺少尺寸，以占位形状显示。材质为通用示意。'
            : '款式和材质为通用示意，不代表实物或制作结果。',
    };
}

module.exports = {readMaterials, previewScene, INSTANCE_LIMIT};
