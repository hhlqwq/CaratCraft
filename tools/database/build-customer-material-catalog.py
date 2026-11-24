"""由唯一客户目录生成完整候选种子, 不连接数据库或兼容历史结构."""

import argparse
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CATALOG = ROOT / "backend/platform-biz/src/main/resources/catalog/customer-material-catalog.json"
TARGET = ROOT / "database/init/030_material_catalog.sql"


def literal(value):
    """编码受版本控制的固定种子值, 不接收运行时用户输入."""
    return "'" + str(value).replace("\\", "\\\\").replace("'", "''") + "'"


def render():
    """候选使用固定身份且默认停用零库存, 不构造旧类型或迁移桥接数据."""
    catalog = json.loads(CATALOG.read_text("utf-8"))
    lines = ["SET NAMES utf8mb4;", "", "-- 由tools/database/build-customer-material-catalog.py生成, 只用于新建空库.",
             "-- 候选尚未确认实际尺寸及库存, 默认停用且库存为0.", "START TRANSACTION;", ""]
    for index, row in enumerate(catalog["options"], 128):
        kind, code, name, params = map(literal, [row["componentType"], row["optionCode"], row["name"], row["materialParams"]])
        lines.extend([
            "INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)",
            f"VALUES ({index},1999105,{kind},{code},{name},0,{params},{row['zIndex']},0);",
            "INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)",
            f"VALUES ({index},1999105,{kind},{code},0);", ""])
    lines.extend(["COMMIT;", ""])
    return "\n".join(lines)


def main():
    """生成完整初始化候选, 或只读检查目录与种子一致性."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    content = render()
    if args.check:
        if TARGET.read_text("utf-8") != content:
            raise ValueError("客户候选目录与初始化种子不一致")
    else:
        TARGET.write_text(content, encoding="utf-8", newline="\n")
    count = len(json.loads(CATALOG.read_text("utf-8"))["options"])
    print(f"客户候选初始化一致: {count}项, 默认停用、库存0")


if __name__ == "__main__":
    main()
