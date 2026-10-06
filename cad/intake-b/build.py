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
ARM = 7.15 * IN                        # the hook's arm, from centre: its 14 mm hub clears the plate (7.56 in) and the servo
ZB = FACE + 7.48 * IN                  # FLOWER block's back edge with the hook down (24 in overall)
V_IN, V_OUT, V_FWD, V_TOP = PLATE_IN + PLATE_T, 9.0 * IN - 4.0, 1.75 * IN, 4.0 * IN   # Rigid V plates
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
    pts = [(-127.0, AX_ZR - 24), (-79.0, AX_ZR - 24), (-79.0, FACE - 40), (HY + 16, FACE - 18), (HY + 16, HZ + 16),
           (ROLL_Y, ROLL_Z + 16), (-127.0, ROLL_Z + 6)]
    p = cq.Workplane("YZ").polyline(pts).close().extrude(PLATE_T).translate((min(x0, x1), 0, 0))
    holes = [(AX_Y, AX_ZF), (AX_Y, AX_ZR), (ROLL_Y, ROLL_Z), (HY, HZ)]          # wheels, roller, hinge (right) / motor (left)
    for y, z in holes: p = p.cut(cyl("x", (0, y, z), 14.0, min(x0, x1) - 1, max(x0, x1) + 1))
    for y in A.STANDOFF_Y:
        for z in A.STANDOFF_Z: p = p.cut(cyl("x", (0, y, z), M4, min(x0, x1) - 1, max(x0, x1) + 1))
    for y, z in ((-110.0, FACE - 8), (-60.0, FACE - 8)): p = p.cut(cyl("x", (0, y, z), M4, min(x0, x1) - 1, max(x0, x1) + 1))  # V plate tab
    return p
part(fixed, "side_plate_R (1/8 in aluminium)", side_plate(True), ALU, "cut")
part(fixed, "side_plate_L (1/8 in aluminium)", side_plate(False), ALU, "cut")
for n, (wp, col, kind) in A.parts.items():            # keep the chassis add-ons, minus the old plates, hinge and servo
    if n.startswith(("outer_plate", "hinge_", "servo", "bearing_R_hinge")): continue
    part(fixed, n, wp, col, kind)

# ---- the roller ----
part(fixed, "roller_shaft (8mm REX, 400 mm)", cyl("x", (0, ROLL_Y, ROLL_Z), 8.0, xr(PLATE_IN + PLATE_T + 4), xl(PLATE_IN + PLATE_T + 4)), STEEL, "buy")
part(fixed, "roller_wheels (48 mm gecko, 13.8 in of them)", cyl("x", (0, ROLL_Y, ROLL_Z), 48.0, xr(ROLL_HALF), xl(ROLL_HALF)), (0.35, 0.66, 0.31), "buy")
for s, f in (("R", xr), ("L", xl)):
    part(fixed, f"roller_bearing_{s} (8mm REX flanged)", cyl("x", (0, ROLL_Y, ROLL_Z), 14.0, f(PLATE_IN), f(PLATE_IN + PLATE_T + 1.2)), BRASS, "buy")
# Belt drive inside the left plate, between the roller's end (6.9 in) and the plate (7.56 in): outside the plate the
# Rigid V's left plate would cut through the pulley and belt.
PUL0, PUL1 = ROLL_HALF + 1.5, PLATE_IN - 2.0
part(fixed, "roller_pulley_L (HTD5, inside the plate)", cyl("x", (0, ROLL_Y, ROLL_Z), 30.0, xl(PUL0), xl(PUL1)), BLACK, "buy")
part(fixed, "motor_pulley_L (HTD5, inside the plate)", cyl("x", (0, HY, HZ), 30.0, xl(PUL0), xl(PUL1)), BLACK, "buy")
part(fixed, "roller_belt_L (HTD5, 9 mm)", box(xl(PUL0 + 1.5), xl(PUL1 - 1.5), ROLL_Y - 15, HY + 15, HZ - 15, HZ + 15).cut(box(xl(PUL0), xl(PUL1), ROLL_Y - 12, HY + 12, HZ - 12, HZ + 12)), BLACK, "buy")
part(fixed, "roller_motor_L (goBILDA 5203, inboard, over the roller)", cyl("x", (0, HY, HZ), 37.0, xl(PUL0 - 2), xl(PUL0 - 122)), BLACK, "buy")
part(fixed, "motor_shaft_L (8mm REX, through the pulley into the plate's bearing)", cyl("x", (0, HY, HZ), 8.0, xl(PUL0 - 2), xl(PLATE_IN + PLATE_T + 3)), STEEL, "buy")
mb = box(xl(124.0), xl(PUL0 - 8), HY - 24, HY + 24, FACE, HZ - 18.5)          # bolts to the left upright's front face
for y in (-7.2, 8.8): mb = mb.cut(cyl("z", (C + 128.0, y, 0), M4, FACE - 1, HZ))
part(fixed, "motor_bracket_L (print)", mb, BLUE, "print")
part(fixed, "motor_bearing_L (8mm REX flanged)", cyl("x", (0, HY, HZ), 14.0, xl(PLATE_IN), xl(PLATE_IN + PLATE_T + 1.2)), BRASS, "buy")
# ---- the hook's servo, inboard of the right plate, spline out toward the hub ----
SPL = ARM - 7.0 - 2.0                  # servo face 2 mm inboard of the hub
servo = box(xr(SPL - 38.6), xr(SPL), HY - 10.2, HY + 30.6, HZ - 10, HZ + 10)
servo = servo.union(box(xr(SPL - 8.5), xr(SPL - 6), HY - 17, HY + 37.4, HZ - 10, HZ + 10))
part(fixed, "servo_R (goBILDA 2000 Torque)", servo, BLACK, "buy")
sb = box(xr(124.0), xr(SPL - 6), HY - 17, HY + 37.4, FACE, HZ - 10)       # bolts to the right upright's front face
for y in (-7.2, 8.8): sb = sb.cut(cyl("z", (C - 128.0, y, 0), M4, FACE - 1, HZ))
part(fixed, "servo_bracket_R (print)", sb, BLUE, "print")
part(fixed, "hinge_bearing_R (8mm REX flanged)", cyl("x", (0, HY, HZ), 14.0, xr(PLATE_IN), xr(PLATE_IN + PLATE_T + 1.2)), BRASS, "buy")
part(fixed, "servo_shaft_R (goBILDA 8mm REX servo shaft, 25T, 36 mm)", cyl("x", (0, HY, HZ), 8.0, xr(SPL), xr(SPL + 36)), STEEL, "buy")

# ---- the hook: hub on the hinge, one sloping arm, corner block, front shaft, FLOWER block, curtains ----
CB = (xr(ARM), F + 2.9 * IN, ZB + 14.0)            # where the arm enters the corner block
hub = box(xr(ARM - 7.0), xr(ARM + 7.0), HY - 14, HY + 14, HZ - 14, HZ + 14)
d = cq.Vector(0, CB[1] - HY, CB[2] - HZ).normalized()
boss = rod((xr(ARM), HY, HZ), (xr(ARM), HY + d.y * 30, HZ + d.z * 30), 14.0)
hub = hub.union(boss).cut(cyl("x", (0, HY, HZ), BORE, xr(ARM - 8), xr(ARM + 8)))
hub = hub.cut(rod((xr(ARM), HY + d.y * 10, HZ + d.z * 10), (xr(ARM), HY + d.y * 31, HZ + d.z * 31), BORE))
part(hook, "hinge_hub_R (print)", hub, BLUE, "print")
arm0 = (xr(ARM), HY + d.y * 12, HZ + d.z * 12); arm1 = (xr(ARM), CB[1] + d.y * 12, CB[2] + d.z * 12)
part(hook, "arm_shaft (8mm REX, cut to 200 mm)", rod(arm0, arm1), STEEL, "buy")
cb = box(xr(ARM - 8), xr(ARM + 7), F + A.BOTTOM, F + 3.3 * IN, ZB + A.ROD_X - 14, ZB + A.DEPTH)
cb = cb.cut(rod(CB, (CB[0], CB[1] + d.y * 14, CB[2] + d.z * 14), BORE))
rex = (cq.Workplane("YZ").polygon(6, A.REX_AF / math.cos(math.pi / 6)).extrude(26).translate((xr(ARM - 3), FS_Y, FS_Z))
       .intersect(cyl("x", (0, FS_Y, FS_Z), BORE, xr(ARM - 3), xr(ARM - 3) + 26)))
part(hook, "corner_block_R (print)", cb.cut(rex), BLUE, "print")
part(hook, "front_shaft (8mm REX, 312 mm)", cyl("x", (0, FS_Y, FS_Z), 8.0, xr(ARM - 3), xl(A.FRONT_LEFT)), STEEL, "buy")
part(hook, "flower_block (print)", ramp_block().translate((0, 0, ZB - A.ZB)), (0.69, 0.42, 0.85), "print")
for dd in (60, 110):
    for s, f in (("R", xr), ("L", xl)): part(hook, f"curtain_clip_{dd}_{s} (print)", clip("x").translate((f(dd), FS_Y - 7.5, FS_Z)), BLUE, "print")
for s, f in (("R", xr), ("L", xl)):
    part(hook, f"collar_{s} (8mm REX clamping collar)", cyl("x", (0, FS_Y, FS_Z), 21.0, f(1.55 * IN - 4), f(1.55 * IN + 4)), STEEL, "buy")
# As the hook folds (125-135 deg) the curtains sweep through the tops of the two tall front towers (4.8-5.3 in from
# centre, 14.3 in tall), so both curtains end at 4.7 in. The right one leaves 2.45 in to the arm: no POLLEN gets out.
CURT_END = 4.7 * IN
part(hook, "curtain_R (1/16 polycarbonate)", box(xr(38.6), xr(CURT_END), F + 1.3 * IN, F + 3.5 * IN, FS_Z - 0.8, FS_Z + 0.8), POLY, "cut")
part(hook, "curtain_L (1/16 polycarbonate)", box(xl(38.6), xl(CURT_END), F + 1.3 * IN, F + 3.5 * IN, FS_Z - 0.8, FS_Z + 0.8), POLY, "cut")
def at(z): return HY + (CB[1] - HY) * (z - HZ) / (CB[2] - HZ)            # the arm's height at z
z0, z1 = FACE + 2.3 * IN, ZB + A.ROD_X - 16
side = cq.Workplane("YZ").polyline([(F + 1.3 * IN, z0), (at(z0) - 8, z0), (at(z1) - 8, z1), (F + 1.3 * IN, z1)]).close().extrude(1.6).translate((xr(ARM) - 0.8, 0, 0))
part(hook, "side_panel_R (1/16 polycarbonate)", side, POLY, "cut")

# ---- the Rigid V's corner plates (optional) ----
for s, f, sg in (("R", xr, -1), ("L", xl, 1)):
    a, b = (f(V_IN), FACE + 2.0), (f(V_OUT), FACE + V_FWD)
    dx, dz = b[0] - a[0], b[1] - a[1]; L = math.hypot(dx, dz)
    pl = cq.Workplane("XY").box(L, V_TOP - 0.25 * IN, 3.175, centered=(False, False, True))      # length along x, height along y
    pl = pl.rotate((0, 0, 0), (0, 1, 0), math.degrees(math.atan2(-dz, dx))).translate((a[0], F + 0.25 * IN, a[1]))
    tab = box(f(V_IN), f(V_IN + 3.175), -118.0, -52.0, FACE - 20, FACE + 4)
    part(vee, f"rigid_v_plate_{s} (1/8 in aluminium, optional)", pl.union(tab), ALU, "cut")

if __name__ == "__main__":
    out = os.path.dirname(os.path.abspath(__file__)); os.makedirs(out + "/stl", exist_ok=True)
    assy = cq.Assembly(name="DHS intake option b")
    for title, d_ in (("chassis and roller (fixed)", fixed), ("ramp hook (turns about the hinge, 0-150 deg)", hook), ("rigid V plates (optional)", vee)):
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
