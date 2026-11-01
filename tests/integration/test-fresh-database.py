"""以专用临时容器验证完整新库、Unicode和材料目录, 不访问业务实例."""

import argparse
from datetime import datetime
import importlib.util
import json
import os
from pathlib import Path
import secrets

ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location("isolated", ROOT / "tests/integration/run-community-regressions.py")
ISOLATED = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(ISOLATED)


def verify(mysql, env):
    """核对完整结构、地区、候选及全部文本编码, 只读取本次合成库."""
    def sql(statement):
        """查询本次已登记容器, 凭据不进入参数或报告."""
        return ISOLATED.docker(["exec", "--env", "MYSQL_PWD", mysql, "mysql", "-uroot",
            "--default-character-set=utf8mb4", "--batch", "--skip-column-names",
            env["MYSQL_DATABASE"], "-e", statement], env).stdout.strip()

    checks = {
        "completeVersion": sql("SELECT version FROM CC_SCHEMA_VERSION WHERE id=1") == "1",
        "regions": sql("SELECT COUNT(*) FROM sys_region") == "46181",
        "noMigrationLedger": sql("SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='CC_SCHEMA_MIGRATION'") == "0",
        "noBridgeGoods": sql("SELECT COUNT(*) FROM mall_goods WHERE goods_sn='DEMO-BRIDGE-001'") == "0",
        "feedbackUnicode": sql("SELECT TABLE_COLLATION FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='mall_feedback'") == "utf8mb4_unicode_ci",
    }
    catalog = json.loads((ROOT / "backend/platform-biz/src/main/resources/catalog/customer-material-catalog.json").read_text("utf-8"))
    actual = sql("SELECT option_code,component_type,name,material_params,z_index,enabled,price_delta FROM CC_COMPONENT_OPTION WHERE goods_id=1999105 AND option_code LIKE 'client-%' ORDER BY option_code")
    rows = {row[0]: row[1:] for row in (line.split("\t") for line in actual.splitlines())}
    checks["catalogExact"] = len(rows) == len(catalog["options"]) == 112
    for expected in catalog["options"]:
        row = rows.get(expected["optionCode"])
        if row is None or [row[0], row[1], json.loads(row[2]), int(row[3]), int(row[4]), float(row[5])] != [expected["componentType"], expected["name"], json.loads(expected["materialParams"]), expected["zIndex"], 0, 0]:
            checks["catalogExact"] = False
    checks["candidateStockZero"] = sql("SELECT COUNT(*) FROM CC_COMPONENT_STOCK WHERE goods_id=1999105 AND option_code LIKE 'client-%' AND stock=0") == "112"
    spec = importlib.util.spec_from_file_location("encoding", ROOT / "tools/database/check-text-encoding.py")
    encoding = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(encoding)
    encoding.query = sql
    text = encoding.inspect_database(env["MYSQL_DATABASE"])
    report = {"schemaVersion": 1, "checks": checks, "encoding": text,
              "businessDatabaseChanged": False, "initializationFiles": sorted(path.name for path in (ROOT / "database/init").iterdir())}
    if not all(checks.values()) or not text["ok"]:
        raise AssertionError(json.dumps(report, ensure_ascii=False))
    return report


def main():
    """创建隔离MySQL, 验证结束后按资源归属清理, 不保留测试库."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--date", default=datetime.now().strftime("%Y-%m-%d"))
    args = parser.parse_args()
    datetime.strptime(args.date, "%Y-%m-%d")
    run = "schema-" + secrets.token_hex(4)
    fixtures = ISOLATED.Fixtures(run)
    env = os.environ.copy()
    env.update(MYSQL_ROOT_PASSWORD=secrets.token_hex(24), MYSQL_PASSWORD=secrets.token_hex(24),
               MYSQL_DATABASE="schema_test", MYSQL_USER="schema_app")
    env["MYSQL_PWD"] = env["MYSQL_ROOT_PASSWORD"]
    try:
        network = fixtures.create_network()
        mysql = fixtures.start("caratcraft-" + run, ["--network", network, "--tmpfs", "/var/lib/mysql",
            "--env", "MYSQL_ROOT_PASSWORD", "--env", "MYSQL_PASSWORD", "--env", "MYSQL_DATABASE", "--env", "MYSQL_USER",
            "--mount", f"type=bind,source={ROOT / 'database/init'},target=/docker-entrypoint-initdb.d,readonly",
            "mysql:8.0.36", "--character-set-server=utf8mb4", "--collation-server=utf8mb4_general_ci",
            "--default-authentication-plugin=mysql_native_password"], env)
        ISOLATED.wait_for_mysql(mysql, env)
        report = verify(mysql, env)
        report["verifiedDate"] = args.date
        (ROOT / "docs/资料/数据库初始化验证.json").write_text(json.dumps(report, ensure_ascii=False, indent=4) + "\n", "utf-8", newline="\n")
        print("完整初始化、112项同源候选、46181条地区和全库Unicode检查通过", flush=True)
    finally:
        fixtures.close()


if __name__ == "__main__":
    main()
