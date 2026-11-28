SET NAMES utf8mb4;

-- 缺列可添加, 同名异构列必须停止.
SET @cc_actor_id_exists = EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='SYS_LOG' AND COLUMN_NAME='actor_id');
SET @cc_actor_id_ok = EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='SYS_LOG' AND COLUMN_NAME='actor_id' AND COLUMN_TYPE='varchar(32)' AND IS_NULLABLE='YES');
SET @cc_actor_type_exists = EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='SYS_LOG' AND COLUMN_NAME='actor_type');
SET @cc_actor_type_ok = EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='SYS_LOG' AND COLUMN_NAME='actor_type' AND COLUMN_TYPE='varchar(16)' AND IS_NULLABLE='YES');
SET @cc_module_exists = EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='SYS_LOG' AND COLUMN_NAME='module');
SET @cc_module_ok = EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='SYS_LOG' AND COLUMN_NAME='module' AND COLUMN_TYPE='varchar(50)' AND IS_NULLABLE='YES');
SET @cc_operation_type_exists = EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='SYS_LOG' AND COLUMN_NAME='operation_type');
SET @cc_operation_type_ok = EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='SYS_LOG' AND COLUMN_NAME='operation_type' AND COLUMN_TYPE='varchar(32)' AND IS_NULLABLE='YES');
SET @cc_target_id_exists = EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='SYS_LOG' AND COLUMN_NAME='target_id');
SET @cc_target_id_ok = EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='SYS_LOG' AND COLUMN_NAME='target_id' AND COLUMN_TYPE='varchar(128)' AND IS_NULLABLE='YES');
SET @cc_result_exists = EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='SYS_LOG' AND COLUMN_NAME='result');
SET @cc_result_ok = EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='SYS_LOG' AND COLUMN_NAME='result' AND COLUMN_TYPE='varchar(16)' AND IS_NULLABLE='YES');
SET @cc_summary_exists = EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='SYS_LOG' AND COLUMN_NAME='summary');
SET @cc_summary_ok = EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='SYS_LOG' AND COLUMN_NAME='summary' AND COLUMN_TYPE='varchar(1000)' AND IS_NULLABLE='YES');
SET @cc_error_code_exists = EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='SYS_LOG' AND COLUMN_NAME='error_code');
SET @cc_error_code_ok = EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='SYS_LOG' AND COLUMN_NAME='error_code' AND COLUMN_TYPE='varchar(40)' AND IS_NULLABLE='YES');
SET @cc_compatible = (NOT @cc_actor_id_exists OR @cc_actor_id_ok) AND (NOT @cc_actor_type_exists OR @cc_actor_type_ok) AND (NOT @cc_module_exists OR @cc_module_ok) AND (NOT @cc_operation_type_exists OR @cc_operation_type_ok) AND (NOT @cc_target_id_exists OR @cc_target_id_ok) AND (NOT @cc_result_exists OR @cc_result_ok) AND (NOT @cc_summary_exists OR @cc_summary_ok) AND (NOT @cc_error_code_exists OR @cc_error_code_ok);
SET @cc_complete = @cc_actor_id_ok AND @cc_actor_type_ok AND @cc_module_ok AND @cc_operation_type_ok AND @cc_target_id_ok AND @cc_result_ok AND @cc_summary_ok AND @cc_error_code_ok;
