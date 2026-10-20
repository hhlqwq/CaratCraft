"""从已核验应用包恢复同版本第三方依赖缓存, 不修改运行容器或批准新依赖."""

import argparse
import hashlib
import json
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parents[1]


def main():
    """只复制许可索引哈希一致的库, 不覆盖已有缓存或正在下载的文件."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("jars", nargs="+", type=Path)
    args = parser.parse_args()
    records = json.loads((ROOT / "vendor/platform-wxshop/legal/generated/runtime-components.json")
                         .read_text("utf-8"))["backend"]
    approved = {row["file"]: row for row in records}
    repository = ROOT / ".tools/m2/repository"
    copied = 0
    for application in args.jars:
        with zipfile.ZipFile(application) as archive:
            for entry in archive.namelist():
                if not entry.startswith("BOOT-INF/lib/") or not entry.endswith(".jar"):
                    continue
                name = entry.rsplit("/", 1)[-1]
                record = approved.get(name)
                if (not record or not record.get("coordinates")
                        or record["coordinates"].startswith("com.platform:")):
                    continue
                content = archive.read(entry)
                if hashlib.sha256(content).hexdigest() != record["sha256"]:
                    raise ValueError("已有应用依赖与许可索引不符: " + name)
                group, artifact, version = record["coordinates"].split(":")
                destination = repository / group.replace(".", "/") / artifact / version / name
                if destination.exists() or list(destination.parent.glob(name + ".part*")):
                    continue
                destination.parent.mkdir(parents=True, exist_ok=True)
                destination.write_bytes(content)
                copied += 1
    print(f"已按索引校验并补充{copied}个第三方JAR, 未覆盖已有缓存或更新依赖版本")


if __name__ == "__main__":
    main()
