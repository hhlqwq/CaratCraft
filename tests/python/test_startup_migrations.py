"""用真实Bash执行迁移就绪检查, 不连接Docker或业务库."""

import hashlib
import os
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]


class StartupMigrationTest(unittest.TestCase):
    """覆盖完整台账、任意缺版本和不可自动恢复的冲突."""

    def test_bash_checks_every_version_and_sha(self):
        """未来迁移自动进入门禁, 缺版本返回42而冲突返回1."""
        bash = os.environ.get("CARATCRAFT_BASH")
        if not bash:
            self.skipTest("需要已有CARATCRAFT_BASH, 不自动下载")
        temporary = Path("D:/tmp/hailongcodex" if os.name == "nt" else "/tmp/hailongcodex")
        temporary.mkdir(parents=True, exist_ok=True)
        with tempfile.TemporaryDirectory(prefix="startup-migrations-", dir=temporary) as directory:
            fixture = Path(directory)
            migrations = fixture / "database/migrations"
            migrations.mkdir(parents=True)
            rows = []
            for name in ("V20261022_01_sku_contract.sql", "V20261024_01_material_catalog.sql",
                         "V20990101_01_future.sql"):
                content = ("-- synthetic " + name + "\n").encode()
                (migrations / name).write_bytes(content)
                rows.append(name + "\t" + hashlib.sha256(content).hexdigest())
            cases = {
                "ready": ("\n".join(rows), 0),
                "missing_old": ("\n".join(rows[1:]), 42),
                "missing_material": ("\n".join([rows[0], rows[2]]), 42),
                "missing_future": ("\n".join(rows[:2]), 42),
                "empty": ("", 42),
                "conflict": ("\n".join([rows[0].split("\t")[0] + "\t" + "0" * 64, *rows[1:]]), 1),
                "unknown": ("\n".join([*rows, "V20990102_01_unknown.sql\t" + "0" * 64]), 1),
                "duplicate": ("\n".join([*rows, rows[0]]), 1),
            }
            command = r'''source <(sed '/^if \[\[ \$# -gt 0/,$d' deploy/scripts/dev.sh)
PROJECT_DIR="$CARATCRAFT_MIGRATION_FIXTURE"
database_query() { printf '%s\n' "$CARATCRAFT_MIGRATION_ROWS"; }
assert_database_migrations
'''
            for name, (records, expected) in cases.items():
                with self.subTest(name=name):
                    environment = dict(os.environ, CARATCRAFT_MIGRATION_FIXTURE=fixture.as_posix(),
                                       CARATCRAFT_MIGRATION_ROWS=records)
                    result = subprocess.run([bash, "-c", command], cwd=ROOT, env=environment,
                                            capture_output=True, text=True, encoding="utf-8")
                    self.assertEqual(expected, result.returncode, result.stdout + result.stderr)


if __name__ == "__main__":
    unittest.main()
