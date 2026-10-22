"""验证目录迁移后的真实交付输入、初始化顺序和不可改写迁移, 不连接数据库."""

import hashlib
import importlib.util
import json
from pathlib import Path
import re
import subprocess
import sys
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
        """数字前缀保持原初始化顺序, 初始化不再依赖历史SOURCE或目录."""
        record = json.loads((ROOT / "docs/资料/仓库结构.json").read_text("utf-8"))
        mapping = sorted((old, new) for old, new in record["files"].items()
                         if new.startswith("database/init/"))
        expected = [new for _, new in mapping]
        actual = sorted(file.relative_to(ROOT).as_posix()
                        for file in (ROOT / "database/init").iterdir() if file.is_file())
        self.assertEqual(expected, actual)
        for name in actual:
            self.assertNotRegex((ROOT / name).read_text("utf-8-sig"), r"(?im)^\s*SOURCE\s+[^\r\n;]+;")
        self.assertFalse((ROOT / "database/legacy").exists())

    def test_generated_migrations_match_unique_sources(self):
        """只读生成校验必须覆盖全部迁移且不改写初始化文件."""
        target = ROOT / "database/init/130_schema_versions.sql"
        before = target.read_bytes()
        subprocess.run([sys.executable, str(ROOT / "tools/database/build-migration-baseline.py"),
                        "--check"], check=True, capture_output=True)
        self.assertEqual(before, target.read_bytes())

    def test_runtime_configuration_and_assets_have_one_source(self):
        """运行配置和素材台账不得重新引入已删除的备用入口或无用副本."""
        configuration = ROOT / "apps/admin-ui/static/config"
        self.assertEqual(["index.js"], sorted(file.name for file in configuration.iterdir()))
        vite = (ROOT / "apps/admin-ui/vite.config.mjs").read_text("utf-8")
        self.assertNotIn("path.join(output, 'config')", vite)
        self.assertIn("./static/config/index.js", vite)
        records = json.loads((ROOT / "legal/project-assets.json").read_text("utf-8"))
        for record in records:
            file = ROOT / record["file"]
            self.assertEqual(record["sha256"], hashlib.sha256(file.read_bytes()).hexdigest(), file)
        self.assertFalse((ROOT / "data").exists())

    def test_compose_preserves_project_identity_and_build_context(self):
        """配置迁移不能默默更换默认卷归属或丢失根目录构建上下文."""
        compose = (ROOT / "deploy/docker-compose.yml").read_text("utf-8")
        self.assertRegex(compose, r"(?m)^name: platform-wxshop$")
        self.assertEqual(compose.count("context: .."), 3)
        self.assertIn("../database/init:/docker-entrypoint-initdb.d:ro", compose)
        self.assertNotIn("/opt/caratcraft/legacy", compose)
        self.assertIn("./secrets:/run/secrets:ro", compose)


if __name__ == "__main__":
    unittest.main(verbosity=2)
