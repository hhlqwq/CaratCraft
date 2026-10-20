"""按实际构建图同步前端许可, 仅为本轮固定版本原生Vue组件补充同包MIT原文."""

import argparse
import importlib.util
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
INDEX = ROOT / "vendor/platform-wxshop/legal/generated/runtime-components.json"
NATIVE_VUE = {"vue", "@vue/reactivity", "@vue/runtime-core", "@vue/runtime-dom"}


def main():
    """保留既有准入选择及复制样式声明, 未经本轮核对的新依赖直接拒绝."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--date", required=True)
    args = parser.parse_args()
    from datetime import datetime
    day = datetime.strptime(args.date, "%Y-%m-%d").strftime("%Y-%m-%d")
    spec = importlib.util.spec_from_file_location("license_inventory", ROOT / "tools/build-license-bundle.py")
    inventory = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(inventory)
    data = json.loads(INDEX.read_text("utf-8"))
    approved = {(row["name"], row["version"]): row for row in data["frontend"]}
    selected = []
    for row in inventory.frontend_records():
        key = (row["name"], row["version"])
        if key in approved:
            selected.append(approved[key])
            continue
        if row["name"] not in NATIVE_VUE or row["version"] != "3.5.44" or row["license"] != "MIT":
            raise ValueError("前端组件缺少本轮核对记录: " + str(key))
        licenses = [ROOT / file for file in row["evidence"] if Path(file).name == "LICENSE"]
        if len(licenses) != 1:
            raise ValueError("原始包缺少唯一LICENSE: " + str(key))
        text = licenses[0].read_text("utf-8")
        if "MIT License" not in text or "Permission is hereby granted" not in text or "AS IS" not in text:
            raise ValueError("原始MIT文本不完整: " + str(key))
        row["selectedLicense"] = "MIT"
        row["evidenceMethod"] = "锁定版本原始npm包内的元数据和完整LICENSE, 保留原权利人声明"
        selected.append(row)
    # 此样式复制在本仓库SCSS中, 构建图没有外部npm节点, 仍须随包保留许可.
    if ("normalize.css", "7.0.0") in approved:
        selected.append(approved[("normalize.css", "7.0.0")])
    data["frontend"] = selected
    data["date"] = day
    INDEX.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", "utf-8", newline="\n")
    print(f"前端索引已同步实际构建及复制样式: {len(selected)}项, 后端及既有源码包记录保持不变")


if __name__ == "__main__":
    main()
