"""Intake option (b) from doc/intake-design.md, in the robot CAD's frame (mm): x across (right is -x), y up, z forward.
A 14 in roller 1.0 in in front of the face, carried by the outer wheel plates (extended up and forward); the ramp
hook hung from a hinge 6 in up on the right plate, folding over the top to 150 degrees; the roller's motor and belt
on the left; the hook's servo inboard of the right plate; and the Rigid V's two corner plates as an optional group.
The wheel-plate standoffs, 80 mm wheel shafts, bearings and odometry pods are cad/robot-addons'.

    python3 cad/intake-b/build.py      # writes dhs-intake-b.step and stl/ next to this file
"""
import math, os, sys
import cadquery as cq
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "robot-addons"))
import build as A                      # the robot frame, helpers and the chassis add-ons
from build import C, F, FACE, IN, AX_Y, AX_ZF, AX_ZR, RAIL_OUT, PLATE_IN, PLATE_T, BORE, M3, M4, xr, xl, box, cyl, clip, ramp_block

# ---- the numbers the intake study fixed ----
ROLL_Y = F + 2.4 * IN + 24.0           # roller axle: bottom of the 48 mm wheels 2.4 in up
ROLL_Z = FACE + 1.0 * IN               # ...1.0 in in front of the face (its front 1.94 in out)
ROLL_HALF = 6.9 * IN                   # 13.8 in of wheels: 0.1 in less than 14 so the hook's hub clears the roller's end
HINGE_UP = float(os.environ.get("HINGE_UP", "6.0"))
HY, HZ = F + HINGE_UP * IN, FACE + 1.0 * IN   # hook hinge: 6 in up, over the roller's axle
# Roller drive, 1:1: two goBILDA 3417-4008-0024 pulleys (24T HTD5, 8mm REX bore) on the shortest 9 mm belt, the
# 3412 Series 55-tooth (275 mm). Its centres: (275 - 24 * 5) / 2 = 77.5 mm, so the motor sits 77.5 mm over the roller.
# The 2:3 step down (3417-4008-0016 on the motor) on the same belt needs about 87.3 mm: the bracket's slots give both.
PITCH_D24 = 24 * 5 / math.pi           # 38.2 mm
MOTOR_C = 77.5
MOTOR_Y = ROLL_Y + MOTOR_C
EX_X, EX_T, GEAR_W = 1.8 * IN, 3.175, 6.0
ROLL_GAPS = ((-(EX_X + 5.0), -(EX_X - 5.0)), (EX_X - 5.0, EX_X + EX_T / 2 + 3.0 + GEAR_W + 5.0))   # roller gaps for the extractor (+ = right)
ARM = 7.15 * IN                        # the hook's arm, from centre: its 14 mm hub clears the plate (7.56 in) and the servo
ZB = FACE + 7.48 * IN                  # FLOWER block's back edge with the hook down (24 in overall)
V_IN, V_OUT, V_TOP = PLATE_IN + PLATE_T, 9.0 * IN - 4.0, 4.0 * IN    # Rigid V plates: from the side plates' outer face...
V_ROOT, V_TIP = ROLL_Z + 16 - 6, FACE + 2.8 * IN                        # ...at their front edge, forward to 2.8 in (18 in start)
FS_Y, FS_Z = F + A.ROD_Z, ZB + A.ROD_X # front shaft

fixed, hook, vee = {}, {}, {}
def part(d, name, wp, col, kind): d[name] = (wp, col, kind)
BLUE, ALU, STEEL, BRASS, POLY, BLACK = (0.18, 0.37, 0.62), (0.75, 0.78, 0.82), (0.8, 0.82, 0.85), (0.85, 0.75, 0.3), (0.6, 0.78, 0.96), (0.13, 0.15, 0.17)

def rod(p0, p1, d=8.0):
    p0, p1 = cq.Vector(*p0), cq.Vector(*p1)
    return cq.Workplane().add(cq.Solid.makeCylinder(d / 2, (p1 - p0).Length, p0, (p1 - p0).normalized()))

# ---- side plates: the outer wheel plates, now also the roller's and the hook's ----
def side_plate(right):
    f = xr if right else xl
    x0, x1 = f(PLATE_IN), f(PLATE_IN + PLATE_T)
    top = MOTOR_Y + 16
    pts = [(-127.0, AX_ZR - 24), (-79.0, AX_ZR - 24), (-79.0, FACE - 40), (top, FACE - 18), (top, HZ + 16),
           (ROLL_Y + 30, ROLL_Z + 16), (ROLL_Y, ROLL_Z + 16), (-127.0, ROLL_Z + 16)]
    p = cq.Workplane("YZ").polyline(pts).close().extrude(PLATE_T).translate((min(x0, x1), 0, 0))
    holes = [(AX_Y, AX_ZF), (AX_Y, AX_ZR), (ROLL_Y, ROLL_Z)] + ([] if right else [(MOTOR_Y, HZ)])   # wheels, roller, hinge / motor
    for y, z in holes: p = p.cut(cyl("x", (0, y, z), 14.0, min(x0, x1) - 1, max(x0, x1) + 1))
    for y in A.STANDOFF_Y:
        for z in A.STANDOFF_Z: p = p.cut(cyl("x", (0, y, z), M4, min(x0, x1) - 1, max(x0, x1) + 1))
    for y, z in ((-110.0, ROLL_Z - 2), (-60.0, ROLL_Z - 2)): p = p.cut(cyl("x", (0, y, z), M4, min(x0, x1) - 1, max(x0, x1) + 1))  # V plate tab
    return p
part(fixed, "side_plate_R (1/8 in aluminium)", side_plate(True), ALU, "cut")
part(fixed, "side_plate_L (1/8 in aluminium)", side_plate(False), ALU, "cut")
for n, (wp, col, kind) in A.parts.items():            # keep the chassis add-ons, minus the old plates, hinge and servo
    if n.startswith(("outer_plate", "hinge_", "servo", "bearing_R_hinge")): continue
    part(fixed, n, wp, col, kind)

# ---- the roller ----
part(fixed, "roller_shaft (8mm REX, 400 mm)", cyl("x", (0, ROLL_Y, ROLL_Z), 8.0, xr(PLATE_IN + PLATE_T + 4), xl(PLATE_IN + PLATE_T + 4)), STEEL, "buy")
_edges = [-ROLL_HALF, ROLL_GAPS[0][0], ROLL_GAPS[0][1], ROLL_GAPS[1][0], ROLL_GAPS[1][1], ROLL_HALF]   # + = right
for i in range(0, 6, 2):
    lo, hi = _edges[i], _edges[i + 1]
    part(fixed, f"roller_wheels_{i // 2} (48 mm gecko)", cyl("x", (0, ROLL_Y, ROLL_Z), 48.0, C - hi, C - lo), (0.35, 0.66, 0.31), "buy")
for s, f in (("R", xr), ("L", xl)):
    part(fixed, f"roller_bearing_{s} (goBILDA 1611-0514-4008, 8mm REX bore)", cyl("x", (0, ROLL_Y, ROLL_Z), 14.0, f(PLATE_IN), f(PLATE_IN + PLATE_T + 1.2)), BRASS, "buy")
# Belt drive inside the left plate, between the roller's end (6.9 in) and the plate (7.56 in): outside the plate the
# Rigid V's left plate would cut through the pulley and belt.
PUL0, PUL1 = ROLL_HALF + 1.5, PLATE_IN - 2.0
part(fixed, "roller_pulley_L (goBILDA 3417-4008-0024, 24T HTD5)", cyl("x", (0, ROLL_Y, ROLL_Z), PITCH_D24, xl(PUL0), xl(PUL1)), BLACK, "buy")
part(fixed, "motor_pulley_L (goBILDA 3417-4008-0024, 24T HTD5)", cyl("x", (0, MOTOR_Y, HZ), PITCH_D24, xl(PUL0), xl(PUL1)), BLACK, "buy")
R = PITCH_D24 / 2
part(fixed, "roller_belt_L (goBILDA 3412 Series, 9 mm, 55T, 275 mm)", box(xl(PUL0 + 1.5), xl(PUL1 - 1.5), ROLL_Y - R - 3, MOTOR_Y + R + 3, HZ - R - 3, HZ + R + 3)
     .cut(box(xl(PUL0), xl(PUL1), ROLL_Y - R, MOTOR_Y + R, HZ - R, HZ + R)), BLACK, "buy")
FACE_X = PUL0 - 1.0                     # the motor's mounting face, just inboard of its pulley
part(fixed, "roller_motor_L (goBILDA 5203-2402-0005, 1150 RPM)", cyl("x", (0, MOTOR_Y, HZ), 37.0, xl(FACE_X - 7), xl(FACE_X - 127)), BLACK, "buy")
part(fixed, "motor_shaft_L (the motor's own 24 mm 8mm-REX output shaft)", cyl("x", (0, MOTOR_Y, HZ), 8.0, xl(FACE_X - 7), xl(FACE_X + 20)), STEEL, "buy")
mb = box(xl(FACE_X - 6), xl(FACE_X), MOTOR_Y - 24, MOTOR_Y + 34, FACE, HZ + 24)          # face plate: the motor bolts to it
mb = mb.union(box(xl(124.0), xl(FACE_X - 6), MOTOR_Y - 24, MOTOR_Y + 34, FACE, HZ - 18.5))  # ...and a cradle back to the upright
mb = mb.cut(box(xl(FACE_X - 7), xl(FACE_X + 1), MOTOR_Y - 1, MOTOR_Y + 1, HZ - 1, HZ + 1))
for dz in (-8, 8):                     # slots: the motor 77.5 mm over the roller (1:1) or 87.3 mm (2:3)
    for dy in (-8, 8):
        mb = mb.cut(box(xl(FACE_X - 7), xl(FACE_X + 1), MOTOR_Y + dy - 2.15, MOTOR_Y + dy + 9.8 + 2.15, HZ + dz - 2.15, HZ + dz + 2.15))
mb = mb.cut(box(xl(FACE_X - 7), xl(FACE_X + 1), MOTOR_Y - 7, MOTOR_Y + 9.8 + 7, HZ - 7, HZ + 7))
for y in (-7.2, 8.8): mb = mb.cut(cyl("z", (C + 128.0, y, 0), M4, FACE - 1, HZ))
part(fixed, "motor_bracket_L (print)", mb, BLUE, "print")
part(fixed, "motor_bearing_L (goBILDA 1611-0514-4008, 8mm REX bore)", cyl("x", (0, MOTOR_Y, HZ), 14.0, xl(PLATE_IN), xl(PLATE_IN + PLATE_T + 1.2)), BRASS, "buy")
# ---- the FLOWER extractor (unified design, 6 Oct 2026): it pivots on the roller's own shaft ----
# Two 1/8 in aluminium arms, 1.8 in each side of centre, on round-bore bearings riding the roller's REX shaft in two
# gaps in the roller. A short cross shaft carries the FLOWER block, its back edge 2.5 in ahead of the roller's front.
# It turns 0 (down) to 125 deg (folded up over the roller). A servo above the roller on the right drives the right arm
# through a 1:1 printed gear pair (module 1.5, 34 teeth, 51 mm centres); the hard stops act on a tab on the servo's gear.
EX_X = 1.8 * IN                        # arms, from centre
EX_ZB = FACE + 1.94 * IN + 2.5 * IN    # the block's back edge, down
EX_FS = (F + A.ROD_Z, EX_ZB + A.ROD_X) # cross shaft (y, z)
EX_T = 3.175                           # arm plate
GEAR_R, GEAR_W = 25.5, 6.0             # pitch radius; face width
SV_Y, SV_Z = ROLL_Y + 2 * GEAR_R, ROLL_Z                       # the servo's spline: 51 mm over the roller's axle
GEAR_X0, GEAR_X1 = EX_X + EX_T / 2 + 3.0, EX_X + EX_T / 2 + 3.0 + GEAR_W   # gear plane, 3 mm outboard of the right arm (on a spacer)

def link(p0, p1, w, x0, x1):
    """A flat bar in a y-z plane between two points (y, z), rounded ends, from x0 to x1."""
    (y0, z0), (y1, z1) = p0, p1
    L = math.hypot(y1 - y0, z1 - z0); ang = math.degrees(math.atan2(y1 - y0, z1 - z0))
    bar = cq.Workplane("YZ").center(0, L / 2).rect(w, L).extrude(abs(x1 - x0))
    bar = bar.union(cq.Workplane("YZ").circle(w / 2).extrude(abs(x1 - x0))).union(cq.Workplane("YZ").center(0, L).circle(w / 2).extrude(abs(x1 - x0)))
    return bar.rotate((0, 0, 0), (1, 0, 0), -ang).translate((min(x0, x1), y0, z0))
for s, f in (("R", xr), ("L", xl)):
    xa, xb = f(EX_X - EX_T / 2), f(EX_X + EX_T / 2)
    arm = link((ROLL_Y, ROLL_Z), EX_FS, 18.0, xa, xb)
    arm = arm.cut(cyl("x", (0, ROLL_Y, ROLL_Z), 14.0, xa - 1 if xa < xb else xb - 1, max(xa, xb) + 1))
    arm = arm.cut(cyl("x", (0, EX_FS[0], EX_FS[1]), BORE, min(xa, xb) - 1, max(xa, xb) + 1))
    part(hook, f"extractor_arm_{s} (1/8 in aluminium)", arm, ALU, "cut")
    part(hook, f"extractor_bearing_{s} (goBILDA 1611-0514-0008, round 8 mm bore, rides the roller shaft)",
         cyl("x", (0, ROLL_Y, ROLL_Z), 14.0, f(EX_X - 2.5), f(EX_X + 2.5)), BRASS, "buy")
part(hook, "extractor_cross_shaft (8mm REX, 104 mm)", cyl("x", (0, EX_FS[0], EX_FS[1]), 8.0, xr(EX_X + 6), xl(EX_X + 6)), STEEL, "buy")
part(hook, "flower_block (print)", ramp_block().translate((0, 0, EX_ZB - A.ZB)), (0.69, 0.42, 0.85), "print")
for s, f in (("R", xr), ("L", xl)):
    part(hook, f"block_collar_{s} (8mm REX clamping collar)", cyl("x", (0, EX_FS[0], EX_FS[1]), 21.0, f(1.36 * IN + 0.5), f(1.36 * IN + 8.5)), STEEL, "buy")
part(hook, "arm_gear_R (print: module 1.5, 34T, bolted to the right arm)",
     cyl("x", (0, ROLL_Y, ROLL_Z), 2 * GEAR_R + 3, xr(GEAR_X0), xr(GEAR_X1)).union(cyl("x", (0, ROLL_Y, ROLL_Z), 22.0, xr(EX_X + EX_T / 2), xr(GEAR_X0))).cut(cyl("x", (0, ROLL_Y, ROLL_Z), 14.0, xr(GEAR_X0) + 1, xr(GEAR_X1) - 1)), BLUE, "print")

# The servo, its gear and the stops (fixed). The servo gear turns the opposite way; its tab (r 30-38 mm) swings from 200 deg
# (down) to 75 deg (stowed) about the spline, clear of the mesh (238-302 deg), and meets a stop block 1 deg past each end.
SPLX = GEAR_X1 + 0.5                   # servo's spline face
sv = box(xr(SPLX + 2), xr(SPLX + 40.6), SV_Y - 10.2, SV_Y + 30.6, SV_Z - 10, SV_Z + 10)
sv = sv.union(box(xr(SPLX + 8), xr(SPLX + 10.5), SV_Y - 17, SV_Y + 37.4, SV_Z - 10, SV_Z + 10))
part(fixed, "extractor_servo (goBILDA 2000-0025-0002, Torque)", sv, BLACK, "buy")
def sector_at(cy, cz, a0, a1, r0, r1, x0, x1):
    n = 12; ang = [math.radians(a0 + (a1 - a0) * i / n) for i in range(n + 1)]
    pts = [(cy + r1 * math.sin(q), cz + r1 * math.cos(q)) for q in ang] + [(cy + r0 * math.sin(q), cz + r0 * math.cos(q)) for q in reversed(ang)]
    return cq.Workplane("YZ").polyline(pts).close().extrude(abs(x1 - x0)).translate((min(x0, x1), 0, 0))
TAB_HALF = 7.0
sgear = cyl("x", (0, SV_Y, SV_Z), 2 * GEAR_R + 3, xr(GEAR_X0), xr(GEAR_X1)).union(sector_at(SV_Y, SV_Z, 200 - TAB_HALF, 200 + TAB_HALF, 20, 38, xr(GEAR_X0), xr(GEAR_X1)))
part(fixed, "servo_gear (print: module 1.5, 34T, on the servo's spline; with the stop tab)", sgear, BLUE, "print")
for nm, a0, a1 in (("down", 200 + TAB_HALF + 1, 200 + TAB_HALF + 13), ("stowed", 75 - TAB_HALF - 13, 75 - TAB_HALF - 1)):
    part(fixed, f"extractor_stop_{nm} (print, on the servo bracket)", sector_at(SV_Y, SV_Z, a0, a1, 30, 40, xr(GEAR_X0), xr(SPLX + 2)), BLUE, "print")
sb = box(xr(SPLX + 2), xr(124.0), SV_Y + 12, SV_Y + 34, FACE, SV_Z - 10)          # bolts to the right upright's front face...
sb = sb.union(box(xr(SPLX + 40.6), xr(124.0), SV_Y - 17, SV_Y + 37.4, FACE, SV_Z + 10))   # ...and holds the servo's outer end
for y in (-7.2, 8.8): sb = sb.cut(cyl("z", (C - 128.0, y, 0), M4, FACE - 1, SV_Z + 11))
part(fixed, "extractor_servo_bracket (print)", sb, BLUE, "print")

# ---- the Rigid V's corner plates (optional) ----
for s, f, sg in (("R", xr, -1), ("L", xl, 1)):
    a, b = (f(V_IN), V_ROOT), (f(V_OUT), V_TIP)
    dx, dz = b[0] - a[0], b[1] - a[1]; L = math.hypot(dx, dz)
    pl = cq.Workplane("XY").box(L, V_TOP - 0.25 * IN, 3.175, centered=(False, False, True))      # length along x, height along y
    pl = pl.rotate((0, 0, 0), (0, 1, 0), math.degrees(math.atan2(-dz, dx))).translate((a[0], F + 0.25 * IN, a[1]))
    tab = box(f(V_IN), f(V_IN + 3.175), -118.0, -52.0, V_ROOT - 24, V_ROOT + 2)
    part(vee, f"rigid_v_plate_{s} (1/8 in aluminium, optional)", pl.union(tab), ALU, "cut")

if __name__ == "__main__":
    out = os.path.dirname(os.path.abspath(__file__)); os.makedirs(out + "/stl", exist_ok=True)
    assy = cq.Assembly(name="DHS intake option b")
    for title, d_ in (("chassis and roller (fixed)", fixed), ("FLOWER extractor (turns about the roller axle, 0-125 deg)", hook), ("rigid V plates (optional)", vee)):
        sub = cq.Assembly(name=title)
        for n, (wp, col, kind) in d_.items(): sub.add(wp, name=n, color=cq.Color(*col))
        assy.add(sub)
    assy.save(out + "/dhs-intake-b.step")
    mesh = {}
    for grp, d_ in (("fixed", fixed), ("hook", hook), ("vee", vee)):
        for n, (wp, col, kind) in d_.items():
            shp = wp.val() if len(wp.vals()) == 1 else cq.Compound.makeCompound(wp.vals())
            v, f_ = shp.tessellate(0.2, 0.3)
            mesh[n] = dict(grp=grp, col=col, kind=kind, v=[(p.x, p.y, p.z) for p in v], f=f_)
            if kind == "print": shp.exportStl(f"{out}/stl/{n.split(' ')[0]}.stl", 0.05, 0.2)
    if os.environ.get("MESH_OUT"):
        import pickle; pickle.dump(mesh, open(os.environ["MESH_OUT"], "wb"))
    print(len(fixed), "fixed,", len(hook), "hook,", len(vee), "V parts")
