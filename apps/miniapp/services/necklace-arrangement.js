/** 检查有序实例上限, 每次编辑返回新数组供撤回使用. */
function edit(ids, action, index, optionId) {
    let next = ids.slice();
    if (action === 'replace') {
        const id = Number(optionId);
        if (!Number.isSafeInteger(index) || index < 0 || index >= next.length) {
            throw new Error('请先选中要替换的材料');
        }
        if (!Number.isSafeInteger(id) || id <= 0) {
            throw new Error('材料无效');
        }
        next[index] = id;
    } else if (action === 'insert' || action === 'copy') {
        if (next.length >= 300) {
            throw new Error('最多添加300个材料实例');
        }
        if (action === 'copy' && (index < 0 || index >= next.length)) {
            return next;
        }
        const id = action === 'copy' ? next[index] : Number(optionId);
        if (!Number.isSafeInteger(id) || id <= 0) {
            throw new Error('材料无效');
        }
        next.splice(Math.max(0, Math.min(next.length, index + 1)), 0, id);
    } else if (action === 'remove') {
        if (index >= 0 && index < next.length) {
            next.splice(index, 1);
        }
    } else if (action === 'left' || action === 'right') {
        const target = index + (action === 'left' ? -1 : 1);
        if (index >= 0 && index < next.length && target >= 0 && target < next.length) {
            const value = next[index];
            next[index] = next[target];
            next[target] = value;
        }
    } else if (action === 'clear') {
        next = [];
    }
    return next;
}

/** 判断配饰是否垂挂在主链下方, 未声明时按串入链身处理. */
function isHanging(params) {
    return String(params.lengthRole || '').toUpperCase() === 'HANGING';
}

/** 按商家尺寸生成项链式布局: 链条作底弧, 珠串沿下弧排布, 尾扣居顶, 吊坠垂于底部. */
function preview(ids, options) {
    const indexed = {};
    options.forEach(function (option) {
        indexed[Number(option.id)] = option;
    });
    const nodes = ids.map(function (id, index) {
        const option = indexed[id] || {name: '已停用材料', componentType: 'unknown'};
        let params = {};
        try {
            params = JSON.parse(option.materialParams || '{}');
        } catch (error) {
            /* 未知尺寸以提示呈现. */
        }
        const type = String(option.componentType).toLowerCase();
        const pearl = type === 'pearl';
        const chain = type === 'chain';
        const clasp = type === 'clasp';
        const hanging = type === 'accessory' && isHanging(params);
        const size = Number(pearl ? params.diameterMm : chain ? params.lengthMm : params.spanMm);
        const known = size > 0 && size <= 3000;
        return {
            index: index,
            optionId: id,
            name: option.displayName || option.name,
            type: option.componentType,
            pearl: pearl,
            clasp: clasp,
            hanging: hanging,
            onArc: !chain && !clasp && !hanging,
            image: option.assetUrl || '',
            known: known,
            sizeMm: known ? size : null,
            weight: chain ? 10 : known ? size : 10,
            x: 50,
            y: 50,
            diameter: 8,
        };
    });
    const arcNodes = nodes.filter(function (node) {
        return node.onArc;
    });
    const total = arcNodes.reduce(function (sum, node) {
        return sum + node.weight;
    }, 0);
    const maxWeight = arcNodes.reduce(function (maximum, node) {
        return Math.max(maximum, node.weight);
    }, 1);
    const scale = Math.min((2 * Math.PI * 37) / Math.max(1, total), 18 / maxWeight);
    // 屏幕坐标y向下, 珠串以90°(正下方)为中心沿链身下弧排布, 首个材料在左翼, 顺时针向右翼展开.
    const span = (200 * Math.PI) / 180;
    const start = ((90 + 100) * Math.PI) / 180;
    let offset = 0;
    arcNodes.forEach(function (node) {
        const angle = total > 0 ? start - ((offset + node.weight / 2) / total) * span : start;
        node.x = 50 + Math.cos(angle) * 40;
        node.y = 50 + Math.sin(angle) * 36;
        node.diameter = Math.min(node.weight * scale, 22);
        offset += node.weight;
    });
    // 链条与尾扣固定在顶部汇合处, 垂挂配饰固定在底部中央.
    nodes.forEach(function (node) {
        if (node.onArc) {
            return;
        }
        if (node.hanging) {
            node.x = 50;
            node.y = 90;
            node.diameter = 12;
        } else {
            node.x = 50;
            node.y = 7;
            node.diameter = 8;
        }
    });
    return nodes;
}

module.exports = {edit: edit, preview: preview};
