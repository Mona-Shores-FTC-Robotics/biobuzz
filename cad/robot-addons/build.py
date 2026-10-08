"""Bolt-on add-ons for the DHS robot, in the robot CAD's own frame (mm): x across (the robot's right is -x), y up, z forward.
Outer wheel plates + standoffs + 80 mm wheel shafts + outer bearings, odometry pods, and the ramp hook with its hinges and servo.

    pip install cadquery
    python3 cad/robot-addons/build.py        # writes dhs-addons.step and stl/*.stl next to this file
"""
import math, os, sys
import cadquery as cq
from OCP.BRepTools import BRepTools
from OCP.TopoDS import TopoDS_Shape
from OCP.BRep import BRep_Builder

IN = 25.4
C, F, FACE = -59.62, -151.75, 207.73          # robot centre x, floor y, front face z
AX_Y, AX_ZF, AX_ZR = -103.75, 159.70, -128.30  # wheel axle height, front and rear axle z
RAIL_OUT = 136.0                               # rail outer face, from centre (right rail x -195.62)
PLATE_IN, PLATE_T = RAIL_OUT + 56.0, 3.175     # outer plate: 56 mm standoffs, 1/8 in aluminium
HY, HZ = F + 2.2 * IN, FACE + 0.8 * IN         # hinge axis: 2.2 in up, 0.8 in in front of the face
SIDE = 6.1 * IN                                # side arm, from centre
GAP = 7.4 * IN; ZB = FACE + GAP                # FLOWER block back edge
BOTTOM, TOP, DEPTH, FLAT = 0.7 * IN, 1.35 * IN, 1.4 * IN, 0.5 * IN
ARC_R, SLANT, LAND = 1.36 * IN, 0.2 * IN, 0.12 * IN
ROD_X, ROD_Z = DEPTH - 12.0, (BOTTOM + TOP) / 2
FS_Y, FS_Z = F + ROD_Z, ZB + ROD_X             # front shaft axis
LO_Y, HI_Y = F + 1.5 * IN, F + 3.75 * IN       # side arm shafts: bottom and top
RISER_Z = HZ + 2.6 * IN                        # where the arm's tall part starts
CORNER_Z0, CORNER_Z1 = ZB + ROD_X - 14.0, ZB + DEPTH
BORE, M4, M3 = 8.3, 4.3, 2.6
REX_AF = 7.0 + 0.3                             # REX across flats (+ clearance): CHECK on a test print before trusting it
FRONT_LEFT = 132.0                             # the front shaft's free left end, from centre (288 mm shaft)
STANDOFF_Y, STANDOFF_Z = (-117.05, -89.75), (63.7, -56.3)   # the middles of the rail's vertical slots, clear of both wheels
xr = lambda d: C - d       # right side
xl = lambda d: C + d       # left side

def box(x0, x1, y0, y1, z0, z1):
    return cq.Workplane("XY").box(abs(x1 - x0), abs(y1 - y0), abs(z1 - z0)).translate(((x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2))
def cyl(axis, c, d, a0, a1):
    """Cylinder of diameter d along axis 'x'|'y'|'z' from a0 to a1, through point c (the other two coords)."""
    L = abs(a1 - a0); m = (a0 + a1) / 2
    if axis == "x": return cq.Workplane("YZ").circle(d / 2).extrude(L).translate((min(a0, a1), c[1], c[2]))
    if axis == "y": return cq.Workplane("XZ").circle(d / 2).extrude(-L).translate((c[0], min(a0, a1), c[2]))
    return cq.Workplane("XY").circle(d / 2).extrude(L).translate((c[0], c[1], min(a0, a1)))
def mirror(wp):  # right side -> left side, about the robot's centre plane
    return wp.mirror("YZ", (C, 0, 0))

parts = {}   # name -> (workplane, colour, kind)
def add(name, wp, colour, kind): parts[name] = (wp, colour, kind)

# ---------------- outer wheel plates (both sides) ----------------
def outer_plate(hinge=True):
    x0, x1 = xr(PLATE_IN), xr(PLATE_IN + PLATE_T)
    pts = ([(-127.0, AX_ZR - 24), (-79.0, AX_ZR - 24), (-79.0, 196.0), (-52.0, 206.0), (-52.0, HZ + 6), (HY - 2, HZ + 16), (-127.0, HZ + 14)]
           if hinge else [(-127.0, AX_ZR - 24), (-79.0, AX_ZR - 24), (-79.0, AX_ZF + 24), (-127.0, AX_ZF + 24)])
    prof = cq.Workplane("YZ").polyline(pts).close()
    p = prof.extrude(PLATE_T).translate((x1, 0, 0))
    for z in (AX_ZF, AX_ZR): p = p.cut(cyl("x", (0, AX_Y, z), 14.0, x1 - 1, x0 + 1))
    if hinge: p = p.cut(cyl("x", (0, HY, HZ), 14.0, x1 - 1, x0 + 1))
    for y in STANDOFF_Y:
        for z in STANDOFF_Z: p = p.cut(cyl("x", (0, y, z), M4, x1 - 1, x0 + 1))
    return p
plateR = outer_plate()
add("outer_plate_R (1/8 in aluminium)", plateR, (0.75, 0.78, 0.82), "cut")
add("outer_plate_L (1/8 in aluminium)", mirror(outer_plate(hinge=False)), (0.75, 0.78, 0.82), "cut")
for side, f in (("R", xr), ("L", xl)):
    for y in STANDOFF_Y:
        for z in STANDOFF_Z:
            add(f"standoff_{side}_{z:+.0f}_{y:.0f} (goBILDA 1501-0006-0560, M4 standoff, 56 mm)", cyl("x", (0, y, z), 7.0, f(RAIL_OUT), f(PLATE_IN)), (0.55, 0.6, 0.66), "buy")
    for z in (AX_ZF, AX_ZR):
        add(f"wheel_shaft_{side}_{'front' if z > 0 else 'rear'} (goBILDA 2106-4008-0800, 8mm REX, 80 mm, e-clips; replaces 72 mm)", cyl("x", (0, AX_Y, z), 8.0, f(121.5), f(201.5)), (0.8, 0.82, 0.85), "buy")
        add(f"bearing_{side}_{'front' if z > 0 else 'rear'} (goBILDA 1611-0514-4008, 8mm REX bore)", cyl("x", (0, AX_Y, z), 14.0, f(PLATE_IN), f(PLATE_IN + PLATE_T + 1.2)), (0.85, 0.75, 0.3), "buy")
add("bearing_R_hinge (goBILDA 1611-0514-4008, 8mm REX bore)", cyl("x", (0, HY, HZ), 14.0, xr(PLATE_IN), xr(PLATE_IN + PLATE_T + 1.2)), (0.85, 0.75, 0.3), "buy")

# ---------------- hinge brackets on the front uprights (printed) ----------------
def bracket():
    x0, x1 = xr(124.0), xr(136.0)
    prof = cq.Workplane("YZ").polyline([(-47.0, FACE), (-47.0, FACE + 6), (-80.0, FACE + 6), (HY + 6, HZ + 2),
                                         (HY + 2, HZ + 11), (HY - 9, HZ + 11), (HY - 11, FACE + 4), (HY - 11, FACE)]).close()
    b = prof.extrude(12.0).translate((x1, 0, 0))
    b = b.union(cyl("x", (0, HY, HZ), 24.0, x1, x0))
    b = b.cut(cyl("x", (0, HY, HZ), 14.0, x1 - 1, x0 + 1))
    for y in (-71.2, -55.2): b = b.cut(cyl("z", (C - 128.0, y, 0), M4, FACE - 1, FACE + 7))
    return b
brR = bracket()
add("hinge_bracket_R (print)", brR, (0.18, 0.37, 0.62), "print")

# ---------------- the hook (built for the right side, mirrored) ----------------
hook = {}
def hadd(name, wp, colour, kind): hook[name] = (wp, colour, kind)
def hub():
    x0, x1 = xr(137.5), xr(189.0)   # 3 mm clear of the outer plate
    h = box(x0, x1, LO_Y - 6, HY + 12, HZ - 11, HZ + 22)
    h = h.cut(cyl("x", (0, HY, HZ), BORE, x0 + 1, x1 - 1))
    h = h.cut(cyl("z", (xr(SIDE), LO_Y, 0), BORE, HZ + 4, HZ + 23))
    h = h.cut(cyl("y", (xr(SIDE) + 10, 0, HZ), M3, HY, HY + 13))
    h = h.cut(cyl("y", (xr(SIDE) - 10, 0, HZ), M3, HY, HY + 13))
    return h
def riser():
    x0, x1 = xr(SIDE - 8), xr(SIDE + 8)
    r = box(x0, x1, LO_Y - 6, HI_Y + 8, RISER_Z - 9, RISER_Z + 9)
    r = r.cut(cyl("z", (xr(SIDE), LO_Y, 0), BORE, RISER_Z - 10, RISER_Z + 10))
    r = r.cut(cyl("z", (xr(SIDE), HI_Y, 0), BORE, RISER_Z - 10, RISER_Z + 10))
    return r
def corner():
    x0, x1 = xr(SIDE - 9), xr(SIDE + 9)
    c = box(x0, x1, F + BOTTOM, HI_Y + 9, CORNER_Z0, CORNER_Z1)
    c = c.cut(cyl("z", (xr(SIDE), LO_Y, 0), BORE, CORNER_Z0 - 1, CORNER_Z0 + 20))
    c = c.cut(cyl("z", (xr(SIDE), HI_Y, 0), BORE, CORNER_Z0 - 1, CORNER_Z0 + 20))
    rex = (cq.Workplane("YZ").polygon(6, REX_AF / math.cos(math.pi / 6)).extrude(30).translate((xr(156.0), FS_Y, FS_Z))
           .intersect(cyl("x", (0, FS_Y, FS_Z), BORE, xr(156.0), xr(156.0) + 30)))
    c = c.cut(rex)                                   # REX-shaped, blind: the front shaft can't turn in it
    return c
def clip(axis):
    """The curtain clip: 16 x 16 x 38 mm, shaft bore 7.5 mm up, 24 mm panel slot from the top."""
    c = box(-8, 8, 0, 38, -8, 8).cut(cyl("x", (0, 7.5, 0), BORE, -9, 9)).cut(box(-9, 9, 14, 39, -0.93, 0.93))
    for y in (31, 21): c = c.cut(cyl("z", (0, y, 0), 3.3, -9, 9))
    return c if axis == "x" else c.rotate((0, 0, 0), (0, 1, 0), 90)
def ramp_block():
    h, run = TOP - BOTTOM, DEPTH - FLAT
    side = cq.Workplane("YZ").polyline([(0, 0), (h, run), (h, DEPTH), (0, DEPTH)]).close().extrude(2 * ARC_R + 4).translate((-(ARC_R + 2), 0, 0))
    plan = cq.Workplane("XZ").center(0, DEPTH - ARC_R).circle(ARC_R).extrude(-h * 3).translate((0, -h, 0))
    lean = SLANT / (h - LAND); top = h + 2
    cone = (cq.Workplane("XY").polyline([(0, -1), (ARC_R, -1), (ARC_R, LAND), (ARC_R - lean * (top - LAND), top), (0, top)]).close()
            .revolve(360, (0, 0, 0), (0, 1, 0)).translate((0, 0, DEPTH - ARC_R)))
    b = side.intersect(plan).intersect(cone)
    b = b.cut(cyl("x", (0, ROD_Z - BOTTOM, ROD_X), BORE, -ARC_R - 5, ARC_R + 5))
    for x in (-14, 14): b = b.cut(cyl("y", (x, 0, ROD_X), M3, -1, ROD_Z - BOTTOM))
    return b.translate((C, F + BOTTOM, ZB))
hubR, riserR, cornerR = hub(), riser(), corner()
for nm, wp in (("hinge_hub", hubR), ("riser", riserR), ("corner_block", cornerR)):
    hadd(nm + "_R (print)", wp, (0.18, 0.37, 0.62), "print")     # one arm only: a second would corral spilled pieces
hadd("flower_block (print)", ramp_block(), (0.69, 0.42, 0.85), "print")
for s, f in (("R", xr),):
    hadd(f"arm_shaft_bottom_{s} (8mm REX, 192 mm)", cyl("z", (f(SIDE), LO_Y, 0), 8.0, HZ + 4, HZ + 4 + 192), (0.8, 0.82, 0.85), "buy")
    hadd(f"arm_shaft_top_{s} (8mm REX, 144 mm)", cyl("z", (f(SIDE), HI_Y, 0), 8.0, CORNER_Z0 + 19 - 144, CORNER_Z0 + 19), (0.8, 0.82, 0.85), "buy")
    hadd(f"side_panel_{s} (1/16 polycarbonate)", box(f(SIDE) - 0.8, f(SIDE) + 0.8, LO_Y + 8, HI_Y - 8, RISER_Z + 10, CORNER_Z0 - 2), (0.6, 0.78, 0.96), "cut")
    for z in (RISER_Z + 28, CORNER_Z0 - 22):
        hadd(f"side_clip_{s}_{z:.0f}_lo (print)", clip("z").translate((f(SIDE), LO_Y - 7.5, z)), (0.18, 0.37, 0.62), "print")
        hadd(f"side_clip_{s}_{z:.0f}_hi (print)", clip("z").mirror("XZ").translate((f(SIDE), HI_Y + 7.5, z)), (0.18, 0.37, 0.62), "print")
hadd("front_shaft (8mm REX, 288 mm)", cyl("x", (0, FS_Y, FS_Z), 8.0, xr(156.0), xl(FRONT_LEFT)), (0.8, 0.82, 0.85), "buy")
for d in (60, 110):
    for f in (xr, xl): hadd(f"curtain_clip_{d}_{'R' if f is xr else 'L'} (print)", clip("x").translate((f(d), FS_Y - 7.5, FS_Z)), (0.18, 0.37, 0.62), "print")
for s, f in (("R", xr), ("L", xl)):
    hadd(f"curtain_{s} (1/16 polycarbonate)", box(f(38.6), f(SIDE - 10) if s == "R" else f(FRONT_LEFT - 8), F + 1.3 * IN, F + 3.5 * IN, FS_Z - 0.8, FS_Z + 0.8), (0.6, 0.78, 0.96), "cut")
    for x in (42.0, 46.0):
        pass
for z in (FS_Z,):
    for d in (1.55 * IN,):
        for f in (xr, xl): hadd(f"collar_{'R' if f is xr else 'L'} (8mm REX clamping collar)", cyl("x", (0, FS_Y, FS_Z), 21.0, f(d - 4), f(d + 4)), (0.8, 0.82, 0.85), "buy")

# hinge axles: left one 88 mm through bracket, hub, plate bearing; right: 48 mm stub + the servo's 36 mm REX servo shaft
add("hinge_stub_R (8mm REX, 48 mm)", cyl("x", (0, HY, HZ), 8.0, xr(117.0), xr(165.0)), (0.8, 0.82, 0.85), "buy")
add("servo_shaft_R (goBILDA 8mm REX servo shaft, 25T, 36 mm)", cyl("x", (0, HY, HZ), 8.0, xr(161.0), xr(197.0)), (0.8, 0.82, 0.85), "buy")
SPL = PLATE_IN + PLATE_T + 2.0
servo = box(xr(SPL + 2), xr(SPL + 40.6), HY - 10.2, HY + 30.6, HZ - 10, HZ + 10)
servo = servo.union(box(xr(SPL + 8), xr(SPL + 10.5), HY - 17, HY + 37.4, HZ - 10, HZ + 10)).union(cyl("x", (0, HY, HZ), 6.0, xr(SPL), xr(SPL + 2)))
add("servo_R (goBILDA 2000-0025-0002, Torque)", servo, (0.13, 0.15, 0.17), "buy")

# ---------------- odometry pods (from the example STEP) ----------------
# The pods are goBILDA's own CAD, cut from the team's example robot (pod1/pod2.brep, ~16 MB each, kept out of git).
# The STEP gets a stand-in box with each pod's exact envelope; with POD_DIR set, the real pods are meshed for checks.
# The right pod's mount (M4 tapped, 32 mm square) on the rail's slot ends: 1 mm up and 8 mm back from where the
# example robot had it, so four screws from inside the rail reach it.
POD_MOVE = {"odometry_pod_R_strafe (goBILDA 3110-0001-0002)": ("pod1", (-332.75, 70.31, 2.23, -290.18, 139.4, 45.65), (xr(RAIL_OUT) + 290.18, F - 70.31 + 1.0, -8.3 - 23.94 - 8.0)),
            "odometry_pod_L_forward (goBILDA 3110-0001-0002)": ("pod2", (-48.09, 70.31, -21.77, -4.66, 139.4, 20.79), (xl(RAIL_OUT + 28.0) + 26.375, F - 70.31, 8.0 + 21.77))}
PODS = {}
for n, (fn, bb, d) in POD_MOVE.items():
    add(n + " STAND-IN", box(bb[0] + d[0], bb[3] + d[0], bb[1] + d[1], bb[4] + d[1], bb[2] + d[2], bb[5] + d[2]), (0.95, 0.75, 0.1), "buy")
    path = os.path.join(os.environ.get("POD_DIR", ""), fn + ".brep")
    if os.environ.get("POD_DIR") and os.path.exists(path):
        s = TopoDS_Shape(); BRepTools.Read_s(s, path, BRep_Builder()); PODS[n] = cq.Shape.cast(s).translate(cq.Vector(*d))
ad = box(xl(RAIL_OUT), xl(RAIL_OUT + 6), -125.0, -82.0, -24.0, 8.0).union(box(xl(RAIL_OUT), xl(RAIL_OUT + 52), -125.0, -82.0, 2.0, 8.0))
add("pod_adapter_L (print)", ad, (0.18, 0.37, 0.62), "print")

if __name__ == "__main__":
    out = sys.argv[1] if len(sys.argv) > 1 else os.path.dirname(os.path.abspath(__file__))
    os.makedirs(out + "/stl", exist_ok=True)
    import pickle
    assy = cq.Assembly(name="DHS add-ons")
    fixed = cq.Assembly(name="chassis add-ons (fixed)")
    hk = cq.Assembly(name="ramp hook (turns about the hinge axis)")
    for n, (wp, col, kind) in parts.items(): fixed.add(wp, name=n, color=cq.Color(*col))
    for n, (wp, col, kind) in hook.items(): hk.add(wp, name=n, color=cq.Color(*col))
    assy.add(fixed); assy.add(hk)
    assy.save(out + "/dhs-addons.step")
    # meshes for checks and the web view (page frame comes later)
    mesh = {}
    for grp, d in (("fixed", parts), ("hook", hook)):
        for n, (wp, col, kind) in d.items():
            shp = wp.val() if len(wp.vals()) == 1 else cq.Compound.makeCompound(wp.vals())
            v, f = shp.tessellate(0.2, 0.3)
            mesh[n] = dict(grp=grp, col=col, kind=kind, v=[(p.x, p.y, p.z) for p in v], f=f)
            if kind == "print": shp.exportStl(f"{out}/stl/{n.split(' ')[0]}.stl", 0.05, 0.2)
    for n, p in PODS.items():
        v, f = p.tessellate(0.4, 0.6)
        mesh[n] = dict(grp="pod", col=(0.95, 0.75, 0.1), kind="buy", v=[(q.x, q.y, q.z) for q in v], f=f)
    if os.environ.get("MESH_OUT"): pickle.dump(mesh, open(os.environ["MESH_OUT"], "wb"))
    print(len(parts), "fixed parts,", len(hook), "hook parts")
