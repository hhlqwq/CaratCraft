# CaratCraft 本地环境搭建与测试指南

> 目标读者：第一次接触本项目的人。推荐先使用下方 Docker 方式；后半部分保留手动安装流程，便于不使用 Docker 时排查和开发。

## 零、推荐：Docker 一键启动

Docker 方式不需要在宿主机单独安装 JDK、Maven、Node.js、MySQL 或 Redis。首次使用时，先完成以下环境准备：

1. 安装并启动 Docker Desktop，等待界面显示 Docker Engine 正常运行。
2. 在 PowerShell 中执行 `docker version`，确认 Client 和 Server 均能正常显示；如果只有 Client 信息，说明 Docker Engine 尚未启动或当前用户无权访问。
3. 确认 Docker 能访问 Docker Hub。项目首次构建需要下载 `nginx`、`node`、`eclipse-temurin` 和 `maven` 等基础镜像。

可以先执行以下命令验证 Docker Hub 连通性：

```powershell
docker pull nginx:1.27-alpine
docker pull node:20-alpine
docker pull eclipse-temurin:21-jre
docker pull maven:3.9.11-eclipse-temurin-21
```

四个镜像均成功下载后，在仓库根目录执行首次构建和启动：

```powershell
# 在仓库根目录执行 / Run from the repository root.
.\dev.ps1
```

首次构建会编译 Java 后端和 Vue 管理端，耗时取决于网络与机器性能。成功后会生成以下本地镜像：

```text
carat-craft-backend:local
carat-craft-admin-ui:local
```

之后可以继续使用 `.\dev.ps1`，也可以双击 `start-local-test.cmd` 快速启动。`start-local-test.cmd` 使用 `--no-build` 复用已有镜像，不能代替首次构建；如果本地镜像尚不存在，会出现 `No such image: carat-craft-backend:local` 或 `No such image: carat-craft-admin-ui:local`。

### 0.1 Docker Desktop 代理配置（按需）

如果当前网络不能直接访问 Docker Hub，先启动本机代理软件，并找到它实际监听的 **HTTP 代理端口**或**混合代理端口**。不要直接照抄示例端口，应以代理软件当前显示的端口为准。

先检查该端口是否正在监听：

```powershell
Test-NetConnection 127.0.0.1 -Port <实际端口>
```

只有输出包含 `TcpTestSucceeded : True` 时，Docker 才可能通过该端口访问网络。然后打开 Docker Desktop 的 `Settings → Resources → Proxies`，启用手动代理，并为 HTTP 和 HTTPS 填写：

```text
http://host.docker.internal:<实际端口>
```

保存设置并由 Docker Desktop 应用配置后，再执行前面的 `docker pull` 命令验证。若代理软件仅提供 SOCKS 端口，应优先在代理软件中启用 HTTP 或混合端口，避免把 SOCKS 端口误填为 HTTP 代理。

### 0.2 Docker 启动入口说明

| 场景 | 命令 | 说明 |
| --- | --- | --- |
| 首次启动或代码修改后重新构建 | `.\dev.ps1` | 构建本地镜像并启动全部服务 |
| 已有本地镜像，只需快速启动 | `start-local-test.cmd` | 使用 `--no-build`，不重新构建代码 |
| BuildKit 本身出现兼容问题 | `.\dev.ps1 -LegacyBuild` | 改用传统构建流程，首次构建可能更慢 |
| 仅查看状态 | `.\dev.ps1 -Action status` | 不重新构建 |

`LegacyBuild` 不能绕过基础镜像下载。如果报错发生在 `load metadata for docker.io/library/...`，应先解决 Docker Hub 或代理连通性。

常用操作：

```powershell
.\dev.ps1 -Action status   # 查看状态 / Shows status.
.\dev.ps1 -Action logs     # 查看日志 / Follows logs.
.\dev.ps1 -Action stop     # 停止并保留数据 / Stops and keeps data.
.\dev.ps1 -ResetData       # 清空测试数据并重建 / Resets test data.
```

启动成功后访问 `http://localhost:8888`。商城接口为 `http://localhost:8888/platform-framework-api`。

以下章节仅适用于不使用 Docker、希望分别启动各进程的场景。

## 一、系统组成（一共 6 个进程）

| 进程 | 端口 | 作用 | 启动方式 |
| --- | --- | --- | --- |
| MySQL 8 | 3306 | 数据库 | 手动前台运行 |
| Redis | 6379 | 缓存/登录令牌 | 双击 exe |
| platform-api | 8081 | 商城后端接口（小程序用） | Maven 命令 |
| platform-admin | 8080 | 管理后台接口 | Maven 命令 |
| platform-admin-ui | 8000 | 管理后台网页前端 | npm run dev |
| 微信开发者工具 | - | 小程序模拟器 | 图形界面导入 |

启动顺序：MySQL → Redis → platform-api → platform-admin → admin-ui。
谁报"连接失败"就先检查它的上游是否在运行。

## 二、首次环境准备（每台电脑只做一次）

### 2.1 安装 MySQL 8（免安装 zip 版）

1. 到 https://dev.mysql.com/downloads/mysql/ 下载 **ZIP Archive** 版。
2. 解压到 `D:\software\mysql`（确保 `D:\software\mysql\bin\mysqld.exe` 存在）。
3. 打开 PowerShell 执行：

```powershell
# 初始化数据目录（仅首次，成功后无输出）
& "D:\software\mysql\bin\mysqld.exe" --initialize-insecure --console
```

### 2.2 设置 root 密码

先临时前台启动 MySQL（窗口保持开着），另开一个 PowerShell 窗口执行设密码命令：

```powershell
# 窗口 A：前台运行 MySQL
& "D:\software\mysql\bin\mysqld.exe" --console

# 窗口 B：设置密码为 REDACTED_CREDENTIAL（与项目配置一致），成功时无任何输出
& "D:\software\mysql\bin\mysql.exe" -uroot --skip-password -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'REDACTED_CREDENTIAL';"

# 验证密码生效（能打印出数字表格即成功）
& "D:\software\mysql\bin\mysql.exe" -uroot -pREDACTED_CREDENTIAL -e "SELECT 1;"
```

> 说明：PowerShell 调用带引号路径必须加 `&` 前缀；注册 Windows 服务（`--install`）需要管理员权限且会开机自启，本项目推荐前台运行，不用了直接关窗口即可。

### 2.3 安装 Redis（Windows 版）

1. 到 https://github.com/tporadowski/redis/releases 下载 `Redis-x64-5.0.14.1.zip`。
2. 解压到 `D:\software\redis`，以后每次双击 `redis-server.exe` 即可。
3. 默认端口 6379、无密码，与项目配置一致，零配置。

### 2.4 安装 Node.js 16（必须是 16）

管理前端是 Vue2 + webpack4 老工程，Node 17 及以上会报 OpenSSL 加密算法错误。

1. 下载：https://npmmirror.com/mirrors/node/v16.20.2/node-v16.20.2-win-x64.zip
2. 解压到 `D:\software\nodejs`（确保 `D:\software\nodejs\node.exe` 存在）。

## 三、数据库初始化（只做一次）

依次执行以下命令。注意两点：
- PowerShell 不支持 `<` 重定向，必须用 `cmd /c '...'` 包一层；
- 新版 mysql 客户端不支持 `-e "source xxx"` 写法。

```powershell
$mysql = "D:\software\mysql\bin\mysql.exe"

# 1. 建库
& $mysql -uroot -pREDACTED_CREDENTIAL -e "CREATE DATABASE IF NOT EXISTS platform DEFAULT CHARACTER SET utf8mb4;"
```

```powershell
# 2. 导入底座基础表（较大，约 1-2 分钟）
cmd /c '"D:\software\mysql\bin\mysql.exe" -uroot -pREDACTED_CREDENTIAL --default-character-set=utf8mb4 platform < D:\code\gitee\carat-craft\vendor\platform-wxshop\_sql\base.sql'

# 3. 商城业务数据
cmd /c '"D:\software\mysql\bin\mysql.exe" -uroot -pREDACTED_CREDENTIAL --default-character-set=utf8mb4 platform < D:\code\gitee\carat-craft\vendor\platform-wxshop\_sql\mall.sql'

# 4. 定制模块 5 张表
cmd /c '"D:\software\mysql\bin\mysql.exe" -uroot -pREDACTED_CREDENTIAL --default-character-set=utf8mb4 platform < D:\code\gitee\carat-craft\vendor\platform-wxshop\_sql\carat_craft_customization.sql'

# 5. 管理后台工单菜单（缺了它后台看不到菜单）
cmd /c '"D:\software\mysql\bin\mysql.exe" -uroot -pREDACTED_CREDENTIAL --default-character-set=utf8mb4 platform < D:\code\gitee\carat-craft\vendor\platform-wxshop\_sql\carat_craft_menu.sql'

# 6. 省市区数据（代码启动必需，缺失会启动失败）
cmd /c '"D:\software\mysql\bin\mysql.exe" -uroot -pREDACTED_CREDENTIAL --default-character-set=utf8mb4 platform < D:\code\gitee\carat-craft\vendor\platform-wxshop\_sql\sys_region.sql'

# 7. 定制模块演示数据（DIY 演示商品）
cmd /c '"D:\software\mysql\bin\mysql.exe" -uroot -pREDACTED_CREDENTIAL --default-character-set=utf8mb4 platform < D:\code\gitee\carat-craft\vendor\platform-wxshop\_sql\carat_craft_demo_data.sql'
```

验证导入结果（应看到 5 张 CC_ 表和 4 条菜单）：

```powershell
& $mysql -uroot -pREDACTED_CREDENTIAL platform -e "SHOW TABLES LIKE 'CC_%'; SELECT COUNT(*) FROM sys_region;"
```

## 四、日常启动（每次开发）

### 窗口 1：MySQL

```powershell
& "D:\software\mysql\bin\mysqld.exe" --console
```

看到 `ready for connections` 即成功，窗口保持开着。

### 窗口 2：Redis

双击 `D:\software\redis\redis-server.exe`。

### 窗口 3：platform-api（商城后端）

```powershell
$env:JAVA_HOME = "D:\code\gitee\carat-craft\.tools\jdk21\jdk-21.0.8+9"
$env:MAVEN_OPTS = "-Dmaven.repo.local=D:\code\gitee\carat-craft\.tools\m2"
Set-Location D:\code\gitee\carat-craft\vendor\platform-wxshop
& "D:\code\gitee\carat-craft\.tools\maven\apache-maven-3.9.11\bin\mvn.cmd" spring-boot:run -pl platform-api
```

成功标志：`Undertow started on port 8081`，之后日志停止滚动、命令**不退出**（服务在前台运行）。

> 首次运行前若报 `Could not find platform-biz`，先执行一次依赖安装：
> `mvn.cmd install -pl platform-biz -am -DskipTests`

### 窗口 4：platform-admin（管理后台后端）

新开窗口，前三行同上，最后一行换成：

```powershell
& "D:\code\gitee\carat-craft\.tools\maven\apache-maven-3.9.11\bin\mvn.cmd" spring-boot:run -pl platform-admin
```

成功标志：`Undertow started on port 8080`。

### 窗口 5：admin-ui（管理后台前端）

```powershell
$env:Path = "D:\software\nodejs;$env:Path"
Set-Location D:\code\gitee\carat-craft\vendor\platform-wxshop\platform-admin-ui
npm run dev
```

成功标志：`Your application is running here: http://localhost:8000` 或 `Compiled successfully`。

### 微信开发者工具

导入目录 `vendor/platform-wxshop/wx-mall`。使用 Docker 时，小程序 API 默认指向 `http://localhost:8888/platform-framework-api/app/`；不使用 Docker 且单独启动 platform-api 时，将 `apiBaseUrl` 覆盖为 `http://localhost:8081/platform-framework-api/app/`。

## 五、功能验证清单

| # | 验证点 | 操作 | 预期 |
| --- | --- | --- | --- |
| 1 | 后端接口连通 | 浏览器或 Postman POST `http://localhost:8081/platform-framework-api/app/customization/config?goodsId=1006002` | 返回 `"code":0`，含商品和组件选项 JSON |
| 2 | 管理后台登录 | 访问 http://localhost:8000 ，使用底座管理员账号登录（默认 admin/admin，以 base.sql 中 SYS_USER 为准，未逐一验证） | 进入首页 |
| 3 | 工单菜单可见 | 左侧"定制管理 → 生产工单" | 列表页正常打开（无数据时为空列表） |
| 4 | 工单状态机 | 对 PENDING 工单直接流转到"已完成" | 提示"不允许从 PENDING 流转到 COMPLETED"；PENDING→REVIEWED→PRODUCING→QUALITY_CHECK→COMPLETED 为合法链路，审核前后均可 CANCELLED |
| 5 | DIY 实时报价 | 小程序进入商品 1006002 的 designer 页，切换链条/吊坠/宝石选项 | 价格随选择实时变化；同类组件互斥（选新吊坠自动取消旧吊坠） |
| 6 | 保存设计并结算 | 填刻字(≤20 字) → 确认设计 → 结算页 → 提交订单 | 下单成功跳转支付页；未配置微信支付时支付会失败属正常 |
| 7 | 取消释放设计 | 对未支付订单执行取消，再回 designer 重新确认同一方案 | 可再次下单（快照已恢复 DRAFT），不报"设计已被占用" |

## 六、单元测试（不需要数据库）

```powershell
$env:JAVA_HOME = "D:\code\gitee\carat-craft\.tools\jdk21\jdk-21.0.8+9"
$env:MAVEN_OPTS = "-Dmaven.repo.local=D:\code\gitee\carat-craft\.tools\m2"
Set-Location D:\code\gitee\carat-craft\vendor\platform-wxshop
& "D:\code\gitee\carat-craft\.tools\maven\apache-maven-3.9.11\bin\mvn.cmd" test -pl platform-biz -am "-Dtest=Custom*Test,OrderPaymentServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false"
```

预期：10 个用例全部通过。不要全量执行 `mvn test`——底座其余测试有真实外部副作用（发邮件、退款等）。

## 七、常见问题排查（全部是实际踩过的坑）

| 现象 | 原因与解决 |
| --- | --- |
| PowerShell 提示"表达式或语句中包含意外的标记" | 带引号路径的命令少了开头 `&`，或用 `.\xxx.exe` 方式调用 |
| `Install/Remove of the Service Denied!` | 注册 Windows 服务需管理员权限；本项目用前台运行可绕过 |
| `ERROR 1049 Unknown database 'platform'` | 忘了先建库，执行第三节的建库语句 |
| `ERROR 1064 ... near 'source xxx'` | 新版 mysql 客户端不支持 source，改用 `cmd /c '... < 文件'` 方式 |
| 启动报 `Could not resolve placeholder 'spring.redis.host'` | yml 里 Redis 配置键必须是 `spring.redis.*` 而不是 `spring.data.redis.*`（本项目 4 个 yml 已修复） |
| 启动报 `Table 'platform.sys_region' doesn't exist` | 漏导 `_sql/sys_region.sql`，补导即可 |
| 后端窗口"没反应" | 不是卡死。启动完成后日志静止、命令不退出就是正常运行中 |
| localhost:8000 打不开 | 管理前端没启动，执行第四节的 npm run dev |
| 登录页请求失败 | Docker 模式访问 `http://localhost:8888`；本地直启模式下管理接口为 8080、管理前端为 8000，确认前端代理地址与启动模式一致 |
| 前端编译报 OpenSSL/EVP 错误 | Node 版本高于 16，换 Node 16 |
| `npm ERR! ENOENT ... package.json` | 执行目录不对，确认路径包含 `platform-wxshop\platform-admin-ui` |
| 控制台中文乱码 | Windows 终端 GBK 与 UTF-8 显示差异，不影响功能 |
| 端口被占用（8080/8081 起不来） | 上一次的 java 进程残留：`taskkill /PID <pid> /F`，或任务管理器结束 java.exe |
| `failed to do request ... EOF` 或 `load metadata for docker.io/library/...` | Docker 无法下载基础镜像。先确认 Docker Desktop 已运行，再按照第 0.1 节检查代理端口，并用 `docker pull` 单独验证基础镜像下载 |
| `proxyconnect ... host.docker.internal:<端口> ... connection failed` | Docker 配置的代理端口没有服务监听，或代理软件未启动；使用 `Test-NetConnection` 检查实际 HTTP/混合端口后更新 Docker Desktop Proxies |
| `No such image: carat-craft-backend:local` | 尚未完成首次构建，却运行了只复用镜像的 `start-local-test.cmd`；改为执行 `.\dev.ps1` |
| `No such image: carat-craft-admin-ui:local` | 尚未完成首次构建，却运行了只复用镜像的 `start-local-test.cmd`；改为执行 `.\dev.ps1` |
| MySQL 日志出现 `ERROR 1064 ... base.sql` | 初始化 SQL 执行失败。即使容器随后显示 `healthy`，业务表也可能不完整；修复 SQL 后需确认测试数据可删除，再执行 `.\dev.ps1 -ResetData` 重新初始化 |

## 八、演示数据说明

`_sql/carat_craft_demo_data.sql` 将在售商品 **1006002**（¥899，库存 100）设为 DIY 款，必选组件：chain、pendant。

| 组件类型 | 选项 | 加价 |
| --- | --- | --- |
| chain 链条 | 18K 金链 / 玫瑰金链 / 铂金 PT950 | +300 / +320 / +800 |
| length 长度 | 40cm / 45cm | +0 / +50 |
| pendant 吊坠 | 六爪单钻坠 / 心形宝石坠 | +1200 / +900 |
| gem 宝石 | 不加宝石 / 配钻 10 分 / 红宝石 30 分 | +0 / +2000 / +1500 |

成交价 = 商品基础价 + 所选选项加价之和，由服务端计算，客户端传价格无效。
