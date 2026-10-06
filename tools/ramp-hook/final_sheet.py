"""The ramp hook's final sheet, drawn to scale (sim-review/ramp-hook-final.svg; doc/ramp-hook.md).

    python3 tools/ramp-hook/final_sheet.py

From the side and from above at one scale, with a 1 in scale bar, the block at four times that, and the numbers.
FLOWER sizes are the Competition Manual's Fig 9-12 (bottom ring 0.43 in thick, hole 2.79 in, retrieval opening
3.55 in, 3.57 in from the ring's front edge to the uprights). Where the figure gives no number, it's scaled off the
figure and marked "est.". The FLOWER's centre is 2.71 in from the wall (AdvantageScope's field CAD). The block is
cad/ramp-hook/parts.py's ramp_block.
"""
import math
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(HERE, "..", ".."))
sys.path.insert(0, os.path.join(REPO, "cad", "ramp-hook"))
import parts as P  # noqa: E402

IN = 25.4
# ---- FLOWER (x: inches from the field wall toward the robot; y across; z up from the tiles) ----
FC = 2.71                       # centre, from the wall
HOLE = 2.79 / 2                 # bottom ring's hole radius
RING_T = 0.43                   # bottom ring's thickness
OPEN = 3.55                     # retrieval opening: tiles to the lower bracket
UP_FACE = FC - 1.25             # uprights' face (est., scaled)
UP_T = 0.75                     # uprights' size (est.)
RING_FRONT = UP_FACE + 3.57     # the ring's front edge
UP_Y0 = math.sqrt(HOLE ** 2 - (FC - UP_FACE) ** 2)    # uprights' inside faces sit with their corners on the hole
POST, POST_R = 1.55, 0.65       # posts round the column (est.)
BRACKET_T = 0.6                 # lower bracket's thickness (est.)
R = 1.4                         # POLLEN radius (2.8 in)
# ---- robot ----
CH, GAP = 14.5, 8.0
DEPTH, ARC_R = P.DEPTH / IN, P.ARC_R / IN
BOTTOM, TOP = P.BOTTOM / IN, P.TOP / IN
TIP = FC - ARC_R                # the block's tip on the centreline (the curve is centred on the hole)
BACK = TIP + DEPTH              # the block's back edge
ROD_X, ROD_Z, ROD_R = TIP + (P.DEPTH - P.ROD_X) / IN, P.ROD_Z / IN, P.ROD_D / IN / 2
FACE = BACK + GAP               # chassis front face (the intake)
WALL_H = 3.5                    # curtains: they pass under the FLOWER's bracket (3.55)
SIDE_H = 4.0                    # side wall: a ladder of two 8 mm shafts with a panel between, ~4 in to the top clips
CURT_B = 1.3                    # curtains' bottom, on the clips
HW = CH / 2

INK, DIM, RED = "#1b222b", "#6b7682", "#b5452f"
BLUE, DBLUE, PRINT, ALU, ORANGE, GREEN, YEL, BLK, GREY, UPR = ("#4f8fe0", "#2f5f9e", "#b06ad8", "#aab3bd", "#e8742a",
                                                              "#3fae4f", "#f1cf3a", "#2b2f35", "#c3cad2", "#9aa3ad")
o = []


def text(x, y, s, size=13, fill=INK, anchor="start", w=400):
    o.append(f'<text x="{x:.1f}" y="{y:.1f}" font-size="{size}" fill="{fill}" text-anchor="{anchor}" font-weight="{w}">{s}</text>')


def poly(pts, fill="none", stroke=INK, sw=1.2, dash=None, op=1.0):
    d = f' stroke-dasharray="{dash}"' if dash else ""
    o.append(f'<polygon points="{" ".join(f"{a:.1f},{b:.1f}" for a, b in pts)}" fill="{fill}" fill-opacity="{op}" '
             f'stroke="{stroke}" stroke-width="{sw}"{d}/>')


def line(x0, y0, x1, y1, stroke=DIM, sw=1.2, dash=None):
    d = f' stroke-dasharray="{dash}"' if dash else ""
    o.append(f'<line x1="{x0:.1f}" y1="{y0:.1f}" x2="{x1:.1f}" y2="{y1:.1f}" stroke="{stroke}" stroke-width="{sw}"{d}/>')


def circle(x, y, r, fill="none", stroke=INK, sw=1.2, dash=None):
    d = f' stroke-dasharray="{dash}"' if dash else ""
    o.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{r:.1f}" fill="{fill}" stroke="{stroke}" stroke-width="{sw}"{d}/>')


def dim(x0, y0, x1, y1, label, side=1, col=RED, size=12):
    """A dimension line with ticks; the label above (horizontal) or beside (vertical, side 1 right, -1 left)."""
    line(x0, y0, x1, y1, col, 1.1)
    horiz = abs(y1 - y0) < 0.01
    for a, b in ((x0, y0), (x1, y1)):
        line(a, b - 4, a, b + 4, col, 1.1) if horiz else line(a - 4, b, a + 4, b, col, 1.1)
    if horiz:
        text((x0 + x1) / 2, y0 - 5, label, size, col, "middle", 700)
    else:
        text((x0 + x1) / 2 + 6 * side, (y0 + y1) / 2 + 4, label, size, col, "start" if side > 0 else "end", 700)


def scale_bar(x, y, k, label="1 in"):
    for i in range(3):
        poly([(x + i * k, y), (x + (i + 1) * k, y), (x + (i + 1) * k, y + 6), (x + i * k, y + 6)],
             fill=INK if i % 2 == 0 else "#ffffff", stroke=INK, sw=1)
    text(x, y + 20, "0", 11, DIM); text(x + 3 * k, y + 20, "3 in", 11, DIM, "middle"); text(x + k, y + 20, label, 11, DIM, "middle")


W, H = 1760, 1480
o.append(f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" font-family="Helvetica,Arial,sans-serif">')
o.append('<rect width="100%" height="100%" fill="#ffffff"/>')
text(30, 42, "Ramp hook: the final design, to scale", 24, INK, w=700)
text(30, 66, "All views at one scale (scale bars below). FLOWER from the Competition Manual, Fig 9-12; \"est.\" marks a size scaled off the figure, not dimensioned. "
             "Robot sizes are this design's.", 14, DIM)

K = 33.0                                   # px per inch for views 1 and 2
# ================= 1. SIDE SECTION through the centreline =================
SX0, SZ0 = 80 + (FACE + CH + 0.4) * K, 600     # wall at x = 0 on the right; tiles at SZ0
X = lambda x: SX0 - x * K
Z = lambda z: SZ0 - z * K
text(30, 100, "1 · From the side, cut through the middle (the robot drives right, into the FLOWER)", 17, INK, w=700)
line(X(FACE + CH + 0.3), Z(0), X(-0.7), Z(0), INK, 1.6)
poly([(X(0), Z(13.5)), (X(-0.6), Z(13.5)), (X(-0.6), Z(0)), (X(0), Z(0))], fill="#d9dee4", stroke="none")
text(X(-0.3), Z(13.5) - 6, "field wall", 11, DIM, "middle")
# FLOWER: bottom ring either side of the hole, uprights, bracket, posts cut at 12.5 in
poly([(X(RING_FRONT), Z(RING_T)), (X(FC + HOLE), Z(RING_T)), (X(FC + HOLE), Z(0)), (X(RING_FRONT), Z(0))], fill=BLK, stroke="none")
poly([(X(FC - HOLE), Z(RING_T)), (X(0.05), Z(RING_T)), (X(0.05), Z(0)), (X(FC - HOLE), Z(0))], fill=BLK, stroke="none")
poly([(X(UP_FACE), Z(OPEN)), (X(UP_FACE - UP_T), Z(OPEN)), (X(UP_FACE - UP_T), Z(RING_T)), (X(UP_FACE), Z(RING_T))], fill=UPR, stroke="#7d8792", dash="3 2")
poly([(X(RING_FRONT + 0.1), Z(OPEN + BRACKET_T)), (X(0.1), Z(OPEN + BRACKET_T)), (X(0.1), Z(OPEN)), (X(RING_FRONT + 0.1), Z(OPEN))], fill=BLK, stroke="none")
for s in (-1, 1):
    poly([(X(FC + s * POST - POST_R), Z(12.5)), (X(FC + s * POST + POST_R), Z(12.5)), (X(FC + s * POST + POST_R), Z(OPEN + BRACKET_T)),
          (X(FC + s * POST - POST_R), Z(OPEN + BRACKET_T))], fill=GREEN, stroke="none", op=0.85)
text(X(FC), Z(12.5) - 8, "posts continue to 21.5 in", 11, DIM, "middle")
for zc, xc in ((R, UP_FACE + R), (4.3, FC), (7.2, FC), (10.1, FC)):
    circle(X(xc), Z(zc), R * K, fill=YEL, stroke="#9a8420")
# the block (centreline section), the rod in it
SL, LA = P.SLANT / IN, P.LAND / IN
poly([(X(TIP), Z(BOTTOM)), (X(TIP), Z(BOTTOM + LA)), (X(TIP + SL), Z(TOP)), (X(TIP + P.FLAT / IN), Z(TOP)), (X(BACK), Z(BOTTOM))], fill=PRINT, stroke="#6d3a92", sw=1.6)
circle(X(ROD_X), Z(ROD_Z), ROD_R * K, fill=ALU, stroke="#5d6670")
# curtains (beyond, either side of the block) and the side wall (beyond)
poly([(X(ROD_X - 0.03), Z(WALL_H)), (X(ROD_X + 0.03), Z(WALL_H)), (X(ROD_X + 0.03), Z(CURT_B)), (X(ROD_X - 0.03), Z(CURT_B))], stroke=DBLUE, sw=2.2, dash="3 3")
poly([(X(TIP), Z(SIDE_H)), (X(FACE), Z(SIDE_H)), (X(FACE), Z(0)), (X(TIP), Z(0))], stroke=DBLUE, sw=1.2, dash="7 5")
for zz in (P.SIDE_Z_LO / IN, P.SIDE_Z_HI / IN):
    line(X(TIP), Z(zz), X(FACE), Z(zz), "#5d6670", 2.2)
# robot
poly([(X(FACE + CH), Z(4.5)), (X(FACE), Z(4.5)), (X(FACE), Z(0.5)), (X(FACE + CH), Z(0.5))], fill=GREY, stroke=INK)
circle(X(FACE + 0.6), Z(1.4), 1.3 * K, fill=ORANGE, stroke="#b2561d")
text(X(FACE + CH / 2), Z(2.6), "chassis (option 3)", 14, INK, "middle")
text(X(FACE + 0.6), Z(3.2) - 4, "intake", 12, ORANGE, "middle", 700)
# labels and dimensions
text(X(GAP / 2 + BACK), Z(SIDE_H) - 8, f"side wall beyond: two shafts, panel between, ~{SIDE_H:g} in", 12, DBLUE, "middle", 700)
text(X(ROD_X) - 10, Z(2.4), "curtains (dotted)", 12, DBLUE, "end")
text(X(UP_FACE - UP_T / 2), Z(OPEN / 2) + 4, "", 11)
dim(X(FACE), Z(-0.9), X(BACK), Z(-0.9), f"{GAP:g} in clear")
dim(X(BACK), Z(-0.9), X(TIP), Z(-0.9), f"{DEPTH:g}")
dim(X(FACE + CH), Z(-2.5), X(TIP), Z(-2.5), f"{CH + FACE - TIP:.1f} in, hook down (R105 limit 24)")
dim(X(RING_FRONT), Z(-1.6), X(UP_FACE), Z(-1.6), "3.57 (manual)")
dim(X(-0.2), Z(0), X(-0.2), Z(RING_T), "0.43", 1)
dim(X(-1.05), Z(0), X(-1.05), Z(OPEN), "3.55 opening", 1)
dim(X(FACE + CH + 0.25), Z(0), X(FACE + CH + 0.25), Z(SIDE_H), f"{SIDE_H:g}", -1)
text(X(UP_FACE - UP_T / 2), Z(OPEN) - BRACKET_T * K - 6, "lower bracket", 11, DIM, "middle")
text(X(UP_FACE - UP_T / 2) - 2, Z(2.2), "uprights", 11, "#55606b", "middle")
scale_bar(X(FACE + CH), Z(-3.1), K)

# ================= 2. TOP VIEW =================
TX0, TY0 = SX0, 1090
Y = lambda y: TY0 + y * K                 # robot's right (+y) at the bottom of the page
text(30, 765, "2 · From above, seated in a FLOWER", 17, INK, w=700)
poly([(X(0), Y(-8.5)), (X(-0.6), Y(-8.5)), (X(-0.6), Y(8.5)), (X(0), Y(8.5))], fill="#d9dee4", stroke="none")
# bottom ring: an octagon (est.), its hole, the uprights, the posts
RO = (RING_FRONT - FC) / math.cos(math.pi / 8)
octp = [(FC + RO * math.cos(math.pi / 8 + k * math.pi / 4), RO * math.sin(math.pi / 8 + k * math.pi / 4)) for k in range(8)]
octp = [(max(0.05, a), b) for a, b in octp]
poly([(X(a), Y(b)) for a, b in octp], fill=BLK, stroke="none", op=0.85)
circle(X(FC), Y(0), HOLE * K, fill="#ffffff", stroke=BLK, sw=1.2)
for s in (-1, 1):
    y0, y1 = s * UP_Y0, s * (UP_Y0 + UP_T)
    poly([(X(UP_FACE), Y(y0)), (X(UP_FACE - UP_T), Y(y0)), (X(UP_FACE - UP_T), Y(y1)), (X(UP_FACE), Y(y1))], fill=UPR, stroke="#7d8792")
for sx in (-1, 1):
    for sy in (-1, 1):
        circle(X(FC + sx * POST), Y(sy * POST), POST_R * K, fill=GREEN, stroke="#2a7a37", dash="3 2")
circle(X(UP_FACE + R), Y(0), R * K, fill=YEL, stroke="#9a8420", dash="4 3")
# the block: the hole's circle, cut at its back edge
pts = []
for i in range(41):
    a = math.pi / 2 + math.pi * i / 40
    x, y = FC + ARC_R * math.cos(a), ARC_R * math.sin(a)
    pts.append((min(x, BACK), y))
pts = [(BACK, -ARC_R), *pts, (BACK, ARC_R)]
poly([(X(a), Y(b)) for a, b in pts], fill=PRINT, stroke="#6d3a92", sw=1.6, op=0.9)
# rod, end blocks, curtains, side wall, chassis
line(X(ROD_X), Y(-HW), X(ROD_X), Y(HW - 0.35), "#5d6670", ROD_R * 2 * K)
for yy in (-HW, HW - 0.35 - 0.55):
    poly([(X(ROD_X + 0.4), Y(yy)), (X(ROD_X - 0.4), Y(yy)), (X(ROD_X - 0.4), Y(yy + 0.55)), (X(ROD_X + 0.4), Y(yy + 0.55))], fill=DBLUE, stroke="none")
for a, b in ((-HW + 0.6, -ARC_R - 0.15), (ARC_R + 0.15, HW - 1.0)):
    line(X(ROD_X + 0.12), Y(a), X(ROD_X + 0.12), Y(b), BLUE, 3.5)
poly([(X(TIP), Y(HW - 0.35)), (X(FACE), Y(HW - 0.35)), (X(FACE), Y(HW)), (X(TIP), Y(HW))], fill=BLUE, stroke=DBLUE)
poly([(X(FACE + CH), Y(-HW)), (X(FACE), Y(-HW)), (X(FACE), Y(HW)), (X(FACE + CH), Y(HW))], fill=GREY, stroke=INK)
poly([(X(FACE + 0.9), Y(-6.5)), (X(FACE), Y(-6.5)), (X(FACE), Y(6.5)), (X(FACE + 0.9), Y(6.5))], fill=ORANGE, stroke="none")
text(X(FACE + CH / 2), Y(0) + 5, "chassis 14.5 × 14.5", 14, INK, "middle")
text(X(BACK + GAP / 2), Y(-1.2), "8 in clear", 15, DIM, "middle", 700)
text(X(BACK + GAP / 2), Y(HW) + 20, "side wall: 2 shafts + panel; corner block at the front, hinge block at the chassis", 12, DBLUE, "middle", 700)
text(X(ROD_X) + 14, Y(-HW + 2.2), "curtains on clips", 12, DBLUE, "start", 700)
text(X(ROD_X) + 14, Y(-HW + 3.0), "8 mm goBILDA shaft", 12, "#5d6670", "start", 700)
text(X(FC), Y(-3.2), "FLOWER: bottom ring, uprights,", 11, BLK, "middle")
text(X(FC), Y(-3.2) + 14, "posts (dashed), POLLEN (dashed)", 11, BLK, "middle")
dim(X(FACE + CH), Y(HW) + 46, X(TIP), Y(HW) + 46, f"{CH + FACE - TIP:.1f} in total")
dim(X(FACE + CH) + 14, Y(-HW), X(FACE + CH) + 14, Y(HW), "14.5", 1)
scale_bar(X(FACE + CH), Y(HW) + 70, K)

# ================= 3. THE BLOCK, 4x the scale =================
K4 = 3 * K
BX0, BZ0 = 1560, 320
bx = lambda x: BX0 - x * K4                # x from the block's tip
bz = lambda z: BZ0 - (z - BOTTOM) * K4
text(1080, 100, "3 · The block (ramp_block.stl), 3 × scale", 17, INK, w=700)
poly([(bx(0), bz(BOTTOM)), (bx(0), bz(BOTTOM + LA)), (bx(SL), bz(TOP)), (bx(P.FLAT / IN), bz(TOP)), (bx(DEPTH), bz(BOTTOM))], fill=PRINT, stroke="#6d3a92", sw=2, op=0.85)
text(bx(SL / 2) + 10, bz((BOTTOM + TOP) / 2) - 2, f"slant {SL:g} in", 12, "#6d3a92", "start", 700)
text(bx(SL / 2) + 10, bz((BOTTOM + TOP) / 2) + 13, f"(~{math.degrees(math.atan2(SL, TOP - BOTTOM - LA)):.0f}°), {LA:g} in strip", 11, "#6d3a92", "start")
circle(bx((P.DEPTH - P.ROD_X) / IN), bz(ROD_Z), (P.ROD_D + P.FIT) / IN / 2 * K4, fill="#ffffff", stroke="#6d3a92", sw=1.5)
line(bx(-0.3), bz(0), bx(DEPTH + 0.2), bz(0), INK, 1.4)
text(bx(DEPTH + 0.2), bz(0) + 16, "tiles", 11, DIM)
poly([(bx(-0.05), bz(RING_T)), (bx(-0.3), bz(RING_T)), (bx(-0.3), bz(0)), (bx(-0.05), bz(0))], fill=UPR, stroke="none")
dim(bx(DEPTH), bz(BOTTOM) + 30, bx(0), bz(BOTTOM) + 30, f"{DEPTH:g} in ({P.DEPTH:.1f} mm)")
dim(bx(0), bz(TOP) - 18, bx(P.FLAT / IN), bz(TOP) - 18, f"flat {P.FLAT / IN:g}")
dim(bx(-0.45), bz(0), bx(-0.45), bz(TOP), f"top {TOP:g} in", 1)
dim(bx(DEPTH) - 14, bz(0), bx(DEPTH) - 14, bz(BOTTOM), f"{BOTTOM:g} in up", -1)
dim(bx(DEPTH) - 14, bz(BOTTOM), bx(DEPTH) - 14, bz(TOP), f"{(TOP - BOTTOM) * IN:.1f} mm", -1)
text(bx((P.DEPTH - P.ROD_X) / IN), bz(ROD_Z) + 36, f"rod bore {P.ROD_D + P.FIT:.2f} mm", 12, "#6d3a92", "middle", 700)
text(bx(-0.18), bz(RING_T) - 8, "upright", 11, DIM, "middle")
# plan of the block
PX0, PY0 = bx(0), 560
text(1080, 400, "From above: the front follows the bottom ring's hole", 14, INK, w=700)
pp = []
for i in range(41):
    a = math.pi / 2 + math.pi * i / 40
    pp.append((min(ARC_R + ARC_R * math.cos(a), DEPTH), ARC_R * math.sin(a)))
pp = [(DEPTH, -ARC_R), *pp, (DEPTH, ARC_R)]
poly([(bx(a), PY0 + b * K4) for a, b in pp], fill=PRINT, stroke="#6d3a92", sw=2, op=0.85)
line(bx((P.DEPTH - P.ROD_X) / IN), PY0 - ARC_R * K4 - 10, bx((P.DEPTH - P.ROD_X) / IN), PY0 + ARC_R * K4 + 10, "#5d6670", 2, "6 4")
dim(bx(DEPTH) - 16, PY0 - ARC_R * K4, bx(DEPTH) - 16, PY0 + ARC_R * K4, f"{2 * ARC_R:.2f} in ({2 * P.ARC_R:.0f} mm)", -1)
text(bx(0.5), PY0 + 4, f"R {ARC_R:.2f} in", 13, "#ffffff", "middle", 700)

# ================= the numbers =================
bx0, by0 = 1060, 770
poly([(bx0, by0), (1740, by0), (1740, 1250), (bx0, 1250)], fill="#fff7ec", stroke="#e0a030", sw=1.5)
rows = [("FLOWER (manual, Fig 9-12)", None),
        ("POLLEN", "2.8 in"), ("Bottom ring thickness (the pocket's wall)", "0.43 in"), ("Bottom ring hole", "2.79 in"),
        ("Retrieval opening, tiles to the lower bracket", "3.55 in"), ("Ring's front edge to the uprights", "3.57 in"),
        ("Robot", None),
        ("Chassis (option 3)", "14.5 × 14.5 in"), ("Clear space, intake to the block", "8 in"),
        ("Block: front to back × tall, bottom up", f"{DEPTH:g} × {TOP - BOTTOM:.1f} in, {BOTTOM:g} in"),
        ("Block's top, front curve and slant", f"{TOP:g} in, R {ARC_R:.2f} in, {P.SLANT / IN:g} in back"),
        ("Shaft, through the block", f"8 mm, {ROD_Z:.2f} in up"),
        ("Curtains, either side of the block", f"{WALL_H:g} in tall"),
        ("Side wall: 2 shafts + panel", f"~{SIDE_H:g} in tall"),
        ("Hook down, front to back", f"{CH + FACE - TIP:.1f} in of 24"),
        ("Stowed: the hook stands up, walls fold back over the chassis", "under 18 in"),
        ("Model: 4 POLLEN out, driven to the uprights", "every case, ~1.3 s")]
y = by0 + 30
for label, val in rows:
    if val is None:
        text(bx0 + 16, y, label, 15, INK, "start", 700)
    else:
        text(bx0 + 24, y, label, 13, INK)
        text(1724, y, val, 13, INK, "end", 700)
    y += 27
text(1060, 1285, f"Why the curtains are {WALL_H:g} in and the side wall ~{SIDE_H:g} in:", 13, INK, "start", 700)
text(1060, 1305, "the curtains pass under the FLOWER's bracket (3.55 in). Stowed, walls fold back over the chassis, so the", 13, DIM)
text(1060, 1323, "side wall could be taller if it clears the chassis and intake. A falling POLLEN's first bounce is", 13, DIM)
text(1060, 1341, "about 13 in high, so no wall stops it. Walls catch the rolling", 13, DIM)
text(1060, 1359, "and low-hopping ones, which needs a wall taller than a POLLEN's middle (1.4 in).", 13, DIM)
o.append("</svg>")
open(os.path.join(REPO, "sim-review", "ramp-hook-final.svg"), "w").write("\n".join(o))
print("ok")
