"""Side and front views of a layout's corner and its arm (down and stowed), over the 26 Nov STEP's points.
    python3 take2/picture.py <points.npy> take1|2a_upright|2b_flat <out.png>"""
import os, sys, numpy as np, matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle, Circle
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE); sys.path.insert(0, os.path.dirname(HERE))
import geometry as G, layouts as L, trimesh   # noqa: E402
from geometry import IN
P = np.load(sys.argv[1]); name = sys.argv[2]
boxes = G.corner_boxes if name == "take1" else L.LAYOUTS[name][0]
stl = os.path.join(os.path.dirname(HERE), "arm.stl") if name == "take1" else os.path.join(HERE, name, "arm.stl")
import json, parts   # noqa: E402
cj = os.path.join(os.path.dirname(HERE), "clearance.json") if name == "take1" else os.path.join(HERE, name, "clearance.json")
hj = json.load(open(cj))["hump"]
hm = (hj["height_mm"], hj["leaves_line_u_mm"], *hj["full_u_mm"], hj["back_on_line_u_mm"])
outline = np.array(parts._outline(hm, G.ARM_W / 2)); outline = np.vstack([outline, outline[:1]])
fig, ax = plt.subplots(1, 2, figsize=(16, 7.5))
s = P[(P[:, 1] > 60) & (P[:, 1] < 220) & (P[:, 0] > 100) & (P[:, 0] < 360) & (P[:, 2] < 260)]
ax[0].scatter(s[:, 0] / IN, s[:, 2] / IN, s=.05, c="0.75")
s = P[(P[:, 0] > 150) & (P[:, 0] < 300) & (np.abs(P[:, 1]) < 230) & (P[:, 2] < 260)]
ax[1].scatter(s[:, 1] / IN, s[:, 2] / IN, s=.05, c="0.75")
col = {"servo": "tab:blue", "tabs": "tab:blue", "frame": "0.35", "mount_side": "0.35", "screw": "k", "wheel": "tab:orange"}
for side in ("left", "right"):
    for k, v in boxes(side).items():
        if v[0] == "box":
            lo, hi = v[1] / IN, v[2] / IN
            if side == "left": ax[0].add_patch(Rectangle((lo[0], lo[2]), hi[0] - lo[0], hi[2] - lo[2], fc=col[k], alpha=.5, ec="k"))
            ax[1].add_patch(Rectangle((lo[1], lo[2]), hi[1] - lo[1], hi[2] - lo[2], fc=col[k], alpha=.5, ec="k"))
        else:
            _, c, a, r, h = v; c = c / IN; r /= IN; h /= IN
            if a[0] > 0.5:   # axle along X: a disc seen edge-on from the side, round from the front
                if side == "left": ax[0].add_patch(Rectangle((c[0] - h, c[2] - r), 2 * h, 2 * r, fc=col[k], alpha=.5, ec="k"))
                ax[1].add_patch(Circle((c[1], c[2]), r, fc=col[k], alpha=.35 if k == "wheel" else .8, ec="k"))
            else:            # axle up: edge-on from both
                if side == "left": ax[0].add_patch(Rectangle((c[0] - r, c[2] - h), 2 * r, 2 * h, fc=col[k], alpha=.5, ec="k"))
                ax[1].add_patch(Rectangle((c[1] - r, c[2] - h), 2 * r, 2 * h, fc=col[k], alpha=.5, ec="k"))
for th, ls in ((0, "--"), (162, "-")):
    q = G.arm_to_robot(outline[:, 0], outline[:, 1], 0, th) / IN
    ax[0].plot(q[:, 0], q[:, 2], "g" + ls, lw=2, label=f"printed arm, {'stowed' if th == 0 else 'down'} ({hm[0]:.0f} mm hump)")
for side in ("left", "right"):
    lo, hi = G.ARM_Y[side]; ax[1].axvspan(min(lo, hi) / IN, max(lo, hi) / IN, color="g", alpha=.15)
ax[1].add_patch(Circle((4.6, 1.4), 1.4, fc="gold", ec="k", alpha=.6)); ax[1].text(4.6, 1.4, "POLLEN", ha="center", fontsize=7)
ax[0].add_patch(Circle((11.9, 1.4), 1.4, fc="gold", ec="k", alpha=.6)); ax[0].text(11.9, 1.4, "POLLEN", ha="center", fontsize=7)
ax[0].axvline(10.43, c="r", ls=":", lw=1); ax[0].text(10.48, 9.5, "18 in cube front", color="r", fontsize=8)
ax[0].set_title(f"{name}: left side (front is right); grey = the 26 Nov STEP")
ax[1].set_title("from the front (robot's left on the right); green bands = the arms")
for a in ax: a.set_aspect("equal"); a.grid(True, alpha=.4); a.axhline(0, c="brown"); a.set_ylabel("height off tile, in")
ax[0].set_xlabel("X forward from the drive axles' middle, in"); ax[1].set_xlabel("Y left, in")
ax[0].legend(loc="lower left"); ax[0].set_xlim(3, 14); ax[0].set_ylim(-0.3, 10); ax[1].set_ylim(-0.3, 8); ax[1].invert_xaxis()
plt.tight_layout(); plt.savefig(sys.argv[3], dpi=100)
