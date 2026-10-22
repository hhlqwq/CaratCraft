"""验证交付文件清单和私人配置排除, 不生成大型应用包."""

import importlib.util
from datetime import datetime
import os
from pathlib import Path
import tempfile
import unittest
import zipfile
from unittest.mock import patch

ROOT = Path(__file__).resolve().parents[1]
SPEC = importlib.util.spec_from_file_location("delivery", ROOT / "tools/package-commercial-application.py")
DELIVERY = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(DELIVERY)


class DeliveryPackagingTest(unittest.TestCase):
    """使用合成文件校验排除规则, 不读取私人文件内容."""

    def setUp(self):
        """使用明确日期的约定临时目录, 用例失败时也清理本用例创建的文件."""
        value = os.environ.get("CARATCRAFT_TEST_DATE", datetime.now().strftime("%Y-%m-%d"))
        day = datetime.strptime(value, "%Y-%m-%d").strftime("%Y-%m-%d")
        directory = Path("D:/tmp/hailongcodex") / day
        directory.mkdir(parents=True, exist_ok=True)
        temporary = tempfile.TemporaryDirectory(prefix="delivery-", dir=directory)
        self.addCleanup(temporary.cleanup)
        self.directory = Path(temporary.name)

    def test_required_style_configuration_is_exported(self):
        """交付源码包含实际格式配置, 不只有执行脚本."""
        selected = {path.relative_to(ROOT).as_posix() for path in DELIVERY.source_files()}
        self.assertIn(".prettierrc.json", selected)
        self.assertIn(".editorconfig", selected)
        self.assertIn("env/build-local-test.cmd", selected)
        self.assertIn("env/start-local-test.cmd", selected)

    def test_application_excludes_private_and_development_files(self):
        """已忽略的私人配置即使存在磁盘上也不能进入应用归档."""
        temporary = self.directory
        for name in ["app.json", "project.private.config.json", "node_modules/example.js", "secrets/token.json", ".env", "private.key"]:
            file = temporary / name
            file.parent.mkdir(parents=True, exist_ok=True)
            file.write_text("synthetic-test-only", encoding="utf-8")
        selected = [file.relative_to(temporary).as_posix() for file in temporary.rglob("*")
                    if DELIVERY.application_file(file, temporary)]
        self.assertEqual(["app.json"], selected)

    def test_dirty_source_is_rejected_before_archive_creation(self):
        """脏源码不能被标记一个提交号后当作正式交付输出."""
        with patch("sys.argv", ["package", "--date", "20261021"]), \
                patch.object(DELIVERY.subprocess, "run"), \
                patch.object(DELIVERY.subprocess, "check_output", side_effect=[b"revision", b" M modified.js"]):
            with self.assertRaisesRegex(ValueError, "干净源码树"):
                DELIVERY.main()

    def test_backend_private_resources_block_delivery(self):
        """模拟旧target混入本机配置, 即使源码排除也必须拒绝交付."""
        temporary = self.directory
        for module in ("platform-admin", "platform-api"):
            destination = temporary / module / "target" / (module + ".jar")
            destination.parent.mkdir(parents=True, exist_ok=True)
            with zipfile.ZipFile(destination, "w") as archive:
                archive.writestr("BOOT-INF/classes/application-local.yml", "synthetic-test-only")
        with patch.object(DELIVERY, "VENDOR", temporary):
            with self.assertRaisesRegex(ValueError, "私人配置"):
                DELIVERY.verify_backend_files()


if __name__ == "__main__":
    unittest.main(verbosity=2)
