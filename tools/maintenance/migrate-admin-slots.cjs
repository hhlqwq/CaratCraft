/* 按Vue模板AST转换已弃用插槽语法, 仅用于本次Vue3迁移. */
const fs = require('node:fs');
const path = require('node:path');
const root = path.resolve(__dirname, '../../apps/admin-ui');
const {parse} = require(path.join(root, 'node_modules/@vue/compiler-sfc'));

/** 依据原始偏移改写插槽属性, 保留其他模板、注释及脚本. */
function migrate(file) {
    const source = fs.readFileSync(file, 'utf8');
    const {descriptor} = parse(source, {filename: file});
    if (!descriptor.template) {
        return;
    }
    const edits = [];
    /** 遍历模板节点, 将命名与作用域属性合并为同一个v-slot. */
    function visit(node) {
        if (node.type === 1) {
            const slot = node.props.find((prop) => prop.type === 6 && prop.name === 'slot');
            const scope = node.props.find((prop) => prop.type === 6 && prop.name === 'slot-scope');
            if (slot || scope) {
                const directive =
                    '#' +
                    (slot?.value?.content || 'default') +
                    (scope ? '="' + scope.value.content + '"' : '');
                const attrs = [slot, scope].filter(Boolean);
                attrs.forEach((prop) =>
                    edits.push([prop.loc.start.offset, prop.loc.end.offset, ''])
                );
                if (node.tag === 'template') {
                    edits.push([attrs[0].loc.start.offset, attrs[0].loc.start.offset, directive]);
                } else {
                    edits.push([
                        node.loc.start.offset,
                        node.loc.start.offset,
                        '<template ' + directive + '>',
                    ]);
                    edits.push([node.loc.end.offset, node.loc.end.offset, '</template>']);
                }
            }
        }
        for (const child of node.children || []) {
            visit(child);
        }
        for (const branch of node.branches || []) {
            visit(branch);
        }
    }
    const ast = descriptor.template.ast;
    if (!ast) {
        throw new Error('无法读取模板AST: ' + file);
    }
    visit(ast);
    let result = source;
    for (const [start, end, text] of edits.sort((a, b) => b[0] - a[0] || b[1] - a[1])) {
        result = result.slice(0, start) + text + result.slice(end);
    }
    if (edits.length) {
        fs.writeFileSync(file, result);
    }
}

/** 只处理业务Vue文件, 不接触第三方组件代码. */
function walk(directory) {
    for (const entry of fs.readdirSync(directory, {withFileTypes: true})) {
        const file = path.join(directory, entry.name);
        if (entry.isDirectory()) {
            walk(file);
        } else if (file.endsWith('.vue')) {
            migrate(file);
        }
    }
}

walk(path.join(root, 'src'));
console.log('业务模板插槽迁移完成.');
