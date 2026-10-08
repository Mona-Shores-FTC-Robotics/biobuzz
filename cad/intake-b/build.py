"""The unified front, in the robot CAD's frame (mm): x across (right is -x), y up, z forward.
A 13.8 in roller, its axle 1.06 in in front of the face, that floats straight up 1.3 in so a NECTAR passes under it; the FLOWER
extractor on its own fixed shaft ahead of the roller; and the Rigid V's two corner plates as an optional group.
The wheel-plate standoffs, 80 mm wheel shafts, bearings and odometry pods are cad/robot-addons'.

    python3 cad/intake-b/build.py      # writes dhs-intake-b.step and stl/ next to this file

Groups: "fixed" (the chassis add-ons, side plates, extractor bearings, servo), "float" (the roller, its motor, belt
and carriage: they rise FLOAT together), "hook" (the extractor: turns about its shaft, 0 down to STOW stowed) and "vee".
"""
import math, os, sys
import cadquery as cq
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "robot-addons"))
import build as A                      # the robot frame, helpers and the chassis add-ons
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
import fasteners as FA                 # goBILDA screws and nuts: bolt() drills the holes and adds them
from build import C, F, FACE, IN, AX_Y, AX_ZF, AX_ZR, RAIL_OUT, PLATE_IN, PLATE_T, BORE, M3, M4, xr, xl, box, cyl, clip, ramp_block

# ---- the numbers the intake study fixed ----
ROLL_R = 25.4                          # 2 in vector wheels (WCP-0353/0354): they move every piece to the middle, where the lane is
ROLL_Y = F + 2.4 * IN + ROLL_R         # roller axle at rest: bottom of the wheels 2.4 in up
ROLL_Z = FACE + 0.061 * IN + ROLL_R    # ...its back 0.06 in clear of the front uprights (its front 2.06 in out)
ROLL_HALF = 6.9 * IN                   # the roller's ends: the left pulley and the right gear sit outside this
VEC_IN, VEC_N, VEC_W = 0.4 * IN, 6, 1.0 * IN   # vector wheels from 0.4 in each side of centre, 6 a side, 1.0 in wide
FLOAT = 1.3 * IN                       # the roller rises this far: 0.85 in passes a NECTAR with 0.4 in of give, 1.3 with none
# Roller drive, 1:1: two goBILDA 3417-4008-0024 pulleys (24T HTD5, 8mm REX bore) on the shortest 9 mm belt, the
# 3412 Series 55-tooth (275 mm). Its centres: (275 - 24 * 5) / 2 = 77.5 mm, so the motor sits 77.5 mm over the roller.
# The motor rides on the roller's carriage, so the belt never changes length as the roller floats.
PITCH_D24 = 24 * 5 / math.pi           # 38.2 mm
MOTOR_C = 77.5
MOTOR_Y = ROLL_Y + MOTOR_C
EXS_Y, EXS_Z = F + 4.5 * IN, FACE + 2.4 * IN     # the extractor's shaft: above any NECTAR, ahead of the roller
EX_X = float(os.environ.get("EX_X", "4.2")) * IN   # extractor arms, from centre: outside the FLOWER (its widest part, the black bracket, is +-2.35 in)
EX_T = 3.175                           # 1/8 in aluminium
EX_ZB = FACE + 1.94 * IN + 2.5 * IN    # the FLOWER block's back edge, down
EX_FS = (F + A.ROD_Z, EX_ZB + A.ROD_X) # the block's cross shaft (y, z)
STOW = float(os.environ.get("STOW", "146"))      # degrees from down to stowed: 146 keeps the block inside the 18 in start and the left arm off the roller motor
V_IN, V_OUT, V_TOP = PLATE_IN + PLATE_T, 9.0 * IN - 4.0, 4.0 * IN    # Rigid V plates: from the side plates' outer face...
V_ROOT, V_TIP = ROLL_Z + 14, FACE + 2.8 * IN                          # ...at their front edge, forward to 2.8 in (18 in start)
OUT0, OUT1 = PLATE_IN + PLATE_T, PLATE_IN + 2 * PLATE_T              # the float plates, outboard of the side plates
GEAR_R, GEAR_W = 25.5, 6.0
GX0, GX1 = ROLL_HALF + 2.0, ROLL_HALF + 2.0 + GEAR_W                   # gear plane, right of the roller's end

fixed, flt, hook, vee = {}, {}, {}, {}
def part(d, name, wp, col, kind): d[name] = (wp, col, kind)
BLUE, ALU, STEEL, BRASS, POLY, BLACK = (0.18, 0.37, 0.62), (0.75, 0.78, 0.82), (0.8, 0.82, 0.85), (0.85, 0.75, 0.3), (0.6, 0.78, 0.96), (0.13, 0.15, 0.17)

def link(p0, p1, w, x0, x1):
    """A flat bar in a y-z plane between two points (y, z), rounded ends, from x0 to x1."""
    (y0, z0), (y1, z1) = p0, p1
    L = math.hypot(y1 - y0, z1 - z0); ang = math.degrees(math.atan2(y1 - y0, z1 - z0))
    bar = cq.Workplane("YZ").center(0, L / 2).rect(w, L).extrude(abs(x1 - x0))
    bar = bar.union(cq.Workplane("YZ").circle(w / 2).extrude(abs(x1 - x0))).union(cq.Workplane("YZ").center(0, L).circle(w / 2).extrude(abs(x1 - x0)))
    return bar.rotate((0, 0, 0), (1, 0, 0), -ang).translate((min(x0, x1), y0, z0))
def sector_at(cy, cz, a0, a1, r0, r1, x0, x1):
    n = 12; ang = [math.radians(a0 + (a1 - a0) * i / n) for i in range(n + 1)]
    pts = [(cy + r1 * math.sin(q), cz + r1 * math.cos(q)) for q in ang] + [(cy + r0 * math.sin(q), cz + r0 * math.cos(q)) for q in reversed(ang)]
    return cq.Workplane("YZ").polyline(pts).close().extrude(abs(x1 - x0)).translate((min(x0, x1), 0, 0))
def rex(y, z, x0, x1):
    """An 8 mm REX shaft's profile along x (the hex across its flats, inside its 8 mm round): a hole that drives it."""
    lo, hi = min(x0, x1), max(x0, x1)
    return (cq.Workplane("YZ").polygon(6, A.REX_AF / math.cos(math.pi / 6)).extrude(hi - lo).translate((lo, y, z))
            .intersect(cyl("x", (0, y, z), BORE, lo, hi)))
def slot(y0, y1, z, w, x0, x1):
    """A vertical slot through a plate in the y-z plane, rounded ends."""
    return cyl("x", (0, y0, z), w, x0, x1).union(cyl("x", (0, y1, z), w, x0, x1)).union(box(x0, x1, y0, y1, z - w / 2, z + w / 2))

# ---- side plates: the outer wheel plates, now also the roller's and the extractor's ----
def side_plate(right):
    f = xr if right else xl
    x0, x1 = f(PLATE_IN), f(PLATE_IN + PLATE_T); lo, hi = min(x0, x1) - 1, max(x0, x1) + 1
    top = MOTOR_Y + 16
    pts = [(-127.0, AX_ZR - 24), (-79.0, AX_ZR - 24), (-79.0, FACE - 40), (top, FACE - 18), (top, ROLL_Z + 16),
           (EXS_Y + 12, ROLL_Z + 16), (EXS_Y + 12, EXS_Z + 11), (EXS_Y - 12, EXS_Z + 11), (EXS_Y - 12, ROLL_Z + 16),
           (-127.0, ROLL_Z + 16)]
    p = cq.Workplane("YZ").polyline(pts).close().extrude(PLATE_T).translate((min(x0, x1), 0, 0))
    for y, z in ((AX_Y, AX_ZF), (AX_Y, AX_ZR), (EXS_Y, EXS_Z)): p = p.cut(cyl("x", (0, y, z), 14.0, lo, hi))
    p = p.cut(slot(ROLL_Y, ROLL_Y + FLOAT, ROLL_Z, 10.0, lo, hi))                  # the roller shaft rises in this
    if right: p = p.cut(slot(ROLL_Y + 40, ROLL_Y + 40 + FLOAT, ROLL_Z, 6.0, lo, hi))  # the right float plate's guide screw
    else: p = p.cut(slot(MOTOR_Y, MOTOR_Y + FLOAT + 20, ROLL_Z, 12.0, lo, hi))       # the motor shaft's end rises in this notch
    for y in A.STANDOFF_Y:
        for z in A.STANDOFF_Z: p = p.cut(cyl("x", (0, y, z), M4, lo, hi))
    for y in (-115.0, -96.0): p = p.cut(cyl("x", (0, y, ROLL_Z + 3), M4, lo, hi))   # V plate tab
    return p
part(fixed, "side_plate_R (1/8 in aluminium)", side_plate(True), ALU, "cut")
part(fixed, "side_plate_L (1/8 in aluminium)", side_plate(False), ALU, "cut")
for n, (wp, col, kind) in A.parts.items():            # keep the chassis add-ons, minus the old plates, hinge and servo
    if n.startswith(("outer_plate", "hinge_", "servo", "bearing_R_hinge")): continue
    part(fixed, n, wp, col, kind)
for s, f in (("R", xr), ("L", xl)):
    part(fixed, f"float_stop_{s} (print, under the float plate; its tail, behind the V plate's tab, carries its screw)",
         box(f(OUT0), f(OUT1), ROLL_Y - 19, ROLL_Y - 13, ROLL_Z - 36, ROLL_Z + 10).union(box(f(OUT0), f(OUT1), ROLL_Y - 31, ROLL_Y - 13, ROLL_Z - 36, ROLL_Z - 20)), BLUE, "print")

# ---- the floating roller and its carriage ----
part(flt, "roller_shaft (8mm REX, 400 mm)", cyl("x", (0, ROLL_Y, ROLL_Z), 8.0, xr(OUT1 + 3), xl(OUT1 + 3)), STEEL, "buy")
# The roller centres what it picks up: everything behind it except the lane's 3.7 in is the robot's face, so a piece
# taken in off-centre has to be moved sideways by the roller itself. Each half is vector wheels whose rollers push it
# back and toward the middle; the face is the fence it slides along. Pulling in
# (bottom moving back), a WCP-0353 pushes to the robot's left, so it goes on the right half and the WCP-0354 on the left.
# Their 1/2 in hex bores take a printed insert on the 8mm REX shaft. A ball's centre can't pass 6.16 in (the side
# plates), so the 0.4-6.4 in each side covers every one, and inside 0.4 in it already clears the lane's walls.
part(flt, "roller_centre_wheel (48 mm gecko, 0.8 in)", cyl("x", (0, ROLL_Y, ROLL_Z), 48.0, C - VEC_IN, C + VEC_IN), (0.35, 0.66, 0.31), "buy")
for s, sgn, hand in (("L", 1, "WCP-0354"), ("R", -1, "WCP-0353")):
    for k in range(VEC_N):
        a, b = VEC_IN + k * VEC_W, VEC_IN + (k + 1) * VEC_W
        part(flt, f"roller_vector_{s}{k} ({hand}, 2 in vector wheel, printed 1/2 hex to 8mm REX insert)",
             cyl("x", (0, ROLL_Y, ROLL_Z), 2 * ROLL_R, C + sgn * a, C + sgn * b), (0.15, 0.15, 0.17), "buy")
    e0 = VEC_IN + VEC_N * VEC_W
    part(flt, f"roller_end_spacer_{s} (goBILDA 8mm REX spacer, 12.5 mm)", cyl("x", (0, ROLL_Y, ROLL_Z), 10.0, C + sgn * e0, C + sgn * ROLL_HALF), STEEL, "buy")
for s, f in (("R", xr), ("L", xl)):
    part(flt, f"roller_bearing_{s} (goBILDA 1611-0514-4008, 8mm REX bore)", cyl("x", (0, ROLL_Y, ROLL_Z), 14.0, f(OUT0), f(OUT1 + 1.2)), BRASS, "buy")
fr = link((ROLL_Y, ROLL_Z), (ROLL_Y + 40, ROLL_Z), 24.0, xr(OUT0), xr(OUT1))        # right: outboard, slides on the side plate
fr = fr.cut(cyl("x", (0, ROLL_Y, ROLL_Z), 14.0, xr(OUT1) - 1, xr(OUT0) + 1)).cut(cyl("x", (0, ROLL_Y + 40, ROLL_Z), M4, xr(OUT1) - 1, xr(OUT0) + 1))
part(flt, "float_plate_R (1/8 in aluminium)", fr, ALU, "cut")
# Belt drive inside the left plate, between the roller's end (6.9 in) and the plate (7.56 in).
PUL0, PUL1 = ROLL_HALF + 1.5, PLATE_IN - 2.0
part(flt, "roller_pulley_L (goBILDA 3417-4008-0024, 24T HTD5)", cyl("x", (0, ROLL_Y, ROLL_Z), PITCH_D24, xl(PUL0), xl(PUL1)), BLACK, "buy")
part(flt, "motor_pulley_L (goBILDA 3417-4008-0024, 24T HTD5)", cyl("x", (0, MOTOR_Y, ROLL_Z), PITCH_D24, xl(PUL0), xl(PUL1)), BLACK, "buy")
R = PITCH_D24 / 2
part(flt, "roller_belt_L (goBILDA 3412 Series, 9 mm, 55T, 275 mm)", box(xl(PUL0 + 1.5), xl(PUL1 - 1.5), ROLL_Y - R - 3, MOTOR_Y + R + 3, ROLL_Z - R - 3, ROLL_Z + R + 3)
     .cut(box(xl(PUL0), xl(PUL1), ROLL_Y - R, MOTOR_Y + R, ROLL_Z - R, ROLL_Z + R)), BLACK, "buy")
FACE_X = PUL0 - 1.0                     # the motor's mounting face, just inboard of its pulley
part(flt, "roller_motor_L (goBILDA 5203-2402-0005, 1150 RPM)", cyl("x", (0, MOTOR_Y, ROLL_Z), 37.0, xl(FACE_X - 6), xl(FACE_X - 126)), BLACK, "buy")
part(flt, "motor_shaft_L (the motor's own 24 mm 8mm-REX output shaft)", cyl("x", (0, MOTOR_Y, ROLL_Z), 8.0, xl(FACE_X - 6), xl(FACE_X + 18)), STEEL, "buy")
# Left carriage: the motor's face plate and cradle (sliding on the left upright's front face on two shoulder screws in
# slots), a bridge over the motor pulley and the side plate's top edge, and an outboard link plate down to the roller's
# left bearing. Roller, motor and both pulleys move as one.
BR0, BR1 = MOTOR_Y + 24, MOTOR_Y + 40
# The uprights (the mentor's goBILDA 5-hole low-side channels) have no holes in their front flanges; their web, on the
# outboard side (|x| 133.5-136), has short vertical slots at y -7.3, 16.7, 40.7, 10.45 mm behind the face. The carriage
# and the servo bracket each wrap round the upright's front corner and bolt into those, nuts inside the channel.
WEB_X, WEB_Z = 136.0, FACE - 10.45      # the web's outboard face; the slots' height behind the face
GUIDE_Y = (16.7, 40.7)                  # the carriage's two shoulder screws, in the left upright's web slots
mb = box(xl(FACE_X - 6), xl(FACE_X), MOTOR_Y - 24, BR1, FACE, ROLL_Z + 24)                    # face plate
mb = mb.union(box(xl(124.0), xl(FACE_X - 6), -28.0, 46.0, FACE, FACE + 4.0))                 # cradle on the upright's face
mb = mb.union(box(xl(FACE_X - 6), xl(OUT0), BR0, BR1, ROLL_Z - 12, ROLL_Z + 12))               # bridge, to the side plate's outer face
mb = mb.cut(box(xl(FACE_X - 7), xl(FACE_X + 1), MOTOR_Y - 7, MOTOR_Y + 7, ROLL_Z - 7, ROLL_Z + 7))
for dz in (-8, 8):    # the Yellow Jacket's face: M4 on goBILDA's 16 mm square; counterbored, the heads sit under the pulley
    for dy in (-8, 8): mb = mb.cut(cyl("x", (0, MOTOR_Y + dy, ROLL_Z + dz), 7.6, xl(FACE_X - 4.2), xl(FACE_X + 1)))
mb = mb.union(box(xl(WEB_X + 0.2), xl(WEB_X + 4.2), -24.0, 47.0, FACE - 17.0, FACE + 4.0))     # tab on the upright's web
for y in GUIDE_Y:   # the shoulders (5 mm) ride in these as the roller floats
    mb = mb.cut(cyl("x", (0, y - FLOAT, WEB_Z), 5.3, xl(WEB_X), xl(WEB_X + 5)).union(cyl("x", (0, y, WEB_Z), 5.3, xl(WEB_X), xl(WEB_X + 5)))
                .union(box(xl(WEB_X), xl(WEB_X + 5), y - FLOAT, y, WEB_Z - 2.65, WEB_Z + 2.65)))
part(flt, "motor_carriage_L (print)", mb, BLUE, "print")
fl = link((ROLL_Y, ROLL_Z), (BR1 - 12, ROLL_Z), 24.0, xl(OUT0), xl(OUT1))
fl = fl.cut(cyl("x", (0, ROLL_Y, ROLL_Z), 14.0, xl(OUT0) - 1, xl(OUT1) + 1))
part(flt, "float_link_L (1/8 in aluminium, bolts to the carriage's bridge)", fl, ALU, "cut")

# ---- the FLOWER extractor: its own fixed shaft at X 9.9, Z 4.5 in ----
# Two 1/8 in aluminium arms, 4.2 in each side of centre, clamped to an 8 mm REX shaft that turns in bearings in the side
# plates. A short cross shaft carries the FLOWER block, its back edge 2.5 in ahead of the roller's front. It turns 0 (down)
# to STOW (folded up in front of the robot). A servo over the roller on the right drives the shaft through a 1:1 printed
# gear pair (module 1.5, 34 teeth, 51 mm centres) in the gap between the roller's right end and the side plate; the hard
# stops act on a tab on the servo's gear.
# Two stub shafts, each from its side plate's bearing in to the arm: nothing crosses the middle above the block, so the
# FLOWER (its bracket, posts and uprights) passes between the arms. The arms and the block's cross shaft make a U.
# Each arm sits on its stub's inner end, held there by an M4 screw and washer in the shaft's tapped end; goBILDA spacers
# (8 mm bore, 10 mm OD) fill from the arm out to the bearing (on the right, either side of the gear). So the U of arms
# and cross shaft is trapped between the side plates, and nothing near the roller is wider than 12 mm: the floating
# roller's 2 in wheels pass under the stubs with 0.07 in to spare, where 21 mm collars would touch them.
for s, f in (("R", xr), ("L", xl)):
    part(hook, f"extractor_stub_{s} (8mm REX, {PLATE_IN + PLATE_T + 2 - (EX_X - EX_T / 2):.0f} mm, tapped M4 ends)", cyl("x", (0, EXS_Y, EXS_Z), 8.0, f(EX_X - EX_T / 2), f(PLATE_IN + PLATE_T + 2)), STEEL, "buy")
    part(hook, f"arm_washer_{s} (M4 large washer, 12 mm OD, under the screw in the stub's end)", cyl("x", (0, EXS_Y, EXS_Z), 12.0, f(EX_X - EX_T / 2 - 1.2), f(EX_X - EX_T / 2))
         .cut(cyl("x", (0, EXS_Y, EXS_Z), FA.CLEAR[4], f(EX_X - EX_T / 2 - 2), f(EX_X - EX_T / 2 + 1))), STEEL, "buy")
    runs = [(EX_X + EX_T / 2, GX0), (GX1, PLATE_IN - 1.2)] if s == "R" else [(EX_X + EX_T / 2, PLATE_IN - 1.2)]
    for k, (a0, a1) in enumerate(runs):
        part(hook, f"arm_spacers_{s}{k} (goBILDA 8 mm bore spacers, 10 mm OD, stacked to {a1 - a0:.1f} mm)",
             cyl("x", (0, EXS_Y, EXS_Z), 10.0, f(a0), f(a1)).cut(cyl("x", (0, EXS_Y, EXS_Z), 8.1, f(a0) - 1 if f(a0) < f(a1) else f(a0) + 1, f(a1) + 1 if f(a0) < f(a1) else f(a1) - 1)), STEEL, "buy")
for s, f in (("R", xr), ("L", xl)):
    part(fixed, f"extractor_bearing_{s} (goBILDA 1611-0514-4008, 8mm REX bore)", cyl("x", (0, EXS_Y, EXS_Z), 14.0, f(PLATE_IN - 1.2), f(PLATE_IN + PLATE_T)), BRASS, "buy")
    xa, xb = f(EX_X - EX_T / 2), f(EX_X + EX_T / 2)
    arm = link((EXS_Y, EXS_Z), EX_FS, 15.0, xa, xb)    # 15 mm wide: clears the roller motor when stowed and the roller floats
    arm = arm.cut(rex(EXS_Y, EXS_Z, min(xa, xb) - 1, max(xa, xb) + 1)).cut(cyl("x", (0, EX_FS[0], EX_FS[1]), BORE, min(xa, xb) - 1, max(xa, xb) + 1))   # REX: the stub drives it
    if s == "L":   # stowed, the left arm lies over the roller's motor: relieve it for the motor's whole float, 1.75 mm clear
        mx0, mx1 = xl(FACE_X - 7), xl(FACE_X - 127)
        relief = (cyl("x", (0, MOTOR_Y, ROLL_Z), 41.0, mx0, mx1).union(cyl("x", (0, MOTOR_Y + FLOAT, ROLL_Z), 41.0, mx0, mx1))
                  .union(box(mx0, mx1, MOTOR_Y, MOTOR_Y + FLOAT, ROLL_Z - 20.5, ROLL_Z + 20.5)))
        arm = arm.cut(relief.rotate((0, EXS_Y, EXS_Z), (1, EXS_Y, EXS_Z), STOW))
    part(hook, f"extractor_arm_{s} (1/8 in aluminium, REX hole at the shaft{'; relieved over the roller motor' if s == 'L' else ''})", arm, ALU, "cut")
    part(hook, f"cross_washer_{s} (M4 large washer, 12 mm OD, under the screw in the cross shaft's end)", cyl("x", (0, EX_FS[0], EX_FS[1]), 12.0, f(EX_X + EX_T / 2), f(EX_X + EX_T / 2 + 1.2))
         .cut(cyl("x", (0, EX_FS[0], EX_FS[1]), FA.CLEAR[4], f(EX_X + EX_T / 2 - 1), f(EX_X + EX_T / 2 + 2))), STEEL, "buy")
CROSS_HALF = EX_X + EX_T / 2 - 0.5     # its ends 0.5 mm inside the arms' outer faces, so the screws' washers clamp the arms
part(hook, f"extractor_cross_shaft (8mm REX, {2 * CROSS_HALF:.0f} mm, tapped M4 ends)", cyl("x", (0, EX_FS[0], EX_FS[1]), 8.0, xr(CROSS_HALF), xl(CROSS_HALF)), STEEL, "buy")
part(hook, "flower_block (print)", ramp_block().translate((0, 0, EX_ZB - A.ZB)), (0.69, 0.42, 0.85), "print")
for s, f in (("R", xr), ("L", xl)):
    part(hook, f"block_collar_{s} (8mm REX clamping collar)", cyl("x", (0, EX_FS[0], EX_FS[1]), 21.0, f(1.36 * IN + 0.5), f(1.36 * IN + 8.5)), STEEL, "buy")
# A sector gear: teeth only where they mesh between down and stowed, so nothing sticks out ahead when stowed.
SV_Y, SV_Z = EXS_Y + 44.45, EXS_Z - 25.0                               # the servo's spline: 51 mm from the shaft, up and back
MESH_Q = math.degrees(math.atan2(SV_Y - EXS_Y, SV_Z - EXS_Z))         # the mesh, seen from the shaft (about 120 deg)
egear = cyl("x", (0, EXS_Y, EXS_Z), 20.0, xr(GX0), xr(GX1)).union(sector_at(EXS_Y, EXS_Z, MESH_Q - STOW - 12, MESH_Q + 12, 10, GEAR_R + 1.5, xr(GX0), xr(GX1)))
part(hook, "extractor_gear_R (print: module 1.5, 34T sector; REX bore, held between the stub's spacers)", egear.cut(rex(EXS_Y, EXS_Z, xr(GX1) - 1, xr(GX0) + 1)), BLUE, "print")
SPLX = GX0 - 0.5                                                       # servo's spline face (inboard of the gear)
# goBILDA's 2000-0025-0002 as its file has it: case top at SPLX (the spline's 4.1 mm reach 3.6 mm into the gear), body
# 40 mm inboard, the tabs 10.3-12.8 mm in from the top, their M4 holes 48 x 10 mm apart.
sv = box(xr(SPLX), xr(SPLX - 40.0), SV_Y - 10, SV_Y + 10, SV_Z - 30.0, SV_Z + 10.0)
sv = sv.union(box(xr(SPLX - 10.3), xr(SPLX - 12.8), SV_Y - 10, SV_Y + 10, SV_Z - 37.2, SV_Z + 17.2))
for dy in (-5.0, 5.0):
    for dz in (-34.0, 14.0): sv = sv.cut(cyl("x", (0, SV_Y + dy, SV_Z + dz), 4.5, xr(SPLX - 10), xr(SPLX - 13.1)))
sv = sv.union(cyl("x", (0, SV_Y, SV_Z), 5.9, xr(SPLX), xr(SPLX + 4.1)))      # its H25T spline, M3 tapped in the centre
part(fixed, "extractor_servo (goBILDA 2000-0025-0002, Torque)", sv, BLACK, "buy")

# The servo's gear turns the opposite way to the shaft: its tab (r 20-38 mm) swings from TAB_D (down) to TAB_D - STOW
# (stowed), clear of the mesh (about 260-340 deg), from pointing forward and from the roller shaft, and meets a stop block 1 deg past each end.
TAB_HALF, TAB_D = 7.0, 205.0
sgear = cyl("x", (0, SV_Y, SV_Z), 2 * GEAR_R + 3, xr(GX0), xr(GX1)).union(sector_at(SV_Y, SV_Z, TAB_D - TAB_HALF, TAB_D + TAB_HALF, 20, 38, xr(GX0), xr(GX1)))
sgear = sgear.cut(cyl("x", (0, SV_Y, SV_Z), 6.2, xr(GX0) + 0.1, xr(GX0 + 4.1)))   # the H25T spline's socket (print it to fit, or ream)
part(fixed, "servo_gear (print: module 1.5, 34T, on the servo's H25T spline, an M3 screw in its centre; with the stop tab)", sgear, BLUE, "print")
# One printed bracket, the hard stops part of it:
# - a base on the right upright's front flange (two M4 into nuts inside the channel);
# - a frame behind the servo's tabs, its window round the servo's body (four M4 through the tabs into heat-set inserts:
#   a nut there would touch the body);
# - an arm under each hard stop, outside the tab's swing (r 20-38 mm, 52-212 deg about the spline).
FR0, FR1 = SPLX - 21.8, SPLX - 12.8     # the frame: 9 mm, inboard of the tabs (room for 8 mm heat-set inserts)
stops = {nm: sector_at(SV_Y, SV_Z, a0, a1, 30, 40, xr(GX0), xr(GX1 + 4)) for nm, a0, a1 in
         (("down", TAB_D + TAB_HALF + 1, TAB_D + TAB_HALF + 13), ("stowed", TAB_D - STOW - TAB_HALF - 13, TAB_D - STOW - TAB_HALF - 1))}
sb = box(xr(124.0), xr(FR1), SV_Y - 23, SV_Y + 17, FACE, FACE + 5.6)                          # base, on the upright's front
sb = sb.union(box(xr(WEB_X + 0.2), xr(WEB_X + 5.2), SV_Y - 23, SV_Y + 17, FACE - 17.0, FACE + 5.6))   # foot, on its web
sb = sb.union(box(xr(FR0), xr(FR1), SV_Y - 15, SV_Y + 36, FACE, SV_Z + 34))                  # frame
sb = sb.cut(box(xr(FR0) - 1, xr(FR1) + 1, SV_Y - 20.0, SV_Y + 10.6, SV_Z - 30.6, SV_Z + 10.6))  # its window, open on the low side: the floated roller passes there
sb = sb.union(box(xr(FR1), xr(GX1 + 4), SV_Y - 32, SV_Y - 22, FACE, FACE + 5.6)).union(stops["down"])          # down stop's arm
sb = sb.union(box(xr(FR1), xr(GX1 + 4), SV_Y + 22, SV_Y + 36, SV_Z + 18, SV_Z + 34)).union(stops["stowed"])    # stowed stop's arm
part(fixed, "extractor_servo_bracket (print; the hard stops are part of it)", sb, BLUE, "print")

# ---- the Rigid V's corner plates (optional) ----
for s, f, sg in (("R", xr, -1), ("L", xl, 1)):
    a, b = (f(V_IN), V_ROOT), (f(V_OUT), V_TIP)
    dx, dz = b[0] - a[0], b[1] - a[1]; L = math.hypot(dx, dz)
    pl = cq.Workplane("XY").box(L, V_TOP - 0.25 * IN, 3.175, centered=(False, False, True))      # length along x, height along y
    pl = pl.rotate((0, 0, 0), (0, 1, 0), math.degrees(math.atan2(-dz, dx))).translate((a[0], F + 0.25 * IN, a[1]))
    tab = box(f(V_IN), f(V_IN + 3.175), -125.0, -86.0, V_ROOT - 24, V_ROOT + 2)
    part(vee, f"rigid_v_plate_{s} (1/8 in aluminium, optional)", pl.union(tab), ALU, "cut")

# ---- fasteners: every screw, nut and insert (cad/fasteners.py picks the goBILDA length and drills the holes) ----
IN_ = {"R": (1, 0, 0), "L": (-1, 0, 0)}          # inboard, along x
OUT_ = {"R": (-1, 0, 0), "L": (1, 0, 0)}
for s, f in (("R", xr), ("L", xl)):
    # side plate to its four standoffs, and the chassis rail to the standoffs' other ends (goBILDA standoffs: tapped through)
    heads = [(f(PLATE_IN + PLATE_T), y, z) for y in A.STANDOFF_Y for z in A.STANDOFF_Z]
    FA.drill(fixed, [f"side_plate_{s}"], FA.bolt(fixed, f"plate_standoff_{s}", "side plate to its standoffs", heads, IN_[s], PLATE_T,
                                                  nut=False, tapped=8, into=f"^standoff_{s}", through=(f"side_plate_{s}",)))
    heads = [(f(A.RAIL_OUT - 2.5), y, z) for y in A.STANDOFF_Y for z in A.STANDOFF_Z]
    FA.bolt(fixed, f"rail_standoff_{s}", "chassis rail to the standoffs", heads, OUT_[s], 2.5, nut=False, tapped=8, into=f"^standoff_{s}", through=("mentor: ",),
            service="from inside the rail: the transfer's lane servo (right) or feeder (left) comes out first")
    # the float stop, by its tail, to the side plate
    FA.drill(fixed, [f"float_stop_{s}", f"side_plate_{s}"], FA.bolt(fixed, f"float_stop_{s}", "float stop to the side plate", [(f(OUT1), ROLL_Y - 24, ROLL_Z - 28)],
                                                                   IN_[s], 2 * PLATE_T, through=(f"float_stop_{s}", f"side_plate_{s}")))
    # the extractor's arm on its stub's end, and the cross shaft's end against the arm (a washer under each head)
    FA.bolt(hook, f"arm_stub_{s}", "extractor arm on its stub's end", [(f(EX_X - EX_T / 2 - 1.2), EXS_Y, EXS_Z)], OUT_[s], 1.2,
            nut=False, tapped=10, into=f"^extractor_stub_{s}", through=(f"arm_washer_{s}",))
    FA.bolt(hook, f"cross_end_{s}", "cross shaft's end against the extractor arm", [(f(EX_X + EX_T / 2 + 1.2), EX_FS[0], EX_FS[1])], IN_[s], 1.7,
            nut=False, tapped=10, into="^extractor_cross_shaft", through=(f"cross_washer_{s}",))
    # the Rigid V plate's tab to the side plate
    h = FA.bolt(vee, f"v_tab_{s}", "Rigid V plate's tab to the side plate", [(f(V_IN + 3.175), y, ROLL_Z + 3) for y in (-115.0, -96.0)],
                IN_[s], 2 * 3.175, through=(f"rigid_v_plate_{s}", f"side_plate_{s}"))
    FA.drill(vee, [f"rigid_v_plate_{s}"], h); FA.drill(fixed, [f"side_plate_{s}"], h)
# the odometry pods: the right one's mount from inside the rail, through its slot ends; the left one's up through the
# adapter's arm, and the adapter to the rail (nuts inside the rail)
FA.bolt(fixed, "pod_R", "right odometry pod to the rail", [(xr(A.RAIL_OUT - 2.5), y, z) for y in (-119.4, -87.4) for z in (-32.3, -0.3)],
        OUT_["R"], 2.7, nut=False, tapped=11, into="^odometry_pod_R", through=())
FA.drill(fixed, ["pod_adapter_L"], FA.bolt(fixed, "pod_L", "left odometry pod to its adapter", [(xl(d), y, 2.0) for d in (148.0, 180.0) for y in (-120.4, -88.4)],
                                          (0, 0, 1), 6.2, nut=False, tapped=11, into="^odometry_pod_L", through=("pod_adapter_L",)))
# (the rail's round holes at z -16.3: their heads stay out of the key's way to the pod's screws)
FA.drill(fixed, ["pod_adapter_L"], FA.bolt(fixed, "pod_adapter", "left pod's adapter to the rail", [(xl(A.RAIL_OUT + 6), y, -16.3) for y in (-111.5, -95.5)],
                                          IN_["L"], 6 + 2.5, through=("pod_adapter_L",)))
# the roller motor's face, through the carriage's counterbores (the heads under the pulley)
FA.drill(flt, ["motor_carriage_L"], FA.bolt(flt, "motor_face", "roller motor to its carriage", [(xl(FACE_X - 4.2), MOTOR_Y + dy, ROLL_Z + dz) for dy in (-8, 8) for dz in (-8, 8)],
        IN_["L"], 1.8, nut=False, tapped=10.5, into="^roller_motor_L", through=("motor_carriage_L",),
        service="take the float link (2 screws) and the motor's pulley off first; the key then goes through the side plate's service holes"))
# Service holes in the side plates, so a hex key reaches screws the plates would hide: the roller motor's four (left; take
# its pulley off first), the servo's two lower tab screws and its gear's centre screw (right; take the gear off before
# the two upper tab screws, which it covers).
ACCESS = {"L": [(MOTOR_Y + dy, ROLL_Z + dz) for dy in (-8, 8) for dz in (-8, 8)],
          "R": [(SV_Y + dy, SV_Z - 34) for dy in (-5, 5)] + [(SV_Y, SV_Z)]}
for s, f in (("R", xr), ("L", xl)):
    cut = None
    for y, z in ACCESS[s]:
        h = cyl("x", (0, y, z), 8.0, f(PLATE_IN) - (1 if s == "L" else -1), f(PLATE_IN + PLATE_T) + (1 if s == "L" else -1)); cut = h if cut is None else cut.union(h)
    FA.drill(fixed, [f"side_plate_{s}"], cut)
# the right float plate's guide: a shoulder screw through the float plate, its shoulder riding in the side plate's slot,
# the nut on the shoulder's end (it clamps the float plate only, so the plate slides)
FA.bolt(flt, "float_guide_R", "right float plate's guide in the side plate's slot", [(xr(OUT1), ROLL_Y + 40, ROLL_Z)], IN_["R"], 2 * PLATE_T + 0.3,
        through=("float_plate_R", "side_plate_R"), label="M4 shoulder screw, 5 mm x 6.5 mm shoulder")
# the float link to the carriage's bridge (heat-set inserts in the print)
FA.drill(flt, ["float_link_L"], FA.bolt(flt, "link_bridge", "float link to the carriage's bridge", [(xl(OUT1), BR0 + 8, ROLL_Z + dz) for dz in (-6, 6)],
                                       IN_["L"], PLATE_T, nut=False, tapped=8, into="^motor_carriage_L", through=("float_link_L",)))
# the servo bracket's foot to the right upright's web slots, and the carriage's shoulder screws in the left one's: nuts
# inside the channels
FA.drill(fixed, ["extractor_servo_bracket"], FA.bolt(fixed, "servo_bracket", "servo bracket to the right upright's web", [(xr(WEB_X + 5.2), y, WEB_Z) for y in (-7.3, 16.7)],
                                                    IN_["R"], 5.0 + 0.2 + 2.5, through=("extractor_servo_bracket",)))
FA.bolt(fixed, "carriage_guides", "roller carriage's slide, on the left upright's web", [(xl(WEB_X + 4.2), y, WEB_Z) for y in GUIDE_Y],
        IN_["L"], 4.0 + 0.2 + 2.5, through=("motor_carriage_L",), label="M4 shoulder screw, 5 mm x 4 mm shoulder, low head")
# the servo by its tabs to the bracket's frame (heat-set inserts in the frame)
FA.drill(fixed, ["extractor_servo_bracket"], FA.bolt(fixed, "servo_tabs", "servo to its bracket", [(xr(SPLX - 10.3), SV_Y + dy, SV_Z + dz) for dy in (-5, 5) for dz in (-34, 14)],
                                                    IN_["R"], 2.5, nut=False, tapped=8, into="^extractor_servo_bracket", through=("extractor_servo ",),
                                                    service="the servo gear covers the upper two: take it off first (its screw through the side plate's service hole)"))
# the servo gear on its spline: an M3 into the spline's tapped centre
FA.drill(fixed, ["servo_gear"], FA.bolt(fixed, "servo_gear", "servo gear on the spline", [(xr(GX1), SV_Y, SV_Z)], IN_["R"], GX1 - GX0 - 3.6, d=3,
                                       nut=False, tapped=7, into="^extractor_servo ", through=("servo_gear",)))

GROUPS = (("chassis, side plates and servo (fixed)", "fixed", fixed), ("roller and its motor (float up to 1.3 in)", "float", flt),
          ("FLOWER extractor (turns about its shaft, 0 down to STOW)", "hook", hook), ("rigid V plates (optional)", "vee", vee))
if __name__ == "__main__":
    out = os.path.dirname(os.path.abspath(__file__)); os.makedirs(out + "/stl", exist_ok=True)
    assy = cq.Assembly(name="DHS unified front")
    for title, g, d_ in GROUPS:
        sub = cq.Assembly(name=title)
        for n, (wp, col, kind) in d_.items(): sub.add(wp, name=n, color=cq.Color(*col))
        assy.add(sub)
    assy.save(out + "/dhs-intake-b.step")
    mesh = {}
    for title, grp, d_ in GROUPS:
        for n, (wp, col, kind) in d_.items():
            shp = wp.val() if len(wp.vals()) == 1 else cq.Compound.makeCompound(wp.vals())
            v, f_ = shp.tessellate(0.2, 0.3)
            mesh[n] = dict(grp=grp, col=col, kind=kind, v=[(p.x, p.y, p.z) for p in v], f=f_)
            if kind == "print": shp.exportStl(f"{out}/stl/{n.split(' ')[0]}.stl", 0.05, 0.2)
    if os.environ.get("MESH_OUT"):
        import pickle; pickle.dump(mesh, open(os.environ["MESH_OUT"], "wb"))
    print(len(fixed), "fixed,", len(flt), "float,", len(hook), "hook,", len(vee), "V parts")
