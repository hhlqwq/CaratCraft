"""导出当前工作区的应用交付包, 不包含Git历史、私有配置或第三方基础镜像."""

import hashlib
import json
import subprocess
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
VENDOR = ROOT / "vendor/platform-wxshop"
DESTINATION = ROOT / ".tools/copyright/release"


def source_files():
    """只导出当前源码树和本轮材料, 不复制已删除文件或历史审计截图."""
    result = subprocess.run(["git", "ls-files", "-z", "--cached", "--others", "--exclude-standard"],
                            cwd=ROOT, capture_output=True, check=True)
    selected = []
    for relative in set(result.stdout.decode("utf-8").split("\0")):
        if not relative:
            continue
        path = ROOT / relative
        if not path.is_file():
            continue
        if relative.startswith("vendor/platform-wxshop/"):
            if path.name.lower() == "agents.md":
                continue
            selected.append(path)
        elif relative.startswith(("tools/", "legal/", "data/", "docs/copyright-", "docs/remediation-",
                                  "docs/client-requirements-", "docs/wechat-ai-", "docs/source-style")):
            selected.append(path)
        elif relative in {"README.md", ".gitignore", ".gitattributes", "dev.ps1", "dev.sh"}:
            selected.append(path)
    # 许可材料是交付必需项, 即使外部全局Git规则有遗漏也必须完整导出.
    selected.extend(path for path in (VENDOR / "legal").rglob("*") if path.is_file())
    return sorted(set(selected))


def main():
    """准入检查通过后打包应用和可重新构建的源码, 输出校验清单."""
    import sys
    subprocess.run([sys.executable, str(ROOT / "tools/check-copyright-distribution.py")],
                   cwd=ROOT, check=True)
    DESTINATION.mkdir(parents=True, exist_ok=True)
    source = DESTINATION / "caratcraft-source-20261016.zip"
    with zipfile.ZipFile(source, "w", compression=zipfile.ZIP_DEFLATED) as archive:
        for path in source_files():
            archive.write(path, path.relative_to(ROOT).as_posix())
    application = DESTINATION / "caratcraft-application-20261016.zip"
    with zipfile.ZipFile(application, "w", compression=zipfile.ZIP_DEFLATED) as archive:
        for module in ["platform-admin", "platform-api"]:
            jar = VENDOR / module / "target" / (module + ".jar")
            archive.write(jar, "backend/" + jar.name)
            print("已加入应用: " + jar.name, flush=True)
        for folder, prefix in [(ROOT / ".tools/copyright/admin-ui-dist", "frontend"),
                               (VENDOR / "wx-mall", "wx-mall"), (VENDOR / "legal", "legal")]:
            for path in folder.rglob("*"):
                if path.is_file():
                    archive.write(path, prefix + "/" + path.relative_to(folder).as_posix())
        archive.write(source, "source/" + source.name)
    records = [{"file": path.name, "bytes": path.stat().st_size,
                "sha256": hashlib.sha256(path.read_bytes()).hexdigest()}
               for path in [source, application]]
    manifest = DESTINATION / "manifest.json"
    manifest.write_text(json.dumps(records, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(records, ensure_ascii=False))


if __name__ == "__main__":
    main()
