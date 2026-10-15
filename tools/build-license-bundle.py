"""从实际JAR和webpack模块生成第三方许可证据, 不自动把未知许可批准为可分发."""

import hashlib
import io
import json
import re
import xml.etree.ElementTree as ET
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
M2 = ROOT / ".tools/m2"
UI = ROOT / "vendor/platform-wxshop/platform-admin-ui"
OUTPUT = ROOT / "vendor/platform-wxshop/legal/generated"
NS = {"m": "http://maven.apache.org/POM/4.0.0"}


def pom_path(group, artifact, version):
    """定位本地官方Maven元数据文件."""
    return M2 / group.replace(".", "/") / artifact / version / f"{artifact}-{version}.pom"


def read_pom(path, visited=None):
    """追溯父POM许可证, 同时保留实际证据文件."""
    visited = set() if visited is None else visited
    if path in visited or not path.is_file():
        return [], []
    visited.add(path)
    try:
        doc = ET.parse(path).getroot()
    except ET.ParseError:
        return [], []
    for node in doc.iter():
        node.tag = node.tag.rsplit("}", 1)[-1]
    properties = {item.tag: item.text or "" for item in doc.findall("properties/*")}
    properties["project.version"] = doc.findtext("version", path.parent.name)
    licenses = [{"name": item.findtext("name", ""),
                 "url": item.findtext("url", "")}
                for item in doc.findall("licenses/license")]
    evidence = [path.relative_to(ROOT).as_posix()]
    if not licenses:
        parent = doc.find("parent")
        if parent is not None:
            values = [parent.findtext(key, "")
                      for key in ["groupId", "artifactId", "version"]]
            for index, value in enumerate(values):
                for key, replacement in properties.items():
                    value = value.replace("${" + key + "}", replacement)
                values[index] = value
            inherited, ancestors = read_pom(pom_path(*values), visited)
            licenses.extend(inherited)
            evidence.extend(ancestors)
    return licenses, evidence


def safe_name(name):
    """为证据目录生成不含路径分隔符的名称."""
    return re.sub(r"[^A-Za-z0-9_.-]", "_", name)


def write_evidence(component, filename, data):
    """原样保存许可或来源证据, 不替换第三方署名."""
    target = OUTPUT / "evidence" / safe_name(component) / safe_name(filename)
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(data)
    return target.relative_to(ROOT).as_posix()


def jar_record(name, data, module, jar_index):
    """核对最终嵌套JAR的坐标、许可、哈希和原有NOTICE."""
    record = {"file": name, "modules": [module],
              "sha256": hashlib.sha256(data).hexdigest(), "licenses": [],
              "evidence": [], "sourceUrl": "", "status": "待核验"}
    with zipfile.ZipFile(io.BytesIO(data)) as archive:
        coords = None
        for entry in archive.namelist():
            base = entry.rsplit("/", 1)[-1].upper()
            if base.startswith(("LICENSE", "NOTICE", "COPYING")) and not entry.endswith("/"):
                record["evidence"].append(write_evidence(name, entry, archive.read(entry)))
            if entry.endswith("pom.properties") and entry.startswith("META-INF/maven/"):
                properties = {}
                for line in archive.read(entry).decode("utf-8", "replace").splitlines():
                    if "=" in line and not line.startswith("#"):
                        key, value = line.split("=", 1)
                        properties[key.strip()] = value.strip()
                if all(properties.get(key) for key in ["groupId", "artifactId", "version"]):
                    coords = [properties[key] for key in ["groupId", "artifactId", "version"]]
                    break
        # 坐标扫描结束后仍需提取其他许可条目.
        for entry in archive.namelist():
            base = entry.rsplit("/", 1)[-1].upper()
            if base.startswith(("LICENSE", "NOTICE", "COPYING")) and not entry.endswith("/"):
                evidence = write_evidence(name, entry, archive.read(entry))
                if evidence not in record["evidence"]:
                    record["evidence"].append(evidence)
    # 优先采用与整个JAR哈希一致的仓库坐标, 避免误认shaded包内依赖的坐标.
    candidates = jar_index.get(name, [])
    for candidate in candidates:
        if hashlib.sha256(candidate.read_bytes()).hexdigest() == record["sha256"]:
            relative = candidate.relative_to(M2).parts
            coords = [".".join(relative[:-3]), relative[-3], relative[-2]]
            break
    if coords:
        record["coordinates"] = ":".join(coords)
        pom = pom_path(*coords)
        record["licenses"], ancestors = read_pom(pom)
        for ancestor in ancestors:
            path = ROOT / ancestor
            record["evidence"].append(write_evidence(name, path.name, path.read_bytes()))
        group, artifact, version = coords
        prefix = "https://repo.maven.apache.org/maven2/" + group.replace(".", "/")
        record["sourceUrl"] = f"{prefix}/{artifact}/{version}/{artifact}-{version}-sources.jar"
        names = " ".join(item["name"] for item in record["licenses"])
        if record["licenses"]:
            record["status"] = "已取得许可声明, 按交付义务处理"
        if re.search(r"LGPL|CDDL|EPL|MPL|GNU|GPL", names, re.IGNORECASE):
            record["requiresLicenseChoiceOrSource"] = True
    if not record["licenses"]:
        for evidence in record["evidence"]:
            if "LICENSE" in Path(evidence).name.upper():
                text = (ROOT / evidence).read_text(encoding="utf-8", errors="replace")
                if "Apache License" in text and "Version 2.0" in text:
                    record["licenses"] = [{"name": "Apache-2.0", "evidence": evidence}]
                    record["status"] = "已取得JAR内许可原文, 按交付义务处理"
                    break
                if "Apache Software License" in text and "Version 1.1" in text:
                    record["licenses"] = [{"name": "Apache-1.1", "evidence": evidence}]
                    record["status"] = "已取得JAR内许可原文, 按交付义务处理"
                    break
    if name == "dom4j-1.6.1.jar":
        source = ROOT / ".tools/copyright/dom4j-1.6.1-sources.jar"
        if source.is_file():
            with zipfile.ZipFile(source) as archive:
                text = archive.read("org/dom4j/Document.java").decode("utf-8").replace("\r", "")
            notice = text[text.rfind("/*"):]
            evidence = write_evidence(name, "DOM4J-LICENSE.txt", notice.encode("utf-8"))
            record["evidence"].append(evidence)
            record["licenses"] = [{"name": "DOM4J permissive license", "evidence": evidence}]
            record["status"] = "已核对同版本官方源码尾部许可"
    if name == "cos_api-5.6.169.jar":
        source = ROOT / ".tools/copyright/cos_api-5.6.169-sources.jar"
        if source.is_file():
            headers = set()
            with zipfile.ZipFile(source) as archive:
                for entry in archive.namelist():
                    if entry.endswith(".java"):
                        text = archive.read(entry).decode("utf-8", "replace")
                        if text.lstrip().startswith("/*"):
                            headers.add(text[:text.index("*/") + 2])
            evidence = write_evidence(name, "SOURCE-COPYRIGHT-NOTICES.txt",
                                      "\n\n".join(sorted(headers)).encode("utf-8"))
            record["evidence"].append(evidence)
            record["licenses"] = [{"name": "Apache-2.0", "evidence": evidence}]
            record["status"] = "已核对同版本官方源码Apache2许可头"
    if name.startswith("platform-"):
        record["status"] = "项目模块, 见底座LICENSE及源码头"
    return record


def backend_records():
    """仅枚举本次打包的admin/API运行依赖, 不混入测试JAR."""
    jar_index = {}
    for jar in M2.rglob("*.jar"):
        jar_index.setdefault(jar.name, []).append(jar)
    records = {}
    for module in ["platform-admin", "platform-api"]:
        jar = ROOT / "vendor/platform-wxshop" / module / "target" / (module + ".jar")
        with zipfile.ZipFile(jar) as archive:
            entries = [entry for entry in archive.namelist() if
                       entry.startswith("BOOT-INF/lib/") and entry.endswith(".jar")]
            for index, entry in enumerate(entries, 1):
                name = entry.rsplit("/", 1)[-1]
                data = archive.read(entry)
                key = name + ":" + hashlib.sha256(data).hexdigest()
                if key in records:
                    records[key]["modules"].append(module)
                else:
                    records[key] = jar_record(name, data, module, jar_index)
                if index % 60 == 0 or index == len(entries):
                    print(f"{module}运行许可盘点: {index}/{len(entries)}", flush=True)
    return list(records.values())


def frontend_records():
    """依据webpack实际模块追溯npm包, 排除未入包开发工具."""
    stats = ROOT / ".tools/copyright/webpack-stats.json"
    if not stats.is_file():
        raise FileNotFoundError("请先运行tools/build-copyright-ui.cjs")
    data = json.loads(stats.read_text(encoding="utf-8"))
    modules = list(data.get("modules", []))
    folders = set()
    while modules:
        module = modules.pop()
        modules.extend(module.get("modules", []))
        identifier = module.get("identifier", "").split("!")[-1].split("?")[0]
        if "node_modules" not in identifier:
            continue
        file = Path(identifier)
        if not file.is_absolute():
            file = UI / file
        if not file.is_file():
            continue
        for parent in file.parents:
            if (parent / "package.json").is_file():
                folders.add(parent)
                break
    records = []
    for folder in sorted(folders):
        metadata = folder / "package.json"
        if not metadata.is_file():
            continue
        package = json.loads(metadata.read_text(encoding="utf-8"))
        component = package["name"] + "-" + package["version"]
        evidence = [write_evidence(component, "package.json", metadata.read_bytes())]
        for path in folder.iterdir():
            if path.is_file() and path.name.upper().startswith(("LICENSE", "LICENCE", "NOTICE", "COPYING")):
                evidence.append(write_evidence(component, path.name, path.read_bytes()))
        records.append({"name": package["name"], "version": package["version"],
                        "license": package.get("license", package.get("licenses", "待核验")),
                        "evidence": evidence,
                        "source": package.get("repository", package.get("homepage", ""))})
    vendored = UI / "src/third-party/dompurify"
    package = json.loads((vendored / "package.json").read_text(encoding="utf-8"))
    records.append({"name": "dompurify", "version": package["version"],
                    "license": "Apache-2.0 (从双许可中选择)",
                    "evidence": [write_evidence("dompurify-" + package["version"],
                                                "LICENSE", (vendored / "LICENSE").read_bytes())],
                    "source": "https://registry.npmjs.org/dompurify/" + package["version"]})
    return records


def main():
    """输出版本化发布许可索引和仍需决策的清单."""
    OUTPUT.mkdir(parents=True, exist_ok=True)
    data = {"date": "2026-10-14", "backend": backend_records(),
            "frontend": frontend_records()}
    data["unresolvedBackend"] = [entry["file"] for entry in data["backend"]
                                 if entry["status"] == "待核验"]
    (OUTPUT / "runtime-components.json").write_text(
        json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"backend": len(data["backend"]), "frontend": len(data["frontend"]),
                      "unresolved": data["unresolvedBackend"]}, ensure_ascii=False))


if __name__ == "__main__":
    main()
