"""补齐缺失的同版本npm许可及阿里云SDK源码声明, 所有下载只用于权利证据."""

import hashlib
import base64
import json
import re
import urllib.request
import zipfile
from pathlib import Path
from urllib.parse import quote


ROOT = Path(__file__).resolve().parents[2]
LEGAL = ROOT / "legal"
INDEX = LEGAL / "generated/runtime-components.json"


def fetch(url):
    """读取官方元数据或小型声明文件, 限制响应大小."""
    request = urllib.request.Request(url, headers={"User-Agent": "CaratCraft-license-evidence"})
    with urllib.request.urlopen(request, timeout=40) as response:
        data = response.read(5 * 1024 * 1024 + 1)
    if len(data) > 5 * 1024 * 1024:
        raise ValueError("证据文件超过5MiB")
    return data


def npm_license(record):
    """根据实际发布版本的gitHead寻找仓库根许可, 不套用最新版声明."""
    name, version = record["name"], record["version"]
    if any("LICENSE" in Path(path).name.upper() for path in record["evidence"]):
        return
    metadata = json.loads(fetch("https://registry.npmjs.org/" + quote(name, safe="@") + "/" + version))
    repository = metadata.get("repository", record.get("source", ""))
    repository = repository.get("url", "") if isinstance(repository, dict) else repository
    match = re.search(r"github.com[/:]([^/]+/[^/#]+)", repository)
    revision = metadata.get("gitHead")
    if not match or not revision:
        raise ValueError("缺少同版本仓库或gitHead")
    repository = match.group(1).removesuffix(".git")
    tree_url = f"https://api.github.com/repos/{repository}/git/trees/{revision}"
    tree = json.loads(fetch(tree_url))
    files = [entry["path"] for entry in tree.get("tree", []) if entry["type"] == "blob"
             and entry["path"].upper().startswith(("LICENSE", "LICENCE", "COPYING"))]
    if not files:
        raise ValueError("同版本仓库根目录未找到许可文件")
    folder = LEGAL / "generated/evidence" / (name.replace("/", "_").replace("@", "_") + "-" + version)
    folder.mkdir(parents=True, exist_ok=True)
    for file in files:
        url = f"https://raw.githubusercontent.com/{repository}/{revision}/{quote(file)}"
        content = fetch(url)
        target = folder / ("UPSTREAM-" + file)
        target.write_bytes(content)
        record["evidence"].append(target.relative_to(ROOT).as_posix())
        record.setdefault("upstreamEvidence", []).append({"url": url,
            "sha256": hashlib.sha256(content).hexdigest(), "gitHead": revision})


def sdk_license(record):
    """从确切版本的官方源码提取许可头和署名, 保留原始权利人."""
    url = record["sourceUrl"]
    archive_path = ROOT / ".tools/copyright" / url.rsplit("/", 1)[-1]
    content = fetch(url)
    checksum = fetch(url + ".sha1").decode("ascii").strip().split()[0]
    if hashlib.sha1(content).hexdigest() != checksum:
        raise ValueError("官方源码校验不一致")
    archive_path.write_bytes(content)
    headers = set()
    with zipfile.ZipFile(archive_path) as archive:
        for entry in archive.namelist():
            if entry.endswith(".java"):
                text = archive.read(entry).decode("utf-8", "replace")
                if text.lstrip().startswith("/*"):
                    headers.add(text[:text.index("*/") + 2])
    if not any("Apache License, Version 2.0" in header for header in headers):
        raise ValueError("源码中未找到Apache2授权")
    target = LEGAL / "generated/evidence" / record["file"] / "SOURCE-COPYRIGHT-NOTICES.txt"
    target.write_text("\n\n".join(sorted(headers)), encoding="utf-8")
    record["evidence"].append(target.relative_to(ROOT).as_posix())
    record["licenses"] = [{"name": "Apache-2.0", "evidence": target.relative_to(ROOT).as_posix()}]
    record["status"] = "已核对同版本官方源码许可"


def preserve_mit_package(record):
    """保留明确声明MIT的原始发布包和完整许可文本, 不编造版权日期或权利人."""
    if record["license"] != "MIT":
        raise ValueError("只为明确声明MIT的版本补齐文本")
    name, version = record["name"], record["version"]
    metadata = json.loads(fetch("https://registry.npmjs.org/" + quote(name, safe="@") + "/" + version))
    if metadata.get("license") != "MIT":
        raise ValueError("官方同版本元数据没有明确声明MIT")
    dist = metadata["dist"]
    content = fetch(dist["tarball"])
    if dist.get("integrity", "").startswith("sha512-"):
        actual = "sha512-" + base64.b64encode(hashlib.sha512(content).digest()).decode("ascii")
        if actual != dist["integrity"]:
            raise ValueError("npm官方发布包SHA512不一致")
    elif hashlib.sha1(content).hexdigest() != dist["shasum"]:
        raise ValueError("npm官方发布包SHA1不一致")
    source = LEGAL / "sources" / (name.replace("/", "_") + "-" + version + ".tgz")
    source.parent.mkdir(parents=True, exist_ok=True)
    source.write_bytes(content)
    folder = LEGAL / "generated/evidence" / (name.replace("/", "_") + "-" + version)
    folder.mkdir(parents=True, exist_ok=True)
    original = LEGAL / "licenses/element-ui/LICENSE"
    text = original.read_text(encoding="utf-8")
    text = "The MIT License (MIT)\n\n" + text[text.index("Permission is hereby granted"):]
    license_file = folder / "MIT-TEXT.txt"
    license_file.write_text(text, encoding="utf-8")
    metadata_file = folder / "OFFICIAL-VERSION-METADATA.json"
    metadata_file.write_text(json.dumps(metadata, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    record["evidence"].extend([license_file.relative_to(ROOT).as_posix(),
                               metadata_file.relative_to(ROOT).as_posix()])
    record["originalPackage"] = {"file": source.relative_to(ROOT).as_posix(),
                                  "url": dist["tarball"],
                                  "sha256": hashlib.sha256(content).hexdigest()}
    record["evidenceMethod"] = "官方同版本MIT声明+原始发布包全部版权声明+标准MIT文本, 不替换署名"


def main():
    """补齐证据并记录无法取得的项目, 不静默忽略缺口."""
    data = json.loads(INDEX.read_text(encoding="utf-8"))
    failures = []
    if not any(item["name"] == "normalize.css" for item in data["frontend"]):
        data["frontend"].append({"name": "normalize.css", "version": "7.0.0", "license": "MIT",
                                 "source": "https://github.com/necolas/normalize.css", "evidence": []})
    for record in data["frontend"]:
        try:
            print("核对前端许可原文: " + record["name"] + " " + record["version"], flush=True)
            npm_license(record)
        except Exception as error:
            try:
                preserve_mit_package(record)
                print("保留同版本MIT原始发布包: " + record["name"], flush=True)
            except Exception as fallback_error:
                failures.append({"name": record["name"], "version": record["version"],
                                 "reason": str(error) + "; " + str(fallback_error)})
    for record in data["backend"]:
        if record["file"].startswith("aliyun-java-sdk-") and not any(
                item.get("name") for item in record["licenses"]):
            try:
                print("核对SDK源码声明: " + record["file"], flush=True)
                sdk_license(record)
            except Exception as error:
                failures.append({"file": record["file"], "reason": str(error)})
    data["licenseEvidenceBlockers"] = failures
    INDEX.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"blockers": failures}, ensure_ascii=False))
    if failures:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
