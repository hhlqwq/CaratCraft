SET NAMES utf8mb4;
SET collation_connection='utf8mb4_general_ci';
-- 使用同一会话中的人工批准临时表 cc_tier_mapping; 不猜测机构或历史账号类型.
-- 调用前在隔离副本验证备份, 停止管理端写入. 本脚本不改动商品和社区历史归属.
DELIMITER //
DROP PROCEDURE IF EXISTS cc_apply_account_tiers//
CREATE PROCEDURE cc_apply_account_tiers()
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF COALESCE(@cc_confirm_account_tiers, '') <> 'APPLY_ACCOUNT_TIERS'
       OR NOT EXISTS(SELECT 1 FROM SYS_ORG WHERE ORG_NO=CONVERT(@cc_default_org USING utf8mb4) COLLATE utf8mb4_general_ci AND STATUS=1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Missing confirmation or enabled default organization';
    END IF;
    IF NOT EXISTS(SELECT 1 FROM cc_tier_mapping WHERE USER_ID='1' AND ACCOUNT_TYPE='PLATFORM_ADMIN') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Missing the fixed platform account mapping';
    END IF;
    IF EXISTS(SELECT 1 FROM SYS_USER u LEFT JOIN cc_tier_mapping m ON m.USER_ID COLLATE utf8mb4_general_ci=u.USER_ID WHERE m.USER_ID IS NULL) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='An existing account has no approved mapping';
    END IF;
    IF EXISTS(SELECT 1 FROM cc_tier_mapping m LEFT JOIN SYS_USER u ON u.USER_ID=m.USER_ID COLLATE utf8mb4_general_ci WHERE u.USER_ID IS NULL) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Approved mapping contains an unknown account';
    END IF;
    IF EXISTS(SELECT 1 FROM cc_tier_mapping WHERE (USER_ID='1' AND ACCOUNT_TYPE<>'PLATFORM_ADMIN')
              OR (USER_ID<>'1' AND (ACCOUNT_TYPE NOT IN ('MERCHANT_ADMIN','EMPLOYEE')
                  OR NOT (ORG_NO <=> CONVERT(@cc_default_org USING utf8mb4) COLLATE utf8mb4_general_ci)))) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Account mapping does not match existing accounts and default studio';
    END IF;

    IF NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE()
            AND TABLE_NAME='SYS_USER' AND COLUMN_NAME='ACCOUNT_TYPE') THEN
        ALTER TABLE SYS_USER ADD COLUMN ACCOUNT_TYPE varchar(16) DEFAULT NULL,
            ADD COLUMN TEMPORARY_PASSWORD_EXPIRES_AT datetime DEFAULT NULL,
            ADD CONSTRAINT ck_sys_user_account_type CHECK (
                (USER_ID='1' AND ACCOUNT_TYPE='PLATFORM_ADMIN') OR
                (USER_ID<>'1' AND ACCOUNT_TYPE IN ('MERCHANT_ADMIN','EMPLOYEE')));
    END IF;
    IF NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE()
            AND TABLE_NAME='SYS_ROLE' AND COLUMN_NAME='DIRECT_USER_ID') THEN
        ALTER TABLE SYS_ROLE ADD COLUMN DIRECT_USER_ID varchar(32) DEFAULT NULL,
            ADD UNIQUE KEY uk_sys_role_direct_user(DIRECT_USER_ID);
    END IF;

    START TRANSACTION;
    IF EXISTS(SELECT 1 FROM SYS_USER WHERE ACCOUNT_TYPE IS NOT NULL) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Accounts already classified; review any partial migration';
    END IF;
    UPDATE SYS_USER u JOIN cc_tier_mapping m ON m.USER_ID COLLATE utf8mb4_general_ci=u.USER_ID
    SET u.ACCOUNT_TYPE=m.ACCOUNT_TYPE, u.ORG_NO=IF(m.ACCOUNT_TYPE='PLATFORM_ADMIN',NULL,m.ORG_NO);
    -- 须通过人工引导签发新凭据, 不延用历史内置超管密码.
    UPDATE SYS_USER SET STATUS=0, PASSWORD=NULL, SALT=NULL, PASSWORD_CHANGE_REQUIRED=1,
        TEMPORARY_PASSWORD_EXPIRES_AT=NULL WHERE USER_ID='1';
    DELETE t FROM SYS_USER_TOKEN t JOIN cc_tier_mapping m ON m.USER_ID COLLATE utf8mb4_general_ci=t.USER_ID;
    COMMIT;
END//
DELIMITER ;
CALL cc_apply_account_tiers();
DROP PROCEDURE cc_apply_account_tiers;
