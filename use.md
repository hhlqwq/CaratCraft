# CaratCraft部署与迁移指南

当前后端为JDK21、Spring Boot3.2.5和MyBatis-Plus3.5.10, 管理端为Vue3、Element Plus和Vite.
功能修复和测试范围以[整改台账](docs/remediation-status.md)为准. 原审查附件是整改前记录, 2026-10-14交付包不是后续修复的最新构建.

## 新建开发环境

1. 安装并启动Docker及Compose V2, 在仓库根目录操作.
2. Windows先运行`build-local-test.cmd`构建镜像, 成功后运行`start-local-test.cmd`启动环境; Linux/macOS运行`./dev.sh`. 首次创建`vendor/platform-wxshop/deploy/.env`时生成独立随机数据库、Redis和JWT秘密, 不在终端打印.
3. 运行已有环境前先阅读下一节, 启动脚本不会自动修改现有数据库密码或账号.
4. 管理端默认`http://localhost:8888`, 后台路径`/platform-framework`, 商城路径`/platform-framework-api/app/`. 端口由`.env`中的`NGINX_PORT`统一指定.
5. 小程序开发版通过`wx-mall/config/environment.js`配置服务地址. 真机无法用localhost访问电脑, 请使用可达的开发服务地址; 体验/正式版必须配置HTTPS并加入微信合法域名.

首次构建会获取镜像和构建依赖, 需要网络和磁盘空间. 本指南不是执行指令记录, 不代表已构建或重启当前机器的容器.

Windows构建脚本默认使用本机缓存, 具体条件见[缓存构建说明](docs/cached-build.md). 无缓存的新电脑可用`build-local-test.cmd -OnlineBuild`执行上述联网构建; 在线模式不自动填充宿主机离线缓存. 日常启动不依赖镜像仓库连通性.

Windows两个入口职责独立: `build-local-test.cmd`委托`dev.ps1 -Action build`, 只构建镜像; `start-local-test.cmd`委托`dev.ps1 -Action start`, 只启动本地镜像并等待就绪. 构建成功后不会自动启动或替换正在运行的容器, 需再运行启动脚本. 直接运行`.\dev.ps1`同样只启动. `-NoBuild`保留兼容旧命令, 构建时不能使用; 传统构建器兼容方式为`build-local-test.cmd -LegacyBuild`. 镜像缺失或源码标识不匹配时要求先构建, 启动不会回退到网络构建. 未提交修改统一标记dirty, 该标识不能区分两次未提交修改的内容, 修改应用源码后必须主动构建. Linux/macOS入口仍默认构建, 使用`./dev.sh --no-build`执行日常启动.

## 启动失败与部分初始化恢复

`platform-mysql healthy`只证明数据库可连接. 如果API报`sys_region`不存在或管理端报`mall_order`不存在, 先盘点全部表与迁移版本, 不能只补第一张报错表. 初始化SQL仅在空数据目录首次启动时执行; 更新源码或重启MySQL不会补跑中断后的SQL.

1. 查看`.\dev.ps1 -Action status`与`.\dev.ps1 -Action logs`, 结合启动失败时输出的健康检查结果定位. `HealthCheck`类不存在时必须重建后端镜像; 放宽探针不能修复旧应用或缺表.
2. 保留现有数据卷, 暂停项目应用写入, 备份数据库、上传文件、私有配置和更新前镜像. 数据库备份应使用`--single-transaction --hex-blob`, 避免Quartz二进制数据被文本编码损坏, 并在独立库恢复核验行数.
3. 对部分初始化的旧库, 先在备份副本逐项确认缺失的基础表. 只补建确认不存在的表; 地区表可恢复仓库地区字典, 缺失的交易等业务表不导入演示记录. 不对当前库执行含`DROP TABLE`的完整`base.sql`或`sys_region.sql`, 不重跑演示种子和旧目录清理脚本.
4. 在副本执行下一节的V版本迁移, 验证既有记录、唯一约束和版本校验台账, 然后将已验证的增量步骤应用到目标库. 部分执行过但缺少台账的版本需先核对DDL和数据, 不强行标记成功.
5. 构建并更新两个后端与管理端, 验证内部就绪探针、管理台和公开API. 数据库结构恢复不会补回原本未导入的演示商品或真实业务记录.

启动预检只检查关键基础表与最新迁移记录, 最终就绪仍由应用探针判断. 构建失败时先处理基础镜像或依赖仓库的网络连接; `LegacyBuild`不保证解决Docker Hub网络故障. 本次使用本地缓存编译、封装镜像及保留数据的恢复证据见[启动恢复记录](docs/startup-recovery-2026-10-21.md).

## 已有数据库升级

不要通过`ResetData`升级生产或需要保留的数据. 它会删除数据库和缓存, 需要先备份并明确确认.

1. 先备份MySQL数据库和上传文件卷, 备份存放在受保护位置, 不提交Git. 同时记录当前镜像/提交和部署配置, 并验证备份可恢复.
2. 保留原数据库卷. 使用数据库管理账号为当前数据库建立独立的`caratcraft`应用账号, 只授权`SELECT, INSERT, UPDATE, DELETE`; 数据库结构迁移使用单独管理账号.
3. 在私有`.env`配置`MYSQL_APP_USER`和`MYSQL_APP_PASSWORD`, 使之与实际数据库账号一致. 更改`.env`不会自动更改已有MySQL账号密码. root密码也须通过数据库自身的账号管理流程轮换后同步配置.
4. 配置非空且足够强的`REDIS_PASSWORD`, 服务端与客户端须一致. Redis配置统一为`spring.data.redis.*`; 未跟踪的本机配置如仍使用`spring.redis.*`, 请由维护者一并迁移.
5. 保留正确的JWT密钥可以减少会话失效; 旧密钥为空或不足256位时须生成新值并安排客户端重新登录.
6. 使用下述版本迁移工具检查并应用迁移, 再由运维安排应用更新. 此操作可能包含DDL, MySQL的DDL不能保证随事务全部回滚, 须先验证备份和幂等重跑行为.

迁移工具默认只读计划, 只发现`V日期_序号_名称.sql`, 不执行旧演示种子、旧目录清理或全库重建脚本. 它使用数据库命名锁、`CC_SCHEMA_MIGRATION`台账和SHA256校验, 已执行文件发生变化时拒绝继续; 新改动须新建版本.

```powershell
# 在当前进程配置MIGRATION_JDBC_URL、MIGRATION_JDBC_USER和MIGRATION_JDBC_PASSWORD.
# URL示例: jdbc:mariadb://127.0.0.1:3306/实际数据库?sslMode=disable
# 驱动路径填写随交付提供或自行取得的MariaDB Connector/J确切版本.
java -cp '<MariaDB驱动路径>' tools/SchemaMigrationTool.java --plan
java -cp '<MariaDB驱动路径>' tools/SchemaMigrationTool.java --apply
```

新建数据库由`tools/build-migration-baseline.py`生成的入口按顺序执行相同V版本并记录校验值. 历史初始化脚本只用于新建空库, 不作为已有商家数据库的升级程序. 原来按ID段或全表删除品牌、专题、帮助和商品的清理逻辑已撤销.

## 配置与访问边界

- 数据库和Redis只绑定宿主127.0.0.1, 应用容器通过内部服务名访问. 生产网络仍应由部署者限制外部入口并提供HTTPS.
- 微信小程序、公众号、支付的变量通过Compose共享环境块统一传入两个后端. 变量为空表示未配置, 不能视为支付已就绪.
- `WX_PAY_APPID`必须与实际小程序应用匹配; 预支付使用当前用户`maOpenid`. 回调地址必须是支付平台可以访问的真实地址.
- 支付证书放置于私有`deploy/secrets/apiclient_cert.p12`, 只读挂载给后端. 证书不上传图片库、不放入Nginx静态目录.
- 本地图片上传限制5MB和2000万像素, JPEG/PNG/GIF会重新编码为PNG. Nginx请求体上限6MB. SVG、HTML和证书不通过图片接口托管.
- 管理端使用同源接口地址. 小程序体验/正式环境不读取本机storage中的API覆盖值.
- 应用对象缓存切换到`caratcraft:v2:`命名空间, 旧缓存保留并自然过期, 购买上下文需要重新建立. 不执行`FLUSHALL`或删除其他应用缓存.

## 日常运维

| 操作 | Windows | Linux/macOS |
|---|---|---|
| 构建镜像 | `build-local-test.cmd` | `docker compose --env-file vendor/platform-wxshop/deploy/.env -f vendor/platform-wxshop/docker-compose.yml build` |
| 查看状态 | `.\dev.ps1 status` | `./dev.sh status` |
| 查看日志 | `.\dev.ps1 logs` | `./dev.sh logs` |
| 启动 | `start-local-test.cmd` | `./dev.sh` |
| 重启 | `.\dev.ps1 restart` | `./dev.sh restart` |
| 停止 | `.\dev.ps1 stop` | `./dev.sh stop` |
| 重建可丢弃测试数据 | `.\dev.ps1 -ResetData` | `./dev.sh --reset-data` |

重置仅删除当前项目明确解析出的MySQL与Redis卷, 上传图片卷保留, 并要求输入确认词. Bash重置功能需要Python3解析Compose配置; 脚本不会猜测卷名或删除其他项目的数据.

内部`/internal/health/live`只检查应用HTTP存活; `ready`检查数据库、迁移结构/台账和Redis, 失败返回503. Nginx不公开内部探针. 构建标识由Git提交生成, 存在未提交内容时标记dirty. `healthy`也不等同于微信支付、短信或真机业务已经验证.

## 验证方式

```powershell
node --test tools/test-client-regressions.cjs
```

Java只运行明确选择的安全用例, 不默认执行外部集成示例. 原真实邮件、支付和微信示例保持禁用. Redis测试须显式设置`CARATCRAFT_REDIS_TEST=true`; 数据库测试须设置`CARATCRAFT_DATABASE_TEST=true`, 并且URL必须指向本机`caratcraft_refactor_test_日期`独立库.

`tools/prepare-integration-database.py`只创建独立测试表结构. 测试不调用真实支付、退款、短信或商家账号. 微信开发者工具、真机、平台支付联调及正式域名验证须由实际部署者提供相应环境, 未验证项继续在台账中注明.

商用交付须同时保留[许可与对应源码材料](vendor/platform-wxshop/legal/README.md). 不将整个旧Git历史作为已清理的商用源码交付物.
