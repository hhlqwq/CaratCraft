"""保留源码现有版权头的副本, 不删除或改写第三方声明."""

import hashlib
import json
import subprocess
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
LEGAL = ROOT / "vendor/platform-wxshop/legal"


def main():
    """按原始版权头分组记录文件, 供源码和二进制交付随附."""
    files = subprocess.run(["git", "ls-files", "-z"], cwd=ROOT,
                           capture_output=True, check=True).stdout.decode("utf-8").split("\0")
    groups = {}
    for relative in files:
        file = ROOT / relative
        if not relative.startswith("vendor/platform-wxshop/") or not file.is_file():
            continue
        if file.suffix not in {".java", ".js", ".scss", ".css"}:
            continue
        try:
            text = file.read_text(encoding="utf-8")
        except UnicodeError:
            continue
        if not text.lstrip().startswith("/*") or "*/" not in text:
            continue
        notice = text[:text.index("*/") + 2].strip()
        if "Copyright" not in notice and "License" not in notice and "license" not in notice:
            continue
        key = hashlib.sha256(notice.encode("utf-8")).hexdigest()
        group = groups.setdefault(key, {"notice": notice, "files": []})
        group["files"].append(relative)
    output = ["CaratCraft source copyright notices\n",
              "以下为源文件原有声明的原样副本, 不是重新署名或第三方背书.\n"]
    for group in groups.values():
        output.extend(["\nFiles:\n", "\n".join(group["files"]), "\n\n", group["notice"], "\n"])
    (LEGAL / "SOURCE-NOTICES.txt").write_text("".join(output), encoding="utf-8")
    (LEGAL / "source-notice-map.json").write_text(
        json.dumps(list(groups.values()), ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"noticeGroups": len(groups),
                      "sourceFiles": sum(len(group["files"]) for group in groups.values())}))


if __name__ == "__main__":
    main()
