"""在随机独立MySQL库验证内置菜单编码修复, 不修改业务库或商家自定义名称."""

import argparse
from datetime import datetime
from pathlib import Path
import subprocess
import uuid

ROOT = Path(__file__).resolve().parents[2]


def execute(sql):
    """沿用容器管理凭据执行合成测试SQL, 密码不进入命令参数或终端输出."""
    result = subprocess.run(
        ["docker", "exec", "-i", "platform-mysql", "sh", "-c",
         'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -uroot --batch '
         '--skip-column-names --default-character-set=utf8mb4'],
        input=sql.encode("utf-8"), capture_output=True, check=False)
    if result.returncode:
        raise RuntimeError(result.stderr.decode("utf-8", errors="replace"))
    return result.stdout.decode("utf-8").strip()


def damaged(name, windows):
    """重现旧导入中的Latin1或Windows1252误解码, 未定义控制字节按原值保留."""
    raw = name.encode("utf-8")
    if not windows:
        return raw.decode("latin1")
    undefined = {0x81, 0x8D, 0x8F, 0x90, 0x9D}
    return "".join(chr(value) if value in undefined
                   else bytes([value]).decode("cp1252") for value in raw)


def main():
    """验证两种已知损坏、正常中文、自定义名称、未知ID和重复执行的边界."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--date", required=True, help="客户端日期, YYYY-MM-DD")
    args = parser.parse_args()
    day = datetime.strptime(args.date, "%Y-%m-%d").strftime("%Y%m%d")
    database = f"caratcraft_init_audit_{day}_{uuid.uuid4().hex[:6]}"
    execute(f"CREATE DATABASE `{database}` CHARACTER SET utf8mb4;")
    prefix = f"USE `{database}`; SET NAMES utf8mb4;\n"
    execute(prefix + "CREATE TABLE SYS_MENU (MENU_ID VARCHAR(64) PRIMARY KEY,"
            "NAME VARCHAR(255) NOT NULL) ENGINE=InnoDB CHARACTER SET utf8mb4;")
    migration = (ROOT / "database/migrations/"
                 "V20261021_04_menu_encoding.sql").read_text("utf-8")
    for windows in (False, True):
        fixtures = {"10": damaged("系统设置", windows), "13": damaged("公众号管理", windows),
                    "11": "运营自定义导航",
                    "12": "开发工具", "999999": damaged("系统设置", windows)}
        inserts = []
        for identity, name in fixtures.items():
            encoded = name.encode("utf-8").hex()
            inserts.append(f"INSERT INTO SYS_MENU VALUES('{identity}',"
                           f"CONVERT(UNHEX('{encoded}') USING utf8mb4));")
        execute(prefix + "DELETE FROM SYS_MENU;\n" + "\n".join(inserts) + migration)
        select = prefix + "SELECT MENU_ID,HEX(NAME) FROM SYS_MENU ORDER BY MENU_ID;"
        rows = dict(line.split("\t") for line in execute(select).splitlines())
        assert rows["10"] == "系统设置".encode("utf-8").hex().upper()
        assert rows["13"] == "公众号管理".encode("utf-8").hex().upper()
        for identity in ("11", "12", "999999"):
            assert rows[identity] == fixtures[identity].encode("utf-8").hex().upper()
        execute(prefix + migration)
        assert execute(select) == "\n".join(f"{key}\t{value}" for key, value in rows.items())
    print("菜单编码迁移回归通过: 两种损坏、正常中文、自定义名称、未知ID及幂等执行.")
    print("隔离测试库保留: " + database)


if __name__ == "__main__":
    main()
