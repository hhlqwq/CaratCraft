"""在独立临时Redis运行短信及购买上下文回归, 不访问业务Redis或短信供应商."""

import argparse
from datetime import datetime
import os
from pathlib import Path
import re
import secrets
import subprocess
import uuid

ROOT = Path(__file__).resolve().parents[2]


def main():
    """只管理本次创建的随机容器, 密码仅用于合成测试且不输出到日志."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--date", required=True, help="客户端日期, YYYY-MM-DD")
    args = parser.parse_args()
    day = datetime.strptime(args.date, "%Y-%m-%d").strftime("%Y-%m-%d")
    temporary = Path("D:/tmp/hailongcodex") / day
    temporary.mkdir(parents=True, exist_ok=True)
    name = "caratcraft-regression-redis-" + uuid.uuid4().hex[:12]
    password = secrets.token_urlsafe(32)
    result = subprocess.run([
        "docker", "run", "--detach", "--rm", "--pull=never", "--name", name,
        "--publish", "127.0.0.1::6379", "--read-only", "--tmpfs", "/data",
        "redis:7.2-alpine", "redis-server", "--save", "", "--appendonly", "no",
        "--requirepass", password,
    ], capture_output=True, text=True)
    if result.returncode:
        raise RuntimeError("独立Redis启动失败: " + result.stderr)
    container = result.stdout.strip()
    if not re.fullmatch(r"[a-f0-9]{64}", container):
        raise RuntimeError("未取得本次独立容器的有效标识")
    try:
        mapping = subprocess.check_output(["docker", "port", container, "6379/tcp"], text=True).strip()
        if not re.fullmatch(r"127\.0\.0\.1:[0-9]+", mapping):
            raise RuntimeError("Redis测试端口必须只绑定回环地址")
        env = os.environ.copy()
        env["CARATCRAFT_REDIS_TEST"] = "true"
        env["CARATCRAFT_REDIS_TEST_PORT"] = mapping.split(":")[-1]
        env["CARATCRAFT_REDIS_TEST_PASSWORD"] = password
        env["JAVA_HOME"] = str(ROOT / ".tools/jdk21/jdk-21.0.8+9")
        env["PATH"] = env["JAVA_HOME"] + "/bin;" + env["PATH"]
        agent = ROOT / ".tools/m2/net/bytebuddy/byte-buddy-agent/1.14.13/byte-buddy-agent-1.14.13.jar"
        env["MAVEN_OPTS"] = f"-javaagent:{agent} -Djava.io.tmpdir={temporary} -Djansi.tmpdir={temporary} -Dfile.encoding=UTF-8"
        log = ROOT / ".tools/copyright/redis-regression.log"
        print("[1/2] 独立Redis已启动, 执行离线回归", flush=True)
        with log.open("w", encoding="utf-8") as output:
            tested = subprocess.run([
                str(ROOT / ".tools/maven/apache-maven-3.9.11/bin/mvn.cmd"), "-o",
                "-s", str(ROOT / ".tools/copyright/maven-central.xml"),
                "-f", str(ROOT / "backend/pom.xml"),
                "-Dmaven.repo.local=" + str(ROOT / ".tools/m2"), "-DforkCount=0",
                "-Dsurefire.failIfNoSpecifiedTests=false", "-Dtest=MobileVerificationRedisTest", "test",
            ], cwd=ROOT, env=env, stdout=output, stderr=subprocess.STDOUT)
        for line in log.read_text("utf-8", errors="replace").splitlines():
            if "Tests run:" in line or "BUILD " in line or "[ERROR]" in line:
                print(line, flush=True)
        if tested.returncode:
            raise RuntimeError("Redis回归失败, 请查看工作区日志")
    finally:
        subprocess.run(["docker", "stop", "--time", "5", container], capture_output=True, check=True)
        print("[2/2] 本次独立Redis已停止并自动移除, 业务Redis未变更", flush=True)


if __name__ == "__main__":
    main()
