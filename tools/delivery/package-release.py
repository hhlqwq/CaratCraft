"""开发机在Docker中打包已构建镜像, 商家服务器只校验和加载签名发行包."""

import argparse
import hashlib
import json
from pathlib import Path
import re
import subprocess
import tempfile
import zipfile


# 固定镜像名只由标准构建维护, 发行包记录不可变ID而非可漂移标签.
IMAGES = {'backend': 'carat-craft-backend:local', 'admin-ui': 'carat-craft-admin-ui:local',
          'maintenance': 'carat-craft-maintenance:local'}


def digest(path):
    """流式读取大镜像归档, 不让包大小决定内存占用."""
    value = hashlib.sha256()
    with open(path, 'rb') as source:
        for block in iter(lambda: source.read(1024 * 1024), b''):
            value.update(block)
    return value.hexdigest()


def main():
    """仅发布当前源码已构建镜像及显式迁移资源, 私钥不进入发行包."""
    parser = argparse.ArgumentParser()
    parser.add_argument('--root', default='/source')
    parser.add_argument('--output', required=True)
    parser.add_argument('--key', required=True)
    parser.add_argument('--deployment-project')
    args = parser.parse_args()
    root = Path(args.root)
    destination = Path(args.output)
    if destination.exists():
        raise ValueError('目标发行包已存在, 不会覆盖')
    release = json.loads((root / 'release.json').read_text())
    if not re.fullmatch(r'\d+\.\d+\.\d+', release['version']):
        raise ValueError('请先完成发行版本确认')
    deployment = {}
    architecture = None
    built_at = []
    for component, name in IMAGES.items():
        image = json.loads(subprocess.check_output(['docker', 'image', 'inspect', name]))[0]
        if image['Os'] != 'linux' or (architecture is not None and architecture != image['Architecture']):
            raise ValueError('发行镜像操作系统或CPU架构不一致')
        if image['Config'].get('Labels', {}).get('org.opencontainers.image.version') != release['version']:
            raise ValueError('镜像实际软件版本与发行版本不一致')
        built_at.append(image['Created'])
        architecture = image['Architecture']
        source = image['Config'].get('Labels', {}).get('io.caratcraft.source-digest', '')
        if not re.fullmatch('[a-f0-9]{64}', source):
            raise ValueError('镜像缺少标准构建输入摘要')
        deployment[component] = {'id': image['Id'], 'sourceDigest': source}
    with tempfile.TemporaryDirectory(prefix='cc-release-') as folder:
        staging = Path(folder)
        print('正在导出三项应用镜像, 只导出ID且不改动本地镜像标签...', flush=True)
        subprocess.run(['docker', 'image', 'save', '--output', str(staging / 'images.tar'),
                        *[item['id'] for item in deployment.values()]], check=True)
        (staging / 'release.json').write_text(json.dumps(release, ensure_ascii=False))
        (staging / 'deployment.json').write_text(json.dumps(deployment))
        files = {file.name: file for file in staging.iterdir()}
        for folder_name in ('legal', 'database/upgrade/local'):
            for file in (root / folder_name).rglob('*'):
                if file.is_file() and not file.is_symlink():
                    files[file.relative_to(root).as_posix()] = file
        for line in (root / 'database/upgrade/local/manifest.txt').read_text().splitlines():
            sequence, directory = line.split(' ')
            for name in ('check.sql', 'apply.sql'):
                relative = 'database/upgrade/' + directory + '/' + name
                files[relative] = root / relative
        manifest = {'format': 1, 'version': release['version'], 'minimumVersion': '0.1.0',
                    'architecture': architecture, 'builtAt': max(built_at),
                    'files': {name: digest(file) for name, file in sorted(files.items())}}
        if args.deployment_project:
            manifest['deploymentProject'] = args.deployment_project
        content = json.dumps(manifest, ensure_ascii=False, sort_keys=True, separators=(',', ':')).encode()
        (staging / 'manifest.json').write_bytes(content)
        subprocess.run(['openssl', 'pkeyutl', '-sign', '-rawin', '-inkey', args.key,
                        '-in', str(staging / 'manifest.json'), '-out', str(staging / 'manifest.sig')], check=True)
        print('正在写入签名发行包...', flush=True)
        try:
            with zipfile.ZipFile(destination, 'x', compression=zipfile.ZIP_STORED, allowZip64=True) as archive:
                archive.write(staging / 'manifest.json', 'manifest.json')
                archive.write(staging / 'manifest.sig', 'manifest.sig')
                for name, file in sorted(files.items()):
                    archive.write(file, name)
            if destination.stat().st_size > 4 * 1024**3:
                raise ValueError('发行包超过当前4 GiB上传限制')
        except BaseException:
            destination.unlink(missing_ok=True)
            raise
        print('发行包已生成, SHA256=' + digest(destination), flush=True)


if __name__ == '__main__':
    main()
