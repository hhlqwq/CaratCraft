"""验证台账只更新固定文档的指定区块, 不覆盖人工维护内容."""

from datetime import datetime
import importlib.util
import json
import os
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location("remediation", ROOT / "tools/maintenance/update-remediation.py")
SUBJECT = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(SUBJECT)


class DocumentationMaintenanceTest(unittest.TestCase):
    """合成源数据和手工段落, 不读写真实台账."""

    def setUp(self):
        """用明确日期和随机子目录隔离本用例, 无论失败与否均清理."""
        day = os.environ.get("CARATCRAFT_TEST_DATE", datetime.now().strftime("%Y-%m-%d"))
        parent = Path("D:/tmp/hailongcodex") / datetime.strptime(day, "%Y-%m-%d").strftime("%Y-%m-%d")
        parent.mkdir(parents=True, exist_ok=True)
        temporary = tempfile.TemporaryDirectory(prefix="documentation-", dir=parent)
        self.addCleanup(temporary.cleanup)
        self.root = Path(temporary.name)
        materials = self.root / "docs/资料"
        materials.mkdir(parents=True)
        (materials / "历史问题.json").write_text(json.dumps({"baseline": "test", "items": [
            {"id": "A001", "priority": "P1", "title": "合成问题"}]}), encoding="utf-8")
        self.state = materials / "整改台账.json"
        self.state.write_text(json.dumps({"items": [{"id": "A001", "status": "已修复",
            "changes": "原有说明", "validation": "原有证据"}]}), encoding="utf-8")
        self.document = self.root / "docs/项目维护文档.md"
        self.document.write_text("手工部署说明\n<!-- 整改台账:开始 -->\n旧表\n<!-- 整改台账:结束 -->\n手工恢复说明\n", encoding="utf-8")

    def test_update_preserves_manual_sections_and_existing_evidence(self):
        """重建目录中的台账时保留人工内容和已填写的整改证据."""
        with patch.object(SUBJECT, "ROOT", self.root):
            SUBJECT.main()
        content = self.document.read_text("utf-8")
        self.assertTrue(content.startswith("手工部署说明\n"))
        self.assertTrue(content.endswith("手工恢复说明\n"))
        self.assertIn("A001", content)
        self.assertEqual(json.loads(self.state.read_text("utf-8"))["items"][0]["validation"], "原有证据")
        self.assertEqual(len(list((self.root / "docs").glob("*.md"))), 1)

    def test_missing_markers_change_neither_document_nor_state(self):
        """缺少标记时明确失败, 不先写台账再误覆盖手工内容."""
        self.document.write_text("仅有手工内容", encoding="utf-8")
        before = self.state.read_bytes()
        with patch.object(SUBJECT, "ROOT", self.root), self.assertRaisesRegex(ValueError, "唯一"):
            SUBJECT.main()
        self.assertEqual(self.state.read_bytes(), before)
        self.assertEqual(self.document.read_text("utf-8"), "仅有手工内容")

    def test_repeated_generation_does_not_duplicate_tables(self):
        """相同台账重复生成保持字节一致, 不在文档尾部不断追加报告."""
        with patch.object(SUBJECT, "ROOT", self.root):
            SUBJECT.main()
            before = self.document.read_bytes()
            SUBJECT.main()
        self.assertEqual(self.document.read_bytes(), before)


if __name__ == "__main__":
    unittest.main(verbosity=2)
