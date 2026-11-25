"""Renders the assembly, deployed and stowed, from the side and from the front-left, with the corner modules' boxes.

    python3 cad/flower-stub/views.py      # writes views.png (needs parts.py's cadquery)
"""
import os, sys, math
import numpy as np
import matplotlib; matplotlib.use("Agg")
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import parts as P

def tris(w, tol=0.3):
    v, f = w.val().tessellate(tol, 0.3)
    v = np.array([[p.x, p.y, p.z] for p in v]) / P.IN
    return v[np.array(f)]

def scene(a, cd):
    g = P.rot_about_pivot(P.moving_group(), a - P.ARM_DEP)
    items = [(g, "#f4c20d"), (P.mount(True), "#9b59b6"), (P.mirror(P.mount(False)), "#9b59b6"),
             (P.link(a, cd), "#6c3483"), (P.crank(cd), "#6c3483"), (P.box(*P.SERVO_CASE), "#333333")]
    for name, kk in P.keepouts().items():
        if name.startswith("corner"):
            items.append((kk, "#7fb3d5"))
    items.append((P.keepouts()["Pollen Kicker (lowest)"], "#e0552b"))
    return items

crank = None
ln = P.link_len(); guess = P.ARM_DEP + P.LEVER_OFFSET_DEG
_, c0, _ = P.crank_pin_for(P.ARM_DEP, guess, ln); _, c1, _ = P.crank_pin_for(P.STOW_DEG, c0 + 102, ln)
fig = plt.figure(figsize=(16, 12))
for row, (label, a, cd) in enumerate((("deployed", P.ARM_DEP, c0), ("stowed", P.STOW_DEG, c1))):
    sc = scene(a, cd)
    for col, (el, az) in enumerate(((0, -90), (22, -130))):
        ax = fig.add_subplot(2, 2, row * 2 + col + 1, projection="3d")
        for w, c in sc:
            ax.add_collection3d(Poly3DCollection(tris(w), facecolor=c, edgecolor="k", linewidths=0.05, alpha=0.9 if c != "#7fb3d5" else 0.35))
        ax.plot([P.BOX_FRONT] * 2, [-9, 9], [0, 0], color="r", lw=1)
        ax.set_xlim(6.5, 13.5); ax.set_ylim(-7, 7); ax.set_zlim(0, 8); ax.set_box_aspect((7, 14, 8))
        ax.view_init(el, az); ax.set_title(f"{label}, {'side (left)' if col == 0 else 'front-left'}", fontsize=10)
        ax.set_xlabel("X fwd"); ax.set_ylabel("Y left"); ax.set_zlabel("Z")
fig.suptitle("FLOWER stub: the U (yellow), the mounts and link (purple), the servo (dark), Tim's corner modules (blue boxes), the Pollen Kicker (orange). Red line: 18 in cube front.", fontsize=10)
fig.tight_layout(); fig.savefig(os.path.join(os.path.dirname(os.path.abspath(__file__)), "views.png"), dpi=90)
