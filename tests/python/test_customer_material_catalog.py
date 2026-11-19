"""按客户资料独立核对候选规格和同源初始化种子生成, 不执行SQL."""

import json
from pathlib import Path
import subprocess
import sys
import unittest


ROOT = Path(__file__).resolve().parents[2]


class CustomerMaterialCatalogTest(unittest.TestCase):
    """保证目录来源指纹、各品种区间和待实测语义没有被发布数据替代."""

    def test_all_customer_pearl_intervals_are_present(self):
        """按甲方原清单逐品种核对区间, 不只检查总行数."""
        catalog = json.loads((ROOT / "backend/platform-biz/src/main/resources/catalog/customer-material-catalog.json").read_text("utf-8"))
        expected = {"澳白": range(8, 14), "金珠": range(8, 14), "大溪地": range(8, 14),
                    "Akoya白色": range(4, 9), "Akoya真多麻": range(4, 9), "Akoya羽黄金": range(4, 9),
                    "淡水AK": range(4, 8), "爱迪生": range(9, 14), "淡水无核白": range(3, 11),
                    "淡水无核紫": range(3, 11), "淡水无核橙": range(3, 11)}
        pearls = [json.loads(row["materialParams"]) for row in catalog["options"] if row["componentType"] == "pearl"]
        for name, values in expected.items():
            intervals = {(row["candidateDiameterMinMm"], row["candidateDiameterMaxMm"])
                         for row in pearls if row["variety"] == name}
            self.assertEqual({(low, low + 1) for low in values}, intervals, name)
        self.assertEqual(1, sum(row["variety"] == "巴洛克" for row in pearls))
        self.assertTrue(all("diameterMm" not in row for row in pearls))
        self.assertEqual("4cdb7d6266146989ee6bac5fbba25069612578aaa3f27ee3456a6b299c9f1c0f", catalog["sourceSha256"])

    def test_candidate_migration_matches_single_source(self):
        """初始化种子严格对应资源且重复生成不改写受控源码."""
        path = ROOT / "database/init/030_material_catalog.sql"
        before = path.read_bytes()
        subprocess.run([sys.executable, str(ROOT / "tools/database/build-customer-material-catalog.py"), "--check"], check=True, capture_output=True)
        self.assertEqual(before, path.read_bytes())
        self.assertTrue(path.read_text("utf-8").startswith("SET NAMES utf8mb4;"))


if __name__ == "__main__":
    unittest.main(verbosity=2)
