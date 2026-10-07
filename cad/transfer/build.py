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
FEED_SQ_P = 0.10                        # a POLLEN squeezed this much each side, the feeders on their stops; a NECTAR swings each out 0.41
ARM_L = 16 / 25.4                       # each feeder hangs on two short arms from a pivot straight above its axis (a 20T-20T mod 0.8 gear pair's centres): it swings out
SWING = (RN - RP) / 1.0                 # sideways (0.41 in each; the arc rises 0.15) against a band, so both sizes enter the stopped feeders
PIVOT_Z = None                          # set below
FEED_Y = RF + RP - FEED_SQ_P            # each feeder's axis this far either side of the centreline (2.72)
FEED_Z = 4.78 - 0.26 - RF               # their tops 0.26 under the flywheels' bottoms (axes at 3.10; swung out, the right motor clears a launcher block)
FLOOR_Z = 1.3                           # the lane's ball-bottom height: a POLLEN's centre (2.70) and a NECTAR's (3.11) are both in the feeders' grip
PIVOT_Z = FEED_Z + ARM_L
SWING_DEG = math.degrees(math.asin(SWING / ARM_L))
GRIP_TOP_P = FEED_Z + math.sqrt((RF + RP) ** 2 - FEED_Y ** 2)    # gripped up to here (centre): 4.01 POLLEN
GRIP_TOP_N = FEED_Z + math.sqrt((RF + RN) ** 2 - FEED_Y ** 2)    # 5.00 NECTAR; the flywheels take a NECTAR from 5.77
# The count, by length. The lead ball's back rests on the backstop; the queue runs nose to tail to the intake roller's
# axle (X 8.56), and a ball is held once its centre is behind it. Every legal load must fit (the longest, 3 NECTAR +
# 1 POLLEN, needs 12.26 in from the backstop to the axle) and every illegal one must not (the shortest, 5 POLLEN,
# needs 12.60; 4 NECTAR 12.67). The backstop sits mid-window, 12.43 in behind the axle, on +-0.2 in slots to tune it.
ROLLER_AXLE_X = 8.56
BACKSTOP_X = ROLLER_AXLE_X - 12.43      # -3.87: a NECTAR in the feeders is centred on the column (X -2.04)
RAMP = ((8.0, 0.05), (5.7, FLOOR_Z))
R_R = 12 / 25.4                         # lane rollers: 24 mm compliant, tops at FLOOR_Z
ROLL_X = [5.3 - 0.8 * i for i in range(8)]    # eight shafts, 0.8 in apart, the last just ahead of the feeders. As in the mentor's
ROLL_W = 1.0                            # lane, each shaft's wheels sit to one side, alternating, so neighbours overlap and leave pockets
ROLL_Z = FLOOR_Z - R_R
CEIL_GAP, FOAM = 2.6, 0.5               # the ceiling's foam face 2.6 over the lane: a POLLEN presses the foam 0.2; a NECTAR lifts it 0.82
CEIL_X = (5.3, -0.95 + SH)              # ends clear of a ball going up the column
LINK_L, LINK_DEG = 1.2, 45.0            # parallel links, rest 45 deg down toward the rear, level at 0.85 lift
LINKS = ((6.05, 5.2), (2.2, 1.35))      # (fixed pivot X, ceiling tab X): clear of the drive motors' encoder caps (X 3.09..5.11)
CHAN_RAISE = 30 / 25.4                  # the old intake's 11-hole channel goes up 30 mm: a lane NECTAR (top 4.92), the lifted ceiling (5.37) and its front links (5.49) pass under it; its top (7.50) stays under the L-beam above (7.58)
RAIL_IN = 4.88
LM = (4.75, 1.0)                        # the lane drive's jackshaft (right side), and its motor's axis Y
LM_Y = -3.3
MOTOR_D, MOTOR_L = 37 / 25.4, 100 / 25.4
FLY_Y = (3.6427, -3.3276)               # the flywheels' axles (mentor's), z 6.646
FLY_Z = 6.646
FM_Y, FM_Z = 6.7, 7.0                   # the flywheel motors, moved out and up (along X), belted to his 41T pulleys
FDM_Y, FDM_Z = 6.7, 5.3                 # the feeder motors, outboard under them, belted to a shaft on each feeder arm's pivot
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
    w = xz([(FEED_X[1] + 0.05, 0.3), (6.5, 0.3), (6.5, 2.6), (FEED_X[1] + 0.05, 2.6)], y0, y1)   # ends just ahead of the feeders
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
    part(fixed, f"lane_shaft_{i} (8mm REX, bearings in both walls)", cyly(x, ROLL_Z, 8 / 25.4, -WO - 0.45, WO + 0.1), STEEL, "buy")
    part(fixed, f"lane_pulley_{i} (print, 12 mm hub pulley, outside the right wall)", cyly(x, ROLL_Z, 12 / 25.4, -WO - 0.4, -WO - 0.1), BLUE, "print")
part(fixed, "lane_belt (3/16 in polycord over the hub pulleys and the jackshaft, as the mentor's lane)", cord((ROLL_X[0], ROLL_Z), (ROLL_X[-1], ROLL_Z), 6 / 25.4, 6 / 25.4, -WO - 0.25), RED, "buy")
part(fixed, "lane_jackshaft (8mm REX, outside the right wall)", cyly(*LM, 8 / 25.4, -WALL_IN, LM_Y), STEEL, "buy")
part(fixed, "lane_motor (goBILDA 5203-2402-0005, 1150 RPM; along X outside the right wall, miter gears to the jackshaft)",
     cq.Workplane("YZ").center(LM_Y, LM[1] + 0.3).circle(MOTOR_D / 2).extrude(MOTOR_L).translate((LM[0] - 0.2 - MOTOR_L, 0, 0)), BLACK, "buy")

# ---- the flat ceiling, foam-faced, on parallel links: lifts evenly, 0.82 for a NECTAR anywhere ----
cz = FLOOR_Z + CEIL_GAP
part(fixed, f"ceiling_foam ({FOAM} in soft polyethylene or EVA foam, 2-3 lb/ft3)", bx(CEIL_X[1], CEIL_X[0], -WALL_IN + 0.15, WALL_IN - 0.15, cz, cz + FOAM), (0.3, 0.3, 0.32), "buy")
ceil = bx(CEIL_X[1], CEIL_X[0], -WALL_IN + 0.05, WALL_IN - 0.05, cz + FOAM, cz + FOAM + T16)
top = cz + FOAM + T16
for fx, tx in LINKS: ceil = ceil.union(bx(tx - 0.25, tx + 0.25, -WO, WO, top, top + 0.06))
part(fixed, "ceiling (1/16 in polycarbonate on four parallel links; band-held down)", ceil, POLY, "cut")
a_ = math.radians(LINK_DEG)
for s in (-1, 1):
    y0, y1 = sorted(WY(s))
    for i, (fx, tx) in enumerate(LINKS):
        pz = top + LINK_L * math.sin(a_)
        part(fixed, f"ceiling_link_post_{'front' if i == 0 else 'rear'}_{'L' if s > 0 else 'R'} (print, on the wall's top edge)", bx(fx - 0.2, fx + 0.2, y0, y1, 2.6, pz + 0.25).cut(cyly(fx, pz, 0.13, -3, 3)), BLUE, "print")
        ly0, ly1 = sorted((s * WO, s * (WO + 0.125)))
        part(fixed, f"ceiling_link_{'front' if i == 0 else 'rear'}_{'L' if s > 0 else 'R'} (1/8 in aluminium, {LINK_L} in centres)", bar((fx, pz), (tx, top + 0.03), 0.35, ly0, ly1), ALU, "cut")
    post = bx(0.55, 0.95, s * WO, s * (WO + 0.3), 1.5, 2.0)
    for dx in (-0.12, 0.0, 0.12): post = post.cut(cyly(0.75 + dx, 1.75, 0.08, -3, 3))
    part(fixed, f"ceiling_band_post_{'L' if s > 0 else 'R'} (print, three holes; band up to the rear tab: 1-2 lbf preload)", post, BLUE, "print")

# ---- the feeders: on sprung arms, one pair each side of the ball, under the flywheels, gripping it by its sides ----
# Each feeder, its shaft and its motor hang from two arms (front and rear) on a pivot straight above the feeder's axis,
# on the launcher's front and rear channels. A hard stop sets the POLLEN squeeze (0.10 a side); a band of about 1 lbf
# preload holds it there; a NECTAR swings each one out 0.41 in. The ball stays on the column either way.
FA_X, RA_X = (-1.0 + SH, -0.875 + SH), (-4.05 + SH, -3.925 + SH)   # the front and rear arm plates' X spans
for s, nm in ((1, "L"), (-1, "R")):
    y = LANE_Y + s * FEED_Y
    part(fixed, f"feeder_{nm} (goBILDA 72 mm Gecko x2, softest durometer)", cq.Workplane("YZ").center(y, FEED_Z).circle(RF).extrude(FEED_W).translate((FEED_X[0], 0, 0)), (0.35, 0.66, 0.31), "buy")
    part(fixed, f"feeder_shaft_{nm} (8mm REX, 96 mm; bearings in the two arms, its gear in front)", cq.Workplane("YZ").center(y, FEED_Z).circle(4 / 25.4).extrude(FA_X[1] + 0.3 - RA_X[0] + 0.05).translate((RA_X[0] - 0.05, 0, 0)), STEEL, "buy")
    # the drive: a motor outboard, belted to a jackshaft on the arm's pivot; a 20T-20T gear pair on the front arm turns the
    # feeder from it, so the swing never changes the belt
    gy = y
    part(fixed, f"feeder_motor_{nm} (goBILDA 5203-2402-0005, 1150 RPM; along X outboard, under the flywheel motor)",
         cq.Workplane("YZ").center(s * FDM_Y, FDM_Z).circle(MOTOR_D / 2).extrude(MOTOR_L).translate((BELT_X - 0.2 - MOTOR_L, 0, 0)), BLACK, "buy")
    part(fixed, f"feeder_pivot_jackshaft_{nm} (8mm REX: the front arm's pivot, out to the belt)", cq.Workplane("YZ").center(y, PIVOT_Z).circle(4 / 25.4).extrude(BELT_X + 0.4 - (-1.10 + SH)).translate((-1.10 + SH, 0, 0)), STEEL, "buy")
    for cx_, cz_, nmp in ((y, PIVOT_Z, "pivot"), (s * FDM_Y, FDM_Z, "motor")):
        part(fixed, f"feeder_belt_pulley_{nmp}_{nm} (16T HTD5)", cq.Workplane("YZ").center(cx_, cz_).circle(P16 / 2).extrude(0.35).translate((BELT_X, 0, 0)), BLACK, "buy")
    (y0_, z0_), (y1_, z1_) = (y, PIVOT_Z), (s * FDM_Y, FDM_Z)
    L = math.hypot(y1_ - y0_, z1_ - z0_); ny, nz = -(z1_ - z0_) / L, (y1_ - y0_) / L; runs = None
    for sg in (1, -1):
        a0 = (y0_ + sg * ny * P16 / 2, z0_ + sg * nz * P16 / 2); a1 = (y1_ + sg * ny * P16 / 2, z1_ + sg * nz * P16 / 2)
        ln = math.hypot(a1[0] - a0[0], a1[1] - a0[1]); ang = math.degrees(math.atan2(a1[1] - a0[1], a1[0] - a0[0]))
        r = cq.Workplane("YZ").rect(ln, 0.09).extrude(0.35).rotate((0, 0, 0), (1, 0, 0), ang).translate((BELT_X, (a0[0] + a1[0]) / 2, (a0[1] + a1[1]) / 2))
        runs = r if runs is None else runs.union(r)
    part(fixed, f"feeder_belt_{nm} (HTD5 9 mm, motor to pivot)", runs, BLACK, "buy")
    for cz_, nmg in ((PIVOT_Z, "pivot"), (FEED_Z, "feeder")):
        part(fixed, f"feeder_gear_{nmg}_{nm} (20T mod 0.8, 8mm REX bore; on the front arm)", cq.Workplane("YZ").center(y, cz_).circle(8.5 / 25.4).extrude(0.25).translate((FA_X[1] + 0.03, 0, 0)), (0.7, 0.6, 0.3), "buy")
    for x0, x1, w in (FA_X + ("front",), RA_X + ("rear",)):
        arm = cq.Workplane("YZ").center(y, (PIVOT_Z + FEED_Z) / 2).rect(0.7, ARM_L + 0.7).extrude(x1 - x0).translate((x0, 0, 0))
        arm = arm.cut(cq.Workplane("YZ").center(y, FEED_Z).circle(14 / 25.4 / 2).extrude(1).translate((x0 - 0.5, 0, 0))).cut(cq.Workplane("YZ").center(y, PIVOT_Z).circle(8.3 / 25.4 / 2).extrude(1).translate((x0 - 0.5, 0, 0)))
        part(fixed, f"feeder_arm_{w}_{nm} (1/8 in aluminium, {ARM_L} in pivot to axle)", arm, ALU, "cut")
    part(fixed, f"feeder_pivot_rear_{nm} (8mm REX stub, rear arm to the launcher's rear channel)",
         cq.Workplane("YZ").center(y, PIVOT_Z).circle(4 / 25.4).extrude(RA_X[0] - (-4.41 + SH)).translate((-4.41 + SH, 0, 0)), STEEL, "buy")
    stop = bx(-1.10 + SH, -1.0 + SH, s * (FEED_Y - 0.6), s * (FEED_Y - 0.35), FEED_Z + 0.4, FEED_Z + 0.6)   # the front arm (0.35 either side of the axle) rests on it
    part(fixed, f"feeder_stop_{nm} (print, on the launcher's front channel, +-0.1 in slots: sets the POLLEN squeeze)", stop, BLUE, "print")
    post = bx(-1.10 + SH, -1.0 + SH, s * (FEED_Y + 0.9), s * (FEED_Y + 1.15), FEED_Z + 0.2, FEED_Z + 0.6)
    part(fixed, f"feeder_band_post_{nm} (print, on the front channel; band to the front arm: about 1 lbf at rest)", post, BLUE, "print")
# under the ball between the feeders: a short floor, and a backstop that sets it on the column
fl = bx(BACKSTOP_X - 0.15, FEED_X[1] + 0.15, -1.3, 1.3, FLOOR_Z - T16, FLOOR_Z)
part(fixed, "feeder_floor (1/16 in polycarbonate, between the feeders)", fl, POLY, "cut")
part(fixed, "backstop (1/8 in polycarbonate, on +-0.2 in slots: sets the 4-piece / 3-NECTAR limit; tune it on the robot)", bx(BACKSTOP_X - 0.125, BACKSTOP_X, -1.3, 1.3, FLOOR_Z, 3.6), POLY, "cut")
for s in (-1, 1):
    part(fixed, f"feeder_floor_hanger_{'L' if s > 0 else 'R'} (print, floor to the launcher's rear channel; the front edge bolts to the lane walls)", bx(-4.85 + SH, -4.62 + SH, s * 1.3, s * 2.0, FLOOR_Z - 0.2, 1.95), BLUE, "print")

# ---- the launcher changes: its front channels reach down to the feeder shafts, the flywheel motors move out and up ----
for s, y0, y1 in ((1, 2.43, 4.67), (-1, -4.36, -2.12)):
    fy = s * FEED_Y
    ch = bx(-1.58 + SH, -1.10 + SH, y0, y1, 1.88, 3.76).cut(bx(-2 + SH, -0.5 + SH, min(fy, fy + s * SWING) - 0.22, max(fy, fy + s * SWING) + 0.22, FEED_Z - 0.22, FEED_Z + 0.32))
    part(launcher, f"launcher_front_channel_{'L' if s > 0 else 'R'} (goBILDA 5-hole lowside U-channel, replacing the 3-hole; slotted for the feeder shaft's swing; carries the feeder arm's pivot)", ch, ALU, "buy")
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
