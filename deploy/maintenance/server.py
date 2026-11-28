"""维护服务只开放固定认证操作, 升级工作由独立工作容器执行."""

from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
import hmac
import json
import os
import shutil
from pathlib import Path
import sys
import threading
import uuid
import zipfile

import metrics
import packages
import upgrades

TOKEN = Path('/run/maintenance/token')


class Handler(BaseHTTPRequestHandler):
    """固定接口与长度限制, 不提供任意命令、路径或Docker代理."""

    def log_message(self, format, *args):
        """避免将HTTP头、令牌及上传参数写入访问日志."""
        return

    def reply(self, code, value):
        """仅输出结构化结果并禁止浏览器缓存维护信息."""
        content = json.dumps(value, ensure_ascii=False).encode()
        self.send_response(code)
        self.send_header('Content-Type', 'application/json; charset=utf-8')
        self.send_header('Content-Length', str(len(content)))
        self.send_header('Cache-Control', 'no-store')
        self.end_headers()
        self.wfile.write(content)

    def authorized(self):
        """使用部署生成且仅后端与维护服务可读的独立令牌."""
        return TOKEN.is_file() and hmac.compare_digest(self.headers.get('Authorization', ''), 'Bearer ' + TOKEN.read_text().strip())

    def do_GET(self):
        """健康检查仅本容器回环免认证, 监控与任务必须认证."""
        if self.path == '/health' and self.client_address[0] == '127.0.0.1':
            self.reply(200, {'ready': True})
            return
        if not self.authorized():
            self.reply(401, {'message': '维护认证失败'})
            return
        try:
            if self.path == '/monitor':
                self.reply(200, metrics.current())
            elif self.path == '/release':
                self.reply(200, upgrades.current_release())
            elif self.path == '/jobs':
                upgrades.reconcile_workers()
                self.reply(200, upgrades.jobs())
            else:
                self.reply(404, {'message': '维护操作不存在'})
        except Exception:
            self.reply(503, {'message': '维护状态暂不可用, 请检查维护服务日志'})

    def do_POST(self):
        """包上传采用定长流式写入, 其余请求只接受有限的结构化参数."""
        if not self.authorized():
            self.reply(401, {'message': '维护认证失败'})
            return
        temporary = None
        try:
            length = int(self.headers.get('Content-Length', '-1'))
            if self.path == '/packages':
                if length <= 0 or length > packages.MAX_PACKAGE:
                    raise ValueError('请选择不超过4 GiB的离线发行包')
                upgrades.assert_idle()
                upgrades.STATE.mkdir(mode=0o700, parents=True, exist_ok=True)
                if shutil.disk_usage(upgrades.STATE).free < length + 256 * 1024**2:
                    raise ValueError('剩余空间不足以接收发行包')
                temporary = upgrades.STATE / ('upload-' + str(uuid.uuid4()) + '.zip')
                with open(temporary, 'xb') as output:
                    remaining = length
                    while remaining:
                        block = self.rfile.read(min(1024 * 1024, remaining))
                        if not block:
                            raise ValueError('上传被中断, 请重新上传')
                        output.write(block)
                        remaining -= len(block)
                self.reply(200, upgrades.validate_upload(temporary, '1'))
            elif self.path in ('/confirm', '/restore'):
                if length < 0 or length > 512:
                    raise ValueError('维护请求格式无效')
                data = json.loads(self.rfile.read(length))
                if set(data) != {'id'}:
                    raise ValueError('维护请求参数无效')
                upgrades.start_worker(data['id'], recovery=self.path == '/restore')
                self.reply(200, {'accepted': True})
            else:
                self.reply(404, {'message': '维护操作不存在'})
        except (ValueError, KeyError, json.JSONDecodeError, zipfile.BadZipFile) as error:
            self.reply(400, {'message': str(error) if isinstance(error, ValueError) else '维护请求格式无效'})
        except Exception:
            self.reply(503, {'message': '维护操作失败, 原有备份与任务记录已保留'})
        finally:
            if temporary is not None:
                temporary.unlink(missing_ok=True)


def repair_upload_permissions():
    """只有权限不兼容时才先备份上传卷再修复UID999, 不修改证书源文件."""
    upgrades.assert_idle()
    root = upgrades.UPLOADS
    paths = [root, *root.rglob('*')]
    if any(path.is_symlink() or not (path.is_dir() or path.is_file()) for path in paths):
        raise ValueError('上传卷含链接或特殊文件, 请人工核对')
    changed = [path for path in paths if path.stat().st_uid != 999 or path.stat().st_gid != 999
               or path.stat().st_mode & 0o777 != (0o755 if path.is_dir() else 0o644)]
    if changed:
        import tarfile
        folder = upgrades.STATE / 'permission-backups'
        folder.mkdir(mode=0o700, parents=True, exist_ok=True)
        backup = folder / (str(uuid.uuid4()) + '.tar')
        with tarfile.open(backup, 'w') as archive:
            for path in paths[1:]:
                archive.add(path, arcname=str(path.relative_to(root)), recursive=False)
        for path in changed:
            os.chown(path, 999, 999)
            path.chmod(0o755 if path.is_dir() else 0o644)
        print('上传卷原权限与内容已备份, UID/GID999权限已修复', flush=True)
    else:
        print('上传卷UID/GID999权限兼容, 无需修改', flush=True)


def main():
    """内部命令供标准启动和持久升级工作容器调用, 不增加商家常规入口."""
    command = sys.argv[1] if len(sys.argv) > 1 else 'serve'
    if command in ('run-job', 'restore-job'):
        # 工作进程等待父进程交接互斥锁, 不依赖固定延迟.
        upgrades.run_job(sys.argv[2], command == 'restore-job')
    elif command == 'request-recovery':
        upgrades.start_worker(sys.argv[2], recovery=True)
        print('失败任务恢复已提交', flush=True)
    elif command == 'wait-recovery':
        import time
        deadline = time.monotonic() + 300
        previous = None
        while time.monotonic() < deadline:
            job = upgrades.load_job(sys.argv[2])
            if job['stage'] != previous:
                print('恢复实际阶段: ' + job['stage'], flush=True)
                previous = job['stage']
            if job['status'] == 'RESTORED':
                return
            if job['status'] == 'FAILED':
                raise ValueError('恢复失败, 保留停写标记与备份, 请核对任务诊断')
            time.sleep(2)
        raise ValueError('恢复仍未完成, 不会强制中断工作容器')
    elif command == 'guard':
        upgrades.reconcile_workers()
        try:
            upgrades.assert_idle()
        except ValueError as error:
            failed = [job['id'] for job in upgrades.jobs() if job.get('recoverable')]
            if failed:
                raise ValueError('维护未恢复, 请使用标准Start的 --recover-failed-task 或 -RecoverFailedTask 参数明确恢复任务 ' + failed[0]) from error
            raise
    elif command == 'backup-startup':
        upgrades.backup_startup()
    elif command == 'record-install':
        upgrades.record_install()
    elif command == 'repair-permissions':
        repair_upload_permissions()
    elif command == 'verify-installed':
        release = upgrades.current_release()
        upgrades.migrate(upgrades.job_path(release['jobId']).parent / 'package')
    elif command == 'diagnose':
        import docker_api
        for service in ('platform-admin', 'platform-api'):
            metadata = docker_api.inspect(service)
            mounts = {item['Destination']: item for item in metadata['Mounts']}
            print(service + ' 用户=' + metadata['Config']['User'] + ' 上传卷=' + str('/data/uploads' in mounts)
                  + ' Docker套接字隔离=' + str('/var/run/docker.sock' not in mounts), flush=True)
        from urllib.request import Request, urlopen
        from urllib.error import HTTPError
        for method, path, expected in (('GET', '/', 200), ('GET', '/platform-framework/', 401),
                ('GET', '/platform-framework-api/', 200),
                ('POST', '/platform-framework-api/app/index/newGoods', 200),
                ('POST', '/platform-framework-api/app/goods/count', 200),
                ('GET', '/platform-framework-api/app/customization/styles', 200)):
            try:
                with urlopen(Request('http://nginx' + path, method=method), timeout=8) as response:
                    status = response.status
                    content = response.read(1024 * 1024)
                    if '/app/' in path and json.loads(content).get('code') != 0:
                        raise ValueError('业务只读接口响应失败: ' + path)
            except HTTPError as error:
                status = error.code
            print(method + ' ' + path + ' HTTP=' + str(status), flush=True)
            if status != expected:
                raise ValueError('应用HTTP诊断未通过, 请核对代理和服务健康')
        print('维护任务=' + str(len(upgrades.jobs())) + ' 维护标记=' + str((upgrades.CONTROL / 'maintenance').exists()), flush=True)
    elif command == 'serve':
        upgrades.STATE.mkdir(mode=0o700, parents=True, exist_ok=True)
        threading.Thread(target=metrics.collect_loop, daemon=True).start()
        server = ThreadingHTTPServer(('0.0.0.0', 8090), Handler)
        server.timeout = 30
        server.serve_forever()
    else:
        raise ValueError('未知维护内部操作')


if __name__ == '__main__':
    try:
        main()
    except Exception as error:
        print('维护操作停止: ' + (str(error) if isinstance(error, ValueError) else type(error).__name__), file=sys.stderr, flush=True)
        sys.exit(1)
