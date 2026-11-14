"""Draws parts.png: the two halves on stand-ins for the arm, its wheel, the shaft and the blue wall; the arm half alone;
the end view against the floor; and how far the halves telescope.

    pip install cadquery trimesh manifold3d matplotlib
    python3 cad/stick-shoulder-19429/parts.py && python3 cad/stick-shoulder-19429/render.py
"""
import importlib.util
import math
import os

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import trimesh

import parts

HERE = os.path.dirname(os.path.abspath(__file__))
_spec = importlib.util.spec_from_file_location("beam_render", os.path.join(HERE, "..", "beam-mount", "render.py"))
_beam = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_beam)
raster, show = _beam.raster, _beam.show          # the beam mount's small z-buffer renderer

BLUE, ORANGE, METAL, DARK, WHITE = (0.25, 0.45, 0.9), (0.95, 0.45, 0.15), (0.75, 0.77, 0.8), (0.15, 0.15, 0.15), (0.92, 0.92, 0.92)


def along_y(r, y0, y1, x=0.0, z=0.0, sections=64):
    c = trimesh.creation.cylinder(radius=r, height=y1 - y0, sections=sections)
    c.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [1, 0, 0]))
    c.apply_translation([x, (y0 + y1) / 2, z])
    return c


def plate(r0, r1, width, y0, y1):
    """A stand-in plate along the arm's line (the arm plate, or the blue wall)."""
    a = math.radians(parts.a_arm())
    b = trimesh.creation.box(extents=[r1 - r0, y1 - y0, width])
    b.apply_transform(trimesh.transformations.rotation_matrix(a, [0, -1, 0]))
    b.apply_translation([math.cos(a) * (r0 + r1) / 2, (y0 + y1) / 2, math.sin(a) * (r0 + r1) / 2])
    return b


def main():
    arm = trimesh.load(os.path.join(HERE, "shoulder_arm_half.stl"))
    wall = trimesh.load(os.path.join(HERE, "shoulder_wall_half.stl"))
    span = parts.SPAN
    wall_at = wall.copy(); wall_at.apply_translation([0, span - parts.SPAN_MIN, 0])
    r = parts.WHEEL_D / 2
    stand_ins = [
        (plate(-12, 110, 24, -3, 0), METAL),                                   # arm plate (outside the part)
        (along_y(4, -10, span + 60), METAL),                                    # 8 mm shaft
        (along_y(9, 2, 14), METAL),                                             # clamp hub
        (along_y(r, 16, 24), DARK), (along_y(r - 3, 24, 32), METAL),            # wheel and its hub
        (plate(-14, 70, 26, span, span + 3), BLUE),                             # blue wall
        (along_y(14, span + 3, span + 60, x=18, z=-6, sections=4), ORANGE),     # the orange bar, roughly
    ]
    fig = plt.figure(figsize=(16, 5.5))
    show(fig.add_subplot(1, 4, 1), raster(stand_ins + [(arm, (0.95, 0.75, 0.2)), (wall_at, (0.35, 0.75, 0.4))], 25, -35),
         f"On stand-ins, span {span:.0f} mm, from the front\nyellow arm half, green wall half")
    show(fig.add_subplot(1, 4, 2), raster(stand_ins[:5] + [(arm, (0.95, 0.75, 0.2))], 30, -150),
         "Arm half alone, from inboard:\nthe inner shell over the wheel")
    ax = fig.add_subplot(1, 4, 3)
    for m, col in ((arm, "goldenrod"), (wall_at, "green")):
        for s in trimesh.intersections.mesh_plane(m, plane_normal=[0, 1, 0], plane_origin=[0, 2.0 if m is arm else span - 2.0, 0]):
            ax.plot(s[:, 0], s[:, 2], color=col, lw=1)
    t = np.linspace(0, 2 * math.pi, 100)
    ax.plot(r * np.cos(t), r * np.sin(t), "k", lw=1)
    ax.axhline(-r, color="0.4", lw=2)
    a = math.radians(parts.a_arm())
    ax.plot([0, 110 * math.cos(a)], [0, 110 * math.sin(a)], color="0.5", lw=6, alpha=0.4)
    ax.annotate("FLOWER this way →", (-95, -r + 3), fontsize=8)
    ax.annotate(f"front edge {parts.floor_gap():.0f} mm\nabove the floor", (-95, -r + 10), fontsize=8)
    ax.set_aspect("equal"); ax.grid(alpha=0.3)
    ax.set_title("End caps (yellow arm, green wall) over the wheel (black)\nfloor grey, arm's line thick grey", fontsize=10)
    ax = fig.add_subplot(1, 4, 4)
    ys = np.linspace(parts.SPAN_MIN, parts.SPAN_MAX, 2)
    overlap = (parts.SPAN_MIN - parts.CAP - 1) - (ys - parts.SPAN_MIN + parts.CAP + 1)
    ax.plot(ys, overlap, "k")
    ax.axvline(span, color="tab:red", ls="--"); ax.annotate(" photo estimate", (span, overlap.max() * 0.9), color="tab:red", fontsize=8)
    ax.set_xlabel("arm plate to blue wall, mm"); ax.set_ylabel("shells overlap, mm"); ax.grid(alpha=0.3)
    ax.set_title("The halves fit any span in this range", fontsize=10)
    plt.tight_layout()
    plt.savefig(os.path.join(HERE, "parts.png"), dpi=90)


if __name__ == "__main__":
    main()
