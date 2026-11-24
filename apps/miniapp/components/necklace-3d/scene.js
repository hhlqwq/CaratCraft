/** 第一阶段使用现有二维节点作为实际材料的通用三维投影. */
function previewScene(nodes) {
    if (!nodes.length || nodes.length > 96) {
        throw new Error('当前排列使用二维预览');
    }
    const parts = nodes.map((node, index) => {
        const type = String(node.type).toLowerCase();
        const shape = {pearl: 'pearl', chain: 'chain', clasp: 'clasp'}[type] || 'decoration';
        return {
            index,
            mesh: shape,
            material: type === 'pearl' ? 0 : 1,
            point: [(node.x - 50) * 1.5 + (type === 'chain' ? 9 : 0), (50 - node.y) * 1.5, 0],
            radius: Math.min(13, Math.max(2, node.diameter * 0.65)),
        };
    });
    return {
        parts,
        optionIds: nodes.map((node) => node.optionId),
        extent: 77,
        limitation: '3D 外观为通用示意, 制作尺寸和报价以原设计为准。',
    };
}

module.exports = {previewScene};
