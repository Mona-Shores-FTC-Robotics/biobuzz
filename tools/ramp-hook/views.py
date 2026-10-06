"""The current ramp hook in three dimensioned views (sim-review/ramp-hook-views.svg; doc/ramp-hook.md).

    python3 tools/ramp-hook/views.py
"""
import math
import os
# Robot frame, inches: x forward from the chassis face (toward the FLOWER), y across (robot's right +), h up.
CH_L, CH_W, GAP, BAR_D, BAR_H, BAR_B, REACH = 14.5, 14.5, 8.0, 1.5, 0.8, 0.5, 2.4
T, WALL_H, WALL_B, CUT, SLV = 0.35, 4.0, 0.5, 6.0, 0.5
HW = CH_W / 2
BAR_F = GAP + BAR_D                       # bar front, 10.75
HOLE_R = 1.6
HOLE_C = BAR_F - REACH + HOLE_R           # hole centre
WALLX = HOLE_C + 2.71                     # field wall
PLATE_C, PLATE_R = HOLE_C - 0.6, 3.3
P = 1.55
BG, INK, DIM, BLUE, DBLUE, STEEL, ORANGE, GREEN, YEL, BLK, GREY = ("#ffffff", "#1b222b", "#6b7682", "#4f8fe0", "#2f5f9e",
    "#8d97a3", "#e8742a", "#3fae4f", "#f1cf3a", "#33373d", "#c3cad2")
o = []
def T_(x, y, s, size=13, fill=INK, anchor="start", w=400):
    o.append(f'<text x="{x:.1f}" y="{y:.1f}" font-size="{size}" fill="{fill}" text-anchor="{anchor}" font-weight="{w}">{s}</text>')
def poly(pts, fill="none", stroke=INK, sw=1.5, dash=None, op=1):
    d = f' stroke-dasharray="{dash}"' if dash else ""
    o.append(f'<polygon points="{" ".join(f"{a:.1f},{b:.1f}" for a, b in pts)}" fill="{fill}" fill-opacity="{op}" stroke="{stroke}" stroke-width="{sw}"{d}/>')
def rect(x0, y0, x1, y1, **k): poly([(x0, y0), (x1, y0), (x1, y1), (x0, y1)], **k)
def circ(x, y, r, fill="none", stroke=INK, sw=1.2, dash=None):
    d = f' stroke-dasharray="{dash}"' if dash else ""
    o.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{r:.1f}" fill="{fill}" stroke="{stroke}" stroke-width="{sw}"{d}/>')
def line(x0, y0, x1, y1, stroke=DIM, sw=1.2, dash=None):
    d = f' stroke-dasharray="{dash}"' if dash else ""
    o.append(f'<line x1="{x0:.1f}" y1="{y0:.1f}" x2="{x1:.1f}" y2="{y1:.1f}" stroke="{stroke}" stroke-width="{sw}"{d}/>')
def dim(x0, y0, x1, y1, label, off=0, col="#b5452f"):
    line(x0, y0, x1, y1, col, 1.2)
    for (a, b) in ((x0, y0), (x1, y1)):
        if abs(y1 - y0) < 1e-6: line(a, b - 5, a, b + 5, col, 1.2)
        else: line(a - 5, b, a + 5, b, col, 1.2)
    mx, my = (x0 + x1) / 2, (y0 + y1) / 2
    if abs(y1 - y0) < 1e-6: T_(mx, my - 6 + off, label, 13, col, "middle", 700)
    else: T_(mx + 8, my + 4 + off, label, 13, col, "start", 700)

W, H = 1500, 1160
o.append(f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" font-family="Helvetica,Arial,sans-serif">')
o.append(f'<rect width="100%" height="100%" fill="{BG}"/>')
T_(30, 40, "Ramp hook: the current best design", 22, INK, w=700)
T_(30, 64, "Inches. Robot sizes are placeholders; FLOWER sizes are estimated from the 6 Oct photo. Mark up anything that doesn't match what you're picturing.", 14, DIM)

# ---------- 1. TOP VIEW ----------
K = 24; OX, OY = 30 + 14 * K, 120 + 10 * K     # x -> right, y -> down (robot's right at the bottom)
X = lambda x: OX + x * K; Y = lambda y: OY + y * K
T_(30, 100, "1 · From above", 17, INK, w=700)
rect(X(-CH_L), Y(-HW), X(0), Y(HW), fill=GREY, stroke=INK)
T_(X(-CH_L / 2), Y(0) + 5, "chassis", 14, INK, "middle")
rect(X(-0.9), Y(-6.5), X(0), Y(6.5), fill=ORANGE, stroke="none")
T_(X(-1.2), Y(-6.5) - 6, "intake", 12, ORANGE, "end", 700)
# FLOWER
octp = [(PLATE_C + PLATE_R * math.cos(math.pi / 8 + k * math.pi / 4), PLATE_R * math.sin(math.pi / 8 + k * math.pi / 4)) for k in range(8)]
poly([(X(a), Y(b)) for a, b in octp], fill=BLK, stroke=BLK, op=0.18, dash="5 4")
circ(X(HOLE_C), Y(0), HOLE_R * K, stroke=BLK, sw=1.5)
circ(X(HOLE_C), Y(0), 1.4 * K, fill=YEL, stroke="#9a8420")
rect(X(HOLE_C - P - 0.8), Y(-P - 0.8), X(HOLE_C + P + 0.8), Y(P + 0.8), stroke=BLK, sw=1.2, dash="3 3")
for sx in (-1, 1):
    for sy in (-1, 1): circ(X(HOLE_C + sx * P), Y(sy * P), 0.65 * K, fill=GREEN, stroke="#2a7a37")
rect(X(WALLX), Y(-9), X(WALLX + 0.6), Y(9), fill="#9aa3ad", stroke="none")
T_(X(WALLX) + 22, Y(-8), "field wall", 12, DIM)
# hook
rect(X(0), Y(HW - T), X(BAR_F), Y(HW), fill=BLUE, stroke=DBLUE)              # arm wall
rect(X(GAP), Y(-HW), X(BAR_F), Y(HW - T), fill=STEEL, stroke="#5d6670", op=0.85)        # bar, full width
for y0, y1 in ((-HW, -CUT / 2), (CUT / 2, HW - T)):
    rect(X(BAR_F - T), Y(y0), X(BAR_F), Y(y1), fill=BLUE, stroke=DBLUE)     # curtains, standing on the bar's front
T_(X(GAP) - 8, Y(-5.6), "triangular bar, full width →", 13, "#4a535d", "end", 700)
T_(X(BAR_F) + 6, Y(-HW + 0.6), "curtain", 12, DBLUE, "start", 700)
T_(X(4), Y(HW) + 22, "arm wall", 12, DBLUE, "middle", 700)
T_(X(BAR_F) + 6, Y(HW - 1.2), "curtain", 12, DBLUE, "start", 700)
T_(X(HOLE_C), Y(-PLATE_R) - 8, "FLOWER: 4 posts, bracket, base plate with the pocket", 12, BLK, "middle")
T_(X(GAP / 2) - 20, Y(1.6), "8 in clear", 14, DIM, "middle", 700)
dim(X(0), Y(-HW) - 34, X(GAP), Y(-HW) - 34, "8.00")
dim(X(GAP), Y(-HW) - 34, X(BAR_F), Y(-HW) - 34, "1.5")
dim(X(-CH_L), Y(-HW) - 34, X(0), Y(-HW) - 34, "14.5 (option 3)")
dim(X(-CH_L), Y(HW) + 48, X(BAR_F), Y(HW) + 48, "24.0 total (R105 limit 24)")
line(X(GAP - 0.9), Y(-CUT / 2), X(GAP - 0.9), Y(CUT / 2), "#b5452f", 1.2)
for yy in (-CUT / 2, CUT / 2): line(X(GAP - 0.9) - 5, Y(yy), X(GAP - 0.9) + 5, Y(yy), "#b5452f", 1.2)
T_(X(GAP - 0.9) - 8, Y(0.5), "gap in the curtains 6.0", 13, "#b5452f", "end", 700)

# ---------- 2. SIDE SECTION ----------
K2 = 34; SX, SY = 90 + 13.5 * K2 - 13.25 * K2 + 760, 520   # place on the right
K2 = 28; SX = 1044; SY = 600
X2 = lambda x: SX + x * K2; H2 = lambda h: SY - h * K2
T_(SX - 60, 100, "2 · From the side, cut through the middle of the gap", 17, INK, w=700)
line(X2(-3), H2(0), X2(WALLX + 0.8), H2(0), INK, 1.5)
rect(X2(WALLX), H2(12), X2(WALLX + 0.6), H2(0), fill="#9aa3ad", stroke="none")
rect(X2(-3), H2(4.4), X2(0), H2(0.4), fill=GREY, stroke=INK)
circ(X2(-0.6), H2(1.5), 1.3 * K2, fill=ORANGE, stroke="#b2561d")
T_(X2(-0.6), H2(3.2) - 4, "intake", 12, ORANGE, "middle", 700)
rect(X2(0), H2(WALL_H), X2(BAR_F), H2(0), stroke=DBLUE, sw=1.2, dash="6 4")
T_(X2(1.0), H2(WALL_H) - 6, "arm wall, beyond (dashed)", 12, DBLUE)
rect(X2(BAR_F - T), H2(WALL_H), X2(BAR_F), H2(BAR_B + BAR_H), stroke=DBLUE, sw=1.4, dash="2 3")
T_(X2(-3), H2(7.0), "Curtains either side of the gap (dotted):", 12, DBLUE, "start", 700)
T_(X2(-3), H2(7.0) + 16, "straight up off the bar's front, 1.3 to 4 in", 12, DBLUE, "start")
# FLOWER section
PL0, PL1 = PLATE_C - PLATE_R, WALLX
rect(X2(PL0), H2(0.25), X2(HOLE_C - HOLE_R), H2(0), fill=BLK, stroke="none")
rect(X2(HOLE_C + HOLE_R), H2(0.25), X2(PL1), H2(0), fill=BLK, stroke="none")
for hc in (1.4, 4.3, 7.2, 10.1): circ(X2(HOLE_C), H2(hc), 1.4 * K2, fill=YEL, stroke="#9a8420")
rect(X2(HOLE_C - P - 0.8), H2(4.6), X2(HOLE_C + P + 0.8), H2(4.0), fill=BLK, stroke="none")
for sx in (-1, 1): rect(X2(HOLE_C + sx * P - 0.65), H2(12), X2(HOLE_C + sx * P + 0.65), H2(4.6), fill=GREEN, stroke="none", op=0.85)
rect(X2(HOLE_C + 1.9 - 0.37), H2(4.0), X2(HOLE_C + 1.9 + 0.37), H2(0.25), fill="#aab2bb", stroke="none")
T_(X2(HOLE_C + P + 1.0), H2(4.3) + 4, "bracket", 12, BLK)
T_(X2(PL0) - 4, H2(0) + 20, "base plate, pocket under the column", 12, BLK, "end")
# the bar, cut
poly([(X2(GAP), H2(BAR_B)), (X2(BAR_F), H2(BAR_B)), (X2(BAR_F), H2(BAR_B + BAR_H))], fill=STEEL, stroke="#4a535d", sw=1.8)
T_(X2(-3), H2(0) + 46, "Triangular bar: 1.5 deep, 0.8 tall (about 28°), flat bottom 0.5 up.", 13, "#4a535d", "start", 700)
T_(X2(-3), H2(0) + 64, "Its flat front face goes 2.4 in past the pocket's edge, under the bottom POLLEN.", 13, "#4a535d", "start", 700)
dim(X2(BAR_F) + 18, H2(BAR_B), X2(BAR_F) + 18, H2(BAR_B + BAR_H), "0.8")
dim(X2(GAP) - 70, H2(0), X2(GAP) - 70, H2(BAR_B), "0.5")
dim(X2(HOLE_C - HOLE_R), H2(-3.2), X2(BAR_F), H2(-3.2), "2.4 in")

# ---------- 3. FRONT VIEW ----------
K3 = 26; FX, FY = 30 + 7.6 * K3, 1060
X3 = lambda y: FX + (y + HW) * K3; H3 = lambda h: FY - h * K3
T_(30, 790, "3 · Looking at the front of the hook", 17, INK, w=700)
line(X3(-HW - 1), H3(0), X3(HW + 1), H3(0), INK, 1.5)
for y0, y1 in ((-HW, -CUT / 2), (CUT / 2, HW - T)):
    rect(X3(y0), H3(WALL_H), X3(y1), H3(BAR_B + BAR_H), fill=BLUE, stroke=DBLUE)
rect(X3(HW - T), H3(WALL_H), X3(HW), H3(0), fill=BLUE, stroke=DBLUE)
rect(X3(-HW), H3(BAR_B + BAR_H), X3(HW - T), H3(BAR_B), fill=STEEL, stroke="#4a535d")
# FLOWER beyond, dashed
rect(X3(-PLATE_R), H3(0.25), X3(PLATE_R), H3(0), stroke=BLK, dash="4 3", sw=1)
rect(X3(-P - 0.8), H3(4.6), X3(P + 0.8), H3(4.0), stroke=BLK, dash="4 3", sw=1)
circ(X3(0), H3(1.4), 1.4 * K3, stroke="#9a8420", dash="4 3")
T_(X3(0), H3(WALL_H) - 30, "the FLOWER beyond (dashed) sits in the cutout", 12, BLK, "middle")
T_(X3(-HW + 2.1), H3(2.6), "curtain", 12, "#ffffff", "middle", 700)
T_(X3(HW - 2.3), H3(2.6), "curtain", 12, "#ffffff", "middle", 700)
T_(X3(HW) + 8, H3(2.0), "arm wall", 12, DBLUE, "start", 700)
T_(X3(-HW), H3(BAR_B) + 22, "triangular bar, full width", 12, "#4a535d", "start", 700)
dim(X3(-CUT / 2), H3(WALL_H) - 12, X3(CUT / 2), H3(WALL_H) - 12, "6.0")
dim(X3(-HW), H3(-1.0), X3(HW), H3(-1.0), "14.5")
T_(X3(-HW), H3(0) + 52, "The bar sits 0.5 off the floor, so it passes over the FLOWER's base plate.", 12, DIM)

# ---------- question box ----------
qx, qy = 760, 820
rect(qx, qy, 1470, 1075, fill="#fff7ec", stroke="#e0a030", sw=1.5)
qs = ["The spec",
      "Bar: steel triangle across the full width, 1.5 deep × 0.8 tall, bottom 0.5 up,",
      "   flat front face topping out at 1.3 in, its top corner rounded ~1/8 in.",
      "Curtains: straight up off the bar's front, 1.3 to 4 in, 6 in gap for the FLOWER.",
      "Side wall: vertical, 4 in. 8 in clear between the intake and the bar.",
      "Size: 14.5 + 8 + 1.5 = 24.0 (option 3 as is). Stowed 18.5: hinge 0.5 in inside.",
      "Model: empties a FLOWER in every case tried, all 4 out in 0.8 to 1.1 s."]
for k, q in enumerate(qs):
    T_(qx + 18, qy + 34 + k * 31, q, 16 if k == 0 else 14, INK, "start", 700 if k in (0, 6) else 400)
o.append("</svg>")
open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "sim-review", "ramp-hook-views.svg"), "w").write("\n".join(o))
