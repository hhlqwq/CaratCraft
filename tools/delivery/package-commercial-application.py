"""导出当前工作区的应用交付包, 不包含Git历史、私有配置或第三方基础镜像."""

import hashlib
import argparse
import json
import subprocess
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
BACKEND = ROOT / "backend"
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
        if not application_file(path, ROOT):
            continue
        if path.name.lower() == "agents.md":
            continue
        if relative.startswith(("backend/", "apps/", "database/", "deploy/", "env/",
                                "tools/", "tests/", "legal/", "data/", "docs/")):
            selected.append(path)
        elif relative in {"README.md", ".gitignore", ".dockerignore", ".gitattributes",
                          ".editorconfig", ".prettierrc.json"}:
            selected.append(path)
    # 许可材料是交付必需项, 即使外部全局Git规则有遗漏也必须完整导出.
    selected.extend(path for path in (ROOT / "legal").rglob("*") if application_file(path, ROOT))
    return sorted(set(selected))


def application_file(path, root):
    """应用目录不递归收录私人配置、开发缓存或临时凭证."""
    relative = path.relative_to(root)
    return (path.is_file()
            and not any(part in {"node_modules", ".git", "secrets", "__pycache__", "target", "dist", ".tools", "packages"} for part in relative.parts)
            and path.name not in {"project.private.config.json", ".env"}
            and (not path.name.startswith(".env.") or path.name == ".env.example")
            and not path.name.endswith((".log", ".tmp", ".key", ".pem", ".p12"))
            and "application-local" not in path.name)


def verify_backend_files():
    """检查JAR内部私有资源, 防止旧target缓存绕过源码与Maven资源排除."""
    for module in ("platform-admin", "platform-api"):
        jar = BACKEND / module / "target" / (module + ".jar")
        with zipfile.ZipFile(jar) as archive:
            for entry in archive.namelist():
                name = Path(entry).name
                if ("application-local" in name or name == ".env"
                        or name.endswith((".key", ".pem", ".p12"))):
                    raise ValueError("后端构建包含私人配置或凭证资源, 请清除对应target残留并重新构建: " + module)


def main():
    """准入检查通过后打包应用和可重新构建的源码, 输出校验清单."""
    import sys
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--date", required=True, help="交付日期, YYYYMMDD, 使用客户端日期")
    args = parser.parse_args()
    from datetime import datetime
    day = datetime.strptime(args.date, "%Y%m%d").strftime("%Y%m%d")
    subprocess.run([sys.executable, str(ROOT / "tools/legal/check-copyright-distribution.py")],
                   cwd=ROOT, check=True)
    revision = subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=ROOT).decode().strip()
    dirty = bool(subprocess.check_output(["git", "status", "--porcelain"], cwd=ROOT).strip())
    if dirty:
        raise ValueError("正式交付必须使用已提交的干净源码树, 请先完成审阅并提交")
    verify_backend_files()
    if not dirty:
        assets = json.loads((ROOT / "legal/project-assets.json").read_text(encoding="utf-8"))
        for index, asset in enumerate(assets, 1):
            content = subprocess.check_output(["git", "show", "HEAD:" + asset["file"]], cwd=ROOT)
            if hashlib.sha256(content).hexdigest() != asset["sha256"]:
                raise ValueError("Git中的素材字节与来源清单不一致: " + asset["file"])
            if index % 50 == 0 or index == len(assets):
                print(f"Git素材校验进度: {index}/{len(assets)}", flush=True)
    DESTINATION.mkdir(parents=True, exist_ok=True)
    source = DESTINATION / f"caratcraft-source-{day}-{revision[:12]}.zip"
    application = DESTINATION / f"caratcraft-application-{day}-{revision[:12]}.zip"
    manifest = DESTINATION / f"manifest-{day}-{revision[:12]}.json"
    if any(path.exists() for path in (source, application, manifest)):
        raise FileExistsError("该提交的交付文件已存在, 请核对既有清单, 不覆盖历史交付")
    with zipfile.ZipFile(source, "w", compression=zipfile.ZIP_DEFLATED) as archive:
        selected = source_files()
        for path in selected:
            archive.write(path, path.relative_to(ROOT).as_posix())
        archive.writestr("source-files.json", json.dumps(
            [path.relative_to(ROOT).as_posix() for path in selected], ensure_ascii=False))
    with zipfile.ZipFile(application, "w", compression=zipfile.ZIP_DEFLATED) as archive:
        for module in ["platform-admin", "platform-api"]:
            jar = BACKEND / module / "target" / (module + ".jar")
            archive.write(jar, "backend/" + jar.name)
            print("已加入应用: " + jar.name, flush=True)
        for folder, prefix in [(ROOT / ".tools/copyright/admin-ui-dist", "frontend"),
                               (ROOT / "apps/miniapp", "miniapp"), (ROOT / "legal", "legal")]:
            for path in folder.rglob("*"):
                if application_file(path, folder):
                    archive.write(path, prefix + "/" + path.relative_to(folder).as_posix())
        archive.write(source, "source/" + source.name)
    records = [{"file": path.name, "bytes": path.stat().st_size,
                "sha256": hashlib.sha256(path.read_bytes()).hexdigest()}
               for path in [source, application]]
    for record in records:
        record.update(revision=revision, sourceTreeDirty=dirty, date=day)
    manifest.write_text(json.dumps(records, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(records, ensure_ascii=False))


if __name__ == "__main__":
    main()
