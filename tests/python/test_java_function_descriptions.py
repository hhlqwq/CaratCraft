"""使用已有JDK镜像验证Java职责门禁的真实语法树行为, 不下载依赖."""

from datetime import datetime
import os
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]


@unittest.skipUnless(os.environ.get("CARATCRAFT_JAVA_STYLE_DOCKER") == "true",
                     "需要显式启用已有JDK镜像的隔离语法树回归")
class JavaFunctionDescriptionTest(unittest.TestCase):
    """只在约定临时目录生成合成Java, 不编译项目或操作业务服务."""

    def check_source(self, source):
        """调用真实JDK解析器并返回退出码, 不将字符串匹配作为语法检查替身."""
        day = os.environ.get("CARATCRAFT_TEST_DATE", datetime.now().strftime("%Y-%m-%d"))
        parent = Path("D:/tmp/hailongcodex") / datetime.strptime(day, "%Y-%m-%d").strftime("%Y-%m-%d")
        parent.mkdir(parents=True, exist_ok=True)
        with tempfile.TemporaryDirectory(prefix="java-description-", dir=parent) as directory:
            fixture = Path(directory)
            code = fixture / "backend/sample/src/main/java/Sample.java"
            code.parent.mkdir(parents=True)
            code.write_text(source, encoding="utf-8")
            result = subprocess.run([
                "docker", "run", "--rm", "--pull=never", "--network", "none",
                "--mount", f"type=bind,source={ROOT / 'tools/quality'},target=/checker,readonly",
                "--mount", f"type=bind,source={fixture},target=/fixture,readonly",
                "maven:3.9.11-eclipse-temurin-21", "java", "/checker/CheckFunctionDescriptions.java", "/fixture",
            ], capture_output=True, text=True, encoding="utf-8")
            self.assertFalse(list(fixture.rglob("*.class")), "职责检查不得生成构建产物")
            return result

    def test_missing_constructor_and_method_are_rejected(self):
        """类说明不能替代构造函数和普通方法的职责说明."""
        result = self.check_source("/** 样本类. */ class Sample { Sample() {} void save() {} }")
        self.assertNotEqual(0, result.returncode)
        self.assertIn("save需要中文职责注释", result.stderr)

    def test_chinese_documentation_is_accepted(self):
        """带中文职责的构造函数和泛型方法通过只读语法树检查."""
        result = self.check_source("class Sample { /** 初始化样本. */ Sample() {} "
                                   "/** 读取本人记录. */ java.util.List<String> list() { return null; } }")
        self.assertEqual(0, result.returncode, result.stderr)

    def test_english_only_does_not_satisfy_documentation(self):
        """英文注释不能绕过项目要求的中文职责说明."""
        result = self.check_source("class Sample { /** Loads rows. */ void load() {} }")
        self.assertNotEqual(0, result.returncode)

    def test_syntax_errors_cannot_report_success(self):
        """语法错误必须阻断检查, 不因没有成功解析的方法而空集合通过."""
        result = self.check_source("class Sample { /** 读取状态. */ void load( { }")
        self.assertNotEqual(0, result.returncode)


if __name__ == "__main__":
    unittest.main()
