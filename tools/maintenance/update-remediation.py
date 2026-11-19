"""从历史审查清单生成当前整改台账, 不修改历史证据."""

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def main():
    """保留已有整改状态, 为新增审查项建立待处理记录并生成可读表格."""
    source = json.loads((ROOT / "docs/资料/历史问题.json").read_text("utf-8"))
    target = ROOT / "docs/资料/整改台账.json"
    previous = json.loads(target.read_text("utf-8")) if target.exists() else {"items": []}
    states = {item["id"]: item for item in previous["items"]}
    items = []
    for finding in source["items"]:
        item = states.get(finding["id"], {
            "id": finding["id"], "status": "待处理", "changes": "", "validation": ""
        })
        item.update(priority=finding["priority"], title=finding["title"])
        items.append(item)
    counts = {state: sum(item["status"] == state for item in items)
              for state in sorted({item["status"] for item in items})}
    lines = ["状态统计: " + "; ".join(f"{key} {value}项" for key, value in counts.items()), "",
             "| 编号 | 优先级 | 问题 | 历史状态 |", "| --- | --- | --- | --- |"]
    for item in items:
        lines.append(f'| {item["id"]} | {item["priority"]} | {item["title"]} | '
                     f'{item["status"]} |')
    document = ROOT / "docs/项目维护文档.md"
    content = document.read_text("utf-8")
    begin = "<!-- 整改台账:开始 -->"
    end = "<!-- 整改台账:结束 -->"
    if content.count(begin) != 1 or content.count(end) != 1:
        raise ValueError("维护文档缺少唯一的整改台账区块, 不覆盖人工内容")
    before, remainder = content.split(begin)
    _, after = remainder.split(end)
    target.write_text(json.dumps({"baseline": source["baseline"], "items": items},
                                 ensure_ascii=False, indent=4) + "\n", "utf-8")
    document.write_text(before + begin + "\n\n" + "\n".join(lines) + "\n\n" + end + after, "utf-8")
    print(f"整改台账已更新: {counts}")


if __name__ == "__main__":
    main()
