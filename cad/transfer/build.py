"""The intake-to-launcher transfer, v2 (7 Oct 2026): ramp, a short climbing wheel lane under a sprung ceiling, and a
cup of two feeder wheels under the flywheels. Drawn in the robot CAD's frame, so dhs-transfer.step lands in place in
Onshape next to cad/intake-b/dhs-intake-b.step.

    python3 cad/transfer/build.py        # writes dhs-transfer.step and stl/ next to this file

Drawn in the robot frame (+X forward, +Y left, +Z up, inches, origin on the floor under the chassis centre, 7.56 in
behind the front face) and moved into the CAD's millimetres at the end. Groups: "fixed" (ramp, lane, walls, ceiling,
cup, drives) and "float" (nothing yet: the lane no longer runs off the roller's shaft).

How it works: the intake roller pushes each ball up the ramp; two driven wheel shafts carry it up the lane under a
sprung, foam-lined ceiling to the front feeder; it rolls over the stopped front feeder and drops into the cup, sitting
on both feeders under the flywheels. Feeding spins both feeders up into the cup: the ball pops straight up into the
flywheels, and the front feeder's top, moving forward, holds the next ball back. Stopped, they let it roll in.
The mentor's layout from his screenshots (feeders across the robot, front and back of the ball); the numbers are ours.
"""
import math, os, sys
import cadquery as cq
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "robot-addons"))
from build import C, F, FACE, IN

CENTRE_BACK_IN = 7.56
T16, TW = 1 / 16, 1 / 8
LANE_Y = 0.0                            # the lane's centreline (the front drive motors' encoder caps leave |Y| 1.87)
WALL_IN = 1.87                          # the lane walls' inner faces, |Y|: a NECTAR (1.81) clears the caps by 0.06
RN, RP = 1.81, 1.40                     # NECTAR, POLLEN radii
# the cup: two feeder wheels (the mentor's FeederWheel: 66.7 mm foam on a 40 mm hub, 48 mm wide), shafts across Y
RF, FW = 66.68 / 2 / IN, 48 / IN
CUP_X = -2.845                          # under the flywheels' centre (X -3.79..-1.90)
CUP_S = 2.3                             # each feeder's axis this far ahead of / behind the cup's centre
FEED_Z = 1.56                           # feeder axes: their bottoms 0.25 off the tiles
FEED_X = (CUP_X + CUP_S, CUP_X - CUP_S) # front, rear
SEAT_N = FEED_Z + math.sqrt((RN + RF) ** 2 - CUP_S ** 2)   # a NECTAR's centre in the cup (3.67: clear of the flywheels, which start touching at 5.77)
SEAT_P = FEED_Z + math.sqrt((RP + RF) ** 2 - CUP_S ** 2)
# the ramp and the lane: ball-bottom line from the ramp's top to the front feeder's top
RAMP = ((8.0, 0.05), (5.8, 0.9))
LANE_TOP = (FEED_X[0], FEED_Z + RF)
SLOPE = (LANE_TOP[1] - RAMP[1][1]) / (RAMP[1][0] - LANE_TOP[0])
line = lambda x: RAMP[1][1] + SLOPE * (RAMP[1][0] - x)
LANE_WHEELS = [(3.74, 32 / IN / 2, "compliant wheel, 32 mm, 30A (to choose)"), (None, 24 / IN, "48 mm Gecko wheel")]   # the second sits just ahead of the front feeder
CEIL_GAP = 2.6                          # the ceiling's foam face above the ball-bottom line at rest: a POLLEN (2.80) presses the foam 0.2
FOAM = 0.5                              # soft polyethylene/EVA foam (2-3 lb/ft3): the squeeze is in the foam, not the ball
CEIL_X = (6.5, 0.95)                    # from over the ramp's top to just ahead of the front feeder
LINK_L, LINK_DEG = 1.2, 45.0            # parallel links: rest 45 deg down toward the rear; level is 0.85 up (a NECTAR needs 0.82)
LINKS = ((6.3, 5.45), (2.2, 1.35))      # (fixed pivot X, ceiling tab X) front and rear: clear of the drive motors' encoder caps
BEAM = (5.55, 2.4)                      # break-beam across the lane (X, z): counts balls in, so the code stops the intake at the limit
CHAN_RAISE = 21 / IN                    # the old intake's 11-hole channel goes up 21 mm (a lane NECTAR's top is 5.14 under it)
RAIL_IN = 4.88
FM = (0.3, 0.95)                        # the feeder drive's jackshaft (X, z), along Y; miter gears to the feeder motor
FM_Y = 2.9                              # the feeder motor's axis Y (along X, under the left flywheel motor)
LM = (4.75, 1.15)                       # the lane drive's jackshaft (right side)
LM_Y = -3.3                             # the lane motor's axis Y (along X, outside the right wall)
MOTOR_D, MOTOR_L = 37 / IN, 100 / IN

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
WY = lambda s: (s * WALL_IN, s * (WALL_IN + TW))
WO = WALL_IN + TW

def axle_on_line(x, r):
    """Where a wheel of radius r sits so its top touches the lane's ball-bottom line near X x."""
    a = math.atan(SLOPE)
    return (x - r * math.sin(a), line(x) - r * math.cos(a))
# the second lane wheel: as far back as it can go and still clear the front feeder by 0.06 in
_r = LANE_WHEELS[1][1]; _x = LANE_TOP[0] + 1.0
while math.dist(axle_on_line(_x, _r), (FEED_X[0], FEED_Z)) < _r + RF + 0.06: _x += 0.01
LANE_WHEELS[1] = (round(_x, 2), _r, LANE_WHEELS[1][2])

# ---- the ramp (1/16 in polycarbonate) and its two brackets to the side plates, then a short static lane floor ----
first_x = axle_on_line(*LANE_WHEELS[0][:2])[0] + LANE_WHEELS[0][1]
pts = [RAMP[0], RAMP[1], (first_x, line(first_x))]
def sheet(pts, y0, y1, t=T16):
    out = None
    for (x0, z0), (x1, z1) in zip(pts, pts[1:]):
        L = math.hypot(x1 - x0, z1 - z0); nx, nz = -(z1 - z0) / L * t, (x1 - x0) / L * t
        seg = xz([(x0, z0), (x1, z1), (x1 - nx, z1 - nz), (x0 - nx, z0 - nz)], y0, y1)
        out = seg if out is None else out.union(seg)
    return out
part(fixed, "ramp_and_lane_lip (1/16 in polycarbonate, bent at X 5.8)", sheet(pts, -WALL_IN, WALL_IN), POLY, "cut")
for s in (-1, 1):
    br = xz([(7.75, 0.12), (8.15, 0.12), (8.15, 0.3), (7.75, 0.3)], s * WALL_IN, s * 4.0).union(xz([(7.75, 0.12), (8.15, 0.12), (8.15, 1.6), (7.75, 1.6)], s * 4.0, s * 7.56))
    part(fixed, f"ramp_bracket_{'L' if s > 0 else 'R'} (print, to the side plate; slotted +-0.5 in in X)", br, BLUE, "print")

# ---- the lane walls (1/8 in polycarbonate): low under the front drive motors, they carry the lane shafts and the front feeder ----
def wall(s):
    y0, y1 = sorted(WY(s))
    w = xz([(-1.2, 0.3), (6.5, 0.3), (6.5, 2.6), (-1.2, 2.6)], y0, y1)        # ends ahead of the launcher's side plates (X -1.26)
    w = w.cut(cyly(*BEAM, 0.22, y0 - 0.1, y1 + 0.1))                                  # the break-beam's window
    for x, r, _ in LANE_WHEELS: w = w.cut(cyly(*axle_on_line(x, r), 14 / IN, y0 - 0.1, y1 + 0.1))
    w = w.cut(cyly(FEED_X[0], FEED_Z, 14 / IN, y0 - 0.1, y1 + 0.1))
    w = w.cut(cyly(*(FM if s > 0 else LM), 14 / IN, y0 - 0.1, y1 + 0.1))
    return w
for s, nm in ((1, "L"), (-1, "R")): part(fixed, f"lane_wall_{nm} (1/8 in polycarbonate)", wall(s), POLY, "cut")
for s, nm in ((1, "L"), (-1, "R")):
    y0, y1 = sorted(WY(s))
    rp = xz([(FEED_X[1] - 0.85, 0.3), (-4.95, 0.3), (-4.95, 2.4), (FEED_X[1] - 0.85, 2.4)], y0, y1)     # under and behind the launcher's 5-hole channel
    rp = rp.cut(cyly(FEED_X[1], FEED_Z, 14 / IN, y0 - 0.1, y1 + 0.1))
    part(fixed, f"rear_feeder_plate_{nm} (1/8 in aluminium)", rp, ALU, "cut")
    yy0, yy1 = sorted((s * WO, s * RAIL_IN))
    br = bx(-5.95, -5.45, yy0, yy1, 1.0, 1.125).union(bx(-5.95, -5.45, s * (RAIL_IN - 0.125), s * RAIL_IN, 1.0, 1.6)).union(bx(-5.95, -5.45, s * WO, s * (WO + 0.125), 1.0, 1.6))
    part(fixed, f"rear_feeder_bracket_{nm} (1/8 in aluminium, plate to rail)", br, ALU, "cut")
for x in (5.5, 0.2):                    # wall brackets to the rails, under the motors' bodies: low at the front (the drive
    for s in (-1, 1):                   # wheels' shafts are at z 1.9), high at the rear (the feeder motor runs under it)
        y0, y1 = sorted((s * WO, s * RAIL_IN))
        zb = 1.95 if x < 1 else 1.0
        br = bx(x - 0.3, x + 0.3, y0, y1, zb, zb + 0.125).union(bx(x - 0.3, x + 0.3, s * (RAIL_IN - 0.125), s * RAIL_IN, 1.0, max(zb + 0.125, 1.6))).union(bx(x - 0.3, x + 0.3, s * WO, s * (WO + 0.125), 1.0, zb + 0.125))
        part(fixed, f"wall_bracket_X{x:+.1f}_{'L' if s > 0 else 'R'} (1/8 in aluminium, wall to rail over the motors' bodies)", br, ALU, "cut")

# ---- the lane's two driven shafts ----
P16 = 16 * 5 / math.pi / IN
for i, (x, r, nm) in enumerate(LANE_WHEELS):
    ax_ = axle_on_line(x, r)
    part(fixed, f"lane_wheels_{i} ({nm} x2)", cyly(*ax_, 2 * r, LANE_Y - 0.94, LANE_Y + 0.94), BLACK if r > 0.8 else (0.25, 0.25, 0.28), "buy")
    part(fixed, f"lane_shaft_{i} (8mm REX, 1611-0514-4008 bearings in both walls)", cyly(*ax_, 8 / IN, -WO - 0.45, WO + 0.1), STEEL, "buy")
    part(fixed, f"lane_shaft_pulley_{i} (16T HTD5, outside the right wall)", cyly(*ax_, P16, -WO - 0.4, -WO - 0.05), BLACK, "buy")
part(fixed, "lane_jackshaft (8mm REX, outside the right wall) and its 16T", cyly(*LM, 8 / IN, -WALL_IN, LM_Y).union(cyly(*LM, P16, -WO - 0.4, -WO - 0.05)), STEEL, "buy")
ax0, ax1 = axle_on_line(*LANE_WHEELS[0][:2]), axle_on_line(*LANE_WHEELS[1][:2])
part(fixed, "lane_belt (HTD5 9 mm, over the jackshaft and both lane shafts)", cord(LM, ax0, P16 / 2, P16 / 2, -WO - 0.22, t=0.09).union(cord(ax0, ax1, P16 / 2, P16 / 2, -WO - 0.22, t=0.09)), BLACK, "buy")
part(fixed, "lane_motor (goBILDA 5203-2402-0005, 1150 RPM; along X outside the right wall, miter gears to the jackshaft)",
     cq.Workplane("YZ").center(LM_Y, LM[1]).circle(MOTOR_D / 2).extrude(MOTOR_L).translate((LM[0] - 0.2 - MOTOR_L, 0, 0)), BLACK, "buy")
part(fixed, "lane_miter_gears (1:1 pair, 8mm REX bore, to choose)", cq.Workplane("YZ").center(LM_Y, LM[1]).circle(0.4).extrude(0.3).translate((LM[0] - 0.2, 0, 0)), (0.7, 0.6, 0.3), "buy")

# ---- the sprung, foam-lined ceiling on parallel links: it lifts evenly along its length (0.82 for a NECTAR anywhere) ----
c0, c1 = (CEIL_X[0], line(CEIL_X[0]) + CEIL_GAP), (CEIL_X[1], line(CEIL_X[1]) + CEIL_GAP)
def strip(z_lo, t, y):                 # a band along the ceiling line, z_lo..z_lo+t above the foam face
    return xz([(c0[0], c0[1] + z_lo), (c1[0], c1[1] + z_lo), (c1[0], c1[1] + z_lo + t), (c0[0], c0[1] + z_lo + t)], -y, y)
part(fixed, f"ceiling_foam ({FOAM} in soft polyethylene or EVA foam, 2-3 lb/ft3, glued under the ceiling)", strip(0, FOAM, WALL_IN - 0.15), (0.3, 0.3, 0.32), "buy")
ceil = strip(FOAM, T16, WALL_IN - 0.05)
top = lambda x: line(x) + CEIL_GAP + FOAM + T16
a_ = math.radians(LINK_DEG)
for fx, tx in LINKS:                    # tabs from the ceiling out over the walls to the links
    ceil = ceil.union(bx(tx - 0.25, tx + 0.25, -WO, WO, top(tx) - 0.06, top(tx)))
part(fixed, "ceiling (1/16 in polycarbonate on four parallel links; band-held down)", ceil, POLY, "cut")
for s in (-1, 1):
    y0, y1 = sorted(WY(s))
    for i, (fx, tx) in enumerate(LINKS):
        pz = top(tx) + LINK_L * math.sin(a_)
        post = bx(fx - 0.2, fx + 0.2, y0, y1, 2.6, pz + 0.25).cut(cyly(fx, pz, 0.13, -3, 3))
        part(fixed, f"ceiling_link_post_{'front' if i == 0 else 'rear'}_{'L' if s > 0 else 'R'} (print, on the wall's top edge)", post, BLUE, "print")
        ly0, ly1 = sorted((s * WO, s * (WO + 0.125)))
        part(fixed, f"ceiling_link_{'front' if i == 0 else 'rear'}_{'L' if s > 0 else 'R'} (1/8 in aluminium, {LINK_L} in centres)", bar((fx, pz), (tx, top(tx) - 0.03), 0.35, ly0, ly1), ALU, "cut")
    post = bx(0.55, 0.95, s * WO, s * (WO + 0.3), 1.5, 2.0)
    for dx in (-0.12, 0.0, 0.12): post = post.cut(cyly(0.75 + dx, 1.75, 0.08, -3, 3))
    part(fixed, f"ceiling_band_post_{'L' if s > 0 else 'R'} (print, three holes; band up to the rear tab: about 1-2 lbf preload, 2 lbf/in)", post, BLUE, "print")
    bb = bx(BEAM[0] - 0.25, BEAM[0] + 0.25, s * WO, s * (WO + 0.45), BEAM[1] - 0.35, BEAM[1] + 0.35).cut(cyly(*BEAM, 0.22, -3, 3))
    part(fixed, f"break_beam_bracket_{'L' if s > 0 else 'R'} (print; holds one half of an IR break-beam pair, e.g. Adafruit 2167, across the lane)", bb, BLUE, "print")

# ---- the cup: two feeders under the flywheels ----
for nm, x in (("front", FEED_X[0]), ("rear", FEED_X[1])):
    part(fixed, f"feeder_{nm} (mentor's FeederWheel: 66.7 mm foam on a 40 mm hub, 48 mm wide)", cyly(x, FEED_Z, 2 * RF, LANE_Y - FW / 2, LANE_Y + FW / 2), (0.55, 0.8, 0.95), "buy")
    part(fixed, f"feeder_shaft_{nm} (8mm REX, bearings in the {'lane walls' if nm == 'front' else 'rear feeder plates'})", cyly(x, FEED_Z, 8 / IN, -WO - 0.1, WO + 0.1), STEEL, "buy")
    part(fixed, f"feeder_pulley_{nm} (print, 20 mm V-groove, inside the lane beside the feeder)", cyly(x, FEED_Z, 20 / IN, LANE_Y + FW / 2 + 0.05, LANE_Y + FW / 2 + 0.3), BLUE, "print")
P20 = 20 / IN
part(fixed, "feeder_jackshaft (8mm REX, from the miter gears into the lane) and its pulley", cyly(*FM, 8 / IN, LANE_Y + FW / 2 + 0.3, FM_Y - 0.5).union(cyly(*FM, P20, LANE_Y + FW / 2 + 0.05, LANE_Y + FW / 2 + 0.3)), STEEL, "buy")
yb = LANE_Y + FW / 2 + 0.175
part(fixed, "feeder_belt_front (3/16 in polycord, jackshaft to the front feeder)", cord(FM, (FEED_X[0], FEED_Z), P20 / 2, P20 / 2, yb), RED, "buy")
part(fixed, "feeder_belt_rear (3/16 in polycord, crossed: the rear feeder turns the other way)", bar((FEED_X[0], FEED_Z + P20 / 2), (FEED_X[1], FEED_Z - P20 / 2), 3 / 16, yb - 0.09, yb + 0.09).union(bar((FEED_X[0], FEED_Z - P20 / 2), (FEED_X[1], FEED_Z + P20 / 2), 3 / 16, yb - 0.09, yb + 0.09)), RED, "buy")
part(fixed, "feeder_motor (goBILDA 5203-2402-0003, 1620 RPM; along X under the left flywheel motor)",
     cq.Workplane("YZ").center(FM_Y, FM[1]).circle(MOTOR_D / 2).extrude(MOTOR_L).translate((FM[0] - 0.45 - MOTOR_L, 0, 0)), BLACK, "buy")
part(fixed, "feeder_miter_gears (1:1 pair, 8mm REX bore, to choose)", cq.Workplane("YZ").center(FM_Y, FM[1]).circle(0.4).extrude(0.3).translate((FM[0] - 0.45, 0, 0)), (0.7, 0.6, 0.3), "buy")

# ---- into the robot CAD's millimetres: (x, y, z)_CAD = (C + Y, F + Z, FACE - 7.56 in + X), all times 25.4 ----
MAT = cq.Matrix([[0, IN, 0, C], [0, 0, IN, F], [IN, 0, 0, FACE - CENTRE_BACK_IN * IN]])
def to_cad(wp):
    shp = wp.val() if len(wp.vals()) == 1 else cq.Compound.makeCompound(wp.vals())
    return shp.transformGeometry(MAT)
GROUPS = (("transfer, fixed", "fixed", fixed),)
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
