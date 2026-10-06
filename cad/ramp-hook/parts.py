"""Printable parts for the ramp hook's FLOWER block, threaded on a stock rod (cad/ramp-hook/README.md).

    pip install trimesh manifold3d
    python3 cad/ramp-hook/parts.py          # writes ramp_block, curtain_clip, end_block, corner_block, hinge_block, fit_coupon .stl

Millimetres, Z up, each part flat on the bed as it should print. Every size is a constant below, so a part can be
re-run after Thursday's measurements. The block's shape is the one tools/ramp-hook/ramp.py likes best against the
manual's FLOWER (Fig 9-12): 1.4 in front to back, top at 1.3 in, bottom 0.5 in above the tiles, a 0.5 in flat top.
"""
import math
import os

import trimesh
from trimesh.creation import box, cylinder

IN = 25.4
HERE = os.path.dirname(os.path.abspath(__file__))

# ---- the block (tile frame: z above the tiles; x back to front, toward the FLOWER's uprights) ----
BOTTOM = 0.7 * IN            # 17.8: clears the bottom ring (0.43 in) by 0.27 in. ramp.py empties every case from 0.6 to 0.85 in
TOP = 1.35 * IN              # 34.3: under a POLLEN's centre (1.4 in), or the block shoves it back instead of lifting
DEPTH = 1.4 * IN             # 35.6: back edge to the tip of the curved front
FLAT = 0.5 * IN              # flat top behind the front
ARC_R = 1.36 * IN            # 34.5: the front's curve in plan, just inside the bottom ring's hole (2.79 in across)
SLANT = 0.2 * IN             # the curved front leans back this much at the top (about 21°), so it slides under a POLLEN
LAND = 0.12 * IN             # ...above a short vertical strip at the bottom, which is what bears on the uprights
# ---- the rod ----
ROD_D = 8.0                  # goBILDA 8 mm REX shaft (its corners sit on an 8 mm circle) or any 8 mm round shaft
FIT = 0.3                    # bore clearance; print fit_coupon.stl first and change this to suit your printer
ROD_X = DEPTH - 12.0         # rod centre, mm in front of the back edge: 12 mm behind the tip, under the flat top,
                             # and about 4 mm clear of the uprights' faces where the bare shaft passes them
ROD_Z = (BOTTOM + TOP) / 2   # rod centre above the tiles (1.02 in), halfway up the block
SCREW_D = 2.6                # M3 self-tapping set screws from underneath, into the rod
# ---- curtain clip and end block ----
PANEL_T = 1.6                # 1/16 in polycarbonate curtain
CLIP_W = 16.0                # along the rod
END_HOLE_D, END_HOLE_PITCH = 4.2, 16.0   # M4, 16 mm apart: lines up with goBILDA's 8 mm grid of 4 mm holes
# ---- the side wall: a ladder of two 8 mm shafts with a polycarbonate panel clipped between them ----
GAP = 8.0 * IN               # clear space, chassis face to the block's back edge
HINGE_X = -GAP               # the chassis' front face, in this frame (x from the block's back edge)
SIDE_Y = 7.25 * IN - 8.0     # the side wall's rods, 8 mm in from the robot's right edge (14.5 in wide)
SIDE_Z_LO = 12.0             # bottom rod centre above the tiles
SIDE_Z_HI = 4.0 * IN - 7.5   # top rod centre: a clip flipped over it tops out at 4 in
PIVOT_Z = 40.0               # hinge axle above the tiles, clear of the bottom rod's bore
BORE = ROD_D + FIT


def prism_xz(points, y0, y1):
    """A prism from a polygon in the x-z plane, extruded along y from y0 to y1."""
    import shapely.geometry as sg
    poly = sg.Polygon(points)
    m = trimesh.creation.extrude_polygon(poly, y1 - y0)       # polygon in x-y, extruded along +z
    m.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [1, 0, 0]))   # (x, y, z) -> (x, -z, y)
    m.apply_translation([0, y1, 0])
    return m


def rod_hole(length, x, z, d):
    c = cylinder(radius=d / 2, height=length, sections=48)
    c.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [1, 0, 0]))
    c.apply_translation([x, 0, z])
    return c


def ramp_block():
    """Back edge at x 0, curved tip at x DEPTH; z from the part's bottom (BOTTOM above the tiles)."""
    h, slope_run = TOP - BOTTOM, DEPTH - FLAT
    side = [(0, 0), (slope_run, h), (DEPTH, h), (DEPTH, 0)]
    body = prism_xz(side, -ARC_R - 2, ARC_R + 2)
    plan = cylinder(radius=ARC_R, height=h * 3, sections=128)
    plan.apply_translation([DEPTH - ARC_R, 0, h])
    body = body.intersection(plan)
    # the front's slant: a cone round the same axis, full radius up to LAND, SLANT less at the top
    top = h + 2.0
    lean = SLANT / (h - LAND)
    cone = trimesh.creation.revolve([[0, -1.0], [ARC_R, -1.0], [ARC_R, LAND], [ARC_R - lean * (top - LAND), top], [0, top]], sections=128)
    cone.apply_translation([DEPTH - ARC_R, 0, 0])
    body = body.intersection(cone)
    holes = [rod_hole(ARC_R * 3, ROD_X, ROD_Z - BOTTOM, ROD_D + FIT)]
    for y in (-14.0, 14.0):
        s = cylinder(radius=SCREW_D / 2, height=ROD_Z - BOTTOM + 1, sections=24)
        s.apply_translation([ROD_X, y, (ROD_Z - BOTTOM) / 2 - 0.5])
        holes.append(s)
    return body.difference(trimesh.util.concatenate(holes))


def curtain_clip():
    """Slides on the rod outside the FLOWER block and holds a curtain panel upright in a slot."""
    w, d, hgt = CLIP_W, 16.0, 38.0
    body = box(extents=[d, w, hgt]); body.apply_translation([0, 0, hgt / 2])
    bore_z = 7.5
    holes = [rod_hole(w * 3, 0, bore_z, ROD_D + FIT)]
    slot = box(extents=[PANEL_T + 0.25, w * 3, 24.0]); slot.apply_translation([0, 0, hgt - 12.0 + 0.01]); holes.append(slot)
    for z in (hgt - 7, hgt - 17):
        b = cylinder(radius=1.65, height=d * 3, sections=24)
        b.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [0, 1, 0]))
        b.apply_translation([0, 0, z]); holes.append(b)
    s = cylinder(radius=SCREW_D / 2, height=bore_z, sections=24); s.apply_translation([0, 0, bore_z / 2 - 0.5]); holes.append(s)
    return body.difference(trimesh.util.concatenate(holes))


def end_block():
    """Bolts to the inside of the side wall and holds the rod's end at ROD_Z above the tiles."""
    d, w, hgt = 20.0, 14.0, ROD_Z + 12.0
    body = box(extents=[d, w, hgt]); body.apply_translation([0, 0, hgt / 2])
    holes = [rod_hole(w + 0.0, 0, ROD_Z, ROD_D + FIT)]          # blind: the rod stops against the wall side
    holes[0].apply_translation([0, -2.0, 0])
    for z in (hgt - 6, hgt - 6 - END_HOLE_PITCH):
        b = cylinder(radius=END_HOLE_D / 2, height=w * 3, sections=24)
        b.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [1, 0, 0]))
        b.apply_translation([d / 2 - 5, 0, z]); holes.append(b)
    return body.difference(trimesh.util.concatenate(holes))


def x_hole(x0, x1, y, z, d):
    """A bore along x from x0 to x1."""
    c = cylinder(radius=d / 2, height=x1 - x0, sections=48)
    c.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [0, 1, 0]))
    c.apply_translation([(x0 + x1) / 2, y, z])
    return c


def y_hole(y0, y1, x, z, d):
    """A bore along y from y0 to y1."""
    c = cylinder(radius=d / 2, height=y1 - y0, sections=48)
    c.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [1, 0, 0]))
    c.apply_translation([x, (y0 + y1) / 2, z])
    return c


def z_hole(z0, z1, x, y, d):
    c = cylinder(radius=d / 2, height=z1 - z0, sections=24)
    c.apply_translation([x, y, (z0 + z1) / 2])
    return c


def slab(x0, x1, y0, y1, z0, z1):
    b = box(extents=[x1 - x0, y1 - y0, z1 - z0])
    b.apply_translation([(x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2])
    return b


def corner_block():
    """The hook's front-right corner, in the hook's frame (x from the block's back edge, y to the robot's right, z up).
    Holds the front shaft's right end (along y) and the side wall's two rods' front ends (along x), all blind, at
    three different heights so the bores never meet. M3 set screws into each."""
    x0, x1 = ROD_X - 14.0, DEPTH
    y0, y1 = SIDE_Y - 10.0, SIDE_Y + 8.0
    z0, z1 = 2.0, SIDE_Z_HI + 10.0
    body = slab(x0, x1, y0, y1, z0, z1)
    holes = [y_hole(y0 - 1, SIDE_Y + 4.0, ROD_X, ROD_Z, BORE),                  # front shaft, from the inside
             x_hole(x0 - 1, ROD_X + 6.0, SIDE_Y, SIDE_Z_LO, BORE),               # side wall, bottom rod
             x_hole(x0 - 1, ROD_X + 6.0, SIDE_Y, SIDE_Z_HI, BORE),               # side wall, top rod
             y_hole(SIDE_Y + 3.0, y1 + 1, ROD_X, ROD_Z, SCREW_D),                # set screw into the front shaft
             z_hole(z0 - 1, SIDE_Z_LO, ROD_X - 6.0, SIDE_Y, SCREW_D),            # ...into the bottom rod
             z_hole(SIDE_Z_HI, z1 + 1, ROD_X - 6.0, SIDE_Y, SCREW_D)]            # ...into the top rod
    return body.difference(trimesh.util.concatenate(holes))


def hinge_block():
    """The side wall's back end, at the chassis' front face. Holds the two rods' back ends (blind, along x) and turns
    on an 8 mm goBILDA shaft (along y) carried in bearings or pillow blocks on the chassis. The hook swings up about
    it to stow."""
    x0, x1 = HINGE_X + 2.0, HINGE_X + 36.0
    y0, y1 = SIDE_Y - 24.0, SIDE_Y + 8.0
    z0, z1 = 2.0, SIDE_Z_HI + 10.0
    body = slab(x0, x1, y0, y1, z0, z1)
    pivot_x = HINGE_X + 10.0
    holes = [x_hole(x1 - 20.0, x1 + 1, SIDE_Y, SIDE_Z_LO, BORE),
             x_hole(x1 - 20.0, x1 + 1, SIDE_Y, SIDE_Z_HI, BORE),
             y_hole(y0 - 1, y1 + 1, pivot_x, PIVOT_Z, BORE),                   # the hinge axle, right through
             z_hole(z0 - 1, SIDE_Z_LO, x1 - 10.0, SIDE_Y, SCREW_D),
             z_hole(SIDE_Z_HI, z1 + 1, x1 - 10.0, SIDE_Y, SCREW_D),
             z_hole(PIVOT_Z, z1 + 1, pivot_x, SIDE_Y - 12.0, SCREW_D)]          # locks the block to the axle
    return body.difference(trimesh.util.concatenate(holes))


def fit_coupon():
    """Four short bores at 0.1 mm steps around ROD_D + FIT: slide the rod in, keep the snug one."""
    parts, holes = [], []
    for k, extra in enumerate((-0.1, 0.0, 0.1, 0.2)):
        b = box(extents=[14, 8, 14]); b.apply_translation([k * 16, 0, 7]); parts.append(b)
        holes.append(rod_hole(30, k * 16, 7, ROD_D + FIT + extra))
    return trimesh.util.concatenate(parts).difference(trimesh.util.concatenate(holes))


def main():
    out = {}
    for name, fn in (("ramp_block", ramp_block), ("curtain_clip", curtain_clip), ("end_block", end_block),
                     ("corner_block", corner_block), ("hinge_block", hinge_block),
                     ("fit_coupon", fit_coupon)):
        m = fn()
        m.apply_translation([0, 0, -m.bounds[0][2]])
        path = os.path.join(HERE, name + ".stl")
        m.export(path)
        out[name] = (m.is_watertight, m.extents.round(1).tolist(), round(m.volume / 1000, 1))
        print(f"{name}: watertight {m.is_watertight}, {m.extents.round(1).tolist()} mm, {m.volume / 1000:.1f} cm³")
    return out


if __name__ == "__main__":
    main()
