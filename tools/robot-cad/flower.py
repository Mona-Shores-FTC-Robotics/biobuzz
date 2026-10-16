"""The FLOWER as meshes in the robot frame (+X forward, +Y left, +Z up, inches, origin under the chassis centre), with
its centre d inches ahead of the front face (X 7.56).

Measured from FIRST's field CAD (am-5855 Flower Assembly, STEP v26-27.2) by flower_from_field_cad.py on 16 Oct 2026,
about the bottom POLLEN's centre on the tile top: base ring (Layer X) top 0.35 in, front edge 2.36 in ahead, back 2.47
behind, 5.92 wide, hole 3.08 across; the uprights' (Peanut Supports) front face 1.18 in behind, inner faces 0.61 each
side, outer 1.89, 1.28 deep, 0.35 to 4.10 tall; the bracket (Layer B) 3.90 to 5.25; the four pipes at (+-1.72, +-1.72),
1.05 across, from 4.25 up. Until then these were scaled off the manual's Fig 9-12 (ring 0.43, hole 2.79, uprights 1.25
behind, opening 3.55, pipes to the base)."""
import math, numpy as np, trimesh
from shapely.geometry import Polygon, Point
CX = 2.71                       # centre to wall (AdvantageScope's field CAD)
HOLE_R, RING_T, OPEN = 3.08 / 2, 0.35, 3.90
UP_FACE, UP_T = 1.18, 1.28      # the uprights' front face: 1.18 in beyond the centre (toward the wall), 1.28 deep
UP_Y0 = 0.61                    # their inner faces, |Y|
RING_FRONT, RING_BACK, RING_HALF = 2.36, 2.47, 2.96
P, BRACKET_T, POST_R, POST_TOP = 1.72, 1.35, 0.525, 21.25
def parts(d):
    """{name: trimesh} with the FLOWER's centre d in ahead of the face. The robot faces the wall (+X)."""
    xc = 7.56 + d
    out = {}
    # the base: square at the back, its front corners chamfered (the field CAD's outline, to about 0.1 in)
    pts = [(-RING_FRONT, -2.15), (-RING_FRONT, 2.15), (-1.1, RING_HALF), (min(RING_BACK, CX - 0.05), RING_HALF),
           (min(RING_BACK, CX - 0.05), -RING_HALF), (-1.1, -RING_HALF)]
    ring = Polygon([(xc + px, py) for px, py in pts]).difference(Point(xc, 0).buffer(HOLE_R, 48))
    out["flower_ring"] = trimesh.creation.extrude_polygon(ring, RING_T)
    for s in (-1, 1):
        b = trimesh.creation.box(bounds=[[xc + UP_FACE, min(s * UP_Y0, s * (UP_Y0 + UP_T)), RING_T], [xc + UP_FACE + UP_T, max(s * UP_Y0, s * (UP_Y0 + UP_T)), OPEN]])
        out[f"flower_upright_{s:+d}"] = b
    out["flower_bracket"] = trimesh.creation.box(bounds=[[xc - 2.41, -RING_HALF, OPEN], [xc + 2.60, RING_HALF, OPEN + BRACKET_T]])
    POST_BOTTOM = 4.25
    for sx in (-1, 1):
        for sy in (-1, 1):
            c = trimesh.creation.cylinder(radius=POST_R, height=POST_TOP - POST_BOTTOM, sections=24)
            c.apply_translation([xc + sx * P, sy * P, (POST_TOP + POST_BOTTOM) / 2]); out[f"flower_post_{sx:+d}{sy:+d}"] = c
    out["field_wall"] = trimesh.creation.box(bounds=[[xc + CX, -20, 0], [xc + CX + 0.5, 20, 12]])
    return out
