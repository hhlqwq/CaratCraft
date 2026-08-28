-- CaratCraft 稳定性索引升级 / CaratCraft stability index migration.
SET @schema_name = DATABASE();

SET @index_exists = (SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = @schema_name AND table_name = 'mall_order'
    AND index_name = 'idx_mall_order_user_add_time');
SET @sql = IF(@index_exists = 0,
  'ALTER TABLE mall_order ADD INDEX idx_mall_order_user_add_time (user_id, add_time)',
  'SELECT 1');
PREPARE stability_stmt FROM @sql;
EXECUTE stability_stmt;
DEALLOCATE PREPARE stability_stmt;

SET @index_exists = (SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = @schema_name AND table_name = 'mall_order'
    AND index_name = 'idx_mall_order_status_pay_add_time');
SET @sql = IF(@index_exists = 0,
  'ALTER TABLE mall_order ADD INDEX idx_mall_order_status_pay_add_time (order_status, pay_status, add_time)',
  'SELECT 1');
PREPARE stability_stmt FROM @sql;
EXECUTE stability_stmt;
DEALLOCATE PREPARE stability_stmt;

SET @index_exists = (SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = @schema_name AND table_name = 'CC_PRODUCTION_WORK_ORDER'
    AND index_name = 'idx_cc_work_order_status_update_time');
SET @sql = IF(@index_exists = 0,
  'ALTER TABLE CC_PRODUCTION_WORK_ORDER ADD INDEX idx_cc_work_order_status_update_time (status, update_time)',
  'SELECT 1');
PREPARE stability_stmt FROM @sql;
EXECUTE stability_stmt;
DEALLOCATE PREPARE stability_stmt;
