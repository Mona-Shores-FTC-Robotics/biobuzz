"""Pictures of the poker plate (poker_plate.py): from the front-left, and the side section on the channel."""
import os, numpy as np, trimesh
import matplotlib; matplotlib.use("Agg"); import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
from matplotlib.patches import Rectangle, Polygon, Circle
HERE = os.path.dirname(os.path.abspath(__file__))
fig = plt.figure(figsize=(13, 5))
m = trimesh.load(os.path.join(HERE, "stl", "poker_plate.stl")); ax = fig.add_subplot(1, 2, 1, projection="3d")
tri = m.vertices[m.faces]; n = m.face_normals; shade = 0.55 + 0.45 * np.clip(n @ np.array([0.4, -0.6, 0.7]), 0, 1)
ax.add_collection3d(Poly3DCollection(tri, facecolors=np.c_[0.88 * shade, 0.45 * shade, 0.12 * shade, np.ones(len(tri))], edgecolors="none"))
lo, hi = m.bounds; c = (lo + hi) / 2; r = (hi - lo).max() / 2
ax.set_xlim(c[0] - r, c[0] + r); ax.set_ylim(c[1] - r, c[1] + r); ax.set_zlim(c[2] - r, c[2] + r); ax.view_init(elev=28, azim=-50); ax.set_axis_off(); ax.set_title("poker_plate, from the front-left: two bosses go into the web's bearing holes, beside the motor", fontsize=9)
ax = fig.add_subplot(1, 2, 2)
ax.add_patch(Rectangle((-48, -12.2), 48, 12.2, fc="#999", ec="k")); ax.add_patch(Rectangle((-46, -12.2), 44, 10.2, fc="white", ec="none"))
ax.text(-24, -6, "intake top channel", ha="center", va="center", fontsize=7)
ax.add_patch(Rectangle((-60, -8), 12, 32, fc="#bbb", ec="k")); ax.text(-54, 26, "cross\nchannel", ha="center", fontsize=7)
ax.add_patch(Rectangle((-43, -44), 38, 39, fc="#ddd", ec="#888", ls="--")); ax.text(-24, -26, "intake motor\n(in the cavity,\nnear the centre)", ha="center", va="center", fontsize=7, color="#666")
ax.add_patch(Polygon([[-47.5, 0], [0.3, 0], [0.3, -15], [4.3, -15], [4.3, 6], [-47.5, 6]], fc="#e0552b", ec="k", lw=0.8, closed=True))
ax.add_patch(Rectangle((0, 2.3), 33.0, 45.7, fc="#e0552b", ec="k", lw=0.8)); ax.add_patch(Polygon([[0, 6], [-24, 6], [0, 30]], fc="#e0552b", ec="k", lw=0.8))
ax.add_patch(Rectangle((-24 - 6.9, -6), 13.8, 6, fc="#e0552b", ec="k", lw=0.8)); ax.plot([-24, -24], [-16, 8], color="k", lw=1.5); ax.add_patch(Rectangle((-27, -16), 6, 3, fc="#555"))
ax.annotate("", (33, -20), (0, -20), arrowprops=dict(arrowstyle="<->")); ax.text(16, -24, "33 mm = 1.3 in ahead of the face", ha="center", fontsize=7)
ax.annotate("", (38, 48), (38, 2.3), arrowprops=dict(arrowstyle="<->")); ax.text(40, 25, "5.7 to 7.5 in up\n(intake lowered)", fontsize=7, va="center")
ax.annotate("M4 down through a boss in a\n14 mm hole, nut below (x2, at 1.9 and 2.8 in\non the encoder side, clear of motor and gearbox)", (-24, -12), (8, -40), fontsize=7, arrowprops=dict(arrowstyle="->"))
ax.annotate("rear edge butts the\ncross channel's face", (-47.5, 3), (-66, 40), fontsize=7, arrowprops=dict(arrowstyle="->"))
ax.annotate("lip over the\nfront flange", (2.3, -12), (14, -8), fontsize=7, arrowprops=dict(arrowstyle="->"))
ax.set_xlim(-72, 60); ax.set_ylim(-48, 56); ax.set_aspect("equal"); ax.set_xlabel("mm, + ahead of the intake face"); ax.set_title("on the channel, from the side (the bosses are off-centre, out of the motor's way)", fontsize=9)
fig.tight_layout(); fig.savefig(os.path.join(HERE, "poker_plate.png"), dpi=120); print("poker_plate.png")
