"""The current ramp hook in three dimensioned views (sim-review/ramp-hook-views.svg; doc/ramp-hook.md).

    python3 tools/ramp-hook/views.py

Version 2: a vertical side wall and curtains; in the curtains' gap, two cheeks carry a short 3D-printed triangular
insert and a stop at the FLOWER's bracket height. Driving in until the stop meets the bracket sets the depth.
"""
import math
import os

# Robot frame, inches: x forward from the chassis face (toward the FLOWER), y across (robot's right +), h up.
CH_L, CH_W, GAP = 14.5, 14.5, 8.0
INS_D, INS_H, INS_B, REACH = 1.25, 0.8, 0.5, 2.1      # the printed insert, and how far past the pocket's edge when seated
T, WALL_H, CUT = 0.35, 4.0, 5.2                          # wall thickness, side wall height, gap between the cheeks
STRIP_D, STRIP_H, CURT_TOP, CHEEK_TOP = 0.75, 0.25, 3.9, 4.65
HW = CH_W / 2
FRONT = GAP + INS_D                                      # the hook's front, 9.25
HOLE_R = 1.6
HOLE_C = FRONT - REACH + HOLE_R                          # the pocket's centre when seated
WALLX = HOLE_C + 2.71                                    # field wall
PLATE_C, PLATE_R = HOLE_C - 0.6, 3.3
P = 1.55                                                 # posts on a square round the column
BRACKET_HALF = P + 0.8
STOP_X = HOLE_C - BRACKET_HALF                           # where the bracket's front face meets the stop
BG, INK, DIM, BLUE, DBLUE, ALU, PRINT, ORANGE, GREEN, YEL, BLK, GREY = (
    "#ffffff", "#1b222b", "#6b7682", "#4f8fe0", "#2f5f9e", "#aab3bd", "#b06ad8", "#e8742a", "#3fae4f", "#f1cf3a",
    "#33373d", "#c3cad2")
RED = "#b5452f"
o = []


def T_(x, y, s, size=13, fill=INK, anchor="start", w=400):
    o.append(f'<text x="{x:.1f}" y="{y:.1f}" font-size="{size}" fill="{fill}" text-anchor="{anchor}" font-weight="{w}">{s}</text>')


def poly(pts, fill="none", stroke=INK, sw=1.5, dash=None, op=1):
    d = f' stroke-dasharray="{dash}"' if dash else ""
    o.append(f'<polygon points="{" ".join(f"{a:.1f},{b:.1f}" for a, b in pts)}" fill="{fill}" fill-opacity="{op}" '
             f'stroke="{stroke}" stroke-width="{sw}"{d}/>')


def rect(x0, y0, x1, y1, **k):
    poly([(x0, y0), (x1, y0), (x1, y1), (x0, y1)], **k)


def circ(x, y, r, fill="none", stroke=INK, sw=1.2, dash=None):
    d = f' stroke-dasharray="{dash}"' if dash else ""
    o.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{r:.1f}" fill="{fill}" stroke="{stroke}" stroke-width="{sw}"{d}/>')


def line(x0, y0, x1, y1, stroke=DIM, sw=1.2, dash=None):
    d = f' stroke-dasharray="{dash}"' if dash else ""
    o.append(f'<line x1="{x0:.1f}" y1="{y0:.1f}" x2="{x1:.1f}" y2="{y1:.1f}" stroke="{stroke}" stroke-width="{sw}"{d}/>')


def dim(x0, y0, x1, y1, label, col=RED, anchor=None):
    line(x0, y0, x1, y1, col, 1.2)
    horiz = abs(y1 - y0) < 1e-6
    for a, b in ((x0, y0), (x1, y1)):
        line(a, b - 5, a, b + 5, col, 1.2) if horiz else line(a - 5, b, a + 5, b, col, 1.2)
    mx, my = (x0 + x1) / 2, (y0 + y1) / 2
    if horiz:
        T_(mx, my - 6, label, 13, col, "middle", 700)
    elif anchor == "end":
        T_(mx - 8, my + 4, label, 13, col, "end", 700)
    else:
        T_(mx + 8, my + 4, label, 13, col, "start", 700)


W, H = 1500, 1160
o.append(f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" font-family="Helvetica,Arial,sans-serif">')
o.append(f'<rect width="100%" height="100%" fill="{BG}"/>')
T_(30, 40, "Ramp hook v2: drive in until the stop seats on the FLOWER's bracket", 22, INK, w=700)
T_(30, 64, "Inches. Robot sizes are placeholders; FLOWER sizes are estimated from the 6 Oct photo, scaled off the green pipe.", 14, DIM)

# ---------- 1. TOP VIEW ----------
K = 25
OX, OY = 30 + 14.8 * K, 130 + 9 * K                     # x to the right, y down the page (robot's right at the bottom)
X = lambda x: OX + x * K
Y = lambda y: OY + y * K
T_(30, 100, "1 · From above, seated against a FLOWER", 17, INK, w=700)
rect(X(-CH_L), Y(-HW), X(0), Y(HW), fill=GREY, stroke=INK)
T_(X(-CH_L / 2), Y(0) + 5, "chassis (option 3)", 14, INK, "middle")
rect(X(-0.9), Y(-6.5), X(0), Y(6.5), fill=ORANGE, stroke="none")
T_(X(-1.2), Y(-6.5) - 6, "intake", 12, ORANGE, "end", 700)
# FLOWER
octp = [(PLATE_C + PLATE_R * math.cos(math.pi / 8 + k * math.pi / 4), PLATE_R * math.sin(math.pi / 8 + k * math.pi / 4)) for k in range(8)]
poly([(X(a), Y(b)) for a, b in octp], fill=BLK, stroke=BLK, op=0.12, dash="5 4")
circ(X(HOLE_C), Y(0), HOLE_R * K, stroke=BLK, sw=1.5)
circ(X(HOLE_C), Y(0), 1.4 * K, fill=YEL, stroke="#9a8420")
rect(X(HOLE_C - BRACKET_HALF), Y(-BRACKET_HALF), X(HOLE_C + BRACKET_HALF), Y(BRACKET_HALF), stroke=BLK, sw=1.4, dash="4 3")
for sx in (-1, 1):
    for sy in (-1, 1):
        circ(X(HOLE_C + sx * P), Y(sy * P), 0.65 * K, fill=GREEN, stroke="#2a7a37")
rect(X(WALLX), Y(-9), X(WALLX + 0.6), Y(9), fill="#9aa3ad", stroke="none")
T_(X(WALLX) + 22, Y(-8.3), "field wall", 12, DIM)
# hook
rect(X(0), Y(HW - T), X(FRONT), Y(HW), fill=BLUE, stroke=DBLUE)                       # side wall
for y0, y1 in ((-HW, -CUT / 2 - T), (CUT / 2 + T, HW - T)):
    rect(X(FRONT - STRIP_D), Y(y0), X(FRONT), Y(y1), fill=ALU, stroke="#7d8792")       # flat strip
    rect(X(FRONT - T), Y(y0), X(FRONT), Y(y1), fill=BLUE, stroke=DBLUE)               # curtain on its front
for s in (-1, 1):
    rect(X(STOP_X - 0.4), Y(s * CUT / 2), X(FRONT), Y(s * (CUT / 2 + T)), fill=DBLUE, stroke=DBLUE)   # cheeks
rect(X(GAP), Y(-CUT / 2), X(FRONT), Y(CUT / 2), fill=PRINT, stroke="#7a3fa0", op=0.85)              # insert
rect(X(STOP_X - 0.4), Y(-CUT / 2), X(STOP_X), Y(CUT / 2), fill=DBLUE, stroke=DBLUE)                 # stop
T_(X(GAP / 2) - 30, Y(1.8), "8 in clear", 14, DIM, "middle", 700)
T_(X(2.6), Y(-4.6), "stop", 13, DBLUE, "end", 700)
line(X(2.7), Y(-4.75), X(STOP_X - 0.2), Y(-CUT / 2 + 0.3), DBLUE, 1.2)
T_(X(2.6), Y(-3.4), "printed insert", 13, "#7a3fa0", "end", 700)
line(X(2.7), Y(-3.55), X(GAP + 0.4), Y(-CUT / 2 + 0.9), "#7a3fa0", 1.2)
T_(X(FRONT) + 6, Y(-HW + 0.6), "curtain + strip", 12, DBLUE, "start", 700)
T_(X(FRONT) + 6, Y(HW - 1.0), "curtain + strip", 12, DBLUE, "start", 700)
T_(X(FRONT) + 6, Y(CUT / 2 + T) + 16, "cheek", 12, DBLUE, "start", 700)
T_(X(4), Y(HW) + 22, "side wall", 12, DBLUE, "middle", 700)
T_(X(HOLE_C), Y(-PLATE_R) - 10, "FLOWER: bracket (dashed square) seats on the stop", 12, BLK, "middle")
dim(X(-CH_L), Y(-HW) - 34, X(0), Y(-HW) - 34, "14.5")
dim(X(0), Y(-HW) - 34, X(GAP), Y(-HW) - 34, "8.00")
dim(X(GAP), Y(-HW) - 34, X(FRONT), Y(-HW) - 34, "1.25")
dim(X(-CH_L), Y(HW) + 48, X(FRONT), Y(HW) + 48, f"{CH_L + FRONT:.2f} total (R105 limit 24)")
dim(X(STOP_X), Y(HW) + 16, X(FRONT), Y(HW) + 16, f"{FRONT - STOP_X:.2f}")

# ---------- 2. SIDE SECTION ----------
K2 = 30
SX, SY = 1050, 620
X2 = lambda x: SX + x * K2
H2 = lambda h: SY - h * K2
T_(SX - 70, 100, "2 · From the side, cut through the middle of the gap", 17, INK, w=700)
line(X2(-2.6), H2(0), X2(WALLX + 0.8), H2(0), INK, 1.5)
rect(X2(WALLX), H2(12), X2(WALLX + 0.6), H2(0), fill="#9aa3ad", stroke="none")
rect(X2(-2.6), H2(4.4), X2(0), H2(0.4), fill=GREY, stroke=INK)
circ(X2(-0.6), H2(1.5), 1.3 * K2, fill=ORANGE, stroke="#b2561d")
T_(X2(-0.6), H2(3.3) - 4, "intake", 12, ORANGE, "middle", 700)
rect(X2(0), H2(WALL_H), X2(FRONT), H2(0), stroke=DBLUE, sw=1.2, dash="6 4")
T_(X2(0.3), H2(WALL_H) - 6, "side wall, beyond", 12, DBLUE)
rect(X2(STOP_X - 0.4), H2(CHEEK_TOP), X2(FRONT), H2(INS_B), stroke=DBLUE, sw=1.4, dash="2 3")
T_(X2(-2.6), H2(7.6), "Cheeks either side of the gap (dotted),", 12, DBLUE, "start", 700)
T_(X2(-2.6), H2(7.6) + 16, "0.5 to 4.65 in, carry the insert and the stop", 12, DBLUE, "start")
# FLOWER
PL0 = PLATE_C - PLATE_R
rect(X2(PL0), H2(0.25), X2(HOLE_C - HOLE_R), H2(0), fill=BLK, stroke="none")
rect(X2(HOLE_C + HOLE_R), H2(0.25), X2(WALLX), H2(0), fill=BLK, stroke="none")
for hc in (1.4, 4.3, 7.2, 10.1):
    circ(X2(HOLE_C), H2(hc), 1.4 * K2, fill=YEL, stroke="#9a8420")
rect(X2(HOLE_C - BRACKET_HALF), H2(4.6), X2(HOLE_C + BRACKET_HALF), H2(4.0), fill=BLK, stroke="none")
for sx in (-1, 1):
    rect(X2(HOLE_C + sx * P - 0.65), H2(12), X2(HOLE_C + sx * P + 0.65), H2(4.6), fill=GREEN, stroke="none", op=0.85)
rect(X2(HOLE_C + 1.9 - 0.37), H2(4.0), X2(HOLE_C + 1.9 + 0.37), H2(0.25), fill="#aab2bb", stroke="none")
T_(X2(HOLE_C + BRACKET_HALF) + 6, H2(4.3) + 4, "bracket", 12, BLK)
# the stop, cut, touching the bracket's front
rect(X2(STOP_X - 0.4), H2(4.6), X2(STOP_X), H2(4.0), fill=DBLUE, stroke=DBLUE)
T_(X2(STOP_X - 0.2), H2(5.3), "stop", 12, DBLUE, "middle", 700)
line(X2(STOP_X - 0.2), H2(5.15), X2(STOP_X - 0.2), H2(4.65), DBLUE, 1.2)
# the insert, cut
poly([(X2(GAP), H2(INS_B)), (X2(FRONT), H2(INS_B)), (X2(FRONT), H2(INS_B + INS_H))], fill=PRINT, stroke="#7a3fa0", sw=1.8)
T_(X2(-2.6), H2(0) + 46, "Printed insert: 1.25 deep, 0.8 tall, bottom 0.5 up, front face tops out at 1.3.", 13, "#7a3fa0", "start", 700)
T_(X2(-2.6), H2(0) + 64, "Seated, its front is 2.1 in past the pocket's edge (1.9 to 2.3 still works).", 13, "#7a3fa0", "start", 700)
dim(X2(FRONT) + 16, H2(INS_B), X2(FRONT) + 16, H2(INS_B + INS_H), "0.8")
dim(X2(HOLE_C - HOLE_R), H2(-3.0), X2(FRONT), H2(-3.0), "2.1")
T_(X2(PL0) - 6, H2(0) + 18, "base plate", 12, BLK, "end")

# ---------- 3. FRONT VIEW ----------
K3 = 27
FX, FY = 40 + 7.6 * K3, 1065
X3 = lambda y: FX + (y + HW) * K3
H3 = lambda h: FY - h * K3
T_(30, 800, "3 · Looking at the front of the hook", 17, INK, w=700)
line(X3(-HW - 1), H3(0), X3(HW + 1), H3(0), INK, 1.5)
for y0, y1 in ((-HW, -CUT / 2 - T), (CUT / 2 + T, HW - T)):
    rect(X3(y0), H3(INS_B + STRIP_H), X3(y1), H3(INS_B), fill=ALU, stroke="#7d8792")
    rect(X3(y0), H3(CURT_TOP), X3(y1), H3(INS_B + STRIP_H), fill=BLUE, stroke=DBLUE)
rect(X3(HW - T), H3(WALL_H), X3(HW), H3(0), fill=BLUE, stroke=DBLUE)
for s in (-1, 1):
    a, b = s * CUT / 2, s * (CUT / 2 + T)
    rect(X3(min(a, b)), H3(CHEEK_TOP), X3(max(a, b)), H3(INS_B), fill=DBLUE, stroke=DBLUE)
rect(X3(-CUT / 2), H3(4.6), X3(CUT / 2), H3(4.0), fill=DBLUE, stroke=DBLUE, op=0.55)          # stop, set back
rect(X3(-CUT / 2), H3(INS_B + INS_H), X3(CUT / 2), H3(INS_B), fill=PRINT, stroke="#7a3fa0")   # insert
circ(X3(0), H3(1.4), 1.4 * K3, stroke="#9a8420", dash="4 3")
rect(X3(-PLATE_R), H3(0.25), X3(PLATE_R), H3(0), stroke=BLK, dash="4 3", sw=1)
T_(X3(-HW + 2.1), H3(2.4), "curtain", 12, "#ffffff", "middle", 700)
T_(X3(HW - 2.3), H3(2.4), "curtain", 12, "#ffffff", "middle", 700)
T_(X3(0), H3(4.6) - 8, "stop (set back 2.85 in)", 12, DBLUE, "middle", 700)
T_(X3(0), H3(INS_B) + 22, "printed insert", 12, "#7a3fa0", "middle", 700)
T_(X3(HW) + 8, H3(2.0), "side wall", 12, DBLUE, "start", 700)
dim(X3(-CUT / 2), H3(CHEEK_TOP) - 26, X3(CUT / 2), H3(CHEEK_TOP) - 26, f"{CUT:.1f} between the cheeks")
dim(X3(-HW), H3(-1.4), X3(HW), H3(-1.4), "14.5")

# ---------- spec box ----------
qx, qy = 780, 800
rect(qx, qy, 1470, 1095, fill="#fff7ec", stroke="#e0a030", sw=1.5)
qs = ["The spec",
      "Stop: a crossbar between the cheeks at the bracket's height (4.0 to 4.6 in).",
      "   Drive in until it seats: that sets the depth and centres the robot.",
      "Insert: 3D printed, 5.2 wide, 1.25 deep × 0.8 tall, only in the gap.",
      "   Model: seated at 2.1 in it empties a FLOWER, all 4 out in about 1.0 s;",
      "   29 of 30 runs still empty anywhere from 1.9 to 2.3 in.",
      "Elsewhere: a flat strip at the curtains' feet; curtains 0.75 to 3.9 in.",
      "Size: 14.5 + 8 + 1.25 = 23.75 of 24. Stowed ≈ 19.2: hinge ~1.2 in inside.",
      "Measure: the bracket's front to the pocket's edge (sets the stop)."]
for k, q in enumerate(qs):
    T_(qx + 18, qy + 32 + k * 30, q, 16 if k == 0 else 14, INK, "start", 700 if k in (0, 8) else 400)
o.append("</svg>")
open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "sim-review", "ramp-hook-views.svg"), "w").write("\n".join(o))
