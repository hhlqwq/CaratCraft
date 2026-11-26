"""使用已有Docker镜像和Maven缓存运行全后端隔离回归, 不访问业务容器或下载依赖."""

import argparse
from datetime import datetime
import json
import os
from pathlib import Path
import re
import secrets
import signal
import shutil
import subprocess
import time
import uuid

ROOT = Path(__file__).resolve().parents[2]
LABEL = "com.caratcraft.test.run"


def docker(arguments, env=None, check=True):
    """捕获Docker命令结果, 参数中仅传环境变量名而不传秘密值."""
    return subprocess.run(["docker", *arguments], env=env, capture_output=True,
                          text=True, encoding="utf-8", errors="replace", check=check)


class Fixtures:
    """仅管理本次随机标识下新建的测试容器和内部网络."""

    def __init__(self, run_id):
        """保存本次归属标识, 不枚举、复用或删除业务资源."""
        self.run_id = run_id
        self.containers = []
        self.network = None

    def create_network(self):
        """内部网络阻断外网, 仅给本次测试服务提供独立网络空间."""
        name = "caratcraft-test-" + self.run_id
        result = docker(["network", "create", "--internal", "--label", LABEL + "=" + self.run_id, name])
        self.network = result.stdout.strip()
        return name

    def start(self, name, arguments, env):
        """显式禁止拉取镜像, 记录Docker实际返回的容器身份用于精确清理."""
        result = docker(["run", "-d", "--pull=never", "--name", name,
                         "--label", LABEL + "=" + self.run_id, *arguments], env)
        identity = result.stdout.strip()
        if not re.fullmatch(r"[a-f0-9]{64}", identity):
            raise RuntimeError("测试容器未返回有效身份: " + name)
        self.containers.append(identity)
        return identity

    def close(self):
        """按归属标签和实际ID清理本次资源, 无法核对时保留并报告."""
        for identity in reversed(self.containers):
            result = docker(["inspect", identity], check=False)
            if result.returncode == 0:
                item = json.loads(result.stdout)[0]
                if (item["Config"].get("Labels") or {}).get(LABEL) != self.run_id:
                    raise RuntimeError("测试容器归属变化, 拒绝清理")
                docker(["rm", "-f", identity])
        if self.network:
            result = docker(["network", "inspect", self.network], check=False)
            if result.returncode == 0:
                item = json.loads(result.stdout)[0]
                if (item.get("Labels") or {}).get(LABEL) != self.run_id:
                    raise RuntimeError("测试网络归属变化, 拒绝清理")
                docker(["network", "rm", self.network])


def list_residual():
    """列出测试资源归属和状态, 不读取容器环境或其他敏感字段."""
    for command in (
        ["ps", "-a", "--filter", "label=" + LABEL,
         "--format", '{{.ID}} {{.Names}} {{.Status}} {{.Label "com.caratcraft.test.run"}}'],
        ["network", "ls", "--filter", "label=" + LABEL, "--format", "{{.ID}} {{.Name}}"],
    ):
        print(docker(command).stdout.rstrip(), flush=True)


def clean_stopped_run(run_id):
    """仅处理指定批次的已停止容器和空专用网络, 发现活动资源即拒绝."""
    if not re.fullmatch(r"[a-f0-9]{12}", run_id):
        raise ValueError("测试批次标识无效")
    label_filter = "label=" + LABEL + "=" + run_id
    ids = docker(["ps", "-aq", "--no-trunc", "--filter", label_filter]).stdout.split()
    networks = docker(["network", "ls", "-q", "--no-trunc", "--filter", label_filter]).stdout.split()
    for identity in ids:
        item = json.loads(docker(["inspect", "--type", "container", identity]).stdout)[0]
        labels = item["Config"].get("Labels") or {}
        if (labels.get(LABEL) != run_id or labels.get("com.docker.compose.project")
                or item["Name"] not in {"/caratcraft-mysql-" + run_id, "/caratcraft-redis-" + run_id}
                or item["State"]["Status"] not in {"created", "exited", "dead"}):
            raise RuntimeError("发现活动容器或归属不明确, 拒绝清理")
        for mount in item.get("Mounts", []):
            if (mount["Type"] != "bind" or mount["RW"]
                    or mount["Destination"] != "/docker-entrypoint-initdb.d"
                    or Path(mount["Source"]).resolve() != (ROOT / "database/init").resolve()):
                raise RuntimeError("发现预期外的挂载, 拒绝清理")
    if len(networks) > 1:
        raise RuntimeError("发现多个测试网络, 拒绝清理")
    for identity in networks:
        item = json.loads(docker(["network", "inspect", identity]).stdout)[0]
        if (not item["Internal"] or item["Name"] != "caratcraft-test-" + run_id
                or (item.get("Labels") or {}).get(LABEL) != run_id
                or not set(item.get("Containers") or {}).issubset(ids)):
            raise RuntimeError("测试网络归属不明或仍有其他容器使用, 拒绝清理")
    for identity in ids:
        docker(["rm", identity])
    for identity in networks:
        docker(["network", "rm", identity])
    print(f"批次 {run_id}: 移除 {len(ids)} 个已停止测试容器和 {len(networks)} 个测试网络", flush=True)


def wait_for_mysql(identity, env):
    """仅当TCP连接和完整初始化版本表均可读时继续, 输出有限等待进度."""
    for attempt in range(60):
        result = docker(["exec", "--env", "MYSQL_PWD", identity, "mysql", "--protocol=TCP",
                         "-h127.0.0.1", "-uroot", "--batch", "--skip-column-names",
                         env["MYSQL_DATABASE"], "-e", "SELECT version FROM CC_SCHEMA_VERSION WHERE id=1"], env, False)
        if result.returncode == 0 and result.stdout.strip() == "1":
            return
        if attempt % 5 == 0:
            print("等待隔离MySQL初始化: " + str(attempt * 2) + "秒", flush=True)
        time.sleep(2)
    raise RuntimeError("隔离MySQL初始化未完成; 不连接或重置业务数据库")


def clean_build_outputs(root):
    """仅清理工作区四个已知模块的真实target目录, 拒绝符号链接或路径越界."""
    workspace = root.resolve()
    backend = (workspace / "backend").resolve()
    if not backend.is_relative_to(workspace):
        raise ValueError("后端构建目录超出工作区")
    for module in ("platform-common", "platform-biz", "platform-admin", "platform-api"):
        target = backend / module / "target"
        if target.is_symlink() or target.resolve() != target:
            raise ValueError("构建输出路径存在链接或重定向, 不执行清理: " + str(target))
        if target.exists():
            print("清理已知构建输出: backend/" + module + "/target", flush=True)
            shutil.rmtree(target)


def main():
    """创建临时MySQL/Redis后运行正式Maven生命周期, 无论成功失败均清理本次资源."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--maven-cache", type=Path, default=Path.home() / ".m2/repository")
    parser.add_argument("--settings", type=Path, default=ROOT / "deploy/maven-settings.xml")
    parser.add_argument("--maven-image", default="maven:3.9.11-eclipse-temurin-21")
    parser.add_argument("--mysql-image", default="mysql:8.0.36")
    parser.add_argument("--redis-image", default="redis:7.2-alpine")
    parser.add_argument("--package", action="store_true", help="测试通过后打包两个后端应用")
    parser.add_argument("--http", action="store_true", help="打包后启动隔离API并验证真实HTTP参考接口")
    parser.add_argument("--tests", help="仅运行逗号分隔的指定Java测试类")
    parser.add_argument("--review-http", action="store_true", help="仅验收商品、定制、反馈、公开内容和优惠结算")
    parser.add_argument("--http-only", action="store_true", help="只验证已有应用包的真实HTTP, 不编译或声明源码已构建")
    parser.add_argument("--native-workspace", action="store_true", help="在容器原生目录编译源码快照并恢复受控产物")
    parser.add_argument("--date", default=datetime.now().strftime("%Y-%m-%d"))
    residue = parser.add_mutually_exclusive_group()
    residue.add_argument("--list-residual", action="store_true", help="只读列出带标签的测试容器和网络")
    residue.add_argument("--clean-stopped-run", metavar="RUN_ID", help="清理指定批次已停止且归属明确的测试资源")
    args = parser.parse_args()
    if args.tests and not re.fullmatch(r"[A-Za-z0-9_]+(?:,[A-Za-z0-9_]+)*", args.tests):
        parser.error("--tests 接受逗号分隔的测试类名")
    if args.list_residual:
        list_residual()
        return
    if args.clean_stopped_run:
        clean_stopped_run(args.clean_stopped_run)
        return
    if args.review_http and not (args.http or args.http_only):
        parser.error("--review-http 需要配合 --http 或 --http-only")
    if args.http_only and (args.http or args.package or args.native_workspace):
        parser.error("--http-only不与--http、--package或--native-workspace组合使用")
    if args.http_only:
        for module in ("platform-admin", "platform-api"):
            if not (ROOT / "backend" / module / "target" / (module + ".jar")).is_file():
                parser.error("缺少已有应用包, 请先离线构建: " + module)
    day = datetime.strptime(args.date, "%Y-%m-%d").strftime("%Y%m%d")
    cache = args.maven_cache.resolve()
    settings = args.settings.resolve()
    if not args.http_only and (not cache.is_dir() or not settings.is_file()):
        raise ValueError("请先准备Maven缓存及与缓存来源一致的settings; 本工具不下载依赖")
    for image in (args.maven_image, args.mysql_image, args.redis_image):
        docker(["image", "inspect", image])
    run_id = uuid.uuid4().hex[:12]
    database = "caratcraft_init_audit_" + day + "_" + run_id[:6]
    output = ROOT / ".tools/integration" / (day + "-" + run_id)
    output.mkdir(parents=True, exist_ok=False)
    env = os.environ.copy()
    env.update({"MYSQL_ROOT_PASSWORD": secrets.token_urlsafe(32), "MYSQL_DATABASE": database,
                "MYSQL_USER": "isolated_app", "MYSQL_PASSWORD": secrets.token_urlsafe(32)})
    env.update({"MYSQL_PWD": env["MYSQL_ROOT_PASSWORD"], "CARATCRAFT_DATABASE_TEST": "true",
                "CARATCRAFT_DATABASE_TEST_URL": "jdbc:mariadb://127.0.0.1:3306/" + database,
                "CARATCRAFT_DATABASE_TEST_PASSWORD": env["MYSQL_ROOT_PASSWORD"],
                "CARATCRAFT_REDIS_TEST": "true", "CARATCRAFT_REDIS_TEST_PORT": "6379"})
    fixtures = Fixtures(run_id)

    def finish_on_signal(signum, _frame):
        """由主流程的finally释放已经登记的测试资源."""
        raise SystemExit(128 + signum)

    for signum in (signal.SIGTERM, getattr(signal, "SIGBREAK", None)):
        if signum is not None:
            signal.signal(signum, finish_on_signal)
    try:
        print("[1/4] 创建全新隔离MySQL和Redis, 不复用业务卷", flush=True)
        network = fixtures.create_network()
        mysql_args = ["--network", network, "--tmpfs", "/var/lib/mysql",
                      "--mount", "type=bind,source=" + str(ROOT / "database/init") + ",target=/docker-entrypoint-initdb.d,readonly"]
        for key in ("MYSQL_ROOT_PASSWORD", "MYSQL_DATABASE", "MYSQL_USER", "MYSQL_PASSWORD"):
            mysql_args.extend(["--env", key])
        mysql = fixtures.start("caratcraft-mysql-" + run_id, [*mysql_args, args.mysql_image,
                               "--character-set-server=utf8mb4", "--collation-server=utf8mb4_general_ci",
                               "--default-authentication-plugin=mysql_native_password"], env)
        fixtures.start("caratcraft-redis-" + run_id, ["--network", "container:" + mysql,
                       "--tmpfs", "/data", args.redis_image, "redis-server", "--save", "", "--appendonly", "no"], env)
        wait_for_mysql(mysql, env)
        if args.http_only:
            print("[2/4] 仅验收已有应用包, 不清理或重新编译源码", flush=True)
            from reference_http_probe import verify_reference_http
            verify_reference_http(ROOT, fixtures, mysql, args.maven_image, env, output, docker, args.review_http)
            print("[4/4] 已有应用包HTTP验证完成, 清理本次资源", flush=True)
            return
        print("[2/4] 清理固定构建输出并执行完整离线回归: " + database, flush=True)
        clean_build_outputs(ROOT)
        command = ["docker", "run", "--rm", "--pull=never", "--network", "container:" + mysql,
                   "--mount", "type=bind,source=" + str(ROOT) + ",target=/workspace",
                   "--mount", "type=bind,source=" + str(cache) + ",target=/root/.m2/repository,readonly",
                   "--mount", "type=bind,source=" + str(settings) + ",target=/tmp/test-settings.xml,readonly",
                   "--workdir", "/workspace/backend"]
        for key in ("CARATCRAFT_DATABASE_TEST", "CARATCRAFT_DATABASE_TEST_URL", "CARATCRAFT_DATABASE_TEST_PASSWORD",
                    "CARATCRAFT_REDIS_TEST", "CARATCRAFT_REDIS_TEST_PORT"):
            command.extend(["--env", key])
        command.extend([args.maven_image, "mvn", "-o", "-B", "-s", "/tmp/test-settings.xml",
                        "-DforkCount=0", "package" if args.package or args.http else "test"])
        if args.tests:
            command.extend(["-Dtest=" + args.tests, "-Dsurefire.failIfNoSpecifiedTests=false"])
        if args.native_workspace:
            from native_maven import command as native_command
            command = native_command(
                ROOT, cache, settings, output, args.maven_image, mysql,
                "package" if args.package or args.http else "test",
                ("CARATCRAFT_DATABASE_TEST", "CARATCRAFT_DATABASE_TEST_URL", "CARATCRAFT_DATABASE_TEST_PASSWORD",
                 "CARATCRAFT_REDIS_TEST", "CARATCRAFT_REDIS_TEST_PORT"), args.tests)
        log = output / "maven.log"
        with log.open("w", encoding="utf-8") as stream:
            result = subprocess.run(command, cwd=ROOT, env=env, stdout=stream, stderr=subprocess.STDOUT)
        if args.native_workspace:
            from native_maven import restore_outputs
            if (output / "native-results/targets.tar").is_file():
                restore_outputs(ROOT, output)
            elif result.returncode == 0:
                raise RuntimeError("原生构建成功但没有返回产物归档")
        for line in log.read_text("utf-8", errors="replace").splitlines():
            if "Tests run:" in line or "BUILD " in line or "[ERROR]" in line:
                print(line, flush=True)
        if result.returncode:
            raise RuntimeError("Maven离线验证失败, 日志: " + str(log))
        print("[3/4] Maven回归通过, 日志: " + str(log), flush=True)
        if args.http:
            from reference_http_probe import verify_reference_http
            verify_reference_http(ROOT, fixtures, mysql, args.maven_image, env, output, docker, args.review_http)
        print("[4/4] 清理本次测试容器和内部网络", flush=True)
    finally:
        fixtures.close()


if __name__ == "__main__":
    main()
