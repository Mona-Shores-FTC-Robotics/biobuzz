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
ax.set_title("poker_u: an L over the intake's front channel, the roof on rails 5 mm above the web,\nno back wall; the poke 1.3 in ahead and its buttress (81 x 104 x 60 mm, 72 cm3)", fontsize=9)
ax = fig.add_subplot(1, 2, 2)
# the channel, in mm, x ahead of its front face, z up from its web top: 48 deep, web 2.5 thick on top, flanges 12 down
ax.add_patch(Rectangle((-48, -12), 48, 12, fc="#c8c8c8", ec="k", lw=0.8)); ax.add_patch(Rectangle((-45.5, -12.5), 43, 10, fc="white", ec="none"))
ax.add_patch(Ellipse((-24, -6.5), 35.6, 35.6, fc="#5b7fd6", ec="k", lw=0.6, alpha=0.9)); ax.text(-24, -27, "intake motor\n(inside the channel)", ha="center", fontsize=7, color="white")
ax.text(-24, -46, "the intake's front channel: web on top,\n12 mm flanges down, 4 mm holes in the flanges", ha="center", fontsize=7)
# the L: the front wall down the lip, two rails on the web's clear outer strips, the roof 5 mm up, the poke, the buttress
U = "#e0552b"; ax.add_patch(Rectangle((-48, 5), 52.3, 4, fc=U, ec="k", lw=0.8)); ax.add_patch(Rectangle((0.3, -12), 4, 21, fc=U, ec="k", lw=0.8))
ax.add_patch(Rectangle((-4.5, 0), 4.8, 5, fc=U, ec="k", lw=0.8)); ax.add_patch(Rectangle((-48, 0), 4.5, 5, fc=U, ec="k", lw=0.8))
ax.add_patch(Rectangle((4.3, 2.4), 28.7, 45.7, fc=U, ec="k", lw=0.8)); ax.add_patch(Polygon([[-36, 9], [4.3, 9], [4.3, 48.1]], fc=U, ec="k", lw=0.8))
for hx in (-32, -16): ax.add_patch(Rectangle((hx - 3.5, 0), 7, 4, fc="#777", ec="k", lw=0.5))   # bolt heads on the web's pattern, under the roof
ax.text(-24, -27, "bolt heads on the channel's top\npass under the raised roof", ha="center", fontsize=7, color="white")
