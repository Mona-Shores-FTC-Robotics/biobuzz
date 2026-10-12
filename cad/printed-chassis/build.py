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
    elec       the battery on a shelf at the back; the hubs and the Limelight on a deck over the front
"""
import math, os, re, sys
import cadquery as cq

HERE = os.path.dirname(os.path.abspath(__file__))
MM = 1 / 25.4
CHECK_ONLY = os.environ.get("CHECK_ONLY") == "1"
LM_DIR = os.path.join(HERE, ".cache", "launcher")    # the mentor's launcher module, cached by launcher_module.py
USE_LM = os.path.exists(os.path.join(LM_DIR, "index.json")) and os.environ.get("NO_LAUNCHER_MODULE") != "1"
DM_DIR = os.path.join(HERE, ".cache", "drive")       # the mentor's drive corners, cached by drive_module.py
USE_DM = False                          # layout v3: our own four corners on his track and wheelbase (his built corners are the
                                        # reference for both, not the parts: theirs put the front motors across the lane, where
                                        # the feeder's motor must go). drive_module.py stays, to compare against his.
DM_LAYOUT = True                        # the frame laid out for his track and wheelbase

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
def rail_x(k): return RAIL_X[0] + (8 + 8 * k) * MM    # the rails' 4 mm hole stations: every 8 mm from 8 mm off the end (goBILDA's STEP, via the CAD chat)
LM_FEET = (rail_x(12), rail_x(21))     # X: the launcher's side channels sit 1.0 in over the rails' top flanges; the rear foot just ahead of the wheel
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
DRIVE_BELT = {1: 360, -1: 275}          # 2 x centres + 120: both exact
DRIVE_MOTOR = {1: (AXLE[1], WHEEL_R + 5 * PITCH),             # front: straight over the axle, over the stars and the intake's pivot
               -1: (AXLE[-1] - math.sqrt(77.5 ** 2 - 72 ** 2) * MM + 0.06, WHEEL_R + 3 * PITCH)}   # (0.06 in forward: the belt inside the outline; 0.45 mm of tension in the slots)   # rear: behind and three holes up, under the
                                        # launcher's back (as his), at a 275 mm belt's own centres (the plate's motor holes slotted)
MPLATE = {1: (7, "1123-0048-0192"), -1: (4, "rear motor plate")}   # the front ones rise beside the front cross channel's ends   # the axle at its lowest hole, the motor at its top one

# ---------------------------------------------------------------- intake
ROLL_R = 24 * MM                        # goBILDA 48 mm Gecko wheels, as the mentor's roller (no vector wheels: his star wheels centre)
ROLL = (FACE + 1.0, 2.4 + ROLL_R)       # axle at rest: bottom 2.4 in off the tiles, ahead of the face on the swing arms
ROLL_HALF = 4.35                        # the wheels span the mouth the star wheels can reach (+-4.9; his mouth is 9.76);
                                        # the shaft carries on to the pulley at 6.55..6.9 and the arms at 6.95..7.25
# ---------------------------------------------------------------- the mentor's star wheels (his 9 Oct Robot.step, read by the CAD chat)
STAR_R, STAR_W = 3.5 / 2, 0.5           # "3.5in OD 7mm Hex Bore": a flexible 12-flap star, lying flat (axis vertical); vendor TBD
STAR = (FACE - 2.36, 3.15)              # its centre: 2.36 in behind the face, 3.15 either side (tips 2.80 apart: a NECTAR bends each 0.41)
STAR_Z = 1.75                           # its mid-plane: low, on a piece still rolling on the tiles, between a POLLEN's middle (1.40)
                                        # and a NECTAR's (1.81); his sit at 1.84. The roller touches a piece first (the stars' servos
                                        # are slow); the stars drag it in and centre it before it climbs the ramp behind them. (Until
                                        # 12 Oct they sat at 3.2, up the ramp, and only centred what the roller had already lifted.)
STAR_BRG_Z = (STAR_Z + STAR_W / 2 + 0.10, STAR_Z + STAR_W / 2 + 0.10 + 5 * MM)   # the star shaft's bearing (8x14x5, round bore), in the bracket's arm just over the star
STAR_HUB_Z = (3.78, 3.78 + 13 * MM)     # the clutch's printed hub (its 12 mm clutch and a 1 mm floor), on the servo, which stays up
                                        # under the front drive motor: the shaft runs down from it through the bearing to the star
SV_Z = STAR_HUB_Z[1] + 0.02             # the star servo's spline face, on the hub: its case top 0.04 under the front drive motor
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
LANE_SHAFTS = [ROLL[0] - 3.16 - 1.4 * k for k in range(2, 5)]   # the lane's free rollers, from just behind the ramp's top
RAMP = ((STAR[0] + 0.11, 0.05), (LANE_SHAFTS[0] + 0.51, LANE_BOTTOM))   # from the tiles beside the stars up to the lane: a piece
                                        # rolls in on the tiles under the roller and past the stars, then climbs

# ---------------------------------------------------------------- launcher (cad/transfer's, about the column)
FLY_R, FLY_Y, FLY_Z = 96 / 2 * MM, 2.85, 6.65
FLY_X = (COL_X - 0.95, COL_X + 0.95)
FLY_TRAVEL = RN - RP                    # each flywheel module's spring travel: a POLLEN at rest, a NECTAR pushes each side out 0.41
                                        # (the same squeeze on both: rest gap = 2 RP - squeeze; his module's CAD has 1.93, the rig sets it)
FLY_AXIS = (2.855, 6.673)               # Y, z: each flywheel shaft (his module)
FLY_SPRING = dict(rate=None, preload=None)   # lbf/in, lbf: the launcher rig sets them (README, Open 3)
# the mentor's flywheel parts that ride the sprung modules (each side slides out in Y, its motor with it); his plates stay
# put as the frame (slotted for the shafts' travel).
# The feeder (the mentor, 11 Oct): a pair of wheels parallel to the flywheels, one each side of the column under them, each
# riding its side's module, so one spring a side sets both pinches. The left one is driven by the feeder's own motor, on
# the left module (never from a flywheel: the 8 Oct motor budget); the right one is free and takes the pad's place. It
# stays put (no gate servo): stopping its motor is the gate. The transfer's single feeder, its yoke, gate servo and pad,
# and its flywheel-to-feeder drive are left out of his module (OLD_FEEDER)
FLY_MOVES = re.compile(r"^(96mm_Gecko|96mm_Steel_Shaft|8mm_Spacer_|part_piece|8mm_REX_Hyper_Hub|1505-0032-0160|8x14x5mm_Bearing_-_Round_Bore_GB_[3-6]_|"
                       r"flywheel_(motor|pulley|belt)_)")
OLD_FEEDER = re.compile(r"^(flywheel_feeder_pulley|flywheel_spacers_L_|flywheel_shaft_eclip_L|feeder_(?!floor)|gate_servo|pad_)")   # his bridge too: our tray replaces it
FEED_R = 24 * MM                        # 48 mm Gecko wheels, two side by side a side, as wide as the flywheels' pair
FEED_SQUEEZE = 0.37                     # into a POLLEN, each side (as the transfer's feeder): the rig sets it
FEED_Y = RP + FEED_R - FEED_SQUEEZE     # 1.975: the wheels' axes, either side of the column
FEED_Z = 3.87                           # high enough that a POLLEN is always gripped by the feeder or the flywheels, from the
                                        # backstop up, and still 0.1 in clear of the flywheels at rest
# v3: his frame re-housed (README, DECISIONS.md): the flywheels' pivot hardware goes (the sprung slides replace it); his turret
# drive plate is redrawn shorter; the rear lower cross channel, its blocks and the column's back guides rise 0.9 in, above the
# feeder's drive; the feeder bridge is trimmed clear of the battery
LM_DROP = re.compile(r"^(Sonic_Hub|32mm_Steel_Shaft|1301-|1600-|1603-|turret_drive_plate)")
LM_RAISE = re.compile(r"^(Hole_Lowside_U-Channel_GB__1\.|Dual_Block_GB__1\.|Dual_Block_GB_[234]__1|Hole_U_Beam_|Square_Beam_)")
LM_RAISE_Z = 0.9
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
HUB = (143 * MM, 103 * MM, 29.6 * MM)   # REV Control / Expansion Hub: long, wide, thick (REV's own model)
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
    return belt_look(cyl("y", p0, d + 0.1, y0, y1).union(cyl("y", p1, d + 0.1, y0, y1)).union(
        slab_y(_strip(p0, p1, (d + 0.1) / 2), y0, y1)), p0, p1, "y", y0, y1)

def belt_x(p0, p1, d, x0, x1):
    """The same in a Y-Z plane."""
    s = _strip(p0, p1, (d + 0.1) / 2)
    return belt_look(cyl("x", p0, d + 0.1, x0, x1).union(cyl("x", p1, d + 0.1, x0, x1)).union(
        cq.Workplane("YZ").polyline(s).close().extrude(x1 - x0).translate((x0, 0, 0))), p0, p1, "x", x0, x1)

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
    return dress(body.union(cyl("y", axis_xz, 8 * MM, face, face + sign * MOTOR_SHAFT)), "motor", (axis_xz[0], face, axis_xz[1]), (0, sign, 0), (1, 0, 0))

def motor_x(face, yz, sign, length=MOTOR_L1):
    body = box(face, face - sign * length, yz[0] - MOTOR_SQ / 2, yz[0] + MOTOR_SQ / 2, yz[1] - MOTOR_SQ / 2, yz[1] + MOTOR_SQ / 2)
    return dress(body.union(cyl("x", yz, 8 * MM, face, face + sign * MOTOR_SHAFT)), "motor", (face, yz[0], yz[1]), (sign, 0, 0), (0, 0, 1))

SERVO_TAB = (0.337, 0.435)              # goBILDA 2000 servo: its tabs, from the case's spline face (vendor.py's model); their holes 4.5 mm,
SERVO_HOLES = ((-0.551, 0.197), (-0.551, -0.197), (1.339, 0.197), (1.339, -0.197))   # at these (along, across) from the spline
def servo(sku, origin, z, x):
    """A goBILDA 2000 servo's envelope, dressed: its case, its tabs and its spline, in the frame vendor.py gives it (origin
    on the spline's axis at the case's face, z out along the spline, x toward the case's far end)."""
    w = box(-0.394, 1.181, -0.394, 0.394, -1.506, 0).union(box(-0.68, 1.46, -0.394, 0.394, -SERVO_TAB[1], -SERVO_TAB[0])).union(cyl("z", (0, 0), 0.25, 0, 0.23))
    w = cq.Workplane().add(solid(w).moved(cq.Location(cq.Plane(origin=origin, xDir=x, normal=z))))
    return dress(w, sku, origin, z, x)

def rot_y(wp, about_xz, deg):
    """Rotate about a line along Y through (x, z); positive raises what's ahead of it."""
    out = wp.rotate((about_xz[0], 0, about_xz[1]), (about_xz[0], 1, about_xz[1]), -deg)
    if hasattr(wp, "looks"): out.looks = _turned(wp.looks, (about_xz[0], 0, about_xz[1]), (0, 1, 0), -deg)
    return out

# ================================================================ dress: what a bought part looks like
# The checks use each bought part's envelope (above). For the STEP and the views it is dressed: goBILDA's own model
# (vendor.py; its frame is in vendor.MODELS) placed by an origin, the direction its local Z points and the direction its
# local X points, or a drawn one in the same kind of frame (a toothed belt, a REX shaft). dress() hangs the look on the
# envelope; mir() and rot_y() carry it along. A mirrored look is the real part turned, never a mirror image of it (a
# right-hand mecanum becomes the left-hand one).
def dress(w, what, origin, z, x):
    w.looks = getattr(w, "looks", []) + [(what, tuple(origin), tuple(z), tuple(x))]
    return w

def _turned(looks, p0, d, deg):
    from OCP.gp import gp_Trsf, gp_Ax1, gp_Pnt, gp_Dir, gp_Vec
    t = gp_Trsf(); t.SetRotation(gp_Ax1(gp_Pnt(*p0), gp_Dir(*d)), math.radians(deg))
    P = lambda p: (lambda q: (q.X(), q.Y(), q.Z()))(gp_Pnt(*p).Transformed(t))
    V = lambda v: (lambda q: (q.X(), q.Y(), q.Z()))(gp_Vec(*v).Transformed(t))
    return [(k, P(o), V(z), V(x)) for k, o, z, x in looks]

MIRROR_KEY = {"mecanum_A": "mecanum_B", "mecanum_B": "mecanum_A"}
def _mirrored(looks):
    """Each look across Y = 0, kept a proper placement: the frame is also reflected through a plane the part is
    symmetric about (vendor.SYMMETRY: local Y = 0 unless it says otherwise), so the part lands where its mirror image
    would, unmirrored."""
    import vendor
    f = lambda v: (v[0], -v[1], v[2])
    out = []
    for k, o, z, x in looks:
        a, c = vendor.SYMMETRY.get(k, (1, 0.0)) if isinstance(k, str) else (1, 0.0)
        X, Z = cq.Vector(*x), cq.Vector(*z); Y = Z.cross(X); ax = (X, Y, Z)[a]
        o2 = cq.Vector(*o) + ax * (2 * c)
        x2, z2 = f(tuple(X)), f(tuple(Z))
        if a == 0: x2 = tuple(-v for v in x2)
        if a == 2: z2 = tuple(-v for v in z2)
        out.append((MIRROR_KEY.get(k, k) if isinstance(k, str) else k, f(tuple(o2)), z2, x2))
    return out

REX_FLATS = 3.5 * MM                    # goBILDA's 8mm REX: a hexagon 7 mm across its flats, its corners on an 8 mm circle (read from 2106-4008-0800)
def rex_shaft(length, bore=0.0):
    """An 8mm REX shaft (or, with a bore, a REX standoff) along local Z from 0 to length."""
    hexa = cq.Workplane("XY").polygon(6, 2 * REX_FLATS / math.cos(math.pi / 6)).extrude(length)
    w = hexa.intersect(cq.Workplane("XY").circle(4 * MM).extrude(length))
    if bore: w = w.cut(cq.Workplane("XY").circle(bore / 2).extrude(length))
    return solid(w)

def spur_gear(teeth, module, width, bore=None):
    """An involute spur gear (20 deg pressure angle) in local XY, from z 0 to width, inches; bore: 'rex' or a diameter."""
    m = module * MM; rp = m * teeth / 2; rb = rp * math.cos(math.radians(20)); ra = rp + m; rf = rp - 1.25 * m
    inv = lambda a: math.tan(a) - a
    half = math.pi / (2 * teeth) + inv(math.acos(rb / rp))          # the tooth's half-angle at the base circle
    def flank(sign, base):
        pts = []
        for i in range(9):
            r = max(rb, rf) + (ra - max(rb, rf)) * i / 8
            a = base + sign * (half - inv(math.acos(min(1.0, rb / r))))
            pts.append((r * math.cos(a), r * math.sin(a)))
        return pts
    prof = []
    for k in range(teeth):
        c = 2 * math.pi * k / teeth
        right, left = flank(-1, c), flank(1, c)
        prof += [(rf * math.cos(c - half - 0.06), rf * math.sin(c - half - 0.06))] + right + left[::-1] + [(rf * math.cos(c + half + 0.06), rf * math.sin(c + half + 0.06))]
    g = cq.Workplane("XY").polyline(prof).close().extrude(width)
    if bore == "rex": g = g.cut(cq.Workplane("XY").polygon(6, 2 * (REX_FLATS + 0.1 * MM) / math.cos(math.pi / 6)).extrude(width))
    elif bore: g = g.cut(cq.Workplane("XY").circle(bore / 2).extrude(width))
    return g

def placed(w, origin, z, x=(1, 0, 0)):
    """A shape drawn in its own frame, placed (as dress() places a model)."""
    return cq.Workplane().add(solid(w).moved(cq.Location(cq.Plane(origin=origin, xDir=x, normal=z))))

HTD5 = dict(pitch=5 * MM, land_r=18.53 * MM, tooth=2.06 * MM, back=1.54 * MM, width=9 * MM)   # the 24T's tips (goBILDA's model), HTD 5 mm belt
def toothed_belt(centres, r=HTD5["land_r"]):
    """A 9 mm HTD5 belt round two equal pulleys `centres` apart, in local XY (pulleys at (0, 0) and (centres, 0)),
    centred on local Z = 0: its back a smooth loop, its teeth on the inside at the belt's own pitch."""
    path_len = 2 * centres + 2 * math.pi * r
    n = round(path_len / HTD5["pitch"]); pitch = path_len / n
    def at(u):                              # a point on the land line and the outward normal there, u along the loop
        u %= path_len
        if u < centres: return (u, -r), (0, -1)
        u -= centres
        if u < math.pi * r: a = -math.pi / 2 + u / r; return (centres + r * math.cos(a), r * math.sin(a)), (math.cos(a), math.sin(a))
        u -= math.pi * r
        if u < centres: return (centres - u, r), (0, 1)
        u -= centres; a = math.pi / 2 + u / r; return (r * math.cos(a), r * math.sin(a)), (math.cos(a), math.sin(a))
    inner = []
    for i in range(n):                      # each pitch: land, flank down, tooth, flank up
        for f, d in ((0.0, 0), (0.18, 0), (0.30, 1), (0.70, 1), (0.82, 0)):
            (px, py), (nx, ny) = at((i + f) * pitch); dd = d * HTD5["tooth"]
            inner.append((px - nx * dd, py - ny * dd))
    outer = []
    for i in range(4 * n):
        (px, py), (nx, ny) = at(i * pitch / 4); outer.append((px + nx * HTD5["back"], py + ny * HTD5["back"]))
    w = HTD5["width"]
    band = cq.Workplane("XY").polyline(outer).close().extrude(w).cut(cq.Workplane("XY").polyline(inner).close().extrude(w))
    return solid(band.translate((0, 0, -w / 2)))

def belt_look(w, p0, p1, axis, a0, a1):
    """Dress a belt envelope from belt_y/belt_x: a 24T pulley at each end and the toothed belt, in the plane mid a0..a1."""
    m = (a0 + a1) / 2
    P = (lambda p: (p[0], m, p[1])) if axis == "y" else (lambda p: (m, p[0], p[1]))
    z = (0, 1, 0) if axis == "y" else (1, 0, 0)
    q0, q1 = P(p0), P(p1); d = [b - a for a, b in zip(q0, q1)]; c = math.sqrt(sum(v * v for v in d))
    x = tuple(v / c for v in d)
    for q in (q0, q1): dress(w, "3417-4008-0024", q, z, x)
    return dress(w, ("belt", round(c, 4)), q0, z, x)

def _drawn(key):
    """A drawn look's shape in its local frame (inches), made once per size."""
    if key not in _DRAWN:
        kind, size = key
        _DRAWN[key] = toothed_belt(size) if kind == "belt" else rex_shaft(size, 3.3 * MM if kind == "rex_standoff" else 0.0)
    return _DRAWN[key]
_DRAWN = {}

# ================================================================ parts
def solid(w): return w.val() if hasattr(w, "val") else w

_BB = {}
def bb(w):
    k = id(w)
    if k not in _BB:
        b = solid(w).BoundingBox(); _BB[k] = ((b.xmin, b.xmax, b.ymin, b.ymax, b.zmin, b.zmax), w)   # keep w alive so its id stays unique
    return _BB[k][0]

def overlap(a, b, tol=1e-4):
    A, B = bb(a), bb(b)
    if any(A[2 * i] > B[2 * i + 1] - tol or B[2 * i] > A[2 * i + 1] - tol for i in range(3)): return 0.0
    try: return solid(a).intersect(solid(b)).Volume()
    except Exception: return -1.0

PARTS = {}          # name -> dict(shape, module, kind ('print' | 'buy' | 'cut'), what, moves)

def part(module, name, shape, kind, what, moves=None):
    assert name not in PARTS, name
    PARTS[name] = dict(shape=shape, module=module, kind=kind, what=what, moves=moves, looks=_looks(shape, kind, what))

def _looks(shape, kind, what):
    """The part's dress: the one dress() hung on it (a motor's SKU read from its description), or, for a plain shaft or
    standoff, one made from its envelope's long axis."""
    looks = getattr(shape, "looks", None)
    if looks:
        sku = re.search(r"5203-2402-00\d\d", what)
        return [((sku.group(0) if sku else "5203-2402-0014") if k == "motor" else k, o, z, x) for k, o, z, x in looks]
    if kind != "buy": return None
    rex, so = re.search(r"8mm REX (shaft|standoff)", what), re.findall(r"1501-0006-(\d{4})", what)
    if not (rex or so): return None
    b = bb(shape); ext = [b[1] - b[0], b[3] - b[2], b[5] - b[4]]; i = max(range(3), key=lambda j: ext[j])
    others = [ext[j] for j in range(3) if j != i]
    if abs(others[0] - others[1]) > 0.01: return None                       # not a plain cylinder
    o = [(b[0] + b[1]) / 2, (b[2] + b[3]) / 2, (b[4] + b[5]) / 2]; o[i] = b[2 * i]
    z = tuple(1.0 if j == i else 0.0 for j in range(3)); x = tuple(1.0 if j == (i + 1) % 3 else 0.0 for j in range(3))
    if rex: return [(("rex_standoff" if rex.group(1) == "standoff" else "rex", round(ext[i], 4)), tuple(o), z, x)]
    out = []
    for L in so:                                                              # standoffs stacked from the low end
        out.append((f"1501-0006-{L}", tuple(o), z, x)); o[i] += int(L) / 10 * MM
    return out

SIDES = ((1, "L"), (-1, "R"))
def mir(wp, s):
    if s > 0: return wp
    out = wp.mirror("XZ")
    if hasattr(wp, "looks"): out.looks = _mirrored(wp.looks)
    return out

# ---- frame
for s, n in SIDES:
    part("frame", f"rail_{n}", mir(dress(channel_x(*RAIL_X, RAIL_OUT, True, RAIL_Z[0], CH_LOW), "1121-0014-0360", (RAIL_X[0], RAIL_OUT, RAIL_Z[0]), (0, 0, 1), (0, -1, 0)), s), "buy", "goBILDA 1121-0014-0360 low-side U-channel (360 mm), the mentor's track" if DM_LAYOUT else "goBILDA 1121-0013-0336 low-side U-channel, 13 hole (336 mm), as today's rails")
if not USE_DM:                          # his rear motors' 1107 channel ties the rails at the back
  rc = box(RAIL_X[0], RAIL_X[0] + CH_T, -120 * MM, 120 * MM, *RAIL_Z).union(box(RAIL_X[0], RAIL_X[0] + CH_LOW, -120 * MM, 120 * MM, RAIL_Z[0], RAIL_Z[0] + CH_T)).union(
     box(RAIL_X[0], RAIL_X[0] + CH_LOW, -120 * MM, 120 * MM, RAIL_Z[1] - CH_T, RAIL_Z[1]))
  part("frame", "rear_cross", dress(rc, "1121-0009-0240", (RAIL_X[0], -120 * MM, RAIL_Z[0]), (0, 0, 1), (1, 0, 0)), "buy",
     "goBILDA 1121-0009-0240 low-side U-channel (240 mm), standing at the rails' back ends, between their webs on goBILDA pattern brackets (CHECK the bracket)")
if not USE_DM:
  part("frame", "front_cross", dress(channel_y(-120 * MM, 120 * MM, FRONT_CROSS_X, FRONT_CROSS_Z, open_down=True), "1120-0009-0240",
       (FRONT_CROSS_X, -120 * MM, FRONT_CROSS_Z), (0, 0, 1), (1, 0, 0)), "buy",
     "goBILDA 1120-0009-0240 U-channel (240 mm), over the lane and the front drive motors, between the front motor plates on printed end blocks (no uprights: the mouth stays open to its full width)")
FC_END_Y = 112 * MM                     # the front cross channel's last side-wall holes (goBILDA's model: every 8 mm along, 8/16/32/40 mm up)
REAR_CROSS_HOLE = (RAIL_X[0] + 8 * MM, 112 * MM)   # the rear cross channel's top flange: its row 8 mm from its web, its last station
for s_, n in (() if USE_DM else SIDES):
    eb = box(FRONT_CROSS_X + 0.11, FRONT_CROSS_X + CH - 0.11, 4.15, 4.73, FRONT_CROSS_Z + 0.05, FRONT_CROSS_Z + CH - 0.11).union(   # inside the channel's end
         box(FRONT_CROSS_X, FRONT_CROSS_X + CH, 4.72, RAIL_OUT, FRONT_CROSS_Z, RAIL_Z[0] + 192 * MM))                                # out to the motor plate
    part("frame", f"front_cross_end_{n}", mir(eb, s_), "print",
         "PETG-CF: the front cross channel's end block: inside the channel's end (2 M4 through its side walls) and out to the front motor plate (2 M4 through the plate)")
    cb_ = box(RAIL_X[0] + CH_T + 0.01, RAIL_X[0] + CH_LOW - 0.01, 4.0, RAIL_OUT - CH_T - 0.005, RAIL_Z[0] + CH_T + 0.005, RAIL_Z[1] - CH_T - 0.005)
    part("frame", f"rear_corner_{n}", mir(cb_, s_), "print",
         "PETG-CF: a rear corner block, inside the corner of the rear cross channel and the rail: an M4 down through the channel's top flange, two through the rail's top and bottom flanges")

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
    op = dress(box(*RAIL_X, *OPLATE_Y, *RAIL_Z), "1123-0048-0336", (RAIL_X[0] + 12 * MM, OPLATE_Y[0], RAIL_Z[1]), (0, 0, -1), (0, 1, 0))   # 336 of the rail's 360, centred: its holes on the rail's 24 mm bores
    part("frame", f"outer_plate_{n}", mir(op, s_), "buy", "goBILDA 1123-0048-0336 pattern plate (1 x 13 hole): the wheels' outer bearings, on 56 mm standoffs")
    for k in (-3, 1):                       # between the wheels, where no belt runs
        for dz in (-16 * MM, 16 * MM):
            part("frame", f"standoff_washers_{n}_{k}_{'hi' if dz > 0 else 'lo'}", mir(cyl("y", (RAIL_CX + k * PITCH, WHEEL_R + dz), 9 * MM, RAIL_OUT, MPLATE_Y[1]), s_), "buy",
                 "two M4 flat washers (2.5 mm, the motor plates' thickness): no motor plate here, so they make up its place between the rail's web and the standoff")
            part("frame", f"standoff_{n}_{k}_{'hi' if dz > 0 else 'lo'}", mir(cyl("y", (RAIL_CX + k * PITCH, WHEEL_R + dz), 6 * MM, MPLATE_Y[1], OPLATE_Y[0]), s_), "buy",
                 "goBILDA 1501-0006-0560 M4 standoff, 56 mm")
for sx, fx in (() if USE_DM else ((1, "F"), (-1, "B"))):
    ax = AXLE[sx]; mx, mz = DRIVE_MOTOR[sx]; nh, pn = MPLATE[sx]
    for s, n in SIDES:
        nm = fx + n
        mp = box(min(ax, mx) - CH / 2, max(ax, mx) + CH / 2, *MPLATE_Y, RAIL_Z[0], RAIL_Z[0] + nh * PITCH + CH - PITCH)
        if mx != ax:                    # the rear: the motor's two columns of holes, and only a strip down to the axle (his launcher's frame is over the rest)
            mp = box(max(mx - CH / 2, RAIL_X[0]), mx + MOTOR_SQ / 2 + 0.04, *MPLATE_Y, RAIL_Z[0], RAIL_Z[0] + nh * PITCH + CH - PITCH).union(box(mx, ax + 0.3, *MPLATE_Y, RAIL_Z[0], WHEEL_R + 0.6))
        if mx == ax:
            mp = dress(mp, pn, (ax - CH / 2, MPLATE_Y[0], RAIL_Z[0]), (1, 0, 0), (0, 1, 0))
            part(f"corner_{nm}", f"motor_plate_{nm}", mir(mp, s), "buy", f"goBILDA {pn}, standing on the rail's web: the drive motor screws to it, the wheel's shaft through its lowest hole")
        else:                           # the rear: no stock plate has the motor 72 mm up and 29 mm behind the axle, so it's cut
            hy = lambda c, d: cyl("y", c, d, MPLATE_Y[0] - 0.1, MPLATE_Y[1] + 0.1)
            u = ((ax - mx) / math.hypot(ax - mx, WHEEL_R - mz), (WHEEL_R - mz) / math.hypot(ax - mx, WHEEL_R - mz))   # along the belt
            mp = mp.cut(hy((ax, WHEEL_R), 14 * MM)).cut(hy((mx, mz), 14 * MM))                                # the axle's bearing, the motor's boss
            for a_, b_ in ((-8, -8), (-8, 8), (8, -8), (8, 8)):                                                 # its four M4, slotted 2 mm along the belt
                c = (mx + a_ * MM, mz + b_ * MM)
                mp = mp.cut(hy(c, 4.4 * MM)).cut(hy((c[0] + 2 * MM * u[0], c[1] + 2 * MM * u[1]), 4.4 * MM)).cut(
                     slab_y(_strip(c, (c[0] + 2 * MM * u[0], c[1] + 2 * MM * u[1]), 2.2 * MM), MPLATE_Y[0] - 0.1, MPLATE_Y[1] + 0.1))
            for k in (1, 4):                                                                                    # to the rail's web: its +-8 mm rows
                for dz in (-8 * MM, 8 * MM): mp = mp.cut(hy((rail_x(k), WHEEL_R + dz), 4.4 * MM))
            part(f"corner_{nm}", f"motor_plate_{nm}", mir(mp, s), "cut",
                 "rear motor plate, cut from 2.5 mm (0.1 in) 6061 aluminium sheet to the drawing (or drilled from a goBILDA 1123-0048-0144 with the STEP printed 1:1 as a template): "
                 "the axle's 14 mm bearing hole, the motor's 14 mm boss hole and its four M4 slotted 2 mm along the belt for tension, four M4 to the rail's web")
        part(f"corner_{nm}", f"wheel_{nm}", mir(dress(cyl("y", (ax, WHEEL_R), 2 * WHEEL_R, *WHEEL_Y), "mecanum_A" if sx > 0 else "mecanum_B", (ax, sum(WHEEL_Y) / 2, WHEEL_R), (0, 1, 0), (1, 0, 0)), s), "buy",
             "goBILDA 3213-3606-0002 96 mm mecanum, one wheel of the set of 4 (its rollers make an X seen from above)")
        part(f"corner_{nm}", f"wheel_shaft_{nm}", mir(cyl("y", (ax, WHEEL_R), 8 * MM, RAIL_OUT - CH_T, OPLATE_Y[1] + 0.05), s), "buy",
             "goBILDA 2106-4008-0800 8mm REX shaft, 80 mm, in a 1611 bearing in the rail's web and one in the outer plate; spacers and an e-clip")
        part(f"corner_{nm}", f"drive_motor_{nm}", mir(motor_y(MPLATE_Y[0], (mx, mz), 1), s), "buy", "goBILDA 5203-2402-0014 Yellow Jacket, 435 RPM (13.7:1), as last season's drive (DECODE's Pedro constants: 537.7 ticks a rev, 96 mm wheels, 1:1)")
        part(f"corner_{nm}", f"drive_belt_{nm}", mir(belt_y((ax, WHEEL_R), (mx, mz), PULLEY24_D, *PULLEY_Y), s), "buy",
             f"goBILDA 3417-4008-0024 24T HTD5 pulleys x2, 3412-0009-0{DRIVE_BELT[sx]} belt")
# ---- odometry pods (goBILDA 4-bar, for 96 mm wheels; the Pinpoint's): a forward pod on the left, a strafe pod on the right
POD_H, POD_W, POD_D = 69.5 * MM, 43 * MM, 41.5 * MM     # goBILDA's model: floor to the top of its block; across its back plate; back plate to the far side, the way its wheel rolls
# Both pods on the right rail (the left, under the launcher, has the feeder's drive), by their own tapped M4 holes
# (goBILDA's model), each with two M4 through the web on its +-16 mm rows, so at a station that's a multiple of 24 mm.
# The forward pod inside the rail, by its plate's side face (two holes 32 mm apart, up and down) on a printed adapter
# that fills the channel: the rail's 96 mm (k 11; 120 has the outer plate's standoff). The strafe pod is 43 mm wide
# across its back plate, more than fits between the lane walls' standoffs inside, so it's outside the web, between the
# wheels, its back plate on a 5 mm printed spacer (pocketed over the walls' screw heads): one column of its 32 mm square
# at the rail's 168 mm (k 20), the screws' heads inside the channel.
ODO_K = {"F": 11, "S": 20}
ODO_FWD_X = RAIL_X[0] + (8 + 8 * ODO_K["F"]) * MM + POD_D / 2 - 5.5 * MM       # its side holes 5.5 mm from its plate's back
ODO_STRAFE_XC = RAIL_X[0] + (8 + 8 * ODO_K["S"]) * MM + 16 * MM                # its back plate's middle: its holes 16 mm either side
ODO_SPACER_T = 5 * MM
ODO_STRAFE_Y = -(RAIL_OUT + ODO_SPACER_T)                                         # its back plate's face
part("odometry", "odo_pod_forward", dress(box(ODO_FWD_X - POD_D / 2, ODO_FWD_X + POD_D / 2, -(RAIL_IN - 0.15), -(RAIL_IN - 0.15) + POD_W, 0, POD_H), "3110-0001-0002",
     (ODO_FWD_X - POD_D / 2, -(RAIL_IN - 0.15) + POD_W / 2, 0), (0, 0, 1), (1, 0, 0)), "buy",
     "goBILDA 3110-0001-0002 4-bar odometry pod (96 mm drive wheel version), its wheel rolling along X: screwed by its plate's side face (its tapped holes) to a printed adapter from the right rail")
part("odometry", "odo_pod_strafe", dress(box(ODO_STRAFE_XC - POD_W / 2, ODO_STRAFE_XC + POD_W / 2, ODO_STRAFE_Y - POD_D, ODO_STRAFE_Y, 0, POD_H), "3110-0001-0002",
     (ODO_STRAFE_XC, ODO_STRAFE_Y, 0), (0, 0, 1), (0, -1, 0)), "buy",
     "goBILDA 3110-0001-0002 4-bar odometry pod, its wheel rolling along Y: outside the right rail between the wheels, its back plate on a printed spacer on the web")
for n, x0, x1 in (("F", ODO_FWD_X - POD_D / 2 + 0.06, ODO_FWD_X + POD_D / 2 + 0.05),):
    ad = box(x0, x1, -(RAIL_IN - 0.15), -(RAIL_OUT - CH_T), RAIL_Z[0] + CH_T, 2.66)   # fills the rail up to under its top flange (the pod's upper screw is at 2.52)
    for xf in LM_FEET: ad = ad.cut(box(xf - 0.17, xf + 0.17, -9, 0, 2.50, 3))       # clear of the launcher feet's nuts under the flange
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
    part(f"front_{n}", f"front_pad_{n}", mir(box(FACE + 0.45, FACE + 0.65, RAIL_OUT + 0.05, HALF_W - 0.05, RAIL_Z[0] + 0.1, 4.0), s), "print",
         "TPU 95A gyroid lattice, 10-20% infill, no top skin, 5 mm (or memory foam on a backer, as the drop test picks): the soft pad on the flap root's front face, where the spill lands; three countersunk M3 into the root, swappable")
    part(f"front_{n}", f"flap_face_{n}", mir(foam, s), "print", "swappable face plate on three M3 screws into the flap's heat-set inserts: TPU 95A as a gyroid lattice, 10-20% infill, no top skin (it crushes and soaks up the hit); for the drop test also solid TPU (the control) and memory foam on a 1 mm backer (the deadest reference), all the same outline")

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
    (bx0, bx1), (bz0, bz1) = EX_BLOCK         # doc/ramp-hook.md: 1.4 in deep, a 0.12 in upright strip at the front, the front leaning
    blk = slab_y([(bx0, bz0), (bx1, bz0), (bx1, bz0 + 0.12), (bx1 - 0.2, bz1), (bx1 - 0.7, bz1), (bx0, bz0 + 0.35)], -2.4, 2.4)   # back 0.2 in to a 0.5 in flat top
    blk = blk.cut(cyl("z", (bx1 + 1.395 - 0.25, 0), 2.79, 0, 3))   # its front curved to the bottom ring's 2.79 in hole, 0.25 in deep
    blk = blk.cut(placed(cq.Workplane("XY").polygon(6, 2 * (REX_FLATS + 0.1 * MM) / math.cos(math.pi / 6)).extrude(5.0), (EX_CROSS[0], -2.5, EX_CROSS[1]), (0, 1, 0)))
    out["ex_block"] = (blk, "print", "PETG: the FLOWER block (doc/ramp-hook.md's profile), threaded on the cross shaft (a REX bore), clamping collars either side")
    res = {}
    for k, (w, kind, what) in out.items():
        res[k] = (w if k.startswith("ex_stub") else rot_y(w, EX_AXIS, deg), kind, what)
    return res
for k, (w, kind, what) in extractor(EX_STOW).items(): part("extractor", k, w, kind, what, moves="extractor")
EX_GEAR_D = 30 * MM                     # a 1:1 pair of 30-tooth module 1 gears: each pitch diameter is the centres
EX_GEAR_SERVO = (EX_AXIS[0], EX_AXIS[1] + EX_GEAR_D)   # the servo's spline straight over the stub, its case over the flap's top
EX_GEAR_Y = (-7.04, -6.82)              # on the servo's horn, past its spline's end
EX_SERVO_FACE = -7.30                   # the case's spline face; its tabs 0.34-0.44 in outboard of it, on the mount
part("extractor", "ex_servo", servo("2000-0025-0002", (EX_AXIS[0], EX_SERVO_FACE, EX_AXIS[1] + 30 * MM), (0, 1, 0), (-1, 0, 0)), "buy",
     "goBILDA 2000-0025-0002 Torque servo, straight over the right stub, its spline inboard: its tabs on the mount (4 M4), its case out through the mount's window over the flap")
def ex_servo_mount():
    """The extractor servo's mount: a plate its tabs screw to (its case through a window), on a cap over the right flap's
    tall top edge (two M3 down into the flap)."""
    root, tip = (FACE, HALF_W), FLAP_TIP
    ang = math.atan2(tip[1] - root[1], tip[0] - root[0]); L = math.hypot(tip[0] - root[0], tip[1] - root[1])
    cap = box(L * 1.9 / 3.4, L * 2.9 / 3.4, -0.25 - 0.6, 0, 5.0, 5.25).union(box(L * 1.9 / 3.4, L * 2.9 / 3.4, -0.25 - 0.15, -0.25, 4.45, 5.0))   # a cap on the flap's top, a cheek down its inner face
    cap = cap.rotate((0, 0, 0), (0, 0, 1), math.degrees(ang)).translate((root[0], root[1], 0))
    zc, yt = EX_GEAR_SERVO[1], EX_SERVO_FACE - SERVO_TAB[1]
    pl = box(EX_AXIS[0] - 1.54, EX_AXIS[0] + 0.75, yt - 0.25, yt, 5.05, zc + 0.57).cut(box(EX_AXIS[0] - 1.201, EX_AXIS[0] + 0.414, -10, 0, zc - 0.414, zc + 0.414))
    nrm = (-math.sin(ang), math.cos(ang))       # the flap's outward normal (left side)
    v = -0.25 - 0.15                            # the cheek's inner face
    pts = [((root[0] + u * math.cos(ang) + v * nrm[0], -(root[1] + u * math.sin(ang) + v * nrm[1]), 4.75), (nrm[0], -nrm[1], 0)) for u in (L * 2.05 / 3.4, L * 2.75 / 3.4)]
    return mir(cap, -1).union(pl).cut(cyl("y", EX_AXIS, 0.62, -9, -6)), pts   # clear of the stub and its bearing
_m, EX_MOUNT_SCREWS = ex_servo_mount()
part("extractor", "ex_servo_mount", _m, "print",
     "PETG-CF: the extractor servo's mount: the servo's tabs screw to its plate (4 M4 into inserts), its case out through the window; it sits astride the right flap's tall top, two M3 through its cheek into the flap")
_gw = EX_GEAR_Y[1] - EX_GEAR_Y[0]
_gs = spur_gear(30, 1, _gw, 6 * MM)
for a in range(4): _gs = _gs.cut(cq.Workplane("XY").circle(1.7 * MM).extrude(_gw).translate((8 * MM * math.cos(math.pi / 4 + a * math.pi / 2), 8 * MM * math.sin(math.pi / 4 + a * math.pi / 2), 0)))
part("extractor", "ex_horn", cyl("y", EX_GEAR_SERVO, 24 * MM, EX_SERVO_FACE + 0.03, EX_GEAR_Y[0]), "buy",
     "goBILDA 25T-spline servo horn (round, 4 M3; CHECK the SKU): on the extractor servo's spline, its gear screwed to it")
part("extractor", "ex_gear_servo", placed(_gs, (EX_GEAR_SERVO[0], EX_GEAR_Y[0], EX_GEAR_SERVO[1]), (0, 1, 0), (math.cos(math.pi / 30), 0, -math.sin(math.pi / 30))), "print",   # half a tooth round: meshed
     f"PETG-CF: the extractor's servo gear, on the servo's 25T horn (4 M3 on a 16 mm circle, CHECK on the horn), mod 1, {round(EX_GEAR_D * 25.4)} teeth, 1:1 with the stub's")
part("extractor", "ex_gear_stub", placed(spur_gear(30, 1, _gw, "rex"), (EX_AXIS[0], EX_GEAR_Y[0], EX_AXIS[1]), (0, 1, 0)), "print",
     f"PETG-CF: the extractor's stub gear, clamped on the right stub (REX bore, a set screw), mod 1, {round(EX_GEAR_D * 25.4)} teeth")

# ---- intake (moves: lift 0 .. FLOAT)
def lift_deg(lift):
    v = (ROLL[0] - PIVOT[0], ROLL[1] - PIVOT[1]); r = math.hypot(*v); a0 = math.atan2(v[1], v[0])
    return math.degrees(math.asin((v[1] + lift) / r) - a0)

ROLL_WHEELS = 10
def intake(lift):
    out = {}
    rl = cyl("y", ROLL, 2 * ROLL_R, -ROLL_HALF, ROLL_HALF)
    for k in range(ROLL_WHEELS): dress(rl, "3632-4008-0048", (ROLL[0], -ROLL_HALF + 8 * MM + k * (2 * ROLL_HALF - 16 * MM) / (ROLL_WHEELS - 1), ROLL[1]), (0, 1, 0), (1, 0, 0))
    out["roller"] = (rl, "buy", f"goBILDA 3632-4008-0048 48 mm Gecko wheels x{ROLL_WHEELS}, as the mentor's roller, evenly spaced across the {2 * ROLL_HALF:.1f} in span on 8mm REX spacers")
    out["roller_shaft"] = (cyl("y", ROLL, 8 * MM, -ARM_Y[1], ARM_Y[1]), "buy", "goBILDA 8mm REX shaft, cut to 371 mm (CHECK)")
    mx, mz = INTAKE_MOTOR; top = mz + PULLEY24_D / 2 + 0.17          # over the motor's pulley and its belt
    for s, n in SIDES:                  # both arms the same: pivot to roller, and up to the motor's height
        y0, y1 = ARM_Y
        arm = slab_y(_strip(PIVOT, ROLL, 0.35), y0, y1).union(cyl("y", ROLL, 0.8, y0, y1)).union(cyl("y", PIVOT, 0.8, y0, y1))
        arm = arm.union(slab_y(_strip(ROLL, INTAKE_MOTOR, 0.4), y0, y1)).union(cyl("y", INTAKE_MOTOR, 1.1, y0, y1))
        arm = arm.union(box(mx - 0.1, mx + 0.75, y0, y1, mz, top + 0.25))     # (its front half: lifted, the back swings toward the front cross channel)
        out[f"intake_arm_{n}"] = (mir(arm, s), "print", "PETG-CF, 7.6 mm: swing arm, pivot to the roller's bearing and up to the motor's height; the two arms, the roller's shaft and the tie make one stiff frame")
    face = BELT_Y[0] - 0.02             # the motor's face, inboard of its belt
    mm_ = box(mx - 0.75, mx + 0.75, face - 0.25, face, mz - 0.75, mz + 0.75).union(box(mx - 0.1, mx + 0.75, face - 0.25, face, mz, top + 0.25)).union(
          box(mx - 0.1, mx + 0.75, face - 0.25, ARM_Y[0], top, top + 0.25))
    mm_ = mm_.cut(cyl("y", INTAKE_MOTOR, 14 * MM, face - 1, face + 1))
    out["intake_motor_mount"] = (mir(mm_, -1), "print",
         "PETG-CF: the intake motor's mount: a plate on the motor's face (4 M4 into it, the shaft through), bridged over its pulley and belt to the right arm (2 M4 into inserts)")
    tie_y = (0.30, 0.30 + 120 * MM)
    out["intake_tie"] = (cyl("y", INTAKE_MOTOR, 8 * MM, *tie_y), "buy", "goBILDA 1516-4008-1200 8mm REX standoff, 120 mm (CHECK it's stocked): the left arm to the motor's cradle, on the motor's axis")
    cr = cyl("y", INTAKE_MOTOR, 1.75, -0.95, -0.35).cut(cyl("y", INTAKE_MOTOR, 36.4 * MM, -1, -0.3)).union(cyl("y", INTAKE_MOTOR, 0.7, -0.14, tie_y[0] + 0.25))
    cr = cr.cut(placed(cq.Workplane("XY").polygon(6, 2 * (REX_FLATS + 0.1 * MM) / math.cos(math.pi / 6)).extrude(0.5), (mx, tie_y[0] - 0.25, mz), (0, 1, 0)))
    out["intake_cradle"] = (cr, "print",
         "PETG: the intake motor's cradle: a ring clamped round the can's far end (an M3 pinch screw), the tie standoff in its REX socket (a set screw), so the motor spans both arms")
    out["roller_belt"] = (mir(belt_y(INTAKE_MOTOR, ROLL, PULLEY24_D, *BELT_Y), -1), "buy",
                          f"goBILDA 3417-4008-0024 24T HTD5 pulleys x2, 3412-0009-0{ROLLER_BELT} belt")
    out["intake_motor"] = (mir(motor_y(face, INTAKE_MOTOR, 1, MOTOR_L1), -1), "buy", "goBILDA 5203-2402-0003 Yellow Jacket, 1620 RPM: the roller (about 170 in/s at its surface)")
    d = lift_deg(lift)
    return {k: (rot_y(w, PIVOT, d), kind, what) for k, (w, kind, what) in out.items()}
for k, (w, kind, what) in intake(0).items(): part("intake", k, w, kind, what, moves="intake")

# ---- lane
(rx0, rz0), (rx1, rz1) = RAMP
SKIN_T = 0.10                           # the ramp's soft skin: the simulator's biggest lever is bounce where the spill lands
SKIN_X1 = 7.0                           # (the body's front and the mouth: 0.4 to 0.1 is worth 6 to 9 points), back to here, then thin to the tip
MOUTH_HALF = RAIL_IN - 0.08            # the mouth: a piece rolls in on the tiles anywhere across it, and the stars, low, reach its
                                        # middle there and centre it before it climbs the ramp
FLANGE_HOLE_Y = RAIL_OUT - 8.0 * MM     # the low-side flanges' row of 4 mm holes, 8.0 mm from the web's outer face (goBILDA's STEP)
MOUTH_TABS = (rail_x(32), rail_x(38))   # clear of his front corners' hubs inside the rail (X 3.68 .. 4.94)
RAMP_EDGE = RAIL_IN - 0.01              # the ramp spans the mouth out to the rails' flanges' inner edges
LANE_WALL_FRONT = 3.12                  # the lane walls' front ends (the ceiling's posts on them at 2.9), beside the ramp's top: notched round them
for s_, n in SIDES:
    ys = (0, RAMP_EDGE) if s_ > 0 else (-RAMP_EDGE, 0)
    def ramp_z(x): return rz1 + (rx1 - x) / (rx1 - rx0) * (rz0 - rz1)
    rmp = slab_y([(rx0, rz0 - SKIN_T), (rx1, rz1 - SKIN_T), (rx1, rz1 - SKIN_T - 0.15), (rx0, rz0 - SKIN_T - 0.15)], *ys)
    notch = box(rx1 - 0.1, LANE_WALL_FRONT + 0.02, s_ * (WALL_IN - 0.01), s_ * 2.52, -1, 3)        # the walls' ends and the ceiling's posts
    rmp = rmp.cut(notch)
    part("lane", f"ramp_skin_{n}", slab_y([(rx1, rz1), (rx0, rz0), (rx0, rz0 - SKIN_T), (rx1, rz1 - SKIN_T)], *ys).cut(notch), "print",
         "TPU 95A gyroid lattice, 10-20% infill, 2.5 mm, one thin top skin (pieces roll up it): the ramp's soft skin; four countersunk M3 into the ramp, swappable like the flaps' faces")
    for x in MOUTH_TABS:                # a tab on the rail's bottom flange, a web down its edge to the ramp
        rmp = rmp.union(box(x - 0.25, x + 0.25, s_ * (MOUTH_HALF + 0.01), s_ * (RAIL_OUT - CH_T - 0.01), RAIL_Z[0] + CH_T, LANE_BOTTOM))
        rmp = rmp.union(box(x - 0.25, x + 0.25, s_ * (MOUTH_HALF + 0.01), s_ * RAMP_EDGE, ramp_z(x) - SKIN_T - 0.15, LANE_BOTTOM))
    part("lane", f"mouth_floor_{n}", rmp, "print", "PETG: half the ramp (the mouth's whole width), from the tiles beside the star wheels up to the lane, "
         "on two tabs on the rail's bottom flange")
LANE_X = (COL_X + 1.4, FACE - 0.16)
WALL_SPLIT = 1.65
for s, n in SIDES:
    xm = WALL_SPLIT                                               # between shafts 2 and 3, so each half holds whole bearings and two standoffs
    for h, (a, b) in (("front", (xm, LANE_WALL_FRONT)), ("rear", (LANE_X[0], xm))):   # ahead of these, the ramp and the stars
        top = 2.6
        part("lane", f"lane_wall_{n}_{h}", mir(box(a, b, WALL_IN, WALL_IN + WALL_T, 0.3, top), s), "print", "PETG, 1/4 in: half a lane wall with its shafts' bearings; on two standoffs to the rail")
WALL_STANDOFFS = {"rear": ((rail_x(19), 0), (rail_x(25), 1)), "front": ((rail_x(27), 1), (rail_x(28), 0))}   # X, row (0: 8 mm over the
                                        # axles' row, clear of the gate servo; 1: 8 mm under it); between the odometry pods, and behind the
                                        # front wheels (the screws' heads are outside the rails' webs)
WS_Z = (WHEEL_R + 8 * MM, WHEEL_R - 8 * MM, WHEEL_R + 16 * MM, WHEEL_R - 16 * MM)   # the web's rows (at a 24 mm station only the +-16 ones)
WALL_STANDOFFS_L = {"rear": ((rail_x(22), 1), (rail_x(25), 0))}   # the left rear wall's both ahead of the feeder's motor (its reserve ends at X 0.83)
def wall_standoffs(n): return {h: (WALL_STANDOFFS_L.get(h, v) if n == "L" else v) for h, v in WALL_STANDOFFS.items()}
for s, n in SIDES:
    for h, sts in wall_standoffs(n).items():
        for k, (x, row) in enumerate(sts):
            part("lane", f"wall_standoff_{n}_{h}{k}", mir(cyl("y", (x, WS_Z[row]), 6 * MM, WALL_IN + WALL_T, RAIL_OUT - CH_T), s), "buy", "two goBILDA 1501-0006-0400 M4 standoffs (40 mm) on an M4 set screw: 80 mm (goBILDA's 1501s stop at 56 mm)")
# the strafe odometry pod's spacer (the pod is above, in the odometry section)
sp = box(ODO_STRAFE_XC - POD_W / 2, ODO_STRAFE_XC + POD_W / 2, ODO_STRAFE_Y, -RAIL_OUT, RAIL_Z[0], RAIL_Z[1])
for x, row in WALL_STANDOFFS["rear"] + WALL_STANDOFFS["front"]: sp = sp.cut(cyl("y", (x, WS_Z[row]), 8.5 * MM, -9, 0))   # the walls' screw heads
part("odometry", "odo_spacer_S", sp, "print", "PETG, 5 mm: the strafe pod's spacer on the right rail's web, pocketed over the lane walls' screw heads")
# ---- the mentor's star wheels, servo-driven through one-way bearings (drive TBD: his CAD draws none)
for s, n in SIDES:
    sx, sy = STAR[0], s * STAR[1]
    part("lane", f"star_{n}", dress(cyl("z", (sx, sy), 2 * STAR_R, STAR_Z - STAR_W / 2, STAR_Z + STAR_W / 2), "SR-INTAKEWHEEL-35-7mm", (sx, sy, STAR_Z), (0, 0, 1), (1, 0, 0)), "buy",
         "SWYFT Intake Wheel 3.5 in, 7 mm hex (SR-INTAKEWHEEL-35-7mm, 4 for $24.99): 0.5 in wide, 30A TPE over-molded on a nylon core, its black 7 mm hex insert; drawn from SWYFT's own STEP, as bought (the mentor cuts the rim into flaps)")
    part("lane", f"star_shaft_{n}", cyl("z", (sx, sy), 8 * MM, STAR_Z - STAR_W / 2 - 0.1, STAR_HUB_Z[1] - 1 * MM), "buy",
         "8 mm ROUND hardened shaft (ground steel, h6), vertical: the clutch's rollers run on it, so not 8mm REX; the star on its lower end through a printed 7 mm hex adapter (a set screw on a ground flat)")
    part("lane", f"star_bearing_{n}", cyl("z", (sx, sy), 14 * MM, *STAR_BRG_Z).cut(cyl("z", (sx, sy), 8 * MM, 0, 10)), "buy",
         "goBILDA 8x14x5 mm round-bore flanged bearing (as in the mentor's launcher): the star shaft's, in the bracket's arm over the star")
    part("lane", f"star_clutch_{n}", cyl("z", (sx, sy), 20 * MM, *STAR_HUB_Z), "print",
         "PETG hub, 13.9-13.95 mm press bore for the one-way clutch (the mentor's: Amazon Sankoly-US SK230309GZZC-6P, read as HF081412, 8 x 14 x 12 mm drawn cup; "
         "CHECK with calipers): its top screwed to the servo's goBILDA 25T horn (4 M3), so the servo turns the hub, the clutch drives the shaft and the star one way only; a fast piece overruns it, and it can't be pushed backwards")
    sv = servo("2000-0025-0003", (sx, sy, SV_Z), (0, 0, -1), (-1, 0, 0))   # its spline down to the clutch's hub, its body back toward the drive motor
    part("lane", f"star_servo_{n}", sv, "buy", "goBILDA 2000-0025-0003 Speed servo, continuous mode, over the star, its spline on the star's axis")
    top = SV_Z + SERVO_TAB[0]               # the servo's tabs sit on a plate, its case down through a window in it
    br = box(sx - 1.54, sx + 0.75, STAR[1] - 0.40, 4.45, top - 0.25, top).cut(box(sx - 1.201, sx + 0.414, 0, STAR[1] + 0.414, 0, 10))
    br = br.union(box(UCH_FRONT, 4.05, 4.4, 4.85, PIVOT[1] - 0.45, top))                             # down the U-channel mount's front face
    arm = box(sx - 0.42, sx + 0.42, STAR[1] - 0.42, 4.85, *STAR_BRG_Z).union(cyl("z", (sx, STAR[1]), 0.84, *STAR_BRG_Z))   # the star shaft's bearing arm, over the star,
    br = br.union(arm.cut(cyl("z", (sx, STAR[1]), 14 * MM, 0, 10)))                                       # out to the pivot's boss (the bearing pressed in from below)
    br = br.union(box(PIVOT[0] - 0.3, sx + 0.42, 4.6, 4.85, STAR_BRG_Z[0], PIVOT[1] - 0.45))
    br = br.union(box(PIVOT[0] - 0.3, PIVOT[0] + 0.3, 4.75, 4.95, PIVOT[1] - 0.45, top))        # the arms' pivot, inboard of the arm
    br = br.union(box(UCH_FRONT, PIVOT[0] + 0.3, 4.6, 4.85, PIVOT[1] - 0.45, PIVOT[1] - 0.25))
    br = br.union(box(PIVOT[0] - 0.3, PIVOT[0] + 0.3, 4.6, 4.95, PIVOT[1] - 0.45, PIVOT[1] + 0.35))   # the pivot's boss, deep enough for its insert
    br = br.union(box(UCH_FRONT, 3.58, 4.4, RAIL_OUT - 0.02, RAIL_Z[1], RAIL_Z[1] + 0.2))             # a foot on the rail's top flange
    br = br.union(box(UCH_FRONT, 3.45, 4.4, 4.80, RAIL_Z[1], PIVOT[1] - 0.45))                         # up to the face, clear of the star
    part("lane", f"front_bracket_{n}", mir(br, s), "print",
         "PETG: on the rail's top flange (his motor fills the U-channel mount, so nothing bolts through that); the star's servo stands on its plate by its tabs (4 M4 into inserts), "
         "its case down through the plate's window to the clutch's hub; and it carries the intake arm's pivot (a shoulder screw)")
for k, x in enumerate(LANE_SHAFTS):
    part("lane", f"lane_shaft_{k}", cyl("y", (x, LANE_BOTTOM - 12 * MM), 8 * MM, -WALL_IN - WALL_T, WALL_IN + WALL_T), "buy", "goBILDA 8mm REX shaft, 144 mm (2106-4008-1440)")
    part("lane", f"lane_roller_{k}", cyl("y", (x, LANE_BOTTOM - 12 * MM), 24 * MM, (-1 if k % 2 else 0) * 1.0, (0 if k % 2 else 1) * 1.0), "print", "TPU 95A: 24 mm x 1 in lane roller, free on two 8x14x5 bearings (the lane is passive: the roller's throw and the queue carry each piece back)")
CEIL_X = (-0.55, 4.5)                    # from just ahead of the launcher's front uprights (X -0.63) to the front drive motors
CEIL_Z = LANE_BOTTOM + 2 * RP - 0.2     # the free rollers' bottoms, at rest (a POLLEN presses them 0.2)
CEIL_POSTS = (2.90,)                    # X: the ceiling hinges on one post a side at its front (his front drive motors cross over the
                                        # lane behind it, from X 1.0 to 2.6, so no post fits there); its rear is free, banded down
ceil = box(*CEIL_X, -WALL_IN, WALL_IN, CEIL_Z + 0.8, CEIL_Z + 0.95)
for x in CEIL_POSTS: ceil = ceil.union(box(x - 0.17, x + 0.17, -(WALL_IN + WALL_T - 0.01), WALL_IN + WALL_T - 0.01, CEIL_Z + 0.8, CEIL_Z + 0.95))   # ears, against the posts' inner faces
part("lane", "ceiling", ceil, "print", "PETG: the sprung ceiling's frame, hinged at its front on the posts' pins, its rear banded down (a piece lifts it), carrying free rollers")
for s, n in SIDES:
    for k, x in enumerate(CEIL_POSTS):
        part("lane", f"ceiling_post_{n}{k}", mir(box(x - 0.17, x + 0.17, WALL_IN + WALL_T, 2.5, 1.0, CEIL_Z + 0.95), s), "print",
             "PETG: a ceiling post, screwed to the wall's outer face; the ceiling's hinge pin through its top")
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
        if OLD_FEEDER.match(e["file"]) or LM_DROP.match(e["file"]): continue
        w = cq.Workplane().add(cq.Shape.cast(sh)); mv = None
        if re.match(r"^Hole_Lowside_U-Channel_GB_[46]__1", e["file"]):   # his side channels end at the rear blocks, clear of our rear belts
            w = w.intersect(box(-4.98, 20, -20, 20, -20, 20))
        if e["file"].startswith("Hole_Lowside_U-Channel_GB_3__1"):   # his right rear upright: its flanges trimmed behind the right yoke
            w = w.intersect(box(-20, -4.72, -20, 20, -20, 20))
        if re.match(r"^Hole_Lowside_U-Channel_GB_(10|12)__1", e["file"]):   # his back plates: only the flywheels' part (above the feeder's arms)
            w = w.intersect(box(-20, 20, -20, 20, 4.75, 20))
        if re.match(r"^Hole_Lowside_U-Channel_GB_(9|10|11|12)__1", e["file"]):   # the bearing blocks' windows, 0.41 in outward: cut rough,
            for sg in (1, -1):                                                     # 0.06 in oversize (the printed guides do the fitting)
                y0, y1 = sorted((sg * (FLY_AXIS[0] - 0.52), sg * (FLY_AXIS[0] + 0.52 + FLY_TRAVEL)))
                w = w.cut(box(-20, 20, y0, y1, FLY_AXIS[1] - 0.62, FLY_AXIS[1] + 0.62))
        if e["file"].startswith("feeder_floor"):            # notched round the feeder arms' rear blocks
            w = w.cut(box(-4.70, -3.80, 0.6, 20, -20, 20)).cut(box(-4.70, -3.80, -20, -0.6, -20, 20))
        if LM_RAISE.match(e["file"]): w = cq.Workplane().add(solid(w).translate(cq.Vector(0, 0, LM_RAISE_Z)))
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
LM_CH_Z = 3.843                         # the launcher's side channels' undersides
LM_FOOT_Y1 = 5.669                      # the launcher's side channel's outer edge
LM_HOLE_Y, LM_HOLE_X0 = 5.54, -0.665    # its flange's holes (read from his model): every 8 mm along X from this one
FOOT_SEAT = 3.30                        # the foot-to-rail screw's head, in its pocket
TURRET_TOP = "lm_2325-0105-0176_1"        # the turret's 176T ring: the hood screws to its top
HOOD_FLANGE_D = 5.6                     # the hood's flange on the turret's top, four M4 down into it
# Four springs (11 Oct): each flywheel side and each feeder wheel opens on its own, so a NECTAR waiting in the feeder never
# opens the flywheels under a POLLEN being shot. Each feeder wheel is on its own arm, hung from a pivot shaft right under
# it (the wheel moves sideways, 0.04 in down over its 0.41). On the left that pivot shaft is also the drive: the feeder's
# motor, bolted to the frame, belts to it (belt A, behind the launcher's back plates), and a belt along the arm (belt B)
# takes it up to the wheel. Both belts keep their length however the arm swings, so the motor never moves.
LT_Z = (RAIL_Z[0] + CH_T, 1.175)      # the launcher tray: on the rails' bottom flanges, under the feeder floor
LT_X = (-4.65, -1.30)
LT_TABS = (rail_x(8), rail_x(10))     # its screws into the rails' bottom flanges
FEED_PIVOT_Z = FEED_Z - centres(215)    # 2.0: the arm's pivot, 47.5 mm (a 215 mm belt) under the wheel's axle
FEED_SWING = math.degrees(math.asin(FLY_TRAVEL / centres(215)))   # 12.7 deg: the arm's swing for a NECTAR
FEED_MOTOR = (FEED_Y + math.sqrt(centres(215) ** 2 - 0.2 ** 2), FEED_PIVOT_Z + 0.2)   # Y, z: the feeder's motor (a 5203, 1150 RPM
                                        # to start), along X, fixed to the frame; belt A (215 mm) from it to the left pivot shaft
FEED_MOTOR_FACE = -4.17                 # its face on the rear block; its shaft back to belt A's plane
BELT_A_X = (-5.04, -5.04 + PULLEY_W)    # belt A, behind the launcher's back plates
BELT_B_X = (-3.90, -3.90 + PULLEY_W)    # belt B, between the arm's rear plate and the wheels
ARM_X = ((-4.15, -3.91), (-1.40, -1.16))   # each arm's two plates, either side of its wheels
if USE_LM:
    FX = (-3.307, -1.413)               # the flywheels' wheels' span along X (his module)
    for s, n in SIDES:
        y, mv = s * FEED_Y, f"feed_{n}"
        fw = cyl("x", (y, FEED_Z), 2 * FEED_R, *FX)
        for xc in (FX[0] + 8 * MM, FX[1] - 8 * MM): dress(fw, "3632-4008-0048", (xc, y, FEED_Z), (1, 0, 0), (0, 1, 0))
        part("launcher", f"feeder_wheel_{n}", fw, "buy",
             "goBILDA 3632-4008-0048 48 mm Gecko wheels x2 (as the roller's): " + ("driven through its arm's belt" if s > 0 else "free, in the pad's place"), mv)
        part("launcher", f"feeder_shaft_{n}", cyl("x", (y, FEED_Z), 8 * MM, ARM_X[0][0], ARM_X[1][1]), "buy",
             "goBILDA 8mm REX shaft, in 1611 bearings in the arm's two plates" + ("; belt B's 24T pulley on it" if s > 0 else ", the wheels free on bearings"), mv)
        arm = None
        for a, b in ARM_X:
            pl = cq.Workplane("YZ").polyline(_strip((y, FEED_PIVOT_Z), (y, FEED_Z), 0.32)).close().extrude(b - a).translate((a, 0, 0))
            pl = pl.union(cyl("x", (y, FEED_PIVOT_Z), 0.64, a, b)).union(cyl("x", (y, FEED_Z), 0.64, a, b))
            if a < -3: pl = pl.union(box(-4.03, b, s * 1.40, s * 1.70, LT_Z[1] + 0.26, FEED_PIVOT_Z - 0.2))   # the rear plate's tab: rests on the block's foot
            arm = pl if arm is None else arm.union(pl)
        part("launcher", f"feeder_arm_{n}", arm, "print", "PETG-CF, 6 mm: the feeder wheel's arm, two plates either side of the wheels on 1611 bearings, swinging on the pivot shaft; "
             "an extension spring (or a band) from it to the launcher's frame pulls it in, a stop screw sets the rest gap (the rig sets both)", mv)
        part("launcher", f"feeder_pivot_{n}", cyl("x", (y, FEED_PIVOT_Z), 8 * MM, BELT_A_X[0] if s > 0 else -4.41, -0.97), "buy",
             "goBILDA 8mm REX shaft: the arm's pivot" + (", and the feeder's drive: belt A's pulley behind, belt B's on the arm, turning in 1611 bearings in the rear and front blocks" if s > 0 else ", in the rear and front blocks"))
        blk = box(-4.41, -4.17, s * 1.6, s * (4.65 if s > 0 else 3.25), LT_Z[1], FEED_PIVOT_Z + 0.6)
        foot = box(-4.06, -3.92, s * 1.06, s * 1.85, LT_Z[1], LT_Z[1] + 0.25).union(box(-4.41, -4.06, s * (1.18 if s > 0 else 1.50), s * 1.85, LT_Z[1], LT_Z[1] + 0.25))   # clear of the backstop and belt B
        blk = blk.union(foot).union(box(-4.41, -3.95, s * 2.9, s * 3.05, 2.98, 3.42))          # the spring's lug, outboard of the arm
        part("launcher", f"feeder_rear_block_{n}", blk, "print",
             "PETG: the arm's rear pivot block" + (", the feeder motor's face plate" if s > 0 else "") + ", the spring's lug, and the foot the arm's tab rests on (an M3 set screw in it sets the rest gap), on the launcher tray")
        part("launcher", f"feeder_front_block_{n}", box(-1.15, -0.97, s * 1.75, s * 2.25, 0.7, 2.3), "print", "PETG: the arm's front pivot block, screwed to the lane wall's end")
    part("launcher", "feeder_belt_B", belt_x((FEED_Y, FEED_PIVOT_Z), (FEED_Y, FEED_Z), PULLEY24_D, *BELT_B_X), "buy",
         "goBILDA 3417-4008-0024 24T HTD5 x2, 3412-0009-0215 belt: the pivot shaft up the arm to the feeder wheel", "feed_L")
    part("launcher", "feeder_belt_A", belt_x(FEED_MOTOR, (FEED_Y, FEED_PIVOT_Z), PULLEY24_D, *BELT_A_X), "buy",
         "goBILDA 3417-4008-0024 24T HTD5 x2, 3412-0009-0215 belt: the feeder's motor to the left arm's pivot shaft")
    part("launcher", "feeder_motor", motor_x(FEED_MOTOR_FACE, FEED_MOTOR, -1), "buy",
         "goBILDA 5203-2402-0005 Yellow Jacket, 1150 RPM (CHECK the speed on the rig), its face on the left rear block: fixed, never moves")
if USE_LM:                              # v3: the turret's drive gear still points forward (as his); its servo and encoder B each
    TURRET_SWING = {"servo": 30, "enc_B": 270}   # swung about the gear's axis to an angle clear of the front drive motors, the
                                        # flywheels (open too) and the launch column (searched every 15 deg; belt and mesh unchanged)
    gk = [k for k in PARTS if k.startswith("lm_2302-0014-0064")][0]; gb = bb(PARTS[gk]["shape"])
    gax = ((gb[0] + gb[1]) / 2, (gb[2] + gb[3]) / 2)
    for k in list(PARTS):
        m = re.match(r"^lm_turret_(servo|belt|enc_B)", k)
        if not m: continue
        ang = TURRET_SWING["enc_B" if m.group(1) == "enc_B" else "servo"]
        PARTS[k]["shape"] = cq.Workplane().add(solid(PARTS[k]["shape"]).rotate(cq.Vector(gax[0], gax[1], 0), cq.Vector(gax[0], gax[1], 1), ang))
    tb = [bb(PARTS[k]["shape"]) for k in PARTS if re.match(r"^lm_(turret_|2302-|1231-)", k)]
    tp = box(max(-0.45, min(t[0] for t in tb) - 0.15), min(3.3, max(t[1] for t in tb) + 0.15), min(t[2] for t in tb) - 0.15, max(t[3] for t in tb) + 0.15, 6.811, 6.999)
    for k in [k for k in PARTS if re.match(r"^lm_(turret_|2302-|1231-)", k)]:
        b = bb(PARTS[k]["shape"])
        if b[4] < 6.999 and b[5] > 6.811 and overlap(tp, PARTS[k]["shape"]) > 1e-5: tp = tp.cut(box(b[0] - 0.02, b[1] + 0.02, b[2] - 0.02, b[3] + 0.02, 6, 8))
    part("launcher", "turret_drive_plate", tp, "cut", "3/16 in aluminium (or 6 mm PETG-CF): the turret's drive plate, his redrawn shorter (to X 3.3, clear of the front drive motors), "
         "with windows for the servo and the encoders; under the launcher's front top channel")
# The flywheel sides' mechanism (four springs): each side's shaft runs in two printed bearing blocks that slide 0.41 in
# in slots in his front and back plates (like a belt tensioner); a printed yoke over the motor ties it to the rear block,
# so the side's wheels, shaft, pulleys, belt and motor move together and the belt keeps its length; an extension spring
# from the yoke to an anchor on the rear top channel pulls it in, and an M4 stop screw in the anchor sets the rest gap
if USE_LM:
    for s, n in SIDES:
        tr = box(*LT_X, 0, s * (RAIL_OUT - CH_T - 0.01), *LT_Z)
        if s < 0: tr = tr.cut(box(ODO_FWD_X - POD_W / 2 - 0.08, ODO_FWD_X + POD_W / 2 + 0.08, -6, -3.0, 0, 3))   # the forward odometry pod drops through
        part("launcher", f"launcher_tray_{n}", tr, "print", "PETG, 3.3 mm: half the launcher's tray, across the rails' bottom flanges under the feeder floor; the feeder arms' blocks and spring posts stand on it")
if USE_LM:
    for s, n in SIDES:
        y, mv = s * FLY_AXIS[0], f"fly_{n}"
        for f_, (xa, xb), plate in (("front", (-1.08, -0.73), 11 if n == "L" else 9), ("rear", (-4.30, -3.95), 12 if n == "L" else 10)):
            ga, gb = sorted((s * (FLY_AXIS[0] - 0.50), s * (FLY_AXIS[0] + 0.47 + FLY_TRAVEL)))
            gd = box(xa, xb, ga, gb, FLY_AXIS[1] + 0.555, FLY_AXIS[1] + 0.75).union(box(xa, xb, ga, gb, FLY_AXIS[1] - 0.75, FLY_AXIS[1] - 0.555))
            ge = (ga, ga + 0.04) if s > 0 else (gb - 0.04, gb)                      # tied at the column's end, beyond the block's travel
            gd = gd.union(box(xa, xb, *ge, FLY_AXIS[1] - 0.75, FLY_AXIS[1] + 0.75))
            part("launcher", f"fly_guide_{n}_{f_}", gd, "print",
                 f"PETG-CF: the {f_} bearing block's guide, inside his plate's channel: a rail over and under the block (0.005 in each side, a sliding fit), "
                 "so his plate only needs a rough window; 2 M3 through the plate's web into its inserts")
        part("launcher", f"fly_block_{n}_front", box(-1.10, -0.62, y - 0.45, y + 0.45, FLY_AXIS[1] - 0.55, FLY_AXIS[1] + 0.55), "print",
             "PETG-CF: the flywheel's front bearing block, the shaft's 8x14 bearing in it, sliding 0.41 in between its printed guide's rails", mv)
        yk = box(-4.41, -3.93, y - 0.45, y + 0.45, FLY_AXIS[1] - 0.55, FLY_AXIS[1] + 0.55)                      # the rear bearing block, in the back plate's slot
        yk = yk.union(box(-4.70, -4.41, s * (FLY_AXIS[0] - 0.35), s * (FLY_AXIS[0] + 0.35), 6.78, 8.20))       # up behind the plate, over his rear channel
        yk = yk.union(box(-4.70, -4.42, s * (FLY_AXIS[0] - 0.35), s * 7.40, 7.95, 8.20)).union(box(-4.70, -2.19, s * 5.30, s * 7.40, 7.95, 8.20))   # over to the motor
        part("launcher", f"fly_yoke_{n}", yk, "print",
             "PETG-CF, one piece: the flywheel's rear bearing block (in the back plate's slot), up behind the plate and over to the motor's bracket, so the motor rides with the wheels", mv)
        part("launcher", f"fly_anchor_{n}", box(-4.71, -4.43, s * 0.55, s * (FLY_AXIS[0] - 0.36), 7.85, 8.38), "print",
             "PETG: the flywheel side's spring anchor and stop, on the rear top channel's front face; an M4 stop screw through it sets the rest gap (the rig sets it)")
        part("launcher", f"fly_spring_{n}", cyl("y", (-4.56, 8.24), 0.25, s * 0.75, s * (FLY_AXIS[0] - 0.36)), "buy",
             "extension spring, about 0.25 in OD: the anchor to the yoke (rate and preload from the rig; CHECK)")
        # the feeder arm's spring and stop: a post on the feeder floor's rear corner
        part("launcher", f"feeder_spring_{n}", cyl("y", (-4.05, 3.20), 0.2, s * 2.9, s * (FEED_Y + 0.33)), "buy",
             "compression spring, about 0.2 in OD, 0.6 in free: the rear block's lug pushes the arm in (rate and preload from the rig; CHECK)")
if USE_LM:
    for s, n in SIDES:
        for k, x in enumerate(LM_FEET):
            ft = box(x - 0.3, x + 0.3, 4.95, LM_FOOT_Y1, RAIL_Z[1], LM_CH_Z).cut(cyl("z", (x, FLANGE_HOLE_Y), 8.5 * MM, FOOT_SEAT, LM_CH_Z + 0.1))
            part("launcher", f"launcher_foot_{n}{k}", mir(ft, s), "print",
                 "PETG: a launcher foot, between the rail's top flange and the launcher's side channel: an M4 down through a pocket into a nut under the rail's flange "
                 "(fitted before the launcher goes on), and one down from inside the launcher's channel into an insert (the launcher lifts off its feet)")
HOOD_Z = 9.70 if USE_LM else KIT_Z + KIT_H   # on the turret's top (the module's kit is 9.68 at its top)
# The hubs sit on a deck on the front cross channel under printed roofs (below), and the turret turns the hood all the way
# round, so every shot leaves over them. The hood is a curved elbow: the ball runs up the column, starts to turn at
# HOOD_BEND_Z and leaves at LAUNCH_DEG above flat, its centre on an arc of HOOD_BEND. The "shot_exit" reserve is the
# ball's path from there (grown by SHOT_CLEAR), swept all the way round the turret: nothing may be in it. Until 11 Oct the
# hood turned shots out flat at 10 to 11.5 in, and one aimed within about 75 deg of ahead went through the hubs.
DECK_Z, DECK_T = FRONT_CROSS_Z + CH, 0.24  # the hub deck, flat on the front cross channel's top
HUB_GAP = 0.6                           # round each hub's edges, for its plugs (they go in from above, at its edges) and their bends
ROOF_Z = DECK_Z + DECK_T + HUB[2] + HUB_GAP   # the roofs' undersides
ROOF_T = 0.12
SHOT_CLEAR = 0.25
LAUNCH_DEG = 45.0                       # the shallowest shot the hood gives (the launcher rig sets it: if it's shallower, the check says so)
HOOD_BEND = 3.0                         # the ball's centre's bend radius in the hood
HOOD_BEND_Z = HOOD_Z + 0.2              # where it starts to turn (its centre), just over the turret's top
HOOD_WALL, HOOD_PLAY = 0.12, 0.10       # the hood's wall; the ball's clearance in it
def hood_path(f):
    """The ball's centre in the hood, f radians round the bend from straight up: (out from the column's axis, z)."""
    return (HOOD_BEND * (1 - math.cos(f)), HOOD_BEND_Z + HOOD_BEND * math.sin(f))
HOOD_TURN = math.radians(90 - LAUNCH_DEG)
HOOD_EXIT = hood_path(HOOD_TURN)
def _hood_tube(rad):
    P = lambda f: cq.Vector(COL_X + hood_path(f)[0], 0, hood_path(f)[1])
    w = cq.Wire.assembleEdges([cq.Edge.makeLine(cq.Vector(COL_X, 0, HOOD_Z), cq.Vector(COL_X, 0, HOOD_BEND_Z)),
                               cq.Edge.makeThreePointArc(P(0), P(HOOD_TURN / 2), P(HOOD_TURN))])
    return cq.Workplane("XY").workplane(offset=HOOD_Z).center(COL_X, 0).circle(rad).sweep(cq.Workplane().add(w), transition="round")
hood = _hood_tube(RN + HOOD_PLAY + HOOD_WALL).cut(_hood_tube(RN + HOOD_PLAY))
hood = hood.union(cyl("z", (COL_X, 0), HOOD_FLANGE_D, HOOD_Z, HOOD_Z + 0.2).cut(cyl("z", (COL_X, 0), 2 * (RN + HOOD_PLAY), HOOD_Z - 0.1, HOOD_Z + 0.3)))
part("launcher", "hood", hood, "print",
     f"PETG: the hood, a curved elbow on the turret's top: it turns the shot {LAUNCH_DEG:.0f} deg up from flat on a {HOOD_BEND:.0f} in bend "
     "(a stand-in: the launcher rig sets its shape; any shape whose shot clears the shot_exit check will do)")
HOOD_TOP = bb(PARTS["hood"]["shape"])[5]

# ---- electronics (v3.1, 11 Oct): the battery where it lifts straight out between matches; the hubs flat on a deck over the
# front, their plugs at their edges; the Limelight on the deck's front tongue. (Until v3.1 the battery sat low between the
# rails, boxed in by the rear cross channel, the rear drive motors and the launcher: it couldn't come out.)
RACK_SCREW_X = RAIL_X[0] + 6 * MM       # the rear cross channel's top flange, mid-flange (CHECK, as the rails' flanges)
RACK_SCREW_Y = (8 * MM, 16 * MM)        # between the rear motors' inner ends (|Y| 0.76)
RACK_Z = RAIL_Z[1] + 72 * MM            # over the rear drive motors (their tops at 5.53), on 72 mm of standoffs
RACK_STANDOFF = (RAIL_Z[1], RACK_Z)     # goBILDA 1501 M4 standoffs, 72 mm
BAT_X = (RAIL_X[0] + 0.02, RAIL_X[0] + 0.02 + BATTERY[2])   # standing on edge across the robot, on the shelf over the rear drive
BAT_Z = (RACK_Z + 0.2, RACK_Z + 0.2 + BATTERY[1])           # motors; nothing over it, so it lifts straight out (a strap holds it)
for s_, n in SIDES:
    sh = box(RACK_SCREW_X - 0.2, BAT_X[1] + 0.02, 0, s_ * (BATTERY[0] / 2 + 0.2), RACK_Z, RACK_Z + 0.2)
    sh = sh.union(box(RACK_SCREW_X - 0.2, BAT_X[1] + 0.02, s_ * (BATTERY[0] / 2 + 0.02), s_ * (BATTERY[0] / 2 + 0.2), RACK_Z, BAT_Z[0] + 0.8))   # an end cheek
    sh = sh.cut(box(-6.6, -6.2, s_ * 1.0, s_ * 1.8, RACK_Z - 0.1, RACK_Z + 0.3))       # a slot for the strap
    part("elec", f"rack_{n}", sh, "print",
         "PETG: half the battery's shelf over the rear drive motors, an end cheek each side; the battery stands on it, a hook-and-loop strap through the slots, and lifts straight out")
    for k, y in enumerate(RACK_SCREW_Y):
        part("elec", f"rack_standoff_{n}{k}", cyl("z", (RACK_SCREW_X, s_ * y), 6 * MM, *RACK_STANDOFF), "buy", "goBILDA 1501-0006-0480 and 1501-0006-0240 M4 standoffs (48 + 24 mm) on an M4 set screw: 72 mm (goBILDA's 1501s stop at 56 mm)")
_bat = box(*BAT_X, -BATTERY[0] / 2, BATTERY[0] / 2, *BAT_Z)
part("elec", "battery", cq.Workplane().add(solid(_bat).fillet(4 * MM, solid(_bat).Edges())), "buy",
     "Modern Robotics 12 V NiMH battery (the mentor's; CHECK its size), standing on edge on the shelf at the back: unstrap, lift out")

# The hub deck (11 Oct, v3.2): two PETG halves flat on the front cross channel's top, the hubs lying on them either side of
# the Limelight, each under a printed roof on four posts. Every edge of a hub has HUB_GAP clear for its plugs (the
# "plugs_" reserves); the roof keeps falling pieces and other robots' arms off; four screws take it off. Clear of the
# turret's drive and encoders behind (X < 2.46) and the extractor in front (X > 8.78); the shot leaves over the roofs.
HUB_X = (3.10, 3.10 + HUB[1])           # 103 mm along X: from just ahead of the turret's encoder tower (its plugs' gap 0.64)
HUB_Y = (2.05, 2.05 + HUB[0])           # 143 mm across, |Y|: the Limelight between the two hubs' plug gaps
POST = 0.36                             # the roofs' posts (part of the deck), at each hub's corners, clear of its edges' plugs
POST_X = (HUB_X[0] - 0.04 - POST, HUB_X[1] + 0.04)
POST_Y = (HUB_Y[0] - 0.03 - POST, HUB_Y[1] + 0.03)
ROOF_X = (HUB_X[0] - HUB_GAP, HUB_X[1] + HUB_GAP)
ROOF_Y = (HUB_Y[0] - HUB_GAP, HUB_Y[1] + HUB_GAP)
DECK_X = (2.60, POST_X[1] + POST + 0.05)
FC_HOLE_X = FRONT_CROSS_X + 40 * MM     # the front cross channel's web: goBILDA's model has 4 mm holes 40 mm across, every 8 mm along
FC_HOLE_Y = (56 * MM, 96 * MM)          # (fitted before the hubs go on)
DECK_IN = 1.15                          # each half's inner edge: the Limelight's base between them (and each half fits a 180 mm bed)
DECK_END = (FC_HOLE_X, 112 * MM)        # the outboard screw: through the channel's top into its printed end block, which fills the channel's end
DECK_RIB_X = (3.45, 5.15)               # the outboard ribs under each half, past the end block (|Y| > 5.45)
for s_, n in SIDES:
    dk = box(*DECK_X, s_ * DECK_IN, s_ * (POST_Y[1] + POST + 0.05), DECK_Z, DECK_Z + DECK_T)
    for x in DECK_RIB_X: dk = dk.union(box(x - 0.12, x + 0.12, s_ * 5.45, s_ * (POST_Y[1] + POST), DECK_Z - 0.4, DECK_Z))
    for px in POST_X:
        for py in POST_Y: dk = dk.union(box(px, px + POST, s_ * py, s_ * (py + POST), DECK_Z, ROOF_Z))
    part("elec", f"hub_deck_{n}", dk, "print",
         "PETG (not CF: no carbon under the hub), 6 mm: half the hub deck, flat on the front cross channel's top (2 M4 into it, 1 into its end block: all fitted before the hub), "
         "ribbed under its outboard end; its hub screwed flat on it, four posts for the hub's roof")
    part("elec", f"hub_roof_{n}", box(*ROOF_X, s_ * ROOF_Y[0], s_ * ROOF_Y[1], ROOF_Z, ROOF_Z + ROOF_T), "print",
         "PETG, 3 mm: the roof over a hub, on the deck's four posts (4 M4): falling pieces and other robots' arms land on it; the plugs and their wires go in under its edges")
for s_, n, nm, sku in ((1, "L", "control_hub", "REV-31-1595"), (-1, "R", "expansion_hub", "REV-31-1153")):
    y0, y1 = sorted((s_ * HUB_Y[0], s_ * HUB_Y[1]))
    part("elec", nm, dress(box(*HUB_X, y0, y1, DECK_Z + DECK_T, DECK_Z + DECK_T + HUB[2]), sku,
         ((HUB_X[0] + HUB_X[1]) / 2, (y0 + y1) / 2, DECK_Z + DECK_T), (0, 1, 0), (-1, 0, 0)), "buy",
         f"REV {'Control' if s_ > 0 else 'Expansion'} Hub, flat on the deck's {'left' if s_ > 0 else 'right'} half under its roof, {HUB_GAP} in clear round every edge for its plugs")
part("elec", "ground_strap", cyl("y", (HUB_X[0] - 0.3, DECK_Z + DECK_T + 0.18), 0.35, 2.4, 3.9), "buy",
     "REV-31-1269 Resistive Grounding Strap: the control hub's spare XT30 to a frame screw (REV's ESD advice), cable-tied to the deck behind the hub")
LL = (48.11 * MM, 72.11 * MM, 16.8 * MM)  # Limelight 3A: tall, wide, thick (its docs' drawing)
LL_Z, LL_XD = (math.sqrt(0.5), 0, math.sqrt(0.5)), (-math.sqrt(0.5), 0, math.sqrt(0.5))   # the way it looks (45 deg up); up its face
LL_BASE = DECK_Z + 0.35                 # the Limelight's base: thick enough for its screws' inserts
LL_X0 = FRONT_CROSS_X + 0.08            # between the hubs, over the front cross channel: its top under the shot
LL_O = (LL_X0 + LL[0] / 2 * math.sqrt(0.5), 0.0, LL_BASE + LL[0] / 2 * math.sqrt(0.5))   # its back face's middle, its lower edge on the tongue
def _ll(pts): return [(LL_O[0] + a * LL_XD[0] + b * LL_Z[0], LL_O[2] + a * LL_XD[2] + b * LL_Z[2]) for a, b in pts]
_lo, _hi = _ll([(-LL[0] / 2, 0)])[0], _ll([(LL[0] / 2, 0)])[0]
part("elec", "limelight_mount", slab_y([_lo, _hi, (_hi[0], LL_BASE)], -0.75, 0.75).union(box(FRONT_CROSS_X + 0.02, FRONT_CROSS_X + CH - 0.02, -(DECK_IN - 0.1), DECK_IN - 0.1, DECK_Z, LL_BASE)), "print",
     "PETG-CF: the Limelight's 45 deg wedge on a base on the front cross channel's top, between the hub deck's halves (2 M4 up through the channel's top into its inserts), the camera on its face (2 M4); its pose goes in CameraMount")
ll = cq.Workplane("XY").box(LL[0], LL[1], LL[2]).edges("|Z").fillet(4 * MM).translate((0, 0, LL[2] / 2)).union(
     cq.Workplane("XY").circle(6 * MM).extrude(3 * MM).translate((0, 0, LL[2])))
part("elec", "limelight", cq.Workplane().add(solid(ll).moved(cq.Location(cq.Plane(origin=LL_O, xDir=LL_XD, normal=LL_Z)))), "buy",
     "Limelight 3A (72 x 48 x 17 mm), looking 45 deg up from between the hubs; its pose goes in CameraMount")


# ================================================================ fasteners and joints (v2: every part shows what holds it)
# Every screw is drawn and checked: it passes through each part it clamps (a printed part gets its hole cut; a goBILDA
# part of the mentor's must show a real hole there; the rails' holes are on goBILDA's 8 mm grid), its threads end in an
# insert, a nut, a standoff or a tapped hole with enough engagement, and its head, the hex key behind it and any nut hit
# nothing. Parts held another way (a shaft in its bearings, a servo on its own screws) are listed in CAPTIVE and DEVICE.
# Then every part must reach the rails through these joints.
SCREW = {   # d, clearance, head d, head h, insert hole, insert length, nut h, nut d (mm); socket-head cap screws, nylocs, Ruthex-type inserts
    "M3": (3.0, 3.4, 5.5, 3.0, 4.0, 5.7, 4.0, 5.5),       # (a nut's across-flats: turned flat to a wall)
    "M4": (4.0, 4.4, 7.0, 4.0, 5.6, 8.1, 5.0, 7.0),
    "M5": (5.0, 5.5, 8.5, 5.0, 6.4, 9.5, 5.0, 8.0),
}
FASTENERS = []
LIFTS_OUT = {"battery"}     # comes out every match, so it never blocks a screw's head or its key
def screw(name, a, b, size, length, p, v, end, via=(), cb=False, what="", match=None, before=()):
    """A screw: its head seated on part a at p, pointing along v, through via, threading into b ('insert', 'nut', 'tapped').
    before: parts fitted after it (it's turned before they go on, so they never block its head or key)."""
    n = math.sqrt(sum(c * c for c in v)); v = tuple(c / n for c in v)
    FASTENERS.append(dict(name=name, a=a, b=b, size=size, L=length, p=tuple(p), v=v, end=end, via=tuple(via), cb=cb, what=what, match=match, before=tuple(before)))

CAPTIVE = []        # (part, held by, how)
DEVICE = []         # (part, mounted on, its own fasteners: text, count of screws)
def captive(a, b, how): CAPTIVE.append((a, b, how))
def device(a, b, how, count): DEVICE.append((a, b, how, count))

def _cyl(p, v, r, t0, t1):
    P = cq.Vector(*[p[i] + v[i] * t0 for i in range(3)])
    return cq.Workplane().add(cq.Solid.makeCylinder(r, t1 - t0, P, cq.Vector(*v)))

def screw_solids(f):
    d, _, hd, hh, *_ = SCREW[f["size"]]
    return _cyl(f["p"], f["v"], d / 2 * MM, 0, f["L"] * MM).union(_cyl(f["p"], f["v"], hd / 2 * MM, -hh * MM, 0))

# ---- the joints
for s, n in SIDES:
    m = (1, s, 1)
    def P(x, y, z): return (x, s * y, z)
    def V(x, y, z): return (x, s * y, z)
    # flap root -> rail web: from inside the rail, into the root's inserts
    for x, z in ((rail_x(42), WHEEL_R + 8 * MM), (rail_x(43), WHEEL_R - 8 * MM)):
        screw(f"flap_{n}_{x:.2f}", f"rail_{n}", f"flap_{n}", "M4", 12, P(x, RAIL_OUT - CH_T, z), V(0, 1, 0), "insert", what="the flap's root to the rail's web")
    # the swappable face -> flap: three countersunk M3 into the flap's inserts
    root, tip = (FACE, HALF_W), FLAP_TIP
    ang = math.atan2(tip[1] - root[1], tip[0] - root[0]); L = math.hypot(tip[0] - root[0], tip[1] - root[1])
    nrm = (math.sin(ang), -math.cos(ang))                  # into the flap from its inner face (left side)
    for k, f in enumerate((0.42, 0.66, 0.9)):
        x0, y0 = root[0] + f * L * math.cos(ang), root[1] + f * L * math.sin(ang)
        px, py = x0 + nrm[0] * 0.33, y0 + nrm[1] * 0.33     # the countersunk head's seat, 3 mm into the 5 mm face plate
        screw(f"face_{n}{k}", f"flap_face_{n}", f"flap_{n}", "M3", 8, P(px + 0.0, py, 2.25), V(-nrm[0], -nrm[1], 0), "insert", cb=True,
              what="the swappable face to its flap (countersunk, flush on the face)")
    # odometry adapters -> right rail web; the pods screw through both
    if s < 0:
        for z in (WHEEL_R + 16 * MM, WHEEL_R - 16 * MM):
            screw(f"odo_F{z:.2f}", "rail_R", "odo_pod_forward", "M4", 25, (rail_x(ODO_K["F"]), -RAIL_OUT, z), (0, 1, 0), "tapped", via=("odo_adapter_F",),
                  what="the forward odometry pod, through its adapter and the rail's web (from outside) into its side face")
        for z in (WHEEL_R + 16 * MM, WHEEL_R - 16 * MM):
            screw(f"odo_S{z:.2f}", "rail_R", "odo_pod_strafe", "M4", 16, (rail_x(ODO_K["S"]), -(RAIL_OUT - CH_T), z), (0, -1, 0), "tapped", via=("odo_spacer_S",),
                  what="the strafe odometry pod: from inside the rail, through the web and its spacer into its back plate")
    # lane walls -> standoffs -> rail web
    for h, sts in wall_standoffs(n).items():
        for k, (x, row) in enumerate(sts):
            z = WS_Z[row]
            screw(f"wall_{n}_{h}{k}", f"lane_wall_{n}_{h}", f"wall_standoff_{n}_{h}{k}", "M4", 10, P(x, WALL_IN + 4 * MM, z), V(0, 1, 0), "tapped", cb=True,
                  what="a lane wall to its standoff (counterbored: the head below the lane's face)")
            screw(f"wall_rail_{n}_{h}{k}", f"rail_{n}", f"wall_standoff_{n}_{h}{k}", "M4", 10, P(x, RAIL_OUT, z), V(0, -1, 0), "tapped",
                  what="the standoff to the rail's web (from outside)")
    # the mouth -> the rail's bottom flange: from above, inside the rail, into a nut under it
    for x in MOUTH_TABS:
        screw(f"mouth_{n}_{x:.2f}", f"mouth_floor_{n}", f"rail_{n}", "M4", 16, P(x, FLANGE_HOLE_Y, LANE_BOTTOM), V(0, 0, -1), "nut",
              what="the mouth to the rail's bottom flange")
    # front bracket -> the rail's top flange
    for x in (rail_x(31), rail_x(32)):
        screw(f"bracket_{n}_{x:.2f}", f"front_bracket_{n}", f"rail_{n}", "M4", 16, P(x, FLANGE_HOLE_Y, RAIL_Z[1] + 0.2), V(0, 0, -1), "nut",
              what="the front bracket's foot to the rail's top flange")
    # intake arm pivot: a shoulder screw through the arm into the bracket's boss
    screw(f"arm_pivot_{n}", f"front_bracket_{n}", f"intake_arm_{n}", "M5", 18, P(PIVOT[0], 4.6, PIVOT[1]), V(0, 1, 0), "insert",
          what="the intake arm's pivot: a shoulder screw from inside the bracket, turning in a flanged bushing in its boss, into the arm's insert (the drive motor's plate is just outboard of the arm)")
    # ceiling posts -> walls (M3 from inside the lane, counterbored)
    for k, x in enumerate(CEIL_POSTS):
        wall = f"lane_wall_{n}_{'rear' if x < WALL_SPLIT else 'front'}"
        for z in (1.4, 2.2):
            screw(f"post_{n}{k}_{z}", wall, f"ceiling_post_{n}{k}", "M3", 12, P(x, WALL_IN + 3 * MM, z), V(0, 1, 0), "insert", cb=True,
                  what="a ceiling post to the wall (counterbored in the wall)")
        captive("ceiling", f"ceiling_post_{n}{k}", "hinged on a pin (an M4 shoulder screw) through its ear and the post")
    for k, y in enumerate(RACK_SCREW_Y):
        screw(f"rack_{n}{k}", f"rack_{n}", f"rack_standoff_{n}{k}", "M4", 12, P(RACK_SCREW_X, y, RACK_Z + 0.2), V(0, 0, -1), "tapped",
              what="the electronics rack to its standoff")
        screw(f"rack_standoff_{n}{k}", "rear_cross", f"rack_standoff_{n}{k}", "M4", 10, P(RACK_SCREW_X, y, RAIL_Z[1] - CH_T), (0, 0, 1), "tapped",
              what="the rack's standoff to the rear cross channel's top flange (from inside the channel)")
    # launcher feet: up from inside the rail; down from inside the launcher's side channel
    if USE_LM:
        lmch = "lm_Hole_Lowside_U-Channel_GB_6__1" if s > 0 else "lm_Hole_Lowside_U-Channel_GB_4__1"
        for k, x in enumerate(LM_FEET):
            screw(f"foot_rail_{n}{k}", f"launcher_foot_{n}{k}", f"rail_{n}", "M4", 20, P(x, FLANGE_HOLE_Y, FOOT_SEAT), V(0, 0, -1), "nut", cb=True,
                  what="a launcher foot to the rail's top flange")
            xl = LM_HOLE_X0 + round((x - LM_HOLE_X0) / (8 * MM)) * 8 * MM
            screw(f"foot_lm_{n}{k}", lmch, f"launcher_foot_{n}{k}", "M4", 12, P(xl, LM_HOLE_Y, LM_CH_Z + CH_T), V(0, 0, -1), "insert",
                  what="the launcher's side channel to its foot", match="his channel's flange holes don't read cleanly from his model: mark the foot's insert from the real channel")
    if USE_LM:
        for x in LT_TABS:
            screw(f"tray_{n}_{x:.2f}", f"launcher_tray_{n}", f"rail_{n}", "M4", 12, P(x, FLANGE_HOLE_Y, LT_Z[1]), V(0, 0, -1), "nut",
                  what="the launcher tray to the rail's bottom flange")
        for yy in (2.6, 2.95):
            screw(f"feeder_block_{n}_{yy}", f"feeder_rear_block_{n}", f"launcher_tray_{n}", "M4", 20, P(-4.29, yy, 1.55), V(0, 0, -1), "nut", cb=True,
                  what="the feeder arm's rear block to the tray")
        for z in (0.95, 1.42):
            screw(f"feeder_front_{n}_{z}", f"feeder_front_block_{n}", f"lane_wall_{n}_rear", "M3", 12, P(-1.15, WALL_IN + WALL_T / 2, z), V(1, 0, 0), "insert",
                  what="the feeder arm's front block to the lane wall's end")
        for y in (0.75, 2.2):
            screw(f"anchor_{n}_{y}", f"fly_anchor_{n}", "lm_Hole_Lowside_U-Channel_GB_2__1", "M4", 16, P(-4.43, y, 7.95), V(-1, 0, 0), "nut",
                  what="the flywheel spring's anchor to the rear top channel", match="through the channel's front face: mark from the real channel")
    # the extractor, the stars, the intake and the lane's shafts: held by bearings and shafts
    captive(f"ex_stub_{n}", f"flap_{n}", "turns in a goBILDA 1611 flanged bearing in the flap")
    captive(f"ex_arm_{n}", f"ex_stub_{n}", "clamped on the stub (REX bore, a set screw)")
    captive("ex_cross", f"ex_arm_{n}", "through both arms, M4 screws into its ends")
    captive(f"star_shaft_{n}", f"star_bearing_{n}", "in it, the clutch in its hub above the second support")
    captive(f"star_clutch_{n}", f"star_shaft_{n}", "the one-way clutch pressed in its bore, on the round shaft")
    captive(f"star_{n}", f"star_shaft_{n}", "on the shaft's lower end: TO SETTLE with the mentor's star in hand (its 7 mm hex bore can't take the 8 mm round shaft; DECISIONS, open)")
    captive(f"star_bearing_{n}", f"front_bracket_{n}", "pressed into the arm from below")
    device(f"star_servo_{n}", f"front_bracket_{n}", "M4 x 12 through the servo's tabs into the bracket's inserts", 4)
    captive("roller_shaft", f"intake_arm_{n}", "in a goBILDA 1611 flanged bearing in each arm, e-clips")
if not USE_DM:
    for s, n in SIDES:
        for fx in ("F", "B"):
            nm = fx + n
            device(f"motor_plate_{nm}", f"rail_{n}", "M4 x 10 through the plate and the rail's web, nylocs inside (goBILDA grid)", 4)
            device(f"drive_motor_{nm}", f"motor_plate_{nm}", "M4 x 8 into the motor's face (goBILDA pattern)", 4)
            captive(f"wheel_shaft_{nm}", f"rail_{n}", "in a goBILDA 1611 bearing in the rail's web, and one in the outer plate")
            captive(f"wheel_{nm}", f"wheel_shaft_{nm}", "on the REX shaft, a hub")
            captive(f"drive_belt_{nm}", f"wheel_shaft_{nm}", "on the 24T pulleys")
        device(f"outer_plate_{n}", f"standoff_{n}_1_hi", "M4 x 8 into the standoffs", 4)
        for k in (-3, 1):
            for h in ("hi", "lo"):
                device(f"standoff_{n}_{k}_{h}", f"standoff_washers_{n}_{k}_{h}", "M4 x 12 through the rail's web and the washers into it", 1)
                captive(f"standoff_washers_{n}_{k}_{h}", f"rail_{n}", "on the standoff's screw")
                device(f"outer_plate_{n}", f"standoff_{n}_{k}_{h}", "", 0)
    for s_, n in SIDES:
        for x, z, v in ((FRONT_CROSS_X, FRONT_CROSS_Z + 16 * MM, (1, 0, 0)), (FRONT_CROSS_X + CH, FRONT_CROSS_Z + 32 * MM, (-1, 0, 0))):
            screw(f"fc_end_{n}_{z:.2f}", "front_cross", f"front_cross_end_{n}", "M4", 12, (x, s_ * FC_END_Y, z), v, "insert",
                  what="the front cross channel's side wall into its end block")
        for dx in (-16 * MM, 16 * MM):
            screw(f"fc_plate_{n}_{dx:+.2f}", f"motor_plate_F{n}", f"front_cross_end_{n}", "M4", 12, (AXLE[1] + dx, s_ * (RAIL_OUT + PLATE_T), RAIL_Z[0] + 176 * MM),
                  (0, -s_, 0), "insert", what="the front motor plate into the cross channel's end block (its holes 16 mm off its middle, 176 mm up)")
        screw(f"rc_top_{n}", "rear_cross", f"rear_corner_{n}", "M4", 12, (REAR_CROSS_HOLE[0], s_ * REAR_CROSS_HOLE[1], RAIL_Z[1]), (0, 0, -1), "insert",
              what="the rear cross channel's top flange into its corner block")
        screw(f"rc_rail_top_{n}", f"rail_{n}", f"rear_corner_{n}", "M4", 12, (rail_x(0), s_ * FLANGE_HOLE_Y, RAIL_Z[1]), (0, 0, -1), "insert",
              what="the rail's top flange into the rear corner block")
        screw(f"rc_rail_bot_{n}", f"rail_{n}", f"rear_corner_{n}", "M4", 12, (rail_x(0), s_ * FLANGE_HOLE_Y, RAIL_Z[0]), (0, 0, 1), "insert",
              what="the rail's bottom flange into the rear corner block (from under the robot)")
captive("ex_block", "ex_cross", "clamped on the cross shaft")
for n in ("L", "R"):
    device(f"front_pad_{n}", f"flap_{n}", "3 countersunk M3 x 10 into the root's inserts", 3)
    device(f"ramp_skin_{n}", f"mouth_floor_{n}", "4 countersunk M3 x 8 into the ramp's inserts", 4)
if USE_LM:
    for n in ("L", "R"):
        captive(f"feeder_wheel_{n}", f"feeder_shaft_{n}", "on the shaft (the left's pinned, the right's free on bearings)")
        captive(f"feeder_shaft_{n}", f"feeder_arm_{n}", "in 1611 bearings in the arm's plates, e-clips")
        captive(f"feeder_arm_{n}", f"feeder_pivot_{n}", "swings on 1611 bearings on the pivot shaft")
        captive(f"feeder_pivot_{n}", f"feeder_rear_block_{n}", "in 1611 bearings in the rear and front blocks, e-clips")
        captive(f"feeder_pivot_{n}", f"feeder_front_block_{n}", "in a 1611 bearing")
    for n in ("L", "R"):
        captive(f"fly_block_{n}_front", f"lm_Hole_Lowside_U-Channel_GB_{11 if n == 'L' else 9}__1", "slides in the plate's slot, the shaft's bearing in it")
        captive(f"fly_yoke_{n}", f"lm_Hole_Lowside_U-Channel_GB_{12 if n == 'L' else 10}__1", "its block slides in the plate's slot, the shaft's bearing in it")
        device(f"fly_yoke_{n}", f"lm_flywheel_motor_bracket_{n}_1_8_in_aluminium_L_bent_on_the_laun_1", "M4 x 10 through the bracket's top into the yoke's inserts", 2)
        for f_, plate in (("front", 11 if n == "L" else 9), ("rear", 12 if n == "L" else 10)):
            device(f"fly_guide_{n}_{f_}", f"lm_Hole_Lowside_U-Channel_GB_{plate}__1", "2 M3 through the plate's web into the guide's inserts (mark the holes from the real plate)", 2)
        captive(f"fly_spring_{n}", f"fly_anchor_{n}", "hooked to the anchor's pin and the yoke's")
        captive(f"feeder_spring_{n}", f"feeder_rear_block_{n}", "seated in the lug's pocket and the arm's")
    device("feeder_motor", "feeder_rear_block_L", "M4 x 8 into the motor's face (goBILDA pattern)", 4)
    captive("feeder_belt_A", "feeder_pivot_L", "on the 24T pulleys"); captive("feeder_belt_B", "feeder_pivot_L", "on the 24T pulleys")
if USE_LM: device("turret_drive_plate", "lm_Hole_Lowside_U-Channel_GB_8__1", "M4 x 10 up through the plate into nylocs in the launcher's front top channel", 4)
device("ex_servo", "ex_servo_mount", "M4 x 12 through the servo's tabs into the mount's inserts", 4)
for k, (p_, v_) in enumerate(EX_MOUNT_SCREWS):
    screw(f"ex_mount_{k}", "ex_servo_mount", "flap_R", "M3", 10, p_, v_, "insert", what="the extractor servo's mount: through its cheek into the right flap's tall top (an insert in the flap)")
device("ex_gear_servo", "ex_horn", "4 M3 into the servo's goBILDA horn", 4)
device("ex_horn", "ex_servo", "on the servo's spline, its M3 centre screw", 1)
captive("ex_gear_stub", "ex_stub_R", "clamped on the stub, a set screw")
captive("roller", "roller_shaft", "on the REX shaft")
captive("roller_belt", "roller_shaft", "on the 24T pulleys")
device("intake_motor", "intake_motor_mount", "M4 x 8 through the mount's plate into the motor's face (goBILDA's 16 mm square)", 4)
device("intake_motor_mount", "intake_arm_R", "M4 x 12 through the arm into the mount's inserts", 2)
captive("intake_tie", "intake_arm_L", "an M4 x 10 through the arm into the standoff's end")
captive("intake_cradle", "intake_tie", "the standoff in the cradle's REX socket, a set screw")
captive("intake_motor", "intake_cradle", "clamped round its can by the cradle's M3 pinch screw")
for k, x in enumerate(LANE_SHAFTS):
    wall = "lane_wall_{}_front" if x > WALL_SPLIT else "lane_wall_{}_rear"
    for n in ("L", "R"):
        captive(f"lane_shaft_{k}", wall.format(n) if "{" in wall else f"{wall}_{n}", "in a goBILDA 1611 flanged bearing, e-clips")
    captive(f"lane_roller_{k}", f"lane_shaft_{k}", "free on two 8x14x5 bearings on the shaft")
for k in range(4): captive(f"ceiling_roller_{k}", "ceiling", "on two 608 bearings and an 8 mm shaft through the ceiling's sides")
captive("battery", "rack_L", "a hook-and-loop strap round it and through both halves' slots")
device("control_hub", "hub_deck_L", "M3 x 8 up through the deck into its four mounting holes", 4)
device("expansion_hub", "hub_deck_R", "M3 x 8 up through the deck into its four mounting holes", 4)
device("limelight", "limelight_mount", "M4 into its mounting holes, through the wedge's face", 2)
captive("ground_strap", "hub_deck_L", "two cable ties through the deck; its ring terminal under a frame screw")
for s_, n in SIDES:
    hub = "control_hub" if s_ > 0 else "expansion_hub"
    for y in FC_HOLE_Y:
        screw(f"deck_{n}_{y:.2f}", f"hub_deck_{n}", "front_cross", "M4", 16, (FC_HOLE_X, s_ * y, DECK_Z + DECK_T - 4 * MM), (0, 0, -1), "nut", cb=True,
              before=(f"{hub}",), what="the hub deck to the front cross channel's top (its outer hole row), a nyloc inside the channel: before the hub goes on")
    screw(f"deck_end_{n}", f"hub_deck_{n}", f"front_cross_end_{n}", "M4", 20, (DECK_END[0], s_ * DECK_END[1], DECK_Z + DECK_T - 4 * MM), (0, 0, -1), "insert", via=("front_cross",), cb=True,
          before=(f"{hub}",), what="the hub deck through the front cross channel's top into its end block (an insert): before the hub goes on")
    for i, px in enumerate(POST_X):
        for j, py in enumerate(POST_Y):
            screw(f"roof_{n}{i}{j}", f"hub_roof_{n}", f"hub_deck_{n}", "M4", 12, (px + POST / 2, s_ * (py + POST / 2), ROOF_Z + ROOF_T), (0, 0, -1), "insert",
                  what="the hub's roof down onto a post of the deck (an insert in the post's top): four out and the roof lifts off")
for y in (-0.4, 0.4):
    screw(f"ll_mount_{y}", "front_cross", "limelight_mount", "M4", 10, (FC_HOLE_X, y, DECK_Z - CH_T), (0, 0, 1), "insert",
          what="up through the front cross channel's top into the Limelight base's inserts (from inside the channel)")
for k in range(4):
    a = math.radians(90 * k)
    screw(f"hood_{k}", "hood", TURRET_TOP if USE_LM else "turret_kit", "M4", 12, (COL_X + 64 * MM * math.cos(a), 64 * MM * math.sin(a), HOOD_Z + 0.2), (0, 0, -1), "tapped",
          what="the hood's flange to the turret's 176T gear (its 4 mm holes on a 64 mm radius, at 0, 90, 180 and 270 deg from the drive gear)",
          match="confirm the 64 mm-radius holes on the part, and whether they go on into the race (the screws' length); the gear's free 56 mm-radius holes are the fallback")

# ================================================================ checks


# parts that touch by design (a shaft in its part, a belt round its motor's shaft, a stack)
ALLOWED = [("wheel_shaft_", "wheel_"), ("wheel_shaft_", "rail_"), ("wheel_shaft_", "motor_plate_"), ("wheel_shaft_", "outer_plate_"),
           ("drive_belt_", "drive_motor_"), ("drive_belt_", "wheel_shaft_"), ("drive_motor_", "motor_plate_"), ("motor_plate_", "rail_"),
           ("standoff_", "outer_plate_"), ("standoff_", "motor_plate_"), ("standoff_", "rail_"), ("outer_plate_", "flap_"), ("intake_pivot_", "outer_plate_"),
           ("roller_shaft", "roller"), ("roller_shaft", "intake_arm_"), ("roller_belt", "intake_motor"), ("intake_motor", "intake_motor_mount"), ("intake_motor_mount", "intake_arm_R"), ("intake_tie", "intake_arm_L"), ("intake_tie", "intake_cradle"), ("intake_cradle", "intake_motor"), ("roller_belt", "intake_motor_mount"), ("roller_belt", "roller_shaft"),
           ("lane_shaft_", "lane_roller_"), ("lane_shaft_", "lane_wall_"), ("fly_shaft_", "flywheel_"), ("fly_shaft_", "fly_cassette_"),
           ("fly_belt_", "fly_shaft_"), ("fly_belt_", "fly_motor_"), ("feeder_shaft", "feeder"), ("feeder_belt", "feeder_shaft"),
           ("feeder_belt", "feeder_motor"), ("ex_stub_", "flap_"), ("ex_stub_", "ex_arm_"), ("ex_cross", "ex_arm_"), ("ex_cross", "ex_block"),
           ("turret_kit", "top_plate_"), ("top_plate_", "top_plate_"), ("tray_", "tray_"), ("lane_wall_", "lane_wall_"), ("flap_face_", "flap_"), ("front_pad_", "flap_"), ("front_pad_", "flap_face_"), ("ramp_skin_", "mouth_floor_"), ("ramp_skin_", "ramp_skin_"),
           ("intake_motor", "intake_arm_R"), ("ex_gear_", "ex_gear_"), ("ex_gear_stub", "ex_stub_R"), ("ex_gear_servo", "ex_servo"), ("rear_cross", "rail_"), ("front_cross", "motor_plate_F"), ("front_cross_end_", "front_cross"), ("front_cross_end_", "motor_plate_F"), ("rear_corner_", "rear_cross"), ("rear_corner_", "rail_"), ("standoff_washers_", "rail_"), ("standoff_washers_", "standoff_"), ("ex_horn", "ex_servo"), ("ex_horn", "ex_gear_servo"),
           ("star_shaft_", "star_"), ("star_bearing_", "star_shaft_"), ("star_bearing_", "front_bracket_"), ("star_clutch_", "star_"), ("star_shaft_", "star_clutch_"), ("lane_shaft_", "mouth_floor_"), ("ramp_", "ramp_"), ("mouth_floor_", "mouth_floor_"), ("ramp_", "mouth_floor_"),
           ("star_bracket_", "front_cross"), ("star_bracket_", "star_servo_"), ("tray", "front_cross"), ("limelight", "limelight_mount"), ("limelight_mount", "hub_deck_"), ("limelight_mount", "front_cross"), ("hub_deck_", "hub_deck_"), ("hub_roof_", "hub_deck_"), ("ground_strap", "hub_deck_"), ("hub_deck_", "front_cross"), ("hub_deck_", "control_hub"), ("hub_deck_", "expansion_hub"), ("rack_", "battery"),
           ("dm_", "rail_"), ("feeder_shaft_", "feeder_wheel_"), ("feeder_shaft_", "feeder_arm_"), ("feeder_pivot_", "feeder_arm_"), ("feeder_pivot_", "feeder_rear_block_"),
           ("feeder_pivot_", "feeder_front_block_"), ("feeder_belt_B", "feeder_pivot_L"), ("feeder_belt_B", "feeder_shaft_L"), ("feeder_belt_A", "feeder_pivot_L"),
           ("feeder_belt_A", "feeder_motor"), ("feeder_motor", "feeder_rear_block_L"), ("feeder_front_block_", "lane_wall_"), ("feeder_rear_block_", "launcher_tray_"), ("feeder_post_", "launcher_tray_"), ("launcher_tray_", "rail_"), ("launcher_tray_", "lm_feeder_floor"), ("launcher_tray_", "lm_backstop"),
           ("feeder_arm_", "feeder_wheel_"), ("fly_block_", "lm_"), ("fly_guide_", "lm_"), ("fly_yoke_", "lm_flywheel_motor_bracket"), ("fly_yoke_", "fly_block_"), ("fly_yoke_", "lm_"),
           ("fly_anchor_", "lm_Hole_Lowside_U-Channel_GB_2__1"), ("fly_spring_", "fly_anchor_"), ("fly_spring_", "fly_yoke_"), ("fly_anchor_", "fly_yoke_"),
           ("feeder_spring_", "feeder_rear_block_"), ("feeder_spring_", "feeder_arm_"), ("feeder_arm_", "feeder_rear_block_"),   # through his plates' slots, with the modules ("front_bracket_", "dm_F"), ("front_bracket_", "star_servo_"), ("intake_arm_", "front_bracket_"),
           ("rack_", "dm_B"), ("rack_", "control_hub"), ("rack_", "expansion_hub"), ("rack_", "rack_"), 
           ("flap_", "rail_"),
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


# ================================================================ the space budget (layout v3)
# Volumes reserved before the parts that fill them are drawn: nothing else may enter one. A mechanism that needs a motor
# gets its motor's box here first, so a missing home shows up at layout time, not after the screws are done.
def _hood_sweep():
    b = bb(PARTS["hood"]["shape"])
    reach = max(math.hypot(x - COL_X, y) for x in b[:2] for y in b[2:4])
    return cyl("z", (COL_X, 0), 2 * reach, HOOD_BEND_Z, b[5] + 0.05)

def _shot_exit(reach=14.0):
    """The ball's path after the hood, grown by SHOT_CLEAR, revolved about the column's axis: (out, z) clipped at the axis."""
    t = math.radians(LAUNCH_DEG); rr = RN + SHOT_CLEAR; (u0, z0) = HOOD_EXIT
    L = (reach - u0) / math.cos(t); n = (-math.sin(t), math.cos(t)); d = (math.cos(t), math.sin(t))
    poly = [(u0 + n[0] * rr, z0 + n[1] * rr), (u0 - n[0] * rr, z0 - n[1] * rr)]
    poly += [(u0 + L * d[0] - n[0] * rr, z0 + L * d[1] - n[1] * rr), (u0 + L * d[0] + n[0] * rr, z0 + L * d[1] + n[1] * rr)]
    out, lim = [], 0.05                                         # clip to u >= lim (Sutherland-Hodgman, one edge)
    for i, (p, q) in enumerate(zip(poly, poly[1:] + poly[:1])):
        if p[0] >= lim: out.append(p)
        if (p[0] >= lim) != (q[0] >= lim):
            k = (lim - p[0]) / (q[0] - p[0]); out.append((lim, p[1] + k * (q[1] - p[1])))
    return cq.Workplane("XZ", origin=(COL_X, 0, 0)).polyline(out).close().revolve(360, (0, 0, 0), (0, 1, 0))

RESERVED = {
    "feeder_motor": (box(FEED_MOTOR_FACE, FEED_MOTOR_FACE + MOTOR_L1, FEED_MOTOR[0] - MOTOR_SQ / 2, FEED_MOTOR[0] + MOTOR_SQ / 2,
                         FEED_MOTOR[1] - MOTOR_SQ / 2, FEED_MOTOR[1] + MOTOR_SQ / 2), ("feeder_",), "the feeder's motor, fixed to the frame"),
    "feeder_drive": (box(*BELT_A_X, FEED_Y - 0.8, FEED_MOTOR[0] + 0.8, FEED_PIVOT_Z - 0.8, FEED_MOTOR[1] + 0.8), ("feeder_",),
                     "belt A, the motor to the left arm's pivot shaft, behind the launcher's back plates"),
    "turret_sweep": (_hood_sweep(), ("hood",), "the hood as the turret turns"),
    "shot_exit": (_shot_exit(), ("hood",), f"every shot leaving the hood ({LAUNCH_DEG:.0f} deg up), the ball grown by {SHOT_CLEAR} in, in any direction the turret faces, out to 14 in"),
    **{f"plugs_{n}": (box(HUB_X[0] - HUB_GAP, HUB_X[0], s_ * HUB_Y[0], s_ * HUB_Y[1], DECK_Z + DECK_T, ROOF_Z)
                      .union(box(HUB_X[1], HUB_X[1] + HUB_GAP, s_ * HUB_Y[0], s_ * HUB_Y[1], DECK_Z + DECK_T, ROOF_Z))
                      .union(box(*HUB_X, s_ * (HUB_Y[0] - HUB_GAP), s_ * HUB_Y[0], DECK_Z + DECK_T, ROOF_Z))
                      .union(box(*HUB_X, s_ * HUB_Y[1], s_ * (HUB_Y[1] + HUB_GAP), DECK_Z + DECK_T, ROOF_Z))
                      .union(box(*HUB_X, s_ * HUB_Y[0], s_ * HUB_Y[1], DECK_Z + DECK_T + HUB[2], ROOF_Z)), (hub, "ground_strap"),
                      f"round the {hub.replace('_', ' ')}'s edges and over it: its plugs and their wires")
       for s_, n, hub in ((1, "L", "control_hub"), (-1, "R", "expansion_hub"))},
}

def space_report():
    ok = True
    print("== the space budget: reserved volumes (layout v3)")
    for nm, (w, owners, what) in RESERVED.items():
        inn = [k for k, p in PARTS.items() if not k.startswith(owners) and overlap(w, p["shape"]) > 1e-4]
        print(f"   {nm} ({what}): " + ("clear" if not inn else "INTRUDED by " + ", ".join(inn[:8]) + (f" and {len(inn) - 8} more" if len(inn) > 8 else "")))
        ok &= not inn
    return ok

def joint_report():
    """The v2 checks: every screw, then every part's path to the rails."""
    ok = True
    shapes = {k: p["shape"] for k, p in PARTS.items()}
    holed = lambda k: k.startswith(("lm_", "dm_"))            # the mentor's goBILDA parts: their holes are in the model
    def trange(sol, f):                                         # where along the screw a solid sits (in)
        b = solid(sol).BoundingBox(); p, v = f["p"], f["v"]
        ts = [sum((c[i] - p[i]) * v[i] for i in range(3)) for c in ((x, y, z) for x in (b.xmin, b.xmax) for y in (b.ymin, b.ymax) for z in (b.zmin, b.zmax))]
        return min(ts), max(ts)
    def inside(sh, w):
        try:
            x = solid(sh).intersect(solid(w)); return x if x.Volume() > 1e-6 else None
        except Exception: return None
    print(f"== fasteners: {len(FASTENERS)} screws drawn and checked")
    bad = []
    for f in FASTENERS:
        d, clr, hd, hh, ih, il, nh, nd = SCREW[f["size"]]
        L = f["L"] * MM
        shank = _cyl(f["p"], f["v"], d / 2 * MM, 0, L)
        def fail(msg): bad.append(f"{f['name']}: {msg}")
        for k in (f["a"],) + f["via"] + (f["b"],):
            if k not in shapes: fail(f"no part {k}"); continue
        if any(k not in shapes for k in (f["a"],) + f["via"] + (f["b"],)): continue
        clamp = [f["a"]] + list(f["via"]) + ([f["b"]] if f["end"] == "nut" else [])
        for k in dict.fromkeys(clamp):
            w = shapes[k]
            if holed(k):
                if f["match"]: continue
                if inside(_cyl(f["p"], f["v"], (d / 2 - 0.1) * MM, 0, L), w) is not None: fail(f"hits {k}'s material: no hole there")
                elif inside(_cyl(f["p"], f["v"], hd / 2 * MM, 0, L), w) is None: fail(f"misses {k}")
            else:
                if inside(shank, w) is None: fail(f"misses {k}")
                if k.startswith("rail_"):                       # goBILDA's grid, 4 mm holes every 8 mm (the 14 mm ones every 24)
                    p, v = f["p"], f["v"]
                    st = (p[0] - RAIL_X[0]) / MM; gx = st / 8
                    if abs(gx - round(gx)) > 0.02 or not 8 <= round(st) <= 352: fail(f"off the rail's 8 mm hole stations along X ({p[0]:.3f})")
                    if abs(v[1]) > 0.9:
                        gz = (p[2] - WHEEL_R) / MM / 8
                        ok_rows = (-2, 2) if round(st) % 24 == 0 else (-2, -1, 1, 2)
                        if abs(gz - round(gz)) > 0.02 or round(gz) not in ok_rows: fail(f"off the rail web's 4 mm holes (z {p[2]:.3f} at station {round(st)} mm)")
                    elif abs(abs(p[1]) - FLANGE_HOLE_Y) > 0.02: fail(f"off the rail flange's hole row (Y {p[1]:.3f})")
        # where it threads
        b = shapes[f["b"]]
        if f["end"] in ("insert", "tapped") and not (f["match"] and holed(f["b"])):
            x = inside(shank, b); need = (0.8 * il if f["end"] == "insert" else 1.5 * d) * MM
            got = 0 if x is None else x.Volume() / (math.pi * (d / 2 * MM) ** 2)
            if got < need - 1e-3: fail(f"only {got / MM:.1f} mm of thread in {f['b']} (needs {need / MM:.1f})")
            if x is not None and trange(x, f)[1] > L - 1e-3 and f["end"] == "insert":
                pass
        others = {k: w for k, w in shapes.items() if k not in clamp and k != f["b"]}
        if f["end"] == "nut":
            def wall(k):                                     # through a part's own hole the shank meets nothing: the hole's wall
                for r in (d, 2 * d, 4 * d):
                    x = inside(_cyl(f["p"], f["v"], r * MM, 0, L), shapes[k])
                    if x is not None: return x
                return shapes[k]
            t_exit = max(trange(inside(shank, shapes[k]) or wall(k), f)[1] for k in clamp)
            if L - t_exit < nh * MM - 1e-3: fail(f"{(L - t_exit) / MM:.1f} mm past the last part: no room for the nut ({nh} mm)")
            nut = _cyl(f["p"], f["v"], nd / 2 * MM, t_exit + 0.005, t_exit + nh * MM)
            for k, w in shapes.items():
                if overlap(nut, w) > 1e-5: fail(f"its nut hits {k}")
        for k, w in others.items():
            if f["match"] and holed(k): continue
            if overlap(shank, w) > 1e-5: fail(f"runs into {k}")
        head = _cyl(f["p"], f["v"], hd / 2 * MM, -hh * MM, -0.002)
        key = _cyl(f["p"], f["v"], 2.0 * MM, -hh * MM - 0.75, -hh * MM)
        for k, w in shapes.items():
            if (f["cb"] or f["match"]) and k == f["a"]: continue
            if k in LIFTS_OUT or k in f["before"]: continue   # out of the way whenever a screw is turned
            if overlap(head, w) > 1e-5: fail(f"its head hits {k}")
            if overlap(key, w) > 1e-5: fail(f"no room for the hex key: {k}")
    for m in bad: print("   " + m)
    for f in FASTENERS:
        if f["match"]: print(f"   to locate on the robot: {f['name']} ({f['what']}): {f['match']}")
    print("   " + ("all pass: through real holes, enough thread, heads, keys and nuts clear" if not bad else f"{len(bad)} problems"))
    ok &= not bad

    print("== every part reaches the rails through screws, bearings or its own mounting")
    node = lambda k: "launcher module (the mentor's)" if k.startswith("lm_") else (f"drive corner {k[3:5]} (the mentor's)" if k.startswith("dm_") else k)
    edges = {}
    def link(a, b):
        a, b = node(a), node(b); edges.setdefault(a, set()).add(b); edges.setdefault(b, set()).add(a)
    for f in FASTENERS:
        for k in (f["a"],) + f["via"]: link(k, f["b"])
    for a, b, *_ in CAPTIVE + DEVICE: link(a, b)
    if USE_DM:
        for c in ("FL", "BL"): link(f"dm_{c}_", "rail_L")      # his corners bolt to the rails by their own plates
        for c in ("FR", "BR"): link(f"dm_{c}_", "rail_R")
        link("dm_BR_", "rail_L")                               # his 1107 ties the rails at the back
    for k, p in PARTS.items():
        if p["moves"] in ("fly_L", "fly_R"): link(k, "lm_")   # the sprung modules: on the launcher frame (their slides TBD, README)
    seen, todo = set(), ["rail_L"]
    while todo:
        k = todo.pop()
        if k in seen: continue
        seen.add(k); todo += list(edges.get(k, ()))
    loose = sorted({node(k) for k in PARTS} - seen)
    print("   " + ("all of them" if not loose else "LOOSE: " + ", ".join(loose)))
    ok &= not loose
    n = {}
    for f in FASTENERS: n[(f["size"], f["L"])] = n.get((f["size"], f["L"]), 0) + 1
    print("   screws: " + ", ".join(f"{c} x {sz} x {l}" for (sz, l), c in sorted(n.items())) + f"; plus {sum(c for *_, c in DEVICE)} on devices (servos, hubs, motor)")
    return ok

# Colours, for the STEP and the views: printed parts by their material (read from their description), bought parts with
# no model by what they are, the mentor's parts by their names
MATERIAL = (("TPU", (0.96, 0.70, 0.16)), ("PETG-CF", (0.22, 0.23, 0.26)), ("polycarbonate", (0.80, 0.88, 0.95)),
            ("aluminium", (0.80, 0.81, 0.83)), ("PETG", (0.16, 0.42, 0.85)))
NAMED = ((r"Gecko|belt|servo|motor|Encoder|enc_[AB]_REV|spring|limelight|hub|battery", (0.15, 0.15, 0.16)),
         (r"[Bb]earing|[Ss]haft|[Ss]pacer|eclip|Steel", (0.70, 0.71, 0.73)),
         (r"print|PETG", (0.16, 0.42, 0.85)), (r"polycarbonate", (0.80, 0.88, 0.95)))
def colour_of(k):
    p = PARTS[k]
    if p["kind"] == "print":
        return next((c for w, c in MATERIAL if w in p["what"]), MATERIAL[-1][1])
    if k.startswith(("flap_face", "front_pad", "ramp_skin")): return MATERIAL[0][1]
    return next((c for rx, c in NAMED if re.search(rx, k + " " + p["what"])), (0.80, 0.81, 0.83))   # else aluminium (channels, plates, blocks)

DRAWN_COLOUR = {"belt": (0.10, 0.10, 0.11), "rex": (0.70, 0.71, 0.73), "rex_standoff": (0.78, 0.79, 0.81)}
def look_parts(k):
    """A dressed part as [(shape in its own frame, cq.Location, colour)], inches; None when it has no dress or a model
    it needs isn't cached (vendor.py)."""
    import vendor
    L = PARTS[k].get("looks")
    if not L: return None
    out = []
    for what, o, z, x in L:
        loc = cq.Location(cq.Plane(origin=o, xDir=x, normal=z))
        if isinstance(what, str):
            m = vendor.model(what)
            if m is None: return None
            out += [(sh, loc, col) for sh, col in m]
        else:
            out.append((_drawn(what), loc, DRAWN_COLOUR[what[0]]))
    return out

def dress_report():
    """Each dressed part against its envelope: where the real part reaches past the box the checks use, and, there,
    what it hits."""
    import vendor
    print("== dress: the real parts (goBILDA's models, drawn belts and shafts) against their envelopes")
    ok, missing, n = True, [], 0
    for k, p in PARTS.items():
        if not p.get("looks"): continue
        lp = look_parts(k)
        if lp is None: missing.append(k); continue
        n += 1
        R = [1e9, -1e9] * 3
        for sh, loc, _ in lp:
            b = vendor.placed_box(sh, loc)
            for i in range(3): R[2 * i] = min(R[2 * i], b[2 * i]); R[2 * i + 1] = max(R[2 * i + 1], b[2 * i + 1])
        A = bb(p["shape"]); past = max(max(A[2 * i] - R[2 * i], R[2 * i + 1] - A[2 * i + 1]) for i in range(3))
        if past <= 0.02: continue
        real = [s.moved(loc) for s, loc, _ in lp]
        hits = []
        for q, pq in PARTS.items():
            if q == k or allowed(k, q) or (q.startswith(k.split("_")[0] + "_") and False): continue
            B = bb(pq["shape"])
            if any(R[2 * i] > B[2 * i + 1] or B[2 * i] > R[2 * i + 1] for i in range(3)): continue
            v = 0.0
            for r in real:
                for so in r.Solids():
                    sb = so.BoundingBox()
                    if sb.xmin > B[1] or B[0] > sb.xmax or sb.ymin > B[3] or B[2] > sb.ymax or sb.zmin > B[5] or B[4] > sb.zmax: continue
                    try: v += so.intersect(solid(pq["shape"])).Volume()
                    except Exception: v = -1
            if v > 1e-4 or v < 0: hits.append(f"{q} ({v:.4f} in^3)")
        print(f"   {k}: {past:.2f} in past its envelope" + ("; HITS " + ", ".join(hits) if hits else ", clear"))
        ok &= not hits
    print(f"   {n} parts dressed" + (f"; not cached, so drawn as envelopes: {len(missing)} (run vendor.py)" if missing else ""))
    return ok

def service_report():
    """What has to come off the robot between matches comes off without other parts moving: the battery lifts straight
    up out of its shelf, and each hub's roof straight up off its posts (four screws), both to the hood's height."""
    ok = True
    for k, what in (("battery", "the battery lifts straight out"), ("hub_roof_L", "the left hub's roof lifts off"), ("hub_roof_R", "the right hub's roof lifts off")):
        print(f"== service: {what}")
        b = bb(PARTS[k]["shape"]); path = box(b[0], b[1], b[2], b[3], b[5], HOOD_TOP)
        hit = [j for j, p in PARTS.items() if j != k and overlap(path, p["shape"]) > 1e-5]
        print("   clear, " + f"{HOOD_TOP - b[5]:.1f} in over it" if not hit else "   BLOCKED by " + ", ".join(hit))
        ok &= not hit
    return ok

LL_FOV = (54.5, 42.0)       # Limelight 3A's field of view, across and up (deg)
def view_report():
    """The Limelight sees out: nothing of the robot inside its field of view (to 30 in), the extractor stowed."""
    print("== the Limelight's view (54.5 x 42 deg, 45 deg up)")
    lens = tuple(LL_O[i] + (LL[2] + 0.02) * LL_Z[i] for i in range(3))
    d = 30.0
    pl = cq.Plane(origin=lens, xDir=(0, 1, 0), normal=LL_Z)
    cone = cq.Workplane(pl).rect(0.02, 0.02).workplane(offset=d).rect(2 * d * math.tan(math.radians(LL_FOV[0] / 2)), 2 * d * math.tan(math.radians(LL_FOV[1] / 2))).loft()
    hit = [k for k, p in PARTS.items() if k not in ("limelight", "limelight_mount") and overlap(cone, p["shape"]) > 1e-5]
    print("   clear" if not hit else "   BLOCKED by " + ", ".join(hit))
    return not hit

def mounting_report():
    """Every part said to be mounted on another (DEVICE: a servo's tabs, a motor's face, a bracket) actually touches it:
    a claimed mounting across a gap is a part that floats."""
    from OCP.BRepExtrema import BRepExtrema_DistShapeShape
    print("== mountings: each part touches what it's said to bolt to")
    bad = []
    for a, b, how, _ in DEVICE + [(a, b, how, 0) for a, b, how in CAPTIVE if "TO SETTLE" not in how]:
        if a not in PARTS or b not in PARTS: continue
        d = BRepExtrema_DistShapeShape(solid(PARTS[a]["shape"]).wrapped, solid(PARTS[b]["shape"]).wrapped); d.Perform()
        if d.Value() > (0.13 if "bearing" in how else 0.03): bad.append(   # (an undrawn bearing's ring: 14 mm bore, 8 mm shaft)
           f"{a} is {d.Value():.2f} in from {b} ({how[:50]})")
    for m in bad: print("   FLOATS: " + m)
    if not bad: print(f"   all {len({(a, b) for a, b, _, _ in DEVICE} | {(a, b) for a, b, _ in CAPTIVE})} touch")
    for a, b, how in CAPTIVE:
        if "TO SETTLE" in how: print(f"   to settle: {a} on {b}: {how.split('TO SETTLE ', 1)[1]}")
    return not bad

def report():
    ok = service_report()
    ok &= mounting_report()
    ok &= space_report()
    ok &= view_report()
    ok &= joint_report()
    ok &= dress_report()
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

    fk = [k for k, p in PARTS.items() if p["moves"] in ("feed_L", "feed_R")]
    if fk:
        print(f"== the feeder arms swinging 0 .. {FEED_SWING:.1f} deg each (a NECTAR pushes its wheel out {FLY_TRAVEL:.2f} in), on their own springs")
        fixed = {k: v for k, v in shapes.items() if k not in fk and PARTS[k]["moves"] not in ("feed_L", "feed_R")}
        def pose(k, f):
            sg = 1 if PARTS[k]["moves"] == "feed_L" else -1
            return solid(shapes[k]).rotate(cq.Vector(0, sg * FEED_Y, FEED_PIVOT_Z), cq.Vector(1, sg * FEED_Y, FEED_PIVOT_Z), -sg * FEED_SWING * f)
        def hits(f):
            out = {}
            for k in fk:
                w = pose(k, f)
                for o, ow in fixed.items():
                    if allowed(k, o): continue
                    v = overlap(w, ow)
                    if v > 1e-4: out[(k, o)] = v
            return out
        rest = hits(0.0); new_ = {}
        for f in (0.5, 1.0):
            for ko, v in hits(f).items():
                if v > rest.get(ko, 0) + 1e-4: new_.setdefault(ko, f)
        for (a, b), f in sorted(new_.items()): print(f"   at {f * FEED_SWING:.1f} deg: CLASH {a} x {b}")
        w = bb(pose("feeder_wheel_L", 1.0)); print("   " + ("clear" if not new_ else f"{len(new_)} clashes") + f"; the left wheel's axle moves {(w[2] + w[3]) / 2 - FEED_Y:.2f} in out, {(w[4] + w[5]) / 2 - FEED_Z:+.2f} in up")
        ok &= not new_

    gk = [k for k in PARTS if k.startswith("lm_96mm_Gecko") and bb(PARTS[k]["shape"])[2] > 0]
    if gk and "feeder_wheel_L" in PARTS:
        b = bb(PARTS[gk[0]]["shape"]); WY, WZ, WR = (b[2] + b[3]) / 2, (b[4] + b[5]) / 2, (b[3] - b[2]) / 2
        print("== the feeder pair hands each piece to the flywheels (centred in the column, each side's module open as far as the piece pushes it)")
        for nm, r in (("POLLEN", RP), ("NECTAR", RN)):
            d = max(0.0, r - RP)                                   # the module's opening: a NECTAR pushes it 0.41
            def reach(cy, cz, cr):
                h2 = (r + cr) ** 2 - (cy + d) ** 2
                return (cz - math.sqrt(h2), cz + math.sqrt(h2)) if h2 > 0 else None
            f, w = reach(FEED_Y, FEED_Z, FEED_R), reach(WY, WZ, WR)
            z0 = LANE_BOTTOM + r
            good = f and w and f[0] <= z0 and f[1] - w[0] >= 0.05
            print(f"   {nm}: gripped by the feeder from z {f[0]:.2f} (at the backstop it's at {z0:.2f}) to {f[1]:.2f}; by the flywheels from {w[0]:.2f}: "
                  + (f"{f[1] - w[0]:.2f} in of overlap" if good else "A GAP"))
            ok &= bool(good)

    print("== pieces on their path (a NECTAR and a POLLEN at each step; only what's meant to touch them may)")
    bad = []
    LM_TOUCH = re.compile(r"^lm_.*(feeder|pad_|Gecko|backstop|Hyper_Hub|Spacer|Sonic)", re.I)
    TOUCH = ("lane_roller_", "ceiling", "feeder", "pad", "flywheel_", "roller", "ramp_", "mouth_floor_", "floor", "backstop", "hood", "star_")   # the hood's lid turns the shot
    (rx0, rz0), (rx1, rz1) = RAMP
    def floor_z(x): return 0.0 if x >= rx0 else (rz1 + (rx1 - x) / (rx1 - rx0) * (rz0 - rz1) if x >= rx1 else LANE_BOTTOM)
    lane = [(x, floor_z(x)) for x in (BACKSTOP_X + 1.82, 0.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)]   # its bottom on the lane, the ramp, the tiles
    path = [(COL_X, z) for z in (5.0, 6.65, 8.0, 9.0, HOOD_BEND_Z)] \
           + [(COL_X + hood_path(HOOD_TURN * k / 4)[0], hood_path(HOOD_TURN * k / 4)[1]) for k in (1, 2, 3, 4)]
    for r in (RN, RP):                      # off-centre: a piece at the mouth's edge, on the floor, where it meets a star's tips
        for sgn in (1, -1):                 # (the star, the floor, the ramp and the roller, which rises, may touch it; nothing else)
            yc = sgn * (MOUTH_HALF - r)          # as far out as the floor holds it
            d = STAR_R + r - 0.41
            xc = STAR[0] + math.sqrt(max(d * d - (abs(yc) - STAR[1]) ** 2, 0))
            ball = cq.Workplane("XY").sphere(r).translate((xc, yc, floor_z(xc) + r))
            for k, w in shapes.items():
                if k in (f"star_{'L' if sgn > 0 else 'R'}",) or k.startswith(("mouth_floor_", "ramp_", "roller", "lane_roller_")): continue
                y = yc
                if overlap(ball, w) > 1e-4: bad.append(f"off-centre {'NECTAR' if r == RN else 'POLLEN'} at Y {y} x {k}")
    for r in (RN, RP):
        for x, z0, lifted in [(x, z, False) for x, z in lane] + [(x, z, True) for x, z in path]:
            ball = cq.Workplane("XY").sphere(r).translate((x, 0.15 if lifted else 0, z0 if lifted else z0 + r))
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

def cut_holes():
    """Each screw's holes in the parts it passes: clearance, a counterbore for a sunk head, the insert's hole where it threads."""
    for f in FASTENERS:
        d, clr, hd, hh, ih, il, nh, nd = SCREW[f["size"]]
        L = f["L"] * MM
        clamp = [f["a"]] + list(f["via"]) + ([f["b"]] if f["end"] == "nut" else [])
        for k in dict.fromkeys(clamp + [f["b"]]):
            p = PARTS[k]
            if not (p["kind"] == "print" or k.startswith("rail_")): continue
            dia = clr if k in clamp else (ih if f["end"] == "insert" else clr)
            cut = _cyl(f["p"], f["v"], dia / 2 * MM, -0.01, L)
            if f["cb"] and k == f["a"]: cut = cut.union(_cyl(f["p"], f["v"], (hd + 0.6) / 2 * MM, -0.6, 0))
            try: p["shape"] = solid(p["shape"]).cut(solid(cut))
            except Exception as e: print("   (hole not cut:", f["name"], k, e, ")")

BEARING_OD = 14.1 * MM                  # goBILDA 1611 flanged / 8x14x5 bearings: a 14 mm pocket (CHECK the press on a fit coupon)
def cut_bores():
    """Every shaft through a printed part gets its bore: a bearing's pocket where the joint says it turns in one, else a
    clearance (or, for a printed roller on its shaft, a snug) hole along the shaft's own axis."""
    def axis_of(w):
        b = bb(w); ext = [b[1] - b[0], b[3] - b[2], b[5] - b[4]]; i = ext.index(max(ext))
        c = [(b[2 * j] + b[2 * j + 1]) / 2 for j in range(3)]
        return "xyz"[i], [c[j] for j in range(3) if j != i], (b[2 * i], b[2 * i + 1])
    jobs = []
    for a, b, how in CAPTIVE:
        for sh, pr in ((a, b), (b, a)):
            if sh in PARTS and pr in PARTS and re.search(r"(shaft|pivot|stub)", sh) and PARTS[pr]["kind"] == "print" and not re.search(r"(shaft|pivot|stub)", pr):
                d = 13.92 * MM if "clutch" in how else BEARING_OD if re.search(r"(1611|bearing)", how) else (8.1 * MM if "roller" in pr else 8.4 * MM)
                jobs.append((sh, pr, d))
    for n in ("L", "R"):
        for k in (f"fly_block_{n}_front", f"fly_yoke_{n}"):
            if k in PARTS:
                y = (1 if n == "L" else -1) * FLY_AXIS[0]
                jobs.append((None, k, ("x", [y, FLY_AXIS[1]], (-6, 2), BEARING_OD)))
    done = 0
    for sh, pr, d in jobs:
        if sh is None: ax, c, (a0, a1), d = d
        else: ax, c, (a0, a1) = axis_of(PARTS[sh]["shape"])
        try:
            PARTS[pr]["shape"] = solid(PARTS[pr]["shape"]).cut(solid(cyl(ax, c, d, a0 - 0.5, a1 + 0.5))); done += 1
        except Exception as e: print("   (bore not cut:", sh, pr, e, ")")
    return done

def finish_edges():
    """Round every printed part's edges 0.8 mm (or its straight ones, or chamfer them 0.5 mm, whichever OCC manages), after
    its holes are cut: nicer to hold and print, and no sharp corners on a robot people handle. The gears keep their teeth."""
    done = 0
    for k, p in PARTS.items():
        if p["kind"] != "print" or k.startswith("ex_gear"): continue
        s_ = solid(p["shape"]); lines = [e for e in s_.Edges() if e.geomType() == "LINE"]
        for f in (lambda: s_.fillet(0.8 * MM, s_.Edges()), lambda: s_.fillet(0.8 * MM, lines), lambda: s_.chamfer(0.5 * MM, None, lines)):
            try:
                r = f()
                if r.isValid() and abs(r.Volume() - s_.Volume()) < 0.05 * s_.Volume(): p["shape"] = cq.Workplane().add(r); done += 1; break
            except Exception: pass
    return done

def fasteners_md():
    rows, n, ins, nuts = [], {}, {}, {}
    for f in FASTENERS:
        n[(f["size"], f["L"])] = n.get((f["size"], f["L"]), 0) + 1
        if f["end"] == "insert": ins[f["size"]] = ins.get(f["size"], 0) + 1
        if f["end"] == "nut": nuts[f["size"]] = nuts.get(f["size"], 0) + 1
    rows += ["## Screws (v2, drawn and checked by build.py)", "", "| Screw | One robot | Two robots |", "|---|---|---|"]
    for (sz, l), c in sorted(n.items()): rows.append(f"| {sz} x {l} socket head{' (countersunk for the faces)' if sz == 'M3' and l == 8 else ''} | {c} | {2 * c} |")
    for sz, c in sorted(ins.items()): rows.append(f"| {sz} heat-set insert | {c} | {2 * c} |")
    for sz, c in sorted(nuts.items()): rows.append(f"| {sz} nyloc nut | {c} | {2 * c} |")
    dv = sum(c for *_, c in DEVICE)
    rows += [f"| on the devices (servos, hubs, intake motor, Limelight): see below | {dv} | {2 * dv} |", "",
             "Buy 20% over for each line: dropped screws and stripped inserts.", "",
             "## Every joint", "", "| Joint | Holds | Screw | Through | Into |", "|---|---|---|---|---|"]
    for f in FASTENERS:
        into = {"insert": f"an insert in `{f['b']}`", "nut": "a nyloc", "tapped": f"`{f['b']}`'s thread"}[f["end"]]
        thr = ", ".join(f"`{k}`" for k in dict.fromkeys([f["a"]] + list(f["via"]) + ([f["b"]] if f["end"] == "nut" else [])))
        rows.append(f"| {f['name']} | {f['what']}{' (CHECK: ' + f['match'] + ')' if f['match'] else ''} | {f['size']} x {f['L']} | {thr} | {into} |")
    rows += ["", "| Held without a screw of its own | By | How |", "|---|---|---|"]
    for a, b, how in CAPTIVE:
        if how: rows.append(f"| `{a}` | `{b}` | {how} |")
    for a, b, how, c in DEVICE:
        if how: rows.append(f"| `{a}` | `{b}` | {how} ({c}) |")
    return "\n".join(rows) + "\n"


# ---------------------------------------------------------------- the bill of materials (both robots), from the model
LM_NAMES = [   # the mentor's launcher's parts, as a buyer reads them
    (r"^(2325-|1628-|IR$|OR$|2302-0014-0064|1231-0048)", "goBILDA 3208-0004-0001 gear-driven turret kit (176T ring, bearing, 64T drive gear and its mount)", "kit"),
    (r"^Hole Lowside U-Channel", "goBILDA 1121 low-side U-channel, {len} mm (1121-00xx-{len:04d})", "len"),
    (r"^96mm Gecko Wheel", "goBILDA 96 mm Gecko wheel, 15A (the flywheels)", None),
    (r"^96mm Steel Shaft", "goBILDA 96 mm 8mm REX steel shaft", None),
    (r"^8x14x5mm Bearing", "goBILDA 8 x 14 x 5 mm round-bore bearing", None),
    (r"^8mm REX Hyper Hub", "goBILDA 8mm REX Hyper Hub", None),
    (r"^8mm Spacer", "goBILDA 8mm REX spacer (8 mm long)", None),
    (r"^1505-0032-0160", "goBILDA 1505-0032-0160 (as the mentor's flywheel stack; CHECK)", None),
    (r"^Dual Block", "goBILDA dual block (CHECK the SKU)", None),
    (r"^Quad Block", "goBILDA quad block (CHECK the SKU)", None),
    (r"^Square Beam", "goBILDA 40 mm square beam", None),
    (r"^Hole U Beam", "goBILDA 40 mm U-beam", None),
    (r"^Hole Flat Beam", "goBILDA 168 mm flat beam", None),
    (r"^Hole L Beam", "goBILDA 120 mm L-beam", None),
    (r"^part piece", "the mentor's flywheel-shaft collar (as his module; CHECK the part)", None),
]

def bom_md():
    buy, made, printed = {}, {}, []
    def add(d, k, n=1): d[k] = d.get(k, 0) + n
    for k, p in PARTS.items():
        w = p["what"]
        if k.startswith("lm_"):
            nm = re.sub(r"\s*\(\d+\)$", "", w.split(": ", 1)[1]).replace(" (rides the sprung module)", "")
            if re.search(r"(with the shaft|included)", nm): continue      # e-clips: they come with their shafts
            if p["kind"] == "buy" and re.match(r"^[A-Za-z0-9_]+ \(", nm):    # cad/transfer's parts carry their own description
                inner = nm[nm.index("(") + 1:nm.rindex(")")]
                if re.search(r"(print|PETG)", inner): printed.append((k, "PETG", inner, p)); continue
                if re.search(r"(aluminium|polycarbonate)", inner): add(made, inner); continue
                add(buy, inner); continue
            for rx, label, how in LM_NAMES:
                if re.search(rx, nm):
                    if how == "kit": add(buy, label, 0); buy[label] = 1
                    elif how == "len":
                        b = bb(p["shape"]); L = round(max(b[1] - b[0], b[3] - b[2], b[5] - b[4]) * 25.4 / 24) * 24
                        add(buy, label.format(len=L))
                    else: add(buy, label)
                    break
            else: add(buy, nm + " (the mentor's module; CHECK)")
            continue
        if p["kind"] == "print": printed.append((k, None, w, p)); continue
        if p["kind"] == "cut": add(made, w); continue
        if "set of 4" in w: buy["goBILDA 3213-3606-0002 96 mm mecanum, set of 4"] = 1; continue
        add(buy, w)
    rows = ["# Bill of materials (written by build.py from the model)", "",
            "Quantities are for **one robot**; the last column is for two. CHECK marks a part number or size to confirm before ordering.",
            "Buy about 10% extra of every small part.", "", "## Bought", "", "| Part | One robot | Two robots |", "|---|---|---|"]
    for nm, c in sorted(buy.items()): rows.append(f"| {nm} | {c} | {2 * c} |")
    rows += ["", "## Made (cut, not printed)", "", "| Part | One robot | Two robots |", "|---|---|---|"]
    for nm, c in sorted(made.items()): rows.append(f"| {nm} | {c} | {2 * c} |")
    rows += ["", "## Printed", "", "Grams are an estimate (the part's volume at about 55% effective fill); print hours at about 12 g/h.", "",
             "| Part | Material | One robot | Two robots | Grams each |", "|---|---|---|---|---|"]
    tot = {}
    for k, mat, w, p in sorted(printed, key=lambda r: r[0]):
        mat = mat or ("PETG-CF" if "PETG-CF" in w else "TPU 95A" if "TPU" in w else "PETG")
        g = solid(p["shape"]).Volume() * 16.387 * (1.27 if "PETG" in mat else 1.21) * 0.55
        tot[mat] = tot.get(mat, 0) + g
        rows.append(f"| `{k}` | {mat} | 1 | 2 | {g:.0f} |")
    rows += ["", "| Filament | One robot | Two robots |", "|---|---|---|"]
    for mat, g in sorted(tot.items()): rows.append(f"| {mat} | {g / 1000:.2f} kg | {2 * g / 1000:.2f} kg |")
    hours = sum(tot.values()) / 12
    rows += ["", f"About {hours:.0f} printer-hours a robot, {2 * hours:.0f} for two: with four home printers, about {2 * hours / 4 / 10:.1f} days at 10 h a day.", ""]
    return "\n".join(rows) + "\n" + fasteners_md()

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

# For Onshape: one sub-assembly per subsystem, in build order; inside it, its fixed parts, then one per moving body
# (named with the mate it needs), then its screws. Readable names; the mentor's parts say so.
def subsystem(k):
    m = PARTS[k]["module"]
    if k.startswith(("feeder_", "lm_feeder_floor", "lm_backstop")): return "09 Feeder and backstop"
    if k.startswith("fly_") or PARTS[k]["moves"] in ("fly_L", "fly_R"): return "10 Flywheels (sprung sides)"
    if k.startswith(("hood", "turret_drive_plate", "lm_turret_", "lm_2325", "lm_1628", "lm_IR", "lm_OR", "lm_2302", "lm_1231")): return "11 Turret and hood"
    if m == "launcher": return "12 Launcher frame and feet"
    if k.startswith(("star_", "front_bracket_")): return "07 Star wheels and front brackets"
    return {"frame": "01 Frame", "odometry": "03 Odometry", "extractor": "06 Extractor", "intake": "05 Intake roller", "lane": "08 Mouth and lane",
            "elec": "13 Electronics"}.get(m, "02 Drive corners" if m.startswith("corner") else "04 Flaps and soft faces" if m.startswith("front") else "14 Other")
def motion(k):
    mv = PARTS[k]["moves"]
    if mv == "intake": return f"moves - intake arms, revolute on the arms' pivot (Y axis), 0 to {lift_deg(FLOAT):.0f} deg up"
    if mv == "extractor" and not k.startswith("ex_stub"): return "moves - extractor, revolute on the stub shafts (Y axis), 0 to 146 deg, drawn stowed"
    for n in ("FL", "FR", "BL", "BR"):
        if k in (f"wheel_{n}", f"dm_{n}_wheel"): return f"moves - wheel {n}, revolute on its shaft"
    if k in ("star_L", "star_R"): return f"moves - star wheel {k[-1]}, revolute on its vertical shaft"
    if mv in ("feed_L", "feed_R"): return f"moves - feeder arm {mv[-1]}, revolute on its pivot shaft (X axis), 0 to {FEED_SWING:.0f} deg out, sprung in"
    if mv in ("fly_L", "fly_R"): return f"moves - flywheel side {mv[-1]}, slider along Y, 0 to {FLY_TRAVEL:.2f} in out, sprung in"
    return None

if __name__ == "__main__":
    ok = report()
    print("ALL CHECKS PASS" if ok else "CHECKS FAIL")
    if CHECK_ONLY: sys.exit(0 if ok else 1)
    cut_holes()
    print(f"cut {cut_bores()} bores (bearing pockets and shaft holes) in the printed parts")
    print(f"finished the edges of {finish_edges()} printed parts")
    os.makedirs(os.path.join(HERE, "stl"), exist_ok=True)
    # The STEP carries no mates: README, "In Onshape", says which to add (the "moves - " groups name them).
    seen = {}
    def label(k):
        nm = k
        if k.startswith("lm_"):
            nm = "mentor - " + re.sub(r"[^A-Za-z0-9 .,()+_-]", "", PARTS[k]["what"].split(": ", 1)[1].split(" (")[0])[:48]
        seen[nm] = seen.get(nm, 0) + 1
        return nm if seen[nm] == 1 else f"{nm} ({seen[nm]})"
    MMSH, dressed, plain = {}, [], []
    def mm(sh):                              # each model's shape scaled once, so every copy of it in the STEP is one shape, placed
        if id(sh) not in MMSH: MMSH[id(sh)] = (sh.scale(25.4), sh)
        return MMSH[id(sh)][0]
    def mm_loc(loc):
        from OCP.gp import gp_Trsf, gp_Vec
        t = gp_Trsf(); t.SetValues(*[loc.wrapped.Transformation().Value(i, j) * (25.4 if j == 4 else 1) for i in (1, 2, 3) for j in (1, 2, 3, 4)])
        return cq.Location(t)
    def add_part(tgt, k, nm):
        lp = look_parts(k) if PARTS[k].get("looks") else None
        if lp:
            dressed.append(k)
            sub = cq.Assembly(name=nm)
            for i, (sh, loc, c) in enumerate(lp): sub.add(mm(sh), name=f"{nm} {i + 1}", loc=mm_loc(loc), color=cq.Color(*c))
            tgt.add(sub, name=nm)
        else:
            if PARTS[k].get("looks"): plain.append(k)
            tgt.add(solid(PARTS[k]["shape"]).scale(25.4), name=nm, color=cq.Color(*colour_of(k)))
    tree = {}
    for k in PARTS: tree.setdefault(subsystem(k), {}).setdefault(motion(k), []).append(k)
    screws = {}
    for f in FASTENERS: screws.setdefault(subsystem(f["a"]) if f["a"] in PARTS else "14 Other", []).append(f)
    asm = cq.Assembly(name="biobuzz_printed_chassis_v3")
    full = cq.Assembly(name="biobuzz_printed_chassis_v3_full")
    for sysname in sorted(set(tree) | set(screws)):
        sub, fsub = cq.Assembly(name=sysname), cq.Assembly(name=sysname)
        for mo in sorted(tree.get(sysname, {}), key=lambda m: (m is not None, m or "")):
            tgt, ftgt = (sub, fsub) if mo is None else (cq.Assembly(name=mo), cq.Assembly(name=mo))
            for k in tree[sysname][mo]:
                p = PARTS[k]; nm = label(k)
                if not k.startswith(("lm_", "dm_")): tgt.add(solid(p["shape"]).scale(25.4), name=nm, color=cq.Color(*colour_of(k)))
                add_part(ftgt, k, nm)
                if p["kind"] == "print": solid(p["shape"]).scale(25.4).exportStl(os.path.join(HERE, "stl", f"{k}.stl"), tolerance=0.05, angularTolerance=0.3, ascii=False)   # 0.05 mm: finer than a printer
            if mo is not None:
                if tgt.children: sub.add(tgt, name=mo)
                fsub.add(ftgt, name=mo)
        if screws.get(sysname):
            sz, fsz = cq.Assembly(name="screws"), cq.Assembly(name="screws")
            for f in screws[sysname]:
                sh = solid(screw_solids(f)).scale(25.4); nm = f"{f['size']}x{f['L']} - {f['name']}"
                sz.add(sh, name=nm, color=cq.Color(0.2, 0.2, 0.22)); fsz.add(sh, name=nm, color=cq.Color(0.2, 0.2, 0.22))
            sub.add(sz, name="screws"); fsub.add(fsz, name="screws")
        if sub.children: asm.add(sub, name=sysname)
        full.add(fsub, name=sysname)
    asm.save(os.path.join(HERE, "printed-chassis.step"))
    if USE_LM or USE_DM:
        import gzip, shutil
        fp = os.path.join(HERE, ".cache", "printed-chassis-full.step"); full.save(fp)
        with open(fp, "rb") as a, gzip.open(fp + ".gz", "wb") as b: shutil.copyfileobj(a, b)
        full.save(os.path.join(HERE, ".cache", "printed-chassis.glb"), tolerance=0.15, angularTolerance=0.35)   # meshes, for viewing (millimetres)
    with open(os.path.join(HERE, "BOM.md"), "w") as f: f.write(bom_md())
    with open(os.path.join(HERE, "parts.md"), "w") as f:
        f.write("# Parts (written by build.py)\n\nCHECK marks a size not read from a vendor file. Two robots: double every line.\n\n" + parts_md() + "\n" + fasteners_md())
    print(f"STEP: {len(dressed)} bought parts as their real models" + (f"; {len(plain)} as envelopes (vendor.py hasn't cached their models)" if plain else ""))
    try:
        import render
        render.render(sys.modules[__name__], os.path.join(HERE, "views"))
    except Exception as e:                   # no VTK or no display: the flat views
        print("render.py failed (", e, "): drawing the flat views"); views(os.path.join(HERE, "views"))
    print("wrote printed-chassis.step, stl/, parts.md, views/")
