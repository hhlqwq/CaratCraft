"""验证PowerShell与Bash迁移指纹和隔离MySQL的结构演进."""

from datetime import date
import hashlib
import importlib.util
import os
import re
from pathlib import Path
import secrets
import shutil
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location("isolated", ROOT / "tests/integration/run-community-regressions.py")
ISOLATED = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(ISOLATED)


def bash_function(bash, root, function, *args, content=None, check=True):
    """只加载生产Bash函数, 不执行宿主启动入口."""
    source = r"""
source <(sed '/^if \[\[ \$# -gt 0/,$d' deploy/scripts/dev.sh)
ROOT_DIR="$(cd "$1" && pwd)"
shift
"$@"
"""
    return subprocess.run([bash, "-c", source, "fixture", root.as_posix(), function, *args],
                          cwd=ROOT, input=content, capture_output=True, encoding="utf-8",
                          timeout=30, check=check)


def migration_batch(root=ROOT, completed=(), bash=None, baseline=False):
    """用正式脚本按已完成的编号渲染迁移批次."""
    if bash:
        mode = "baseline" if baseline else ""
        return bash_function(bash, root, "migration_batch", "\n".join(completed), mode).stdout
    source = r"""
$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [Text.UTF8Encoding]::new($false)
$ProjectRoot = $env:CC_MIGRATION_TEST_ROOT
$ScriptPath = Join-Path (Get-Location).Path 'deploy/scripts/dev.ps1'
$Tokens = $null; $Errors = $null
$Ast = [Management.Automation.Language.Parser]::ParseFile($ScriptPath, [ref]$Tokens, [ref]$Errors)
if ($Errors.Count) { throw ($Errors | Out-String) }
$Function = $Ast.FindAll({ param($Node) $Node -is [Management.Automation.Language.FunctionDefinitionAst] -and $Node.Name -eq 'New-AutomaticMigrationBatch' }, $false)
. ([scriptblock]::Create($Function[0].Extent.Text))
New-AutomaticMigrationBatch -Completed @($env:CC_MIGRATION_TEST_COMPLETED -split ',') -Baseline:($env:CC_MIGRATION_TEST_BASELINE -eq '1')
"""
    powershell = shutil.which("powershell") or shutil.which("pwsh")
    env = dict(os.environ, CC_MIGRATION_TEST_ROOT=str(root), CC_MIGRATION_TEST_COMPLETED=",".join(completed),
               CC_MIGRATION_TEST_BASELINE=str(int(baseline)))
    result = subprocess.run([powershell, "-NoProfile", "-Command", source], cwd=ROOT, env=env,
                            capture_output=True, encoding="utf-8", timeout=30)
    if result.returncode:
        raise RuntimeError(result.stderr)
    return result.stdout


def test_cross_platform_digest(root, bash):
    """核对真实批次指纹、SQL修改和读不到文件时的失败状态."""
    def fingerprint(batch):
        """从历史保护语句中读取实际生成的迁移指纹."""
        return re.search(r"checksum <> '([0-9A-F]{64})'", batch).group(1)

    check = root / "database/upgrade/goods-questions/check.sql"
    apply = root / "database/upgrade/goods-questions/apply.sql"
    expected = hashlib.sha256("".join(hashlib.sha256(path.read_bytes()).hexdigest().upper()
                                      for path in (check, apply)).encode("ascii")).hexdigest().upper()
    ps = fingerprint(migration_batch(root))
    assert ps == fingerprint(migration_batch(root, bash=bash)) == expected
    assert bash_function(bash, root, "sha256_file", "-", content="abc").stdout.strip() == hashlib.sha256(b"abc").hexdigest().upper()
    missing = bash_function(bash, root, "sha256_file", "does-not-exist.sql", check=False)
    assert missing.returncode != 0 and not missing.stdout
    original = apply.read_bytes()
    try:
        apply.write_bytes(original + b"\n-- Fixture checksum change.\n")
        changed = fingerprint(migration_batch(root))
        assert changed != ps and changed == fingerprint(migration_batch(root, bash=bash))
    finally:
        apply.write_bytes(original)
    print("跨平台SQL指纹一致，改动SQL后同步变化，文件缺失返回非零。", flush=True)


def install_next_migration(root):
    """在隔离副本登记扩列迁移, 并更新该版本的当前结构要求."""
    upgrade = root / "database/upgrade"
    next_migration = upgrade / "test-review-width"
    next_migration.mkdir()
    (next_migration / "check.sql").write_text("""
SET NAMES utf8mb4;
SET @cc_width = (SELECT CHARACTER_MAXIMUM_LENGTH FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='mall_comment' AND COLUMN_NAME='staff_reply');
SET @cc_compatible = @cc_width IN (1000, 1500), @cc_complete = @cc_width = 1500;
""".lstrip(), encoding="utf-8", newline="\n")
    (next_migration / "apply.sql").write_text("""
SET NAMES utf8mb4;
SET @cc_test_sql = IF(@cc_need_apply, 'ALTER TABLE mall_comment MODIFY staff_reply VARCHAR(1500) NULL', 'DO 0');
PREPARE cc_test_stmt FROM @cc_test_sql;
EXECUTE cc_test_stmt;
DEALLOCATE PREPARE cc_test_stmt;
""".lstrip(), encoding="utf-8", newline="\n")
    (upgrade / "local/manifest.txt").write_text("0001 goods-questions\n0002 test-review-width\n", encoding="utf-8", newline="\n")
    contract = upgrade / "local/current-schema.sql"
    condition = "COLUMN_TYPE = 'varchar(1000)' AND IS_NULLABLE = 'YES' AND COLUMN_DEFAULT IS NULL"
    current = contract.read_text("utf-8")
    assert current.count(condition) == 1
    contract.write_text(current.replace(condition, condition.replace("1000", "1500")), encoding="utf-8", newline="\n")


def verify_mysql(source, bash):
    """在临时库验证初始化、迁移恢复、合法结构演进和失败边界."""
    # 本专项只保留待演进的历史迁移, 平台新迁移由平台HTTP隔离套件验证.
    (source / "database/upgrade/local/manifest.txt").write_text("0001 goods-questions\n", encoding="utf-8")
    snapshot = (source / "database/upgrade/local/snapshot.sql").read_text("utf-8")
    run = "migration-" + secrets.token_hex(4)
    fixtures = ISOLATED.Fixtures(run)
    env = dict(os.environ, MYSQL_ROOT_PASSWORD=secrets.token_hex(24), MYSQL_DATABASE="schema_test",
               MYSQL_USER="schema_app", MYSQL_PASSWORD=secrets.token_hex(24))
    env["MYSQL_PWD"] = env["MYSQL_ROOT_PASSWORD"]
    try:
        mysql = fixtures.start("caratcraft-" + run, ["--network", "none", "--tmpfs", "/var/lib/mysql",
            "--env", "MYSQL_ROOT_PASSWORD", "--env", "MYSQL_DATABASE", "--env", "MYSQL_USER", "--env", "MYSQL_PASSWORD",
            "--mount", f"type=bind,source={ROOT / 'database/init'},target=/caratcraft-init,readonly",
            "--entrypoint", "/bin/bash",
            "m.daocloud.io/docker.io/library/mysql:8.0.36", "-ec", 'cp /caratcraft-init/* /docker-entrypoint-initdb.d/; chmod 0644 /docker-entrypoint-initdb.d/*; chmod 0755 /docker-entrypoint-initdb.d/*.sh; exec docker-entrypoint.sh --default-authentication-plugin=mysql_native_password'], env)
        ISOLATED.wait_for_mysql(mysql, env)

        def sql(statement, error=None):
            """只在本次临时容器执行SQL, 失败必须匹配相应阶段."""
            result = subprocess.run(["docker", "exec", "-i", "--env", "MYSQL_PWD", mysql, "mysql", "-uroot",
                "--default-character-set=utf8mb4", "--batch", "--skip-column-names", env["MYSQL_DATABASE"]],
                input=statement, encoding="utf-8", capture_output=True, env=env, timeout=30)
            if error is not None:
                assert result.returncode != 0 and error in result.stdout, result.stdout + result.stderr
            else:
                assert result.returncode == 0, result.stdout + result.stderr
            return result.stdout.strip()

        def migrate(root=source, engine=None, error=None):
            """按实际迁移记录生成并运行批次."""
            completed = sql(snapshot).splitlines()
            return sql(migration_batch(root, completed, engine), error)

        counts = sql("SELECT component_type, COUNT(*) FROM CC_COMPONENT_OPTION "
                     "WHERE goods_id=1999105 AND option_code LIKE 'client-%' "
                     "GROUP BY component_type ORDER BY component_type;")
        assert counts.splitlines() == ["accessory\t14", "chain\t27", "clasp\t4", "pearl\t67"]
        assert sql("SELECT COUNT(*) FROM CC_COMPONENT_OPTION WHERE goods_id=1999105 AND name='皇冠';") == "1"
        print("空库初始化 112 项参考材料，只有一条皇冠记录。", flush=True)

        tables = "CHECKSUM TABLE mall_goods, CC_COMPONENT_OPTION, mall_coupon, sys_user;"
        data_before = sql(tables)
        assert "baseline" in migrate()
        before = sql("SELECT checksum, applied_at FROM CC_SCHEMA_MIGRATION;")
        assert "verified" in migrate()
        assert before == sql("SELECT checksum, applied_at FROM CC_SCHEMA_MIGRATION;")
        print("新库结构直接登记，重复运行保持原记录。", flush=True)

        sql("TRUNCATE CC_SCHEMA_MIGRATION; DROP TABLE CC_GOODS_QUESTION; ALTER TABLE mall_comment DROP COLUMN staff_reply;")
        assert "applying" in migrate()
        assert "verified" in migrate()
        assert sql("SELECT COUNT(*) FROM CC_GOODS_QUESTION;") == "0"
        print("旧库缺表及缺列自动补齐，重复运行通过。", flush=True)

        sql("UPDATE CC_SCHEMA_MIGRATION SET applied_at=NULL; ALTER TABLE mall_comment DROP COLUMN staff_reply;")
        assert "applying" in migrate()
        assert "verified" in migrate()
        print("部分 DDL 已提交时可补齐剩余结构。", flush=True)

        valid_digest = sql("SELECT checksum FROM CC_SCHEMA_MIGRATION;")
        sql("UPDATE CC_SCHEMA_MIGRATION SET checksum=REPEAT('F',64);")
        migrate(error="0001-goods-questions: checksum and completion")
        sql(f"UPDATE CC_SCHEMA_MIGRATION SET checksum='{valid_digest}';")
        sql("ALTER TABLE mall_comment MODIFY staff_reply VARCHAR(500) NULL;")
        migrate(error="current schema: cc_goods_question and mall_comment.staff_reply")
        sql("ALTER TABLE mall_comment MODIFY staff_reply VARCHAR(1000) NULL;")
        sql(migration_batch(source), "0001-goods-questions: checksum and completion")

        history = sql("SELECT checksum, applied_at FROM CC_SCHEMA_MIGRATION WHERE migration_id='0001-goods-questions';")
        install_next_migration(source)
        assert "0002-test-review-width: applying" in migrate(engine=bash)
        assert history == sql("SELECT checksum, applied_at FROM CC_SCHEMA_MIGRATION WHERE migration_id='0001-goods-questions';")
        completed = sql(snapshot).splitlines()
        assert len(completed) == 2
        batch = migration_batch(source, completed)
        assert "0001-goods-questions: precheck" not in batch and "0002-test-review-width: precheck" not in batch
        assert "0002-test-review-width: verified" in sql(batch)
        sql("ALTER TABLE mall_comment MODIFY staff_reply VARCHAR(1000) NULL;")
        migrate(error="current schema: cc_goods_question and mall_comment.staff_reply")
        assert data_before == sql(tables)
        print("合法扩列迁移及再次启动通过，历史记录保留；指纹、完成状态或现行结构失配均停止。", flush=True)

        # 模拟最新整库初始化自带新宽度, 独立初始化只登记当前结构已涵盖的增量.
        sql("TRUNCATE CC_SCHEMA_MIGRATION;")
        sql(migration_batch(source, baseline=True), "current schema:")
        assert sql("SELECT COUNT(*) FROM CC_SCHEMA_MIGRATION;") == "0"
        sql("ALTER TABLE mall_comment MODIFY staff_reply VARCHAR(1500) NULL;")
        assert "0002-test-review-width: baseline" in sql(migration_batch(source, bash=bash, baseline=True))
        assert "0001-goods-questions: verified" in migrate()
        print("最新空库初始化直接登记当前基线，结构不符时拒绝登记。", flush=True)
    finally:
        fixtures.close()

def main():
    """准备只含本次用例所需的SQL副本, 正式迁移文件保持原样."""
    bash = os.environ.get("CARATCRAFT_BASH") or (shutil.which("bash") if os.name != "nt" else None)
    base = Path("D:/tmp/hailongcodex") / date.today().isoformat() if os.name == "nt" else Path(tempfile.gettempdir())
    base.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="cc-migration-", dir=base) as target:
        root = Path(target)
        for name in ("local", "goods-questions"):
            shutil.copytree(ROOT / "database/upgrade" / name, root / "database/upgrade" / name)
        if bash:
            test_cross_platform_digest(root, bash)
        else:
            print("未配置 CARATCRAFT_BASH，跨平台指纹对照未执行。", flush=True)
        verify_mysql(root, bash)



if __name__ == "__main__":
    main()
