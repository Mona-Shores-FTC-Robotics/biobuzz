"""Take 2 (mentor, 27 Nov): two other corner layouts, each on a printed mount, each with its own arm. Take 1 (the
folder above) is unchanged: goBILDA's servo frame on the intake channel's hole column, upright wheels 1.98 in up.

- **2a, upright, clear of the mecanum:** the same two 3.5 in SWYFT wheels, axle forward, but the servo's back face in
  line with the front mecanum's front (X 192.1, nothing under it) and the wheels' bottom 1.81 in off the tile (take 1:
  1.98), a little nearer the POLLEN's middle. The servo's flat side against the mount's 6 mm side plate on the intake channel.
- **2b, flat:** one 2.5 in intake wheel lying flat (axle up), its middle at the POLLEN's middle (1.4 in), so it pushes
  each POLLEN sideways into the intake mouth; the servo upright above it, spline down, its body forward, on a 6 mm
  plate under its ears that hangs off the channel. The wheel's front at 10.36 in, inside the 18 in starting cube
  (whose front is about 10.4 in ahead of the drive axles in the 26 Nov STEP).

Both mounts bolt to the intake's upright 5 Hole Lowside U-Channel's outer face, through its hole columns (X 200 and 208,
Z 80.7 + 8k; 26 Nov STEP). Every number is a constant here; corner_boxes() is the shape the hump is sized against."""
import sys, os
import numpy as np
import cadquery as cq
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.dirname(HERE))
import geometry as G   # noqa: E402

IN = 25.4
WALL = G.CORNER["wall_y"]            # the intake channel's outer face, |Y| 135.8
CH_FRONT = 216.1                     # its front edge, X
COLUMNS_X = (200.0, 208.0)           # its two front hole columns
HOLES_Z = [80.7 + 8 * k for k in range(17)]
PLATE = 6.0                          # the mounts' plates
MECANUM_FRONT = 192.1
BALL = 2.8 * IN

# ---- 2a ----
A = dict(servo_back_x=MECANUM_FRONT, wheel_bottom=1.5 * IN + 8.0, wheel_d=88.74, wheel_w=25.4, hub_gap=6.0)   # 1.81 in: 1.5 in
# was one hole too low (mentor, 27 Nov: "it does need to be mounted up one hole"); take 1 rides at 1.98
A["wheel_z"] = A["wheel_bottom"] + A["wheel_d"] / 2
A["wy"] = WALL + PLATE + G.CORNER["servo_in"]            # the servo's flat side on the mount's side plate

def boxes_2a(side):
    s = 1 if side == "left" else -1
    c = G.CORNER; wy = s * A["wy"]; wz = A["wheel_z"]; zc = wz + 10.0
    sx0 = A["servo_back_x"]; sx1 = sx0 + c["servo_len"]; ex = sx0 + c["ear_x"]
    wx0 = sx1 + A["hub_gap"]; wx1 = wx0 + A["wheel_w"]
    ylo, yhi = sorted((wy - s * c["servo_in"], wy + s * c["servo_out"]))
    ear_lo, ear_hi = sorted((s * (WALL + PLATE), wy + s * (c["servo_out"] + 4.0)))
    side_lo, side_hi = sorted((s * WALL, s * (WALL + PLATE)))
    return {
        "wheel": ("cyl", np.array([(wx0 + wx1) / 2, wy, wz]), np.array([1.0, 0, 0]), A["wheel_d"] / 2, A["wheel_w"] / 2),
        "servo": ("box", np.array([sx0, ylo, zc - 20.0]), np.array([sx1, yhi, zc + 20.0])),
        "tabs": ("box", np.array([sx1 - c["tab_len"], ylo, zc - 27.15]), np.array([sx1, yhi, zc + 27.15])),
        "frame": ("box", np.array([ex - PLATE, ear_lo, zc - 31.0]), np.array([ex, ear_hi, zc + 31.0])),     # the ear plate
        "mount_side": ("box", np.array([196.0, side_lo, zc - 31.0]), np.array([ex, side_hi, zc + 31.0])),
        "screw": ("cyl", np.array([wx1 + 4.0, wy, wz]), np.array([1.0, 0, 0]), 10.0, 4.0),
    }

def mount_2a(side="left"):
    """The printed mount, robot frame: a side plate on the channel (6 M4 holes, two columns, three rows) and, square to
    it, a plate under the servo's ears with a window for its body and the ears' four screw holes."""
    s = 1 if side == "left" else -1
    b = boxes_2a(side); f = b["frame"]; m = b["mount_side"]
    zc = A["wheel_z"] + 10.0; wy = s * A["wy"]
    body = cq.Workplane("XY").box(*(f[2] - f[1]), centered=False).translate(tuple(f[1]))
    body = body.union(cq.Workplane("XY").box(*(m[2] - m[1]), centered=False).translate(tuple(m[1])))
    body = body.cut(cq.Workplane("XY").box(20.0, 21.0, 41.0).translate((float(f[1][0] + 3), float(wy + s * 1.0), float(zc))))
    for dz in (-24.75, 24.75):
        for dy in (-5.0, 5.0):
            body = body.cut(cq.Workplane("YZ").circle(1.7).extrude(20).translate((float(f[1][0] - 7), float(wy + dy), float(zc + dz))))
    rows = [z for z in HOLES_Z if zc - 27 < z < zc + 27][:3]
    for x in COLUMNS_X:
        for z in rows:
            body = body.cut(cq.Workplane("XZ").circle(2.2).extrude(-30.0 * s).translate((x, float(s * (WALL - 10)), z)))
    return body

# ---- 2b ----
B = dict(wheel_d=63.5, wheel_w=12.7, wx=231.5)
B["wy"] = WALL + PLATE + G.CORNER["servo_in"]
B["wz"] = BALL / 2

def boxes_2b(side):
    s = 1 if side == "left" else -1
    R = B["wheel_d"] / 2; h = B["wheel_w"]; wx = B["wx"]; wy = s * B["wy"]
    z0, z1 = B["wz"] - h / 2, B["wz"] + h / 2
    pz0 = z1 + 10.5                                  # the plate under the ears: 10.5 mm of horn and hub above the wheel
    ez = pz0 + PLATE                                 # the ears on the plate
    sz0 = ez - 3.5                                   # the case's spline end, 3.5 below the ears' top... (vendor STEP)
    sx0, sx1 = wx - 30.0, wx + 10.0                  # the body runs back from the spline (10 mm off its middle), so its
                                                     # ears stay ahead of the mecanum and the whole lot inside the cube
    ylo, yhi = sorted((wy - s * G.CORNER["servo_in"], wy + s * G.CORNER["servo_out"]))
    plo, phi = sorted((s * (WALL + PLATE), wy + s * 14.0))
    side_lo, side_hi = sorted((s * WALL, s * (WALL + PLATE)))
    return {
        "wheel": ("cyl", np.array([wx, wy, B["wz"]]), np.array([0, 0, 1.0]), R, h / 2),
        "screw": ("cyl", np.array([wx, wy, z0 - 4.0]), np.array([0, 0, 1.0]), 10.0, 4.0),
        "servo": ("box", np.array([sx0, ylo, sz0]), np.array([sx1, yhi, sz0 + 37.0])),
        "tabs": ("box", np.array([wx - 37.15, ylo, ez]), np.array([wx + 17.15, yhi, ez + 3.5])),
        "frame": ("box", np.array([MECANUM_FRONT + 2.0, plo, pz0]), np.array([wx + 21.0, phi, pz0 + PLATE])),
        "mount_side": ("box", np.array([196.0, side_lo, pz0]), np.array([CH_FRONT, side_hi, 104.0])),
    }

def mount_2b(side="left"):
    """The printed mount, robot frame: a side plate on the channel (6 M4 holes) and, square to it at the bottom, a
    plate under the servo's ears with a window for its body and the ears' four screw holes."""
    s = 1 if side == "left" else -1
    b = boxes_2b(side); f = b["frame"]; m = b["mount_side"]
    body = cq.Workplane("XY").box(*(f[2] - f[1]), centered=False).translate(tuple(f[1]))
    body = body.union(cq.Workplane("XY").box(*(m[2] - m[1]), centered=False).translate(tuple(m[1])))
    wx, wy = B["wx"], s * B["wy"]
    body = body.cut(cq.Workplane("XY").box(41.0, 21.0, 20.0).translate((wx - 10.0, float(wy + s * 1.0), float(f[1][2] + 3))))
    for dx in (-24.75, 24.75):
        for dy in (-5.0, 5.0):
            body = body.cut(cq.Workplane("XY").circle(1.7).extrude(20).translate((wx - 10.0 + dx, float(wy + dy), float(f[1][2] - 5))))
    for x in COLUMNS_X:
        for z in (80.7, 88.7, 96.7):
            body = body.cut(cq.Workplane("XZ").circle(2.2).extrude(-30.0 * s).translate((x, float(s * (WALL - 10)), z)))
    return body

LAYOUTS = {"2a_upright": (boxes_2a, mount_2a), "2b_flat": (boxes_2b, mount_2b)}

# ---- 2c: 2b with the servo beside the arm, and a straight arm ----
# The servo, its hub and the mount stay between the intake channel and the arm's band; only the flat wheel (Z 29 to 42)
# passes under the arm, below where it comes down. It needs the LEFT arm 15.3 mm further out, at the right arm's
# |Y| 167.5 to 179.7 (the 26 Nov STEP has it at 152.2 to 164.4, right over the servo): a 15 mm spacer at its pivot and
# its tip, the same arm.
LEFT_ARM_2C = (167.53, 179.73)

def boxes_2c(side):
    b = boxes_2b(side); s = 1 if side == "left" else -1
    lo, hi = b["frame"][1].copy(), b["frame"][2].copy()
    edge = s * (B["wy"] + G.CORNER["servo_out"] + 2.0)          # the shelf stops 2 mm past the servo's outer face
    if s > 0: hi[1] = edge
    else: lo[1] = edge
    b["frame"] = ("box", lo, hi)
    return b

def mount_2c(side="left"):
    """2b's mount with its shelf trimmed to 2 mm past the servo's outer face, so it stays out of the arm's band."""
    s = 1 if side == "left" else -1
    m = mount_2b(side)
    cut_lo = s * (B["wy"] + G.CORNER["servo_out"] + 2.0)
    return m.cut(cq.Workplane("XY").box(200, 60, 200).translate((220.0, float(cut_lo + s * 30.0), 80.0)))

LAYOUTS["2c_straight"] = (boxes_2c, mount_2c)
