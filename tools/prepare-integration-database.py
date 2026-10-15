"""为真实事务回归创建独立测试库, 不运行商城种子或历史清理脚本."""

import argparse
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1] / "vendor/platform-wxshop"


def execute_sql(sql):
    """通过容器已有mysql客户端执行SQL, 密码仅在容器环境内读取."""
    result = subprocess.run(
        ["docker", "exec", "-i", "platform-mysql", "sh", "-c",
         'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4'],
        input=sql.encode("utf-8"), capture_output=True, check=False)
    if result.returncode:
        raise RuntimeError(result.stderr.decode("utf-8", errors="replace"))


def main():
    """仅创建固定测试前缀的数据库和测试需要的当前表结构."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--database", default="caratcraft_refactor_test_20261015")
    args = parser.parse_args()
    if not re.fullmatch(r"caratcraft_refactor_test_[0-9]{8}", args.database):
        raise ValueError("只允许caratcraft_refactor_test_日期格式的测试库")
    print("[1/3] 创建独立事务测试库, 不操作业务库", flush=True)
    execute_sql(f"CREATE DATABASE IF NOT EXISTS `{args.database}` CHARACTER SET utf8mb4;")
    source = (ROOT / "_sql/mall.sql").read_text("utf-8")
    schema = re.search(r"CREATE TABLE `mall_order`\s*\(.*?;", source, re.S).group(0)
    schema = schema.replace("CREATE TABLE `mall_order`", "CREATE TABLE IF NOT EXISTS `mall_order`", 1)
    custom = (ROOT / "_sql/carat_craft_customization.sql").read_text("utf-8")
    tables = re.findall(r"CREATE TABLE IF NOT EXISTS\s+`[^`]+`\s*\(.*?;", custom, re.S)
    print(f"[2/3] 同步订单及{len(tables)}张定制表的当前结构", flush=True)
    execute_sql(f"USE `{args.database}`;\n" + schema + "\n" + "\n".join(tables))
    print("[3/3] 加载新增改价流水结构", flush=True)
    execute_sql(f"USE `{args.database}`;\n" +
                (ROOT / "_sql/migrations/V20261015_01_order_integrity.sql").read_text("utf-8"))
    print(f"独立测试库已就绪: {args.database}; 保留供后续审查, 未修改业务库", flush=True)


if __name__ == "__main__":
    main()
