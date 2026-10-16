# 商家管理后台

运行环境为Node.js 22.13及以上至24.x、pnpm 9.15.9. 当前技术栈为Vue 3.5.44、Element Plus 2.14.7、Vue Router 4、Vuex 4、Axios 1和Vite 8. 业务组件逐页通过Vue官方兼容构建迁移, Element Plus及其内部组件使用Vue3模式. 不再安装Vue2、Element UI旧组件运行代码、webpack4或Gulp工具链.

执行`pnpm install --frozen-lockfile --ignore-scripts`, 然后运行`pnpm run lint`与`pnpm run build`. 开发服务器为`pnpm run dev`, 默认仅监听本机8081端口; 生产输出为`dist/`. 接口地址位于`static/config/index.js`, 默认走当前站点的`/platform-framework`, 由部署Nginx转发. 不使用旧模板中的第三方开发服务器地址.

`src/`保存业务源码, `static/`保存部署静态文件, `vite.config.mjs`负责构建与静态资源复制, `eslint.config.mjs`负责代码检查. 根目录`tools/build-copyright-ui.cjs`可以生成隔离构建与真实模块清单, 供许可核验使用.

第三方原始版权和许可必须随交付保留, 具体见上级`legal/README.md`. 旧Element UI仅保留已核验的MIT图标字体及其CSS, 不代表字体版权属于项目作者. Vue3迁移兼容配置仍是明确的技术债, 后续可以逐页切换到标准Vue3模式, 不能直接删除所有兼容配置而跳过回归.
