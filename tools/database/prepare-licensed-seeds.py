"""生成只含结构与项目演示内容的商城初始化脚本, 不连接数据库."""

import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
SQL_ROOT = ROOT / "database/init"


def extract_schema(text):
    """按SQL字符串和注释边界提取建表语句, 不保留旧商品记录."""
    output = []
    for match in re.finditer(r"CREATE TABLE\s+`[^`]+`", text, re.IGNORECASE):
        index = match.start()
        cursor = index
        quote = None
        while cursor < len(text):
            char = text[cursor]
            if quote:
                if char == "\\":
                    cursor += 2
                    continue
                if char == quote:
                    if cursor + 1 < len(text) and text[cursor + 1] == quote:
                        cursor += 2
                        continue
                    quote = None
            elif char in {"'", '"', "`"}:
                quote = char
            elif char == ";":
                output.append(text[index:cursor + 1])
                break
            cursor += 1
        else:
            raise ValueError("发现未闭合的建表语句")
    if not output:
        raise ValueError("未找到商城表结构")
    return output


def main():
    """保留表结构, 使用最小自有演示桥接记录支持既有迁移链."""
    mall = SQL_ROOT / "050_mall.sql"
    schema = extract_schema(mall.read_text(encoding="utf-8"))
    header = "-- CaratCraft商城结构与项目演示种子, 不包含第三方商品文案或图片.\n"
    header += "-- 只用于新建空库; 不要在有业务数据的数据库执行.\nSET NAMES utf8mb4;\n\n"
    seed = """
-- 此桥接商品只供既有定制迁移使用, 后续迁移会转到1999105.
INSERT INTO `mall_category` (`id`, `name`, `parent_id`, `is_show`)
VALUES (1999000, '项目演示目录', 0, 1);
INSERT INTO `mall_goods`
(`id`, `category_id`, `goods_sn`, `name`, `goods_number`, `goods_brief`,
 `goods_desc`, `is_on_sale`, `is_delete`, `retail_price`, `primary_pic_url`, `list_pic_url`)
VALUES (1006002, 1999000, 'DEMO-BRIDGE-001', '演示 · 定制迁移占位商品', 100,
 '仅供初始化迁移使用, 不是真实售卖商品.', '<p>项目演示数据.</p>', 0, 0, 399.00,
 '/static/images/demo/demo-platinum-pearl-necklace.webp',
 '/static/images/demo/demo-platinum-pearl-necklace.webp');
INSERT INTO `mall_product` (`id`, `goods_id`, `goods_sn`, `goods_number`, `retail_price`)
VALUES (1006002, 1006002, 'DEMO-BRIDGE-001-STD', 100, 399.00);
INSERT INTO `mall_specification` (`id`, `name`, `sort_order`)
VALUES (1, '颜色', 1), (2, '规格', 2), (4, '重量', 3);
"""
    mall.write_text(header + "\n\n".join(schema) + "\n" + seed, encoding="utf-8")
    base = SQL_ROOT / "010_base.sql"
    text = base.read_text(encoding="utf-8")
    replacement = ("INSERT INTO `SYS_CONFIG` VALUES ('2', 'SMS_CONFIG_KEY', "
                   "'{\\\"domain\\\":\\\"\\\",\\\"name\\\":\\\"\\\","
                   "\\\"pwd\\\":\\\"\\\",\\\"sign\\\":\\\"\\\","
                   "\\\"type\\\":1}', '0', '短信配置, 由部署者设置');")
    lines = [replacement if line.startswith("INSERT INTO `SYS_CONFIG` VALUES ('2',")
             else line for line in text.splitlines()]
    base.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"已保留{len(schema)}张商城表结构, 移除旧商品/广告/用户演示记录.")


if __name__ == "__main__":
    main()
