SET NAMES utf8mb4;

-- 每个条目必须独立给出检查结果, 不继承上一条的状态.
SET @cc_compatible = NULL, @cc_complete = NULL;
SELECT '__ID__: checksum';
CREATE TEMPORARY TABLE cc_checksum_check (ok TINYINT NOT NULL CHECK (ok = 1));
INSERT INTO cc_checksum_check
SELECT NOT EXISTS(
    SELECT 1 FROM CC_SCHEMA_MIGRATION
    WHERE migration_id = '__ID__' AND checksum <> '__SHA256__'
);
DROP TEMPORARY TABLE cc_checksum_check;

SET @cc_recorded = (SELECT EXISTS(
    SELECT 1 FROM CC_SCHEMA_MIGRATION WHERE migration_id = '__ID__' AND applied_at IS NOT NULL
));
