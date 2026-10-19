/** 检查有序实例上限, 每次编辑返回新数组供撤回使用. */
function edit(ids, action, index, optionId) {
  var next = ids.slice();
  if (action === 'insert' || action === 'copy') {
    if (next.length >= 300) throw new Error('最多添加300个材料实例');
    if (action === 'copy' && (index < 0 || index >= next.length)) return next;
    var id = action === 'copy' ? next[index] : Number(optionId);
    if (!Number.isSafeInteger(id) || id <= 0) throw new Error('材料无效');
    next.splice(Math.max(0, Math.min(next.length, index + 1)), 0, id);
  } else if (action === 'remove') {
    if (index >= 0 && index < next.length) next.splice(index, 1);
  } else if (action === 'left' || action === 'right') {
    var target = index + (action === 'left' ? -1 : 1);
    if (index >= 0 && index < next.length && target >= 0 && target < next.length) {
      var value = next[index]; next[index] = next[target]; next[target] = value;
    }
  } else if (action === 'clear') next = [];
  return next;
}

/** 按商家尺寸生成二维排列, 未知尺寸显式标记; 线材只显示定位符号. */
function preview(ids, options) {
  var indexed = {};
  options.forEach(function (option) { indexed[Number(option.id)] = option; });
  var nodes = ids.map(function (id, index) {
    var option = indexed[id] || {name: '已停用材料', componentType: 'unknown'};
    var params = {};
    try { params = JSON.parse(option.materialParams || '{}'); } catch (error) { /* 未知尺寸以提示呈现. */ }
    var pearl = String(option.componentType).toLowerCase() === 'pearl';
    var chain = String(option.componentType).toLowerCase() === 'chain';
    var size = Number(pearl ? params.diameterMm : chain ? params.lengthMm : params.spanMm);
    var known = size > 0 && size <= 3000;
    return {index: index, optionId: id, name: option.displayName || option.name,
      type: option.componentType, pearl: pearl, known: known, sizeMm: known ? size : null,
      weight: chain ? 10 : known ? size : 10};
  });
  var total = nodes.reduce(function (sum, node) { return sum + node.weight; }, 0);
  var offset = 0;
  var maxWeight = nodes.reduce(function (maximum, node) { return Math.max(maximum, node.weight); }, 1);
  var scale = Math.min(2 * Math.PI * 37 / Math.max(1, total), 18 / maxWeight);
  nodes.forEach(function (node) {
    var angle = (offset + node.weight / 2) / total * Math.PI * 2 - Math.PI / 2;
    node.x = 50 + Math.cos(angle) * 39;
    node.y = 50 + Math.sin(angle) * 37;
    node.diameter = node.weight * scale;
    offset += node.weight;
  });
  return nodes;
}

module.exports = {edit: edit, preview: preview};
