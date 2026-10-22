/* 格式化完整前端业务源码, 文件范围来自Git而非生成目录. */
const fs = require('node:fs');
const path = require('node:path');
const {execFileSync} = require('node:child_process');
const prettier = require('../../apps/admin-ui/node_modules/prettier');

const root = path.resolve(__dirname, '../..');
const write = process.argv.includes('--write');

/** 只选择当前仍存在的业务文件, 不遍历依赖、构建产物、第三方原件或待提交删除项. */
function businessFiles() {
    const names = fs.existsSync(path.join(root, '.git'))
        ? execFileSync('git', ['ls-files', '--cached', '--others', '--exclude-standard', '-z'], {
              cwd: root,
              encoding: 'utf8',
          }).split('\0')
        : JSON.parse(fs.readFileSync(path.join(root, 'source-files.json'), 'utf8'));
    if (
        !Array.isArray(names) ||
        names.some(
            (name) =>
                typeof name !== 'string' ||
                path.isAbsolute(name) ||
                name.split(/[\\/]/).includes('..')
        )
    ) {
        throw new Error('源码文件清单无效');
    }
    return [...new Set(names)]
        .filter((name) => {
            if (
                [
                    '.prettierrc.json',
                    'apps/admin-ui/package.json',
                    'apps/admin-ui/vite.config.mjs',
                    'apps/admin-ui/eslint.config.mjs',
                ].includes(name)
            ) {
                return true;
            }
            if (name.startsWith('tools/') || name.startsWith('tests/')) {
                return /\.(js|cjs|mjs)$/.test(name);
            }
            if (name.startsWith('apps/admin-ui/static/config/')) {
                return name.endsWith('.js');
            }
            if (name.startsWith('apps/admin-ui/src/')) {
                return (
                    !/\/(third-party|element-ui-theme)\//.test(name) &&
                    /\.(js|vue|css|scss)$/.test(name)
                );
            }
            return name.startsWith('apps/miniapp/') && /\.(js|wxss|json)$/.test(name);
        })
        .filter((name) => fs.existsSync(path.join(root, name)))
        .sort();
}

/** 使用固定版本和根配置检查全部文件, 写入时仅替换格式有差异的文件. */
async function main() {
    const config = JSON.parse(fs.readFileSync(path.join(root, '.prettierrc.json'), 'utf8'));
    const files = businessFiles();
    let changed = 0;
    for (let index = 0; index < files.length; index++) {
        const name = files[index];
        const file = path.join(root, name);
        const source = fs.readFileSync(file, 'utf8');
        const options = {...config, filepath: file};
        if (name.endsWith('.wxss')) {
            options.parser = 'css';
        }
        const formatted = await prettier.format(source, options);
        if (source !== formatted) {
            changed++;
            if (write) {
                fs.writeFileSync(file, formatted);
            } else {
                console.error('格式不一致: ' + name);
            }
        }
        if ((index + 1) % 50 === 0) {
            console.log(`前端格式进度: ${index + 1}/${files.length}`);
        }
    }
    console.log(`前端业务格式${write ? '整理' : '检查'}: ${files.length}个文件, ${changed}个差异.`);
    if (!write && changed) {
        process.exitCode = 1;
    }
}

main().catch((error) => {
    console.error(error.message);
    process.exitCode = 1;
});
