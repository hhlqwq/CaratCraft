import {createRequire} from 'node:module';

const require = createRequire(
    new URL('../vendor/platform-wxshop/platform-admin-ui/package.json', import.meta.url)
);
const js = require('@eslint/js');

/** 工具脚本使用明确的Node环境, 不借用浏览器或微信全局对象. */
export default [
    js.configs.recommended,
    {
        files: ['**/*.{js,cjs,mjs}'],
        languageOptions: {
            ecmaVersion: 'latest',
            globals: Object.fromEntries(
                [
                    'process',
                    '__dirname',
                    '__filename',
                    'Buffer',
                    'URL',
                    'console',
                    'setTimeout',
                    'clearTimeout',
                    'setImmediate',
                    'structuredClone',
                    'fetch',
                    'TextEncoder',
                    'TextDecoder',
                    'performance',
                ].map((name) => [name, 'readonly'])
            ),
        },
        rules: {
            curly: ['error', 'all'],
            'no-var': 'error',
            'prefer-const': 'error',
            'no-unused-vars': ['error', {args: 'none', caughtErrors: 'none'}],
        },
    },
    {
        files: ['**/verify-admin-browser.cjs'],
        languageOptions: {
            // 此文件内的Playwright evaluate回调在页面环境执行.
            globals: Object.fromEntries(
                ['window', 'document', 'getComputedStyle', 'innerWidth'].map((name) => [
                    name,
                    'readonly',
                ])
            ),
        },
    },
];
