SET NAMES utf8mb4;

-- 商品三十天回收站迁移，适用于已有数据库并可重复执行。


SET @has_delete_time = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'mall_goods'
      AND COLUMN_NAME = 'delete_time'
);
SET @add_delete_time_sql = IF(
    @has_delete_time = 0,
    'ALTER TABLE mall_goods ADD COLUMN delete_time datetime DEFAULT NULL COMMENT ''移入回收站时间'' AFTER is_delete',
    'SELECT 1'
);
PREPARE add_delete_time_stmt FROM @add_delete_time_sql;
EXECUTE add_delete_time_stmt;
DEALLOCATE PREPARE add_delete_time_stmt;

SET @has_recycle_index = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'mall_goods'
      AND INDEX_NAME = 'idx_goods_recycle'
);
SET @add_recycle_index_sql = IF(
    @has_recycle_index = 0,
    'ALTER TABLE mall_goods ADD INDEX idx_goods_recycle (is_delete, delete_time)',
    'SELECT 1'
);
PREPARE add_recycle_index_stmt FROM @add_recycle_index_sql;
EXECUTE add_recycle_index_stmt;
DEALLOCATE PREPARE add_recycle_index_stmt;

-- 旧逻辑删除数据从迁移时开始计算三十天，并立即下架。
UPDATE mall_goods
SET delete_time = COALESCE(delete_time, NOW()),
    is_on_sale = 0
WHERE is_delete = 1;

INSERT INTO SYS_MENU (MENU_ID, PARENT_ID, NAME, URL, PERMS, TYPE, ICON, ORDER_NUM) VALUES
('1703', '17', '商品回收站', 'mall/goods-recycle', 'mall:goods:recycle:list', 1, 'delete', 3),
('170301', '1703', '恢复商品', NULL, 'mall:goods:recycle:restore', 2, NULL, 0),
('170302', '1703', '永久删除', NULL, 'mall:goods:recycle:delete', 2, NULL, 0)
ON DUPLICATE KEY UPDATE
NAME = VALUES(NAME), URL = VALUES(URL), PERMS = VALUES(PERMS), TYPE = VALUES(TYPE),
ICON = VALUES(ICON), ORDER_NUM = VALUES(ORDER_NUM);

INSERT INTO SCHEDULE_JOB
(JOB_ID, BEAN_NAME, METHOD_NAME, PARAMS, CRON_EXPRESSION, STATUS, REMARK, CREATE_TIME)
VALUES
('goodsRecycleTask', 'goodsRecycleTask', 'purgeExpiredGoods', NULL, '0 20 3 * * ?', 0,
 '每天凌晨3点20分清理超过三十天的回收站商品', NOW())
ON DUPLICATE KEY UPDATE
BEAN_NAME = VALUES(BEAN_NAME), METHOD_NAME = VALUES(METHOD_NAME), PARAMS = VALUES(PARAMS),
CRON_EXPRESSION = VALUES(CRON_EXPRESSION), REMARK = VALUES(REMARK);

