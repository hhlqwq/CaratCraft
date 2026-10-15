"""在随机独立数据库验证完整新库SQL初始化, 不运行启动或权限变更脚本."""

from datetime import datetime
from pathlib import Path
import re
import subprocess
import uuid

ROOT = Path(__file__).resolve().parents[1] / "vendor/platform-wxshop/_sql"


def run_sql(sql):
    """只使用容器自身的管理密码, 不在输出或命令参数中展开秘密."""
    result = subprocess.run(
        ["docker", "exec", "-i", "platform-mysql", "sh", "-c",
         'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -uroot --batch --skip-column-names --default-character-set=utf8mb4'],
        input=sql.encode("utf-8"), capture_output=True)
    if result.returncode:
        raise RuntimeError(result.stderr.decode("utf-8", errors="replace"))
    return result.stdout.decode("utf-8", errors="replace")


def expand_sql(file, parents=()):
    """展开受限根目录内的SOURCE, 以原始批处理模式保证SQL错误立即失败."""
    file = file.resolve()
    if not file.is_relative_to(ROOT.resolve()) or file in parents:
        raise ValueError("SQL引用越界或循环: " + str(file))
    text = file.read_text("utf-8-sig")
    if re.search(r"(?im)^\s*(USE|CREATE\s+DATABASE|DROP\s+DATABASE)\b", text):
        raise ValueError("初始化脚本不得切换或删除其他数据库: " + file.name)
    pattern = r"(?im)^\s*SOURCE\s+/docker-entrypoint-initdb.d/([a-zA-Z0-9_./-]+\.sql);\s*$"
    return re.sub(pattern, lambda match: expand_sql(ROOT / match.group(1), parents + (file,)), text)


def main():
    """逐文件输出初始化进度, 检查迁移台账及来源不明的商品外链."""
    database = "caratcraft_init_audit_" + datetime.now().strftime("%Y%m%d") + "_" + uuid.uuid4().hex[:6]
    if not re.fullmatch(r"caratcraft_init_audit_[0-9]{8}_[0-9a-f]{6}", database):
        raise ValueError("独立测试库名称不合法")
    files = sorted(ROOT.glob("*.sql"))
    run_sql(f"CREATE DATABASE `{database}` CHARACTER SET utf8mb4;")
    for index, file in enumerate(files, 1):
        print(f"[{index}/{len(files)}] 独立库执行 {file.name}", flush=True)
        run_sql(f"USE `{database}`;\nSET autocommit=0;\n" + expand_sql(file) + "\nCOMMIT;")
    values = run_sql(f"""USE `{database}`;
SELECT COUNT(*) FROM CC_SCHEMA_MIGRATION;
SELECT COUNT(*) FROM mall_goods WHERE primary_pic_url REGEXP '^https?://' OR list_pic_url REGEXP '^https?://';
SELECT COUNT(*) FROM CC_CUSTOM_PRODUCT c JOIN mall_goods g ON g.id=c.goods_id
WHERE g.goods_sn='DEMO-DIY-EAR-001' AND c.enabled=1;
""").strip().splitlines()
    if values != [str(len(list((ROOT / "migrations").glob("V*.sql")))), "0", "0"]:
        raise AssertionError("新库迁移、商品外链或旧示例停用检查失败: " + repr(values))
    print(f"完整SQL初始化检查通过: {database}; 测试库保留, 未修改业务库", flush=True)


if __name__ == "__main__":
    main()
