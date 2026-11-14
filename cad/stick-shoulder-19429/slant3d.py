"""Draws slant3d.png: the slant face for 19429's right arm, on the Onshape robot model, FLOWER stick down.

    pip install trimesh manifold3d matplotlib fast_simplification
    python3 cad/stick-shoulder-19429/slant3d.py

What the face does: a ball the robot drives into rides UP it, and while it is on it, the face's sideways tilt rolls it
toward the robot's middle (to the right, looking at the robot's front). So the face is a ramp like the orange one,
rising toward the robot, AND higher at the arm than at the blue wall. A face tilted both ways meets the floor on a
diagonal: further forward at the arm, back at the blue wall's front tip, where the ball rolls on past the tip onto the
orange ramp. The red arrows on the face are the way a ball on it is pushed (straight downhill on the face).

Shape only, for agreeing on it: no screw holes yet, sizes from the CAD (cad/advantagescope), not yet 19429's. Every
number that sets the face is a constant below.

Frame, millimetres: origin on the shaft axis, X forward, Y the robot's (left +), Z up. The right arm's inner face is at
Y -148.2, the blue wall (a stand-in: the CAD has none) at the ramp's right end, Y -105.5.
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

ARM_Y, WALL_Y, WALL_T = -148.2, -105.5, 3.0
WALL_OUT = WALL_Y - WALL_T                 # the blue wall's outer face
WHEEL_GAP = 2.0                            # the wheel sits this far off the arm (as the CAD's left one does)
# ---- the face ----
INCLINE_DEG = 45.0          # how steeply it rises toward the robot (the orange ramp's job, for a ball against the arm)
TIP_X = 14.0                # its foot at the blue wall: level with the orange ramp's front edge, the wall's front tip
ARM_X = 64.0                # its foot at the arm: this much further forward makes the sideways tilt
FOOT_UP = 3.0               # its foot above the floor, so it never drags
TOP_Z = 52.0                # how high it goes, above the shaft axis
FACE_T = 4.0                # plate thickness
POLLEN_D = 71.0             # 2.8 in

GREY, METAL, DARK, ORANGE, BLUE, NEW, RED, WHITE = (0.7, 0.71, 0.74), (0.8, 0.82, 0.86), (0.15, 0.15, 0.15), \
    (0.95, 0.45, 0.15), (0.25, 0.45, 0.9), (1.0, 0.8, 0.1), (0.85, 0.1, 0.1), (0.95, 0.95, 0.95)


def scene_meshes(path, transform=None):
    s = trimesh.load(path)
    out = {}
    for n in s.graph.nodes_geometry:
        T, g = s.graph[n]
        m = s.geometry[g].copy()
        m.apply_transform(T)
        m.apply_scale(1000.0)
        if transform is not None:
            m.apply_transform(transform)
        out[n] = m
    return out


def deploy():
    """The stick's parts turned down until its wheel just touches the floor."""
    c = json.load(open(os.path.join(MODEL, "components.json")))["components"][1]
    p, d = np.array(c["axis_point_m"]) * 1000, np.array(c["axis_direction"])
    lo, hi = 90.0, 162.0
    for _ in range(40):
        a = (lo + hi) / 2
        parts = scene_meshes(os.path.join(MODEL, "model_1.glb"), trimesh.transformations.rotation_matrix(math.radians(a), d, p))
        bottom = next(m for n, m in parts.items() if n.startswith("3400")).bounds[0][2]
        lo, hi = (a, hi) if bottom > 0 else (lo, a)
    return parts


def hull(pts):
    return trimesh.convex.convex_hull(np.array(pts, dtype=float))


class Face:
    """The plane z = foot_z + tan(incline) * (foot_x(y) - x), foot_x running from ARM_X at the arm to TIP_X at the wall."""

    def __init__(self, floor_z):
        self.foot_z = floor_z + FOOT_UP
        self.k = math.tan(math.radians(INCLINE_DEG))

    def foot_x(self, y):
        return TIP_X + (ARM_X - TIP_X) * (WALL_OUT - y) / (WALL_OUT - ARM_Y)

    def x_at(self, y, z):
        return self.foot_x(y) - (z - self.foot_z) / self.k

    def point(self, y, z):
        return np.array([self.x_at(y, z), y, z])

    def normal(self):
        """Out of the face: forward, up, and toward the robot's middle (the gradient of z - plane(x, y))."""
        g = np.array([self.k, self.k * (ARM_X - TIP_X) / (WALL_OUT - ARM_Y), 1.0])
        return g / np.linalg.norm(g)

    def downhill(self):
        """Straight down the face: the way gravity rolls a ball on it, relative to the robot."""
        n = self.normal()
        g = np.array([0, 0, -1.0])
        d = g - n * (g @ n)
        return d / np.linalg.norm(d)


def slant_piece(face, a):
    """The face plate, a side plate up the arm to the arm tab, a side plate on the blue wall, and the two tabs."""
    corners = [face.point(ARM_Y, face.foot_z), face.point(WALL_OUT, face.foot_z), face.point(WALL_OUT, TOP_Z), face.point(ARM_Y, TOP_Z)]
    n = face.normal()
    plate = hull(corners + [c - n * FACE_T for c in corners])
    u = np.array(a); v = np.array([-u[1], u[0]]); v = v if v[1] > 0 else -v
    tab_pts = lambda y0, y1, r0, r1, s0, s1: [(x, y, z) for r in (r0, r1) for s in (s0, s1)
                                             for (x, z) in [tuple(r * u + s * v)] for y in (y0, y1)]
    arm_tab = tab_pts(ARM_Y, ARM_Y + 4, 30, 72, -4, 14)
    # the arm side plate stays above the wheel: from the face's arm edge above WHEEL_TOP to the tab
    wheel_top = 17.75 + 10
    arm_side = hull([tuple(face.point(y, z)) for y in (ARM_Y, ARM_Y + 4) for z in (wheel_top, TOP_Z)] + arm_tab)
    wall_tab = tab_pts(WALL_OUT - 4, WALL_OUT, 20, 60, -8, 18)
    wall_side = hull([tuple(face.point(y, z)) for y in (WALL_OUT - 4, WALL_OUT) for z in (face.foot_z, TOP_Z)] + wall_tab)
    return [plate, arm_side, wall_side]


def arrows(face, n_rows=3, n_cols=3, length=26.0):
    """Short red bars on the face pointing downhill, with a dot at the head."""
    out = []
    d = face.downhill(); nrm = face.normal()
    for i in range(n_rows):
        z = face.foot_z + 12 + i * (TOP_Z - face.foot_z - 22) / max(1, n_rows - 1)
        for j in range(n_cols):
            y = ARM_Y + 8 + j * (WALL_OUT - ARM_Y - 16) / max(1, n_cols - 1)
            p = face.point(y, z) + nrm * 1.5
            seg = trimesh.creation.cylinder(radius=1.4, segment=[p - d * length / 2, p + d * length / 2], sections=12)
            head = trimesh.creation.icosphere(radius=3.2); head.apply_translation(p + d * length / 2)
            out += [(seg, RED), (head, RED)]
    return out


def wheel_hits(face, a, y0, y1, r):
    """How many points of the wheel's cylinder (about the shaft axis, y0..y1, radius r) lie inside the piece's
    (convex) parts: 0 means it clears."""
    g = np.random.default_rng(0)
    rr, th, yy = r * np.sqrt(g.random(6000)), 2 * math.pi * g.random(6000), y0 + (y1 - y0) * g.random(6000)
    pts = np.c_[rr * np.cos(th), yy, rr * np.sin(th)]
    n = 0
    for m in slant_piece(face, a):
        inside = np.all(np.einsum('nfk,fk->nf', pts[:, None, :] - m.triangles[:, 0][None], m.face_normals) <= 1e-6, axis=1)
        n += int(inside.sum())
    return n


def ball_on(face, y):
    """A POLLEN resting on the floor against the face at y."""
    r = POLLEN_D / 2
    nrm = face.normal()
    # centre at height r above the floor (floor = foot_z - FOOT_UP), r from the face along its normal
    zc = face.foot_z - FOOT_UP + r
    c = face.point(y, zc - nrm[2] * r) + nrm * r
    c[2] = zc
    b = trimesh.creation.icosphere(subdivisions=3, radius=r); b.apply_translation(c)
    return b


def main():
    parts = deploy()
    shaft = next(m for n, m in parts.items() if n.startswith("312mm"))
    hub = next(m for n, m in parts.items() if n.startswith("Servo Hub"))
    sc = shaft.bounds.mean(0)
    hc = hub.bounds.mean(0)
    a = np.array([hc[0] - sc[0], hc[2] - sc[2]]); a /= np.linalg.norm(a)
    floor_z = -sc[2]
    to_world = trimesh.transformations.translation_matrix([sc[0], 0, sc[2]])

    robot = trimesh.util.concatenate(list(scene_meshes(os.path.join(MODEL, "model.glb")).values()))
    lo, hi = np.array([sc[0] - 95, -185, -2]), np.array([sc[0] + 110, -45, 110])

    def crop(m):
        c = m.triangles_center
        k = np.nonzero(np.all((c > lo) & (c < hi), axis=1))[0]
        return m.submesh([k], append=True) if len(k) else None

    left_wheel = next(m for n, m in parts.items() if n.startswith("3400"))
    wheel = left_wheel.copy()
    wheel.apply_transform(trimesh.transformations.reflection_matrix([0, 0, 0], [0, 1, 0])); wheel.fix_normals()
    wheel.apply_translation([0, (ARM_Y + WHEEL_GAP) - wheel.bounds[0][1], 0])

    face = Face(floor_z)
    u = np.array(a); v = np.array([-u[1], u[0]]); v = v if v[1] > 0 else -v
    fin_xz = [(TIP_X + 2, floor_z + 3), (TIP_X - 2, 18.0), tuple(80 * u + 18 * v), tuple(80 * u - 12 * v), (-10.0, floor_z + 4)]
    fin = hull([(x, y, z) for x, z in fin_xz for y in (WALL_OUT, WALL_Y)])

    def world(m):
        m = m.copy(); m.apply_transform(to_world); return m

    piece = [world(m) for m in slant_piece(face, a)]
    # the wheel must not touch the piece
    hits = wheel_hits(face, a, wheel.bounds[0][1], wheel.bounds[1][1], (wheel.bounds[1][2] - wheel.bounds[0][2]) / 2)
    print(f"floor {floor_z:.1f}; downhill on the face {np.round(face.downhill(), 2)} (x fwd, y left, z up); wheel samples inside: {hits}")

    floor = trimesh.creation.box(extents=[hi[0] - lo[0], hi[1] - lo[1], 1])
    floor.apply_translation([(lo[0] + hi[0]) / 2, (lo[1] + hi[1]) / 2, -0.6])
    stick = [(m, ORANGE if n.startswith("Flower Ramp") else METAL) for n, m in parts.items()]
    base = [(crop(m), c) for m, c in [(robot, GREY)] + stick + [(wheel, DARK), (world(fin), BLUE)]]
    base = [(m, c) for m, c in base if m is not None] + [(floor, (0.45, 0.45, 0.47))]
    new = [(m, NEW) for m in piece] + [(world(m), c) for m, c in arrows(face)]
    ball = (world(ball_on(face, ARM_Y + 20)), WHITE)

    fig = plt.figure(figsize=(16, 10.5))
    views = [(15, -15, "From the front"), (30, -55, "From the front, outside the arm"), (8, -90, "From the right side"),
             (82, 0, "From above (front is down)"), (22, -25, "With a POLLEN against it"), (35, 160, "From behind, inboard")]
    for i, (el, az, label) in enumerate(views):
        scene = base + new + ([ball] if "POLLEN" in label else [])
        show(fig.add_subplot(2, 3, i + 1), raster(scene, el, az, size=540), label)
    fig.suptitle("19429 right arm, stick down, on the Onshape robot model. Yellow: the slant face, screwed to the arm and the blue wall\n"
                 "(blue: stand-in). Red arrows: the way a ball on the face is pushed, up toward the robot's middle. Shape only.", fontsize=12)
    plt.tight_layout(rect=(0, 0, 1, 0.94))
    plt.savefig(os.path.join(HERE, "slant3d.png"), dpi=80)
    print("slant3d.png")


if __name__ == "__main__":
    main()
