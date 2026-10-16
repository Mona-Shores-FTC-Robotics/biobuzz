"""Three blade profiles at their stop in the FLOWER's slice, with the pinned POLLEN resting on each: Block A as driven
on 15 Oct (steep face first, ramp toward the robot), the v2 lane, and v3 the mountain. Writes profiles_compare.png.

    python3 cad/modules/flower-stick-proto/profiles_compare.py
"""
import math, os
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import Circle, Polygon
from stack_sequence import draw_flower
HERE = os.path.dirname(os.path.abspath(__file__))
R, PIN = 1.4, 0.35
# (name, stop x in the FLOWER frame, profile x behind the lip / z, note)
BLADES = [
    ("Block A, 15 Oct: steep face first, ramp toward the robot", 1.75, [(0.0, 0.52), (0.0, 0.64), (0.2, 1.30), (0.7, 1.30), (1.4, 0.52)],
     "face leans 17°: stalls.\nIts curved tip passes the uprights' points, so it stops 0.57 in deeper,\nwith the ball over the ramp's foot"),
    ("v3 mountain: 55° nose, peak 1.28, 45° down", 1.18, [(0.0, 0.52), (0.0, 0.53), (0.53, 1.28), (1.29, 0.52)],
     "one profile the full width; peak 0.30 in past the pinned ball;\nthe ball drops into the hole and climbs the ring"),
    ("v4 carry: v3 with a 14° tail to the ring", 1.18, [(0.0, 0.52), (0.0, 0.53), (0.53, 1.28), (3.54, 0.52)],
     "the ball rides the blade to the Gecko; margin still 0.30:\na stop 0.3 in short leaves the ball on the peak"),
    ("v5, searched: 60° nose, peak 1.10, 12° tail", 1.18, [(0.0, 0.52), (0.0, 0.60), (0.29, 1.10), (3.02, 0.52)],
     "best of 882 shapes over 12 scenarios: margin 0.54,\nedge at 0.60 meets the ball at 35°, feeds in 2.6 s worst case"),
]


def rest_height(pts, cx):
    """The height of a ball of radius R centred at x = cx resting on the polyline pts (FLOWER frame) or the tiles."""
    best = R
    for (ax, az), (bx, bz) in zip(pts, pts[1:]):
        ex, ez = bx - ax, bz - az; L2 = ex * ex + ez * ez
        # the ball touches the segment: find the centre height z such that the distance from (cx, z) to the segment is R
        for z in [R + i * 0.005 for i in range(0, 400)]:
            t = max(0.0, min(1.0, ((cx - ax) * ex + (z - az) * ez) / L2)); px, pz = ax + t * ex, az + t * ez
            if math.hypot(cx - px, z - pz) >= R:
                best = max(best, z); break
    return best


fig, axes = plt.subplots(2, 2, figsize=(11, 8.6), sharey=True, sharex=True)
for ax, (name, stop, prof, note) in zip(axes.flat, BLADES):
    draw_flower(ax)
    pts = [(stop - x, z) for x, z in prof]
    ax.add_patch(Polygon(pts, closed=True, color="#d9711c"))
    rod = (stop - (0.93 if name.startswith("Block A") else 0.77), 0.77)
    ax.add_patch(Circle(rod, 0.16, color="#8b96a1", ec="#17212b", lw=0.8))
    z = rest_height(pts, PIN)
    ax.add_patch(Circle((PIN, z), R, facecolor="#d8c21a", edgecolor="#17212b", alpha=.75))
    ax.annotate("", (PIN - 1.2, z), (PIN - R - 0.1, z), arrowprops=dict(arrowstyle="->", color="#17212b", lw=1.5)) if not name.startswith("Block A") else None
    ax.plot([PIN, PIN], [-0.3, 0.2], color="#17212b", lw=0.8, ls=":")
    ax.text(PIN, -0.25, "pinned ball", fontsize=7, ha="center", va="bottom", color="#17212b")
    ax.set_xlim(-4.0, 3.3); ax.set_ylim(-0.3, 4.6); ax.set_aspect("equal"); ax.grid(alpha=.2)
    ax.set_title(name, fontsize=9.5)
    ax.text(-3.9, 4.45, note, fontsize=7.5, va="top", color="#56636f", bbox=dict(boxstyle="round,pad=0.3", fc="white", ec="none", alpha=.85))
    ax.set_xlabel("inches ahead of the FLOWER's centre", fontsize=9)
for ax in axes[:, 0]: ax.set_ylabel("inches above the tiles")
fig.suptitle("Four profiles at their stop, the bottom POLLEN resting on each (FLOWER from the field CAD; the robot is to the left)", fontsize=10.5)
fig.tight_layout()
out = os.path.join(HERE, "profiles_compare.png"); fig.savefig(out, dpi=130); print(out)
