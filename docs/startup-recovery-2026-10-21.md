# 本地启动恢复记录

日期: 2026-10-21, Asia/Shanghai. 本次在用户确认后执行工作区修改、数据库备份与增量修复、镜像构建和项目应用更新. 以下为本次恢复后的状态, README中此前的本机健康记录是历史检查结果.

## 根因

- `start-local-test.cmd`此前固定`up --no-build`, 更新源码仍运行2026-09-01构建的后端镜像. 新版Compose的Java健康检查需要`HealthCheck.class`, 旧镜像缺少该类, 两个后端均无法通过健康检查.
- 当前库只保留61张表, 缺失18张基础表, API因`sys_region`不存在反复退出, 管理端定时任务查询`mall_order`失败. 初次初始化不完整的具体历史错误已不在当前MySQL日志中, 因此不声称确认了当时中断的原因.
- 既有库还缺少全部11个V版本迁移, 以及旧初始化脚本创建的`mall_goods.delete_time`字段和`idx_goods_recycle`索引. 后者会让游客首页在容器健康时仍返回500.

## 代码修复

1. Windows分为两个独立脚本: `build-local-test.cmd`委托`dev.ps1 -Action build`, 只构建镜像; `start-local-test.cmd`委托`dev.ps1 -Action start`, 只启动通过校验的本地镜像. 两个入口均转发参数并保留失败退出码. 直接运行`dev.ps1`同样只启动, 不构建.
2. Windows及Bash启动流程先构建或核对镜像源码标识, 启动数据服务后检查基础表和最新迁移记录, 最后等待所有应用就绪. 失败时输出状态和健康检查结果.
3. 管理端镜像也记录`BUILD_REVISION`. 免构建模式拒绝缺失或源码标识不匹配的镜像. dirty标识不能区分两次未提交修改的内容, 继续修改应用源码后仍应重新构建.
4. 新增`V20261021_03_goods_recycle_schema.sql`, 幂等补齐回收站字段和索引. 不更改历史删除时间、商品记录、菜单或自动清理任务. 新建库迁移入口已重新生成, 共12个版本.
5. SQL通过标准输入传给Docker, 兼容Windows PowerShell 5.1的原生命令引号处理. 新增回归脚本使用UTF-8 BOM, 保证该版本正确解析中文.
6. 构建失败时提示查看上方构建错误及对应镜像/依赖仓库网络, 不再输出缺表和后端健康诊断, 不继续启动. 免构建模式不会触发构建或自动回退到网络构建.
7. 经用户确认, Windows构建默认改用本机缓存, 通过禁网Maven容器及本地Node编译后封装镜像, 无需获取node基础镜像. 显式`-OnlineBuild`保留标准联网构建, 具体条件与操作见[缓存构建说明](cached-build.md).

## 数据与备份

恢复前停止两个项目后端, 保留MySQL和Redis容器及全部数据卷. 当前库、私有环境配置、上传目录及更新前镜像均已备份到工作区忽略目录`.tools/startup-recovery-20261021/`, 不提交Git. `platform-before.sql`为1,762,458字节, SHA256为`fdc7da4938e391fc49ed1590bd6d36f4e3d7c5983f6f14c534cf392bae9bb56c`.

备份恢复到独立库`caratcraft_recovery_check_20261021`, 原有61张表精确行数全部一致. 基础表参考库为`caratcraft_recovery_base_20261021`, 仅从参考库为当前库补建确认不存在的18张表. 地区字典恢复46,181条, 缺失的订单、SKU等业务表保持空表, 不导入演示交易或覆盖现有商品.

11个原V版本在副本执行成功后应用当前库, 当前库95张表的逐表行数与演练库一致. 完整初始化参考库`caratcraft_init_audit_20261021_5e5b2c`用于进一步比较全部字段与索引, 确认仅剩回收站字段和索引缺失. 新增第12个迁移先在恢复副本和已存在字段的新库验证, 再应用当前库; 业务表行数保持不变. 相关迁移日志、备份校验和操作记录保留在同一忽略目录.

未删除、重建MySQL、Redis或上传数据卷. 独立验证库保留作为本次证据, 不作为应用运行库. 本次未在工作区外创建或清理临时文件.

## 构建与验证

标准Compose构建在获取`node:24-alpine`时遇到Docker Hub认证地址连接超时. 本次后端使用本机已有Maven镜像和依赖缓存离线编译当前源码, 管理端使用本机已有Node运行时和锁定依赖完成Vite生产构建, 再使用已有JRE/Nginx基础镜像封装. 构建上下文保留在备份目录的`backend-image`和`frontend-image`中, 包含当前构建的JAR/静态文件、健康检查类及许可材料. 未修改系统网络或Docker Desktop配置.

验证结果:

- `tools/Test-LocalStartup.ps1`在Windows PowerShell 5.1及PowerShell 7下通过. 覆盖缺表、旧迁移、查询失败、旧镜像、缺镜像、启动阶段顺序、失败诊断和双击入口转发.
- 拆分后无参数执行`start-local-test.cmd`成功, 五个容器持续healthy. 构建成功/失败及完整`-Action build`调度通过隔离Docker替身验证, 只调用构建而不调用容器启动或数据库查询; 本次拆分未重新执行网络构建.
- `dev.sh`通过容器内`bash -n`语法检查; 本机真实启动使用Windows入口, 不把语法检查算作Linux整套部署验收.
- 加入第12个迁移后的完整新库初始化通过, 独立库为`caratcraft_init_audit_20261021_2e710b`; 回收站迁移的缺字段分支、已有字段分支及原版本校验已在独立库验证. 当前库与完整参考库比较, 缺失字段和索引均为0, 迁移台账有12个版本.
- `start-local-test.cmd -NoBuild`实际执行成功, 退出码0. `platform-mysql`、`platform-redis`、`platform-admin`、`platform-api`、`platform-nginx`全部healthy.
- 两个内部`/internal/health/ready`返回`status=ready`, 应用版本2.0.0, 源码标识`efbd6c9-dirty`.
- `GET /`返回HTTP 200; 后台验证码返回HTTP 200及JPEG; 游客首页和分类接口使用POST, 均返回HTTP 200、`success=true`、`code=0`.
- `git diff --check`通过. 本次不把离线构建的`-DskipTests`计为Java测试通过, 未重复运行与启动无关的整套业务用例.

当前可访问`http://localhost:8888/`. 本机已有新镜像, 再次启动可直接双击`start-local-test.cmd`. 首次安装或更新应用源码后先运行`build-local-test.cmd`, 构建成功再运行启动脚本; 构建本身不启动或更新正在运行的服务. 获取新基础镜像仍需要Docker Hub可达. 微信、真实支付、短信与AI不在本次环境恢复验收范围内.
