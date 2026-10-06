"""Draws cad/ramp-hook/parts.svg (tens of MB, not committed; parts.png is a screenshot of it): the printed parts on
the rod, in a shaded 3/4 view, with the block's sizes.

    python3 cad/ramp-hook/render.py         (after parts.py; needs trimesh only)

A small painter's-algorithm renderer, so no CAD program or graphics library is needed to see the parts.
"""
import math
import os
import sys

import numpy as np
import trimesh

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import parts as P  # noqa: E402

IN = P.IN


def placed(name, dx=0.0, dy=0.0, dz=0.0, rz=0.0):
    m = trimesh.load(os.path.join(HERE, name + ".stl"))
    if rz:
        m.apply_transform(trimesh.transformations.rotation_matrix(rz, [0, 0, 1]))
    m.apply_translation([dx, dy, dz])
    return m


def scene():
    """The hook in its frame, mm: x toward the FLOWER (from the block's back edge), y to the robot's right, z up."""
    items = []
    steel, clipc, panelc, darkc = (170, 178, 188), (79, 143, 224), (160, 196, 240), (47, 95, 158)
    items.append((placed("ramp_block", dz=P.BOTTOM), (176, 106, 216)))
    # front shaft: from the left end to the corner block
    y_left, y_right = -P.SIDE_Y, P.SIDE_Y + 4.0
    rod = trimesh.creation.cylinder(radius=P.ROD_D / 2, height=y_right - y_left, sections=32)
    rod.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [1, 0, 0]))
    rod.apply_translation([P.ROD_X, (y_left + y_right) / 2, P.ROD_Z])
    items.append((rod, steel))
    for y in (-P.ARC_R - 4, P.ARC_R + 4):               # clamping collars either side of the block
        c = trimesh.creation.cylinder(radius=11, height=8, sections=32)
        c.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [1, 0, 0]))
        c.apply_translation([P.ROD_X, y + (6 if y > 0 else -6), P.ROD_Z]); items.append((c, steel))
    for y in (-60.0, -130.0, 60.0, 130.0):
        items.append((placed("curtain_clip", dx=P.ROD_X, dy=y, dz=P.ROD_Z - 7.5), clipc))
    for y0, y1 in ((y_left + 6, -P.ARC_R - 16), (P.ARC_R + 16, P.SIDE_Y - 12)):
        panel = trimesh.creation.box(extents=[P.PANEL_T, y1 - y0, 3.5 * IN - 1.3 * IN])
        panel.apply_translation([P.ROD_X, (y0 + y1) / 2, 1.3 * IN + (3.5 * IN - 1.3 * IN) / 2])
        items.append((panel, panelc))
    # side wall: corner block, hinge block, two rods, clips (flipped on the top rod), a panel between
    items.append((placed("corner_block", dz=2.0), darkc))
    items.append((placed("hinge_block", dz=2.0), darkc))
    x0, x1 = P.HINGE_X + 16.0, P.ROD_X + 6.0
    for z in (P.SIDE_Z_LO, P.SIDE_Z_HI):
        r = trimesh.creation.cylinder(radius=P.ROD_D / 2, height=x1 - x0, sections=32)
        r.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [0, 1, 0]))
        r.apply_translation([(x0 + x1) / 2, P.SIDE_Y, z]); items.append((r, steel))
    for x in (-140.0, -50.0):
        items.append((placed("curtain_clip", dx=x, dy=P.SIDE_Y, dz=P.SIDE_Z_LO - 7.5, rz=math.pi / 2), clipc))
        top = trimesh.load(os.path.join(HERE, "curtain_clip.stl"))
        top.apply_transform(trimesh.transformations.rotation_matrix(math.pi, [1, 0, 0]))
        top.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [0, 0, 1]))
        top.apply_translation([x, P.SIDE_Y, P.SIDE_Z_HI + 7.5]); items.append((top, clipc))
    side = trimesh.creation.box(extents=[(P.ROD_X - 14) - (P.HINGE_X + 36), P.PANEL_T, P.SIDE_Z_HI - P.SIDE_Z_LO - 13])
    side.apply_translation([((P.ROD_X - 14) + (P.HINGE_X + 36)) / 2, P.SIDE_Y, (P.SIDE_Z_LO + P.SIDE_Z_HI) / 2])
    items.append((side, panelc))
    return items


def project(v, az=math.radians(-38), el=math.radians(26)):
    ca, sa, ce, se = math.cos(az), math.sin(az), math.cos(el), math.sin(el)
    x, y, z = v[:, 0], v[:, 1], v[:, 2]
    u = x * sa + y * ca
    w = -x * ca + y * sa
    sx = u
    sy = -(z * ce - w * se)
    depth = w * ce + z * se
    return np.stack([sx, sy], 1), depth


def draw(items, ox, oy, k, light=np.array([0.4, -0.5, 0.8]), az=math.radians(-38), el=math.radians(26)):
    light = light / np.linalg.norm(light)
    toward = np.array([-math.cos(az) * math.cos(el), math.sin(az) * math.cos(el), math.sin(el)])   # toward the viewer
    polys = []
    for m, col in items:
        m = m.subdivide_to_size(max_edge=2.5)       # small triangles, so sorting by their centres is right
        pts, depth = project(m.vertices)
        for f, n in zip(m.faces, m.face_normals):
            if float(np.dot(n, toward)) <= 0:       # faces turned away (and the bore's far side) aren't drawn
                continue
            shade = 0.45 + 0.55 * max(0.0, float(np.dot(n, light)))
            c = tuple(int(min(255, ch * shade)) for ch in col)
            polys.append((depth[f].mean(), pts[f], c))
    polys.sort(key=lambda p: p[0])
    out = []
    for _, p, c in polys:
        s = " ".join(f"{ox + a * k:.1f},{oy + b * k:.1f}" for a, b in p)
        out.append(f'<polygon points="{s}" fill="rgb{c}" stroke="rgb{c}" stroke-width="0.4"/>')
    return out


def main():
    W, H = 1500, 900
    o = [f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" font-family="Helvetica,Arial,sans-serif">',
         '<rect width="100%" height="100%" fill="#ffffff"/>']
    t = lambda x, y, s, size=15, fill="#1b222b", anchor="start", w=400: o.append(
        f'<text x="{x}" y="{y}" font-size="{size}" fill="{fill}" text-anchor="{anchor}" font-weight="{w}">{s}</text>')
    t(30, 40, "Ramp hook: printed parts on goBILDA 8 mm shafts", 22, w=700)
    t(30, 64, "Purple: the FLOWER block. Dark blue: corner block and hinge block. Blue: clips. Light blue: 1/16 in polycarbonate. The side wall is a ladder: two shafts, a panel between.", 14, "#6b7682")
    o += draw(scene(), 560, 470, 1.25)
    t(470, 850, "The whole hook, 3/4 view from the FLOWER side: the block in the middle of the front shaft, the side wall on the right, the hinge at the back.", 14, "#6b7682", "middle")
    # the block alone, larger
    block = trimesh.load(os.path.join(HERE, "ramp_block.stl"))
    o += draw([(block, (176, 106, 216))], 1170, 330, 5.0)
    t(1170, 120, "The FLOWER block (ramp_block.stl)", 17, anchor="middle", w=700)
    lines = [f"Front to back {P.DEPTH:.1f} mm (1.4 in); width {2 * P.ARC_R:.0f} mm", f"Height {P.TOP - P.BOTTOM:.1f} mm, top {P.TOP / IN:.2f} in above the tiles",
             f"Bottom {P.BOTTOM / IN:.2f} in above the tiles; flat top {P.FLAT:.1f} mm", f"Curved front: radius {P.ARC_R:.1f} mm (bottom ring hole 2.79 in)",
             f"Rod bore {P.ROD_D + P.FIT:.2f} mm, {P.DEPTH - P.ROD_X:.1f} mm back from the tip", "Two M3 set screws from underneath",
             "Print flat side down, PETG, 5 walls, 40% infill"]
    for i, s in enumerate(lines):
        t(960, 560 + i * 26, s, 15)
    o.append("</svg>")
    open(os.path.join(HERE, "parts.svg"), "w").write("\n".join(o))


if __name__ == "__main__":
    main()
