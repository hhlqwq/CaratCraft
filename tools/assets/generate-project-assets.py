"""从几何图形重建项目演示素材, 不读取或加工第三方图片."""

import hashlib
import json
import math
from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[2]
WX_IMAGES = ROOT / "apps/miniapp/static/images"
UI = ROOT / "apps/admin-ui"
GOLD = "#b47a3f"
INK = "#34524c"


def draw_icon(draw, kind, size, color):
    """在方形画布中使用线段、圆和多边形绘制通用图标."""
    unit = min(size) / 24
    offset = ((size[0] - 24 * unit) / 2, (size[1] - 24 * unit) / 2)

    def points(values):
        """将24单位坐标转换到实际画布坐标."""
        return [(x * unit + offset[0], y * unit + offset[1]) for x, y in values]

    def line(values):
        """绘制等宽折线."""
        draw.line(points(values), fill=color, width=max(1, round(unit * 1.5)), joint="curve")

    def ellipse(bounds):
        """绘制图标圆形轮廓."""
        first, last = points([(bounds[0], bounds[1]), (bounds[2], bounds[3])])
        draw.ellipse((*first, *last), outline=color, width=max(1, round(unit * 1.5)))

    if kind == "user":
        ellipse((8, 3, 16, 11))
        line([(4, 21), (5, 16), (9, 13), (15, 13), (19, 16), (20, 21)])
    elif kind == "home":
        line([(3, 10), (12, 3), (21, 10), (19, 10), (19, 21), (14, 21),
              (14, 15), (10, 15), (10, 21), (5, 21), (5, 10)])
    elif kind == "cart":
        line([(2, 4), (5, 4), (8, 16), (18, 16), (21, 7), (6, 7)])
        ellipse((8, 19, 10, 21))
        ellipse((17, 19, 19, 21))
    elif kind == "search":
        ellipse((3, 3, 16, 16))
        line([(15, 15), (21, 21)])
    elif kind in {"close", "delete"}:
        line([(5, 5), (19, 19)])
        line([(19, 5), (5, 19)])
    elif kind in {"left", "right", "up", "down"}:
        arrows = {"left": [(15, 4), (7, 12), (15, 20)],
                  "right": [(8, 4), (16, 12), (8, 20)],
                  "up": [(4, 15), (12, 7), (20, 15)],
                  "down": [(4, 8), (12, 16), (20, 8)]}
        line(arrows[kind])
    elif kind == "checked":
        ellipse((3, 3, 21, 21))
        line([(7, 12), (10, 15), (17, 8)])
    elif kind == "radio":
        ellipse((3, 3, 21, 21))
    elif kind == "edit":
        line([(5, 19), (6, 14), (16, 4), (20, 8), (10, 18), (5, 19)])
        line([(5, 22), (21, 22)])
    elif kind == "document":
        line([(5, 3), (15, 3), (19, 7), (19, 21), (5, 21), (5, 3)])
        line([(8, 10), (16, 10)])
        line([(8, 14), (16, 14)])
        line([(8, 18), (13, 18)])
    elif kind == "heart":
        line([(12, 21), (3, 11), (3, 6), (7, 3), (12, 7), (17, 3),
              (21, 6), (21, 11), (12, 21)])
    elif kind == "chat":
        line([(3, 4), (21, 4), (21, 17), (10, 17), (5, 21), (5, 17),
              (3, 17), (3, 4)])
        line([(7, 9), (17, 9)])
        line([(7, 13), (13, 13)])
    else:
        line([(3, 8), (7, 3), (17, 3), (21, 8), (12, 21), (3, 8), (21, 8)])
        line([(7, 3), (12, 21), (17, 3)])


def icon_kind(name):
    """按资源用途选择通用图形, 不复用原图的路径或像素."""
    groups = [("settings", ["xitongpeizhi", "settings", "config"]),
              ("menu", ["zhedie", "menu", "daohang"]),
              ("chart", ["charts", "tongji"]), ("box", ["stock", "goods", "store"]),
              ("lock", ["lock", "password", "role"]),
              ("search", ["search"]), ("home", ["choice", "home", "shouye"]),
              ("cart", ["shoping", "cart"]), ("user", ["avatar", "me_", "geren", "admin", "user"]),
              ("left", ["back", "prev"]), ("right", ["right", "more", "next", "go."]),
              ("up", ["up"]), ("down", ["down"]),
              ("close", ["clear", "error"]), ("delete", ["del-", "shanchu"]),
              ("checked", ["checked", "wxpay"]), ("radio", ["radio", "checkbox"]),
              ("edit", ["edit"]), ("heart", ["collect"]),
              ("chat", ["comment", "kefu", "feedback", "wechat", "wx-"]),
              ("document", ["empty", "policy", "coupon", "sort", "order"])]
    for kind, keywords in groups:
        if any(keyword in name.lower() for keyword in keywords):
            return kind
    return "diamond"


def draw_jewelry(image, name, layer=False):
    """绘制明确的珠宝示意图, 透明配件层保留原有画布契约."""
    draw = ImageDraw.Draw(image)
    width, height = image.size
    metal = "#c9a256" if "gold" in name and "white" not in name else "#afb9c0"
    if "rose" in name:
        metal = "#cd9890"
    if "chain" in name or not layer:
        bounds = (width * .16, height * .03, width * .84, height * .76)
        draw.arc(bounds, 0, 180, fill=metal, width=max(2, round(width * .014)))
        draw.arc((bounds[0] + 4, bounds[1], bounds[2] - 4, bounds[3] - 4),
                 0, 180, fill="#ebe1ce", width=max(1, round(width * .003)))
    if "pearl" in name or (not layer and "platinum" in name):
        color = "#e9e4da"
        if "black" in name or "tahiti" in name:
            color = "#61746e"
        elif "gold" in name:
            color = "#d8b766"
        elif "purple" in name:
            color = "#bea6bb"
        if layer:
            bounds = (width * .1, height * .1, width * .9, height * .9)
        else:
            bounds = (width * .43, height * .69, width * .57, height * .83)
        draw.ellipse(bounds, fill=color, outline="#9b9b92", width=max(1, round(width * .004)))
        x, y, right, bottom = bounds
        draw.ellipse((x + (right - x) * .18, y + (bottom - y) * .15,
                      x + (right - x) * .38, y + (bottom - y) * .35), fill="#faf8f2")
    elif "gem" in name or not layer:
        center = (width * .5, height * (.5 if layer else .76))
        radius = min(width, height) * (.35 if layer else .09)
        points = [(center[0] + radius * math.cos(math.pi / 3 * i),
                   center[1] + radius * math.sin(math.pi / 3 * i)) for i in range(6)]
        color = "#4d77a7" if "sapphire" in name else "#dbe7ed"
        if "rose" in name:
            color = "#c38d9a"
        draw.polygon(points, fill=color, outline=metal, width=max(2, round(width * .005)))
        for point in points:
            draw.line([center, point], fill="#f8f6f0", width=max(1, round(width * .003)))
    if not layer:
        # 使用几何字形绘制DEMO标记, 不加载第三方字体.
        letters = [([(0, 0), (0, 1), (.5, 1), (.8, .8), (.8, .2), (.5, 0), (0, 0)]),
                   ([(.8, 0), (0, 0), (0, 1), (.8, 1), (0, 1), (0, .5), (.6, .5)]),
                   ([(0, 1), (0, 0), (.4, .5), (.8, 0), (.8, 1)]),
                   ([(.2, 0), (.6, 0), (.8, .2), (.8, .8), (.6, 1),
                     (.2, 1), (0, .8), (0, .2), (.2, 0)])]
        unit = width * .035
        for index, glyph in enumerate(letters):
            draw.line([(width * .41 + (x + index * 1.3) * unit,
                        height * .91 + y * unit) for x, y in glyph],
                      fill=INK, width=max(1, round(unit * .1)))


def write_bitmap(path, size):
    """创建新的位图, 按既有尺寸输出以保持页面引用兼容."""
    scale = 2
    canvas_size = tuple(value * scale for value in size)
    is_layer = "layers" in path.parts
    is_demo = "demo" in path.parts
    background = (250, 247, 241, 255) if is_demo else (0, 0, 0, 0)
    image = Image.new("RGBA", canvas_size, background)
    draw = ImageDraw.Draw(image)
    if is_layer or is_demo:
        draw_jewelry(image, path.name, is_layer)
    elif path.name in {"logo.png", "favicon.ico"}:
        draw.rounded_rectangle((0, 0, *canvas_size), radius=min(canvas_size) * .2, fill=INK)
        draw_icon(draw, "diamond", canvas_size, "#d7b574")
    elif "bg_mobile_head" in path.name:
        draw.rectangle((0, 0, *canvas_size), fill="#34524c")
        draw.line((20, 30, 55, 30), fill="#ffffff", width=4)
    elif "bg_mobile_foot" in path.name:
        draw.rectangle((0, 0, *canvas_size), fill="#f5f5f5")
        draw.line((20, 20, 50, 20), fill="#777777", width=3)
    else:
        color = GOLD if "pressed" in path.name or "checked" in path.name else "#69756e"
        draw_icon(draw, icon_kind(path.name), canvas_size, color)
    image = image.resize(size, Image.Resampling.LANCZOS)
    if path.suffix.lower() in {".jpg", ".jpeg"}:
        image = image.convert("RGB")
    if path.suffix.lower() == ".webp":
        image.save(path, lossless=True, method=6)
    elif path.suffix.lower() == ".ico":
        image.save(path, sizes=[(16, 16), (32, 32), (48, 48)])
    else:
        image.save(path)


def write_svg(path):
    """重绘SVG资源, 使用项目内的几何线条而非复制旧路径."""
    kind = icon_kind(path.name)
    paths = {
        "home": '<path d="m3 11 9-8 9 8M5 10v11h5v-7h4v7h5V10"/>',
        "settings": '<circle cx="12" cy="12" r="7"/><circle cx="12" cy="12" r="2.5"/><path d="M12 2v3M12 19v3M2 12h3M19 12h3M5 5l2 2M17 17l2 2M5 19l2-2M17 7l2-2"/>',
        "menu": '<path d="M3 5h18M3 12h18M3 19h18M8 5v14"/>',
        "chart": '<path d="M3 3v18h18M7 16v-5M12 16V6M17 16V9"/>',
        "box": '<path d="m3 7 9-4 9 4v13H3zM3 7h18M12 3v17M8 11h2M15 11h2"/>',
        "lock": '<rect x="4" y="10" width="16" height="11" rx="2"/><path d="M7 10V7a5 5 0 0 1 10 0v3M12 14v3"/>',
        "left": '<path d="m15 4-8 8 8 8"/>',
        "right": '<path d="m9 4 8 8-8 8"/>',
        "up": '<path d="m4 15 8-8 8 8"/>',
        "down": '<path d="m4 9 8 8 8-8"/>',
        "close": '<path d="m5 5 14 14M19 5 5 19"/>',
        "delete": '<path d="M3 6h18M8 6V3h8v3M5 6l1 15h12l1-15M10 10v7M14 10v7"/>',
        "checked": '<circle cx="12" cy="12" r="9"/><path d="m7 12 3 3 7-7"/>',
        "radio": '<circle cx="12" cy="12" r="9"/>',
        "user": '<circle cx="12" cy="7" r="4"/><path d="M4 21v-3l4-4h8l4 4v3"/>',
        "cart": '<path d="M2 4h3l3 12h10l3-9H6"/><circle cx="9" cy="20" r="1"/><circle cx="18" cy="20" r="1"/>',
        "search": '<circle cx="10" cy="10" r="7"/><path d="m15 15 7 7"/>',
        "document": '<path d="M5 3h10l4 4v14H5zM8 10h8M8 14h8M8 18h5"/>',
        "heart": '<path d="m12 21-9-10V6l4-3 5 4 5-4 4 3v5z"/>',
        "edit": '<path d="m5 19 1-5L16 4l4 4-10 10zM5 22h16"/>',
        "chat": '<path d="M3 4h18v13H10l-5 4v-4H3zM7 9h10M7 13h6"/>',
    }
    shape = paths.get(kind, '<path d="m3 8 4-5h10l4 5-9 13zM3 8h18M7 3l5 18 5-18"/>')
    if "chain" in path.name:
        color = "#bc9252" if "gold" in path.name else "#adb7be"
        if "rose" in path.name:
            color = "#c78e86"
        shape = '<path d="M4 3v7c0 12 16 12 16 0V3"/>'
    elif "pearl" in path.name:
        color = "#76887e" if "tahiti" in path.name else "#cdc5ae"
        shape = '<circle cx="12" cy="12" r="9" fill="' + color + '"/><circle cx="9" cy="8" r="2" fill="#faf8f3"/>'
    else:
        color = "currentColor"
    svg = ('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">'
           '<title>项目几何示意图</title><g fill="none" stroke="' + color +
           '" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">' +
           shape + '</g></svg>\n')
    path.write_text(svg, encoding="utf-8", newline="\n")


def main():
    """批量重建项目资产并输出可复核的来源与哈希清单."""
    files = [path for path in WX_IMAGES.rglob("*") if path.suffix.lower() in
             {".png", ".webp", ".svg"}]
    files += list((UI / "src/icons/svg").glob("*.svg"))
    files += [UI / "static/img/favicon.ico",
              ROOT / "backend/platform-api/src/main/resources/favicon.ico",
              ROOT / "backend/platform-admin/src/main/resources/favicon.ico"]
    files += list((UI / "src/assets/img").glob("*"))
    records = []
    for index, path in enumerate(files, 1):
        if path.suffix.lower() == ".svg":
            write_svg(path)
        elif path.suffix.lower() in {".png", ".jpg", ".webp", ".ico"}:
            # 只读取尺寸作为页面契约, 不读取旧图片像素进行派生.
            with Image.open(path) as old:
                size = old.size
            write_bitmap(path, size)
        else:
            continue
        records.append({"file": path.relative_to(ROOT).as_posix(),
                        "sha256": hashlib.sha256(path.read_bytes()).hexdigest(),
                        "source": "tools/assets/generate-project-assets.py中的几何绘图",
                        "purpose": "界面图标或明确标注的演示示意图"})
        if index % 20 == 0 or index == len(files):
            print(f"资产重建进度: {index}/{len(files)}", flush=True)
    output = ROOT / "legal/project-assets.json"
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(records, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
