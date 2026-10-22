"""验证地区种子批量提交边界与记录原始内容, 不连接数据库."""

import hashlib
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]


class RegionSeedTest(unittest.TestCase):
    """初始化性能整改不能改变任何地区记录或把DDL混入数据事务."""

    def test_records_keep_original_content_and_count(self):
        """全部地区INSERT与整改前按LF记录的摘要一致, 不靠减少数据加速."""
        source = (ROOT / "database/init/060_sys_region.sql").read_text("utf-8")
        records = [line for line in source.splitlines(keepends=True)
                   if line.startswith("INSERT INTO `sys_region`")]
        self.assertEqual(len(records), 46181)
        self.assertEqual(hashlib.sha256("".join(records).encode("utf-8")).hexdigest(),
                         "5f3b819f643df2737199eca18a4872018d14a012a0b46d57d476a337d61008b3")

    def test_transaction_only_wraps_seed_records(self):
        """建表先执行, 全部INSERT由一个显式事务提交, 中间没有隐式提交DDL."""
        source = (ROOT / "database/init/060_sys_region.sql").read_text("utf-8")
        self.assertEqual(source.count("START TRANSACTION;"), 1)
        self.assertEqual(source.count("COMMIT;"), 1)
        ddl, inserts = source.split("START TRANSACTION;")
        self.assertIn("CREATE TABLE `sys_region`", ddl)
        self.assertNotIn("INSERT INTO `sys_region`", ddl)
        self.assertTrue(inserts.rstrip().endswith("COMMIT;"))
        self.assertNotRegex(inserts, r"(?im)^\s*(?:CREATE|DROP|ALTER|TRUNCATE)\b")


if __name__ == "__main__":
    unittest.main()
