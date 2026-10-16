# 前端依赖说明

实际版本以`package.json`和`pnpm-lock.yaml`为准. 安装、构建和规范检查步骤见同目录`README.md`.

运行依赖为Vue3及官方迁移兼容构建、Vue Router、Vuex、Element Plus、Axios和Lodash. 复制的DOMPurify与图标字体另保留原始许可证. 旧ECharts监控图表已不再使用, CryptoJS由浏览器Web Crypto替代, 不再作为应用依赖发布.

所有实际入包组件由构建模块清单生成到`../legal/generated/runtime-components.json`, 商业分发义务以该索引及随附原文为准. 开发工具不混入运行组件清单.
