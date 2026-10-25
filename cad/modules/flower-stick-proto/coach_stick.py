"""The coach's flower stick from the October 25 CAD (Robot Copy 1), swung to the ground on its own arm geometry, next to the
same plate keyed to lie flat and to our mountain blade on his arms. Inches ahead of the intake face and up from the tile.

    python3 cad/modules/flower-stick-proto/coach_stick.py      # coach_stick_as_drawn.png

page/coach_stick.json is measured from the STEP (page/stick_mesh.py, scratch): the arms pivot 2.37 in behind the face and
7.89 in up, the shaft across their tips is 9.09 in from the pivot, the plate (Part 4, 7.9 in wide, 0.48 in thick, 3.26 in
long) hangs from the shaft; "deployed" swings the arm until the shaft is 0.77 in up, where our sled wheels hold it.
"""
import os, json, numpy as np
import matplotlib; matplotlib.use("Agg"); import matplotlib.pyplot as plt
from matplotlib.patches import Polygon, Circle, Rectangle
from shapely.geometry import Polygon as SPoly, Point
HERE = os.path.dirname(os.path.abspath(__file__))
C = json.load(open(os.path.join(HERE, "page", "coach_stick.json")))
MOUNTAIN = json.load(open(os.path.join(HERE, "page", "mountain_profile.json")))
piv = np.array(C["pivot"]); L = C["L"]; rot = C["rot"]; sh0 = np.array(C["shaft_stowed"]); sh1 = np.array(C["shaft_deployed"])
stowed = np.array(C["stowed_outline_centre"]); deployed = np.array(C["deployed_outline"]["-0.43"])
STOP = 1.18
def rot_about(pts, centre, ang):
    c, s = np.cos(ang), np.sin(ang); d = np.asarray(pts) - centre
    return centre + np.c_[c * d[:, 0] - s * d[:, 1], s * d[:, 0] + c * d[:, 1]]
def arm_bar(angle_dir, width=1.26, back=1.5, fwd=9.8):
    d = np.asarray(angle_dir) / np.hypot(*angle_dir); n = np.array([-d[1], d[0]]) * width / 2
    a, b = piv - d * back, piv + d * fwd
    return np.array([a + n, b + n, b - n, a - n])
def first_contact(poly, ball=(-0.29, 1.75), r=1.4):
    """the FLOWER origin (ahead of the face) at which the driving-in blade first touches the bottom ball"""
    P = SPoly(poly); ox = 12.0
    while ox > -5 and P.distance(Point(ox + ball[0], ball[1])) > r: ox -= 0.01
    return ox
def flower(ax, ox, balls):
    """the FLOWER's centreline section with its origin (under the bottom POLLEN) at ox ahead of the face"""
    ax.add_patch(Rectangle((ox - 2.36, 0), 4.72, 0.35, color="#777"))                           # the ring
    ax.add_patch(Rectangle((ox - 1.54, 0), 3.08, 0.35, color="#dde"))                            # its hole
    ax.add_patch(Rectangle((ox + STOP, 0.35), 1.22, 3.75, color="#999", alpha=0.6))               # the posts (uprights)
    for sg in (1, -1):
        x0, x1 = sorted([ox + sg * 1.71, ox + sg * 2.41]); ax.add_patch(Rectangle((x0, 3.90), x1 - x0, 0.35, color="#555"))   # the collar
        ax.add_patch(Rectangle((ox + sg * 1.72 - 0.525, 4.25), 1.05, 4.5, color="#8a8", alpha=0.5))                              # the pipes
    for (bx, bz) in balls: ax.add_patch(Circle((ox + bx, bz), 1.4, fc="#e8d44a", ec="k", alpha=0.75))
    ax.text(ox, -0.6, "FLOWER", ha="center", fontsize=8)
def robot(ax, wheel_z):
    ax.plot([0, 0], [0, 18], color="#444", lw=1, ls=":")                                           # the intake face
    ax.add_patch(Circle((-0.94, wheel_z), 0.94, fc="#bbb", ec="k", lw=0.8))                       # the intake wheels (axle 4.67 up in the CAD)
    ax.add_patch(Rectangle((-1.9, 7.03), 1.9, 0.48, color="#555"))                                # the intake's top channel
    ax.add_patch(Circle(tuple(piv), 0.16, fc="k")); ax.text(piv[0] - 0.3, piv[1] + 0.3, "pivot", ha="right", fontsize=8)
    ax.plot([-8, 0], [0, 0], color="k", lw=1)
fig, axes = plt.subplots(1, 3, figsize=(16, 7.2), sharey=True)
balls = [(-0.29, 1.75), (0.35, 4.48)]
# A: as drawn, keyed to the arm
ax = axes[0]; robot(ax, 4.67)
ax.add_patch(Polygon(arm_bar(C["stowedDir"] if "stowedDir" in C else sh0 - piv), fc="none", ec="#999", ls="--", lw=1))
ax.add_patch(Polygon(stowed, fc="none", ec="#c60", ls="--", lw=1))
ax.add_patch(Polygon(arm_bar(sh1 - piv), fc="#ccc", ec="k", lw=0.8, alpha=0.8))
lip_a = deployed[:, 0].max(); ox_a = first_contact(deployed); flower(ax, ox_a, balls)
ax.add_patch(Polygon(deployed, fc="#e07020", ec="k", lw=0.8))
ax.add_patch(Circle(tuple(sh1), 0.77, fc="none", ec="k", ls=":", lw=0.8)); ax.add_patch(Circle(tuple(sh1), 0.16, fc="k"))
ax.annotate("the plate's face leans 49° toward the FLOWER:\nthe ball meets an overhang, and is pushed down", (lip_a - 0.3, 2.6), (-7.8, 11.5), fontsize=9, arrowprops=dict(arrowstyle="->"))
ax.text(piv[0] + 2.0, piv[1] - 3.4, "arm (at the sides,\n5.4 in out)", fontsize=8, rotation=-52, ha="center", va="center")
low = arm_bar(sh1 - piv)[:, 1].min(); ax.plot([2.5, 5.5], [0, 0], color="#c33", lw=2)
ax.annotate(f"the channel runs 1 in past the shaft:\nits corner is {-low:.1f} in below the tile here.\nCut it back to the shaft hole.", (sh1[0] + 0.9, low), (3.2, 13.0), fontsize=8, color="#c33", arrowprops=dict(arrowstyle="->", color="#c33"))
ax.set_title("A. The plate as drawn, keyed to the hex shaft,\nswung until the shaft is 0.77 in up", fontsize=10)
# B: the same plate keyed to lie flat
ax = axes[1]; robot(ax, 4.67)
flat = stowed + (sh1 - sh0)                                                                        # the stowed outline, carried to the deployed shaft
need = rot_about(stowed, sh0, -rot)                                                               # what the stowed CAD must show for that
ax.add_patch(Polygon(arm_bar(sh0 - piv), fc="none", ec="#999", ls="--", lw=1)); ax.add_patch(Polygon(need, fc="none", ec="#c60", ls="--", lw=1))
ax.add_patch(Polygon(arm_bar(sh1 - piv), fc="#ccc", ec="k", lw=0.8, alpha=0.8))
lip_b = flat[:, 0].max(); ox_b = first_contact(flat); flower(ax, ox_b, balls)
ax.add_patch(Polygon(flat, fc="#e07020", ec="k", lw=0.8)); ax.add_patch(Circle(tuple(sh1), 0.77, fc="none", ec="k", ls=":", lw=0.8)); ax.add_patch(Circle(tuple(sh1), 0.16, fc="k"))
ax.annotate("stowed, the plate would stand up\n49° off the arm tips, bump down", (need[:, 0].mean(), need[:, 1].mean()), (-7.5, 12.0), fontsize=9, arrowprops=dict(arrowstyle="->"))
ax.annotate("lying flat: a 0.48 in bump\nbehind a 0.1 in lip", (sh1[0] - 0.3, 1.05), (-7.8, 3.2), fontsize=9, arrowprops=dict(arrowstyle="->"))
ax.set_title("B. The same plate turned 131° on the shaft,\nso it lies flat at the ground", fontsize=10)
# C: our mountain blade on his arms, with the poker
ax = axes[2]; robot(ax, 2.89)
ax.add_patch(Polygon(arm_bar(sh0 - piv), fc="none", ec="#999", ls="--", lw=1))
ax.add_patch(Polygon(arm_bar(sh1 - piv), fc="#ccc", ec="k", lw=0.8, alpha=0.8))
lip_c = sh1[0] + 0.53
prof = np.array([[lip_c - x, z] for x, z in MOUNTAIN]); body = np.vstack([prof, [[prof[-1, 0], 0.52], [lip_c, 0.52]]])
ox_c = first_contact(body); flower(ax, ox_c, balls)
ax.add_patch(Polygon(body, fc="#e07020", ec="k", lw=0.8)); ax.add_patch(Circle(tuple(sh1), 0.77, fc="none", ec="k", ls=":", lw=0.8)); ax.add_patch(Circle(tuple(sh1), 0.16, fc="k"))
ax.add_patch(Rectangle((0, 5.3), 1.2, 2.2, fc="#e0552b", ec="k", lw=0.8)); ax.text(1.35, 6.4, "poker\n1.2 in ahead\n5.3–7.5 up", fontsize=8, va="center")
ax.annotate("intake wheels lowered to 2.89 in\n(the CAD has the axle at 4.67)", (-0.94, 2.89), (-7.8, 4.6), fontsize=8, arrowprops=dict(arrowstyle="->"))
ax.set_title("C. Our mountain blade on his arms, keyed flat,\nand the poker on the intake's top channel", fontsize=10)
for ax in axes:
    ax.set_xlim(-8.5, 9.0); ax.set_ylim(-1.2, 19.0); ax.set_aspect("equal"); ax.set_xlabel("inches ahead of the intake face"); ax.grid(True, lw=0.3, alpha=0.5)
axes[0].set_ylabel("inches above the tile")
fig.suptitle("The coach's flower-stick arms (October 25 CAD): pivot 2.37 in behind the face, 7.89 in up; 9.09 in to the shaft; the shaft lands 3.28 in ahead of the face on 0.77 in wheels. Each drawn as the blade first touches the bottom ball.", fontsize=10)
fig.tight_layout(); fig.savefig(os.path.join(HERE, "coach_stick_as_drawn.png"), dpi=110)
print("FLOWER origin at first contact: A", round(ox_a, 2), "B", round(ox_b, 2), "C", round(ox_c, 2)); print("lips ahead of face: A", round(lip_a, 2), "B", round(lip_b, 2), "C", round(lip_c, 2), "| flat plate z", flat[:, 1].min().round(2), flat[:, 1].max().round(2), "x", flat[:, 0].min().round(2), flat[:, 0].max().round(2))
