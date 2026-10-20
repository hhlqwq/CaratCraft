"""按Maven失败日志补齐精确版本开发依赖, 每份下载均校验Maven Central原始摘要."""

import argparse
from concurrent.futures import ThreadPoolExecutor
import hashlib
from pathlib import Path
import re
import xml.etree.ElementTree as ET
from urllib.request import build_opener, ProxyHandler, Request
from urllib.error import HTTPError, URLError

ROOT = Path(__file__).resolve().parents[1]
CENTRAL = "https://repo.maven.apache.org/maven2/"
OPENER = build_opener()


def read(relative):
    """对瞬时连接失败最多重试三次, 明确报告缺失文件而不忽略摘要."""
    for attempt in range(3):
        try:
            request = Request(CENTRAL + relative, headers={"User-Agent": "CaratCraft-development-cache"})
            with OPENER.open(request, timeout=15) as response:
                return response.read()
        except HTTPError as error:
            raise ValueError(f"官方仓库返回{error.code}: {relative}") from error
        except (URLError, TimeoutError) as error:
            if attempt == 2:
                raise ValueError("官方仓库连接失败: " + relative) from error


def fetch(relative):
    """只从固定仓库读取明确坐标文件, 校验摘要后写入工作区缓存."""
    destination = ROOT / ".tools/m2/repository" / relative
    if destination.is_file():
        return 0
    content = read(relative)
    checksum = read(relative + ".sha1").decode("ascii").strip().split()[0]
    if not re.fullmatch(r"[a-fA-F0-9]{40}", checksum) or hashlib.sha1(content).hexdigest() != checksum.lower():
        raise ValueError("仓库摘要不符: " + relative)
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_bytes(content)
    return 1


def main():
    """仅解析受限格式的失败坐标, 同时补充对应POM以保留完整传递依赖解析."""
    global OPENER
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("log", type=Path)
    parser.add_argument("--direct", action="store_true", help="本次下载直连官方仓库, 不修改系统代理")
    parser.add_argument("--parents", action="store_true", help="同时补齐已缓存POM显式声明的父POM")
    args = parser.parse_args()
    if args.direct:
        OPENER = build_opener(ProxyHandler({}))
    text = args.log.read_text("utf-8", errors="replace")
    (ROOT / ".tools/copyright/maven-gap-last-failure.log").write_text(text, "utf-8")
    artifacts = set(re.findall(r"\b([A-Za-z0-9_.-]+):([A-Za-z0-9_.-]+):(jar|pom):(?:([A-Za-z0-9_.-]+):)?([A-Za-z0-9_.-]+)\b", text))
    if args.parents:
        namespace = {"m": "http://maven.apache.org/POM/4.0.0"}
        for pom in (ROOT / ".tools/m2/repository").rglob("*.pom"):
            document = ET.parse(pom).getroot()
            properties = document.find("m:properties", namespace)
            variables = {} if properties is None else {item.tag.rsplit("}", 1)[-1]: item.text for item in properties}
            candidates = [document.find("m:parent", namespace)]
            candidates.extend(item for item in document.findall("m:dependencyManagement/m:dependencies/m:dependency", namespace)
                              if item.findtext("m:scope", namespaces=namespace) == "import")
            for candidate in candidates:
                if candidate is None:
                    continue
                values = [candidate.findtext("m:" + key, namespaces=namespace) for key in ("groupId", "artifactId", "version")]
                values = [variables.get(value[2:-1], value) if value and value.startswith("${") and value.endswith("}") else value for value in values]
                if all(value and re.fullmatch(r"[A-Za-z0-9_.-]+", value) for value in values):
                    artifacts.add((values[0], values[1], "pom", "", values[2]))
    files = set()
    for group, artifact, kind, classifier, version in artifacts:
        if group == "com.platform":
            continue
        if any(not item or item in {".", ".."} for item in group.split(".")):
            raise ValueError("依赖坐标无效")
        folder = group.replace(".", "/") + "/" + artifact + "/" + version + "/" + artifact + "-" + version
        files.update({folder + ("-" + classifier if classifier and kind == "jar" else "") + "." + kind,
                      folder + ".pom"})
    with ThreadPoolExecutor(max_workers=4) as executor:
        count = sum(executor.map(fetch, sorted(files)))
    print(f"按原始仓库摘要补齐{count}份精确版本开发依赖, 未升级任何版本")


if __name__ == "__main__":
    main()
