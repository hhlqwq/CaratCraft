/** 检查有序实例上限, 每次编辑返回新数组供撤回使用. */
function edit(ids, action, index, optionId, count = 1) {
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
        if (!Number.isSafeInteger(count) || count < 1 || next.length + count > 2048) {
            throw new Error('排列请求超过2048项处理容量');
        }
        if (action === 'copy' && (index < 0 || index >= next.length)) {
            return next;
        }
        const id = action === 'copy' ? next[index] : Number(optionId);
        if (!Number.isSafeInteger(id) || id <= 0) {
            throw new Error('材料无效');
        }
        next.splice(Math.max(0, Math.min(next.length, index + 1)), 0, ...Array(count).fill(id));
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

/** 扩展链沿用原有稳定编码, 新材料从商家配置读取连接职责. */
function isExtender(option) {
    if (option?.componentType !== 'clasp') return false;
    try {
        return option.optionCode === 'client-extension' ||
            JSON.parse(option.materialParams || '{}')?.claspRole === 'EXTENDER';
    } catch (error) {
        return false;
    }
}

/** 从唯一的材料排列生成管理列表, 不计算二维坐标. */
function describe(ids, options) {
    const byId = new Map(options.map((option) => [Number(option.id), option]));
    return ids.map((id, index) => {
        const option = byId.get(id);
        return {index, optionId: id, name: option?.name || '已停用材料', type: option?.componentType || 'unknown', extender: isExtender(option)};
    });
}

module.exports = {edit, describe, isExtender};
