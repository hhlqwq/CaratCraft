/* 构建隔离的管理端产物并记录真实模块, 不覆盖部署页面. */
const path = require('node:path');
const fs = require('node:fs');
const {spawnSync} = require('node:child_process');
const root = path.resolve(__dirname, '../..');
const ui = path.join(root, 'apps/admin-ui');
const env = {
    ...process.env,
    NODE_ENV: 'production',
    CARATCRAFT_UI_OUTPUT: path.join(root, '.tools/copyright/admin-ui-dist'),
    CARATCRAFT_UI_STATS: path.join(root, '.tools/copyright/webpack-stats.json'),
};
console.log('开始构建管理端并生成实际依赖清单.');
fs.mkdirSync(path.dirname(env.CARATCRAFT_UI_STATS), {recursive: true});
const result = spawnSync(
    process.execPath,
    [path.join(ui, 'node_modules/vite/bin/vite.js'), 'build'],
    {cwd: ui, env, stdio: 'inherit'}
);
if (result.error) {
    console.error(result.error.message);
}
process.exitCode = result.status === null ? 1 : result.status;
