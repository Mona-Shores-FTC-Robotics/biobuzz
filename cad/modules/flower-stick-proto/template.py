"""Full-size paper template for cutting the FLOWER block by hand (no printer): print at 100 %, check the 1 in square,
tape it on 3/4 in plywood, hardwood or HDPE. Plan (top) and side views, inches. Writes block_template.pdf.

    python3 cad/modules/flower-stick-proto/template.py
"""
import math, os, numpy as np, matplotlib
matplotlib.use("Agg"); import matplotlib.pyplot as plt
HERE = os.path.dirname(os.path.abspath(__file__))
DEPTH, FLAT, ARC_R, ROD_BACK = 1.4, 0.5, 1.36, 12 / 25.4
BOTTOM, H = 0.65, 0.75              # 3/4 in stock: bottom 0.65 in off the tiles, top 1.40
SHAFT_Z = 0.96                      # the shaft's height off the tiles (the grid plates set it)
fig = plt.figure(figsize=(8.5, 11))
ax = fig.add_axes([0, 0, 1, 1]); ax.set_xlim(0, 8.5); ax.set_ylim(0, 11); ax.axis("off")
T = dict(fontsize=9, va="top")
ax.text(0.6, 10.5, "FLOWER block: cut-it-yourself template (issue #179)", fontsize=13, weight="bold", va="top")
ax.text(0.6, 10.1, "Print at 100 % / actual size. The square must measure 1 in.\n"
        "Stock: 3/4 in plywood, hardwood or HDPE, 0.75 in thick. On the robot that puts the bottom at 0.65 in and\n"
        "the top at 1.40 in. Sand the top down to 1.35 if you can.", **T)
ax.add_patch(plt.Rectangle((6.6, 7.6), 1, 1, fill=False, lw=1)); ax.text(6.65, 7.5, "1 in", **T)
# plan view: the curved front (radius 1.36 in), back edge straight
ox, oy = 2.2, 5.0                   # plan: x forward (right on the page), y across
th = np.linspace(-math.pi / 2, math.pi / 2, 200)
cx = ox + DEPTH - ARC_R
xs = cx + ARC_R * np.cos(th); ys = oy + ARC_R * np.sin(th)
keep = xs >= ox
xs, ys = xs[keep], ys[keep]
ax.plot(np.r_[ox, xs, ox, ox], np.r_[ys[0], ys, ys[-1], ys[0]], "k", lw=1.2)
sx = ox + DEPTH - ROD_BACK
ax.plot([sx, sx], [ys.min() - 0.3, ys.max() + 0.3], "k-.", lw=0.6)
ax.text(ox - 0.1, oy + 1.9, "TOP VIEW (plan): cut this outline", fontsize=10, weight="bold")
ax.text(ox + DEPTH + 0.2, oy, "front: curved, radius 1.36 in\n(it nests between the FLOWER's\ngrey uprights)", fontsize=8, va="center")
ax.text(ox - 1.9, oy, "back edge\n(toward the\nrobot)", fontsize=8, va="center")
ax.text(sx + 0.1, ys.min() - 0.1, "shaft line, 0.47 in behind the tip", fontsize=7, va="top")
# side view
sx0, sz0 = 2.2, 1.6
side = [(0, 0), (DEPTH - FLAT, H), (DEPTH, H), (DEPTH, 0), (0, 0)]
ax.plot([sx0 + p[0] for p in side], [sz0 + p[1] for p in side], "k", lw=1.2)
ax.plot([sx0 - 0.3, sx0 + DEPTH + 0.3], [sz0 + H, sz0 + H], color="0.6", lw=0.5, ls=":")
hz = SHAFT_Z - BOTTOM
ax.add_patch(plt.Circle((sx0 + DEPTH - ROD_BACK, sz0 + hz), 4.15 / 25.4, fill=False, lw=1))
ax.plot([sx0 + DEPTH - ROD_BACK] * 2, [sz0 - 0.15, sz0 + H + 0.15], "k-.", lw=0.5)
ax.text(sx0 - 0.1, sz0 + 1.5, "SIDE VIEW: slope the back down to the bottom edge", fontsize=10, weight="bold")
ax.text(sx0 + DEPTH + 0.2, sz0 + 0.35,
        f"8 mm hole straight across, {hz:.2f} in up from the bottom,\n0.47 in behind the front.\n"
        "Drill 8 mm or 5/16 in (snug on the REX shaft).\n"
        "Flat top 0.5 in, then the slope to the back.", fontsize=8, va="center")
ax.text(0.6, 0.9, "Lock it on the shaft: a #6 wood screw (or M3) up from the bottom into one of the shaft's flats, plus the two\n"
        "collars either side. Square the flat top to the tiles before tightening.", fontsize=8, va="top")
fig.savefig(os.path.join(HERE, "block_template.pdf"))
