/* 清理微信页面模板残留, 只自动处理语法树可证明无业务语句的部分. */
const fs = require('node:fs');
const path = require('node:path');
const {createRequire} = require('node:module');
const {execFileSync} = require('node:child_process');
const root = path.resolve(__dirname, '../..');
const requireUi = createRequire(path.join(root, 'apps/admin-ui/package.json'));
const {Linter} = requireUi('eslint');

const lifecycleNames = new Set([
    'onLoad',
    'onReady',
    'onShow',
    'onHide',
    'onUnload',
    'onReachBottom',
]);

/** 删除空生命周期和未使用的无副作用局部绑定, 保留分享能力与所有事件处理器. */
const cleanup = {
    meta: {
        type: 'suggestion',
        schema: [],
        fixable: 'code',
        messages: {residue: '移除页面模板残留.'},
    },
    /** 使用词法引用和语句列表识别可安全移除的页面模板残留. */
    create(context) {
        const source = context.sourceCode;
        return {
            'CallExpression[callee.name="Page"] > ObjectExpression > Property'(node) {
                const value = node.value;
                if (
                    !lifecycleNames.has(node.key.name) ||
                    value.type !== 'FunctionExpression' ||
                    value.body.body.length
                ) {
                    return;
                }
                context.report({
                    node,
                    messageId: 'residue',
                    fix(fixer) {
                        const following = source.getTokenAfter(node);
                        const end =
                            following && following.value === ','
                                ? following.range[1]
                                : node.range[1];
                        return fixer.removeRange([node.range[0], end]);
                    },
                });
            },
            VariableDeclaration(node) {
                if (node.declarations.length !== 1) {
                    return;
                }
                const declaration = node.declarations[0];
                const init = declaration.init;
                if (!init || declaration.id.type !== 'Identifier') {
                    return;
                }
                const safe =
                    init.type === 'ThisExpression' ||
                    (init.type === 'CallExpression' &&
                        init.callee.name === 'getApp' &&
                        !init.arguments.length) ||
                    (init.type === 'MemberExpression' &&
                        init.object.type === 'Identifier' &&
                        init.object.name === 'event' &&
                        init.property.name === 'currentTarget');
                if (!safe) {
                    return;
                }
                const variable = source.getDeclaredVariables(node)[0];
                if (!variable || variable.references.some((reference) => reference.isRead())) {
                    return;
                }
                context.report({node, messageId: 'residue', fix: (fixer) => fixer.remove(node)});
            },
        };
    },
};

/** 对受版本管理的小程序脚本执行有限清理, 每个文件只在确有差异时写入. */
function main() {
    const names = execFileSync('git', ['ls-files', '-z', '--', 'apps/miniapp'], {
        cwd: root,
        encoding: 'utf8',
    })
        .split('\0')
        .filter((name) => name.endsWith('.js'));
    const linter = new Linter();
    let changed = 0;
    for (const name of names) {
        const file = path.join(root, name);
        const input = fs.readFileSync(file, 'utf8');
        const result = linter.verifyAndFix(input, [
            {
                languageOptions: {ecmaVersion: 'latest', sourceType: 'commonjs'},
                plugins: {project: {rules: {cleanup}}},
                rules: {'project/cleanup': 'error'},
            },
        ]);
        if (result.messages.length) {
            throw new Error(name + ': ' + result.messages[0].message);
        }
        if (result.fixed) {
            fs.writeFileSync(file, result.output);
            changed++;
        }
    }
    console.log(`小程序模板残留清理: ${names.length}个脚本, ${changed}个文件调整.`);
}

main();
