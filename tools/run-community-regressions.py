"""在独立测试库运行社区、排列、商城媒体及交易回归, 不启动项目服务."""

import argparse
from datetime import datetime
import os
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]


def main():
    """密码只进入子进程环境, 输出中仅保留测试进度和失败摘要."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--database", required=True)
    parser.add_argument("--package", action="store_true", help="定向测试通过后同时打包后端")
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
    temporary = Path("D:/tmp/hailongcodex") / datetime.now().strftime("%Y-%m-%d")
    temporary.mkdir(parents=True, exist_ok=True)
    env["MAVEN_OPTS"] = f'-javaagent:{agent} -Djava.io.tmpdir={temporary} -Djansi.tmpdir={temporary} -Dfile.encoding=UTF-8'
    tests = ",".join(["CustomQuoteServiceImplTest", "CustomDesignServiceImplTest", "CustomConfigurationServiceTest",
                      "CustomProductionWorkflowTest", "CommunityDatabaseTest", "OrderTransactionDatabaseTest",
                      "AppCustomizationControllerTest", "AppCommunityControllerTest", "ReadinessControllerTest", "SafeImageUploadTest",
                      "ProductVideoUploadTest", "PearlProductValidatorTest", "PearlCatalogServiceTest", "DiskCloudStorageServiceTest"])
    command = [str(ROOT / ".tools/maven/apache-maven-3.9.11/bin/mvn.cmd"), "-o",
               "-s", str(ROOT / ".tools/copyright/maven-central.xml"),
               "-f", str(ROOT / "vendor/platform-wxshop/pom.xml"),
               "-Dmaven.repo.local=" + str(ROOT / ".tools/m2"), "-DforkCount=0",
               "-Dsurefire.failIfNoSpecifiedTests=false", "-Dtest=" + tests,
               "package" if args.package else "test"]
    log = ROOT / ".tools/copyright/community-java-tests.log"
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
