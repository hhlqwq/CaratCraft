SET NAMES utf8mb4;

-- 只读取得已完成编号. 批次持锁后会再次核对, 防止并发迁移使计划过期.
SET @cc_snapshot_sql = IF(EXISTS(
    SELECT 1 FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'CC_SCHEMA_MIGRATION'
), 'SELECT migration_id FROM CC_SCHEMA_MIGRATION WHERE applied_at IS NOT NULL', 'DO 0');
PREPARE cc_snapshot_stmt FROM @cc_snapshot_sql;
EXECUTE cc_snapshot_stmt;
DEALLOCATE PREPARE cc_snapshot_stmt;
