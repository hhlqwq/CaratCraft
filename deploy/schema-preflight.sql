-- 只读检查启动所需的增量字段, 不更改版本或数据.
SELECT CONCAT(needed.table_name, '.', needed.column_name)
FROM (
    SELECT 'sys_user' AS table_name, 'account_type' AS column_name
    UNION ALL SELECT 'sys_user', 'temporary_password_expires_at'
    UNION ALL SELECT 'sys_user', 'password_change_required'
    UNION ALL SELECT 'sys_user', 'last_login_at'
    UNION ALL SELECT 'sys_user', 'org_no'
    UNION ALL SELECT 'sys_role', 'direct_user_id'
    UNION ALL SELECT 'sys_role', 'delegable'
    UNION ALL SELECT 'sys_log', 'scope_org_no'
    UNION ALL SELECT 'mall_goods', 'scope_org_no'
    UNION ALL SELECT 'mall_order', 'scope_org_no'
    UNION ALL SELECT 'mall_feedback', 'scope_org_no'
    UNION ALL SELECT 'mall_feedback', 'staff_reply'
    UNION ALL SELECT 'cc_community_post', 'scope_org_no'
    UNION ALL SELECT 'cc_community_post', 'staff_deleted'
) needed
LEFT JOIN information_schema.COLUMNS present
    ON present.TABLE_SCHEMA = DATABASE()
    AND LOWER(present.TABLE_NAME) = needed.table_name
    AND LOWER(present.COLUMN_NAME) = needed.column_name
WHERE present.COLUMN_NAME IS NULL
ORDER BY needed.table_name, needed.column_name;
