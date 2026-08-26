var assert = require('node:assert/strict');
var test = require('node:test');
var model3d = require('../utils/model3d.js');

/** 验证合法变换参数生成 XR-Frame 配置 / Verifies valid XR-Frame transforms. */
test('creates model with controlled transforms', function () {
  var model = model3d.createXrModel(7, 'https://example.com/pendant.glb', {
    position: [1, 2, 3],
    rotation: [0, 90, 0],
    scale: [0.5, 0.5, 0.5]
  });
  assert.equal(model.assetId, 'cc-model-7');
  assert.equal(model.position, '1 2 3');
  assert.equal(model.rotation, '0 90 0');
  assert.equal(model.scale, '0.5 0.5 0.5');
});

/** 验证异常参数安全回退 / Verifies invalid transforms use safe defaults. */
test('falls back for invalid model parameters', function () {
  var model = model3d.createXrModel('base', '/base.glb', '{bad-json');
  assert.equal(model.position, '0 0 0');
  assert.equal(model.rotation, '0 0 0');
  assert.equal(model.scale, '1 1 1');
});
