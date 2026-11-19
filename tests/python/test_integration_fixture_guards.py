"""验证隔离回归工具拒绝清理其他归属资源及空初始化库."""

import importlib.util
import json
from pathlib import Path
from types import SimpleNamespace
import unittest
from unittest.mock import patch

SPEC = importlib.util.spec_from_file_location(
    "isolated_runner", Path(__file__).resolve().parents[1] / "integration/run-community-regressions.py")
RUNNER = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(RUNNER)


class FixtureGuardsTest(unittest.TestCase):
    """通过命令边界核对归属, 不创建或删除真实Docker资源."""

    def test_foreign_container_is_never_removed(self):
        """归属不符或标签缺失时, 必须在删除命令前失败."""
        for labels in (None, {RUNNER.LABEL: "someone-else"}):
            with self.subTest(labels=labels):
                fixture = RUNNER.Fixtures("ours")
                fixture.containers.append("identity")
                response = SimpleNamespace(returncode=0, stdout=json.dumps([{"Config": {"Labels": labels}}]))
                with patch.object(RUNNER, "docker", return_value=response) as command:
                    with self.assertRaises(RuntimeError):
                        fixture.close()
                    self.assertEqual(command.call_count, 1)

    def test_cleanup_orders_containers_before_network(self):
        """仅删除精确登记身份, 并在容器移除后删除本次网络."""
        fixture = RUNNER.Fixtures("ours")
        fixture.containers = ["mysql", "api"]
        fixture.network = "network-id"
        response = SimpleNamespace(returncode=0, stdout=json.dumps([
            {"Config": {"Labels": {RUNNER.LABEL: "ours"}}, "Labels": {RUNNER.LABEL: "ours"}}]))
        with patch.object(RUNNER, "docker", return_value=response) as command:
            fixture.close()
            self.assertEqual([call.args[0] for call in command.call_args_list], [
                ["inspect", "api"], ["rm", "-f", "api"],
                ["inspect", "mysql"], ["rm", "-f", "mysql"],
                ["network", "inspect", "network-id"], ["network", "rm", "network-id"]])

    def test_empty_initialization_is_not_ready(self):
        """连接成功但尚未导入版本表内容时继续等待."""
        replies = [SimpleNamespace(returncode=0, stdout="0"), SimpleNamespace(returncode=0, stdout="1")]
        with patch.object(RUNNER, "docker", side_effect=replies) as command, patch.object(RUNNER.time, "sleep") as sleep:
            RUNNER.wait_for_mysql("ours", {"MYSQL_DATABASE": "isolated"})
            self.assertEqual(command.call_count, 2)
            sleep.assert_called_once_with(2)


if __name__ == "__main__":
    unittest.main()
