SET NAMES utf8mb4;
-- 回滚前停写并核对备份. 业务归属、审核记录等增量字段保留以避免丢失已产生的数据.
-- 只有未绑定员工、无机构分配且未标记委派的模板可自动删除.
DROP PROCEDURE IF EXISTS cc_rollback_admin_jobs;
DELIMITER $$
CREATE PROCEDURE cc_rollback_admin_jobs()
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    IF EXISTS(SELECT 1 FROM SYS_USER_ROLE WHERE ROLE_ID IN ('cc-job-goods','cc-job-fulfillment','cc-job-support','cc-job-refunds','cc-job-community','cc-job-system'))
        OR EXISTS(SELECT 1 FROM SYS_ROLE_ORG WHERE ROLE_ID IN ('cc-job-goods','cc-job-fulfillment','cc-job-support','cc-job-refunds','cc-job-community','cc-job-system'))
        OR EXISTS(SELECT 1 FROM SYS_ROLE WHERE ROLE_ID IN ('cc-job-goods','cc-job-fulfillment','cc-job-support','cc-job-refunds','cc-job-community','cc-job-system') AND (DELEGABLE=1 OR COALESCE(REMARK,'')<>'岗位模板，分配前确认数据机构'))
        OR EXISTS(SELECT 1 FROM SYS_ROLE_MENU WHERE MENU_ID IN ('1802','180201','1803','180301','180302','180108','140602','110106') AND ROLE_ID NOT IN ('cc-job-goods','cc-job-fulfillment','cc-job-support','cc-job-refunds','cc-job-community','cc-job-system')) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='存在实际授权, 停止回滚并由管理员核对';
    END IF;
    DELETE FROM SYS_ROLE_MENU WHERE ROLE_ID IN ('cc-job-goods','cc-job-fulfillment','cc-job-support','cc-job-refunds','cc-job-community','cc-job-system');
    DELETE FROM SYS_ROLE WHERE ROLE_ID IN ('cc-job-goods','cc-job-fulfillment','cc-job-support','cc-job-refunds','cc-job-community','cc-job-system') AND REMARK='岗位模板，分配前确认数据机构';
    UPDATE SYS_MENU SET PARENT_ID='1801',NAME='审核退款' WHERE MENU_ID='180105' AND PERMS='mall:refund:execute' AND PARENT_ID='1802';
    UPDATE SYS_MENU SET PARENT_ID='1801' WHERE MENU_ID IN ('180106','180107') AND PERMS IN ('mall:refund:retry','mall:refund:reconcile') AND PARENT_ID='1802';
    DELETE FROM SYS_MENU WHERE (MENU_ID='1802' AND PERMS='mall:refund:list' AND PARENT_ID='18') OR (MENU_ID='180201' AND PERMS='mall:refund:review' AND PARENT_ID='1802') OR (MENU_ID='1803' AND PERMS='mall:aftersale:list' AND PARENT_ID='18') OR (MENU_ID='180301' AND PERMS='mall:aftersale:register' AND PARENT_ID='1803') OR (MENU_ID='180302' AND PERMS='mall:aftersale:handle' AND PARENT_ID='1803') OR (MENU_ID='180108' AND PERMS='mall:order:price:update' AND PARENT_ID='1801') OR (MENU_ID='140602' AND PERMS='mall:feedback:update' AND PARENT_ID='1406') OR (MENU_ID='110106' AND PERMS='sys:user:assign' AND PARENT_ID='1101');
    COMMIT;
END$$
DELIMITER ;
CALL cc_rollback_admin_jobs();
DROP PROCEDURE cc_rollback_admin_jobs;
