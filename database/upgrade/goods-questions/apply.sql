SET NAMES utf8mb4;

-- 已有库请先备份并在隔离环境验收. 此脚本不写入旧商品或常见问题.
CREATE TABLE IF NOT EXISTS CC_GOODS_QUESTION (
    id BIGINT NOT NULL AUTO_INCREMENT,
    goods_id INT NOT NULL,
    user_id INT UNSIGNED NOT NULL,
    question VARCHAR(500) NOT NULL,
    answer VARCHAR(1000) DEFAULT NULL,
    status VARCHAR(12) NOT NULL DEFAULT 'PENDING',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY ix_goods_question_public (goods_id, status, id),
    KEY ix_goods_question_user (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 评价保存独立商家回复, 不替换用户原始正文.
SET @cc_reply_sql = IF(EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mall_comment' AND COLUMN_NAME = 'staff_reply'
), 'SELECT 1', 'ALTER TABLE mall_comment ADD COLUMN staff_reply VARCHAR(1000) DEFAULT NULL');
PREPARE cc_reply_stmt FROM @cc_reply_sql;
EXECUTE cc_reply_stmt;
DEALLOCATE PREPARE cc_reply_stmt;
