SET NAMES utf8mb4;

-- 当前应用版本依赖的结构. 后续版本按新契约维护此文件, 历史迁移SQL保持不可变.
SELECT 'current schema: cc_goods_question and mall_comment.staff_reply';
SET @cc_question_ok = EXISTS (
    SELECT 1 FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'cc_goods_question'
      AND ENGINE = 'InnoDB' AND TABLE_COLLATION = 'utf8mb4_unicode_ci'
) AND (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'cc_goods_question'
      AND (
          (COLUMN_NAME = 'id' AND COLUMN_TYPE = 'bigint' AND IS_NULLABLE = 'NO' AND EXTRA = 'auto_increment')
          OR (COLUMN_NAME = 'goods_id' AND COLUMN_TYPE = 'int' AND IS_NULLABLE = 'NO')
          OR (COLUMN_NAME = 'user_id' AND COLUMN_TYPE = 'int unsigned' AND IS_NULLABLE = 'NO')
          OR (COLUMN_NAME = 'question' AND COLUMN_TYPE = 'varchar(500)' AND IS_NULLABLE = 'NO')
          OR (COLUMN_NAME = 'answer' AND COLUMN_TYPE = 'varchar(1000)' AND IS_NULLABLE = 'YES')
          OR (COLUMN_NAME = 'status' AND COLUMN_TYPE = 'varchar(12)' AND IS_NULLABLE = 'NO' AND COLUMN_DEFAULT = 'PENDING')
          OR (COLUMN_NAME = 'create_time' AND COLUMN_TYPE = 'datetime' AND IS_NULLABLE = 'NO' AND COLUMN_DEFAULT = 'CURRENT_TIMESTAMP')
          OR (COLUMN_NAME = 'update_time' AND COLUMN_TYPE = 'datetime' AND IS_NULLABLE = 'NO'
              AND COLUMN_DEFAULT = 'CURRENT_TIMESTAMP' AND EXTRA LIKE '%on update CURRENT_TIMESTAMP%')
      )
) = 8 AND (
    SELECT COUNT(*) FROM (
        SELECT INDEX_NAME, GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS columns_in_order
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'cc_goods_question'
        GROUP BY INDEX_NAME
    ) indexes_found
    WHERE (INDEX_NAME = 'PRIMARY' AND columns_in_order = 'id')
       OR (INDEX_NAME = 'ix_goods_question_public' AND columns_in_order = 'goods_id,status,id')
       OR (INDEX_NAME = 'ix_goods_question_user' AND columns_in_order = 'user_id,create_time')
) = 3;

SET @cc_reply_ok = EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'mall_comment' AND COLUMN_NAME = 'staff_reply'
      AND COLUMN_TYPE = 'varchar(1000)' AND IS_NULLABLE = 'YES' AND COLUMN_DEFAULT IS NULL
);

CREATE TEMPORARY TABLE cc_current_schema_check (ok TINYINT NOT NULL CHECK (ok = 1));
INSERT INTO cc_current_schema_check
SELECT @cc_question_ok AND @cc_reply_ok AND EXISTS(
    SELECT 1 FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'mall_comment' AND ENGINE = 'InnoDB'
);
