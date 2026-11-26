SET NAMES utf8mb4;

-- 加锁后, 先核对所有已登记指纹及完成状态, 再执行任何待运行的增量.
SELECT '__ID__: checksum and completion';
CREATE TEMPORARY TABLE cc_history_check (ok TINYINT NOT NULL CHECK (ok = 1));
INSERT INTO cc_history_check
SELECT NOT EXISTS(
    SELECT 1 FROM CC_SCHEMA_MIGRATION
    WHERE migration_id = '__ID__' AND checksum <> '__SHA256__'
) AND __COMPLETED__ = EXISTS(
    SELECT 1 FROM CC_SCHEMA_MIGRATION
    WHERE migration_id = '__ID__' AND applied_at IS NOT NULL
);
DROP TEMPORARY TABLE cc_history_check;
