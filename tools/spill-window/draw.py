"""Draws where a TIP's spill first touches the floor, from the simulator, on a top view of our end of the
field (sim-review/spill-window.html; open it in a browser, or screenshot it).

    ./gradlew :TeamCode:testDebugUnitTest --tests '*SpillLandingTest*'   # writes build/sim-logs/spill-first-touch.csv
    python3 tools/spill-window/draw.py [csv] [out.html]

The CSV has one row per spilled piece: how far from the audience wall and at what x it first touched the
tiles, with no robot in the way (SpillLandingTest.landings), from the red HIVE's audience CELL. Pedro frame:
origin at the corner where the red alliance wall (x = 0, left) meets the audience wall (y = 0, bottom),
inches. Every HIVE number below is FieldSim's, which takes it from HiveGeometry (the manual).
"""
import math
import os
import sys

REPO = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
CSV = sys.argv[1] if len(sys.argv) > 1 else os.path.join(REPO, "TeamCode/build/sim-logs/spill-first-touch.csv")
OUT = sys.argv[2] if len(sys.argv) > 2 else os.path.join(REPO, "sim-review/spill-window.html")

FIELD = 141.5                    # FieldFrame.FIELD_SIZE_INCHES, wall face to wall face
CENTRE = FIELD / 2               # FieldFrame.FIELD_CENTRE_INCHES
TILE = FIELD / 6                 # 6 tiles a side; seams every 23.58 in
# HiveGeometry: frame 49.46 wide, 38.95 deep; foot bars 2 in wide (FieldSim.FOOT_BAR_HALF_WIDTH_IN)
FOOT_X = (CENTRE - 49.46 / 2, CENTRE + 49.46 / 2)
FOOT_Y = (CENTRE - 38.95 / 2, CENTRE + 38.95 / 2)
FOOT_HALF = 1.0
# The rockers: red centred 12.75 in left of centre, blue right (HIVE_CENTER_TO_CENTER 25.5); CELLs 20 in wide,
# back 9.42 in from the axle (CELL_SPACING / 2), 12 in deep, tilted 30°, floor and roof from FieldSim.
RED_X, BLUE_X = CENTRE - 12.75, CENTRE + 12.75
HALF_W, BACK, OPEN = 10.0, 18.84 / 2, 18.84 / 2 + 12.0
TILT = math.radians(30)
FLOOR = (53.5 - 43.95 - OPEN * math.sin(TILT)) / math.cos(TILT)
ROOF = FLOOR + 14.0
PIVOT_Z = 43.95


def cell(end):
    """A settled CELL's footprint in y (Pedro) and where its open end is: end -1 is the audience CELL,
    lowered (the rocker at +30°), end +1 the scoring CELL, raised."""
    c, s = math.cos(TILT), math.sin(TILT)
    pts = [(CENTRE + v * c - w * s, PIVOT_Z + v * s + w * c)
           for v in (end * BACK, end * OPEN) for w in (FLOOR, ROOF)]
    ys = [p[0] for p in pts]
    lip = [CENTRE + end * OPEN * c - w * s for w in (FLOOR, ROOF)]
    return min(ys), max(ys), lip, PIVOT_Z + end * OPEN * s + FLOOR * c


DOWN, UP = cell(-1), cell(1)

# Our robot: 18 in square on the CELL's axis, back against the audience wall, side walls slid 6 in forward.
ROBOT_X, HALF, SLIDE = RED_X, 9.0, 6.0

rows = [list(map(float, l.split(","))) for l in open(CSV) if l.strip() and not l.startswith("#")]
pts = [(r[1], r[0]) for r in rows]  # (x, y)
q = lambda v, p: sorted(v)[min(len(v) - 1, int(p * len(v)))]
ys, xs = [p[1] for p in pts], [p[0] for p in pts]
y5, y50, y95, x5, x95 = q(ys, .05), q(ys, .5), q(ys, .95), q(xs, .05), q(xs, .95)

X0, X1, Y0, Y1, K = 0.0, FIELD, 0.0, 96.0, 7.0
ML, MB, MT, MR = 64, 56, 30, 30
W, H = (X1 - X0) * K, (Y1 - Y0) * K
px = lambda x: ML + (x - X0) * K
py = lambda y: MT + H - (y - Y0) * K
inside = [(x, y) for x, y in pts if X0 < x < X1 and Y0 < y < Y1]

o = [f'<svg width="{W + ML + MR:.0f}" height="{H + MT + MB:.0f}" xmlns="http://www.w3.org/2000/svg" '
     'font-family="Helvetica,Arial,sans-serif">',
     f'<rect x="{px(X0)}" y="{py(Y1)}" width="{W}" height="{H}" fill="#e9e6e1"/>']
# Tile seams, labelled with their distance from the wall.
for k in range(1, 6):
    d = k * TILE
    o.append(f'<line x1="{px(d):.1f}" x2="{px(d):.1f}" y1="{py(Y0)}" y2="{py(Y1)}" stroke="#b9b3aa" stroke-width="1.2"/>')
    o.append(f'<text x="{px(d):.1f}" y="{py(Y0) + 30}" font-size="11" fill="#777" text-anchor="middle">{d:.2f}</text>')
    if d < Y1:
        o.append(f'<line x1="{px(X0)}" x2="{px(X1)}" y1="{py(d):.1f}" y2="{py(d):.1f}" stroke="#b9b3aa" stroke-width="1.2"/>')
        o.append(f'<text x="{px(X0) - 22}" y="{py(d) + 4:.1f}" font-size="11" fill="#777" text-anchor="end">{d:.2f}</text>')
o.append(f'<text x="{px(X1)}" y="{py(Y0) + 46}" font-size="11" fill="#777" text-anchor="end">'
         f'tile seams, in from the red wall / audience wall: 6 tiles of 141.5 / 6 = {TILE:.2f} in</text>')
# Field centre lines.
o += [f'<line x1="{px(CENTRE)}" x2="{px(CENTRE)}" y1="{py(Y0)}" y2="{py(Y1)}" stroke="#2f7d4f" stroke-dasharray="8 5" stroke-width="1.4"/>',
      f'<line x1="{px(X0)}" x2="{px(X1)}" y1="{py(CENTRE)}" y2="{py(CENTRE)}" stroke="#2f7d4f" stroke-dasharray="8 5" stroke-width="1.4"/>',
      f'<text x="{px(CENTRE) + 5}" y="{py(Y1) + 14}" font-size="11" fill="#2f7d4f">x = 70.75 (field centre, Pedro)</text>',
      f'<text x="{px(X1) - 5}" y="{py(CENTRE) - 6}" font-size="11" fill="#2f7d4f" text-anchor="end">y = 70.75 (field centre: the HIVE axle)</text>']
# HIVE frame: the feet you drive between, and the frame's outline overhead.
o += [f'<rect x="{px(FOOT_X[0])}" y="{py(FOOT_Y[1])}" width="{(FOOT_X[1] - FOOT_X[0]) * K}" height="{(FOOT_Y[1] - FOOT_Y[0]) * K}" '
      'fill="none" stroke="#5b4a8b" stroke-dasharray="3 4" stroke-width="1"/>']
for fx in FOOT_X:
    o.append(f'<rect x="{px(fx - FOOT_HALF)}" y="{py(FOOT_Y[1])}" width="{2 * FOOT_HALF * K}" height="{(FOOT_Y[1] - FOOT_Y[0]) * K}" fill="#5b4a8b"/>')
o += [f'<text x="{px(FOOT_X[0]) - 12}" y="{py(80) - 4}" font-size="11" fill="#5b4a8b" text-anchor="end">HIVE foot bar</text>',
      f'<text x="{px(FOOT_X[0]) - 12}" y="{py(80) + 10}" font-size="11" fill="#5b4a8b" text-anchor="end">x {FOOT_X[0]:.1f}, y {FOOT_Y[0]:.1f}–{FOOT_Y[1]:.1f}</text>',
      f'<text x="{px(FOOT_X[1]) + 12}" y="{py(80) - 4}" font-size="11" fill="#5b4a8b">x {FOOT_X[1]:.1f} (frame 49.46 × 38.95,</text>',
      f'<text x="{px(FOOT_X[1]) + 12}" y="{py(80) + 10}" font-size="11" fill="#5b4a8b">open underneath)</text>']
# The red CELLs, looking down: the audience CELL lowered (the one that spills toward us), the scoring
# CELL raised. The blue HIVE is drawn as its whole footprint only.
lo, hi, lip, lipz = DOWN
ulo, uhi, _, _ = UP
o += [f'<rect x="{px(BLUE_X - HALF_W)}" y="{py(uhi):.1f}" width="{2 * HALF_W * K}" height="{(uhi - lo) * K:.1f}" '
      'fill="#3a6fd8" fill-opacity="0.06" stroke="#3a6fd8" stroke-opacity="0.45" stroke-dasharray="5 4"/>',
      f'<rect x="{px(RED_X - HALF_W)}" y="{py(hi):.1f}" width="{2 * HALF_W * K}" height="{(hi - lo) * K:.1f}" '
      'fill="#c0392b" fill-opacity="0.13" stroke="#c0392b" stroke-width="2"/>',
      f'<rect x="{px(RED_X - HALF_W)}" y="{py(lip[0]):.1f}" width="{2 * HALF_W * K}" height="{(lip[0] - lip[1]) * K:.1f}" '
      'fill="#c0392b" fill-opacity="0.35"/>',
      f'<rect x="{px(RED_X - HALF_W)}" y="{py(uhi):.1f}" width="{2 * HALF_W * K}" height="{(uhi - ulo) * K:.1f}" '
      'fill="none" stroke="#c0392b" stroke-opacity="0.7" stroke-dasharray="5 4" stroke-width="1.5"/>']
o += [f'<text x="{px(RED_X)}" y="{py(hi) + 15:.1f}" font-size="11" fill="#c0392b" text-anchor="middle" font-weight="bold">red CELL, down</text>',
      f'<text x="{px(RED_X)}" y="{py(hi) + 28:.1f}" font-size="10" fill="#c0392b" text-anchor="middle">y {lo:.1f}–{hi:.1f}, x {RED_X - HALF_W:.0f}–{RED_X + HALF_W:.0f}</text>',
      f'<text x="{px(RED_X)}" y="{py(lip[0]) - 4:.1f}" font-size="10" fill="#c0392b" text-anchor="middle">open end, lip {lipz:.0f} in up</text>',
      f'<text x="{px(RED_X)}" y="{py(UP[1]) + 14:.1f}" font-size="10" fill="#c0392b" text-anchor="middle">red CELL, up (y {UP[0]:.1f}–{UP[1]:.1f})</text>',
      f'<text x="{px(BLUE_X)}" y="{py(70.75) - 6:.1f}" font-size="10" fill="#3a6fd8" text-anchor="middle">blue HIVE footprint</text>']
# The spill: each piece's first touch, and the box 90% of them fall in.
for x, y in inside:
    o.append(f'<circle cx="{px(x):.1f}" cy="{py(y):.1f}" r="1.7" fill="#d9480f" opacity="0.3"/>')
o += [f'<rect x="{px(x5):.1f}" y="{py(y95):.1f}" width="{(x95 - x5) * K:.1f}" height="{(y95 - y5) * K:.1f}" fill="none" stroke="#d9480f" stroke-width="2.2"/>',
      f'<text x="{px(42):.1f}" y="{py(y95) + 12:.1f}" font-size="11" fill="#d9480f" text-anchor="end">90% of first touches</text>',
      f'<text x="{px(42):.1f}" y="{py(y95) + 25:.1f}" font-size="11" fill="#d9480f" text-anchor="end">y {y5:.1f}–{y95:.1f}, x {x5:.1f}–{x95:.1f}</text>',
      f'<text x="{px(42):.1f}" y="{py(y95) + 38:.1f}" font-size="11" fill="#d9480f" text-anchor="end">median y {y50:.1f}</text>']
# Robot against the audience wall.
o += [f'<rect x="{px(ROBOT_X - HALF)}" y="{py(2 * HALF)}" width="{2 * HALF * K}" height="{2 * HALF * K}" fill="#c9ccd3" stroke="#444" stroke-width="1.2"/>',
      f'<rect x="{px(ROBOT_X - HALF + 1)}" y="{py(2 * HALF)}" width="{(2 * HALF - 2) * K}" height="{1.5 * K}" fill="#f0a020"/>']
for wx in (ROBOT_X - HALF, ROBOT_X + HALF - 0.5):
    o.append(f'<rect x="{px(wx)}" y="{py(2 * HALF + SLIDE)}" width="{0.5 * K}" height="{2 * HALF * K}" fill="#3a6fd8"/>')
o += [f'<text x="{px(ROBOT_X - HALF) - 6}" y="{py(12)}" font-size="11" fill="#333" text-anchor="end">our robot, back on the wall:</text>',
      f'<text x="{px(ROBOT_X - HALF) - 6}" y="{py(12) + 13}" font-size="11" fill="#333" text-anchor="end">front face y 18, arm tips y 24</text>',
      f'<text x="{px(ROBOT_X - HALF) - 6}" y="{py(12) + 26}" font-size="11" fill="#333" text-anchor="end">x {ROBOT_X - HALF:.0f}–{ROBOT_X + HALF:.0f}, on the red CELL\'s axis</text>']
# Walls: red alliance wall on the left, blue on the right, audience wall at the bottom.
o += [f'<rect x="{px(X0) - 8}" y="{py(Y1)}" width="8" height="{H + 8}" fill="#c0392b"/>',
      f'<rect x="{px(X1)}" y="{py(Y1)}" width="8" height="{H + 8}" fill="#3a6fd8"/>',
      f'<rect x="{px(X0) - 8}" y="{py(Y0)}" width="{W + 16}" height="8" fill="#555"/>',
      f'<text x="{px(X0) - 14}" y="{py(Y1 / 2)}" font-size="13" fill="#c0392b" font-weight="bold" text-anchor="middle" '
      f'transform="rotate(-90 {px(X0) - 14} {py(Y1 / 2)})">RED alliance wall (x = 0)</text>',
      f'<text x="{px(X1) + 22}" y="{py(Y1 / 2)}" font-size="13" fill="#3a6fd8" font-weight="bold" text-anchor="middle" '
      f'transform="rotate(90 {px(X1) + 22} {py(Y1 / 2)})">BLUE alliance wall (x = 141.5)</text>',
      f'<text x="{px(X0) + 4}" y="{py(Y0) + 20}" font-size="13" fill="#333" font-weight="bold">AUDIENCE wall (y = 0)</text>',
      f'<text x="{px(X0) - 10}" y="{py(Y0) + 22}" font-size="11" fill="#777" text-anchor="end">(0, 0)</text>',
      f'<text x="{px(X1)}" y="{py(Y1) - 10}" font-size="11" fill="#777" text-anchor="end">field continues to y = 141.5 ↑</text>',
      '</svg>']

html = ('<!doctype html><html><head><meta charset="utf-8"><title>Spill landing window</title></head>'
        '<body style="margin:0;background:#fff;padding:12px;font-family:Helvetica,Arial">'
        f'<div style="font-size:16px;font-weight:bold">Where a red TIP\'s spill first touches the floor: {len(pts)} pieces, '
        f'{len(pts) // 6} simulated TIPs, no robot in the way</div>'
        f'<div style="font-size:12px;color:#555;margin:2px 0 6px;max-width:1040px">Top view, Pedro frame: 141.5 in wall to wall, '
        f'centre 70.75, inches. Each dot is one piece\'s first touch. '
        'Purple: the HIVE frame\'s two foot bars and its outline overhead (you drive under it between the bars). '
        'Red solid: the lowered audience CELL seen from above, its open end shaded; dashed: the raised one. '
        f'The simulator lands a little short of the 3 Oct films (median {y50:.0f} in against about 48).</div>'
        + "".join(o) + '</body></html>')
open(OUT, "w").write(html)
print(f"wrote {OUT}: {len(pts)} first touches, 90% y {y5:.1f}-{y95:.1f}, x {x5:.1f}-{x95:.1f}, median {y50:.1f}; "
      f"down CELL y {DOWN[0]:.1f}-{DOWN[1]:.1f}, lip y {DOWN[2][0]:.1f} z {DOWN[3]:.1f}")
