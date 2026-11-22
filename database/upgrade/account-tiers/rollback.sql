SET NAMES utf8mb4;
-- 回滚前停写. 已经创建个人授权或仍有临时管理员凭据时须从已核验备份恢复.
DELIMITER //
DROP PROCEDURE IF EXISTS cc_rollback_account_tiers//
CREATE PROCEDURE cc_rollback_account_tiers()
BEGIN
    IF COALESCE(@cc_confirm_account_tier_rollback, '') <> 'ROLLBACK_ACCOUNT_TIERS'
       OR EXISTS(SELECT 1 FROM SYS_ROLE WHERE DIRECT_USER_ID IS NOT NULL)
       OR EXISTS(SELECT 1 FROM SYS_USER WHERE ACCOUNT_TYPE IN ('PLATFORM_ADMIN','MERCHANT_ADMIN')
                 AND PASSWORD_CHANGE_REQUIRED=1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Rollback requires review of active temporary credentials and direct grants';
    END IF;
    DELETE t FROM SYS_USER_TOKEN t JOIN SYS_USER u ON u.USER_ID=t.USER_ID WHERE u.ACCOUNT_TYPE IS NOT NULL;
    ALTER TABLE SYS_ROLE DROP INDEX uk_sys_role_direct_user, DROP COLUMN DIRECT_USER_ID;
    ALTER TABLE SYS_USER DROP CHECK ck_sys_user_account_type,
        DROP COLUMN TEMPORARY_PASSWORD_EXPIRES_AT, DROP COLUMN ACCOUNT_TYPE;
END//
DELIMITER ;
CALL cc_rollback_account_tiers();
DROP PROCEDURE cc_rollback_account_tiers;
