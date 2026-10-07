"""The intake-to-launcher transfer, v3 (7 Oct 2026): a ramp, a flat lane of small compliant rollers under a flat sprung
ceiling, and a pair of side feeders under the flywheels that pinch each ball and drive it straight up into them.
Drawn in the robot CAD's frame, so dhs-transfer.step lands in place in Onshape next to cad/intake-b/dhs-intake-b.step.

    python3 cad/transfer/build.py        # writes dhs-transfer.step and stl/ next to this file

Robot frame: +X forward, +Y left, +Z up, inches, origin on the floor under the chassis centre (7.56 in behind the front
face), moved into the CAD's millimetres at the end. Groups: "fixed" (ramp, lane, ceiling, feeders and their drives) and
"launcher" (the changes the feeders need in the mentor's launcher: the flywheel motors moved out and up, its front
channels extended down to carry the feeder shafts).

How it works: the intake roller pushes each ball up the ramp onto the lane. The lane's rollers carry it back under the
ceiling, which presses it onto them, and push it in between the two feeders, which sit under the flywheels and grip it
by its sides. Stopped, the feeders hold it there, below the flywheels' reach; the next ball waits behind it. Feeding
runs the feeders up: they drive the ball, gripped, straight up into the flywheels.
"""
import math, os, sys
import cadquery as cq
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "robot-addons"))
from build import C, F, FACE, IN

CENTRE_BACK_IN = 7.56
T16, TW = 1 / 16, 1 / 8
LANE_Y = 0.0                            # the lane's and the feeders' centreline
WALL_IN = 1.87                          # the lane walls' inner faces, |Y|: a NECTAR clears the drive motors' encoder caps by 0.06
RN, RP = 1.81, 1.40                     # NECTAR, POLLEN radii
LAUNCHER_SHIFT = 0.8                    # the mentor's launcher and turret move forward this much (cad/full-robot applies it), so the
                                        # lane's length holds at most 4 pieces, at most 3 of them NECTAR (see BACKSTOP_X)
SH = LAUNCHER_SHIFT
COL_X = -2.845 + SH                     # the launch column: under the flywheels' centre and the turret's axis
# the feeders: goBILDA 72 mm Gecko wheels, two side by side, on shafts along X, under the flywheels (96 mm ones would
# reach the drive rails)
RF, FEED_W = 36 / 25.4, 1.89
FEED_X = (-3.79 + SH, -1.90 + SH)       # the flywheels' X span
# One driven feeder on the left, fixed; opposite it a sprung foam pad on a bottom hinge. The pad takes the size
# difference: it rests where a POLLEN presses its foam 0.2 (and the feeder's tread 0.1), and a NECTAR pushes it back
# 0.77. Low band preload, so both sizes enter the stopped feeder under the lane's push.
FEED_IN = 1.45                          # the feeder's tread, inner edge, Y: a POLLEN presses 0.10 into it and centres at Y 0.15; a NECTAR presses 0.15, centres at -0.21
FEED_Y = FEED_IN + RF                   # its axle's Y (2.87)
FEED_Z = 3.25                           # its axle: as high as it goes and stay 0.17 clear of the left flywheel (their axles 0.77 apart in Y)
FLOOR_Z = 1.3                           # the lane's ball-bottom height: a POLLEN's centre (2.70) and a NECTAR's (3.11) are in the feeder's grip
PAD_FACE = -1.05                        # the pad's foam face at rest, Y; 0.5 in of foam on 1/8 in aluminium
PAD_FOAM = 0.5
PAD_HINGE = (-1.75, 1.45)               # (Y, z) of its hinge, along X, under the plate
PAD_TOP = 4.55
GRIP_TOP_P = FEED_Z + math.sqrt((RF + RP) ** 2 - (FEED_Y - 0.15) ** 2)    # the feeder grips a POLLEN up to here (centre)
GRIP_TOP_N = FEED_Z + math.sqrt((RF + RN) ** 2 - (FEED_Y + 0.21) ** 2)    # and a NECTAR; the flywheels take a NECTAR from 5.77
# The count, by length. The lead ball's back rests on the backstop; the queue runs nose to tail to the intake roller's
# axle (X 8.62), and a ball is held once its centre is behind it. Every legal load must fit (the longest, 3 NECTAR +
# 1 POLLEN, needs 12.26 in from the backstop to the axle) and every illegal one must not (the shortest, 5 POLLEN,
# needs 12.60; 4 NECTAR 12.67). The backstop sits mid-window, 12.43 in behind the axle, on +-0.2 in slots to tune it.
ROLLER_AXLE_X = 7.56 + 0.061 + 1.0      # the roller's 2 in vector wheels, their back 0.06 in clear of the face
BACKSTOP_X = ROLLER_AXLE_X - 12.43      # -3.81: a NECTAR in the feeders sits 0.06 in ahead of the column's centre (X -2.04)
RAMP = ((8.0, 0.05), (5.7, FLOOR_Z))
R_R = 12 / 25.4                         # lane rollers: 24 mm compliant, tops at FLOOR_Z
ROLL_X = [5.3 - 0.8 * i for i in range(8)]    # eight shafts, 0.8 in apart, the last just ahead of the feeders. As in the mentor's
ROLL_W = 1.0                            # lane, each shaft's wheels sit to one side, alternating, so neighbours overlap and leave pockets
ROLL_Z = FLOOR_Z - R_R
CEIL_GAP, FOAM = 2.6, 0.5               # the ceiling's foam face 2.6 over the lane: a POLLEN presses the foam 0.2; a NECTAR lifts it 0.82
CEIL_X = (5.3, -0.95 + SH)              # ends clear of a ball going up the column
CEIL_PINS = (5.35, 1.35)                # the ceiling rides on two pins a side in vertical slots in posts on the walls' top edges
CEIL_TRAVEL = 0.85                      # slot length: a NECTAR lifts it 0.82
CHAN_RAISE = 30 / 25.4                  # the old intake's 11-hole channel goes up 30 mm: a lane NECTAR (top 4.92), the lifted ceiling (5.37) and its front links (5.49) pass under it; its top (7.50) stays under the L-beam above (7.58)
RAIL_IN = 4.88
LM = (4.75, 1.0)                        # the lane drive's jackshaft (right side), and its motor's axis Y
LM_Y = -3.3
MOTOR_D, MOTOR_L = 37 / 25.4, 100 / 25.4
FLY_Y = (3.6427, -3.3276)               # the flywheels' axles (mentor's), z 6.646
FLY_Z = 6.646
FM_Y, FM_Z = 6.7, 7.0                   # the flywheel motors, moved out and up (along X), belted to his 41T pulleys
FDM_Y, FDM_Z = 6.7, 5.3                 # the feeder motor, outboard on the left under the flywheel motor, belted straight to the feeder
BELT_X = 0.95 + SH * 0                  # the feeder motors' belt plane, ahead of the launcher frame

fixed, flt, arm = {}, {}, {}
def part(d, name, wp, col, kind): d[name] = (wp, col, kind)
BLUE, ALU, STEEL, POLY, BLACK, GREEN, RED = (0.18, 0.37, 0.62), (0.75, 0.78, 0.82), (0.8, 0.82, 0.85), (0.6, 0.78, 0.96), (0.13, 0.15, 0.17), (0.35, 0.66, 0.31), (0.75, 0.2, 0.2)

# ---- helpers, all in robot-frame inches (X fwd, Y left, Z up) ----
def bx(x0, x1, y0, y1, z0, z1):
    return cq.Workplane("XY").box(abs(x1 - x0), abs(y1 - y0), abs(z1 - z0)).translate(((x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2))
def cyly(x, z, d, y0, y1):
    """A cylinder along Y."""
    y0, y1 = min(y0, y1), max(y0, y1)
    return cq.Workplane("XZ").center(x, z).circle(d / 2).extrude(-(y1 - y0)).translate((0, y0, 0))
def xz(pts, y0, y1):
    """A profile drawn in X-Z, extruded across Y from y0 to y1."""
    y0, y1 = min(y0, y1), max(y0, y1)
    return cq.Workplane("XZ").polyline(pts).close().extrude(-(y1 - y0)).translate((0, y0, 0))
def bar(p0, p1, w, y0, y1):
    """A flat bar in X-Z between two points, rounded ends."""
    y0, y1 = min(y0, y1), max(y0, y1)
    (x0, z0), (x1, z1) = p0, p1
    L = math.hypot(x1 - x0, z1 - z0); a = math.degrees(math.atan2(z1 - z0, x1 - x0))
    b = cq.Workplane("XZ").center(L / 2, 0).rect(L, w).extrude(-(y1 - y0))
    b = b.union(cq.Workplane("XZ").circle(w / 2).extrude(-(y1 - y0))).union(cq.Workplane("XZ").center(L, 0).circle(w / 2).extrude(-(y1 - y0)))
    return b.rotate((0, 0, 0), (0, 1, 0), -a).translate((x0, y0, z0))
def cord(p0, p1, r0, r1, y, t=3 / 16):
    """A belt loop between pulleys of pitch radius r0, r1: its two straight runs."""
    (x0, z0), (x1, z1) = p0, p1; dx, dz = x1 - x0, z1 - z0; L = math.hypot(dx, dz)
    nx, nz = -dz / L, dx / L; out = None
    for s in (1, -1):
        piece = bar((x0 + s * nx * r0, z0 + s * nz * r0), (x1 + s * nx * r1, z1 + s * nz * r1), t, y - t / 2, y + t / 2)
        out = piece if out is None else out.union(piece)
    return out
def arc_slot(c, r, a0, a1, w, y0, y1, n=16):
    """An arc-shaped slot about centre c, from angle a0 to a1 (degrees, X-Z)."""
    pts = [(c[0] + (r + s * w / 2) * math.cos(math.radians(a0 + (a1 - a0) * i / n)), c[1] + (r + s * w / 2) * math.sin(math.radians(a0 + (a1 - a0) * i / n)))
           for s in (1, -1) for i in (range(n + 1) if s == 1 else range(n, -1, -1))]
    sl = xz(pts, y0, y1)
    for a in (a0, a1): sl = sl.union(cyly(c[0] + r * math.cos(math.radians(a)), c[1] + r * math.sin(math.radians(a)), w, y0, y1))
    return sl
WY = lambda s: (s * WALL_IN, s * (WALL_IN + TW))
WO = WALL_IN + TW
launcher = {}

def sheet(pts, y0, y1, t=T16):
    out = None
    for (x0, z0), (x1, z1) in zip(pts, pts[1:]):
        L = math.hypot(x1 - x0, z1 - z0); nx, nz = -(z1 - z0) / L * t, (x1 - x0) / L * t
        seg = xz([(x0, z0), (x1, z1), (x1 - nx, z1 - nz), (x0 - nx, z0 - nz)], y0, y1)
        out = seg if out is None else out.union(seg)
    return out

# ---- the ramp (1/16 in polycarbonate) and its two brackets to the side plates ----
part(fixed, "ramp (1/16 in polycarbonate)", sheet([RAMP[0], RAMP[1], (ROLL_X[0] + R_R, FLOOR_Z)], -WALL_IN, WALL_IN), POLY, "cut")
for s in (-1, 1):
    br = xz([(7.75, 0.12), (8.15, 0.12), (8.15, 0.3), (7.75, 0.3)], s * WALL_IN, s * 4.0).union(xz([(7.75, 0.12), (8.15, 0.12), (8.15, 1.6), (7.75, 1.6)], s * 4.0, s * 7.56))
    part(fixed, f"ramp_bracket_{'L' if s > 0 else 'R'} (print, to the side plate; slotted +-0.5 in in X)", br, BLUE, "print")

# ---- the lane walls: low under the front drive motors, they carry the rollers ----
def wall(s):
    y0, y1 = sorted(WY(s))
    w = xz([(FEED_X[1] + 0.3, 0.3), (6.5, 0.3), (6.5, 2.6), (FEED_X[1] + 0.3, 2.6)], y0, y1)   # ends just ahead of the feeder and the pad's hinge block
    for x in ROLL_X: w = w.cut(cyly(x, ROLL_Z, 14 / 25.4, y0 - 0.1, y1 + 0.1))
    if s < 0: w = w.cut(cyly(*LM, 14 / 25.4, y0 - 0.1, y1 + 0.1))
    return w
for s, nm in ((1, "L"), (-1, "R")): part(fixed, f"lane_wall_{nm} (1/8 in polycarbonate)", wall(s), POLY, "cut")
for x in (5.75, 0.2):
    for s in (-1, 1):
        y0, y1 = sorted((s * WO, s * RAIL_IN))
        br = bx(x - 0.25, x + 0.25, y0, y1, 1.0, 1.125).union(bx(x - 0.25, x + 0.25, s * (RAIL_IN - 0.125), s * RAIL_IN, 1.0, 1.6)).union(bx(x - 0.25, x + 0.25, s * WO, s * (WO + 0.125), 1.0, 1.6))
        part(fixed, f"wall_bracket_X{x:+.2f}_{'L' if s > 0 else 'R'} (1/8 in aluminium, wall to rail)", br, ALU, "cut")

# ---- the lane: the mentor's offset wheels, small enough for the lane's height; printed hub pulleys and one belt ----
P16 = 16 * 5 / math.pi / 25.4
for i, x in enumerate(ROLL_X):
    side = 1 if i % 2 == 0 else -1
    part(fixed, f"lane_wheels_{i} (24 mm compliant wheels, {ROLL_W} in of them on the {'left' if side > 0 else 'right'}, to choose)", cyly(x, ROLL_Z, 2 * R_R, side * 0.05, side * (0.05 + ROLL_W)), (0.25, 0.25, 0.28), "buy")
    part(fixed, f"lane_shaft_{i} (8mm REX, bearings in both walls)", cyly(x, ROLL_Z, 8 / 25.4, -WO - (0.8 if i == 0 else 0.45), WO + 0.1), STEEL, "buy")
    part(fixed, f"lane_pulley_{i} (print, 12 mm hub pulley, outside the right wall)", cyly(x, ROLL_Z, 12 / 25.4, -WO - 0.4, -WO - 0.1), BLUE, "print")
part(fixed, "lane_belt (3/16 in polycord over the hub pulleys and the jackshaft, as the mentor's lane)", cord((ROLL_X[0], ROLL_Z), (ROLL_X[-1], ROLL_Z), 6 / 25.4, 6 / 25.4, -WO - 0.25), RED, "buy")
# the lane motor lies along X outside the right wall; a polycord loop with a quarter twist runs from its pulley to the
# first lane shaft's second pulley (no gears)
LMZ = LM[1] + 0.3
part(fixed, "lane_motor (goBILDA 5203-2402-0005, 1150 RPM; along X outside the right wall)",
     cq.Workplane("YZ").center(LM_Y, LMZ).circle(MOTOR_D / 2).extrude(MOTOR_L).translate((LM[0] - 0.2 - MOTOR_L, 0, 0)), BLACK, "buy")
part(fixed, "lane_motor_pulley (print, 16 mm V-groove)", cq.Workplane("YZ").center(LM_Y, LMZ).circle(8 / 25.4).extrude(0.2).translate((LM[0] - 0.2, 0, 0)), BLUE, "print")
part(fixed, "lane_drive_pulley (print, 16 mm V-groove, on lane shaft 0 beside its hub pulley)", cyly(ROLL_X[0], ROLL_Z, 16 / 25.4, -WO - 0.75, -WO - 0.55), BLUE, "print")
part(fixed, "lane_drive_cord (3/16 in polycord, quarter-twist loop, motor to lane shaft 0)", bar((LM[0] - 0.1, LMZ), (ROLL_X[0], ROLL_Z), 3 / 16, LM_Y + 0.0, -WO - 0.65), RED, "buy")

# ---- the flat ceiling, foam-faced, on pins in vertical slots: lifts evenly, 0.82 for a NECTAR anywhere; unhook the bands
#      and it lifts off ----
cz = FLOOR_Z + CEIL_GAP
part(fixed, f"ceiling_foam ({FOAM} in soft polyethylene or EVA foam, 2-3 lb/ft3)", bx(CEIL_X[1], CEIL_X[0], -WALL_IN + 0.15, WALL_IN - 0.15, cz, cz + FOAM), (0.3, 0.3, 0.32), "buy")
ceil = bx(CEIL_X[1], CEIL_X[0], -WALL_IN + 0.05, WALL_IN - 0.05, cz + FOAM, cz + FOAM + T16)
top = cz + FOAM + T16
for tx in CEIL_PINS: ceil = ceil.union(bx(tx - 0.2, tx + 0.2, -WO, WO, top, top + 0.125))
part(fixed, "ceiling (1/16 in polycarbonate with four pin tabs; band-held down)", ceil, POLY, "cut")
for s in (-1, 1):
    y0, y1 = sorted((s * WO, s * (WO + 0.25)))
    for i, tx in enumerate(CEIL_PINS):
        post = bx(tx - 0.25, tx + 0.25, y0, y1, 2.6, top + CEIL_TRAVEL + 0.15)   # the front ones stay under the raised 11-hole channel
        post = post.cut(bx(tx - 0.07, tx + 0.07, -3, 3, top + 0.06 - 0.07, top + 0.06 + CEIL_TRAVEL + 0.07))
        part(fixed, f"ceiling_post_{'front' if i == 0 else 'rear'}_{'L' if s > 0 else 'R'} (print, on the wall's top edge; vertical slot for the ceiling's pin, a hook for its band)", post, BLUE, "print")
        part(fixed, f"ceiling_pin_{'front' if i == 0 else 'rear'}_{'L' if s > 0 else 'R'} (M3 shoulder screw through the ceiling's tab)", cyly(tx, top + 0.06, 0.12, -WO - 0.3 if s < 0 else WO - 0.05, WO + 0.3 if s > 0 else -WO + 0.05), STEEL, "buy")

# ---- the feeder: one driven, fixed, on the left; a sprung foam pad on the right ----
# A bolt-on module each: the feeder (wheels, shaft, two bearings) drops out of the launcher's front and rear channels
# with its belt off; the pad (plate, foam, hinge, band) comes off its two hinge blocks.
y = LANE_Y + FEED_Y
part(fixed, "feeder (goBILDA 72 mm Gecko x2, softest durometer)", cq.Workplane("YZ").center(y, FEED_Z).circle(RF).extrude(FEED_W).translate((FEED_X[0], 0, 0)), (0.35, 0.66, 0.31), "buy")
part(fixed, "feeder_shaft (8mm REX, bearings in the launcher's front and rear channels; its pulley ahead of the front one)",
     cq.Workplane("YZ").center(y, FEED_Z).circle(4 / 25.4).extrude(BELT_X + 0.4 - (-4.45 + SH)).translate((-4.45 + SH, 0, 0)), STEEL, "buy")
part(fixed, "feeder_motor (goBILDA 5203-2402-0005, 1150 RPM; along X outboard on the left, under the flywheel motor)",
     cq.Workplane("YZ").center(FDM_Y, FDM_Z).circle(MOTOR_D / 2).extrude(MOTOR_L).translate((BELT_X - 0.2 - MOTOR_L, 0, 0)), BLACK, "buy")
part(fixed, "feeder_motor_bracket (1/8 in aluminium, on the launcher frame's left side channel, ahead of the flywheel motor's)", bx(BELT_X - 1.1, BELT_X - 0.2, 5.35, 5.83, 4.4, 4.525).union(bx(BELT_X - 1.1, BELT_X - 0.2, 5.83, 5.955, 4.4, FDM_Z + 0.5)), ALU, "cut")
for cy_, cz_, nmp in ((y, FEED_Z, "feeder"), (FDM_Y, FDM_Z, "motor")):
    part(fixed, f"feeder_pulley_{nmp} (16T HTD5)", cq.Workplane("YZ").center(cy_, cz_).circle(P16 / 2).extrude(0.35).translate((BELT_X, 0, 0)), BLACK, "buy")
def belt_yz(p0, p1, r0, r1, x, w=0.35):
    (y0_, z0_), (y1_, z1_) = p0, p1
    L = math.hypot(y1_ - y0_, z1_ - z0_); ny, nz = -(z1_ - z0_) / L, (y1_ - y0_) / L; runs = None
    for sg in (1, -1):
        a0 = (y0_ + sg * ny * r0, z0_ + sg * nz * r0); a1 = (y1_ + sg * ny * r1, z1_ + sg * nz * r1)
        ln = math.hypot(a1[0] - a0[0], a1[1] - a0[1]); ang = math.degrees(math.atan2(a1[1] - a0[1], a1[0] - a0[0]))
        r = cq.Workplane("YZ").rect(ln, 0.09).extrude(w).rotate((0, 0, 0), (1, 0, 0), ang).translate((x, (a0[0] + a1[0]) / 2, (a0[1] + a1[1]) / 2))
        runs = r if runs is None else runs.union(r)
    return runs
part(fixed, "feeder_belt (HTD5 9 mm, motor to feeder; fixed centres)", belt_yz((y, FEED_Z), (FDM_Y, FDM_Z), P16 / 2, P16 / 2, BELT_X), BLACK, "buy")
# the pad: 1/8 in aluminium plate, foam on its inner face, hinged along X at its bottom, banded inward against a stop
px0, px1 = FEED_X[0] + 0.05, FEED_X[1] - 0.05
part(fixed, f"pad_foam ({PAD_FOAM} in soft foam, as the ceiling's)", bx(px0, px1, PAD_FACE - PAD_FOAM, PAD_FACE, PAD_HINGE[1] + 0.4, PAD_TOP), (0.3, 0.3, 0.32), "buy")
pad = bx(px0, px1, PAD_FACE - PAD_FOAM - 0.125, PAD_FACE - PAD_FOAM, PAD_HINGE[1] - 0.15, PAD_TOP)
part(fixed, "pad_plate (1/8 in aluminium; hinged at its bottom, a band at its top pulls it in)", pad, ALU, "cut")
part(fixed, "pad_hinge (8mm REX rod through two printed blocks on the feeder floor's hangers)", cq.Workplane("YZ").center(*PAD_HINGE).circle(4 / 25.4).extrude(px1 - px0 + 0.6).translate((px0 - 0.3, 0, 0)), STEEL, "buy")
for hx in (px0 - 0.3, px1 + 0.05):
    part(fixed, f"pad_hinge_block_{'rear' if hx < px0 else 'front'} (print)", bx(hx, hx + 0.25, PAD_HINGE[0] - 0.3, PAD_HINGE[0] + 0.3, PAD_HINGE[1] - 0.3, PAD_HINGE[1] + 0.3), BLUE, "print")
part(fixed, "pad_stop (print, beside the front hinge block, under the lane: a tab on the pad's foot rests on it; +-0.1 in slots set the POLLEN squeeze)", bx(px1 + 0.05, px1 + 0.3, PAD_HINGE[0] + 0.05, PAD_HINGE[0] + 0.35, FLOOR_Z - 0.45, FLOOR_Z - 0.15), BLUE, "print")

# under the ball between the feeders: a short floor, and a backstop that sets it on the column
fl = bx(BACKSTOP_X - 0.15, FEED_X[1] + 0.15, -1.3, 1.3, FLOOR_Z - T16, FLOOR_Z)
part(fixed, "feeder_floor (1/16 in polycarbonate, between the feeders)", fl, POLY, "cut")
part(fixed, "backstop (1/8 in polycarbonate, on +-0.2 in slots: sets the 4-piece / 3-NECTAR limit; tune it on the robot)", bx(BACKSTOP_X - 0.125, BACKSTOP_X, -1.3, 1.3, FLOOR_Z, 3.6), POLY, "cut")
for s in (-1, 1):
    part(fixed, f"feeder_floor_hanger_{'L' if s > 0 else 'R'} (print, floor to the launcher's rear channel; the front edge bolts to the lane walls)", bx(-4.85 + SH, -4.62 + SH, s * 1.3, s * 2.0, FLOOR_Z - 0.2, 1.95), BLUE, "print")

# ---- the launcher changes: its front channels reach down to the feeder shafts, the flywheel motors move out and up ----
part(launcher, "launcher_front_channel_L (goBILDA 5-hole lowside U-channel, replacing the 3-hole: carries the feeder shaft's front bearing)", bx(-1.58 + SH, -1.10 + SH, 2.43, 4.67, 1.88, 3.76), ALU, "buy")
P41 = 41 * 5 / math.pi / 25.4
for s, fy in ((1, FLY_Y[0]), (-1, FLY_Y[1])):
    my = s * FM_Y
    part(launcher, f"flywheel_motor_{'L' if s > 0 else 'R'} (the mentor's 312 RPM Yellow Jacket, moved: along X outboard of the launcher frame)",
         cq.Workplane("YZ").center(my, FM_Z).circle(MOTOR_D / 2).extrude(MOTOR_L).translate((-3.8 + SH, 0, 0)), (0.95, 0.75, 0.2), "buy")
    part(launcher, f"flywheel_motor_pulley_{'L' if s > 0 else 'R'} (16T HTD5)", cq.Workplane("YZ").center(my, FM_Z).circle(P16 / 2).extrude(0.35).translate((-4.2 + SH, 0, 0)), BLACK, "buy")
    (y0_, z0_), (y1_, z1_) = (fy, FLY_Z), (my, FM_Z)
    L = math.hypot(y1_ - y0_, z1_ - z0_); ny, nz = -(z1_ - z0_) / L, (y1_ - y0_) / L
    runs = None
    for sg in (1, -1):
        a0 = (y0_ + sg * ny * P41 / 2, z0_ + sg * nz * P41 / 2); a1 = (y1_ + sg * ny * P16 / 2, z1_ + sg * nz * P16 / 2)
        ln = math.hypot(a1[0] - a0[0], a1[1] - a0[1]); ang = math.degrees(math.atan2(a1[1] - a0[1], a1[0] - a0[0]))
        r = cq.Workplane("YZ").rect(ln, 0.09).extrude(0.35).rotate((0, 0, 0), (1, 0, 0), ang).translate((-4.2 + SH, (a0[0] + a1[0]) / 2, (a0[1] + a1[1]) / 2))
        runs = r if runs is None else runs.union(r)
    part(launcher, f"flywheel_belt_{'L' if s > 0 else 'R'} (HTD5 9 mm, his 41T to the moved motor)", runs, BLACK, "buy")
    part(launcher, f"flywheel_motor_bracket_{'L' if s > 0 else 'R'} (1/8 in aluminium, on the launcher frame's side channel)", bx(-3.0 + SH, -1.0 + SH, s * 5.35, s * 5.83, 5.74, 5.865).union(bx(-3.0 + SH, -1.0 + SH, s * 5.83, s * 5.955, 5.74, FM_Z + 0.75)), ALU, "cut")

# ---- into the robot CAD's millimetres: (x, y, z)_CAD = (C + Y, F + Z, FACE - 7.56 in + X), all times 25.4 ----
MAT = cq.Matrix([[0, IN, 0, C], [0, 0, IN, F], [IN, 0, 0, FACE - CENTRE_BACK_IN * IN]])
def to_cad(wp):
    shp = wp.val() if len(wp.vals()) == 1 else cq.Compound.makeCompound(wp.vals())
    return shp.transformGeometry(MAT)
GROUPS = (("transfer, fixed", "fixed", fixed), ("launcher changes (the mentor's to agree)", "launcher", launcher))
if __name__ == "__main__":
    out = os.path.dirname(os.path.abspath(__file__)); os.makedirs(out + "/stl", exist_ok=True)
    assy = cq.Assembly(name="DHS transfer"); mesh = {}
    for title, grp, d_ in GROUPS:
        sub = cq.Assembly(name=title)
        for nme, (wp, col, kind) in d_.items():
            shp = to_cad(wp); sub.add(shp, name=nme, color=cq.Color(*col))
            v, f_ = shp.tessellate(0.2, 0.3)
            mesh[nme] = dict(grp=grp, col=col, kind=kind, v=[(p.x, p.y, p.z) for p in v], f=f_)
            if kind == "print": shp.exportStl(f"{out}/stl/{nme.split(' ')[0]}.stl", 0.05, 0.2)
        assy.add(sub)
    assy.save(out + "/dhs-transfer.step")
    if os.environ.get("MESH_OUT"):
        import pickle; pickle.dump(mesh, open(os.environ["MESH_OUT"], "wb"))
    print(len(fixed), "fixed,", len(launcher), "launcher parts")
