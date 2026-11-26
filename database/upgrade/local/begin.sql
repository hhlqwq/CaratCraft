SET NAMES utf8mb4;

-- 同一MySQL连接持有锁; DDL隐式提交不释放锁, 断开连接时自动释放.
SET @cc_lock_name = CONCAT('cc_migrations_', LEFT(SHA2(DATABASE(), 256), 32));
CREATE TEMPORARY TABLE cc_lock_check (ok TINYINT NOT NULL CHECK (ok = 1));
INSERT INTO cc_lock_check VALUES (COALESCE(GET_LOCK(@cc_lock_name, 15), 0));

CREATE TABLE IF NOT EXISTS CC_SCHEMA_MIGRATION (
    migration_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    checksum CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    applied_at DATETIME DEFAULT NULL,
    PRIMARY KEY (migration_id)
) ENGINE=InnoDB;

CREATE TEMPORARY TABLE cc_registry_check (ok TINYINT NOT NULL CHECK (ok = 1));
INSERT INTO cc_registry_check
SELECT NOT EXISTS(SELECT 1 FROM CC_SCHEMA_MIGRATION WHERE migration_id NOT IN (__IDS__));
