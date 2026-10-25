"""The low arm: the FLOWER stick as the intake's front ramp, pivoting low at the chassis front like the robot in the
October 25 photo, drawn against the robot's own geometry from the "flowerextractordown" export. Inches ahead of the intake
face, left of centre, up from the tile.

    python3 cad/modules/flower-stick-proto/low_arm.py      # low_arm.png, page/low_arm.json

The geometry (all on goBILDA's 24 mm pattern):
- pivot 0.5 in ahead of the face, 2.2 in up, on a bracket off the front end of each chassis side rail (the rails end at the
  face line, 4.9 to 5.35 in off centre, 1.2 to 3.1 in up); the arms are 5-hole lowside U-channels (144 mm) standing on
  edge at 3.95 to 4.45 in off centre, inner faces at the blade's ends;
- the shaft hole 72 mm (3 holes) down the arm from the pivot: the arm sits 30 deg below horizontal with the shaft 0.77 in up
  on 39 mm wheels keyed to the shaft outboard of the arms; the shaft turns in the arms' 8x14x4 bearings;
- the blade (the wide mountain, 7.9 in) is bolted to the arms through ears over the arm's next two holes, so its angle
  comes from the arm and the shaft only carries the wheels (the blade's bore is a free 8.6 mm);
- stowed, the arm stands 95 deg (5 deg back from vertical) with the blade folded up in front of the intake mouth;
- the poker on the intake's top channel reaches 1.0 in (the face is 3.47 in behind the lip here).
"""
import os, json, math, numpy as np
import matplotlib; matplotlib.use("Agg"); import matplotlib.pyplot as plt
from matplotlib.patches import Polygon, Circle, Rectangle
from shapely.geometry import Polygon as SPoly, Point
HERE = os.path.dirname(os.path.abspath(__file__))
MOUNTAIN = json.load(open(os.path.join(HERE, "page", "mountain_profile.json")))
HOLE = 24 / 25.4
PIVOT = np.array([0.5, 2.2]); L = 3 * HOLE; SHAFT_Z = 0.77; WHEEL_R = 0.77; SHAFT_BACK = 0.53
ARM_W = 32 / 25.4; ARM_Y = (3.95, 4.45); WHEEL_Y = (4.45, 4.95); STOW_DEG = 95.0; POKER = (1.0, 5.3, 7.5)
ang_dep = -math.asin((PIVOT[1] - SHAFT_Z) / L); ang_stow = math.radians(STOW_DEG)
def at(ang): return PIVOT + L * np.array([math.cos(ang), math.sin(ang)])
shaft = at(ang_dep); lip = shaft[0] + SHAFT_BACK
def rot(P, c, a):
    co, si = math.cos(a), math.sin(a); q = np.asarray(P) - c; return c + np.c_[co * q[:, 0] - si * q[:, 1], si * q[:, 0] + co * q[:, 1]]
def arm_poly(ang):
    """the channel from one hole behind the pivot to 12 mm past the shaft hole, its tip's lower corner cut off level with 0.1 in up
    when deployed (a 32 mm channel ending at a shaft 0.77 in up would dig into the tile otherwise)"""
    d = np.array([math.cos(ang), math.sin(ang)]); n = np.array([-d[1], d[0]]) * ARM_W / 2
    a, b = PIVOT - d * HOLE, PIVOT + d * (L + 12 / 25.4); poly = SPoly([a + n, b + n, b - n, a - n])
    cut = SPoly([(-20, 0.1), (20, 0.1), (20, 30), (-20, 30)])         # in the deployed pose: keep what is above 0.1 in
    dep = rot(np.array(poly.exterior.coords), PIVOT, ang_dep - ang); kept = SPoly(dep).intersection(cut)
    return rot(np.array(kept.exterior.coords), PIVOT, ang - ang_dep)
blade_dep = np.array([[lip - x, z] for x, z in MOUNTAIN] + [[lip - MOUNTAIN[-1][0], 0.52], [lip, 0.52]])
ear_dep = np.array([shaft + np.array([math.cos(ang_dep + math.pi), math.sin(ang_dep + math.pi)]) * 0 + np.array([-0.45, 0]) * 0 for _ in range(1)])
# the ears: a strip 1.0 in wide along the arm from the shaft hole up to 2.3 holes, 0.5 in thick at each end of the blade
d = np.array([math.cos(ang_dep + math.pi), math.sin(ang_dep + math.pi)]); n = np.array([-d[1], d[0]]) * 0.5
ear_dep = np.array([shaft - d * 0.4 + n, shaft + d * 2.3 * HOLE + n, shaft + d * 2.3 * HOLE - n, shaft - d * 0.4 - n])
a_fold = ang_stow - ang_dep
blade_stow, ear_stow, shaft_stow = rot(blade_dep, PIVOT, a_fold), rot(ear_dep, PIVOT, a_fold), at(ang_stow)
def first_contact(poly, ball=(-0.29, 1.75), r=1.4):
    P = SPoly(poly); ox = 14.0
    while ox > -5 and P.distance(Point(ox + ball[0], ball[1])) > r: ox -= 0.01
    return ox
STOP = 1.18
def flower(ax, ox, balls):
    ax.add_patch(Rectangle((ox - 2.36, 0), 4.72, 0.35, color="#777")); ax.add_patch(Rectangle((ox - 1.54, 0), 3.08, 0.35, color="#dde"))
    ax.add_patch(Rectangle((ox + STOP, 0.35), 1.22, 3.75, color="#999", alpha=0.6))
    for sg in (1, -1):
        x0, x1 = sorted([ox + sg * 1.71, ox + sg * 2.41]); ax.add_patch(Rectangle((x0, 3.90), x1 - x0, 0.35, color="#555"))
        ax.add_patch(Rectangle((ox + sg * 1.72 - 0.525, 4.25), 1.05, 4.5, color="#8a8", alpha=0.5))
    for (bx, bz) in balls: ax.add_patch(Circle((ox + bx, bz), 1.4, fc="#e8d44a", ec="k", alpha=0.75))
    ax.text(ox, -0.6, "FLOWER", ha="center", fontsize=8)
def robot(ax):
    ax.plot([-9, 0], [0, 0], color="k", lw=1); ax.plot([0, 0], [0, 14], color="#444", lw=1, ls=":")
    ax.add_patch(Rectangle((-15.1, 1.22), 15.1, 1.89, fc="#ddd", ec="#888", lw=0.8)); ax.text(-4.5, 2.17, "chassis side rail (4.9 to 5.35 in out)", fontsize=7, va="center", ha="center")
    ax.add_patch(Rectangle((-1.89, 3.11), 1.89, 5.67, fc="#ccc", ec="#888", lw=0.8)); ax.text(-0.95, 9.0, "intake\nupright", fontsize=7, ha="center")
    ax.add_patch(Circle((-1.9, 2.0), 1.89, fc="none", ec="#888", lw=0.8, ls="--")); ax.text(-1.9, 2.0, "mecanum\n(5.2 to 7.1 out)", fontsize=6, ha="center", va="center")
    ax.add_patch(Circle((-0.94, 2.77), 0.94, fc="#bbb", ec="k", lw=0.8)); ax.text(-0.94, 4.1, "intake wheels", fontsize=7, ha="center")
    ax.add_patch(Rectangle((-1.9, 7.03 - 1.9), 1.9, 0.48, fc="#555")); 
    ax.add_patch(Rectangle((0, POKER[1]), POKER[0], POKER[2] - POKER[1], fc="#e0552b", ec="k", lw=0.8)); ax.text(POKER[0] + 0.15, 6.4, f"poker\n{POKER[0]} in", fontsize=7, va="center")
    ax.add_patch(Rectangle((0.0, 1.22), 0.75, 1.89, fc="#aaa", ec="k", lw=0.8)); ax.text(0.9, 1.4, "pivot bracket", fontsize=7)
    ax.add_patch(Circle(tuple(PIVOT), 0.16, fc="k"))
fig, axes = plt.subplots(1, 2, figsize=(13, 6.4), sharey=True)
balls = [(-0.29, 1.75), (0.35, 4.48)]
ax = axes[0]; robot(ax)
ax.add_patch(Polygon(arm_poly(ang_stow), fc="none", ec="#999", ls="--", lw=1)); ax.add_patch(Polygon(blade_stow, fc="none", ec="#c60", ls="--", lw=1))
ax.add_patch(Polygon(arm_poly(ang_dep), fc="#ccc", ec="k", lw=0.8, alpha=0.85)); ax.add_patch(Polygon(ear_dep, fc="#e07020", ec="k", lw=0.6, alpha=0.9))
ax.add_patch(Polygon(blade_dep, fc="#e07020", ec="k", lw=0.8)); ax.add_patch(Circle(tuple(shaft), WHEEL_R, fc="#444", ec="k", alpha=0.35)); ax.add_patch(Circle(tuple(shaft), 0.16, fc="k"))
ox = first_contact(blade_dep); flower(ax, ox, balls)
ax.annotate(f"arm {math.degrees(-ang_dep):.0f}° down, 72 mm pivot to shaft, tip cut level;\nshaft {shaft[0]:.2f} in ahead on 39 mm wheels, lip {lip:.2f}", (shaft[0], shaft[1] + 0.3), (-8.8, 5.6), fontsize=8, arrowprops=dict(arrowstyle="->"))
ax.annotate("ears: the blade bolts to the arm's\nnext two holes; the shaft is free in it", (ear_dep[1][0], ear_dep[1][1]), (-8.8, 7.6), fontsize=8, arrowprops=dict(arrowstyle="->"))
ax.set_title("A. Deployed: the blade is the intake's ramp, the wheels set its height", fontsize=10)
ax = axes[1]; robot(ax)
ax.add_patch(Polygon(arm_poly(ang_dep), fc="none", ec="#999", ls="--", lw=1)); ax.add_patch(Polygon(blade_dep, fc="none", ec="#c60", ls="--", lw=1))
ax.add_patch(Polygon(arm_poly(ang_stow), fc="#ccc", ec="k", lw=0.8, alpha=0.85)); ax.add_patch(Polygon(ear_stow, fc="#e07020", ec="k", lw=0.6)); ax.add_patch(Polygon(blade_stow, fc="#e07020", ec="k", lw=0.8))
ax.add_patch(Circle(tuple(shaft_stow), WHEEL_R, fc="#444", ec="k", alpha=0.35)); ax.add_patch(Circle(tuple(shaft_stow), 0.16, fc="k"))
front = max(blade_stow[:, 0].max(), shaft_stow[0] + WHEEL_R, arm_poly(ang_stow)[:, 0].max()); top = max(blade_stow[:, 1].max(), shaft_stow[1] + WHEEL_R)
ax.plot([front, front], [0, 12], color="#c33", lw=0.8, ls="--"); ax.text(front + 0.15, 11.5, f"foremost point {front:.2f} in ahead of the face", fontsize=7, color="#c33")
ax.set_title(f"B. Stowed at {STOW_DEG:.0f}°: folded up in front of the mouth, top {top:.1f} in up", fontsize=10)
for ax in axes:
    ax.set_xlim(-9.0, 9.0); ax.set_ylim(-1.2, 12.5); ax.set_aspect("equal"); ax.set_xlabel("inches ahead of the intake face"); ax.grid(True, lw=0.3, alpha=0.5)
axes[0].set_ylabel("inches above the tile")
fig.suptitle("The low arm on our robot (chassis from the flowerextractordown export): pivot 0.5 in ahead of the face, 2.2 in up, off the side rails", fontsize=10)
fig.tight_layout(); fig.savefig(os.path.join(HERE, "low_arm.png"), dpi=110)
out = {"pivot": PIVOT.tolist(), "L": L, "hole": HOLE, "shaft_z": SHAFT_Z, "wheel_r": WHEEL_R, "shaft_back": SHAFT_BACK, "arm_w": ARM_W, "arm_y": ARM_Y, "wheel_y": WHEEL_Y,
       "ang_dep_deg": math.degrees(ang_dep), "stow_deg": STOW_DEG, "shaft_dep": shaft.tolist(), "lip_ahead": lip, "shaft_stow": shaft_stow.tolist(), "stowed_front": front, "stowed_top": top,
       "poker": POKER, "ear_dep": ear_dep.tolist(), "blade_stow": blade_stow.tolist(), "ear_stow": ear_stow.tolist(), "stowed_dir": [math.cos(ang_stow), math.sin(ang_stow)]}
json.dump(out, open(os.path.join(HERE, "page", "low_arm.json"), "w"), indent=1)
print(f"deployed: arm {math.degrees(-ang_dep):.1f} deg, shaft ({shaft[0]:.2f}, {shaft[1]:.2f}), lip {lip:.2f} ahead of the face, FLOWER centre at first contact {ox:.2f}")
print(f"stowed at {STOW_DEG} deg: shaft ({shaft_stow[0]:.2f}, {shaft_stow[1]:.2f}), foremost point {front:.2f} ahead of the face, top {top:.2f}; robot rear at -17.62 -> overall {front + 17.62:.2f} in")
