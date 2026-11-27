"""Shared geometry for the printed FLOWER stick arms: the robot frame, the arm's line, its swing, and the corner-wheel
parts it must clear.

Robot frame (doc/corner-wheels.md): +X forward, +Y left, +Z up, origin on the floor under the middle of the drive
axles. **Millimetres here**; the README gives inches and goBILDA holes too.

Where the numbers come from:

- **The arm, its pivot and its swing** are the mentor's robot STEP of 26 Nov 2026 (`robotdhs.step`, not in git; the
  session's stripped copy is `corners_and_stick.step` on branch claude/flower-arm-real-cad): the stick's pivot is its
  servo hub's axis, the tip its 384 mm 8 mm REX shaft, 240.0 mm apart, and each arm's band in Y is that arm's 9 Hole
  Mini U-Channel (12.2 mm across; the right arm sits 15 mm further out than the left). The STEP draws the stick down,
  taken here as 162 deg. Before 26 Nov this file used the 31 Oct CAD in cad/advantagescope, whose arms sat 0.3 in
  further forward, 0.3 in lower and 0.6 to 0.8 in further in.
- **The corner wheels are PLACEHOLDERS** (the servo frame is goBILDA's 1802-0043-0001, from its STEP) until the mentor's hole counts come: the built wheel (a 3.5 in SWYFT Intake
  Wheel, upright, axle forward, a goBILDA Speed servo behind it in a SWYFT mount) is not in any CAD this session can
  read (Onshape's API: HTTP 402 on 26 Nov). They are placed so the straight arm, down, first meets them at its 6th hole
  station from the pivot, which is what the mentor saw. Every number is a constant below; change it and rerun.
"""
import math

import numpy as np

IN = 25.4

# ---- the arm and its swing (26 Nov STEP) -----------------------------------------------------------------------------
PIVOT_XZ = np.array([146.16, 192.60])          # the stick's axis, along Y: the mentor's robot STEP of 26 Nov (its servo hub)
STOWED_DIR = np.array([-0.44330, 0.89637])     # pivot -> tip hole, stowed (0 deg): the STEP's stick, drawn down, turned back 162 deg
STOWED_PERP = np.array([-STOWED_DIR[1], STOWED_DIR[0]])   # +v: forward-up once the stick is down
SWING_DEG = 162.0                              # FlowerStickSubsystem.swingDegrees(): 0 in, 162 out
PITCH = 24.0                                   # the channel's hole stations (3 goBILDA 8 mm holes)
GRID = 8.0                                     # goBILDA's hole grid
TIP_U = 240.0                                  # pivot hole -> tip hole, along the arm
END_PAST = 16.0                                # the printed arm ends this far past both holes (the channel: 24)
ARM_W = 32.0                                   # in the swing plane
ARM_T = 12.0                                   # across, in Y: the channel's band
# Each arm's band in Y (inner face, outer face). The right arm sits 0.45 in further out than the left in the CAD.
ARM_Y = {"left": (152.23, 164.43), "right": (-167.53, -179.73)}   # the arms' bands in the 26 Nov STEP (9 Hole Mini U-Channels)

# ---- the corner wheel, its servo and its mount ------------------------------------------------------------------------
# Mentor, 26 Nov: a 3.5 in SWYFT Intake Wheel (vendor/ STEP: 88.74 mm across, 12.7 mm wide), two side by side, on a
# goBILDA Speed servo (continuous) standing up, axle forward, in goBILDA's 1802 Servo Frame (vendor/ STEP), "bolted on the
# side so that the servo is basically flush with that wall". The wall is the intake's upright 5 Hole Lowside U-Channel
# (26 Nov STEP: X 167.9 to 216.1, |Y| 123.8 to 135.8, Z from 72.6 up), whose outer face carries a column of holes at
# X 208.0, Z 80.7 + 8k, next to its front edge (the mentor circled it). The frame's long edge lies on that face (option A,
# mentor, 26 Nov), its edge holes (through the plate's middle, at -24, 0, +8 and +24 mm from the window's middle) on the
# column, so the servo comes back through the window to X 183.8, over the front mecanum's front 8 mm, whose rollers reach
# Z 80.1 there: the servo sits "high enough to clear it" (mentor), so its middle is on hole k = 3, the lowest with its
# body and cable boss above the rollers.
CORNER = dict(
    wheel_d=88.74,           # SWYFT Intake Wheel 3.5 in, from its STEP (vendor/)
    wheel_w=2 * 0.5 * IN,    # two SWYFT wheels side by side, no spacer (mentor, 26 Nov): 2 x 12.7 mm
    wall_y=135.8,            # the intake channel's outer face, |Y| (26 Nov STEP)
    hole_x=208.0,            # its front column of 4 mm holes, X; the holes at Z hole_z0 + 8k (26 Nov STEP)
    hole_z0=80.7,
    frame_holes_up=3,        # the servo body's middle on the column's hole hole_z0 + 8 x this (3: its body 4.6 mm, its
                             # cable boss 0.6 mm, above the mecanum's rollers; one lower is in them)
    frame_w=43.0,            # goBILDA 1802-0043-0001 Servo Frame (vendor/ STEP): a 6 mm aluminium plate, 59 x 43, the servo's
    frame_len=59.0,          # body through its 41 x 21 window (in the plate's middle) and its ears on it
    frame_t=6.0,
    ear_x=27.2,              # the servo's ears' underside, from its body's back end (vendor/ STEP)
    servo_len=40.0,          # goBILDA Speed servo (2000-0025-0003, vendor/ STEP): the body along its spline (X), with its cap
    servo_in=8.2,            # across (Y), from the spline: 8.2 inboard (its flat side, toward the channel), 10.2 outboard
    servo_out=10.2,
    servo_h=40.0,            # upright (Z): the spline 10 mm below the body's middle
    tab_h=54.3,              # its ears, upright, 27.15 either side of the body's middle
    tab_len=12.8,            # along the spline (X): the ears and the case above them
    hub_gap=6.0,             # PLACEHOLDER: servo face to the wheels' back face (horn, hub)
    screw_d=20.0,            # PLACEHOLDER: the screw and washer on the wheels' front face, on the axle (mentor, 26 Nov):
    screw_len=8.0,           # a washer up to 20 mm across, the head and washer standing 8 mm proud
)
CORNER["frame_z"] = CORNER["hole_z0"] + 8.0 * CORNER["frame_holes_up"]   # the servo body's middle, Z
CORNER["wheel_z"] = CORNER["frame_z"] - 10.0                               # the spline, so the wheels' axle

CLEARANCE = 4.0              # the brief's "about half a hole" (goBILDA 8 mm grid)
CUBE_FRONT_X = 10.43 * IN    # R102: the 18 in starting cube's front (the robot's back at X -7.57 in)
CUBE_TOP_Z = 18.0 * IN


def corner_boxes(side):
    """The corner's parts as simple solids: ('cyl', centre, axis, radius, half-length) or ('box', lo, hi)."""
    c = CORNER
    s = 1 if side == "left" else -1
    wy = s * (c["wall_y"] + c["frame_w"] / 2)         # the spline, so the wheels' axle: the frame window's middle
    fx0 = c["hole_x"] - c["frame_t"] / 2              # the frame plate, its edge holes on the wall's hole column
    fx1 = fx0 + c["frame_t"]
    sx0 = fx1 - c["ear_x"]                            # the servo body's back end
    sx1 = sx0 + c["servo_len"]
    wx0 = sx1 + c["hub_gap"]
    wx1 = wx0 + c["wheel_w"]
    zc, wz = c["frame_z"], c["wheel_z"]
    wheel = ("cyl", np.array([(wx0 + wx1) / 2, wy, wz]), np.array([1.0, 0, 0]), c["wheel_d"] / 2, c["wheel_w"] / 2)
    ylo, yhi = sorted((wy - s * c["servo_in"], wy + s * c["servo_out"]))
    servo = ("box", np.array([sx0, ylo, zc - c["servo_h"] / 2]), np.array([sx1, yhi, zc + c["servo_h"] / 2]))
    tabs = ("box", np.array([sx1 - c["tab_len"], ylo, zc - c["tab_h"] / 2]), np.array([sx1, yhi, zc + c["tab_h"] / 2]))
    flo, fhi = sorted((wy - c["frame_w"] / 2, wy + c["frame_w"] / 2))
    frame = ("box", np.array([fx0, flo, zc - c["frame_len"] / 2]), np.array([fx1, fhi, zc + c["frame_len"] / 2]))
    screw = ("cyl", np.array([wx1 + c["screw_len"] / 2, wy, wz]), np.array([1.0, 0, 0]), c["screw_d"] / 2,
             c["screw_len"] / 2)
    return {"wheel": wheel, "servo": servo, "tabs": tabs, "frame": frame, "screw": screw}


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
