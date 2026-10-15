# 审查测试证据与复现方法

> 此附件为版权整改前的历史审查记录. 后续文件可能已替换或移除, 当前状态见[版权整改结果](copyright-remediation-2026-10-14.md). 哈希和问题行号不应当作整改后现状.

审查基线: cc5bd4b49453813f68a62cfdb8841f7e0366ef38. 测试执行日期: 2026-10-13, 登录后界面补查: 2026-10-14, Asia/Shanghai. [主报告](audit-2026-10-13.md) / [问题数据](audit-2026-10-13-findings.json).

## 1. 已有Java单元测试

使用工作区已有JDK21和Maven3.9.11/.m2, 全程offline. 初次fork JVM在Windows沙箱中因Byte Buddy attach命名管道权限错误无法结束; 用户批准后只结束此次Surefire/attacher测试进程, 预加载已有agent, forkCount=0重跑. 这项运行参数是审查环境兼容处理, 不应无条件代入生产构建.

```powershell
Set-Location 'D:\code\gitee\carat-craft\vendor\platform-wxshop'
$env:JAVA_HOME = 'D:\code\gitee\carat-craft\.tools\jdk21\jdk-21.0.8+9'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$env:MAVEN_OPTS = '-javaagent:D:\code\gitee\carat-craft\.tools\m2\net\bytebuddy\byte-buddy-agent\1.14.13\byte-buddy-agent-1.14.13.jar -Djava.io.tmpdir=D:\tmp\hailongcodex\2026-10-13 -Djansi.tmpdir=D:\tmp\hailongcodex\2026-10-13 -Dfile.encoding=UTF-8'
& 'D:\code\gitee\carat-craft\.tools\maven\apache-maven-3.9.11\bin\mvn.cmd' -o -B '-Dmaven.repo.local=D:\code\gitee\carat-craft\.tools\m2' -pl platform-api -am test '-Dtest=Custom*Test,OrderPaymentServiceImplTest,AppCustomizationControllerTest' '-Dsurefire.failIfNoSpecifiedTests=false' '-DforkCount=0'
```

| 类 | 用例数 | 失败 | 错误 | 跳过 |
|---|---:|---:|---:|---:|
| CustomDesignServiceImplTest | 8 | 0 | 0 | 0 |
| CustomQuoteServiceImplTest | 3 | 0 | 0 | 0 |
| OrderPaymentServiceImplTest | 2 | 0 | 0 | 0 |
| AppCustomizationControllerTest | 3 | 0 | 0 | 0 |
| 合计 | 16 | 0 | 0 | 0 |

Surefire XML为本次执行生成, 在platform-biz/target/surefire-reports和platform-api/target/surefire-reports中. 没有把其中2026-08旧报告视作新证据. 实际BUILD SUCCESS, 总计39.747s. 通过用例不覆盖真实MySQL唯一约束、事务回滚、双用户HTTP链和并发.

管理端另执行offline Maven compile, platform-common/biz/admin成功, 155个admin源文件编译, 总计56.278s. 仍出现BOM import位置不正确、resources3.1.0不识别propertiesEncoding等警告.

没有全量执行PlatformApiApplicationTests, 因其中有真实发送邮件、退款等调用. 测试入口应在重构时硬隔离, 而不是仅依靠README警告.

## 2. 当前Java类的方法级隔离探针

临时复现源文件位于D:\tmp\hailongcodex\2026-10-13\AuditProbe.java, 编译/运行参数为audit-javac.args和audit-java.args. 输出保存在audit-java-results.txt. 全部数据库/Redis/短信/支付依赖由Mockito替代, 没有真实外部副作用.

可在原环境直接重跑:

```powershell
& 'D:\code\gitee\carat-craft\.tools\jdk21\jdk-21.0.8+9\bin\javac.exe' '@D:\tmp\hailongcodex\2026-10-13\audit-javac.args'
& 'D:\code\gitee\carat-craft\.tools\jdk21\jdk-21.0.8+9\bin\java.exe' '@D:\tmp\hailongcodex\2026-10-13\audit-java.args'
```

| 输入/操作 | 观察到的结果 | 主报告 |
|---|---|---|
| 当前用户7提交地址id99 | code0, 写入owner7, 原归属getById未调用 | A001 |
| 地址isDefault缺失 | NullPointerException | A044 |
| 当前用户7更新用户8购物车, number=-5 | code0, owner8记录quantity变为-5 | A002/A043 |
| 待付款订单0/0调用confirmOrder | code0, 订单301, shippingStatus2 | A017 |
| 用户缓存验证码1234, 请求任意另一个合法手机号 | code0, 绑定请求中号码, 缓存无目标号码上下文 | A006 |
| orderService.update返回false后markPaid | 工单服务仍被调用, 未抛出异常 | A016 |
| JwtUtils secret为空 | IllegalArgumentException | A009 |
| JwtUtils使用YAML默认字符串 | WeakKeyException | A009 |

前两种越权测试只证明控制器/服务方法缺少对应防线. 没有对真实用户地址或购物车写入, 没有伪称完整HTTP漏洞利用验证.

## 3. 当前JS源码隔离执行

使用Node24.19.0、vm、现有Babel6/vue-template-compiler加载源码, 提供wx、Page、FileReader和HTTP的可控stub. 所有测试仅在内存执行, 没有上传、下单或支付.

| 场景 | 观察结果 | 主报告 |
|---|---|---|
| 合法商品JSON: goods.name/categoryId齐全 | JSON解析失败:data is not defined; confirmCalls=0,httpCalls=0 | A040 |
| util.request网络fail | showLoading1次, hideLoading0次 | A041 |
| 调用app备用updateManager | util is not defined | A047 |
| 旧quote199, 新报价返回400库存不足 | quote仍为199, 没有失效状态 | A036 |
| S925银选项, 商家assetUrl设银图 | 最终chainUrl仍chain-white-gold.webp | A035 |
| checkout.submitOrder连续调用两次 | 发送2次提交请求 | A023 |
| 预支付request拒绝 | 外层Promise40ms后仍未settle, 出现未处理rejection | A024 |
| 先加载商品1后商品2, 商品2响应先返回 | goodsId2, config商品1, SKU11 | A037 |

对于竞态, stub控制了响应顺序以确定性复现. 这不是网络性能/浏览器或微信真机压力测试. 未把wxParse的ES模块import语法误当作源码语法Bug.

## 4. 静态解析、lint与构建

- JSON: 53个解析通过.
- 小程序JS: 64个使用Babel语法解析通过. vm.Script脚本模式无法解析import的误报已排除.
- Vue: 136个模板通过现有vue-template-compiler解析, 不代表数据/事件运行行为正确.
- WXML绑定扫描: reset.startNext和个人地址页selectAddress确实缺失; wxParseTagATap由库动态注入, 已排除误报.
- 非Qoder主要Markdown: 检出底座README20个不存在的本地图片引用.
- 图片字面路径: WXML静态路径未检出缺失;动态拼接与远程域名需另查.

lint命令:

```powershell
Set-Location 'D:\code\gitee\carat-craft\vendor\platform-wxshop\platform-admin-ui'
node node_modules/eslint/bin/eslint.js --ext .js,.vue src --format json
```

结果: 154个输入文件, 5error/0warning. goods.vue:172两个no-undef; goods-recycle.vue:75/122/127三个curly.

隔离生产webpack使用现有webpack.prod.conf, NODE_ENV=production, --openssl-legacy-provider; 仅将output.path改到D:\tmp\hailongcodex\2026-10-13\admin-ui-webpack. 不是修改仓库配置, 没有调用会先删除原dist的gulp入口. hasErrors=false, 生成439资产, 2条模块warning实际上包含上述5个lint error. 另有Sass/Tapable旧API废弃警告.

构建成功不能证明商品导入功能通过. 本次未验证完整gulp版本目录/运行时config合并发布链.

## 5. Docker恢复后的HTTP与数据库验证

用户启动Docker后, 五个容器全部running/healthy. 只读核对端口: MySQL3306、Redis6379公开到所有宿主网卡; Nginx8888; 应用8080/8081仅容器内. 没有重启、重建或停止项目容器.

| 方法/入口 | HTTP/业务结果 |
|---|---|
| GET / | 200, 登录页面可渲染 |
| POST app/index/newGoods | 200/code0 |
| POST app/index/hotGoods | 200/code0 |
| GET app/customization/config?goodsId=1999105 | 200/code0 |
| POST customization/quote, 合法pearl/material/style | 200/code0,totalPrice2639.00 |
| POST customization/quote, 空optionIds | 400/code400, 提示必选pearl |
| POST app/test/userListByMobile, 不存在的审查号码 | 200/code0/data=null, 未登录可调用 |
| POST app/comment/count, 无token | HTTP200/code401 |
| GET app/index/newGoods, 错误方法 | HTTP200/code500 |
| GET API/v3/api-docs | 200, 完整文档匿名开放 |
| GET ADMIN/druid/ | HTTP200/code404, 当前监控入口未证实开放 |

只查询数据库计数/结构, 不输出用户字段: WX_USER1行,mall_user0行,订单8行,快照6行,定制商品2行,选项55行; 启用选项缺库存记录数为0. 1999106仍enabled1; uk_cc_order_snapshot为唯一索引; 当前唯一微信用户UNION_ID为空. 这些是本机旧数据卷当前状态, 不能当作生产普遍状态.

容器环境仅检查是否存在/值长, 不输出secret: API中的WX_MINIAPP_APPID和JWT_SECRET为空; WX_PAY_APPID/MCH_ID/MCH_KEY/NOTIFY_URL及WX_MP_APPID未注入. 因此没有继续尝试真实微信登录或支付.

复现取消后再绑定的SQL只创建临时克隆表:

```sql
CREATE TEMPORARY TABLE audit_order_binding LIKE CC_ORDER_CUSTOMIZATION;
INSERT INTO audit_order_binding(order_id,order_goods_id,snapshot_id,status)
VALUES(900001,900001,900001,'CANCELLED');
INSERT INTO audit_order_binding(order_id,order_goods_id,snapshot_id,status)
VALUES(900002,900002,900001,'LOCKED');
```

实际结果: ERROR1062, Duplicate entry900001 for key audit_order_binding.uk_cc_order_snapshot. 原业务表未改动. 这证明真实MySQL模式约束与取消后恢复DRAFT的重新购买策略冲突, 并非只靠mock推断.

## 6. 界面与剩余验证

真实浏览器访问本机管理登录页, 保存了[截图](audit-2026-10-13-assets/admin-login.png). 已观察品牌、页脚、标签标题和输入框. 用户自行完成管理员登录后, 于2026-10-14继续只读检查首页、商品列表/SKU详情、定制配置、组件库存、工单空态与订单列表. 没有保存表单、导入商品、发货、退款、删除记录或改变工单状态.

| 页面/场景 | 观察结果 | 证据/主报告 |
|---|---|---|
| 首页窄窗口624x790 | 图表刻度重叠、卡片内容裁切 | [窄窗口截图](audit-2026-10-13-assets/admin-home-narrow.png), A080 |
| 首页桌面窗口1280x900 | 主体正常渲染, 已使用空间显示成可用, 物理/逻辑核心标签与字段对应颠倒 | [桌面截图](audit-2026-10-13-assets/admin-home-desktop.png), A086 |
| 商品列表及SKU详情 | 页面正常渲染; 简化迁移拟停用的1999106仍上架/启用 | [商品截图](audit-2026-10-13-assets/admin-goods.png), A056 |
| 定制配置 | 2个商品启用; 部分列宽不足, 数值与模板代码裁切 | [配置截图](audit-2026-10-13-assets/admin-custom-config.png), A080 |
| 组件库存 | 首屏库存数据正常显示 | 只读检查, 未执行扣减或库存修改 |
| 生产工单 | 当前为空, 筛选及空态可显示 | [工单截图](audit-2026-10-13-assets/admin-work-orders.png), A079; 状态流转尚未实测 |
| 订单列表 | 8行记录均无用户昵称; 与WX_USER/mall_user关联问题一致 | A061; 未保存含收货信息的截图 |
| 登录后导航 | 跳转首页成功, 控制台仍产生导航守卫重定向的未处理Promise拒绝 | A087 |

桌面首页截图只保存上方界面区域, 避免复制后续机器IP等环境信息. 小程序外观仅检查WXML/WXSS和素材, 没有微信开发者工具或真机截图.

继续重构时必须补充: 用户A/B HTTP写入、数据库并发和回滚、支付与取消/退款竞态、真实回调补偿、上传内容/体积测试、新旧库迁移、备份/恢复与手机视觉. 原业务数据应保留, 另建明确可丢弃的测试数据集.
