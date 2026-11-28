"""在维护镜像内验证签名、路径、完整性、版本与任务互斥, 不操作业务数据库."""

import importlib.util
import json
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch
import uuid
import zipfile

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'deploy/maintenance'))
import packages
import upgrades


class MaintenanceTests(unittest.TestCase):
    """每例使用独立目录及临时签名密钥, 不接触正式信任根或Docker."""

    def setUp(self):
        """创建隔离文件系统和测试发行签名密钥."""
        self.folder = tempfile.TemporaryDirectory()
        self.root = Path(self.folder.name)
        self.key = self.root / 'private.pem'
        subprocess.run(['openssl', 'genpkey', '-algorithm', 'ED25519', '-out', str(self.key)], check=True, capture_output=True)
        self.public = self.root / 'public.pem'
        subprocess.run(['openssl', 'pkey', '-in', str(self.key), '-pubout', '-out', str(self.public)], check=True, capture_output=True)
        self.trust = patch.object(packages, 'PUBLIC_KEY', self.public)
        self.trust.start()
        self.state = patch.object(upgrades, 'STATE', self.root / 'state')
        self.state.start()
        self.control = patch.object(upgrades, 'CONTROL', self.root / 'control')
        self.control.start()

    def tearDown(self):
        """仅释放本例资源, 生产密钥和卷均未挂载到该测试."""
        self.control.stop()
        self.state.stop()
        self.trust.stop()
        self.folder.cleanup()

    def package(self, *, architecture='arm64', version='0.1.1', extra=None, corrupt=False):
        """生成最小完整格式的签名夹具, 镜像内容不会交给Docker加载."""
        deployment = {key: {'id': 'sha256:' + 'a' * 64, 'sourceDigest': 'b' * 64}
                      for key in ('backend', 'admin-ui', 'maintenance')}
        files = {'images.tar': b'isolated-test', 'release.json': json.dumps({'version': version}).encode(),
                 'deployment.json': json.dumps(deployment).encode(), 'database/upgrade/local/manifest.txt': b'0001 test',
                 'database/upgrade/local/current-schema.sql': b'SELECT 1;'}
        if extra:
            files.update(extra)
        import hashlib
        manifest = {'format': 1, 'version': version, 'minimumVersion': '0.1.0', 'builtAt': '2026-11-28T00:00:00Z',
                    'architecture': architecture, 'files': {name: hashlib.sha256(value).hexdigest() for name, value in files.items()}}
        content = json.dumps(manifest).encode()
        unsigned = self.root / 'manifest.json'
        signed = self.root / 'manifest.sig'
        unsigned.write_bytes(content)
        subprocess.run(['openssl', 'pkeyutl', '-sign', '-rawin', '-inkey', str(self.key), '-in', str(unsigned), '-out', str(signed)], check=True, capture_output=True)
        package = self.root / (str(uuid.uuid4()) + '.zip')
        with zipfile.ZipFile(package, 'x') as archive:
            archive.writestr('manifest.json', content)
            archive.writestr('manifest.sig', signed.read_bytes())
            for name, value in files.items():
                archive.writestr(name, value + b'changed' if corrupt and name == 'images.tar' else value)
        return package

    def test_bash_migration_fingerprints(self):
        """实际Bash迁移库对两项正式SQL生成与独立SHA256计算一致的指纹."""
        import hashlib
        import re
        command = 'source "$1/deploy/scripts/migrations.sh"; ROOT_DIR="$1"; migration_batch "" preflight'
        result = subprocess.run(['bash', '-euc', command, 'fingerprint', str(ROOT)], check=True, capture_output=True, text=True)
        fingerprints = re.findall(r"checksum <> '([0-9A-F]{64})'", result.stdout)
        expected = []
        for line in (ROOT / 'database/upgrade/local/manifest.txt').read_text().splitlines():
            _, name = line.split(' ')
            directory = ROOT / 'database/upgrade' / name
            files = ''.join(hashlib.sha256((directory / file).read_bytes()).hexdigest().upper() for file in ('check.sql', 'apply.sql'))
            expected.append(hashlib.sha256(files.encode()).hexdigest().upper())
        self.assertEqual(expected, fingerprints)
        self.assertNotIn('INSERT INTO CC_SCHEMA_MIGRATION VALUES', result.stdout)

    def test_valid_signed_package(self):
        """完整合法包通过, 文件和镜像身份真实从签名清单读出."""
        result = packages.validate(self.package(), self.root / 'output', 'arm64')
        self.assertEqual(result['version'], '0.1.1')
        self.assertEqual(set(result['deployment']), {'backend', 'admin-ui', 'maintenance'})

    def test_content_tampering(self):
        """签名后改变镜像内容必须失败并清除不完整解压目录."""
        with self.assertRaisesRegex(ValueError, '完整性'):
            packages.validate(self.package(corrupt=True), self.root / 'output', 'arm64')
        self.assertFalse((self.root / 'output').exists())

    def test_wrong_architecture(self):
        """发行架构不匹配时在镜像加载前拒绝."""
        with self.assertRaisesRegex(ValueError, '架构'):
            packages.validate(self.package(architecture='amd64'), self.root / 'output', 'arm64')

    def test_path_traversal_and_executable(self):
        """签名也不能放开越界路径或包内任意可执行脚本."""
        for name in ('../escape', '/absolute', 'database/upgrade/run.sh', 'C:/escape', 'folder\\escape'):
            with self.subTest(name=name), self.assertRaises(ValueError):
                packages.validate(self.package(extra={name: b'bad'}), self.root / 'output', 'arm64')

    def test_untrusted_signature(self):
        """更换预置信任公钥后不能接受旧签名, 包内声明不能替换信任根."""
        package = self.package()
        subprocess.run(['openssl', 'genpkey', '-algorithm', 'ED25519', '-out', str(self.key)], check=True, capture_output=True)
        subprocess.run(['openssl', 'pkey', '-in', str(self.key), '-pubout', '-out', str(self.public)], check=True, capture_output=True)
        with self.assertRaisesRegex(ValueError, '签名'):
            packages.validate(package, self.root / 'output', 'arm64')

    def test_version_rejects_git_or_prerelease(self):
        """软件发行与Git及开发预发布编号保持独立."""
        for value in ('main', '01623d4e', '1.0.0-alpha.1', '01.0.0', None):
            with self.subTest(value=value), self.assertRaises(ValueError):
                packages.version_tuple(value)

    def test_task_lock_and_maintenance_guard(self):
        """独立文件句柄不能重入维护锁, 维护标记禁止源码部署."""
        with upgrades.task_lock():
            with self.assertRaisesRegex(ValueError, '维护任务'):
                with upgrades.task_lock():
                    self.fail('互斥锁失效')
        upgrades.maintenance(True, str(uuid.uuid4()))
        with self.assertRaises(ValueError):
            upgrades.assert_idle()

    def test_recovery_requires_marker_and_complete_backup(self):
        """失败恢复缺少一致备份时绝不执行数据库删除."""
        identifier = str(uuid.uuid4())
        job = {'id': identifier, 'original': {'deployment': {}}, 'backupComplete': False}
        with patch('docker_api.database') as database:
            with self.assertRaises((ValueError, FileNotFoundError)):
                upgrades.restore(job)
            database.assert_not_called()


if __name__ == '__main__':
    unittest.main()
