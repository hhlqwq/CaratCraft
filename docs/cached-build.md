# Windows缓存构建

`build-local-test.cmd`默认从当前源码执行缓存构建, `start-local-test.cmd`仍只负责启动. 本模式解决本机已有依赖但Docker Hub认证地址连接超时的构建阻塞, 不修改系统DNS、Docker代理或运行中的业务容器.

## 使用方式

```bat
build-local-test.cmd
start-local-test.cmd
```

没有本机缓存的新电脑可以显式执行标准联网构建:

```bat
build-local-test.cmd -OnlineBuild
```

`-LegacyBuild`沿用联网构建, 仅更换构建器, 不保证解决仓库连接超时. 以上两个在线方式不自动填充宿主机的Maven缓存或前端node_modules.

构建不需要数据库、Redis或JWT运行密钥, 不创建或改写私有`.env`. 联网构建在该文件不存在时只使用`.env.example`作为Compose变量来源; 缓存构建不读取密钥正文. 运行配置的生成和校验仍由启动入口负责, 不能将镜像构建成功视为业务环境已可启动.

## 离线前置条件

- 本地基础镜像为`maven:3.9.11-eclipse-temurin-21`、`eclipse-temurin:21-jre`和`nginx:1.27-alpine`. 不需要拉取`node:24-alpine`. 缺失镜像需联网预先拉取或由可信交付材料通过`docker load`导入.
- 本地Node版本为`>=22.13且<25`. 优先查找PATH, 再查找本机Codex随附Node, 也可通过`build-local-test.cmd -NodePath "绝对路径/node.exe"`指定. 不下载或安装运行时.
- Maven依赖缓存默认为工作区`.tools/m2/repository`, 可通过`-MavenCache "绝对路径"`指定其他只读缓存. Maven使用`-o`, 不下载依赖. 缺少的具体依赖由Maven错误给出.
- 管理端已有node_modules, 所有直接依赖版本须与当前package.json中的固定版本一致. 缺少依赖需联网按pnpm锁文件安装. 检查不自动安装依赖, 也不把直接依赖检查等同于验证全部传递依赖; 编译还会检查实际使用到的模块. 本次安装缓存多留有已不引用的Vue兼容包, 不将其自动加入应用.

只读检查可以单独运行:

```powershell
powershell -File tools/Build-CachedImages.ps1 -CheckOnly
```

## 构建与产物

每次在工作区忽略目录`.tools/offline-build/日期时间-随机标识/`保留独立源码副本、JAR、探针、镜像上下文及build.json. 这是持久构建记录, 不使用工作区外临时目录. 后端从没有target目录的新源码副本编译, 排除application-local.yml, 避免旧class或本机秘密配置混入. Maven缓存只读挂载, 不挂载业务数据库或上传数据卷.

后端编译容器使用`--pull=never --network=none`, 源码和Maven缓存只读挂载, class及打包中间文件在本次独立容器文件系统生成, 只将最终JAR写回构建记录目录. 健康检查类也在同一JDK镜像中编译. 管理端使用本机Node和已安装依赖执行Vite生产构建, 输出固定到本次镜像上下文的dist目录并在结束后恢复原环境变量, 失败时不取旧dist封装. 两个镜像均包含最新许可材料, 后端保持非root运行、上传目录权限和HealthCheck.class.

封装阶段只在当前构建进程暂时关闭BuildKit, 使用本地基础镜像并指定`--pull=false --network=none`, 避免BuildKit访问基础镜像元数据的认证地址; 完成后恢复原环境变量. 不修改Docker Desktop配置. 两个带本次构建标识的镜像都封装成功后, 才更新`carat-craft-backend:local`和`carat-craft-admin-ui:local`标签. 构建失败不自动启动服务或替换正在运行的容器.

build.json记录代码标识、Node版本、Maven缓存位置、产物目录与前端锁文件SHA256. 校验值通过.NET计算, 不依赖可选PowerShell模块的Get-FileHash命令. 若业务已运行, 构建后需要再运行启动脚本才会更新应用容器.

## 验证

2026-10-21实测: 无参数执行`build-local-test.cmd`完整成功, 退出码0. 两个后端、Vite前端、HealthCheck及两个运行镜像均完成构建, 记录位于`.tools/offline-build/20261021-234324-98ebba1e/build.json`. 后端编译和镜像封装均禁网, 无node基础镜像拉取. Maven使用`-DskipTests`, 不把编译成功算作Java业务用例通过.

构建记录SHA256经独立核验, 两个JAR均不含application-local配置. 镜像探针、JAR、管理端及许可文件检查通过, 后端默认用户UID为999. 五个业务容器ID与构建前一致且全部健康, 未部署本次镜像.

`tools/test-cached-build.cjs`验证直接依赖完整、缺失及版本不符的处理. `tools/Test-CachedBuild.ps1`验证缺少基础镜像、Maven缓存、Node版本上下界和异常输出、前端依赖检查失败. `tools/Test-LocalStartup.ps1`继续验证构建与启动职责分离及在线构建入口, 并通过不可读配置替身验证构建不读取运行密钥、不生成私有配置. 两套PowerShell替身回归均在Windows PowerShell5.1及PowerShell7通过. 本机完整缓存构建的最终结果以实际构建记录为准, 不把单元替身验证计为镜像构建成功.
