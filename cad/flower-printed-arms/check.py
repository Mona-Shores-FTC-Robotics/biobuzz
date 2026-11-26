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


def foot_placement(side, theta):
    """4x4 from a foot's own frame (parts.foot_skid: the tip hole's axis on Z, the arm's inner face at Z 0, Z inboard)
    to the robot frame."""
    R = rot_y(theta)
    d, n = R @ STOWED_DIR, R @ STOWED_PERP
    origin = PIVOT_XZ + TIP_U * d
    sy = -1.0 if side == "left" else 1.0           # inboard
    return np.array([[d[0], n[0], 0, origin[0]],
                     [0, 0, sy, ARM_Y[side][0]],
                     [d[1], n[1], 0, origin[1]],
                     [0, 0, 0, 1.0]])


def envelope(straight):
    """The arm's outer shape, no pockets or holes: what clearance is measured to."""
    hm = (0.0, 0, 1, 2, 3) if straight else parts.hump()
    return parts._poly(parts._outline(hm, ARM_W / 2), ARM_T).val()


WHEEL_STEP = os.path.join(HERE, "vendor", "SR-INTAKEWHEEL-3.5in-0.5inHEX-02.step")
SERVO_STEP = os.path.join(HERE, "vendor", "goBILDA-2000-0025-0003-speed-servo.step")
_WHEEL = None
_SERVO = None


def real_servo(side):
    """goBILDA's Speed servo from its STEP, placed as geometry.corner_boxes puts the servo: its spline (STEP: on +Z at
    X -10, Y 0, the body's bottom at Z -27.2) pointing forward on the wheels' axle, the long side upright."""
    global _SERVO
    if _SERVO is None:
        _SERVO = cq.importers.importStep(SERVO_STEP).val()
    b = corner_boxes(side)
    lo, hi = b["servo"][1], b["servo"][2]
    wz = b["wheel"][1][2]
    wy = b["wheel"][1][1]
    # STEP (x, y, z) -> robot (X, Y, Z) = (lo.X + z + 27.2, wy - y, wz + x + 10)
    M = np.array([[0, 0, 1, lo[0] + 27.2], [0, -1, 0, wy], [1, 0, 0, wz + 10.0], [0, 0, 0, 1.0]])
    return _SERVO.transformGeometry(to_cq_matrix(M))


def corner_solids(side, real_wheel=False):
    """The corner's parts, robot frame: the SWYFT wheel from its STEP (its axle turned to point forward), the servo and
    its mount as boxes."""
    global _WHEEL
    out = {}
    for name, k in corner_boxes(side).items():
        if k[0] == "cyl":
            _, c, ax, r, h = k
            if not real_wheel or name != "wheel":    # the wheels' outer cylinder (an envelope, and fast); the screw
                out[name] = cq.Solid.makeCylinder(r, 2 * h, cq.Vector(*(c - ax * h)), cq.Vector(*ax))
                continue
            if _WHEEL is None:
                _WHEEL = cq.importers.importStep(WHEEL_STEP).val()
            # the STEP: axle on Z, faces at Z 0 and 12.7, centred on X = Y = 0. Z -> +X, X -> -Z.
            w1 = 12.7                                   # one wheel; geometry.CORNER may stack several
            s = None
            for x0 in np.arange(c[0] - h, c[0] + h - 1e-6, w1):
                M = np.array([[0, 0, 1, x0], [0, 1, 0, c[1]], [-1, 0, 0, c[2]], [0, 0, 0, 1.0]])
                one = _WHEEL.transformGeometry(to_cq_matrix(M))
                s = one if s is None else s.fuse(one)
        else:
            _, lo, hi = k
            s = cq.Solid.makeBox(*(hi - lo), cq.Vector(*lo))
        out[name] = s
    if real_wheel:                                   # the viewer and the export: goBILDA's servo for the two boxes
        out.pop("tabs", None)
        out["servo"] = real_servo(side)
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


def _pts(shape, n=40000):
    v, f = shape.tessellate(0.3)
    m = trimesh.Trimesh([[p.x, p.y, p.z] for p in v], f)
    p, _ = trimesh.sample.sample_surface_even(m, n, seed=2)
    return p


def _row(d):
    return {"mm": round(d[0], 1), "in": round(d[0] / IN, 2), "at_deg": d[1]}


def main():
    report = {"units": "mm and in", "corner_parts": "the two SWYFT wheels as their 88.74 mm outer cylinder; servo and mount PLACEHOLDERS "
                                                    "(geometry.CORNER) until measured", "sides": {}}
    tree = cKDTree(robot_points())
    angles = list(np.arange(0, SWING_DEG, 3.0)) + [SWING_DEG]
    foot = parts.foot_skid().val()
    pieces = {
        "straight (today)": {"arm": (envelope(True), placement)},
        "printed": {"arm": (envelope(False), placement), "foot (a) skid": (foot, foot_placement)},
    }
    samples = {"straight (today)": {"arm": arm_points(True)},
               "printed": {"arm": arm_points(False), "foot (a) skid": _pts(foot)}}
    for side in ("left", "right"):
        obst = corner_solids(side)
        rs = {}
        for label, group in pieces.items():
            res = {}
            for pname, (shape, place) in group.items():
                worst = {k: (1e9, None) for k in obst}
                for th in angles:
                    placed = shape.transformGeometry(to_cq_matrix(place(side, th)))
                    for k, s in obst.items():
                        dist = placed.distance(s)
                        if dist < worst[k][0]:
                            worst[k] = (dist, float(th))
                p = samples[label][pname]
                if pname == "arm":
                    p = p[np.hypot(p[:, 0], p[:, 1]) > 30.0]          # leave out the pivot pad
                rob = (1e9, None)
                for th in angles:
                    M = place(side, th)
                    q = p @ M[:3, :3].T + M[:3, 3]
                    dd, _ = tree.query(q, distance_upper_bound=60)
                    if dd.min() < rob[0]:
                        rob = (float(dd.min()), float(th))
                res[pname] = {**{k: _row(v) for k, v in worst.items()}, "rest of robot (31 Oct CAD)": _row(rob)}
            rs[label] = res
        report["sides"][side] = rs
    # slack: the corner parts shifted forward and up (parts.SIZING_SHIFTS), the printed arm only
    report["slack"] = {}
    env = envelope(False)
    for dx, dz in parts.SIZING_SHIFTS[1:]:
        key = f"corner parts {dx:.0f} mm forward, {dz:.0f} mm up"
        report["slack"][key] = {}
        for side in ("left", "right"):
            obst = {k: s.translate(cq.Vector(dx, 0, dz)) for k, s in corner_solids(side).items()}
            worst = (1e9, None, None)
            for th in list(np.arange(0, SWING_DEG, 6.0)) + [SWING_DEG]:
                placed = env.transformGeometry(to_cq_matrix(placement(side, th)))
                for k, s in obst.items():
                    d = placed.distance(s)
                    if d < worst[0]:
                        worst = (d, float(th), k)
            report["slack"][key][side] = {"mm": round(worst[0], 1), "at_deg": worst[1], "nearest": worst[2]}
    # the starting cube (R102), stowed; and the reach, down
    for key, th in (("stowed", 0.0), ("down", SWING_DEG)):
        qs = []
        for pname, (shape, place) in pieces["printed"].items():
            M = place("left", th)
            qs.append(samples["printed"][pname] @ M[:3, :3].T + M[:3, 3])
        q = np.vstack(qs)
        report[key] = {"front_x_in": round(q[:, 0].max() / IN, 2), "top_z_in": round(q[:, 2].max() / IN, 2),
                       "lowest_z_in": round(q[:, 2].min() / IN, 2)}
    report["stowed"]["cube_front_x_in"] = round(CUBE_FRONT_X / IN, 2)
    report["stowed"]["cube_top_z_in"] = 18.0
    h, a, b, c, e = parts.hump()
    report["hump"] = {"height_mm": h, "leaves_line_u_mm": a, "full_u_mm": [b, c], "back_on_line_u_mm": e,
                      "note": "u along the arm from the pivot hole; hole station n is at u = 24 (n - 1)"}
    with open(os.path.join(HERE, "clearance.json"), "w") as f:
        json.dump(report, f, indent=1)
    print(json.dumps(report, indent=1))


if __name__ == "__main__":
    main()
