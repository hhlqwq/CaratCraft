/* 在实际构建环境提取包元数据和许可原文, 供跨平台只读许可核验. */
const fs = require('node:fs');
const path = require('node:path');
const root = path.resolve(__dirname, '../..');

/** 逐个核对真实模块归属, 缺失依赖立即失败而非跳过许可检查. */
function snapshot() {
    const stats = JSON.parse(
        fs.readFileSync(path.join(root, '.tools/copyright/webpack-stats.json'), 'utf8')
    );
    const output = path.join(root, '.tools/copyright/frontend-node-modules');
    const packages = new Map();
    const modules = [...stats.modules];
    while (modules.length) {
        const module = modules.pop();
        modules.push(...(module.modules || []));
        const identifier = module.identifier.split('!').pop().split('?')[0];
        if (!identifier.includes('/node_modules/')) {
            continue;
        }
        if (!fs.existsSync(identifier)) {
            throw new Error('构建模块不可读取: ' + identifier);
        }
        let folder = path.dirname(identifier);
        while (!fs.existsSync(path.join(folder, 'package.json'))) {
            const parent = path.dirname(folder);
            if (parent === folder) {
                throw new Error('模块缺少包归属: ' + identifier);
            }
            folder = parent;
        }
        const relative = folder.split('/node_modules/').slice(1).join('/node_modules/');
        if (!relative || relative.split('/').includes('..')) {
            throw new Error('模块归属越界');
        }
        packages.set(folder, path.join(output, relative));
    }
    if (!packages.size) {
        throw new Error('实际构建图没有可核验的npm包');
    }
    for (const [folder, destination] of packages) {
        fs.mkdirSync(destination, {recursive: true});
        for (const entry of fs.readdirSync(folder, {withFileTypes: true})) {
            if (
                entry.isFile() &&
                (entry.name === 'package.json' ||
                    /^(LICENSE|LICENCE|NOTICE|COPYING)/i.test(entry.name))
            ) {
                fs.copyFileSync(path.join(folder, entry.name), path.join(destination, entry.name));
            }
        }
    }
    console.log('实际前端包元数据及许可快照: ' + packages.size + '项');
}

snapshot();
