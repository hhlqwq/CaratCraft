"""使用正式PowerShell批次验证隔离MySQL的结构增量和失败恢复."""

import importlib.util
import os
from pathlib import Path
import secrets
import shutil
import subprocess

ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location("isolated", ROOT / "tests/integration/run-community-regressions.py")
ISOLATED = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(ISOLATED)


def migration_batch():
    """加载生产批次生成函数, 跳过标准脚本的宿主和容器操作."""
    source = r"""
$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [Text.UTF8Encoding]::new($false)
$ProjectRoot = (Get-Location).Path
$Tokens = $null; $Errors = $null
$Ast = [Management.Automation.Language.Parser]::ParseFile((Join-Path $ProjectRoot 'deploy/scripts/dev.ps1'), [ref]$Tokens, [ref]$Errors)
if ($Errors.Count) { throw ($Errors | Out-String) }
$Function = $Ast.FindAll({ param($Node) $Node -is [Management.Automation.Language.FunctionDefinitionAst] -and $Node.Name -eq 'New-AutomaticMigrationBatch' }, $false)
. ([scriptblock]::Create($Function[0].Extent.Text))
New-AutomaticMigrationBatch
"""
    powershell = shutil.which("powershell") or shutil.which("pwsh")
    result = subprocess.run([powershell, "-NoProfile", "-Command", source], cwd=ROOT,
                            capture_output=True, encoding="utf-8", timeout=30)
    if result.returncode:
        raise RuntimeError(result.stderr)
    return result.stdout


def main():
    """验证空库登记、旧库自动补齐、中断续跑、指纹失配和结构漂移."""
    batch = migration_batch()
    run = "migration-" + secrets.token_hex(4)
    fixtures = ISOLATED.Fixtures(run)
    env = dict(os.environ, MYSQL_ROOT_PASSWORD=secrets.token_hex(24), MYSQL_DATABASE="schema_test",
               MYSQL_USER="schema_app", MYSQL_PASSWORD=secrets.token_hex(24))
    env["MYSQL_PWD"] = env["MYSQL_ROOT_PASSWORD"]
    try:
        mysql = fixtures.start("caratcraft-" + run, ["--network", "none", "--tmpfs", "/var/lib/mysql",
            "--env", "MYSQL_ROOT_PASSWORD", "--env", "MYSQL_DATABASE", "--env", "MYSQL_USER", "--env", "MYSQL_PASSWORD",
            "--mount", f"type=bind,source={ROOT / 'database/init'},target=/docker-entrypoint-initdb.d,readonly",
            "m.daocloud.io/docker.io/library/mysql:8.0.36", "--default-authentication-plugin=mysql_native_password"], env)
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

        counts = sql("SELECT component_type, COUNT(*) FROM CC_COMPONENT_OPTION "
                     "WHERE goods_id=1999105 AND option_code LIKE 'client-%' "
                     "GROUP BY component_type ORDER BY component_type;")
        assert counts.splitlines() == ["accessory\t14", "chain\t27", "clasp\t4", "pearl\t67"]
        assert sql("SELECT COUNT(*) FROM CC_COMPONENT_OPTION WHERE goods_id=1999105 AND name='皇冠';") == "1"
        print("空库初始化 112 项参考材料，只有一条皇冠记录。", flush=True)

        tables = "CHECKSUM TABLE mall_goods, CC_COMPONENT_OPTION, mall_coupon, sys_user;"
        data_before = sql(tables)
        assert "baseline" in sql(batch)
        before = sql("SELECT checksum, applied_at FROM CC_SCHEMA_MIGRATION;")
        assert "verified" in sql(batch)
        assert before == sql("SELECT checksum, applied_at FROM CC_SCHEMA_MIGRATION;")
        print("新库结构直接登记，重复运行保持原记录。", flush=True)

        sql("TRUNCATE CC_SCHEMA_MIGRATION; DROP TABLE CC_GOODS_QUESTION; ALTER TABLE mall_comment DROP COLUMN staff_reply;")
        assert "applying" in sql(batch)
        assert "verified" in sql(batch)
        assert sql("SELECT COUNT(*) FROM CC_GOODS_QUESTION;") == "0"
        print("旧库缺表及缺列自动补齐，重复运行通过。", flush=True)

        sql("UPDATE CC_SCHEMA_MIGRATION SET applied_at=NULL; ALTER TABLE mall_comment DROP COLUMN staff_reply;")
        assert "applying" in sql(batch)
        assert "verified" in sql(batch)
        print("部分 DDL 已提交时可补齐剩余结构。", flush=True)

        valid_digest = sql("SELECT checksum FROM CC_SCHEMA_MIGRATION;")
        sql("UPDATE CC_SCHEMA_MIGRATION SET checksum=REPEAT('F',64);")
        sql(batch, "0001-goods-questions: checksum")
        sql(f"UPDATE CC_SCHEMA_MIGRATION SET checksum='{valid_digest}';")
        sql("ALTER TABLE mall_comment MODIFY staff_reply VARCHAR(500) NULL;")
        sql(batch, "0001-goods-questions: precheck")
        assert data_before == sql(tables)
        print("迁移指纹失配及结构漂移均停止，商品、材料、券与账号校验和一致。", flush=True)
    finally:
        fixtures.close()


if __name__ == "__main__":
    main()
