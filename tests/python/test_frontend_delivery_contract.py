"""交付仅接收当前默认构建入口, 不使用旧审计目录或空资源."""

import importlib.util
from pathlib import Path
import unittest
from unittest.mock import MagicMock

SPEC = importlib.util.spec_from_file_location("delivery", Path(__file__).resolve().parents[2]
                                             / "tools/delivery/package-commercial-application.py")
DELIVERY = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(DELIVERY)


class FrontendDeliveryContractTest(unittest.TestCase):
    """在文件系统查询边界检查准入条件, 不创建工作区外文件."""

    def test_complete_default_output_is_required(self):
        """只查默认路径并要求入口及JavaScript资源同时存在."""
        for index, assets, accepted in ((False, [], False), (True, [], False),
                                        (True, ["main.js"], True)):
            with self.subTest(index=index, assets=assets):
                root = MagicMock()
                directory = root.__truediv__.return_value
                directory.__truediv__.return_value.is_file.return_value = index
                directory.__truediv__.return_value.glob.return_value = assets
                if accepted:
                    self.assertIs(DELIVERY.frontend_output(root), directory)
                else:
                    with self.assertRaisesRegex(ValueError, "完整管理端产物"):
                        DELIVERY.frontend_output(root)
                root.__truediv__.assert_called_once_with("apps/admin-ui/dist")


if __name__ == "__main__":
    unittest.main()
