"""核对当前应用发布物与许可索引, 不批准完整第三方基础镜像再分发."""

import hashlib
import importlib.util
import json
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
BACKEND = ROOT / "backend"
LEGAL = ROOT / "legal"


def main():
    """验证实际库哈希、必要源码、项目素材及已移除组件不再入包."""
    data = json.loads((LEGAL / "generated/runtime-components.json").read_text(encoding="utf-8"))
    errors = list(data.get("distributionBlockers", []))
    errors.extend(data.get("licenseEvidenceBlockers", []))
    spec = importlib.util.spec_from_file_location("license_inventory", ROOT / "tools/legal/build-license-bundle.py")
    inventory = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(inventory)
    indexed_frontend = {(entry["name"], entry["version"]) for entry in data["frontend"]}
    for folder in inventory.frontend_package_roots():
        metadata = json.loads((folder / "package.json").read_text(encoding="utf-8"))
        key = (metadata["name"], metadata["version"])
        if key not in indexed_frontend:
            errors.append({"file": "@".join(key), "reason": "实际入包前端组件未完成许可准入"})
    approved = {entry["file"]: entry for entry in data["backend"]}
    for module in ["platform-admin", "platform-api"]:
        jar = BACKEND / module / "target" / (module + ".jar")
        with zipfile.ZipFile(jar) as archive:
            for entry in archive.namelist():
                if not entry.startswith("BOOT-INF/lib/") or not entry.endswith(".jar"):
                    continue
                name = entry.rsplit("/", 1)[-1]
                record = approved.get(name)
                if not record or record.get("selectedLicense") == "待核验":
                    errors.append({"file": name, "reason": "缺少许可选择"})
                    continue
                if hashlib.sha256(archive.read(entry)).hexdigest() != record["sha256"]:
                    errors.append({"file": name, "reason": "发布JAR版本/哈希变化"})
                if name.startswith(("mysql-connector", "ojdbc", "aizuda-monitor", "velocity-tools", "struts-")):
                    errors.append({"file": name, "reason": "已移除依赖仍入包"})
    for record in data["backend"] + data["frontend"]:
        if not record.get("selectedLicense") or record["selectedLicense"] == "待核验":
            errors.append({"file": record.get("file", record.get("name")), "reason": "许可未准入"})
        for evidence in record.get("evidence", []):
            if not (ROOT / evidence).is_file():
                errors.append({"file": evidence, "reason": "许可证据丢失"})
    for source in data.get("sourceBundles", []) + [entry["originalPackage"] for entry in data["frontend"]
                                                    if "originalPackage" in entry]:
        path = ROOT / source["file"]
        if not path.is_file() or hashlib.sha256(path.read_bytes()).hexdigest() != source["sha256"]:
            errors.append({"file": source["file"], "reason": "对应源码或原包变化"})
    assets = json.loads((LEGAL / "project-assets.json").read_text(encoding="utf-8"))
    for asset in assets:
        path = ROOT / asset["file"]
        if not path.is_file() or hashlib.sha256(path.read_bytes()).hexdigest() != asset["sha256"]:
            errors.append({"file": asset["file"], "reason": "项目绘图来源/哈希变化"})
    forbidden = ["apps/admin-ui/static/ueditor", "apps/admin-ui/src/components/ueditor",
                 "apps/miniapp/lib/wxParse", ".qoder", "apps/miniapp/agent", "apps/miniapp/skills"]
    for relative in forbidden:
        if (ROOT / relative).exists():
            errors.append({"file": relative, "reason": "已移除目录重新出现"})
    dist = ROOT / ".tools/copyright/admin-ui-dist"
    if not dist.is_dir():
        errors.append({"file": str(dist), "reason": "缺少本次前端产物"})
    else:
        for file in dist.rglob("*"):
            if file.is_file() and (file.suffix.lower() in {".swf", ".exe"} or "ueditor" in file.parts):
                errors.append({"file": file.relative_to(ROOT).as_posix(), "reason": "旧插件仍被发布"})
    result = {"backendComponents": len(data["backend"]), "frontendComponents": len(data["frontend"]),
              "projectAssets": len(assets), "sourceBundles": len(data.get("sourceBundles", [])),
              "errors": errors}
    (LEGAL / "distribution-check.json").write_text(
        json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False))
    if errors:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
