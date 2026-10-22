/* 检查项目统一约定, 不读取秘密配置或改写第三方源码. */
const fs = require('node:fs');
const path = require('node:path');
const documents = ['项目开发文档.md', '项目规范文档.md', '项目维护文档.md', '功能使用文档.md'];

/** 只递归指定业务目录, 不进入依赖和生成产物. */
function filesAt(directory) {
    if (!fs.existsSync(directory)) {
        return [];
    }
    return fs.readdirSync(directory, {withFileTypes: true}).flatMap((entry) => {
        if (['node_modules', 'target', 'dist', '.git'].includes(entry.name)) {
            return [];
        }
        const file = path.join(directory, entry.name);
        return entry.isDirectory() ? filesAt(file) : [file];
    });
}

/** 验证唯一文档入口、链接、4空格配置及已统一的源码约定. */
function check(root) {
    const errors = [];
    const directory = path.join(root, 'docs');
    const markdown = filesAt(directory).filter((file) => file.endsWith('.md'));
    const actual = markdown.map((file) => path.relative(directory, file)).sort();
    if (JSON.stringify(actual) !== JSON.stringify([...documents].sort())) {
        errors.push('docs只能保留四份固定中文主文档: ' + actual.join(', '));
    }
    const entries = [
        'README.md',
        'AGENTS.md',
        'legal/README.md',
        'vendor/platform-wxshop/README.md',
        'vendor/platform-wxshop/Agents.md',
        'vendor/platform-wxshop/legal/README.md',
    ].map((file) => path.join(root, file));
    for (const file of [...markdown, ...entries]) {
        if (!fs.existsSync(file)) {
            continue;
        }
        const content = fs.readFileSync(file, 'utf8').replace(/```[\s\S]*?```/g, '');
        for (const match of content.matchAll(/!?\[[^\]]*\]\(([^\s)]+)\)/g)) {
            const target = decodeURIComponent(match[1].replace(/^<|>$/g, ''));
            if (/^[a-z][a-z0-9+.-]*:/i.test(target) || target.startsWith('#')) {
                continue;
            }
            const relative = target.split('#')[0];
            if (relative && !fs.existsSync(path.resolve(path.dirname(file), relative))) {
                errors.push(path.relative(root, file) + ': 链接不存在: ' + relative);
            }
        }
    }
    const config = path.join(root, '.prettierrc.json');
    if (fs.existsSync(config)) {
        const settings = JSON.parse(fs.readFileSync(config, 'utf8'));
        if (settings.tabWidth !== 4 || settings.useTabs !== false) {
            errors.push('Prettier须使用4个空格');
        }
    }
    const editor = path.join(root, '.editorconfig');
    if (
        fs.existsSync(editor) &&
        /indent_size\s*=\s*(?!4\b)\d+/.test(fs.readFileSync(editor, 'utf8'))
    ) {
        errors.push('EditorConfig须统一4个空格');
    }
    const project = path.join(root, 'vendor/platform-wxshop');
    for (const module of ['platform-common', 'platform-biz', 'platform-admin', 'platform-api']) {
        for (const file of filesAt(path.join(project, module, 'src')).filter((name) =>
            name.endsWith('.java')
        )) {
            if (/^import\s+(?:static\s+)?[^;]+\.\*;/m.test(fs.readFileSync(file, 'utf8'))) {
                errors.push(path.relative(root, file) + ': 不允许星号导入');
            }
        }
    }
    for (const file of filesAt(path.join(project, 'wx-mall')).filter((name) =>
        name.endsWith('.js')
    )) {
        if (/\b(?:const|let|var)\s+that\s*=\s*this\b/.test(fs.readFileSync(file, 'utf8'))) {
            errors.push(path.relative(root, file) + ': 不再使用that别名');
        }
    }
    return errors;
}

/** 命令行检查失败返回非零, 供统一门禁使用. */
function main() {
    const errors = check(path.resolve(__dirname, '..'));
    errors.forEach((error) => console.error(error));
    if (errors.length) {
        process.exitCode = 1;
    } else {
        console.log('项目约定检查通过: 固定中文文档、有效链接、4空格、显式导入和词法this.');
    }
}

module.exports = {check};
if (require.main === module) {
    main();
}
