"""验证隔离构建快照不携带秘密, 产物无法覆盖工作区外文件或源码."""

import importlib.util
import io
import os
from pathlib import Path
import tarfile
import tempfile
from types import SimpleNamespace
import unittest
from unittest.mock import patch

ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location("native_maven", ROOT / "tests/integration/native_maven.py")
NATIVE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(NATIVE)
TEMP = Path(os.environ.get("CARATCRAFT_TEST_TEMP", ROOT / ".tools/restructure/test-temp"))


class NativeMavenSafetyTest(unittest.TestCase):
    """使用合成归档和本轮临时目录, 不运行Docker或读取业务配置."""

    def setUp(self):
        """每例创建独立工作区和输出, 不重用其他任务的文件."""
        TEMP.mkdir(parents=True, exist_ok=True)
        self.temporary = tempfile.TemporaryDirectory(dir=TEMP)
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name).resolve()
        self.output = self.root / "output"
        self.output.mkdir()
        (self.output / "native-results").mkdir()

    def archive(self, name, content=b"test", link=False):
        """生成单项受控归档, 恶意路径只写进元数据而不在磁盘创建."""
        path = self.output / "native-results/targets.tar"
        with tarfile.open(path, "w") as archive:
            item = tarfile.TarInfo(name)
            if link:
                item.type = tarfile.SYMTYPE
                item.linkname = "../../outside"
                archive.addfile(item)
            else:
                item.size = len(content)
                archive.addfile(item, io.BytesIO(content))

    def test_outputs_reject_escape_source_and_links(self):
        """产物不得越界、覆盖源码或注入链接, 拒绝后不产生后端文件."""
        for name, link in [("../outside", False),
                           ("platform-api/src/main/overwrite.java", False),
                           ("platform-api/target/link", True),
                           ("/platform-api/target/absolute", False)]:
            with self.subTest(name=name):
                self.archive(name, link=link)
                with self.assertRaises(ValueError):
                    NATIVE.restore_outputs(self.root, self.output)
                self.assertFalse((self.root / "backend").exists())

    def test_outputs_restore_only_current_module_target(self):
        """合法报告恢复到明确模块target, 保持实际报告原文."""
        self.archive("platform-api/target/surefire-reports/TEST-example.xml", b"<testsuite/>")
        NATIVE.restore_outputs(self.root, self.output)
        self.assertEqual((self.root / "backend/platform-api/target/surefire-reports/TEST-example.xml")
                         .read_bytes(), b"<testsuite/>")

    def test_source_snapshot_excludes_private_files(self):
        """即使Git边界误返回私人路径, 快照仍只携带公开模板和源码."""
        files = {"backend/pom.xml": "<project/>", "deploy/.env.example": "PUBLIC=example",
                 "deploy/.env": "PRIVATE=synthetic", "deploy/secrets/test.pem": "synthetic-key",
                 "apps/miniapp/project.private.config.json": "{}"}
        for name, text in files.items():
            file = self.root / name
            file.parent.mkdir(parents=True, exist_ok=True)
            file.write_text(text, encoding="utf-8")
        cache = self.root / "cache"
        cache.mkdir()
        # 本例输出目录由快照函数独立创建, 与产物恢复夹具分开.
        output = self.root / "snapshot"
        output.mkdir()
        response = SimpleNamespace(stdout=("\0".join(files) + "\0").encode())
        with patch.object(NATIVE.subprocess, "run", return_value=response):
            inputs, _ = NATIVE.prepare_inputs(self.root, cache, output)
        with tarfile.open(inputs / "source.tar") as archive:
            self.assertEqual(set(archive.getnames()), {"backend/pom.xml", "deploy/.env.example"})


if __name__ == "__main__":
    unittest.main()
