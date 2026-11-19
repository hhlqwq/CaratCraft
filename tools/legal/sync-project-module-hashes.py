"""仅同步本仓库构建模块的哈希, 不更新或批准第三方依赖的许可与哈希."""

import hashlib
import json
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parents[2]
BACKEND = ROOT / "backend"
INDEX = ROOT / "legal/generated/runtime-components.json"


def main():
    """校验两个发布应用内的模块均等于本地构建结果, 全部一致后才更新索引."""
    data = json.loads(INDEX.read_text("utf-8"))
    license_file = ROOT / "legal/UPSTREAM-LICENSE"
    notice_file = ROOT / "legal/SOURCE-NOTICES.txt"
    text = license_file.read_text("utf-8")
    if "Apache License" not in text or "Version 2.0" not in text or not notice_file.is_file():
        raise ValueError("项目根许可或源码声明缺失, 不同步哈希")
    changes = []
    for module in ("platform-common", "platform-biz"):
        filename = module + "-2.0.0.jar"
        records = [row for row in data["backend"] if row["file"] == filename]
        if len(records) != 1:
            raise ValueError("项目模块索引缺失或重复: " + filename)
        record = records[0]
        if record.get("coordinates") != "com.platform:" + module + ":2.0.0" or record.get("selectedLicense") != "Apache-2.0":
            raise ValueError("项目坐标或既有许可准入不一致: " + filename)
        local_name = "platform-common.jar" if module == "platform-common" else filename
        expected = hashlib.sha256((BACKEND / module / "target" / local_name).read_bytes()).hexdigest()
        for app in ("platform-admin", "platform-api"):
            with zipfile.ZipFile(BACKEND / app / "target" / (app + ".jar")) as archive:
                actual = hashlib.sha256(archive.read("BOOT-INF/lib/" + filename)).hexdigest()
            if actual != expected:
                raise ValueError("应用内项目模块与本地构建结果不一致: " + app + "/" + filename)
        changes.append((record, module, expected))
    for record, module, checksum in changes:
        record["sha256"] = checksum
        record["status"] = "项目模块, 保留UPSTREAM-LICENSE及源码原有声明"
        record["sourceUrl"] = "https://gitee.com/hhlqwq/carat-craft/tree/master/backend/" + module
        for path in (license_file, notice_file):
            evidence = path.relative_to(ROOT).as_posix()
            if evidence not in record["evidence"]:
                record["evidence"].append(evidence)
    INDEX.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", "utf-8")
    print("已核对并同步2个项目模块, 第三方组件的哈希和许可选择保持原样", flush=True)


if __name__ == "__main__":
    main()
