"""Draws concepts3d.png: the three slant concepts (concepts.png) as 3D shapes on the robot model from the team's Onshape
CAD (cad/advantagescope/Robot_BIOBUZZ), FLOWER stick swung down until its wheel touches the floor.

    pip install trimesh manifold3d matplotlib fast_simplification shapely mapbox_earcut
    python3 cad/stick-shoulder-19429/concepts3d.py

Concept shapes only, for choosing between them: no screw holes, and the sizes are the CAD's, not 19429's. The CAD's
right arm sits 43 mm from the ramp's end, like the photos of 19429. The CAD has no blue wall, no shoulder and no right
wheel, so those are drawn as stand-ins: the blue wall at the ramp's end, a wheel copied from the left arm's.

Local frame for the shapes, millimetres: origin on the shaft axis, X forward (the robot's), Y the robot's (left +),
Z up. The right arm's inner face is at Y -148.2 and the ramp's right end at Y -105.5.
"""
import importlib.util
import json
import math
import os

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import trimesh

HERE = os.path.dirname(os.path.abspath(__file__))
MODEL = os.path.join(HERE, "..", "advantagescope", "Robot_BIOBUZZ")
_spec = importlib.util.spec_from_file_location("beam_render", os.path.join(HERE, "..", "beam-mount", "render.py"))
_beam = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_beam)
raster, show = _beam.raster, _beam.show

ARM_Y, WALL_Y, WALL_T = -148.2, -105.5, 3.0      # right arm's inner face; the blue wall stands at the ramp's end
WALL_OUT = WALL_Y - WALL_T                         # the blue wall's outer face, where the new piece meets it
X_BACK, X_FRONT = -30.0, 12.0                      # the slant's depth, back to front
GREY, METAL, DARK, ORANGE, BLUE, NEW = (0.7, 0.71, 0.74), (0.8, 0.82, 0.86), (0.15, 0.15, 0.15), \
    (0.95, 0.45, 0.15), (0.25, 0.45, 0.9), (1.0, 0.8, 0.1)


def scene_meshes(path, transform=None):
    s = trimesh.load(path)
    out = {}
    for n in s.graph.nodes_geometry:
        T, g = s.graph[n]
        m = s.geometry[g].copy()
        m.apply_transform(T)
        m.apply_scale(1000.0)                      # metres -> millimetres
        if transform is not None:
            m.apply_transform(transform)
        out[n] = m
    return out


def deploy():
    """The stick's parts turned down until its wheel just touches the floor, and that angle."""
    c = json.load(open(os.path.join(MODEL, "components.json")))["components"][1]
    p, d = np.array(c["axis_point_m"]) * 1000, np.array(c["axis_direction"])
    lo, hi = 90.0, 162.0
    for _ in range(40):
        a = (lo + hi) / 2
        parts = scene_meshes(os.path.join(MODEL, "model_1.glb"), trimesh.transformations.rotation_matrix(math.radians(a), d, p))
        wheel_bottom = next(m for n, m in parts.items() if n.startswith("3400")).bounds[0][2]
        lo, hi = (a, hi) if wheel_bottom > 0 else (lo, a)
    return parts, a


def prism_x(yz, x0, x1):
    """A YZ cross-section extruded along X from x0 to x1."""
    from shapely.geometry import Polygon
    poly = Polygon(yz)
    m = trimesh.creation.extrude_polygon(poly, x1 - x0)          # along +Z in (Y, Z) -> remap
    v = m.vertices.copy()
    m.vertices = np.c_[v[:, 2] + x0, v[:, 0], v[:, 1]]
    m.fix_normals()
    return m


def hull(pts):
    return trimesh.convex.convex_hull(np.array(pts, dtype=float))


def along_arm_plate(a, r0, r1, s0, s1, y0, y1):
    """A plate in the XZ plane along the arm's direction a (unit, in XZ), from r0 to r1, s0..s1 across it."""
    u = np.array(a); v = np.array([-u[1], u[0]])
    if v[1] < 0:
        v = -v
    pts = []
    for r in (r0, r1):
        for s in (s0, s1):
            x, z = r * u + s * v
            pts += [(x, y0, z), (x, y1, z)]
    return hull(pts)


SLANT = [(ARM_Y, 55.0), (WALL_OUT, 12.0)]          # your slant's top edge, carried straight on to the blue wall


def slant_z(y):
    (y0, z0), (y1, z1) = SLANT
    return z0 + (z1 - z0) * (y - y0) / (y1 - y0)


def section_a():
    return [(ARM_Y, 55.0), (WALL_OUT, 12.0), (WALL_OUT, 7.0), (-133.0, 7.0), (-137.0, 21.0), (ARM_Y, 21.0)]


def section_b(r=20.0, top=48.0):
    """A, then a concave cove up the wall's outer face to `top`."""
    cy, cz = WALL_OUT - r, top
    arc = []
    for i in range(13):
        t = math.radians(-75 + 75 * i / 12)
        arc.append((cy + r * math.cos(t), cz + r * math.sin(t)))
    start_y = arc[0][0]
    return [(ARM_Y, 55.0), (start_y, slant_z(start_y))] + arc + [(WALL_OUT, 7.0), (-133.0, 7.0), (-137.0, 21.0), (ARM_Y, 21.0)]


def nose_c(floor_z):
    """C's funnel seen from above: ahead of the slant, from outboard-forward to the blue wall's front tip."""
    lo = floor_z + 7.0
    return hull([(X_FRONT, -140.0, lo), (X_FRONT, -140.0, 21.0), (40.0, -140.0, lo), (40.0, -140.0, 10.0),
                 (X_FRONT, WALL_OUT, lo), (X_FRONT, WALL_OUT, 12.0), (16.0, WALL_OUT, lo), (16.0, WALL_OUT, 9.0)])


def concept(name, a, floor_z):
    tabs = [along_arm_plate(a, 24, 70, -10, 10, ARM_Y, ARM_Y + 4),          # screws to the arm
            along_arm_plate(a, 18, 55, -6, 18, WALL_OUT - 4, WALL_OUT)]      # screws through the blue wall
    if name == "A":
        body = [prism_x(section_a(), X_BACK, X_FRONT)]
    elif name == "B":
        body = [prism_x(section_b(), X_BACK, X_FRONT)]
    else:
        body = [prism_x(section_a(), X_BACK, X_FRONT), nose_c(floor_z)]
    return body + tabs


def stand_ins(parts, a, floor_z):
    left_wheel = next(m for n, m in parts.items() if n.startswith("3400"))
    w = left_wheel.copy()
    w.apply_transform(trimesh.transformations.reflection_matrix([0, 0, 0], [0, 1, 0]))
    w.fix_normals()
    w.apply_translation([0, (ARM_Y + 2.0) - w.bounds[0][1], 0])               # 2 mm off the right arm, as the left is
    u = np.array(a); v = np.array([-u[1], u[0]]); v = v if v[1] > 0 else -v
    fin_xz = [(16.0, floor_z + 3), (12.0, 15.0), tuple(75 * u + 15 * v), tuple(75 * u - 12 * v), (-10.0, floor_z + 4)]
    fin = hull([(x, y, z) for x, z in fin_xz for y in (WALL_OUT, WALL_Y)])
    return w, fin


def main():
    parts, angle = deploy()
    shaft = next(m for n, m in parts.items() if n.startswith("312mm"))
    hub = next(m for n, m in parts.items() if n.startswith("Servo Hub"))
    sc = shaft.bounds.mean(0)
    origin = np.array([sc[0], 0.0, sc[2]])
    hc = hub.bounds.mean(0)
    a = np.array([hc[0] - sc[0], hc[2] - sc[2]]); a /= np.linalg.norm(a)
    floor_z = -sc[2]
    to_world = trimesh.transformations.translation_matrix(origin)
    print(f"stick down at {angle:.1f} deg; shaft {sc[2]:.1f} mm above the floor; arm {math.degrees(math.atan2(a[1], -a[0])):.0f} deg up")

    robot = scene_meshes(os.path.join(MODEL, "model.glb"))
    robot = trimesh.util.concatenate(list(robot.values()))
    near = robot.bounds[1][0] - 160                                             # just the robot's front, for close-ups
    keep = robot.triangles_center[:, 0] > near
    front = robot.submesh([np.nonzero(keep & (robot.triangles_center[:, 1] < 20))[0]], append=True)
    import fast_simplification
    v, f = fast_simplification.simplify(front.vertices, front.faces, target_reduction=0.85)
    front = trimesh.Trimesh(v, f)
    stick = [(m, ORANGE if n.startswith("Flower Ramp") else METAL) for n, m in parts.items()]
    wheel, fin = stand_ins({n: m.copy() for n, m in parts.items()}, a, floor_z)
    wheel_w = wheel                                                             # built from world parts already
    fin_w = fin.copy(); fin_w.apply_transform(to_world)
    lo, hi = np.array([sc[0] - 95, -178, -2]), np.array([sc[0] + 70, -52, 105])   # close in on the right arm

    def crop(m):
        c = m.triangles_center
        k = np.nonzero(np.all((c > lo) & (c < hi), axis=1))[0]
        return m.submesh([k], append=True) if len(k) else None

    floor = trimesh.creation.box(extents=[hi[0] - lo[0], hi[1] - lo[1], 1])
    floor.apply_translation([(lo[0] + hi[0]) / 2, (lo[1] + hi[1]) / 2, -0.6])
    base = [(crop(m), c) for m, c in [(front, GREY)] + stick + [(wheel_w, DARK), (fin_w, BLUE)]]
    base = [(m, c) for m, c in base if m is not None] + [(floor, (0.45, 0.45, 0.47))]

    fig = plt.figure(figsize=(16, 15))
    titles = {"A": "A. Straight on", "B": "B. Cove up the wall", "C": "C. Funnel to the tip"}
    for row, name in enumerate("ABC"):
        new = []
        for m in concept(name, a, floor_z):
            m = m.copy(); m.apply_transform(to_world); new.append((m, NEW))
        views = [(15, -20, "from the front"), (25, -60, "from the front, outside the arm"), (80, 0, "from above (front is down)")]
        for col, (el, az, label) in enumerate(views):
            img = raster(base + new, el, az, size=520)
            show(fig.add_subplot(3, 3, row * 3 + col + 1), img, f"{titles[name]}: {label}")
    fig.suptitle("19429 right arm on the Onshape robot model, stick down. Yellow: the new piece. Blue: stand-in for the blue wall.\n"
                 "Shapes only: no screw holes; sizes from the CAD, not yet 19429's.", fontsize=12)
    plt.tight_layout(rect=(0, 0, 1, 0.96))
    plt.savefig(os.path.join(HERE, "concepts3d.png"), dpi=80)
    print("concepts3d.png")


if __name__ == "__main__":
    main()
