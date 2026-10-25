"""The start box: the robot's side silhouette from the export with the low arm folded, against the 18 in limit.

    python3 cad/modules/flower-stick-proto/start_box.py     # start_box.png
"""
import os, json, numpy as np
import matplotlib; matplotlib.use("Agg"); import matplotlib.pyplot as plt
from matplotlib.patches import Polygon, Circle, Rectangle
HERE = os.path.dirname(os.path.abspath(__file__))
A = json.load(open(os.path.join(HERE, "page", "low_arm.json")))
CHASSIS_REAR, MECANUM_REAR, BELT_REAR = -15.0, -15.41, -17.62        # from the export: the plastic side plates, the rear mecanums, the launcher concept's belt
fig, ax = plt.subplots(figsize=(13, 5))
ax.plot([-20, 6], [0, 0], color="k", lw=1)
ax.add_patch(Rectangle((CHASSIS_REAR, 1.2), -CHASSIS_REAR, 5.0, fc="#dde", ec="#888", lw=0.8)); ax.text(-7.5, 3.7, "chassis (plates end 15.0 in behind the face)", ha="center", fontsize=9)
for x in (-1.9, -13.5): ax.add_patch(Circle((x, 1.89), 1.89, fc="#bbb", ec="k", lw=0.8))
ax.add_patch(Rectangle((-1.9, 3.1), 1.9, 5.7, fc="#ccc", ec="#888")); ax.text(-0.95, 9.1, "intake", ha="center", fontsize=8)
ax.add_patch(Rectangle((BELT_REAR, 4.0), 2.6, 1.0, fc="#fcc", ec="#c33", lw=0.8)); ax.text(BELT_REAR + 1.3, 5.4, "launcher concept's\nbelt, 17.6 behind", ha="center", fontsize=8, color="#c33")
ax.add_patch(Polygon(np.array(A["arm_stow"]), fc="#ccc", ec="k", lw=0.8, alpha=0.85)); ax.add_patch(Polygon(np.array(A["ear_stow"]), fc="#e07020", ec="k", lw=0.6)); ax.add_patch(Polygon(np.array(A["blade_stow"]), fc="#e07020", ec="k", lw=0.8))
sh = A["shaft_stow"]; ax.add_patch(Circle(tuple(sh), A["wheel_r"], fc="#444", ec="k", alpha=0.35)); ax.add_patch(Circle(tuple(A["pivot"]), 0.16, fc="k"))
front = A["stowed_front"]
for x0, label, col, y in ((CHASSIS_REAR, "18 in from the chassis plates", "#2a2", 10.5), (BELT_REAR, "18 in from the belt", "#c33", 11.6)):
    ax.plot([x0, x0 + 18], [y, y], color=col, lw=2); ax.plot([x0, x0], [0, y], color=col, lw=0.8, ls=":"); ax.plot([x0 + 18, x0 + 18], [0, y], color=col, lw=0.8, ls=":"); ax.text(x0 + 9, y + 0.2, label, ha="center", fontsize=8, color=col)
ax.plot([front, front], [0, 9.5], color="#e07020", lw=1, ls="--"); ax.text(front + 0.15, 8.6, f"stick folded:\n{front:.2f} in ahead of the face", fontsize=8, color="#c60")
ax.text(-9.5, 7.3, f"chassis + folded stick: {front - CHASSIS_REAR:.1f} in (mecanums {front - MECANUM_REAR:.1f})   |   with the belt: {front - BELT_REAR:.1f} in", ha="center", fontsize=9)
ax.set_xlim(-20, 6); ax.set_ylim(-0.8, 12.5); ax.set_aspect("equal"); ax.set_xlabel("inches ahead of the intake face"); ax.set_ylabel("inches above the tile"); ax.grid(True, lw=0.3, alpha=0.5)
ax.set_title("The start box with the low arm folded (stowed at 85°, tail cut 2.4 in)", fontsize=10)
fig.tight_layout(); fig.savefig(os.path.join(HERE, "start_box.png"), dpi=110); print("start_box.png", round(front - CHASSIS_REAR, 2), round(front - BELT_REAR, 2))
