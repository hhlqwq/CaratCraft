"""用真实微信WXML编译器复现转义错误, 不用替身正则冒充编译."""

import os
from pathlib import Path
import subprocess
import tempfile
import unittest


class MiniappCompilationTest(unittest.TestCase):
    """验证官方编译器能拒绝旧表达式并接受修正后的逻辑与和查询参数."""

    @classmethod
    def setUpClass(cls):
        """仅复用明确指定的现有开发者工具, 不下载编译器."""
        install = os.environ.get("CARATCRAFT_WECHAT_DEVTOOLS")
        if not install:
            raise unittest.SkipTest("需要显式设置已有CARATCRAFT_WECHAT_DEVTOOLS")
        cls.compiler = Path(install) / "resources/app.asar.unpacked/node_modules/wcc-exec/wcc.exe"
        if not cls.compiler.is_file():
            raise RuntimeError("指定微信WXML编译器不存在")

    def compile(self, content):
        """在约定目录编译独立页面并只清理本次样例及输出."""
        folder = Path(os.environ.get("CARATCRAFT_TEST_TEMP",
                                    "D:/tmp/hailongcodex" if os.name == "nt" else "/tmp/hailongcodex"))
        folder.mkdir(parents=True, exist_ok=True)
        with tempfile.TemporaryDirectory(prefix="wxml-regression-", dir=folder) as directory:
            source = Path(directory) / "index.wxml"
            source.write_text(content, encoding="utf-8")
            return subprocess.run([str(self.compiler), "-o", str(Path(directory) / "output.js"), "./index.wxml"],
                                  cwd=directory, capture_output=True, text=True,
                                  encoding="utf-8", errors="replace")

    def test_encoded_boolean_operator_is_rejected(self):
        """原协议页表达式必须被真实编译器拒绝, 防止错误回归."""
        result = self.compile('<view wx:if="{{!loading &amp;&amp; !documents.length}}">空</view>')
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("unexpected", result.stderr + result.stdout)

    def test_raw_operator_and_query_separator_compile(self):
        """修正后的表达式和协议版本查询参数可由官方编译器直接解析."""
        result = self.compile('<view wx:if="{{!loading && !documents.length}}">空</view>'
                              '<navigator url="/pages/legal?kind={{kind}}&version={{version}}">协议</navigator>')
        self.assertEqual(result.returncode, 0, result.stderr)


if __name__ == "__main__":
    unittest.main(verbosity=2)
