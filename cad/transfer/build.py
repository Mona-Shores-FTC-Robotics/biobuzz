"""The intake-to-launcher transfer, v4 (8 Oct 2026): a ramp, a flat lane of small compliant rollers under a flat sprung
ceiling, and one driven feeder against a sprung pad under the flywheels that drives each ball straight up into them.
Built from goBILDA parts wherever they fit, every screw drawn. Drawn in the robot CAD's frame, so dhs-transfer.step lands
in place in Onshape next to cad/intake-b/dhs-intake-b.step.

    python3 cad/transfer/build.py        # writes dhs-transfer.step and stl/ next to this file

Robot frame: +X forward, +Y left, +Z up, inches, origin on the floor under the chassis centre (7.56 in behind the front
face), moved into the CAD's millimetres at the end. Groups: "fixed" (ramp, lane, ceiling, feeder, pad and their drives)
and "launcher" (the changes in the mentor's launcher: the flywheel motors moved out and up).

How it works: the intake roller pushes each ball up the ramp onto the lane. The lane's rollers carry it back under the
ceiling, which presses it onto them, and push it in between the feeder and the pad, under the flywheels, against the
backstop. Stopped, the feeder holds it there, below the flywheels' reach; the rest queue behind it. Feeding runs the
feeder: it drives the ball, pinched against the pad, straight up into the flywheels.

What v4 changed from v3, and why (README "What changed in v4"): the lane runs on a continuous-rotation servo instead of a
motor (v3 made nine DC motors; FTC allows eight), on five shafts instead of eight; the walls hang from the drive rails on
goBILDA REX standoffs; the feeder's bearings sit in two small plates on the mentor's launcher channels instead of holes
drilled in them; the feeder runs on a second continuous-rotation servo, belted (215 mm) to its shaft; every belt is a
goBILDA length on fixed centres; every screw is drawn and checked.
"""
import math, os, sys
import cadquery as cq
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "robot-addons")); sys.path.insert(0, os.path.join(HERE, ".."))
from build import C, F, FACE, IN
import fasteners as FA

CENTRE_BACK_IN = 7.56
T16, TW = 1 / 16, 1 / 8
LANE_Y = 0.0                            # the lane's and the feeder's centreline
RN, RP = 1.81, 1.40                     # NECTAR, POLLEN radii
LAUNCHER_SHIFT = 0.8                    # the mentor's launcher and turret move forward this much (cad/full-robot applies it), so the
                                        # lane's length holds at most 4 pieces, at most 3 of them NECTAR (see BACKSTOP_X)
SH = LAUNCHER_SHIFT
COL_X = -2.845 + SH                     # the launch column: under the flywheels' centre and the turret's axis
MM = 1 / 25.4

# ---- the drive rails (mentor's goBILDA 1107 channel, 384 mm): web 2.3 mm, inner face at |Y| 5.26, flanges inward to 4.88.
# Its web's holes: 14 mm at X = 24k mm, z 48.3 mm; 4 mm at X = 24k +- 8 mm, z 48.3 +- 8 and +- 16 mm; 4 mm at X = 24k + 12, z 48.3.
RAIL_WEB_IN, RAIL_WEB_OUT, RAIL_IN = 5.26, 5.35, 4.88
RAIL_HOLE = lambda xmm, zmm: (xmm * MM, zmm * MM)
# ---- the lane walls hang from the rails on goBILDA 1516-4008-0800 REX standoffs (80 mm): 1/4 in polycarbonate, inner face
# 1.86 from the centreline (a NECTAR clears the front drive motors' encoder caps by 0.05)
STANDOFF_L = 80 * MM
WO = RAIL_WEB_IN - STANDOFF_L           # the walls' outer faces, |Y| (2.11)
WALL_T = 0.25
WALL_IN = WO - WALL_T                   # their inner faces (1.86)
WALL_Z = (0.3, 2.6)
STANDOFFS = {1: [RAIL_HOLE(x, z) for x in (40, 88) for z in (40.1, 56.5)],      # X, z: two columns of each rail's 4 mm holes, where a key
             -1: [RAIL_HOLE(x, z) for x in (8, 88) for z in (40.1, 56.5)]}      # reaches them from outside (clear of the pods and wheels)
# the feeders: goBILDA 72 mm Gecko wheels, two side by side, on a shaft along X, under the flywheels (96 mm ones would
# reach the drive rails)
RF, FEED_W = 36 / 25.4, 1.89
FEED_X = (-3.79 + SH, -1.90 + SH)       # the flywheels' X span
# One driven feeder on the left, fixed; opposite it a sprung foam pad on a bottom hinge. The pad takes the size
# difference: it rests where a POLLEN presses its foam 0.2 (and the feeder's tread 0.1), and a NECTAR pushes it back
# 0.77. Low band preload, so both sizes enter the stopped feeder under the lane's push.
FEED_IN = 1.45                          # the feeder's tread, inner edge, Y: a POLLEN presses 0.10 into it and centres at Y 0.15; a NECTAR presses 0.15, centres at -0.21
FEED_Y = FEED_IN + RF                   # its axle's Y (2.87)
# its axle (FEED_Z, below): 3.25, as high as it goes and stay 0.17 clear of the left flywheel
FLOOR_Z = 1.3                           # the lane's ball-bottom height: a POLLEN's centre (2.70) and a NECTAR's (3.11) are in the feeder's grip
PAD_FACE = -1.05                        # the pad's foam face at rest, Y; 0.5 in of foam on 1/8 in aluminium
PAD_FOAM = 0.5
PAD_HINGE = (-1.90, 1.62)               # (Y, z) of its hinge, along X, outboard of the plate's foot, in two blocks on the feeder floor
PAD_TOP = 4.55
# The count, by length. The lead ball's back rests on the backstop; the queue runs nose to tail to the intake roller's
# axle (X 8.62), and a ball is held once its centre is behind it. Every legal load must fit (the longest, 3 NECTAR +
# 1 POLLEN, needs 12.26 in from the backstop to the axle) and every illegal one must not (the shortest, 5 POLLEN,
# needs 12.60; 4 NECTAR 12.67). The backstop sits mid-window, 12.43 in behind the axle, on +-0.2 in slots to tune it.
ROLLER_AXLE_X = 7.56 + 0.061 + 1.0      # the roller's 2 in vector wheels, their back 0.06 in clear of the face
BACKSTOP_X = ROLLER_AXLE_X - 12.43      # -3.81: a NECTAR in the feeders sits 0.06 in ahead of the column's centre (X -2.04)
RAMP = ((8.0, 0.05), (5.7, FLOOR_Z))
R_R = 12 / 25.4                         # lane rollers: 24 mm printed TPU, tops at FLOOR_Z
ROLL_X = [5.3 - 1.4 * i for i in range(5)]    # five shafts, 1.4 in apart, the last just ahead of the feeder. As in the mentor's
ROLL_W = 1.0                            # lane, each shaft's roller sits to one side, alternating, so a ball rides on both sides
ROLL_Z = FLOOR_Z - R_R
CEIL_GAP, FOAM = 2.6, 0.5               # the ceiling's foam face 2.6 over the lane: a POLLEN presses the foam 0.2; a NECTAR lifts it 0.82
CEIL_X = (5.3, -0.95 + SH)              # ends clear of a ball going up the column
CEIL_PINS = (5.35, 2.5)                # the ceiling rides on two pins a side in vertical slots in posts on the walls' outer faces
CEIL_TRAVEL = 0.85                      # slot length: a NECTAR lifts it 0.82
CHAN_RAISE = 30 / 25.4                  # the old intake's 11-hole channel goes up 30 mm: a lane NECTAR (top 4.92) and the lifted ceiling pass under it
WALL_X = (FEED_X[1] + 0.3, 7.4)         # the walls end just ahead of the feeder and the pad's hinge block, and carry the ramp in slots
RAMP_SLOT = 0.1                         # the ramp's edges sit this deep in slots routed in the walls' inner faces
# the lane's drive: a goBILDA 2000-0025-0004 Super Speed servo in continuous rotation (programmed with the 3102 programmer),
# powered at 6 V by a REV Servo Power Module (230 RPM). Its tabs stand on two 43 mm standoffs from the right rail's web,
# its spline toward the lane; a 40 mm printed pulley on the spline drives shaft 3's 16 mm groove by polycord: 2.5:1 up,
# about 575 RPM at the lane shafts (28 in/s of roller tread, about 14 in/s of ball)
SV_TABS = [RAIL_HOLE(x, 56.5) for x in (32, 80)]   # the tabs' lower pair of holes (48 mm apart) on two of the web's holes
SV_C = (56 * MM, (56.5 + 5) * MM)                  # the case's centre (X, z): the holes are 5 mm below it
SV_SPL = (SV_C[0] - 10 * MM, SV_C[1])              # the spline: 10 mm from the centre, toward the back (clear of shaft 2's pulley)
SV_STANDOFF = 43 * MM
SV_TAB_Y = -(RAIL_WEB_IN - SV_STANDOFF)            # the tabs' face, Y (-3.57)
SV_PUL_PD = 40 * MM                                # the servo pulley's groove (pitch) diameter
SV_SHAFT = 3                                       # the lane shaft it drives
# pulleys: printed, three 3/16 in polycord grooves on every lane shaft outside the right wall (one part); the shafts chain
# in pairs on grooves a and b, the servo drives shaft 3 on groove c
PUL_Y0, PUL_L = -(WO + 0.04), 17 * MM              # from the bearing's flange outward
GROOVE = [PUL_Y0 - (3 + 5.5 * k) * MM for k in range(3)]
PUL_PD = 16 * MM                                   # the grooves' pitch diameter
# the feeder's drive: a second goBILDA 2000-0025-0004 Super Speed servo in continuous rotation, straight above the
# feeder's shaft, ahead of the launcher: a goBILDA 1910-0025-0816 hub on its spline carries a goBILDA 3411-0014-0024
# 24T hub-mount pulley, belted (goBILDA 3412-0009-0215) to a 3417-4008-0024 24T on the feeder's shaft. The servo's tabs
# stand on two 34 mm standoffs from the front bearing plate, which reaches up the mentor's front 3-hole channel for it.
# A servo can't throw the ball the 1.7 in up to the flywheels (README "Open"): either the hand-off closes up, or a DC
# motor goes here once one of the mentor's launcher channels moves to make room for it.
P24 = 24 * 5 / math.pi / 25.4
MOTOR_D = 37 * MM
QB = 43 * MM                                       # a goBILDA quad block: sizes the flywheel motor bracket's upright
FEED_BELT = (215, "3412-0009-0215")
FEED_C = (FEED_BELT[0] * MM - math.pi * P24) / 2   # 47.5 mm: the belt's exact centres
FD_PUL_X = (-0.06 + 5.5 * MM, -0.06 + 17.5 * MM)  # the 24Ts' plane: ahead of the front bearing plate by the hub-pulley screws' heads
BELT_X = sum(FD_PUL_X) / 2
P16, P41 = 16 * 5 / math.pi / 25.4, 41 * 5 / math.pi / 25.4
def belt_len(c, d0, d1):
    """An open belt's pitch length (inches) for centres c and pitch diameters d0, d1."""
    return 2 * c + math.pi * (d0 + d1) / 2 + (d1 - d0) ** 2 / (4 * c)

FEED_Z = 3.25
FS_TILT = math.radians(3)                          # the feeder servo sits on the belt's centres, 3 deg inboard of straight up:
FS_Y, FS_Z = FEED_IN + RF - FEED_C * math.sin(FS_TILT), FEED_Z + FEED_C * math.cos(FS_TILT)   # clear of the front plate's screws
FS_LONG, FS_ACROSS = (-math.sin(FS_TILT), math.cos(FS_TILT)), (math.cos(FS_TILT), math.sin(FS_TILT))   # its case's long axis, (Y, z)
FS_HOLES = [(FS_Y + 34 * MM * FS_LONG[0] + dy * MM * FS_ACROSS[0], FS_Z + 34 * MM * FS_LONG[1] + dy * MM * FS_ACROSS[1]) for dy in (-5, 5)]
# (its tabs' far pair of holes; the near pair is inside the pulley)
GRIP_TOP_P = FEED_Z + math.sqrt((RF + RP) ** 2 - (FEED_Y - 0.15) ** 2)    # the feeder grips a POLLEN up to here (centre)
GRIP_TOP_N = FEED_Z + math.sqrt((RF + RN) ** 2 - (FEED_Y + 0.21) ** 2)    # and a NECTAR; the flywheels take a NECTAR from 5.77

# the flywheel motors, moved out and up (along X, facing back), each belted to his 41T pulley on a goBILDA belt length:
# 315 mm (left) and 320 mm (right); the centres are set for those
FLY_Y = (3.6427, -3.3276)               # the flywheels' axles (mentor's), z 6.646
FLY_Z = 6.646
FLY_BELT = {1: (315, "3412-0009-0315"), -1: (320, "3412-0009-0320")}
def _fly_motor(s):
    fy = FLY_Y[0] if s > 0 else FLY_Y[1]; d = (s * 1.0, 0.115); n = math.hypot(*d); d = (d[0] / n, d[1] / n)
    L = FLY_BELT[s][0] * MM; lo, hi = 2.0, 5.0
    for _ in range(60):
        c = (lo + hi) / 2
        lo, hi = (c, hi) if belt_len(c, P41, P16) < L else (lo, c)
    return fy + d[0] * c, FLY_Z + d[1] * c
FM = {s: _fly_motor(s) for s in (1, -1)}
FLY_BELT_X = -3.26                      # his 41T pulleys' belt plane (launcher moved)
FM_FACE = -2.65                         # the moved motors' faces, X (shafts pointing back)
SIDE_FLANGE = {1: (5.35, 5.83), -1: (-5.51, -5.04)}   # the launcher side channels' top flanges, Y; top face z 5.74
SIDE_FLANGE_HOLES = {1: 5.669, -1: -5.354}          # their 4 mm holes' line, Y; at X = -0.149 - 8 mm k
SIDE_FLANGE_T = 2.54
SIDE_TOP = 5.74

FEEDER_SPINS = r"^(feeder \(|feeder_shaft |feeder_pulley \(|feeder_spacers_|feeder_shaft_spacer |feeder_shaft_collar |feeder_eclip )"   # what turns with the feeder
PAD_SWINGS = r"^pad_(plate|foam|foam_glue|knuckle_rear|knuckle_front|hinge|hinge_eclip_rear|hinge_eclip_front) "                 # what swings with the pad

fixed, launcher = {}, {}
def part(d, name, wp, col, kind): d[name] = (wp, col, kind)
BLUE, ALU, STEEL, POLY, BLACK, GREEN, RED, TPU = (0.18, 0.37, 0.62), (0.75, 0.78, 0.82), (0.8, 0.82, 0.85), (0.6, 0.78, 0.96), (0.13, 0.15, 0.17), (0.35, 0.66, 0.31), (0.75, 0.2, 0.2), (0.22, 0.22, 0.24)

# ---- helpers, all in robot-frame inches (X fwd, Y left, Z up) ----
def bx(x0, x1, y0, y1, z0, z1):
    return cq.Workplane("XY").box(abs(x1 - x0), abs(y1 - y0), abs(z1 - z0)).translate(((x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2))
def cyly(x, z, d, y0, y1):
    """A cylinder along Y."""
    y0, y1 = min(y0, y1), max(y0, y1)
    return cq.Workplane("XZ").center(x, z).circle(d / 2).extrude(-(y1 - y0)).translate((0, y0, 0))
def cylz(x, y, d, z0, z1):
    """A cylinder along Z."""
    z0, z1 = min(z0, z1), max(z0, z1)
    return cq.Workplane("XY").center(x, y).circle(d / 2).extrude(z1 - z0).translate((0, 0, z0))
def cylx(y, z, d, x0, x1):
    """A cylinder along X."""
    x0, x1 = min(x0, x1), max(x0, x1)
    return cq.Workplane("YZ").center(y, z).circle(d / 2).extrude(x1 - x0).translate((x0, 0, 0))
def xz(pts, y0, y1):
    """A profile drawn in X-Z, extruded across Y from y0 to y1."""
    y0, y1 = min(y0, y1), max(y0, y1)
    return cq.Workplane("XZ").polyline(pts).close().extrude(-(y1 - y0)).translate((0, y0, 0))
def yz(pts, x0, x1):
    """A profile drawn in Y-Z, extruded along X from x0 to x1."""
    x0, x1 = min(x0, x1), max(x0, x1)
    return cq.Workplane("YZ").polyline(pts).close().extrude(x1 - x0).translate((x0, 0, 0))
def _hull(plane, c0, r0, c1, r1, w, at):
    """The 2D hull of two circles (in the plane's own coordinates), extruded w: plane "YZ" along +X from at, "XZ" along +Y."""
    (a0, b0), (a1, b1) = c0, c1; L = math.hypot(a1 - a0, b1 - b0); u = ((a1 - a0) / L, (b1 - b0) / L); n = (-u[1], u[0])
    sp = (r0 - r1) / L; cp = math.sqrt(max(0.0, 1 - sp * sp))
    m = [(u[0] * sp + s * n[0] * cp, u[1] * sp + s * n[1] * cp) for s in (1, -1)]
    poly = [(a0 + r0 * m[0][0], b0 + r0 * m[0][1]), (a1 + r1 * m[0][0], b1 + r1 * m[0][1]), (a1 + r1 * m[1][0], b1 + r1 * m[1][1]), (a0 + r0 * m[1][0], b0 + r0 * m[1][1])]
    ext = (lambda wp: wp.extrude(w)) if plane == "YZ" else (lambda wp: wp.extrude(-w))
    s = ext(cq.Workplane(plane).polyline(poly).close())
    for c, r in ((c0, r0), (c1, r1)): s = s.union(ext(cq.Workplane(plane).center(*c).circle(r)))
    return s.translate((at, 0, 0) if plane == "YZ" else (0, at, 0))
def loop(plane, c0, r0, c1, r1, t, w, at):
    """A belt (or cord) loop of thickness t and width w around two pulleys of pitch radii r0, r1, centred on the pitch line."""
    return _hull(plane, c0, r0 + t / 2, c1, r1 + t / 2, w, at).cut(_hull(plane, c0, max(0.01, r0 - t / 2), c1, max(0.01, r1 - t / 2), w + 0.02, at - 0.01))
def sheet(pts, y0, y1, t=T16):
    out = None
    for (x0, z0), (x1, z1) in zip(pts, pts[1:]):
        L = math.hypot(x1 - x0, z1 - z0); nx, nz = -(z1 - z0) / L * t, (x1 - x0) / L * t
        seg = xz([(x0, z0), (x1, z1), (x1 - nx, z1 - nz), (x0 - nx, z0 - nz)], y0, y1)
        out = seg if out is None else out.union(seg)
    return out
def mirror_y(wp): return wp.mirror("XZ")
def rex_y(x, z, y0, y1):
    """An 8mm REX bore (a 7.3 mm hexagon, corners rounded to 8.3 mm) along Y, through x, z."""
    y0, y1 = min(y0, y1), max(y0, y1)
    hexa = cq.Workplane("XZ").center(x, z).polygon(6, 7.3 * MM / math.cos(math.pi / 6)).extrude(-(y1 - y0))
    return hexa.intersect(cq.Workplane("XZ").center(x, z).circle(8.3 * MM / 2).extrude(-(y1 - y0))).translate((0, y0, 0))
def rex_x(y, z, x0, x1):
    """The same along X."""
    x0, x1 = min(x0, x1), max(x0, x1)
    hexa = cq.Workplane("YZ").center(y, z).polygon(6, 7.3 * MM / math.cos(math.pi / 6)).extrude(x1 - x0)
    return hexa.intersect(cq.Workplane("YZ").center(y, z).circle(8.3 * MM / 2).extrude(x1 - x0)).translate((x0, 0, 0))

# ---- into the robot CAD's millimetres: (x, y, z)_CAD = (C + Y, F + Z, FACE - 7.56 in + X), all times 25.4 ----
def cad_pt(p): return (C + p[1] * IN, F + p[2] * IN, FACE - CENTRE_BACK_IN * IN + p[0] * IN)
def cad_dir(a): return (a[1], a[2], a[0])
def bolt(parts, joint, holds, heads, axis, grip_mm, **kw):
    """cad/fasteners.py's bolt, for heads in the robot frame (inches): the screws go into d (inches) and its records
    (SCREWS, NUTS, INFO) in the team CAD's mm, as the full-robot STEP and the fastener check read them. Returns the
    clearance-hole cutter, in inches."""
    tmp = {}; heads_mm = [tuple(c * IN for c in h) for h in heads]
    cut = FA.bolt(tmp, joint, holds, heads_mm, axis, grip_mm, **kw)
    if not kw.get("nut", True) and kw.get("into"):
        L = FA.length_for(grip_mm, kw.get("d", 4), False, kw.get("tapped"), kw.get("min_engage"))
        for i, h in enumerate(heads): THREADS.append((f"{joint}_{i}", h, axis, L, grip_mm, kw.get("d", 4), kw["into"]))
    for n, (wp, col, kind) in tmp.items():
        parts[n] = (cq.Workplane().add(wp.val().scale(1 / IN)), col, kind)
        for reg in (FA.SCREWS, FA.NUTS):
            if n in reg:
                sku, p, a = reg[n]; reg[n] = (sku, cad_pt(tuple(c / IN for c in p)), cad_dir(a))
        if n in FA.INFO:
            I = FA.INFO[n]; I["head"] = cad_pt(tuple(c / IN for c in I["head"])); I["axis"] = cad_dir(I["axis"])
    return cq.Workplane().add(cut.val().scale(1 / IN))
def drill(d, names, cutter): FA.drill(d, names, cutter)
THREADS = []                                # (screw name, head (in), axis, length, grip (mm), d, into): for threaded_holes()
def vendor(name, fname, src_o, src_ax, src_ref, dst_o, dst_ax, dst_ref):
    """Place a directional vendor part (its own STEP, local mm) at a robot-frame point (inches) and direction."""
    FA.PLACED[name] = (fname, src_o, src_ax, src_ref, cad_pt(dst_o), cad_dir(dst_ax), cad_dir(dst_ref))

# ---- the ramp (1/16 in polycarbonate): its edges slide into slots routed in the walls (a drop of CA each side holds it) ----
RAMP_PTS = [RAMP[0], RAMP[1], (ROLL_X[0] + R_R, FLOOR_Z)]
part(fixed, "ramp (1/16 in polycarbonate; its edges in the walls' slots)", sheet(RAMP_PTS, -WALL_IN - RAMP_SLOT + 0.01, WALL_IN + RAMP_SLOT - 0.01), POLY, "cut")

# ---- the lane walls: 1/4 in polycarbonate, on four REX standoffs each from the rail, low under the front drive motors ----
def wall(s):
    y0, y1 = sorted((s * WALL_IN, s * WO))
    w = xz([(WALL_X[0], WALL_Z[0]), (WALL_X[1], WALL_Z[0]), (WALL_X[1], WALL_Z[1]), (WALL_X[0], WALL_Z[1])], y0, y1)
    for x in ROLL_X: w = w.cut(cyly(x, ROLL_Z, 14 * MM, y0 - 0.1, y1 + 0.1))        # the shafts' flanged bearings, pressed in from outside
    slot = sheet([(RAMP_PTS[0][0] - 0.3, RAMP_PTS[0][1] - 0.3 * 1.25 / 2.3)] + RAMP_PTS[1:], s * WALL_IN - 0.01 * s, s * (WALL_IN + RAMP_SLOT), t=T16 + 0.01)
    return w.cut(slot)
for s, nm in ((1, "L"), (-1, "R")): part(fixed, f"lane_wall_{nm} (1/4 in polycarbonate)", wall(s), POLY, "cut")
for s, nm in ((1, "L"), (-1, "R")):
    for k, (x, z) in enumerate(STANDOFFS[s]):
        part(fixed, f"wall_standoff_{nm}{k} (goBILDA 1516-4008-0800, 8mm REX standoff, 80 mm)", cyly(x, z, 8 * MM, s * WO, s * RAIL_WEB_IN), STEEL, "buy")
        vendor(f"wall_standoff_{nm}{k} (goBILDA 1516-4008-0800, 8mm REX standoff, 80 mm)", "1516-4008-0800.STEP", (0, 0, 0), (0, 1, 0), (1, 0, 0), (x, s * RAIL_WEB_IN, z), (0, -s, 0), (1, 0, 0))
    heads = [(x, s * RAIL_WEB_OUT, z) for x, z in STANDOFFS[s]]
    bolt(fixed, f"wall_rail_{nm}", f"the {nm} wall's standoffs to the rail (from outside the rail)", heads, (0, -s, 0), 2.3, nut=False, tapped=10, into=f"wall_standoff_{nm}")
    heads = [(x, s * WALL_IN, z) for x, z in STANDOFFS[s]]
    c = bolt(fixed, f"wall_standoff_{nm}", f"the {nm} wall to its standoffs (flat heads, flush inside the lane)", heads, (0, s, 0), WALL_T * IN, nut=False, tapped=10, into=f"wall_standoff_{nm}", through=(f"lane_wall_{nm}",), flat=True)
    drill(fixed, [f"lane_wall_{nm}"], c)

# ---- the lane: five shafts of printed TPU rollers, alternating sides, in flanged bearings in both walls ----
BRG_IN = WO - 4.2 * MM                  # a pressed-in 1611 bearing's inner face, |Y| (its flange on the wall's outer face)
SHAFT_L = 144                           # goBILDA 2106-4008-1440: 8mm REX, e-clip grooves at both ends
for i, x in enumerate(ROLL_X):
    side = 1 if i % 2 == 0 else -1
    bore = rex_y(x, ROLL_Z, -3, 3)
    part(fixed, f"lane_roller_{i} (print, TPU 95A, 24 mm x 1 in, 8mm REX bore; on the {'left' if side > 0 else 'right'})", cyly(x, ROLL_Z, 2 * R_R, side * 0.05, side * (0.05 + ROLL_W)).cut(bore), TPU, "print")
    part(fixed, f"lane_spacer_{i}a (print, PETG, 12 mm OD tube, 8mm REX bore)", cyly(x, ROLL_Z, 12 * MM, side * (0.05 + ROLL_W), side * BRG_IN).cut(bore), BLUE, "print")
    part(fixed, f"lane_spacer_{i}b (print, PETG, 12 mm OD tube, 8mm REX bore)", cyly(x, ROLL_Z, 12 * MM, side * 0.05, -side * BRG_IN).cut(bore), BLUE, "print")
    y_left = WO + 0.08                  # the left e-clip, just outside the bearing's flange
    part(fixed, f"lane_shaft_{i} (goBILDA 2106-4008-1440, 8mm REX, 144 mm, e-clips)", cyly(x, ROLL_Z, 8 * MM, y_left, y_left - SHAFT_L * MM), STEEL, "buy")
    for s, nm in ((1, "L"), (-1, "R")):
        part(fixed, f"lane_bearing_{i}{nm} (goBILDA 1611-0514-4008 flanged bearing)", cyly(x, ROLL_Z, 14 * MM, s * BRG_IN, s * WO).union(cyly(x, ROLL_Z, 15 * MM, s * WO, s * (WO + 0.8 * MM))), STEEL, "buy")
        vendor(f"lane_bearing_{i}{nm} (goBILDA 1611-0514-4008 flanged bearing)", "1611-0514-4008.STEP", (0, 4.8, 0), (0, -1, 0), (1, 0, 0), (x, s * (WO + 0.8 * MM), ROLL_Z), (0, -s, 0), (1, 0, 0))
    pul = cyly(x, ROLL_Z, 19 * MM, PUL_Y0, PUL_Y0 - PUL_L)
    for g in GROOVE: pul = pul.cut(cyly(x, ROLL_Z, 22 * MM, g + 2.4 * MM, g - 2.4 * MM).cut(cyly(x, ROLL_Z, PUL_PD - 4.8 * MM, g + 3 * MM, g - 3 * MM)))
    part(fixed, f"lane_pulley_{i} (print, PETG, three 3/16 in polycord grooves, 8mm REX bore)", pul.cut(bore), BLUE, "print")
    part(fixed, f"lane_shaft_spacer_{i} (goBILDA 8mm REX spacers, stacked to the e-clip)", cyly(x, ROLL_Z, 12 * MM, PUL_Y0 - PUL_L, y_left - SHAFT_L * MM + 0.08), STEEL, "buy")
for i in range(4):                      # the shafts chain in pairs, grooves a and b alternating
    g = GROOVE[i % 2]
    part(fixed, f"lane_cord_{i} (3/16 in polycord, welded loop, cut 8% short; shafts {i}-{i + 1})", loop("XZ", (ROLL_X[i], ROLL_Z), PUL_PD / 2, (ROLL_X[i + 1], ROLL_Z), PUL_PD / 2, 3 / 16, 3 / 16, g - 3 / 32), RED, "buy")
# the servo: case 40 x 20 mm, tabs 55 mm, its case top 12.8 mm above the tabs' face and its spline 4.1 mm more
def servo_local():
    """A goBILDA 2000 series servo in its own frame (mm): the spline at the origin pointing +z (its top at z 4.1), the
    case's top at z 0, the tabs' face at z -12.8, the case's centre at x +10 (the long axis), its bottom at z -36.8."""
    case = cq.Workplane("XY").box(40, 20, 36.8).translate((10, 0, -18.4))
    tabs = cq.Workplane("XY").box(55, 20, 2.5).translate((10, 0, -12.8 - 1.25))
    for dx in (-24, 24):
        for dy in (-5, 5): tabs = tabs.cut(cq.Workplane("XY").circle(2.15).extrude(4).translate((10 + dx, dy, -16)))
    return case.union(tabs).union(cq.Workplane("XY").circle(3).extrude(4.1))
def place_local(wp, o, z_dir, x_dir):
    """A local-mm part moved to robot inches: its origin to o, its +z along z_dir, its +x along x_dir."""
    sh = wp.val().scale(MM)
    loc = cq.Location(cq.Plane(origin=o, xDir=x_dir, normal=z_dir))
    return cq.Workplane().add(sh.moved(loc))
def servo():
    return place_local(servo_local(), (SV_SPL[0], SV_TAB_Y + 12.8 * MM, SV_SPL[1]), (0, 1, 0), (1, 0, 0))
SV_NAME = "lane_servo (goBILDA 2000-0025-0004 Super Speed, continuous rotation, 6 V)"
part(fixed, SV_NAME, servo(), BLACK, "buy")
vendor(SV_NAME, "2000-0025-0002.step", (-10.0, 0.0, 12.8), (0, 0, 1), (1, 0, 0), (SV_SPL[0], SV_TAB_Y + 12.8 * MM, SV_SPL[1]), (0, 1, 0), (1, 0, 0))
for k, (x, z) in enumerate(SV_TABS):
    part(fixed, f"servo_standoff_{k} (goBILDA 1501-0006-0430, M4 standoff, 43 mm)", cyly(x, z, 6 * MM, -RAIL_WEB_IN, SV_TAB_Y), STEEL, "buy")
bolt(fixed, "lane_servo_rail", "the lane servo's standoffs to the right rail (from outside the rail)", [(x, -RAIL_WEB_OUT, z) for x, z in SV_TABS], (0, 1, 0), 2.3, nut=False, tapped=10, into="^servo_standoff_")
bolt(fixed, "lane_servo_tabs", "the lane servo's tabs to its standoffs", [(x, SV_TAB_Y + 2.5 * MM, z) for x, z in SV_TABS], (0, -1, 0), 2.5, nut=False, tapped=10, into="^servo_standoff_", through=("lane_servo",), service="the right wall off first (its four flat heads), or a short key")
sp_y0 = SV_TAB_Y + 12.8 * MM
sp_y1 = GROOVE[2] + 3 * MM                          # the pulley runs from the case's top out to past groove c
pul = cyly(*SV_SPL, SV_PUL_PD + 3 * MM, sp_y0, sp_y1).cut(cyly(*SV_SPL, SV_PUL_PD + 6 * MM, GROOVE[2] + 2.4 * MM, GROOVE[2] - 2.4 * MM).cut(cyly(*SV_SPL, SV_PUL_PD - 4.8 * MM, GROOVE[2] + 3 * MM, GROOVE[2] - 3 * MM)))
pul = pul.cut(cyly(*SV_SPL, 6.2 * MM, sp_y0 - 0.01, sp_y0 + 4.2 * MM)).cut(cyly(*SV_SPL, 3.4 * MM, sp_y0, sp_y1 + 0.01)).cut(cyly(*SV_SPL, 6 * MM, sp_y1 - 3.2 * MM, sp_y1 + 0.01))
part(fixed, "servo_pulley (print, PETG: one 40 mm polycord groove, H25T spline socket; an M3 screw into the spline holds it)", pul, BLUE, "print")
bolt(fixed, "servo_pulley", "the lane servo's pulley to its spline (the servo's own M3 horn screw)", [(SV_SPL[0], sp_y1 - 3.2 * MM, SV_SPL[1])], (0, -1, 0), (sp_y1 - 3.2 * MM - (sp_y0 + 4.1 * MM)) * IN, d=3, nut=False, tapped=4.5, min_engage=3, label="M3 x 8 servo horn screw, with the servo", into="lane_servo", through=("servo_pulley",), modelled=True, service="the right wall off first (its four flat heads)")
part(fixed, f"servo_cord (3/16 in polycord, welded loop, cut 8% short; servo to shaft {SV_SHAFT}, 2.5:1)", loop("XZ", SV_SPL, SV_PUL_PD / 2, (ROLL_X[SV_SHAFT], ROLL_Z), PUL_PD / 2, 3 / 16, 3 / 16, GROOVE[2] - 3 / 32), RED, "buy")

# ---- the flat ceiling, foam-faced, on pins in vertical slots: lifts evenly, 0.82 for a NECTAR anywhere; unhook the bands
#      and it lifts off ----
cz = FLOOR_Z + CEIL_GAP
part(fixed, f"ceiling_foam ({FOAM} in soft polyethylene or EVA foam, 2-3 lb/ft3)", bx(CEIL_X[1], CEIL_X[0], -WALL_IN + 0.15, WALL_IN - 0.15, cz, cz + FOAM), (0.3, 0.3, 0.32), "buy")
ceil = bx(CEIL_X[1], CEIL_X[0], -WALL_IN + 0.05, WALL_IN - 0.05, cz + FOAM, cz + FOAM + T16)
top = cz + FOAM + T16
PIN_Z = top + 0.125                      # the pins' axis: the middle of the pin blocks
part(fixed, "ceiling (1/16 in polycarbonate; band-held down)", ceil, POLY, "cut")
part(fixed, f"ceiling_foam_glue (contact cement, or 3M 300LSE transfer tape: the foam to the ceiling)", bx(CEIL_X[1] + 0.1, CEIL_X[0] - 0.1, -WALL_IN + 0.2, WALL_IN - 0.2, cz + FOAM - 0.01, cz + FOAM), (0.9, 0.9, 0.5), "buy")
for s in (-1, 1):
    nm = 'L' if s > 0 else 'R'
    y0, y1 = sorted((s * WO, s * (WO + 0.25)))
    for i, tx in enumerate(CEIL_PINS):
        fr = 'front' if i == 0 else 'rear'
        pn = f"ceiling_post_{fr}_{nm} (print, PETG, on the wall's outer face; vertical slot for the ceiling's pin, a hook on top for its band; M4 heat-set inserts)"
        post = bx(tx - 0.25, tx + 0.25, y0, y1, WALL_Z[1] - 0.6, PIN_Z + CEIL_TRAVEL + 0.13)   # under the raised 11-hole channel
        post = post.cut(bx(tx - 0.07, tx + 0.07, -3, 3, PIN_Z - 0.07, PIN_Z + CEIL_TRAVEL + 0.07))
        post = post.union(cyly(tx, PIN_Z + CEIL_TRAVEL - 0.05, 0.16, s * (WO + 0.25), s * (WO + 0.45)))   # the band's hook, on its outer face
        part(fixed, pn, post, BLUE, "print")
        # the pin block: printed, on the ceiling's edge, reaching out over the wall to the post; the pin threads into its end
        b0, b1 = sorted((s * (WALL_IN - 0.35), s * (WO - 0.01)))
        blk = bx(tx - 0.2, tx + 0.2, b0, b1, top, top + 0.25)
        part(fixed, f"ceiling_pin_block_{fr}_{nm} (print, PETG: on the ceiling's edge; an M3 heat-set insert in its end for the pin, two below for its screws)", blk, BLUE, "print")
        part(fixed, f"ceiling_pin_{fr}_{nm} (M3 shoulder screw, 4 mm shoulder x 10 mm, through the post's slot into the pin block)", cyly(tx, PIN_Z, 4 * MM, s * (WO - 0.01), s * (WO + 0.25 + 0.04)).union(cyly(tx, PIN_Z, 3 * MM, s * (WO - 0.01), s * (WO - 0.01 - 0.2))).union(cyly(tx, PIN_Z, 7 * MM, s * (WO + 0.29), s * (WO + 0.29 + 0.1))), STEEL, "buy")
        c = bolt(fixed, f"ceiling_block_{fr}_{nm}", "the pin block to the ceiling (from below; the foam is cut away round the heads)", [(tx + dx, s * (WALL_IN - 0.2), top - T16) for dx in (-0.1, 0.1)], (0, 0, 1), T16 * IN, d=3, nut=False, tapped=5.7, min_engage=4, into=f"ceiling_pin_block_{fr}_{nm}", through=("ceiling (",))
        drill(fixed, ["ceiling ("], c)
        for dx in (-0.1, 0.1):                  # the foam is cut away round the heads
            relief = cylz(tx + dx, s * (WALL_IN - 0.2), 8 * MM, cz - 0.01, cz + FOAM + 0.01)
            k_ = next(k for k in fixed if k.startswith("ceiling_foam ("))
            wp_, col_, kind_ = fixed[k_]; fixed[k_] = (wp_.cut(relief), col_, kind_)
    heads = [(tx, s * WALL_IN, WALL_Z[1] - 0.3) for tx in CEIL_PINS]
    c = bolt(fixed, f"ceiling_post_{nm}", f"the ceiling's posts to the {nm} wall (flat heads, flush inside the lane, into heat-set inserts)", heads, (0, s, 0), WALL_T * IN, nut=False, tapped=10, into=f"ceiling_post_", through=(f"lane_wall_{nm}",), flat=True)
    drill(fixed, [f"lane_wall_{nm}"], c)

# ---- the feeder: one driven, fixed, on the left; a sprung foam pad on the right ----
# The feeder's shaft runs in two flanged bearings in small 1/4 in aluminium plates bolted to the mentor's launcher
# channels' existing holes (their own bearing holes miss its axis: his channels lean 5.4 deg): the rear plate inside the
# rear channel, on its web; the front one on the front of the front 3-hole channel's web, hanging below it. A bolt-on
# module: belt off, four plate screws, and it drops out.
y = LANE_Y + FEED_Y
U, V = (0.0944, 0.9955), (0.9955, -0.0944)        # the launcher channels' along and across directions (Y, z), as they lean
def lat(o, a, b): return (o[0] + (a * U[0] + b * V[0]) * MM, o[1] + (a * U[1] + b * V[1]) * MM)
REAR_O, FRONT_O = (3.286, 2.907), (3.464, 4.794)   # a 14 mm pattern hole on each channel (Y, z)
REAR_WEB, FRONT_WEB = (-3.99, -4.09), (-0.31, -0.41)   # the webs' faces, X (toward the feeder, away)
PL_T = 0.25
REAR_WEB_T, FRONT_WEB_T = 2.54, 2.54
# rear: two screws from behind the rear channel's web into the plate's tapped holes (the feeder blocks the front);
# front: two screws and nuts through the front channel's outboard holes, clear of the sonic hub the mentor has behind
# its web at its lowest pattern (the plate clears that hub's four screw heads)
FB = {"rear": (REAR_WEB[0], REAR_WEB[0] + PL_T, [lat(REAR_O, -8, -8), lat(REAR_O, 12, 0)], []),
      "front": (FRONT_WEB[0], FRONT_WEB[0] + PL_T, [lat(FRONT_O, 16, 16), lat(FRONT_O, 40, 16)], FS_HOLES)}
HUB_SCREWS = [lat(FRONT_O, a, b) for a in (-8, 8) for b in (-8, 8)]
def plate_outline(pts, x0, x1, r):
    """A plate round a set of points (Y, z): the convex hull of r-radius circles about them."""
    import itertools
    pl = None
    for p_ in pts:
        c_ = cylx(*p_, 2 * r, x0, x1); pl = c_ if pl is None else pl.union(c_)
    for a, b in itertools.combinations(pts, 2):
        ln = math.hypot(b[0] - a[0], b[1] - a[1]); nn = ((a[1] - b[1]) / ln * r, (b[0] - a[0]) / ln * r)
        pl = pl.union(yz([(a[0] + nn[0], a[1] + nn[1]), (b[0] + nn[0], b[1] + nn[1]), (b[0] - nn[0], b[1] - nn[1]), (a[0] - nn[0], a[1] - nn[1])], x0, x1))
    for a, b, c_ in itertools.combinations(pts, 3):
        if abs((b[0] - a[0]) * (c_[1] - a[1]) - (b[1] - a[1]) * (c_[0] - a[0])) > 1e-4: pl = pl.union(yz([a, b, c_], x0, x1))
    return pl
for nm, (x0, x1, holes, extra) in FB.items():
    pl = plate_outline([(y, FEED_Z)] + holes + extra, x0, x1, 0.3).cut(cylx(y, FEED_Z, 14 * MM, x0 - 0.1, x1 + 0.1))
    if nm == "front":
        pl = pl.cut(cylx(FLY_Y[0], FLY_Z, 18 * MM, x0 - 0.1, x1 + 0.1))   # clear of the left flywheel shaft's bearing, proud of the web
        for h in HUB_SCREWS: pl = pl.cut(cylx(*h, 8.5 * MM, x0 - 0.1, x1 + 0.1))   # and of the hub's screw heads
    part(fixed, f"feeder_bearing_plate_{nm} (1/4 in aluminium, on the launcher channel's web)", pl, ALU, "cut")
    part(fixed, f"feeder_bearing_{nm} (goBILDA 1611-0514-4008 flanged bearing)", cylx(y, FEED_Z, 14 * MM, x1 - 4.2 * MM, x1).union(cylx(y, FEED_Z, 15 * MM, x1, x1 + 0.8 * MM)), STEEL, "buy")
    vendor(f"feeder_bearing_{nm} (goBILDA 1611-0514-4008 flanged bearing)", "1611-0514-4008.STEP", (0, 4.8, 0), (0, -1, 0), (1, 0, 0), (x1 + 0.8 * MM, y, FEED_Z), (-1, 0, 0), (0, 1, 0))
    if nm == "rear":
        c = bolt(fixed, "feeder_plate_rear", "the feeder's rear bearing plate, from behind the rear channel's web into its tapped holes", [(REAR_WEB[1], *h) for h in holes], (1, 0, 0), REAR_WEB_T, nut=False, tapped=PL_T * IN, min_engage=5, into="feeder_bearing_plate_rear")
    else:
        c = bolt(fixed, "feeder_plate_front", "the feeder's front bearing plate to the front channel's web (nuts behind it)", [(x1, *h) for h in holes], (-1, 0, 0), PL_T * IN + FRONT_WEB_T, nut=True, through=(f"feeder_bearing_plate_{nm}",))
        drill(fixed, [f"feeder_bearing_plate_{nm}"], c)
    if extra:
        c = bolt(fixed, f"feeder_servo_standoffs", "the feeder servo's standoffs to the front bearing plate (flat heads from behind, before the plate goes on)", [(x0, *h) for h in extra], (1, 0, 0), PL_T * IN, nut=False, tapped=10, into="feeder_servo_standoff_", through=(f"feeder_bearing_plate_{nm}",), flat=True, service="before the plate goes on")
        drill(fixed, [f"feeder_bearing_plate_{nm}"], c)
part(fixed, "feeder (goBILDA 3632-0014-0072 72 mm Gecko x2, softest durometer)", cylx(y, FEED_Z, 2 * RF, FEED_X[0], FEED_X[1]), GREEN, "buy")
x_rear = REAR_WEB[0] + 0.02                         # the shaft's rear end, just clear of the rear channel's web
FEED_SHAFT_L = 120
part(fixed, "feeder_shaft (goBILDA 2106-4008-1200, 8mm REX, 120 mm, e-clip at the front)", cylx(y, FEED_Z, 8 * MM, x_rear, x_rear + FEED_SHAFT_L * MM), STEEL, "buy")
part(fixed, "feeder_spacers_rear (goBILDA 8mm REX spacers, stacked: rear bearing to the wheels)", cylx(y, FEED_Z, 12 * MM, FB["rear"][1] + 0.8 * MM, FEED_X[0]), STEEL, "buy")
FCOL = (FB["front"][0] - 10.3 * MM, FB["front"][0])   # a clamping collar against the front bearing: with the e-clip ahead, it holds the shaft both ways
part(fixed, "feeder_spacers_front (goBILDA 8mm REX spacers, stacked: the wheels to the collar)", cylx(y, FEED_Z, 12 * MM, FEED_X[1], FCOL[0]), STEEL, "buy")
part(fixed, "feeder_shaft_collar (goBILDA 2910-1020-4008, 8mm REX clamping collar)", cylx(y, FEED_Z, 20 * MM, *FCOL), STEEL, "buy")
part(fixed, "feeder_spacers_mid (goBILDA 8mm REX spacers: the front bearing to the pulley)", cylx(y, FEED_Z, 12 * MM, FB["front"][1] + 0.8 * MM, FD_PUL_X[0]), STEEL, "buy")
# the feeder's servo, straight above the shaft, spline pointing back; its hub and hub-mount pulley in the belt's plane
FSN = "feeder_servo (goBILDA 2000-0025-0004 Super Speed, continuous rotation)"
FS_TOP = FD_PUL_X[1] + 7.5 * MM                     # the servo's case top, X: the pulley, then the 1910 hub's 7 mm
part(fixed, FSN, place_local(servo_local(), (FS_TOP, FS_Y, FS_Z), (-1, 0, 0), (0, *FS_LONG)), BLACK, "buy")
vendor(FSN, "2000-0025-0002.step", (-10.0, 0.0, 12.8), (0, 0, 1), (1, 0, 0), (FS_TOP, FS_Y, FS_Z), (-1, 0, 0), (0, *FS_LONG))
part(fixed, "feeder_servo_hub (goBILDA 1910-0025-0816 servo hub)", cylx(FS_Y, FS_Z, 32 * MM, FD_PUL_X[1], FS_TOP - 0.5 * MM), ALU, "buy")
part(fixed, "feeder_servo_pulley (goBILDA 3411-0014-0024, 24T HTD5 hub-mount)", cylx(FS_Y, FS_Z, 38.8 * MM, *FD_PUL_X).cut(cylx(FS_Y, FS_Z, 14 * MM, FD_PUL_X[0] - 0.1, FD_PUL_X[1] + 0.1)), BLACK, "buy")
c = bolt(fixed, "feeder_hub_pulley", "the 24T hub-mount pulley to the servo hub (4 mm of the hub's 7 mm thread)", [(FD_PUL_X[0], FS_Y + dy * MM, FS_Z + dz * MM) for dy in (-8, 8) for dz in (-8, 8)], (1, 0, 0), 12, nut=False, tapped=7, min_engage=4, into="feeder_servo_hub", through=("feeder_servo_pulley",), service="before the servo goes on")
drill(fixed, ["feeder_servo_pulley"], c)                   # (the real pulley's pattern holes)
FS_TAB = FS_TOP + 12.8 * MM                          # the tabs' face toward the plate, X
for k, (hy, hz) in enumerate(FS_HOLES):
    part(fixed, f"feeder_servo_standoff_{k} (goBILDA 1501-0006-0340, M4 standoff, 34 mm)", cylx(hy, hz, 6 * MM, -0.06, FS_TAB), STEEL, "buy")
bolt(fixed, "feeder_servo_tabs", "the feeder servo's tabs to its standoffs", [(FS_TAB + 2.5 * MM, hy, hz) for hy, hz in FS_HOLES], (-1, 0, 0), 2.5, nut=False, tapped=10, into="feeder_servo_standoff_", through=("feeder_servo (",))
part(fixed, "feeder_pulley (goBILDA 3417-4008-0024, 24T HTD5, 8mm REX)", cylx(y, FEED_Z, 38.8 * MM, *FD_PUL_X), BLACK, "buy")
part(fixed, "feeder_eclip (included with the shaft)", cylx(y, FEED_Z, 12 * MM, x_rear + FEED_SHAFT_L * MM - 0.04, x_rear + FEED_SHAFT_L * MM - 0.01), STEEL, "buy")
part(fixed, "feeder_shaft_spacer (goBILDA 8mm REX spacers: the pulley to the e-clip)", cylx(y, FEED_Z, 12 * MM, FD_PUL_X[1], x_rear + FEED_SHAFT_L * MM - 0.04), STEEL, "buy")
part(fixed, f"feeder_belt (goBILDA {FEED_BELT[1]}, HTD5 9 mm, {FEED_BELT[0]} mm; fixed centres {FEED_C * IN:.1f} mm)", loop("YZ", (y, FEED_Z), P24 / 2, (FS_Y, FS_Z), P24 / 2, 0.14, 9 * MM, BELT_X - 4.5 * MM), BLACK, "buy")
# the pad: 1/8 in aluminium plate, foam on its inner face, hinged along X at its bottom, banded inward against a stop
px0, px1 = FEED_X[0] + 0.05, FEED_X[1] - 0.05
part(fixed, f"pad_foam ({PAD_FOAM} in soft foam, as the ceiling's)", bx(px0, px1, PAD_FACE - PAD_FOAM, PAD_FACE, PAD_HINGE[1] + 0.55, PAD_TOP), (0.3, 0.3, 0.32), "buy")
PLY = (PAD_FACE - PAD_FOAM - 0.125, PAD_FACE - PAD_FOAM)   # the plate's faces, Y
pad = bx(px0, px1, *PLY, FLOOR_Z + 0.05, PAD_TOP)          # its foot reaches below the hinge: the stop works on it there
part(fixed, "pad_plate (1/8 in aluminium; hinged at its foot, a band at its top pulls it in)", pad, ALU, "cut")
part(fixed, f"pad_foam_glue (contact cement, or 3M 300LSE transfer tape: the foam to the plate)", bx(px0 + 0.05, px1 - 0.05, PLY[1], PLY[1] + 0.01, PAD_HINGE[1] + 0.6, PAD_TOP - 0.05), (0.9, 0.9, 0.5), "buy")
PH_L = 64
part(fixed, f"pad_hinge (goBILDA 2106-4008-{PH_L * 10:04d}, 8mm REX, {PH_L} mm, e-clips: turns in the blocks' round holes)", cylx(*PAD_HINGE, 8 * MM, (px0 + px1) / 2 - PH_L * MM / 2, (px0 + px1) / 2 + PH_L * MM / 2), STEEL, "buy")
KN = {"rear": (px0, px0 + 0.4), "front": (px1 - 0.4, px1)}
KN_Z = (PAD_HINGE[1] - 0.22, PAD_HINGE[1] + 0.4)
for nm, (k0, k1) in KN.items():               # the knuckles: on the rod (REX bore, so they turn with it), bolted to the plate's foot
    kn = bx(k0, k1, PAD_HINGE[0] - 0.22, PLY[0], *KN_Z).cut(rex_x(*PAD_HINGE, k0 - 0.1, k1 + 0.1))
    part(fixed, f"pad_knuckle_{nm} (print, PETG: on the hinge rod, bolted to the pad's foot; M4 heat-set inserts)", kn, BLUE, "print")
    c = bolt(fixed, f"pad_knuckle_{nm}", f"the pad plate to its {nm} knuckle (from the plate's inner face, below the foam)", [(kx, PLY[1], KN_Z[1] - 0.09) for kx in (k0 + 0.1, k1 - 0.1)], (0, -1, 0), 3.2, nut=False, tapped=10, into=f"pad_knuckle_{nm}", through=("pad_plate",))
    drill(fixed, ["pad_plate"], c)
HB = {}
for hx in (px0 - 0.3, px1 + 0.05):
    nm = 'rear' if hx < px0 else 'front'
    HB[nm] = hx + 0.125
    part(fixed, f"pad_hinge_block_{nm} (print, PETG, on the feeder floor; the rod turns in its round hole; M4 heat-set insert beside the rod)", bx(hx, hx + 0.25, PAD_HINGE[0] - 0.35, PAD_HINGE[0] + 0.3, FLOOR_Z, PAD_HINGE[1] + 0.3).cut(cylx(*PAD_HINGE, 8.3 * MM, hx - 0.1, hx + 0.4)), BLUE, "print")
for e in (-1, 1):                              # the rod's e-clips, outside the blocks
    xe = (px0 + px1) / 2 + e * (PH_L * MM / 2 - 0.04)
    part(fixed, f"pad_hinge_eclip_{'rear' if e < 0 else 'front'} (included with the rod)", cylx(*PAD_HINGE, 12 * MM, xe - 0.02, xe + 0.02), STEEL, "buy")
# the stop: outboard of the front knuckle, below the hinge. The band pulls the pad's top in, so the knuckle's foot swings
# out onto the stop; a NECTAR pushes the top out and the knuckle lifts off it. Its screw's slot (+-0.1 in, across) sets
# the squeeze.
PSX = KN["front"]
PS_Y = (PAD_HINGE[0] - 0.22 - 0.01 - 0.28, PAD_HINGE[0] - 0.22 - 0.01)
ps = bx(*PSX, *PS_Y, FLOOR_Z, FLOOR_Z + 0.3)
part(fixed, "pad_stop (print, PETG: under the pad's foot, outboard; its screw's slot in the floor, +-0.1 in across, sets the POLLEN squeeze; M4 heat-set insert)", ps, BLUE, "print")

# under the ball between the feeder and the pad: a floor (1/8 in polycarbonate) on a printed bridge bolted behind the
# launcher's two rear channels; the backstop (printed, an L) on slots on the floor sets the ball on the column
FLOOR_T = 0.125
BR_X = (REAR_WEB[1] - 0.31, REAR_WEB[1])            # the bridge's upright, behind the rear channels' webs
SHELF_X = (BR_X[0], -3.55)
SHELF_Z = (FLOOR_Z - FLOOR_T - 0.35, FLOOR_Z - FLOOR_T)
REAR_O_R = (2 * 0.157 - REAR_O[0], REAR_O[1])     # the right rear channel's pattern hole: the left one mirrored about the turret's axis
def lat_r(a, b): return (REAR_O_R[0] - (a * U[0] + b * V[0]) * MM, REAR_O_R[1] + (a * U[1] + b * V[1]) * MM)
BR_HOLES = [lat(REAR_O, -16, b) for b in (16, -16)] + [lat_r(-16, b) for b in (16, -16)]   # clear of the rear bearing plate
bridge = bx(*SHELF_X, -2.55, 1.35, *SHELF_Z)
for side in (BR_HOLES[:2], BR_HOLES[2:]):          # two towers, one behind each channel (the backstop's foot slides between them)
    ys_ = [h[0] for h in side]
    bridge = bridge.union(bx(*BR_X, min(ys_) - 0.25, max(ys_) + 0.25, SHELF_Z[0], max(h[1] for h in side) + 0.25))
    bridge = bridge.union(bx(*BR_X, min(ys_) - 0.25 if ys_[0] > 0 else -2.55, max(ys_) + 0.25 if ys_[0] < 0 else 1.35, *SHELF_Z))
for h in FB["rear"][2]: bridge = bridge.cut(cylx(*h, 8.5 * MM, BR_X[0] - 0.1, BR_X[1] + 0.1))   # the rear plate's screw heads, and a key through
part(fixed, "feeder_bridge (print, PETG or nylon: behind the launcher's rear channels, its shelf under the feeder floor; M4 heat-set inserts)", bridge, BLUE, "print")
bolt(fixed, "feeder_bridge", "the feeder bridge to the rear channels' webs (from inside the channels)", [(REAR_WEB[0], *h) for h in BR_HOLES], (-1, 0, 0), REAR_WEB_T, nut=False, tapped=10, into="feeder_bridge", service="with the feeder out (its two bearing plates)")
FL_X = (SHELF_X[0] + 0.05, FEED_X[1] + 0.15)
fl = bx(*FL_X, -2.5, 1.3, FLOOR_Z - FLOOR_T, FLOOR_Z)
part(fixed, "feeder_floor (1/8 in polycarbonate, between the feeder and the pad)", fl, POLY, "cut")
BS_FOOT = (BACKSTOP_X - 0.5, BACKSTOP_X)
bs = bx(BACKSTOP_X - 0.125, BACKSTOP_X, -1.3, 1.3, FLOOR_Z, 3.6).union(bx(*BS_FOOT, -1.3, 1.3, FLOOR_Z, FLOOR_Z + 0.15))
part(fixed, "backstop (print, PETG, an L: its foot's two slots, +-0.2 in, set the 4-piece / 3-NECTAR limit; tune it on the robot)", bs, BLUE, "print")
BS_SCREWS = [((BS_FOOT[0] + BS_FOOT[1]) / 2 - 0.05, yy, FLOOR_Z + 0.15) for yy in (-0.8, 0.8)]
c = bolt(fixed, "backstop", "the backstop's foot through the floor into the bridge's shelf (slotted)", BS_SCREWS, (0, 0, -1), 0.15 * IN + FLOOR_T * IN, nut=False, tapped=8, into="feeder_bridge", through=("backstop", "feeder_floor"))
drill(fixed, ["feeder_floor"], c)
for bx_, by_, bz_ in BS_SCREWS:                # the foot's slots: +-0.2 in along X
    k_ = next(k for k in fixed if k.startswith("backstop ("))
    wp_, col_, kind_ = fixed[k_]; fixed[k_] = (wp_.cut(bx(bx_ - 0.2 - 2.2 * MM, bx_ + 0.2 + 2.2 * MM, by_ - 2.2 * MM, by_ + 2.2 * MM, FLOOR_Z - 0.1, FLOOR_Z + 0.3)), col_, kind_)
c = bolt(fixed, "pad_blocks", "the pad's hinge blocks to the floor (from below, into heat-set inserts)", [(HB['rear'], PAD_HINGE[0] - 0.25, FLOOR_Z - FLOOR_T), (HB['front'], PAD_HINGE[0] - 0.25, FLOOR_Z - FLOOR_T)], (0, 0, 1), FLOOR_T * IN, nut=False, tapped=10, into="pad_hinge_block", through=("feeder_floor",))
drill(fixed, ["feeder_floor"], c)
PS_SCREW = ((PSX[0] + PSX[1]) / 2, (PS_Y[0] + PS_Y[1]) / 2, FLOOR_Z - FLOOR_T)
bolt(fixed, "pad_stop", "the pad stop to the floor (from below, through a slot in the floor, into its insert)", [PS_SCREW], (0, 0, 1), FLOOR_T * IN, nut=False, tapped=7, min_engage=4, into="pad_stop", through=("feeder_floor",))
k_ = next(k for k in fixed if k.startswith("feeder_floor ("))
wp_, col_, kind_ = fixed[k_]
fixed[k_] = (wp_.cut(bx(PS_SCREW[0] - 2.2 * MM, PS_SCREW[0] + 2.2 * MM, PS_SCREW[1] - 0.1 - 2.2 * MM, PS_SCREW[1] + 0.1 + 2.2 * MM, FLOOR_Z - 0.2, FLOOR_Z + 0.1)), col_, kind_)

# ---- the launcher changes: the flywheel motors move out and up (his old place for them is where the feeder and pad go) ----
for s in (1, -1):
    nm = 'L' if s > 0 else 'R'; my, mz = FM[s]; fy = FLY_Y[0] if s > 0 else FLY_Y[1]
    mn = f"flywheel_motor_{nm} (the mentor's 312 RPM Yellow Jacket, moved: along X outboard of the launcher frame)"
    part(launcher, mn, cylx(my, mz, MOTOR_D, FM_FACE, FM_FACE + 116.5 * MM), (0.95, 0.75, 0.2), "buy")
    vendor(mn, "5203-2402-0019 assembly.STEP", (-37.15, 101.7, -11.05), (0, 1, 0), (1, 0, 0), (FM_FACE, my, mz), (-1, 0, 0), (0, 0, 1))
    part(launcher, f"flywheel_motor_pulley_{nm} (goBILDA 3417-4008-0016, 16T HTD5)", cylx(my, mz, 28 * MM, FLY_BELT_X - 6 * MM, FLY_BELT_X + 6 * MM), BLACK, "buy")
    part(launcher, f"flywheel_belt_{nm} (goBILDA {FLY_BELT[s][1]}, HTD5 9 mm, his 41T to the moved motor)", loop("YZ", (fy, FLY_Z), P41 / 2, (my, mz), P16 / 2, 0.14, 9 * MM, FLY_BELT_X - 4.5 * MM), BLACK, "buy")
    # the bracket: a 1/8 in aluminium L, its foot on the side channel's top flange, its upright holding the motor's face
    f0, f1 = sorted(SIDE_FLANGE[s]); xb0, xb1 = FM_FACE - 0.125, FM_FACE
    yo = my + s * (QB / 2)
    up_y0, up_y1 = sorted((s * min(abs(f0), abs(f1)), yo))
    upright = bx(xb0, xb1, up_y0, up_y1, SIDE_TOP, mz + QB / 2)
    foot = bx(xb0, xb0 + 0.9, f0, f1, SIDE_TOP, SIDE_TOP + 0.125)
    br = upright.union(foot).cut(cylx(my, mz, 16.5 * MM, xb0 - 0.1, xb1 + 0.1))
    bn = f"flywheel_motor_bracket_{nm} (1/8 in aluminium L, bent; on the launcher frame's side channel)"
    part(launcher, bn, br, ALU, "cut")
    c = bolt(launcher, f"fly_face_{nm}", f"the {nm} flywheel motor to its bracket", [(xb0, my + dy * MM, mz + dz * MM) for dy in (-8, 8) for dz in (-8, 8)], (1, 0, 0), 3.2, nut=False, tapped=10.5, into=f"flywheel_motor_{nm} \\(", through=(f"flywheel_motor_bracket_{nm}",), service="before the pulley")
    drill(launcher, [f"flywheel_motor_bracket_{nm}"], c)
    c = bolt(launcher, f"fly_bracket_{nm}", f"the {nm} flywheel motor bracket to the side channel's top flange", [(-0.149 - k * 8 * MM, SIDE_FLANGE_HOLES[s], SIDE_TOP + 0.125) for k in (6, 7)], (0, 0, -1), 3.2 + SIDE_FLANGE_T, nut=True, through=(f"flywheel_motor_bracket_{nm}",))
    drill(launcher, [f"flywheel_motor_bracket_{nm}"], c)

# ---- the holes the screws thread into: a heat-set insert's hole in a printed part, a tap drill in a cut one ----
def threaded_holes(d):
    import re as _re
    for sn, h, a, L, grip, dd, into in THREADS:
        n_ = math.sqrt(sum(c * c for c in a)); a = tuple(c / n_ for c in a)
        p0 = tuple(h[k] + a[k] * (grip - 0.2) * MM for k in range(3)); ln = (L - grip + 1.0) * MM
        for k in list(d):
            wp_, col_, kind_ = d[k]
            if kind_ not in ("print", "cut") or not _re.search(into, k): continue
            dia = ({4: 5.6, 3: 4.0}[dd] if kind_ == "print" else {4: 3.3, 3: 2.5}[dd]) * MM
            hole = cq.Workplane().add(cq.Solid.makeCylinder(dia / 2, ln, cq.Vector(*p0), cq.Vector(*a)))
            d[k] = (wp_.cut(hole), col_, kind_)
threaded_holes(fixed); threaded_holes(launcher)

# ---- into the robot CAD's millimetres: (x, y, z)_CAD = (C + Y, F + Z, FACE - 7.56 in + X), all times 25.4 ----
MAT = cq.Matrix([[0, IN, 0, C], [0, 0, IN, F], [IN, 0, 0, FACE - CENTRE_BACK_IN * IN]])
def to_cad(wp):
    shp = wp.val() if len(wp.vals()) == 1 else cq.Compound.makeCompound(wp.vals())
    return shp.transformGeometry(MAT)
GROUPS = (("transfer, fixed", "fixed", fixed), ("launcher changes (the mentor's to agree)", "launcher", launcher))
if __name__ == "__main__":
    out = HERE; os.makedirs(out + "/stl", exist_ok=True)
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
    print(f"feeder belt: centres {FEED_C * IN:.2f} mm, needs {belt_len(FEED_C, P24, P24) * IN:.1f} mm, belt {FEED_BELT[0]}")
    for s in (1, -1):
        fy = FLY_Y[0] if s > 0 else FLY_Y[1]; c = math.hypot(FM[s][0] - fy, FM[s][1] - FLY_Z)
        print(f"flywheel belt {'L' if s > 0 else 'R'}: motor at Y {FM[s][0]:.3f} z {FM[s][1]:.3f}, needs {belt_len(c, P41, P16) * IN:.1f} mm, belt {FLY_BELT[s][0]}")
