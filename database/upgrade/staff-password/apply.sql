SET NAMES utf8mb4;
-- 对已有验收库幂等增加临时密码状态. 先备份, 保留所有原登录凭据.
SET @staff_password_ddl = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE()
           AND TABLE_NAME='SYS_USER' AND COLUMN_NAME='PASSWORD_CHANGE_REQUIRED'),
    'SELECT ''PASSWORD_CHANGE_REQUIRED already present''',
    'ALTER TABLE SYS_USER ADD COLUMN PASSWORD_CHANGE_REQUIRED tinyint(1) NOT NULL DEFAULT 0'
);
PREPARE staff_password_upgrade FROM @staff_password_ddl;
EXECUTE staff_password_upgrade;
DEALLOCATE PREPARE staff_password_upgrade;
