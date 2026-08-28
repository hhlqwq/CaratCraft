# CaratCraft

CaratCraft（克拉匠造，手工克拉·珠宝匠心）是一款面向珠宝定制商家的微信小程序商城。用户可 DIY 项链款式并在小程序内完成微信支付下单；商家通过后台维护商品、规则、订单和生产工单。

## 品牌与 UI 规范

- 小程序品牌名"克拉匠造"，主色为金棕色 `#b47a3f`（tabBar 选中色与定制器一致），导航栏标题"克拉匠造"，图片在data\profile_photo.png。
- tabBar 为四个页签：首页 / **定制**（designer 一级入口）/ 购物车 / 我的。"定制"页签图标暂复用原专题图标占位，待替换为钻石主题图标；tab 页传参通过 `designerGoodsId` storage 键中转。
- 商品详情页仍保留"开始定制"按钮（switchTab 进入定制器）；原"品牌制造商直供"栏目已移除，首页新增 DIY 定制入口卡。
- 遗留待办：约 45 处页面样式仍引用网易严选 CDN 图片素材（图标/背景，功能正常），需设计素材后批量替换为本地图。

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

默认构建使用 BuildKit 缓存。当 Docker Hub 可通过普通 `docker pull` 访问、但 BuildKit 拉取镜像失败时，可使用 `LegacyBuild` / `--legacy-build` 临时关闭 BuildKit 和 Compose Bake；该模式不影响镜像功能，但首次构建会较慢。

启动完成后的默认地址：

- 管理台：`http://localhost:8888`
- 商城 API：`http://localhost:8888/platform-framework-api`
- 小程序 API：`http://localhost:8888/platform-framework-api/app/`

微信开发者工具导入 `vendor/platform-wxshop/wx-mall`。开发工具访问本机 HTTP 接口时，需要开启“不校验合法域名、web-view、TLS 版本以及 HTTPS 证书”。真机测试需配置可访问的 HTTPS 域名，不能使用手机自身的 `localhost`。

不使用 Docker 时仍可手动准备本地依赖，并在 `vendor/platform-wxshop` 下定向运行：`mvn test -pl platform-biz -am "-Dtest=Custom*Test,OrderPaymentServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false"`。底座其他测试包含真实外部副作用，不建议直接全量执行。已有旧版定制表的数据库升级时，手动执行一次 `vendor/platform-wxshop/_sql/migrations/carat_craft_customization_v2.sql`；组件库存功能上线后，已有演示数据的环境还需手动执行一次 `_sql/carat_craft_demo_data.sql`（幂等，会重建演示商品 1006002 的定制选项与初始组件库存），否则定制报价会提示"组件库存不足"。

管理后台菜单可由上述种子 SQL 自动导入；如需手工配置，页面 URL 为 `custom/work-order`，对应权限为 `custom:work-order:list` 和 `custom:work-order:update`。工单状态流转由服务端状态机约束（PENDING_REVIEW→REVIEWED→PRODUCING→QUALITY_CHECK→COMPLETED，审核前后均可 CANCELLED）。每次状态变更会记录操作日志到 `CC_WORK_ORDER_LOG` 表，可通过 `GET /custom/work-order/log/{workOrderId}` 查询。

本地自动化测试覆盖服务端报价、必选与互斥组件、设计锁价绑定、支付金额校验、支付后工单幂等，以及 3D 变换参数安全回退。真实微信扣款和真机 GPU 渲染仍需在微信公众平台配置合法 AppID、商户号、证书、回调及资源域名后联调。

系统内置定时任务（通过管理后台"定时任务"菜单配置）：
- `orderTimeoutTask.closeExpiredOrders`：每 5 分钟自动关闭超过 2 小时未支付的订单，释放库存和优惠券

管理端"定制管理"菜单包含两个页面：
- 生产工单：状态流转（服务端状态机约束）与操作日志查看（`CC_WORK_ORDER_LOG` 记录操作人、状态流转和备注）。
- 组件库存：维护每个定制组件选项的库存数量，报价与下单时会校验并扣减对应库存，防止超售。已有数据库需手动执行 `_sql/carat_craft_menu.sql` 中新增的菜单行（ID 2002/200201）导入入口。

支付回调接口 `/app/pay/notify` 启用 IP 白名单校验（`wx.pay.notifyIpWhitelist`，支持精确 IP 与 CIDR 网段，默认内置微信支付回调通知网段，上线前请与微信支付官方文档核对），白名单之外来源的请求会被直接拒绝。该开关可通过 `wx.pay.notifyIpWhitelistEnabled` 关闭。

## 敏感配置说明

所有敏感配置（微信小程序 AppID/Secret、支付商户号/密钥、JWT 密钥、支付回调地址）已从代码中移除，改为环境变量读取。

- 本地开发：复制 `vendor/platform-wxshop/platform-api/src/main/resources/application-local.yml` 并填写真实配置
- Docker 部署：复制 `deploy/.env.example` 为 `deploy/.env` 并填写真实配置
- 以上两个文件已加入 `.gitignore`，不会被提交

提交代码前请确认未提交支付证书、密钥、数据库密码、真实用户数据或对象存储凭证。

## 前端说明

当前仅保留原生微信小程序（`wx-mall`），已移除 uni-app 版本（`uni-mall`）以减少维护成本。定制功能（designer 页面）仅在 wx-mall 中实现。
