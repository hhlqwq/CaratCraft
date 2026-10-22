# Java服务端

本目录为Java21/Spring Boot3.2.5多模块工程. 统一架构、接口契约与当前状态见[项目开发文档](../docs/项目开发文档.md), 构建部署见[项目维护文档](../docs/项目维护文档.md).

- `platform-common`: 公共响应、异常、安全工具与就绪检查.
- `platform-biz`: 商城、定制、社区、库存及交易业务.
- `platform-admin`: 商家管理API.
- `platform-api`: 小程序公开及本人API.

两个API依赖业务模块, 业务模块依赖公共模块. 业务编排逐步集中在服务层, Controller保持薄层. Java测试随模块源码维护. 管理端和小程序分别位于`../apps/admin-ui/`和`../apps/miniapp/`, 数据库位于`../database/`, 环境入口位于`../env/`.

原底座许可原文保存在[UPSTREAM-LICENSE](../legal/UPSTREAM-LICENSE), 其余版权与对应源码见[发布许可](../legal/README.md). 目录迁移不改变源码版权或重新署名.
