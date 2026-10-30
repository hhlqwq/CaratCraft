"""使用已安装微信开发者工具的官方WXML/WXSS编译器核验全量源码."""

import argparse
import json
import os
from pathlib import Path
import shutil
import subprocess
import tempfile


ROOT = Path(__file__).resolve().parents[2]
PROJECT = ROOT / "apps/miniapp"


def execute(command, cwd):
    """执行本机已有编译器, 非零退出及官方错误诊断均视为失败."""
    result = subprocess.run(command, cwd=cwd, capture_output=True, text=True,
                            encoding="utf-8", errors="replace")
    if result.returncode != 0:
        raise RuntimeError(result.stdout[-6000:] + result.stderr[-6000:])
    return result


def main():
    """真实编译全部模板和样式, 另记JavaScript语法检查及其验收边界."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--devtools-root", required=True)
    parser.add_argument("--report", type=Path)
    args = parser.parse_args()
    compiler = Path(args.devtools_root) / "resources/app.asar.unpacked/node_modules/wcc-exec"
    for name in ("wcc.exe", "wcsc.exe"):
        if not (compiler / name).is_file():
            raise FileNotFoundError("未找到已安装的官方编译器: " + name)
    node = shutil.which("node")
    if not node:
        raise RuntimeError("JavaScript语法检查需要已有Node, 不自动下载")
    files = {suffix: sorted(path.relative_to(PROJECT).as_posix() for path in PROJECT.rglob("*" + suffix)
                            if not any(part in {"node_modules", "miniprogram_npm"} for part in path.parts))
             for suffix in (".wxml", ".wxss", ".js")}
    app = json.loads((PROJECT / "app.json").read_text("utf-8"))
    for route in app["pages"]:
        for suffix in (".js", ".json", ".wxml", ".wxss"):
            if not (PROJECT / (route + suffix)).is_file():
                raise ValueError("页面文件缺失: " + route + suffix)
    temporary = Path(os.environ.get("CARATCRAFT_TEST_TEMP",
                                   "D:/tmp/hailongcodex" if os.name == "nt" else "/tmp/hailongcodex"))
    temporary.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="miniapp-compile-", dir=temporary) as output:
        for suffix, executable in ((".wxml", "wcc.exe"), (".wxss", "wcsc.exe")):
            if not files[suffix]:
                raise ValueError("编译输入为空: " + suffix)
            destination = Path(output) / ("wxml.js" if suffix == ".wxml" else "wxss.js")
            execute([str(compiler / executable), "-o", str(destination), *["./" + name for name in files[suffix]]], PROJECT)
            if not destination.is_file() or not destination.stat().st_size:
                raise RuntimeError("官方编译未生成有效输出: " + suffix)
            print(f"官方{suffix}编译通过: {len(files[suffix])}个文件", flush=True)
        for script in files[".js"]:
            execute([node, "--check", script], PROJECT)
    evidence = {"pages": len(app["pages"]), "wxml": len(files[".wxml"]),
                "wxss": len(files[".wxss"]), "javascriptSyntax": len(files[".js"]),
                "officialWxmlWxssCompilePassed": True,
                "scope": "已有微信开发者工具官方WCC/WCSC编译, JS为Node语法检查; 不替代IDE运行、真机、微信登录或支付验收",
                "temporaryFilesCleaned": True, "uploaded": False}
    if args.report:
        args.report.write_text(json.dumps(evidence, ensure_ascii=False, indent=4) + "\n", encoding="utf-8", newline="\n")
    print(json.dumps(evidence, ensure_ascii=False))


if __name__ == "__main__":
    main()
