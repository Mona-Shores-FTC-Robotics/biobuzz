"""The FLOWER (Competition Manual Fig 9-12, as tools/ramp-hook/ramp.py and the 3D page draw it), as meshes in the robot
frame (+X forward, +Y left, +Z up, inches, origin under the chassis centre), with its centre d inches ahead of the
front face (X 7.56). Scaled off the figure, not measured: check against the field CAD before trusting a few hundredths."""
import math, numpy as np, trimesh
from shapely.geometry import Polygon, Point
CX = 2.71                       # centre to wall
HOLE_R, RING_T, OPEN = 2.79 / 2, 0.43, 3.55
UP_FACE, UP_T = 1.25, 0.75      # the grey uprights' face: 1.25 in beyond the centre (toward the wall)
UP_Y0 = math.sqrt(HOLE_R ** 2 - UP_FACE ** 2)      # their inner faces, |Y| (0.62)
RING_FRONT = UP_FACE + 3.57 - CX + CX - UP_FACE     # placeholder, set below
P, BRACKET_T, POST_R, POST_TOP = 1.55, 0.6, 0.62, 21.5
def parts(d):
    """{name: trimesh} with the FLOWER's centre d in ahead of the face. The robot faces the wall (+X)."""
    xc = 7.56 + d
    out = {}
    front = 3.57 - UP_FACE                           # the ring's flat edge toward the robot, from the centre (2.32)
    ro = front / math.cos(math.pi / 8)
    pts = []
    for k in range(8):
        a = math.pi / 8 + k * math.pi / 4
        px, py = -ro * math.cos(a), ro * math.sin(a)   # -x is toward the robot
        pts.append((xc + min(px, CX - 0.05), py))
    ring = Polygon(pts).difference(Point(xc, 0).buffer(HOLE_R, 48))
    out["flower_ring"] = trimesh.creation.extrude_polygon(ring, RING_T)
    for s in (-1, 1):
        b = trimesh.creation.box(bounds=[[xc + UP_FACE, min(s * UP_Y0, s * (UP_Y0 + UP_T)), RING_T], [xc + UP_FACE + UP_T, max(s * UP_Y0, s * (UP_Y0 + UP_T)), OPEN]])
        out[f"flower_upright_{s:+d}"] = b
    half = P + 0.8
    out["flower_bracket"] = trimesh.creation.box(bounds=[[xc - half, -half, OPEN], [xc + half, half, OPEN + BRACKET_T]])
    for sx in (-1, 1):
        for sy in (-1, 1):
            c = trimesh.creation.cylinder(radius=POST_R, height=POST_TOP - OPEN - BRACKET_T, sections=24)
            c.apply_translation([xc + sx * P, sy * P, OPEN + BRACKET_T + (POST_TOP - OPEN - BRACKET_T) / 2]); out[f"flower_post_{sx:+d}{sy:+d}"] = c
    out["field_wall"] = trimesh.creation.box(bounds=[[xc + CX, -20, 0], [xc + CX + 0.5, 20, 12]])
    return out
