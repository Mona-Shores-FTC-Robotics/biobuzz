"""Draws the two-job hook against a FLOWER: from the side, from the top, and where the FLOWERs are on the field
(sim-review/flower-backboard.html; doc/flower-backboard.md explains it).

    python3 tools/flower-backboard/draw.py [--plate ANGLE HEIGHT SPEED low|high] [out.html]

--plate draws a backboard from plate.py (angle from vertical, the height of the ball's centre where it meets
the plate, launch speed in in/s, which arc) and one shot off it at the middle guess (e 0.3, mu 0.3, no spin).

Same look as tools/spill-window/draw.py. Every FLOWER size is from the Competition Manual (§9.7, Fig 9-12);
the FLOWERs' places are from AdvantageScope's field CAD (biobuzz-staged-pieces.csv); the hook is design 13,
"spring hood, large right hook" (AutoStudyTest.hook on claude/biobuzz-robot-body-designs-hi386c): a 14 in
chassis, a 10 in arm and an 18 in crossbeam with 4 in guides, hinged at the bottom of the chassis' front face.
Grey dashed labels mark what isn't measured or published.
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(__file__))
import plate  # noqa: E402

REPO = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
ARGS = sys.argv[1:]
PLATE = None
if "--plate" in ARGS:
    i = ARGS.index("--plate")
    PLATE = (float(ARGS[i + 1]), float(ARGS[i + 2]), float(ARGS[i + 3]), ARGS[i + 4] == "high")
    del ARGS[i:i + 5]
OUT = ARGS[0] if ARGS else os.path.join(REPO, "sim-review", "flower-backboard.html")

BG, INK, DIM = "#2b2b2b", "#e8e8e8", "#9a9a9a"
ROBOT, GUIDE, FLOWER_C, PIPE, TARGET, BAD = "#c9ccd3", "#5aa0ff", "#e0b04a", "#7fd39b", "#7fd39b", "#ff3b3b"
NECTAR_C = "#ff5a5a"

# The FLOWER (inches): Competition Manual §9.7, Fig 9-12.
TOP, OPENING, BACKSTOP_H = 21.5, 4.0, 1.25
TO_FIELD_EDGE, TO_BACKSTOP = 2.40, 1.89      # from the opening's centre, Fig 9-12 top view
RETRIEVAL, BOTTOM_RING = 3.55, 0.43
CENTRE = plate.CX                             # 2.71 in from the wall: AdvantageScope's field CAD
FACE = CENTRE + TO_FIELD_EDGE                 # the robot's front face against the top ring's field edge
# The hook (design 13).
CHASSIS, ARM, GUIDE_H, WIDTH = 14.0, 10.0, 4.0, 18.0
REACH = math.hypot(ARM, GUIDE_H)
NECTAR_R = 1.8

o = []


def text(x, y, s, size=12, fill=INK, anchor="start", bold=False):
    w = ' font-weight="bold"' if bold else ""
    o.append(f'<text x="{x:.1f}" y="{y:.1f}" font-size="{size}" fill="{fill}" text-anchor="{anchor}"{w}>{s}</text>')


def line(x1, y1, x2, y2, stroke=INK, width=1.0, dash=None, cap="butt"):
    d = f' stroke-dasharray="{dash}"' if dash else ""
    o.append(f'<line x1="{x1:.1f}" y1="{y1:.1f}" x2="{x2:.1f}" y2="{y2:.1f}" stroke="{stroke}" stroke-width="{width}"{d} stroke-linecap="{cap}"/>')


def rect(x, y, w, h, fill="none", stroke="none", width=1.0, dash=None, opacity=1.0):
    d = f' stroke-dasharray="{dash}"' if dash else ""
    o.append(f'<rect x="{x:.1f}" y="{y:.1f}" width="{w:.1f}" height="{h:.1f}" fill="{fill}" fill-opacity="{opacity}" '
             f'stroke="{stroke}" stroke-width="{width}"{d}/>')


def circle(x, y, r, fill="none", stroke="none", width=1.0, dash=None, opacity=1.0):
    d = f' stroke-dasharray="{dash}"' if dash else ""
    o.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{r:.1f}" fill="{fill}" fill-opacity="{opacity}" stroke="{stroke}" '
             f'stroke-width="{width}"{d}/>')


# ---- Panel 1: from the side. x is inches from the wall (wall on the left), z up; the robot faces the wall. ----
K1, X0, Y0 = 18.0, 175, 70         # px per inch; where (wall, z = 31) is drawn
sx = lambda x: X0 + x * K1
sz = lambda z: Y0 + (31 - z) * K1
LBL = sx(FACE + 18) + 12          # the label column, right of the start cube
text(40, 40, "From the side: the hook at a FLOWER, the robot's front face against the top ring", 15, bold=True)
# R105 and the start cube
back = FACE + CHASSIS
rect(sx(back - 24), sz(29), 24 * K1, 29 * K1, stroke=DIM, width=1.2, dash="7 5")
text(sx(back - 24) + 4, sz(29) - 6, "R105: 24 in long from the chassis' back (14 in chassis + 10 in forward), 29 in tall", 11, DIM)
rect(sx(FACE), sz(18), 18 * K1, 18 * K1, stroke=DIM, width=1, dash="2 4")
text(sx(FACE + 18) - 4, sz(18) + 14, "18 in start cube (R102)", 11, DIM, "end")
# the target: above the opening, inside 29 in
rect(sx(0.82), sz(29), (FACE - 0.82) * K1, (29 - TOP - BACKSTOP_H) * K1, fill=TARGET, opacity=0.18, stroke=TARGET, width=1)
text(sx(0.82) - 6, sz(27.5), "a backboard", 11, TARGET, "end")
text(sx(0.82) - 6, sz(27.5) + 14, "goes here: over", 11, TARGET, "end")
text(sx(0.82) - 6, sz(27.5) + 28, "the opening,", 11, TARGET, "end")
text(sx(0.82) - 6, sz(27.5) + 42, "under 29 in", 11, TARGET, "end")
# the tiles, the wall
line(sx(-9), sz(0), sx(back + 1), sz(0), INK, 1.5)
rect(sx(-1), sz(12), 1 * K1, 12 * K1, fill="#555")
text(sx(-0.5), sz(12) - 6, "wall", 11, DIM, "middle")
# the FLOWER
mid = BOTTOM_RING + RETRIEVAL
rect(sx(0.31), sz(BOTTOM_RING), (FACE - 0.31) * K1, BOTTOM_RING * K1, fill="#222", stroke=FLOWER_C)
rect(sx(0.1), sz(mid), 0.7 * K1, RETRIEVAL * K1, fill="#999")
rect(sx(0.31), sz(mid + 0.6), (FACE - 0.31) * K1, 0.6 * K1, fill="#222", stroke=FLOWER_C, dash="3 2")
for px_ in (CENTRE - 1.8, CENTRE + 1.8):
    rect(sx(px_ - 0.3), sz(TOP - 0.5), 0.6 * K1, (TOP - 0.5 - mid - 0.6) * K1, fill=PIPE, opacity=0.8)
rect(sx(0.31), sz(TOP), (CENTRE - OPENING / 2 - 0.31) * K1, 0.5 * K1, fill=FLOWER_C)
rect(sx(CENTRE + OPENING / 2), sz(TOP), (FACE - CENTRE - OPENING / 2) * K1, 0.5 * K1, fill=FLOWER_C)
rect(sx(CENTRE - TO_BACKSTOP - 0.25), sz(TOP + BACKSTOP_H), 0.25 * K1, BACKSTOP_H * K1, fill="#b07ae0")
circle(sx(CENTRE), sz(TOP + NECTAR_R + 0.2), NECTAR_R * K1, stroke=NECTAR_C, width=1.5, dash="4 3")
# the robot
rect(sx(FACE), sz(14), CHASSIS * K1, 14 * K1, fill=ROBOT, stroke="#888", opacity=0.9)
text(sx(FACE + CHASSIS / 2 + 1.5), sz(9), "14 in chassis", 12, "#222", "middle")
text(sx(FACE + CHASSIS / 2 + 1.5), sz(9) + 15, "(height not decided)", 11, "#555", "middle")
# the hook: flat (where it would be, through the FLOWER), 45 degrees, stowed. Arm toward the wall, guides up when flat.
for ang, op, dash in ((0, 0.45, "5 4"), (45, 0.6, None), (90, 1.0, None)):
    a = math.radians(ang)
    tip = (FACE - ARM * math.cos(a), ARM * math.sin(a))
    g = (tip[0] + GUIDE_H * math.sin(a), tip[1] + GUIDE_H * math.cos(a))
    o.append(f'<g opacity="{op}">')
    line(sx(FACE), sz(0), sx(tip[0]), sz(tip[1]), GUIDE, 5, dash, cap="round")
    line(sx(tip[0]), sz(tip[1]), sx(g[0]), sz(g[1]), GUIDE, 3, dash, cap="round")
    o.append('</g>')
circle(sx(FACE), sz(0), 4, fill="#222")
# the reach circle
r = REACH * K1
o.append(f'<path d="M {sx(FACE - REACH):.1f} {sz(0):.1f} A {r:.1f} {r:.1f} 0 0 1 {sx(FACE + REACH):.1f} {sz(0):.1f}" '
         f'fill="none" stroke="{GUIDE}" stroke-dasharray="6 5" stroke-width="1.3"/>')
line(sx(CENTRE + 0.4), sz(REACH), sx(CENTRE + 0.4), sz(TOP + BACKSTOP_H), BAD, 1.6)
line(sx(CENTRE + 0.1), sz(REACH), sx(CENTRE + 0.7), sz(REACH), BAD, 1.6)
text(sx(-1.2), sz((REACH + TOP) / 2) - 6, f"{TOP + BACKSTOP_H - REACH:.0f} in", 15, BAD, "end", bold=True)
text(sx(-1.2), sz((REACH + TOP) / 2) + 10, "short", 15, BAD, "end", bold=True)
# the labels, in a column to the right
for z, s_, c in ((TOP + BACKSTOP_H, "FLOWER: backstop 1.25 in above the top ring", "#b07ae0"),
                 (TOP, "top ring 21.5 in up, opening 4.0 in (NECTAR 3.6 in)", FLOWER_C),
                 (ARM + 1.2, "hook (design 13): 10 in arm, 4 in guides, hinge at the floor;", GUIDE),
                 (ARM - 0.0, f"stowed, 45 degrees, and flat (dashed, which here would be inside the FLOWER)", GUIDE),
                 (ARM - 1.2, f"blue dashed arc: everything on it stays within {REACH:.1f} in of the hinge", GUIDE),
                 (mid, "middle ring 4 in up (its thickness isn't published)", DIM),
                 (1.6, "retrieval opening 3.55 in", DIM)):
    text(LBL, sz(z) + 4, s_, 11, c)
# the backboard from plate.py, with a shot off it
if PLATE:
    a, z, v, high = PLATE
    segs, target = plate.segments(math.radians(a), z)
    (lx, lz), (hx, hz) = segs[0]
    line(sx(lx), sz(lz), sx(hx), sz(hz), "#ffffff", 4, cap="round")
    th = plate.aim(v, target, high)
    p, vel, w = plate.EXIT, (-v * math.cos(th), v * math.sin(th)), 0.0
    pts = [p]
    for _ in range(20000):
        vel = (vel[0], vel[1] - plate.G * 1e-4)
        p = (p[0] + vel[0] * 1e-4, p[1] + vel[1] * 1e-4)
        for s_ in segs:
            p, vel, w, _h = plate.collide(p, vel, w, s_, 0.3, 0.3)
        pts.append(p)
        if p[1] < TOP - 3 and vel[1] < 0:
            break
    o.append('<polyline points="' + " ".join(f"{sx(x):.1f},{sz(zz):.1f}" for x, zz in pts[::20])
             + f'" fill="none" stroke="{NECTAR_C}" stroke-width="1.5" stroke-dasharray="3 3"/>')
    circle(sx(plate.EXIT[0]), sz(plate.EXIT[1]), 3, fill=NECTAR_C)
    text(LBL, sz(26) + 4, f"white: a backboard {a:g} degrees from vertical (not on the hook: see the doc)", 11, "#ffffff")
    text(LBL, sz(24.6) + 4, f"red: one NECTAR off it at {v:g} in/s, if it bounces at e 0.3, mu 0.3 (guesses)", 11, NECTAR_C)
    text(LBL, sz(plate.EXIT[1]) + 4, "launcher exit: FieldSim's 17 in, 9 in behind the face (placeholders)", 11, NECTAR_C)
SIDE_H = sz(-1.5)

# ---- Panel 2: from the top, close up. x from the wall to the right, y across. ----
K2 = 15.0
X2, Y2 = 60, SIDE_H + 60
tx = lambda x: X2 + x * K2
ty = lambda y: Y2 + (10 - y) * K2
text(40, Y2 - 22, "From the top: 0.2 in to spare around a NECTAR", 15, bold=True)
rect(tx(-1), ty(10), 1 * K2, 20 * K2, fill="#555")
rect(tx(CENTRE - TO_BACKSTOP), ty(TO_FIELD_EDGE), (TO_BACKSTOP + TO_FIELD_EDGE) * K2, 2 * TO_FIELD_EDGE * K2,
     fill=FLOWER_C, opacity=0.75)
rect(tx(CENTRE - TO_BACKSTOP - 0.25), ty(TO_FIELD_EDGE - 0.4), 0.25 * K2, 2 * (TO_FIELD_EDGE - 0.4) * K2, fill="#b07ae0")
circle(tx(CENTRE), ty(0), OPENING / 2 * K2, fill=BG, stroke="#222", width=1.5)
circle(tx(CENTRE), ty(0), NECTAR_R * K2, fill=NECTAR_C, opacity=0.55)
rect(tx(FACE), ty(WIDTH / 2), CHASSIS * K2, WIDTH * K2, fill=ROBOT, stroke="#888", opacity=0.9)
line(tx(FACE + 0.15), ty(-WIDTH / 2), tx(FACE + 0.15), ty(WIDTH / 2), GUIDE, 3)
line(tx(FACE + 0.15), ty(-WIDTH / 2 + 0.15), tx(FACE + 4), ty(-WIDTH / 2 + 0.15), GUIDE, 3)
text(tx(FACE + CHASSIS / 2), ty(0) + 4, "robot, centred on the FLOWER", 12, "#222", "middle")
cap = Y2 + 20 * K2 + 22
for i, (s_, c) in enumerate((("Gold: the top ring, approximately (Fig 9-12: 2.40 in to its field edge, 1.89 in to the", FLOWER_C),
                             ("backstop, purple). The opening is 4.0 in, a NECTAR 3.6 in: its centre has to be within", INK),
                             ("0.2 in of the opening's, both ways, to drop straight in. Blue: the hook stowed, its", INK),
                             ("crossbeam across the front face, its arm on the right. Nothing on it reaches the ring's height.", GUIDE))):
    text(40, cap + 16 * i, s_, 11, c)
TOP_H = cap + 16 * 4 + 10

# ---- Panel 3: the field, where the FLOWERs are. ----
K3 = 2.6
X3, Y3 = 600, SIDE_H + 60
fx = lambda x: X3 + x * K3
FIELD = 141.5  # util/FieldFrame.FIELD_SIZE_INCHES
MID = FIELD / 2
fy = lambda y: Y3 + (FIELD - y) * K3
text(X3, Y3 - 22, "The four FLOWERs, and red's LOADING ZONE", 15, bold=True)
rect(fx(0), fy(FIELD), FIELD * K3, FIELD * K3, fill="#3a3a3a", stroke=INK)
rect(fx(MID - 24.7), fy(MID + 19.5), 49.5 * K3, 39 * K3, fill="#666", opacity=0.6)
text(fx(MID), fy(MID) + 4, "HIVE frame", 12, INK, "middle")
lz = (0, 11, 4 * FIELD / 6, 5 * FIELD / 6)  # FieldSim.loadingZone(RED): tile A5
rect(fx(lz[0]), fy(lz[3]), (lz[1] - lz[0]) * K3, (lz[3] - lz[2]) * K3, fill="#ff3b3b", opacity=0.45)
text(fx(13), fy(108) + 4, "red LOADING ZONE", 11, "#ff8080")
lzc = (5.5, 4.5 * FIELD / 6)
flowers = [("W", 2.71, 47.36), ("N", 47.36, 138.79), ("E", 138.79, 94.14), ("S", 94.14, 2.71)]
for name, x, y in flowers:
    circle(fx(x), fy(y), 2.4 * K3, fill=FLOWER_C)
    d = math.hypot(x - lzc[0], y - lzc[1])
    if d < 80:
        line(fx(lzc[0]), fy(lzc[1]), fx(x), fy(y), INK, 1, "4 4")
        mx, my = (lzc[0] + x) / 2, (lzc[1] + y) / 2
        text(fx(mx) + 6, fy(my), f"{d:.0f} in", 12, INK)
    text(fx(x) + (10 if x < 100 else -10), fy(y) + (-12 if y < 10 else 18 if y > 130 else -8), f"({x:g}, {y:g})", 11, DIM,
         "start" if x < 100 else "end")
text(fx(0), fy(0) + 18, "Pedro frame: red alliance wall x = 0 (left), audience wall y = 0 (bottom)", 11, DIM)
FIELD_H = fy(0) + 30

H = max(TOP_H, FIELD_H) + 10
W = 1080
svg = (f'<svg width="{W}" height="{H:.0f}" xmlns="http://www.w3.org/2000/svg" font-family="Helvetica,Arial,sans-serif">'
       f'<rect width="100%" height="100%" fill="{BG}"/>' + "".join(o) + "</svg>")
with open(OUT, "w") as f:
    f.write('<!doctype html><html><head><meta charset="utf-8"><title>FLOWER backboard</title></head>'
            f'<body style="margin:0;background:{BG}">{svg}</body></html>')
print(f"wrote {OUT} ({W} x {H:.0f})")
