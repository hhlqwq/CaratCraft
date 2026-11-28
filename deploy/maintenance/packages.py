"""校验固定格式的签名离线发行包, 不从包内接受信任根或可执行脚本."""

import hashlib
import json
import os
from pathlib import Path, PurePosixPath
import re
import shutil
import subprocess
import tempfile
import zipfile

import docker_api

PUBLIC_KEY = Path(os.environ.get("CARATCRAFT_RELEASE_PUBLIC_KEY", "/app/trust/release.pem"))
MAX_PACKAGE = 4 * 1024**3
MAX_EXPANDED = 8 * 1024**3


def digest(path):
    """流式计算文件摘要, 避免将镜像或备份整体载入内存."""
    hasher = hashlib.sha256()
    with open(path, "rb") as source:
        for block in iter(lambda: source.read(1024 * 1024), b""):
            hasher.update(block)
    return hasher.hexdigest()


def version_tuple(value):
    """仅接受正式的三段发行编号, 拒绝使用Git哈希充当版本."""
    if not isinstance(value, str) or not re.fullmatch(r"(?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*)", value):
        raise ValueError("发行版本格式无效")
    return tuple(int(part) for part in value.split("."))


def verify_signature(content, signature):
    """只使用预置公钥验证Ed25519签名, 包内公钥不参与信任判断."""
    if len(signature) != 64 or not PUBLIC_KEY.is_file():
        raise ValueError("发行签名或预置信任公钥不可用")
    with tempfile.TemporaryDirectory(prefix="cc-signature-") as folder:
        manifest = Path(folder) / "manifest.json"
        signed = Path(folder) / "manifest.sig"
        manifest.write_bytes(content)
        signed.write_bytes(signature)
        result = subprocess.run(["openssl", "pkeyutl", "-verify", "-pubin", "-inkey", str(PUBLIC_KEY),
                                 "-rawin", "-in", str(manifest), "-sigfile", str(signed)],
                                capture_output=True, timeout=15, check=False)
        if result.returncode:
            raise ValueError("升级包发行签名验证失败")


def validate(package, destination, architecture):
    """签名、架构、完整清单及路径均通过后才解压, 拒绝链接、重复文件和压缩炸弹."""
    if Path(package).stat().st_size > MAX_PACKAGE:
        raise ValueError("升级包超过4 GiB限制")
    with zipfile.ZipFile(package) as archive:
        entries = archive.infolist()
        names = [entry.filename for entry in entries]
        if len(entries) > 4096 or len(names) != len(set(names)):
            raise ValueError("升级包文件数量异常或包含重复路径")
        total = 0
        for entry in entries:
            name = PurePosixPath(entry.filename)
            mode = entry.external_attr >> 16
            if (not name.parts or name.is_absolute() or ".." in name.parts or "\\" in entry.filename
                    or ":" in entry.filename or str(name) != entry.filename or entry.is_dir()
                    or mode & 0o170000 not in (0, 0o100000) or entry.flag_bits & 1):
                raise ValueError("升级包包含不允许的路径或文件类型")
            total += entry.file_size
            if total > MAX_EXPANDED:
                raise ValueError("升级包展开大小超过限制")
        if not {"manifest.json", "manifest.sig"} <= set(names):
            raise ValueError("升级包缺少发行清单或签名")
        if archive.getinfo("manifest.json").file_size > 256 * 1024 or archive.getinfo("manifest.sig").file_size != 64:
            raise ValueError("发行清单过大")
        content = archive.read("manifest.json")
        verify_signature(content, archive.read("manifest.sig"))
        manifest = json.loads(content)
        if not isinstance(manifest, dict):
            raise ValueError("发行清单必须为结构化对象")
        version_tuple(manifest.get("version"))
        version_tuple(manifest.get("minimumVersion"))
        if version_tuple(manifest["minimumVersion"]) > version_tuple(manifest["version"]) or not isinstance(manifest.get("builtAt"), str):
            raise ValueError("发行升级范围或构建时间无效")
        if manifest.get("format") != 1 or manifest.get("architecture") != architecture:
            raise ValueError("发行包格式或CPU架构不兼容")
        if manifest.get("deploymentProject") not in (None, docker_api.PROJECT):
            raise ValueError("发行包限定的部署项目与当前环境不一致")
        files = manifest.get("files")
        if not isinstance(files, dict) or set(names) != {"manifest.json", "manifest.sig", *files}:
            raise ValueError("发行清单与实际文件不一致")
        required = {"images.tar", "release.json", "deployment.json", "database/upgrade/local/manifest.txt",
                    "database/upgrade/local/current-schema.sql"}
        if not required <= set(files):
            raise ValueError("发行包缺少部署、镜像或迁移资源")
        allowed = re.compile(r"(?:images\.tar|release\.json|deployment\.json|legal/[A-Za-z0-9_./-]+|database/upgrade/[a-z0-9/-]+\.(?:sql|txt))")
        if any(not allowed.fullmatch(name) for name in files):
            raise ValueError("发行包包含范围外资源")
        if shutil.disk_usage(destination.parent).free < total * 2 + 256 * 1024**2:
            raise ValueError("剩余空间不足以解压、加载并备份升级数据")
        destination.mkdir(mode=0o700)
        try:
            for name, expected in files.items():
                if not re.fullmatch(r"[a-f0-9]{64}", str(expected)):
                    raise ValueError("文件摘要格式无效")
                target = destination / name
                target.parent.mkdir(parents=True, exist_ok=True)
                hasher = hashlib.sha256()
                with archive.open(name) as source, open(target, "xb") as output:
                    for block in iter(lambda: source.read(1024 * 1024), b""):
                        hasher.update(block)
                        output.write(block)
                if hasher.hexdigest() != expected:
                    raise ValueError("文件完整性校验失败: " + name)
            release = json.loads((destination / "release.json").read_text())
            if release.get("version") != manifest["version"]:
                raise ValueError("软件版本与发行清单不一致")
            deployment = json.loads((destination / "deployment.json").read_text())
            if set(deployment) != {"backend", "admin-ui", "maintenance"}:
                raise ValueError("运行镜像清单不完整")
            for item in deployment.values():
                if set(item) != {"id", "sourceDigest"} or not re.fullmatch(r"sha256:[a-f0-9]{64}", item["id"]) or not re.fullmatch(r"[a-f0-9]{64}", item["sourceDigest"]):
                    raise ValueError("镜像身份声明无效")
            manifest["deployment"] = deployment
            manifest["expandedBytes"] = total
            return manifest
        except BaseException:
            shutil.rmtree(destination)
            raise
