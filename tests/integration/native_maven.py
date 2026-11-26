"""使用受控源码快照在容器原生目录构建, 避免Windows逐个class文件跨文件系统写入."""

from pathlib import Path, PurePosixPath
import shlex
import subprocess
import tarfile

MODULES = ("platform-common", "platform-biz", "platform-admin", "platform-api")


def prepare_inputs(root, cache, output):
    """只打包Git可见源码和公开依赖缓存, 拒绝链接及工作区外输出."""
    root = root.resolve()
    if output.is_symlink():
        raise ValueError("原生构建输出拒绝链接")
    output = output.resolve()
    if not output.is_relative_to(root):
        raise ValueError("原生构建输出必须属于工作区")
    inputs = output / "native-inputs"
    results = output / "native-results"
    inputs.mkdir(exist_ok=False)
    results.mkdir(exist_ok=False)
    names = subprocess.run(["git", "ls-files", "--cached", "--others", "--exclude-standard", "-z"],
                           cwd=root, check=True, capture_output=True).stdout.decode("utf-8").split("\0")
    count = 0
    with tarfile.open(inputs / "source.tar", "w") as archive:
        for name in sorted(set(names) - {""}):
            relative = PurePosixPath(name)
            if relative.is_absolute() or ".." in relative.parts or ":" in name or "\\" in name:
                raise ValueError("源码路径越界")
            if any(part in {".git", ".tools", "node_modules", "target", "dist", "secrets"}
                   for part in relative.parts):
                continue
            if ((relative.name.startswith(".env") and relative.name != ".env.example")
                    or relative.name == "project.private.config.json"
                    or relative.suffix.lower() in {".pem", ".p12", ".pfx", ".key"}):
                continue
            source = root / name
            if source.is_symlink() or not source.resolve().is_relative_to(root):
                raise ValueError("源码快照拒绝链接及越界路径")
            if source.is_file():
                archive.add(source, arcname=name, recursive=False)
                count += 1
    if count == 0:
        raise ValueError("源码快照为空")
    print("原生构建源码快照: " + str(count) + "个文件", flush=True)
    with tarfile.open(inputs / "cache.tar", "w") as archive:
        archive.add(cache, arcname="repository")
    print("原生构建缓存快照完成, 不下载依赖", flush=True)
    return inputs, results


def command(root, cache, settings, output, image, mysql, goal, environment_keys, tests=None):
    """生成明确Docker参数, Maven禁网解析且秘密只按环境变量名传入."""
    if goal not in {"test", "package"}:
        raise ValueError("未知Maven验收目标")
    inputs, results = prepare_inputs(root, cache, output)
    arguments = ["docker", "run", "--rm", "--pull=never", "--network", "container:" + mysql,
                 "--tmpfs", "/native:rw,size=2g", "--tmpfs", "/root/.m2/repository:rw,size=512m",
                 "--mount", f"type=bind,source={inputs},target=/inputs,readonly",
                 "--mount", f"type=bind,source={results},target=/results",
                 "--mount", f"type=bind,source={settings},target=/tmp/test-settings.xml,readonly",
                 "--workdir", "/native"]
    for key in environment_keys:
        arguments.extend(["--env", key])
    selection = " -Dsurefire.failIfNoSpecifiedTests=false " + shlex.quote("-Dtest=" + tests) if tests else ""
    script = (
        "tar -xf /inputs/source.tar -C /native; "
        "tar -xf /inputs/cache.tar -C /root/.m2/repository --strip-components=1; "
        "cd /native/backend; "
        "status=0; mvn -o -B -s /tmp/test-settings.xml -DforkCount=0 " + goal + selection + " || status=$?; "
        "tar -cf /results/targets.tar --files-from /dev/null; "
        "for module in platform-common platform-biz platform-admin platform-api; do "
        "if [ -d \"$module/target\" ]; then tar -rf /results/targets.tar \"$module/target\"; fi; done; "
        "exit \"$status\""
    )
    return [*arguments, image, "sh", "-ec", script]


def restore_outputs(root, output):
    """只恢复四模块target中的普通文件, 拒绝归档越界、链接和意外源码覆盖."""
    root = root.resolve()
    backend = root / "backend"
    if backend.is_symlink() or backend.resolve() != backend:
        raise ValueError("后端目录被重定向")
    source = output / "native-results/targets.tar"
    with tarfile.open(source) as archive:
        members = archive.getmembers()
        if len(members) > 50000 or sum(item.size for item in members) > 2 * 1024 ** 3:
            raise ValueError("构建产物归档超过上限")
        for item in members:
            parts = PurePosixPath(item.name).parts
            if ("\\" in item.name or ":" in item.name or len(parts) < 2 or parts[0] not in MODULES
                    or parts[1] != "target" or ".." in parts
                    or not (item.isfile() or item.isdir())):
                raise ValueError("构建产物路径或类型不安全: " + item.name)
            target = backend / parts[0] / "target"
            if target.is_symlink() or target.resolve() != target:
                raise ValueError("构建目录被重定向")
            if not (backend / item.name).resolve().is_relative_to(target):
                raise ValueError("构建产物越界")
        archive.extractall(backend, members=members, filter="data")
    print("原生构建产物和测试报告已恢复到四模块target", flush=True)
