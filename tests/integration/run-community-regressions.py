"""在独立测试库运行社区、排列、商城媒体及交易回归, 不启动项目服务."""

import argparse
from datetime import datetime
import os
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[2]


def main():
    """密码只进入子进程环境, 输出中仅保留测试进度和失败摘要."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--database", required=True)
    parser.add_argument("--package", action="store_true", help="定向测试通过后同时打包后端")
    parser.add_argument("--date", help="客户端日期, YYYY-MM-DD, 用于临时目录")
    parser.add_argument("--docker", action="store_true", help="使用已有Maven/JDK镜像, 无需宿主机Java")
    parser.add_argument("--all", action="store_true", help="运行全部后端用例, 未配置的外部服务用例保留跳过状态")
    args = parser.parse_args()
    if not re.fullmatch(r"caratcraft_init_audit_[0-9]{8}_[a-f0-9]{6}", args.database):
        raise ValueError("只允许使用独立初始化测试库")
    env = os.environ.copy()
    password = subprocess.run(
        ["docker", "exec", "platform-mysql", "printenv", "MYSQL_ROOT_PASSWORD"],
        capture_output=True, check=True).stdout.decode().strip()
    env["CARATCRAFT_DATABASE_TEST"] = "true"
    env["CARATCRAFT_DATABASE_TEST_URL"] = "jdbc:mariadb://127.0.0.1:3306/" + args.database
    env["CARATCRAFT_DATABASE_TEST_PASSWORD"] = password
    env["JAVA_HOME"] = str(ROOT / ".tools/jdk21/jdk-21.0.8+9")
    env["PATH"] = env["JAVA_HOME"] + "/bin;" + env["PATH"]
    agent = ROOT / ".tools/m2/net/bytebuddy/byte-buddy-agent/1.14.13/byte-buddy-agent-1.14.13.jar"
    day = datetime.strptime(args.date, "%Y-%m-%d") if args.date else datetime.now()
    temporary = Path("D:/tmp/hailongcodex") / day.strftime("%Y-%m-%d")
    temporary.mkdir(parents=True, exist_ok=True)
    env["MAVEN_OPTS"] = f'-javaagent:{agent} -Djava.io.tmpdir={temporary} -Djansi.tmpdir={temporary} -Dfile.encoding=UTF-8'
    tests = ",".join([
        "CustomQuoteServiceImplTest",
        "CustomDesignServiceImplTest",
        "CustomConfigurationServiceTest",
        "CustomProductionWorkflowTest",
        "CommunityDatabaseTest",
        "OrderTransactionDatabaseTest",
        "AppCustomizationControllerTest",
        "AppCommunityControllerTest",
        "ReadinessControllerTest",
        "SafeImageUploadTest",
        "ProductVideoUploadTest",
        "PearlProductValidatorTest",
        "PearlCatalogServiceTest",
        "DiskCloudStorageServiceTest",
        "MerchantServicesDatabaseTest",
        "CartApplicationServiceTest",
        "CheckoutIntegrityTest",
        "FinishedLengthRulesTest",
        "LegalDocumentDatabaseTest",
        "AccountLoginDatabaseTest",
        "AccountLoginControllerTest",
    ])
    command = [str(ROOT / ".tools/maven/apache-maven-3.9.11/bin/mvn.cmd"), "-o",
               "-s", str(ROOT / ".tools/copyright/maven-central.xml"),
               "-f", str(ROOT / "backend/pom.xml"),
               "-Dmaven.repo.local=" + str(ROOT / ".tools/m2"), "-DforkCount=0",
               "-Dsurefire.failIfNoSpecifiedTests=false", "-Dtest=" + tests,
               "package" if args.package else "test"]
    if args.docker:
        cache = ROOT / ".tools/m2"
        if (cache / "repository").is_dir():
            cache = cache / "repository"
        # 同一离线缓存沿用下载时的官方仓库标识, 避免Maven拒绝已有依赖.
        settings = "/workspace/.tools/copyright/maven-central.xml"
        if not (ROOT / ".tools/copyright/maven-central.xml").is_file():
            settings = "/workspace/deploy/maven-settings.xml"
        container_agent = "/root/.m2/repository/net/bytebuddy/byte-buddy-agent/1.14.13/byte-buddy-agent-1.14.13.jar"
        env["MAVEN_OPTS"] = "-javaagent:" + container_agent + " -Djava.io.tmpdir=/test-temp -Djansi.tmpdir=/test-temp -Dfile.encoding=UTF-8"
        command = ["docker", "run", "--rm", "--pull=never", "--network", "container:platform-mysql",
                   "--mount", f"type=bind,source={ROOT},target=/workspace",
                   "--mount", f"type=bind,source={cache},target=/root/.m2/repository",
                   "--mount", f"type=bind,source={temporary},target=/test-temp",
                   "--workdir", "/workspace/backend"]
        for key in ("CARATCRAFT_DATABASE_TEST", "CARATCRAFT_DATABASE_TEST_URL",
                    "CARATCRAFT_DATABASE_TEST_PASSWORD", "MAVEN_OPTS"):
            command.extend(["--env", key])
        command.extend(["maven:3.9.11-eclipse-temurin-21", "mvn", "-o", "-B",
                        "-s", settings, "-DforkCount=0",
                        "-Dsurefire.failIfNoSpecifiedTests=false"])
        if not args.all:
            command.append("-Dtest=" + tests)
        command.append("package" if args.package else "test")
    elif args.all:
        command = [value for value in command if not value.startswith("-Dtest=")]
    log_name = "community-java-tests-docker.log" if args.docker else "community-java-tests.log"
    log = ROOT / ".tools/copyright" / log_name
    print("[1/2] 执行社区、排列、旧设计及交易回归: " + args.database, flush=True)
    with log.open("w", encoding="utf-8") as output:
        result = subprocess.run(command, cwd=ROOT, env=env, stdout=output, stderr=subprocess.STDOUT)
    text = log.read_text("utf-8", errors="replace")
    for line in text.splitlines():
        if "Tests run:" in line or "BUILD " in line or "[ERROR]" in line:
            print(line, flush=True)
    print("[2/2] 结果日志: " + str(log), flush=True)
    raise SystemExit(result.returncode)


if __name__ == "__main__":
    main()
