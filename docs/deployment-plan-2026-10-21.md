# 本机业务库升级与部署计划

日期: 2026-10-21. 本批代码、隔离测试和应用构建已经完成, 本文用于审核下一步部署. 本批未写入业务库, 未停止或重建项目服务. 未经单独确认不执行以下写库或重启步骤.

## 当前环境证据

只读检查时platform-mysql、platform-redis、platform-admin、platform-api、platform-nginx均健康. 当前应用容器未提供可验证的源码提交标签, 不把健康状态解释为已部署最新功能.

对业务库platform和独立初始化库caratcraft_init_audit_20261021_045856仅比较information_schema元数据, 未读取业务行. 发现缺少CC_SCHEMA_MIGRATION, 无法直接使用版本台账判定既有版本. 结构差异如下:

| 类型 | 差异 |
| --- | --- |
| 缺少16张表 | CC_COMMUNITY_LIKE、CC_COMMUNITY_LOG、CC_COMMUNITY_MEDIA、CC_COMMUNITY_POST、CC_LEGAL_ACCEPTANCE、CC_LEGAL_DOCUMENT、CC_LEGAL_HEAD、CC_LOGIN_IDENTITY_LOCK、CC_MERCHANT_CONTACT、CC_MERCHANT_CONTACT_LOG、CC_ORDER_LEGAL、CC_ORDER_PRICE_LOG、CC_PRODUCT_VIDEO、CC_REFUND、CC_SCHEMA_MIGRATION、CC_STOCK_LOG |
| 缺少15个字段 | CC_CUSTOM_PRODUCT.length_rules、CC_DESIGN_SNAPSHOT.version; mall_goods的pearl_source、jewelry_type、pearl_variety、pearl_origin、pearl_shape、pearl_luster、pearl_blemish、pearl_diameter_min、pearl_diameter_max、video_media_id、video_url; mall_order的request_key、request_digest |
| 4个定义变化 | SYS_OSS.URL由varchar(200)扩至varchar(1000); WX_USER.MA_OPENID、OPENID、UNION_ID改用utf8mb4_bin以保留微信身份大小写区别 |

此表不是完整升级脚本. 索引、约束、历史数据兼容性和迁移中间状态仍须在备份副本逐项核对, 不能只按字段数量决定执行结果.

## 经确认后执行的顺序

1. 暂停产生新交易的入口, 备份业务数据库、上传文件、当前应用和私有配置, 核验备份可恢复. 密钥和个人业务数据不提交到仓库.
2. 只读盘点MA_OPENID/OPENID的重复组、空字符串及历史UnionID关联. 有冲突则停止, 不删除、合并账号或转移订单积分.
3. 将备份恢复到独立副本, 用tools/SchemaMigrationTool.java的--plan输出与11份V版本逐一核对. 对已存在但没有台账的变更必须核验完整后处理, 不伪造成功记录, 不直接在业务库重跑半完成版本. 若存在冲突, 先提交可审阅的兼容迁移方案.
4. 在副本演练迁移并核验数据、索引、约束、就绪检查及核心交易, 确认每个DDL的失败恢复措施. MySQL DDL可能隐式提交, 不能依赖事务撤销整个升级.
5. 使用审核通过的固定提交和SHA256交付清单升级业务库. 只执行核准的增量步骤, 不执行root空库初始化SQL, 不重建MySQL或Redis数据卷.
6. 更新platform-admin、platform-api及platform-nginx承载的管理端静态文件, 重建或重启这三个应用服务. 保留MySQL、Redis服务及上传数据. 新应用就绪检查必须通过16张必要表和V20261021_02版本要求.
7. 验证管理员登录、商品、DIY、社区审核、协议发布/确认、订单和售后. 商家填入自己的正式协议、客服、材料和工艺规则后, 再以实际微信、支付和AI账号完成外部验收.

回滚时必须使应用版本与数据库兼容. 有新订单或迁移后数据时, 不以旧备份直接覆盖新业务; 先停止新写入并评估补偿或前向修复. 部署前应明确恢复负责人及可接受停机窗口.

## 当前确认边界

用户已授权本批代码修改、测试、提交和推送. 业务库迁移及应用服务重启需要另行确认, 依据全局规范“如果要重启或者杀掉其他程序的进程前必须先向我确认”及此前不操作业务库的约定. 确认部署后仍须先完成备份与副本演练, 不直接跳过上述核对.
