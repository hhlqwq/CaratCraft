"""验证许可盘点不能因跨平台构建路径缺失而空集合通过, 不创建临时文件."""

import importlib.util
import json
import os
from pathlib import Path
import unittest
from unittest.mock import patch

ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location(
    "frontend_license_inventory", ROOT / "tools/legal/build-license-bundle.py")
SUBJECT = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(SUBJECT)


class FrontendLicenseGraphTest(unittest.TestCase):
    """使用只读路径替身验证缺失、边界和明确包快照契约."""

    def run_graph(self, modules, snapshot="", metadata=None):
        """只向盘点函数提供声明的构建图和元数据路径."""
        stats = ROOT / ".tools/copyright/webpack-stats.json"
        files = {stats, *(metadata or [])}
        with patch.dict(os.environ, {"CARATCRAFT_FRONTEND_NODE_MODULES": snapshot}), \
                patch.object(Path, "is_file", lambda value: value in files), \
                patch.object(Path, "read_text", return_value=json.dumps({"modules": modules})):
            return SUBJECT.frontend_package_roots()

    def test_unavailable_module_is_rejected(self):
        """容器绝对路径在宿主机不存在时必须失败, 不静默忽略全部依赖."""
        with self.assertRaises(FileNotFoundError):
            self.run_graph([{"identifier": "/unavailable/node_modules/example/index.js"}])

    def test_empty_graph_is_rejected(self):
        """没有npm归属的构建图不能作为前端许可验收证据."""
        with self.assertRaises(ValueError):
            self.run_graph([])

    def test_explicit_snapshot_preserves_package_identity(self):
        """跨平台快照保持pnpm及作用域包路径, 无需复制源文件或符号链接."""
        snapshot = ROOT / ".tools/copyright/synthetic-license-snapshot"
        package = snapshot / ".pnpm/example@1.0/node_modules/@scope/example"
        actual = self.run_graph(
            [{"identifier": "/workspace/node_modules/.pnpm/example@1.0/node_modules/@scope/example/index.js"}],
            str(snapshot), [package / "package.json"])
        self.assertEqual([package], actual)

    def test_snapshot_does_not_use_parent_project_metadata(self):
        """快照中包缺失时不得向上误用项目package.json充当依赖证据."""
        snapshot = ROOT / ".tools/copyright/synthetic-license-snapshot"
        with self.assertRaises(FileNotFoundError):
            self.run_graph([{"identifier": "/workspace/node_modules/example/index.js"}],
                           str(snapshot), [ROOT / "package.json"])


if __name__ == "__main__":
    unittest.main()
