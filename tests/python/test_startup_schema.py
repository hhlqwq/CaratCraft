"""以真实Bash核对完整初始化就绪边界, 不连接Docker或业务数据库."""

import os
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]


class StartupSchemaTest(unittest.TestCase):
    """当前、未完成及未知结构采用明确结果, 不猜测历史格式."""

    def test_bash_checks_complete_schema_version(self):
        """未完成及未知版本均停止, 不触发恢复动作."""
        bash = os.environ.get("CARATCRAFT_BASH")
        if not bash:
            self.skipTest("需要已有CARATCRAFT_BASH, 不自动下载")
        command = r'''ROOT_DIR="$PWD"; eval "$(sed '/^ROOT_DIR=/d; /^if \[\[ \$# -gt 0/,$d' deploy/scripts/dev.sh)"
database_query() { printf '%s\n' "$CARATCRAFT_SCHEMA_VERSION"; }
assert_database_schema
'''
        for value, expected in [("1", 0), ("", 1), ("0", 1), ("2", 1), ("1\n1", 1), ("unknown", 1)]:
            with self.subTest(value=value):
                result = subprocess.run([bash, "-c", command], cwd=ROOT,
                    env=dict(os.environ, CARATCRAFT_SCHEMA_VERSION=value),
                    capture_output=True, text=True, encoding="utf-8")
                self.assertEqual(expected, result.returncode, result.stdout + result.stderr)

    def test_first_start_preserves_existing_or_incomplete_resources(self):
        """以Docker元数据替身检查首次、已有和残缺状态, 不连接真实数据卷."""
        bash = os.environ.get("CARATCRAFT_BASH")
        if not bash:
            self.skipTest("需要已有CARATCRAFT_BASH, 不自动下载")
        volumes = "\n".join(f"platform-wxshop_{kind}-data" for kind in ("mysql", "redis", "upload"))
        cases = [
            ("fresh", "", False, "", "", "", 0, "true"),
            ("prepared-retry", "", True, "", "", "", 0, "true"),
            ("existing", volumes, True, "", "", "", 0, "false"),
            ("partial", "platform-wxshop_mysql-data", True, "", "", "", 1, "不完整"),
            ("lost-config", volumes, False, "", "", "", 1, "配置丢失"),
            ("foreign", volumes, True, "other", "", "", 1, "归属异常"),
            ("leftover", "", False, "", "platform-admin", "", 1, "残留容器"),
            ("unfinished", "", False, "", "", ".initializing", 1, "未完成"),
            ("lost-volumes", "", True, "", "", ".initialized", 1, "持久卷缺失"),
            ("old-config-only", "", True, "", "", "", 1, "已有配置但无持久卷"),
        ]
        command = r'''ROOT_DIR="$PWD"; eval "$(sed '/^ROOT_DIR=/d; /^if \[\[ \$# -gt 0/,$d' deploy/scripts/dev.sh)"
ROOT_DIR="$TEST_ROOT"
ENV_FILE="$ROOT_DIR/deploy/.env"
# 只提供查询响应, 任何写入型Docker调用均使测试失败.
docker() {
    case "$*" in
        'volume ls --format {{.Name}}') printf '%s' "$TEST_VOLUMES" ;;
        'container ls -a --format {{.Names}}') printf '%s' "$TEST_CONTAINERS" ;;
        'container ls -aq --filter label=com.docker.compose.project=platform-wxshop') ;;
        'volume inspect '*) printf '%s' "${TEST_OWNER:-platform-wxshop}" ;;
        *) return 99 ;;
    esac
}
initialize_environment() { printf 'configured\n'; }
prepare_local_start
printf '%s\n' "$FRESH_ENVIRONMENT"
'''
        for name, present, config, owner, containers, marker, expected, message in cases:
            with self.subTest(name=name), tempfile.TemporaryDirectory(
                dir=os.environ.get("CARATCRAFT_TEST_TEMP")
            ) as temporary:
                deploy = Path(temporary) / "deploy"
                deploy.mkdir()
                if config:
                    (deploy / ".env").write_text("preserved", encoding="utf-8")
                if marker:
                    (deploy / marker).mkdir()
                if name == "prepared-retry":
                    (deploy / "secrets").mkdir()
                    for filename in ("platform-bootstrap.txt", "platform-bootstrap.runtime.txt"):
                        (deploy / "secrets" / filename).write_text("prepared", encoding="utf-8")
                result = subprocess.run([bash, "-c", command], cwd=ROOT,
                    env=dict(os.environ, TEST_ROOT=temporary, TEST_VOLUMES=present,
                        TEST_OWNER=owner, TEST_CONTAINERS=containers),
                    capture_output=True, text=True, encoding="utf-8")
                self.assertEqual(expected, result.returncode, result.stdout + result.stderr)
                self.assertIn(message, result.stdout + result.stderr)
                if config:
                    self.assertEqual("preserved", (deploy / ".env").read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
