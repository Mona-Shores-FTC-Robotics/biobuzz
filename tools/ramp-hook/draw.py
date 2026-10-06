"""Draws the ramp hook (sim-review/ramp-hook.svg; doc/ramp-hook.md explains it):

  1. from above: the small right hook on option 3 (14.5 in chassis), its tongue on the crossbeam, at a FLOWER;
  2. from the side: the slice ramp.py simulates, at its best guess (12 deg, tip 2.4 in in, ring 0.43 in);
  3. four moments of that run, as ramp.py simulates them.

    python3 tools/ramp-hook/draw.py [out.svg]

Every FLOWER size is from the Competition Manual (§9.7, Fig 9-12). Grey dashed labels mark what isn't measured or
published. The hook is design 14's 8 in arm on option 3 (README "On option 3, the baseline robot" on
claude/biobuzz-robot-body-designs-hi386c).
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(__file__))
import ramp  # noqa: E402

REPO = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
OUT = sys.argv[1] if len(sys.argv) > 1 else os.path.join(REPO, "sim-review", "ramp-hook.svg")

BG, PANEL, INK, DIM = "#111318", "#1b1e25", "#e8eaee", "#9aa1ad"
ROBOT, GUIDE, FLOWER_C, POLLEN, ORANGE, AMBER = "#545b69", "#60a5fa", "#e0b04a", "#f5d76e", "#ff8a1f", "#f5b041"

SLOPE, DEPTH, RING = 12.0, 2.4, 0.43
CHASSIS, ARM, TONGUE_W = 14.5, 8.0, 2.0

o = []


def text(x, y, s, size=13, fill=INK, anchor="start", bold=False):
    w = ' font-weight="600"' if bold else ""
    o.append(f'<text x="{x:.1f}" y="{y:.1f}" font-size="{size}" fill="{fill}" text-anchor="{anchor}"{w}>{s}</text>')


def line(x1, y1, x2, y2, stroke=INK, width=1.5, dash=None):
    d = f' stroke-dasharray="{dash}"' if dash else ""
    o.append(f'<line x1="{x1:.1f}" y1="{y1:.1f}" x2="{x2:.1f}" y2="{y2:.1f}" stroke="{stroke}" stroke-width="{width}"{d} stroke-linecap="round"/>')


def rect(x, y, w, h, fill="none", stroke="none", width=1.5, dash=None, opacity=1.0):
    d = f' stroke-dasharray="{dash}"' if dash else ""
    o.append(f'<rect x="{x:.1f}" y="{y:.1f}" width="{w:.1f}" height="{h:.1f}" fill="{fill}" fill-opacity="{opacity}" stroke="{stroke}" stroke-width="{width}"{d}/>')


def circle(x, y, r, fill="none", stroke="none", width=1.5, dash=None):
    d = f' stroke-dasharray="{dash}"' if dash else ""
    o.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{r:.1f}" fill="{fill}" stroke="{stroke}" stroke-width="{width}"{d}/>')


def poly(pts, stroke=INK, width=2.0, fill="none"):
    p = " ".join(f"{x:.1f},{y:.1f}" for x, y in pts)
    o.append(f'<polyline points="{p}" fill="{fill}" stroke="{stroke}" stroke-width="{width}" stroke-linejoin="round"/>')


def side_scene(ox, oy, k, tip_x, balls, x_max=14.0, labels=False):
    """The slice: wall at ox, tiles at oy, k px per inch; x from the wall, z up."""
    X = lambda x: ox + x * k
    Z = lambda z: oy - z * k
    line(X(0), Z(0), X(x_max), Z(0), DIM, 1.2)                       # tiles
    top = 12.0
    rect(X(-0.6), Z(top), 0.6 * k, top * k, fill="#3a3f4a")            # wall
    back, front = ramp.BACK, ramp.FRONT
    rect(X(back - 0.15), Z(top), 0.15 * k, top * k, fill=FLOWER_C)     # tube, back
    rect(X(front), Z(top), 0.15 * k, (top - ramp.WINDOW_TOP) * k, fill=FLOWER_C)  # tube, front above the window
    rect(X(front), Z(RING), ramp.RING_T * k, RING * k, fill=FLOWER_C)  # bottom ring
    line(X(back), Z(0), X(front), Z(0), FLOWER_C, 2)
    if tip_x is not None:
        pts = [(tip_x + x, z) for x, z in ramp.ramp_profile(RING, math.radians(SLOPE), DEPTH)]
        poly([(X(x), Z(z)) for x, z in pts], GUIDE, 3)
        intake = tip_x + ramp.INTAKE_BEHIND_TIP
        if intake < x_max + 1:
            rect(X(intake), Z(3.2), 1.0 * k, 3.2 * k, fill=ORANGE, opacity=0.9)
    for x, z in balls:
        circle(X(x), Z(z), ramp.R * k, fill=POLLEN, stroke="#8a7a2a", width=1)
    if labels:
        text(X(front) + 10, Z(3.0), "retrieval opening 3.55 in", 12, DIM)
        text(X(front) + 10, Z(6.0), "staged POLLEN, centres 1.4 / 4.3 / 7.2 / 10.1 in (CAD)", 12, DIM)
        text(X(front) + 10, Z(10.0), "tube 3.2 in inside (a guess)", 12, DIM)
        intake = tip_x + ramp.INTAKE_BEHIND_TIP
        text(X(intake) + 0.5 * k, Z(3.2) - 8, f"intake, {ramp.INTAKE_BEHIND_TIP:g} in behind the tip", 12, ORANGE, "middle")
        tz = ramp.tip_height(RING, math.radians(SLOPE), DEPTH)
        text(X(front), Z(0) + 20, f"tongue {SLOPE:g}°, tip {DEPTH} in past the opening's edge and {tz:.2f} in up; "
             "it rests on the ring and runs down to the hook's floor plate", 12, GUIDE)
        text(X(front), Z(0) + 37, f"bottom ring {RING} in tall (manual); its thickness is a guess", 12, DIM)


def top_view(ox, oy, k):
    """From above, front up: wall at the top, the FLOWER, the hook down."""
    X = lambda x: ox + x * k
    Y = lambda y: oy + y * k           # y from the wall, down the page
    rect(X(-12), Y(-0.6), 24 * k, 0.6 * k, fill="#3a3f4a")
    text(X(0), Y(-0.6) - 6, "wall", 12, DIM, "middle")
    circle(X(0), Y(ramp.CX), ramp.TUBE_R * k + 0.15 * k, stroke=FLOWER_C, width=3)
    circle(X(0), Y(ramp.CX), ramp.R * k, fill=POLLEN, stroke="#8a7a2a", width=1)
    beam = ramp.FRONT + ramp.RING_T                       # the crossbeam's face against the ring
    tip = ramp.FRONT - DEPTH
    rect(X(-TONGUE_W / 2), Y(tip), TONGUE_W * k, (beam - tip) * k, fill=GUIDE, opacity=0.5, stroke=GUIDE)
    # Crossbeam across the chassis, the right arm back to the chassis' front face, then the chassis.
    half = CHASSIS / 2
    line(X(-half), Y(beam), X(half), Y(beam), GUIDE, 5)
    line(X(half), Y(beam), X(half), Y(beam + ARM), GUIDE, 5)
    rect(X(-half), Y(beam + ARM), CHASSIS * k, CHASSIS * k, fill=ROBOT, stroke="#a3abb9", width=2)
    rect(X(-6), Y(beam + ARM) + 3, 12 * k, 1.2 * k, fill=ORANGE)
    text(X(0), Y(beam + ARM + CHASSIS / 2), "option 3, 14.5 in", 13, INK, "middle")
    text(X(half) + 8, Y(beam + ARM / 2), f"arm {ARM:g} in, inside wall", 12, GUIDE)
    text(X(half) + 8, Y(beam + ARM / 2) + 15, "leaning in 30° (E)", 12, GUIDE)
    text(X(-half), Y(beam) - 8, "crossbeam", 12, GUIDE)
    text(X(TONGUE_W / 2) + 6, Y(tip) + 4, f"tongue {TONGUE_W:g} in wide (a guess)", 12, GUIDE)
    total = CHASSIS + ARM + (beam - tip)
    colour = "#f2545b" if total > 24 else AMBER
    x = X(-half) - 18
    line(x, Y(tip), x, Y(beam + ARM + CHASSIS), colour, 1.4)
    text(x - 6, Y((tip + beam + ARM + CHASSIS) / 2), f"{total:.1f} in", 13, colour, "end", True)
    text(x - 6, Y((tip + beam + ARM + CHASSIS) / 2) + 16, "R105 allows 24", 12, colour, "end")


W, H = 1500, 850
o.append(f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}" '
         f'font-family="Helvetica,Arial,sans-serif">')
o.append(f'<rect width="100%" height="100%" fill="{BG}"/>')
text(24, 34, "The ramp hook: the small right hook's crossbeam as a tongue into a FLOWER's retrieval opening", 18, INK, bold=True)
text(24, 56, "Not measured: every robot number, the tube's inside, the ring's thickness, POLLEN bounce and friction. "
             "From the manual: the opening, the ring height, the staged POLLEN.", 13, DIM)

rect(16, 72, 520, 762, fill=PANEL)
text(32, 98, "1 · From above", 15, INK, bold=True)
top_view(276, 140, 17)

rect(552, 72, 932, 430, fill=PANEL)
text(568, 98, "2 · From the side, through the FLOWER's centre (tongue in, before the POLLEN move)", 15, INK, bold=True)
tip_final = ramp.FRONT - DEPTH
side_scene(600, 432, 26, tip_final, [(ramp.CX, z) for z in ramp.STAGED], x_max=33, labels=True)

trace = []
res = ramp.run(slope_deg=SLOPE, tip_depth=DEPTH, ring=RING, trace=trace)
rect(552, 518, 932, 316, fill=PANEL)
text(568, 544, f"3 · ramp.py's run (e 0.4, mu 0.4, drive in at 12 in/s): all 4 out at {res[0]:.2f} s, "
               f"the last at the intake at {res[1]:.2f} s", 15, INK, bold=True)
for i, want in enumerate((0.25, 0.45, 0.7, 0.95)):
    t, tip, balls = min(trace, key=lambda f: abs(f[0] - want))
    ox = 590 + i * 228
    side_scene(ox, 810, 17, tip, balls, x_max=12)
    text(ox + 100, 574, f"{t:.2f} s", 14, INK, "middle", True)

o.append("</svg>")
os.makedirs(os.path.dirname(OUT), exist_ok=True)
open(OUT, "w").write("\n".join(o))
print(OUT)
