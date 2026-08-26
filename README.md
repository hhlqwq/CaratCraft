# CaratCraft

CaratCraft（手工克拉，珠宝匠心）是一款面向珠宝定制商家的微信小程序商城。用户可 DIY 项链款式并在小程序内完成微信支付下单；商家通过后台维护商品、规则、订单和生产工单。

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
- 管理端提供生产工单列表和状态流转接口。
- 已支持 XR-Frame 3D 预览及 2D 自动降级；基础项链和选中组件可分别加载 GLB/glTF 模型，不实现 AR。
- 下单事务会重新校验商品、库存、必选组件、优惠券和最终金额，并原子扣减库存、占用优惠券。
- 支付查询与签名回调统一校验微信实付金额，再以事务方式更新订单和幂等生成生产工单。
- 未支付取消或退款完成后恢复库存、释放优惠券并关闭定制流程；进入制作、质检或完成状态后禁止用户直接退款。

## 3D 模型配置

- `CC_CUSTOM_PRODUCT.model_url`：基础项链 GLB/glTF 的 HTTPS 地址。
- `CC_COMPONENT_OPTION.model_url`：链条、吊坠、宝石等选项模型地址；选中后与基础模型组合展示。
- `CC_COMPONENT_OPTION.material_params`：JSON 变换参数，例如 `{"position":[0,0,0],"rotation":[0,0,0],"scale":[1,1,1]}`。
- 各模型应使用一致的坐标原点、尺度和朝向；金属、宝石材质建议直接烘焙进 GLB。资源域名需加入微信小程序合法下载域名。
- 未配置模型、设备不支持 XR-Frame 或模型加载失败时，页面保持或自动回到 2D 预览。

## 本地开发与测试

1. 准备 JDK 21、Maven、MySQL 8 和 Redis。
2. 导入底座 SQL 后，再执行 `vendor/platform-wxshop/_sql/carat_craft_customization.sql`；需要管理后台生产工单菜单时，继续执行 `vendor/platform-wxshop/_sql/carat_craft_menu.sql`。旧版本数据库升级另见 `carat_craft_customization_v2.sql`。
3. 修改 `vendor/platform-wxshop/platform-api/src/main/resources/application-dev.yml` 中的本地数据库与 Redis 配置。
4. 在 `vendor/platform-wxshop` 下定向执行定制模块测试：
   `mvn test -pl platform-biz -am "-Dtest=Custom*Test,OrderPaymentServiceImplTest"`。
   注意：底座其余单元测试含真实外部副作用（发送邮件、退款、生成小程序码等），不要全量执行 `mvn test`。
5. 启动 `platform-api`，微信开发者工具导入 `vendor/platform-wxshop/wx-mall`。小程序 API 默认指向 `http://localhost:8081/platform-framework-api/app/`，也可预先通过 Storage 中的 `apiBaseUrl` 覆盖。

管理后台菜单可由上述种子 SQL 自动导入；如需手工配置，页面 URL 为 `custom/work-order`，对应权限为 `custom:work-order:list` 和 `custom:work-order:update`。工单状态流转由服务端状态机约束（PENDING→REVIEWED→PRODUCING→QUALITY_CHECK→COMPLETED，审核前后均可 CANCELLED）。

本地自动化测试覆盖服务端报价、必选与互斥组件、设计锁价绑定、支付金额校验、支付后工单幂等，以及 3D 变换参数安全回退。真实微信扣款和真机 GPU 渲染仍需在微信公众平台配置合法 AppID、商户号、证书、回调及资源域名后联调。

## 贡献说明

提交前请确认未提交支付证书、密钥、数据库密码、真实用户数据或对象存储凭证。
