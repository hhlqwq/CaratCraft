"""离线比较两平台真实摘要实现, 不连接Docker或读取私人配置."""

import hashlib
from datetime import datetime
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]


class BuildRevisionTest(unittest.TestCase):
    """以真实源码和独立计算验证两个入口执行同一摘要契约."""

    def test_platform_revisions_match_and_follow_build_inputs(self):
        """两平台使用同一清单, 只有该镜像的输入变更时更新摘要."""
        powershell = shutil.which("pwsh") or shutil.which("powershell")
        bash = os.environ.get("CARATCRAFT_BASH")
        if not powershell or not bash:
            self.skipTest("Requires PowerShell and CARATCRAFT_BASH")
        inputs = (ROOT / "deploy/build-inputs.txt").read_text("utf-8")
        self.assertIn("deploy/build-inputs.txt text eol=lf", (ROOT / ".gitattributes").read_text("utf-8"))
        scopes = {"backend": [], "admin-ui": []}
        parent = Path(os.environ.get("CARATCRAFT_TEST_TEMP", "D:/tmp/hailongcodex")) / datetime.now().strftime("%Y-%m-%d")
        parent.mkdir(parents=True, exist_ok=True)
        with tempfile.TemporaryDirectory(prefix="image-inputs-", dir=parent) as directory:
            fixture = Path(directory)
            for line in inputs.splitlines():
                scope, relative = line.split(" ", 1)
                if (ROOT / relative).is_dir():
                    relative += "/fixture.txt"
                path = fixture / relative
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_bytes(b"public input")
                scopes[scope].append(relative)
            (fixture / "deploy/build-inputs.txt").write_text(inputs, encoding="utf-8", newline="\n")

            def revisions():
                """对照独立摘要算法及两端真实脚本."""
                expected = []
                for names in scopes.values():
                    manifest = "".join(f"{name}:{hashlib.sha256((fixture / name).read_bytes()).hexdigest().upper()}\n"
                                       for name in sorted(names))
                    expected.append(hashlib.sha256(manifest.encode()).hexdigest())
                env = dict(os.environ, CARATCRAFT_REVISION_FIXTURE=str(fixture),
                           CARATCRAFT_REVISION_SOURCE=str(ROOT / "deploy/scripts/dev.ps1"))
                ps = r"""
$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [Text.UTF8Encoding]::new($false)
$ProjectRoot = $env:CARATCRAFT_REVISION_FIXTURE
$WorkspaceRoot = $ProjectRoot
$EnvExample = Join-Path $ProjectRoot 'deploy/.env.example'
$Tokens = $null; $Errors = $null
$Ast = [Management.Automation.Language.Parser]::ParseFile($env:CARATCRAFT_REVISION_SOURCE, [ref]$Tokens, [ref]$Errors)
foreach ($Definition in $Ast.FindAll({ param($Node) $Node -is [Management.Automation.Language.FunctionDefinitionAst] }, $false)) {
    . ([scriptblock]::Create($Definition.Extent.Text))
}
Set-BuildRevisions
Write-Output "RESULT=$env:BACKEND_BUILD_REVISION,$env:ADMIN_UI_BUILD_REVISION"
"""
                result = subprocess.run([powershell, "-NoProfile", "-Command", ps], env=env, check=True,
                                        capture_output=True, text=True, encoding="utf-8")
                self.assertEqual(expected, result.stdout.splitlines()[-1].removeprefix("RESULT=").split(","))
                shell = r"""
ROOT_DIR="$PWD"; eval "$(sed '/^ROOT_DIR=/d; /^if \[\[ \$# -gt 0/,$d' deploy/scripts/dev.sh)"
ROOT_DIR="$(cd "$1" && pwd)"
ENV_EXAMPLE="$ROOT_DIR/deploy/.env.example"
set_build_revisions
printf '%s\n' "$BACKEND_BUILD_REVISION" "$ADMIN_UI_BUILD_REVISION"
"""
                result = subprocess.run([bash, "-c", shell, "fixture", fixture.as_posix()], check=True,
                                        cwd=ROOT, capture_output=True, text=True, encoding="utf-8")
                self.assertEqual(expected, result.stdout.splitlines())
                return expected

            before = revisions()
            for relative in ("README.md", "AGENTS.md", "deploy/.env", ".git/config",
                             "apps/miniapp/pages/demo.js", "apps/miniapp/pages/demo.wxml", "apps/miniapp/pages/demo.wxss",
                             "apps/miniapp/project.config.json", "apps/admin-ui/.env.local",
                             "backend/platform-admin/src/main/application-local.yml", "backend/platform-admin/src/test/ignored.java", "apps/admin-ui/README.md"):
                local = fixture / relative
                local.parent.mkdir(parents=True, exist_ok=True)
                local.write_bytes(b"excluded change")
            self.assertEqual(before, revisions())
            (fixture / "apps/admin-ui/src/fixture.txt").write_bytes(b"Vue change")
            vue = revisions()
            self.assertEqual(before[0], vue[0])
            self.assertNotEqual(before[1], vue[1])
            (fixture / "backend/platform-admin/src/main/fixture.txt").write_bytes(b"Java change")
            java = revisions()
            self.assertEqual(vue[1], java[1])
            self.assertNotEqual(vue[0], java[0])
            (fixture / "apps/miniapp/static/images/fixture.txt").write_bytes(b"shared image change")
            shared = revisions()
            self.assertEqual(java[0], shared[0])
            self.assertNotEqual(java[1], shared[1])

    def test_bash_no_build_checks_images_and_stops_on_failure(self):
        """显式禁止构建时只检查镜像, 镜像失配不再启动服务."""
        bash = os.environ.get("CARATCRAFT_BASH")
        if not bash:
            self.skipTest("需要CARATCRAFT_BASH")
        prefix = r'''ROOT_DIR="$PWD"; eval "$(sed '/^ROOT_DIR=/d; /^if \[\[ \$# -gt 0/,$d' deploy/scripts/dev.sh)"; '''
        functions = r'''compose() { printf '%s\n' "$*"; }
assert_local_images() { return 0; }
assert_data_volumes() { return 0; }
NO_BUILD=true
BACKEND_BUILD_REVISION=java
ADMIN_UI_BUILD_REVISION=vue
docker() {
    case "$1 $2 ${!#}" in
        'image inspect carat-craft-backend:local'|'image inspect carat-craft-admin-ui:local') echo 'image-id' ;;
        'container inspect platform-nginx') echo 'image-id vue healthy' ;;
        'container inspect platform-admin'|'container inspect platform-api') echo 'image-id java healthy' ;;
        'exec platform-nginx '*)
            [[ "${!#}" == *'http://127.0.0.1/platform-framework-api/'* &&
                "${!#}" != *'/app/'* && "${!#}" != *'/internal/'* ]] ;;
        *) return 1 ;;
    esac
}
assert_database_ready() { return 0; }
apply_automatic_migrations() { echo migration; }
show_access_info() { echo ready; }
# 隔离状态标记创建, 不写入真实工作区.
mkdir() { return 0; }
start_local_environment
'''
        result = subprocess.run([bash, "-c", prefix + functions], cwd=ROOT,
                                capture_output=True, text=True, encoding="utf-8", check=True)
        self.assertEqual(result.stdout.strip().splitlines(), [
            "up --detach --no-recreate --no-build --pull never --wait --wait-timeout 300 mysql redis",
            "migration",
            "up --detach --no-deps --no-build --pull never --wait --wait-timeout 300 platform-admin platform-api nginx",
            "platform-admin：镜像一致，健康",
            "platform-api：镜像一致，健康",
            "platform-nginx：镜像一致，健康",
            "代理验证通过：管理台、后台与 API 根路径。",
            "ready"])
        functions += r'''assert_local_images() { return 1; }
show_startup_diagnostics() { echo failed; }
start_local_environment
'''
        result = subprocess.run([bash, "-c", prefix + functions], cwd=ROOT,
                                capture_output=True, text=True, encoding="utf-8")
        self.assertNotEqual(result.returncode, 0)
        self.assertEqual(result.stdout.strip().splitlines()[-1], "ready")
        self.assertNotIn("failed", result.stdout)

    def test_bash_incompatible_database_preserves_volumes(self):
        """旧库结构失配时报错, 保留数据且不启动应用."""
        bash = os.environ.get("CARATCRAFT_BASH")
        if not bash:
            self.skipTest("Requires CARATCRAFT_BASH")
        command = r"""
ROOT_DIR="$PWD"; eval "$(sed '/^ROOT_DIR=/d; /^if \[\[ \$# -gt 0/,$d' deploy/scripts/dev.sh)"
ROOT_DIR="$PWD"
compose() { printf '%s\n' "$*"; }
assert_local_images() { return 0; }
assert_data_volumes() { return 0; }
reset_test_data_volumes() { echo unexpected-reset; }
NO_BUILD=true
database_query() {
    case "$1" in
        'SELECT TABLE_NAME'*) printf '%s\n' sys_region mall_order mall_user mall_goods CC_SCHEMA_VERSION ;;
        'SELECT version'*) echo 1 ;;
        *'SELECT CONCAT'*) echo sys_user.account_type ;;
        *) return 1 ;;
    esac
}
show_startup_diagnostics() { echo diagnostics; }
start_local_environment
"""
        result = subprocess.run([bash, "-c", command], cwd=ROOT, capture_output=True, text=True, encoding="utf-8")
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("sys_user.account_type", result.stderr)
        self.assertEqual(result.stdout.splitlines(), [
            "up --detach --no-recreate --no-build --pull never --wait --wait-timeout 300 mysql redis", "diagnostics"])


if __name__ == "__main__":
    unittest.main()
