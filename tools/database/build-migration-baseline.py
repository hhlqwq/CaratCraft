"""生成新建空库的版本迁移入口及校验台账, 已有库使用SchemaMigrationTool."""

import argparse
import hashlib
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2] / "database"


def main():
    """按版本顺序内联SQL及成功后的校验记录, 不执行数据库命令."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="只验证生成一致性, 不写文件")
    args = parser.parse_args()
    files = sorted(path for path in (ROOT / "migrations").glob("V*.sql")
                   if re.fullmatch(r"V[0-9]{8}_[0-9]+_[a-z0-9_]+\.sql", path.name))
    lines = ["-- 由tools/database/build-migration-baseline.py生成, 只用于新建空库初始化.",
             "SET NAMES utf8mb4;", "",
             "CREATE TABLE IF NOT EXISTS CC_SCHEMA_MIGRATION (",
             "  version VARCHAR(191) PRIMARY KEY, sha256 CHAR(64) NOT NULL,",
             "  applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP",
             ") ENGINE=InnoDB;", ""]
    for path in files:
        checksum = hashlib.sha256(path.read_text("utf-8").replace("\r\n", "\n").encode("utf-8")).hexdigest()
        lines.extend([f"-- 版本: {path.name}", path.read_text("utf-8").replace("\r\n", "\n").strip(),
                      "INSERT IGNORE INTO CC_SCHEMA_MIGRATION(version,sha256)",
                      f"VALUES ('{path.name}', '{checksum}');", ""])
    target = ROOT / "init/130_schema_versions.sql"
    expected = "\n".join(lines).rstrip() + "\n"
    if args.check:
        if target.read_text("utf-8") != expected:
            raise ValueError("新库版本入口与V迁移不一致, 请运行生成器同步")
    else:
        target.write_text(expected, "utf-8", newline="\n")
    print(f"新建库迁移入口已同步: {len(files)}个版本")


if __name__ == "__main__":
    main()
