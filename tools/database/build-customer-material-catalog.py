"""由单一客户候选目录生成受控迁移, 不连接或修改业务数据库."""

import argparse
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
CATALOG = ROOT / "backend/platform-biz/src/main/resources/catalog/customer-material-catalog.json"
TARGET = ROOT / "database/migrations/V20261024_01_material_catalog.sql"


def literal(value):
    """将仓库固定候选值编码为SQL字符串, 不接收运行时用户输入."""
    return "'" + str(value).replace("\\", "\\\\").replace("'", "''") + "'"


def render():
    """保留旧身份和快照, 只停用旧分类并为明确演示商品新增停用候选."""
    catalog = json.loads(CATALOG.read_text("utf-8"))
    lines = ["SET NAMES utf8mb4;", "", "-- 由tools/database/build-customer-material-catalog.py生成, 候选原文见同源JSON.",
             "-- 不猜测旧类型归属, 保留原选项、库存和订单快照; 受影响模板须商家重新配置.",
             "START TRANSACTION;", "",
             "UPDATE CC_CUSTOM_PRODUCT p SET p.enabled=0, p.required_component_types='pearl,clasp,chain'",
             "WHERE (p.required_component_types IS NOT NULL AND p.required_component_types<>''",
             "    AND p.required_component_types NOT REGEXP '^(pearl|clasp|accessory|chain)(,(pearl|clasp|accessory|chain))*$')",
             "OR EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=p.goods_id",
             "    AND o.enabled=1 AND o.component_type NOT IN ('pearl','clasp','accessory','chain'));", "",
             "UPDATE CC_COMPONENT_OPTION SET enabled=0",
             "WHERE component_type NOT IN ('pearl','clasp','accessory','chain');", "",
             "-- 不覆盖商家同代码配置; 仅匹配项目明确的项链演示货号和模板."]
    for row in catalog["options"]:
        values = [row["componentType"], row["optionCode"], row["name"], row["materialParams"]]
        kind, code, name, params = map(literal, values)
        lines.extend([
            "INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)",
            f"SELECT g.id,{kind},{code},{name},0,{params},{row['zIndex']},0",
            "FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id",
            "WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'",
            f"AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type={kind} AND o.option_code={code});",
            "INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)",
            f"SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o",
            "JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id",
            "WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'",
            f"AND o.component_type={kind} AND o.option_code={code}",
            "AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);", ""])
    lines.extend(["COMMIT;", ""])
    return "\n".join(lines)


def main():
    """只读核对生成结果, 或在尚未发布本版本时写入迁移源码."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    content = render()
    if args.check:
        if TARGET.read_text("utf-8") != content:
            raise ValueError("客户候选目录与迁移不一致")
    else:
        if TARGET.exists() and TARGET.read_text("utf-8") != content:
            raise ValueError("已存在的V迁移不能重写, 调整客户目录请新增资源版本和迁移")
        TARGET.write_text(content, encoding="utf-8", newline="\n")
    print("客户材料候选迁移一致: 112项, 默认停用、库存0")


if __name__ == "__main__":
    main()
