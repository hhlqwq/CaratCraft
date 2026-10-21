-- 补齐旧初始化遗漏的商品回收站结构, 不重置删除时间或注册清理任务.
-- 已由旧初始化建立结构的新库保持原样, 支持DDL中断后的重试.
SET @cc_has_delete_time = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='mall_goods' AND COLUMN_NAME='delete_time'
);
SET @cc_recycle_sql = IF(@cc_has_delete_time=0,
  'ALTER TABLE mall_goods ADD COLUMN delete_time DATETIME DEFAULT NULL COMMENT ''移入回收站时间'' AFTER is_delete',
  'SELECT 1');
PREPARE cc_recycle_stmt FROM @cc_recycle_sql;
EXECUTE cc_recycle_stmt;
DEALLOCATE PREPARE cc_recycle_stmt;

SET @cc_has_recycle_index = (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='mall_goods' AND INDEX_NAME='idx_goods_recycle'
);
SET @cc_recycle_sql = IF(@cc_has_recycle_index=0,
  'ALTER TABLE mall_goods ADD INDEX idx_goods_recycle (is_delete,delete_time)',
  'SELECT 1');
PREPARE cc_recycle_stmt FROM @cc_recycle_sql;
EXECUTE cc_recycle_stmt;
DEALLOCATE PREPARE cc_recycle_stmt;
