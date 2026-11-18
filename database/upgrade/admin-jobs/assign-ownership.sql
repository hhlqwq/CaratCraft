SET NAMES utf8mb4;
-- 仅由有权负责人填写已核对的精确行映射. 默认空表, 不把未知归属推断为全店.
DROP TEMPORARY TABLE IF EXISTS cc_approved_business_owner;
CREATE TEMPORARY TABLE cc_approved_business_owner (
    resource_kind varchar(16) NOT NULL,
    resource_id bigint unsigned NOT NULL,
    org_no varchar(10) NOT NULL,
    PRIMARY KEY (resource_kind,resource_id)
);

-- 在此写入已经确认的 GOODS、ORDER、FEEDBACK、COMMUNITY 记录与机构编号.
-- INSERT INTO cc_approved_business_owner VALUES ('GOODS',实际记录编号,'已核对机构编号');

DELIMITER $$
DROP PROCEDURE IF EXISTS cc_set_approved_business_owner$$
CREATE PROCEDURE cc_set_approved_business_owner()
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    IF EXISTS(
        SELECT 1 FROM cc_approved_business_owner m
        LEFT JOIN SYS_ORG o ON CAST(o.ORG_NO AS BINARY)=CAST(m.org_no AS BINARY) AND o.STATUS=1
        LEFT JOIN mall_goods g ON m.resource_kind='GOODS' AND g.id=m.resource_id
        LEFT JOIN mall_order t ON m.resource_kind='ORDER' AND t.id=m.resource_id
        LEFT JOIN mall_feedback f ON m.resource_kind='FEEDBACK' AND f.msg_id=m.resource_id
        LEFT JOIN CC_COMMUNITY_POST c ON m.resource_kind='COMMUNITY' AND c.id=m.resource_id
        WHERE o.ORG_NO IS NULL OR COALESCE(g.id,t.id,f.msg_id,c.id) IS NULL
            OR COALESCE(CAST(g.scope_org_no AS BINARY),CAST(t.scope_org_no AS BINARY),
                        CAST(f.scope_org_no AS BINARY),CAST(c.scope_org_no AS BINARY),
                        CAST(m.org_no AS BINARY))<>CAST(o.ORG_NO AS BINARY)
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='归属映射存在无效编号、停用机构或历史冲突';
    END IF;
    UPDATE mall_goods g JOIN cc_approved_business_owner m ON m.resource_kind='GOODS' AND g.id=m.resource_id
        SET g.scope_org_no=m.org_no WHERE g.scope_org_no IS NULL;
    UPDATE mall_order t JOIN cc_approved_business_owner m ON m.resource_kind='ORDER' AND t.id=m.resource_id
        SET t.scope_org_no=m.org_no WHERE t.scope_org_no IS NULL;
    UPDATE mall_feedback f JOIN cc_approved_business_owner m ON m.resource_kind='FEEDBACK' AND f.msg_id=m.resource_id
        SET f.scope_org_no=m.org_no WHERE f.scope_org_no IS NULL;
    UPDATE CC_COMMUNITY_POST c JOIN cc_approved_business_owner m ON m.resource_kind='COMMUNITY' AND c.id=m.resource_id
        SET c.scope_org_no=m.org_no WHERE c.scope_org_no IS NULL;
    COMMIT;
END$$
DELIMITER ;
CALL cc_set_approved_business_owner();
DROP PROCEDURE cc_set_approved_business_owner;
DROP TEMPORARY TABLE cc_approved_business_owner;
