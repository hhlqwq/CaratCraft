/** 解析受控的三维材质和变换参数 / Parses controlled 3D material and transform parameters. */
function parseModelParams(value) {
  if (!value) return {};
  if (typeof value === 'object') return value;
  try {
    return JSON.parse(value);
  } catch (error) {
    return {};
  }
}

/** 将三维向量转换为 XR-Frame 字符串 / Converts a 3D vector to an XR-Frame string. */
function vectorValue(value, fallback) {
  if (!Array.isArray(value) || value.length !== 3) return fallback;
  var valid = value.every(function (item) { return Number.isFinite(Number(item)); });
  return valid ? value.map(Number).join(' ') : fallback;
}

/** 创建安全的 XR-Frame 模型配置 / Creates a safe XR-Frame model configuration. */
function createXrModel(id, modelUrl, materialParams) {
  var params = parseModelParams(materialParams);
  return {
    assetId: 'cc-model-' + id,
    modelUrl: modelUrl,
    position: vectorValue(params.position, '0 0 0'),
    rotation: vectorValue(params.rotation, '0 0 0'),
    scale: vectorValue(params.scale, '1 1 1')
  };
}

module.exports = {
  createXrModel: createXrModel,
  parseModelParams: parseModelParams,
  vectorValue: vectorValue
};
