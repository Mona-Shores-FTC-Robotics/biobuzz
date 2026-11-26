"""Clearance check for the printed FLOWER arm through the stick's whole 0..162 deg swing, 1 deg steps.

    python3 cad/flower-printed-arms/check.py     # prints the table and writes clearance.json

- **Against the corner parts** (geometry.corner_boxes: wheel, servo, SWYFT mount; PLACEHOLDERS until measured):
  exact solid-to-solid distances (OpenCascade), each arm against its own corner, for today's straight channel and the
  printed arm.
- **Against the rest of the robot:** the 31 Oct CAD (cad/advantagescope/Robot_BIOBUZZ/model.glb, the stick left out),
  as surface samples, so good to about 1 mm. It is not the robot of today: no corner wheels, no 23 Nov transfer, no
  GARDEN guides. The pivot pad (within 30 mm of the pivot hole) is left out: it is the same as today's and sits
  against the servo and its hub by design.
- **The 18 in starting cube** (R102), with the stick stowed: the printed arm's furthest forward and highest points.
"""
import json
import math
import os
import sys

import cadquery as cq
import numpy as np
import trimesh
from scipy.spatial import cKDTree

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import parts  # noqa: E402
from geometry import (ARM_T, ARM_W, ARM_Y, CUBE_FRONT_X, CUBE_TOP_Z, END_PAST, IN, PIVOT_XZ, STOWED_DIR,  # noqa: E402
                      STOWED_PERP, SWING_DEG, TIP_U, corner_boxes, rot_y)

ROBOT_GLB = os.path.join(HERE, "..", "advantagescope", "Robot_BIOBUZZ", "model.glb")


def placement(side, theta):
    """4x4 from the arm's own frame (u, v, z: the web on the bed at z 0) to the robot frame, mm."""
    R = rot_y(theta)
    d, n = R @ STOWED_DIR, R @ STOWED_PERP
    y_in = ARM_Y[side][0]
    sy = 1.0 if side == "left" else -1.0          # z grows outward from the inner face
    return np.array([[d[0], n[0], 0, PIVOT_XZ[0]],
                     [0, 0, sy, y_in],
                     [d[1], n[1], 0, PIVOT_XZ[1]],
                     [0, 0, 0, 1.0]])


def to_cq_matrix(M):
    return cq.Matrix([list(map(float, row)) for row in M[:3]])


def envelope(straight):
    """The arm's outer shape, no pockets or holes: what clearance is measured to."""
    hm = (0.0, 0, 1, 2, 3) if straight else parts.hump()
    return parts._poly(parts._outline(hm, ARM_W / 2), ARM_T).val()


def corner_solids(side):
    out = {}
    for name, k in corner_boxes(side).items():
        if k[0] == "cyl":
            _, c, ax, r, h = k
            s = cq.Solid.makeCylinder(r, 2 * h, cq.Vector(*(c - ax * h)), cq.Vector(*ax))
        else:
            _, lo, hi = k
            s = cq.Solid.makeBox(*(hi - lo), cq.Vector(*lo))
        out[name] = s
    return out


def robot_points(n=1_500_000):
    """Surface samples of the 31 Oct robot near the arms' bands, mm."""
    s = trimesh.load(ROBOT_GLB)
    m = s.to_geometry() if hasattr(s, "to_geometry") else s.dump(concatenate=True)
    m.apply_scale(1000.0)
    c = m.triangles_center
    keep = (np.abs(c[:, 1]) > 100) & (np.abs(c[:, 1]) < 200)
    sub = m.submesh([np.nonzero(keep)[0]], append=True)
    pts, _ = trimesh.sample.sample_surface(sub, n, seed=1)
    return np.vstack([pts, sub.vertices])


def arm_points(straight, step=1.5):
    m = trimesh.Trimesh(**trimesh.exchange.stl.load_stl(open(os.path.join(HERE, "arm.stl"), "rb"))) \
        if not straight else None
    if straight:
        # today's channel: a 288 x 32 x 12 box about the same line
        box = trimesh.creation.box((TIP_U + 2 * END_PAST, ARM_W, ARM_T))
        box.apply_translation((TIP_U / 2, 0, ARM_T / 2))
        m = box
    pts, _ = trimesh.sample.sample_surface_even(m, 60000, seed=2)
    return pts


def main():
    report = {"units": "mm and in", "corner_parts": "PLACEHOLDERS (geometry.CORNER) until measured", "sides": {}}
    robot = robot_points()
    tree = cKDTree(robot)
    shapes = {"straight (today)": envelope(True), "printed": envelope(False)}
    pts = {"straight (today)": arm_points(True), "printed": arm_points(False)}
    for side in ("left", "right"):
        obst = corner_solids(side)
        rs = {}
        for label, env in shapes.items():
            worst = {k: (1e9, None) for k in obst}
            for th in np.arange(0, SWING_DEG + 0.01, 1.0):
                placed = env.transformGeometry(to_cq_matrix(placement(side, th)))
                for k, s in obst.items():
                    dist = placed.distance(s)
                    if dist < worst[k][0]:
                        worst[k] = (dist, float(th))
            # the robot
            p = pts[label]
            p = p[np.hypot(p[:, 0], p[:, 1]) > 30.0]          # leave out the pivot pad
            rob = (1e9, None)
            for th in np.arange(0, SWING_DEG + 0.01, 2.0):
                M = placement(side, th)
                q = p @ M[:3, :3].T + M[:3, 3]
                dd, _ = tree.query(q, distance_upper_bound=60)
                if dd.min() < rob[0]:
                    rob = (float(dd.min()), float(th))
            rs[label] = {k: {"mm": round(v[0], 1), "in": round(v[0] / IN, 2), "at_deg": v[1]} for k, v in worst.items()}
            rs[label]["rest of robot (31 Oct CAD)"] = {"mm": round(rob[0], 1), "in": round(rob[0] / IN, 2),
                                                       "at_deg": rob[1]}
        report["sides"][side] = rs
    # the starting cube, stowed
    M = placement("left", 0.0)
    q = pts["printed"] @ M[:3, :3].T + M[:3, 3]
    report["stowed"] = {"front_x_in": round(q[:, 0].max() / IN, 2), "cube_front_x_in": round(CUBE_FRONT_X / IN, 2),
                        "top_z_in": round(q[:, 2].max() / IN, 2), "cube_top_z_in": 18.0}
    M = placement("left", SWING_DEG)
    q = pts["printed"] @ M[:3, :3].T + M[:3, 3]
    report["down"] = {"front_x_in": round(q[:, 0].max() / IN, 2), "lowest_z_in": round(q[:, 2].min() / IN, 2)}
    h, a, b, c, e = parts.hump()
    report["hump"] = {"height_mm": h, "leaves_line_u_mm": a, "full_u_mm": [b, c], "back_on_line_u_mm": e,
                      "note": "u along the arm from the pivot hole; hole station n is at u = 24 (n - 1)"}
    with open(os.path.join(HERE, "clearance.json"), "w") as f:
        json.dump(report, f, indent=1)
    print(json.dumps(report, indent=1))


if __name__ == "__main__":
    main()
