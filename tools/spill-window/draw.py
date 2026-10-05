"""Draws where a TIP's spill first touches the floor, from the simulator, on the Visualizer's BIOBUZZ field
(sim-review/spill-window.html; open it in a browser, or screenshot it).

    ./gradlew :TeamCode:testDebugUnitTest --tests '*SpillLandingTest*'   # writes build/sim-logs/spill-first-touch.csv
    python3 tools/spill-window/draw.py [--face 35] [--arms 6] [csv] [out.html]

The background is the Visualizer's field image (public/fields/biobuzz.webp, in the Visualizer checkout that
autogen.py uses: AUTO_BUILDER_DIR, or ../visualizer). The Visualizer stretches it over the whole field,
0-141.5 in on both axes, so it is drawn here the same way: Pedro frame, origin at the corner where the red
alliance wall (x = 0, left) meets the audience wall (y = 0, bottom), inches. It shows the HIVE as it
starts the match.

The CSV has one row per spilled piece from the red HIVE's audience CELL, with no robot in the way
(SpillLandingTest.landings). Drawn is where each piece first hit anything after leaving the CELL: the tiles,
the HIVE's feet, or a piece already down. (Its first touch of the tiles alone is later for a piece that lands
on the pile and rolls off it, often a long way off.) Each piece is drawn at its true size; the dashed box
bounds every footprint, the solid one 90% of them on each axis.
"""
import base64
import os
import sys

REPO = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
ARGS = sys.argv[1:]
FACE = 18.0  # the robot's front face, inches from the audience wall (18: backed against it)
if "--face" in ARGS:
    i = ARGS.index("--face")
    FACE = float(ARGS[i + 1])
    del ARGS[i:i + 2]
ARMS = 0.0  # side walls slid this far past the front face (the long U: 6), 0 for none
if "--arms" in ARGS:
    i = ARGS.index("--arms")
    ARMS = float(ARGS[i + 1])
    del ARGS[i:i + 2]
CSV = ARGS[0] if ARGS else os.path.join(REPO, "TeamCode/build/sim-logs/spill-first-touch-8-pollen.csv")
OUT = ARGS[1] if len(ARGS) > 1 else os.path.join(REPO, "sim-review/spill-window.html")
VIS = [os.environ.get("AUTO_BUILDER_DIR", ""), os.path.join(REPO, "..", "visualizer"),
       os.path.join(REPO, "..", "mona-shores-ftc-robotics", "visualizer")]
FIELD_IMAGE = next((p for p in (os.path.join(v, "public/fields/biobuzz.webp") for v in VIS if v) if os.path.isfile(p)), None)
if FIELD_IMAGE is None:
    sys.exit("No Visualizer checkout with public/fields/biobuzz.webp: set AUTO_BUILDER_DIR")

FIELD = 141.5          # FieldFrame.FIELD_SIZE_INCHES, wall face to wall face
CENTRE = FIELD / 2     # FieldFrame.FIELD_CENTRE_INCHES
TILE = FIELD / 6
# Our robot: the plain 18 in square, side walls in, on the red CELL's axis (FieldSim.RED_HIVE_X_IN),
# facing the HIVE with its front face FACE in from the audience wall.
ROBOT_X, HALF = CENTRE - 12.75, 9.0
ROBOT_Y = FACE - HALF

rows = [list(map(float, l.split(","))) for l in open(CSV) if l.strip() and not l.startswith("#")]
pts = [(r[6], r[5], r[7]) for r in rows]  # where each piece first hit anything after leaving the CELL: (x, y, radius)
q = lambda v, p: sorted(v)[min(len(v) - 1, int(p * len(v)))]
ys, xs = [p[1] for p in pts], [p[0] for p in pts]
y5, y95, x5, x95 = q(ys, .05), q(ys, .95), q(xs, .05), q(xs, .95)
# Both boxes bound the pieces' footprints, not their centres: every piece, and 90% on each axis.
rad = max(p[2] for p in pts)
ALL = (min(x - r for x, y, r in pts), min(y - r for x, y, r in pts), max(x + r for x, y, r in pts), max(y + r for x, y, r in pts))
NINETY = (x5 - rad, y5 - rad, x95 + rad, y95 + rad)

Y1, K = 100.0, 7.0          # show y 0-100: our end, the HIVE and the field centre
ML, MB, MT, MR = 70, 56, 14, 34
BG, INK = "#2b2b2b", "#e8e8e8"   # the margin matches the field image's tiles
W, H = FIELD * K, Y1 * K
px = lambda x: ML + x * K
py = lambda y: MT + H - y * K
img = base64.b64encode(open(FIELD_IMAGE, "rb").read()).decode()

o = [f'<svg width="{W + ML + MR:.0f}" height="{H + MT + MB:.0f}" xmlns="http://www.w3.org/2000/svg" '
     'font-family="Helvetica,Arial,sans-serif">',
     f'<rect width="100%" height="100%" fill="{BG}"/>',
     f'<clipPath id="field"><rect x="{px(0)}" y="{py(Y1)}" width="{W}" height="{H}"/></clipPath>',
     f'<image x="{px(0)}" y="{py(FIELD)}" width="{W}" height="{FIELD * K}" preserveAspectRatio="none" clip-path="url(#field)" '
     f'href="data:image/webp;base64,{img}"/>']
# Axes like a graph: x grows to the right, y grows up; ticks at the tile seams, Pedro inches.
for k in range(7):
    d = k * TILE
    o += [f'<line x1="{px(d):.1f}" x2="{px(d):.1f}" y1="{py(0)}" y2="{py(0) + 5}" stroke="{INK}"/>',
          f'<text x="{px(d):.1f}" y="{py(0) + 19}" font-size="12" fill="{INK}" text-anchor="middle">{d:.2f}</text>']
    if d <= Y1:
        o += [f'<line x1="{px(0) - 5}" x2="{px(0)}" y1="{py(d):.1f}" y2="{py(d):.1f}" stroke="{INK}"/>',
              f'<text x="{px(0) - 8}" y="{py(d) + 4:.1f}" font-size="12" fill="{INK}" text-anchor="end">{d:.2f}</text>']
o += [f'<text x="{px(FIELD / 2)}" y="{py(0) + 44}" font-size="14" font-weight="bold" fill="{INK}" text-anchor="middle">x (in) →</text>',
      f'<text x="18" y="{py(Y1 / 2)}" font-size="14" font-weight="bold" fill="{INK}" text-anchor="middle" '
      f'transform="rotate(-90 18 {py(Y1 / 2)})">y (in) →</text>',
      f'<line x1="{px(CENTRE)}" x2="{px(CENTRE)}" y1="{py(0)}" y2="{py(Y1)}" stroke="#7fd39b" stroke-opacity="0.7" stroke-dasharray="8 6"/>',
      f'<line x1="{px(0)}" x2="{px(FIELD)}" y1="{py(CENTRE)}" y2="{py(CENTRE)}" stroke="#7fd39b" stroke-opacity="0.7" stroke-dasharray="8 6"/>']
# The spill: each piece's footprint, true size, where it first hit anything; the box around every one
# (dashed) and around 90% on each axis (solid).
for x, y, r in pts:
    o.append(f'<circle cx="{px(x):.1f}" cy="{py(y):.1f}" r="{r * K:.1f}" fill="#ffb347" fill-opacity="0.03"/>')
for x, y, r in pts:
    o.append(f'<circle cx="{px(x):.1f}" cy="{py(y):.1f}" r="1.1" fill="#ffd9a0" opacity="0.6"/>')
for (bx0, by0, bx1, by1), dash in ((ALL, ' stroke-dasharray="7 5"'), (NINETY, "")):
    o.append(f'<rect x="{px(bx0):.1f}" y="{py(by1):.1f}" width="{(bx1 - bx0) * K:.1f}" height="{(by1 - by0) * K:.1f}" '
             f'fill="none" stroke="#ff3b3b" stroke-width="2.2"{dash}/>')
# Our robot, with its centre's Pedro coordinates.
o += [f'<rect x="{px(ROBOT_X - HALF)}" y="{py(ROBOT_Y + HALF)}" width="{2 * HALF * K}" height="{2 * HALF * K}" fill="#c9ccd3" stroke="#888" stroke-width="1.2"/>',
      f'<rect x="{px(ROBOT_X - HALF + 1)}" y="{py(ROBOT_Y + HALF)}" width="{(2 * HALF - 2) * K}" height="{1.5 * K}" fill="#f0a020"/>',
      f'<circle cx="{px(ROBOT_X)}" cy="{py(ROBOT_Y)}" r="3" fill="#222"/>',
      f'<text x="{px(ROBOT_X)}" y="{py(ROBOT_Y) + 18}" font-size="12" fill="#222" text-anchor="middle">({ROBOT_X:g}, {ROBOT_Y:g})</text>']
if ARMS:  # side walls, 0.25 in thick (RobotAssets.WALL_THICKNESS_IN), slid forward along the sides
    for wx in (ROBOT_X - HALF, ROBOT_X + HALF - 0.25):
        o.append(f'<rect x="{px(wx):.1f}" y="{py(ROBOT_Y + HALF + ARMS):.1f}" width="{max(2.0, 0.25 * K):.1f}" height="{2 * HALF * K}" fill="#5aa0ff"/>')
o.append('</svg>')

html = ('<!doctype html><html><head><meta charset="utf-8"><title>Spill landing window</title></head>'
        f'<body style="margin:0;background:{BG}">' + "".join(o) + '</body></html>')
open(OUT, "w").write(html)
print(f"wrote {OUT} on {FIELD_IMAGE}: {len(pts)} first contacts; footprints: all x {ALL[0]:.1f}-{ALL[2]:.1f} y {ALL[1]:.1f}-{ALL[3]:.1f}, "
      f"90% x {NINETY[0]:.1f}-{NINETY[2]:.1f} y {NINETY[1]:.1f}-{NINETY[3]:.1f}; robot front face {FACE:g}, arms {ARMS:g}")
