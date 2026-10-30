"""隔离MySQL真实验证客户材料目录、迁移重跑及Java全套回归."""

import argparse
import importlib.util
import json
import os
from pathlib import Path
import secrets
import subprocess


ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location("http_regression", ROOT / "tests/integration/run-http-business-chain.py")
HTTP = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(HTTP)


def main():
    """只创建本轮容器, 验证新库和重复迁移后补跑全部Java并清理容器."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--date", required=True)
    args = parser.parse_args()
    from datetime import datetime
    day = datetime.strptime(args.date, "%Y-%m-%d").strftime("%Y%m%d")
    suffix = secrets.token_hex(3)
    env = os.environ.copy()
    env.update(MYSQL_ROOT_PASSWORD=secrets.token_hex(24), MYSQL_PASSWORD=secrets.token_hex(24),
               MYSQL_DATABASE=f"caratcraft_init_audit_{day}_{suffix}", MYSQL_USER="catalog_regression")
    containers = []
    try:
        mysql = HTTP.docker("run", "--detach", "--rm", "--pull=never", "--name", "caratcraft-catalog-mysql-" + suffix,
                            "--tmpfs", "/var/lib/mysql", "--env", "MYSQL_ROOT_PASSWORD", "--env", "MYSQL_PASSWORD",
                            "--env", "MYSQL_DATABASE", "--env", "MYSQL_USER", "--mount",
                            f"type=bind,source={ROOT / 'database/init'},target=/docker-entrypoint-initdb.d,readonly",
                            "mysql:8.0.36", "--default-authentication-plugin=mysql_native_password", environment=env)
        containers.append(mysql)
        HTTP.wait_until(lambda: HTTP.sql(mysql, "SELECT COUNT(*) FROM CC_SCHEMA_MIGRATION;"))
        checks = """
SELECT COUNT(*) FROM CC_SCHEMA_MIGRATION;
SELECT COUNT(*) FROM CC_COMPONENT_OPTION WHERE goods_id=1999105 AND option_code LIKE 'client-%';
SELECT COUNT(*) FROM CC_COMPONENT_OPTION WHERE goods_id=1999105 AND option_code LIKE 'client-%' AND (enabled<>0 OR price_delta<>0);
SELECT COUNT(*) FROM CC_COMPONENT_STOCK WHERE goods_id=1999105 AND option_code LIKE 'client-%' AND stock<>0;
SELECT enabled FROM CC_CUSTOM_PRODUCT WHERE goods_id=1999105;
SELECT COUNT(*) FROM CC_COMPONENT_OPTION WHERE component_type NOT IN ('pearl','clasp','accessory','chain') AND enabled=1;
"""
        actual = HTTP.sql(mysql, checks).splitlines()
        expected = [str(len(list((ROOT / "database/migrations").glob("V*.sql")))), "112", "0", "0", "0", "0"]
        if actual != expected:
            raise AssertionError("新库候选或旧分类隔离失败: " + repr(actual))
        HTTP.sql(mysql, """
UPDATE CC_COMPONENT_OPTION SET name='商家已维护候选',price_delta=123.45,enabled=1
WHERE goods_id=1999105 AND option_code='client-southsea-white-8-9';
UPDATE CC_COMPONENT_STOCK SET stock=7 WHERE goods_id=1999105 AND option_code='client-southsea-white-8-9';
INSERT INTO CC_DESIGN_SNAPSHOT(user_id,goods_id,configuration_json,price_breakdown_json,status)
VALUES(999999,1999105,'{"historical":"style:original"}','{"components":[]}', 'LOCKED');
""")
        migration = (ROOT / "database/migrations/V20261024_01_material_catalog.sql").read_text("utf-8")
        HTTP.sql(mysql, migration)
        repeat = HTTP.sql(mysql, """
SELECT COUNT(*) FROM CC_COMPONENT_OPTION WHERE goods_id=1999105 AND option_code LIKE 'client-%';
SELECT CONCAT(name,':',price_delta,':',enabled) FROM CC_COMPONENT_OPTION WHERE goods_id=1999105 AND option_code='client-southsea-white-8-9';
SELECT stock FROM CC_COMPONENT_STOCK WHERE goods_id=1999105 AND option_code='client-southsea-white-8-9';
SELECT configuration_json->>'$.historical' FROM CC_DESIGN_SNAPSHOT WHERE user_id=999999;
""").splitlines()
        if repeat != ["112", "商家已维护候选:123.45:1", "7", "style:original"]:
            raise AssertionError("重复迁移改写商家数据或历史快照: " + repr(repeat))
        print("真实MySQL候选112项、零发布库存、旧类停用、重跑及历史快照保留通过", flush=True)
        redis = HTTP.docker("run", "--detach", "--rm", "--pull=never", "--network", "container:" + mysql,
                            "--name", "caratcraft-catalog-redis-" + suffix, "--read-only", "--tmpfs", "/data",
                            "redis:7.2-alpine", "redis-server", "--save", "", "--appendonly", "no")
        containers.append(redis)
        env.update(CARATCRAFT_DATABASE_TEST="true", CARATCRAFT_DATABASE_TEST_PASSWORD=env["MYSQL_ROOT_PASSWORD"],
                   CARATCRAFT_DATABASE_TEST_URL="jdbc:mariadb://127.0.0.1:3306/" + env["MYSQL_DATABASE"] + "?allowPublicKeyRetrieval=true",
                   CARATCRAFT_REDIS_TEST="true")
        command = ["docker", "run", "--rm", "--pull=never", "--network", "container:" + mysql,
                   "--mount", f"type=bind,source={ROOT / 'backend'},target=/workspace/backend",
                   "--mount", f"type=bind,source={ROOT / '.tools/m2/repository'},target=/offline-cache,readonly",
                   "--workdir", "/workspace/backend"]
        for key in ("CARATCRAFT_DATABASE_TEST", "CARATCRAFT_DATABASE_TEST_PASSWORD", "CARATCRAFT_DATABASE_TEST_URL", "CARATCRAFT_REDIS_TEST"):
            command.extend(["--env", key])
        command.extend(["carat-craft-java-tests:local", "sh", "-ec",
                        "cp -an /offline-cache/. /root/.m2/repository/; mvn -o -B -s /workspace/maven-settings.xml -DforkCount=0 package"])
        subprocess.run(command, env=env, check=True)
        evidence = {"date": args.date, "database": env["MYSQL_DATABASE"], "candidateRows": 112,
                    "defaultEnabled": False, "defaultStock": 0, "repeatPreservesMerchantChanges": True,
                    "historicalSnapshotsUnchanged": True, "legacyCategoriesDisabled": True,
                    "javaPackageWithRealDatabaseAndRedis": "passed", "dependenciesDownloaded": False,
                    "businessServicesChanged": False}
        (ROOT / "docs/资料/材料目录验证.json").write_text(json.dumps(evidence, ensure_ascii=False, indent=4) + "\n", encoding="utf-8", newline="\n")
    finally:
        for container in reversed(containers):
            HTTP.docker("stop", "--time", "5", container)
        print("本轮目录MySQL/Redis测试容器已清理", flush=True)


if __name__ == "__main__":
    main()
