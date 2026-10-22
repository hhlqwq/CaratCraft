"""验证目录迁移后的真实交付输入、初始化顺序和不可改写迁移, 不连接数据库."""

import hashlib
import importlib.util
import json
from pathlib import Path
import re
import unittest


ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location(
    "structure_delivery", ROOT / "tools/delivery/package-commercial-application.py"
)
DELIVERY = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(DELIVERY)


class RepositoryStructureTest(unittest.TestCase):
    """以实际源码目录和迁移前清单验证跨工具的一致性."""

    def test_source_export_contains_all_runtime_build_inputs(self):
        """新源码包必须保留容器所需源码、锁文件和部署配置."""
        selected = {file.relative_to(ROOT).as_posix() for file in DELIVERY.source_files()}
        required = {
            ".dockerignore", "backend/pom.xml", "apps/admin-ui/package.json",
            "apps/admin-ui/pnpm-lock.yaml", "apps/miniapp/app.json",
            "deploy/docker-compose.yml", "deploy/Dockerfile.backend",
            "deploy/Dockerfile.admin-ui", "deploy/.env.example",
            "legal/UPSTREAM-LICENSE", "tools/quality/check-project-conventions.cjs",
            "tests/python/test_repository_structure.py", "docs/资料/仓库结构.json",
        }
        self.assertFalse(required - selected, required - selected)
        self.assertFalse(any(name.startswith("vendor/") for name in selected))
        self.assertFalse(any(set(Path(name).parts) & {"target", "dist", "node_modules", "packages"}
                             for name in selected))

    def test_version_migrations_keep_original_bytes_and_names(self):
        """所有受控V版本与迁移前SHA256一致, 不通过重新命名绕过台账."""
        record = json.loads((ROOT / "docs/资料/仓库结构.json").read_text("utf-8"))
        expected = record["versionSqlSha256"]
        actual = {file.relative_to(ROOT).as_posix(): hashlib.sha256(file.read_bytes()).hexdigest()
                  for file in (ROOT / "database/migrations").glob("V*.sql")}
        self.assertEqual(expected, actual)

    def test_initialization_keeps_order_and_resolvable_sources(self):
        """数字前缀保持原初始化顺序, 显式SOURCE仍指向随包存在的历史辅助SQL."""
        record = json.loads((ROOT / "docs/资料/仓库结构.json").read_text("utf-8"))
        mapping = sorted((old, new) for old, new in record["files"].items()
                         if new.startswith("database/init/"))
        expected = [new for _, new in mapping]
        actual = sorted(file.relative_to(ROOT).as_posix()
                        for file in (ROOT / "database/init").iterdir() if file.is_file())
        self.assertEqual(expected, actual)
        for name in actual:
            for source in re.findall(r"(?im)^SOURCE[ \t]+([^\r\n;]+);[ \t]*$",
                                     (ROOT / name).read_text("utf-8-sig")):
                locations = {
                    "/docker-entrypoint-initdb.d/": "database/init",
                    "/opt/caratcraft/legacy/": "database/legacy",
                }
                matches = [(prefix, directory) for prefix, directory in locations.items()
                           if source.startswith(prefix)]
                self.assertEqual(len(matches), 1, source)
                prefix, directory = matches[0]
                target = ROOT / directory / source[len(prefix):]
                self.assertTrue(target.resolve().is_relative_to((ROOT / "database").resolve()))
                self.assertTrue(target.is_file(), target)

    def test_compose_preserves_project_identity_and_build_context(self):
        """配置迁移不能默默更换默认卷归属或丢失根目录构建上下文."""
        compose = (ROOT / "deploy/docker-compose.yml").read_text("utf-8")
        self.assertRegex(compose, r"(?m)^name: platform-wxshop$")
        self.assertEqual(compose.count("context: .."), 3)
        self.assertIn("../database/init:/docker-entrypoint-initdb.d:ro", compose)
        self.assertIn("../database/legacy:/opt/caratcraft/legacy:ro", compose)
        self.assertIn("./secrets:/run/secrets:ro", compose)


if __name__ == "__main__":
    unittest.main(verbosity=2)
