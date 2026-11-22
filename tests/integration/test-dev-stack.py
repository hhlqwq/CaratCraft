"""在随机项目中验证本地镜像、Docker DNS 和上传卷, 不接触现有应用和数据卷."""

import json
from pathlib import Path
import secrets
import subprocess
import time
from urllib.request import Request, urlopen

ROOT = Path(__file__).resolve().parents[2]
DEPLOY = ROOT / 'deploy'


def run(*args, content=None):
    """执行测试项目 Docker 操作并保留失败原因."""
    return subprocess.run(['docker', *args], input=content, cwd=ROOT, encoding='utf-8',
                          capture_output=True, check=True, timeout=420).stdout.strip()


def local_stack(project):
    """以公开模板解析实际 Compose, 将容器名、持久卷、端口及密钥隔离."""
    environment = ['compose', '-p', project, '--env-file', str(DEPLOY / '.env.example')]
    config = json.loads(run(*environment, '-f', str(DEPLOY / 'docker-compose.yml'),
                            'config', '--format', 'json'))
    password = secrets.token_hex(32)
    for name, service in config['services'].items():
        service.pop('container_name', None)
        service.pop('build', None)
        service['restart'] = 'no'
        service.pop('ports', None)
        settings = service.setdefault('environment', {})
        for key in ('MYSQL_ROOT_PASSWORD', 'MYSQL_PASSWORD', 'REDIS_PASSWORD', 'JWT_SECRET'):
            if key in settings:
                settings[key] = password
        if name in ('platform-admin', 'platform-api'):
            service['volumes'] = [volume for volume in service['volumes'] if volume['type'] == 'volume']
        if name == 'nginx':
            service['ports'] = [{'target': 80, 'published': '0', 'host_ip': '127.0.0.1'}]
    prefix = [*environment, '--project-directory', str(DEPLOY), '-f', '-']
    return config, prefix


def check_public(base):
    """核对小程序四类真实读取, 保持各接口现有 HTTP 方法."""
    for method, endpoint in [('POST', 'index/newGoods'), ('POST', 'goods/count'),
                             ('GET', 'community/list?page=1&size=2'),
                             ('GET', 'customization/config?goodsId=1999105')]:
        with urlopen(Request(f'{base}/platform-framework-api/app/{endpoint}', method=method), timeout=10) as response:
            payload = json.load(response)
            assert response.status == 200 and payload['code'] == 0 and payload['data'] is not None
        print(f'{method} {endpoint}: HTTP 200, code=0', flush=True)


def main():
    """验证隔离数据初始化、镜像版本、转发与重启后的上传访问."""
    project = 'caratcraft-env-' + secrets.token_hex(5)
    config, prefix = local_stack(project)

    def compose(*args):
        """仅向本次随机标识的 Compose 项目传递合成配置."""
        return run(*prefix, *args, content=json.dumps(config))

    def container(service):
        """取得隔离服务的真实容器标识."""
        return compose('ps', '-q', service)

    up = ['up', '-d', '--no-build', '--pull', 'never', '--wait', '--wait-timeout', '300']
    try:
        config['services']['mysql']['environment']['CARATCRAFT_DB_INIT'] = 'true'
        compose(*up, 'mysql', 'redis')
        config['services']['mysql']['environment']['CARATCRAFT_DB_INIT'] = 'false'
        compose('rm', '--stop', '--force', 'mysql')
        compose(*up)
        labels = []
        for service in ('platform-admin', 'platform-api', 'nginx'):
            metadata = json.loads(run('inspect', container(service)))[0]
            assert metadata['State']['Health']['Status'] == 'healthy'
            image_labels = metadata['Config']['Labels']
            labels.append((image_labels['org.opencontainers.image.revision'], image_labels['io.caratcraft.source-digest']))
        assert len(set(labels)) == 1 and len(labels[0][0]) == 40 and len(labels[0][1]) == 64
        print(f'应用镜像一致: Git {labels[0][0]} · 源码摘要 {labels[0][1]}', flush=True)
        base = 'http://' + compose('port', 'nginx', '80')
        check_public(base)
        assert run('exec', container('mysql'), 'sh', '-c',
                   'MYSQL_PWD="$MYSQL_PASSWORD" mysql -u"$MYSQL_USER" -D"$MYSQL_DATABASE" -Nse "SELECT version FROM CC_SCHEMA_VERSION WHERE id=1"') == '1'
        assert run('exec', container('redis'), 'sh', '-c',
                   'REDISCLI_AUTH="$REDIS_PASSWORD" redis-cli ping') == 'PONG'
        marker = secrets.token_hex(12)
        run('exec', container('platform-admin'), 'sh', '-c', 'printf %s "$1" > /data/uploads/env-test.png', 'sh', marker)
        compose('restart', 'platform-admin', 'nginx')
        for attempt in range(20):
            try:
                restarted_base = 'http://' + compose('port', 'nginx', '80')
                with urlopen(restarted_base + '/upload/env-test.png', timeout=3) as response:
                    assert response.read().decode('ascii') == marker
                break
            except OSError:
                if attempt == 19:
                    raise
                time.sleep(1)
        print('MySQL 结构版本、Redis PONG、应用重启后上传卷访问均通过。', flush=True)
    finally:
        compose('down')
        for name in config['volumes']:
            volume = f'{project}_{name}'
            try:
                metadata = json.loads(run('volume', 'inspect', volume))[0]
            except subprocess.CalledProcessError:
                continue
            assert metadata['Labels']['com.docker.compose.project'] == project
            run('volume', 'rm', volume)


if __name__ == '__main__':
    try:
        main()
    except subprocess.CalledProcessError as error:
        raise SystemExit(error.stderr)
