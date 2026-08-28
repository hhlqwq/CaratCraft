# Docker 部署说明

## 一键开发测试

Docker 会通过多阶段构建自动编译 Java 后端和 Vue 管理端，不需要预先准备 JAR 或 dist。

在仓库根目录执行：

```powershell
.\dev.ps1
```

Ubuntu 或 macOS：

```bash
./dev.sh
```

## 启动

首次启动前，如需调整端口、数据库名或 JVM 参数，先复制并修改：

```bash
cp deploy/.env.example deploy/.env
```

其中 `NGINX_PORT`、数据库密码和 JVM 参数都可以按本机情况修改。`MAVEN_PROFILE` 仅供旧的独立 JAR 构建脚本使用。要启用小程序微信登录，必须填写 `WX_MINIAPP_APPID`、`WX_MINIAPP_SECRET` 和随机的 `JWT_SECRET`；这些变量会注入商城 API 和管理后台容器。

也可以直接执行标准 Compose 命令：

```bash
docker compose --env-file deploy/.env up --build -d --wait
```

## 停止

```bash
scripts/docker-down.sh
```

## 说明

- `mysql` 初始化脚本直接挂载仓库 `_sql/` 目录，首次创建数据卷时按文件名顺序执行；`_sql/migrations` 不会自动执行。已有数据卷需要手动执行 `carat_craft_demo_jewelry_v1.sql`，以写入明确标注为演示的珠宝成品和 DIY 本地图层数据。该脚本包含 `SET NAMES utf8mb4`；Windows 下不要删除这行，否则通过管道导入中文时可能发生二次编码乱码。
- `nginx` 在容器内监听 `80`，宿主机默认通过 `8888` 访问，同时托管前端静态资源并反代两个后端服务。
- 后端容器运行时启用 `docker` profile，只覆盖 MySQL 和 Redis 地址，不改你现有 `dev/test/prod` 配置。
- MySQL 和 Redis 使用 Docker 命名数据卷，普通停止不会丢失数据；只有显式使用 `--reset-data` 或 `-ResetData` 才会删除测试数据。
- `scripts/build-jars.sh` 和 `scripts/build-admin-ui.sh` 保留给需要导出独立部署产物的场景，一键测试不再依赖它们。
