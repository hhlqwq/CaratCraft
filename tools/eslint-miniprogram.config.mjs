import {createRequire} from 'node:module';

const require = createRequire(
  new URL('../vendor/platform-wxshop/platform-admin-ui/package.json', import.meta.url)
);
const js = require('@eslint/js');

/** 约束页面入口和具名工具函数的职责说明, 匿名回调由所属函数说明业务目的. */
const functionDescription = {
  meta: {type: 'suggestion', schema: [], messages: {missing: '函数需要说明职责的中文注释.'}},
  /** 为每个源文件建立函数文档检查, 不扫描第三方或生成文件. */
  create(context) {
    /** 检查紧邻函数的注释, 防止将其他字段说明误认为函数文档. */
    function inspect(node) {
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
      'CallExpression[callee.name=/^(Page|App|Component|Behavior)$/] > ObjectExpression > Property'(
        node
      ) {
        if (['FunctionExpression', 'ArrowFunctionExpression'].includes(node.value.type))
          inspect(node);
      },
    };
  },
};

/** 检查原生小程序业务脚本, 微信运行时全局对象由宿主提供. */
export default [
  js.configs.recommended,
  {
    files: ['**/*.js'],
    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'commonjs',
      globals: Object.fromEntries(
        [
          'wx',
          'App',
          'Page',
          'Component',
          'Behavior',
          'getApp',
          'getCurrentPages',
          'console',
          'setTimeout',
          'clearTimeout',
          'setInterval',
          'clearInterval',
        ].map((name) => [name, 'readonly'])
      ),
    },
    plugins: {project: {rules: {'function-description': functionDescription}}},
    rules: {
      'no-unused-vars': ['error', {args: 'none', caughtErrors: 'none'}],
      'no-var': 'error',
      'prefer-const': 'error',
      curly: ['error', 'all'],
      'project/function-description': 'error',
    },
  },
];
