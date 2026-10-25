"""Pictures of the poker saddle (poker_clip.py): from the front-left, and the side section on the channel."""
import os, numpy as np, trimesh
import matplotlib; matplotlib.use("Agg"); import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
from matplotlib.patches import Rectangle, Polygon
HERE = os.path.dirname(os.path.abspath(__file__))
fig = plt.figure(figsize=(12, 4.8))
m = trimesh.load(os.path.join(HERE, "stl", "poker_clip.stl")); ax = fig.add_subplot(1, 2, 1, projection="3d")
tri = m.vertices[m.faces]; n = m.face_normals; shade = 0.55 + 0.45 * np.clip(n @ np.array([0.4, -0.6, 0.7]), 0, 1)
ax.add_collection3d(Poly3DCollection(tri, facecolors=np.c_[0.88 * shade, 0.45 * shade, 0.12 * shade, np.ones(len(tri))], edgecolors="none"))
lo, hi = m.bounds; c = (lo + hi) / 2; r = (hi - lo).max() / 2
ax.set_xlim(c[0] - r, c[0] + r); ax.set_ylim(c[1] - r, c[1] + r); ax.set_zlim(c[2] - r, c[2] + r); ax.view_init(elev=22, azim=-55); ax.set_axis_off(); ax.set_title("poker_clip: the saddle, from the front-left", fontsize=9)
ax = fig.add_subplot(1, 2, 2)
ax.add_patch(Rectangle((-48, -12.2), 48, 12.2, fc="#999", ec="k")); ax.add_patch(Rectangle((-46, -12.2), 44, 10.2, fc="white", ec="none"))   # the channel, open below
ax.text(-24, -6, "intake top channel\n(9-hole lowside U, web up)", ha="center", va="center", fontsize=7)
sect = [(-52.3, 0), (4.3, 0), (4.3, 4), (-52.3, 4), (-52.3, -12.5)]                                            # the saddle's section (simplified)
ax.add_patch(Polygon([[-52.3, 4], [4.3, 4], [4.3, -15], [-2.5, -15], [-2.5, -12.5], [0.3, -12.5], [0.3, 0], [-48.3, 0], [-48.3, -12.5], [-52.3, -12.5]], fc="#e0552b", ec="k", lw=0.8, closed=True))
ax.add_patch(Rectangle((0, 2.3), 33.0, 45.7, fc="#e0552b", ec="k", lw=0.8)); ax.add_patch(Polygon([[0, 4], [-18, 4], [0, 22]], fc="#e0552b", ec="k", lw=0.8))
ax.add_patch(Rectangle((-24 - 6.9, -6), 13.8, 6, fc="#e0552b", ec="k", lw=0.8))
ax.annotate("", (33, -20), (0, -20), arrowprops=dict(arrowstyle="<->")); ax.text(16, -24, "33 mm = 1.3 in ahead of the face", ha="center", fontsize=7)
ax.annotate("", (38, 48), (38, 2.3), arrowprops=dict(arrowstyle="<->")); ax.text(40, 25, "5.7 to 7.5 in up\n(intake lowered)", fontsize=7, va="center")
ax.annotate("lip hooks under\nthe front flange", (-1, -14), (12, -36), fontsize=7, arrowprops=dict(arrowstyle="->"))
ax.annotate("back wall behind\nthe back flange", (-50, -8), (-62, -36), fontsize=7, arrowprops=dict(arrowstyle="->"))
ax.annotate("peg in the web's\ncentre 14 mm hole", (-24, -3), (-45, 30), fontsize=7, arrowprops=dict(arrowstyle="->"))
ax.set_xlim(-70, 60); ax.set_ylim(-42, 56); ax.set_aspect("equal"); ax.set_xlabel("mm, + ahead of the intake face"); ax.set_title("on the channel, from the side: hook the lip, rotate down", fontsize=9)
fig.tight_layout(); fig.savefig(os.path.join(HERE, "poker_clip.png"), dpi=120); print("poker_clip.png")
