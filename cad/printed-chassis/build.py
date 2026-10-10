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
    corner_XX  four drive corners, all goBILDA: wheel shaft in the rail and an outer pattern plate, motor on a vertical
               pattern plate above the axle, belted 1:1
    odometry   the Pinpoint's two goBILDA odometry pods on printed adapters
    front_X    the two fixed flaps, which are also the side plates carrying the extractor's stubs
    extractor  the FLOWER extractor (cad/intake-b's geometry, its arms printed)
    intake     the roller on two printed swing arms; its motor rides the right arm's tail
    lane       ramp, walls, five roller shafts, a sprung ceiling of free rollers, floor and backstop
    launcher   flywheel cassettes, feeder and its motor, pad, top plate, goBILDA's turret kit with cad/transfer's servo and
               two-encoder drive under its drive gear, hood
    elec       tray, Control Hub, Expansion Hub, battery, Limelight mast
"""
import math, os, re, sys
import cadquery as cq

HERE = os.path.dirname(os.path.abspath(__file__))
MM = 1 / 25.4
CHECK_ONLY = os.environ.get("CHECK_ONLY") == "1"
LM_DIR = os.path.join(HERE, ".cache", "launcher")    # the mentor's launcher module, cached by launcher_module.py
USE_LM = os.path.exists(os.path.join(LM_DIR, "index.json")) and os.environ.get("NO_LAUNCHER_MODULE") != "1"
DM_DIR = os.path.join(HERE, ".cache", "drive")       # the mentor's drive corners, cached by drive_module.py
USE_DM = os.path.exists(os.path.join(DM_DIR, "index.json")) and os.environ.get("NO_DRIVE_MODULE") != "1"
DM_LAYOUT = USE_DM or os.environ.get("DM_LAYOUT") == "1"   # the frame laid out for his corners (drive_module.py places them by it)

# ---------------------------------------------------------------- the outline the Auto asks for
FACE, BACK = 7.3, -7.05                 # body 14.35 in (the back gives the start outline's margin; the face and the flaps stay put)
HALF_W = 7.62                           # 15.24 in wide over the pods, the simulator's frameWidthIn
FLAP_TIP = (FACE + 3.4, 8.875)          # the simulator's flap: hinged at the front corner, tip 3.4 ahead (1.255 out, inside the margin)
R102 = 18.0
START_CUBE = R102 - 0.25                # designed to 17.75 over everything: 1/8 in a side for print error, faces, screw heads, cables
BED = (180.0, 180.0, 180.0)             # every printed part within 180 mm each way: a 210 mm bed less a brim, and less warp

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
RAIL_OUT = 5.35 if DM_LAYOUT else 5.0      # the rails' webs' outer faces, |Y| (5.35: the mentor's track, his corners outside them)
RAIL_IN = RAIL_OUT - CH_LOW             # their flanges' inner edges (4.53): low-side, so the feeder's wheels pass over them
PITCH = 24 * MM                         # goBILDA's pattern: a 14 mm hole every 24 mm along a channel or plate
PLATE_T = 2.5 * MM                      # 1123 pattern plates
RAIL_Z = (WHEEL_R - CH / 2, WHEEL_R + CH / 2)   # on its side, open inward, its holes' row at the axles' height
RAIL_BACK = 0.15 if DM_LAYOUT else 0.0     # with his corners the rails sit 0.15 in back from the face, so his rear motors clear the launcher
RAIL_CX = FACE - RAIL_BACK - WHEEL_R - 5 * PITCH   # the rail's middle hole; the front axle five holes ahead, the rear six behind
RAIL_X = (FACE - RAIL_BACK - 360 * MM, FACE - RAIL_BACK) if DM_LAYOUT else (RAIL_CX - 168 * MM, RAIL_CX + 168 * MM)   # 1121-0014-0360 (his corners' rear motor
                                        # mounts sit 1.9 in behind the rear axle), or 0013-0336: the front end at the face
# ---------------------------------------------------------------- drive pods: identical, the front ones' motors higher (over the lane)
# Each corner, as the mentor's (doc/robot-cad.md) plus cad/robot-addons' outer plate, all goBILDA: the wheel's shaft in a
# bearing in the rail's web and one in an outer pattern plate on 56 mm standoffs; its pulley inboard of the wheel; the motor
# straight above the axle, its face screwed to a vertical pattern plate on the rail, so the belt's centres are a whole
# number of pattern holes and nothing is drilled.
AXLE = ({1: RAIL_CX + 4 * PITCH, -1: RAIL_CX - 6 * PITCH} if DM_LAYOUT else   # his wheelbase, 240 mm: the front wheels a hole back, as his,
        {1: RAIL_CX + 5 * PITCH, -1: RAIL_CX - 6 * PITCH})                         # so the rails' front ends carry the flaps' root blocks
UCH_FRONT = AXLE[1] - 1.55              # the front of his front drive motor's 1-hole U-channel mount (X 3.83 with the axle at 5.41)
MPLATE_Y = (RAIL_OUT, RAIL_OUT + PLATE_T)                 # the motor plates, on the rails' webs' outer faces
PULLEY_Y = (MPLATE_Y[1] + 0.05, MPLATE_Y[1] + 0.05 + PULLEY_W)   # the belt's plane, between the motor plate and the wheel
WHEEL_Y = (PULLEY_Y[1] + 0.05, PULLEY_Y[1] + 0.05 + WHEEL_W)
OPLATE_Y = (MPLATE_Y[1] + 56 * MM, MPLATE_Y[1] + 56 * MM + PLATE_T)   # the outer plates, on 56 mm standoffs
DRIVE_HOLES = {1: 5, -1: 3}             # the motor this many holes above the axle: front over the lane's ceiling (120 mm); rear 72 mm
                                        # (two holes up, its shaft's end touches the wheel)
DRIVE_BELT = {1: 360, -1: 265}          # 2 x centres + 120: front exact; rear 0.5 mm long (CHECK its tension on the robot)
DRIVE_MOTOR = {sx: (AXLE[sx], WHEEL_R + DRIVE_HOLES[sx] * PITCH) for sx in (1, -1)}
MPLATE = {1: (5, "1123-0048-0144"), -1: (4, "1123-0048-0144, cut to 4 holes (120 mm) so it stays under the launcher's belts")}   # the axle at its lowest hole, the motor at its top one

# ---------------------------------------------------------------- intake
ROLL_R = 24 * MM                        # goBILDA 48 mm Gecko wheels, as the mentor's roller (no vector wheels: his star wheels centre)
ROLL = (FACE + 1.0, 2.4 + ROLL_R)       # axle at rest: bottom 2.4 in off the tiles, ahead of the face on the swing arms
ROLL_HALF = 4.35                        # the wheels span the mouth the star wheels can reach (+-4.9; his mouth is 9.76);
                                        # the shaft carries on to the pulley at 6.55..6.9 and the arms at 6.95..7.25
# ---------------------------------------------------------------- the mentor's star wheels (his 9 Oct Robot.step, read by the CAD chat)
STAR_R, STAR_W = 3.5 / 2, 0.5           # "3.5in OD 7mm Hex Bore": a flexible 12-flap star, lying flat (axis vertical); vendor TBD
STAR = (FACE - 2.36, 3.15)              # its centre: 2.36 in behind the face, 3.15 either side (tips 2.80 apart: a NECTAR bends each 0.41)
STAR_Z = 3.2                            # its mid-plane: his sit at z 1.84, at a ball on the floor's middle; here the balls are up the
                                        # ramp (bottom z 1.3) where the stars are, so they sit just over the rails: a NECTAR's middle (3.11), 0.5 above a POLLEN's
                                        # (his sit 0.44 above a POLLEN's)
SV_Z = STAR_Z + STAR_W / 2 + 0.05 + 14 * MM + 0.1   # the star servo's underside: over the clutch's printed hub
STAR_FLEX = 0.08                        # clearance round each star; its flaps bend toward the lane, where only pieces are (how far: push one with a NECTAR)
ARM_Y = (5.0, 5.3)                      # inside the wheels (his belts run outside them), over the rails
BELT_Y = (4.4, 4.99)
PIVOT = (4.3, 4.05)                     # the arms' pivot: just ahead of the front drive motors' mounts, level with the roller's mid-float
FLOAT = 1.3                             # the roller rises this much (a NECTAR passes under)
ROLLER_BELT = 275
INTAKE_MOTOR = at(ROLL, (7.0, 6.1), centres(ROLLER_BELT))   # on the right arm, above the roller: its belt stays ahead of the star wheels

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
BACKSTOP_SLOT = 0.2                     # +-, on the robot: set it with real pieces
COL_X = BACKSTOP_X + 1.765              # the launch column: the first ball's centre against the backstop, under the turret's axis
LANE_SHAFTS = [ROLL[0] - 3.16 - 1.4 * k for k in range(5)]
RAMP = ((FACE + 0.44, 0.05), (FACE - 1.86, LANE_BOTTOM))

# ---------------------------------------------------------------- launcher (cad/transfer's, about the column)
FLY_R, FLY_Y, FLY_Z = 96 / 2 * MM, 2.85, 6.65
FLY_X = (COL_X - 0.95, COL_X + 0.95)
FLY_TRAVEL = RN - RP                    # each flywheel module's spring travel: a POLLEN at rest, a NECTAR pushes each side out 0.41
                                        # (the same squeeze on both: rest gap = 2 RP - squeeze; his module's CAD has 1.93, the rig sets it)
FLY_SPRING = dict(rate=None, preload=None)   # lbf/in, lbf: the launcher rig sets them (README, Open 3)
# the mentor's flywheel parts that ride the sprung modules (each side slides out in Y, its motor with it); his plates stay
# put as the frame (slotted for the shafts' travel), and the feeder's yoke turns on a fixed stub ahead of the left module
FLY_MOVES = re.compile(r"^(96mm_Gecko|96mm_Steel_Shaft|8mm_Spacer_|part_piece|8mm_REX_Hyper_Hub|1505-0032-0160|8x14x5mm_Bearing_-_Round_Bore_GB_[3-6]_|"
                       r"flywheel_(motor|pulley|belt)_)")
FLY_PLATES = re.compile(r"^Hole_Lowside_U-Channel_GB_(9|10|11|12)_")
FEED = (2.87, 3.30, 36 * MM)            # feeder axle Y, z, radius (72 mm Gecko)
FLY_BELT = 315
FLY_MOTOR = (FLY_Y + math.sqrt(centres(FLY_BELT) ** 2 - (7.0 - FLY_Z) ** 2), 7.0)                 # |Y|, z; along X, face 0.6 behind the column, shaft back
FEED_BELT = 225
FEED_MOTOR = at((2.87, 3.30), (3.9, 5.0), centres(FEED_BELT))   # Y, z; along X ahead of the feeder, shaft back, belted 1:1
KIT_Z = 8.85                            # goBILDA 3208-0004-0001 gear-driven turret kit (its STEP): a 144 mm square base, 20 mm tall,
KIT_HALF, KIT_H = 72 * MM, 20 * MM      # 176T ring, 105 mm bore (the ball's path); its 64T drive gear 96 mm from the axis, the
KIT_LOBE = 122.4 * MM                   # base reaching 122.4 mm out that way. The drive gear points back, clear of the tray.
KIT_BORE = 105 / 2 * MM
KIT_ANG = -40.0                         # the drive gear points back and 40 deg to the left, so the kit's lobe stays inside the back
                                        # (its square's front corner then reaches X 1.63 at Y +0.35: the tray is notched round it)
_ka = math.radians(KIT_ANG)
DRIVE_GEAR = (COL_X - 96 * MM * math.cos(_ka), -96 * MM * math.sin(_ka))   # the 64T's axis; encoder A rides its shaft, B a 36T meshing it
ENC_B = (DRIVE_GEAR[0] + 40 * MM * math.sin(_ka), DRIVE_GEAR[1] - 40 * MM * math.cos(_ka))   # 40 mm off, square to the ring's direction, toward the back

FRONT_CROSS_X, FRONT_CROSS_Z = AXLE[1] - CH / 2, 7.4   # between the front motor plates, over the front drive motors
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
def solid(w): return w.val() if hasattr(w, "val") else w

_BB = {}
def bb(w):
    k = id(w)
    if k not in _BB:
        b = solid(w).BoundingBox(); _BB[k] = ((b.xmin, b.xmax, b.ymin, b.ymax, b.zmin, b.zmax), w)   # keep w alive so its id stays unique
    return _BB[k][0]

PARTS = {}          # name -> dict(shape, module, kind ('print' | 'buy' | 'cut'), what, moves)

def part(module, name, shape, kind, what, moves=None):
    assert name not in PARTS, name
    PARTS[name] = dict(shape=shape, module=module, kind=kind, what=what, moves=moves)

SIDES = ((1, "L"), (-1, "R"))
def mir(wp, s): return wp if s > 0 else wp.mirror("XZ")

# ---- frame
for s, n in SIDES:
    part("frame", f"rail_{n}", mir(channel_x(*RAIL_X, RAIL_OUT, True, RAIL_Z[0], CH_LOW), s), "buy", "goBILDA 1121-0014-0360 low-side U-channel (360 mm), his track" if USE_DM else "goBILDA 1121-0013-0336 low-side U-channel, 13 hole (336 mm), as today's rails")
if not USE_DM:                          # his rear motors' 1107 channel ties the rails at the back
  part("frame", "rear_cross", box(RAIL_X[0], RAIL_X[0] + CH_T, -120 * MM, 120 * MM, *RAIL_Z).union(box(RAIL_X[0], RAIL_X[0] + CH_LOW, -120 * MM, 120 * MM, RAIL_Z[0], RAIL_Z[0] + CH_T)).union(
     box(RAIL_X[0], RAIL_X[0] + CH_LOW, -120 * MM, 120 * MM, RAIL_Z[1] - CH_T, RAIL_Z[1])), "buy",
     "goBILDA 1121-0009-0240 low-side U-channel (240 mm), standing at the rails' back ends, between their webs on goBILDA pattern brackets (CHECK the bracket)")
if not USE_DM:
  part("frame", "front_cross", channel_y(-120 * MM, 120 * MM, FRONT_CROSS_X, FRONT_CROSS_Z, open_down=True), "buy",
     "goBILDA 1120-0009-0240 U-channel (240 mm), over the lane and the front drive motors, between the front motor plates on goBILDA pattern brackets (no uprights: the mouth stays open to its full width)")

# ---- drive corners: the mentor's (drive_module.py), each placed by its own axle; without its cache, this file's own
if USE_DM:
    import json
    from OCP.BRepTools import BRepTools
    from OCP.BRep import BRep_Builder
    from OCP.TopoDS import TopoDS_Shape
    for e in json.load(open(os.path.join(DM_DIR, "index.json")))["parts"]:
        sh = TopoDS_Shape(); BRepTools.Read_s(sh, os.path.join(DM_DIR, e["file"]), BRep_Builder())
        part(f"corner_{e['corner']}", "dm_" + e["file"][:-5], cq.Workplane().add(cq.Shape.cast(sh)), "buy", f"the mentor's drive corner {e['corner']}: {e['name']}")
for s_, n in (() if USE_DM else SIDES):
    op = box(*RAIL_X, *OPLATE_Y, *RAIL_Z)
    part("frame", f"outer_plate_{n}", mir(op, s_), "buy", "goBILDA 1123-0048-0336 pattern plate (1 x 13 hole): the wheels' outer bearings, on 56 mm standoffs")
    for k in (-3, 2):                       # between the wheels, where no belt runs
        for dz in (-16 * MM, 16 * MM):
            part("frame", f"standoff_{n}_{k}_{'hi' if dz > 0 else 'lo'}", mir(cyl("y", (RAIL_CX + k * PITCH, WHEEL_R + dz), 6 * MM, MPLATE_Y[1], OPLATE_Y[0]), s_), "buy",
                 "goBILDA 1501-0006-0560 M4 standoff, 56 mm")
for sx, fx in (() if USE_DM else ((1, "F"), (-1, "B"))):
    ax = AXLE[sx]; mx, mz = DRIVE_MOTOR[sx]; nh, pn = MPLATE[sx]
    for s, n in SIDES:
        nm = fx + n
        mp = box(ax - CH / 2, ax + CH / 2, *MPLATE_Y, RAIL_Z[0], RAIL_Z[0] + nh * PITCH + CH - PITCH)
        part(f"corner_{nm}", f"motor_plate_{nm}", mir(mp, s), "buy", f"goBILDA {pn} pattern plate (1 x {nh} hole), standing on the rail's web: the drive motor screws to it, {DRIVE_HOLES[sx]} holes above the axle")
        part(f"corner_{nm}", f"wheel_{nm}", mir(cyl("y", (ax, WHEEL_R), 2 * WHEEL_R, *WHEEL_Y), s), "buy", "goBILDA 3213-3606-0002 96 mm mecanum (set of 4)")
        part(f"corner_{nm}", f"wheel_shaft_{nm}", mir(cyl("y", (ax, WHEEL_R), 8 * MM, RAIL_OUT - CH_T, OPLATE_Y[1] + 0.05), s), "buy",
             "goBILDA 2106-4008-0800 8mm REX shaft, 80 mm, in a 1611 bearing in the rail's web and one in the outer plate; spacers and an e-clip")
        part(f"corner_{nm}", f"drive_motor_{nm}", mir(motor_y(MPLATE_Y[0], (mx, mz), 1), s), "buy", "goBILDA 5203-2402-0014 Yellow Jacket, 435 RPM (13.7:1), as last season's drive (DECODE's Pedro constants: 537.7 ticks a rev, 96 mm wheels, 1:1)")
        part(f"corner_{nm}", f"drive_belt_{nm}", mir(belt_y((ax, WHEEL_R), (mx, mz), PULLEY24_D, *PULLEY_Y), s), "buy",
             f"goBILDA 3417-4008-0024 24T HTD5 pulleys x2, 3412-0009-0{DRIVE_BELT[sx]} belt")
# ---- odometry pods (goBILDA 4-bar, for 96 mm wheels; the Pinpoint's): a forward pod on the left, a strafe pod on the right
POD_H, POD_W, POD_D = 69.5 * MM, 43 * MM, 41.5 * MM     # floor to the top of its block; across its block; mount face to wheel side
ODO_FWD_X = 2.15                        # both pods on the right: the left side under the launcher has its feeder belt and gate servo
part("odometry", "odo_pod_forward", box(ODO_FWD_X - POD_W / 2, ODO_FWD_X + POD_W / 2, -(RAIL_IN - 0.15), -(RAIL_IN - 0.15) + POD_D, 0, POD_H), "buy",
     "goBILDA 3110-0001-0002 4-bar odometry pod (96 mm drive wheel version), its wheel rolling along X, on a printed adapter from the right rail")
part("odometry", "odo_pod_strafe", box(-0.7, -0.7 + POD_D, -(RAIL_IN - 0.15), -(RAIL_IN - 0.15) + POD_W, 0, POD_H), "buy",
     "goBILDA 3110-0001-0002 4-bar odometry pod, its wheel rolling along Y, on a printed adapter from the right rail")
for n, x0, x1 in (("F", ODO_FWD_X - POD_W / 2 - 0.1, ODO_FWD_X + POD_W / 2 + 0.1), ("S", -0.8, -0.7 + POD_D + 0.1)):
    ad = box(x0, x1, -(RAIL_IN - 0.15), -RAIL_IN, RAIL_Z[0] + CH_T, RAIL_Z[1] - CH_T)
    part("odometry", f"odo_adapter_{n}", ad, "print", "PETG: the odometry pod's adapter, inside the rail's web (the pod's offsets are measured on the robot, for the Pinpoint)")

# ---- the fixed flaps: also the side plates that carry the extractor's stubs
for s, n in SIDES:
    root, tip = (FACE, HALF_W), FLAP_TIP
    ang = math.atan2(tip[1] - root[1], tip[0] - root[0]); L = math.hypot(tip[0] - root[0], tip[1] - root[1])
    T, FACE_T = 0.25, 0.2                  # a 5 mm face plate (3 mm TPU on a 2 mm PETG backer), screwed to the flap's inner side
    plate = box(0, L, -T, 0, 0.25, 4.25).union(box(L * (2.4 - 0.5) / 3.4, L * (2.4 + 0.5) / 3.4, -T, 0, 4.25, 5.0))   # taller round the extractor's stub
    plate = plate.rotate((0, 0, 0), (0, 0, 1), math.degrees(ang)).translate((root[0], root[1], 0))
    plate = plate.union(box(AXLE[1] + WHEEL_R + 0.1, FACE + 0.45, RAIL_OUT + 0.01, HALF_W, RAIL_Z[0], 4.05))   # the root: ahead of the wheel, against the rail's web
    foam = box(L * 0.3, L, -T - FACE_T, -T, 0.25, 4.25).rotate((0, 0, 0), (0, 0, 1), math.degrees(ang)).translate((root[0], root[1], 0))
    trim = box(-20, FLAP_TIP[0], -20, 20, -20, 20)
    plate, foam = plate.intersect(trim), foam.intersect(trim)
    part(f"front_{n}", f"flap_{n}", mir(plate, s), "print", "PETG-CF, 6 mm (material: it carries the extractor's stub, so it must not flex): the fixed flap and the extractor's side plate; bolts to the front pod")
    part(f"front_{n}", f"flap_face_{n}", mir(foam, s), "print", "swappable face plate on three M3 screws into the flap's heat-set inserts: TPU 95A printed on a PETG backer (dual-material) by default; a bare PETG plate or foam glued to a backer for the drop test, all the same outline")

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
    out["roller"] = (cyl("y", ROLL, 2 * ROLL_R, -ROLL_HALF, ROLL_HALF), "buy", "goBILDA 3632-4008-0048 48 mm Gecko wheels, as the mentor's roller (about 10 across the 9.8 in span)")
    out["roller_shaft"] = (cyl("y", ROLL, 8 * MM, -ARM_Y[1], ARM_Y[1]), "buy", "goBILDA 8mm REX shaft, cut to 371 mm (CHECK)")
    for s, n in SIDES:
        y0, y1 = ARM_Y
        arm = slab_y(_strip(PIVOT, ROLL, 0.35), y0, y1).union(cyl("y", ROLL, 0.8, y0, y1)).union(cyl("y", PIVOT, 0.8, y0, y1))
        if s < 0: arm = arm.union(slab_y(_strip(ROLL, INTAKE_MOTOR, 0.4), y0, y1)).union(cyl("y", INTAKE_MOTOR, 1.1, y0, y1))   # up to the motor
        out[f"intake_arm_{n}"] = (mir(arm, s), "print", "PETG: swing arm, pivot to roller bearing (the right one also carries the motor)")
    out["roller_belt"] = (mir(belt_y(INTAKE_MOTOR, ROLL, PULLEY24_D, *BELT_Y), -1), "buy",
                          f"goBILDA 3417-4008-0024 24T HTD5 pulleys x2, 3412-0009-0{ROLLER_BELT} belt")
    out["intake_motor"] = (mir(motor_y(BELT_Y[0] - 0.02, INTAKE_MOTOR, 1, MOTOR_L1), -1), "buy", "goBILDA 5203-2402-0003 Yellow Jacket, 1620 RPM: the roller (about 170 in/s at its surface) and the lane")
    d = lift_deg(lift)
    return {k: (rot_y(w, PIVOT, d), kind, what) for k, (w, kind, what) in out.items()}
for k, (w, kind, what) in intake(0).items(): part("intake", k, w, kind, what, moves="intake")

# ---- lane
(rx0, rz0), (rx1, rz1) = RAMP
MOUTH_HALF = RAIL_IN - 0.08            # the ramp and the floor under the stars span the whole mouth, inside the rails' flanges, so a
                                        # piece taken in off-centre climbs to the lane's height too, where the stars reach its middle
MOUTH_FLOOR_X = (STAR[0] - STAR_R - 0.05, rx1)
for s_, n in SIDES:
    rmp = slab_y([(rx0, rz0), (rx1, rz1), (rx1, rz1 - 0.12), (rx0, rz0 - 0.05)], *((0, MOUTH_HALF) if s_ > 0 else (-MOUTH_HALF, 0)))
    rmp = rmp.cut(box(LANE_SHAFTS[0] - 0.55, LANE_SHAFTS[0] + 0.55, -1.05, 1.05, 0, 2))
    part("lane", f"ramp_{n}", rmp, "print", "PETG: half the ramp, the mouth's whole width")
    fl = box(*MOUTH_FLOOR_X, 0, s_ * MOUTH_HALF, LANE_BOTTOM - 0.13, LANE_BOTTOM)
    for x in LANE_SHAFTS[:2]: fl = fl.cut(box(x - 0.55, x + 0.55, -1.05, 1.05, 0, 2))     # the lane's first two rollers come up through it
    part("lane", f"mouth_floor_{n}", fl, "print", "PETG: half the floor under the star wheels, at the lane's height; carries lane shafts 0 and 1's bearings on hangers")
LANE_X = (COL_X + 1.4, FACE - 0.16)
for s, n in SIDES:
    xm = (LANE_SHAFTS[2] + LANE_SHAFTS[3]) / 2                    # split between two shafts, so each half holds whole bearings
    for h, (a, b) in (("front", (xm, MOUTH_FLOOR_X[0] - 0.02)), ("rear", (LANE_X[0], xm))):   # ahead of these, the mouth floor and the stars
        top = 2.6
        part("lane", f"lane_wall_{n}_{h}", mir(box(a, b, WALL_IN, WALL_IN + WALL_T, 0.3, top), s), "print", "PETG, 1/4 in: half a lane wall with its shafts' bearings; on two REX standoffs to the rail")
# ---- the mentor's star wheels, servo-driven through one-way bearings (drive TBD: his CAD draws none)
for s, n in SIDES:
    sx, sy = STAR[0], s * STAR[1]
    part("lane", f"star_{n}", cyl("z", (sx, sy), 2 * STAR_R, STAR_Z - STAR_W / 2, STAR_Z + STAR_W / 2), "buy",
         "SWYFT Intake Wheel 3.5 in, 7 mm hex (SR-INTAKEWHEEL-35-7mm, 4 for $24.99): 0.5 in wide, 30A TPE on a nylon core, cut by the team into 16 flaps, as the mentor's; on a 7 mm hex through a ~25 mm black adapter (TBD)")
    part("lane", f"star_shaft_{n}", cyl("z", (sx, sy), 8 * MM, STAR_Z - STAR_W / 2 - 0.1, STAR_Z + 0.9), "buy",
         "8 mm ROUND hardened shaft (ground steel, h6), vertical, from the servo: the clutch's rollers run on it, so not 8mm REX (its flats would let them slip)")
    part("lane", f"star_clutch_{n}", cyl("z", (sx, sy), 20 * MM, STAR_Z + STAR_W / 2 + 0.05, STAR_Z + STAR_W / 2 + 0.05 + 14 * MM), "print",
         "PETG hub, 13.9-13.95 mm press bore for the one-way clutch (the mentor's: Amazon Sankoly-US SK230309GZZC-6P, read as HF081412, 8 x 14 x 12 mm drawn cup; "
         "CHECK with calipers; free-wheel direction TBD), its other end driving the star's hex adapter: the star can't be pushed backwards, and a fast piece overruns it")
    sv = box(sx - 1.13, sx + 0.45, sy - 0.4, sy + 0.4, SV_Z, SV_Z + 37 * MM)    # its body back toward the drive motor, clear of the intake motor
    part("lane", f"star_servo_{n}", sv, "buy", "continuous servo over the star, its spline on the star's axis (TBD: the mentor's choice and its coupling; a goBILDA Speed servo drawn)")
    top = SV_Z + 37 * MM
    br = box(UCH_FRONT, sx + 0.5, 2.7, 4.95, top, top + 0.2)                                        # over the servo
    br = br.union(box(UCH_FRONT, 4.05, 4.4, 4.95, PIVOT[1] - 0.45, top))                             # down the U-channel mount's front face
    br = br.union(box(PIVOT[0] - 0.3, PIVOT[0] + 0.3, 4.75, 4.95, PIVOT[1] - 0.45, top))        # the arms' pivot, inboard of the arm
    br = br.union(box(UCH_FRONT, PIVOT[0] + 0.3, 4.75, 4.95, PIVOT[1] - 0.45, PIVOT[1] - 0.25))
    part("lane", f"front_bracket_{n}", mir(br, s), "print",
         "PETG: bolts to the front drive motor's U-channel mount; hangs the star's servo and carries the intake arm's pivot (an M5 shoulder screw)")
for k, x in enumerate(LANE_SHAFTS):
    part("lane", f"lane_shaft_{k}", cyl("y", (x, LANE_BOTTOM - 12 * MM), 8 * MM, -WALL_IN - WALL_T, WALL_IN + WALL_T), "buy", "goBILDA 8mm REX shaft, 144 mm (2106-4008-1440)")
    part("lane", f"lane_roller_{k}", cyl("y", (x, LANE_BOTTOM - 12 * MM), 24 * MM, (-1 if k % 2 else 0) * 1.0, (0 if k % 2 else 1) * 1.0), "print", "TPU 95A: 24 mm x 1 in lane roller")
CEIL_X = (-0.55, 4.5)                    # from just ahead of the launcher's front uprights (X -0.63) to the front drive motors
CEIL_Z = LANE_BOTTOM + 2 * RP - 0.2     # the free rollers' bottoms, at rest (a POLLEN presses them 0.2)
part("lane", "ceiling", box(*CEIL_X, -WALL_IN, WALL_IN, CEIL_Z + 0.8, CEIL_Z + 0.95), "print",
     "PETG: the sprung ceiling's frame, on pins in slotted posts (as cad/transfer), carrying free rollers")
for k in range(4):
    x = CEIL_X[0] + 0.5 + k * (CEIL_X[1] - CEIL_X[0] - 1.0) / 3
    part("lane", f"ceiling_roller_{k}", cyl("y", (x, CEIL_Z + 0.4), 0.8, -1.5, 1.5), "print",
         "PETG: a free-spinning ceiling roller on two 608 bearings (the ball moves at the lane's full tread speed, not half)")
if not USE_LM:                            # the launcher module brings its own floor and backstop (cad/transfer's)
  part("lane", "floor", box(BACKSTOP_X - 0.2, COL_X + 1.4, -1.7, 1.45, LANE_BOTTOM - 0.13, LANE_BOTTOM), "print", "PETG: the floor under the feeder and pad")
  part("lane", "backstop", box(BACKSTOP_X - 0.2, BACKSTOP_X, -1.2, 1.2, LANE_BOTTOM, 3.0), "print", "PETG: backstop, on +-0.2 in slots (set with real balls: the 4-piece count)")

# ---- launcher: the mentor's goBILDA launcher module with cad/transfer's changes (launcher_module.py), placed by the
# column; without its cache, this file's own stand-in
if USE_LM:
    import json
    from OCP.BRepTools import BRepTools
    from OCP.BRep import BRep_Builder
    from OCP.TopoDS import TopoDS_Shape
    for e in json.load(open(os.path.join(LM_DIR, "index.json")))["parts"]:
        sh = TopoDS_Shape(); BRepTools.Read_s(sh, os.path.join(LM_DIR, e["file"]), BRep_Builder())
        k = "lm_" + e["file"][:-5]
        w = cq.Workplane().add(cq.Shape.cast(sh)); mv = None
        if FLY_MOVES.match(e["file"]):
            b = bb(w); mv = "fly_L" if b[2] + b[3] > 0 else "fly_R"
        part("launcher", k, w, "buy", f"the mentor's launcher module: {e['name']}" + (" (rides the sprung module)" if mv else ""), mv)
else:
    for s, n in SIDES:
        fy = s * FLY_Y
        for k, (a, b) in enumerate(((FLY_X[0], FLY_X[0] + 0.8), (FLY_X[1] - 0.8, FLY_X[1]))):
            part("launcher", f"flywheel_{n}{k}", cyl("x", (fy, FLY_Z), 2 * FLY_R, a, b), "buy", "96 mm flywheel, as the mentor's launcher (CHECK the part)")
        part("launcher", f"fly_shaft_{n}", cyl("x", (fy, FLY_Z), 8 * MM, FLY_X[0] - 1.05, FLY_X[1] + 0.3), "buy", "goBILDA 8mm REX shaft, 120 mm")
        cas = box(FLY_X[1] + 0.05, FLY_X[1] + 0.3, s * 2.1, s * 5.4, 4.7, 8.6).union(box(FLY_X[0] - 0.3, FLY_X[0] - 0.05, s * 2.1, s * 5.4, 4.7, 8.6))   # inner edges clear of a NECTAR in the column
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
    TOP_X = (COL_X - 3.9, COL_X + 3.0)
    for s_, n in SIDES:
        half = box(*TOP_X, 0, s_ * 4.0, KIT_Z - 0.25, KIT_Z).cut(cyl("z", (COL_X, 0), 2 * KIT_BORE, 0, 20))
        part("launcher", f"top_plate_{n}", half, "print",
             "PETG-CF, 6 mm (material: the turret's base): half the launcher's top plate (two halves, bolted on the cassettes); the turret kit bolts across both by its own pattern")
    kit = box(COL_X - KIT_HALF, COL_X + KIT_HALF, -KIT_HALF, KIT_HALF, KIT_Z, KIT_Z + KIT_H).union(
          box(COL_X - KIT_LOBE, COL_X - KIT_HALF, -1.1, 1.1, KIT_Z, KIT_Z + KIT_H)).cut(cyl("z", (COL_X, 0), 2 * KIT_BORE, 0, 20))
    kit = kit.rotate((COL_X, 0, 0), (COL_X, 0, 1), KIT_ANG)
    part("launcher", "turret_kit", kit, "buy", "goBILDA 3208-0004-0001 gear-driven turret kit (176T ring, 64T drive gear, 105 mm bore), by its own mounting pattern; drawn as its envelope")
    gx, gy = DRIVE_GEAR; pz = KIT_Z - 0.25
    part("launcher", "turret_servo", box(gx + 0.1, gx + 0.9, gy - 0.75 - 40 * MM, gy - 0.75, pz - 37 * MM, pz), "buy",
         "goBILDA 2000-0025-0003 Speed servo, continuous, under the top plate: belted 1:1 (24T, 295 mm) to the 64T's shaft (cad/modules/turret-kit)")
    part("launcher", "turret_enc_A", cyl("z", DRIVE_GEAR, 1.4, pz - 0.75, pz), "buy", "REV-11-1271 Thru-Bore encoder on the 64T's shaft (2.75 turns a turret turn), in cad/modules/turret-kit's printed cradle")
    part("launcher", "turret_enc_B", cyl("z", ENC_B, 1.4, pz - 0.75, pz), "buy", "REV-11-1271 on a goBILDA 2303-4008-0036 36T meshing the 64T (4.89 turns): with A, the angle anywhere in 1178 deg; both on an OctoQuad, I2C bus 2")
HOOD_Z = 9.70 if USE_LM else KIT_Z + KIT_H   # on the turret's top (the module's kit is 9.68 at its top)
hood = cyl("z", (COL_X, 0), 2 * KIT_BORE + 0.4, HOOD_Z, 11.5).cut(cyl("z", (COL_X, 0), 2 * KIT_BORE, KIT_Z, 12))
part("launcher", "hood", hood.union(box(COL_X, COL_X + 2.4, -KIT_BORE - 0.2, KIT_BORE + 0.2, 11.5, 12.2)), "print", "PETG: the hood that turns the shot out (drawn as a tube and a lid)")

# ---- electronics
# Electronics (the mentor's CAD places none, CHECK with him): both hubs standing on edge in a printed rack on the rear
# drive motors' cross channel, their plugs to the back; the battery low under that channel, between the rails; the
# Limelight on a mast from the left front bracket, looking forward over everything.
EL_X = (AXLE[-1] - 1.75, AXLE[-1] - 0.75)                  # the hubs' X (1 in thick, standing), behind the launcher's rear channels
RACK_Z = 5.73 + 0.0                          # the 1107 channel's top
for s_, n in SIDES:
    part("elec", f"rack_{n}", box(EL_X[0] - 0.12, EL_X[1] + 0.05, 0, s_ * 5.75, RACK_Z, RACK_Z + 0.2).union(
         box(EL_X[0] - 0.12, EL_X[0] - 0.02, 0, s_ * 5.75, RACK_Z, RACK_Z + 0.2 + HUB[1])), "print",
         "PETG: half the electronics rack, on the rear cross channel: a shelf and a back wall the hub screws to")
part("elec", "control_hub", box(*EL_X, 0.05, 0.05 + HUB[0], RACK_Z + 0.2, RACK_Z + 0.2 + HUB[1]), "buy", "REV Control Hub, standing on edge, plugs to the back")
part("elec", "expansion_hub", box(*EL_X, -0.05 - HUB[0], -0.05, RACK_Z + 0.2, RACK_Z + 0.2 + HUB[1]), "buy", "REV Expansion Hub, standing on edge, plugs to the back")
part("elec", "battery", box(AXLE[-1] - 1.82, AXLE[-1] - 1.82 + BATTERY[2], -BATTERY[0] / 2, BATTERY[0] / 2, 0.45, 0.45 + BATTERY[1]), "buy",
     "Modern Robotics 12 V NiMH battery (the mentor's), on edge across the robot, low between the rails under the rear cross channel (CHECK its size)")
for s_, n in SIDES:
    part("elec", f"battery_cradle_{n}", box(AXLE[-1] - 1.87, AXLE[-1] - 1.87 + BATTERY[2] + 0.05, 0, s_ * (RAIL_IN - 0.02), 0.3, 0.45), "print",
         "PETG: half the battery's cradle, hung from the rails' bottom flanges")
for k, (z0, z1) in enumerate(((SV_Z + 0.2 + 37 * MM, 9.6), (9.6, 13.4))):
    part("elec", f"limelight_mast_{k}", box(4.4, 4.8, 3.0, 3.5, z0, z1), "print",
         "PETG: Limelight mast on the left front bracket, in two pieces (bed size); lens about 14 in up, 45 deg up (its offsets go in CameraMount)")
part("elec", "limelight", box(4.05, 5.15, 2.15, 4.35, 13.4, 14.8), "buy", "Limelight 3A")

# ================================================================ checks

def overlap(a, b, tol=1e-4):
    A, B = bb(a), bb(b)
    if any(A[2 * i] > B[2 * i + 1] - tol or B[2 * i] > A[2 * i + 1] - tol for i in range(3)): return 0.0
    try: return solid(a).intersect(solid(b)).Volume()
    except Exception: return -1.0

# parts that touch by design (a shaft in its part, a belt round its motor's shaft, a stack)
ALLOWED = [("wheel_shaft_", "wheel_"), ("wheel_shaft_", "rail_"), ("wheel_shaft_", "motor_plate_"), ("wheel_shaft_", "outer_plate_"),
           ("drive_belt_", "drive_motor_"), ("drive_belt_", "wheel_shaft_"), ("drive_motor_", "motor_plate_"), ("motor_plate_", "rail_"),
           ("standoff_", "outer_plate_"), ("standoff_", "motor_plate_"), ("standoff_", "rail_"), ("outer_plate_", "flap_"), ("intake_pivot_", "outer_plate_"),
           ("roller_shaft", "roller"), ("roller_shaft", "intake_arm_"), ("roller_belt", "intake_motor"), ("roller_belt", "roller_shaft"),
           ("lane_shaft_", "lane_roller_"), ("lane_shaft_", "lane_wall_"), ("fly_shaft_", "flywheel_"), ("fly_shaft_", "fly_cassette_"),
           ("fly_belt_", "fly_shaft_"), ("fly_belt_", "fly_motor_"), ("feeder_shaft", "feeder"), ("feeder_belt", "feeder_shaft"),
           ("feeder_belt", "feeder_motor"), ("ex_stub_", "flap_"), ("ex_stub_", "ex_arm_"), ("ex_cross", "ex_arm_"), ("ex_cross", "ex_block"),
           ("turret_kit", "top_plate_"), ("top_plate_", "top_plate_"), ("tray_", "tray_"), ("lane_wall_", "lane_wall_"), ("flap_face_", "flap_"),
           ("intake_motor", "intake_arm_R"), ("ex_servo", "flap_R"), ("rear_cross", "rail_"), ("front_cross", "motor_plate_F"),
           ("star_shaft_", "star_"), ("star_clutch_", "star_"), ("star_shaft_", "star_clutch_"), ("lane_shaft_", "mouth_floor_"), ("ramp_", "ramp_"), ("mouth_floor_", "mouth_floor_"), ("ramp_", "mouth_floor_"),
           ("star_bracket_", "front_cross"), ("star_bracket_", "star_servo_"), ("tray", "front_cross"), ("limelight", "limelight_mast"),
           ("dm_FL_motor", "lm_feeder_belt"),   # KNOWN: his left-front motor's can touches the transfer's feeder belt (0.0002 in^3) with
                                                # the front axles a hole back; the launcher's feeder is being redesigned (README, Open 2)
           ("dm_", "rail_"), ("front_bracket_", "dm_F"), ("front_bracket_", "star_servo_"), ("intake_arm_", "front_bracket_"),
           ("rack_", "dm_B"), ("rack_", "control_hub"), ("rack_", "expansion_hub"), ("rack_", "rack_"), ("battery_cradle_", "rail_"),
           ("battery_cradle_", "battery"), ("battery_cradle_", "battery_cradle_"), ("limelight_mast", "front_bracket_L"), ("limelight_mast_", "limelight_mast_"), ("flap_", "rail_"),
           ("odo_adapter_", "rail_"), ("odo_adapter_", "odo_pod_")]
def allowed(a, b):
    return any((a.startswith(p) and b.startswith(q)) or (b.startswith(p) and a.startswith(q)) for p, q in ALLOWED)

def clashes(shapes, only=None):
    shapes = dict(shapes)
    for s_, n in SIDES:                     # each star with room for its flaps to bend
        if f"star_{n}" in shapes:
            shapes[f"star_{n}"] = cyl("z", (STAR[0], s_ * STAR[1]), 2 * (STAR_R + STAR_FLEX), STAR_Z - STAR_W / 2 - 0.1, STAR_Z + STAR_W / 2 + 0.1)
    names = sorted(shapes); out = []
    for i, a in enumerate(names):
        for b in names[i + 1:]:
            if only and not (only(a) or only(b)): continue
            if only and only(a) and only(b): continue
            if allowed(a, b) or (a[:3] in ("lm_", "dm_") and a[:3] == b[:3]): continue
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

    if any(p["moves"] in ("fly_L", "fly_R") for p in PARTS.values()):
        print(f"== the flywheel modules opening 0 .. {FLY_TRAVEL:.2f} in each side (a NECTAR between them), motors with them")
        mvk = [k for k, p in PARTS.items() if p["moves"] in ("fly_L", "fly_R")]
        fixed = {k: v for k, v in shapes.items() if k not in mvk and not FLY_PLATES.match(k[3:])}
        def hits(d):
            out = {}
            for k in mvk:
                sg = 1 if PARTS[k]["moves"] == "fly_L" else -1
                w = solid(shapes[k]).translate(cq.Vector(0, sg * d, 0))
                for f, fw in fixed.items():
                    v = overlap(w, fw)
                    if v > 1e-4 or v < 0: out[(k, f)] = v
            return out
        rest = hits(0.0); new_ = {}
        for d in (FLY_TRAVEL / 2, FLY_TRAVEL):
            for kf, v in hits(d).items():
                if v > rest.get(kf, 0) + 1e-4: new_.setdefault(kf, d)
        for (a, b), d in sorted(new_.items()): print(f"   at {d:.2f}: CLASH {a} x {b}")
        print("   " + ("clear (his plates are slotted for the shafts' travel)" if not new_ else f"{len(new_)} clashes"))
        ok &= not new_
        b = [bb(solid(shapes[k]).translate(cq.Vector(0, (1 if PARTS[k]["moves"] == "fly_L" else -1) * FLY_TRAVEL, 0))) for k in mvk]
        print(f"   opened: Y {min(x[2] for x in b):.2f}..{max(x[3] for x in b):.2f} (R105 allows 18 wide after START)")

    print("== pieces on their path (a NECTAR and a POLLEN at each step; only what's meant to touch them may)")
    bad = []
    LM_TOUCH = re.compile(r"^lm_.*(feeder|pad_|Gecko|backstop|Hyper_Hub|Spacer|Sonic)", re.I)
    TOUCH = ("lane_roller_", "ceiling", "feeder", "pad", "flywheel_", "roller", "ramp_", "mouth_floor_", "floor", "backstop", "hood", "star_")   # the hood's lid turns the shot
    path = [(x, LANE_BOTTOM) for x in (BACKSTOP_X + 1.82, 0.0, 2.0, 4.0, 5.5, 6.5)] + [(COL_X, z) for z in (5.0, 6.65, 8.0, 9.0, 10.5)]
    for r in (RN, RP):                      # off-centre: a piece at the mouth's edge, on the floor, where it meets a star's tips
        for sgn in (1, -1):                 # (the star, the floor, the ramp and the roller, which rises, may touch it; nothing else)
            yc = sgn * (MOUTH_HALF - r)          # as far out as the floor holds it
            d = STAR_R + r - 0.41
            xc = STAR[0] + math.sqrt(max(d * d - (abs(yc) - STAR[1]) ** 2, 0))
            ball = cq.Workplane("XY").sphere(r).translate((xc, yc, LANE_BOTTOM + r))
            for k, w in shapes.items():
                if k in (f"star_{'L' if sgn > 0 else 'R'}",) or k.startswith(("mouth_floor_", "ramp_", "roller", "lane_roller_")): continue
                y = yc
                if overlap(ball, w) > 1e-4: bad.append(f"off-centre {'NECTAR' if r == RN else 'POLLEN'} at Y {y} x {k}")
    for r in (RN, RP):
        for x, z0 in path:
            ball = cq.Workplane("XY").sphere(r).translate((x, 0.15 if z0 > LANE_BOTTOM else 0, z0 + (r if z0 == LANE_BOTTOM else 0)))
            for k, w in shapes.items():
                if k.startswith(TOUCH) or k.startswith(("lane_shaft_",)) or LM_TOUCH.search(k): continue
                if overlap(ball, w) > 1e-4: bad.append(f"{'NECTAR' if r == RN else 'POLLEN'} at X {x:.2f} z {z0:.2f} x {k}")
    print("   " + ("; ".join(sorted(set(bad))) if bad else "clear: the mouth's edges past the stars, lane, column, ring bore and hood"))
    ok &= not bad

    print("== a NECTAR rolling in across the mouth (on the tiles, then up the ramp), against the extractor and the flaps")
    (rx0, rz0), (rx1, rz1) = RAMP
    def bottom(x):
        return 0.0 if x >= rx0 else (rz0 + (rx0 - x) / (rx0 - rx1) * (rz1 - rz0) if x >= rx1 else LANE_BOTTOM)
    hit, gap = [], 99.0
    for y in (0.0, 1.5, 2.5, ROLL_HALF - RN + 0.5):          # as far out as the roller takes one (beyond it, the flaps' roots fence the mouth)
        for i in range(41):
            x = 12.5 - 0.15 * i; z = bottom(x) + RN
            ball = cq.Workplane("XY").sphere(RN).translate((x, y, z))
            for k in shapes:
                if k.startswith(("ex_", "flap")) and overlap(ball, shapes[k]) > 1e-5: hit.append(f"X {x:.2f} Y {y} x {k}")
            if y + RN > EX_ARM_Y: gap = min(gap, math.hypot(x - EX_AXIS[0], z - EX_AXIS[1]) - RN - 4 * MM)
    print("   " + ("; ".join(hit[:6]) if hit else f"clear; it passes {gap:.2f} in under the extractor's stub shafts (only a piece wider of centre than {EX_ARM_Y - RN:.2f} in goes under them)"))
    ok &= not hit

    print("== outline")
    allb = [bb(w) for w in shapes.values()]
    x0, x1 = min(b[0] for b in allb), max(b[1] for b in allb)
    y0, y1 = min(b[2] for b in allb), max(b[3] for b in allb)
    z1 = max(b[5] for b in allb)
    print(f"   start: X {x0:.2f}..{x1:.2f} ({x1 - x0:.2f}), Y {y0:.2f}..{y1:.2f} ({y1 - y0:.2f}), height {z1:.2f}: designed to {START_CUBE} (R102 {R102}, 1/8 in a side)")
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
    lo, hi = gap - BACKSTOP_SLOT, gap + BACKSTOP_SLOT
    print(f"   the backstop's slots reach {lo:.2f} to {hi:.2f}: " + ("they cover the whole window, so it's set on the robot with real pieces" if lo <= COUNT_WINDOW[0] and hi >= COUNT_WINDOW[1] else "NOT the whole window"))
    ok &= lo <= COUNT_WINDOW[0] and hi >= COUNT_WINDOW[1]

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
        if k.startswith(("lm_", "dm_")): continue
        rows.append(f"| `{k}` | {p['module']} | {dict(print='print', buy='buy', cut='cut')[p['kind']]} | {p['what']} |")
    if USE_LM: rows.append("| `lm_*` | launcher | buy | the mentor's launcher module (cad/modules/launcher-module), placed by the launch column |")
    if USE_DM: rows.append("| `dm_*` | corner_* | buy | the mentor's four drive corners (cad/modules/drive-module), each by its axle; the right ones mirrored from his left |")
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
        if k.startswith(("flap_face", "pad")): c = (0.95, 0.72, 0.20)
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
    # For Onshape (README, "In Onshape"): one FRAME group of everything that doesn't move, and one MOVES group per moving
    # body, named with the mate it needs, as cad/full-robot does. STEP carries no mates.
    def group_of(k):
        if PARTS[k]["moves"] == "intake": return f"MOVES 1 intake arms - revolute on the arms' pivot (Y axis), 0 to {lift_deg(FLOAT):.0f} deg up"
        if PARTS[k]["moves"] == "extractor" and not k.startswith("ex_stub"): return "MOVES 2 extractor - revolute on the stub shafts (Y axis), 0 to 146 deg, drawn stowed"
        for n, i in (("FL", 3), ("FR", 4), ("BL", 5), ("BR", 6)):
            if k in (f"wheel_{n}", f"dm_{n}_wheel"): return f"MOVES {i} wheel {n} - revolute on its shaft"
        if k == "star_L": return "MOVES 7 star wheel L - revolute on its vertical shaft"
        if k == "star_R": return "MOVES 8 star wheel R - revolute on its vertical shaft"
        if PARTS[k]["moves"] == "fly_L": return f"MOVES 9 flywheel module L - slider along Y, 0 to {FLY_TRAVEL:.2f} in out (sprung in)"
        if PARTS[k]["moves"] == "fly_R": return f"MOVES 10 flywheel module R - slider along Y, 0 to {FLY_TRAVEL:.2f} in out (sprung in)"
        return "FRAME - fix"
    groups = {}
    for k in PARTS: groups.setdefault(group_of(k), []).append(k)
    asm = cq.Assembly(name="printed_chassis")
    full = cq.Assembly(name="printed_chassis_with_the_mentors_modules")      # with his launcher and drive corners: too big for git
    for g in sorted(groups, key=lambda g: (not g.startswith("FRAME"), int(g.split()[1]) if g.startswith("MOVES") else 0)):
        sub, fsub = cq.Assembly(name=g), cq.Assembly(name=g)
        for k in groups[g]:
            p = PARTS[k]
            s = solid(p["shape"]).scale(25.4)
            col = {"print": cq.Color(0.15, 0.45, 0.85), "buy": cq.Color(0.55, 0.55, 0.58), "cut": cq.Color(0.8, 0.8, 0.8)}[p["kind"]]
            if k.startswith(("flap_face", "pad")): col = cq.Color(0.95, 0.75, 0.2)
            if not k.startswith(("lm_", "dm_")): sub.add(s, name=k, color=col)
            fsub.add(s, name=k, color=col)
            if p["kind"] == "print": cq.exporters.export(cq.Workplane().add(s), os.path.join(HERE, "stl", f"{k}.stl"))
        if sub.children: asm.add(sub, name=g)
        full.add(fsub, name=g)
    asm.save(os.path.join(HERE, "printed-chassis.step"))
    if USE_LM or USE_DM:
        import gzip, shutil
        fp = os.path.join(HERE, ".cache", "printed-chassis-full.step"); full.save(fp)
        with open(fp, "rb") as a, gzip.open(fp + ".gz", "wb") as b: shutil.copyfileobj(a, b)
    with open(os.path.join(HERE, "parts.md"), "w") as f:
        f.write("# Parts, layout v1 (written by build.py)\n\nScrews, nuts and inserts come with v2. CHECK marks a size not read from a vendor file.\n\n" + parts_md())
    views(os.path.join(HERE, "views"))
    print("wrote printed-chassis.step, stl/, parts.md, views/")
