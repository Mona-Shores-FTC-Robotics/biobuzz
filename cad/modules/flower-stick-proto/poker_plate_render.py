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
ax.set_xlim(c[0] - r, c[0] + r); ax.set_ylim(c[1] - r, c[1] + r); ax.set_zlim(c[2] - r, c[2] + r); ax.view_init(elev=28, azim=-50); ax.set_axis_off(); ax.set_title("poker_plate, from the front-left: the back plate's two bosses go into the cross channel's holes", fontsize=9)
ax = fig.add_subplot(1, 2, 2)
ax.add_patch(Rectangle((-48, -12.2), 48, 12.2, fc="#999", ec="k")); ax.add_patch(Rectangle((-46, -12.2), 44, 10.2, fc="white", ec="none"))
ax.text(-24, -6, "intake top channel", ha="center", va="center", fontsize=7)
ax.add_patch(Rectangle((-60, -7.4), 12, 32, fc="#bbb", ec="k")); ax.add_patch(Rectangle((-58, -5.4), 8, 28, fc="white", ec="none")); ax.add_patch(Rectangle((-50, -7.4), 2, 32, fc="#bbb", ec="none"))
ax.text(-54, 28, "cross\nchannel", ha="center", fontsize=7)
ax.add_patch(Rectangle((-43, -44), 38, 39, fc="#ddd", ec="#888", ls="--")); ax.text(-24, -26, "intake motor\n(in the cavity,\nnear the centre)", ha="center", va="center", fontsize=7, color="#666")
ax.add_patch(Polygon([[-47.5, 0], [0.3, 0], [0.3, -15], [4.3, -15], [4.3, 6], [-41.5, 6], [-41.5, 24], [-47.5, 24]], fc="#e0552b", ec="k", lw=0.8, closed=True))
ax.add_patch(Rectangle((0, 2.3), 33.0, 45.7, fc="#e0552b", ec="k", lw=0.8)); ax.add_patch(Polygon([[-47.5, 6], [0, 6], [0, 48], [-47.5, 24]], fc="#e0552b", ec="k", lw=0.8))
ax.add_patch(Rectangle((-52.5, 8.6 - 6.9), 5, 13.8, fc="#e0552b", ec="k", lw=0.8))                                  # the boss into the cross channel's hole
ax.plot([-58, -40], [8.6, 8.6], color="k", lw=1.5); ax.add_patch(Rectangle((-57, 6.6), 3, 4, fc="#555"))            # the bolt, nut inside the channel
ax.annotate("wedge behind the poke, full depth,\ninto the back plate, onto the cross channel", (-30, 20), (-70, 50), fontsize=7, arrowprops=dict(arrowstyle="->"))
ax.annotate("boss into the cross channel's 14 mm hole,\nM4 through, nut inside the channel\n(x2, at 1.9 in either side of centre)", (-52, 8.6), (6, -40), fontsize=7, arrowprops=dict(arrowstyle="->"))
ax.annotate("lip over the\nfront flange", (2.3, -12), (14, -8), fontsize=7, arrowprops=dict(arrowstyle="->"))
ax.annotate("", (33, -20), (0, -20), arrowprops=dict(arrowstyle="<->")); ax.text(16, -24, "33 mm = 1.3 in ahead of the face", ha="center", fontsize=7)
ax.annotate("", (38, 48), (38, 2.3), arrowprops=dict(arrowstyle="<->")); ax.text(40, 25, "5.7 to 7.5 in up\n(intake lowered)", fontsize=7, va="center")
ax.set_xlim(-72, 60); ax.set_ylim(-48, 56); ax.set_aspect("equal"); ax.set_xlabel("mm, + ahead of the intake face"); ax.set_title("on the intake, from the side: bolted to the cross channel, seated on the top channel", fontsize=9)
fig.tight_layout(); fig.savefig(os.path.join(HERE, "poker_plate.png"), dpi=120); print("poker_plate.png")
