# 身份、版权、商业分发与工具残留清单

> 此附件为版权整改前的历史审查记录. 后续文件可能已替换或移除, 当前状态见[版权整改结果](copyright-remediation-2026-10-14.md). 哈希和问题行号不应当作整改后现状.

审查日期: 2026-10-13. [主报告](audit-2026-10-13.md) / [全文件哈希](audit-2026-10-13-inventory.md). 本文是发现清单与整改路径, 不是无版权风险证书, 没有删除或重署名原文件.

## 1. 已确认的身份和清理意图

当前Git配置和近期提交使用hehailong、845367567@qq.com, remote为hhlqwq/carat-craft; 既有对话记忆也提供hhlqwq仓库线索. 因此何海龙/hehailong/hhlqwq作为用户身份线索, 不列为无关人员. 这只能确认身份相符, 不能仅据同名证明每个导入文件都由用户原创或已转让著作权.

用户明确openwtai.com和微同是无关内容. 展示文案、商业推广、运行配置/回调/短信签名可列为替换目标; 第三方许可证中包含旧域名的声明必须先确认来源与义务, 不能文本全局替换后自称权利已清.

## 2. 能否修改、分发、商用的处理方式

- 自己原创且拥有权利的业务代码与品牌, 可以制定自己的发布许可和署名; 应留有源码/素材来源证据.
- MIT、BSD、Apache等第三方组件通常允许修改/商业使用, 但仍有保留版权、许可或NOTICE等条件, 不等于第三方作品自动变为自己的作品. 具体依据分别为[MIT](https://opensource.org/license/mit)、[BSD3](https://opensource.org/license/BSD-3-Clause)、[Apache2第4节](https://www.apache.org/licenses/LICENSE-2.0.html).
- 商业授权组件、来源不明素材、字体与旧二进制插件先补授权或替换. 框架根目录LICENSE不能覆盖夹带的所有第三方作品.
- 必须先决定实际交付形态: 自用/托管服务、开源源码分发、闭源源码交付、二进制/OEM分发. 所需许可证条件不同, 当前不能宣称任意闭源商业分发已经全部获准.

## 3. 当前优先权利缺口

| 对象 | 证据与当前状态 | 处理建议 |
|---|---|---|
| 底座源码 | LICENSE为Apache2, 数百文件头为BSD风格; 本人身份署名与其他作者混合 | 核对原始仓库、版本及所有复制片段, 建立自有修改说明和第三方NOTICE |
| Highcharts3.0.6 | UEditor/dialogs/charts/charts.html:85引用, 静态目录被webpack复制; 多个Highcharts文件license指向厂商 | 商用授权证明未找到, 优先移除不用图表功能或替换; 保留时核对旧版本与OEM分发许可 |
| 严选/MUJI等旧图片和文案 | mall.sql含原目录和远程图片, 清理迁移并未抹除仓库原种子 | 从正式种子/交付中移出, 不能把第三方商品图改品牌后当原创 |
| 图标/字体/演示珠宝图 | 有本地资产, 没有逐素材来源与授权台账 | 记录作者/来源/版本/许可/用途; 未能确认的替换, 字体与图片分开审核 |
| UEditor旧EXE/SWF | 截图插件EXE和5个SWF, 来源声明不足且技术过时 | 不执行, 从发布包排除或替换; Flash维护状态见[Adobe说明](https://www.adobe.com/products/flashplayer/end-of-life-alternative.html) |
| DOCX | 两份开发文档同SHA256, 上线材料带腾讯云cps返佣链接, 内容过期 | 旧参考归档, 重写自有上线文档, 不保留返佣购买建议 |
| MySQL/其他JAR | 测试类路径296JAR, 多许可/例外/继承许可证 | 按实际运行发布物核对; 官方[MySQL商业分发说明](https://www.mysql.com/about/legal/licensing/oem/)不能自动套成全部依赖已批准 |

Highcharts官方目前要求生产/商业使用具备适当商业许可, 见[厂商说明](https://www.highcharts.com/blog/download/). 本地版本是3.0.6, 取得时适用的旧授权、来源和分发权限仍须核验, 没有用现行条款替代旧合同判定侵权.

jsqlparser和javassist本地POM分别含LGPL/Apache、MPL/LGPL/Apache多许可声明; 需要核对实际版本和选择, 不能因出现GPL字样就声称整个项目必然需要开源. JUnit/Mockito等仅用于测试的组件也不能一概当成生产交付依赖. Connector/J的FOSS例外要核对是否适用于实际发布方式.

## 4. AI工具残留与上线清洁度

.qoder/repowiki为明确工具文档/索引/元数据; wx-mall/agent、skills及app.json的agent/subPackages是已声明的助手能力与配置. 若产品不需要它们, 清理时同时调整配置/引用, 不只删除目录. 源码里的英文库名、版权署名或普通算法不能仅因名称有AI片段就当作工具残留, 本次词匹配使用单词边界并保留人工核对状态.

可执行的目标是清理不用的工具元数据、模板推广、虚假能力说明和重复生成文档, 将代码/文档按实际功能、用户规范和运行证据重写. 不伪造原创证明、提交历史或作者, 不承诺隐藏真实来源就能通过所谓AI检测. 当前没有审查平台的具体检测规则, 不能保证某个检测结果. 必须保留的第三方许可不因检测诉求删除.

## 5. 作者署名: 全部已检出值及文件

以下以@作者字段扫描, 不是完整版权权利人清单; 行号仅用于定位. 同一署名可能混在本人版权头或复制片段里. 第三方库作者保留适用声明, 业务文件中的旧作者也先核对来源.

### 何海龙 (410文件)

- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/PlatformAdminApplication.java:45](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/PlatformAdminApplication.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/common/annotation/SysLog.java:26](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/common/annotation/SysLog.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/common/aspect/SysLogAspect.java:44](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/common/aspect/SysLogAspect.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/common/utils/ShiroUtils.java:30](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/common/utils/ShiroUtils.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/ActResourceHandlerConfig.java:29](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/ActResourceHandlerConfig.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/FilterConfig.java:29](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/FilterConfig.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/KaptchaConfig.java:32](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/KaptchaConfig.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/MybatisPlusConfig.java:35](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/MybatisPlusConfig.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/ShiroConfig.java:43](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/ShiroConfig.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/SwaggerConfig.java:54](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/SwaggerConfig.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/datascope/DataScope.java:27](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/datascope/DataScope.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/datascope/DataScopeInnerInterceptor.java:43](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/datascope/DataScopeInnerInterceptor.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/service/impl/SysGeneratorServiceImpl.java:40](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/service/impl/SysGeneratorServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/config/ScheduleConfig.java:31](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/config/ScheduleConfig.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/controller/ScheduleJobController.java:38](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/controller/ScheduleJobController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/controller/ScheduleJobLogController.java:38](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/controller/ScheduleJobLogController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/dao/ScheduleJobDao.java:33](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/dao/ScheduleJobDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/dao/ScheduleJobLogDao.java:33](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/dao/ScheduleJobLogDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/entity/ScheduleJobEntity.java:34](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/entity/ScheduleJobEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/entity/ScheduleJobLogEntity.java:31](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/entity/ScheduleJobLogEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/service/ScheduleJobLogService.java:30](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/service/ScheduleJobLogService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/service/ScheduleJobService.java:30](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/service/ScheduleJobService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/service/impl/ScheduleJobLogServiceImpl.java:32](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/service/impl/ScheduleJobLogServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/service/impl/ScheduleJobServiceImpl.java:39](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/service/impl/ScheduleJobServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/task/TestTask.java:29](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/task/TestTask.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/utils/ScheduleJob.java:38](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/utils/ScheduleJob.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/utils/ScheduleRunnable.java:31](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/utils/ScheduleRunnable.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/utils/ScheduleUtils.java:29](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/utils/ScheduleUtils.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAdController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAdController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAdPositionController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAdPositionController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAddressController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAddressController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAttributeCategoryController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAttributeCategoryController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAttributeController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAttributeController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallBrandController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallBrandController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCartController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCartController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCategoryController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCategoryController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallChannelController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallChannelController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCollectController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCollectController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCouponController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCouponController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCouponGoodsController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCouponGoodsController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallFeedbackController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallFeedbackController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallFootprintController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallFootprintController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsAttributeController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsAttributeController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsController.java:43](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsGalleryController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsGalleryController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsIssueController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsIssueController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsSpecificationController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsSpecificationController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallHelpIssueController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallHelpIssueController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallHelpTypeController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallHelpTypeController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallKeywordsController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallKeywordsController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallOrderController.java:48](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallOrderController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallOrderGoodsController.java:44](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallOrderGoodsController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallSearchHistoryController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallSearchHistoryController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallShippingController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallShippingController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallSpecificationController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallSpecificationController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallTopicCategoryController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallTopicCategoryController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallTopicController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallTopicController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallUserCouponController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallUserCouponController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallUserLevelController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallUserLevelController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/oss/controller/SysOssController.java:49](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/oss/controller/SysOssController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/AbstractController.java:32](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/AbstractController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysCacheController.java:38](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysCacheController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysConfigController.java:40](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysConfigController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysDictController.java:47](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysDictController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysDictGroupController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysDictGroupController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysLogController.java:39](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysLogController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysLoginController.java:48](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysLoginController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysMenuController.java:47](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysMenuController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysMonitorController.java:43](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysMonitorController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysOrgController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysOrgController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysRoleController.java:42](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysRoleController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysSmsLogController.java:42](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysSmsLogController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysUserController.java:48](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysUserController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysUserTokenController.java:37](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysUserTokenController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysCaptchaDao.java:28](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysCaptchaDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysDictDao.java:33](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysDictDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysDictGroupDao.java:33](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysDictGroupDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysLogDao.java:28](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysLogDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysOrgDao.java:31](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysOrgDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysRoleDao.java:33](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysRoleDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysRoleMenuDao.java:30](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysRoleMenuDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysRoleOrgDao.java:30](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysRoleOrgDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysUserRoleDao.java:30](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysUserRoleDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysCacheEntity.java:28](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysCacheEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysCaptchaEntity.java:30](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysCaptchaEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysDictEntity.java:35](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysDictEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysDictGroupEntity.java:37](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysDictGroupEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysLogEntity.java:34](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysLogEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysOrgEntity.java:38](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysOrgEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysRoleEntity.java:35](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysRoleEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysRoleMenuEntity.java:30](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysRoleMenuEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysRoleOrgEntity.java:31](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysRoleOrgEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysUserRoleEntity.java:30](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysUserRoleEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/enums/MenuTypeEnum.java:25](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/enums/MenuTypeEnum.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/form/PasswordForm.java:26](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/form/PasswordForm.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/form/SysLoginForm.java:26](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/form/SysLoginForm.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/oauth2/Oauth2Filter.java:40](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/oauth2/Oauth2Filter.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/oauth2/Oauth2Realm.java:37](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/oauth2/Oauth2Realm.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/oauth2/Oauth2Token.java:26](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/oauth2/Oauth2Token.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysCacheService.java:29](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysCacheService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysCaptchaService.java:29](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysCaptchaService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysDictGroupService.java:31](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysDictGroupService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysDictService.java:31](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysDictService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysLogService.java:30](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysLogService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysMenuService.java:29](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysMenuService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysOrgService.java:30](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysOrgService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysRoleMenuService.java:29](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysRoleMenuService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysRoleService.java:31](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysRoleService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysUserRoleService.java:29](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysUserRoleService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysUserService.java:31](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysUserService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysUserTokenService.java:30](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysUserTokenService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysCacheServiceImpl.java:36](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysCacheServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysCaptchaServiceImpl.java:37](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysCaptchaServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysDictGroupServiceImpl.java:37](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysDictGroupServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysDictServiceImpl.java:37](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysDictServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysLogServiceImpl.java:34](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysLogServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysMenuServiceImpl.java:37](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysMenuServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysOrgServiceImpl.java:36](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysOrgServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysRoleMenuServiceImpl.java:34](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysRoleMenuServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysRoleServiceImpl.java:42](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysRoleServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysUserRoleServiceImpl.java:33](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysUserRoleServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysUserServiceImpl.java:44](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysUserServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysUserTokenServiceImpl.java:37](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysUserTokenServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/MsgReplyRuleManageController.java:39](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/MsgReplyRuleManageController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/MsgTemplateManageController.java:42](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/MsgTemplateManageController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/TemplateMsgLogManageController.java:38](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/TemplateMsgLogManageController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxAssetsManageController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxAssetsManageController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMaAnalysisController.java:48](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMaAnalysisController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMenuManageController.java:39](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMenuManageController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMpDraftController.java:43](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMpDraftController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMpFreePublishController.java:37](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMpFreePublishController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMsgManageController.java:40](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMsgManageController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxQrCodeManageController.java:42](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxQrCodeManageController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxUserManageController.java:38](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxUserManageController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxUserTagsManageController.java:38](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxUserTagsManageController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/form/DraftArticles.java:28](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/form/DraftArticles.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/form/DraftForm.java:29](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/form/DraftForm.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/PlatformApiApplication.java:44](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/PlatformApiApplication.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/annotation/IgnoreAuth.java:26](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/annotation/IgnoreAuth.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/annotation/LoginUser.java:31](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/annotation/LoginUser.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/CacheConfig.java:28](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/CacheConfig.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/MybatisPlusConfig.java:35](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/MybatisPlusConfig.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/SwaggerConfig.java:56](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/SwaggerConfig.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/SwaggerResourceHandlerConfig.java:31](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/SwaggerResourceHandlerConfig.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/WebMvcConfig.java:34](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/WebMvcConfig.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/interceptor/AuthorizationInterceptor.java:40](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/interceptor/AuthorizationInterceptor.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppLoginController.java:59](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppLoginController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/WxMaPortalController.java:38](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/WxMaPortalController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/WxMpPortalController.java:36](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/WxMpPortalController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/resolver/LoginUserHandlerMethodArgumentResolver.java:39](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/resolver/LoginUserHandlerMethodArgumentResolver.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/utils/JwtUtils.java:37](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/utils/JwtUtils.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/annotation/LimitAspect.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/annotation/LimitAspect.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/annotation/LockAspect.java:35](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/annotation/LockAspect.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/annotation/ServiceLimit.java:26](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/annotation/ServiceLimit.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/annotation/ServiceLock.java:26](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/annotation/ServiceLock.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/AbstractAssert.java:27](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/AbstractAssert.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/ValidatorUtils.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/ValidatorUtils.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/AddGroup.java:24](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/AddGroup.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/AliyunGroup.java:24](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/AliyunGroup.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/DiskGroup.java:24](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/DiskGroup.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/Group.java:26](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/Group.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/HuaweiGroup.java:24](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/HuaweiGroup.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/MinioGroup.java:6](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/MinioGroup.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/QcloudGroup.java:24](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/QcloudGroup.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/QiniuGroup.java:24](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/QiniuGroup.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/UpdateGroup.java:24](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/UpdateGroup.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/AliPayService.java:15](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/AliPayService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMaConfiguration.java:47](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMaConfiguration.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMaProperties.java:26](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMaProperties.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMpConfiguration.java:36](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMpConfiguration.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMpMessageRouterConfiguration.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMpMessageRouterConfiguration.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMpProperties.java:27](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMpProperties.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/JsonArrayTypeHandler.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/JsonArrayTypeHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAdDao.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAdDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAdPositionDao.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAdPositionDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAddressDao.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAddressDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAttributeCategoryDao.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAttributeCategoryDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAttributeDao.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAttributeDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallBrandDao.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallBrandDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCartDao.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCartDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCategoryDao.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCategoryDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallChannelDao.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallChannelDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCollectDao.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCollectDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCouponDao.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCouponDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCouponGoodsDao.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCouponGoodsDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallFeedbackDao.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallFeedbackDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallFootprintDao.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallFootprintDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsAttributeDao.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsAttributeDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsDao.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsGalleryDao.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsGalleryDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsIssueDao.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsIssueDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsSpecificationDao.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsSpecificationDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallHelpIssueDao.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallHelpIssueDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallHelpTypeDao.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallHelpTypeDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallKeywordsDao.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallKeywordsDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallOrderDao.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallOrderDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallOrderGoodsDao.java:37](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallOrderGoodsDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallRelatedGoodsDao.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallRelatedGoodsDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallSearchHistoryDao.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallSearchHistoryDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallShippingDao.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallShippingDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallSpecificationDao.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallSpecificationDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallTopicCategoryDao.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallTopicCategoryDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallTopicDao.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallTopicDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallUserCouponDao.java:37](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallUserCouponDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallUserLevelDao.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallUserLevelDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dto/BuyGoodsRequest.java:9](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dto/BuyGoodsRequest.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAdEntity.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAdEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAdPositionEntity.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAdPositionEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAddressEntity.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAddressEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAttributeCategoryEntity.java:31](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAttributeCategoryEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAttributeEntity.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAttributeEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallBrandEntity.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallBrandEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCartEntity.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCartEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCategoryEntity.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCategoryEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallChannelEntity.java:31](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallChannelEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCollectEntity.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCollectEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCouponEntity.java:35](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCouponEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCouponGoodsEntity.java:31](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCouponGoodsEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallFeedbackEntity.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallFeedbackEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallFootprintEntity.java:35](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallFootprintEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsAttributeEntity.java:31](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsAttributeEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsEntity.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsGalleryEntity.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsGalleryEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsIssueEntity.java:31](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsIssueEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsSpecificationEntity.java:31](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsSpecificationEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallHelpIssueEntity.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallHelpIssueEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallHelpTypeEntity.java:31](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallHelpTypeEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallKeywordsEntity.java:31](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallKeywordsEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallOrderEntity.java:42](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallOrderEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallOrderGoodsEntity.java:35](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallOrderGoodsEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallRelatedGoodsEntity.java:31](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallRelatedGoodsEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallSearchHistoryEntity.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallSearchHistoryEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallShippingEntity.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallShippingEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallSpecificationEntity.java:31](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallSpecificationEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallTopicCategoryEntity.java:31](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallTopicCategoryEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallTopicEntity.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallTopicEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallUserCouponEntity.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallUserCouponEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallUserLevelEntity.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallUserLevelEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAdPositionService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAdPositionService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAdService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAdService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAddressService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAddressService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAttributeCategoryService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAttributeCategoryService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAttributeService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAttributeService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallBrandService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallBrandService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCartService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCartService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCategoryService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCategoryService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallChannelService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallChannelService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCollectService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCollectService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCommentPictureService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCommentPictureService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCouponGoodsService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCouponGoodsService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCouponService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCouponService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallFeedbackService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallFeedbackService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallFootprintService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallFootprintService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsAttributeService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsAttributeService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsGalleryService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsGalleryService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsIssueService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsIssueService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsSpecificationService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsSpecificationService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallHelpIssueService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallHelpIssueService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallHelpTypeService.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallHelpTypeService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallKeywordsService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallKeywordsService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallOrderGoodsService.java:35](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallOrderGoodsService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallOrderService.java:37](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallOrderService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallRelatedGoodsService.java:27](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallRelatedGoodsService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallSearchHistoryService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallSearchHistoryService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallShippingService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallShippingService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallSpecificationService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallSpecificationService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallTopicCategoryService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallTopicCategoryService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallTopicService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallTopicService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallUserCouponService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallUserCouponService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallUserLevelService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallUserLevelService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAdPositionServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAdPositionServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAdServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAdServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAddressServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAddressServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAttributeCategoryServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAttributeCategoryServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAttributeServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAttributeServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallBrandServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallBrandServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCartServiceImpl.java:36](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCartServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCategoryServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCategoryServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallChannelServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallChannelServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCollectServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCollectServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCommentPictureServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCommentPictureServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCommentServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCommentServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCouponGoodsServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCouponGoodsServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCouponServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCouponServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallFeedbackServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallFeedbackServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallFootprintServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallFootprintServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsAggregateServiceImpl.java:41](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsAggregateServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsAttributeServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsAttributeServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsGalleryServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsGalleryServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsIssueServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsIssueServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsServiceImpl.java:41](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsSpecificationServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsSpecificationServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallHelpIssueServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallHelpIssueServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallHelpTypeServiceImpl.java:39](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallHelpTypeServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallKeywordsServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallKeywordsServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallOrderGoodsServiceImpl.java:41](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallOrderGoodsServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallOrderServiceImpl.java:51](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallOrderServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallRelatedGoodsServiceImpl.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallRelatedGoodsServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallSearchHistoryServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallSearchHistoryServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallShippingServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallShippingServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallSpecificationServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallSpecificationServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallTopicCategoryServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallTopicCategoryServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallTopicServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallTopicServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallUserCouponServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallUserCouponServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallUserLevelServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallUserLevelServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/AbstractCloudStorageService.java:31](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/AbstractCloudStorageService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/AliyunCloudStorageService.java:31](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/AliyunCloudStorageService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/CloudStorageConfig.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/CloudStorageConfig.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/DiskCloudStorageService.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/DiskCloudStorageService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/HuaWeiCloudStorageService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/HuaWeiCloudStorageService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/MinioCloudStorageService.java:19](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/MinioCloudStorageService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/QcloudCloudStorageService.java:37](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/QcloudCloudStorageService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/QiniuCloudStorageService.java:35](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/QiniuCloudStorageService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/UploadFactory.java:28](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/UploadFactory.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/dao/SysOssDao.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/dao/SysOssDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/entity/SysOssEntity.java:31](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/entity/SysOssEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/service/SysOssService.java:30](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/service/SysOssService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/service/impl/SysOssServiceImpl.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/service/impl/SysOssServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysConfigDao.java:29](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysConfigDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysMenuDao.java:30](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysMenuDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysSmsLogDao.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysSmsLogDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysUserDao.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysUserDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysUserTokenDao.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysUserTokenDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SmsConfig.java:29](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SmsConfig.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysConfigEntity.java:31](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysConfigEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysMenuEntity.java:35](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysMenuEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysSmsLogEntity.java:31](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysSmsLogEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysUserEntity.java:39](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysUserEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysUserTokenEntity.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysUserTokenEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/Tree.java:12](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/Tree.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/MailService.java:25](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/MailService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/ShiroService.java:30](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/ShiroService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/SysConfigService.java:30](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/SysConfigService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/SysSmsLogService.java:31](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/SysSmsLogService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/MailServiceImpl.java:36](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/MailServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/ShiroServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/ShiroServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/SysConfigServiceImpl.java:41](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/SysConfigServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/SysSmsLogServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/SysSmsLogServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/TaskExcutor.java:26](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/TaskExcutor.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/MsgReplyRuleDao.java:26](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/MsgReplyRuleDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/MsgTemplateDao.java:26](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/MsgTemplateDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/TemplateMsgLogDao.java:26](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/TemplateMsgLogDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/WxMsgDao.java:28](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/WxMsgDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/WxQrCodeDao.java:28](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/WxQrCodeDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/WxUserDao.java:26](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/WxUserDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dto/PageSizeConstant.java:22](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dto/PageSizeConstant.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/MsgReplyRuleEntity.java:35](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/MsgReplyRuleEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/MsgTemplateEntity.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/MsgTemplateEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/TemplateMsgLogEntity.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/TemplateMsgLogEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/WxMsgEntity.java:39](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/WxMsgEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/WxQrCodeEntity.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/WxQrCodeEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/WxUserEntity.java:40](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/WxUserEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/MaterialFileDeleteForm.java:25](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/MaterialFileDeleteForm.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/TemplateMsgBatchForm.java:36](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/TemplateMsgBatchForm.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxMsgReplyForm.java:26](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxMsgReplyForm.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxQrCodeForm.java:28](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxQrCodeForm.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxUserBatchTaggingForm.java:27](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxUserBatchTaggingForm.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxUserTagForm.java:27](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxUserTagForm.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxUserTaggingForm.java:26](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxUserTaggingForm.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/MsgReplyRuleService.java:30](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/MsgReplyRuleService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/MsgReplyService.java:28](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/MsgReplyService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/MsgTemplateService.java:31](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/MsgTemplateService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/TemplateMsgLogService.java:28](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/TemplateMsgLogService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/TemplateMsgService.java:25](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/TemplateMsgService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxAssetsService.java:28](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxAssetsService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxMsgService.java:30](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxMsgService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxQrCodeService.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxQrCodeService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxUserService.java:28](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxUserService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxUserTagsService.java:27](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxUserTagsService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/MsgReplyRuleServiceImpl.java:43](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/MsgReplyRuleServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/MsgReplyServiceImpl.java:46](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/MsgReplyServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/MsgTemplateServiceImpl.java:41](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/MsgTemplateServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/TemplateMsgLogServiceImpl.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/TemplateMsgLogServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/TemplateMsgServiceImpl.java:42](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/TemplateMsgServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxAssetsServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxAssetsServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxMsgServiceImpl.java:36](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxMsgServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxQrCodeServiceImpl.java:40](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxQrCodeServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxUserServiceImpl.java:50](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxUserServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxUserTagsServiceImpl.java:38](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxUserTagsServiceImpl.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/exception/BusinessException.java:26](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/exception/BusinessException.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/exception/BusinessExceptionHandler.java:34](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/exception/BusinessExceptionHandler.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/exception/FilterExceptionController.java:39](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/exception/FilterExceptionController.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/CharUtil.java:24](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/CharUtil.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/Constant.java:24](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/Constant.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/DateUtils.java:38](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/DateUtils.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/HttpContextUtils.java:29](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/HttpContextUtils.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/HttpUtils.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/HttpUtils.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/IpUtils.java:31](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/IpUtils.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/JsonUtils.java:25](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/JsonUtils.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/LambdaUtils.java:27](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/LambdaUtils.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/LatLgtUtils.java:27](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/LatLgtUtils.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/ObjectUtils.java:35](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/ObjectUtils.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/PdfUtils.java:33](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/PdfUtils.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/Query.java:30](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/Query.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/RestResponse.java:32](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/RestResponse.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/SeckillStatEnum.java:22](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/SeckillStatEnum.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/SpringContextUtils.java:29](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/SpringContextUtils.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/StringUtils.java:29](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/StringUtils.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/TokenGenerator.java:29](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/TokenGenerator.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/XssFilter.java:28](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/XssFilter.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/XssHttpServletRequestWrapper.java:39](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/XssHttpServletRequestWrapper.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/AntiSqlInjectionFilter.java:32](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/AntiSqlInjectionFilter.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/CorsConfig.java:33](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/CorsConfig.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/FilterConfig.java:32](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/FilterConfig.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/RedisConfig.java:54](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/RedisConfig.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/RedisLock.java:30](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/RedisLock.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/RedisTemplateUtil.java:37](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/RedisTemplateUtil.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/WebConfigurer.java:37](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/WebConfigurer.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/undertom/CustomizationBean.java:30](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/undertom/CustomizationBean.java)

### hehailong (31文件)

- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/controller/SysGeneratorController.java:48](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/controller/SysGeneratorController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/dao/SysGeneratorDao.java:34](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/dao/SysGeneratorDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/entity/ColumnEntity.java:24](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/entity/ColumnEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/entity/ResultMapEntity.java:30](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/entity/ResultMapEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/entity/TableEntity.java:26](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/entity/TableEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/service/SysGeneratorService.java:32](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/service/SysGeneratorService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/utils/GenUtils.java:45](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/utils/GenUtils.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCommentController.java:42](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCommentController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCommentPictureController.java:41](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCommentPictureController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysRoleOrgService.java:30](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysRoleOrgService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysRoleOrgServiceImpl.java:35](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysRoleOrgServiceImpl.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppBaseController.java:37](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppBaseController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppBuyController.java:21](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppBuyController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppCouponController.java:35](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppCouponController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppHelpIssueController.java:23](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppHelpIssueController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppRegisterController.java:18](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppRegisterController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppSearchController.java:28](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppSearchController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppTestController.java:19](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppTestController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/entity/FullUserInfo.java:27](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/entity/FullUserInfo.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/vo/RegionVo.java:7](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/vo/RegionVo.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/cache/RegionCacheUtil.java:13](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/cache/RegionCacheUtil.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCommentDao.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCommentDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCommentPictureDao.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCommentPictureDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCommentEntity.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCommentEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCommentPictureEntity.java:31](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCommentPictureEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCommentService.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCommentService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysRegionDao.java:12](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysRegionDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysRegionEntity.java:11](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysRegionEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/SysRegionService.java:11](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/SysRegionService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/SysRegionServiceImpl.java:15](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/SysRegionServiceImpl.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/SmsUtil.java:37](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/SmsUtil.java)

### pengjun (18文件)

- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppRegionController.java:22](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppRegionController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppUserController.java:25](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppUserController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/request/ApiAuthLoginByWeixinRequest.java:7](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/request/ApiAuthLoginByWeixinRequest.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/request/ApiOrderSubmitRequest.java:8](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/request/ApiOrderSubmitRequest.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/request/LoginAppRequest.java:8](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/request/LoginAppRequest.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/request/LoginWxMpRequest.java:7](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/request/LoginWxMpRequest.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/request/LoginWxPhoneRequest.java:7](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/request/LoginWxPhoneRequest.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/request/LoginWxRequest.java:8](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/request/LoginWxRequest.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/request/PhoneCodeRequest.java:7](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/request/PhoneCodeRequest.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/request/PhoneRequest.java:7](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/request/PhoneRequest.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/vo/LoginVo.java:8](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/vo/LoginVo.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallProductDao.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallProductDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dto/GoodsAggregateDTO.java:31](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dto/GoodsAggregateDTO.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallProductEntity.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallProductEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsAggregateService.java:8](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsAggregateService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallProductService.java:30](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallProductService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallProductServiceImpl.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallProductServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/support/SkuCartesianHelper.java:26](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/support/SkuCartesianHelper.java)

### Harmon <br> (14文件)

- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppAddressController.java:23](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppAddressController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppBrandController.java:22](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppBrandController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppCartController.java:28](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppCartController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppCatalogController.java:20](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppCatalogController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppCollectController.java:22](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppCollectController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppCommentController.java:27](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppCommentController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppFeedbackController.java:21](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppFeedbackController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppFootprintController.java:24](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppFootprintController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppGoodsController.java:26](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppGoodsController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppIndexController.java:23](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppIndexController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppOrderController.java:35](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppOrderController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppPayController.java:42](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppPayController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppTopicController.java:22](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppTopicController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/entity/CouponInfoVo.java:6](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/entity/CouponInfoVo.java)

### Binary Wang(https://github.com/binarywang) (14文件)

- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/builder/AbstractBuilder.java:30](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/builder/AbstractBuilder.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/builder/ImageBuilder.java:29](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/builder/ImageBuilder.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/builder/TextBuilder.java:29](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/builder/TextBuilder.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/AbstractHandler.java:28](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/AbstractHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/KfSessionHandler.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/KfSessionHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/LocationHandler.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/LocationHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/LogHandler.java:36](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/LogHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/MenuHandler.java:36](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/MenuHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/MsgHandler.java:37](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/MsgHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/NullHandler.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/NullHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/ScanHandler.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/ScanHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/StoreCheckNotifyHandler.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/StoreCheckNotifyHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/SubscribeHandler.java:36](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/SubscribeHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/UnsubscribeHandler.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/UnsubscribeHandler.java)

### Torstein Hønsi (5文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/themes/dark-blue.js:3` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/themes/dark-green.js:3` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/themes/gray.js:3` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/themes/grid.js:3` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/themes/skies.js:3` (基线中已移除文件)

### ${author} (5文件)

- [vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Controller.java.vm:41](../vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Controller.java.vm)
- [vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Dao.java.vm:33](../vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Dao.java.vm)
- [vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Entity.java.vm:36](../vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Entity.java.vm)
- [vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Service.java.vm:32](../vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Service.java.vm)
- [vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/ServiceImpl.java.vm:38](../vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/ServiceImpl.java.vm)

### hehailong <br> (4文件)

- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppUploadController.java:47](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppUploadController.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/AesUtil.java:29](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/AesUtil.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/Base64Util.java:25](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/Base64Util.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/QRCodeUtil.java:37](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/QRCodeUtil.java)

### Michael Nelson, Torstein Hønsi. (2文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/adapters/prototype-adapter.js:5` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/adapters/prototype-adapter.src.js:5` (基线中已移除文件)

### Stoyan Stefanov <sstoo@gmail.com> (2文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/modules/canvas-tools.js:3` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/modules/canvas-tools.src.js:3` (基线中已移除文件)

### liliudong (2文件)

- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/NoPetsGroup.java:6](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/NoPetsGroup.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/PetsGroup.java:6](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/PetsGroup.java)

### Binary Wang (2文件)

- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxPayConfiguration.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxPayConfiguration.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxPayProperties.java:30](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxPayProperties.java)

### zhuliyun (2文件)

- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/JsonDateSerializer.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/JsonDateSerializer.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/JsonTimeSerializer.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/JsonTimeSerializer.java)

### erik (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/wordimage/tangram.js:830` (基线中已移除文件)

### zhanyi (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/ueditor.all.js:14530,16487,17197,17376,21925,22842` (基线中已移除文件)

### xuheng (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/ueditor.all.js:22424` (基线中已移除文件)

### Jinqn (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/ueditor.all.js:23733,24457` (基线中已移除文件)

### Guoqing (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/ueditor.all.js:24506` (基线中已移除文件)

### robbenmu (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/ueditor.all.js:24884` (基线中已移除文件)

### Administrator (1文件)

- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/RequestUriLogInterceptor.java:18](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/RequestUriLogInterceptor.java)

### liepngjun (1文件)

- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppSmsController.java:31](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppSmsController.java)

### Joseph O'Connell (1文件)

- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/HtmlFilter.java:56](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/HtmlFilter.java)

### Cal Hendersen (1文件)

- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/HtmlFilter.java:57](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/HtmlFilter.java)

### Michael Semb Wever (1文件)

- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/HtmlFilter.java:58](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/HtmlFilter.java)


## 6. 邮箱: 全部已检出值及文件

845367567@qq.com与当前身份相符; 939961241@qq.com、139邮箱等未在本轮确认为本人联系方式. 示例邮箱/库作者联系方式不等于应删除内容, 需要按所属文件用途处理.

### 845367567@qq.com (459文件)

- [vendor/platform-wxshop/platform-admin-ui/package.json:5](../vendor/platform-wxshop/platform-admin-ui/package.json)
- [vendor/platform-wxshop/platform-admin-ui/src/views/common/login.vue:41](../vendor/platform-wxshop/platform-admin-ui/src/views/common/login.vue)
- [vendor/platform-wxshop/platform-admin-ui/src/views/main-content.vue:33](../vendor/platform-wxshop/platform-admin-ui/src/views/main-content.vue)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/PlatformAdminApplication.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/PlatformAdminApplication.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/common/annotation/SysLog.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/common/annotation/SysLog.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/common/aspect/SysLogAspect.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/common/aspect/SysLogAspect.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/common/utils/ShiroUtils.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/common/utils/ShiroUtils.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/ActResourceHandlerConfig.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/ActResourceHandlerConfig.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/FilterConfig.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/FilterConfig.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/KaptchaConfig.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/KaptchaConfig.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/MybatisPlusConfig.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/MybatisPlusConfig.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/ShiroConfig.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/ShiroConfig.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/SwaggerConfig.java:14](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/SwaggerConfig.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/datascope/DataScope.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/datascope/DataScope.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/datascope/DataScopeInnerInterceptor.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/datascope/DataScopeInnerInterceptor.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/controller/SysGeneratorController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/controller/SysGeneratorController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/dao/SysGeneratorDao.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/dao/SysGeneratorDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/entity/ColumnEntity.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/entity/ColumnEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/entity/ResultMapEntity.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/entity/ResultMapEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/entity/TableEntity.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/entity/TableEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/service/SysGeneratorService.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/service/SysGeneratorService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/service/impl/SysGeneratorServiceImpl.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/service/impl/SysGeneratorServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/utils/GenUtils.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/utils/GenUtils.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/config/ScheduleConfig.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/config/ScheduleConfig.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/controller/ScheduleJobController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/controller/ScheduleJobController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/controller/ScheduleJobLogController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/controller/ScheduleJobLogController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/dao/ScheduleJobDao.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/dao/ScheduleJobDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/dao/ScheduleJobLogDao.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/dao/ScheduleJobLogDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/entity/ScheduleJobEntity.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/entity/ScheduleJobEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/entity/ScheduleJobLogEntity.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/entity/ScheduleJobLogEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/service/ScheduleJobLogService.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/service/ScheduleJobLogService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/service/ScheduleJobService.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/service/ScheduleJobService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/service/impl/ScheduleJobLogServiceImpl.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/service/impl/ScheduleJobLogServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/service/impl/ScheduleJobServiceImpl.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/service/impl/ScheduleJobServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/task/TestTask.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/task/TestTask.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/utils/ScheduleJob.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/utils/ScheduleJob.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/utils/ScheduleRunnable.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/utils/ScheduleRunnable.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/utils/ScheduleUtils.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/utils/ScheduleUtils.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAdController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAdController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAdPositionController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAdPositionController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAddressController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAddressController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAttributeCategoryController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAttributeCategoryController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAttributeController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAttributeController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallBrandController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallBrandController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCartController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCartController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCategoryController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCategoryController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallChannelController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallChannelController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCollectController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCollectController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCommentController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCommentController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCommentPictureController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCommentPictureController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCouponController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCouponController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCouponGoodsController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCouponGoodsController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallFeedbackController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallFeedbackController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallFootprintController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallFootprintController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsAttributeController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsAttributeController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsGalleryController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsGalleryController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsIssueController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsIssueController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsSpecificationController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsSpecificationController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallHelpIssueController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallHelpIssueController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallHelpTypeController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallHelpTypeController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallKeywordsController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallKeywordsController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallOrderController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallOrderController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallOrderGoodsController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallOrderGoodsController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallSearchHistoryController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallSearchHistoryController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallShippingController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallShippingController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallSpecificationController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallSpecificationController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallTopicCategoryController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallTopicCategoryController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallTopicController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallTopicController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallUserCouponController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallUserCouponController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallUserLevelController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallUserLevelController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/oss/controller/SysOssController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/oss/controller/SysOssController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/AbstractController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/AbstractController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysCacheController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysCacheController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysConfigController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysConfigController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysDictController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysDictController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysDictGroupController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysDictGroupController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysLogController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysLogController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysLoginController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysLoginController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysMenuController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysMenuController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysMonitorController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysMonitorController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysOrgController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysOrgController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysRoleController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysRoleController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysSmsLogController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysSmsLogController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysUserController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysUserController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysUserTokenController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysUserTokenController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysCaptchaDao.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysCaptchaDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysDictDao.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysDictDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysDictGroupDao.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysDictGroupDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysLogDao.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysLogDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysOrgDao.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysOrgDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysRoleDao.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysRoleDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysRoleMenuDao.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysRoleMenuDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysRoleOrgDao.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysRoleOrgDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysUserRoleDao.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysUserRoleDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysCacheEntity.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysCacheEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysCaptchaEntity.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysCaptchaEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysDictEntity.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysDictEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysDictGroupEntity.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysDictGroupEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysLogEntity.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysLogEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysOrgEntity.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysOrgEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysRoleEntity.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysRoleEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysRoleMenuEntity.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysRoleMenuEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysRoleOrgEntity.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysRoleOrgEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysUserRoleEntity.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysUserRoleEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/enums/MenuTypeEnum.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/enums/MenuTypeEnum.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/form/PasswordForm.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/form/PasswordForm.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/form/SysLoginForm.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/form/SysLoginForm.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/oauth2/Oauth2Filter.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/oauth2/Oauth2Filter.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/oauth2/Oauth2Realm.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/oauth2/Oauth2Realm.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/oauth2/Oauth2Token.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/oauth2/Oauth2Token.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysCacheService.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysCacheService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysCaptchaService.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysCaptchaService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysDictGroupService.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysDictGroupService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysDictService.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysDictService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysLogService.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysLogService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysMenuService.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysMenuService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysOrgService.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysOrgService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysRoleMenuService.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysRoleMenuService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysRoleOrgService.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysRoleOrgService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysRoleService.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysRoleService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysUserRoleService.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysUserRoleService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysUserService.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysUserService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysUserTokenService.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysUserTokenService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysCacheServiceImpl.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysCacheServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysCaptchaServiceImpl.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysCaptchaServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysDictGroupServiceImpl.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysDictGroupServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysDictServiceImpl.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysDictServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysLogServiceImpl.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysLogServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysMenuServiceImpl.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysMenuServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysOrgServiceImpl.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysOrgServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysRoleMenuServiceImpl.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysRoleMenuServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysRoleOrgServiceImpl.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysRoleOrgServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysRoleServiceImpl.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysRoleServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysUserRoleServiceImpl.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysUserRoleServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysUserServiceImpl.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysUserServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysUserTokenServiceImpl.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysUserTokenServiceImpl.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/MsgReplyRuleManageController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/MsgReplyRuleManageController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/MsgTemplateManageController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/MsgTemplateManageController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/TemplateMsgLogManageController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/TemplateMsgLogManageController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxAssetsManageController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxAssetsManageController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMaAnalysisController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMaAnalysisController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMenuManageController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMenuManageController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMpDraftController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMpDraftController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMpFreePublishController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMpFreePublishController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMsgManageController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMsgManageController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxQrCodeManageController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxQrCodeManageController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxUserManageController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxUserManageController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxUserTagsManageController.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxUserTagsManageController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/form/DraftArticles.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/form/DraftArticles.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/form/DraftForm.java:16](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/form/DraftForm.java)
- [vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Controller.java.vm:16](../vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Controller.java.vm)
- [vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Dao.java.vm:16](../vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Dao.java.vm)
- [vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Entity.java.vm:16](../vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Entity.java.vm)
- [vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Service.java.vm:16](../vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Service.java.vm)
- [vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/ServiceImpl.java.vm:16](../vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/ServiceImpl.java.vm)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/PlatformApiApplication.java:16](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/PlatformApiApplication.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/annotation/IgnoreAuth.java:16](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/annotation/IgnoreAuth.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/annotation/LoginUser.java:16](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/annotation/LoginUser.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/CacheConfig.java:16](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/CacheConfig.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/MybatisPlusConfig.java:16](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/MybatisPlusConfig.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/SwaggerConfig.java:16](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/SwaggerConfig.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/SwaggerResourceHandlerConfig.java:16](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/SwaggerResourceHandlerConfig.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/WebMvcConfig.java:16](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/WebMvcConfig.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/interceptor/AuthorizationInterceptor.java:16](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/interceptor/AuthorizationInterceptor.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppBaseController.java:16](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppBaseController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppLoginController.java:16](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppLoginController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppUploadController.java:16](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppUploadController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/WxMaPortalController.java:16](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/WxMaPortalController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/WxMpPortalController.java:16](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/WxMpPortalController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/entity/FullUserInfo.java:16](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/entity/FullUserInfo.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/resolver/LoginUserHandlerMethodArgumentResolver.java:16](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/resolver/LoginUserHandlerMethodArgumentResolver.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/utils/JwtUtils.java:16](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/utils/JwtUtils.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/builder/AbstractBuilder.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/builder/AbstractBuilder.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/builder/ImageBuilder.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/builder/ImageBuilder.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/builder/TextBuilder.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/builder/TextBuilder.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/annotation/LimitAspect.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/annotation/LimitAspect.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/annotation/LockAspect.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/annotation/LockAspect.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/annotation/ServiceLimit.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/annotation/ServiceLimit.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/annotation/ServiceLock.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/annotation/ServiceLock.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/AbstractAssert.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/AbstractAssert.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/ValidatorUtils.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/ValidatorUtils.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/AddGroup.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/AddGroup.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/AliyunGroup.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/AliyunGroup.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/DiskGroup.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/DiskGroup.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/Group.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/Group.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/HuaweiGroup.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/HuaweiGroup.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/QcloudGroup.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/QcloudGroup.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/QiniuGroup.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/QiniuGroup.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/UpdateGroup.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/UpdateGroup.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMaConfiguration.java:14](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMaConfiguration.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMaProperties.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMaProperties.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMpConfiguration.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMpConfiguration.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMpMessageRouterConfiguration.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMpMessageRouterConfiguration.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMpProperties.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMpProperties.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxPayConfiguration.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxPayConfiguration.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxPayProperties.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxPayProperties.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/AbstractHandler.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/AbstractHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/JsonArrayTypeHandler.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/JsonArrayTypeHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/KfSessionHandler.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/KfSessionHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/LocationHandler.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/LocationHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/LogHandler.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/LogHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/MenuHandler.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/MenuHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/MsgHandler.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/MsgHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/NullHandler.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/NullHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/ScanHandler.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/ScanHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/StoreCheckNotifyHandler.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/StoreCheckNotifyHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/SubscribeHandler.java:14](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/SubscribeHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/UnsubscribeHandler.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/UnsubscribeHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAdDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAdDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAdPositionDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAdPositionDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAddressDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAddressDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAttributeCategoryDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAttributeCategoryDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAttributeDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAttributeDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallBrandDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallBrandDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCartDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCartDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCategoryDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCategoryDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallChannelDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallChannelDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCollectDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCollectDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCommentDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCommentDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCommentPictureDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCommentPictureDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCouponDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCouponDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCouponGoodsDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCouponGoodsDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallFeedbackDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallFeedbackDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallFootprintDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallFootprintDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsAttributeDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsAttributeDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsGalleryDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsGalleryDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsIssueDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsIssueDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsSpecificationDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsSpecificationDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallHelpIssueDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallHelpIssueDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallHelpTypeDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallHelpTypeDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallKeywordsDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallKeywordsDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallOrderDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallOrderDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallOrderGoodsDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallOrderGoodsDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallProductDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallProductDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallRelatedGoodsDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallRelatedGoodsDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallSearchHistoryDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallSearchHistoryDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallShippingDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallShippingDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallSpecificationDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallSpecificationDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallTopicCategoryDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallTopicCategoryDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallTopicDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallTopicDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallUserCouponDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallUserCouponDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallUserLevelDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallUserLevelDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dto/GoodsAggregateDTO.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dto/GoodsAggregateDTO.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAdEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAdEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAdPositionEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAdPositionEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAddressEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAddressEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAttributeCategoryEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAttributeCategoryEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAttributeEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAttributeEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallBrandEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallBrandEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCartEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCartEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCategoryEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCategoryEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallChannelEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallChannelEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCollectEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCollectEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCommentEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCommentEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCommentPictureEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCommentPictureEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCouponEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCouponEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCouponGoodsEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCouponGoodsEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallFeedbackEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallFeedbackEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallFootprintEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallFootprintEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsAttributeEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsAttributeEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsGalleryEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsGalleryEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsIssueEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsIssueEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsSpecificationEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsSpecificationEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallHelpIssueEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallHelpIssueEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallHelpTypeEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallHelpTypeEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallKeywordsEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallKeywordsEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallOrderEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallOrderEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallOrderGoodsEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallOrderGoodsEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallProductEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallProductEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallRelatedGoodsEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallRelatedGoodsEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallSearchHistoryEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallSearchHistoryEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallShippingEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallShippingEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallSpecificationEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallSpecificationEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallTopicCategoryEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallTopicCategoryEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallTopicEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallTopicEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallUserCouponEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallUserCouponEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallUserLevelEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallUserLevelEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAdPositionService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAdPositionService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAdService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAdService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAddressService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAddressService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAttributeCategoryService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAttributeCategoryService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAttributeService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAttributeService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallBrandService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallBrandService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCartService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCartService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCategoryService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCategoryService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallChannelService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallChannelService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCollectService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCollectService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCommentPictureService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCommentPictureService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCommentService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCommentService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCouponGoodsService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCouponGoodsService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCouponService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCouponService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallFeedbackService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallFeedbackService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallFootprintService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallFootprintService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsAttributeService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsAttributeService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsGalleryService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsGalleryService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsIssueService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsIssueService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsSpecificationService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsSpecificationService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallHelpIssueService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallHelpIssueService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallHelpTypeService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallHelpTypeService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallKeywordsService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallKeywordsService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallOrderGoodsService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallOrderGoodsService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallOrderService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallOrderService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallProductService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallProductService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallRelatedGoodsService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallRelatedGoodsService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallSearchHistoryService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallSearchHistoryService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallShippingService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallShippingService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallSpecificationService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallSpecificationService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallTopicCategoryService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallTopicCategoryService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallTopicService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallTopicService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallUserCouponService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallUserCouponService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallUserLevelService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallUserLevelService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAdPositionServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAdPositionServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAdServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAdServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAddressServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAddressServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAttributeCategoryServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAttributeCategoryServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAttributeServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAttributeServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallBrandServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallBrandServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCartServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCartServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCategoryServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCategoryServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallChannelServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallChannelServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCollectServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCollectServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCommentPictureServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCommentPictureServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCommentServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCommentServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCouponGoodsServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCouponGoodsServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCouponServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCouponServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallFeedbackServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallFeedbackServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallFootprintServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallFootprintServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsAggregateServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsAggregateServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsAttributeServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsAttributeServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsGalleryServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsGalleryServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsIssueServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsIssueServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsSpecificationServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsSpecificationServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallHelpIssueServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallHelpIssueServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallHelpTypeServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallHelpTypeServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallKeywordsServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallKeywordsServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallOrderGoodsServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallOrderGoodsServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallOrderServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallOrderServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallProductServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallProductServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallRelatedGoodsServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallRelatedGoodsServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallSearchHistoryServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallSearchHistoryServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallShippingServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallShippingServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallSpecificationServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallSpecificationServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallTopicCategoryServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallTopicCategoryServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallTopicServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallTopicServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallUserCouponServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallUserCouponServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallUserLevelServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallUserLevelServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/support/SkuCartesianHelper.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/support/SkuCartesianHelper.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/AbstractCloudStorageService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/AbstractCloudStorageService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/AliyunCloudStorageService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/AliyunCloudStorageService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/CloudStorageConfig.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/CloudStorageConfig.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/DiskCloudStorageService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/DiskCloudStorageService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/QcloudCloudStorageService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/QcloudCloudStorageService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/QiniuCloudStorageService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/QiniuCloudStorageService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/UploadFactory.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/UploadFactory.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/dao/SysOssDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/dao/SysOssDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/entity/SysOssEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/entity/SysOssEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/service/SysOssService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/service/SysOssService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/service/impl/SysOssServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/service/impl/SysOssServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysConfigDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysConfigDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysMenuDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysMenuDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysSmsLogDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysSmsLogDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysUserDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysUserDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysUserTokenDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysUserTokenDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SmsConfig.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SmsConfig.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysConfigEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysConfigEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysMenuEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysMenuEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysSmsLogEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysSmsLogEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysUserEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysUserEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysUserTokenEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysUserTokenEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/MailService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/MailService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/ShiroService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/ShiroService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/SysConfigService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/SysConfigService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/SysSmsLogService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/SysSmsLogService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/MailServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/MailServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/ShiroServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/ShiroServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/SysConfigServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/SysConfigServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/SysSmsLogServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/SysSmsLogServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/TaskExcutor.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/TaskExcutor.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/MsgReplyRuleDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/MsgReplyRuleDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/MsgTemplateDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/MsgTemplateDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/TemplateMsgLogDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/TemplateMsgLogDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/WxMsgDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/WxMsgDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/WxQrCodeDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/WxQrCodeDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/WxUserDao.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/WxUserDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dto/PageSizeConstant.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dto/PageSizeConstant.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/MsgReplyRuleEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/MsgReplyRuleEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/MsgTemplateEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/MsgTemplateEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/TemplateMsgLogEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/TemplateMsgLogEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/WxMsgEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/WxMsgEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/WxQrCodeEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/WxQrCodeEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/WxUserEntity.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/WxUserEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/MaterialFileDeleteForm.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/MaterialFileDeleteForm.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/TemplateMsgBatchForm.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/TemplateMsgBatchForm.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxMsgReplyForm.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxMsgReplyForm.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxQrCodeForm.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxQrCodeForm.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxUserBatchTaggingForm.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxUserBatchTaggingForm.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxUserTagForm.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxUserTagForm.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxUserTaggingForm.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxUserTaggingForm.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/MsgReplyRuleService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/MsgReplyRuleService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/MsgReplyService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/MsgReplyService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/MsgTemplateService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/MsgTemplateService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/TemplateMsgLogService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/TemplateMsgLogService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/TemplateMsgService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/TemplateMsgService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxAssetsService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxAssetsService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxMsgService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxMsgService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxQrCodeService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxQrCodeService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxUserService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxUserService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxUserTagsService.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxUserTagsService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/MsgReplyRuleServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/MsgReplyRuleServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/MsgReplyServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/MsgReplyServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/MsgTemplateServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/MsgTemplateServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/TemplateMsgLogServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/TemplateMsgLogServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/TemplateMsgServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/TemplateMsgServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxAssetsServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxAssetsServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxMsgServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxMsgServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxQrCodeServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxQrCodeServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxUserServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxUserServiceImpl.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxUserTagsServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxUserTagsServiceImpl.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/exception/BusinessException.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/exception/BusinessException.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/exception/BusinessExceptionHandler.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/exception/BusinessExceptionHandler.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/exception/FilterExceptionController.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/exception/FilterExceptionController.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/AesUtil.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/AesUtil.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/Base64Util.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/Base64Util.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/CharUtil.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/CharUtil.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/Constant.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/Constant.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/DateUtils.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/DateUtils.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/HttpContextUtils.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/HttpContextUtils.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/IpUtils.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/IpUtils.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/JsonUtils.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/JsonUtils.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/LambdaUtils.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/LambdaUtils.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/LatLgtUtils.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/LatLgtUtils.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/ObjectUtils.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/ObjectUtils.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/PdfUtils.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/PdfUtils.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/QRCodeUtil.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/QRCodeUtil.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/Query.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/Query.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/RestResponse.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/RestResponse.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/SeckillStatEnum.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/SeckillStatEnum.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/SmsUtil.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/SmsUtil.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/SpringContextUtils.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/SpringContextUtils.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/StringUtils.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/StringUtils.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/TokenGenerator.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/TokenGenerator.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/HtmlFilter.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/HtmlFilter.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/XssFilter.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/XssFilter.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/XssHttpServletRequestWrapper.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/XssHttpServletRequestWrapper.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/AntiSqlInjectionFilter.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/AntiSqlInjectionFilter.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/CorsConfig.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/CorsConfig.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/FilterConfig.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/FilterConfig.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/RedisConfig.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/RedisConfig.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/RedisLock.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/RedisLock.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/RedisTemplateUtil.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/RedisTemplateUtil.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/WebConfigurer.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/WebConfigurer.java)
- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/undertom/CustomizationBean.java:16](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/undertom/CustomizationBean.java)
- [vendor/platform-wxshop/wx-mall/pages/auth/btnAuth/btnAuth.wxml:5,9](../vendor/platform-wxshop/wx-mall/pages/auth/btnAuth/btnAuth.wxml)
- [vendor/platform-wxshop/wx-mall/pages/ucenter/index/index.wxml:28](../vendor/platform-wxshop/wx-mall/pages/ucenter/index/index.wxml)

### 939961241@qq.com (32文件)

- [vendor/platform-wxshop/_sql/base.sql:738](../vendor/platform-wxshop/_sql/base.sql)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/SwaggerConfig.java:74](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/SwaggerConfig.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/controller/SysGeneratorController.java:49](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/controller/SysGeneratorController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/dao/SysGeneratorDao.java:35](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/dao/SysGeneratorDao.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/entity/ColumnEntity.java:25](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/entity/ColumnEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/entity/TableEntity.java:27](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/entity/TableEntity.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/service/SysGeneratorService.java:33](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/service/SysGeneratorService.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/utils/GenUtils.java:46](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/utils/GenUtils.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCommentController.java:43](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCommentController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCommentPictureController.java:42](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCommentPictureController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/SwaggerConfig.java:76](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/SwaggerConfig.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppCouponController.java:36](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppCouponController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppHelpIssueController.java:24](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppHelpIssueController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppRegisterController.java:19](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppRegisterController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppSearchController.java:29](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppSearchController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppSmsController.java:32](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppSmsController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppTestController.java:20](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppTestController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/vo/RegionVo.java:8](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/vo/RegionVo.java)
- [vendor/platform-wxshop/platform-api/src/test/java/com/platform/PlatformApiApplicationTests.java:41](../vendor/platform-wxshop/platform-api/src/test/java/com/platform/PlatformApiApplicationTests.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/cache/RegionCacheUtil.java:14](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/cache/RegionCacheUtil.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/MinioGroup.java:7](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/MinioGroup.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCommentDao.java:35](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCommentDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCommentPictureDao.java:35](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCommentPictureDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCommentEntity.java:35](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCommentEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCommentPictureEntity.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCommentPictureEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCommentService.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCommentService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/HuaWeiCloudStorageService.java:17](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/HuaWeiCloudStorageService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/MinioCloudStorageService.java:20](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/MinioCloudStorageService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysRegionDao.java:13](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysRegionDao.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysRegionEntity.java:12](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysRegionEntity.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/SysRegionService.java:12](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/SysRegionService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/SysRegionServiceImpl.java:16](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/SysRegionServiceImpl.java)

### sstoo@gmail.com (2文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/modules/canvas-tools.js:3` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/modules/canvas-tools.src.js:3` (基线中已移除文件)

### gabelerner@gmail.com (2文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/modules/canvas-tools.js:9` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/modules/canvas-tools.src.js:293` (基线中已移除文件)

### 15209831990@139.com (2文件)

- [vendor/platform-wxshop/platform-admin/src/main/resources/application.yml:108](../vendor/platform-wxshop/platform-admin/src/main/resources/application.yml)
- [vendor/platform-wxshop/platform-api/src/main/resources/application.yml:91](../vendor/platform-wxshop/platform-api/src/main/resources/application.yml)

### i@izs.me (1文件)

- [vendor/platform-wxshop/platform-admin-ui/pnpm-lock.yaml:2672,2676,5477](../vendor/platform-wxshop/platform-admin-ui/pnpm-lock.yaml)

### oising@gmail.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/SyntaxHighlighter/shCore.js:1972` (基线中已移除文件)

### hancong03@baidu.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/ueditor.all.js:26412` (基线中已移除文件)

### sadfsd@sdfsd.cn (1文件)

- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/StringUtils.java:136](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/StringUtils.java)

### address@domain.foo (1文件)

- `vendor/platform-wxshop/wx-mall/lib/wxParse/showdown.js:1252` (基线中已移除文件)

### foo@example.com (1文件)

- `vendor/platform-wxshop/wx-mall/lib/wxParse/showdown.js:1568` (基线中已移除文件)


## 7. 无关品牌、旧目录文案和未确认联系方式: 逐文件

| 文件 | 标记 | 使用位置类别 |
|---|---|---|
| [README.md](../README.md) | openwtai域名, 微同品牌, 严选/旧商城示例文案 | 文档 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/_index.yaml` (基线中已移除文件) | 微同品牌 | 旧工具文档 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/微同商城前端应用/_module.yaml` (基线中已移除文件) | 微同品牌 | 旧工具文档 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/微同平台管理后台前端/_module.yaml` (基线中已移除文件) | 微同品牌 | 旧工具文档 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/微同平台管理后台前端/概述.md` (基线中已移除文件) | 微同品牌 | 旧工具文档 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/系统架构与开发文档/概述.md` (基线中已移除文件) | 微同品牌 | 旧工具文档 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/UniApp跨平台商城/API接口与网络请求.md` (基线中已移除文件) | openwtai域名 | 旧工具文档 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/UniApp跨平台商城/跨平台适配策略.md` (基线中已移除文件) | 微同品牌 | 旧工具文档 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/UniApp跨平台商城/页面路由系统.md` (基线中已移除文件) | 微同品牌 | 旧工具文档 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/微信小程序原生开发/微信小程序原生开发.md` (基线中已移除文件) | 严选/旧商城示例文案 | 旧工具文档 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/快速开始.md` (基线中已移除文件) | openwtai域名 | 旧工具文档 |
| [vendor/platform-wxshop/README.md](../vendor/platform-wxshop/README.md) | openwtai域名, 严选/旧商城示例文案 | 文档 |
| [vendor/platform-wxshop/_sql/base.sql](../vendor/platform-wxshop/_sql/base.sql) | 微同品牌, 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/_sql/mall.sql](../vendor/platform-wxshop/_sql/mall.sql) | 严选/旧商城示例文案 | 源码/配置/SQL |
| [vendor/platform-wxshop/_sql/migrations/20260901_cleanup_legacy_catalog.sql](../vendor/platform-wxshop/_sql/migrations/20260901_cleanup_legacy_catalog.sql) | 严选/旧商城示例文案 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/PlatformAdminApplication.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/PlatformAdminApplication.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/common/annotation/SysLog.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/common/annotation/SysLog.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/common/aspect/SysLogAspect.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/common/aspect/SysLogAspect.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/common/utils/ShiroUtils.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/common/utils/ShiroUtils.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/ActResourceHandlerConfig.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/ActResourceHandlerConfig.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/FilterConfig.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/FilterConfig.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/KaptchaConfig.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/KaptchaConfig.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/MybatisPlusConfig.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/MybatisPlusConfig.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/ShiroConfig.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/ShiroConfig.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/SwaggerConfig.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/SwaggerConfig.java) | openwtai域名, 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/datascope/DataScope.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/datascope/DataScope.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/datascope/DataScopeInnerInterceptor.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/datascope/DataScopeInnerInterceptor.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/controller/SysGeneratorController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/controller/SysGeneratorController.java) | openwtai域名, 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/dao/SysGeneratorDao.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/dao/SysGeneratorDao.java) | openwtai域名, 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/entity/ColumnEntity.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/entity/ColumnEntity.java) | openwtai域名, 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/entity/ResultMapEntity.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/entity/ResultMapEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/entity/TableEntity.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/entity/TableEntity.java) | openwtai域名, 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/service/SysGeneratorService.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/service/SysGeneratorService.java) | openwtai域名, 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/service/impl/SysGeneratorServiceImpl.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/service/impl/SysGeneratorServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/utils/GenUtils.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/gen/utils/GenUtils.java) | openwtai域名, 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/config/ScheduleConfig.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/config/ScheduleConfig.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/controller/ScheduleJobController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/controller/ScheduleJobController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/controller/ScheduleJobLogController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/controller/ScheduleJobLogController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/dao/ScheduleJobDao.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/dao/ScheduleJobDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/dao/ScheduleJobLogDao.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/dao/ScheduleJobLogDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/entity/ScheduleJobEntity.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/entity/ScheduleJobEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/entity/ScheduleJobLogEntity.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/entity/ScheduleJobLogEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/service/ScheduleJobLogService.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/service/ScheduleJobLogService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/service/ScheduleJobService.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/service/ScheduleJobService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/service/impl/ScheduleJobLogServiceImpl.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/service/impl/ScheduleJobLogServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/service/impl/ScheduleJobServiceImpl.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/service/impl/ScheduleJobServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/task/TestTask.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/task/TestTask.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/utils/ScheduleJob.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/utils/ScheduleJob.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/utils/ScheduleRunnable.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/utils/ScheduleRunnable.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/utils/ScheduleUtils.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/job/utils/ScheduleUtils.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAdController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAdController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAdPositionController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAdPositionController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAddressController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAddressController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAttributeCategoryController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAttributeCategoryController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAttributeController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallAttributeController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallBrandController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallBrandController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCartController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCartController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCategoryController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCategoryController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallChannelController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallChannelController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCollectController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCollectController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCommentController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCommentController.java) | openwtai域名, 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCommentPictureController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCommentPictureController.java) | openwtai域名, 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCouponController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCouponController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCouponGoodsController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallCouponGoodsController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallFeedbackController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallFeedbackController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallFootprintController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallFootprintController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsAttributeController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsAttributeController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsGalleryController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsGalleryController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsIssueController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsIssueController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsSpecificationController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallGoodsSpecificationController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallHelpIssueController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallHelpIssueController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallHelpTypeController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallHelpTypeController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallKeywordsController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallKeywordsController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallOrderController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallOrderController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallOrderGoodsController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallOrderGoodsController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallSearchHistoryController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallSearchHistoryController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallShippingController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallShippingController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallSpecificationController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallSpecificationController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallTopicCategoryController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallTopicCategoryController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallTopicController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallTopicController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallUserCouponController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallUserCouponController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallUserLevelController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/mall/controller/MallUserLevelController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/oss/controller/SysOssController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/oss/controller/SysOssController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/AbstractController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/AbstractController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysCacheController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysCacheController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysConfigController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysConfigController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysDictController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysDictController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysDictGroupController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysDictGroupController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysLogController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysLogController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysLoginController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysLoginController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysMenuController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysMenuController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysMonitorController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysMonitorController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysOrgController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysOrgController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysRoleController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysRoleController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysSmsLogController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysSmsLogController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysUserController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysUserController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysUserTokenController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/controller/SysUserTokenController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysCaptchaDao.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysCaptchaDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysDictDao.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysDictDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysDictGroupDao.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysDictGroupDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysLogDao.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysLogDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysOrgDao.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysOrgDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysRoleDao.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysRoleDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysRoleMenuDao.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysRoleMenuDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysRoleOrgDao.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysRoleOrgDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysUserRoleDao.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/dao/SysUserRoleDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysCacheEntity.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysCacheEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysCaptchaEntity.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysCaptchaEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysDictEntity.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysDictEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysDictGroupEntity.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysDictGroupEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysLogEntity.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysLogEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysOrgEntity.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysOrgEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysRoleEntity.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysRoleEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysRoleMenuEntity.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysRoleMenuEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysRoleOrgEntity.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysRoleOrgEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysUserRoleEntity.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/entity/SysUserRoleEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/enums/MenuTypeEnum.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/enums/MenuTypeEnum.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/form/PasswordForm.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/form/PasswordForm.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/form/SysLoginForm.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/form/SysLoginForm.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/oauth2/Oauth2Filter.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/oauth2/Oauth2Filter.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/oauth2/Oauth2Realm.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/oauth2/Oauth2Realm.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/oauth2/Oauth2Token.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/oauth2/Oauth2Token.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysCacheService.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysCacheService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysCaptchaService.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysCaptchaService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysDictGroupService.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysDictGroupService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysDictService.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysDictService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysLogService.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysLogService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysMenuService.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysMenuService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysOrgService.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysOrgService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysRoleMenuService.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysRoleMenuService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysRoleOrgService.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysRoleOrgService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysRoleService.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysRoleService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysUserRoleService.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysUserRoleService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysUserService.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysUserService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysUserTokenService.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/SysUserTokenService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysCacheServiceImpl.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysCacheServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysCaptchaServiceImpl.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysCaptchaServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysDictGroupServiceImpl.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysDictGroupServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysDictServiceImpl.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysDictServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysLogServiceImpl.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysLogServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysMenuServiceImpl.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysMenuServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysOrgServiceImpl.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysOrgServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysRoleMenuServiceImpl.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysRoleMenuServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysRoleOrgServiceImpl.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysRoleOrgServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysRoleServiceImpl.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysRoleServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysUserRoleServiceImpl.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysUserRoleServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysUserServiceImpl.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysUserServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysUserTokenServiceImpl.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/sys/service/impl/SysUserTokenServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/MsgReplyRuleManageController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/MsgReplyRuleManageController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/MsgTemplateManageController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/MsgTemplateManageController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/TemplateMsgLogManageController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/TemplateMsgLogManageController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxAssetsManageController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxAssetsManageController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMaAnalysisController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMaAnalysisController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMenuManageController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMenuManageController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMpDraftController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMpDraftController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMpFreePublishController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMpFreePublishController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMsgManageController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMsgManageController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxQrCodeManageController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxQrCodeManageController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxUserManageController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxUserManageController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxUserTagsManageController.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxUserTagsManageController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/form/DraftArticles.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/form/DraftArticles.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/form/DraftForm.java](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/form/DraftForm.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/resources/application.yml](../vendor/platform-wxshop/platform-admin/src/main/resources/application.yml) | 非当前已确认邮箱, openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Controller.java.vm](../vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Controller.java.vm) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Dao.java.vm](../vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Dao.java.vm) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Entity.java.vm](../vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Entity.java.vm) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Service.java.vm](../vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Service.java.vm) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/ServiceImpl.java.vm](../vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/ServiceImpl.java.vm) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/java/com/platform/PlatformApiApplication.java](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/PlatformApiApplication.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/java/com/platform/annotation/IgnoreAuth.java](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/annotation/IgnoreAuth.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/java/com/platform/annotation/LoginUser.java](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/annotation/LoginUser.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/CacheConfig.java](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/CacheConfig.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/MybatisPlusConfig.java](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/MybatisPlusConfig.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/SwaggerConfig.java](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/SwaggerConfig.java) | openwtai域名, 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/SwaggerResourceHandlerConfig.java](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/SwaggerResourceHandlerConfig.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/WebMvcConfig.java](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/WebMvcConfig.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/java/com/platform/interceptor/AuthorizationInterceptor.java](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/interceptor/AuthorizationInterceptor.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppBaseController.java](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppBaseController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppCouponController.java](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppCouponController.java) | 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppHelpIssueController.java](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppHelpIssueController.java) | 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppLoginController.java](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppLoginController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppRegisterController.java](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppRegisterController.java) | 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppSearchController.java](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppSearchController.java) | 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppSmsController.java](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppSmsController.java) | 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppTestController.java](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppTestController.java) | 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppUploadController.java](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppUploadController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/WxMaPortalController.java](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/WxMaPortalController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/WxMpPortalController.java](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/WxMpPortalController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/entity/FullUserInfo.java](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/entity/FullUserInfo.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/vo/RegionVo.java](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/vo/RegionVo.java) | 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/java/com/platform/resolver/LoginUserHandlerMethodArgumentResolver.java](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/resolver/LoginUserHandlerMethodArgumentResolver.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/java/com/platform/utils/JwtUtils.java](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/utils/JwtUtils.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/main/resources/application.yml](../vendor/platform-wxshop/platform-api/src/main/resources/application.yml) | 非当前已确认邮箱, openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-api/src/test/java/com/platform/PlatformApiApplicationTests.java](../vendor/platform-wxshop/platform-api/src/test/java/com/platform/PlatformApiApplicationTests.java) | 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/builder/AbstractBuilder.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/builder/AbstractBuilder.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/builder/ImageBuilder.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/builder/ImageBuilder.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/builder/TextBuilder.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/builder/TextBuilder.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/cache/RegionCacheUtil.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/cache/RegionCacheUtil.java) | 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/annotation/LimitAspect.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/annotation/LimitAspect.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/annotation/LockAspect.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/annotation/LockAspect.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/annotation/ServiceLimit.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/annotation/ServiceLimit.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/annotation/ServiceLock.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/annotation/ServiceLock.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/AbstractAssert.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/AbstractAssert.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/ValidatorUtils.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/ValidatorUtils.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/AddGroup.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/AddGroup.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/AliyunGroup.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/AliyunGroup.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/DiskGroup.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/DiskGroup.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/Group.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/Group.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/HuaweiGroup.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/HuaweiGroup.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/MinioGroup.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/MinioGroup.java) | 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/QcloudGroup.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/QcloudGroup.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/QiniuGroup.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/QiniuGroup.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/UpdateGroup.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/group/UpdateGroup.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMaConfiguration.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMaConfiguration.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMaProperties.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMaProperties.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMpConfiguration.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMpConfiguration.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMpMessageRouterConfiguration.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMpMessageRouterConfiguration.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMpProperties.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxMpProperties.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxPayConfiguration.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxPayConfiguration.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxPayProperties.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/config/WxPayProperties.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/AbstractHandler.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/AbstractHandler.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/JsonArrayTypeHandler.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/JsonArrayTypeHandler.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/KfSessionHandler.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/KfSessionHandler.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/LocationHandler.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/LocationHandler.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/LogHandler.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/LogHandler.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/MenuHandler.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/MenuHandler.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/MsgHandler.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/MsgHandler.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/NullHandler.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/NullHandler.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/ScanHandler.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/ScanHandler.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/StoreCheckNotifyHandler.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/StoreCheckNotifyHandler.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/SubscribeHandler.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/SubscribeHandler.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/UnsubscribeHandler.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/UnsubscribeHandler.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAdDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAdDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAdPositionDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAdPositionDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAddressDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAddressDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAttributeCategoryDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAttributeCategoryDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAttributeDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallAttributeDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallBrandDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallBrandDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCartDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCartDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCategoryDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCategoryDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallChannelDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallChannelDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCollectDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCollectDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCommentDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCommentDao.java) | openwtai域名, 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCommentPictureDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCommentPictureDao.java) | openwtai域名, 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCouponDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCouponDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCouponGoodsDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallCouponGoodsDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallFeedbackDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallFeedbackDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallFootprintDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallFootprintDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsAttributeDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsAttributeDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsGalleryDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsGalleryDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsIssueDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsIssueDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsSpecificationDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallGoodsSpecificationDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallHelpIssueDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallHelpIssueDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallHelpTypeDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallHelpTypeDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallKeywordsDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallKeywordsDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallOrderDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallOrderDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallOrderGoodsDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallOrderGoodsDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallProductDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallProductDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallRelatedGoodsDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallRelatedGoodsDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallSearchHistoryDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallSearchHistoryDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallShippingDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallShippingDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallSpecificationDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallSpecificationDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallTopicCategoryDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallTopicCategoryDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallTopicDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallTopicDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallUserCouponDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallUserCouponDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallUserLevelDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dao/MallUserLevelDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dto/GoodsAggregateDTO.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/dto/GoodsAggregateDTO.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAdEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAdEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAdPositionEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAdPositionEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAddressEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAddressEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAttributeCategoryEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAttributeCategoryEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAttributeEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallAttributeEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallBrandEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallBrandEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCartEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCartEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCategoryEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCategoryEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallChannelEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallChannelEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCollectEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCollectEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCommentEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCommentEntity.java) | openwtai域名, 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCommentPictureEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCommentPictureEntity.java) | openwtai域名, 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCouponEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCouponEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCouponGoodsEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallCouponGoodsEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallFeedbackEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallFeedbackEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallFootprintEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallFootprintEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsAttributeEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsAttributeEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsGalleryEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsGalleryEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsIssueEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsIssueEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsSpecificationEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallGoodsSpecificationEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallHelpIssueEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallHelpIssueEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallHelpTypeEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallHelpTypeEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallKeywordsEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallKeywordsEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallOrderEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallOrderEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallOrderGoodsEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallOrderGoodsEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallProductEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallProductEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallRelatedGoodsEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallRelatedGoodsEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallSearchHistoryEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallSearchHistoryEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallShippingEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallShippingEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallSpecificationEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallSpecificationEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallTopicCategoryEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallTopicCategoryEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallTopicEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallTopicEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallUserCouponEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallUserCouponEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallUserLevelEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/entity/MallUserLevelEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAdPositionService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAdPositionService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAdService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAdService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAddressService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAddressService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAttributeCategoryService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAttributeCategoryService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAttributeService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallAttributeService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallBrandService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallBrandService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCartService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCartService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCategoryService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCategoryService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallChannelService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallChannelService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCollectService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCollectService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCommentPictureService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCommentPictureService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCommentService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCommentService.java) | openwtai域名, 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCouponGoodsService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCouponGoodsService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCouponService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallCouponService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallFeedbackService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallFeedbackService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallFootprintService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallFootprintService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsAttributeService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsAttributeService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsGalleryService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsGalleryService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsIssueService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsIssueService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsSpecificationService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallGoodsSpecificationService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallHelpIssueService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallHelpIssueService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallHelpTypeService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallHelpTypeService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallKeywordsService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallKeywordsService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallOrderGoodsService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallOrderGoodsService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallOrderService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallOrderService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallProductService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallProductService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallRelatedGoodsService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallRelatedGoodsService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallSearchHistoryService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallSearchHistoryService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallShippingService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallShippingService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallSpecificationService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallSpecificationService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallTopicCategoryService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallTopicCategoryService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallTopicService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallTopicService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallUserCouponService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallUserCouponService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallUserLevelService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/MallUserLevelService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAdPositionServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAdPositionServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAdServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAdServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAddressServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAddressServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAttributeCategoryServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAttributeCategoryServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAttributeServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallAttributeServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallBrandServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallBrandServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCartServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCartServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCategoryServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCategoryServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallChannelServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallChannelServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCollectServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCollectServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCommentPictureServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCommentPictureServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCommentServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCommentServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCouponGoodsServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCouponGoodsServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCouponServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallCouponServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallFeedbackServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallFeedbackServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallFootprintServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallFootprintServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsAggregateServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsAggregateServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsAttributeServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsAttributeServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsGalleryServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsGalleryServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsIssueServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsIssueServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsSpecificationServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallGoodsSpecificationServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallHelpIssueServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallHelpIssueServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallHelpTypeServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallHelpTypeServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallKeywordsServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallKeywordsServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallOrderGoodsServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallOrderGoodsServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallOrderServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallOrderServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallProductServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallProductServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallRelatedGoodsServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallRelatedGoodsServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallSearchHistoryServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallSearchHistoryServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallShippingServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallShippingServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallSpecificationServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallSpecificationServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallTopicCategoryServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallTopicCategoryServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallTopicServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallTopicServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallUserCouponServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallUserCouponServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallUserLevelServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/impl/MallUserLevelServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/support/SkuCartesianHelper.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/mall/service/support/SkuCartesianHelper.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/AbstractCloudStorageService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/AbstractCloudStorageService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/AliyunCloudStorageService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/AliyunCloudStorageService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/CloudStorageConfig.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/CloudStorageConfig.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/DiskCloudStorageService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/DiskCloudStorageService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/HuaWeiCloudStorageService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/HuaWeiCloudStorageService.java) | 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/MinioCloudStorageService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/MinioCloudStorageService.java) | 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/QcloudCloudStorageService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/QcloudCloudStorageService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/QiniuCloudStorageService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/QiniuCloudStorageService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/UploadFactory.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/UploadFactory.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/dao/SysOssDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/dao/SysOssDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/entity/SysOssEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/entity/SysOssEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/service/SysOssService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/service/SysOssService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/service/impl/SysOssServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/service/impl/SysOssServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysConfigDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysConfigDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysMenuDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysMenuDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysRegionDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysRegionDao.java) | 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysSmsLogDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysSmsLogDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysUserDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysUserDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysUserTokenDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/dao/SysUserTokenDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SmsConfig.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SmsConfig.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysConfigEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysConfigEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysMenuEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysMenuEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysRegionEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysRegionEntity.java) | 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysSmsLogEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysSmsLogEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysUserEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysUserEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysUserTokenEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/entity/SysUserTokenEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/MailService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/MailService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/ShiroService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/ShiroService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/SysConfigService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/SysConfigService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/SysRegionService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/SysRegionService.java) | 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/SysSmsLogService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/SysSmsLogService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/MailServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/MailServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/ShiroServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/ShiroServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/SysConfigServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/SysConfigServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/SysRegionServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/SysRegionServiceImpl.java) | 非当前已确认邮箱 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/SysSmsLogServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/sys/service/impl/SysSmsLogServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/TaskExcutor.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/TaskExcutor.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/MsgReplyRuleDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/MsgReplyRuleDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/MsgTemplateDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/MsgTemplateDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/TemplateMsgLogDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/TemplateMsgLogDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/WxMsgDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/WxMsgDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/WxQrCodeDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/WxQrCodeDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/WxUserDao.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dao/WxUserDao.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dto/PageSizeConstant.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/dto/PageSizeConstant.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/MsgReplyRuleEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/MsgReplyRuleEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/MsgTemplateEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/MsgTemplateEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/TemplateMsgLogEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/TemplateMsgLogEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/WxMsgEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/WxMsgEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/WxQrCodeEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/WxQrCodeEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/WxUserEntity.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/entity/WxUserEntity.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/MaterialFileDeleteForm.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/MaterialFileDeleteForm.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/TemplateMsgBatchForm.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/TemplateMsgBatchForm.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxMsgReplyForm.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxMsgReplyForm.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxQrCodeForm.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxQrCodeForm.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxUserBatchTaggingForm.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxUserBatchTaggingForm.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxUserTagForm.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxUserTagForm.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxUserTaggingForm.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/form/WxUserTaggingForm.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/MsgReplyRuleService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/MsgReplyRuleService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/MsgReplyService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/MsgReplyService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/MsgTemplateService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/MsgTemplateService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/TemplateMsgLogService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/TemplateMsgLogService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/TemplateMsgService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/TemplateMsgService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxAssetsService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxAssetsService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxMsgService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxMsgService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxQrCodeService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxQrCodeService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxUserService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxUserService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxUserTagsService.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/WxUserTagsService.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/MsgReplyRuleServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/MsgReplyRuleServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/MsgReplyServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/MsgReplyServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/MsgTemplateServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/MsgTemplateServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/TemplateMsgLogServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/TemplateMsgLogServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/TemplateMsgServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/TemplateMsgServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxAssetsServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxAssetsServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxMsgServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxMsgServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxQrCodeServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxQrCodeServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxUserServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxUserServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxUserTagsServiceImpl.java](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/WxUserTagsServiceImpl.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/exception/BusinessException.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/exception/BusinessException.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/exception/BusinessExceptionHandler.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/exception/BusinessExceptionHandler.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/exception/FilterExceptionController.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/exception/FilterExceptionController.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/AesUtil.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/AesUtil.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/Base64Util.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/Base64Util.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/CharUtil.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/CharUtil.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/Constant.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/Constant.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/DateUtils.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/DateUtils.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/HttpContextUtils.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/HttpContextUtils.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/IpUtils.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/IpUtils.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/JsonUtils.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/JsonUtils.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/LambdaUtils.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/LambdaUtils.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/LatLgtUtils.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/LatLgtUtils.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/ObjectUtils.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/ObjectUtils.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/PdfUtils.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/PdfUtils.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/QRCodeUtil.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/QRCodeUtil.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/Query.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/Query.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/RestResponse.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/RestResponse.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/SeckillStatEnum.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/SeckillStatEnum.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/SmsUtil.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/SmsUtil.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/SpringContextUtils.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/SpringContextUtils.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/StringUtils.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/StringUtils.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/TokenGenerator.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/TokenGenerator.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/HtmlFilter.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/HtmlFilter.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/XssFilter.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/XssFilter.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/XssHttpServletRequestWrapper.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/XssHttpServletRequestWrapper.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/AntiSqlInjectionFilter.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/AntiSqlInjectionFilter.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/CorsConfig.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/CorsConfig.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/FilterConfig.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/FilterConfig.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/RedisConfig.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/RedisConfig.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/RedisLock.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/RedisLock.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/RedisTemplateUtil.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/RedisTemplateUtil.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/WebConfigurer.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/config/WebConfigurer.java) | openwtai域名 | 源码/配置/SQL |
| [vendor/platform-wxshop/platform-common/src/main/java/com/platform/undertom/CustomizationBean.java](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/undertom/CustomizationBean.java) | openwtai域名 | 源码/配置/SQL |
| `vendor/platform-wxshop/wx-mall/agent/AGENTS.md` (基线中已移除文件) | 严选/旧商城示例文案 | 文档 |

## 8. Agent/Qoder/工具标记: 逐文件

路径命中和正文命中均列出; 正文仅是筛查候选, 不作为断言某代码由AI生成的证据.

| 文件 | 路径/内容依据 |
|---|---|
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/_index.yaml` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/基础设施部署与运维/_module.yaml` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/基础设施部署与运维/技术栈.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/基础设施部署与运维/架构设计.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/基础设施部署与运维/概述.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/基础设施部署与运维/特殊配置与命令.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/基础设施部署与运维/编码规范.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/微信小程序商城前端/_module.yaml` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/微信小程序商城前端/技术栈.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/微信小程序商城前端/架构设计.md:2` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/微信小程序商城前端/概述.md:1` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/微信小程序商城前端/编码规范.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/微同商城前端应用/_module.yaml` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/微同商城前端应用/技术栈.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/微同商城前端应用/架构设计.md:4` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/微同商城前端应用/概述.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/微同商城前端应用/编码规范.md:3` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/微同平台管理后台前端/_module.yaml` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/微同平台管理后台前端/技术栈.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/微同平台管理后台前端/架构设计.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/微同平台管理后台前端/概述.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/微同平台管理后台前端/编码规范.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/_module.yaml` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台核心业务与基础支撑模块/_module.yaml` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台核心业务与基础支撑模块/商城核心业务模块/_module.yaml` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台核心业务与基础支撑模块/商城核心业务模块/技术栈.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台核心业务与基础支撑模块/商城核心业务模块/架构设计.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台核心业务与基础支撑模块/商城核心业务模块/概述.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台核心业务与基础支撑模块/商城核心业务模块/编码规范.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台核心业务与基础支撑模块/多源对象存储服务模块/_module.yaml` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台核心业务与基础支撑模块/多源对象存储服务模块/架构设计.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台核心业务与基础支撑模块/多源对象存储服务模块/概述.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台核心业务与基础支撑模块/多源对象存储服务模块/编码规范.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台核心业务与基础支撑模块/微信公众号业务集成模块/_module.yaml` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台核心业务与基础支撑模块/微信公众号业务集成模块/技术栈.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台核心业务与基础支撑模块/微信公众号业务集成模块/架构设计.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台核心业务与基础支撑模块/微信公众号业务集成模块/概述.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台核心业务与基础支撑模块/微信公众号业务集成模块/编码规范.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台核心业务与基础支撑模块/技术栈.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台核心业务与基础支撑模块/架构设计.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台核心业务与基础支撑模块/概述.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台核心业务与基础支撑模块/系统基础配置与权限管理/_module.yaml` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台核心业务与基础支撑模块/系统基础配置与权限管理/技术栈.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台核心业务与基础支撑模块/系统基础配置与权限管理/架构设计.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台核心业务与基础支撑模块/系统基础配置与权限管理/概述.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台核心业务与基础支撑模块/系统基础配置与权限管理/编码规范.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台核心业务与基础支撑模块/编码规范.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台管理后台服务/_module.yaml` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台管理后台服务/技术栈.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台管理后台服务/架构设计.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台管理后台服务/概述.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台管理后台服务/编码规范.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台通用基础组件/_module.yaml` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台通用基础组件/技术栈.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台通用基础组件/架构设计.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台通用基础组件/概述.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/平台通用基础组件/编码规范.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/技术栈.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/架构设计.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/概述.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/移动端API网关与认证中心/_module.yaml` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/移动端API网关与认证中心/技术栈.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/移动端API网关与认证中心/架构设计.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/移动端API网关与认证中心/概述.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/移动端API网关与认证中心/编码规范.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/电商平台后端服务聚合体/编码规范.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/系统架构与开发文档/_module.yaml` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/系统架构与开发文档/技术栈.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/系统架构与开发文档/架构设计.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/系统架构与开发文档/概述.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/系统架构与开发文档/特殊配置与命令.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/knowledge/zh/系统架构与开发文档/编码规范.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/UniApp跨平台商城/AI助手集成.md:1,31,34,38,50,55,60,62,64,67,73,80,84,154,156,162,163,180,181,379,386,387,393,394,557,575,577,580,584,588` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/UniApp跨平台商城/API接口与网络请求.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/UniApp跨平台商城/UI组件库与样式系统.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/UniApp跨平台商城/UniApp框架架构.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/UniApp跨平台商城/UniApp跨平台商城.md:45,46,55,56,69,78,79,87,119,138,141,145,188,221,227,231,239,242,246,277,280,289,303,333,339,353,364,365,541,550` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/UniApp跨平台商城/状态管理(Vuex).md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/UniApp跨平台商城/组件系统开发.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/UniApp跨平台商城/跨平台适配策略.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/UniApp跨平台商城/页面路由系统.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/前端架构设计.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/微信小程序原生开发/API接口与微信能力.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/微信小程序原生开发/微信小程序原生开发.md:49,50,51,52,60,61,72,81,82,83,91,107,111,146,183,186,188,202,211,226,233,278,290,481,558,560,561,564,570,572` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/微信小程序原生开发/性能优化与用户体验.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/微信小程序原生开发/组件系统与自定义组件.md:78` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/微信小程序原生开发/调试工具与发布流程.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/微信小程序原生开发/页面开发与路由.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/微信小程序原生开发/项目结构与配置.md:245` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/管理后台UI架构/Element UI组件库集成.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/管理后台UI架构/Vue2核心架构.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/管理后台UI架构/构建配置与优化.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/管理后台UI架构/管理后台UI架构.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/管理后台UI架构/组件开发规范.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/管理后台UI架构/路由与状态管理.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/后端服务架构/业务API服务 (platform-api).md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/后端服务架构/业务逻辑模块 (platform-biz).md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/后端服务架构/公共模块 (platform-common).md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/后端服务架构/后端服务架构.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/后端服务架构/平台管理服务 (platform-admin).md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/开发指南/代码规范.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/开发指南/开发工具.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/开发指南/开发指南.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/开发指南/开发流程.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/开发指南/测试策略.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/快速开始.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/功能模块扩展/功能模块扩展.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/功能模块扩展/商城业务模块扩展.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/功能模块扩展/微信业务模块扩展.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/功能模块扩展/文件存储模块扩展.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/功能模块扩展/模块集成模式.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/功能模块扩展/系统业务模块扩展.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/性能优化方案/前端性能优化.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/性能优化方案/压力测试方法.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/性能优化方案/后端性能优化.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/性能优化方案/异步处理优化.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/性能优化方案/性能优化方案.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/性能优化方案/数据库性能优化.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/性能优化方案/监控指标体系.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/性能优化方案/缓存策略优化.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/性能优化方案/负载均衡优化.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/扩展开发.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/插件开发模式.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/第三方服务集成/存储服务集成/七牛云存储集成.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/第三方服务集成/存储服务集成/上传工厂模式.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/第三方服务集成/存储服务集成/云存储抽象设计.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/第三方服务集成/存储服务集成/其他云存储服务.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/第三方服务集成/存储服务集成/存储服务集成.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/第三方服务集成/存储服务集成/阿里云OSS集成.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/第三方服务集成/微信服务集成/微信公众号集成.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/第三方服务集成/微信服务集成/微信小程序集成.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/第三方服务集成/微信服务集成/微信支付集成.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/第三方服务集成/微信服务集成/微信服务集成.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/第三方服务集成/微信服务集成/微信模板消息.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/第三方服务集成/微信服务集成/微信用户管理.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/第三方服务集成/支付服务集成/微信支付集成.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/第三方服务集成/支付服务集成/支付安全与风控.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/第三方服务集成/支付服务集成/支付宝服务集成.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/第三方服务集成/支付服务集成/支付服务集成.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/第三方服务集成/监控告警服务.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/第三方服务集成/短信服务集成.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/第三方服务集成/第三方服务集成.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/扩展开发/第三方服务集成/缓存服务集成.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/接口文档/业务API接口/业务API接口.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/接口文档/业务API接口/商品展示接口.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/接口文档/业务API接口/微信集成接口.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/接口文档/业务API接口/文件上传接口.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/接口文档/业务API接口/用户中心接口.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/接口文档/业务API接口/用户认证接口.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/接口文档/业务API接口/订单管理接口.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/接口文档/业务API接口/评论与咨询接口.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/接口文档/业务API接口/购物车接口.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/接口文档/接口契约规范.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/接口文档/接口文档.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/接口文档/管理后台API/代码生成API.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/接口文档/管理后台API/商城管理API.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/接口文档/管理后台API/定时任务API.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/接口文档/管理后台API/对象存储API.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/接口文档/管理后台API/微信管理API.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/接口文档/管理后台API/管理后台API.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/接口文档/管理后台API/系统管理API.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/接口文档/认证授权接口.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/故障排查.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/数据库设计.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/核心业务功能/商城业务模块/商品管理功能.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/核心业务功能/商城业务模块/商城业务模块.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/核心业务功能/商城业务模块/用户管理体系.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/核心业务功能/商城业务模块/营销活动系统.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/核心业务功能/商城业务模块/订单处理流程.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/核心业务功能/微信业务模块/微信业务模块.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/核心业务功能/微信业务模块/微信二维码管理.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/核心业务功能/微信业务模块/微信公众号集成.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/核心业务功能/微信业务模块/微信小程序开发.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/核心业务功能/微信业务模块/微信支付处理.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/核心业务功能/微信业务模块/微信消息推送.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/核心业务功能/文件存储模块.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/核心业务功能/核心业务功能.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/核心业务功能/系统管理模块/数据字典管理.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/核心业务功能/系统管理模块/日志审计管理.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/核心业务功能/系统管理模块/用户权限管理.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/核心业务功能/系统管理模块/系统管理模块.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/核心业务功能/系统管理模块/系统配置管理.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/系统架构设计/技术选型与架构模式.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/系统架构设计/整体架构概览.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/系统架构设计/模块依赖关系.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/系统架构设计/系统架构设计.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/系统架构设计/组件交互与数据流.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/部署与运维/Docker容器化部署.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/部署与运维/备份恢复.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/部署与运维/本地开发部署.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/部署与运维/生产环境部署.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/部署与运维/监控告警.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/部署与运维/部署与运维.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/项目概述/功能特性总览.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/项目概述/技术栈概览.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/项目概述/架构设计理念.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/项目概述/项目概述.md:26,29,30,38,48,73,89,97,98,149,172,173,250,263,266,271,272,274,283,286,287,300,309,311,313,350,351,352,369,378` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/content/项目概述/项目简介与定位.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/repowiki/zh/meta/repowiki-metadata.json:1` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/.qoder/settings.local.json` (基线中已移除文件) | 工具/Agent目录 |
| [vendor/platform-wxshop/Agents.md:3](../vendor/platform-wxshop/Agents.md) | 正文标记, 人工核对 |
| [vendor/platform-wxshop/README.md:24,200](../vendor/platform-wxshop/README.md) | 正文标记, 人工核对 |
| `vendor/platform-wxshop/wx-mall/agent/AGENTS.md:1,21` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/wx-mall/agent/page-meta.json` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/wx-mall/skills/mall-checkout-skill/SKILL.md:8` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/wx-mall/skills/mall-checkout-skill/apis/getCartSnapshot.js` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/wx-mall/skills/mall-checkout-skill/apis/prepareCheckout.js:36` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/wx-mall/skills/mall-checkout-skill/apis/request.js` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/wx-mall/skills/mall-checkout-skill/index.js` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/wx-mall/skills/mall-checkout-skill/mcp.json` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/wx-mall/skills/mall-guide-skill/SKILL.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/wx-mall/skills/mall-guide-skill/apis/getGoodsDetail.js` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/wx-mall/skills/mall-guide-skill/apis/recommendGoods.js` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/wx-mall/skills/mall-guide-skill/apis/request.js` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/wx-mall/skills/mall-guide-skill/apis/searchGoods.js` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/wx-mall/skills/mall-guide-skill/index.js` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/wx-mall/skills/mall-guide-skill/mcp.json` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/wx-mall/skills/mall-order-skill/SKILL.md` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/wx-mall/skills/mall-order-skill/apis/getOrderDetail.js` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/wx-mall/skills/mall-order-skill/apis/listOrders.js` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/wx-mall/skills/mall-order-skill/apis/request.js` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/wx-mall/skills/mall-order-skill/index.js` (基线中已移除文件) | 工具/Agent目录 |
| `vendor/platform-wxshop/wx-mall/skills/mall-order-skill/mcp.json` (基线中已移除文件) | 工具/Agent目录 |

## 9. 外部域名: 全部已检出域名及文件

包含DTD/标准规范、许可证链接、文档引用、第三方插件网络地址、实际运行默认配置与旧SQL图片地址. 不能把每个外部域名都替换为自己的域名. 要优先核对运行默认值和用户数据会发送到的地址. 不访问这些地址下载图片或验证个人信息.

### www.w3.org (116文件)

- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-activiti.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-activiti.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-admin.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-admin.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-brand.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-brand.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-caozuo.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-caozuo.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-cart.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-cart.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-charts.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-charts.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-code.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-code.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-collect.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-collect.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-coupon.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-coupon.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-coupons.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-coupons.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-dangdifill.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-dangdifill.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-daohang.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-daohang.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-databases.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-databases.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-dev.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-dev.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-diamond.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-diamond.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-diaodu.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-diaodu.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-dict.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-dict.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-duanxin.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-duanxin.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-editor.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-editor.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-email.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-email.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-feedback.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-feedback.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-feiliao.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-feiliao.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-fensi.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-fensi.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-files.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-files.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-footprint.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-footprint.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-geren.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-geren.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-gift.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-gift.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-goods.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-goods.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-guize.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-guize.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-history.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-history.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-interface.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-interface.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-issue.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-issue.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-jiesuo.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-jiesuo.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-job.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-job.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-leibie.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-leibie.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-level.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-level.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-live.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-live.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-log.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-log.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-mall.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-mall.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-media.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-media.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-menu.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-menu.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-mp.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-mp.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-myorder.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-myorder.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-orders.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-orders.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-org.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-org.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-oss.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-oss.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-pinglun.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-pinglun.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-print.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-print.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-qrcode.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-qrcode.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-quanxian.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-quanxian.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-redis.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-redis.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-refund.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-refund.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-role.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-role.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-room.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-room.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-saled.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-saled.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-shanchu.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-shanchu.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-shipping.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-shipping.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-shops.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-shops.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-shouye.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-shouye.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-sign.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-sign.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-sousuo.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-sousuo.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-sql.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-sql.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-suoding.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-suoding.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-system.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-system.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-tempmsg.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-tempmsg.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-tixing.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-tixing.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-tubiao.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-tubiao.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-vip.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-vip.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-wechat.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-wechat.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-weibo.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-weibo.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-wuliaoguanli.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-wuliaoguanli.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-wuliaoguanliye.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-wuliaoguanliye.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-xiangqu.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-xiangqu.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-xiangqufill.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-xiangqufill.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-xiaochengxu.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-xiaochengxu.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-xitongpeizhi.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-xitongpeizhi.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-zaixian.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-zaixian.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-zhedie.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-zhedie.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-zhifubao.svg:2,3,4](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-zhifubao.svg)
- [vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-zonghe.svg:1](../vendor/platform-wxshop/platform-admin-ui/src/icons/svg/icon-zonghe.svg)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/anchor/anchor.html:2` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/emotion/emotion.html:2` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/gmap/gmap.html:2` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/help/help.html:2` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/link/link.html:2` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/map/show.html:2,3` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/preview/preview.html:2` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/searchreplace/searchreplace.html:2` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/spechars/spechars.html:2` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/video/video.html:2` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/wordimage/wordimage.html:2` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/highcharts.js:19,64,93` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/highcharts.src.js:41,3688,5708` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/modules/canvas-tools.src.js:861,1394,1613` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/modules/exporting.js:14` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/modules/exporting.src.js:294` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/jquery-1.10.2.js:1078,1080,1084,1085,1088,1138,1610,2148,2232,2279,2296,5395` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/font/vjs.svg:2,3` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.dev.js:616,5950,5962` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/ueditor.all.js:6917` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/ueditor.all.min.js:1` (基线中已移除文件)
- [vendor/platform-wxshop/platform-admin/pom.xml:3](../vendor/platform-wxshop/platform-admin/pom.xml)
- [vendor/platform-wxshop/platform-api/pom.xml:3](../vendor/platform-wxshop/platform-api/pom.xml)
- [vendor/platform-wxshop/platform-biz/pom.xml:3](../vendor/platform-wxshop/platform-biz/pom.xml)
- [vendor/platform-wxshop/platform-common/pom.xml:3](../vendor/platform-wxshop/platform-common/pom.xml)
- [vendor/platform-wxshop/pom.xml:2](../vendor/platform-wxshop/pom.xml)
- [vendor/platform-wxshop/wx-mall/static/images/designer/chain-gold.svg:1](../vendor/platform-wxshop/wx-mall/static/images/designer/chain-gold.svg)
- [vendor/platform-wxshop/wx-mall/static/images/designer/chain-platinum.svg:1](../vendor/platform-wxshop/wx-mall/static/images/designer/chain-platinum.svg)
- [vendor/platform-wxshop/wx-mall/static/images/designer/chain-rose.svg:1](../vendor/platform-wxshop/wx-mall/static/images/designer/chain-rose.svg)
- [vendor/platform-wxshop/wx-mall/static/images/designer/gem-diamond.svg:1](../vendor/platform-wxshop/wx-mall/static/images/designer/gem-diamond.svg)
- [vendor/platform-wxshop/wx-mall/static/images/designer/gem-ruby.svg:1](../vendor/platform-wxshop/wx-mall/static/images/designer/gem-ruby.svg)
- [vendor/platform-wxshop/wx-mall/static/images/designer/pearl-akoya.svg:1](../vendor/platform-wxshop/wx-mall/static/images/designer/pearl-akoya.svg)
- [vendor/platform-wxshop/wx-mall/static/images/designer/pearl-tahiti.svg:1](../vendor/platform-wxshop/wx-mall/static/images/designer/pearl-tahiti.svg)
- [vendor/platform-wxshop/wx-mall/static/images/designer/pearl-white.svg:1](../vendor/platform-wxshop/wx-mall/static/images/designer/pearl-white.svg)
- [vendor/platform-wxshop/wx-mall/static/images/designer/pendant-heart.svg:1](../vendor/platform-wxshop/wx-mall/static/images/designer/pendant-heart.svg)
- [vendor/platform-wxshop/wx-mall/static/images/designer/pendant-solitaire.svg:1](../vendor/platform-wxshop/wx-mall/static/images/designer/pendant-solitaire.svg)

### mybatis.org (61文件)

- [vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Dao.xml.vm:2](../vendor/platform-wxshop/platform-admin/src/main/resources/gen/template/Dao.xml.vm)
- [vendor/platform-wxshop/platform-admin/src/main/resources/mapper/gen/SysGeneratorDao.xml:2](../vendor/platform-wxshop/platform-admin/src/main/resources/mapper/gen/SysGeneratorDao.xml)
- [vendor/platform-wxshop/platform-admin/src/main/resources/mapper/job/ScheduleJobDao.xml:2](../vendor/platform-wxshop/platform-admin/src/main/resources/mapper/job/ScheduleJobDao.xml)
- [vendor/platform-wxshop/platform-admin/src/main/resources/mapper/job/ScheduleJobLogDao.xml:2](../vendor/platform-wxshop/platform-admin/src/main/resources/mapper/job/ScheduleJobLogDao.xml)
- [vendor/platform-wxshop/platform-admin/src/main/resources/mapper/sys/SysDictDao.xml:2](../vendor/platform-wxshop/platform-admin/src/main/resources/mapper/sys/SysDictDao.xml)
- [vendor/platform-wxshop/platform-admin/src/main/resources/mapper/sys/SysDictGroupDao.xml:2](../vendor/platform-wxshop/platform-admin/src/main/resources/mapper/sys/SysDictGroupDao.xml)
- [vendor/platform-wxshop/platform-admin/src/main/resources/mapper/sys/SysLogDao.xml:2](../vendor/platform-wxshop/platform-admin/src/main/resources/mapper/sys/SysLogDao.xml)
- [vendor/platform-wxshop/platform-admin/src/main/resources/mapper/sys/SysMenuDao.xml:2](../vendor/platform-wxshop/platform-admin/src/main/resources/mapper/sys/SysMenuDao.xml)
- [vendor/platform-wxshop/platform-admin/src/main/resources/mapper/sys/SysOrgDao.xml:2](../vendor/platform-wxshop/platform-admin/src/main/resources/mapper/sys/SysOrgDao.xml)
- [vendor/platform-wxshop/platform-admin/src/main/resources/mapper/sys/SysRoleDao.xml:2](../vendor/platform-wxshop/platform-admin/src/main/resources/mapper/sys/SysRoleDao.xml)
- [vendor/platform-wxshop/platform-admin/src/main/resources/mapper/sys/SysRoleMenuDao.xml:2](../vendor/platform-wxshop/platform-admin/src/main/resources/mapper/sys/SysRoleMenuDao.xml)
- [vendor/platform-wxshop/platform-admin/src/main/resources/mapper/sys/SysRoleOrgDao.xml:2](../vendor/platform-wxshop/platform-admin/src/main/resources/mapper/sys/SysRoleOrgDao.xml)
- [vendor/platform-wxshop/platform-admin/src/main/resources/mapper/sys/SysSmsLogDao.xml:2](../vendor/platform-wxshop/platform-admin/src/main/resources/mapper/sys/SysSmsLogDao.xml)
- [vendor/platform-wxshop/platform-admin/src/main/resources/mapper/sys/SysUserDao.xml:2](../vendor/platform-wxshop/platform-admin/src/main/resources/mapper/sys/SysUserDao.xml)
- [vendor/platform-wxshop/platform-admin/src/main/resources/mapper/sys/SysUserRoleDao.xml:2](../vendor/platform-wxshop/platform-admin/src/main/resources/mapper/sys/SysUserRoleDao.xml)
- [vendor/platform-wxshop/platform-admin/src/main/resources/mapper/sys/SysUserTokenDao.xml:2](../vendor/platform-wxshop/platform-admin/src/main/resources/mapper/sys/SysUserTokenDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/common/SafeOrderByMapper.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/common/SafeOrderByMapper.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallAdDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallAdDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallAdPositionDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallAdPositionDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallAddressDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallAddressDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallAttributeCategoryDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallAttributeCategoryDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallAttributeDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallAttributeDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallBrandDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallBrandDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallCartDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallCartDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallCategoryDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallCategoryDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallChannelDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallChannelDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallCollectDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallCollectDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallCommentDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallCommentDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallCommentPictureDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallCommentPictureDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallCouponDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallCouponDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallCouponGoodsDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallCouponGoodsDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallFeedbackDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallFeedbackDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallFootprintDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallFootprintDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallGoodsAttributeDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallGoodsAttributeDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallGoodsDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallGoodsDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallGoodsGalleryDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallGoodsGalleryDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallGoodsIssueDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallGoodsIssueDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallGoodsSpecificationDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallGoodsSpecificationDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallHelpIssueDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallHelpIssueDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallHelpTypeDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallHelpTypeDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallKeywordsDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallKeywordsDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallOrderDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallOrderDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallOrderGoodsDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallOrderGoodsDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallProductDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallProductDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallRelatedGoodsDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallRelatedGoodsDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallSearchHistoryDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallSearchHistoryDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallShippingDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallShippingDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallSpecificationDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallSpecificationDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallTopicCategoryDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallTopicCategoryDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallTopicDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallTopicDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallUserCouponDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallUserCouponDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallUserLevelDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/mall/MallUserLevelDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/oss/SysOssDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/oss/SysOssDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/sys/SysConfigDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/sys/SysConfigDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/sys/SysRegionDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/sys/SysRegionDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/wx/MsgReplyRuleDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/wx/MsgReplyRuleDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/wx/MsgTemplateDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/wx/MsgTemplateDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/wx/TemplateMsgLogDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/wx/TemplateMsgLogDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/wx/WxMsgDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/wx/WxMsgDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/wx/WxQrCodeDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/wx/WxQrCodeDao.xml)
- [vendor/platform-wxshop/platform-biz/src/main/resources/mapper/wx/WxUserDao.xml:2](../vendor/platform-wxshop/platform-biz/src/main/resources/mapper/wx/WxUserDao.xml)

### github.com (48文件)

- [use.md:126](../use.md)
- [vendor/platform-wxshop/README.md:45](../vendor/platform-wxshop/README.md)
- [vendor/platform-wxshop/platform-admin-ui/.eslintrc.js:17](../vendor/platform-wxshop/platform-admin-ui/.eslintrc.js)
- [vendor/platform-wxshop/platform-admin-ui/.postcssrc.js:1](../vendor/platform-wxshop/platform-admin-ui/.postcssrc.js)
- [vendor/platform-wxshop/platform-admin-ui/README.md:5,7,8,9,10,11,66,67](../vendor/platform-wxshop/platform-admin-ui/README.md)
- [vendor/platform-wxshop/platform-admin-ui/build/webpack.dev.conf.js:55](../vendor/platform-wxshop/platform-admin-ui/build/webpack.dev.conf.js)
- [vendor/platform-wxshop/platform-admin-ui/build/webpack.prod.conf.js:48,60](../vendor/platform-wxshop/platform-admin-ui/build/webpack.prod.conf.js)
- [vendor/platform-wxshop/platform-admin-ui/build/webpack.test.conf.js:18](../vendor/platform-wxshop/platform-admin-ui/build/webpack.test.conf.js)
- [vendor/platform-wxshop/platform-admin-ui/config/index.js:55](../vendor/platform-wxshop/platform-admin-ui/config/index.js)
- [vendor/platform-wxshop/platform-admin-ui/package.md:4,6,8,10,12,14,16,18,20,24,26,28](../vendor/platform-wxshop/platform-admin-ui/package.md)
- [vendor/platform-wxshop/platform-admin-ui/pnpm-lock.yaml:1480,4916,5233,5243,5720](../vendor/platform-wxshop/platform-admin-ui/pnpm-lock.yaml)
- [vendor/platform-wxshop/platform-admin-ui/src/assets/scss/index.scss:2](../vendor/platform-wxshop/platform-admin-ui/src/assets/scss/index.scss)
- [vendor/platform-wxshop/platform-admin-ui/src/element-ui/index.js:2](../vendor/platform-wxshop/platform-admin-ui/src/element-ui/index.js)
- [vendor/platform-wxshop/platform-admin-ui/src/main.js:3,5,7,9,15](../vendor/platform-wxshop/platform-admin-ui/src/main.js)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/jquery-1.10.2.js:9729` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video-js.css:766` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.dev.js:5304,5358,6968` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.js:1` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.custom.js:5,13,171,3979,4543` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.flashonly.js:5,13,171` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.html5only.js:5,13,171,4653,5217` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.js:5,13,171,4653,6019` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.withoutimage.js:5,13,171` (基线中已移除文件)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxAssetsManageController.java:39](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxAssetsManageController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMenuManageController.java:37](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMenuManageController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxQrCodeManageController.java:40](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxQrCodeManageController.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/builder/AbstractBuilder.java:30](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/builder/AbstractBuilder.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/builder/ImageBuilder.java:29](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/builder/ImageBuilder.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/builder/TextBuilder.java:29](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/builder/TextBuilder.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/AbstractHandler.java:28](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/AbstractHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/KfSessionHandler.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/KfSessionHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/LocationHandler.java:33](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/LocationHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/LogHandler.java:36](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/LogHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/MenuHandler.java:36](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/MenuHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/MsgHandler.java:37](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/MsgHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/NullHandler.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/NullHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/ScanHandler.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/ScanHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/StoreCheckNotifyHandler.java:32](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/StoreCheckNotifyHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/SubscribeHandler.java:36](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/SubscribeHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/UnsubscribeHandler.java:34](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/handler/UnsubscribeHandler.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/MsgReplyService.java:26](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/MsgReplyService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/MsgReplyServiceImpl.java:44](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/MsgReplyServiceImpl.java)
- `vendor/platform-wxshop/wx-mall/lib/wxParse/html2json.js:6` (基线中已移除文件)
- `vendor/platform-wxshop/wx-mall/lib/wxParse/htmlparser.js:6` (基线中已移除文件)
- `vendor/platform-wxshop/wx-mall/lib/wxParse/showdown.js:6` (基线中已移除文件)
- `vendor/platform-wxshop/wx-mall/lib/wxParse/wxParse.js:6` (基线中已移除文件)
- `vendor/platform-wxshop/wx-mall/lib/wxParse/wxParse.wxml:7` (基线中已移除文件)
- `vendor/platform-wxshop/wx-mall/lib/wxParse/wxParse.wxss:7` (基线中已移除文件)

### localhost (23文件)

- [.stop-local-test-54889eafe7d947489ff71fad670a3b1f.ps1:64,65,66,67](../.stop-local-test-54889eafe7d947489ff71fad670a3b1f.ps1)
- [README.md:98,99,163,164,165](../README.md)
- [dev.ps1:64,65,66,67](../dev.ps1)
- [dev.sh:61,62,63,64](../dev.sh)
- [start-local-test.cmd:18](../start-local-test.cmd)
- [use.md:76,222,226,232,233,263](../use.md)
- `vendor/platform-wxshop/.qoder/repowiki/zh/content/快速开始.md:222,223,253,254,255` (基线中已移除文件)
- `vendor/platform-wxshop/.qoder/repowiki/zh/content/接口文档/管理后台API/管理后台API.md:338,341` (基线中已移除文件)
- `vendor/platform-wxshop/.qoder/repowiki/zh/content/系统架构设计/整体架构概览.md:311,312` (基线中已移除文件)
- `vendor/platform-wxshop/.qoder/repowiki/zh/content/部署与运维/Docker容器化部署.md:308,309,310` (基线中已移除文件)
- `vendor/platform-wxshop/.qoder/repowiki/zh/content/部署与运维/本地开发部署.md:231,340` (基线中已移除文件)
- `vendor/platform-wxshop/.qoder/repowiki/zh/content/部署与运维/部署与运维.md:160,161,162` (基线中已移除文件)
- [vendor/platform-wxshop/Agents.md:36,38,41](../vendor/platform-wxshop/Agents.md)
- [vendor/platform-wxshop/README.md:94,132,133,134](../vendor/platform-wxshop/README.md)
- [vendor/platform-wxshop/_sql/base.sql:332,466,467,468](../vendor/platform-wxshop/_sql/base.sql)
- [vendor/platform-wxshop/_sql/migrations/20260901_migrate_catalog_images_to_webp.sql:54](../vendor/platform-wxshop/_sql/migrations/20260901_migrate_catalog_images_to_webp.sql)
- [vendor/platform-wxshop/deploy/.env.example:19](../vendor/platform-wxshop/deploy/.env.example)
- [vendor/platform-wxshop/docs/系统架构说明.md:179,181](../vendor/platform-wxshop/docs/%E7%B3%BB%E7%BB%9F%E6%9E%B6%E6%9E%84%E8%AF%B4%E6%98%8E.md)
- [vendor/platform-wxshop/platform-admin-ui/README.md:60](../vendor/platform-wxshop/platform-admin-ui/README.md)
- [vendor/platform-wxshop/platform-admin-ui/static/config/index.js:8](../vendor/platform-wxshop/platform-admin-ui/static/config/index.js)
- [vendor/platform-wxshop/platform-admin/src/main/resources/application.yml:203](../vendor/platform-wxshop/platform-admin/src/main/resources/application.yml)
- [vendor/platform-wxshop/platform-api/src/main/resources/application.yml:185](../vendor/platform-wxshop/platform-api/src/main/resources/application.yml)
- [vendor/platform-wxshop/wx-mall/config/api.js:2](../vendor/platform-wxshop/wx-mall/config/api.js)

### gitee.com (9文件)

- [vendor/platform-wxshop/README.md:2,3,4,5,44](../vendor/platform-wxshop/README.md)
- [vendor/platform-wxshop/platform-admin-ui/package.json:7](../vendor/platform-wxshop/platform-admin-ui/package.json)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/SwaggerConfig.java:74](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/SwaggerConfig.java)
- [vendor/platform-wxshop/platform-admin/src/main/resources/application.yml:67](../vendor/platform-wxshop/platform-admin/src/main/resources/application.yml)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/SwaggerConfig.java:76](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/SwaggerConfig.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppCartController.java:31](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppCartController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppPayController.java:45](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppPayController.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppTestController.java:21](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppTestController.java)
- [vendor/platform-wxshop/platform-api/src/main/resources/application.yml:56](../vendor/platform-wxshop/platform-api/src/main/resources/application.yml)

### developers.weixin.qq.com (9文件)

- [vendor/platform-wxshop/platform-admin-ui/src/views/modules/wx/wx-qrcode-add-or-update.vue:10](../vendor/platform-wxshop/platform-admin-ui/src/views/modules/wx/wx-qrcode-add-or-update.vue)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxAssetsManageController.java:38](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxAssetsManageController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMenuManageController.java:36](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMenuManageController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMpDraftController.java:57,84,138,159,179](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMpDraftController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMpFreePublishController.java:52,79,103,123](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMpFreePublishController.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/MsgReplyService.java:25](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/MsgReplyService.java)
- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/MsgReplyServiceImpl.java:43](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/wx/service/impl/MsgReplyServiceImpl.java)
- [vendor/platform-wxshop/wx-mall/project.private.config.json:22](../vendor/platform-wxshop/wx-mall/project.private.config.json)
- [vendor/platform-wxshop/wx-mall/sitemap.json:2](../vendor/platform-wxshop/wx-mall/sitemap.json)

### openwtai.com (8文件)

- `vendor/platform-wxshop/.qoder/repowiki/zh/content/前端架构设计/UniApp跨平台商城/API接口与网络请求.md:44,231,410,421,431` (基线中已移除文件)
- `vendor/platform-wxshop/.qoder/repowiki/zh/content/快速开始.md:198` (基线中已移除文件)
- [vendor/platform-wxshop/README.md:28,35,41](../vendor/platform-wxshop/README.md)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/SwaggerConfig.java:73](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/config/SwaggerConfig.java)
- [vendor/platform-wxshop/platform-admin/src/main/resources/application.yml:165](../vendor/platform-wxshop/platform-admin/src/main/resources/application.yml)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/SwaggerConfig.java:75](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/config/SwaggerConfig.java)
- [vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppLoginController.java:299](../vendor/platform-wxshop/platform-api/src/main/java/com/platform/modules/app/controller/AppLoginController.java)
- [vendor/platform-wxshop/platform-api/src/main/resources/application.yml:153](../vendor/platform-wxshop/platform-api/src/main/resources/application.yml)

### code.google.com (7文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/modules/canvas-tools.js:10` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/modules/canvas-tools.src.js:294,3093` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.dev.js:3899` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.custom.js:5540` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.html5only.js:5412` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.js:6214` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.withoutimage.js:4106` (基线中已移除文件)

### jsperf.com (6文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/jquery-1.10.2.js:6498` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.custom.js:192` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.flashonly.js:192` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.html5only.js:192` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.js:192` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.withoutimage.js:192` (基线中已移除文件)

### weappdev.com (6文件)

- `vendor/platform-wxshop/wx-mall/lib/wxParse/html2json.js:3,9` (基线中已移除文件)
- `vendor/platform-wxshop/wx-mall/lib/wxParse/htmlparser.js:3,9` (基线中已移除文件)
- `vendor/platform-wxshop/wx-mall/lib/wxParse/showdown.js:3,9` (基线中已移除文件)
- `vendor/platform-wxshop/wx-mall/lib/wxParse/wxParse.js:3,9` (基线中已移除文件)
- `vendor/platform-wxshop/wx-mall/lib/wxParse/wxParse.wxml:4,10` (基线中已移除文件)
- `vendor/platform-wxshop/wx-mall/lib/wxParse/wxParse.wxss:4,10` (基线中已移除文件)

### developer.mozilla.org (5文件)

- [vendor/platform-wxshop/platform-admin-ui/pnpm-lock.yaml:5303](../vendor/platform-wxshop/platform-admin-ui/pnpm-lock.yaml)
- `vendor/platform-wxshop/platform-admin-ui/src/components/ueditor/index.vue:38,82` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/src/components/ueditor/readme.md:37,39` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/jquery-1.10.2.js:3448,6799` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.dev.js:648` (基线中已移除文件)

### www.macromedia.com (5文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/music/music.js:106` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/video/video.js:276` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/wordimage/tangram.js:399` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/ueditor.all.js:17660,23662` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/ueditor.all.min.js:1` (基线中已移除文件)

### www.2ality.com (5文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.custom.js:416` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.flashonly.js:416` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.html5only.js:416` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.js:416` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.withoutimage.js:416` (基线中已移除文件)

### t.cn (5文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.custom.js:2051` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.flashonly.js:2051` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.html5only.js:2245` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.js:2245` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.withoutimage.js:1879` (基线中已移除文件)

### bs.baidu.com (5文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/ueditor.all.js:14976` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/ueditor.all.min.js:1` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/ueditor.config.js:177` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/ueditor.parse.js:918` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/ueditor.parse.min.js:7` (基线中已移除文件)

### maven.apache.org (5文件)

- [vendor/platform-wxshop/platform-admin/pom.xml:3,4](../vendor/platform-wxshop/platform-admin/pom.xml)
- [vendor/platform-wxshop/platform-api/pom.xml:3,4](../vendor/platform-wxshop/platform-api/pom.xml)
- [vendor/platform-wxshop/platform-biz/pom.xml:3,4](../vendor/platform-wxshop/platform-biz/pom.xml)
- [vendor/platform-wxshop/platform-common/pom.xml:3,4](../vendor/platform-wxshop/platform-common/pom.xml)
- [vendor/platform-wxshop/pom.xml:2,3](../vendor/platform-wxshop/pom.xml)

### example.com (4文件)

- `vendor/platform-wxshop/.qoder/repowiki/zh/content/接口文档/业务API接口/商品展示接口.md:196,255,260` (基线中已移除文件)
- `vendor/platform-wxshop/.qoder/repowiki/zh/content/接口文档/业务API接口/评论与咨询接口.md:206` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.dev.js:3765` (基线中已移除文件)
- `vendor/platform-wxshop/wx-mall/lib/wxParse/showdown.js:2286` (基线中已移除文件)

### api.map.baidu.com (4文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/map/map.html:7,125` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/map/show.html:17` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/ueditor.all.js:28955` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/ueditor.all.min.js:1` (基线中已移除文件)

### stackoverflow.com (4文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.dev.js:1232,4943,5457` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.custom.js:4592` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.html5only.js:5266` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/webuploader.js:6068` (基线中已移除文件)

### app.baidu.com (3文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/webapp/webapp.html:37,45,48` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/ueditor.all.js:23449,23569` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/ueditor.config.js:358` (基线中已移除文件)

### www.highcharts.com (3文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/highcharts.js:35` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/highcharts.src.js:1790` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/themes/skies.js:12` (基线中已移除文件)

### jsfiddle.net (3文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/highcharts.src.js:4219` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/modules/data.src.js:15` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/modules/drilldown.src.js:8` (基线中已移除文件)

### platform-wxmall.oss-cn-beijing.aliyuncs.com (2文件)

- [vendor/platform-wxshop/_sql/mall.sql:28,32,36,977,980,983,986,989](../vendor/platform-wxshop/_sql/mall.sql)
- [vendor/platform-wxshop/_sql/migrations/20260901_migrate_catalog_images_to_webp.sql:63](../vendor/platform-wxshop/_sql/migrations/20260901_migrate_catalog_images_to_webp.sql)

### eslint.org (2文件)

- [vendor/platform-wxshop/platform-admin-ui/.eslintrc.js:1](../vendor/platform-wxshop/platform-admin-ui/.eslintrc.js)
- [vendor/platform-wxshop/platform-admin-ui/pnpm-lock.yaml:2271](../vendor/platform-wxshop/platform-admin-ui/pnpm-lock.yaml)

### www.iconfont.cn (2文件)

- [vendor/platform-wxshop/platform-admin-ui/README.md:12,112](../vendor/platform-wxshop/platform-admin-ui/README.md)
- [vendor/platform-wxshop/platform-admin-ui/src/main.js:11](../vendor/platform-wxshop/platform-admin-ui/src/main.js)

### vue-loader.vuejs.org (2文件)

- [vendor/platform-wxshop/platform-admin-ui/build/utils.js:54](../vendor/platform-wxshop/platform-admin-ui/build/utils.js)
- [vendor/platform-wxshop/platform-admin-ui/config/index.js:50](../vendor/platform-wxshop/platform-admin-ui/config/index.js)

### ueditor.baidu.com (2文件)

- `vendor/platform-wxshop/platform-admin-ui/src/components/ueditor/index.vue:39` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/src/components/ueditor/readme.md:51,57` (基线中已移除文件)

### img.baidu.com (2文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/emotion/emotion.js:6` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/template/config.js:33,34` (基线中已移除文件)

### code.highcharts.com (2文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/highcharts.js:30` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/highcharts.src.js:1511,1512` (基线中已移除文件)

### www.phpied.com (2文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/modules/canvas-tools.js:4,12` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/modules/canvas-tools.src.js:4,296` (基线中已移除文件)

### spreadsheets.google.com (2文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/modules/data.js:12` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/modules/data.src.js:50,266` (基线中已移除文件)

### export.highcharts.com (2文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/modules/exporting.js:10` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/modules/exporting.src.js:99` (基线中已移除文件)

### dev.w3.org (2文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/jquery-1.10.2.js:7105` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.dev.js:3368,3712,3718` (基线中已移除文件)

### bugzilla.mozilla.org (2文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/jquery-1.10.2.js:7231,7232` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.dev.js:2983,5448` (基线中已移除文件)

### bugs.webkit.org (2文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/jquery-1.10.2.js:7394` (基线中已移除文件)
- `vendor/platform-wxshop/wx-mall/lib/wxParse/showdown.js:2137` (基线中已移除文件)

### videojs.com (2文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video-js.css:2` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video-js.min.css:2` (基线中已移除文件)

### designer.videojs.com (2文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video-js.css:4,362` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video-js.min.css:4` (基线中已移除文件)

### bit.ly (2文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.dev.js:104` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.js:2` (基线中已移除文件)

### adobe.ly (2文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.dev.js:105` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.js:2` (基线中已移除文件)

### zeroclipboard.org (2文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/zeroclipboard/ZeroClipboard.js:6` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/zeroclipboard/ZeroClipboard.min.js:6` (基线中已移除文件)

### maps.google.com (2文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/ueditor.all.js:28961` (基线中已移除文件)
- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/ueditor.all.min.js:1` (基线中已移除文件)

### api.weixin.qq.com (2文件)

- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMpDraftController.java:56,83,137,158,178](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMpDraftController.java)
- [vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMpFreePublishController.java:51,78,102,122](../vendor/platform-wxshop/platform-admin/src/main/java/com/platform/modules/wx/controller/WxMpFreePublishController.java)

### host.docker.internal (1文件)

- [use.md:51](../use.md)

### dev.mysql.com (1文件)

- [use.md:98](../use.md)

### npmmirror.com (1文件)

- [use.md:134](../use.md)

### www.java.com (1文件)

- [vendor/platform-wxshop/.gitignore:25](../vendor/platform-wxshop/.gitignore)

### www.apache.org (1文件)

- [vendor/platform-wxshop/LICENSE:3,195](../vendor/platform-wxshop/LICENSE)

### img.shields.io (1文件)

- [vendor/platform-wxshop/README.md:2,5,6,9,10,11,12,13,14](../vendor/platform-wxshop/README.md)

### makeapullrequest.com (1文件)

- [vendor/platform-wxshop/README.md:6](../vendor/platform-wxshop/README.md)

### doc.openwtai.com (1文件)

- [vendor/platform-wxshop/README.md:42,43](../vendor/platform-wxshop/README.md)

### web.cr6868.com (1文件)

- [vendor/platform-wxshop/_sql/base.sql:333](../vendor/platform-wxshop/_sql/base.sql)

### yanxuan.nosdn.127.net (1文件)

- [vendor/platform-wxshop/_sql/mall.sql:247,249,250,251,253,255,256,258,260,261,263,265,266,268,270,271,273,275,276,278,280,281,283,285,286,288,290,291,293,295,296,298,300,301,303,305,306,308,310,311,312,314,316,317,319,321,322,324,326,327,329,331,332,334,336,337,339,341,342,344,346,347,349,351,352,354,356,357,359,361,362,364,366,367,369,371,372,374,376,377,379,381,382,384,386,387,389,391,392,393,395,397,398,400,402,403,405,407,408,410,412,413,415,417,418,420,422,423,424,426,428,429,431,433,434,436,438,439,441,443,444,446,448,449,451,453,454,456,458,459,461,463,464,466,468,469,471,473,474,476,478,479,481,483,484,486,488,489,491,493,494,527,561,562,564,567,568,570,573,574,576,579,580,583,584,587,588,591,592,595,596,600,601,605,606,609,610,613,614,616,619,620,623,624,627,628,631,632,635,636,639,640,643,644,647,648,651,652,655,656,659,660,663,664,667,668,671,672,675,676,679,680,683,684,687,688,691,692,695,696,698,701,702,705,706,709,710,713,714,717,718,720,723,724,727,728,731,732,735,736,739,740,742,745,746,749,750,753,754,757,758,761,762,764,767,768,771,772,775,776,779,780,783,784,787,788,791,792,795,796,799,800,803,804,806,809,810,813,814,817,818,821,822,825,826,829,830,833,834,837,838,841,842,845,846,849,850,853,854,857,858,861,862,865,866,869,870,874,875,878,879,883,884,887,888,892,893,896,897,900,901,904,905,908,909,912,913,916,917,920,921,924,925,928,929,932,933,937,938,941,942,945,946,949,950,953,954,1219,1221,1222,1226,1228,1229,1233,1235,1236,1240,1242,1243,1247,1249,1250,1255,1256,1260,1262,1263,1267,1269,1270,1274,1276,1277,1281,1283,1284,1289,1290,1294,1296,1297,1301,1303,1304,1308,1310,1311,1315,1317,1318,1322,1324,1325,1329,1331,1332,1336,1338,1339,1343,1345,1346,1350,1352,1353,1358,1359,1364,1365,1369,1371,1372,1376,1378,1379,1383,1385,1386,1390,1392,1393,1398,1399,1403,1405,1406,1410,1412,1413,1417,1419,1420,1425,1426,1430,1432,1433,1437,1439,1440,1444,1446,1447,1451,1453,1454,1458,1460,1461,1465,1467,1468,1472,1474,1475,1479,1481,1482,1487,1488,1492,1494,1495,1499,1501,1502,1506,1508,1509,1513,1515,1516,1520,1522,1523,1527,1529,1530,1535,1536,1540,1542,1543,1548,1549,1553,1555,1556,1560,1562,1563,1568,1569,1574,1575,1580,1581,1585,1587,1588,1592,1594,1595,1600,1601,1606,1607,1612,1613,1618,1619,1623,1625,1626,1630,1632,1633,1638,1639,1644,1645,1649,1651,1652,1656,1658,1659,1663,1665,1666,1670,1672,1673,1677,1679,1680,1684,1686,1687,1691,1693,1694,1698,1700,1701,1705,1707,1708,1712,1714,1715,1719,1721,1722,1726,1728,1729,1733,1735,1736,1740,1742,1743,1748,1749,1753,1755,1756,1760,1762,1763,1767,1769,1770,1774,1776,1777,1781,1783,1784,1789,1790,1795,1796,1801,1802,1806,1808,1809,1813,1815,1816,1820,1822,1823,1827,1829,1830,1834,1836,1837,1841,1843,1844,1848,1850,1851,1855,1857,1858,1863,1864,1868,1870,1871,1875,1877,1878,1882,1884,1885,1889,1891,1892,1896,1898,1899,1904,1905,1909,1911,1912,1917,1918,1923,1924,1928,1930,1931,1936,1937,1942,1943,1947,1949,1950,1954,1956,1957,1961,1963,1964,1968,1970,1971,1975,1977,1978,1982,1984,1985,1989,1991,1992,1996,1998,1999,2003,2005,2006,2010,2012,2013,2017,2019,2020,2024,2026,2027,2031,2033,2034,2038,2040,2041,2045,2047,2048,2052,2054,2055,2059,2061,2062,2066,2068,2069,2073,2075,2076,2080,2082,2083,2087,2089,2090,2094,2096,2097,2101,2103,2104,2108,2110,2111,2115,2117,2118,2122,2124,2125,2129,2131,2132,2136,2138,2139,2143,2145,2146,2150,2152,2153,2157,2159,2160,2164,2166,2167,2171,2173,2174,2178,2180,2181,2185,2187,2188,2192,2194,2195,2199,2201,2202,2206,2208,2209,2213,2215,2216,2221,2222,2227,2228,2233,2234,2239,2240,2245,2246,2251,2252,2256,2258,2259,2263,2265,2266,2270,2272,2273,2277,2279,2280,2285,2286,2291,2292,2297,2298,2303,2304,2308,2310,2311,2315,2317,2318,2322,2324,2325,2329,2331,2332,2337,2338,2343,2344,2348,2350,2351,2355,2357,2358,2363,2364,2368,2370,2371,2376,2377,2382,2383,2387,2389,2390,2394,2396,2397,2402,2403,2407,2409,2410,2414,2416,2417,2422,2423,2428,2429,2434,2435,2440,2441,2445,2447,2448,2452,2454,2455,2459,2461,2462,2466,2468,2469,2473,2475,2476,2481,2483,2484,2488,2490,2491,2495,2497,2498,2502,2504,2505,2509,2511,2512,2516,2518,2519,2524,2525,2529,2531,2532,2536,2538,2539,2543,2545,2546,2550,2552,2553,2557,2559,2560,2564,2566,2567,2571,2573,2574,2578,2580,2581,2585,2587,2588,2592,2594,2595,2599,2601,2602,2606,2608,2609,2613,2615,2616,2621,2622,2627,2628,2633,2634,2638,2640,2641,2645,2647,2648,2653,2654,2658,2660,2661,2666,2667,2672,2673,2678,2679,2684,2685,2689,2691,2692,2696,2698,2699,2703,2705,2706,2710,2712,2713,2717,2719,2720,2724,2726,2727,2732,2733,2738,2739,2744,2745,2750,2751,2756,2757,2762,2763,2768,2769,2774,2775,2779,2781,2782,2787,2788,2792,2794,2795,2800,2801,2806,2807,2812,2813,2817,2820,7461,7464,7467,7585,7588,7591,7594,7597,7600,7603,7606,7609,7612,7615,8468,8469,8474,8475,8480,8482,8486,8488,8492,8493,8498,8500,8504,8505,8510,8512,8516,8517,8521,8523,8527,8528,8532,8533,8538,8540,8544,8546,8550,8552,8556,8558,8562,8563,8567,8569,8573,8575,8579,8580,8600,8602,8604,8606](../vendor/platform-wxshop/_sql/mall.sql)

### mp123.oss-cn-shenzhen.aliyuncs.com (1文件)

- [vendor/platform-wxshop/_sql/mall.sql:2819,8608](../vendor/platform-wxshop/_sql/mall.sql)

### platform-admin (1文件)

- [vendor/platform-wxshop/deploy/nginx/default.conf:28](../vendor/platform-wxshop/deploy/nginx/default.conf)

### platform-api (1文件)

- [vendor/platform-wxshop/deploy/nginx/default.conf:36](../vendor/platform-wxshop/deploy/nginx/default.conf)

### 127.0.0.1 (1文件)

- [vendor/platform-wxshop/docker-compose.yml:144](../vendor/platform-wxshop/docker-compose.yml)

### nodejs.org (1文件)

- [vendor/platform-wxshop/platform-admin-ui/README.md:3](../vendor/platform-wxshop/platform-admin-ui/README.md)

### es6.ruanyifeng.com (1文件)

- [vendor/platform-wxshop/platform-admin-ui/README.md:4](../vendor/platform-wxshop/platform-admin-ui/README.md)

### cn.vuejs.org (1文件)

- [vendor/platform-wxshop/platform-admin-ui/README.md:6,109](../vendor/platform-wxshop/platform-admin-ui/README.md)

### registry.npm.taobao.org (1文件)

- [vendor/platform-wxshop/platform-admin-ui/README.md:53](../vendor/platform-wxshop/platform-admin-ui/README.md)

### elementui.github.io (1文件)

- [vendor/platform-wxshop/platform-admin-ui/README.md:70](../vendor/platform-wxshop/platform-admin-ui/README.md)

### element-cn.eleme.io (1文件)

- [vendor/platform-wxshop/platform-admin-ui/README.md:110](../vendor/platform-wxshop/platform-admin-ui/README.md)

### www.echartsjs.com (1文件)

- [vendor/platform-wxshop/platform-admin-ui/README.md:111](../vendor/platform-wxshop/platform-admin-ui/README.md)

### vuejs.github.io (1文件)

- [vendor/platform-wxshop/platform-admin-ui/build/webpack.prod.conf.js:36](../vendor/platform-wxshop/platform-admin-ui/build/webpack.prod.conf.js)

### vuejs-templates.github.io (1文件)

- [vendor/platform-wxshop/platform-admin-ui/config/index.js:3](../vendor/platform-wxshop/platform-admin-ui/config/index.js)

### 47.93.215.16 (1文件)

- [vendor/platform-wxshop/platform-admin-ui/config/index.js:17](../vendor/platform-wxshop/platform-admin-ui/config/index.js)

### webpack.js.org (1文件)

- [vendor/platform-wxshop/platform-admin-ui/config/index.js:31,45,75](../vendor/platform-wxshop/platform-admin-ui/config/index.js)

### forwardemail.net (1文件)

- [vendor/platform-wxshop/platform-admin-ui/pnpm-lock.yaml:1480](../vendor/platform-wxshop/platform-admin-ui/pnpm-lock.yaml)

### www.npmjs.com (1文件)

- [vendor/platform-wxshop/platform-admin-ui/pnpm-lock.yaml:2564,6042](../vendor/platform-wxshop/platform-admin-ui/pnpm-lock.yaml)

### socket.dev (1文件)

- [vendor/platform-wxshop/platform-admin-ui/pnpm-lock.yaml:3595](../vendor/platform-wxshop/platform-admin-ui/pnpm-lock.yaml)

### v2.vuejs.org (1文件)

- [vendor/platform-wxshop/platform-admin-ui/pnpm-lock.yaml:5868](../vendor/platform-wxshop/platform-admin-ui/pnpm-lock.yaml)

### vuejs.org (1文件)

- `vendor/platform-wxshop/platform-admin-ui/src/components/ueditor/index.vue:148` (基线中已移除文件)

### www.beian.miit.gov.cn (1文件)

- [vendor/platform-wxshop/platform-admin-ui/src/views/common/login.vue:41](../vendor/platform-wxshop/platform-admin-ui/src/views/common/login.vue)

### kf.qq.com (1文件)

- [vendor/platform-wxshop/platform-admin-ui/src/views/modules/wx/msg-template.vue:25](../vendor/platform-wxshop/platform-admin-ui/src/views/modules/wx/msg-template.vue)

### mpkf.weixin.qq.com (1文件)

- [vendor/platform-wxshop/platform-admin-ui/src/views/modules/wx/wx-msg.vue:22](../vendor/platform-wxshop/platform-admin-ui/src/views/modules/wx/wx-msg.vue)

### mp.weixin.qq.com (1文件)

- [vendor/platform-wxshop/platform-admin-ui/src/views/modules/wx/wx-qrcode.vue:25](../vendor/platform-wxshop/platform-admin-ui/src/views/modules/wx/wx-qrcode.vue)

### maps.googleapis.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/gmap/gmap.html:14,62,71` (基线中已移除文件)

### image.baidu.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/image/image.js:1061` (基线中已移除文件)

### static.tieba.baidu.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/music/music.css:26,27,29` (基线中已移除文件)

### tingapi.ting.baidu.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/music/music.js:11` (基线中已移除文件)

### box.baidu.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/music/music.js:12` (基线中已移除文件)

### fpdownload.macromedia.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/wordimage/tangram.js:345` (基线中已移除文件)

### xregexp.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/SyntaxHighlighter/shCore.js:4` (基线中已移除文件)

### xregexp (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/SyntaxHighlighter/shCore.js:224` (基线中已移除文件)

### google.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/SyntaxHighlighter/shCore.js:1705,1706` (基线中已移除文件)

### joseprado.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/SyntaxHighlighter/shCore.js:1987` (基线中已移除文件)

### iatoria.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/SyntaxHighlighter/shCore.js:2390` (基线中已移除文件)

### davidchambersdesign.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/SyntaxHighlighter/shCore.js:2435` (基线中已移除文件)

### www.jensbits.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/SyntaxHighlighter/shCore.js:2537` (基线中已移除文件)

### jldupont.blogspot.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/SyntaxHighlighter/shCore.js:2890` (基线中已移除文件)

### erlang.org (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/SyntaxHighlighter/shCore.js:2892` (基线中已移除文件)

### jroller.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/SyntaxHighlighter/shCore.js:2926` (基线中已移除文件)

### patrickwebster.blogspot.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/SyntaxHighlighter/shCore.js:3018` (基线中已移除文件)

### poshcode.org (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/SyntaxHighlighter/shCore.js:3261` (基线中已移除文件)

### huddledmasses.org (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/SyntaxHighlighter/shCore.js:3261` (基线中已移除文件)

### kevin.vanzonneveld.net (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/highcharts.src.js:389` (基线中已移除文件)

### www.php.net (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/highcharts.src.js:432` (基线中已移除文件)

### forum.jquery.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/highcharts.src.js:1320` (基线中已移除文件)

### highcharts.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/highcharts.src.js:4630` (基线中已移除文件)

### blog.hackers-cafe.net (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/modules/canvas-tools.src.js:710` (基线中已移除文件)

### developers.google.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/highcharts/modules/data.src.js:45` (基线中已移除文件)

### jquery.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/jquery-1.10.2.js:3` (基线中已移除文件)

### sizzlejs.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/jquery-1.10.2.js:6,1004` (基线中已移除文件)

### jquery.org (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/jquery-1.10.2.js:10,1008` (基线中已移除文件)

### json.org (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/jquery-1.10.2.js:564` (基线中已移除文件)

### weblogs.java.net (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/jquery-1.10.2.js:605` (基线中已移除文件)

### bugs.jquery.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/jquery-1.10.2.js:924,1589,1600` (基线中已移除文件)

### javascript.nwbox.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/jquery-1.10.2.js:959` (基线中已移除文件)

### msdn.microsoft.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/jquery-1.10.2.js:2930,4623` (基线中已移除文件)

### blindsignals.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/jquery-1.10.2.js:4001` (基线中已移除文件)

### fluidproject.org (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/jquery-1.10.2.js:4483` (基线中已移除文件)

### erik.eae.net (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/jquery-1.10.2.js:7144` (基线中已移除文件)

### helpful.knobs-dials.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/jquery-1.10.2.js:8714` (基线中已移除文件)

### modern.ie (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video-js.css:125` (基线中已移除文件)

### www.patternify.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video-js.css:302` (基线中已移除文件)

### www.cssplay.co.uk (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video-js.css:679` (基线中已移除文件)

### jsninja.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.dev.js:249` (基线中已移除文件)

### coding.smashingmagazine.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.dev.js:843` (基线中已移除文件)

### robertnyman.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.dev.js:1018` (基线中已移除文件)

### paulirish.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.dev.js:1251` (基线中已移除文件)

### ejohn.org (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.dev.js:1261,2840,3960` (基线中已移除文件)

### www.foliotek.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.dev.js:2035` (基线中已移除文件)

### youtu.be (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.dev.js:2128` (基线中已移除文件)

### twitter.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.dev.js:2257` (基线中已移除文件)

### www.example.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.dev.js:3633,3639,3647,3648,3649` (基线中已移除文件)

### myvideo (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.dev.js:3689` (基线中已移除文件)

### dvcs.w3.org (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.dev.js:4024,4026` (基线中已移除文件)

### wiki.mozilla.org (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.dev.js:4025` (基线中已移除文件)

### help.videojs.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video.dev.js:4842` (基线中已移除文件)

### nsclick.baidu.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/ueditor.all.js:8732` (基线中已移除文件)

### www.youku.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/ueditor.all.js:17707,17730,17737` (基线中已移除文件)

### raw.githubusercontent.com (1文件)

- `vendor/platform-wxshop/platform-admin-ui/static/ueditor/ueditor.config.js:377` (基线中已移除文件)

### docs.jboss.org (1文件)

- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/ValidatorUtils.java:31](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/common/validator/ValidatorUtils.java)

### cloud.tencent.com (1文件)

- [vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/QcloudCloudStorageService.java:53](../vendor/platform-wxshop/platform-biz/src/main/java/com/platform/modules/oss/cloud/QcloudCloudStorageService.java)

### mmbizurl.cn (1文件)

- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/QRCodeUtil.java:96](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/QRCodeUtil.java)

### next.api.aliyun.com (1文件)

- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/SmsUtil.java:35](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/utils/SmsUtil.java)

### code.iamcal.com (1文件)

- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/HtmlFilter.java:34](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/HtmlFilter.java)

### java.sun.com (1文件)

- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/HtmlFilter.java:39](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/HtmlFilter.java)

### us2.php.net (1文件)

- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/HtmlFilter.java:40](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/HtmlFilter.java)

### www.regular-expressions.info (1文件)

- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/HtmlFilter.java:41](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/HtmlFilter.java)

### xss-html-filter.sf.net (1文件)

- [vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/HtmlFilter.java:54](../vendor/platform-wxshop/platform-common/src/main/java/com/platform/common/xss/HtmlFilter.java)

### mvnrepository.com (1文件)

- [vendor/platform-wxshop/pom.xml:279,363,370,377,384](../vendor/platform-wxshop/pom.xml)

### maven.aliyun.com (1文件)

- [vendor/platform-wxshop/pom.xml:441,459](../vendor/platform-wxshop/pom.xml)

### nexus.dev.dtstack.cn (1文件)

- [vendor/platform-wxshop/pom.xml:449](../vendor/platform-wxshop/pom.xml)

### mmbiz.qpic.cn (1文件)

- [vendor/platform-wxshop/wx-mall/app.js:42](../vendor/platform-wxshop/wx-mall/app.js)

### www (1文件)

- `vendor/platform-wxshop/wx-mall/lib/wxParse/showdown.js:1262` (基线中已移除文件)

### bumppo.net (1文件)

- `vendor/platform-wxshop/wx-mall/lib/wxParse/showdown.js:1511` (基线中已移除文件)

### tinyurl.com (1文件)

- `vendor/platform-wxshop/wx-mall/lib/wxParse/showdown.js:1579` (基线中已移除文件)


## 10. 二进制/字体/Word资产完整清单

| 文件 | 后续证据要求 |
|---|---|
| `vendor/platform-wxshop/docs/小程序上线所需材料.docx` (基线中已移除文件) | 核对内容/来源/推广与重复 |
| `vendor/platform-wxshop/docs/微同软件开发文档.docx` (基线中已移除文件) | 核对内容/来源/推广与重复 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#0BB2D4/fonts/element-icons.ttf](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%230BB2D4/fonts/element-icons.ttf) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#0BB2D4/fonts/element-icons.woff](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%230BB2D4/fonts/element-icons.woff) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#11C26D/fonts/element-icons.ttf](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%2311C26D/fonts/element-icons.ttf) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#11C26D/fonts/element-icons.woff](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%2311C26D/fonts/element-icons.woff) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#17B3A3/fonts/element-icons.ttf](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%2317B3A3/fonts/element-icons.ttf) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#17B3A3/fonts/element-icons.woff](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%2317B3A3/fonts/element-icons.woff) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#3E8EF7/fonts/element-icons.ttf](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%233E8EF7/fonts/element-icons.ttf) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#3E8EF7/fonts/element-icons.woff](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%233E8EF7/fonts/element-icons.woff) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#409EFF/fonts/element-icons.ttf](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%23409EFF/fonts/element-icons.ttf) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#409EFF/fonts/element-icons.woff](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%23409EFF/fonts/element-icons.woff) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#667AFA/fonts/element-icons.ttf](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%23667AFA/fonts/element-icons.ttf) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#667AFA/fonts/element-icons.woff](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%23667AFA/fonts/element-icons.woff) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#757575/fonts/element-icons.ttf](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%23757575/fonts/element-icons.ttf) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#757575/fonts/element-icons.woff](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%23757575/fonts/element-icons.woff) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#9463F7/fonts/element-icons.ttf](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%239463F7/fonts/element-icons.ttf) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#9463F7/fonts/element-icons.woff](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%239463F7/fonts/element-icons.woff) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#997B71/fonts/element-icons.ttf](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%23997B71/fonts/element-icons.ttf) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#997B71/fonts/element-icons.woff](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%23997B71/fonts/element-icons.woff) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#EB6709/fonts/element-icons.ttf](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%23EB6709/fonts/element-icons.ttf) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#EB6709/fonts/element-icons.woff](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%23EB6709/fonts/element-icons.woff) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#F74584/fonts/element-icons.ttf](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%23F74584/fonts/element-icons.ttf) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#F74584/fonts/element-icons.woff](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%23F74584/fonts/element-icons.woff) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#FCB900/fonts/element-icons.ttf](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%23FCB900/fonts/element-icons.ttf) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#FCB900/fonts/element-icons.woff](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%23FCB900/fonts/element-icons.woff) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#FF4C52/fonts/element-icons.ttf](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%23FF4C52/fonts/element-icons.ttf) | 字体名称/版本/嵌入与商业分发许可 |
| [vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-#FF4C52/fonts/element-icons.woff](../vendor/platform-wxshop/platform-admin-ui/src/element-ui-theme/element-%23FF4C52/fonts/element-icons.woff) | 字体名称/版本/嵌入与商业分发许可 |
| `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/wordimage/fClipboard_ueditor.swf` (基线中已移除文件) | 来源与许可, 不执行, 排除无用发布 |
| `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/wordimage/imageUploader.swf` (基线中已移除文件) | 来源与许可, 不执行, 排除无用发布 |
| `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/snapscreen/UEditorSnapscreen.exe` (基线中已移除文件) | 来源与许可, 不执行, 排除无用发布 |
| `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/font/vjs.eot` (基线中已移除文件) | 字体名称/版本/嵌入与商业分发许可 |
| `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/font/vjs.ttf` (基线中已移除文件) | 字体名称/版本/嵌入与商业分发许可 |
| `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/font/vjs.woff` (基线中已移除文件) | 字体名称/版本/嵌入与商业分发许可 |
| `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/video-js/video-js.swf` (基线中已移除文件) | 来源与许可, 不执行, 排除无用发布 |
| `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/webuploader/Uploader.swf` (基线中已移除文件) | 来源与许可, 不执行, 排除无用发布 |
| `vendor/platform-wxshop/platform-admin-ui/static/ueditor/third-party/zeroclipboard/ZeroClipboard.swf` (基线中已移除文件) | 来源与许可, 不执行, 排除无用发布 |
| `vendor/platform-wxshop/软件开发文档.docx` (基线中已移除文件) | 核对内容/来源/推广与重复 |

## 11. 同哈希资源与无字面引用候选

重复不等于可以直接删; 插件可能通过固定相对路径使用.

- SHA256 86429276603afc48ee38fc6a1f456904a270cac93307ed512ed22cbd7021cb9c: `vendor/platform-wxshop/docs/微同软件开发文档.docx` (基线中已移除文件); `vendor/platform-wxshop/软件开发文档.docx` (基线中已移除文件)
- SHA256 5d4e66d5e0de7577ceb6a120836958766d804bcefe2b95367a079dafa05d3ea6: `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/attachment/images/bg.png` (基线中已移除文件); `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/background/images/bg.png` (基线中已移除文件); `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/image/images/bg.png` (基线中已移除文件); `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/video/images/bg.png` (基线中已移除文件)
- SHA256 771270b3fb496dfbc59f16b96d6c1fda3ff34cf50d617113a6c6da4afc172124: `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/attachment/images/file-icons.gif` (基线中已移除文件); `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/video/images/file-icons.gif` (基线中已移除文件)
- SHA256 cc5f37c2d8ba738eea50cda6c8ebf4778e5371c40e8785ba63ac4af78aa61bd2: `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/attachment/images/file-icons.png` (基线中已移除文件); `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/video/images/file-icons.png` (基线中已移除文件)
- SHA256 3abaf5c2830eea93d54975eeb85f0b7ca5e00f5ea905aea80a1d04e411a4b2bf: `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/attachment/images/icons.gif` (基线中已移除文件); `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/image/images/icons.gif` (基线中已移除文件); `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/video/images/icons.gif` (基线中已移除文件)
- SHA256 1b765124991e7e100c1a88558925fd734fcbeb1aadd1f24d38d029bd7e7ca779: `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/attachment/images/icons.png` (基线中已移除文件); `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/image/images/icons.png` (基线中已移除文件); `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/video/images/icons.png` (基线中已移除文件)
- SHA256 d78bbfd47a05e576fd117a4c7e1d2a000b4f903548733f8ec02e598cbd842787: `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/attachment/images/image.png` (基线中已移除文件); `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/image/images/image.png` (基线中已移除文件); `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/video/images/image.png` (基线中已移除文件)
- SHA256 d419e68a237c281c2430e28e6094d7b59487e0fcdc817689d46b33e643948d7d: `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/attachment/images/progress.png` (基线中已移除文件); `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/image/images/progress.png` (基线中已移除文件); `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/video/images/progress.png` (基线中已移除文件)
- SHA256 ff263d92eb7c69d32f5c913353880e1d03db5f2a5df192570c3223cef5def635: `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/attachment/images/success.gif` (基线中已移除文件); `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/image/images/success.gif` (基线中已移除文件); `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/video/images/success.gif` (基线中已移除文件)
- SHA256 b55f77fea424080f73f3abd73f814d7cb3cbbf5a852a1fccc92c85f20d243a7c: `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/attachment/images/success.png` (基线中已移除文件); `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/background/images/success.png` (基线中已移除文件); `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/image/images/success.png` (基线中已移除文件); `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/video/images/success.png` (基线中已移除文件)
- SHA256 270d0245a73259bca3b8ad29ec52d5b2663328a633b6cb6c6d42955d348cca7c: `vendor/platform-wxshop/platform-admin-ui/static/ueditor/dialogs/emotion/images/neweditor-tab-bg.png` (基线中已移除文件); `vendor/platform-wxshop/platform-admin-ui/static/ueditor/themes/default/images/neweditor-tab-bg.png` (基线中已移除文件)
- SHA256 1da2e0212ca7ade01c9611c7b7765c0fa634dc1526f3e1b0451b2ea15982699f: `vendor/platform-wxshop/platform-admin-ui/static/ueditor/lang/zh-cn/images/upload.png` (基线中已移除文件); `vendor/platform-wxshop/platform-admin-ui/static/ueditor/themes/default/images/upload.png` (基线中已移除文件)

仅按字面文件名未找到引用的小程序图片 (7项, 动态拼接仍需核对):

- [vendor/platform-wxshop/wx-mall/static/images/address-bg-bd.png](../vendor/platform-wxshop/wx-mall/static/images/address-bg-bd.png)
- [vendor/platform-wxshop/wx-mall/static/images/del-address.png](../vendor/platform-wxshop/wx-mall/static/images/del-address.png)
- [vendor/platform-wxshop/wx-mall/static/images/detail_kefu.png](../vendor/platform-wxshop/wx-mall/static/images/detail_kefu.png)
- [vendor/platform-wxshop/wx-mall/static/images/ic_menu_sort_nor.png](../vendor/platform-wxshop/wx-mall/static/images/ic_menu_sort_nor.png)
- [vendor/platform-wxshop/wx-mall/static/images/ic_menu_sort_pressed.png](../vendor/platform-wxshop/wx-mall/static/images/ic_menu_sort_pressed.png)
- [vendor/platform-wxshop/wx-mall/static/images/icon_go_more.png](../vendor/platform-wxshop/wx-mall/static/images/icon_go_more.png)
- [vendor/platform-wxshop/wx-mall/static/images/selnum.png](../vendor/platform-wxshop/wx-mall/static/images/selnum.png)

## 12. 商业发布前应补齐的材料

1. SOURCE_PROVENANCE: 原始库/版本/来源、本人新增范围、复制片段和授权证据.
2. LICENSE/NOTICE/THIRD_PARTY_NOTICES: 按实际发布文件保留适用许可、版权和修改说明.
3. ASSET_RIGHTS: 图片/图标/字体/商品文案的作者、原始文件、来源、商用与分发权利.
4. SBOM与许可选择: 分开运行依赖、开发测试工具、静态复制库, 记录例外条件和是否进入发布包.
5. 清理验收: 实际源码包/镜像/小程序上传包无无关品牌、推广、工具缓存和无用可执行插件; 不移除必要声明.

本轮只提供清单和证据, 没有以更换许可证或批量署名做“确权”.

## 全量依赖声明附件

本地已安装npm树1497个唯一name/version的许可字段, 与API测试类路径296个JAR的同名POM直接声明, 见[逐依赖声明](audit-2026-10-13-dependency-licenses.md)及[机器可读声明/源码版权定位](audit-2026-10-13-dependency-licenses.json). 开发工具不自动视作发布依赖, 未直接声明项还要查父POM和JAR内许可. 没有根据这些字段自动出具商用批准.
