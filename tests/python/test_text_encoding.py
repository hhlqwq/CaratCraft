"""确保初始化编码声明和全库乱码检查持续覆盖真实Docker默认连接场景."""

import importlib.util
from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location("encoding_check", ROOT / "tools/database/check-text-encoding.py")
ENCODING = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(ENCODING)


class TextEncodingTest(unittest.TestCase):
    """从真实误解码样本和全部初始化入口检查字符契约."""

    def test_each_initialization_declares_utf8_before_statements(self):
        """每个SQL独立连接执行, 不能依靠前一个文件设置过编码."""
        for file in sorted((ROOT / "database/init").glob("*.sql")):
            source = file.read_text("utf-8-sig")
            executable = re.sub(r"\A(?:\s+|--[^\n]*(?:\n|$)|/\*.*?\*/)*", "", source, flags=re.S)
            self.assertTrue(executable.startswith("SET NAMES utf8mb4;"), file.name)

    def test_detector_catches_latin1_and_windows1252_damage(self):
        """截图中的两种UTF-8误解码必须被识别, 正常中文及外文不得误判."""
        for text in ["社区管理", "性别", "中国", "订单超时自动关闭(5分钟)"]:
            broken = text.encode("utf-8").decode("latin1")
            self.assertRegex(broken, ENCODING.MOJIBAKE_PATTERN)
            broken = text.encode("utf-8").decode("cp1252", errors="replace")
            self.assertRegex(broken, ENCODING.MOJIBAKE_PATTERN)
        for text in ["正常中文", "男女未知", "Café", "Müller", "admin", "珍珠😀"]:
            self.assertNotRegex(text, ENCODING.MOJIBAKE_PATTERN)
        self.assertRegex("损坏\ufffd文本", ENCODING.MOJIBAKE_PATTERN)

    def test_database_identifiers_reject_cross_schema_sql(self):
        """只读工具也必须拒绝将数据库参数拼成额外SQL或其他路径."""
        for text in ["platform; DROP DATABASE platform", "a.b", "../platform", "a`b"]:
            with self.assertRaises(ValueError):
                ENCODING.identifier(text)
        self.assertEqual(ENCODING.identifier("caratcraft_init_audit_20261022_ab1234"),
                         "`caratcraft_init_audit_20261022_ab1234`")


if __name__ == "__main__":
    unittest.main()
