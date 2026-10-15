/* 使用既有webpack配置构建隔离产物, 同时记录实际入包模块. */
const fs = require('fs');
const path = require('path');
const root = path.resolve(__dirname, '..');
const ui = path.join(root, 'vendor/platform-wxshop/platform-admin-ui');
process.chdir(ui);
process.env.NODE_ENV = 'production';
const webpack = require(path.join(ui, 'node_modules/webpack'));
const config = require(path.join(ui, 'build/webpack.prod.conf'));
config.output.path = path.join(root, '.tools/copyright/admin-ui-dist');

/** 保存构建结果, 仅把真正的webpack错误作为失败. */
function finishBuild(error, stats) {
  if (error) {
    console.error(error);
    process.exitCode = 1;
    return;
  }
  const result = stats.toJson({all: false, modules: true, nestedModules: true, errors: true, warnings: true});
  fs.writeFileSync(path.join(root, '.tools/copyright/webpack-stats.json'), JSON.stringify(result));
  console.log(JSON.stringify({errors: result.errors, warnings: result.warnings,
    assets: Object.keys(stats.compilation.assets).length}));
  process.exitCode = stats.hasErrors() ? 1 : 0;
}

console.log('开始构建版权整改后的管理端, 输出至工作区隔离目录.');
webpack(config, finishBuild);
