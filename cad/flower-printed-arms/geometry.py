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
END_PAST = 16.0                                # the printed arm ends this far past both holes (the channel: 24)
ARM_W = 32.0                                   # in the swing plane
ARM_T = 12.0                                   # across, in Y: the channel's band
# Each arm's band in Y (inner face, outer face). The right arm sits 0.45 in further out than the left in the CAD.
ARM_Y = {"left": (137.4, 149.4), "right": (-148.2, -160.2)}

# ---- the corner wheel, its servo and its mount ------------------------------------------------------------------------
# Mentor, 26 Nov: a 3.5 in SWYFT Intake Wheel (vendor/ STEP: 88.74 mm across, 12.7 mm wide), two side by side, on a
# goBILDA Speed servo (continuous) standing up, axle forward; the servo flush against the intake's side wall, under the
# FLOWER stick's arm, just ahead of the front mecanum (mentor's arrows on the 3D view, 26 Nov). The wall: the intake side
# channel's outer face, |Y| 5.363 in; the mecanum module's front at X 192 mm (31 Oct CAD). Not measured; the mount is a guess.
CORNER = dict(
    wheel_d=88.74,           # SWYFT Intake Wheel 3.5 in, from its STEP (vendor/)
    wheel_w=2 * 0.5 * IN,    # two SWYFT wheels side by side, no spacer (mentor, 26 Nov): 2 x 12.7 mm
    wall_y=136.2,            # the intake side channel's outer face, |Y| 5.363 in (24 Nov STEP, via cad/corner-wheels on
                             # claude/corner-module-wall-wip): the servo's INNER face against it (mentor's arrows, 26 Nov)
    drive_front_x=192.0,     # the front mecanum module's front, X (31 Oct CAD)
    servo_gap_x=1.0,         # the servo's back face this far ahead of it
    wheel_z=2.4 * IN,        # PLACEHOLDER: axle height (the rim 0.65 in off the tile; "1-2 in off the ground")
    servo_len=40.0,          # goBILDA Speed servo (2000-0025-0003, vendor/ STEP): the body along its spline (X), with its cap
    servo_w=20.0,            # across (Y); the spline on the body's centre line
    servo_h=40.0,            # upright (Z): the spline 10 mm up from its bottom end
    tab_h=54.3,              # its mounting tabs, upright: 17.15 mm below the spline, 37.15 above
    tab_len=12.8,            # along the spline (X), at the spline end
    hub_gap=6.0,             # PLACEHOLDER: servo face to the wheels' back face (horn, hub)
    mount_wall=4.0,          # PLACEHOLDER: the mount round the servo, inboard and front-back (none outboard: the wall)
    screw_d=20.0,            # PLACEHOLDER: the screw and washer on the wheels' front face, on the axle (mentor, 26 Nov):
    screw_len=8.0,           # a washer up to 20 mm across, the head and washer standing 8 mm proud
)

CLEARANCE = 4.0              # the brief's "about half a hole" (goBILDA 8 mm grid)
CUBE_FRONT_X = 10.43 * IN    # R102: the 18 in starting cube's front (the robot's back at X -7.57 in)
CUBE_TOP_Z = 18.0 * IN


def corner_boxes(side):
    """The corner's parts as simple solids: ('cyl', centre, axis, radius, half-length) or ('box', lo, hi)."""
    c = CORNER
    s = 1 if side == "left" else -1
    y_in = c["wall_y"]
    y_out = y_in + c["servo_w"]
    wy = s * (y_out + y_in) / 2                       # the spline, so the wheels' axle
    sx0 = c["drive_front_x"] + c["servo_gap_x"]
    sx1 = sx0 + c["servo_len"]
    wx0 = sx1 + c["hub_gap"]
    wx1 = wx0 + c["wheel_w"]
    wheel = ("cyl", np.array([(wx0 + wx1) / 2, wy, c["wheel_z"]]), np.array([1.0, 0, 0]), c["wheel_d"] / 2,
             c["wheel_w"] / 2)
    ylo, yhi = sorted((s * y_in, s * y_out))
    zlo, zhi = c["wheel_z"] - 10.0, c["wheel_z"] + c["servo_h"] - 10.0
    servo = ("box", np.array([sx0, ylo, zlo]), np.array([sx1, yhi, zhi]))
    tabs = ("box", np.array([sx1 - c["tab_len"], ylo, c["wheel_z"] - 17.15]),
            np.array([sx1, yhi, c["wheel_z"] - 17.15 + c["tab_h"]]))
    w = c["mount_wall"]
    mlo = np.array([sx0, ylo - w, zlo - w])           # no wall behind: the mecanum is there
    mhi = np.array([wx0 - 1.0, yhi + w, zhi + w])
    if s > 0:
        mlo[1] = ylo                                   # nothing inboard of the intake wall
    else:
        mhi[1] = yhi
    mount = ("box", mlo, mhi)
    screw = ("cyl", np.array([wx1 + c["screw_len"] / 2, wy, c["wheel_z"]]), np.array([1.0, 0, 0]), c["screw_d"] / 2,
             c["screw_len"] / 2)
    return {"wheel": wheel, "servo": servo, "tabs": tabs, "mount": mount, "screw": screw}


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
