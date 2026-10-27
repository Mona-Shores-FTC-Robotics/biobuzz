"""Pictures of the poker U (poker_u.py): the part from the front-left, and a side section of it on the intake's front channel
with the motor inside the channel and the folded blade's lip under the poke. -> poker_u.png"""
import os, numpy as np, trimesh
import matplotlib; matplotlib.use("Agg"); import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
from matplotlib.patches import Rectangle, Polygon, Circle, Ellipse
HERE = os.path.dirname(os.path.abspath(__file__))
fig = plt.figure(figsize=(13, 5.2))
m = trimesh.load(os.path.join(HERE, "stl", "poker_u.stl")); ax = fig.add_subplot(1, 2, 1, projection="3d")
tri = m.vertices[m.faces]; n = m.face_normals; shade = 0.55 + 0.45 * np.clip(n @ np.array([0.5, -0.5, 0.7]), 0, 1)
ax.add_collection3d(Poly3DCollection(tri, facecolors=np.c_[0.88 * shade, 0.45 * shade, 0.12 * shade, np.ones(len(tri))], edgecolors="none"))
lo, hi = m.bounds; c = (lo + hi) / 2; r = (hi - lo).max() / 2
ax.set_xlim(c[0] - r, c[0] + r); ax.set_ylim(c[1] - r, c[1] + r); ax.set_zlim(c[2] - r, c[2] + r); ax.view_init(elev=22, azim=-55); ax.set_axis_off()
ax.set_title("poker_u: an L over the intake's front channel, the roof flat on the web with relief\nover the screws, pegs into the web, no back wall; the poke 1.3 in ahead and its buttress (81 x 80 mm)", fontsize=9)
ax = fig.add_subplot(1, 2, 2)
# the channel, in mm, x ahead of its front face, z up from its web top: 48 deep, web 2.5 thick on top, flanges 12 down
ax.add_patch(Rectangle((-48, -12), 48, 12, fc="#c8c8c8", ec="k", lw=0.8)); ax.add_patch(Rectangle((-45.5, -12.5), 43, 10, fc="white", ec="none"))
ax.add_patch(Ellipse((-24, -6.5), 35.6, 35.6, fc="#5b7fd6", ec="k", lw=0.6, alpha=0.9)); ax.text(-24, -27, "intake motor\n(inside the channel)", ha="center", fontsize=7, color="white")
ax.text(-24, -46, "the intake's front channel: web on top,\n12 mm flanges down, 4 mm holes in the flanges", ha="center", fontsize=7)
# the L: the front wall down the lip, the roof flat on the web with relief holes over the screws, the poke, the buttress
U = "#e0552b"; ax.add_patch(Rectangle((-38, 0), 42.3, 4, fc=U, ec="k", lw=0.8)); ax.add_patch(Rectangle((0.3, -12), 4, 16, fc=U, ec="k", lw=0.8))
ax.add_patch(Rectangle((4.3, 2.4), 28.7, 45.7, fc=U, ec="k", lw=0.8)); ax.add_patch(Polygon([[-36, 4], [4.3, 4], [4.3, 48.1]], fc=U, ec="k", lw=0.8))
for hx in (-40, -8): ax.add_patch(Rectangle((hx - 5, -0.5), 10, 5, fc="white", ec="k", lw=0.5, ls="--"))   # the relief holes over the screws
ax.text(-24, -27, "the screws on the channel's top\nstand up through relief holes", ha="center", fontsize=7, color="white")
for px in (-32.2, -15.8): ax.add_patch(Rectangle((px - 1.95, -2), 3.9, 2, fc="#1f5fbf", ec="k", lw=0.5))   # the pegs, 2 mm into the web's holes
ax.text(-24, 7.5, "pegs into the web's holes", ha="center", fontsize=7, color="#1f5fbf")
# the folded blade's lip under the poke (0.4 mm below the web top, 0.8 to 2.1 in ahead)
ax.add_patch(Rectangle((20, -14), 34, 13.6, fc="#999", ec="none", alpha=0.5)); ax.text(37, -20, "the stowed blade's lip,\n2.8 mm under the poke", ha="center", fontsize=7)
ax.annotate("a zip tie through each wall hole and the lip hole behind it,\nunder the lip's bottom edge and back out; two more down the roof holes", (2.3, -8), (12, -34), fontsize=7, arrowprops=dict(arrowstyle="->"))
ax.annotate("the buttress: the push goes\ndown it, into the roof and the\nfront wall, onto the lip", (-12, 20), (-62, 40), fontsize=7, arrowprops=dict(arrowstyle="->"))
ax.annotate("", (33, 54), (0, 54), arrowprops=dict(arrowstyle="<->")); ax.text(16, 56, "33 mm = 1.3 in ahead of the lip's face", ha="center", fontsize=7)
ax.annotate("", (40, 48.1), (40, 2.4), arrowprops=dict(arrowstyle="<->")); ax.text(42, 25, "5.7 to 7.5 in up\n(poker_u_hi: web top at 6.55)", fontsize=7, va="center")
ax.set_xlim(-70, 75); ax.set_ylim(-50, 62); ax.set_aspect("equal"); ax.set_xlabel("mm, + ahead of the channel's face"); ax.set_title("from the side, on the channel", fontsize=9)
fig.tight_layout(); fig.savefig(os.path.join(HERE, "poker_u.png"), dpi=120); print("poker_u.png")
