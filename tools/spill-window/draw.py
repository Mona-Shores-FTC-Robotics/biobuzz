"""Draws where a TIP's spill first touches the floor, from the simulator, with the long U parked at a few
spots (sim-review/spill-window.html; open it in a browser, or screenshot it).

    ./gradlew :TeamCode:testDebugUnitTest --tests '*SpillLandingTest*'   # writes build/sim-logs/spill-first-touch.csv
    python3 tools/spill-window/draw.py [csv] [out.html]

The CSV has one row per spilled piece: how far from its alliance wall and at what x it first touched
the tiles, with no robot in the way (SpillLandingTest.landings). Pedro inches; the spill falls toward
the wall at the lowered CELL's end, so "from the wall" is the same for either end.
"""
import os
import sys

REPO = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
CSV = sys.argv[1] if len(sys.argv) > 1 else os.path.join(REPO, "TeamCode/build/sim-logs/spill-first-touch.csv")
OUT = sys.argv[2] if len(sys.argv) > 2 else os.path.join(REPO, "sim-review/spill-window.html")

# Robot and HIVE geometry, inches (Pedro): an 18 in frame centred on the CELL's axis, arms 6 in past
# its front (RobotAssets.WALL_SLIDE_IN); the HIVE frame's feet start 51.3 in from the wall
# (HiveGeometry.FRAME_DEPTH_IN 38.95 about the centre line 70.75); the CELL opening is 20 in wide.
ROBOT_X, HALF, SLIDE = 60.0, 9.0, 6.0
FEET_FROM_WALL, CELL_X, OPENING = 51.3, 58.0, 20.0
FILMED = (42.0, 54.0)  # the 3 Oct films: first touch about 4 ft out, +-6 in (SpillLandingTest)

# The long U parked with its front face here, and what SideWallSpillTest.howFarForwardToPark measured.
PANELS = [
    (32, "Front face 32 in, arm tips 38 in", "G409 0.05 a TIP over 40 · gathers 39%"),
    (38, "Front face 38 in, arm tips 44 in", "G409 2.0 a TIP: pieces land on the robot's top"),
    (44, "Front face 44 in (at the landing), arm tips 50 in", "G409 5.7 a TIP: nearly every piece hits it first"),
]

rows = [list(map(float, l.split(","))) for l in open(CSV) if l.strip() and not l.startswith("#")]
pts = [(r[1], r[0]) for r in rows]  # (x, from the wall)
q = lambda v, p: sorted(v)[min(len(v) - 1, int(p * len(v)))]
ys, xs = [p[1] for p in pts], [p[0] for p in pts]
y5, y50, y95, x5, x95 = q(ys, .05), q(ys, .5), q(ys, .95), q(xs, .05), q(xs, .95)

X0, X1, Y0, Y1, K = 24, 100, 0, 72, 6.0
W, H = (X1 - X0) * K, (Y1 - Y0) * K
px = lambda x: (x - X0) * K
py = lambda y: H - (y - Y0) * K
outside = sum(1 for x, y in pts if not (X0 < x < X1 and Y0 < y < Y1))


def panel(face, title, caption):
    o = [f'<svg width="{W}" height="{H + 46}" viewBox="0 -24 {W} {H + 46}" xmlns="http://www.w3.org/2000/svg" font-family="Helvetica,Arial,sans-serif">',
         f'<text x="0" y="-8" font-size="14" font-weight="bold" fill="#222">{title}</text>',
         f'<rect x="0" y="0" width="{W}" height="{H}" fill="#f4f1ec" stroke="#999"/>',
         f'<rect x="{px(46)}" y="{py(Y1)}" width="{(95 - 46) * K}" height="{(Y1 - FEET_FROM_WALL) * K}" fill="#d9d2e9" opacity="0.6"/>',
         f'<text x="{px(47)}" y="{py(Y1) + 14}" font-size="11" fill="#5b4a8b">HIVE frame (feet from {FEET_FROM_WALL:.0f} in)</text>',
         f'<rect x="0" y="{py(FILMED[1])}" width="{W}" height="{(FILMED[1] - FILMED[0]) * K}" fill="none" stroke="#a0521d" stroke-dasharray="6 4"/>',
         f'<text x="{W - 4}" y="{py(FILMED[1]) + 13}" font-size="11" fill="#a0521d" text-anchor="end">3 Oct films: first touch about 4 ft out, ±6 in</text>']
    for x, y in pts:
        if X0 < x < X1 and Y0 < y < Y1:
            o.append(f'<circle cx="{px(x):.1f}" cy="{py(y):.1f}" r="1.6" fill="#d9480f" opacity="0.28"/>')
    o += [f'<line x1="{px(CELL_X - OPENING / 2)}" x2="{px(CELL_X + OPENING / 2)}" y1="{py(y95) - 7}" y2="{py(y95) - 7}" stroke="#5b4a8b" stroke-width="3"/>',
          f'<text x="{px(CELL_X - OPENING / 2)}" y="{py(y95) - 12}" font-size="11" fill="#5b4a8b">CELL lip overhead (20 in)</text>',
          f'<rect x="{px(x5)}" y="{py(y95)}" width="{(x95 - x5) * K}" height="{(y95 - y5) * K}" fill="none" stroke="#d9480f" stroke-width="2"/>',
          f'<text x="{px(x95) + 4}" y="{py(y95) + 4}" font-size="11" fill="#d9480f">90% of first touches:</text>',
          f'<text x="{px(x95) + 4}" y="{py(y95) + 17}" font-size="11" fill="#d9480f">{y5:.0f}–{y95:.0f} in out, x {x5:.0f}–{x95:.0f}</text>',
          f'<text x="{px(x95) + 4}" y="{py(y95) + 30}" font-size="11" fill="#d9480f">median {y50:.0f} in</text>',
          f'<rect x="0" y="{H - 4}" width="{W}" height="4" fill="#555"/>',
          f'<text x="4" y="{H - 8}" font-size="11" fill="#555">alliance wall (0 in)</text>']
    for d in (12, 24, 36, 48, 60):
        o.append(f'<text x="{W - 4}" y="{py(d) + 4}" font-size="10" fill="#999" text-anchor="end">{d} in</text>')
    o += [f'<rect x="{px(ROBOT_X - HALF)}" y="{py(face)}" width="{2 * HALF * K}" height="{2 * HALF * K}" fill="#c9ccd3" stroke="#555" opacity="0.92"/>',
          f'<rect x="{px(ROBOT_X - HALF + 1)}" y="{py(face)}" width="{(2 * HALF - 2) * K}" height="{1.5 * K}" fill="#f0a020"/>']
    for wx in (ROBOT_X - HALF, ROBOT_X + HALF - 0.25):
        o.append(f'<rect x="{px(wx)}" y="{py(face + SLIDE)}" width="{0.25 * K + 2}" height="{2 * HALF * K}" fill="#3a6fd8"/>')
    o += [f'<text x="{px(ROBOT_X + HALF) + 4}" y="{py(face - 9)}" font-size="12" fill="#333">robot</text>',
          f'<text x="0" y="{H + 16}" font-size="12" fill="#222">{caption}</text>', '</svg>']
    return "".join(o)


html = ('<!doctype html><html><head><meta charset="utf-8"><title>Spill landing window</title></head>'
        '<body style="margin:0;background:#fff;padding:12px;font-family:Helvetica,Arial">'
        f'<div style="font-size:16px;font-weight:bold">Where a TIP\'s spill first touches the floor ({len(pts)} pieces, '
        f'{len(pts) // 6} simulated TIPs, no robot), and the long U parked three ways</div>'
        f'<div style="font-size:12px;color:#555;margin:2px 0 8px">Top view of our end of the field. Each dot is one '
        f'piece\'s first touch; {outside} more land off this view (bouncing off the HIVE). The simulator\'s spill lands '
        f'a little short of the films (median {y50:.0f} in against about 48); its spread is the simulator\'s own.</div>'
        '<div style="display:flex;gap:18px">' + "".join(panel(*p) for p in PANELS) + '</div>'
        '<div style="font-size:12px;color:#555;margin-top:6px">What must stay short of the landing is the robot\'s '
        '<b>front face and top</b>: thin arms can reach into it, since pieces land between them, on the floor.</div>'
        '</body></html>')
open(OUT, "w").write(html)
print(f"wrote {OUT}: {len(pts)} first touches, 90% {y5:.1f}-{y95:.1f} in out, x {x5:.1f}-{x95:.1f}, median {y50:.1f}")
