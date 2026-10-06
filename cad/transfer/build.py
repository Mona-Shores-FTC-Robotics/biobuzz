"""The intake-to-turret transfer (issue #164, doc/transfer.md on spike/164-transfer, concept A), in the robot CAD's
frame, so dhs-transfer.step lands in place in Onshape next to cad/intake-b/dhs-intake-b.step.

    python3 cad/transfer/build.py        # writes dhs-transfer.step and stl/ next to this file

Drawn in the robot frame the transfer chat uses (+X forward, +Y left, +Z up, inches, origin on the floor under the
chassis centre, 7.56 in behind the front face) and moved into the CAD's millimetres at the end. Groups: "fixed"
(the lane, strands, chute, mounts, J motor), "float" (the lane-drive pulley on the roller's shaft, which rises with
the roller), "arm" (the J-wheel, its shaft and arms: they lift up to 1.2 in for a NECTAR, about the pivot).
Follows the doc's "Build spec for CAD". Where this CAD differs (the arm at 20 deg, not 30, so the J motor clears the
left rail; the lane floor ending at the ramp) the README says why.
"""
import math, os, sys
import cadquery as cq
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "robot-addons"))
from build import C, F, FACE, IN

CENTRE_BACK_IN = 7.56
T16 = 1 / 16                            # polycarbonate
FLOOR_Z, WALL_Y, WALL_TOP = 0.9, 2.1, 5.0
LANE_X0, LANE_X1 = -1.3, 7.2            # walls along the lane (they carry on back as the chute's sides, to X -5.1)
STRAND_Y, STRAND_X, STRAND_Z, STRAND_D = 0.5, (5.6, -1.0), 0.65, 0.5    # build spec: Y +-0.5, 0.5 in pulleys on 6 mm D-shafts
J_AXLE, ARM_L = (-1.32, 4.54), 60 / IN  # 60 mm: a 40T HTD5 belt on two 16T pulleys
ARM_DEG = 20.0                          # arm above horizontal toward the rear. The spec's 30 deg puts the J motor 0.22 in into the
                                        # left rail; at 20 deg it clears by 0.15, and the queue's push still closes the arm (< 42 deg)
PIVOT = (J_AXLE[0] + ARM_L * math.cos(math.radians(ARM_DEG)), J_AXLE[1] - ARM_L * math.sin(math.radians(ARM_DEG)))
LIFT_DEG = math.degrees(1.2 / ARM_L)    # 1.2 in of travel at the axle, perpendicular to the arm
J_R, J_W = 24.0 / IN, 2.0               # 48 mm gecko wheels, two side by side
J_OUT, J_BACK, CHUTE_TOP = 3.64, -4.96, 6.6
RAMP = ((8.0, 0.05), (5.8, 0.9))
CSHAFT = (6.0, 4.0)                     # the lane drive's countershaft, Y +2.6, on the left wall's outer face
ROLLER = (8.56, 3.35)                   # the roller's axle at rest (it floats up 1.3 in)
CHAN_Z = (4.75, 6.32)                   # the old intake's 11-hole channel, raised 8 mm
TURRET = (-3.17, 0.0)
TCHAN = ((-5.6, -5.1), (0.4, 0.9))      # the turret's two cross-channels (the launcher's), tops at z 6.6. The spec's front one
                                        # (X 0.0..0.5) is in the J-wheel's way at full float: the wheel moves forward as it lifts
MOTOR_D, MOTOR_L = 37 / IN, 3.5

fixed, flt, arm = {}, {}, {}
def part(d, name, wp, col, kind): d[name] = (wp, col, kind)
BLUE, ALU, STEEL, POLY, BLACK, GREEN, RED = (0.18, 0.37, 0.62), (0.75, 0.78, 0.82), (0.8, 0.82, 0.85), (0.6, 0.78, 0.96), (0.13, 0.15, 0.17), (0.35, 0.66, 0.31), (0.75, 0.2, 0.2)

# ---- helpers, all in robot-frame inches (X fwd, Y left, Z up) ----
def bx(x0, x1, y0, y1, z0, z1):
    return cq.Workplane("XY").box(abs(x1 - x0), abs(y1 - y0), abs(z1 - z0)).translate(((x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2))
def cyly(x, z, d, y0, y1):
    """A cylinder along Y."""
    y0, y1 = min(y0, y1), max(y0, y1)
    return cq.Workplane("XZ").center(x, z).circle(d / 2).extrude(-(y1 - y0)).translate((0, y0, 0))
def xz(pts, y0, y1):
    """A profile drawn in X-Z, extruded across Y from y0 to y1."""
    y0, y1 = min(y0, y1), max(y0, y1)
    return cq.Workplane("XZ").polyline(pts).close().extrude(-(y1 - y0)).translate((0, y0, 0))
def bar(p0, p1, w, y0, y1):
    """A flat bar in X-Z between two points, rounded ends."""
    y0, y1 = min(y0, y1), max(y0, y1)
    (x0, z0), (x1, z1) = p0, p1
    L = math.hypot(x1 - x0, z1 - z0); a = math.degrees(math.atan2(z1 - z0, x1 - x0))
    b = cq.Workplane("XZ").center(L / 2, 0).rect(L, w).extrude(-(y1 - y0))
    b = b.union(cq.Workplane("XZ").circle(w / 2).extrude(-(y1 - y0))).union(cq.Workplane("XZ").center(L, 0).circle(w / 2).extrude(-(y1 - y0)))
    return b.rotate((0, 0, 0), (0, 1, 0), -a).translate((x0, y0, z0))
def cord(p0, p1, r0, r1, y, t=3 / 16):
    """A belt loop between pulleys of pitch radius r0, r1: its two straight runs."""
    (x0, z0), (x1, z1) = p0, p1; dx, dz = x1 - x0, z1 - z0; L = math.hypot(dx, dz)
    nx, nz = -dz / L, dx / L; out = None
    for s in (1, -1):
        piece = bar((x0 + s * nx * r0, z0 + s * nz * r0), (x1 + s * nx * r1, z1 + s * nz * r1), t, y - t / 2, y + t / 2)
        out = piece if out is None else out.union(piece)
    return out
def arc_slot(c, r, a0, a1, w, y0, y1, n=16):
    """An arc-shaped slot about centre c, from angle a0 to a1 (degrees, X-Z)."""
    pts = [(c[0] + (r + s * w / 2) * math.cos(math.radians(a0 + (a1 - a0) * i / n)), c[1] + (r + s * w / 2) * math.sin(math.radians(a0 + (a1 - a0) * i / n)))
           for s in (1, -1) for i in (range(n + 1) if s == 1 else range(n, -1, -1))]
    sl = xz(pts, y0, y1)
    for a in (a0, a1): sl = sl.union(cyly(c[0] + r * math.cos(math.radians(a)), c[1] + r * math.sin(math.radians(a)), w, y0, y1))
    return sl
WY = lambda s: (s * WALL_Y, s * (WALL_Y + T16))       # a wall's two faces

# ---- the ramp (1/16 in polycarbonate) and its two brackets to the side plates ----
(ax, az), (bxx, bz) = RAMP
dvec = (bxx - ax, bz - az); L = math.hypot(*dvec); nrm = (-dvec[1] / L * T16, dvec[0] / L * T16)
part(fixed, "ramp (1/16 in polycarbonate)", xz([(ax, az), (bxx, bz), (bxx - nrm[0], bz - nrm[1]), (ax - nrm[0], az - nrm[1])], -WALL_Y, WALL_Y), POLY, "cut")
for s in (-1, 1):
    br = xz([(7.75, 0.12), (8.15, 0.12), (8.15, 0.3), (7.75, 0.3)], s * WALL_Y, s * 4.0).union(xz([(7.75, 0.12), (8.15, 0.12), (8.15, 1.6), (7.75, 1.6)], s * 4.0, s * 7.56))
    part(fixed, f"ramp_bracket_{'L' if s > 0 else 'R'} (print, to the side plate; slotted +-0.5 in in X)", br, BLUE, "print")

# ---- the lane: floor, walls, strands ----
floor = bx(LANE_X0, RAMP[1][0], -WALL_Y, WALL_Y, FLOOR_Z - T16, FLOOR_Z)
for x in STRAND_X:
    for s in (-1, 1): floor = floor.cut(bx(x - 0.32, x + 0.32, s * STRAND_Y - 0.15, s * STRAND_Y + 0.15, 0, 2))
part(fixed, "lane_floor (1/16 in polycarbonate, slots for the strand pulleys)", floor, POLY, "cut")
def wall(s):
    y0, y1 = WY(s)
    w = xz([(-5.1, 0.35), (LANE_X1, 0.35), (LANE_X1, WALL_TOP), (5.55, WALL_TOP), (5.55, 4.4), (5.0, 4.4), (5.0, WALL_TOP),
            (4.0, WALL_TOP), (4.0, 3.4), (2.3, 3.4), (2.3, WALL_TOP), (-1.0, WALL_TOP), (-1.0, CHUTE_TOP), (-5.1, CHUTE_TOP)], y0, y1)
    for x in STRAND_X: w = w.cut(cyly(x, STRAND_Z, 10 / IN, y0 - 0.1, y1 + 0.1))           # 6 mm-bore flanged bearings
    w = w.cut(cyly(*PIVOT, 14 / IN, y0 - 0.1, y1 + 0.1))
    w = w.cut(arc_slot(PIVOT, ARM_L, 180 - ARM_DEG - LIFT_DEG - 3, 180 - ARM_DEG + 3, 10 / IN, y0 - 0.1, y1 + 0.1))   # the J shaft floats in this
    if s > 0: w = w.cut(cyly(*CSHAFT, 14 / IN, y0 - 0.1, y1 + 0.1))
    return w
for s, nm in ((1, "L"), (-1, "R")): part(fixed, f"lane_wall_{nm} (1/16 in polycarbonate)", wall(s), POLY, "cut")
for i, x in enumerate(STRAND_X):
    part(fixed, f"strand_shaft_{i} (6 mm D-shaft, {'goBILDA 2100 series, to confirm; carries the lane pulley' if i == 0 else 'idler'})",
         cyly(x, STRAND_Z, 6 / IN, -WALL_Y - 0.2, 2.85 if i == 0 else WALL_Y + 0.2), STEEL, "buy")
    for s in (-1, 1):
        part(fixed, f"strand_pulley_{i}{'L' if s > 0 else 'R'} (print, 0.5 in V-groove)", cyly(x, STRAND_Z, STRAND_D, s * STRAND_Y - 0.12, s * STRAND_Y + 0.12), BLUE, "print")
for s in (-1, 1):
    part(fixed, f"floor_strand_{'L' if s > 0 else 'R'} (3/16 in 83A polycord loop, about 14.8 in)",
         cord((STRAND_X[0], STRAND_Z), (STRAND_X[1], STRAND_Z), STRAND_D / 2 - 0.03, STRAND_D / 2 - 0.03, s * STRAND_Y), RED, "buy")
# hangers from the raised 11-hole channel to the walls' outer faces
for s in (-1, 1):
    part(fixed, f"lane_hanger_{'L' if s > 0 else 'R'} (print, bolts under the 11-hole channel and to the wall)",
         bx(5.03, 5.51, s * (WALL_Y + T16), s * (WALL_Y + 0.55), 3.6, CHAN_Z[0]), BLUE, "print")

# ---- the outer J and chute (printed PETG, two halves) and the lid over the pocket ----
cx, cz = J_AXLE
arc = lambda r: [(cx + r * math.cos(q), cz + r * math.sin(q)) for q in [math.radians(-90 - 90 * i / 24) for i in range(25)]]
shell = arc(J_OUT + 0.125) + [(J_BACK - 0.125, CHUTE_TOP), (J_BACK, CHUTE_TOP)] + arc(J_OUT)[::-1]
part(fixed, "outer_J_and_chute (print, PETG, 1/8 in, two halves; bolts to the walls and the turret's rear channel)", xz(shell, -WALL_Y, WALL_Y), BLUE, "print")
part(fixed, "queue_lid (1/16 in polycarbonate; ahead of the J-wheel's float)", bx(0.45, 2.3, -WALL_Y, WALL_Y, WALL_TOP - T16, WALL_TOP), POLY, "cut")

# ---- the J-wheel on its floating arms (drawn at rest, on the hard stops) ----
part(arm, "J_wheels (48 mm gecko x2)", cyly(*J_AXLE, 2 * J_R, -J_W / 2, J_W / 2), GREEN, "buy")
part(arm, "J_shaft (8mm REX, 130 mm)", cyly(*J_AXLE, 8 / IN, -2.4 - 0.15, 2.75), STEEL, "buy")
P16 = 16 * 5 / math.pi / IN                                       # 16T HTD5 pitch diameter
part(arm, "J_pulley (16T HTD5, 3417-4008-0016, to confirm)", cyly(*J_AXLE, P16, 2.2, 2.55), BLACK, "buy")
for s, y in ((1, 2.6), (-1, -2.4)):
    a = bar(PIVOT, J_AXLE, 0.6, y, y + s * 0.125).cut(cyly(*PIVOT, 14 / IN, y - 1, y + 1)).cut(cyly(*J_AXLE, 8.3 / IN, y - 1, y + 1))
    part(arm, f"J_arm_{'L' if s > 0 else 'R'} (1/8 in aluminium, 60 mm)", a, ALU, "cut")
part(arm, "J_belt (HTD5 9 mm, 40T)", cord(PIVOT, J_AXLE, P16 / 2, P16 / 2, 2.375, t=0.09).union(cyly(*PIVOT, P16, 2.2, 2.55)), BLACK, "buy")   # drawn with the arm; the motor pulley turns on the pivot
for s, y0, y1 in ((1, WALL_Y + T16, 2.95), (-1, -(WALL_Y + T16), -2.75)):
    sx, sz = PIVOT[0] - 1.2, PIVOT[1] + 1.2 * math.tan(math.radians(ARM_DEG)) - 0.3 / math.cos(math.radians(ARM_DEG))  # under the arm, 1.2 in behind the pivot
    if s > 0: y0 = 2.73                                           # left: outboard of the arm (the belt is inboard), from the motor bracket
    part(fixed, f"arm_stop_{'L' if s > 0 else 'R'} (print, on a +-0.1 in slot)", bx(sx - 0.25, sx + 0.25, y0, y1, sz - 0.45, sz - 0.01), BLUE, "print")
    part(fixed, f"band_post_{'L' if s > 0 else 'R'} (print, three holes; 1/4 in surgical tubing to the arm's tip)",
         bx(1.0, 1.4, (WALL_Y + T16) * s, 2.95 * s, 5.2, 5.6), BLUE, "print")

# ---- the J motor: its output shaft is the arm's left pivot ----
part(fixed, "J_motor (goBILDA 5203-2402-0003, 1620 RPM)", cyly(*PIVOT, MOTOR_D, 2.75, 2.75 + MOTOR_L), BLACK, "buy")
part(fixed, "J_motor_shaft (its own 8 mm REX output; the left pivot)", cyly(*PIVOT, 8 / IN, 2.2, 2.75), STEEL, "buy")
mb = bx(PIVOT[0] - 0.9, PIVOT[0] + 0.9, 2.75, 2.95, PIVOT[1] - 0.9, PIVOT[1] - 0.45).union(bx(PIVOT[0] + 0.62, PIVOT[0] + 1.0, WALL_Y + T16, 2.95, PIVOT[1] - 0.9, PIVOT[1] + 0.2))
part(fixed, "J_motor_bracket (print: the motor's face to the left wall, round the arm and belt)", mb, BLUE, "print")
part(fixed, "right_pivot_block (print, on the right wall, with a dead 8 mm stub)", bx(PIVOT[0] - 0.4, PIVOT[0] + 0.4, -(WALL_Y + T16), -2.35, PIVOT[1] - 0.4, PIVOT[1] + 0.4)
     .cut(cyly(*PIVOT, 8.3 / IN, -2.5, -2.0)).union(cyly(*PIVOT, 8 / IN, -2.6, -2.35)), BLUE, "print")

# ---- the lane drive, 2:1 up: roller shaft (32 mm) -> countershaft (16 mm, 16 mm) -> front strand shaft (16 mm) ----
P32, P16V = 32 / IN, 16 / IN
part(flt, "lane_drive_pulley_roller (print, 32 mm V-groove, in the roller's gap)", cyly(*ROLLER, P32, 2.45, 2.62), BLUE, "print")
part(flt, "lane_drive_upper_loop (3/16 in polycord, about 8.3 in)", cord(ROLLER, CSHAFT, P32 / 2, P16V / 2, 2.535), RED, "buy")
part(fixed, "countershaft (8mm REX, 30 mm, one 1611-0514-4008 on the left wall)", cyly(*CSHAFT, 8 / IN, WALL_Y, 2.95), STEEL, "buy")
part(fixed, "countershaft_pulleys (print, 16 mm V-groove x2)", cyly(*CSHAFT, P16V, 2.45, 2.62).union(cyly(*CSHAFT, P16V, 2.66, 2.83)), BLUE, "print")
part(fixed, "lane_shaft_pulley (print, 16 mm V-groove, on the front strand shaft)", cyly(STRAND_X[0], STRAND_Z, P16V, 2.66, 2.83), BLUE, "print")
part(fixed, "lane_drive_lower_loop (3/16 in polycord, about 8.7 in)", cord(CSHAFT, (STRAND_X[0], STRAND_Z), P16V / 2, P16V / 2, 2.745), RED, "buy")

# ---- the turret bearing and its two cross-channels (the launcher's; drawn for reference and fit) ----
part(fixed, "turret_bearing_REFERENCE (goBILDA 3208-0004-0001; outer size to confirm)",
     cq.Workplane("XY").circle(6.0 / 2).circle(105 / IN / 2).extrude(1.2).translate((TURRET[0], TURRET[1], CHUTE_TOP)), (0.55, 0.57, 0.6), "buy")
for x0, x1 in TCHAN:
    part(fixed, f"turret_channel_X{x0:+.1f}_REFERENCE (1120-series U-channel across the rails; its supports are the launcher's)",
         bx(x0, x1, -5.35, 5.35, CHUTE_TOP - 0.94, CHUTE_TOP), ALU, "buy")

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
