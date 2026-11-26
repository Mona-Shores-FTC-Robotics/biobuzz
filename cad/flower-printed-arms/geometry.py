"""Shared geometry for the printed FLOWER stick arms: the robot frame, the arm's line, its swing, and the corner-wheel
parts it must clear.

Robot frame (doc/corner-wheels.md): +X forward, +Y left, +Z up, origin on the floor under the middle of the drive
axles. **Millimetres here**; the README gives inches and goBILDA holes too.

Where the numbers come from:

- **The arm, its pivot and its swing** are the 31 Oct 2026 CAD as cad/advantagescope/Robot_BIOBUZZ holds it
  (`model_1.glb`, `components.json`). Each arm there is an `11 Hole Mini U-Channel (GB)`: 288 mm long, 32 mm wide in
  the swing plane, 12 mm across (in Y). Its pivot hole (the SWYFT servo's hub on the right arm, an 8x14x4 bearing on
  the left) and its tip hole (the 312 mm 8 mm REX shaft, the rollers and the ramp) are 240 mm apart: 10 of the
  channel's 24 mm hole stations, 30 goBILDA 8 mm holes. The ends reach 24 mm past both.
- **The corner wheels are PLACEHOLDERS** until the mentor's hole counts come: the built wheel (a 3.5 in SWYFT Intake
  Wheel, upright, axle forward, a goBILDA Speed servo behind it in a SWYFT mount) is not in any CAD this session can
  read (Onshape's API: HTTP 402 on 26 Nov). They are placed so the straight arm, down, first meets them at its 6th hole
  station from the pivot, which is what the mentor saw. Every number is a constant below; change it and rerun.
"""
import math

import numpy as np

IN = 25.4

# ---- the arm and its swing (31 Oct CAD) -----------------------------------------------------------------------------
PIVOT_XZ = np.array([154.49, 184.62])          # the stick's axis, along Y (components.json)
STOWED_DIR = np.array([-0.44571, 0.89518])     # pivot -> tip hole, stowed (0 deg): up and back
STOWED_PERP = np.array([-STOWED_DIR[1], STOWED_DIR[0]])   # +v: forward-up once the stick is down
SWING_DEG = 162.0                              # FlowerStickSubsystem.swingDegrees(): 0 in, 162 out
PITCH = 24.0                                   # the channel's hole stations (3 goBILDA 8 mm holes)
GRID = 8.0                                     # goBILDA's hole grid
TIP_U = 240.0                                  # pivot hole -> tip hole, along the arm
END_PAST = 24.0                                # the channel reaches this far past both holes
ARM_W = 32.0                                   # in the swing plane
ARM_T = 12.0                                   # across, in Y: the channel's band
# Each arm's band in Y (inner face, outer face). The right arm sits 0.45 in further out than the left in the CAD.
ARM_Y = {"left": (137.4, 149.4), "right": (-148.2, -160.2)}

# ---- the corner wheel, its servo and its mount: PLACEHOLDERS ---------------------------------------------------------
CORNER = dict(
    wheel_d=3.5 * IN,        # SWYFT Intake Wheel, 3.5 in
    wheel_w=1.0 * IN,        # PLACEHOLDER: its width along the axle (X)
    wheel_front_x=10.35 * IN,  # PLACEHOLDER: inside the 18 in cube's front, X 10.43 in
    wheel_y=7.0 * IN,        # PLACEHOLDER: axle |Y| (the rim then reaches |Y| 8.75, inside 9)
    wheel_z=1.85 * IN,       # PLACEHOLDER: axle height (the rim 0.1 in off the tile)
    servo_len=38.0,          # goBILDA Speed servo body along its spline (X), without the spline
    servo_w=40.0,            # across (Y), the spline 10 mm in from its outboard end
    servo_h=20.0,            # height (Z)
    hub_gap=0.30 * IN,       # PLACEHOLDER: servo face to the wheel's back face (horn, hub)
    mount_wall=4.0,          # PLACEHOLDER: the SWYFT mount around the servo, each side
)

CLEARANCE = 4.0              # the brief's "about half a hole" (goBILDA 8 mm grid)
CUBE_FRONT_X = 10.43 * IN    # R102: the 18 in starting cube's front (the robot's back at X -7.57 in)
CUBE_TOP_Z = 18.0 * IN


def corner_boxes(side):
    """The corner's parts as simple solids: ('cyl', centre, axis, radius, half-length) or ('box', lo, hi)."""
    c = CORNER
    s = 1 if side == "left" else -1
    wy = s * c["wheel_y"]
    wx1 = c["wheel_front_x"]
    wx0 = wx1 - c["wheel_w"]
    wheel = ("cyl", np.array([(wx0 + wx1) / 2, wy, c["wheel_z"]]), np.array([1.0, 0, 0]), c["wheel_d"] / 2,
             c["wheel_w"] / 2)
    sx1 = wx0 - c["hub_gap"]
    sx0 = sx1 - c["servo_len"]
    # the spline on the wheel's axle, 10 mm in from the servo's outboard end; the body reaches inboard
    y_out, y_in = wy + s * 10.0, wy - s * (c["servo_w"] - 10.0)
    ylo, yhi = min(y_out, y_in), max(y_out, y_in)
    zlo, zhi = c["wheel_z"] - c["servo_h"] / 2, c["wheel_z"] + c["servo_h"] / 2
    servo = ("box", np.array([sx0, ylo, zlo]), np.array([sx1, yhi, zhi]))
    w = c["mount_wall"]
    mount = ("box", np.array([sx0, ylo - w, zlo - w]), np.array([wx0 - 1.0, yhi + w, zhi + w]))
    return {"wheel": wheel, "servo": servo, "mount": mount}


def rot_y(theta_deg):
    """The stick's turn about its axis (+Y), theta degrees out from stowed, as a 2x2 on (X, Z)."""
    t = math.radians(theta_deg)
    return np.array([[math.cos(t), math.sin(t)], [-math.sin(t), math.cos(t)]])


def arm_to_robot(u, v, y, theta_deg):
    """Arm-local (u along pivot->tip, v perpendicular, +v forward-up when down) to robot frame, mm."""
    u, v, y = np.broadcast_arrays(np.asarray(u, float), np.asarray(v, float), np.asarray(y, float))
    xz0 = np.outer(u, STOWED_DIR) + np.outer(v, STOWED_PERP)
    xz = xz0 @ rot_y(theta_deg).T + PIVOT_XZ
    return np.c_[xz[:, 0], y.ravel(), xz[:, 1]]


def robot_to_arm(p, theta_deg):
    """Robot-frame points (N x 3) to arm-local (u, v, y)."""
    p = np.atleast_2d(p)
    xz = p[:, [0, 2]] - PIVOT_XZ
    xz0 = xz @ rot_y(theta_deg)          # inverse rotation (orthonormal)
    return np.c_[xz0 @ STOWED_DIR, xz0 @ STOWED_PERP, p[:, 1]]


def sample_corner(side, step=1.5):
    """Surface points of the corner's parts, robot frame, mm."""
    pts = []
    for kind in corner_boxes(side).values():
        if kind[0] == "cyl":
            _, c, ax, r, h = kind
            for x in np.arange(-h, h + 1e-6, step):
                for a in np.arange(0, 2 * math.pi, step / r):
                    pts.append(c + [x, r * math.cos(a), r * math.sin(a)])
            for rr in np.arange(0, r, step):
                for a in np.arange(0, 2 * math.pi, step / max(rr, step)):
                    for x in (-h, h):
                        pts.append(c + [x, rr * math.cos(a), rr * math.sin(a)])
        else:
            _, lo, hi = kind
            g = [np.arange(lo[i], hi[i] + 1e-6, step) for i in range(3)]
            for i in range(3):
                for val in (lo[i], hi[i]):
                    a, b = [g[j] for j in range(3) if j != i]
                    A, B = np.meshgrid(a, b)
                    q = np.zeros((A.size, 3))
                    q[:, i] = val
                    q[:, [j for j in range(3) if j != i]] = np.c_[A.ravel(), B.ravel()]
                    pts.extend(q)
    return np.array(pts)
