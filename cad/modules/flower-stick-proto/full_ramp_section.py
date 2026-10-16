"""The section figure: the full-width ramp at its stop in the FLOWER's slice, with the POLLEN at three moments of the
simulator's trace, and the 3.90 in lintel the lifted POLLEN must pass under. Writes full_ramp_section.png.

    python3 tools/ramp-hook/full_ramp_sim.py --trace && python3 cad/modules/flower-stick-proto/full_ramp_section.py
"""
import json, os
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import Circle, Polygon, Rectangle
HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", "..", ".."))
d = json.load(open(os.path.join(ROOT, "tools", "ramp-hook", "full_ramp_trace.json")))
CX, R = d["cx"], 1.4
fx = lambda xs: CX - xs                     # sim x -> FLOWER x (+ toward the wall)
prof, (rx, rz, rr), stop = d["profile"], d["roller"], fx(d["tip_stop"])
frames = d["trace"]
arrive = next(i for i, f in enumerate(frames) if f[1] <= d["tip_stop"] + 1e-6)
picks = [(arrive, "at the stop"), (arrive + 12, "+0.25 s"), (arrive + 25, "+0.5 s")]
picks = [(min(i, len(frames) - 1), lab) for i, lab in picks]

fig, axes = plt.subplots(1, 3, figsize=(13.5, 4.6), sharey=True)
for ax, (i, lab) in zip(axes, picks):
    t, tip, balls = frames[i]
    # the FLOWER's slice: tiles, ring, upright, lintel (Layer B's strip in front of its hole), bracket, pipe, wall
    ax.add_patch(Rectangle((-8, -0.3), 11.2, 0.3, color="#8b96a1"))
    ax.add_patch(Rectangle((-2.40, 0), 0.86, 0.35, color="#3b4148"))      # ring, front of the hole
    ax.add_patch(Rectangle((1.54, 0), 0.93, 0.35, color="#3b4148"))       # ring, behind
    ax.add_patch(Rectangle((1.18, 0.35), 1.28, 3.75, color="#56636f"))    # upright
    ax.add_patch(Rectangle((-2.41, 3.90), 0.87, 1.35, color="#3b4148"))   # lintel
    ax.add_patch(Rectangle((1.54, 3.90), 1.06, 1.35, color="#3b4148"))    # bracket, behind the hole
    ax.add_patch(Rectangle((-2.25, 4.25), 1.05, 3.0, color="#5fae58", alpha=.5))  # pipe (off the slice, for scale)
    ax.add_patch(Rectangle((2.71, -0.3), 0.5, 7.6, color="#8b96a1"))
    # the ramp at this moment, and the roller
    lip = fx(tip)
    pts = [(lip - x, z) for x, z in prof]
    sheet = pts + [(x, z - 0.10) for x, z in reversed(pts)]
    ax.add_patch(Polygon(sheet, closed=True, color="#d9711c"))
    ax.add_patch(Circle((lip - rx, rz), rr, color="#8b96a1"))
    ax.annotate("", (lip - rx - 0.6, rz - rr - 0.15), (lip - rx + 0.6, rz - rr - 0.15), arrowprops=dict(arrowstyle="<-", color="#17212b"))
    for (xs, zs) in balls:
        ax.add_patch(Circle((fx(xs), zs), R, facecolor="#d8c21a", edgecolor="#17212b", alpha=.75))
    ax.plot([-2.41, 1.54], [3.90, 3.90], ls="--", color="#d9711c", lw=1)
    ax.set_xlim(-7.5, 3.3); ax.set_ylim(-0.3, 7.3); ax.set_aspect("equal")
    ax.set_title(f"{lab}  (t = {t:.2f} s)", fontsize=10)
    ax.set_xlabel("inches ahead of the FLOWER's centre (robot side is negative)")
    ax.grid(alpha=.2)
axes[0].set_ylabel("inches above the tiles")
axes[0].text(-7.3, 6.6, "dashed: the lintel's underside, 3.90 in.\nA lifted POLLEN passes under it.", fontsize=8, color="#d9711c", va="bottom")
axes[0].text(stop + 0.05, 1.3, "stop", fontsize=8, rotation=90)
axes[2].text(stop - rx, rz + rr + 0.2, "roller", fontsize=8, ha="center")
fig.suptitle("Full-width ramp at the FLOWER: lip 0.45 in, 45° nose to a 1.0 in ridge, down to a 0.5 in plate, roller outside the bracket", fontsize=11)
fig.tight_layout()
out = os.path.join(HERE, "full_ramp_section.png"); fig.savefig(out, dpi=130); print(out)
