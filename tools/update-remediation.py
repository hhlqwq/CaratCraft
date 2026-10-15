"""从历史审查清单生成当前整改台账, 不修改历史证据."""

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def main():
    """保留已有整改状态, 为新增审查项建立待处理记录并生成可读表格."""
    source = json.loads((ROOT / "docs/audit-2026-10-13-findings.json").read_text("utf-8"))
    target = ROOT / "docs/remediation-status.json"
    previous = json.loads(target.read_text("utf-8")) if target.exists() else {"items": []}
    states = {item["id"]: item for item in previous["items"]}
    items = []
    for finding in source["items"]:
        item = states.get(finding["id"], {
            "id": finding["id"], "status": "待处理", "changes": "", "validation": ""
        })
        item.update(priority=finding["priority"], title=finding["title"])
        items.append(item)
    target.write_text(json.dumps({"baseline": source["baseline"], "items": items},
                                 ensure_ascii=False, indent=2) + "\n", "utf-8")
    counts = {state: sum(item["status"] == state for item in items)
              for state in sorted({item["status"] for item in items})}
    lines = ["# 全仓审查整改台账", "", "历史问题来源: [原审查清单](audit-2026-10-13.md).",
             "原清单保留整改前事实, 当前进度以本台账为准. 只有修改及对应验证完成后才标记已完成.", "",
             "状态统计: " + "; ".join(f"{key} {value}项" for key, value in counts.items()), "",
             "商用交付包仍对应2026-10-14版权整改版本, 后续修复完成后需重新生成, 不将旧包视作最新修复版.", "",
             "| 编号 | 优先级 | 问题 | 状态 | 修改与验证 |", "|---|---|---|---|---|"]
    for item in items:
        detail = "; ".join(value for value in [item["changes"], item["validation"]] if value)
        lines.append(f'| {item["id"]} | {item["priority"]} | {item["title"]} | '
                     f'{item["status"]} | {detail.replace(chr(10), " ").replace("|", "/")} |')
    (ROOT / "docs/remediation-status.md").write_text("\n".join(lines) + "\n", "utf-8")
    print(f"整改台账已更新: {counts}")


if __name__ == "__main__":
    main()
