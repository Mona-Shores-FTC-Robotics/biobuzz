"""The intake-to-turret transfer (issue #164, doc/transfer.md on spike/164-transfer, concept A), in the robot CAD's
frame, so dhs-transfer.step lands in place in Onshape next to cad/intake-b/dhs-intake-b.step.

    python3 cad/transfer/build.py        # writes dhs-transfer.step and stl/ next to this file

Drawn in the robot frame the transfer chat uses (+X forward, +Y left, +Z up, inches, origin on the floor under the
chassis centre, 7.56 in behind the front face) and moved into the CAD's millimetres at the end. Groups: "fixed"
(the lane, strands, chute, mounts, J motor), "float" (the lane-drive pulley on the roller's shaft, which rises with
the roller), "arm" (the J-wheel, its shaft and arms: they lift up to 1.2 in for a NECTAR, about the pivot).
The mounts (three cross-strips to the rails, the J motor's cradle on the left rail) are this CAD's proposal; the
transfer chat's doc leaves them open.
"""
import math, os, sys
import cadquery as cq
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "robot-addons"))
from build import C, F, FACE, IN, M4

CENTRE_BACK_IN = 7.56
T16 = 1 / 16                            # polycarbonate
FLOOR_Z, WALL_Y, WALL_TOP = 0.9, 2.1, 5.0
LANE_X0, LANE_X1 = -1.3, 7.2            # walls' extent along the lane (the chute's walls carry on back to -5.1)
STRAND_Y, STRAND_X, STRAND_Z, STRAND_D = 0.75, (5.6, -1.0), 0.55, 0.75
J_AXLE, PIVOT, ARM_L = (-1.32, 4.54), (0.85, 3.29), 2.5
J_R, J_W = 24.0 / IN, 2.0               # 48 mm gecko wheels, two side by side
J_OUT, J_BACK, CHUTE_TOP = 3.64, -4.96, 6.6
RAMP = ((8.0, 0.05), (5.8, 0.9))
CSHAFT = (6.0, 4.0)                     # the lane drive's countershaft, Y +2.6, outside the left wall
ROLLER = (8.56, 3.35)                   # the roller's axle at rest (it floats up 1.3 in)
JM = (-2.4, 3.7)                        # J motor's axis (along Y), over the left rail
STRIPS_X = (3.78, 1.89, -2.835)          # cross-strips under the lane, on the rails' lower hole row (24 mm pitch)
RAIL_IN = 4.88                          # rails' inner faces, |Y|
TURRET = (-3.17, 0.0)

fixed, flt, arm = {}, {}, {}
def part(d, name, wp, col, kind): d[name] = (wp, col, kind)
BLUE, ALU, STEEL, POLY, BLACK, GREEN, RED = (0.18, 0.37, 0.62), (0.75, 0.78, 0.82), (0.8, 0.82, 0.85), (0.6, 0.78, 0.96), (0.13, 0.15, 0.17), (0.35, 0.66, 0.31), (0.75, 0.2, 0.2)

# ---- helpers, all in robot-frame inches (X fwd, Y left, Z up) ----
def bx(x0, x1, y0, y1, z0, z1):
    return cq.Workplane("XY").box(abs(x1 - x0), abs(y1 - y0), abs(z1 - z0)).translate(((x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2))
def cyly(x, z, d, y0, y1):
    """A cylinder along Y."""
    return cq.Workplane("XZ").center(x, z).circle(d / 2).extrude(-(y1 - y0)).translate((0, y0, 0))
def xz(pts, y0, y1):
    """A profile drawn in X-Z, extruded across Y from y0 to y1."""
    return cq.Workplane("XZ").polyline(pts).close().extrude(-(y1 - y0)).translate((0, y0, 0))
def bar(p0, p1, w, y0, y1):
    """A flat bar in X-Z between two points, rounded ends."""
    (x0, z0), (x1, z1) = p0, p1
    L = math.hypot(x1 - x0, z1 - z0); a = math.degrees(math.atan2(z1 - z0, x1 - x0))
    b = cq.Workplane("XZ").center(L / 2, 0).rect(L, w).extrude(-(y1 - y0))
    b = b.union(cq.Workplane("XZ").circle(w / 2).extrude(-(y1 - y0))).union(cq.Workplane("XZ").center(L, 0).circle(w / 2).extrude(-(y1 - y0)))
    return b.rotate((0, 0, 0), (0, 1, 0), -a).translate((x0, y0, z0))
def cord(p0, p1, r0, r1, y):
    """One polycord loop between pulleys of pitch radius r0, r1: two straight runs (3/16 in)."""
    (x0, z0), (x1, z1) = p0, p1; dx, dz = x1 - x0, z1 - z0; L = math.hypot(dx, dz)
    nx, nz = -dz / L, dx / L; out = None
    for s in (1, -1):
        a, b = (x0 + s * nx * r0, z0 + s * nz * r0), (x1 + s * nx * r1, z1 + s * nz * r1)
        piece = bar(a, b, 3 / 16, y - 3 / 32, y + 3 / 32)
        out = piece if out is None else out.union(piece)
    return out
def ring_slot(c, r, a0, a1, w, y0, y1, n=16):
    """An arc-shaped slot (for the J shaft's float) about centre c."""
    pts = [(c[0] + (r + s * w / 2) * math.cos(math.radians(a0 + (a1 - a0) * i / n)), c[1] + (r + s * w / 2) * math.sin(math.radians(a0 + (a1 - a0) * i / n)))
           for s in (1, -1) for i in (range(n + 1) if s == 1 else range(n, -1, -1))]
    sl = xz(pts, y0, y1)
    for a in (a0, a1): sl = sl.union(cyly(c[0] + r * math.cos(math.radians(a)), c[1] + r * math.sin(math.radians(a)), w, y0, y1))
    return sl

# ---- the ramp: from the tiles under the roller's rear half up to the lane floor (printed, 1/8 in) ----
(ax, az), (bxx, bz) = RAMP
d = (bxx - ax, bz - az); L = math.hypot(*d); n = (-d[1] / L * 0.125, d[0] / L * 0.125)
ramp = xz([(ax, az), (bxx, bz), (bxx - n[0], bz - n[1]), (ax - n[0], az - n[1])], -WALL_Y, WALL_Y)
part(fixed, "ramp (print, PETG)", ramp, BLUE, "print")

# ---- the lane: floor, two polycord strands, walls ----
floor = bx(LANE_X0, RAMP[1][0], -WALL_Y, WALL_Y, FLOOR_Z - T16, FLOOR_Z)
for s in (-1, 1): floor = floor.cut(bx(STRAND_X[1] - 0.45, STRAND_X[0] + 0.45, s * STRAND_Y - 0.15, s * STRAND_Y + 0.15, 0, 2))
part(fixed, "lane_floor (1/16 in polycarbonate, strand slots)", floor, POLY, "cut")
def wall(s):
    y0, y1 = sorted((s * WALL_Y, s * (WALL_Y + T16)))
    w = xz([(-5.1, 0.73), (LANE_X1, 0.73), (LANE_X1, WALL_TOP), (5.6, WALL_TOP), (5.6, 4.7), (4.95, 4.7), (4.95, WALL_TOP),
            (4.0, WALL_TOP), (4.0, 3.4), (2.3, 3.4), (2.3, WALL_TOP), (-1.0, WALL_TOP), (-1.0, CHUTE_TOP), (-5.1, CHUTE_TOP)], y0, y1)
    for x in STRAND_X: w = w.union(bx(x - 0.35, x + 0.35, y0, y1, 0.25, 0.8))                       # ears for the strand shafts' bearings
    for x in STRAND_X: w = w.cut(cyly(x, STRAND_Z, 14 / IN, y0 - 0.1, y1 + 0.1))
    w = w.cut(cyly(*PIVOT, 14 / IN, y0 - 0.1, y1 + 0.1))                                            # the arm's pivot stub
    w = w.cut(ring_slot(PIVOT, ARM_L, 96, 151, 10 / IN, y0 - 0.1, y1 + 0.1))                         # the J shaft lifts in this
    if s > 0: w = w.cut(cyly(*CSHAFT, 14 / IN, y0 - 0.1, y1 + 0.1))                                 # countershaft bearing
    return w
for s, nm in ((1, "L"), (-1, "R")):
    part(fixed, f"lane_wall_{nm} (1/16 in polycarbonate)", wall(s), POLY, "cut")
for i, x in enumerate(STRAND_X):
    part(fixed, f"strand_shaft_{i} (8mm REX, {'125' if i == 0 else '110'} mm)", cyly(x, STRAND_Z, 8 / IN, -WALL_Y - 0.2, (2.85 if i == 0 else WALL_Y + 0.2)), STEEL, "buy")
    for s in (-1, 1):
        part(fixed, f"strand_pulley_{i}{'L' if s > 0 else 'R'} (print, 0.75 in V-groove)", cyly(x, STRAND_Z, STRAND_D, s * STRAND_Y - 0.15, s * STRAND_Y + 0.15), BLUE, "print")
for s in (-1, 1):
    part(fixed, f"floor_strand_{'L' if s > 0 else 'R'} (3/16 in polycord loop, about 14 in)",
         cord((STRAND_X[0], STRAND_Z), (STRAND_X[1], STRAND_Z), STRAND_D / 2 + 3 / 32, STRAND_D / 2 + 3 / 32, s * STRAND_Y), RED, "buy")

# ---- mounts: three 1/8 in aluminium strips under the lane, tabbed up to the rails' lower hole row (z 1.24) ----
for x in STRIPS_X:
    st = bx(x - 0.5, x + 0.5, -RAIL_IN + 0.125, RAIL_IN - 0.125, 0.6, 0.725)
    for s in (-1, 1): st = st.union(bx(x - 0.5, x + 0.5, s * (RAIL_IN - 0.125), s * RAIL_IN, 0.6, 1.6)).cut(cyly(x, 1.24, M4 / IN, -6, 6))
    part(fixed, f"lane_strip_X{x:+.2f} (1/8 in aluminium, bolts to both rails)", st, ALU, "cut")

# ---- the outer J and chute (printed, 1/8 in): arc about the J-wheel's resting axle, then a vertical rear wall ----
cx, cz = J_AXLE
arc = lambda r: [(cx + r * math.cos(q), cz + r * math.sin(q)) for q in [math.radians(-90 - 90 * i / 24) for i in range(25)]]
shell = arc(J_OUT + 0.125) + [(J_BACK - 0.125, CHUTE_TOP), (J_BACK, CHUTE_TOP)] + arc(J_OUT)[::-1]
part(fixed, "outer_J_and_chute (print, PETG, 3 pieces)", xz(shell, -WALL_Y, WALL_Y), BLUE, "print")

# ---- the J-wheel on its floating arms (drawn at rest, on the hard stop) ----
part(arm, "J_wheels (48 mm gecko x2)", cyly(*J_AXLE, 2 * J_R, -J_W / 2, J_W / 2), GREEN, "buy")
part(arm, "J_shaft (8mm REX, 132 mm)", cyly(*J_AXLE, 8 / IN, -2.6, 2.9), STEEL, "buy")
for s in (-1, 1):
    y0, y1 = sorted((s * 2.2, s * 2.45))
    a = bar(PIVOT, J_AXLE, 0.6, y0, y1).cut(cyly(*PIVOT, 14 / IN, y0 - 0.1, y1 + 0.1)).cut(cyly(*J_AXLE, 14 / IN, y0 - 0.1, y1 + 0.1))
    part(arm, f"J_arm_{'L' if s > 0 else 'R'} (print, 6 mm)", a, BLUE, "print")
P16 = 16 * 5 / math.pi / IN                                      # 16T HTD5 pitch diameter
part(arm, "arm_drive (16T HTD5 x2 + belt, 1:1, outside the left arm)", cyly(*J_AXLE, P16, 2.47, 2.85).union(cyly(*PIVOT, P16, 2.47, 2.85))
     .union(bar(PIVOT, J_AXLE, P16 + 0.1, 2.5, 2.82)), BLACK, "buy")
for s in (-1, 1):
    part(fixed, f"pivot_stub_{'L' if s > 0 else 'R'} (8mm REX, in a bearing in the wall)", cyly(*PIVOT, 8 / IN, s * 2.0, s * (3.25 if s > 0 else 2.5)) if s > 0
         else cyly(*PIVOT, 8 / IN, -2.5, -2.0), STEEL, "buy")
part(fixed, "arm_stop (print, on the left wall: the arm rests on it)", bx(PIVOT[0] - 1.6, PIVOT[0] - 0.9, 2.2, 2.45, 3.25, 3.5), BLUE, "print")

# ---- the J motor, over the left rail, belted to the left pivot stub ----
part(fixed, "J_motor (goBILDA 5203-2402-0003, 1620 RPM)", cyly(*JM, 37 / IN, 3.25, 3.25 + 100 / IN), BLACK, "buy")
part(fixed, "J_motor_shaft_pulley (16T HTD5)", cyly(*JM, P16, 2.9, 3.2), BLACK, "buy")
part(fixed, "J_motor_belt (HTD5 9 mm, about 3.3 in centres) and stub pulley (16T)", bar(JM, PIVOT, P16 + 0.1, 2.93, 3.17).union(cyly(*PIVOT, P16, 2.9, 3.2)), BLACK, "buy")
cr = bx(JM[0] - 0.8, JM[0] + 0.8, RAIL_IN, RAIL_IN + 0.47, 2.85, JM[1] - 0.2).cut(cyly(*JM, 37.5 / IN, 4.0, 6.0))
part(fixed, "J_motor_cradle (print, bolts to the left rail's top)", cr, BLUE, "print")

# ---- the lane drive: roller shaft -> countershaft (6.0, 4.0) -> front strand shaft, two polycord loops ----
P24, P16V = 24 / IN, 16 / IN
part(flt, "lane_drive_pulley_roller (print, 24 mm V-groove, in the roller's gap)", cyly(*ROLLER, P24, 2.45, 2.62), BLUE, "print")
part(fixed, "countershaft (8mm REX, 30 mm, one bearing in the left wall)", cyly(*CSHAFT, 8 / IN, WALL_Y, 2.95), STEEL, "buy")
part(fixed, "countershaft_pulleys (print, 24 mm V-groove x2)", cyly(*CSHAFT, P24, 2.45, 2.62).union(cyly(*CSHAFT, P24, 2.66, 2.83)), BLUE, "print")
part(fixed, "lane_shaft_pulley (print, 16 mm V-groove)", cyly(STRAND_X[0], STRAND_Z, P16V, 2.66, 2.83), BLUE, "print")
part(fixed, "lane_drive_lower_loop (3/16 in polycord, about 9 in)", cord(CSHAFT, (STRAND_X[0], STRAND_Z), P24 / 2, P16V / 2, 2.745), RED, "buy")
part(flt, "lane_drive_upper_loop (3/16 in polycord, about 7.5 in)", cord(ROLLER, CSHAFT, P24 / 2, P24 / 2, 2.535), RED, "buy")

# ---- the turret bearing (the launcher's, for reference): goBILDA 3208-0004-0001, 105 mm bore ----
part(fixed, "turret_bearing_REFERENCE (goBILDA 3208-0004-0001; dimensions to confirm)",
     cq.Workplane("XY").circle(6.0 / 2).circle(105 / IN / 2).extrude(1.2).translate((TURRET[0], TURRET[1], CHUTE_TOP)), (0.55, 0.57, 0.6), "buy")

# ---- into the robot CAD's millimetres: (x, y, z)_CAD = (C + Y, F + Z, FACE - 7.56 in + X), all times 25.4 ----
MAT = cq.Matrix([[0, IN, 0, C], [0, 0, IN, F], [IN, 0, 0, FACE - CENTRE_BACK_IN * IN]])
def to_cad(wp):
    shp = wp.val() if len(wp.vals()) == 1 else cq.Compound.makeCompound(wp.vals())
    return shp.transformGeometry(MAT)
GROUPS = (("transfer, fixed", "fixed", fixed), ("lane drive on the roller shaft (floats with the roller)", "float", flt),
          ("J-wheel and arms (lift up to 1.2 in about the pivot)", "arm", arm))
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
    print(len(fixed), "fixed,", len(flt), "float,", len(arm), "arm parts")
