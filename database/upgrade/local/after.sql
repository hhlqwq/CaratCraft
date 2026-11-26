SET NAMES utf8mb4;

SELECT '__ID__: postcheck';
CREATE TEMPORARY TABLE cc_after_check (ok TINYINT NOT NULL CHECK (ok = 1));
INSERT INTO cc_after_check VALUES (@cc_compatible AND @cc_complete);
DROP TEMPORARY TABLE cc_after_check;

UPDATE CC_SCHEMA_MIGRATION SET applied_at = CURRENT_TIMESTAMP
WHERE migration_id = '__ID__' AND applied_at IS NULL;
