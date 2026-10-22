"""在随机独立MySQL库验证SKU迁移和冲突阻断, 不连接业务表."""

import argparse
from datetime import datetime
import importlib.util
from pathlib import Path
import uuid

ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location("menu_test", ROOT / "tests/integration/test-menu-encoding.py")
DATABASE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(DATABASE)


def main():
    """验证旧ID转换、新格式规范化、默认SKU、历史快照及失败前不写表."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--date", required=True)
    args = parser.parse_args()
    day = datetime.strptime(args.date, "%Y-%m-%d").strftime("%Y%m%d")
    name = f"caratcraft_init_audit_{day}_{uuid.uuid4().hex[:6]}"
    DATABASE.execute(f"CREATE DATABASE `{name}` CHARACTER SET utf8mb4;")
    prefix = f"USE `{name}`; SET NAMES utf8mb4;\n"
    migration = (ROOT / "database/migrations/V20261022_01_sku_contract.sql").read_text("utf-8")
    setup = """
DROP TABLE IF EXISTS mall_goods_specification, mall_product, mall_cart, mall_order_goods;
CREATE TABLE mall_goods_specification (id INT PRIMARY KEY, goods_id INT, specification_id INT, value VARCHAR(50));
CREATE TABLE mall_product (id INT PRIMARY KEY, goods_id INT, goods_specification_ids VARCHAR(50));
CREATE TABLE mall_cart (id INT PRIMARY KEY, goods_id INT, product_id INT, goods_specifition_ids VARCHAR(60));
CREATE TABLE mall_order_goods (id INT PRIMARY KEY, goods_specifition_ids VARCHAR(255));
INSERT INTO mall_goods_specification VALUES (1,10,7,'白色'),(2,10,8,'8mm'),(3,11,7,'金色');
INSERT INTO mall_product VALUES (31,10,'2_1'),(32,11,'7:金色'),(33,12,'');
INSERT INTO mall_cart VALUES (1,10,31,'2_1');
INSERT INTO mall_order_goods VALUES (1,'2_1');
"""
    DATABASE.execute(prefix + setup + migration)
    actual = DATABASE.execute(prefix + "SELECT goods_specification_ids FROM mall_product WHERE id=31;")
    assert actual == "7:白色|8:8mm", actual
    assert DATABASE.execute(prefix + "SELECT goods_specifition_ids FROM mall_cart;") == actual
    assert DATABASE.execute(prefix + "SELECT goods_specifition_ids FROM mall_order_goods;") == "2_1"
    DATABASE.execute(prefix + migration)
    assert DATABASE.execute(prefix + "SELECT goods_specification_ids FROM mall_product WHERE id=31;") == actual
    failures = [
        "UPDATE mall_product SET goods_specification_ids='999_1' WHERE id=31;",
        "UPDATE mall_product SET goods_specification_ids='1_1' WHERE id=31;",
        "UPDATE mall_product SET goods_specification_ids='7:白|8:8mm' WHERE id=31;",
        "INSERT INTO mall_product VALUES (34,10,'1_2');",
        "UPDATE mall_goods_specification SET value='白|金' WHERE id=1;",
    ]
    for change in failures:
        DATABASE.execute(prefix + setup + change)
        before = DATABASE.execute(prefix + "SELECT id,HEX(goods_specification_ids) FROM mall_product ORDER BY id;")
        try:
            DATABASE.execute(prefix + migration)
        except RuntimeError as error:
            assert "cc_sku_contract_valid" in str(error), str(error)
        else:
            raise AssertionError("冲突没有阻断迁移: " + change)
        assert DATABASE.execute(prefix + "SELECT id,HEX(goods_specification_ids) FROM mall_product ORDER BY id;") == before
        assert DATABASE.execute(prefix + "SELECT CHARACTER_MAXIMUM_LENGTH FROM information_schema.COLUMNS "
                                f"WHERE TABLE_SCHEMA='{name}' AND TABLE_NAME='mall_product' AND COLUMN_NAME='goods_specification_ids';") == "50"
    DATABASE.execute(prefix + setup + "UPDATE mall_goods_specification SET value=REPEAT('白',45) WHERE goods_id=10;" + migration)
    assert int(DATABASE.execute(prefix + "SELECT CHAR_LENGTH(goods_specification_ids) FROM mall_product WHERE id=31;")) > 50
    print("SKU迁移通过: 唯一新格式、默认值、幂等、字段扩容、历史快照及5种冲突阻断.")
    print("隔离测试库保留: " + name)


if __name__ == "__main__":
    main()
