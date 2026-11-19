"""在第一阶段的隔离数据库中验证第二阶段升级及回滚."""

import importlib.util
import io
from pathlib import Path
import os
import secrets
import subprocess
import tarfile


ROOT = Path(__file__).resolve().parents[2]
BASELINE = "6b8b5c3"
SCRIPTS = ROOT / "database/upgrade/admin-jobs"


def load_runner():
    """复用现有临时数据库防护和容器归属校验."""
    spec = importlib.util.spec_from_file_location("community_regressions", ROOT / "tests/integration/run-community-regressions.py")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def main():
    """检查重复执行、旧授权保留、授权冲突拦截及可逆节点."""
    test = load_runner()
    identity = secrets.token_hex(6)
    work = ROOT / ".tools/integration" / ("admin-jobs-upgrade-" + identity)
    work.mkdir(parents=True, exist_ok=False)
    snapshot = subprocess.check_output(["git", "archive", BASELINE, "database/init"], cwd=ROOT)
    with tarfile.open(fileobj=io.BytesIO(snapshot)) as archives:
        archives.extractall(work, filter="data")
    env = dict(os.environ, MYSQL_ROOT_PASSWORD=secrets.token_urlsafe(32),
               MYSQL_PASSWORD=secrets.token_urlsafe(32), MYSQL_USER="scope_test",
               MYSQL_DATABASE="caratcraft_jobs_upgrade_" + identity)
    env["MYSQL_PWD"] = env["MYSQL_ROOT_PASSWORD"]
    fixtures = test.Fixtures(identity)
    try:
        network = fixtures.create_network()
        container = fixtures.start("caratcraft-jobs-sql-" + identity, [
            "--network", network, "--tmpfs", "/var/lib/mysql",
            "--mount", f"type=bind,source={work / 'database/init'},target=/docker-entrypoint-initdb.d,readonly",
            "--env", "MYSQL_ROOT_PASSWORD", "--env", "MYSQL_PASSWORD", "--env", "MYSQL_USER", "--env", "MYSQL_DATABASE",
            "mysql:8.0.36", "--default-authentication-plugin=mysql_native_password"], env)
        test.wait_for_mysql(container, env)
        prefix = ["docker", "exec", "-i", container, "sh", "-ec",
                  'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql --protocol=TCP -h127.0.0.1 -uroot '
                  '--default-character-set=utf8mb4 --batch --skip-column-names "$MYSQL_DATABASE"']

        def sql(statement, expected=0):
            """只向本次创建的数据库执行输入, 检查预期失败未继续执行."""
            result = subprocess.run(prefix, input=statement, text=True, encoding="utf-8", capture_output=True)
            if (result.returncode == 0) != (expected == 0):
                raise RuntimeError(result.stderr or "SQL没有按预期停止")
            return result.stdout.strip()

        def script(name, expected=0):
            """执行一份已限定的升级脚本."""
            return sql((SCRIPTS / name).read_text("utf-8"), expected)

        sql("INSERT INTO SYS_ROLE(ROLE_ID,ROLE_NAME,CREATE_TIME) VALUES('legacy-fixture','旧角色',NOW());"
            "INSERT INTO SYS_ROLE_MENU(ID,ROLE_ID,MENU_ID) VALUES('legacy-node','legacy-fixture','180102');")
        script("precheck.sql")
        script("apply.sql")
        first = script("verify.sql")
        script("apply.sql")
        assert first == script("verify.sql")
        assert sql("SELECT COUNT(*) FROM SYS_ROLE WHERE ROLE_ID LIKE 'cc-job-%';") == "6"
        assert sql("SELECT GROUP_CONCAT(MENU_ID) FROM SYS_ROLE_MENU WHERE ROLE_ID='legacy-fixture';") == "180102"
        assert sql("SELECT COUNT(*) FROM SYS_USER_ROLE WHERE ROLE_ID LIKE 'cc-job-%';") == "0"
        assert sql("SELECT COUNT(*) FROM mall_goods WHERE scope_org_no IS NOT NULL;") == "0"
        script("assign-ownership.sql")
        sql("INSERT INTO SYS_ORG(ORG_NO,ORG_NAME,STATUS,CREATE_TIME)"
            " VALUES('QT-OWN','归属机构',1,NOW()),('QT-OTHER','其他机构',1,NOW());"
            "INSERT INTO mall_order(id,order_sn) VALUES(880901,'jobs-upgrade-owner');")
        mapping = (SCRIPTS / "assign-ownership.sql").read_text("utf-8")
        marker = "DELIMITER $$"
        first_mapping = "INSERT INTO cc_approved_business_owner VALUES ('ORDER',880901,'QT-OWN');\n"
        sql(mapping.replace(marker, first_mapping + marker, 1))
        sql(mapping.replace(marker, first_mapping + marker, 1))
        conflicting = first_mapping.replace("QT-OWN", "QT-OTHER")
        sql(mapping.replace(marker, conflicting + marker, 1), 1)
        assert sql("SELECT scope_org_no FROM mall_order WHERE id=880901;") == "QT-OWN"
        print("升级重复执行、旧授权及空归属校验通过", flush=True)

        sql("INSERT INTO SYS_USER_ROLE(ID,USER_ID,ROLE_ID) VALUES('rollback-test','1','cc-job-goods');")
        script("rollback.sql", 1)
        assert sql("SELECT COUNT(*) FROM SYS_ROLE WHERE ROLE_ID LIKE 'cc-job-%';") == "6"
        sql("DELETE FROM SYS_USER_ROLE WHERE ID='rollback-test';")
        script("rollback.sql")
        assert sql("SELECT COUNT(*) FROM SYS_ROLE WHERE ROLE_ID LIKE 'cc-job-%';") == "0"
        assert sql("SELECT PARENT_ID FROM SYS_MENU WHERE MENU_ID='180105';") == "1801"
        assert sql("SELECT GROUP_CONCAT(MENU_ID) FROM SYS_ROLE_MENU WHERE ROLE_ID='legacy-fixture';") == "180102"
        print("回滚阻止删除真实绑定, 并通过无绑定时的节点回滚校验", flush=True)
    finally:
        fixtures.close()


if __name__ == "__main__":
    main()
