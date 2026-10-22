"""只读核验整库文本、结构注释与内置中文, 不输出业务正文或修改数据."""

import argparse
import json
import re
import subprocess

# 覆盖UTF-8误按MySQL latin1/Windows1252解码的典型双字节组合与替换字符.
MOJIBAKE_PATTERN = "[ÃÂäåæçèé][\\x80-\\xBFŒœŠšŸŽž€‘’“”–—…†‡‰‹›ˆ˜™]|�"


def identifier(value):
    """仅允许当前项目的普通数据库标识符, 拒绝拼接任意SQL片段."""
    if not re.fullmatch(r"[A-Za-z0-9_]+", value):
        raise ValueError("数据库或表列标识符不合法")
    return "`" + value + "`"


def query(sql):
    """用容器内部管理凭据执行只读查询, 不将秘密放入参数或日志."""
    result = subprocess.run(
        ["docker", "exec", "-i", "platform-mysql", "sh", "-c",
         'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -uroot --batch '
         '--skip-column-names --default-character-set=utf8mb4'],
        input=sql.encode("utf-8"), capture_output=True, check=False)
    if result.returncode:
        raise RuntimeError(result.stderr.decode("utf-8", errors="replace"))
    return result.stdout.decode("utf-8").strip()


def sql_text(value):
    """通过UTF-8十六进制常量表达已知文本, 避免连接编码或转义影响断言."""
    return "CONVERT(UNHEX('" + value.encode("utf-8").hex() + "') USING utf8mb4)"


def inspect_database(database):
    """扫描全部文本列及元数据注释, 并核对关键内置中文的确切字节."""
    schema = identifier(database)
    pattern = sql_text(MOJIBAKE_PATTERN)
    columns = query(
        "SELECT TABLE_NAME,COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA="
        + sql_text(database)
        + " AND DATA_TYPE IN ('char','varchar','tinytext','text','mediumtext','longtext','enum','set','json')"
          " ORDER BY TABLE_NAME,ORDINAL_POSITION;")
    statements = []
    for line in columns.splitlines():
        table, column = line.split("\t")
        statements.append(
            "SELECT " + sql_text(table) + "," + sql_text(column) + ",COUNT(*) FROM "
            + schema + "." + identifier(table) + " WHERE CAST(" + identifier(column)
            + " AS CHAR CHARACTER SET utf8mb4) REGEXP " + pattern + " HAVING COUNT(*)>0")
    issues = []
    for offset in range(0, len(statements), 50):
        result = query(" UNION ALL ".join(statements[offset:offset + 50]) + ";")
        for line in result.splitlines():
            table, column, count = line.split("\t")
            issues.append({"table": table, "column": column, "rows": int(count)})
    metadata = query(
        "SELECT 'column_comments',COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA="
        + sql_text(database) + " AND COLUMN_COMMENT REGEXP " + pattern
        + " UNION ALL SELECT 'table_comments',COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA="
        + sql_text(database) + " AND TABLE_COMMENT REGEXP " + pattern + ";")
    comment_counts = {name: int(count) for name, count in
                      (line.split("\t") for line in metadata.splitlines())}
    seeds = [
        ("系统菜单", "SELECT NAME FROM SYS_MENU WHERE MENU_ID='10'", "系统设置"),
        ("社区菜单", "SELECT NAME FROM SYS_MENU WHERE MENU_ID='9300'", "社区管理"),
        ("商家菜单", "SELECT NAME FROM SYS_MENU WHERE MENU_ID='9400'", "商家服务"),
        ("协议菜单", "SELECT NAME FROM SYS_MENU WHERE MENU_ID='9500'", "协议与隐私"),
        ("字典分组", "SELECT NAME FROM SYS_DICT_GROUP WHERE CODE='SEX'", "性别"),
        ("字典备注", "SELECT REMARK FROM SYS_DICT_GROUP WHERE CODE='SEX'", "性别，1：男 2：女 0：未知"),
        ("订单超时任务", "SELECT REMARK FROM SCHEDULE_JOB WHERE BEAN_NAME='orderTimeoutTask'",
         "订单超时自动关闭(5分钟)"),
        ("地区字典", "SELECT name FROM sys_region WHERE id=1", "中国"),
        ("字典表注释", "SELECT TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE()"
         " AND TABLE_NAME='SYS_DICT_GROUP'", "数据字典分组"),
        ("字典列注释", "SELECT COLUMN_COMMENT FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE()"
         " AND TABLE_NAME='SYS_DICT_GROUP' AND COLUMN_NAME='NAME'", "分组名称"),
    ]
    checks = {}
    for name, select, expected in seeds:
        actual = query("USE " + schema + "; SELECT HEX((" + select + "));")
        checks[name] = actual == expected.encode("utf-8").hex().upper()
    report = {"textColumnsScanned": len(statements), "suspectTextColumns": issues,
              "suspectComments": comment_counts, "seedAssertions": checks}
    report["ok"] = not issues and not any(comment_counts.values()) and all(checks.values())
    return report


def main():
    """以计数报告验收结果, 不将实际业务值或凭据写入报告."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--database", required=True)
    args = parser.parse_args()
    report = inspect_database(args.database)
    print(json.dumps(report, ensure_ascii=False, indent=4))
    if not report["ok"]:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
