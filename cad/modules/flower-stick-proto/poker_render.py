"""Pictures of the printed poker (poker.py): each variant from the front-left, and how it sits on the intake's top channel."""
import os, numpy as np, trimesh
import matplotlib; matplotlib.use("Agg"); import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
from matplotlib.patches import Rectangle, Polygon
HERE = os.path.dirname(os.path.abspath(__file__))
fig = plt.figure(figsize=(13, 4.6))
for k, (mode, title) in enumerate([("hang", "poker_hang: block hangs below the web (intake raised, web top 7.51 in up)"), ("stand", "poker_stand: block rises above the web (intake lowered 1.8 in)")]):
    m = trimesh.load(os.path.join(HERE, "stl", f"poker_{mode}.stl")); ax = fig.add_subplot(1, 3, k + 1, projection="3d")
    tri = m.vertices[m.faces]; n = m.face_normals; shade = 0.55 + 0.45 * np.clip(n @ np.array([0.4, -0.6, 0.7]), 0, 1)
    pc = Poly3DCollection(tri, facecolors=np.c_[0.88 * shade, 0.45 * shade, 0.12 * shade, np.ones(len(tri))], edgecolors="none"); ax.add_collection3d(pc)
    lo, hi = m.bounds; c = (lo + hi) / 2; r = (hi - lo).max() / 2
    ax.set_xlim(c[0] - r, c[0] + r); ax.set_ylim(c[1] - r, c[1] + r); ax.set_zlim(c[2] - r, c[2] + r); ax.view_init(elev=22, azim=-50); ax.set_axis_off(); ax.set_title(title, fontsize=8.5)
ax = fig.add_subplot(1, 3, 3)
# side view, mm: the channel's web and front face, the poker hanging, the intake face line, the ball's reach
ax.add_patch(Rectangle((-48, -12), 48, 12, fc="#999", ec="k"))                         # the channel: 48 deep, 12 tall (its front face at x=0)
ax.add_patch(Rectangle((-42, -10), 36, 8, fc="white", ec="none"))                      # hollow (low-side U)
ax.add_patch(Polygon([[-48, 0], [0, 0], [0, 6], [-48, 6]], fc="#e0552b", ec="k", lw=0.8))         # the flange on the web
ax.add_patch(Polygon([[0, 6], [30.5, 6], [30.5, -56], [0, -56]], fc="#e0552b", ec="k", lw=0.8))   # the block, down the channel's face
ax.add_patch(Polygon([[0, 6], [-10, 6], [0, 16]], fc="#e0552b", ec="k", lw=0.8))                   # the gusset
for x in (-32, -16): ax.plot([x, x], [-14, 8], color="k", lw=1.2)                                   # the web bolts (M4, through the slots)
ax.plot([-4, 30], [-6, -6], color="k", lw=1.2); ax.text(31, -6, "M4 into the channel face", fontsize=7, va="center")
ax.annotate("", (30.5, -62), (0, -62), arrowprops=dict(arrowstyle="<->")); ax.text(15, -66, "30.5 mm = 1.2 in ahead of the face", ha="center", fontsize=7)
ax.annotate("", (36, 6), (36, -56), arrowprops=dict(arrowstyle="<->")); ax.text(38, -25, "56 mm\n(5.3 to 7.5 in up)", fontsize=7, va="center")
ax.text(-24, 10, "intake top channel (9-hole low-side U)", ha="center", fontsize=7)
ax.set_xlim(-55, 75); ax.set_ylim(-72, 20); ax.set_aspect("equal"); ax.set_xlabel("mm, + ahead of the intake face"); ax.set_title("poker_hang on the channel, from the side", fontsize=8.5)
fig.tight_layout(); fig.savefig(os.path.join(HERE, "poker_part.png"), dpi=120); print("poker_part.png")
