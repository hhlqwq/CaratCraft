# AGENTS.md

此文件记录仓库内开发约束, 当前技术事实和部署说明以根README、use.md及整改台账为准.

## 语言

始终使用中文简体回复。

## 仓库结构与职责边界

多模块 Maven 仓库，不要按单体应用处理：

- `platform-admin`：后台管理接口（端口 8080，context-path `/platform-framework`，启动类 `PlatformAdminApplication`）
- `platform-api`：商城/小程序 API（端口 8081，context-path `/platform-framework-api`，启动类 `PlatformApiApplication`）
- `platform-biz`：核心业务逻辑、Service、DAO、MyBatis XML mapper（不是纯公共模块，承载大量真实业务和 SQL）
- `platform-common`：公共配置、工具、缓存
- `platform-admin-ui`：后台管理前端（Vue 2.7.16 + Element UI 2.15.10 + webpack 4，非 Vite/Vue 3）
- `wx-mall`：原生微信小程序
- `_sql`：数据库初始化脚本

## 技术栈

- Spring Boot 3.2.5 / JDK 21 / Undertow
- MyBatis-Plus 3.5.10 + XML mapper
- Shiro 权限 / Redis 缓存 / Knife4j 接口文档

## 构建与运行

后端：
- Maven profile 切换环境：`dev`（默认）/ `test` / `prod`
- 定向编译：`mvn compile -pl platform-admin -am`
- 不要默认执行全量 `mvn test`（部分测试有真实外部副作用：发邮件、退款、企业付款、生成小程序码、同步微信用户）

前端（platform-admin-ui）：
- 开发：`npm run dev`（webpack-dev-server，地址 `http://localhost:8000`）
- 构建：`npm run build`（使用 gulp，非标准 webpack build）
- `baseUrl` 来自 `static/config/index.js` 的 `window.SITE_CONFIG.baseUrl`，同源路径为 `/platform-framework`

小程序（wx-mall）：
- API 基址在 `wx-mall/config/api.js`，环境统一由 `wx-mall/config/environment.js` 选择, 本地默认 `http://localhost:8888/platform-framework-api/app/`

Docker 一键启动：
- `scripts/build-jars.sh && scripts/build-admin-ui.sh` 构建产物
- `cp deploy/.env.example deploy/.env` 后 `scripts/docker-up.sh` 启动
- `scripts/docker-down.sh` 停止

## MyBatis 约束

- mapper 位置：`classpath*:mapper/**/*.xml`
- 实体扫描：`com.platform.modules.*.entity`
- 逻辑删除字段 `isDelete`（0 未删除 / 1 已删除）
- 主键策略 `ASSIGN_UUID`
- LIKE 查询用 `CONCAT('%', #{...}, '%')`，不要拼 `${}`
- 改 XML mapper 时同步检查 DAO 方法签名、实体/VO 字段、前端消费点

## 联调排查

- 后台请求走 `platform-admin`（8080），商城请求走 `platform-api`（8081），不要混淆
- 前端请求失败时先核对 `baseUrl`、服务端口、context-path 是否一致
- 联调异常先排除 profile、本地配置、旧服务实例、前端缓存

## 敏感配置

未经确认不要修改：支付证书、微信 appid/secret、支付密钥、支付宝配置、对象存储配置等。如果问题像"代码没生效"，先排查本地配置和环境 profile，而非改代码。

## 改动原则

- 只改与任务直接相关的文件，不顺手重构或升级依赖
- 匹配现有代码风格，即使较旧
- 未验证的内容必须明确标注"未验证"，不要把推断说成事实

## 当前维护入口

- 版本迁移仅执行`_sql/migrations/V*.sql`, 用`tools/SchemaMigrationTool.java`维护台账, 不在商家数据库重跑初始化种子或历史清理.
- Redis配置使用`spring.data.redis.*`, 应用对象缓存使用版本化键空间.
- 新函数使用中文说明和Google风格, 不为统一格式改写第三方许可或源码.
- 测试/提交/推送遵循当前用户授权; 重启或结束其他程序仍须事先确认.
