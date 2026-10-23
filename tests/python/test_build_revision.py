"""离线比较两平台真实摘要实现, 不连接Docker或读取私人配置."""

import hashlib
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]


class BuildRevisionTest(unittest.TestCase):
    """以真实源码和独立计算验证两个入口执行同一摘要契约."""

    def test_operational_scripts_do_not_change_image_revision(self):
        """实际摘要忽略宿主机脚本, 仍对后端、Dockerfile和SQL变更敏感."""
        powershell = shutil.which("pwsh") or shutil.which("powershell")
        if not powershell:
            self.skipTest("需要已有PowerShell以执行真实摘要函数")
        roots = (ROOT / "deploy/build-inputs.txt").read_text("utf-8").splitlines()
        self.assertIn("deploy/build-inputs.txt text eol=lf",
                      (ROOT / ".gitattributes").read_text("utf-8"))
        self.assertFalse(any(Path("deploy/scripts/dev.ps1").is_relative_to(Path(root))
                             for root in roots), "宿主机操作脚本不能成为镜像构建输入")
        temporary = Path("D:/tmp/hailongcodex/2026-10-23")
        temporary.mkdir(parents=True, exist_ok=True)
        with tempfile.TemporaryDirectory(prefix="image-inputs-", dir=temporary) as directory:
            fixture = Path(directory)
            for relative in roots:
                path = fixture / relative
                if (ROOT / relative).is_dir():
                    path = path / "fixture.txt"
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text("public build input\n", "utf-8")
            (fixture / "deploy/build-inputs.txt").write_text("\n".join(roots) + "\n", "utf-8")
            script = fixture / "deploy/scripts/dev.ps1"
            script.parent.mkdir(parents=True, exist_ok=True)
            script.write_text("host operation\n", "utf-8")
            command = r'''$ErrorActionPreference = 'Stop'
$WorkspaceRoot = $env:CARATCRAFT_REVISION_FIXTURE
$ProjectRoot = $WorkspaceRoot
$EnvExample = Join-Path $ProjectRoot 'deploy/.env.example'
$Tokens = $null; $Errors = $null
$Ast = [Management.Automation.Language.Parser]::ParseFile($env:CARATCRAFT_REVISION_SOURCE, [ref]$Tokens, [ref]$Errors)
foreach ($Definition in $Ast.FindAll({ param($Node) $Node -is [Management.Automation.Language.FunctionDefinitionAst] }, $false)) {
    . ([scriptblock]::Create($Definition.Extent.Text))
}
Set-BuildRevision
$Results = @($env:BUILD_REVISION)
foreach ($Relative in @('deploy/scripts/dev.ps1', 'backend/fixture.txt', 'deploy/Dockerfile.backend', 'database/fixture.txt')) {
    [IO.File]::AppendAllText((Join-Path $ProjectRoot $Relative), 'changed')
    Set-BuildRevision
    $Results += $env:BUILD_REVISION
}
Write-Output ('RESULT=' + ($Results -join ','))
'''
            environment = dict(os.environ, CARATCRAFT_REVISION_FIXTURE=str(fixture),
                               CARATCRAFT_REVISION_SOURCE=str(ROOT / "deploy/scripts/dev.ps1"))
            result = subprocess.run([powershell, "-NoProfile", "-Command", command],
                                    capture_output=True, text=True, encoding="utf-8",
                                    env=environment, check=True)
            revisions = result.stdout.strip().splitlines()[-1].removeprefix("RESULT=").split(",")
            self.assertEqual(len(revisions), 5)
            self.assertEqual(revisions[0], revisions[1])
            for before, after in zip(revisions[1:], revisions[2:]):
                self.assertNotEqual(before, after)

    def test_platform_revisions_match_independent_manifest(self):
        """两个脚本的摘要须与独立清单算法一致, 包括Unicode路径和LF分隔."""
        bash = os.environ.get("CARATCRAFT_BASH")
        powershell = shutil.which("pwsh")
        if not bash or not powershell:
            self.skipTest("需要CARATCRAFT_BASH和pwsh以执行两平台摘要对照")
        roots = (ROOT / "deploy/build-inputs.txt").read_text("utf-8").splitlines()
        excluded = {"node_modules", "target", "dist", ".git", ".idea", ".vite", ".cache", "coverage"}
        files = {}
        for relative in roots:
            item = ROOT / relative
            for file in item.rglob("*") if item.is_dir() else [item]:
                if not file.is_file() or excluded.intersection(file.relative_to(ROOT).parts):
                    continue
                name = file.name
                if (name.startswith("application-local") or name == ".env" or name.startswith(".env.")
                        or file.suffix in {".log", ".tmp", ".key", ".pem", ".p12"}):
                    if file != ROOT / "deploy/.env.example":
                        continue
                files[file.relative_to(ROOT).as_posix()] = hashlib.sha256(file.read_bytes()).hexdigest().upper()
        manifest = "".join(f"{name}:{files[name]}\n" for name in sorted(files))
        expected = hashlib.sha256(manifest.encode("utf-8")).hexdigest()
        command = r'''$ErrorActionPreference = 'Stop'
$ProjectRoot = (Get-Location).Path
$WorkspaceRoot = $ProjectRoot
$EnvExample = Join-Path $ProjectRoot 'deploy/.env.example'
$Tokens = $null; $Errors = $null
$Ast = [Management.Automation.Language.Parser]::ParseFile((Join-Path $ProjectRoot 'deploy/scripts/dev.ps1'), [ref]$Tokens, [ref]$Errors)
foreach ($Definition in $Ast.FindAll({ param($Node) $Node -is [Management.Automation.Language.FunctionDefinitionAst] }, $false)) {
    . ([scriptblock]::Create($Definition.Extent.Text))
}
Set-BuildRevision
Write-Output $env:BUILD_REVISION
'''
        result = subprocess.run([powershell, "-NoProfile", "-Command", command], cwd=ROOT,
                                capture_output=True, text=True, encoding="utf-8", check=True)
        self.assertEqual(expected, result.stdout.strip().splitlines()[-1])
        command = 'source <(sed \'/^if \\[\\[ \\$# -gt 0/,$d\' deploy/scripts/dev.sh); ROOT_DIR="$PWD"; ENV_EXAMPLE="$ROOT_DIR/deploy/.env.example"; set_build_revision; printf "%s\\n" "$BUILD_REVISION"'
        result = subprocess.run([bash, "-c", command], cwd=ROOT, capture_output=True,
                                text=True, encoding="utf-8", check=True)
        self.assertEqual(expected, result.stdout.strip())

    def test_bash_start_never_builds_and_failure_stops_next_stage(self):
        """Bash启动只使用本地镜像, 镜像检查失败后不再启动服务."""
        bash = os.environ.get("CARATCRAFT_BASH")
        if not bash:
            self.skipTest("需要CARATCRAFT_BASH")
        prefix = "source <(sed '/^if \\[\\[ \\$# -gt 0/,$d' deploy/scripts/dev.sh); "
        functions = r'''compose() { printf '%s\n' "$*"; }
assert_local_images() { return 0; }
assert_database_ready() { return 0; }
show_access_info() { echo ready; }
start_local_environment
'''
        result = subprocess.run([bash, "-c", prefix + functions], cwd=ROOT,
                                capture_output=True, text=True, encoding="utf-8", check=True)
        self.assertEqual(result.stdout.strip().splitlines(), [
            "up --detach --no-build --pull never --wait --wait-timeout 300 mysql redis",
            "up --detach --no-build --pull never --wait --wait-timeout 300", "ready"])
        functions += r'''assert_local_images() { return 1; }
show_startup_diagnostics() { echo failed; }
start_local_environment
'''
        result = subprocess.run([bash, "-c", prefix + functions], cwd=ROOT,
                                capture_output=True, text=True, encoding="utf-8")
        self.assertNotEqual(result.returncode, 0)
        self.assertEqual(result.stdout.strip().splitlines()[-1], "failed")


if __name__ == "__main__":
    unittest.main()
