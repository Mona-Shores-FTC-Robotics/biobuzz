"""The unified front, in the robot CAD's frame (mm): x across (right is -x), y up, z forward.
A 13.8 in roller 1.0 in in front of the face that floats straight up 1.3 in so a NECTAR passes under it; the FLOWER
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
from build import C, F, FACE, IN, AX_Y, AX_ZF, AX_ZR, RAIL_OUT, PLATE_IN, PLATE_T, BORE, M3, M4, xr, xl, box, cyl, clip, ramp_block

# ---- the numbers the intake study fixed ----
ROLL_Y = F + 2.4 * IN + 24.0           # roller axle at rest: bottom of the 48 mm wheels 2.4 in up
ROLL_Z = FACE + 1.0 * IN               # ...1.0 in in front of the face (its front 1.94 in out)
ROLL_HALF = 6.9 * IN                   # 13.8 in of wheels
FLOAT = 1.3 * IN                       # the roller rises this far: 0.85 in passes a NECTAR with 0.4 in of give, 1.3 with none
TRANSFER_GAP = (2.35 * IN, 2.9 * IN)   # gap in the roller, left of centre, for the transfer's lane pulley
# Roller drive, 1:1: two goBILDA 3417-4008-0024 pulleys (24T HTD5, 8mm REX bore) on the shortest 9 mm belt, the
# 3412 Series 55-tooth (275 mm). Its centres: (275 - 24 * 5) / 2 = 77.5 mm, so the motor sits 77.5 mm over the roller.
# The motor rides on the roller's carriage, so the belt never changes length as the roller floats.
PITCH_D24 = 24 * 5 / math.pi           # 38.2 mm
MOTOR_C = 77.5
MOTOR_Y = ROLL_Y + MOTOR_C
EXS_Y, EXS_Z = F + 4.5 * IN, FACE + 2.4 * IN     # the extractor's shaft: above any NECTAR, ahead of the roller
EX_X, EX_T = 1.8 * IN, 3.175           # extractor arms, from centre; 1/8 in aluminium
EX_ZB = FACE + 1.94 * IN + 2.5 * IN    # the FLOWER block's back edge, down
EX_FS = (F + A.ROD_Z, EX_ZB + A.ROD_X) # the block's cross shaft (y, z)
STOW = float(os.environ.get("STOW", "150"))      # degrees from down to stowed
V_IN, V_OUT, V_TOP = PLATE_IN + PLATE_T, 9.0 * IN - 4.0, 4.0 * IN    # Rigid V plates: from the side plates' outer face...
V_ROOT, V_TIP = ROLL_Z + 14, FACE + 2.8 * IN                          # ...at their front edge, forward to 2.8 in (18 in start)
OUT0, OUT1 = PLATE_IN + PLATE_T, PLATE_IN + 2 * PLATE_T              # the float plates, outboard of the side plates

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
    part(fixed, f"float_stop_{s} (print, under the float plate)", box(f(OUT0), f(OUT1), ROLL_Y - 19, ROLL_Y - 13, ROLL_Z - 10, ROLL_Z + 10), BLUE, "print")

# ---- the floating roller and its carriage ----
part(flt, "roller_shaft (8mm REX, 400 mm)", cyl("x", (0, ROLL_Y, ROLL_Z), 8.0, xr(OUT1 + 3), xl(OUT1 + 3)), STEEL, "buy")
_edges = [-ROLL_HALF, -TRANSFER_GAP[1], -TRANSFER_GAP[0], ROLL_HALF]   # + = right
for i in range(0, len(_edges), 2):
    lo, hi = _edges[i], _edges[i + 1]
    part(flt, f"roller_wheels_{i // 2} (48 mm gecko)", cyl("x", (0, ROLL_Y, ROLL_Z), 48.0, C - hi, C - lo), (0.35, 0.66, 0.31), "buy")
for s, f in (("R", xr), ("L", xl)):
    part(flt, f"roller_bearing_{s} (goBILDA 1611-0514-4008, 8mm REX bore)", cyl("x", (0, ROLL_Y, ROLL_Z), 14.0, f(OUT0), f(OUT1 + 1.2)), BRASS, "buy")
fr = link((ROLL_Y, ROLL_Z), (ROLL_Y + 40, ROLL_Z), 24.0, xr(OUT0), xr(OUT1))        # right: outboard, slides on the side plate
fr = fr.cut(cyl("x", (0, ROLL_Y, ROLL_Z), 14.0, xr(OUT1) - 1, xr(OUT0) + 1)).cut(cyl("x", (0, ROLL_Y + 40, ROLL_Z), M4, xr(OUT1) - 1, xr(OUT0) + 1))
part(flt, "float_plate_R (1/8 in aluminium)", fr, ALU, "cut")
part(flt, "float_guide_R (M4 shoulder screw, 5 mm shoulder, in the side plate's slot)", cyl("x", (0, ROLL_Y + 40, ROLL_Z), 5.0, xr(PLATE_IN - 3), xr(OUT1 + 3)), STEEL, "buy")
# Belt drive inside the left plate, between the roller's end (6.9 in) and the plate (7.56 in).
PUL0, PUL1 = ROLL_HALF + 1.5, PLATE_IN - 2.0
part(flt, "roller_pulley_L (goBILDA 3417-4008-0024, 24T HTD5)", cyl("x", (0, ROLL_Y, ROLL_Z), PITCH_D24, xl(PUL0), xl(PUL1)), BLACK, "buy")
part(flt, "motor_pulley_L (goBILDA 3417-4008-0024, 24T HTD5)", cyl("x", (0, MOTOR_Y, ROLL_Z), PITCH_D24, xl(PUL0), xl(PUL1)), BLACK, "buy")
R = PITCH_D24 / 2
part(flt, "roller_belt_L (goBILDA 3412 Series, 9 mm, 55T, 275 mm)", box(xl(PUL0 + 1.5), xl(PUL1 - 1.5), ROLL_Y - R - 3, MOTOR_Y + R + 3, ROLL_Z - R - 3, ROLL_Z + R + 3)
     .cut(box(xl(PUL0), xl(PUL1), ROLL_Y - R, MOTOR_Y + R, ROLL_Z - R, ROLL_Z + R)), BLACK, "buy")
FACE_X = PUL0 - 1.0                     # the motor's mounting face, just inboard of its pulley
part(flt, "roller_motor_L (goBILDA 5203-2402-0005, 1150 RPM)", cyl("x", (0, MOTOR_Y, ROLL_Z), 37.0, xl(FACE_X - 7), xl(FACE_X - 127)), BLACK, "buy")
part(flt, "motor_shaft_L (the motor's own 24 mm 8mm-REX output shaft)", cyl("x", (0, MOTOR_Y, ROLL_Z), 8.0, xl(FACE_X - 7), xl(FACE_X + 20)), STEEL, "buy")
# Left carriage: the motor's face plate and cradle (sliding on the left upright's front face on two shoulder screws in
# slots), a bridge over the motor pulley and the side plate's top edge, and an outboard link plate down to the roller's
# left bearing. Roller, motor and both pulleys move as one.
BR0, BR1 = MOTOR_Y + 24, MOTOR_Y + 40
GUIDE_Y = (32.8, 40.8)                  # the upright's front-face holes above the motor (low-head shoulder screws)
mb = box(xl(FACE_X - 6), xl(FACE_X), MOTOR_Y - 24, BR1, FACE, ROLL_Z + 24)                    # face plate
mb = mb.union(box(xl(124.0), xl(FACE_X - 6), -28.0, 46.0, FACE, FACE + 4.0))                 # cradle on the upright's face
mb = mb.union(box(xl(FACE_X - 6), xl(OUT1), BR0, BR1, ROLL_Z - 12, ROLL_Z + 12))               # bridge
mb = mb.cut(box(xl(FACE_X - 7), xl(FACE_X + 1), MOTOR_Y - 7, MOTOR_Y + 7, ROLL_Z - 7, ROLL_Z + 7))
for dz in (-8, 8):
    for dy in (-8, 8): mb = mb.cut(cyl("x", (0, MOTOR_Y + dy, ROLL_Z + dz), 3.4, xl(FACE_X - 7), xl(FACE_X + 1)))
for y in GUIDE_Y: mb = mb.cut(cyl("z", (C + 128.0, y - FLOAT, 0), M4 + 0.5, FACE - 1, FACE + 8).union(cyl("z", (C + 128.0, y, 0), M4 + 0.5, FACE - 1, FACE + 8))
                                .union(box(C + 128.0 - 2.4, C + 128.0 + 2.4, y - FLOAT, y, FACE - 1, FACE + 8)))
part(flt, "motor_carriage_L (print)", mb, BLUE, "print")
fl = link((ROLL_Y, ROLL_Z), (BR1 - 12, ROLL_Z), 24.0, xl(OUT0), xl(OUT1))
fl = fl.cut(cyl("x", (0, ROLL_Y, ROLL_Z), 14.0, xl(OUT0) - 1, xl(OUT1) + 1))
part(flt, "float_link_L (1/8 in aluminium, bolts to the carriage's bridge)", fl, ALU, "cut")
part(fixed, "carriage_guides_L (2 x M4 shoulder screws in the left upright's front face)",
     cyl("z", (C + 128.0, GUIDE_Y[0], 0), 5.0, FACE - 4, FACE + 6.5).union(cyl("z", (C + 128.0, GUIDE_Y[1], 0), 5.0, FACE - 4, FACE + 6.5)), STEEL, "buy")

# ---- the FLOWER extractor: its own fixed shaft at X 9.9, Z 4.5 in ----
# Two 1/8 in aluminium arms, 1.8 in each side of centre, clamped to an 8 mm REX shaft that turns in bearings in the side
# plates. A short cross shaft carries the FLOWER block, its back edge 2.5 in ahead of the roller's front. It turns 0 (down)
# to STOW (folded up in front of the robot). A servo over the roller on the right drives the shaft through a 1:1 printed
# gear pair (module 1.5, 34 teeth, 51 mm centres) in the gap between the roller's right end and the side plate; the hard
# stops act on a tab on the servo's gear.
part(hook, "extractor_shaft (8mm REX, 392 mm)", cyl("x", (0, EXS_Y, EXS_Z), 8.0, xr(PLATE_IN + PLATE_T + 2), xl(PLATE_IN + PLATE_T + 2)), STEEL, "buy")
for s, f in (("R", xr), ("L", xl)):
    part(fixed, f"extractor_bearing_{s} (goBILDA 1611-0514-4008, 8mm REX bore)", cyl("x", (0, EXS_Y, EXS_Z), 14.0, f(PLATE_IN - 1.2), f(PLATE_IN + PLATE_T)), BRASS, "buy")
    xa, xb = f(EX_X - EX_T / 2), f(EX_X + EX_T / 2)
    arm = link((EXS_Y, EXS_Z), EX_FS, 18.0, xa, xb)
    arm = arm.cut(cyl("x", (0, EXS_Y, EXS_Z), BORE, min(xa, xb) - 1, max(xa, xb) + 1)).cut(cyl("x", (0, EX_FS[0], EX_FS[1]), BORE, min(xa, xb) - 1, max(xa, xb) + 1))
    part(hook, f"extractor_arm_{s} (1/8 in aluminium, REX hole at the shaft)", arm, ALU, "cut")
    for d0 in (EX_X - EX_T / 2 - 8.5, EX_X + EX_T / 2 + 0.5):
        part(hook, f"arm_collar_{s}_{d0:.0f} (8mm REX clamping collar)", cyl("x", (0, EXS_Y, EXS_Z), 21.0, f(d0), f(d0 + 8)), STEEL, "buy")
part(hook, "extractor_cross_shaft (8mm REX, 104 mm)", cyl("x", (0, EX_FS[0], EX_FS[1]), 8.0, xr(EX_X + 6), xl(EX_X + 6)), STEEL, "buy")
part(hook, "flower_block (print)", ramp_block().translate((0, 0, EX_ZB - A.ZB)), (0.69, 0.42, 0.85), "print")
for s, f in (("R", xr), ("L", xl)):
    part(hook, f"block_collar_{s} (8mm REX clamping collar)", cyl("x", (0, EX_FS[0], EX_FS[1]), 21.0, f(1.36 * IN + 0.5), f(1.36 * IN + 8.5)), STEEL, "buy")
GEAR_R, GEAR_W = 25.5, 6.0
GX0, GX1 = ROLL_HALF + 2.0, ROLL_HALF + 2.0 + GEAR_W                   # gear plane, right of the roller's end
# A sector gear: teeth only where they mesh between down and stowed, so nothing sticks out ahead when stowed.
SV_Y, SV_Z = EXS_Y + 44.45, EXS_Z - 25.0                               # the servo's spline: 51 mm from the shaft, up and back
MESH_Q = math.degrees(math.atan2(SV_Y - EXS_Y, SV_Z - EXS_Z))         # the mesh, seen from the shaft (about 120 deg)
egear = cyl("x", (0, EXS_Y, EXS_Z), 20.0, xr(GX0), xr(GX1)).union(sector_at(EXS_Y, EXS_Z, MESH_Q - STOW - 12, MESH_Q + 12, 10, GEAR_R + 1.5, xr(GX0), xr(GX1)))
part(hook, "extractor_gear_R (print: module 1.5, 34T sector, clamped to the shaft)", egear.cut(cyl("x", (0, EXS_Y, EXS_Z), BORE, xr(GX1) - 1, xr(GX0) + 1)), BLUE, "print")
SPLX = GX0 - 0.5                                                       # servo's spline face (inboard of the gear)
sv = box(xr(SPLX - 2), xr(SPLX - 38.6), SV_Y - 10, SV_Y + 10, SV_Z - 30.4, SV_Z + 10.2)
sv = sv.union(box(xr(SPLX - 8), xr(SPLX - 10.5), SV_Y - 10, SV_Y + 10, SV_Z - 37.2, SV_Z + 17))
part(fixed, "extractor_servo (goBILDA 2000-0025-0002, Torque)", sv, BLACK, "buy")

# The servo's gear turns the opposite way to the shaft: its tab (r 20-38 mm) swings from TAB_D (down) to TAB_D - STOW
# (stowed), clear of the mesh (about 260-340 deg), from pointing forward and from the roller shaft, and meets a stop block 1 deg past each end.
TAB_HALF, TAB_D = 7.0, 205.0
sgear = cyl("x", (0, SV_Y, SV_Z), 2 * GEAR_R + 3, xr(GX0), xr(GX1)).union(sector_at(SV_Y, SV_Z, TAB_D - TAB_HALF, TAB_D + TAB_HALF, 20, 38, xr(GX0), xr(GX1)))
part(fixed, "servo_gear (print: module 1.5, 34T, on the servo's spline; with the stop tab)", sgear, BLUE, "print")
for nm, a0, a1 in (("down", TAB_D + TAB_HALF + 1, TAB_D + TAB_HALF + 13), ("stowed", TAB_D - STOW - TAB_HALF - 13, TAB_D - STOW - TAB_HALF - 1)):
    part(fixed, f"extractor_stop_{nm} (print, on the servo bracket)", sector_at(SV_Y, SV_Z, a0, a1, 30, 40, xr(GX0), xr(GX1 + 4)), BLUE, "print")
sb = box(xr(124.0), xr(SPLX - 2), SV_Y - 23, SV_Y + 17, FACE, SV_Z - 30.4)               # bolts to the right upright's front face...
sb = sb.union(box(xr(SPLX - 38.6), xr(SPLX - 2), SV_Y - 14, SV_Y - 10, FACE, SV_Z + 10.2))  # ...and cradles the servo
for y in (-7.2, 8.8): sb = sb.cut(cyl("z", (C - 128.0, y, 0), M4, FACE - 1, SV_Z))
part(fixed, "extractor_servo_bracket (print)", sb, BLUE, "print")

# ---- the Rigid V's corner plates (optional) ----
for s, f, sg in (("R", xr, -1), ("L", xl, 1)):
    a, b = (f(V_IN), V_ROOT), (f(V_OUT), V_TIP)
    dx, dz = b[0] - a[0], b[1] - a[1]; L = math.hypot(dx, dz)
    pl = cq.Workplane("XY").box(L, V_TOP - 0.25 * IN, 3.175, centered=(False, False, True))      # length along x, height along y
    pl = pl.rotate((0, 0, 0), (0, 1, 0), math.degrees(math.atan2(-dz, dx))).translate((a[0], F + 0.25 * IN, a[1]))
    tab = box(f(V_IN), f(V_IN + 3.175), -125.0, -86.0, V_ROOT - 24, V_ROOT + 2)
    part(vee, f"rigid_v_plate_{s} (1/8 in aluminium, optional)", pl.union(tab), ALU, "cut")

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
