"""按实际运行组件选择许可分支, 下载需随商业交付提供的对应库源码."""

import hashlib
import json
import urllib.request
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
LEGAL = ROOT / "legal"
INDEX = LEGAL / "generated/runtime-components.json"


def select_license(record):
    """依据已取得的原始声明选择适合本项目的许可分支, 未知项保持阻断."""
    names = " OR ".join(item["name"] for item in record["licenses"])
    lower = names.lower()
    if "apache" in lower:
        if "3.0" in lower:
            return "待核验"
        return "Apache-1.1" if "1.1" in names and "2.0" not in names else "Apache-2.0"
    if "mit" in lower or "bouncy" in lower:
        return "MIT"
    if "bsd" in lower or "distribution license" in lower or "edl" in lower:
        return "BSD/EDL (以随附版本原文为准)"
    if "eclipse public" in lower or "epl" in lower:
        return "EPL-2.0" if "2.0" in names else "EPL-1.0"
    if "cddl" in lower:
        return "CDDL (以随附版本原文为准)"
    if "lgpl" in lower or "lesser" in lower:
        return "LGPL-2.1-or-later"
    if any(keyword in lower for keyword in ["mulan", "indiana", "w3c", "sax", "dom4j", "public domain", "similar to apache"]):
        return names
    if not names and record["file"].startswith("platform-"):
        return "项目模块, 底座Apache2及原源码声明"
    return "待核验"


def download(url, target):
    """从官方版本仓库下载小型证据文件, 防止意外下载大包."""
    target.parent.mkdir(parents=True, exist_ok=True)
    if target.is_file() and url.endswith(".sha1"):
        return target.read_bytes()
    request = urllib.request.Request(url, headers={"User-Agent": "CaratCraft-license-verification"})
    with urllib.request.urlopen(request, timeout=45) as response:
        if int(response.headers.get("Content-Length", 0)) > 5 * 1024 * 1024:
            raise ValueError("源码包超过5MiB, 需要另行确认下载范围")
        data = response.read(5 * 1024 * 1024 + 1)
    if len(data) > 5 * 1024 * 1024:
        raise ValueError("源码包超过5MiB")
    target.write_bytes(data)
    return data


def main():
    """保存原始库源码、校验值及许可选择, 不将库源码改署名."""
    data = json.loads(INDEX.read_text(encoding="utf-8"))
    failures = []
    sources = []
    for record in data["backend"]:
        chosen = select_license(record)
        record["selectedLicense"] = chosen
        if chosen.startswith(("EPL-", "CDDL", "LGPL-")):
            url = record["sourceUrl"]
            name = url.rsplit("/", 1)[-1]
            target = LEGAL / "sources" / name
            try:
                print("核验并提供对应源码: " + name, flush=True)
                content = target.read_bytes() if target.is_file() else download(url, target)
                checksum = download(url + ".sha1", LEGAL / "sources" / (name + ".sha1"))
                if hashlib.sha1(content).hexdigest() != checksum.decode("ascii").strip().split()[0]:
                    raise ValueError("官方SHA1不一致")
                source = {"file": target.relative_to(ROOT).as_posix(), "url": url,
                          "sha256": hashlib.sha256(content).hexdigest(), "license": chosen}
                sources.append(source)
                record["correspondingSource"] = source["file"]
            except Exception as error:
                failures.append({"file": record["file"], "reason": str(error)})
        if chosen == "待核验":
            failures.append({"file": record["file"], "reason": "未找到适用许可"})
    permissive = {"MIT", "Apache-2.0", "BSD-2-Clause", "BSD-3-Clause", "ISC", "0BSD"}
    for record in data["frontend"]:
        license_value = record["license"]
        if isinstance(license_value, list) and len(license_value) == 1:
            license_value = license_value[0].get("type", "")
        if isinstance(license_value, dict):
            license_value = license_value.get("type", "")
        if record["name"] == "dompurify" and record["version"] == "3.4.17":
            license_value = "Apache-2.0"
        record["selectedLicense"] = license_value if license_value in permissive else "待核验"
        if record["selectedLicense"] == "待核验":
            failures.append({"file": record["name"] + "@" + record["version"],
                             "reason": "前端许可需要单独审核, 未自动批准"})
    data["sourceBundles"] = sources
    data["distributionBlockers"] = failures
    INDEX.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"sources": len(sources), "blockers": failures}, ensure_ascii=False))
    if failures:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
