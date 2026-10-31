"""Draws parts.png: the mount in 3D, and from the wall side over goBILDA's hole pattern (cad/beam-mount/README.md).

    pip install trimesh manifold3d matplotlib
    python3 cad/beam-mount/render.py
"""
import os

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
import trimesh

import parts

HERE = os.path.dirname(os.path.abspath(__file__))


def pattern(ax):
    """goBILDA face pattern round the beam's hole and its neighbour: 14 mm holes 24 mm apart, 4.5 mm at +-8 mm."""
    for cx in (0.0, -24.0):
        ax.add_patch(plt.Circle((cx, 0), 7.0, fill=False, color="tab:blue"))
        for dx in (-8, 8):
            for dy in (-8, 8):
                ax.add_patch(plt.Circle((cx + dx, dy), 2.25, fill=False, color="tab:blue"))


def main():
    fig = plt.figure(figsize=(12, 5))
    for row, (name, make) in enumerate((("beam_mount", parts.beam_mount),)):
        m = make()
        ax = fig.add_subplot(1, 2, 1, projection="3d")
        ax.add_collection3d(Poly3DCollection(m.triangles, facecolor="0.75", edgecolor="0.35", linewidths=0.1))
        lo, hi = m.bounds
        c, r = (lo + hi) / 2, (hi - lo).max() / 2
        ax.set_xlim(c[0] - r, c[0] + r); ax.set_ylim(c[1] - r, c[1] + r); ax.set_zlim(c[2] - r, c[2] + r)
        ax.view_init(35, -60); ax.set_title(f"{name}.stl")
        ax2 = fig.add_subplot(1, 2, 2)
        segs = trimesh.intersections.mesh_plane(m, plane_normal=[0, 0, 1], plane_origin=[0, 0, 0.5])
        for s in segs:
            ax2.plot(s[:, 0], s[:, 1], "k", lw=1)
        pattern(ax2)
        ax2.plot(0, 0, "r+", ms=14)
        ax2.set_aspect("equal"); ax2.grid(alpha=0.3)
        ax2.set_title("Underside at the wall (black) over goBILDA's holes (blue); + = beam")
    plt.tight_layout()
    plt.savefig(os.path.join(HERE, "parts.png"), dpi=90)


if __name__ == "__main__":
    main()
