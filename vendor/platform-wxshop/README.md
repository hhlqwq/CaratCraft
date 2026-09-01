<p align="center">
 <a target="_blank" href="https://gitee.com/hehailong/platform/blob/master/LICENSE"><img src="https://img.shields.io/github/license/hehailong92/platform-wxshop"/></a>
 <a href='https://gitee.com/hehailong/platform/stargazers'><img src='https://gitee.com/hehailong/platform/badge/star.svg?theme=gvp' alt='star'/></a>
 <a href='https://gitee.com/hehailong/platform/members'><img src='https://gitee.com/hehailong/platform/badge/fork.svg?theme=gvp' alt='fork'/></a>
 <a target="_blank" href="https://gitee.com/hehailong/platform/contributors?ref=master"><img src="https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fgitee.com%2Fhehailong%2Fplatform%2Fcontributors_count%3Fref%3Dmaster&query=contributors_count&label=Contributors&color=green"/></a>
 <a target="_blank" href="http://makeapullrequest.com"><img src="https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square" alt="PRs Welcome" title="PRs Welcome"></a>
</p>
<p align="center">
 <img src="https://img.shields.io/github/languages/count/hehailong92/platform-wxshop?color=red"/>
 <img src="https://img.shields.io/badge/Java-21-blue.svg"/>
 <img src="https://img.shields.io/badge/SpringBoot-2.7.15-blue.svg"/>
 <img src="https://img.shields.io/badge/MyBatisPlus-3.5.3-blue.svg"/>
 <img src="https://img.shields.io/badge/weixin%20java-4.5.2-blue.svg"/>
 <img src="https://img.shields.io/badge/MySQL-8.0-blue.svg"/>
</p>

## 前后端全部开源微信小程序商城（Java + uniapp）

### 技术站：
- SpringBoot
- Vue2、ElementUI

### 重要说明
- 微信 Ai 能力已经接入，需要在微信公众平台开通`AI 能力`，手机体验微信版本最低 `8.0.75`，开发工具中调试基础库最低`3.16.1`
- 微信 Ai 能力还在灰度内测，暂未开放提审
- 如果开发中遇到不清楚的地方可以添加下方客服微信，请注明来意
- 项目合作洽谈，请联系客服微信（使用微信扫码添加好友，请注明来意）。
- 如需购买 [商业版源码](https://openwtai.com/blogs) 请联系客服。<br>
  <img height="564" src="./images/wx.png" title="微信" width="444"/>

## 使用须知
### ✅允许
- 个人学习使用
- 允许用于学习、毕设等
- 允许进行商业使用，请自觉遵守使用协议，如需要商业使用推荐购买[商业版源码](https://openwtai.com/blogs)
- 请遵守 Apache License2.0 协议，再次开源请注明出处
- 推荐Watch、Star项目，获取项目第一时间更新，同时也是对项目最好的支持
- 希望大家多多支持原创作品

### 重要链接
- 官网：[https://openwtai.com](https://openwtai.com)
- 开发文档：[http://doc.openwtai.com](http://doc.openwtai.com)
- 在线体验：[https://doc.openwtai.com/zh-CN/start/view.html](https://doc.openwtai.com/zh-CN/start/view.html)
- Gitee仓库：[https://gitee.com/hehailong/platform](https://gitee.com/hehailong/platform)
- Github仓库：[https://github.com/hehailong92/platform-wxshop](https://github.com/hehailong92/platform-wxshop)


## 系统架构图
![](./images/system-architecture.png "系统架构图")

## 时序架构图
![](./images/sequence.png "时序架构图")

## 项目结构
~~~
platform
|--_sql 初始化数据库脚本
|--platform-admin 后台管理接口服务
|--platform-admin-ui 后台管理页面
|--platform-api 微信小程序商城api接口服务
|--platform-biz 业务代码
|--platform-common 公共模块
|--wx-mall 微信小程序原生商城
~~~

## 安装教程

* 配置环境（推荐Java21、maven3.8、mysql8.0、redis4.0.1）
* 创建数据库
* 依次初始化sql脚本 
    * /_sql/base.sql
    * /_sql/mall.sql
    * /_sql/sys_region.sql

* 导入项目到IDE中
* 导入支付证书至/platform-admin/src/main/resources/cert/目录下（申请商户号、开通微信支付、下载支付证书）
* 导入支付证书至/platform-api/src/main/resources/cert/目录下
* 修改配置文件 /platform-admin/src/main/resources/application.yml
* 修改配置文件 /platform-api/src/main/resources/application.yml
  * 微信公众号配置
  * 微信小程序配置
  * 微信支付配置
* 修改配置文件 /platform-admin/src/main/resources/application-dev.yml
* 修改配置文件 /platform-api/src/main/resources/application-dev.yml
  * 数据库配置
* 启动redis服务
* 启动后台项目
  * 启动类：PlatformAdminApplication.java
  * 启动类：PlatformApiApplication.java
* 打开微信开发者工具
* 导入 /wx-mall填写appId
* 修改 /wx-mall/config/api.js里API_BASE_URL的值
* 使用idea启动项目访问路径
    * [http://localhost:8000](http://localhost:8000)

## Docker 一键启动

Docker 使用多阶段构建在容器内完成后端和管理端编译，宿主机只需安装 Docker 和 Compose V2。

首次启动前，如需调整 Maven profile、端口、数据库密码或 JVM 参数，先复制配置文件：

```bash
cp deploy/.env.example deploy/.env
```

其中：

* `MAVEN_PROFILE` 控制 `scripts/build-jars.sh` 使用的 Maven profile，默认 `dev`
* `NGINX_PORT` 默认 `8888`
* `MYSQL_ROOT_PASSWORD` 默认 `REDACTED_CREDENTIAL`

在仓库根目录一键启动，Windows 使用：

```powershell
.\dev.ps1
```

Ubuntu / macOS 使用：

```bash
./dev.sh
```

停止：

```bash
scripts/docker-down.sh
```

启动后默认访问地址：

* 管理台：http://localhost:8888
* 后台接口：http://localhost:8888/platform-framework
* 商城接口：http://localhost:8888/platform-framework-api

说明：

* `docker-compose.yml` 会一键启动 `mysql`、`redis`、`platform-admin`、`platform-api`、`nginx`
* `nginx` 负责托管前端静态资源，并反向代理两个后端服务
* 后端容器运行时启用 `docker` profile，自动加载 `application-docker.yml`
* MySQL 首次创建命名数据卷时自动执行仓库 `_sql/` 根目录下的初始化脚本，`_sql/migrations` 仅供已有数据库手动升级
* 普通停止保留 MySQL 和 Redis 数据；只有显式执行 `-ResetData` 或 `--reset-data` 才清空测试数据

## 克拉匠造界面与定制配置

当前分支已将原通用商城界面重构为珠宝定制业务界面：小程序首页支持商品搜索、关键词和业务快捷菜单，商品、交易、账户及认证页面使用不同的布局语言；管理后台统一为深绿侧栏和暖白工作区。

旧版曾将 DIY 拆分为项链和耳饰两个主体。当前版本只保留 `1999105`（“演示 · 珍珠项链 DIY”）作为定制主体，`1999106` 停用并保留历史数据；耳钉、耳钩、耳夹和耳圈改为可选搭配配件。已有数据库执行 `_sql/migrations/20260901_simplify_necklace_diy.sql`；全新 Docker 数据卷会执行 `_sql/zzzzz_carat_craft_simplify_diy.sql`。

小程序定制页只保留项链主体，采用透明素材分层二维预览。珍珠、贵金属和款式为三类必选内容；宝石、长度、搭配配件和刻字默认折叠为可选内容。耳饰不再作为独立定制主体。

小程序定制配置接口以 `GET /app/customization/config?goodsId=1999105` 为规范调用，同时兼容旧版客户端的 `POST` 请求。配置加载中、接口失败、商品下架、商品进入回收站、SKU 缺失或组件为空时，页面会展示明确原因和重新加载入口，不再静默显示空白定制区域。

正式商品和配件均由商家通过网页维护：

1. 在“商城配置 → 商品分类”创建珠宝分类，在“商城配置 → 品牌制造商”维护品牌。
2. 在“商品管理 → 商品管理”新增商品，维护图片、详情、参数、规格、SKU、库存和上架状态；也可以下载模板后导入完整商品 JSON。
3. 在“定制管理 → 定制配置”选择已经创建的商品，设置必选组件，并逐项维护珍珠、贵金属、宝石、搭配配件、款式、长度和二维图层；也可以导入完整配件 JSON。
4. 在“定制管理 → 组件库存”维护每个配件选项的可售库存。当前定制器仅支持项链主体，耳钉、耳钩和耳夹作为可选搭配配件维护。

商品删除采用三十天回收站机制：

1. 商家在“商品管理 → 商品管理”删除商品后，商品会立即下架，并从管理端普通列表、小程序商品列表、购物车、收藏和足迹展示中隐藏。
2. “商品管理 → 商品回收站”显示删除时间和自动清理前剩余天数，支持批量恢复和永久删除。恢复后的商品保持下架，商家检查图片、库存和定制选项后再手动上架。
3. `goodsRecycleTask.purgeExpiredGoods` 每天凌晨 3:20 清理删除超过三十天的商品。清理范围包括商品目录、SKU、相册、参数、购物车、收藏、足迹和可重建 DIY 配置；历史订单、订单商品、设计快照、定制订单、生产工单和工单日志会保留。
4. 已有数据库执行 `_sql/migrations/20260831_goods_recycle_bin.sql` 升级；全新 Docker 数据卷会自动执行 `_sql/zz_carat_craft_goods_recycle.sql`。两个脚本均可重复执行，迁移前已经逻辑删除的商品从脚本执行时间开始计算三十天。

商城目录去底座迁移：

1. 线上目录仅保留 `1999000–1999999` 珠宝业务 ID 段，原严选商品、分类、购物车、收藏、足迹、商品评论和可重建规格数据会被清理。
2. 原严选首页广告、频道、品牌、专题及两个遗留图片域名会被清理，首页改用小程序包内 `/static/images/` 珠宝资源；历史订单、设计快照、定制订单和生产工单不删除。
3. 已有数据库备份后执行 `_sql/migrations/20260901_cleanup_legacy_catalog.sql`；全新 Docker 数据卷自动执行 `_sql/zzzzz_carat_craft_catalog_cleanup.sql`。脚本可重复执行。

小程序商品详情支持游客匿名浏览，游客不会写入收藏、足迹或推荐券数据。“我的设计”仅允许恢复本人 `DRAFT` 快照，设计器会还原组件选项与刻字；`LOCKED` 和 `ORDERED` 快照只读展示。定制接口参数错误使用 HTTP 400 和响应体 `code=400`，小程序请求封装会保留结构化 4xx 消息。

Linux Docker 中的 MySQL 使用 `lower_case_table_names=0`，表名严格区分大小写。商城表的规范名称统一为小写 `mall_*`；实体 `@TableName`、Mapper XML 和手写 SQL 都必须使用小写。`SYS_*`、`WX_*` 和 `CC_*` 表在当前数据库中实际为大写，不得随商城表一起转换。

管理后台的菜单名称存储在 `SYS_MENU`。`_sql/carat_craft_menu.sql` 会先执行 `SET NAMES utf8mb4`，再幂等写入定制管理菜单，避免 SQL 客户端使用错误字符集导致中文被保存为 `å®šåˆ¶` 一类乱码。修复已有数据库菜单后需要重新登录，后台会清理旧的菜单、权限和动态路由会话缓存并重新获取最新菜单。

Docker 环境已启用控制台异常日志。页面出现“服务器内部错误”时，可通过 `docker compose --env-file deploy/.env logs --tail 200 platform-admin` 查看后端具体异常；不要仅依据通用提示判断为数据库故障。

后台首页 CPU、内存和磁盘占用读取 `/sys/monitor/server` 的服务器实时数据。接口不可用时页面会明确显示失败原因，图表不会回退为模拟数值。

## 页面展示
### 管理端
![](./images/home.png "首页")
### 品牌供应商
![](./images/brand.png "品牌供应商")
### 专题管理
![](./images/topic.png "专题")
### SKU编辑
![](./images/sku.png "sku")
### 订单
![](./images/order.png "订单")
### 接口文档
![](./images/15_06_39.png "接口文档")

### 微信 AI
![](./images/guide-skills-1.png "1")
![](./images/guide-skills-2.png "2")
![](./images/guide-skills-3.png "3")
![](./images/guide-skills-4.png "4")
![](./images/guide-skills-5.png "5")
![](./images/guide-skills-6.png "6")
![](./images/guide-skills-7.png "7")

### 小程序首页
![](./images/5.png "小程序首页")
### 专题
![](./images/6.png "专题")
### 分类
![](./images/7.png "分类")
### 购物车
![](./images/8.png "购物车")
### 我的
![](./images/9.png "我的")

### 
