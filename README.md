# CaratCraft

CaratCraft（手工克拉，珠宝匠心）是一款面向珠宝定制商家的微信小程序商城。用户可 DIY 项链款式并在小程序内完成微信支付下单；商家通过后台维护商品、规则、订单和生产工单。

## 一期目标

- 支持链条、吊坠、材质、长度、刻字和宝石等标准化项链选项。
- 使用二维透明图层与 Canvas 实现稳定的实时定制预览。
- 服务端校验组合、重新报价，并将设计与价格快照固化到订单。
- 对接微信支付，完成审核、制作、质检、发货的履约闭环。

## 技术方向

- 商城底座：已导入 `vendor/platform-wxshop`，并新增定制器、规则报价、设计快照和工单模块。
- 小程序：原生微信小程序优先，DIY 页面使用 Canvas 2D。
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
- 预留 `model_url` 和 `material_params` 作为后续 3D 扩展字段，当前不实现 AR。

## 本地开发与测试

1. 准备 JDK 21、Maven、MySQL 8 和 Redis。
2. 导入底座 SQL 后，再执行 `vendor/platform-wxshop/_sql/carat_craft_customization.sql`。
3. 修改 `vendor/platform-wxshop/platform-api/src/main/resources/application-dev.yml` 中的本地数据库与 Redis 配置。
4. 在 `vendor/platform-wxshop` 下执行 `mvn test`，执行单元测试和全模块编译。
5. 启动 `platform-api`，微信开发者工具导入 `vendor/platform-wxshop/wx-mall`。小程序 API 默认指向 `http://localhost:8081/platform-framework-api/app/`，也可预先通过 Storage 中的 `apiBaseUrl` 覆盖。

管理后台菜单可配置页面 URL 为 `custom/work-order`，对应权限为 `custom:work-order:list` 和 `custom:work-order:update`。

本地自动化测试覆盖服务端报价、同类选项互斥、设计保存、订单锁价绑定和支付后工单幂等创建。真实微信扣款仍需在微信公众平台配置合法 AppID、商户号、证书和回调域名后联调。

## 贡献说明

提交前请确认未提交支付证书、密钥、数据库密码、真实用户数据或对象存储凭证。
