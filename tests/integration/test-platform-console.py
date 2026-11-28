"""使用隔离六服务验证平台真实HTTP与签名升级恢复, 不连接本机业务库."""

import base64
from datetime import date
import hashlib
import json
import os
from pathlib import Path
import secrets
import shutil
import socket
import subprocess
import sys
import time
from urllib.error import HTTPError
from urllib.request import Request, urlopen
import uuid

ROOT = Path(__file__).resolve().parents[2]
PROJECT = 'caratcraft-platform-test-' + secrets.token_hex(5)
TEMP = Path('/tmp/hailongcodex') / date.today().isoformat() / PROJECT
TEMP.mkdir(mode=0o700, parents=True)
RESULTS = []
SECRETS = []


def docker(*arguments, content=None, binary=False):
    """执行明确隔离项目操作, 错误只显示退出码, 不打印环境及认证内容."""
    result = subprocess.run(['docker', *arguments], input=content, capture_output=True,
                            text=not binary, cwd=ROOT, timeout=600)
    if result.returncode:
        error = result.stderr if isinstance(result.stderr, str) else result.stderr.decode(errors='replace')
        env_file = TEMP / '.env'
        if env_file.exists():
            for line in env_file.read_text().splitlines():
                key, _, value = line.partition('=')
                if value and any(part in key for part in ('PASSWORD', 'SECRET')): error = error.replace(value, '[已隐藏]')
        raise RuntimeError('隔离Docker操作失败: ' + arguments[0] + ', 退出码=' + str(result.returncode) + '\n' + error[-4000:])
    return result.stdout if binary else result.stdout.strip()


def checked(name, condition=True):
    """按实际断言保存结果, 不将尚未执行的场景记录为通过."""
    if not condition:
        raise AssertionError(name)
    RESULTS.append(name)
    print('通过: ' + name, flush=True)


def compose(*arguments):
    """只调用本轮随机项目、独立配置、独立端口及持久卷."""
    return docker('compose', '-p', PROJECT, '--project-directory', str(TEMP), '--env-file', str(TEMP / '.env'),
                  '-f', str(TEMP / 'docker-compose.yml'), *arguments)


def database(sql):
    """隔离库密码仅由其MySQL容器内部读取, 不输出查询敏感字段."""
    return docker('exec', '-i', PROJECT + '-mysql', 'sh', '-ec',
                  'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -uroot --default-character-set=utf8mb4 --batch --skip-column-names "$MYSQL_DATABASE"', content=sql)


def maintenance(command):
    """运行隔离维护服务内部命令, 不操作正式维护卷."""
    return compose('exec', '-T', 'maintenance', 'python', '/app/server.py', command)


def request(base, endpoint, data=None, token=None, raw=False):
    """经真实Nginx及Shiro发起HTTP, 令牌只存在内存."""
    headers = {'Content-Type': 'application/octet-stream' if raw else 'application/json'}
    if token:
        headers['token'] = token
    body = data if raw else json.dumps(data).encode() if data is not None else None
    try:
        with urlopen(Request(base + '/platform-framework/' + endpoint, data=body, headers=headers), timeout=180) as response:
            return response.status, response.headers, response.read()
    except HTTPError as error:
        return error.code, error.headers, error.read()


def api(base, endpoint, data=None, token=None):
    """返回成功接口数据, 敏感响应绝不进入断言错误文本."""
    status, _, content = request(base, endpoint, data, token)
    result = json.loads(content)
    if status != 200 or result.get('code') != 0:
        raise AssertionError('接口未通过: ' + endpoint + ', HTTP=' + str(status) + ', code=' + str(result.get('code')))
    return result.get('data')


def encrypt(password):
    """沿用既有AES登录协议, 不更改认证实现."""
    SECRETS.append(password)
    data = password.encode()
    data += b'\0' * ((-len(data)) % 16)
    result = subprocess.run(['/usr/bin/openssl', 'enc', '-aes-128-cbc', '-nopad', '-K', b'_aes_secret_key_'.hex(),
                             '-iv', b'_aes_secret_iv__'.hex()], input=data, capture_output=True, check=True)
    return base64.b64encode(result.stdout).decode()


def login(base, user, password):
    """正常取得验证码并在隔离库读取本次挑战, 验证真实登录入口."""
    identifier = str(uuid.uuid4())
    status, headers, image = request(base, 'captcha.jpg?uuid=' + identifier)
    if status != 200 or not image.startswith(b'\xff\xd8'):
        raise AssertionError('验证码接口异常')
    code = database("SELECT CODE FROM SYS_CAPTCHA WHERE UUID='" + identifier + "';")
    token = api(base, 'sys/login', {'userName': user, 'password': encrypt(password), 'uuid': identifier, 'captcha': code})
    SECRETS.append(token)
    return token


def prepare():
    """由真实Compose生成隔离副本, 显式替换全部容器、卷、网络和私有挂载."""
    config = json.loads(docker('compose', '--env-file', str(ROOT / 'deploy/.env.example'), '-f', str(ROOT / 'deploy/docker-compose.yml'), 'config', '--format', 'json'))
    with socket.socket() as reservation:
        reservation.bind(('127.0.0.1', 0))
        test_port = reservation.getsockname()[1]
    password = secrets.token_hex(24)
    bootstrap = secrets.token_hex(16)
    runtime = TEMP / 'runtime'
    runtime.mkdir(mode=0o755)
    (runtime / 'bootstrap').write_text(bootstrap)
    (runtime / 'bootstrap').chmod(0o644)
    (TEMP / 'token').write_text(secrets.token_urlsafe(32))
    (TEMP / 'token').chmod(0o644)
    env = {'MYSQL_DATABASE': 'platform', 'MYSQL_APP_USER': 'caratcraft', 'MYSQL_ROOT_PASSWORD': password,
           'MYSQL_APP_PASSWORD': password, 'REDIS_PASSWORD': password, 'JWT_SECRET': password, 'CARATCRAFT_ENV': 'local'}
    (TEMP / '.env').write_text('\n'.join(key + '=' + value for key, value in env.items()))
    (TEMP / '.env').chmod(0o600)
    for name, service in config['services'].items():
        service.pop('build', None)
        service['container_name'] = PROJECT + '-' + name
        service['restart'] = 'no'
        if name in ('platform-admin', 'platform-api'):
            service['image'] = '${CARATCRAFT_BACKEND_IMAGE:-carat-craft-backend:local}'
        elif name == 'nginx':
            service['image'] = '${CARATCRAFT_ADMIN_UI_IMAGE:-carat-craft-admin-ui:local}'
        elif name == 'maintenance':
            service['image'] = '${CARATCRAFT_MAINTENANCE_IMAGE:-carat-craft-maintenance:local}'
        service.pop('ports', None)
        settings = service.setdefault('environment', {})
        for key in ('MYSQL_ROOT_PASSWORD', 'MYSQL_PASSWORD', 'REDIS_PASSWORD', 'JWT_SECRET'):
            if key in settings:
                settings[key] = password
        if name in ('platform-admin', 'platform-api'):
            settings['CARATCRAFT_ENV'] = 'local'
            settings['CARATCRAFT_DEFAULT_BUSINESS_ORG'] = ''
            settings['SPRING_APPLICATION_JSON'] = json.dumps({'caratcraft.refund-reconciliation-enabled': False})
        for mount in service.get('volumes', []):
            target = mount['target']
            if target == '/run/secrets':
                mount['source'] = str(runtime)
            elif target in ('/run/platform-maintenance/token', '/run/maintenance/token'):
                mount['source'] = str(TEMP / 'token')
            elif target == '/deployment':
                mount['source'] = str(TEMP)
        if name == 'platform-admin':
            settings['CARATCRAFT_SUPER_ADMIN_BOOTSTRAP_FILE'] = '/run/secrets/bootstrap'
        if name == 'mysql':
            settings['CARATCRAFT_DB_INIT'] = 'true'
        if name == 'maintenance':
            settings['CARATCRAFT_COMPOSE_PROJECT'] = PROJECT
            service['volumes'].append({'type': 'bind', 'source': str(ROOT / 'database/upgrade'), 'target': '/source/database/upgrade', 'read_only': True})
        if name == 'nginx':
            service['ports'] = [{'target': 80, 'published': str(test_port), 'host_ip': '127.0.0.1'}]
    for name, volume in config['volumes'].items():
        volume['name'] = PROJECT + '_' + name
    for name, network in config['networks'].items():
        network['name'] = PROJECT + '_' + name
    config['name'] = PROJECT
    (TEMP / 'docker-compose.yml').write_text(json.dumps(config))
    (TEMP / 'docker-compose.yml').chmod(0o600)
    return config, bootstrap


def wait_job(base, token, identifier, terminal):
    """只等待本轮任务的真实阶段, 每十秒提示且有明确超时."""
    deadline = time.monotonic() + 300
    count = 0
    while time.monotonic() < deadline:
        try:
            jobs = api(base, 'platform/jobs', token=token)
            item = next(job for job in jobs if job['id'] == identifier)
            if item['status'] in ('SUCCEEDED', 'FAILED', 'FAILED_RECOVERED', 'RESTORED'):
                checked('升级任务结果=' + terminal, item['status'] == terminal)
                return item
            if count % 5 == 0:
                print('维护任务实际阶段: ' + item['stage'], flush=True)
        except (OSError, TimeoutError):
            pass
        count += 1
        time.sleep(2)
    raise TimeoutError('隔离升级任务未在时限内完成')



def existing_schema_checks():
    """在同一隔离MySQL的独立辅助库验证旧审计增量、重复执行及既有归属保护."""
    auxiliary = 'platform_existing_platform_test'
    database("CREATE DATABASE " + auxiliary + "; USE " + auxiliary + "; CREATE TABLE SYS_ORG LIKE platform.SYS_ORG; CREATE TABLE SYS_CONFIG LIKE platform.SYS_CONFIG; CREATE TABLE SYS_LOG LIKE platform.SYS_LOG; CREATE TABLE mall_goods LIKE platform.mall_goods; INSERT INTO mall_goods SELECT * FROM platform.mall_goods; INSERT INTO SYS_ORG(ORG_NO,ORG_NAME,STATUS,CREATE_TIME) VALUES('LEGACY01','历史工作室',1,NOW());")
    columns = ('actor_id', 'actor_type', 'module', 'operation_type', 'target_id', 'result', 'summary', 'error_code')
    database('USE ' + auxiliary + '; ALTER TABLE SYS_LOG ' + ','.join('DROP COLUMN ' + column for column in columns) + ';')
    before = database('USE ' + auxiliary + '; CHECKSUM TABLE mall_goods;')
    check = (ROOT / 'database/upgrade/platform-console/check.sql').read_text()
    apply = (ROOT / 'database/upgrade/platform-console/apply.sql').read_text()
    for attempt in range(2):
        result = database('USE ' + auxiliary + '; SET @cc_need_apply=1;\n' + check + apply + check + '\nSELECT @cc_complete;')
        if result.splitlines()[-1] != '1':
            raise AssertionError('旧审计结构增量未完成')
    checked('旧结构补齐八列且重复执行不重复创建机构', database("USE " + auxiliary + "; SELECT CONCAT(COUNT(*),':',MIN(ORG_NO),':',MIN(ORG_NAME)) FROM SYS_ORG;") == '1:LEGACY01:历史工作室')
    checked('历史商品归属及内容保持原样', before == database('USE ' + auxiliary + '; CHECKSUM TABLE mall_goods;'))
    checked('唯一已有工作室登记默认配置', database("USE " + auxiliary + "; SELECT PARAM_VALUE FROM SYS_CONFIG WHERE PARAM_KEY='caratcraft.business.org';") == 'LEGACY01')

def account_checks(base, bootstrap):
    """通过正常HTTP验证三级账号、环境密码、平级边界与统一审计."""
    token = login(base, 'admin', bootstrap)
    api(base, 'sys/user/password', {'password': bootstrap, 'newPassword': '123456'}, token)
    token = login(base, 'admin', '123456')
    checked('本地超管正常登录、原密码验证、允许123456')
    nav = api(base, 'sys/menu/nav', token=token)
    checked('超管五个菜单与明确平台权限', len(nav['menuList']) == 5 and len(nav['permissions']) == 5)
    checked('默认工作室自动初始化', api(base, 'platform/workspace', token=token)['name'] == '默认工作室')
    api(base, 'platform/workspace', {'name': '隔离工作室'}, token)
    merchant = api(base, 'platform/merchant/create', {'userName': 'merchant', 'realName': '隔离商家'}, token)
    checked('无需联系方式及环境编码即可开户', merchant['temporaryPassword'] == '123456')
    merchant_token = login(base, 'merchant', merchant['temporaryPassword'])
    denied = request(base, 'sys/menu/nav', token=merchant_token)
    checked('商家首次登录强制改密', denied[0] == 428)
    api(base, 'sys/user/password', {'password': '123456', 'newPassword': '654321'}, merchant_token)
    merchant_token = login(base, 'merchant', '654321')
    merchant_nav = api(base, 'sys/menu/nav', token=merchant_token)
    checked('商家正常登录且无平台管理权限', not any(value.startswith('platform:') for value in merchant_nav['permissions']))
    for path in ('mall/goods/list', 'custom/configuration/materials', 'mall/order/list', 'mall/refund/list'):
        status, _, content = request(base, path, token=token)
        checked('超管禁止经营接口 ' + path, status == 403)
    for path in ('platform/overview', 'platform/monitor', 'platform/logs', 'platform/jobs'):
        checked('商家禁止平台接口 ' + path, request(base, path, token=merchant_token)[0] == 403)
    second = api(base, 'platform/merchant/create', {'userName': 'merchant2', 'realName': '同级商家'}, token)
    checked('平级商家禁止重置他人', request(base, 'sys/user/resetPw', {'userId': second['userId']}, merchant_token)[0] == 403)
    employee = api(base, 'sys/user/save', {'userName': 'employee', 'realName': '隔离员工', 'status': 1, 'roleIdList': [], 'permissionMenuIds': []}, merchant_token)
    employee_token = login(base, 'employee', employee['temporaryPassword'])
    api(base, 'sys/user/password', {'password': employee['temporaryPassword'], 'newPassword': 'abcdef'}, employee_token)
    employee_token = login(base, 'employee', 'abcdef')
    checked('无授权员工禁止业务及平台接口', request(base, 'mall/goods/list', token=employee_token)[0] == 403 and request(base, 'platform/monitor', token=employee_token)[0] == 403)
    checked('匿名平台接口被拒绝', request(base, 'platform/monitor')[0] == 401)
    overview = api(base, 'platform/overview', token=token)
    checked('平台总览使用真实账号数据', int(overview['merchants']['total']) == 2)
    for attempt in range(20):
        monitor = api(base, 'platform/monitor', token=token)
        if len(monitor.get('services', [])) == 5 and all(item.get('health') == 'healthy' for item in monitor['services']):
            break
        if attempt % 5 == 0:
            print('等待下一个15秒真实监控采样...', flush=True)
        time.sleep(2)
    (TEMP / 'monitor.json').write_text(json.dumps(monitor, ensure_ascii=False))
    checked('五核心服务真实健康及监控来源', len(monitor.get('services', [])) == 5 and all(item.get('health') == 'healthy' for item in monitor['services']) and monitor['host']['origin'] in ('DOCKER_VM', 'DOCKER_HOST_KERNEL'))
    records = api(base, 'platform/logs', token=token)['records']
    checked('统一审计无密码及令牌原文', records and all('params' not in item and 'password' not in json.dumps(item).lower() and '654321' not in json.dumps(item) for item in records))
    (TEMP / 'audit-counts.txt').write_text(database("SELECT OPERATION,result,COUNT(*) FROM SYS_LOG GROUP BY OPERATION,result;"))
    checked('管理员创建审计不重复', database("SELECT COUNT(*) FROM SYS_LOG WHERE OPERATION='创建商家管理员' AND result='SUCCESS';") == '2')
    checked('商家日志只包含当前工作室安全字段', api(base, 'sys/log/list', token=merchant_token)['records'] is not None)
    checked('成功变更与失败重置都记录', int(database("SELECT COUNT(*) FROM SYS_LOG WHERE result='FAILED';")) > 0)
    api(base, 'platform/merchant/status', {'userId': merchant['userId'], 'active': False}, token)
    checked('停用后旧会话失效', request(base, 'sys/menu/nav', token=merchant_token)[0] == 401)
    api(base, 'platform/merchant/status', {'userId': merchant['userId'], 'active': True}, token)
    # 生产策略只在隔离管理员容器验证, 保留原密码以便继续测试.
    config = json.loads((TEMP / 'docker-compose.yml').read_text())
    config['services']['platform-admin']['environment']['CARATCRAFT_ENV'] = 'production'
    (TEMP / 'docker-compose.yml').write_text(json.dumps(config))
    compose('up', '-d', '--no-deps', '--no-build', '--pull', 'never', '--wait', '--wait-timeout', '180', 'platform-admin')
    token = login(base, 'admin', '123456')
    policy = api(base, 'sys/password-policy')
    checked('服务端生产策略不可由客户端切换', not policy['local'] and request(base, 'sys/user/password', {'password': '123456', 'newPassword': '654321', 'local': True}, token)[0] == 400)
    config['services']['platform-admin']['environment']['CARATCRAFT_ENV'] = 'local'
    (TEMP / 'docker-compose.yml').write_text(json.dumps(config))
    compose('up', '-d', '--no-deps', '--no-build', '--pull', 'never', '--wait', '--wait-timeout', '180', 'platform-admin')
    return login(base, 'admin', '123456')



def release_fixture(version, previous=None, fail=False):
    """只构建带隔离标识的轻量镜像元数据层, 不重新编译应用或改正式版本."""
    folder = TEMP / ('release-' + version)
    folder.mkdir()
    source = previous if previous else ROOT
    for relative in ('legal', 'database/upgrade'):
        shutil.copytree(source / relative, folder / relative)
    release = {'version': version, 'notes': '仅限' + PROJECT + '隔离验收的签名夹具'}
    (folder / 'release.json').write_text(json.dumps(release))
    name = 'test-failure' if fail else 'test-evolution'
    number = '0004' if fail else '0003'
    migration = folder / 'database/upgrade' / name
    migration.mkdir()
    column = 'failed_marker' if fail else 'version_marker'
    (migration / 'check.sql').write_text("SET @cc_compatible=1; SET @cc_complete=EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='CC_PLATFORM_TEST_SENTINEL' AND COLUMN_NAME='" + column + "');\n")
    sql = "SET @test_sql=IF(@cc_need_apply,'ALTER TABLE CC_PLATFORM_TEST_SENTINEL ADD COLUMN " + column + " INT NULL','DO 0'); PREPARE t FROM @test_sql; EXECUTE t; DEALLOCATE PREPARE t;\n"
    if fail:
        sql += "UPDATE CC_PLATFORM_TEST_SENTINEL SET marker='partially-migrated'; SELECT SLEEP(8); SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='isolated recovery verification';\n"
    (migration / 'apply.sql').write_text(sql)
    with open(folder / 'database/upgrade/local/manifest.txt', 'a') as output:
        output.write(number + ' ' + name + '\n')
    files = {'backend': ('carat-craft-backend:local', '/app/release.json'),
             'admin-ui': ('carat-craft-admin-ui:local', '/usr/share/nginx/html/release.json'),
             'maintenance': ('carat-craft-maintenance:local', '/app/release.json')}
    names = {}
    for component, (base, target) in files.items():
        tag = PROJECT + '-' + component + ':' + version
        names[component] = tag
        digest = hashlib.sha256((PROJECT + component + version).encode()).hexdigest()
        dockerfile = folder / ('Dockerfile-' + component)
        dockerfile.write_text('FROM ' + base + '\nENV BUILD_VERSION=' + version + '\nCOPY release.json ' + target + '\nLABEL org.opencontainers.image.version="' + version + '" io.caratcraft.source-digest="' + digest + '" io.caratcraft.test.project="' + PROJECT + '"\n')
        docker('build', '--file', str(dockerfile), '--tag', tag, str(folder))
    script = folder / 'create-package.py'
    script.write_text("import importlib.util, sys\nspec=importlib.util.spec_from_file_location('release','/tools/package-release.py')\nmodule=importlib.util.module_from_spec(spec);spec.loader.exec_module(module)\nmodule.IMAGES=" + repr(names) + "\nsys.argv=['release','--root','/fixture','--output','/fixture/package.zip','--key','/signing.pem','--deployment-project'," + repr(PROJECT) + "]\nmodule.main()\n")
    print('生成限本隔离项目使用的签名发行夹具 ' + version, flush=True)
    docker('run', '--rm', '--network', 'none',
           '--mount', 'type=bind,source=/var/run/docker.sock,target=/var/run/docker.sock',
           '--mount', 'type=bind,source=' + str(folder) + ',target=/fixture',
           '--mount', 'type=bind,source=' + str(ROOT / 'tools/delivery/package-release.py') + ',target=/tools/package-release.py,readonly',
           '--mount', 'type=bind,source=' + str(ROOT / '.private/release-signing.pem') + ',target=/signing.pem,readonly',
           'carat-craft-maintenance:local', 'python', '/fixture/create-package.py')
    return folder


def upload_package(base, token, path):
    """流式上传真实镜像包, 不在测试进程中一次读取整个归档."""
    import http.client
    from urllib.parse import urlsplit
    url = urlsplit(base)
    connection = http.client.HTTPConnection(url.hostname, url.port, timeout=240, blocksize=1024 * 1024)
    try:
        with open(path, 'rb') as source:
            connection.request('POST', '/platform-framework/platform/packages', source,
                               {'Content-Type': 'application/octet-stream', 'Content-Length': str(path.stat().st_size), 'token': token})
            response = connection.getresponse()
            data = json.loads(response.read())
            if response.status != 200 or data.get('code') != 0:
                raise AssertionError('发行包真实HTTP校验失败: ' + str(response.status) + ', ' + str(data.get('msg')))
            return data['data']
    finally:
        connection.close()


def upgrade_checks(base, token):
    """验证真实独立工作容器、结构演进及半迁移失败后的三项一致恢复."""
    database("CREATE TABLE CC_PLATFORM_TEST_SENTINEL(id INT PRIMARY KEY,marker VARCHAR(64)); INSERT INTO CC_PLATFORM_TEST_SENTINEL VALUES(1,'original');")
    compose('exec', '-T', 'maintenance', 'python', '-c', 'from pathlib import Path; Path("/uploads/platform-test.png").write_bytes(b"original-upload")')
    first = release_fixture('0.1.1')
    job = upload_package(base, token, first / 'package.zip')
    checked('真实HTTP签名镜像包校验且未立即执行', job['status'] == 'VALIDATED')
    api(base, 'platform/confirm', {'id': job['id']}, token)
    wait_job(base, token, job['id'], 'SUCCEEDED')
    checked('实际安装版本升级且数据库数据保留', api(base, 'platform/release', token=token)['version'] == '0.1.1' and database('SELECT marker FROM CC_PLATFORM_TEST_SENTINEL WHERE id=1;') == 'original')
    checked('成功升级上传文件保持', compose('exec', '-T', 'maintenance', 'python', '-c', 'from pathlib import Path; print(Path("/uploads/platform-test.png").read_text())') == 'original-upload')
    failure_upgrade_checks(base, token, first)


def failure_upgrade_checks(base, token, first):
    """独立验证半迁移失败恢复, 复用已成功升级的同一隔离项目."""
    second = release_fixture('0.1.2', previous=first, fail=True)
    failed = upload_package(base, token, second / 'package.zip')
    api(base, 'platform/confirm', {'id': failed['id']}, token)
    injected = False
    deadline = time.monotonic() + 300
    while time.monotonic() < deadline:
        records = json.loads(compose('exec', '-T', 'maintenance', 'python', '-c', 'import upgrades,json; print(json.dumps(upgrades.jobs()))'))
        record = next(value for value in records if value['id'] == failed['id'])
        if record['stage'] == 'MIGRATING' and not injected:
            compose('exec', '-T', 'maintenance', 'python', '-c', 'from pathlib import Path; Path("/uploads/platform-test.png").write_bytes(b"changed-during-failed-upgrade")')
            injected = True
        if record['status'] in ('FAILED', 'FAILED_RECOVERED'):
            checked('半迁移失败触发实际一致恢复', injected and record['status'] == 'FAILED_RECOVERED')
            break
        time.sleep(1)
    else:
        raise TimeoutError('失败恢复任务超时')
    checked('失败后恢复原版本、原数据库与原上传内容',
            api(base, 'platform/release', token=token)['version'] == '0.1.1'
            and database('SELECT marker FROM CC_PLATFORM_TEST_SENTINEL WHERE id=1;') == 'original'
            and database("SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='CC_PLATFORM_TEST_SENTINEL' AND COLUMN_NAME='failed_marker';") == '0'
            and compose('exec', '-T', 'maintenance', 'python', '-c', 'from pathlib import Path; print(Path("/uploads/platform-test.png").read_text())') == 'original-upload')
    checked('恢复上传文件仍兼容应用UID999', compose('exec', '-T', 'maintenance', 'python', '-c', 'from pathlib import Path; p=Path("/uploads/platform-test.png"); print(str(p.stat().st_uid)+":"+str(p.stat().st_gid))') == '999:999')
    api(base, 'platform/jobs', token=token)
    checked('升级与恢复重要结果进入统一审计', int(database("SELECT COUNT(*) FROM SYS_LOG WHERE module='系统维护' AND result='FAILED';")) >= 1)
    checked('恢复后应用与维护服务全部健康', all(json.loads(docker('inspect', PROJECT + '-' + name))[0]['State'].get('Health', {}).get('Status') == 'healthy' for name in ('mysql', 'redis', 'platform-admin', 'platform-api', 'nginx', 'maintenance')))

def main():
    """启动隔离环境、验证真实接口并清理本轮资源, 证据留在临时目录."""
    config, bootstrap = prepare()
    started = time.monotonic()
    try:
        compose('up', '-d', '--no-build', '--pull', 'never', '--wait', '--wait-timeout', '240', 'mysql', 'redis', 'maintenance')
        grants = docker('exec', PROJECT + '-mysql', 'sh', '-ec', 'MYSQL_PWD="$MYSQL_PASSWORD" mysql -u"$MYSQL_USER" "$MYSQL_DATABASE" -Nse "SHOW GRANTS FOR CURRENT_USER"')
        checked('首次初始化确实收窄应用数据库权限', 'SELECT, INSERT, UPDATE, DELETE ON' in grants and 'ALL PRIVILEGES' not in grants)
        existing_schema_checks()
        maintenance('repair-permissions')
        compose('exec', '-T', 'maintenance', 'python', '-c', 'from pathlib import Path; import upgrades; upgrades.migrate(Path("/source"))')
        compose('up', '-d', '--no-build', '--pull', 'never', '--wait', '--wait-timeout', '240')
        maintenance('record-install')
        base = 'http://' + compose('port', 'nginx', '80')
        if '--browser-only' in sys.argv:
            token = login(base, 'admin', bootstrap)
            api(base, 'sys/user/password', {'password': bootstrap, 'newPassword': '123456'}, token)
            token = login(base, 'admin', '123456')
        else:
            token = account_checks(base, bootstrap)
        checked('软件版本来自实际安装登记', api(base, 'platform/release', token=token)['version'] == '0.1.0')
        if '--upgrades' in sys.argv:
            upgrade_checks(base, token)
        if '--browser' in sys.argv or '--browser-only' in sys.argv:
            import platform_browser
            platform_browser.verify(base, token, TEMP, checked, login)
    except BaseException:
        for service in ('mysql', 'maintenance', 'platform-admin'):
            try:
                output = compose('logs', '--tail', '15', service)
                for secret in SECRETS: output = output.replace(secret, '[已隐藏]')
                print(output, flush=True)
            except Exception:
                pass
        raise
    finally:
        # compose只清理本轮随机项目, 正式六服务和卷不在命令范围内.
        workers = docker('ps', '-aq', '--filter', 'label=io.caratcraft.maintenance-project=' + PROJECT).split()
        for worker in workers:
            metadata = json.loads(docker('inspect', worker))[0]
            if metadata['Name'].startswith('/' + PROJECT + '-upgrade-'):
                docker('rm', '-f', worker)
        compose('down', '--volumes', '--remove-orphans')
        tags = docker('image', 'ls', '--filter', 'label=io.caratcraft.test.project=' + PROJECT, '--format', '{{.Repository}}:{{.Tag}}').split()
        for tag in tags:
            if tag.startswith(PROJECT + '-'):
                docker('image', 'rm', tag)
        evidence = {'project': PROJECT, 'checks': RESULTS, 'durationSeconds': round(time.monotonic() - started, 2)}
        (TEMP / 'results.json').write_text(json.dumps(evidence, ensure_ascii=False, indent=4))
        print('隔离专项实际完成 ' + str(len(RESULTS)) + ' 项, 耗时 ' + str(evidence['durationSeconds']) + ' 秒', flush=True)


if __name__ == '__main__':
    main()
