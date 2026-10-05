"""Draws where a TIP's spill first touches the floor, from the simulator, on the Visualizer's BIOBUZZ field
(sim-review/spill-window.html; open it in a browser, or screenshot it).

    ./gradlew :TeamCode:testDebugUnitTest --tests '*SpillLandingTest*'   # writes build/sim-logs/spill-first-touch.csv
    python3 tools/spill-window/draw.py [csv] [out.html]

The background is the Visualizer's field image (public/fields/biobuzz.webp, in the Visualizer checkout that
autogen.py uses: AUTO_BUILDER_DIR, or ../visualizer). The Visualizer stretches it over the whole field,
0-141.5 in on both axes, so it is drawn here the same way: Pedro frame, origin at the corner where the red
alliance wall (x = 0, left) meets the audience wall (y = 0, bottom), inches. It shows the HIVE as it
starts the match.

The CSV has one row per spilled piece: how far from the audience wall and at what x it first touched the
tiles, with no robot in the way (SpillLandingTest.landings), from the red HIVE's audience CELL.
"""
import base64
import os
import sys

REPO = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
CSV = sys.argv[1] if len(sys.argv) > 1 else os.path.join(REPO, "TeamCode/build/sim-logs/spill-first-touch.csv")
OUT = sys.argv[2] if len(sys.argv) > 2 else os.path.join(REPO, "sim-review/spill-window.html")
VIS = [os.environ.get("AUTO_BUILDER_DIR", ""), os.path.join(REPO, "..", "visualizer"),
       os.path.join(REPO, "..", "mona-shores-ftc-robotics", "visualizer")]
FIELD_IMAGE = next((p for p in (os.path.join(v, "public/fields/biobuzz.webp") for v in VIS if v) if os.path.isfile(p)), None)
if FIELD_IMAGE is None:
    sys.exit("No Visualizer checkout with public/fields/biobuzz.webp: set AUTO_BUILDER_DIR")

FIELD = 141.5          # FieldFrame.FIELD_SIZE_INCHES, wall face to wall face
CENTRE = FIELD / 2     # FieldFrame.FIELD_CENTRE_INCHES
TILE = FIELD / 6
# Our robot: 18 in square on the red CELL's axis (FieldSim.RED_HIVE_X_IN), backed against the audience
# wall, facing the HIVE, side walls slid 6 in forward (RobotAssets.WALL_SLIDE_IN).
ROBOT_X, HALF, SLIDE = CENTRE - 12.75, 9.0, 6.0
ROBOT_Y = HALF

rows = [list(map(float, l.split(","))) for l in open(CSV) if l.strip() and not l.startswith("#")]
pts = [(r[1], r[0]) for r in rows]  # (x, y)
q = lambda v, p: sorted(v)[min(len(v) - 1, int(p * len(v)))]
ys, xs = [p[1] for p in pts], [p[0] for p in pts]
y5, y50, y95, x5, x95 = q(ys, .05), q(ys, .5), q(ys, .95), q(xs, .05), q(xs, .95)

Y1, K = 100.0, 7.0          # show y 0-100: our end, the HIVE and the field centre
ML, MB, MT, MR = 62, 50, 10, 16
W, H = FIELD * K, Y1 * K
px = lambda x: ML + x * K
py = lambda y: MT + H - y * K
img = base64.b64encode(open(FIELD_IMAGE, "rb").read()).decode()


def label(x, y, lines, anchor="start", colour="#fff", size=12):
    """Text on a dark backing, so it reads over the field image."""
    w = max(len(s) for s in lines) * size * 0.56 + 10
    h = len(lines) * (size + 3) + 6
    left = x - (w if anchor == "end" else 0)
    out = [f'<rect x="{left:.1f}" y="{y - size - 2:.1f}" width="{w:.1f}" height="{h}" rx="3" fill="#000" fill-opacity="0.62"/>']
    tx = x - 5 if anchor == "end" else x + 5
    for i, s in enumerate(lines):
        out.append(f'<text x="{tx:.1f}" y="{y + i * (size + 3):.1f}" font-size="{size}" fill="{colour}" text-anchor="{anchor}">{s}</text>')
    return out


o = [f'<svg width="{W + ML + MR:.0f}" height="{H + MT + MB:.0f}" xmlns="http://www.w3.org/2000/svg" '
     'xmlns:xlink="http://www.w3.org/1999/xlink" font-family="Helvetica,Arial,sans-serif">',
     f'<clipPath id="field"><rect x="{px(0)}" y="{py(Y1)}" width="{W}" height="{H}"/></clipPath>',
     f'<image x="{px(0)}" y="{py(FIELD)}" width="{W}" height="{FIELD * K}" preserveAspectRatio="none" clip-path="url(#field)" '
     f'href="data:image/webp;base64,{img}"/>']
# Axis ticks outside the image: tile seams and the field centre, Pedro inches.
for k in range(7):
    d = k * TILE
    o.append(f'<text x="{px(d):.1f}" y="{py(0) + 16}" font-size="11" fill="#555" text-anchor="middle">{d:.2f}</text>')
    if d <= Y1:
        o.append(f'<text x="{px(0) - 6}" y="{py(d) + 4:.1f}" font-size="11" fill="#555" text-anchor="end">{d:.2f}</text>')
o.append(f'<text x="{px(FIELD)}" y="{py(0) + 34}" font-size="11" fill="#555" text-anchor="end">'
         f'Pedro inches: the field is 141.5 wall to wall, centre 70.75; tile seams every 141.5 / 6 = {TILE:.2f}</text>')
o.append(f'<text x="{px(0) - 6}" y="{py(0) + 34}" font-size="11" fill="#555" text-anchor="end">(0, 0)</text>')
o += [f'<line x1="{px(CENTRE)}" x2="{px(CENTRE)}" y1="{py(0)}" y2="{py(Y1)}" stroke="#7fd39b" stroke-opacity="0.7" stroke-dasharray="8 6"/>',
      f'<line x1="{px(0)}" x2="{px(FIELD)}" y1="{py(CENTRE)}" y2="{py(CENTRE)}" stroke="#7fd39b" stroke-opacity="0.7" stroke-dasharray="8 6"/>']
o += label(px(CENTRE) + 4, py(Y1) + 18, ["x = 70.75, field centre"], colour="#9fe3b5", size=11)
o += label(px(FIELD) - 4, py(CENTRE) - 8, ["y = 70.75, field centre (the HIVE axle)"], anchor="end", colour="#9fe3b5", size=11)
# The spill: each piece's first touch, and the box 90% of them fall in.
for x, y in pts:
    if 0 < x < FIELD and 0 < y < Y1:
        o.append(f'<circle cx="{px(x):.1f}" cy="{py(y):.1f}" r="1.8" fill="#ffb347" opacity="0.5"/>')
o.append(f'<rect x="{px(x5):.1f}" y="{py(y95):.1f}" width="{(x95 - x5) * K:.1f}" height="{(y95 - y5) * K:.1f}" '
         'fill="none" stroke="#ff3b3b" stroke-width="2.5"/>')
o += label(px(x5) - 10, py(y95) + 14, ["90% of first touches (simulated)", f"y {y5:.1f}–{y95:.1f}, x {x5:.1f}–{x95:.1f}",
                                         f"{x95 - x5:.1f} in wide, median y {y50:.1f}"], anchor="end", colour="#ff8a8a")
# Our robot, backed against the audience wall.
o += [f'<rect x="{px(ROBOT_X - HALF)}" y="{py(ROBOT_Y + HALF)}" width="{2 * HALF * K}" height="{2 * HALF * K}" fill="#c9ccd3" stroke="#fff" stroke-width="1.2"/>',
      f'<rect x="{px(ROBOT_X - HALF + 1)}" y="{py(ROBOT_Y + HALF)}" width="{(2 * HALF - 2) * K}" height="{1.5 * K}" fill="#f0a020"/>',
      f'<circle cx="{px(ROBOT_X)}" cy="{py(ROBOT_Y)}" r="3" fill="#222"/>']
for wx in (ROBOT_X - HALF, ROBOT_X + HALF - 0.5):
    o.append(f'<rect x="{px(wx)}" y="{py(ROBOT_Y + HALF + SLIDE)}" width="{0.5 * K}" height="{2 * HALF * K}" fill="#5aa0ff"/>')
o += label(px(ROBOT_X + HALF) + 10, py(16), [f"robot centre ({ROBOT_X:.2f}, {ROBOT_Y:.2f}), heading 90°",
                                              f"back on the audience wall, x {ROBOT_X - HALF:.2f}–{ROBOT_X + HALF:.2f}",
                                              f"front face y {2 * HALF:.0f}, arm tips y {2 * HALF + SLIDE:.0f}"])
o += label(px(1), py(0) - 8, ["audience wall (y = 0)"], size=11)
o += label(px(1), py(60), ["red alliance", "wall (x = 0)"], colour="#ff8a8a", size=11)
o.append('</svg>')

html = ('<!doctype html><html><head><meta charset="utf-8"><title>Spill landing window</title></head>'
        '<body style="margin:0;background:#fff;padding:12px;font-family:Helvetica,Arial">'
        f'<div style="font-size:16px;font-weight:bold">Where a red TIP\'s spill first touches the floor: {len(pts)} pieces, '
        f'{len(pts) // 6} simulated TIPs, no robot in the way</div>'
        f'<div style="font-size:12px;color:#555;margin:2px 0 6px;max-width:1000px">On the Visualizer\'s field (the HIVE as it '
        'starts the match). Each dot is one piece\'s first touch in the simulator, spilling from the red audience-side CELL; '
        f'the red box holds 90% of them. The simulator lands a little short of the 3 Oct films (median {y50:.0f} in against about 48).</div>'
        + "".join(o) + '</body></html>')
open(OUT, "w").write(html)
print(f"wrote {OUT} on {FIELD_IMAGE}: {len(pts)} first touches, 90% y {y5:.1f}-{y95:.1f}, x {x5:.1f}-{x95:.1f}, median {y50:.1f}")
