"""The section figure: the v2 blade at its stop in the FLOWER's slice, the intake's first roller at the FLOWER's face,
and the POLLEN at three moments of the simulator's trace. Writes intake_section.png.

    python3 tools/ramp-hook/intake_sim.py --trace && python3 cad/modules/flower-stick-proto/intake_section.py
"""
import json, os
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import Circle, Polygon, Rectangle
HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", "..", ".."))
d = json.load(open(os.path.join(ROOT, "tools", "ramp-hook", "intake_trace.json")))
CX, R = d["cx"], 1.4
fx = lambda xs: CX - xs                     # sim x -> FLOWER x (+ toward the wall)
prof, (rx, rz, rr), stop = d["profile"], d["roller"], fx(d["tip_stop"])
frames = d["trace"]
arrive = next(i for i, f in enumerate(frames) if f[1] <= d["tip_stop"] + 1e-6)
picks = [(arrive, "at the stop: the first ball over the ridge"), (arrive + 8, "down the back slope onto the tiles"), (arrive + 18, "at the roller; the stack has dropped")]
picks = [(min(i, len(frames) - 1), lab) for i, lab in picks]

fig, axes = plt.subplots(1, 3, figsize=(13.5, 4.6), sharey=True)
for ax, (i, lab) in zip(axes, picks):
    t, tip, balls = frames[i]
    ax.add_patch(Rectangle((-8, -0.3), 11.2, 0.3, color="#8b96a1"))
    ax.add_patch(Rectangle((-2.40, 0), 0.86, 0.35, color="#3b4148"))      # ring, in front of the hole
    ax.add_patch(Rectangle((1.54, 0), 0.93, 0.35, color="#3b4148"))       # ring, behind
    ax.add_patch(Rectangle((1.18, 0.35), 1.28, 3.75, color="#56636f"))    # upright
    ax.add_patch(Rectangle((-2.41, 3.90), 0.87, 1.35, color="#3b4148"))   # lintel
    ax.add_patch(Rectangle((1.54, 3.90), 1.06, 1.35, color="#3b4148"))    # bracket, behind the hole
    ax.add_patch(Rectangle((-2.25, 4.25), 1.05, 3.0, color="#5fae58", alpha=.5))  # pipe (beside the slice)
    ax.add_patch(Rectangle((2.71, -0.3), 0.5, 7.6, color="#8b96a1"))
    lip = fx(tip)
    pts = [(lip - x, z) for x, z in prof] + [(lip - prof[-1][0], 0.52), (lip, 0.52)]
    ax.add_patch(Polygon(pts, closed=True, color="#d9711c"))
    ax.add_patch(Circle((lip - 0.69, 0.77), 0.16, color="#8b96a1", ec="#17212b", lw=0.8))    # the 8 mm shaft
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
axes[0].text(-7.3, 6.6, "dashed: the lintel's underside, 3.90 in", fontsize=8, color="#d9711c", va="bottom")
axes[0].text(stop + 0.05, 1.3, "stop", fontsize=8, rotation=90)
axes[2].text(stop - rx, rz + rr + 0.2, "intake roller", fontsize=8, ha="center")
axes[2].text(-2.41, 6.2, "bracket's\nfront edge", fontsize=7, ha="center", color="#56636f")
fig.suptitle("The v2 blade feeding the intake: blade at its stop, 2 in of open tile, the intake's first roller at the FLOWER's face (2-D simulator, field-CAD FLOWER)", fontsize=10.5)
fig.tight_layout()
out = os.path.join(HERE, "intake_section.png"); fig.savefig(out, dpi=130); print(out)
