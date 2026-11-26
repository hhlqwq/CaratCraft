SET NAMES utf8mb4;

-- 标准启动已核对结构及迁移状态. 此结构增量不写入旧商品或常见问题.
SET @cc_question_sql = IF(COALESCE(@cc_need_apply, 1) AND NOT COALESCE(@cc_question_exists, 0),
'CREATE TABLE IF NOT EXISTS CC_GOODS_QUESTION (
    id BIGINT NOT NULL AUTO_INCREMENT,
    goods_id INT NOT NULL,
    user_id INT UNSIGNED NOT NULL,
    question VARCHAR(500) NOT NULL,
    answer VARCHAR(1000) DEFAULT NULL,
    status VARCHAR(12) NOT NULL DEFAULT ''PENDING'',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY ix_goods_question_public (goods_id, status, id),
    KEY ix_goods_question_user (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci', 'DO 0');
PREPARE cc_question_stmt FROM @cc_question_sql;
EXECUTE cc_question_stmt;
DEALLOCATE PREPARE cc_question_stmt;

-- 评价保存独立商家回复, 不替换用户原始正文.
SET @cc_reply_sql = IF(NOT COALESCE(@cc_need_apply, 1) OR EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mall_comment' AND COLUMN_NAME = 'staff_reply'
), 'DO 0', 'ALTER TABLE mall_comment ADD COLUMN staff_reply VARCHAR(1000) DEFAULT NULL');
PREPARE cc_reply_stmt FROM @cc_reply_sql;
EXECUTE cc_reply_stmt;
DEALLOCATE PREPARE cc_reply_stmt;
