import {defineConfig} from 'vite';
import vue from '@vitejs/plugin-vue';
import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';

const root = path.dirname(fileURLToPath(import.meta.url));
const output = process.env.CARATCRAFT_UI_OUTPUT || path.join(root, 'dist');

/** 复制静态资源和实际入包模块清单, 供部署及许可证核验使用. */
function distribution() {
  return {
    name: 'caratcraft-distribution',
    /** 在打包完成的HTML前置运行配置, 保留部署时修改接口地址的能力. */
    transformIndexHtml() {
      return [{tag: 'script', attrs: {src: './static/config/index.js'}, injectTo: 'head-prepend'}];
    },
    closeBundle() {
      fs.cpSync(path.join(root, 'static'), path.join(output, 'static'), {recursive: true});
      fs.mkdirSync(path.join(output, 'config'), {recursive: true});
      fs.copyFileSync(path.join(root, 'static/config/index.js'), path.join(output, 'config/index.js'));
    },
    generateBundle() {
      if (process.env.CARATCRAFT_UI_STATS) {
        const modules = [...this.getModuleIds()].filter(id => !id.startsWith('\0')).map(identifier => ({identifier}));
        fs.writeFileSync(process.env.CARATCRAFT_UI_STATS, JSON.stringify({modules, errors: [], warnings: []}));
      }
    },
  };
}

export default defineConfig({
  base: './',
  publicDir: false,
  plugins: [vue({template: {compilerOptions: {compatConfig: {MODE: 2}}}}), distribution()],
  resolve: {
    alias: [{find: /^vue$/, replacement: '@vue/compat'}, {find: '@', replacement: path.join(root, 'src')}],
    extensions: ['.mjs', '.js', '.json', '.vue'],
  },
  define: {'process.env.NODE_ENV': JSON.stringify(process.env.NODE_ENV || 'production'), 'process.env.OPEN_PROXY': 'false'},
  css: {postcss: {plugins: []}},
  build: {outDir: output, emptyOutDir: true},
  server: {host: '127.0.0.1', port: 8081, strictPort: true},
});
