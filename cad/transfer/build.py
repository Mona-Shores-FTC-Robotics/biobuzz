"""The intake-to-launcher transfer, v4 (8 Oct 2026): a ramp, a flat lane of small compliant rollers under a flat sprung
ceiling, and one driven feeder against a sprung pad under the flywheels that drives each ball straight up into them.
Built from goBILDA parts wherever they fit, every screw drawn. Drawn in the robot CAD's frame, so dhs-transfer.step lands
in place in Onshape next to cad/intake-b/dhs-intake-b.step.

    python3 cad/transfer/build.py        # writes dhs-transfer.step and stl/ next to this file

Robot frame: +X forward, +Y left, +Z up, inches, origin on the floor under the chassis centre (7.56 in behind the front
face), moved into the CAD's millimetres at the end. Groups: "fixed" (ramp, lane, ceiling, feeder, pad and their drives)
"launcher" (the changes in the mentor's launcher: the flywheel motors moved out and up) and "elec" (the
electronics bay at the back: the battery, both hubs and the switch on one bent plate; README "Electronics bay").

How it works: the intake roller pushes each ball up the ramp onto the lane. The lane's rollers carry it back under the
ceiling, which presses it onto them, and push it in between the feeder and the pad, under the flywheels, against the
backstop. Stopped, the feeder holds it there, below the flywheels' reach; the rest queue behind it. Feeding runs the
feeder: it drives the ball, pinched against the pad, straight up into the flywheels.

Drives (plan A, README "Drives"): no motor of its own. The intake roller's motor also runs the lane, through a goBILDA
round belt from a pulley on the roller and a goBILDA gear pair that turns it the lane's way; a fixed idler keeps the belt's
length within 3% as the roller floats. The feeder is belted to the left flywheel's shaft, so it spins whenever the
flywheels do, and swings on two arms about that shaft. A servo is the gate: it holds the feeder swung out, clear of the
waiting ball; swung in, it pinches the ball against the sprung pad and drives it up.
The launcher changes: the flywheel motors move out and up, and become goBILDA 6000 RPM (1:1) Yellow Jackets.
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
FEED_IN = 1.45                          # the feeder's tread, inner edge, Y, swung in (feeding): a POLLEN presses 0.10 into it and centres at
                                        # Y 0.15; a NECTAR presses 0.15, centres at -0.21
FEED_OUT = 2.05                         # swung out (waiting): it spins all the time, and clears a waiting NECTAR by 0.45, a POLLEN on the wall by 0.50 (tools/robot-cad/transfer2_check.py)
FEED_Y = FEED_IN + RF                   # its axle's Y, swung in (2.87)
P_C, N_C = FEED_IN + 0.10 - RP, FEED_IN + 0.15 - RN     # a pinched POLLEN's and NECTAR's centres, Y
# it swings on two arms about the left flywheel's shaft, which drives it by a belt: its axle stays FEED_C from that shaft
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
# the lane's drive: from the intake roller (cad/intake-b), whose motor (1150 RPM) now runs the roller and the lane. A
# printed pulley on the roller (20 mm pitch diameter, in a gap in its left vector wheels) drives a goBILDA 5 mm round belt
# round a printed 24 mm pulley on a jackshaft under the ramp and a goBILDA idler hung from the old intake's 9-hole channel.
# The jackshaft turns the way the roller does; a pair of goBILDA 24T pinions turns lane shaft 0 the other way, as the lane
# needs. The idler sits where the belt's length changes least as the roller floats (3%, inside its stretch).
# The lane shafts turn at 1150 x 20 / 24 = 958 RPM: 47 in/s of roller tread, about 24 in/s of ball.
LD_Y = 3.65                             # the round belt's plane, Y: the roller's gap is 3.4-3.9 (cad/intake-b ROLL_GAP)
LD_RR, LD_RJ, LD_RI = 10 * MM, 12 * MM, 8 * MM      # pitch radii: the roller's pulley, the jackshaft's, the idler's (goBILDA 3401)
LD_CORD = 5 * MM
GEAR_C = 19.2 * MM                      # two 24T mod 0.8 pinions' centres
_ja = math.radians(-5)                  # the jackshaft: ahead of lane shaft 0 and a little lower, under the ramp
IDLER = (6.35, 5.40)                    # (X, z): under the 9-hole channel's web (z 6.22), its hanger on four of the web's holes
CHAN_WEB_Z = (6.219, 6.317)             # that channel's web: it opens downward, its flanges at X 5.66-5.76 and 7.45-7.55
CHAN_HOLES = [(6.285, 3.572), (6.934, 3.572), (6.285, 4.222), (6.934, 4.222)]   # 4 mm holes in its web (X, Y), measured from his CAD
ROLLER_AXLE = (7.56 + 0.061 + 1.0, 3.4) # the intake roller's axle at rest (X, z); it rises 1.3
LD_BELT = (334, "3405-0005-0334")       # goBILDA round belt, 5 mm, 334 mm: stretched 5.3-8.5% on this loop as the roller floats
# pulleys: printed, two 3/16 in polycord grooves on every lane shaft outside the right wall (one part); the shafts chain
# in pairs on grooves a and b, from shaft 0
PUL_Y0, PUL_L = -(WO + 0.04), 11.5 * MM            # from the bearing's flange outward
GROOVE = [PUL_Y0 - (3 + 5.5 * k) * MM for k in range(2)]
PUL_PD = 16 * MM                                   # the grooves' pitch diameter
# the feeder's drive: belted to the left flywheel's shaft, so it spins whenever the flywheels do, and swings on two arms
# about that shaft (the belt's centres never change). His 96 mm shaft becomes a
# goBILDA 2106-4008-1440 (144 mm), out through the front channel's bearing; a 3417-4008-0016 16T on it drives a
# 3417-4008-0024 24T on the feeder's shaft on a goBILDA 3412-0009-0275 belt (fixed centres: the feeder's height is set by
# them). The feeder's tread runs at half the flywheels' surface speed (2341 x 16 / 24 = 1561 RPM, 232 in/s).
P24 = 24 * 5 / math.pi / 25.4
MOTOR_D = 37 * MM
QB = 43 * MM                                       # a goBILDA quad block: sizes the flywheel motor bracket's upright
FEED_BELT = (275, "3412-0009-0275")
FD_PUL_X = (0.97, 0.97 + 12 * MM)                  # the belt's plane: ahead of his 8-hole channel (X 0.79) and the arm's servo
BELT_X = sum(FD_PUL_X) / 2
P16, P41 = 16 * 5 / math.pi / 25.4, 41 * 5 / math.pi / 25.4
def belt_len(c, d0, d1):
    """An open belt's pitch length (inches) for centres c and pitch diameters d0, d1."""
    return 2 * c + math.pi * (d0 + d1) / 2 + (d1 - d0) ** 2 / (4 * c)
def centres_for(L, d0, d1):
    lo, hi = 1.0, 10.0
    for _ in range(80):
        c = (lo + hi) / 2
        lo, hi = (c, hi) if belt_len(c, d0, d1) < L else (lo, c)
    return c

# the flywheel motors, moved out and up (along X, facing back), each belted to his 41T pulley on a goBILDA belt length:
# 315 mm (left) and 320 mm (right); the centres are set for those
FLY_IN = 16 * MM                        # each flywheel module moves in two holes (16 mm) on the launcher frame: 145 mm axle to
                                        # axle (was 177), a 49 mm gap between the 96 mm wheels, as the prototype that shot both pieces
                                        # (144). One hole either way per side gives 129 or 161 mm (cad/full-robot moves his modules)
FLY_Y = (3.6427 - FLY_IN, -3.3276 + FLY_IN)   # the flywheels' axles, z 6.646
FLY_Z = 6.646
FLY_BELT = {1: (295, "3412-0009-0295"), -1: (295, "3412-0009-0295")}   # 16T on the motor, 24T on the flywheel (1.5:1: 4000 RPM
                                        # free, so 2400 leaves the motor a third of its speed to recover each shot)
def _fly_motor(s):
    fy = FLY_Y[0] if s > 0 else FLY_Y[1]; d = (s * 1.0, 0.115); n = math.hypot(*d); d = (d[0] / n, d[1] / n)
    L = FLY_BELT[s][0] * MM; lo, hi = 2.0, 5.0
    for _ in range(60):
        c = (lo + hi) / 2
        lo, hi = (c, hi) if belt_len(c, P24, P16) < L else (lo, c)
    return fy + d[0] * c, FLY_Z + d[1] * c
FM = {s: _fly_motor(s) for s in (1, -1)}
FEED_C = centres_for(FEED_BELT[0] * MM, P16, P24)  # the feeder belt's centres (3.44 in)
FEED_Z = FLY_Z - math.sqrt(FEED_C ** 2 - (FLY_Y[0] - FEED_Y) ** 2)   # the feeder's axle height, swung in (3.30): always 0.13 clear of the left flywheel
ARM_IN = math.atan2(FLY_Y[0] - FEED_Y, FLY_Z - FEED_Z)                # the arms' angle from straight down, swung in (feeding)
ARM_OUT = math.asin((FLY_Y[0] - FEED_OUT - RF) / FEED_C)              # and swung out (waiting): 10 deg apart
GRIP_TOP_P = FEED_Z + math.sqrt((RF + RP) ** 2 - (FEED_Y - P_C) ** 2)    # the feeder grips a POLLEN up to here (centre)
GRIP_TOP_N = FEED_Z + math.sqrt((RF + RN) ** 2 - (FEED_Y - N_C) ** 2)    # and a NECTAR; the flywheels take a NECTAR from 5.77
FLY_BELT_X = -3.20                      # the flywheels' 24T pulleys' belt plane (in place of his 41T pulleys)
FM_FACE = -2.65                         # the moved motors' faces, X (shafts pointing back)
SIDE_FLANGE = {1: (5.35, 5.83), -1: (-5.51, -5.04)}   # the launcher side channels' top flanges, Y; top face z 5.74
SIDE_FLANGE_HOLES = {1: 5.669, -1: -5.354}          # their 4 mm holes' line, Y; at X = -0.149 - 8 mm k
SIDE_FLANGE_T = 2.54
SIDE_TOP = 5.74

FEEDER_SPINS = r"^(feeder \(|feeder_shaft |feeder_pulley \(|feeder_spacers_|feeder_shaft_spacer |feeder_shaft_collar |feeder_eclip)"   # what turns with the feeder
FEEDER_SWINGS = r"^(feeder_arm_|feeder_bearing_|feeder_pivot_bearing_|gate_tab |gate_pin_arm )"   # what swings with its yoke (the feeder rides on it)
PAD_SWINGS = r"^pad_(plate|foam|foam_glue|knuckle_rear|knuckle_front|hinge|hinge_eclip_rear|hinge_eclip_front) "                 # what swings with the pad

fixed, launcher, elec = {}, {}, {}
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
    ext = (lambda wp: wp.extrude(w)) if plane in ("YZ", "XY") else (lambda wp: wp.extrude(-w))
    s = ext(cq.Workplane(plane).polyline(poly).close())
    for c, r in ((c0, r0), (c1, r1)): s = s.union(ext(cq.Workplane(plane).center(*c).circle(r)))
    return s.translate({"YZ": (at, 0, 0), "XZ": (0, at, 0), "XY": (0, 0, at)}[plane])
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
JACK = (ROLL_X[0] + GEAR_C * math.cos(_ja), ROLL_Z + GEAR_C * math.sin(_ja))   # (X, z): its bearings clear the ramp's slots
part(fixed, "ramp (1/16 in polycarbonate; its edges in the walls' slots)", sheet(RAMP_PTS, -WALL_IN - RAMP_SLOT + 0.01, WALL_IN + RAMP_SLOT - 0.01), POLY, "cut")

# ---- the lane walls: 1/4 in polycarbonate, on four REX standoffs each from the rail, low under the front drive motors ----
def wall(s):
    y0, y1 = sorted((s * WALL_IN, s * WO))
    w = xz([(WALL_X[0], WALL_Z[0]), (WALL_X[1], WALL_Z[0]), (WALL_X[1], WALL_Z[1]), (WALL_X[0], WALL_Z[1])], y0, y1)
    for x, z in [(x, ROLL_Z) for x in ROLL_X] + [JACK]: w = w.cut(cyly(x, z, 14 * MM, y0 - 0.1, y1 + 0.1))   # the shafts' flanged bearings, pressed in from outside
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
    if i == 0:                          # shaft 0 is longer on the left, for the drive's pinion (its right end as the others')
        part(fixed, f"lane_shaft_0 (goBILDA 2106-4008-1680, 8mm REX, 168 mm, e-clips)", cyly(x, ROLL_Z, 8 * MM, y_left - SHAFT_L * MM + 168 * MM, y_left - SHAFT_L * MM), STEEL, "buy")
    else:
        part(fixed, f"lane_shaft_{i} (goBILDA 2106-4008-1440, 8mm REX, 144 mm, e-clips)", cyly(x, ROLL_Z, 8 * MM, y_left, y_left - SHAFT_L * MM), STEEL, "buy")
    for s, nm in ((1, "L"), (-1, "R")):
        part(fixed, f"lane_bearing_{i}{nm} (goBILDA 1611-0514-4008 flanged bearing)", cyly(x, ROLL_Z, 14 * MM, s * BRG_IN, s * WO).union(cyly(x, ROLL_Z, 15 * MM, s * WO, s * (WO + 0.8 * MM))), STEEL, "buy")
        vendor(f"lane_bearing_{i}{nm} (goBILDA 1611-0514-4008 flanged bearing)", "1611-0514-4008.STEP", (0, 4.8, 0), (0, -1, 0), (1, 0, 0), (x, s * (WO + 0.8 * MM), ROLL_Z), (0, -s, 0), (1, 0, 0))
    pul = cyly(x, ROLL_Z, 19 * MM, PUL_Y0, PUL_Y0 - PUL_L)
    for g in GROOVE: pul = pul.cut(cyly(x, ROLL_Z, 22 * MM, g + 2.4 * MM, g - 2.4 * MM).cut(cyly(x, ROLL_Z, PUL_PD - 4.8 * MM, g + 3 * MM, g - 3 * MM)))
    part(fixed, f"lane_pulley_{i} (print, PETG, two 3/16 in polycord grooves, 8mm REX bore)", pul.cut(bore), BLUE, "print")
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

# ---- the lane's drive, from the intake roller (constants above): a goBILDA 5 mm round belt over the roller's pulley, the
#      idler and the jackshaft's pulley; two goBILDA 24T pinions from the jackshaft to lane shaft 0 ----
def pinion(x, z, y0, teeth_first=True):
    """A goBILDA 2303-4008-0024 (24T mod 0.8, 8mm REX, set screws): 6 mm of teeth (20.8 mm) and 8 mm of hub (15 mm),
    along Y from y0 outward (+Y), the teeth first."""
    t = cyly(x, z, 20.8 * MM, y0, y0 + 6 * MM); h = cyly(x, z, 15 * MM, y0 + 6 * MM, y0 + 14 * MM)
    return t.union(h)
GEAR_Y0 = WO + 0.8 * MM + 0.5 * MM                  # the pinions' teeth, just outside the bearings' flanges
GEAR_Y1 = GEAR_Y0 + 14 * MM
part(fixed, "lane_pinion_0 (goBILDA 2303-4008-0024, 24T mod 0.8 pinion, 8mm REX, set screws; on lane shaft 0)", pinion(ROLL_X[0], ROLL_Z, GEAR_Y0), STEEL, "buy")
part(fixed, "jack_pinion (goBILDA 2303-4008-0024, 24T mod 0.8 pinion, 8mm REX, set screws; on the jackshaft)", pinion(*JACK, GEAR_Y0), STEEL, "buy")
y_l0 = WO + 0.08 - SHAFT_L * MM + 168 * MM          # lane shaft 0's left end
part(fixed, "lane_shaft_spacer_0L (goBILDA 8mm REX spacers, stacked: the pinion to the e-clip)", cyly(ROLL_X[0], ROLL_Z, 12 * MM, GEAR_Y1, y_l0 - 0.06), STEEL, "buy")
part(fixed, "lane_eclip_0L (with the shaft)", cyly(ROLL_X[0], ROLL_Z, 12 * MM, y_l0 - 0.06, y_l0 - 0.03), STEEL, "buy")
JP_Y = (LD_Y - 6.35 * MM, LD_Y + 6.35 * MM)         # the jackshaft's pulley, in the belt's plane
JACK_L = 168
y_j1 = JP_Y[1] + 0.1; y_j0 = y_j1 - JACK_L * MM     # the jackshaft: left end just past its pulley
part(fixed, f"jack_shaft (goBILDA 2106-4008-{JACK_L * 10:04d}, 8mm REX, {JACK_L} mm, e-clips: under the ramp, across the lane)", cyly(*JACK, 8 * MM, y_j0, y_j1), STEEL, "buy")
for sgn, nm in ((1, "L"), (-1, "R")):
    part(fixed, f"jack_bearing_{nm} (goBILDA 1611-0514-4008 flanged bearing)", cyly(*JACK, 14 * MM, sgn * BRG_IN, sgn * WO).union(cyly(*JACK, 15 * MM, sgn * WO, sgn * (WO + 0.8 * MM))), STEEL, "buy")
    vendor(f"jack_bearing_{nm} (goBILDA 1611-0514-4008 flanged bearing)", "1611-0514-4008.STEP", (0, 4.8, 0), (0, -1, 0), (1, 0, 0), (JACK[0], sgn * (WO + 0.8 * MM), JACK[1]), (0, -sgn, 0), (1, 0, 0))
part(fixed, "jack_spacers_L (goBILDA 8mm REX spacers, stacked: the pinion to the pulley)", cyly(*JACK, 12 * MM, GEAR_Y1, JP_Y[0]), STEEL, "buy")
part(fixed, "jack_spacers_R (goBILDA 8mm REX spacers, stacked: the bearing to the e-clip)", cyly(*JACK, 12 * MM, -(WO + 0.8 * MM), y_j0 + 0.06), STEEL, "buy")
for e, yy in (("L", y_j1 - 0.06), ("R", y_j0 + 0.03)):
    part(fixed, f"jack_eclip_{e} (with the shaft)", cyly(*JACK, 12 * MM, yy, yy + 0.03), STEEL, "buy")
def groove_pulley(x, z, pd, y0, y1, bore=True):
    """A printed round-belt pulley along Y: flanges round a 5 mm groove at LD_Y, pitch diameter pd."""
    od = pd + 5 * MM
    pul = cyly(x, z, od, y0, y1).cut(cyly(x, z, od + 0.1, LD_Y - 2.6 * MM, LD_Y + 2.6 * MM).cut(cyly(x, z, pd - 4.6 * MM, LD_Y - 3 * MM, LD_Y + 3 * MM)))
    return pul.cut(rex_y(x, z, y0 - 0.1, y1 + 0.1)) if bore else pul
part(fixed, "jack_pulley (print, PETG: 24 mm pitch diameter groove for the 5 mm round belt, 8mm REX bore)", groove_pulley(*JACK, 2 * LD_RJ, *JP_Y), BLUE, "print")
# the idler: a goBILDA 3401-4008-0016 (16 mm pitch diameter, REX bore) on a short REX shaft in two flanged bearings, in a
# printed fork hung from four of the 9-hole channel's web holes (M4 from above the web, into heat-set inserts)
ID_PY = (LD_Y - 3 * MM, LD_Y - 3 * MM + 14 * MM)    # the 3401's groove side toward the lane, its hub outboard
ID_CHEEK = ((ID_PY[0] - 0.05 - 0.25, ID_PY[0] - 0.05), (ID_PY[1] + 0.05, ID_PY[1] + 0.05 + 0.25))
part(fixed, "idler_pulley (goBILDA 3401-4008-0016, 16 mm PD round-belt pulley, 8mm REX, set screw)", cyly(*IDLER, 19.6 * MM, *ID_PY).cut(cyly(*IDLER, 22 * MM, LD_Y - 2.6 * MM, LD_Y + 2.6 * MM).cut(cyly(*IDLER, 2 * LD_RI - 4.6 * MM, LD_Y - 3 * MM, LD_Y + 3 * MM))), STEEL, "buy")
vendor("idler_pulley (goBILDA 3401-4008-0016, 16 mm PD round-belt pulley, 8mm REX, set screw)", "3401-4008-0016 assembly.STEP", (28.365, 20.325, 31.31), (0, 0, 1), (1, 0, 0), (IDLER[0], ID_PY[0], IDLER[1]), (0, 1, 0), (1, 0, 0))
ID_SH = (ID_CHEEK[0][0] - 0.06, ID_CHEEK[1][1] + 0.06)
part(fixed, f"idler_shaft (goBILDA 2106-4008-0320, 8mm REX, 32 mm, e-clips)", cyly(*IDLER, 8 * MM, ID_SH[0], ID_SH[0] + 32 * MM), STEEL, "buy")
for nm, (c0, c1) in (("in", ID_CHEEK[0]), ("out", ID_CHEEK[1])):
    yf = c1 if nm == "in" else c0                    # the flange on the cheek's face toward the pulley
    part(fixed, f"idler_bearing_{nm} (goBILDA 1611-0514-4008 flanged bearing)", cyly(*IDLER, 14 * MM, c0, c1).union(cyly(*IDLER, 15 * MM, yf, yf + (0.8 * MM if nm == "in" else -0.8 * MM))), STEEL, "buy")
part(fixed, "idler_spacers (goBILDA 8mm REX spacers: the bearings to the pulley)", cyly(*IDLER, 12 * MM, ID_CHEEK[0][1] + 0.8 * MM, ID_PY[0]).union(cyly(*IDLER, 12 * MM, ID_PY[1], ID_CHEEK[1][0] - 0.8 * MM)), STEEL, "buy")
HG_X = (CHAN_HOLES[0][0] - 0.25, CHAN_HOLES[1][0] + 0.25)  # the hanger's block, between the channel's flanges, under its web
HG_Y = (ID_CHEEK[0][0], ID_CHEEK[1][1])
hg = bx(*HG_X, *HG_Y, IDLER[1] + 0.47, CHAN_WEB_Z[0])
hg = hg.union(bx(IDLER[0] - 0.3, IDLER[0] + 0.3, *ID_CHEEK[0], IDLER[1] - 0.3, CHAN_WEB_Z[0])).union(bx(IDLER[0] - 0.3, IDLER[0] + 0.3, *ID_CHEEK[1], IDLER[1] - 0.3, CHAN_WEB_Z[0]))
hg = hg.union(cyly(*IDLER, 0.6, *ID_CHEEK[0])).union(cyly(*IDLER, 0.6, *ID_CHEEK[1]))
hg = hg.cut(cyly(*IDLER, 14 * MM, ID_CHEEK[0][0] - 0.1, ID_CHEEK[1][1] + 0.1).intersect(bx(-9, 9, ID_CHEEK[0][0] - 0.1, ID_CHEEK[1][1] + 0.1, -9, 9)))
hg = hg.cut(bx(IDLER[0] - 0.42, IDLER[0] + 0.42, ID_CHEEK[0][1], ID_CHEEK[1][0], IDLER[1] - 0.6, IDLER[1] + 0.45))   # the pulley's room
part(fixed, "idler_hanger (print, PETG: a fork under the 9-hole channel's web; the idler's two bearings pressed in; M4 heat-set inserts)", hg, BLUE, "print")
bolt(fixed, "idler_hanger", "the idler's hanger to the 9-hole channel's web (from above the web)", [(hx, hy, CHAN_WEB_Z[1]) for hx, hy in CHAN_HOLES], (0, 0, -1), (CHAN_WEB_Z[1] - CHAN_WEB_Z[0]) * IN, nut=False, tapped=10, into="idler_hanger")
# the belt: its path at rest (the roller down), drawn as a 5 mm cord of straight runs and arcs
def belt_path(circles):
    """Tangent runs and arcs round circles [(centre (X, z), r, turn)] in the X-z plane (turn +1: counter-clockwise as
    drawn with X right and z up); returns the cord's centreline as points."""
    def tangent(cA, rA, sA, cB, rB, sB):
        D = (cB[0] - cA[0], cB[1] - cA[1]); dist = math.hypot(*D); k = sB * rB - sA * rA
        th = math.atan2(D[1], D[0]) - math.asin(k / dist); d = (math.cos(th), math.sin(th)); pd = (-d[1], d[0])
        return (cA[0] - sA * rA * pd[0], cA[1] - sA * rA * pd[1]), (cB[0] - sB * rB * pd[0], cB[1] - sB * rB * pd[1])
    n = len(circles); segs = [tangent(*circles[i], *circles[(i + 1) % n]) for i in range(n)]
    pts = []
    for i in range(n):
        c, r, sg = circles[i]; arr = segs[i - 1][1]; dep = segs[i][0]
        a0 = math.atan2(arr[1] - c[1], arr[0] - c[0]); a1 = math.atan2(dep[1] - c[1], dep[0] - c[0])
        sweep = (a1 - a0) % (2 * math.pi) if sg > 0 else -((a0 - a1) % (2 * math.pi))
        for k in range(13): a = a0 + sweep * k / 12; pts.append((c[0] + r * math.cos(a), c[1] + r * math.sin(a)))
    return pts
def belt_len_path(pts): return sum(math.hypot(b[0] - a[0], b[1] - a[1]) for a, b in zip(pts, pts[1:] + pts[:1]))
LD_LOOP = lambda zr: [(JACK, LD_RJ, 1), ((ROLLER_AXLE[0], zr), LD_RR, 1), (IDLER, LD_RI, 1)]   # all three inside the loop: they turn alike
LD_PTS = belt_path(LD_LOOP(ROLLER_AXLE[1]))
cord = None
for a, b in zip(LD_PTS, LD_PTS[1:] + LD_PTS[:1]):
    L_ = math.hypot(b[0] - a[0], b[1] - a[1])
    if L_ < 1e-4: continue
    seg = cq.Workplane().add(cq.Solid.makeCylinder(LD_CORD / 2, L_, cq.Vector(a[0], LD_Y, a[1]), cq.Vector(b[0] - a[0], 0, b[1] - a[1])))
    cord = seg if cord is None else cord.union(seg)
part(fixed, f"lane_drive_belt (goBILDA {LD_BELT[1]}, 5 mm round belt, {LD_BELT[0]} mm: the roller to the jackshaft, over the idler)", cord, RED, "buy")

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

# ---- the feeder: one driven, on the left, on a swinging yoke; a sprung foam pad on the right ----
# The feeder hangs from a yoke ahead of the launcher: two 1/4 in aluminium arms, 0.37 in apart, each with a flanged
# bearing on the left flywheel's shaft (REX bore, so its inner race turns with the shaft) and one on the feeder's shaft,
# which reaches back 3 in from them to carry the wheels (there's no room for an arm behind: his rear channel holds a hub
# where it would go). The flywheel shaft's 16T drives the feeder's 24T by a belt whose centres the yoke keeps. A servo,
# by a pushrod to a tab on the inner arm, swings it: in, the feeder pinches the ball against the pad (drawn so); out
# (10 deg), it clears the waiting ball.
y = LANE_Y + FEED_Y
U, V = (0.0944, 0.9955), (0.9955, -0.0944)        # the launcher channels' along and across directions (Y, z), as they lean
def lat(o, a, b): return (o[0] + (a * U[0] + b * V[0]) * MM, o[1] + (a * U[1] + b * V[1]) * MM)
REAR_O, FRONT_O = (3.286 - FLY_IN, 2.907), (3.464 - FLY_IN, 4.794)   # a 14 mm pattern hole on each left channel (Y, z), the module moved in
REAR_WEB, FRONT_WEB = (-3.99, -4.09), (-0.31, -0.41)   # the webs' faces, X (toward the feeder, away)
PL_T = 0.25
REAR_WEB_T, FRONT_WEB_T = 2.54, 2.54
ARM_X = {"front": (-0.12, -0.12 + PL_T),           # ahead of the front channel, clear of the screw heads of the hub behind its web
         "outer": (0.50, 0.50 + PL_T)}              # under his 8-hole channel's bottom flange, behind the belts' plane
PIVOT = (FLY_Y[0], FLY_Z)
def arm_rot(p, a):
    """A point (Y, z) turned a radians about the flywheel's shaft (positive: the feeder swings out, toward +Y)."""
    dy, dz = p[0] - PIVOT[0], p[1] - PIVOT[1]
    return (PIVOT[0] + dy * math.cos(a) - dz * math.sin(a), PIVOT[1] + dy * math.sin(a) + dz * math.cos(a))
ARM_DIR = ((FEED_Y - PIVOT[0]) / FEED_C, (FEED_Z - PIVOT[1]) / FEED_C)  # pivot to the feeder, drawn (in)
TAB_P = (PIVOT[0] + ARM_DIR[0] * (FEED_C + 0.62), PIVOT[1] + ARM_DIR[1] * (FEED_C + 0.62))   # the pushrod's pin, below the feeder
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
for nm, (x0, x1) in ARM_X.items():
    pts = [PIVOT, (y, FEED_Z)] + ([(TAB_P[0], TAB_P[1] + 0.12)] if nm == "front" else [])
    pl = plate_outline(pts, x0, x1, 0.30).cut(cylx(y, FEED_Z, 14 * MM, x0 - 0.1, x1 + 0.1)).cut(cylx(*PIVOT, 14 * MM, x0 - 0.1, x1 + 0.1))
    part(fixed, f"feeder_arm_{nm} (1/4 in aluminium: turns on the flywheel shaft, carries the feeder)", pl, ALU, "cut")
    for which, (cy_, cz_) in (("", (y, FEED_Z)), ("pivot_", PIVOT)):
        bn = f"feeder_{which}bearing_{nm} (goBILDA 1611-0514-4008 flanged bearing{', on the flywheel shaft' if which else ''})"
        part(fixed, bn, cylx(cy_, cz_, 14 * MM, x1 - 4.2 * MM, x1).union(cylx(cy_, cz_, 15 * MM, x1, x1 + 0.8 * MM)), STEEL, "buy")
        vendor(bn, "1611-0514-4008.STEP", (0, 4.8, 0), (0, -1, 0), (1, 0, 0), (x1 + 0.8 * MM, cy_, cz_), (-1, 0, 0), (0, 1, 0))
part(fixed, "feeder (goBILDA 3632-0014-0072 72 mm Gecko x2, softest durometer)", cylx(y, FEED_Z, 2 * RF, FEED_X[0], FEED_X[1]), GREEN, "buy")
FEED_SHAFT_L = 120
x_rear = FD_PUL_X[1] + 0.1 - FEED_SHAFT_L * MM     # the shaft's rear end, just behind the wheels (its front end past the pulley)
part(fixed, "feeder_shaft (goBILDA 2106-4008-1200, 8mm REX, 120 mm, e-clips: from the yoke back through the wheels)", cylx(y, FEED_Z, 8 * MM, x_rear, x_rear + FEED_SHAFT_L * MM), STEEL, "buy")
part(fixed, "feeder_eclip_rear (with the shaft, behind the wheels)", cylx(y, FEED_Z, 12 * MM, x_rear + 0.03, x_rear + 0.06), STEEL, "buy")
part(fixed, "feeder_spacers_yoke (goBILDA 8mm REX spacers: between the yoke's arms)", cylx(y, FEED_Z, 12 * MM, ARM_X["front"][1] + 0.8 * MM, ARM_X["outer"][0]), STEEL, "buy")
FCOL = (ARM_X["front"][0] - 10.3 * MM, ARM_X["front"][0])   # a clamping collar against the front bearing: with the e-clip ahead, it holds the shaft both ways
part(fixed, "feeder_spacers_front (goBILDA 8mm REX spacers, stacked: the wheels to the collar)", cylx(y, FEED_Z, 12 * MM, FEED_X[1], FCOL[0]), STEEL, "buy")
part(fixed, "feeder_shaft_collar (goBILDA 2910-1020-4008, 8mm REX clamping collar)", cylx(y, FEED_Z, 20 * MM, *FCOL), STEEL, "buy")
part(fixed, "feeder_spacers_mid (goBILDA 8mm REX spacers: the outer arm's bearing to the pulley)", cylx(y, FEED_Z, 12 * MM, ARM_X["outer"][1] + 0.8 * MM, FD_PUL_X[0]), STEEL, "buy")
part(fixed, "feeder_pulley (goBILDA 3417-4008-0024, 24T HTD5, 8mm REX)", cylx(y, FEED_Z, 38.8 * MM, *FD_PUL_X), BLACK, "buy")
part(fixed, "feeder_eclip (included with the shaft)", cylx(y, FEED_Z, 12 * MM, x_rear + FEED_SHAFT_L * MM - 0.04, x_rear + FEED_SHAFT_L * MM - 0.01), STEEL, "buy")
part(fixed, "feeder_shaft_spacer (goBILDA 8mm REX spacers: the pulley to the e-clip)", cylx(y, FEED_Z, 12 * MM, FD_PUL_X[1], x_rear + FEED_SHAFT_L * MM - 0.04), STEEL, "buy")
part(fixed, f"feeder_belt (goBILDA {FEED_BELT[1]}, HTD5 9 mm, {FEED_BELT[0]} mm: the left flywheel's 16T to the feeder's 24T; fixed centres {FEED_C * IN:.1f} mm)", loop("YZ", (y, FEED_Z), P24 / 2, (FLY_Y[0], FLY_Z), P16 / 2, 0.14, 9 * MM, BELT_X - 4.5 * MM), BLACK, "buy")
# the gate servo: a goBILDA 2000-0025-0003 Speed servo, spline up, under the front arm, on a printed bracket inside the left
# drive rail; its printed horn and a printed pushrod swing the arm by an M3 shoulder screw in a printed tab on the arm.
GS_SPL = (0.0, 4.13)                                # the spline, (X, Y): the horn points +X mid-stroke and swings +-45 deg, the pushrod
                                                    # runs along Y to the pin; the servo's long axis toward -Y
TAB_T = (TAB_P[1] - 0.12, TAB_P[1] + 0.20)          # the tab's z: below the feeder's bearing, on the arm's front face
PR_Z = (TAB_T[0] - 0.15 - 4 * MM, TAB_T[0] - 0.15)  # the pushrod's z, under the tab (which drops 0.1 as the yoke swings out)
HORN_Z = (PR_Z[0] - 0.02 - 4 * MM, PR_Z[0] - 0.02)  # the horn's, under the pushrod
GS_TOP = HORN_Z[0] - 4.1 * MM                       # the servo's case top (its spline 4.1 mm proud)
PIN_X = 0.50                                        # the pushrod pin's X, in the tab
HORN_R = 0.50
GSN = "gate_servo (goBILDA 2000-0025-0003 Speed servo: swings the feeder in and out)"
part(fixed, GSN, place_local(servo_local(), (GS_SPL[0], GS_SPL[1], GS_TOP + 4.1 * MM - 4.1 * MM), (0, 0, 1), (0, -1, 0)), BLACK, "buy")
vendor(GSN, "2000-0025-0003.step", (-10.0, 0.0, 12.8), (0, 0, 1), (1, 0, 0), (GS_SPL[0], GS_SPL[1], GS_TOP), (0, 0, 1), (0, -1, 0))
# the horn's tip, for the drawn (in) pose: the pushrod runs from it to the tab's pin
PR_L = 1.05                                         # pushrod centres
def horn_tip(pin):
    """The horn's tip (X, Y) that puts the pushrod's far end on pin (X, Y): the solution ahead of the spline (+X)."""
    sx, sy = GS_SPL; d = math.hypot(pin[0] - sx, pin[1] - sy)
    a = math.atan2(pin[1] - sy, pin[0] - sx); c = (HORN_R ** 2 + d ** 2 - PR_L ** 2) / (2 * HORN_R * d)
    tips = [(sx + HORN_R * math.cos(t), sy + HORN_R * math.sin(t)) for t in (a - math.acos(max(-1, min(1, c))), a + math.acos(max(-1, min(1, c))))]
    return max(tips, key=lambda q: q[0])
PIN_IN = (PIN_X, TAB_P[0]); PIN_OUT = (PIN_X, arm_rot(TAB_P, ARM_IN - ARM_OUT)[0])
HT = horn_tip(PIN_IN)
def bar_xy(p0, p1, w, z0, z1):
    """A flat bar in the X-Y plane between two points, rounded ends."""
    L = math.hypot(p1[0] - p0[0], p1[1] - p0[1]); ang = math.degrees(math.atan2(p1[1] - p0[1], p1[0] - p0[0]))
    b = cq.Workplane("XY").center(L / 2, 0).rect(L, w).extrude(z1 - z0).union(cq.Workplane("XY").circle(w / 2).extrude(z1 - z0)).union(cq.Workplane("XY").center(L, 0).circle(w / 2).extrude(z1 - z0))
    return b.rotate((0, 0, 0), (0, 0, 1), ang).translate((p0[0], p0[1], z0))
hn = bar_xy(GS_SPL, HT, 0.36, *HORN_Z).cut(cylz(*GS_SPL, 6.2 * MM, HORN_Z[0] - 0.01, HORN_Z[0] + 3 * MM)).cut(cylz(*GS_SPL, 3.4 * MM, HORN_Z[0], HORN_Z[1] + 0.01)).cut(cylz(*HT, 3.2 * MM, HORN_Z[0] - 0.01, HORN_Z[1] + 0.01))
part(fixed, "gate_horn (print, PETG: on the servo's H25T spline, the servo's M3 horn screw; an M3 heat-set insert at its tip)", hn, BLUE, "print")
pr = bar_xy(HT, PIN_IN, 0.32, *PR_Z).cut(cylz(*HT, 4.2 * MM, PR_Z[0] - 0.01, PR_Z[1] + 0.01)).cut(cylz(*PIN_IN, 4.2 * MM, PR_Z[0] - 0.01, PR_Z[1] + 0.01))
part(fixed, "gate_pushrod (print, PETG: 4 mm bores on the two shoulder screws)", pr, BLUE, "print")
tab = bx(ARM_X["front"][1], PIN_X + 0.22, TAB_P[0] - 0.22, TAB_P[0] + 0.22, *TAB_T)
part(fixed, "gate_tab (print, PETG: on the front arm's face, below its bearing; M3 heat-set insert for the pin, M4 for its screws)", tab, BLUE, "print")
for nm_, (px_, py_), z_top, z_bot, into in (("gate_pin_arm", PIN_IN, TAB_T[0], PR_Z[0], "gate_tab"), ("gate_pin_horn", HT, PR_Z[1] + 0.12, HORN_Z[0], "gate_horn")):
    sc = cylz(px_, py_, 4 * MM, z_bot - 0.01, z_top).union(cylz(px_, py_, 3 * MM, z_top, z_top + 5 * MM) if nm_ == "gate_pin_arm" else cylz(px_, py_, 3 * MM, z_bot - 4 * MM, z_bot))
    sc = sc.union(cylz(px_, py_, 7 * MM, z_bot - 0.12, z_bot) if nm_ == "gate_pin_arm" else cylz(px_, py_, 7 * MM, z_top, z_top + 0.12))
    part(fixed, f"{nm_} (M3 shoulder screw, 4 mm shoulder: the pushrod's pivot)", sc, STEEL, "buy")
c = bolt(fixed, "gate_tab", "the gate tab to the front arm (from the arm's back face)", [(ARM_X["front"][0], TAB_P[0] + dy, (TAB_T[0] + TAB_T[1]) / 2 + 0.04) for dy in (-0.12, 0.12)], (1, 0, 0), PL_T * IN, nut=False, tapped=10, into="gate_tab", through=("feeder_arm_front",), service="with the arm off the shaft")
drill(fixed, ["feeder_arm_front"], c)
# the servo's bracket: printed, under its tabs (a window for its case), out to the left wall's lower front REX standoff,
# which passes through it (its REX hole can't turn on it): it goes on that standoff before the wall goes on
GS_TAB_Z = GS_TOP - 12.8 * MM                       # the tabs' upper face; they sit on the bracket
gsb_z = (GS_TAB_Z - 2.5 * MM - 0.25, GS_TAB_Z - 2.5 * MM)
GS_SO = STANDOFFS[1][0]                             # (X, z) of that standoff
gsb = bx(GS_SPL[0] - 0.42, GS_SO[0] - 0.2, GS_SPL[1] - 1.65, GS_SPL[1] + 0.72, *gsb_z)
gsb = gsb.union(bx(GS_SO[0] - 0.27, GS_SO[0] + 0.27, 3.3, 4.3, gsb_z[0], GS_SO[1] + 0.37))
gsb = gsb.cut(bx(GS_SPL[0] - 10.5 * MM, GS_SPL[0] + 10.5 * MM, GS_SPL[1] - 30.5 * MM, GS_SPL[1] + 10.5 * MM, gsb_z[0] - 0.1, gsb_z[1] + 0.1))
gsb = gsb.cut(rex_y(*GS_SO, 3.0, 4.6))
part(fixed, "gate_servo_bracket (print, PETG or nylon: under the servo's tabs, on the left wall's front REX standoff)", gsb, BLUE, "print")
GS_HOLES = [(GS_SPL[0] + dx * MM, GS_SPL[1] - ly * MM) for ly in (-14, 34) for dx in (-5, 5)]
c = bolt(fixed, "gate_servo_tabs", "the gate servo's tabs to its bracket (from below; nuts on the tabs)", [(hx, hy, gsb_z[0]) for hx, hy in GS_HOLES], (0, 0, 1), 0.25 * IN + 2.5, nut=True, through=("gate_servo_bracket", "gate_servo ("))
drill(fixed, ["gate_servo_bracket"], c)
# the pad: 1/8 in aluminium plate, foam on its inner face, hinged along X at its bottom, banded inward against a stop.
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
part(fixed, "feeder_bridge (print, PETG or nylon: behind the launcher's rear channels, its shelf under the feeder floor; M4 heat-set inserts)", bridge, BLUE, "print")
bolt(fixed, "feeder_bridge", "the feeder bridge to the rear channels' webs (from inside the channels)", [(REAR_WEB[0], *h) for h in BR_HOLES], (-1, 0, 0), REAR_WEB_T, nut=False, tapped=10, into="feeder_bridge", service="with the feeder out (its yoke off the flywheel shaft)")
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

# ---- the launcher changes: the flywheel motors move out and up (his old place for them is where the feeder and pad go)
#      and become goBILDA 6000 RPM (1:1) Yellow Jackets; the left flywheel's shaft gets longer, for the feeder's belt ----
FLY_SHAFT = (-4.09, -4.09 + 144 * MM)              # his 96 mm shaft's rear end, kept; out through the front arm to the 16T
part(launcher, "flywheel_shaft_L (goBILDA 2106-4008-1440, 8mm REX, 144 mm, e-clip at the front: in place of his 96 mm shaft)", cylx(*PIVOT, 8 * MM, *FLY_SHAFT), STEEL, "buy")
part(launcher, "flywheel_spacers_L_front (goBILDA 8mm REX spacers: his front bearing to the front arm's)", cylx(*PIVOT, 10 * MM, -0.25, ARM_X["front"][0]), STEEL, "buy")
part(launcher, "flywheel_spacers_L_yoke (goBILDA 8mm REX spacers: between the yoke's arms)", cylx(*PIVOT, 12 * MM, ARM_X["front"][1] + 0.8 * MM, ARM_X["outer"][0]), STEEL, "buy")
part(launcher, "flywheel_spacers_L_pulley (goBILDA 8mm REX spacers: the outer arm's bearing to the 16T)", cylx(*PIVOT, 12 * MM, ARM_X["outer"][1] + 0.8 * MM, FD_PUL_X[0]), STEEL, "buy")
part(launcher, "flywheel_feeder_pulley (goBILDA 3417-4008-0016, 16T HTD5, 8mm REX: drives the feeder)", cylx(*PIVOT, 28 * MM, *FD_PUL_X), BLACK, "buy")
part(launcher, "flywheel_shaft_eclip_L (with the shaft)", cylx(*PIVOT, 12 * MM, FLY_SHAFT[1] - 0.06, FLY_SHAFT[1] - 0.03), STEEL, "buy")
for s in (1, -1):
    nm = 'L' if s > 0 else 'R'; my, mz = FM[s]; fy = FLY_Y[0] if s > 0 else FLY_Y[1]
    mn = f"flywheel_motor_{nm} (goBILDA 5203-2402-0001, 6000 RPM Yellow Jacket, 1:1: along X outboard of the launcher frame)"
    part(launcher, mn, cylx(my, mz, MOTOR_D, FM_FACE, FM_FACE + 107.7 * MM), (0.95, 0.75, 0.2), "buy")
    vendor(mn, "5203-2402-0001 assembly.STEP", (-37.15, 92.9, -11.05), (0, 1, 0), (1, 0, 0), (FM_FACE, my, mz), (-1, 0, 0), (0, 0, 1))
    part(launcher, f"flywheel_motor_pulley_{nm} (goBILDA 3417-4008-0016, 16T HTD5)", cylx(my, mz, 28 * MM, FLY_BELT_X - 6 * MM, FLY_BELT_X + 6 * MM), BLACK, "buy")
    part(launcher, f"flywheel_pulley_{nm} (goBILDA 3417-4008-0024, 24T HTD5, on the flywheel's shaft: in place of his 41T)", cylx(fy, FLY_Z, 38.8 * MM, FLY_BELT_X - 6 * MM, FLY_BELT_X + 6 * MM), BLACK, "buy")
    part(launcher, f"flywheel_belt_{nm} (goBILDA {FLY_BELT[s][1]}, HTD5 9 mm: the motor's 16T to the flywheel's 24T)", loop("YZ", (fy, FLY_Z), P24 / 2, (my, mz), P16 / 2, 0.14, 9 * MM, FLY_BELT_X - 4.5 * MM), BLACK, "buy")
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

# ---- the turret's drive (decided 8 Oct: a servo, with two absolute encoders): the goBILDA gear-driven turret kit
#      (3208-0004-0001, 2.75:1) keeps its 64T drive gear, on its own short shaft in a bearing in the kit's mount and one in
#      a plate under the front 8-hole channel. A goBILDA Speed servo in continuous mode stands in that plate, case down
#      through a window, and drives the shaft 1:1 by a belt (the turret turns at about 40 RPM, 240 deg/s). Its angle comes
#      from two REV Thru-Bore absolute encoders on an OctoQuad (issue #169), reading through different ratios: A on the
#      drive gear's shaft (176/64 = 2.75 turns per turret turn), B on a 48T gear meshing that 64T (176/48 = 3.67 turns
#      per turret turn). The pair of readings is unique over 393 deg of turret, so it knows its angle at power-up; a 1:1
#      encoder can't be built from stock gears (the ring is 176T, goBILDA's largest mod 0.8 gear 108T; doc/motors-and-servos.md) ----
TG = (1.737, 0.158)                                 # the drive gear's axis (X, Y), measured from his CAD
TG_MOUNT_Z = (8.889, 9.204)                         # his kit's 1231 mount under the gear
TP_Z = (6.999 - 3 / 16, 6.999)                      # the plate (3/16 in aluminium: it reaches forward to carry the encoders), under the channel's bottom flange
CH8_HOLES = [(0.636, -1.417), (0.636, -2.047)]      # two of that flange's holes (X, Y), measured from his CAD, outside encoder A's cradle and the feeder's belt
TBELT = (295, "3412-0009-0295")
TB_C = (TBELT[0] * MM - math.pi * P24) / 2          # 24T to 24T: 87.5 mm
TM = (2.50, TG[1] + math.sqrt(TB_C ** 2 - (2.50 - TG[0]) ** 2))   # the servo's spline: clear of the wall standoffs and the ceiling's posts
SV_DIR = (1, 0, 0)                                  # the servo's long axis (its case centre 10 mm along it from the spline)
SV_TAB = TP_Z[1]                                    # its tabs' lower face, on the plate
SV_TOP = SV_TAB + 15.3 * MM                         # its case's top; the spline 4.1 mm above
SV_HUB = (SV_TOP + 0.02, SV_TOP + 0.02 + 8 * MM)    # the servo-to-8mm-REX hub on its spline
TPUL_Z = (SV_HUB[1], SV_HUB[1] + 12 * MM)           # the 24Ts' plane
def sv_xy(lx, ly): return (TM[0] + lx * MM * SV_DIR[0] - ly * MM * SV_DIR[1], TM[1] + lx * MM * SV_DIR[1] + ly * MM * SV_DIR[0])
def plate_xy(pts, z0, z1, r):
    """A plate in the X-Y plane round points (X, Y): the hull of r-radius circles."""
    import itertools
    pl = None
    for p_ in pts:
        c_ = cylz(*p_, 2 * r, z0, z1); pl = c_ if pl is None else pl.union(c_)
    for a_, b_ in itertools.combinations(pts, 2):
        ln = math.hypot(b_[0] - a_[0], b_[1] - a_[1]); nn = ((a_[1] - b_[1]) / ln * r, (b_[0] - a_[0]) / ln * r)
        pl = pl.union(cq.Workplane("XY").polyline([(a_[0] + nn[0], a_[1] + nn[1]), (b_[0] + nn[0], b_[1] + nn[1]), (b_[0] - nn[0], b_[1] - nn[1]), (a_[0] - nn[0], a_[1] - nn[1])]).close().extrude(z1 - z0).translate((0, 0, z0)))
    for a_, b_, c_ in itertools.combinations(pts, 3):
        if abs((b_[0] - a_[0]) * (c_[1] - a_[1]) - (b_[1] - a_[1]) * (c_[0] - a_[0])) > 1e-4:
            pl = pl.union(cq.Workplane("XY").polyline([a_, b_, c_]).close().extrude(z1 - z0).translate((0, 0, z0)))
    return pl
sv_corners = [sv_xy(lx, ly) for lx in (10 - 32, 10 + 32) for ly in (-14, 14)]
GB = (TG[0] + 0.8 * (64 + 48) / 2 * MM, TG[1])    # encoder B's 48T, meshing the 64T on the far side from the ring
ENC_PAD = (0.07, GB[0] + 1.24, TG[1] - 1.24, TG[1] + 1.68)   # the plate under both encoders' cradles (X0, X1, Y0, Y1)
tp = plate_xy(CH8_HOLES + [TG, TM], *TP_Z, 0.32).union(plate_xy(sv_corners + [TM], *TP_Z, 0.12)).union(bx(*ENC_PAD, *TP_Z))
tp = tp.cut(cylz(*TG, 14 * MM, TP_Z[0] - 0.1, TP_Z[1] + 0.1)).cut(cylz(*GB, 14 * MM, TP_Z[0] - 0.1, TP_Z[1] + 0.1))
win = [sv_xy(lx, ly) for lx in (10 - 20.5, 10 + 20.5) for ly in (-10.5, 10.5)]
tp = tp.cut(cq.Workplane("XY").polyline([win[0], win[1], win[3], win[2]]).close().extrude(0.5).translate((0, 0, TP_Z[0] - 0.1)))
part(launcher, "turret_drive_plate (3/16 in aluminium: under the front 8-hole channel's bottom flange; the gear shaft's lower bearing, the servo's tabs on it, its case down through a window; both encoders' cradles)", tp, ALU, "cut")
TSN = "turret_servo (goBILDA 2000-0025-0003 Speed servo, set to continuous mode: turns the turret, 1:1 to its drive gear)"
part(launcher, TSN, place_local(servo_local(), (*TM, SV_TOP), (0, 0, 1), SV_DIR), BLACK, "buy")
vendor(TSN, "2000-0025-0003.step", (-10.0, 0.0, 12.8), (0, 0, 1), (1, 0, 0), (*TM, SV_TOP), (0, 0, 1), SV_DIR)
part(launcher, "turret_servo_hub (goBILDA servo hub, 25T spline to 8mm REX, check the SKU: carries the 24T pulley)", cylz(*TM, 18 * MM, *SV_HUB), STEEL, "buy")
part(launcher, "turret_servo_pulley (goBILDA 3417-4008-0024, 24T HTD5, on the servo's hub)", cylz(*TM, 38.8 * MM, *TPUL_Z), BLACK, "buy")
part(launcher, "turret_gear_pulley (goBILDA 3417-4008-0024, 24T HTD5)", cylz(*TG, 38.8 * MM, *TPUL_Z), BLACK, "buy")
c = bolt(launcher, "turret_servo_tabs", "the turret servo's tabs to its plate (from above, nuts under the plate)", [(*sv_xy(10 + dx, dy), SV_TAB + 2.5 * MM) for dx in (-24, 24) for dy in (-5, 5)], (0, 0, -1), 2.5 + 3 / 16 * IN, nut=True, through=("turret_servo (", "turret_drive_plate"), service="before the hub and pulley")
drill(launcher, ["turret_drive_plate"], c)
c = bolt(launcher, "turret_plate", "the turret drive plate under the 8-hole channel's bottom flange (from below; nuts inside the channel, a wrench from its open front)", [(hx, hy, TP_Z[0]) for hx, hy in CH8_HOLES], (0, 0, 1), 2.5 + 3 / 16 * IN, nut=True, through=("turret_drive_plate",))
drill(launcher, ["turret_drive_plate"], c)
part(launcher, f"turret_belt (goBILDA {TBELT[1]}, HTD5 9 mm, {TBELT[0]} mm: fixed centres {TB_C * IN:.1f} mm)", loop("XY", TG, P24 / 2, TM, P24 / 2, 0.14, 9 * MM, sum(TPUL_Z) / 2 - 4.5 * MM), BLACK, "buy")
TGS = (TG_MOUNT_Z[1] + 0.36 - 96 * MM, TG_MOUNT_Z[1] + 0.36)   # the gear's shaft: up into the gear's hub, down through encoder A
part(launcher, "turret_gear_shaft (goBILDA 2106-4008-0960, 8mm REX, 96 mm, e-clips: the drive gear on it as the kit mounts it on a motor's shaft; through encoder A)", cylz(*TG, 8 * MM, *TGS), STEEL, "buy")
for nm, (z0, z1) in (("plate", TP_Z), ("mount", (TG_MOUNT_Z[0] + 0.1, TG_MOUNT_Z[1]))):
    part(launcher, f"turret_gear_bearing_{nm} (goBILDA 1611-0514-4008 flanged bearing)", cylz(*TG, 14 * MM, z0, z1).union(cylz(*TG, 15 * MM, z0 - 0.8 * MM, z0)), STEEL, "buy")
part(launcher, "turret_gear_spacers (goBILDA 8mm REX spacers: the pulley up to the kit's mount)", cylz(*TG, 12 * MM, TPUL_Z[1], TG_MOUNT_Z[0] + 0.1 - 0.8 * MM), STEEL, "buy")
part(launcher, "turret_gear_eclip (with the shaft)", cylz(*TG, 12 * MM, TGS[0] + 0.02, TGS[0] + 0.05), STEEL, "buy")
# the two absolute encoders: REV Thru-Bore (REV-11-1271, absolute PWM output) on both shafts, each in a printed cradle
# with a printed sleeve (8mm REX bore, 1/2 in hex outside) in its bore, cabled to an OctoQuad (FTC Edition) in the
# electronics bay, which reads both pulse widths and goes to the Control Hub's I2C bus 2 (#169)
THRU = (-21.15, 36.21, 24.89, -2.0, 13.83)          # the encoder in its own frame (mm): x from, x to (connector +x), +-y, z from, z to; the bore on the z axis
def thru_box(o, x_dir, extra=0.0):
    """The encoder's envelope (a box, its bore cut) at robot point o (bore axis, local z 0), local +z up, +x along x_dir."""
    b = cq.Workplane("XY").box(THRU[1] - THRU[0] + 2 * extra, 2 * THRU[2] + 2 * extra, THRU[4] - THRU[3]).translate(((THRU[0] + THRU[1]) / 2, 0, (THRU[3] + THRU[4]) / 2))
    b = b.cut(cq.Workplane("XY").circle(6.4).extrude(40).translate((0, 0, -20)))
    return place_local(b, o, (0, 0, 1), x_dir)
CR_W = 6.0                                           # the cradles' walls (mm): room for an M4 heat-set insert
def thru_cradle(o, x_dir, z_top, floor, lid=False):
    """A printed cradle round an encoder: 6 mm walls, a floor `floor` mm under it with a hole for the shaft, up to z_top
    (with a lid over it, holed for the sleeve, if lid)."""
    h = z_top - (o[2] + THRU[3] * MM) + floor * MM
    w = cq.Workplane("XY").box(THRU[1] - THRU[0] + 2 * CR_W, 2 * THRU[2] + 2 * CR_W, h / MM).translate(((THRU[0] + THRU[1]) / 2, 0, THRU[3] - floor + h / MM / 2))
    top = z_top - 0.08 if lid else z_top + 1
    w = w.cut(cq.Workplane("XY").box(THRU[1] - THRU[0] + 0.4, 2 * THRU[2] + 0.4, (top - (o[2] + THRU[3] * MM)) / MM).translate(((THRU[0] + THRU[1]) / 2, 0, THRU[3] + (top - (o[2] + THRU[3] * MM)) / MM / 2)))
    w = w.cut(cq.Workplane("XY").circle(7.5 if not lid else 7.5).extrude(80).translate((0, 0, -40)))
    w = w.cut(cq.Workplane("XY").box(14, 20, 14).translate((THRU[1] + CR_W / 2, 0, (THRU[3] + THRU[4]) / 2)))   # the cable's slot at the connector
    return place_local(w, o, (0, 0, 1), x_dir)
TBN = "(REV-11-1271 Thru-Bore Encoder, absolute PWM output to the OctoQuad)"
# A: under the plate on the drive gear's shaft, its cradle screwed up to the plate
EA_O = (TG[0], TG[1], TP_Z[0] - 0.08 - THRU[4] * MM)       # its bore's origin: the encoder's top 0.08 in under the plate (the bearing's flange)
EA_X = (-1, 0, 0)
part(launcher, f"turret_enc_A {TBN}", thru_box(EA_O, EA_X), (0.15, 0.15, 0.16), "buy")
vendor(f"turret_enc_A {TBN}", "REV-11-1271.STEP", (0, 0, 0), (0, 0, 1), (1, 0, 0), EA_O, (0, 0, 1), EA_X)
part(launcher, "turret_enc_A_sleeve (print PETG: 8mm REX bore, 1/2 in hex outside, in encoder A's bore)", cylz(*TG, 12.6 * MM, EA_O[2] + THRU[3] * MM, EA_O[2] + THRU[4] * MM).cut(cylz(*TG, 8.2 * MM, 0, 20)), BLUE, "print")
part(launcher, "turret_enc_A_cradle (print PETG: holds encoder A under the drive plate; M4 heat-set inserts in its walls)", thru_cradle(EA_O, EA_X, TP_Z[0], 3.0), BLUE, "print")
EA_UP = [(TG[0] - 0.40, TG[1] + s_ * (THRU[2] + CR_W / 2 + 0.2) * MM) for s_ in (-1, 1)]
c = bolt(launcher, "turret_enc_A_cradle", "encoder A's cradle up to the drive plate (from above, into its inserts)", [(x, y, TP_Z[1]) for x, y in EA_UP], (0, 0, -1), 3 / 16 * IN, nut=False, tapped=10, into="turret_enc_A_cradle", through=("turret_drive_plate",), service="before the pulleys")
drill(launcher, ["turret_drive_plate"], c)
# B: a goBILDA 48T on its own shaft beside the drive gear, meshing the 64T; the shaft turns in a bearing in the plate and
# one at the top of a printed tower on the plate, which also cradles encoder B just above the plate
EBS = (TP_Z[0] - 0.12, TP_Z[0] - 0.12 + 80 * MM)
part(launcher, "turret_enc_B_shaft (goBILDA 2106-4008-0800, 8mm REX, 80 mm, e-clips)", cylz(*GB, 8 * MM, *EBS), STEEL, "buy")
TOWER_TOP = 9.30                                   # the tower's top bearing, just under the gear
for nm_, (z0, z1) in (("plate", TP_Z), ("tower", (TOWER_TOP - 0.20, TOWER_TOP))):
    part(launcher, f"turret_enc_B_bearing_{nm_} (goBILDA 1611-0514-4008 flanged bearing)", cylz(*GB, 14 * MM, z0, z1).union(cylz(*GB, 15 * MM, z0 - 0.8 * MM, z0)), STEEL, "buy")
GZ = (9.40, 9.64)                                     # the 64T's (and the ring's) teeth, z
part(launcher, "turret_enc_B_spacers (goBILDA 8mm REX spacers: the tower's bearing up to the gear)", cylz(*GB, 12 * MM, TOWER_TOP, GZ[0]), STEEL, "buy")
part(launcher, "turret_enc_B_gear (goBILDA 2302-0014-0048, aluminium mod 0.8 hub-mount gear, 48T, 14 mm bore, on an 8mm REX hub: meshes the kit's 64T)", cylz(*GB, 0.8 * 50 * MM, *GZ).cut(cylz(*GB, 8.2 * MM, 0, 20)), (0.85, 0.85, 0.88), "buy")
for nm_, z_ in (("top", GZ[1] + 0.01), ("bottom", EBS[0] + 0.02)):
    part(launcher, f"turret_enc_B_eclip_{nm_} (with the shaft)", cylz(*GB, 12 * MM, z_, z_ + 0.03), STEEL, "buy")
EB_O = (GB[0], GB[1], TP_Z[1] + 0.06 - THRU[3] * MM)        # encoder B just above the plate, on its cradle's floor
EB_X = (0, 1, 0)                                      # its connector toward +Y: clear of the drive pulley and the Limelight mast's screws
part(launcher, f"turret_enc_B {TBN}", thru_box(EB_O, EB_X), (0.15, 0.15, 0.16), "buy")
vendor(f"turret_enc_B {TBN}", "REV-11-1271.STEP", (0, 0, 0), (0, 0, 1), (1, 0, 0), EB_O, (0, 0, 1), EB_X)
part(launcher, "turret_enc_B_sleeve (print PETG: 8mm REX bore, 1/2 in hex outside, in encoder B's bore)", cylz(*GB, 12.6 * MM, EB_O[2] + THRU[3] * MM, EB_O[2] + THRU[4] * MM).cut(cylz(*GB, 8.2 * MM, 0, 20)), BLUE, "print")
EB_TOPZ = EB_O[2] + THRU[4] * MM + 0.05
tw = thru_cradle(EB_O, EB_X, EB_TOPZ, 1.5, lid=True)
tw = tw.union(bx(GB[0] - 0.35, GB[0] + 0.35, GB[1] - 0.35, GB[1] + 0.35, EB_TOPZ, TOWER_TOP))   # the column up to the top bearing (clear of the Limelight mast screws' key)
tw = tw.cut(cylz(*GB, 14.2 * MM, TOWER_TOP - 0.21, TOWER_TOP + 0.1)).cut(cylz(*GB, 13.5 * MM, EB_TOPZ - 0.2, TOWER_TOP - 0.2))
part(launcher, "turret_enc_B_tower (print PETG: cradles encoder B on the plate, its column carries the 48T shaft's top bearing; M4 heat-set inserts in its foot)", tw, BLUE, "print")
EB_DN = [(GB[0] + s_ * 0.5, GB[1] + (THRU[0] - CR_W / 2) * MM) for s_ in (-1, 1)]   # in its cradle's back wall, clear of encoder A under the plate
c = bolt(launcher, "turret_enc_B_tower", "encoder B's tower to the drive plate (from below, into its inserts)", [(x, y, TP_Z[0]) for x, y in EB_DN], (0, 0, 1), 3 / 16 * IN, nut=False, tapped=10, into="turret_enc_B_tower", through=("turret_drive_plate",), service="with the plate off the channel")
drill(launcher, ["turret_drive_plate"], c)
# ---- the electronics bay, at the back over the drive motors: a bent 3/16 in aluminium plate stands on the chassis's rear
#      angle; the Control Hub and the Expansion Hub hang on its back face, ports out, with the battery between them in a
#      printed cradle. Every port, the battery and the hubs' screws are reached from behind or above; the switch is on top.
#      The plate's tongue runs back over both rear chassis members and bolts to their top flanges' 8 mm grid ----
REAR_HOLES_X = (-7.247, -5.987)                     # the top-flange hole lines (X) of his rear channel (1107-0013-0336) and angle (1103-0041-0328), z 5.733
REAR_TOP = 5.733
EP_T = 3 / 16
EP_X = (-5.70, -5.70 + EP_T)                        # the plate's upright, at the front edge of the angle's top flange (X -6.146 to -5.673):
                                                    # 0.69 in from the hubs' faces to the frame's back, for the plugs and their wires' bends
EP_Y, EP_TOP = 8.15, 10.60
TONGUE = ((-7.50, EP_X[0]), 1.85)                   # back over both flanges, between the drive motors' encoder caps (|Y| 1.87)
TONGUE_HOLES = [(x, s_ * 8 * 3 * MM) for x in REAR_HOLES_X for s_ in (-1, 1)]
HUB = (143 * MM, 103 * MM, 29.5 * MM)               # REV's drawing: both hubs, M3 through holes 128 x 88 in the corner tabs
HUB_TAB = 4.0                                       # the corner tabs' thickness, mm (to check on a hub; the screw is M3 x 8 either way)
HUB_Z0 = 6.25
HUB_IN = 2.45                                       # the hubs' inner edges, |Y|: the battery cradle between them
BAT = (113.5 * MM, 90.5 * MM, 23 * MM)              # REV-31-1302: along Y, up, along X
CR_FLOOR = (REAR_TOP + EP_T, REAR_TOP + EP_T + 0.3)
BAT_X = (EP_X[0] - 0.03 - BAT[2], EP_X[0] - 0.03)
BAT_Z = (CR_FLOOR[1] + 0.01, CR_FLOOR[1] + 0.01 + BAT[1])
CR_X = (BAT_X[0] - 0.03 - 0.1, EP_X[0])
CR_W = BAT[0] / 2 + 0.01                            # the cradle's walls' inner faces, |Y|
part(elec, "elec_plate (3/16 in 5052 aluminium, cut and bent: the upright the hubs hang on, its tongue bolted to the rear chassis; M3 holes tapped)",
     bx(*EP_X, -EP_Y, EP_Y, REAR_TOP, EP_TOP).union(bx(TONGUE[0][0], EP_X[1], -TONGUE[1], TONGUE[1], REAR_TOP, REAR_TOP + EP_T)), ALU, "cut")
HUBS = {}
for s_, nm, sku in ((1, "control_hub", "REV-31-1595 Control Hub"), (-1, "expansion_hub", "REV-31-1153 Expansion Hub")):
    y0, y1 = sorted((s_ * HUB_IN, s_ * (HUB_IN + HUB[0]))); x0, x1 = EP_X[0] - HUB[2], EP_X[0]; z0, z1 = HUB_Z0, HUB_Z0 + HUB[1]
    tab = 11 * MM
    body = bx(x0, x1, y0, y1, z0, z1)
    for cy in (y0, y1 - tab):
        for cz in (z0, z1 - tab): body = body.cut(bx(x0 - 0.01, x1 - HUB_TAB * MM, cy, cy + tab, cz, cz + tab))
    yc, zc = (y0 + y1) / 2, (z0 + z1) / 2
    holes = [(yc + dy * MM, zc + dz * MM) for dy in (-64, 64) for dz in (-44, 44)]
    for hy, hz in holes: body = body.cut(cylx(hy, hz, 3.4 * MM, x0 - 0.1, x1 + 0.1))
    hn = f"{nm} ({sku}: on the plate's back face, ports out the back; four M3 through its corner tabs)"
    part(elec, hn, body, (0.24, 0.25, 0.27), "buy"); HUBS[nm] = (hn, holes)
    c = bolt(elec, f"hub_{nm}", f"the {nm.replace('_', ' ')} to the electronics plate (from behind, into the plate's tapped holes)", [(x1 - HUB_TAB * MM, hy, hz) for hy, hz in holes], (1, 0, 0), HUB_TAB, d=3, nut=False, tapped=EP_T * IN, min_engage=3, into="elec_plate", through=(nm,), service="its cover off first (one thumb screw)")
    drill(elec, [nm], c)
part(elec, "battery (REV-31-1302 12 V Slim Battery: drops into its cradle from above, a hook-and-loop strap over the top)", bx(*BAT_X, -BAT[0] / 2, BAT[0] / 2, *BAT_Z), (0.10, 0.10, 0.11), "buy")
cr = bx(*CR_X, -CR_W - 0.1, CR_W + 0.1, *CR_FLOOR)                                      # floor
for s_ in (-1, 1): cr = cr.union(bx(*CR_X, *sorted((s_ * CR_W, s_ * (CR_W + 0.1))), CR_FLOOR[0], 8.0))   # side walls
cr = cr.union(bx(CR_X[0], CR_X[0] + 0.1, -CR_W - 0.1, CR_W + 0.1, CR_FLOOR[0], 7.3))  # back lip
for s_ in (-1, 1): cr = cr.cut(bx(CR_X[0] + 0.25, CR_X[1] - 0.25, *sorted((s_ * (CR_W - 0.05), s_ * (CR_W + 0.15))), 7.45, 7.7))   # strap slots
part(elec, "battery_cradle (print PETG: floor, side walls, back lip and strap slots; held down by the tongue's front screws)", cr, (0.95, 0.55, 0.15), "print")
TH = [(x, y, CR_FLOOR[1] - 4.6 * MM) for x, y in TONGUE_HOLES if x > -6.5] 
for x, y, z in TH:
    k_ = next(k for k in elec if k.startswith("battery_cradle"))
    wp_, col_, kind_ = elec[k_]; elec[k_] = (wp_.cut(cylz(x, y, 8 * MM, z, CR_FLOOR[1] + 0.01)), col_, kind_)    # counterbores: the heads sit below the battery
c = bolt(elec, "elec_tongue_front", "the plate's tongue and the battery cradle to the rear angle's top flange (nuts under the flange, from below)", TH, (0, 0, -1), (CR_FLOOR[1] - 4.6 * MM - CR_FLOOR[0]) * IN + EP_T * IN + 2.5, nut=True, through=("battery_cradle", "elec_plate"), service="battery and cradle out first")
drill(elec, ["battery_cradle", "elec_plate"], c)
c = bolt(elec, "elec_tongue_rear", "the plate's tongue to the rear channel's top flange (nuts under the flange)", [(x, y, REAR_TOP + EP_T) for x, y in TONGUE_HOLES if x < -6.5], (0, 0, -1), EP_T * IN + 2.5, nut=True, through=("elec_plate",))
drill(elec, ["elec_plate"], c)
SW = ((EP_X[1] + 0.1, EP_X[1] + 0.1 + 0.55), (-0.6, 0.6), (9.90, 10.75))                   # the switch's body (X, Y, z), its rocker on top
part(elec, "power_switch (REV-31-1387 Switch Cable and Bracket's rocker: on top, under its holder's roof; a finger reaches it from behind)", bx(*SW[0], *SW[1], *SW[2]).union(bx(SW[0][0] + 0.12, SW[0][1] - 0.12, -0.35, 0.35, SW[2][1], SW[2][1] + 0.12)), RED, "buy")
sh = bx(EP_X[1], SW[0][1] + 0.08, -0.72, 0.72, 9.80, SW[2][1]).cut(bx(SW[0][0], SW[0][1], SW[1][0], SW[1][1], SW[2][0], SW[2][1] + 0.1))
sh = sh.union(bx(EP_X[1], EP_X[1] + 3 * MM, -1.15, 1.15, 9.95, 10.45))
part(elec, "switch_holder (print PETG: the switch presses into its pocket; two M3 from the front into the plate's tapped holes)", sh, (0.95, 0.55, 0.15), "print")
c = bolt(elec, "switch_holder", "the switch holder to the electronics plate (from the front)", [(EP_X[1] + 3 * MM, s_ * 0.95, 10.20) for s_ in (-1, 1)], (-1, 0, 0), 3.0, d=3, nut=False, tapped=EP_T * IN, min_engage=3, into="elec_plate", through=("switch_holder",))
drill(elec, ["switch_holder"], c)
# the switch's roof: a ball landing on the bay can't reach the rocker; a finger reaches in over the plate's top edge from behind
ROOF_Z = (11.20, 11.32)
roof = bx(EP_X[1], SW[0][1] + 0.08, -0.72, 0.72, *ROOF_Z)
for y_ in ((-0.72, -0.62), (0.62, 0.72)): roof = roof.union(bx(EP_X[1], SW[0][1] + 0.08, *y_, SW[2][1], ROOF_Z[1]))
roof = roof.union(bx(SW[0][1], SW[0][1] + 0.08, -0.72, 0.72, SW[2][1], ROOF_Z[1]))
k_ = next(k for k in elec if k.startswith("switch_holder"))
wp_, col_, kind_ = elec[k_]; elec[k_] = (wp_.union(roof), col_, kind_)
# the hubs' covers: 1/16 in clear polycarbonate, bent twice; each hooks over the plate's top edge and drops behind its hub
# (cables leave by the open bottom and ends; the LEDs show through). One M3 thumb screw through the front lip holds each:
# undo it from above, lift the cover off
CV_T = 1 / 16
CV_BACK = -7.50                                      # inside the frame's back face (X -7.564); 0.58 in behind the hubs' faces for the plugs
CV_Z0, CV_ZT = 6.60, EP_TOP + 0.02                   # the open bottom; the top's underside
CV_LIP = 10.05                                       # the front lip's bottom, on the plate's front face
for s_, nm in ((1, "control_hub"), (-1, "expansion_hub")):
    y0, y1 = sorted((s_ * (HUB_IN - 0.05), s_ * (HUB_IN + HUB[0] + 0.05)))
    cv = bx(CV_BACK, CV_BACK + CV_T, y0, y1, CV_Z0, CV_ZT + CV_T)                      # back
    cv = cv.union(bx(CV_BACK, EP_X[1] + CV_T, y0, y1, CV_ZT, CV_ZT + CV_T))           # top
    cv = cv.union(bx(EP_X[1], EP_X[1] + CV_T, y0, y1, CV_LIP, CV_ZT + CV_T))          # front lip
    part(elec, f"{nm}_cover (1/16 in clear polycarbonate, bent twice: hooks over the plate's top edge, one thumb screw)", cv, POLY, "cut")
    yc = s_ * (HUB_IN + HUB[0] / 2)
    c = bolt(elec, f"{nm}_cover", f"the {nm.replace('_', ' ')}'s cover to the plate's front face (from the front; a knurled M3 thumb screw, no tool)", [(EP_X[1] + CV_T, yc, 10.45)], (-1, 0, 0), CV_T * IN, d=3, nut=False, tapped=EP_T * IN + 1.0, min_engage=3, into="elec_plate", through=(f"{nm}_cover",))   # its tip ends just above the hub, behind the plate
    drill(elec, [f"{nm}_cover"], c)
# the OctoQuad (Digital Chicken Labs, FTC Edition, size approximate): reads both turret encoders' pulse widths; on the
# electronics plate's front face beside the battery, its connectors up, reached from above; to the Control Hub's I2C bus 2
OQ = ((EP_X[1], EP_X[1] + 13 * MM), (3.0, 3.0 + 61 * MM), (8.45, 8.45 + 38 * MM))   # high, so its screws' keys pass over the launcher's rear channel and the turret ring
part(elec, "octoquad (Digital Chicken Labs OctoQuad FTC Edition, size approximate: the turret encoders to I2C)", bx(*OQ[0], *OQ[1], *OQ[2]), (0.15, 0.3, 0.55), "buy")
oqh = bx(EP_X[1], OQ[0][1] + 2 * MM, OQ[1][0] - 2.5 * MM, OQ[1][1] + 2.5 * MM, OQ[2][0] - 2.5 * MM, OQ[2][0] + 0.5)   # a cup for its lower half
oqh = oqh.cut(bx(OQ[0][0] - 0.1, OQ[0][1] + 0.01, OQ[1][0] - 0.01, OQ[1][1] + 0.01, OQ[2][0], OQ[2][0] + 1))
oqh = oqh.cut(bx(OQ[0][0] + 3 * MM, OQ[0][1] + 3 * MM, OQ[1][0] + 0.3, OQ[1][1] - 0.3, OQ[2][0] - 0.2, OQ[2][0] + 1))  # its front open, for the label
for y0_, y1_ in ((OQ[1][0] - 0.35, OQ[1][0] - 2.5 * MM), (OQ[1][1] + 2.5 * MM, OQ[1][1] + 0.35)):
    oqh = oqh.union(bx(EP_X[1], EP_X[1] + 3 * MM, y0_, y1_, OQ[2][0] - 2.5 * MM, OQ[2][1]))   # the side ears, up to its top
part(elec, "octoquad_holder (print PETG: a cup for the OctoQuad on the plate's front face, two ears screwed to the plate; a strap over its top)", oqh, (0.95, 0.55, 0.15), "print")
c = bolt(elec, "octoquad_holder", "the OctoQuad holder to the electronics plate's front face (from the front, over the launcher's rear channel)", [(EP_X[1] + 3 * MM, y_, OQ[2][1] - 0.15) for y_ in (OQ[1][0] - 0.22, OQ[1][1] + 0.22)], (-1, 0, 0), 3.0, d=3, nut=False, tapped=EP_T * IN, min_engage=3, into="elec_plate", through=("octoquad_holder",))
drill(elec, ["octoquad_holder"], c)
# the Pinpoint odometry computer (goBILDA 3110-0002-0001, 42.5 x 40 x 16.6 mm, four threaded holes 32 mm square on its
# underside): flat, so its IMU's yaw axis is vertical, beside the left flywheel motor. A printed plate bolts down onto the
# top of the launcher's left side channel (two M4 into its 8 mm grid, nuts inside the channel) and reaches forward past the
# channel's front end (X 0.80), where the Pinpoint's four M4 go up from below through the plate. The pods' cables to it
# are short; it goes to the Control Hub's I2C bus 1
PP_C = (0.80 + 21.25 * MM + 0.06, SIDE_FLANGE_HOLES[1] - 14 * MM)  # its centre (X, Y): just past the channel's end, clear of the turret motor
PP_PL = (SIDE_TOP, SIDE_TOP + 0.20)                                  # the plate's z: on the channel's top face
PP_Z = (PP_PL[1], PP_PL[1] + 16.6 * MM)
PP_FL = [(-0.149 - k * 8 * MM, SIDE_FLANGE_HOLES[1]) for k in (0, 2)] # the channel's top holes the plate bolts to
PPN = "pinpoint (goBILDA 3110-0002-0001 Pinpoint odometry computer: flat on its printed plate; connectors toward the front and back)"
part(elec, PPN, bx(PP_C[0] - 21.25 * MM, PP_C[0] + 21.25 * MM, PP_C[1] - 20 * MM, PP_C[1] + 20 * MM, *PP_Z), (0.2, 0.2, 0.22), "buy")
vendor(PPN, "3110-0002-0001.step", (0, 0, -8), (0, 0, 1), (1, 0, 0), (PP_C[0], PP_C[1], PP_Z[0]), (0, 0, 1), (0, 1, 0))
ppl = bx(PP_FL[1][0] - 0.25, PP_C[0] + 21.25 * MM + 0.05, SIDE_FLANGE[1][0], SIDE_FLANGE[1][1], *PP_PL)              # the arm along the channel
ppl = ppl.union(bx(0.80 - 0.3, PP_C[0] + 21.25 * MM + 0.05, PP_C[1] - 20 * MM - 0.05, SIDE_FLANGE[1][1], *PP_PL))   # the Pinpoint's pad
part(elec, "pinpoint_plate (print PETG, 0.2 in: bolted down onto the left side channel's top, reaching past its front end)", ppl, (0.95, 0.55, 0.15), "print")
c = bolt(elec, "pinpoint", "the Pinpoint to its plate (from below, into its threaded holes)", [(PP_C[0] + dx * 16 * MM, PP_C[1] + dy * 16 * MM, PP_PL[0]) for dx in (-1, 1) for dy in (-1, 1)], (0, 0, 1), 0.20 * IN, nut=False, tapped=7.8, into="pinpoint \\(", through=("pinpoint_plate",))
drill(elec, ["pinpoint_plate"], c)
c = bolt(elec, "pinpoint_plate", "the Pinpoint's plate to the top of the left side channel (nuts inside the channel)", [(x, y, PP_PL[1]) for x, y in PP_FL], (0, 0, -1), 0.20 * IN + SIDE_FLANGE_T, nut=True, through=("pinpoint_plate",))
drill(elec, ["pinpoint_plate"], c)
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
threaded_holes(fixed); threaded_holes(launcher); threaded_holes(elec)

# ---- into the robot CAD's millimetres: (x, y, z)_CAD = (C + Y, F + Z, FACE - 7.56 in + X), all times 25.4 ----
MAT = cq.Matrix([[0, IN, 0, C], [0, 0, IN, F], [IN, 0, 0, FACE - CENTRE_BACK_IN * IN]])
def to_cad(wp):
    shp = wp.val() if len(wp.vals()) == 1 else cq.Compound.makeCompound(wp.vals())
    return shp.transformGeometry(MAT)
GROUPS = (("transfer, fixed", "fixed", fixed), ("launcher changes (the mentor's to agree)", "launcher", launcher), ("electronics bay", "elec", elec))
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
    print(len(fixed), "fixed,", len(launcher), "launcher,", len(elec), "electronics parts")
    print(f"feeder belt: centres {FEED_C * IN:.2f} mm, needs {belt_len(FEED_C, P16, P24) * IN:.1f} mm, belt {FEED_BELT[0]}; feeder axle Y {FEED_Y:.3f} z {FEED_Z:.3f}")
    Ls = [belt_len_path(belt_path(LD_LOOP(ROLLER_AXLE[1] + f))) * IN for f in (0, 0.325, 0.65, 0.975, 1.3)]
    print(f"lane drive belt: path {min(Ls):.1f}-{max(Ls):.1f} mm as the roller floats, belt {LD_BELT[0]} (stretch {100 * (min(Ls) / LD_BELT[0] - 1):.1f}-{100 * (max(Ls) / LD_BELT[0] - 1):.1f}%)")
    print(f"feeder arms: in {math.degrees(ARM_IN):.1f} deg, out {math.degrees(ARM_OUT):.1f} deg; grip tops P {GRIP_TOP_P:.2f} N {GRIP_TOP_N:.2f}")
    for s in (1, -1):
        fy = FLY_Y[0] if s > 0 else FLY_Y[1]; c = math.hypot(FM[s][0] - fy, FM[s][1] - FLY_Z)
        print(f"flywheel belt {'L' if s > 0 else 'R'}: motor at Y {FM[s][0]:.3f} z {FM[s][1]:.3f}, needs {belt_len(c, P24, P16) * IN:.1f} mm, belt {FLY_BELT[s][0]}")
