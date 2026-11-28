SET NAMES utf8mb4;

-- 已添加的列只核对, 中断后只补缺列.
SET @cc_column_sql = IF(COALESCE(@cc_need_apply,1) AND NOT @cc_actor_id_exists, 'ALTER TABLE SYS_LOG ADD COLUMN actor_id varchar(32) DEFAULT NULL', 'DO 0');
PREPARE cc_column_stmt FROM @cc_column_sql;
EXECUTE cc_column_stmt;
DEALLOCATE PREPARE cc_column_stmt;
SET @cc_column_sql = IF(COALESCE(@cc_need_apply,1) AND NOT @cc_actor_type_exists, 'ALTER TABLE SYS_LOG ADD COLUMN actor_type varchar(16) DEFAULT NULL', 'DO 0');
PREPARE cc_column_stmt FROM @cc_column_sql;
EXECUTE cc_column_stmt;
DEALLOCATE PREPARE cc_column_stmt;
SET @cc_column_sql = IF(COALESCE(@cc_need_apply,1) AND NOT @cc_module_exists, 'ALTER TABLE SYS_LOG ADD COLUMN module varchar(50) DEFAULT NULL', 'DO 0');
PREPARE cc_column_stmt FROM @cc_column_sql;
EXECUTE cc_column_stmt;
DEALLOCATE PREPARE cc_column_stmt;
SET @cc_column_sql = IF(COALESCE(@cc_need_apply,1) AND NOT @cc_operation_type_exists, 'ALTER TABLE SYS_LOG ADD COLUMN operation_type varchar(32) DEFAULT NULL', 'DO 0');
PREPARE cc_column_stmt FROM @cc_column_sql;
EXECUTE cc_column_stmt;
DEALLOCATE PREPARE cc_column_stmt;
SET @cc_column_sql = IF(COALESCE(@cc_need_apply,1) AND NOT @cc_target_id_exists, 'ALTER TABLE SYS_LOG ADD COLUMN target_id varchar(128) DEFAULT NULL', 'DO 0');
PREPARE cc_column_stmt FROM @cc_column_sql;
EXECUTE cc_column_stmt;
DEALLOCATE PREPARE cc_column_stmt;
SET @cc_column_sql = IF(COALESCE(@cc_need_apply,1) AND NOT @cc_result_exists, 'ALTER TABLE SYS_LOG ADD COLUMN result varchar(16) DEFAULT NULL', 'DO 0');
PREPARE cc_column_stmt FROM @cc_column_sql;
EXECUTE cc_column_stmt;
DEALLOCATE PREPARE cc_column_stmt;
SET @cc_column_sql = IF(COALESCE(@cc_need_apply,1) AND NOT @cc_summary_exists, 'ALTER TABLE SYS_LOG ADD COLUMN summary varchar(1000) DEFAULT NULL', 'DO 0');
PREPARE cc_column_stmt FROM @cc_column_sql;
EXECUTE cc_column_stmt;
DEALLOCATE PREPARE cc_column_stmt;
SET @cc_column_sql = IF(COALESCE(@cc_need_apply,1) AND NOT @cc_error_code_exists, 'ALTER TABLE SYS_LOG ADD COLUMN error_code varchar(40) DEFAULT NULL', 'DO 0');
PREPARE cc_column_stmt FROM @cc_column_sql;
EXECUTE cc_column_stmt;
DEALLOCATE PREPARE cc_column_stmt;

-- 只在机构表完全为空时创建唯一默认工作室, 不覆盖既有机构.
INSERT INTO SYS_ORG(ORG_NO,ORG_NAME,PARENT_NO,ORG_TYPE,STATUS,SORT,CREATE_USER_ID,CREATE_TIME)
SELECT 'CC0001','默认工作室','0',1,1,0,'1',NOW() WHERE NOT EXISTS(SELECT 1 FROM SYS_ORG);
INSERT INTO SYS_CONFIG(ID,PARAM_KEY,PARAM_VALUE,STATUS,REMARK)
SELECT 'cc-platform-default-workspace','caratcraft.business.org',MIN(ORG_NO),0,'平台默认工作室'
FROM SYS_ORG WHERE STATUS=1
HAVING COUNT(*)=1 AND NOT EXISTS(SELECT 1 FROM SYS_CONFIG WHERE PARAM_KEY='caratcraft.business.org');

