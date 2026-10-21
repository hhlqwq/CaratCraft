/* 检查本地直接依赖版本, 不安装依赖或访问网络. */
const fs = require('node:fs');
const path = require('node:path');

/** 对照项目清单核验所有直接依赖, 输出明确的缺失或版本不符信息. */
function main() {
  const directory = process.argv[2];
  const manifest = JSON.parse(fs.readFileSync(path.join(directory, 'package.json'), 'utf8'));
  const missing = [];
  for (const [name, version] of Object.entries({
    ...manifest.dependencies,
    ...manifest.devDependencies,
  })) {
    let installed;
    try {
      installed = JSON.parse(
        fs.readFileSync(path.join(directory, 'node_modules', name, 'package.json'), 'utf8')
      ).version;
    } catch {
      installed = 'missing';
    }
    if (installed !== version) missing.push(`${name}: required=${version}, installed=${installed}`);
  }
  if (missing.length) throw new Error('Frontend cache incomplete:\n' + missing.join('\n'));
  console.log('Frontend dependency versions verified.');
}

main();
