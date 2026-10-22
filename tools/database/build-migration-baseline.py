"""生成新建空库的版本迁移入口及校验台账, 已有库使用SchemaMigrationTool."""

import hashlib
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2] / "database"


def main():
    """按版本顺序内联SQL及成功后的校验记录, 不执行数据库命令."""
    files = sorted(path for path in (ROOT / "migrations").glob("V*.sql")
                   if re.fullmatch(r"V[0-9]{8}_[0-9]+_[a-z0-9_]+\.sql", path.name))
    lines = ["-- 由tools/database/build-migration-baseline.py生成, 只用于新建空库初始化.",
             "CREATE TABLE IF NOT EXISTS CC_SCHEMA_MIGRATION (",
             "  version VARCHAR(191) PRIMARY KEY, sha256 CHAR(64) NOT NULL,",
             "  applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP",
             ") ENGINE=InnoDB;", ""]
    for path in files:
        checksum = hashlib.sha256(path.read_text("utf-8").replace("\r\n", "\n").encode("utf-8")).hexdigest()
        lines.extend([f"-- 版本: {path.name}", path.read_text("utf-8").replace("\r\n", "\n").strip(),
                      "INSERT IGNORE INTO CC_SCHEMA_MIGRATION(version,sha256)",
                      f"VALUES ('{path.name}', '{checksum}');", ""])
    (ROOT / "init/130_schema_versions.sql").write_text("\n".join(lines).rstrip() + "\n", "utf-8", newline="\n")
    print(f"新建库迁移入口已同步: {len(files)}个版本")


if __name__ == "__main__":
    main()
