const TRACK_STEPS = 512;
const SIZE_FIELDS = {pearl: 'diameterMm', chain: 'lengthMm', clasp: 'spanMm', accessory: 'spanMm'};

/** 只接受商家配置的有效尺寸, 未知尺寸保留为空. */
function measure(value) {
    return Number.isFinite(value) && value > 0 && value <= 3000 ? value : null;
}

/** 保留实例顺序和材料规格, 与业务排列共享同一组 ID. */
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
        const actualSize = measure(params[SIZE_FIELDS[type]]);
        const rangeMax = type === 'pearl' && option?.referenceOnly ? measure(params.candidateDiameterMaxMm) : null;
        const sizeMm = actualSize || rangeMax;
        const hanging = (type === 'pearl' || type === 'accessory') && params.lengthRole === 'HANGING';
        const spanMm = hanging ? measure(params.mountSpanMm) || sizeMm :
            (type === 'pearl' ? measure(params.axialMm) || sizeMm : sizeMm);
        return {
            index, optionId, type, sizeMm, spanMm, hanging,
            referenceOnly: Boolean(option?.referenceOnly),
            extender: params.claspRole === 'EXTENDER' || option?.optionCode === 'client-extension',
            chainStyle: params.chainStyle || '',
            dropMm: measure(params.dropMm),
            shape: params.shape || '',
            color: params.color || params.variety || '',
            metal: params.metal || params.material || option?.name || '',
            claspStyle: params.claspStyle || option?.name || '',
        };
    });
}

/** 构造连续中心线, 以等弧长定位全部材料和尾扣. */
function createTrack(length) {
    const points = [];
    const distances = [0];
    for (let index = 0; index <= TRACK_STEPS; index++) {
        const angle = (index * 2 * Math.PI) / TRACK_STEPS;
        const point = [-Math.sin(angle), 1.16 * Math.cos(angle), 0];
        if (index) {
            const previous = points[index - 1];
            distances.push(distances[index - 1] + Math.hypot(point[0] - previous[0], point[1] - previous[1]));
        }
        points.push(point);
    }
    const scale = length / distances[TRACK_STEPS];

    /** 在同一条闭合曲线上插值, 支持跨尾扣中心的区间. */
    return function at(distance) {
        const target = (((distance % length) + length) % length) / scale;
        let low = 0;
        let high = TRACK_STEPS;
        while (high - low > 1) {
            const middle = (high + low) >> 1;
            if (distances[middle] <= target) {
                low = middle;
            } else {
                high = middle;
            }
        }
        const fraction = (target - distances[low]) / (distances[high] - distances[low]);
        return points[low].map((value, axis) => (value + fraction * (points[high][axis] - value)) * scale);
    };
}

/** 从声明颜色选择示意色, 未声明的材料保持中性. */
function pearlTint(color) {
    if (/黑|灰|大溪地|black|grey|gray/i.test(color)) return [0.51, 0.55, 0.64];
    if (/真多麻|银灰/i.test(color)) return [0.75, 0.85, 0.99];
    if (/粉|橙|pink|orange/i.test(color)) return [1, 0.80, 0.84];
    if (/金珠|羽黄金|黄|gold|yellow/i.test(color)) return [1, 0.88, 0.61];
    if (/紫|purple|lavender/i.test(color)) return [0.92, 0.83, 1];
    return [1, 1, 1];
}

/** 区分商家已声明的常见金属色, 未声明则显示中性金属. */
function metalTint(metal) {
    if (/玫瑰金|rose gold/i.test(metal)) return [1, 0.69, 0.53];
    if (/黄金|18[Kk] 金|yellow gold/i.test(metal)) return [1, 0.82, 0.47];
    return [0.90, 0.92, 0.96];
}

/** 依据实际配置区分线材、盒仔、肖邦及卡地亚链节, 兼容未标款式的普通链. */
function chainMesh(item) {
    const style = item?.chainStyle || '';
    if (!item || /钢丝|棉线/.test(style)) return 'thread';
    if (/盒仔/.test(style)) return 'box';
    if (/肖邦/.test(style)) return 'chopin';
    if (/卡地亚/.test(style)) return 'cartier';
    return 'chain';
}

/** 将环线区间转换为相接的金属链环或连接线, 无材质叠盖. */
function connect(parts, at, item, start, end, step) {
    if (end - start < 0.01) return;
    const count = Math.max(1, Math.ceil((end - start) / step));
    const stride = (end - start) / count;
    for (let index = 0; index < count; index++) {
        const from = at(start + index * stride);
        const to = at(start + (index + 1) * stride);
        const dx = to[0] - from[0];
        const dy = to[1] - from[1];
        const span = Math.hypot(dx, dy);
        const mesh = chainMesh(item);
        parts.push({
            index: item ? item.index : -1,
            mesh,
            material: item && !/棉线/.test(item.chainStyle || '') ? 1 : 3,
            tint: metalTint(item?.metal),
            point: [(from[0] + to[0]) / 2, (from[1] + to[1]) / 2, (from[2] + to[2]) / 2],
            rotation: Math.atan2(dy, dx),
            radius: span / (mesh === 'thread' ? 2 : 1.65),
            pickRadius: Math.max(0.85, span / 2),
            extent: span / 2,
        });
    }
}

/** 连接配饰挂点和主体, 挂点占位不等同于挂件外形. */
function attachment(parts, item, anchor, radius) {
    const drop = Math.min(2.5, radius * 0.6);
    parts.push({
        index: item.index,
        mesh: 'chain',
        material: 1,
        point: [anchor[0], anchor[1] - drop / 2, anchor[2]],
        rotation: Math.PI / 2,
        radius: drop / 1.65,
        extent: drop,
    });
    return [anchor[0], anchor[1] - radius - drop, anchor[2]];
}

/** 投影当前排列, 少量珠子居中, 串接部件占用的链线区间不再生成链环. */
function previewScene(optionIds, groups, rules = {}, placement = {}) {
    const items = readMaterials(optionIds, groups).map((item) => ({
        ...item,
        span: item.spanMm || (item.type === 'chain' ? 420 : item.hanging ? 4 : 7),
    }));
    const chains = items.filter((item) => item.type === 'chain');
    const clasps = items.filter((item) => item.type === 'clasp').sort((left, right) => Number(left.extender) - Number(right.extender));
    const beads = items.filter((item) => item.type !== 'chain' && item.type !== 'clasp');
    const spacing = Math.max(measure(rules.spacingMm) || 0, measure(placement.spacingMm) || 0);
    const occupied = beads.reduce((total, item) => total + item.span, 0) + Math.max(0, beads.length - 1) * spacing;
    const claspSpan = clasps.reduce((total, item) => total + item.span, 0);
    const chainSpan = chains.reduce((total, item) => total + item.span, 0);
    const useSum = rules.mode === 'SUM';
    const inline = beads.filter((item) => !item.hanging);
    const inlineLength = inline.reduce((total, item) => total + item.span, 0) +
        Math.max(0, inline.length - 1) * spacing;
    const usable = Math.max(occupied, chainSpan + (useSum ? inlineLength : 0), !chains.length && !beads.length ? 0 : 20);
    if (!usable) {
        return {parts: [], optionIds: optionIds.slice(), extent: 12, limitation: ''};
    }
    const at = createTrack(usable + claspSpan);
    const start = claspSpan / 2;
    const end = start + usable;
    const maxOffset = (usable - occupied) / 2;
    const offset = Math.max(-maxOffset, Math.min(maxOffset, Number(placement.offsetMm) || 0));
    const bodyStart = start + maxOffset + offset;
    const bodyEnd = bodyStart + occupied;
    const parts = [];
    const chainStep = Math.max(3.2, Math.max(chainSpan, usable - occupied) / 100);
    let cusp = start - claspSpan;
    for (const clasp of clasps) {
        if (clasp.extender) {
            connect(parts, at, clasp, cusp, cusp + clasp.span, 3.2);
            cusp += clasp.span;
            continue;
        }
        const center = cusp + clasp.span / 2;
        const from = at(cusp);
        const to = at(cusp + clasp.span);
        parts.push({
            index: clasp.index, mesh: /弹簧|spring/i.test(clasp.claspStyle) ? 'chain' : 'clasp',
            material: clasp.sizeMm === null ? 2 : 1, tint: metalTint(clasp.metal),
            point: at(center), radius: Math.hypot(to[0] - from[0], to[1] - from[1]) / 2,
            rotation: Math.atan2(to[1] - from[1], to[0] - from[0]),
        });
        cusp += clasp.span;
    }

    /** 在金属链的剩余弧段分配实际链段顺序, 穿珠区只保留必要连接. */
    function drawChain(from, to) {
        if (!chains.length) {
            connect(parts, at, null, from, to, 3);
            return;
        }
        const chainDistance = from >= bodyEnd ? from - start - occupied : from - start;
        const visible = usable - occupied;
        let boundary = 0;
        let cursor = from;
        for (const chain of chains) {
            boundary += (chain.span / chainSpan) * visible;
            const segmentEnd = Math.min(to, from + Math.max(0, boundary - chainDistance));
            if (segmentEnd > cursor) {
                connect(parts, at, chain, cursor, segmentEnd, chainStep);
                cursor = segmentEnd;
            }
        }
    }

    let cursor = bodyStart;
    drawChain(start, cursor);
    for (let index = 0; index < beads.length; index++) {
        const item = beads[index];
        const point = at(cursor + item.span / 2);
        const radius = (item.sizeMm || item.span || 7) / 2;
        parts.push({
            index: item.index,
            mesh: item.type === 'pearl' && item.sizeMm !== null ?
                (/巴洛克|异形|baroque/i.test(item.shape) ? 'baroque' : 'pearl') : 'decoration',
            material: item.sizeMm === null ? 2 : item.type === 'pearl' ? 0 : 1,
            tint: pearlTint(item.color),
            point: item.hanging ? attachment(parts, item, point, radius) : point,
            radius,
        });
        if (item.hanging) {
            connect(parts, at, chains[0], cursor, cursor + item.span, chainStep);
        }
        cursor += item.span;
        if (index < beads.length - 1) {
            connect(parts, at, chains[0], cursor, cursor + spacing, chainStep);
            cursor += spacing;
        }
    }
    drawChain(cursor, end);
    const branch = chains.find((chain) => /^[Yy]字链/.test(chain.chainStyle));
    if (branch) {
        const anchor = at(start + usable / 2);
        const drop = branch.dropMm || 24;
        const down = (distance) => [anchor[0], anchor[1] - distance, anchor[2]];
        connect(parts, down, branch, 0, drop, 3.2);
    }
    const extent = parts.reduce((max, part) => Math.max(max, Math.hypot(...part.point) + (part.extent || part.radius)), 12) * 1.08;
    return {
        parts, optionIds: optionIds.slice(), extent,
        limitation: items.some((item) => item.referenceOnly || item.sizeMm === null || item.spanMm === null || (/^[Yy]字链/.test(item.chainStyle) && !item.dropMm))
            ? '部分材料缺少实测尺寸或挂点尺寸，以示意形状展示。'
            : '材质和颜色为预览示意，实物以商家资料为准。',
    };
}

module.exports = {readMaterials, previewScene};
