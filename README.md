# CaratCraft

CaratCraft（克拉匠造，手工克拉·珠宝匠心）是一款面向珠宝定制商家的微信小程序商城。用户可 DIY 项链款式并在小程序内完成微信支付下单；商家通过后台维护商品、规则、订单和生产工单。

## 品牌与 UI 规范

- 小程序品牌名“克拉匠造”，新版以深珠宝绿 `#183d35`、暖金 `#b98545` 和象牙白 `#fffdf8` 为核心色；导航栏标题为“克拉匠造”，tabBar 选中态仍保留暖金色以保证识别度。
- tabBar 为四个页签：首页 / **定制**（designer 一级入口）/ 购物车 / 我的。"定制"页签图标暂复用原专题图标占位，待替换为钻石主题图标；tab 页传参通过 `designerGoodsId` storage 键中转。
- 商品详情页仍保留"开始定制"按钮（switchTab 进入定制器）；原"品牌制造商直供"栏目已移除，首页新增 DIY 定制入口卡。
- 页面不再采用单一商城模板：商品陈列、交易流程、账户档案和身份认证分别使用杂志式、票据式、档案式和珠宝盒式视觉语言；公共排版和空状态由全局样式兜底。

### 待生成图标清单（放入 `wx-mall/static/images/icons/`）

所有页面已改指本地图标路径，按以下文件名生成后放入目录即生效。风格统一要求：扁平线性图标、圆角、灰金色系（主色 `#b47a3f`、辅助灰 `#8c837c`）、透明背景 PNG。

| 文件名 | 用途 | 生成提示词 |
|---|---|---|
| `icon-search.png` | 搜索框放大镜 | 简约扁平灰色放大镜搜索图标，细线条，圆角，透明背景，正方形 81x81 |
| `icon-up.png` | 列表排序上箭头 | 简约扁平细线条向上小箭头图标，暖棕色 #b47a3f，透明背景，正方形 |
| `icon-down.png` | 列表排序下箭头（含默认态） | 简约扁平细线条向下小箭头图标，灰色 #999999，透明背景，正方形 |
| `icon-checkbox.png` | 购物车勾选框未选中 | 简约扁平圆角正方形空心勾选框图标，浅灰描边 #cccccc，透明背景 |
| `icon-checkbox-checked.png` | 购物车勾选框已选中 | 简约扁平圆角正方形实心勾选框图标，金色棕色 #b47a3f 底白色对勾，透明背景 |
| `icon-radio.png` | 单选圆钮未选中 | 简约扁平空心圆形单选按钮图标，浅灰描边 #cccccc，透明背景 |
| `icon-radio-checked.png` | 单选圆钮已选中 | 简约扁平实心圆形单选按钮图标，金色棕色 #b47a3f 底带白色圆点，透明背景 |
| `icon-service-policy.png` | 服务保障徽章 | 简约扁平红色小盾牌保障徽章图标，细腻线条，透明背景，正方形 |
| `icon-address-edit.png` | 地址编辑按钮 | 简约扁平细线条编辑钢笔小图标，灰色 #8c837c，透明背景，正方形 |
| `icon-comment-add.png` | 发表评论按钮 | 简约扁平细线条评论气泡加号图标，金色棕色 #b47a3f，透明背景，正方形 |
| `empty-comment.png` | 评论空状态插画 | 极简浅灰色空评论气泡插画，柔和淡雅，大量留白，扁平风格，透明或浅米白背景，横向构图 |
| `empty-search.png` | 搜索无结果插画 | 极简浅灰色放大镜与虚线问号插画，柔和淡雅，扁平风格，大量留白，横向构图 |
| `empty-address.png` | 无收货地址插画 | 极简浅灰色定位标记与信封插画，柔和淡雅，扁平风格，大量留白，横向构图 |

## 一期目标

- 支持链条、吊坠、材质、长度、刻字和宝石等标准化项链选项。
- 使用二维透明图层提供稳定预览，并通过 XR-Frame 提供可旋转缩放的 3D 预览。
- 服务端校验组合、重新报价，并将设计与价格快照固化到订单。
- 对接微信支付，完成审核、制作、质检、发货的履约闭环。

## 技术方向

- 商城底座：已导入 `vendor/platform-wxshop`，并新增定制器、规则报价、设计快照和工单模块。
- 小程序：原生微信小程序，DIY 页面提供 2D 图层与 XR-Frame 3D 双模式，不申请相机权限、不包含 AR。
- 服务端：Spring Boot、MySQL、Redis。
- 管理端：Vue 与 Element UI。

完整的产品范围、架构、数据模型、接口和实施里程碑见 [项目蓝图](docs/project-blueprint.md)。

## 当前实现

已完成首个可运行的业务闭环：

- 小程序选择项链组件、二维图层预览、刻字和实时服务端报价。
- 登录用户保存设计快照，确认后以立即购买方式进入结算。
- 提交订单时服务端重新校验选项并锁价，禁止客户端篡改定制价格。
- 设计快照与订单商品绑定，微信支付查询或签名回调成功后幂等生成生产工单。
- 管理端提供生产工单列表、状态流转接口和操作日志查询。
- 已支持 XR-Frame 3D 预览及 2D 自动降级；基础项链和选中组件可分别加载 GLB/glTF 模型，不实现 AR。
- 下单事务会重新校验商品、库存、必选组件、优惠券和最终金额，并原子扣减库存、占用优惠券。
- 支付查询与签名回调统一校验微信实付金额，再以事务方式更新订单和幂等生成生产工单。
- 未支付取消或退款完成后恢复库存、释放优惠券并关闭定制流程；进入制作、质检或完成状态后禁止用户直接退款。已发货订单支持退货退款（状态码 402）。
- “我的”页面已提供“我的设计”入口，订单详情会展示该订单绑定的定制预览、设计状态与刻字内容。
- 首页提供款式搜索、关键词快捷检索和八个业务入口，并将“商家成品”与“自己定制”明确分区；搜索结果复用现有商品检索接口，不是静态展示。
- DIY 演示款已提供材质、长度、款式和珍珠种类四个必选分组，每组各三条明确标注的演示选项。生产环境中的真实选项由商家在后台“定制配置”页面新增、修改、删除或完整 JSON 导入。
- 管理后台采用独立的深绿侧栏、暖白内容区和紧凑数据表视觉体系；首页 CPU、内存、磁盘数据来自 `/sys/monitor/server` 实时接口，接口失败时会显示具体原因，不会使用伪造百分比代替。

## 稳定性与部署配置

- Redis 连接池已限制为最多 100 个连接，池耗尽最长等待 2 秒，避免请求无限阻塞。
- 每个 Druid 数据源限制为 30 个活动连接、最长等待 5 秒；当前两个服务各有两个数据源，理论上限为 120，低于 MySQL 默认 151 连接上限。
- 订单列表改为“订单分页 + 订单商品批量查询”，并返回真实订单总数；超时关单任务移除了循环外事务，使每笔订单通过服务层独立提交。
- 已新增 [稳定性索引迁移](vendor/platform-wxshop/_sql/migrations/carat_craft_stability_v1.sql)，已有数据库升级时执行一次即可；它会为订单列表、超时关单和生产工单状态查询创建幂等索引。
- 小程序默认头像改用本地 `static/images/logo.png`，不再依赖失效的 OSS 外链。未发现 `wt-logistics` 插件的调用代码，已移除未启用的插件声明，避免未授权插件阻止小程序编译。
- 后台公众号功能应通过部署环境变量配置 `WX_MP_APPID` 与 `WX_MP_SECRET`。未配置时，公众号标签列表安全返回空数组，后台账号登录不再因微信接口 `appid missing` 弹错；配置后即可恢复标签管理能力。
- 后台首页的小程序概况趋势依赖 `WX_MINIAPP_APPID` 与 `WX_MINIAPP_SECRET`。未配置时，统计接口返回空数据且不请求微信，避免管理员登录后出现 `41002 appid missing`；配置后自动恢复真实统计数据。
- 小程序微信登录同样依赖 `WX_MINIAPP_APPID`、`WX_MINIAPP_SECRET` 与 `JWT_SECRET`。Docker 会将它们注入商城 API 容器；登录页只有在服务端成功签发 token 后才返回购物车或原目标页。
- 定制器读取商品主规格时使用与 Docker MySQL 一致的小写表名 `mall_goods`，定制选项能够正常返回；刻字输入框采用固定高度与行高，避免微信原生控件裁切占位文字。
- 商城实体映射与更新 SQL 统一使用 Docker MySQL 的小写 `mall_*` 表名，避免 Linux 环境中的表名大小写错误。首页新增明确标注为“演示”的商家珠宝成品数据和本地商品图片；定制器提供默认选项、中文组件名、当前选择摘要与本地图层预览。
- 小程序模板已修复购物车和两个地址页的历史损坏闭合标签。所有小程序源码应保持 UTF-8 编码，避免 `�?/view>` 一类内容破坏 WXML 编译。
- 后台登录采用单请求提交：验证码、账号、HTTP 和服务不可达错误只显示一条明确提示，并阻止重复点击造成弹窗堆叠。商品列表支持导入包含基础信息、相册、参数、规格和 SKU 的完整 JSON；“定制配置”支持网页维护 2D 图层、3D 模型、材质参数，并可原子导入整套商品配件配置。
- 后台登录会校验浏览器是否成功保存令牌；接口返回 401 时只显示一次明确的登录失效原因。登录前发出的旧请求即使迟到返回 401，也不会再删除刚签发的新令牌并将用户静默踢回登录页。管理端源码修改后需重新构建并替换 `platform-nginx` 才会生效。
- 小程序用户可同步微信昵称和头像，也可自行修改昵称、选择新头像；手机号在小程序和服务端均按中国大陆手机号规则校验。收藏与足迹页面提供空状态和“去逛逛”入口，足迹会在进入页面时刷新。
- 首页不再加载原商城餐厨、家居等分类楼层，仅展示商家标记的精选/热门珠宝成品。珠宝演示迁移显式执行 `SET NAMES utf8mb4`，避免 Windows 管道导入造成中文二次编码。
- Docker 对外统一使用 Nginx 的 `8888` 端口；容器内部的管理服务 `8080` 与商城服务 `8081` 不直接暴露。小程序默认 API 和 Docker 支付回调均已使用 `http://localhost:8888/platform-framework-api/...`；仅在不使用 Docker 的本地直启场景下使用 `8080/8081`。

## 3D 模型配置

- `CC_CUSTOM_PRODUCT.model_url`：基础项链 GLB/glTF 的 HTTPS 地址。
- `CC_COMPONENT_OPTION.model_url`：链条、吊坠、宝石等选项模型地址；选中后与基础模型组合展示。
- `CC_COMPONENT_OPTION.material_params`：JSON 变换参数，例如 `{"position":[0,0,0],"rotation":[0,0,0],"scale":[1,1,1]}`。
- 各模型应使用一致的坐标原点、尺度和朝向；金属、宝石材质建议直接烘焙进 GLB。资源域名需加入微信小程序合法下载域名。
- 未配置模型、设备不支持 XR-Frame 或模型加载失败时，页面保持或自动回到 2D 预览。

## 本地开发与测试

> 零基础环境搭建与逐步测试手册见 [use.md](use.md)，包含 MySQL/Redis/Node 安装、数据库初始化、启动命令、功能验证清单和常见问题排查。

推荐使用 Docker 一键启动，宿主机不需要安装 JDK、Maven、Node、MySQL 或 Redis。

Windows PowerShell：

```powershell
.\dev.ps1
```

Windows 双击启动：

```text
start-local-test.cmd
```

该启动入口会复用现有镜像且不重新构建代码。它提交 Docker 后台启动后会立即返回；可在 Docker Desktop 中确认容器健康，或直接访问本地地址。

Windows 双击停止：

```text
stop-local-test.cmd
```

停止入口只停止本项目的 Docker 容器，不会关闭 Docker Desktop，也不会删除本地测试数据。

Ubuntu 或 macOS：

```bash
chmod +x dev.sh
./dev.sh
```

首次运行会在容器内构建 Java 后端和 Vue 管理端，然后启动 MySQL、Redis、两个后端服务及 Nginx。

| 操作 | Windows | Ubuntu / macOS |
|---|---|---|
| BuildKit 网络异常兼容启动 | `powershell -File dev.ps1 -LegacyBuild` | `./dev.sh --legacy-build` |
| 启动或更新 | `.\dev.ps1` | `./dev.sh` |
| 不重新构建 | `.\dev.ps1 -NoBuild` | `./dev.sh --no-build` |
| 查看状态 | `.\dev.ps1 -Action status` | `./dev.sh status` |
| 查看日志 | `.\dev.ps1 -Action logs` | `./dev.sh logs` |
| 重启 | `.\dev.ps1 -Action restart` | `./dev.sh restart` |
| 停止 | `.\dev.ps1 -Action stop` | `./dev.sh stop` |
| 清空测试数据重建 | `.\dev.ps1 -ResetData` | `./dev.sh --reset-data` |

`ResetData` 会删除本项目的 MySQL 和 Redis Docker 数据卷，只用于需要全新测试数据的场景。默认启动会保留数据。

> Docker 的初始化 SQL 仅会在 MySQL 数据卷第一次创建时运行。若复用了旧数据卷而后端报 `Table 'platform.sys_region' doesn't exist`，请不要直接使用 `ResetData`：先备份数据，再按 [use.md](use.md) 第三节补导 `vendor/platform-wxshop/_sql/sys_region.sql`。只有明确不需要保留本地测试数据时，才使用 `ResetData` 重建全部初始化表与演示数据。

默认构建使用 BuildKit 缓存。当 Docker Hub 可通过普通 `docker pull` 访问、但 BuildKit 拉取镜像失败时，可使用 `LegacyBuild` / `--legacy-build` 临时关闭 BuildKit 和 Compose Bake；该模式不影响镜像功能，但首次构建会较慢。

后端容器健康检查不依赖 Bash，并兼容 JVM 的 IPv4/IPv6 监听；服务成功监听 8080/8081 后，Docker Compose 才会继续启动 Nginx。若 `docker compose up --wait` 超时，请先运行 `./dev.ps1 -Action logs`（Windows）或 `./dev.sh logs`（Linux/macOS）获取实际容器日志。

启动完成后的默认地址：

- 管理台：`http://localhost:8888`
- 商城 API：`http://localhost:8888/platform-framework-api`
- 小程序 API：`http://localhost:8888/platform-framework-api/app/`

微信开发者工具导入 `vendor/platform-wxshop/wx-mall`。开发工具访问本机 HTTP 接口时，需要开启“不校验合法域名、web-view、TLS 版本以及 HTTPS 证书”。真机测试需配置可访问的 HTTPS 域名，不能使用手机自身的 `localhost`。

不使用 Docker 时仍可手动准备本地依赖，并在 `vendor/platform-wxshop` 下定向运行：`mvn test -pl platform-biz -am "-Dtest=Custom*Test,OrderPaymentServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false"`。底座其他测试包含真实外部副作用，不建议直接全量执行。已有旧版定制表的数据库升级时，手动执行一次 `vendor/platform-wxshop/_sql/migrations/carat_craft_customization_v2.sql`；需要补齐四类 DIY 演示选项的已有环境，再执行一次 `vendor/platform-wxshop/_sql/migrations/carat_craft_demo_components_v2.sql`。该迁移仅重建演示商品 1006002 的选项和组件库存，不影响商家维护的其他商品。

管理后台菜单可由上述种子 SQL 自动导入；如需手工配置，页面 URL 为 `custom/work-order`，对应权限为 `custom:work-order:list` 和 `custom:work-order:update`。工单状态流转由服务端状态机约束（PENDING_REVIEW→REVIEWED→PRODUCING→QUALITY_CHECK→COMPLETED，审核前后均可 CANCELLED）。每次状态变更会记录操作日志到 `CC_WORK_ORDER_LOG` 表，可通过 `GET /custom/work-order/log/{workOrderId}` 查询。

本地自动化测试覆盖服务端报价、必选与互斥组件、设计锁价绑定、支付金额校验、支付后工单幂等，以及 3D 变换参数安全回退。真实微信扣款和真机 GPU 渲染仍需在微信公众平台配置合法 AppID、商户号、证书、回调及资源域名后联调。

系统内置定时任务（通过管理后台"定时任务"菜单配置）：
- `orderTimeoutTask.closeExpiredOrders`：每 5 分钟自动关闭超过 2 小时未支付的订单，释放库存和优惠券

管理端"定制管理"菜单包含两个页面：
- 生产工单：状态流转（服务端状态机约束）与操作日志查看（`CC_WORK_ORDER_LOG` 记录操作人、状态流转和备注）。
- 组件库存：维护每个定制组件选项的库存数量，报价与下单时会校验并扣减对应库存，防止超售。已有数据库需手动执行 `_sql/carat_craft_menu.sql` 中新增的菜单行（ID 2002/200201）导入入口。

“定制配置”页面可启用/停用指定商城商品的定制器，维护预览尺寸、模板、必选组件、2D 图层、3D 模型和材质参数，也支持下载模板并导入完整配件 JSON。导入会先校验再在单个事务中覆盖该商品的配件，失败时整体回滚。新增选项后默认库存为 0，必须在“组件库存”中设置大于 0 的库存后才可参与报价和下单。已有数据库执行一次 `_sql/carat_craft_menu.sql` 即可幂等补充菜单与权限（ID 2003/200301/200302）。

支付回调接口 `/app/pay/notify` 启用 IP 白名单校验（`wx.pay.notifyIpWhitelist`，支持精确 IP 与 CIDR 网段，默认内置微信支付回调通知网段，上线前请与微信支付官方文档核对），白名单之外来源的请求会被直接拒绝。该开关可通过 `wx.pay.notifyIpWhitelistEnabled` 关闭。

## 敏感配置说明

所有敏感配置（微信小程序 AppID/Secret、支付商户号/密钥、JWT 密钥、支付回调地址）已从代码中移除，改为环境变量读取。

- 本地开发：复制 `vendor/platform-wxshop/platform-api/src/main/resources/application-local.yml` 并填写真实配置
- Docker 部署：复制 `deploy/.env.example` 为 `deploy/.env` 并填写真实配置
- 以上两个文件已加入 `.gitignore`，不会被提交

提交代码前请确认未提交支付证书、密钥、数据库密码、真实用户数据或对象存储凭证。

## 前端说明

当前仅保留原生微信小程序（`wx-mall`），已移除 uni-app 版本（`uni-mall`）以减少维护成本。定制功能（designer 页面）仅在 wx-mall 中实现。
