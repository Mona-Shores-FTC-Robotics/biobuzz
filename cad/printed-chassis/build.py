"""The printed clean-sheet robot, layout v1 (10 Oct 2026, issue #172, doc/printed-chassis.md): a goBILDA ladder with
printed modules, drawn from the Sister five Auto. Every mechanism is here at its real size and place; screws come in v2.

    python3 cad/printed-chassis/build.py          # checks, then writes printed-chassis.step, stl/ and parts.md
    CHECK_ONLY=1 python3 cad/printed-chassis/build.py

Frame: the AdvantageScope model's. +X forward, +Y left, +Z up, inches, origin on the floor under the body's centre.
The body is 14.6 in, face to back (FACE = 7.3); the fixed flaps reach 3.4 in ahead of the face, so the robot starts at
18.0 x 18.0 with nothing to deploy but the FLOWER extractor. Exported in millimetres.

Bought parts' sizes are read from goBILDA's own STEP files (bounding boxes; noted where used). Where a size isn't from a
vendor file it says CHECK.

Modules (each comes off on its own; doc/printed-chassis.md, "The frame"):
    frame      two 1120 rails, a low rear cross channel, a high front cross channel on two printed uprights
    pod_XX     four drive pods: wheel on its own shaft between two printed plates, its motor face-mounted above, belted
    front_X    the two fixed flaps, which are also the side plates carrying the extractor's stubs
    extractor  the FLOWER extractor (cad/intake-b's geometry, its arms printed)
    intake     the roller on two printed swing arms; its motor rides the right arm's tail
    lane       ramp, walls, five roller shafts, a sprung ceiling of free rollers, floor and backstop
    launcher   flywheel cassettes, feeder and its motor, pad, top plate, printed turret ring on four bearing rollers, hood
    elec       tray, Control Hub, Expansion Hub, battery, Limelight mast
"""
import math, os, sys
import cadquery as cq

HERE = os.path.dirname(os.path.abspath(__file__))
MM = 1 / 25.4
CHECK_ONLY = os.environ.get("CHECK_ONLY") == "1"

# ---------------------------------------------------------------- the outline the Auto asks for
FACE, BACK = 7.3, -7.3                  # body 14.6 in
HALF_W = 7.62                           # 15.24 in wide over the pods, the simulator's frameWidthIn
FLAP_TIP = (FACE + 3.4, 9.0)            # the simulator's flap: hinged at the front corner, tip 3.4 ahead and 1.38 out
START_CUBE = 18.0
BED = (210.0, 210.0, 200.0)             # the smallest common home printer's build volume, mm (doc: "Printers and materials")

# ---------------------------------------------------------------- pieces
RP, RN = 1.40, 1.81                     # POLLEN, NECTAR radii (2.80 / 3.62 in)

# ---------------------------------------------------------------- bought parts' sizes (goBILDA STEP files)
CH = 48 * MM                            # 1120 U-channel: 48 x 48 mm, 2.5 mm wall
CH_LOW = 12 * MM                        # 1121 low-side U-channel: 48 mm web, 12 mm sides
CH_T = 2.5 * MM
WHEEL_R, WHEEL_W = 96 / 2 * MM, 37.2 * MM          # 3213-3606-0002, 96 mm mecanum
MOTOR_SQ = 37.5 * MM                    # 5203 Yellow Jacket: gearbox and can fit a 37.5 mm square
MOTOR_L1, MOTOR_L2 = 107.7 * MM, 116.5 * MM        # body behind the gearbox face: one-stage (1150, 1620, 6000 RPM), two-stage (312, 435)
MOTOR_SHAFT = 23.5 * MM                 # 8mm REX output, from the gearbox face
PULLEY24_D = 38.2 * MM                  # 24T HTD5: pitch diameter (5 mm x 24 / pi); CHECK the flange diameter
PULLEY_W = 15 * MM                      # CHECK: 3417 pulley with flanges
BELTS = (215, 225, 245, 265, 275, 295, 315, 320, 340, 360, 370, 380, 390, 410, 420, 425, 445, 460, 470, 485, 505)   # goBILDA 3412-0009-xxxx, mm (Oct 2026)

def centres(belt_mm):
    """Centre distance, in, for a goBILDA 9 mm HTD5 belt round two 24T pulleys (pitch length = 2C + 24 x 5 mm)."""
    assert belt_mm in BELTS, belt_mm
    return (belt_mm - 120) / 2 * MM

def at(origin, toward, d):
    """The point d from origin along the line toward a point."""
    dx, dz = toward[0] - origin[0], toward[1] - origin[1]; n = math.hypot(dx, dz)
    return (origin[0] + dx / n * d, origin[1] + dz / n * d)

# ---------------------------------------------------------------- frame
RAIL_OUT = 5.0                          # the rails' webs' outer faces, |Y|
RAIL_IN = RAIL_OUT - CH_LOW             # their flanges' inner edges (4.53): low-side, so the feeder's wheels pass over them
RAIL_Z = (0.75, 0.75 + CH)              # 1120 on its side, open inward
RAIL_X = (-336 * MM / 2, 336 * MM / 2)  # 1121-0013-0336
# ---------------------------------------------------------------- drive pods: identical, the front ones' motors higher (over the lane)
AX = 5.41                               # FACE - WHEEL_R: the front wheels flush with the face, as today's
WHEEL_Y = (5.915, 5.915 + WHEEL_W)      # wheel between the pod's plates; outer face 7.38
POD_IN = (RAIL_OUT, RAIL_OUT + 0.2)     # inner plate (5 mm PETG), against the rail's web
PULLEY_Y = (5.25, 5.25 + PULLEY_W)      # the belt's plane, between the inner plate and the wheel
POD_OUT = (7.42, HALF_W)                # outer plate
POD_X = 3.9                             # the pod's length along X, centred on its axle
POD_TOP = 4.05
DRIVE_BELT = {1: 340, -1: 225}          # front: the motor over the lane's ceiling; rear: straight above the axle
DRIVE_MOTOR = {sx: (sx * AX, WHEEL_R + centres(DRIVE_BELT[sx])) for sx in (1, -1)}

# ---------------------------------------------------------------- intake
ROLL_R = 1.0                            # 2 in vector wheels, as cad/intake-b
ROLL = (FACE + 1.0, 3.4)                # axle at rest: bottom 2.4 in off the tiles, front 2.0 in ahead of the face (as today's)
ROLL_HALF = 6.5                         # the roller's wheels; its pulley at 6.55..6.9, the arms at 6.95..7.25
ARM_Y = (6.95, 7.25)
BELT_Y = (6.55, 6.9)
PIVOT = (2.6, 4.05)                     # the arms' pivot: behind the front pods, level with the roller's mid-float, so it rises straight up
FLOAT = 1.3                             # the roller rises this much (a NECTAR passes under)
ROLLER_BELT = 410
INTAKE_MOTOR = at(ROLL, (3.5, 6.3), centres(ROLLER_BELT))   # on the right arm, above and ahead of the pivot: its belt to the roller clears the front wheel

# ---------------------------------------------------------------- extractor (cad/intake-b's, relative to the face)
EX_AXIS = (FACE + 2.4, 4.5)
EX_ARM_Y = 4.2
EX_CROSS = (FACE + 5.37, 1.025)         # the block's cross shaft, down
EX_BLOCK = ((FACE + 4.44, FACE + 5.84), (0.7, 1.35))
EX_STOW = 146.0

# ---------------------------------------------------------------- lane and the 4-piece count (cad/transfer's window)
LANE_BOTTOM = 1.3
WALL_IN, WALL_T = 1.86, 0.25
COUNT_WINDOW = (12.26, 12.60)           # roller axle to backstop: 3 NECTAR + 1 POLLEN must fit, 5 POLLEN and 4 NECTAR must not
BACKSTOP_X = ROLL[0] - 12.43
COL_X = BACKSTOP_X + 1.765              # the launch column: the first ball's centre against the backstop, under the turret's axis
LANE_SHAFTS = [ROLL[0] - 3.16 - 1.4 * k for k in range(5)]
RAMP = ((FACE + 0.44, 0.05), (FACE - 1.86, LANE_BOTTOM))

# ---------------------------------------------------------------- launcher (cad/transfer's, about the column)
FLY_R, FLY_Y, FLY_Z = 96 / 2 * MM, 2.85, 6.65
FLY_X = (COL_X - 0.95, COL_X + 0.95)
FEED = (2.87, 3.30, 36 * MM)            # feeder axle Y, z, radius (72 mm Gecko)
FLY_BELT = 315
FLY_MOTOR = (FLY_Y + math.sqrt(centres(FLY_BELT) ** 2 - (7.0 - FLY_Z) ** 2), 7.0)                 # |Y|, z; along X, face 0.6 behind the column, shaft back
FEED_BELT = 225
FEED_MOTOR = at((2.87, 3.30), (3.9, 5.0), centres(FEED_BELT))   # Y, z; along X ahead of the feeder, shaft back, belted 1:1
RING = (8.75, 9.25, 3.0, 105 / 2 * MM)  # printed turret ring: z0, z1, outer radius, bore radius (the ball's path)

FRONT_CROSS_X, FRONT_CROSS_Z = 5.3, 7.0
# ---------------------------------------------------------------- electronics (CHECK every size here against the real parts)
TRAY_X, TRAY_Z, TRAY_HALF = (0.9, 7.2), FRONT_CROSS_Z + CH + 0.2, 4.1   # tray on the front cross channel
HUB = (5.63, 4.06, 1.0)                 # REV Control / Expansion Hub, CHECK
BATTERY = (5.7, 3.3, 1.8)               # CHECK

# ================================================================ helpers (inches)
def box(x0, x1, y0, y1, z0, z1):
    return cq.Workplane("XY").box(abs(x1 - x0), abs(y1 - y0), abs(z1 - z0)).translate(((x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2))

def cyl(axis, c, d, a0, a1):
    """A cylinder of diameter d along 'x' | 'y' | 'z' from a0 to a1, through c (the other two coordinates)."""
    lo, L = min(a0, a1), abs(a1 - a0)
    if axis == "x": return cq.Workplane("YZ").circle(d / 2).extrude(L).translate((lo, c[0], c[1]))
    if axis == "y": return cq.Workplane("XZ").circle(d / 2).extrude(-L).translate((c[0], lo, c[1]))
    return cq.Workplane("XY").circle(d / 2).extrude(L).translate((c[0], c[1], lo))

def slab_y(pts, y0, y1):
    """A prism in the X-Z plane (pts as (x, z)), from y0 to y1."""
    return cq.Workplane("XZ").polyline(pts).close().extrude(-(y1 - y0)).translate((0, y0, 0))

def belt_y(p0, p1, d, y0, y1):
    """A belt wrapped round two equal pulleys in an X-Z plane, as a solid (the clash checks see its whole run)."""
    return cyl("y", p0, d + 0.1, y0, y1).union(cyl("y", p1, d + 0.1, y0, y1)).union(
        slab_y(_strip(p0, p1, (d + 0.1) / 2), y0, y1))

def belt_x(p0, p1, d, x0, x1):
    """The same in a Y-Z plane."""
    s = _strip(p0, p1, (d + 0.1) / 2)
    return cyl("x", p0, d + 0.1, x0, x1).union(cyl("x", p1, d + 0.1, x0, x1)).union(
        cq.Workplane("YZ").polyline(s).close().extrude(x1 - x0).translate((x0, 0, 0)))

def _strip(p0, p1, r):
    dx, dz = p1[0] - p0[0], p1[1] - p0[1]; n = math.hypot(dx, dz); ox, oz = -dz / n * r, dx / n * r
    return [(p0[0] + ox, p0[1] + oz), (p1[0] + ox, p1[1] + oz), (p1[0] - ox, p1[1] - oz), (p0[0] - ox, p0[1] - oz)]

def channel_x(x0, x1, y_web, inward, z0, depth=CH):
    """A U-channel along X on its side: web vertical at |Y| y_web (outer face), flanges `depth` deep pointing inward."""
    s = -1 if inward else 1
    web = box(x0, x1, y_web, y_web + s * CH_T, z0, z0 + CH)
    return web.union(box(x0, x1, y_web, y_web + s * depth, z0, z0 + CH_T)).union(box(x0, x1, y_web, y_web + s * depth, z0 + CH - CH_T, z0 + CH))

def channel_y(y0, y1, x0, z0, open_down=False):
    """A 1120 U-channel along Y, web horizontal (on top if open_down)."""
    zw = z0 + CH - CH_T if open_down else z0
    return box(x0, x0 + CH, y0, y1, zw, zw + CH_T).union(box(x0, x0 + CH_T, y0, y1, z0, z0 + CH)).union(box(x0 + CH - CH_T, x0 + CH, y0, y1, z0, z0 + CH))

def motor_y(face, axis_xz, sign, length=MOTOR_L2):
    """A Yellow Jacket along Y: gearbox face at Y = face, shaft toward sign (+1 = +Y), body the other way."""
    body = box(axis_xz[0] - MOTOR_SQ / 2, axis_xz[0] + MOTOR_SQ / 2, face, face - sign * length,
               axis_xz[1] - MOTOR_SQ / 2, axis_xz[1] + MOTOR_SQ / 2)
    return body.union(cyl("y", axis_xz, 8 * MM, face, face + sign * MOTOR_SHAFT))

def motor_x(face, yz, sign, length=MOTOR_L1):
    body = box(face, face - sign * length, yz[0] - MOTOR_SQ / 2, yz[0] + MOTOR_SQ / 2, yz[1] - MOTOR_SQ / 2, yz[1] + MOTOR_SQ / 2)
    return body.union(cyl("x", yz, 8 * MM, face, face + sign * MOTOR_SHAFT))

def rot_y(wp, about_xz, deg):
    """Rotate about a line along Y through (x, z); positive raises what's ahead of it."""
    return wp.rotate((about_xz[0], 0, about_xz[1]), (about_xz[0], 1, about_xz[1]), -deg)

# ================================================================ parts
PARTS = {}          # name -> dict(shape, module, kind ('print' | 'buy' | 'cut'), what, moves)

def part(module, name, shape, kind, what, moves=None):
    assert name not in PARTS, name
    PARTS[name] = dict(shape=shape, module=module, kind=kind, what=what, moves=moves)

SIDES = ((1, "L"), (-1, "R"))
def mir(wp, s): return wp if s > 0 else wp.mirror("XZ")

# ---- frame
for s, n in SIDES:
    part("frame", f"rail_{n}", mir(channel_x(*RAIL_X, RAIL_OUT, True, RAIL_Z[0], CH_LOW), s), "buy", "goBILDA 1121-0013-0336 low-side U-channel, 13 hole (336 mm), as today's rails")
part("frame", "rear_cross", channel_y(-120 * MM, 120 * MM, RAIL_X[0] + CH_T, RAIL_Z[0]), "buy", "goBILDA 1120-0009-0240 U-channel, 9 hole (240 mm), between the rails' webs on goBILDA pattern brackets (CHECK the bracket)")
part("frame", "front_cross", channel_y(-108 * MM, 108 * MM, FRONT_CROSS_X, FRONT_CROSS_Z, open_down=True), "buy", "goBILDA 1120-0008-0216 U-channel, 8 hole (216 mm), over the lane and the front drive motors")
for s, n in SIDES:
    up = box(6.2, FRONT_CROSS_X + CH, 3.6, RAIL_OUT - CH_T - 0.02, RAIL_Z[1], FRONT_CROSS_Z)
    part("frame", f"front_upright_{n}", mir(up, s), "print", "PETG: stands on the rail's top flange, carries the front cross channel")

# ---- drive pods
for sx, fx in ((1, "F"), (-1, "B")):
    ax = sx * AX; mx, mz = DRIVE_MOTOR[sx]
    for s, n in SIDES:
        nm = fx + n
        inner_top = mz + 0.8
        pod = box(ax - POD_X / 2, ax + POD_X / 2, *POD_IN, 0.5, POD_TOP)
        pod = pod.union(box(mx - 0.9, mx + 0.9, *POD_IN, POD_TOP - 0.1, inner_top))            # the motor's mount, up the inner plate
        pod = pod.union(box(ax - POD_X / 2, ax + POD_X / 2, *POD_OUT, 0.5, POD_TOP))
        pod = pod.union(box(ax - POD_X / 2, ax + POD_X / 2, PULLEY_Y[1] + 0.03, POD_OUT[1], POD_TOP - 0.2, POD_TOP))   # bridge over the wheel
        for e in (-1, 1): pod = pod.union(box(ax + e * 0.95, ax + e * POD_X / 2, POD_IN[0], POD_OUT[1], POD_TOP - 0.2, POD_TOP))   # the bridge's ends cross the belt's plane, clear of the belt
        if sx > 0:
            pod = pod.cut(box(FACE - 0.1, FACE + 1, 0, 8, 0, 8)).cut(box(FACE - 0.6, FACE + 1, POD_OUT[0] - 0.01, 8, 0, 8))   # the flap's root closes the front
            pod = pod.cut(slab_y([(INTAKE_MOTOR[0], INTAKE_MOTOR[1] - 0.95), (ROLL[0], ROLL[1] - 0.95), (ROLL[0], 9), (INTAKE_MOTOR[0], 9)], PULLEY_Y[1] + 0.02, 8))   # under the roller's belt and arm
        else: pod = pod.cut(box(BACK - 1, BACK, 0, 8, 0, 8))
        pod = pod.cut(cyl("y", (ax, WHEEL_R), 14 * MM, 0, 8)).cut(cyl("y", (mx, mz), 0.6, 0, 8))
        part(f"pod_{nm}", f"pod_{nm}", mir(pod, s), "print", "PETG: inner plate, outer plate and bridge in one; wheel bearings in both plates, motor on the inner")
        part(f"pod_{nm}", f"wheel_{nm}", mir(cyl("y", (ax, WHEEL_R), 2 * WHEEL_R, *WHEEL_Y), s), "buy", "goBILDA 3213-3606-0002 96 mm mecanum (set of 4)")
        part(f"pod_{nm}", f"wheel_shaft_{nm}", mir(cyl("y", (ax, WHEEL_R), 8 * MM, POD_IN[0] + 0.01, POD_OUT[1] - 0.01), s), "buy", "goBILDA 2106-4008-0640 8mm REX shaft, 64 mm (CHECK the length)")
        part(f"pod_{nm}", f"drive_motor_{nm}", mir(motor_y(POD_IN[0], (mx, mz), 1), s), "buy", "goBILDA 5203 Yellow Jacket, as today's drive (CHECK the ratio)")
        part(f"pod_{nm}", f"drive_belt_{nm}", mir(belt_y((ax, WHEEL_R), (mx, mz), PULLEY24_D, *PULLEY_Y), s), "buy",
             f"goBILDA 3417-4008-0024 24T HTD5 pulleys x2, 3412-0009-0{DRIVE_BELT[sx]} belt")

# ---- the fixed flaps: also the side plates that carry the extractor's stubs
for s, n in SIDES:
    root, tip = (FACE, HALF_W), FLAP_TIP
    ang = math.atan2(tip[1] - root[1], tip[0] - root[0]); L = math.hypot(tip[0] - root[0], tip[1] - root[1])
    T, FOAM = 0.25, 0.5
    plate = box(0, L, -T, 0, 0.25, 4.25).union(box(L * (2.4 - 0.5) / 3.4, L * (2.4 + 0.5) / 3.4, -T, 0, 4.25, 5.0))   # taller round the extractor's stub
    plate = plate.rotate((0, 0, 0), (0, 0, 1), math.degrees(ang)).translate((root[0], root[1], 0))
    plate = plate.union(box(FACE - 0.6, FACE + 0.05, POD_OUT[0], HALF_W, 0.5, POD_TOP))                              # the root: the front pod's outer plate's last 0.6 in
    foam = box(L * 0.5, L, -T - FOAM, -T, 0.25, 4.25).rotate((0, 0, 0), (0, 0, 1), math.degrees(ang)).translate((root[0], root[1], 0))
    trim = box(-20, FLAP_TIP[0], -20, 20, -20, 20)
    plate, foam = plate.intersect(trim), foam.intersect(trim)
    part(f"front_{n}", f"flap_{n}", mir(plate, s), "print", "PETG, 6 mm: the fixed flap and the extractor's side plate; bolts to the front pod")
    part(f"front_{n}", f"flap_foam_{n}", mir(foam, s), "buy", "1/2 in EVA or polyethylene foam on a clip-on backer (the drop test picks it)")

# ---- extractor (moves: angle 0 down .. 146 stowed)
def extractor(deg):
    out = {}
    arm_len = math.hypot(EX_CROSS[0] - EX_AXIS[0], EX_CROSS[1] - EX_AXIS[1])
    for s, n in SIDES:
        y0 = EX_ARM_Y - 0.12
        arm = slab_y(_strip(EX_AXIS, EX_CROSS, 0.36), y0, y0 + 0.24)
        out[f"ex_arm_{n}"] = (mir(arm, s), "print", "PETG, 6 mm: extractor arm (cad/intake-b's, printed)")
        stub_out = 8.3
        out[f"ex_stub_{n}"] = (mir(cyl("y", EX_AXIS, 8 * MM, EX_ARM_Y, stub_out), s), "buy", "goBILDA 1516-4008-0960 8mm REX standoff, cut")
    out["ex_cross"] = (cyl("y", EX_CROSS, 8 * MM, -EX_ARM_Y - 0.12, EX_ARM_Y + 0.12), "buy", "goBILDA 1516-4008-2160 8mm REX standoff (216 mm)")
    out["ex_block"] = (box(*EX_BLOCK[0], -2.4, 2.4, *EX_BLOCK[1]), "print", "PETG: the FLOWER block (doc/ramp-hook.md's profile; drawn as its box here)")
    res = {}
    for k, (w, kind, what) in out.items():
        res[k] = (w if k.startswith("ex_stub") else rot_y(w, EX_AXIS, deg), kind, what)
    return res
for k, (w, kind, what) in extractor(EX_STOW).items(): part("extractor", k, w, kind, what, moves="extractor")
part("extractor", "ex_servo", box(FACE + 1.3, FACE + 2.9, -7.85, -7.3, 4.85, 5.85), "buy", "goBILDA 2000-0025-0002 Torque servo, on the right flap, 1:1 printed gear pair to the right stub (CHECK the box)")

# ---- intake (moves: lift 0 .. FLOAT)
def lift_deg(lift):
    v = (ROLL[0] - PIVOT[0], ROLL[1] - PIVOT[1]); r = math.hypot(*v); a0 = math.atan2(v[1], v[0])
    return math.degrees(math.asin((v[1] + lift) / r) - a0)

def intake(lift):
    out = {}
    out["roller"] = (cyl("y", ROLL, 2 * ROLL_R, -ROLL_HALF, ROLL_HALF), "buy", "WCP-0353 x6 / WCP-0354 x6 2 in vector wheels and a 48 mm gecko, as cad/intake-b (or printed TPU, if a test says so)")
    out["roller_shaft"] = (cyl("y", ROLL, 8 * MM, -ARM_Y[1], ARM_Y[1]), "buy", "goBILDA 8mm REX shaft, cut to 371 mm (CHECK)")
    for s, n in SIDES:
        y0, y1 = ARM_Y
        arm = slab_y(_strip(INTAKE_MOTOR, ROLL, 0.45), y0, y1).union(cyl("y", ROLL, 0.9, y0, y1)).union(cyl("y", INTAKE_MOTOR, 1.2, y0, y1))   # along the belt, over the wheel
        arm = arm.union(slab_y(_strip(PIVOT, INTAKE_MOTOR, 0.35), y0, y1)).union(cyl("y", PIVOT, 0.8, y0, y1))                                  # down to the pivot
        out[f"intake_arm_{n}"] = (mir(arm, s), "print", "PETG: swing arm, pivot to roller bearing (the right one also carries the motor)")
    out["roller_belt"] = (mir(belt_y(INTAKE_MOTOR, ROLL, PULLEY24_D, *BELT_Y), -1), "buy",
                          f"goBILDA 3417-4008-0024 24T HTD5 pulleys x2, 3412-0009-0{ROLLER_BELT} belt")
    out["intake_motor"] = (mir(motor_y(BELT_Y[0] - 0.02, INTAKE_MOTOR, 1, MOTOR_L1), -1), "buy", "goBILDA 5203-2402-0003 Yellow Jacket, 1620 RPM: the roller (about 170 in/s at its surface) and the lane")
    d = lift_deg(lift)
    return {k: (rot_y(w, PIVOT, d), kind, what) for k, (w, kind, what) in out.items()}
for k, (w, kind, what) in intake(0).items(): part("intake", k, w, kind, what, moves="intake")
for s, n in SIDES:
    br = box(PIVOT[0] - 0.5, PIVOT[0] + 0.5, RAIL_OUT, HALF_W, RAIL_Z[0], RAIL_Z[1]).union(box(PIVOT[0] - 0.5, PIVOT[0] + 0.5, ARM_Y[1] + 0.02, HALF_W, RAIL_Z[0], PIVOT[1] + 0.4))
    part("intake", f"intake_pivot_{n}", mir(br, s), "print", "PETG: pivot bracket on the rail's web, outside the arm; an M5 shoulder screw is the pivot")

# ---- lane
(rx0, rz0), (rx1, rz1) = RAMP
part("lane", "ramp", slab_y([(rx0, rz0), (rx1, rz1), (rx1, rz1 - 0.08), (rx0, rz0 - 0.05)], -WALL_IN + 0.01, WALL_IN - 0.01), "print", "PETG: the ramp, in slots in the walls")
LANE_X = (COL_X + 1.4, FACE - 0.16)
for s, n in SIDES:
    part("lane", f"lane_wall_{n}", mir(box(*LANE_X, WALL_IN, WALL_IN + WALL_T, 0.3, 2.6), s), "print", "PETG, 1/4 in: lane wall with the shafts' bearings; on two REX standoffs to the rail")
for k, x in enumerate(LANE_SHAFTS):
    part("lane", f"lane_shaft_{k}", cyl("y", (x, LANE_BOTTOM - 12 * MM), 8 * MM, -WALL_IN - WALL_T, WALL_IN + WALL_T), "buy", "goBILDA 8mm REX shaft, 144 mm (2106-4008-1440)")
    part("lane", f"lane_roller_{k}", cyl("y", (x, LANE_BOTTOM - 12 * MM), 24 * MM, (-1 if k % 2 else 0) * 1.0, (0 if k % 2 else 1) * 1.0), "print", "TPU 95A: 24 mm x 1 in lane roller")
CEIL_X = (COL_X + 1.6, 4.5)
CEIL_Z = LANE_BOTTOM + 2 * RP - 0.2     # the free rollers' bottoms, at rest (a POLLEN presses them 0.2)
part("lane", "ceiling", box(*CEIL_X, -WALL_IN, WALL_IN, CEIL_Z + 0.8, CEIL_Z + 0.95), "print",
     "PETG: the sprung ceiling's frame, on pins in slotted posts (as cad/transfer), carrying free rollers")
for k in range(4):
    x = CEIL_X[0] + 0.5 + k * (CEIL_X[1] - CEIL_X[0] - 1.0) / 3
    part("lane", f"ceiling_roller_{k}", cyl("y", (x, CEIL_Z + 0.4), 0.8, -1.5, 1.5), "print",
         "PETG: a free-spinning ceiling roller on two 608 bearings (the ball moves at the lane's full tread speed, not half)")
part("lane", "floor", box(BACKSTOP_X - 0.2, COL_X + 1.4, -1.7, 1.45, LANE_BOTTOM - 0.13, LANE_BOTTOM), "print", "PETG: the floor under the feeder and pad")
part("lane", "backstop", box(BACKSTOP_X - 0.2, BACKSTOP_X, -1.2, 1.2, LANE_BOTTOM, 3.0), "print", "PETG: backstop, on +-0.2 in slots (set with real balls: the 4-piece count)")

# ---- launcher
for s, n in SIDES:
    fy = s * FLY_Y
    for k, (a, b) in enumerate(((FLY_X[0], FLY_X[0] + 0.8), (FLY_X[1] - 0.8, FLY_X[1]))):
        part("launcher", f"flywheel_{n}{k}", cyl("x", (fy, FLY_Z), 2 * FLY_R, a, b), "buy", "96 mm flywheel, as the mentor's launcher (CHECK the part)")
    part("launcher", f"fly_shaft_{n}", cyl("x", (fy, FLY_Z), 8 * MM, FLY_X[0] - 1.05, FLY_X[1] + 0.3), "buy", "goBILDA 8mm REX shaft, 120 mm")
    cas = box(FLY_X[1] + 0.05, FLY_X[1] + 0.3, s * 2.1, s * 5.4, 4.7, 8.7).union(box(FLY_X[0] - 0.3, FLY_X[0] - 0.05, s * 2.1, s * 5.4, 4.7, 8.7))   # inner edges clear of a NECTAR in the column
    part("launcher", f"fly_cassette_{n}", cas.cut(cyl("x", (fy, FLY_Z), 14 * MM, FLY_X[0] - 1, FLY_X[1] + 1)), "print",
         "PETG: flywheel cassette, its two bearing plates (one per side; comes out as a unit with its wheel and motor)")
    fm = (s * FLY_MOTOR[0], FLY_MOTOR[1])
    part("launcher", f"fly_motor_{n}", motor_x(FLY_X[0] - 1.0 + PULLEY_W + 0.05 + MOTOR_SHAFT, fm, -1), "buy", "goBILDA 5203-2402-0001 Yellow Jacket, 6000 RPM")
    part("launcher", f"fly_belt_{n}", belt_x((fy, FLY_Z), fm, PULLEY24_D, FLY_X[0] - 1.0, FLY_X[0] - 1.0 + PULLEY_W), "buy", f"goBILDA 3417-4008-0024 24T HTD5 x2, 3412-0009-0{FLY_BELT} belt (1:1; the launcher rig sets the ratio)")
FX = (FLY_X[0], FLY_X[1])
part("launcher", "feeder", cyl("x", FEED[:2], 2 * FEED[2], *FX), "buy", "goBILDA 3632-0014-0072 72 mm Gecko x2, softest")
part("launcher", "feeder_shaft", cyl("x", FEED[:2], 8 * MM, FX[0] - 0.2, FX[1] + 0.9), "buy", "goBILDA 8mm REX shaft, 120 mm")
part("launcher", "feeder_motor", motor_x(FX[1] + 1.35, FEED_MOTOR, -1), "buy", "goBILDA 5203-2402-0003 Yellow Jacket, 1620 RPM, belted 1:1 (stopping it is the gate)")
part("launcher", "feeder_belt", belt_x(FEED[:2], FEED_MOTOR, PULLEY24_D, FX[1] + 0.45, FX[1] + 0.45 + PULLEY_W), "buy", f"goBILDA 3417-4008-0024 24T HTD5 x2, 3412-0009-0{FEED_BELT} belt")
part("launcher", "pad", box(*FX, -1.05 - 0.75, -1.05, 1.62, 4.4), "print", "PETG plate with 1/2 in foam, hinged at its foot (cad/transfer's pad)")
part("launcher", "top_plate", box(COL_X - 3.2, COL_X + 3.0, -4.0, 4.0, 8.7, 8.75).cut(cyl("z", (COL_X, 0), 2 * RING[3], 8, 9)), "print",
     "PETG, 6 mm... drawn 0.05 thick here: the launcher's top plate on the cassettes, carries the ring's rollers, the servo and the encoders")
ring = cyl("z", (COL_X, 0), 2 * RING[2], RING[0], RING[1]).cut(cyl("z", (COL_X, 0), 2 * RING[3], 0, 20))
part("launcher", "turret_ring", ring, "print", "PETG: turret ring with its gear cut in its rim (drawn plain); rides on four V-groove rollers")
for k in range(4):
    a = math.radians(45 + 90 * k); r = RING[2] + 0.3
    part("launcher", f"ring_roller_{k}", cyl("z", (COL_X + r * math.cos(a), r * math.sin(a)), 0.6, 8.75, 9.35), "buy", "625 or 608 bearing in a printed V-groove tyre")
hood = cyl("z", (COL_X, 0), 2 * RING[3] + 0.4, RING[1], 11.5).cut(cyl("z", (COL_X, 0), 2 * RING[3], RING[1] - 1, 12))
part("launcher", "hood", hood.union(box(COL_X, COL_X + 2.4, -RING[3] - 0.2, RING[3] + 0.2, 11.5, 12.2)), "print", "PETG: the hood that turns the shot out (drawn as a tube and a lid)")
part("launcher", "turret_servo", box(COL_X - 1.0, COL_X + 0.6, -4.0, -3.2, 8.75, 10.2), "buy", "goBILDA 2000-0025-0003 Speed servo, continuous: drives the ring by a printed pinion")
for k, y in enumerate((3.25, 3.25)):
    part("launcher", f"turret_encoder_{k}", box(COL_X - 1.2 + 1.4 * k, COL_X - 0.2 + 1.4 * k, 3.25, 4.0, 8.75, 9.35), "buy", "REV-11-1271 Thru-Bore encoder on a printed pinion: the two-encoder decode, ratios chosen in v2")

# ---- electronics
part("elec", "tray", box(*TRAY_X, -TRAY_HALF, TRAY_HALF, TRAY_Z - 0.2, TRAY_Z), "print", "PETG: electronics tray on the front cross channel and the launcher's top plate")
part("elec", "control_hub", box(TRAY_X[0] + 0.05, TRAY_X[0] + 0.05 + HUB[0], -HUB[1] - 0.1, -0.1, TRAY_Z, TRAY_Z + HUB[2]), "buy", "REV Control Hub")
part("elec", "expansion_hub", box(TRAY_X[0] + 0.05, TRAY_X[0] + 0.05 + HUB[0], -HUB[1] - 0.1, -0.1, TRAY_Z + HUB[2] + 0.1, TRAY_Z + 2 * HUB[2] + 0.1), "buy", "REV Expansion Hub")
part("elec", "battery", box(TRAY_X[0] + 0.05, TRAY_X[0] + 0.05 + BATTERY[0], 0.3, 0.3 + BATTERY[1], TRAY_Z, TRAY_Z + BATTERY[2]), "buy", "12 V battery, as today's (CHECK)")
part("elec", "limelight_mast", box(6.75, 7.15, -0.5, 0.5, TRAY_Z, 13.4), "print", "PETG: Limelight mast; lens about 14 in up, 45 deg up, as today")
part("elec", "limelight", box(6.1, 7.2, -1.1, 1.1, 13.4, 14.8), "buy", "Limelight 3A")

# ================================================================ checks
def solid(w): return w.val() if hasattr(w, "val") else w

def bb(w):
    b = solid(w).BoundingBox(); return (b.xmin, b.xmax, b.ymin, b.ymax, b.zmin, b.zmax)

def overlap(a, b, tol=1e-4):
    A, B = bb(a), bb(b)
    if any(A[2 * i] > B[2 * i + 1] - tol or B[2 * i] > A[2 * i + 1] - tol for i in range(3)): return 0.0
    try: return solid(a).intersect(solid(b)).Volume()
    except Exception: return -1.0

# parts that touch by design (a shaft in its part, a belt round its motor's shaft, a stack)
ALLOWED = [("wheel_shaft_", "wheel_"), ("wheel_shaft_", "pod_"), ("drive_belt_", "drive_motor_"), ("drive_belt_", "wheel_shaft_"),
           ("roller_shaft", "roller"), ("roller_shaft", "intake_arm_"), ("roller_belt", "intake_motor"), ("roller_belt", "roller_shaft"),
           ("lane_shaft_", "lane_roller_"), ("lane_shaft_", "lane_wall_"), ("fly_shaft_", "flywheel_"), ("fly_shaft_", "fly_cassette_"),
           ("fly_belt_", "fly_shaft_"), ("fly_belt_", "fly_motor_"), ("feeder_shaft", "feeder"), ("feeder_belt", "feeder_shaft"),
           ("feeder_belt", "feeder_motor"), ("ex_stub_", "flap_"), ("ex_stub_", "ex_arm_"), ("ex_cross", "ex_arm_"), ("ex_cross", "ex_block"),
           ("ring_roller_", "turret_ring"), ("ring_roller_", "top_plate"), ("pod_F", "flap_"), ("intake_motor", "intake_arm_R"),
           ("drive_motor_", "pod_"), ("ex_servo", "flap_R"), ("flap_", "pod_F"), ("intake_pivot_", "rail_"), ("front_upright_", "rail_"), ("rear_cross", "rail_"),
           ("pod_", "rail_"), ("front_cross", "front_upright_"), ("tray", "front_cross"), ("limelight", "limelight_mast")]
def allowed(a, b):
    return any((a.startswith(p) and b.startswith(q)) or (b.startswith(p) and a.startswith(q)) for p, q in ALLOWED)

def clashes(shapes, only=None):
    names = sorted(shapes); out = []
    for i, a in enumerate(names):
        for b in names[i + 1:]:
            if only and not (only(a) or only(b)): continue
            if only and only(a) and only(b): continue
            if allowed(a, b): continue
            v = overlap(shapes[a], shapes[b])
            if v > 1e-4 or v < 0: out.append((a, b, v))
    return out

def report():
    ok = True
    shapes = {k: p["shape"] for k, p in PARTS.items()}
    print("== static clashes (start pose: extractor stowed, roller down)")
    c = clashes(shapes)
    for a, b, v in c: print(f"   CLASH {a} x {b}: {v:.4f} in^3"); ok = False
    if not c: print("   none")

    print("== the roller rising 0 ..", FLOAT, "in (swing arms)")
    still = {k: v for k, v in shapes.items() if PARTS[k]["moves"] != "intake"}
    for lift in (0.0, 0.33, 0.65, 1.0, FLOAT):
        mv = {k: w for k, (w, _, _) in intake(lift).items()}
        c = clashes({**still, **mv}, only=lambda n: n in mv)
        ax = rot_y(cyl("y", ROLL, 0.01, 0, 0.01), PIVOT, lift_deg(lift)); b = bb(ax)
        print(f"   lift {lift:.2f}: roller axle at X {b[0]:.3f} z {b[4]:.3f}, " + (", ".join(f"CLASH {a} x {b_}" for a, b_, _ in c) or "clear"))
        ok &= not c

    print("== the extractor, every 10 deg from down to stowed, roller down and up")
    for lift in (0.0, FLOAT):
        mv_i = {k: w for k, (w, _, _) in intake(lift).items()}
        base = {k: v for k, v in shapes.items() if PARTS[k]["moves"] not in ("extractor", "intake")}
        bad = []
        for deg in list(range(0, int(EX_STOW), 10)) + [EX_STOW]:
            mv = {k: w for k, (w, _, _) in extractor(deg).items() if not k.startswith("ex_stub")}
            for a, b, v in clashes({**base, **mv_i, **mv}, only=lambda n: n in mv): bad.append((deg, a, b))
        print(f"   roller up {lift:.2f}: " + ("clear" if not bad else "; ".join(f"{d} deg {a} x {b}" for d, a, b in bad[:8])))
        ok &= not bad

    print("== pieces on their path (a NECTAR and a POLLEN at each step; only what's meant to touch them may)")
    TOUCH = ("lane_roller_", "ceiling", "feeder", "pad", "flywheel_", "roller", "ramp", "floor", "backstop", "hood")   # the hood's lid turns the shot
    path = [(x, LANE_BOTTOM) for x in (BACKSTOP_X + 1.82, 0.0, 2.0, 4.0, 5.5, 6.5)] + [(COL_X, z) for z in (5.0, 6.65, 8.0, 9.0, 10.5)]
    bad = []
    for r in (RN, RP):
        for x, z0 in path:
            ball = cq.Workplane("XY").sphere(r).translate((x, 0.15 if z0 > LANE_BOTTOM else 0, z0 + (r if z0 == LANE_BOTTOM else 0)))
            for k, w in shapes.items():
                if k.startswith(TOUCH) or k.startswith(("lane_shaft_",)): continue
                if overlap(ball, w) > 1e-4: bad.append(f"{'NECTAR' if r == RN else 'POLLEN'} at X {x:.2f} z {z0:.2f} x {k}")
    print("   " + ("; ".join(sorted(set(bad))) if bad else "clear: lane, column, ring bore and hood"))
    ok &= not bad

    print("== outline")
    allb = [bb(w) for w in shapes.values()]
    x0, x1 = min(b[0] for b in allb), max(b[1] for b in allb)
    y0, y1 = min(b[2] for b in allb), max(b[3] for b in allb)
    z1 = max(b[5] for b in allb)
    print(f"   start: X {x0:.2f}..{x1:.2f} ({x1 - x0:.2f} / {START_CUBE}), Y {y0:.2f}..{y1:.2f} ({y1 - y0:.2f} / {START_CUBE}), height {z1:.2f} / {START_CUBE}")
    ok &= x1 - x0 <= START_CUBE + 1e-6 and y1 - y0 <= START_CUBE + 1e-6 and z1 <= START_CUBE
    dep = [bb(w) for k, (w, _, _) in extractor(0).items()]
    dx1 = max(x1, max(b[1] for b in dep))
    print(f"   extractor down: front to back {dx1 - x0:.2f} / 24")
    ok &= dx1 - x0 <= 24
    behind = [k for k, b in zip(shapes, allb) if b[3] > HALF_W + 1e-3 and b[0] < FACE - 1e-3 or b[2] < -HALF_W - 1e-3 and b[0] < FACE - 1e-3]
    print("   wider than 15.24 behind the face: " + (", ".join(behind) or "nothing"))

    print("== the 4-piece count (roller axle to backstop)")
    gap = ROLL[0] - BACKSTOP_X
    print(f"   {gap:.2f} in: 3 NECTAR + 1 POLLEN need {COUNT_WINDOW[0]}, 5 POLLEN {COUNT_WINDOW[1]}: " + ("inside the window" if COUNT_WINDOW[0] < gap < COUNT_WINDOW[1] else "OUTSIDE"))
    ok &= COUNT_WINDOW[0] < gap < COUNT_WINDOW[1]

    print("== printed parts on a", " x ".join(f"{v:.0f}" for v in BED), "mm bed")
    for k, p in PARTS.items():
        if p["kind"] != "print": continue
        b = bb(p["shape"]); d = sorted((b[1] - b[0], b[3] - b[2], b[5] - b[4]))
        dm = [v * 25.4 for v in d]
        fits = all(a <= c for a, c in zip(dm, sorted(BED)))
        if not fits: print(f"   TOO BIG {k}: {dm[0]:.0f} x {dm[1]:.0f} x {dm[2]:.0f} mm"); ok = False
    big = max((k for k, p in PARTS.items() if p["kind"] == "print"), key=lambda k: max(bb(PARTS[k]["shape"])[2 * i + 1] - bb(PARTS[k]["shape"])[2 * i] for i in range(3)))
    b = bb(PARTS[big]["shape"]); print(f"   all fit; the largest is {big}: " + " x ".join(f"{(b[2 * i + 1] - b[2 * i]) * 25.4:.0f}" for i in range(3)) + " mm")
    return ok

def parts_md():
    rows = ["| Part | Module | Make | What |", "|---|---|---|---|"]
    for k, p in sorted(PARTS.items(), key=lambda kv: (kv[1]["module"], kv[0])):
        rows.append(f"| `{k}` | {p['module']} | {dict(print='print', buy='buy', cut='cut')[p['kind']]} | {p['what']} |")
    return "\n".join(rows) + "\n"

def views(out_dir):
    """Side, top, front and a three-quarter view, painted from the tessellated parts."""
    import numpy as np
    import matplotlib; matplotlib.use("Agg"); import matplotlib.pyplot as plt
    from matplotlib.collections import PolyCollection
    COL = {"print": (0.20, 0.50, 0.90), "buy": (0.55, 0.56, 0.60), "cut": (0.80, 0.80, 0.80)}
    tris = []
    for k, p in PARTS.items():
        v, f = solid(p["shape"]).tessellate(0.02, 0.5)
        v = np.array([(q.x, q.y, q.z) for q in v]); c = COL[p["kind"]]
        if k.startswith(("flap_foam", "pad")): c = (0.95, 0.72, 0.20)
        if k.startswith(("wheel_", "roller", "flywheel_", "feeder")) and p["kind"] == "buy": c = (0.25, 0.25, 0.28)
        for t in f: tris.append((v[list(t)], c))
    def paint(ax, R, title):
        polys, keys, cols = [], [], []
        L = np.array([0.3, -0.5, 0.8]); L = L / np.linalg.norm(L)
        for t, c in tris:
            q = t @ R.T
            n = np.cross(t[1] - t[0], t[2] - t[0]); nn = np.linalg.norm(n)
            shade = 0.55 + 0.45 * abs(n @ L) / nn if nn > 0 else 0.8
            polys.append(q[:, :2]); keys.append(q[:, 2].mean()); cols.append(tuple(min(1, ch * shade) for ch in c))
        o = np.argsort(keys)
        ax.add_collection(PolyCollection([polys[i] for i in o], facecolors=[cols[i] for i in o], edgecolors="none"))
        ax.set_aspect("equal"); ax.autoscale_view(); ax.set_title(title, fontsize=9); ax.grid(lw=0.3, alpha=0.4)
    def rot(az, el):
        a, e = math.radians(az), math.radians(el)
        Rz = np.array([[math.cos(a), -math.sin(a), 0], [math.sin(a), math.cos(a), 0], [0, 0, 1]])
        Rx = np.array([[1, 0, 0], [0, math.cos(e), -math.sin(e)], [0, math.sin(e), math.cos(e)]])
        return Rx @ Rz
    side = np.array([[1, 0, 0], [0, 0, 1], [0, -1, 0]])        # looking at the left side: X right, Z up, nearer = +Y
    top = np.array([[1, 0, 0], [0, 1, 0], [0, 0, 1]])
    front = np.array([[0, -1, 0], [0, 0, 1], [1, 0, 0]])
    iso = np.array([[0, 1, 0], [0, 0, 1], [1, 0, 0]]) @ rot(-35, 0)
    iso = rot(0, 0)
    a, e = math.radians(-50), math.radians(25)
    cam = np.array([math.cos(e) * math.cos(a), math.cos(e) * math.sin(a), math.sin(e)])   # toward the viewer
    rt = np.cross([0, 0, 1], cam); rt /= np.linalg.norm(rt); up = np.cross(cam, rt)
    iso = np.array([rt, up, cam])
    os.makedirs(out_dir, exist_ok=True)
    for name, R, title in (("side", side, "From the left (inches; +X forward)"), ("top", top, "From above"),
                           ("front", front, "From the front"), ("three-quarter", iso, "Front right, from above")):
        fig, ax = plt.subplots(figsize=(8, 6), dpi=130); paint(ax, R, title)
        fig.tight_layout(); fig.savefig(os.path.join(out_dir, f"{name}.png")); plt.close(fig)

if __name__ == "__main__":
    ok = report()
    print("ALL CHECKS PASS" if ok else "CHECKS FAIL")
    if CHECK_ONLY: sys.exit(0 if ok else 1)
    os.makedirs(os.path.join(HERE, "stl"), exist_ok=True)
    asm = cq.Assembly(name="printed_chassis")
    for k, p in PARTS.items():
        s = solid(p["shape"]).scale(25.4)
        col = {"print": cq.Color(0.15, 0.45, 0.85), "buy": cq.Color(0.55, 0.55, 0.58), "cut": cq.Color(0.8, 0.8, 0.8)}[p["kind"]]
        if k.startswith(("flap_foam", "pad")): col = cq.Color(0.95, 0.75, 0.2)
        asm.add(s, name=k, color=col)
        if p["kind"] == "print": cq.exporters.export(cq.Workplane().add(s), os.path.join(HERE, "stl", f"{k}.stl"))
    asm.save(os.path.join(HERE, "printed-chassis.step"))
    with open(os.path.join(HERE, "parts.md"), "w") as f:
        f.write("# Parts, layout v1 (written by build.py)\n\nScrews, nuts and inserts come with v2. CHECK marks a size not read from a vendor file.\n\n" + parts_md())
    views(os.path.join(HERE, "views"))
    print("wrote printed-chassis.step, stl/, parts.md, views/")
