"""The coach's stick in his "flowerextractordown" export (Oct 25, the arms swung part way), measured and swung the rest of
the way to the floor, with the Limelight where that export has it. Inches ahead of the intake face and up from the tile.

    python3 cad/modules/flower-stick-proto/coach_stick_down.py     # coach_stick_down.png

page/coach_stick_down.json comes from the export (scratch: list_parts.py, stick_mesh2.py): the frame is set by the mecanum
wheels (floor), the intake uprights (face) and the servo on the arm pivot, which lands within 0.02 in of Robot Copy 1's.
"""
import os, json, numpy as np
import matplotlib; matplotlib.use("Agg"); import matplotlib.pyplot as plt
from matplotlib.patches import Polygon, Circle, Rectangle
from shapely.geometry import Polygon as SPoly, Point
HERE = os.path.dirname(os.path.abspath(__file__))
D = json.load(open(os.path.join(HERE, "page", "coach_stick_down.json")))
piv = np.array(D["pivot_servo"]); L = D["L_now"]; shaft = np.array(D["shaft_now"])[[0, 2]]
arm = np.array(D["arm_hull"]); plate = np.array(D["plate_now"])
STOP = 1.18
def rot(P, c, a):
    co, si = np.cos(a), np.sin(a); q = np.asarray(P) - c; return c + np.c_[co * q[:, 0] - si * q[:, 1], si * q[:, 0] + co * q[:, 1]]
# swing about the servo pivot until the shaft is 0.77 up (our sled wheels); and back up to stowed (10 deg forward of vertical)
ang_now = np.arctan2(shaft[1] - piv[1], shaft[0] - piv[0])
ang_full = -np.arcsin((piv[1] - 0.77) / L); a_full = ang_full - ang_now
ang_stow = np.radians(80.0); a_stow = ang_stow - ang_now
def first_contact(poly, ball=(-0.29, 1.75), r=1.4):
    P = SPoly(poly); ox = 14.0
    while ox > -5 and P.distance(Point(ox + ball[0], ball[1])) > r: ox -= 0.01
    return ox
def flower(ax, ox, balls):
    ax.add_patch(Rectangle((ox - 2.36, 0), 4.72, 0.35, color="#777")); ax.add_patch(Rectangle((ox - 1.54, 0), 3.08, 0.35, color="#dde"))
    ax.add_patch(Rectangle((ox + STOP, 0.35), 1.22, 3.75, color="#999", alpha=0.6))
    for sg in (1, -1):
        x0, x1 = sorted([ox + sg * 1.71, ox + sg * 2.41]); ax.add_patch(Rectangle((x0, 3.90), x1 - x0, 0.35, color="#555"))
        ax.add_patch(Rectangle((ox + sg * 1.72 - 0.525, 4.25), 1.05, 4.5, color="#8a8", alpha=0.5))
    for (bx, bz) in balls: ax.add_patch(Circle((ox + bx, bz), 1.4, fc="#e8d44a", ec="k", alpha=0.75))
    ax.text(ox, -0.6, "FLOWER", ha="center", fontsize=8)
def robot(ax):
    ax.plot([0, 0], [0, 18], color="#444", lw=1, ls=":"); ax.plot([-9, 0], [0, 0], color="k", lw=1)
    ax.add_patch(Circle((-0.94, 2.77), 0.94, fc="#bbb", ec="k", lw=0.8)); ax.text(-1.0, 4.0, "intake wheels\n(lowered in this export)", ha="center", fontsize=7)
    ax.add_patch(Circle(tuple(piv), 0.16, fc="k")); ax.text(piv[0] - 0.3, piv[1] + 0.4, "pivot (servo)", ha="right", fontsize=8)
    b = D["limelight_box"]; ax.add_patch(Rectangle((b["ahead"][0], b["up"][0]), b["ahead"][1] - b["ahead"][0], b["up"][1] - b["up"][0], fc="#333", ec="k"))
    cx, cz = np.mean(b["ahead"]), np.mean(b["up"])
    for e in (24.0, 66.0): ax.plot([cx, cx + 9 * np.cos(np.radians(e))], [cz, cz + 9 * np.sin(np.radians(e))], color="#c33", lw=0.8, ls="--")
    ax.text(cx - 0.4, cz + 1.3, "Limelight\n(where the export has it)", ha="right", fontsize=7)
fig, axes = plt.subplots(1, 2, figsize=(12, 7.2), sharey=True)
balls = [(-0.29, 1.75), (0.35, 4.48)]
ax = axes[0]; robot(ax)
ax.add_patch(Polygon(rot(arm, piv, a_stow), fc="none", ec="#999", ls="--", lw=1)); ax.add_patch(Polygon(rot(plate, piv, a_stow), fc="none", ec="#c60", ls="--", lw=1))
ax.add_patch(Polygon(arm, fc="#ccc", ec="k", lw=0.8, alpha=0.8)); ax.add_patch(Polygon(plate, fc="#e07020", ec="k", lw=0.8)); ax.add_patch(Circle(tuple(shaft), 0.16, fc="k"))
flower(ax, first_contact(plate) + 1.5, balls)
ax.annotate(f"arm {np.degrees(-ang_now):.0f}° down (shaft to servo pivot), plate flat,\n{plate[:, 1].min():.1f} in above the tile", (plate[:, 0].mean(), plate[:, 1].max()), (-8.5, 4.6), fontsize=8, arrowprops=dict(arrowstyle="->"))
ax.set_title("A. The export as it is: swung part way", fontsize=10)
ax = axes[1]; robot(ax)
arm2, plate2, shaft2 = rot(arm, piv, a_full), rot(plate, piv, a_full), rot(shaft[None], piv, a_full)[0]
ax.add_patch(Polygon(rot(arm, piv, a_stow), fc="none", ec="#999", ls="--", lw=1))
ax.add_patch(Polygon(arm2, fc="#ccc", ec="k", lw=0.8, alpha=0.8)); ax.add_patch(Polygon(plate2, fc="#e07020", ec="k", lw=0.8))
ax.add_patch(Circle(tuple(shaft2), 0.77, fc="none", ec="k", ls=":", lw=0.8)); ax.add_patch(Circle(tuple(shaft2), 0.16, fc="k"))
ox = first_contact(plate2); flower(ax, ox, balls)
ax.add_patch(Rectangle((0, 5.3), 1.2, 2.2, fc="#e0552b", ec="k", lw=0.8)); ax.text(1.35, 6.4, "poker as drawn\n(1.2 in ahead)", fontsize=7, va="center")
lip = plate2[np.argmax(plate2[:, 0])]
ax.annotate(f"arm {np.degrees(-ang_full):.0f}° down: shaft {shaft2[0]:.1f} in ahead of the face,\nlip {lip[0]:.1f} in ahead, plate {np.degrees(np.arctan2(*(rot(np.array([[1, 0]]), np.zeros(2), a_full)[0][::-1]))):.0f}°", (lip[0], lip[1]), (-8.5, 3.2), fontsize=8, arrowprops=dict(arrowstyle="->"))
ax.set_title("B. Swung the rest of the way, shaft on 0.77 in wheels", fontsize=10)
for ax in axes:
    ax.set_xlim(-9.0, 10.5); ax.set_ylim(-1.2, 19.0); ax.set_aspect("equal"); ax.set_xlabel("inches ahead of the intake face"); ax.grid(True, lw=0.3, alpha=0.5)
axes[0].set_ylabel("inches above the tile")
fig.suptitle(f"The coach's stick from the flowerextractordown export: pivot ({piv[0]}, {piv[1]}), shaft {L:.2f} in from the pivot (9.09 in Robot Copy 1), plate flat on the shaft", fontsize=10)
fig.tight_layout(); fig.savefig(os.path.join(HERE, "coach_stick_down.png"), dpi=110)
pf = plate2; lipx = pf[:, 0].max()
json.dump({"lip_ahead": float(lipx), "shaft_full": shaft2.tolist(), "arm_full_deg": float(np.degrees(-ang_full)), "profile": [[float(lipx - x), float(z)] for x, z in pf]}, open(os.path.join(HERE, "page", "coach_down_profile.json"), "w"))
print(f"full down: arm {np.degrees(-ang_full):.1f} deg, shaft ({shaft2[0]:.2f}, {shaft2[1]:.2f}), lip {lipx:.2f} ahead of the face, plate lowest {pf[:,1].min():.2f}, FLOWER first contact at {ox:.2f}")
