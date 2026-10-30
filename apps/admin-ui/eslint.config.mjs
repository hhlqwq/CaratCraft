import functionDescription from '../../tools/quality/function-description-rule.mjs';
import js from '@eslint/js';
import vue from 'eslint-plugin-vue';

/** 解析全部业务源码, 旧规则余额由独立基线约束, 不将新增错误静默忽略. */
export default [
    {ignores: ['src/third-party/**', 'src/element-ui-theme/**', 'dist/**', 'node_modules/**']},
    js.configs.recommended,
    ...vue.configs['flat/essential'],
    {
        files: ['src/**/*.{js,vue}', 'static/config/*.js'],
        languageOptions: {
            globals: Object.fromEntries(
                [
                    'window',
                    'document',
                    'navigator',
                    'console',
                    'location',
                    'sessionStorage',
                    'localStorage',
                    'setTimeout',
                    'clearTimeout',
                    'setInterval',
                    'clearInterval',
                    'URL',
                    'FormData',
                    'Blob',
                    'File',
                    'FileReader',
                    'Image',
                    'DOMParser',
                    'Event',
                    'TextEncoder',
                    'crypto',
                    'btoa',
                    'atob',
                    'process',
                    'require',
                    'module',
                ].map((name) => [name, 'readonly'])
            ),
            parserOptions: {ecmaVersion: 'latest', sourceType: 'module'},
        },
        plugins: {project: {rules: {'function-description': functionDescription}}},
        rules: {
            'project/function-description': 'error',
            'no-unused-vars': ['error', {args: 'none', caughtErrors: 'none'}],
            'no-var': 'error',
            'prefer-const': 'error',
            curly: ['error', 'all'],
            // 历史路由文件名与组件名保留, 新增复用组件继续使用多词名称.
            'vue/multi-word-component-names': 'off',
        },
    },
];
