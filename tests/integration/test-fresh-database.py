"""在随机独立数据库验证完整新库SQL初始化, 不运行启动或权限变更脚本."""

from datetime import datetime
import argparse
import importlib.util
import json
from pathlib import Path
import re
import subprocess
import uuid

ROOT = Path(__file__).resolve().parents[2] / "database/init"


def run_sql(sql, charset="utf8mb4"):
    """只使用容器自身的管理密码, 不在输出或命令参数中展开秘密."""
    if charset not in {"utf8mb4", "latin1"}:
        raise ValueError("不支持的测试连接编码")
    result = subprocess.run(
        ["docker", "exec", "-i", "platform-mysql", "sh", "-c",
         'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -uroot --batch --skip-column-names '
         + '--default-character-set=' + charset],
        input=sql.encode("utf-8"), capture_output=True)
    if result.returncode:
        raise RuntimeError(result.stderr.decode("utf-8", errors="replace"))
    return result.stdout.decode("utf-8", errors="replace")


def read_initialization_sql(file):
    """只读取初始化目录内的独立SQL, 禁止跨库和SOURCE影响其他数据库."""
    file = file.resolve()
    if not file.is_relative_to(ROOT.resolve()):
        raise ValueError("初始化SQL越界: " + str(file))
    text = file.read_text("utf-8-sig")
    if re.search(r"(?im)^\s*(USE|CREATE\s+DATABASE|DROP\s+DATABASE)\b", text):
        raise ValueError("初始化脚本不得切换或删除其他数据库: " + file.name)
    if re.search(r"(?im)^\s*SOURCE\s+[^\r\n;]+;", text):
        raise ValueError("初始化必须使用独立SQL, 不允许SOURCE: " + file.name)
    return text


def main():
    """逐文件输出初始化进度, 检查迁移台账及来源不明的商品外链."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--date", help="客户端日期, YYYY-MM-DD, 仅用于独立测试库命名")
    args = parser.parse_args()
    day = datetime.strptime(args.date, "%Y-%m-%d") if args.date else datetime.now()
    database = "caratcraft_init_audit_" + day.strftime("%Y%m%d") + "_" + uuid.uuid4().hex[:6]
    if not re.fullmatch(r"caratcraft_init_audit_[0-9]{8}_[0-9a-f]{6}", database):
        raise ValueError("独立测试库名称不合法")
    files = sorted(ROOT.glob("*.sql"))
    run_sql(f"CREATE DATABASE `{database}` CHARACTER SET utf8mb4;")
    for index, file in enumerate(files, 1):
        print(f"[{index}/{len(files)}] 独立库执行 {file.name}", flush=True)
        run_sql(f"USE `{database}`;\n" + read_initialization_sql(file), charset="latin1")
    values = run_sql(f"""USE `{database}`;
SELECT COUNT(*) FROM CC_SCHEMA_MIGRATION;
SELECT COUNT(*) FROM mall_goods WHERE primary_pic_url REGEXP '^https?://' OR list_pic_url REGEXP '^https?://';
SELECT COUNT(*) FROM CC_CUSTOM_PRODUCT c JOIN mall_goods g ON g.id=c.goods_id
WHERE g.goods_sn='DEMO-DIY-EAR-001' AND c.enabled=1;
""").strip().splitlines()
    if values != [str(len(list((ROOT.parent / "migrations").glob("V*.sql")))), "0", "0"]:
        raise AssertionError("新库迁移、商品外链或旧示例停用检查失败: " + repr(values))
    spec = importlib.util.spec_from_file_location(
        "text_encoding", ROOT.parents[1] / "tools/database/check-text-encoding.py")
    encoding = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(encoding)
    report = encoding.inspect_database(database)
    print(json.dumps(report, ensure_ascii=False, indent=4))
    if not report["ok"]:
        raise AssertionError("新库中文或结构注释验收失败")
    print(f"完整SQL初始化及中文内容检查通过(latin1导入): {database}; 测试库保留, 未修改业务库", flush=True)


if __name__ == "__main__":
    main()
