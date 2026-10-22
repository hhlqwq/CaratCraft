# 商家管理后台

本目录为原生Vue3、Element Plus、Vue Router4、Vuex4和Vite工程, 固定版本以`package.json`与`pnpm-lock.yaml`为准. 不使用Vue2兼容运行时. 旧Element UI仅保留已核验的图标字体及CSS, 不改变第三方权利归属.

开发环境为Node22.13至24.x及pnpm9.15.9. 依赖准备后使用`pnpm run dev`, 默认仅监听本机8081端口; `pnpm run lint`检查业务代码, `pnpm run build`输出`dist/`. 项目统一格式继承仓库根配置.

`src/`保存业务源码, `static/`保存部署静态文件, `vite.config.mjs`负责构建及静态资源复制. 接口运行配置位于`static/config/index.js`, 默认由Nginx转发当前站点的`/platform-framework`.

完整架构、构建和验收边界见[项目开发文档](../../docs/项目开发文档.md)和[项目维护文档](../../docs/项目维护文档.md). 原始声明及对应源码见[发布许可](../../legal/README.md).
