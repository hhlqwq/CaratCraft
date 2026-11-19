/** 对具名函数、对象方法、构造函数及测试用例检查紧邻的中文职责注释. */
export default {
    meta: {type: 'suggestion', schema: [], messages: {missing: '函数需要说明职责的中文注释.'}},
    /** 所有前端和工具共用语法树规则, 匿名短回调由所属函数说明职责. */
    create(context) {
        /** 检查实际声明前的最后一条注释, 不借用远处字段或文件版权说明. */
        function inspect(node) {
            if (
                ['ExportNamedDeclaration', 'ExportDefaultDeclaration'].includes(node.parent?.type)
            ) {
                node = node.parent;
            }
            const comments = context.sourceCode.getCommentsBefore(node);
            const last = comments[comments.length - 1];
            if (
                !last ||
                node.loc.start.line - last.loc.end.line > 1 ||
                !/[\u4e00-\u9fff]/.test(last.value)
            ) {
                context.report({node, messageId: 'missing'});
            }
        }
        return {
            FunctionDeclaration: inspect,
            MethodDefinition: inspect,
            /** 覆盖普通对象及共享页面工厂, 不仅检查直接传入Page的对象. */
            Property(node) {
                if (!['FunctionExpression', 'ArrowFunctionExpression'].includes(node.value.type)) {
                    return;
                }
                for (let parent = node.parent; parent; parent = parent.parent) {
                    if (
                        parent.type === 'AssignmentExpression' &&
                        parent.left.type === 'MemberExpression' &&
                        parent.left.object.name === 'module' &&
                        parent.left.property.name === 'exports'
                    ) {
                        inspect(node);
                        return;
                    }
                    if (parent.type === 'ExportDefaultDeclaration') {
                        if (node.method || node.value.type === 'FunctionExpression') {
                            inspect(node);
                        }
                        return;
                    }
                    if (parent.type === 'CallExpression') {
                        if (
                            parent.callee.type === 'Identifier' &&
                            ['Page', 'App', 'Component', 'Behavior'].includes(parent.callee.name)
                        ) {
                            inspect(node);
                        }
                        return;
                    }
                    if (parent.type === 'ReturnStatement' && node.method) {
                        inspect(node);
                        return;
                    }
                    if (
                        [
                            'FunctionExpression',
                            'ArrowFunctionExpression',
                            'FunctionDeclaration',
                        ].includes(parent.type)
                    ) {
                        return;
                    }
                }
            },
            /** 箭头工具函数和函数表达式在变量声明处接受文档检查. */
            VariableDeclarator(node) {
                if (
                    node.init &&
                    ['FunctionExpression', 'ArrowFunctionExpression'].includes(node.init.type) &&
                    ['Program', 'ExportNamedDeclaration'].includes(node.parent.parent.type)
                ) {
                    inspect(node.parent);
                }
            },
            /** 测试用例本身需要行为说明, 不仅要求夹具函数有注释. */
            CallExpression(node) {
                if (
                    node.callee.type === 'Identifier' &&
                    ['test', 'it'].includes(node.callee.name)
                ) {
                    inspect(node.parent.type === 'ExpressionStatement' ? node.parent : node);
                }
            },
        };
    },
};
