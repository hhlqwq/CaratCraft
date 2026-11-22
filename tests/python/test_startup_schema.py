"""以真实Bash核对完整初始化就绪边界, 不连接Docker或业务数据库."""

import os
from pathlib import Path
import subprocess
import unittest

ROOT = Path(__file__).resolve().parents[2]


class StartupSchemaTest(unittest.TestCase):
    """当前、未完成及未知结构采用明确结果, 不猜测历史格式."""

    def test_bash_checks_complete_schema_version(self):
        """未完成及未知版本均停止, 不触发恢复动作."""
        bash = os.environ.get("CARATCRAFT_BASH")
        if not bash:
            self.skipTest("需要已有CARATCRAFT_BASH, 不自动下载")
        command = r'''source <(sed '/^if \[\[ \$# -gt 0/,$d' deploy/scripts/dev.sh)
database_query() { printf '%s\n' "$CARATCRAFT_SCHEMA_VERSION"; }
assert_database_schema
'''
        for value, expected in [("1", 0), ("", 1), ("0", 1), ("2", 1), ("1\n1", 1), ("unknown", 1)]:
            with self.subTest(value=value):
                result = subprocess.run([bash, "-c", command], cwd=ROOT,
                    env=dict(os.environ, CARATCRAFT_SCHEMA_VERSION=value),
                    capture_output=True, text=True, encoding="utf-8")
                self.assertEqual(expected, result.returncode, result.stdout + result.stderr)


if __name__ == "__main__":
    unittest.main()
