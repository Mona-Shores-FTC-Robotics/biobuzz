"""Draws build/sim-logs/snapshots/snapshot.json (SnapshotTest) as one PNG of top-down frames.

    python3 tools/auto-routes/snapshots.py [snapshot.json] [out.png]

Our half of the field, red alliance, Pedro inches: x across (0 = our wall, 70.75 = centre line),
y up the page (right at the bottom). Grey bar: the HIVE frame's foot; red box: LOADING ZONE.
Robots are squares with a thick line across the intake; the number is how many pieces it holds.
Pieces: POLLEN yellow, red NECTAR red, blue NECTAR blue; ringed = in a CELL; pale = in the air.
"""
import json, math, os, sys
from PIL import Image, ImageDraw, ImageFont

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
src = sys.argv[1] if len(sys.argv) > 1 else f"{ROOT}/TeamCode/build/sim-logs/snapshots/snapshot.json"
out = sys.argv[2] if len(sys.argv) > 2 else src.replace(".json", ".png")
d = json.load(open(src))
# Optional window: SNAP_Y0/SNAP_Y1 (inches) and SNAP_PX (pixels per inch) to zoom in on one end.
PX = float(os.environ.get("SNAP_PX", 4))
Y0, Y1 = float(os.environ.get("SNAP_Y0", 0)), float(os.environ.get("SNAP_Y1", 141.5))
X1, HEAD = 76, 26
W, H = int(X1 * PX), int((Y1 - Y0) * PX)
font = ImageFont.load_default()

def px(x): return x * PX
def py(y): return (Y1 - y) * PX

frames = d["frames"]
img = Image.new("RGB", (len(frames) * (W + 8) - 8, H + HEAD), "white")
for i, f in enumerate(frames):
    fr = Image.new("RGB", (W, H), (238, 238, 238))
    g = ImageDraw.Draw(fr, "RGBA")
    for t in range(0, 145, 24):
        g.line([(px(t), 0), (px(t), H)], fill=(215, 215, 215))
        g.line([(0, py(t)), (W, py(t))], fill=(215, 215, 215))
    g.line([(px(70.75), 0), (px(70.75), H)], fill=(120, 120, 120), width=2)
    g.rectangle([px(0), py(117.9), px(11), py(94.3)], fill=(255, 120, 120, 90))
    g.rectangle([px(46.0), py(90.2), px(70.75), py(51.3)], fill=(0, 0, 0, 25))
    g.rectangle([px(45.0), py(90.2), px(47.0), py(51.3)], fill=(80, 80, 80))
    a = f["angle"]
    g.text((px(3), py(73)), "left CELL up" if a > 0.01 else "right CELL up" if a < -0.01 else "tipping", fill="black", font=font)
    for kind, x, y, z, incell in f["pieces"]:
        if x > X1 + 2 or y > Y1 + 2: continue
        r = (1.4 if kind == "POLLEN" else 1.8) * PX
        c = {"POLLEN": (224, 168, 0), "RED_NECTAR": (200, 64, 44), "BLUE_NECTAR": (44, 96, 200)}[kind]
        if z > (1.4 if kind == "POLLEN" else 1.8) + 1: c = tuple(min(255, v + 70) for v in c)
        g.ellipse([px(x) - r, py(y) - r, px(x) + r, py(y) + r], fill=c, outline="black" if incell else None)
    for k, (x, y, h, frame, iw, held) in enumerate(f["robots"]):
        col = [(31, 111, 178), (46, 125, 79)][k % 2]
        half = frame / 2
        def pt(u, v):
            return (px(x + u * math.cos(h) - v * math.sin(h)), py(y + u * math.sin(h) + v * math.cos(h)))
        g.polygon([pt(half, half), pt(half, -half), pt(-half, -half), pt(-half, half)], fill=col + (110,), outline=col)
        g.line([pt(half, iw / 2), pt(half, -iw / 2)], fill=col, width=5)
        g.text((px(x) - 3, py(y) - 6), str(held), fill="white", font=font)
    img.paste(fr, (i * (W + 8), HEAD))
    ImageDraw.Draw(img).text((i * (W + 8) + 4, 6), f"{f['t']:.1f} s", fill="black", font=font)
img.save(out)
print(out)
