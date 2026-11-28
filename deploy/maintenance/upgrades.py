"""由独立维护工作容器执行离线升级与一致恢复, 状态先持久化再进入下一阶段."""

from contextlib import contextmanager
from datetime import datetime, timezone
import fcntl
import json
import os
from pathlib import Path
import platform
import re
import shutil
import subprocess
import tarfile
import time
import uuid

import docker_api
import packages

STATE = Path(os.environ.get("CARATCRAFT_MAINTENANCE_STATE", "/state"))
DEPLOY = Path("/deployment")
CONTROL = Path("/control")
UPLOADS = Path("/uploads")
TERMINAL = {"SUCCEEDED", "FAILED_RECOVERED", "FAILED", "RESTORED"}
SERVICE_NAME = "platform-maintenance" if docker_api.PROJECT == "platform-wxshop" else docker_api.PROJECT + "-maintenance"


def timestamp():
    """统一使用UTC保存事件时间, 由界面本地化显示."""
    return datetime.now(timezone.utc).isoformat()


def atomic(path, value, mode=0o600):
    """同目录临时文件替换, 避免断电留下半份任务或安装记录."""
    path.parent.mkdir(parents=True, exist_ok=True)
    temporary = path.with_suffix(".tmp")
    with open(temporary, "w", opener=lambda name, flags: os.open(name, flags, mode)) as output:
        json.dump(value, output, ensure_ascii=False)
        output.flush()
        os.fsync(output.fileno())
    temporary.replace(path)


@contextmanager
def task_lock(blocking=False):
    """跨HTTP服务及工作容器互斥, 进程退出后内核自动释放锁."""
    STATE.mkdir(mode=0o700, parents=True, exist_ok=True)
    with open(STATE / "task.lock", "a") as lock:
        try:
            fcntl.flock(lock, fcntl.LOCK_EX | (0 if blocking else fcntl.LOCK_NB))
        except BlockingIOError as error:
            raise ValueError("已有维护任务正在执行") from error
        try:
            yield
        finally:
            fcntl.flock(lock, fcntl.LOCK_UN)


def job_path(identifier):
    """任务标识只接受规范UUID, 不允许路径或命令参数."""
    if not isinstance(identifier, str) or str(uuid.UUID(identifier)) != identifier:
        raise ValueError("任务标识无效")
    return STATE / "jobs" / identifier / "job.json"


def load_job(identifier):
    """读取已经存在的持久任务, 不根据请求创建任意路径."""
    return json.loads(job_path(identifier).read_text())


def update_job(job, stage, status="RUNNING", message=None):
    """记录真实已进入的阶段及非敏感说明, 不使用虚构的百分比."""
    job.update(stage=stage, status=status, updatedAt=timestamp())
    job.setdefault("events", []).append({"time": job["updatedAt"], "stage": stage, "status": status,
                                        "message": message or stage})
    atomic(job_path(job["id"]), job)


def jobs():
    """只返回最近30项任务的结构化状态, 不公开备份内容或命令输出."""
    records = []
    for path in (STATE / "jobs").glob("*/job.json"):
        try:
            job = json.loads(path.read_text())
            job["recoverable"] = job.get("status") == "FAILED" and (CONTROL / "maintenance").exists()
            records.append({key: job.get(key) for key in ("id", "version", "status", "stage", "createdAt", "updatedAt", "events", "actor", "backupComplete", "recoverable", "failureReason")})
        except (OSError, ValueError):
            continue
    return sorted(records, key=lambda job: job["createdAt"], reverse=True)[:30]


def assert_idle():
    """未完成任务或维护标记存在时禁止并发更新及本地源码部署."""
    if (CONTROL / "maintenance").exists() or any(job["status"] not in TERMINAL | {"VALIDATED"} for job in jobs()):
        raise ValueError("系统正在维护或存在待恢复任务, 请先完成恢复")


def own_metadata():
    """仅核对维护服务自身的固定身份, 不接受调用方指定的容器."""
    data = docker_api.request("/containers/" + SERVICE_NAME + "/json")
    labels = data["Config"].get("Labels", {})
    if labels.get("com.docker.compose.project") != docker_api.PROJECT or labels.get("com.docker.compose.service") != "maintenance":
        raise ValueError("维护服务项目归属异常")
    return data


def current_release():
    """安装记录有真实落盘来源, 尚未登记时明确显示待登记."""
    path = STATE / "installed.json"
    if not path.exists():
        return {"version": None, "status": "UNREGISTERED", "message": "等待标准启动完成安装登记"}
    return json.loads(path.read_text())


def running_images():
    """读取五服务中三应用及维护器的实际镜像身份, 不读取业务数据."""
    result = {}
    for key, service in (("backend", "platform-admin"), ("admin-ui", "nginx")):
        data = docker_api.inspect(service)
        result[key] = {"id": data["Image"], "sourceDigest": data["Config"].get("Labels", {}).get("io.caratcraft.source-digest")}
    own = own_metadata()
    result["maintenance"] = {"id": own["Image"], "sourceDigest": own["Config"].get("Labels", {}).get("io.caratcraft.source-digest")}
    return result


def record_install():
    """标准源码启动全部健康后登记实际发行与镜像, 保留首次安装时间."""
    with task_lock():
        assert_idle()
        if (DEPLOY / "private/installed.json").exists():
            return current_release()
        release = json.loads(Path("/app/release.json").read_text())
        images = running_images()
        for service in docker_api.NAMES:
            if docker_api.inspect(service)["State"].get("Health", {}).get("Status") != "healthy":
                raise ValueError("核心服务尚未全部健康, 不能登记安装")
        old = current_release()
        value = {**release, "status": "READY", "installedAt": old.get("installedAt", timestamp()),
                 "builtAt": docker_api.request("/images/" + images["backend"]["id"] + "/json")["Created"],
                 "updatedAt": timestamp(), "deployment": images, "mode": "SOURCE"}
        if old.get("deployment") != images or old.get("version") != release["version"]:
            atomic(STATE / "installed.json", value)
        return current_release()


def validate_upload(package, actor):
    """串行校验签名发行包, 返回确认用的任务而非立即执行升级."""
    with task_lock():
        assert_idle()
        identifier = str(uuid.uuid4())
        directory = job_path(identifier).parent
        directory.mkdir(mode=0o700, parents=True)
        try:
            architecture = {"aarch64": "arm64", "x86_64": "amd64"}.get(platform.machine(), platform.machine())
            manifest = packages.validate(package, directory / "package", architecture)
            preflight(directory / "package")
            installed = current_release()
            if packages.version_tuple(manifest["version"]) <= packages.version_tuple(installed.get("version")):
                raise ValueError("目标版本必须高于已安装版本")
            if packages.version_tuple(installed["version"]) < packages.version_tuple(manifest["minimumVersion"]):
                raise ValueError("当前版本不满足发行包最低升级条件")
            for service in docker_api.NAMES:
                if docker_api.inspect(service)["State"].get("Health", {}).get("Status") != "healthy":
                    raise ValueError("核心服务异常, 请先恢复后升级")
            job = {"id": identifier, "version": manifest["version"], "createdAt": timestamp(),
                   "manifest": manifest, "actor": actor, "original": installed, "events": []}
            update_job(job, "VALIDATED", "VALIDATED", "签名、架构、完整性和服务检查通过, 等待确认升级")
            return {key: job[key] for key in ("id", "version", "status", "stage", "createdAt", "events")}
        except BaseException:
            shutil.rmtree(directory)
            raise


def start_worker(identifier, recovery=False):
    """启动同一维护镜像的短生命周期工作容器, 后台和维护HTTP重启均不打断执行."""
    with task_lock():
        job = load_job(identifier)
        if recovery:
            if job["status"] != "FAILED" or not (CONTROL / "maintenance").exists():
                raise ValueError("仅可恢复仍保持停写的失败任务")
        else:
            assert_idle()
            if job["status"] != "VALIDATED" or current_release().get("deployment") != job["original"].get("deployment"):
                raise ValueError("任务不可重复提交或安装状态已变化, 请重新校验升级包")
        data = own_metadata()
        networks = list(data["NetworkSettings"]["Networks"])
        if len(networks) != 1:
            raise ValueError("维护网络配置异常")
        update_job(job, "QUEUED", "QUEUED", "恢复已确认" if recovery else "升级已确认")
        arguments = ["docker", "run", "--detach", "--rm", "--name", docker_api.PROJECT + "-upgrade-" + identifier,
                     "--label", "io.caratcraft.maintenance-task=" + identifier,
                     "--label", "io.caratcraft.maintenance-project=" + docker_api.PROJECT,
                     "--volumes-from", SERVICE_NAME, "--network", networks[0],
                     "--env", "CARATCRAFT_COMPOSE_PROJECT=" + docker_api.PROJECT,
                     (job["original"]["deployment"]["maintenance"]["id"] if recovery else data["Image"]),
                     "python", "/app/server.py", "restore-job" if recovery else "run-job", identifier]
        try:
            docker_api.run(arguments)
        except BaseException:
            update_job(job, "WORKER_START_FAILED", "FAILED" if recovery else "VALIDATED", "工作容器未启动, 未执行升级")
            raise


def compose(arguments, deployment):
    """仅使用既有项目与配置, 新镜像通过受验证的固定变量传入, 不写入.env."""
    metadata = own_metadata()
    source = next(mount["Source"] for mount in metadata["Mounts"] if mount["Destination"] == "/deployment")
    environment = dict(os.environ)
    # 先清除继承变量, 再注入已经校验的不可变镜像身份.
    for line in (DEPLOY / ".env").read_text().splitlines():
        match = re.match(r"^([A-Z][A-Z0-9_]*)=", line)
        if match:
            environment.pop(match[1], None)
    for key, item in deployment.items():
        environment[{"backend": "CARATCRAFT_BACKEND_IMAGE", "admin-ui": "CARATCRAFT_ADMIN_UI_IMAGE", "maintenance": "CARATCRAFT_MAINTENANCE_IMAGE"}[key]] = item["id"]
    result = subprocess.run(["docker", "compose", "-p", docker_api.PROJECT, "--project-directory", source,
                             "--env-file", str(DEPLOY / ".env"), "-f", str(DEPLOY / "docker-compose.yml"), *arguments],
                            env=environment, stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=420, check=False)
    if result.returncode:
        raise RuntimeError("部署操作失败, 退出码 " + str(result.returncode))


def backup(directory):
    """停写后保存数据库及上传文件的一致备份, 每个文件完成后登记校验摘要."""
    directory.mkdir(mode=0o700, parents=True)
    with open(directory / "database.sql", "xb") as output:
        docker_api.database(output=output)
        output.flush()
        os.fsync(output.fileno())
    with tarfile.open(directory / "uploads.tar", "w") as archive:
        for path in sorted(UPLOADS.rglob("*")):
            if path.is_symlink() or not (path.is_file() or path.is_dir()):
                raise ValueError("上传卷含链接或特殊文件, 停止备份和升级")
            archive.add(path, arcname=path.relative_to(UPLOADS).as_posix(), recursive=False)
    records = {name: packages.digest(directory / name) for name in ("database.sql", "uploads.tar")}
    atomic(directory / "checksums.json", records)


def backup_startup():
    """标准启动仅在待执行新迁移前自动备份, 已有记录不重复导出数据库."""
    with task_lock():
        assert_idle()
        folder = STATE / "startup-backups" / (timestamp().replace(":", "-") + "-" + str(uuid.uuid4())[:8])
        folder.mkdir(mode=0o700, parents=True)
        with open(folder / "database.sql", "xb") as output:
            docker_api.database(output=output)
        dump = folder / "database.sql"
        with open(dump, "rb") as source:
            source.seek(max(0, dump.stat().st_size - 1024))
            if b"-- Dump completed on" not in source.read():
                raise ValueError("数据库备份缺少完整结束标记, 不执行结构升级")
        atomic(folder / "checksums.json", {"database.sql": packages.digest(folder / "database.sql")})
        print("迁移前数据库备份完成, 位于维护持久卷 " + str(folder.relative_to(STATE)), flush=True)


def migrate(root, preflight=False):
    """复用标准Bash迁移库及SQL模板, 核对历史指纹并验证当前版本结构."""
    snapshot = (root / "database/upgrade/local/snapshot.sql").read_text()
    completed = docker_api.database(snapshot).strip()
    result = subprocess.run(["bash", "-euc", 'source /app/migrations.sh; ROOT_DIR="$1"; migration_batch "$2" "$3"',
                             "migration", str(root), completed, "preflight" if preflight else ""], capture_output=True, text=True, timeout=30, check=False)
    if result.returncode:
        raise RuntimeError("生成数据库迁移批次失败")
    docker_api.database(result.stdout)


def maintenance(enabled, identifier):
    """通过只读共享标记暂停应用业务请求和定时任务, 不放行交易操作."""
    CONTROL.mkdir(parents=True, exist_ok=True)
    path = CONTROL / "maintenance"
    if enabled:
        path.write_text(identifier)
        path.chmod(0o644)
    else:
        path.unlink(missing_ok=True)


def wait_healthy(deployment):
    """同时核对五核心服务健康与实际应用镜像, 不以镜像加载成功替代运行验收."""
    deadline = time.monotonic() + 180
    while time.monotonic() < deadline:
        ready = True
        for service in docker_api.NAMES:
            data = docker_api.inspect(service)
            ready = ready and data["State"].get("Health", {}).get("Status") == "healthy"
            if service in ("platform-admin", "platform-api") and data["Image"] != deployment["backend"]["id"]:
                ready = False
            if service == "nginx" and data["Image"] != deployment["admin-ui"]["id"]:
                ready = False
        own = own_metadata()
        ready = ready and own["Image"] == deployment["maintenance"]["id"] and own["State"].get("Health", {}).get("Status") == "healthy"
        if ready:
            return
        time.sleep(2)
    raise RuntimeError("部署后镜像或健康状态验证超时")


def verify_images(root, deployment, architecture, version):
    """镜像先加载再核对不可变身份、来源摘要及架构, 不直接信任镜像标签."""
    docker_api.run(["docker", "image", "load", "--input", str(root / "images.tar")], timeout=600)
    for item in deployment.values():
        image = docker_api.request("/images/" + item["id"] + "/json")
        if image.get("Config", {}).get("Labels", {}).get("org.opencontainers.image.version") != version or image.get("Architecture") != architecture or image.get("Os") != "linux" or image.get("Config", {}).get("Labels", {}).get("io.caratcraft.source-digest") != item["sourceDigest"]:
            raise ValueError("导入镜像与签名发行清单不一致")


def restore(job):
    """仅对尚未恢复业务写入的失败任务, 同时恢复数据库、上传数据及原镜像."""
    original = job["original"]
    directory = job_path(job["id"]).parent / "backup"
    records = json.loads((directory / "checksums.json").read_text())
    if set(records) != {"database.sql", "uploads.tar"} or not job.get("backupComplete") or any(packages.digest(directory / name) != value for name, value in records.items()):
        raise ValueError("一致性备份缺失或摘要不符, 禁止覆盖当前数据")
    if not (CONTROL / "maintenance").is_file() or (CONTROL / "maintenance").read_text() != job["id"]:
        raise ValueError("恢复所需的停写标记不匹配")
    update_job(job, "RESTORING")
    compose(["stop", "nginx", "platform-admin", "platform-api"], original["deployment"])
    assert_applications_stopped()
    # 所有应用停写且维护标记有效后, 清除当前表并按备份恢复, 保留数据库及账号权限.
    tables = docker_api.database("SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_TYPE='BASE TABLE';").splitlines()
    statements = ["SET FOREIGN_KEY_CHECKS=0;"]
    for table in tables:
        if not re.fullmatch(r"[A-Za-z0-9_]+", table):
            raise ValueError("数据库表名异常, 保持维护状态")
        statements.append("DROP TABLE `" + table + "`;")
    statements.append("SET FOREIGN_KEY_CHECKS=1;")
    docker_api.database("\n".join(statements))
    docker_api.database(restore=directory / "database.sql")
    for path in UPLOADS.iterdir():
        if path.is_symlink() or path.is_file():
            path.unlink()
        else:
            shutil.rmtree(path)
    with tarfile.open(directory / "uploads.tar") as archive:
        archive.extractall(UPLOADS, filter="data")
    # 安全解压不继承归档所有者, 明确恢复正式应用UID999所需的可写目录.
    for path in [UPLOADS, *UPLOADS.rglob("*")]:
        if path.is_symlink() or not (path.is_file() or path.is_dir()):
            raise ValueError("上传备份包含不兼容文件类型, 保持维护")
        os.chown(path, 999, 999)
        path.chmod(0o755 if path.is_dir() else 0o644)
    compose(["up", "--detach", "--no-deps", "--no-build", "--pull", "never", "platform-admin", "platform-api", "nginx", "maintenance"], original["deployment"])
    wait_healthy(original["deployment"])
    atomic(STATE / "installed.json", original)
    if original.get("mode") == "OFFLINE":
        atomic(DEPLOY / "private/installed.json", original, 0o644)
    else:
        (DEPLOY / "private/installed.json").unlink(missing_ok=True)
    maintenance(False, job["id"])


def run_job(identifier, recovery=False):
    """独立进程完成升级或恢复, 失败后在备份完整时自动恢复原安装."""
    with task_lock(blocking=True):
        job = load_job(identifier)
        if job["status"] != "QUEUED":
            raise ValueError("任务已被处理")
        if recovery:
            try:
                if job.get("backupComplete"):
                    restore(job)
                else:
                    compose(["up", "--detach", "--no-deps", "--no-build", "--pull", "never", "platform-admin", "platform-api", "nginx", "maintenance"], job["original"]["deployment"])
                    wait_healthy(job["original"]["deployment"])
                    maintenance(False, job["id"])
                update_job(job, "RESTORED", "RESTORED", "原数据库、上传数据与应用已恢复")
            except Exception:
                update_job(job, "RECOVERY_FAILED", "FAILED", "恢复未完成, 保持维护状态并保留备份")
            return
        root = job_path(identifier).parent / "package"
        stopped = False
        try:
            # 确认与执行分开后再次校验文件, 防止校验后内容被更换.
            for name, value in job["manifest"]["files"].items():
                if packages.digest(root / name) != value:
                    raise ValueError("已校验发行文件发生变化")
            preflight(root)
            update_job(job, "LOADING_IMAGES")
            verify_images(root, job["manifest"]["deployment"], job["manifest"]["architecture"], job["manifest"]["version"])
            maintenance(True, identifier)
            update_job(job, "STOPPING_APPLICATIONS")
            stopped = True
            compose(["stop", "nginx", "platform-admin", "platform-api"], job["original"]["deployment"])
            assert_applications_stopped()
            update_job(job, "BACKING_UP")
            backup(job_path(identifier).parent / "backup")
            job["backupComplete"] = True
            update_job(job, "MIGRATING")
            migrate(root)
            update_job(job, "DEPLOYING")
            compose(["up", "--detach", "--no-deps", "--no-build", "--pull", "never", "platform-admin", "platform-api", "nginx", "maintenance"], job["manifest"]["deployment"])
            update_job(job, "VERIFYING")
            wait_healthy(job["manifest"]["deployment"])
            release = json.loads((root / "release.json").read_text())
            installed = {**release, "status": "READY", "installedAt": job["original"]["installedAt"],
                         "updatedAt": timestamp(), "builtAt": job["manifest"]["builtAt"],
                         "deployment": job["manifest"]["deployment"], "mode": "OFFLINE", "jobId": identifier}
            atomic(STATE / "installed.json", installed)
            atomic(DEPLOY / "private/installed.json", installed, 0o644)
            update_job(job, "SUCCEEDED", "SUCCEEDED", "版本、结构、镜像与健康检查通过")
            maintenance(False, identifier)
        except Exception as error:
            failed_stage = job["stage"]
            job["failureReason"] = str(error)[:200] if isinstance(error, (ValueError, RuntimeError)) else type(error).__name__
            if job["status"] == "SUCCEEDED":
                return
            if job.get("backupComplete"):
                try:
                    restore(job)
                    update_job(job, "FAILED_RECOVERED", "FAILED_RECOVERED", "升级在" + failed_stage + "失败, 原数据库、上传数据及镜像已恢复")
                except Exception:
                    update_job(job, "RECOVERY_FAILED", "FAILED", "升级在" + failed_stage + "失败, 自动恢复未完成, 保持维护状态")
            elif stopped:
                try:
                    compose(["up", "--detach", "--no-deps", "--no-build", "--pull", "never", "platform-admin", "platform-api", "nginx"], job["original"]["deployment"])
                    wait_healthy(job["original"]["deployment"])
                    maintenance(False, identifier)
                    update_job(job, "FAILED_RECOVERED", "FAILED_RECOVERED", "备份未完成, 未执行迁移, 原服务已恢复")
                except Exception:
                    update_job(job, "FAILED", "FAILED", "备份未完成, 未执行迁移, 服务恢复失败")
            else:
                maintenance(False, identifier)
                update_job(job, "FAILED", "FAILED", "升级在" + failed_stage + "失败, 尚未停止应用或修改数据库")


def assert_applications_stopped():
    """数据库及文件备份恢复前核对全部业务进程已停止, 不只相信命令退出码."""
    for service in ("nginx", "platform-admin", "platform-api"):
        if docker_api.inspect(service)["State"]["Running"]:
            raise ValueError("业务服务尚未全部停写, 禁止备份或恢复")


def reconcile_workers():
    """识别宿主重启后丢失的工作进程, 保留现场并要求恢复, 不擅自续写数据库."""
    marker = CONTROL / "maintenance"
    if marker.is_file():
        identifier = marker.read_text()
        job = load_job(identifier)
        installed = current_release()
        if job["status"] == "SUCCEEDED" and installed.get("jobId") == identifier:
            with task_lock():
                wait_healthy(installed["deployment"])
                maintenance(False, identifier)
    for visible in jobs():
        if visible["status"] in TERMINAL | {"VALIDATED"}:
            continue
        age = (datetime.now(timezone.utc) - datetime.fromisoformat(visible["updatedAt"])).total_seconds()
        if age < 45:
            continue
        try:
            data = docker_api.request("/containers/" + docker_api.PROJECT + "-upgrade-" + visible["id"] + "/json")
            if data["State"]["Running"]:
                continue
        except RuntimeError:
            pass
        # 活跃工作进程持有相同锁, 即使暂时无法读取Docker也不会误改其状态.
        with task_lock():
            job = load_job(visible["id"])
            if job["status"] not in TERMINAL | {"VALIDATED"}:
                update_job(job, "WORKER_INTERRUPTED", "FAILED", "维护工作进程中断, 保留备份及停写状态, 请处理恢复")


def preflight(root):
    """停机前检查已登记指纹、待执行迁移兼容性和一致备份所需空间."""
    migrate(root, preflight=True)
    sizes = docker_api.database("SELECT COALESCE(SUM(DATA_LENGTH+INDEX_LENGTH),0) FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE();").strip()
    database_size = int(sizes)
    upload_size = 0
    for path in UPLOADS.rglob("*"):
        if path.is_symlink() or not (path.is_file() or path.is_dir()):
            raise ValueError("上传卷包含链接或特殊文件, 无法安全备份")
        if path.is_file():
            upload_size += path.stat().st_size
    required = (database_size + upload_size) * 2 + (root / "images.tar").stat().st_size + 512 * 1024**2
    if shutil.disk_usage(STATE).free < required:
        raise ValueError("剩余空间不足以保留数据库、上传备份及加载镜像")
