// 与服务端SkuContract保持相同的UTF-16组合长度上限.
const MAX_KEY_LENGTH = 2048;

/** 读取可购买库存, 异常值视为不可购买, 数量上限与服务端保持一致. */
function availableStock(product) {
    const value = product && product.goodsNumber;
    if (typeof value !== 'number' && typeof value !== 'string') {
        return 0;
    }
    const stock = Number(value);
    return Number.isSafeInteger(stock) && stock > 0 ? Math.min(stock, 9999) : 0;
}

/** 将完整选中标识转换为内部索引, 不作为存储或接口格式. */
function selectionKey(ids) {
    return JSON.stringify([...ids].sort());
}

/** 按唯一具名契约一次解析当前SKU, 不接收旧数字组合或猜测损坏值. */
function prepare(products, specifications) {
    const groups = [...specifications].sort((a, b) => a.specificationId - b.specificationId);
    const variants = [];
    const bySelection = new Map();
    for (const product of products) {
        const text = product.goodsSpecificationIds;
        if (typeof text !== 'string' || text.length > MAX_KEY_LENGTH) {
            continue;
        }
        let ids = [];
        if (groups.length) {
            const parts = text.split('|');
            if (parts.length !== groups.length) {
                continue;
            }
            ids = parts.map((part, index) => {
                const group = groups[index];
                const matches = group.valueList.filter(
                    (value) => part === `${group.specificationId}:${value.value}`
                );
                return matches.length === 1 ? String(matches[0].id) : '';
            });
            if (!ids.every(Boolean) || new Set(ids).size !== ids.length) {
                continue;
            }
        } else if (text !== '') {
            continue;
        }
        const key = selectionKey(ids);
        bySelection.set(key, bySelection.has(key) ? null : product);
        variants.push({ids, product});
    }
    return {bySelection, variants};
}

/** 必须唯一命中完整选择, 不以数组第一项、子串或旧格式回退. */
function findProduct(index, selectedIds) {
    return index.bySelection.get(selectionKey(selectedIds)) || null;
}

/** 提取每组唯一选中值, 尚未完整选择时保留空值. */
function selections(specifications) {
    return specifications.map((group) => {
        const checked = group.valueList.filter((value) => value.checked);
        return checked.length === 1 ? String(checked[0].id) : '';
    });
}

/** 复用已解析的组合计算候选库存, 已选项仍可取消以退出无效组合. */
function updateAvailability(specifications, index) {
    const selected = selections(specifications);
    return specifications.map((group, position) => ({
        ...group,
        valueList: group.valueList.map((value) => ({
            ...value,
            disabled: !index.variants.some(
                ({ids, product}) =>
                    availableStock(product) > 0 &&
                    index.bySelection.get(selectionKey(ids)) &&
                    ids.includes(String(value.id)) &&
                    selected.every((id, other) => other === position || !id || ids.includes(id))
            ),
        })),
    }));
}

module.exports = {availableStock, prepare, findProduct, selections, updateAvailability};
