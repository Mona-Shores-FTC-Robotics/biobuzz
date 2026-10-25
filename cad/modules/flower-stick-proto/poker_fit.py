"""The poker bracket on the intake's real front: the two channels and the motor pulled from the export (page/channels.py,
meshes in the face frame), with the bracket where it sits. Two views."""
import os, numpy as np, trimesh
import matplotlib; matplotlib.use("Agg"); import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
HERE = os.path.dirname(os.path.abspath(__file__)); SC = "/tmp/claude-0/-home-user-biobuzz/ab6cd868-9c1c-57db-97a7-bfb9362d4a20/scratchpad/robot4"
IN = 25.4
parts = []
for fn, col in (("chan_9_Hole_Lowside_U_Channel_GB_.stl", (0.75, 0.75, 0.75)), ("chan_11_Hole_Mini_U_Channel_GB_.stl", (0.6, 0.6, 0.65)), ("chan_5000_0002_0001_body.stl", (0.25, 0.4, 0.8))):
    parts.append((trimesh.load(os.path.join(SC, fn)), col))
b = trimesh.load(os.path.join(HERE, "stl", "poker_plate.stl")); b.apply_scale(1 / IN); b.apply_translation([0, 0, 5.61])   # the bracket's frame: mm, x ahead of the face, z from the web top
parts.append((b, (0.88, 0.33, 0.17)))
fig = plt.figure(figsize=(14, 6))
for k, (elev, azim, title) in enumerate(((28, -55, "from the front, above: the bracket on the flat channel, its back on the standing channel"), (0, -90, "from the side (the motor, blue, hangs inside the flat channel)"))):
    ax = fig.add_subplot(1, 2, k + 1, projection="3d")
    for m, col in parts:
        tri = m.vertices[m.faces]; n = m.face_normals; shade = 0.5 + 0.5 * np.clip(n @ np.array([0.3, -0.5, 0.8]), 0, 1)
        ax.add_collection3d(Poly3DCollection(tri, facecolors=np.c_[col[0] * shade, col[1] * shade, col[2] * shade, np.ones(len(tri))], edgecolors="none"))
    ax.set_xlim(-4, 3); ax.set_ylim(-3.5, 3.5); ax.set_zlim(3.5, 8.5); ax.set_box_aspect((7, 7, 5)); ax.view_init(elev=elev, azim=azim); ax.set_axis_off(); ax.set_title(title, fontsize=9)
fig.suptitle("The poker bracket on the intake's front, from the export (intake lowered): inches ahead of the face, across, up", fontsize=10)
fig.tight_layout(); fig.savefig(os.path.join(HERE, "poker_fit.png"), dpi=120); print("poker_fit.png")
