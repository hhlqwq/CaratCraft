SET NAMES utf8mb4;

SELECT '__ID__: precheck';
CREATE TEMPORARY TABLE cc_before_check (ok TINYINT NOT NULL CHECK (ok = 1));
INSERT INTO cc_before_check VALUES (@cc_compatible AND @cc_complete IN (0, 1));
DROP TEMPORARY TABLE cc_before_check;

-- 先登记指纹. 若DDL间中断, applied_at仍为空, 下次按实际结构继续.
INSERT INTO CC_SCHEMA_MIGRATION (migration_id, checksum)
SELECT '__ID__', '__SHA256__'
WHERE NOT EXISTS (SELECT 1 FROM CC_SCHEMA_MIGRATION WHERE migration_id = '__ID__');
SET @cc_need_apply = NOT @cc_complete;
SELECT CONCAT('__ID__: ', IF(@cc_need_apply, 'applying', 'baseline'));

SET @cc_compatible = NULL, @cc_complete = NULL;
