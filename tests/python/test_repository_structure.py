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

    def test_database_has_one_complete_initialization(self):
        """未上线项目只保留完整入口, 历史由Git追溯而非继续执行旧链."""
        expected = ["010_schema.sql", "020_seed.sql", "030_material_catalog.sql",
                    "040_regions.sql", "050_ready.sql", "060_restrict_application_user.sh"]
        actual = sorted(file.name for file in (ROOT / "database/init").iterdir())
        self.assertEqual(expected, actual)
        self.assertFalse((ROOT / "database/migrations").exists())
        self.assertFalse((ROOT / "tools/database/SchemaMigrationTool.java").exists())
        for name in actual:
            text = (ROOT / "database/init" / name).read_text("utf-8-sig")
            self.assertNotRegex(text, r"(?im)^\s*SOURCE\s+[^\r\n;]+;")
        seed = (ROOT / "database/init/020_seed.sql").read_text("utf-8")
        self.assertNotIn("DEMO-BRIDGE-001", seed)
        self.assertNotIn("CC_SCHEMA_MIGRATION", seed)

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
