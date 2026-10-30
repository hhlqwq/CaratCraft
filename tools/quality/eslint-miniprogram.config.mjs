import functionDescription from './function-description-rule.mjs';
import {createRequire} from 'node:module';

const require = createRequire(new URL('../../apps/admin-ui/package.json', import.meta.url));
const js = require('@eslint/js');

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
            'object-shorthand': ['error', 'methods'],
            curly: ['error', 'all'],
            'project/function-description': 'error',
        },
    },
];
