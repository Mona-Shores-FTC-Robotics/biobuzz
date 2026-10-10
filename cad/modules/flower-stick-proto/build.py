"""Fixed FLOWER stick prototype for the mentor's robot as he drew it on 9 Oct (issue #179). It's the ramp-hook FLOWER
block (cad/ramp-hook/parts.py) on an 8 mm REX shaft across his front, held at both ends by goBILDA Hyper Hubs on goBILDA
grid plates. The plates bolt to the 4 mm holes his front uprights' webs already have: no drilling. It doesn't fold, so
it's for the practice field: his robot is 19.5 in long with it (the 18 in start, R102, is broken; deployed, 18 x 24 in,
R105, is kept). Robot frame, inches: X forward from the centre, Y left, z up from the tiles. His frame from his intake's
uprights.

    python3 cad/modules/flower-stick-proto/build.py     # writes flower-stick-proto.step and stl/ next to this file

Per side, everything on goBILDA's 8 mm grid, counted from his web's holes:
  plate A (1116-0056-0056, 7 x 7) flat on the upright's web outer face: 2 x M4 through the web's holes at
          X 5.98 / 7.24, z 3.48, nuts inside the channel (the flaps module uses the same two holes);
  plate B (1116-0040-0136, 5 x 17) outboard of A, overlapping its two bottom rows: it runs forward under his corner
          wheel (B's top is z 2.06, the wheel's bottom 2.22) to the hub;
  a 1310-0016-4008 Hyper Hub (8mm REX, clamping) on B's inner face, 4 x M4 through B into the hub;
the 264 mm shaft (2106-4008-2640) between the two hubs, and the block on it. Height: the shaft is at z 0.96 (B's grid);
the block's height is chosen by which of the three printed blocks goes on (bottom 0.60 / 0.65 / 0.70 in). Reach: B on
A's columns 1-17 (REACH = 0) or 2-18 (REACH = 1) moves the block 8 mm.
tools/robot-cad/module_check.py flower_stick / flower_stick_seated checks it against his Robot.step.
"""
import math, os, sys, importlib.util
import cadquery as cq
HERE = os.path.dirname(os.path.abspath(__file__))
def load(n, p):
    s = importlib.util.spec_from_file_location(n, p); m = importlib.util.module_from_spec(s); s.loader.exec_module(m); return m
TR = load("transfer", os.path.join(HERE, "..", "..", "transfer", "build.py"))
MM = 1 / 25.4
G = 8 * MM                   # goBILDA's grid

# ---- his front, measured from his 9 Oct Robot.step ----
WEB_Y = 5.36                 # the uprights' web, outer face (|Y| 5.35), plus 0.01 clearance
COL0, ROW0 = 5.98, 3.48      # the web's 4 mm hole (X, z) we bolt through; its partner is at X 7.24 (col 4). The row below
                             # (z 3.17) has his Dual Block behind it, and the rows above have his roller carriage's
                             # V-groove bearings and V-rail behind them, so these two are the only holes with room for a nut
# ---- the module ----
REACH = int(os.environ.get("REACH", 0))     # 0: B on A's columns 1-17; 1: one column forward (8 mm)
T = 2.5 * MM                 # grid plate thickness (goBILDA's STEP)
A_COLS, A_ROWS = (0, 6), (0, 6)             # plate A, 7 x 7 (rows count down from ROW0)
B_COLS, B_ROWS = (1 + REACH, 17 + REACH), (5, 9)   # plate B, 17 x 5
HUB_COL, SHAFT_ROW = 16 + REACH, 8
HUB_D, HUB_L = 24 * MM, 22 * MM             # 1310-0016-4008
SHAFT_L = 264 * MM                          # 2106-4008-2640
COLLAR_D, COLLAR_L = 20 * MM, 9 * MM        # 2910-0920-4008
COLLAR_Y = 1.45              # collars' inner faces: just outside the FLOWER's grey uprights (|Y| 0.62 to 1.37)
SHAFT_X, SHAFT_Z = COL0 + HUB_COL * G, ROW0 - SHAFT_ROW * G
# ---- the block: cad/ramp-hook/parts.py's shape, the shaft 12 mm behind its tip ----
DEPTH, FLAT, ARC_R, ROD_BACK = 1.4, 0.5, 1.36, 12 * MM
BLOCKS = {"060": 0.60, "065": 0.65, "070": 0.70}   # bottom heights to print; the top is 0.65 in above the bottom
BLOCK = os.environ.get("BLOCK", "065")
TIP_X = SHAFT_X + ROD_BACK

def gx(c): return COL0 + c * G
def gz(r): return ROW0 - r * G
def box(x0, x1, y0, y1, z0, z1):
    return cq.Workplane("XY").box(x1 - x0, y1 - y0, z1 - z0).translate(((x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2))
def ycyl(x, z, y0, y1, d):
    return cq.Workplane("XZ").center(x, z).circle(d / 2).extrude(-(y1 - y0)).translate((0, y0, 0))

def plate(cols, rows, y0):
    return box(gx(cols[0]) - G / 2, gx(cols[1]) + G / 2, y0, y0 + T, gz(rows[1]) - G / 2, gz(rows[0]) + G / 2)

def screw(x, z, y_head, y_tip, nut_at=None):
    """An M4 socket head (left side): the head outboard of y_head, the shank inward to y_tip; a nylon lock nut whose
    outer face is at nut_at, if given."""
    out = ycyl(x, z, y_head, y_head + 4 * MM, 7 * MM).union(ycyl(x, z, y_tip, y_head, 4 * MM))
    if nut_at is not None:
        out = out.union(box(x - 4.05 * MM, x + 4.05 * MM, nut_at - 4.7 * MM, nut_at, z - 3.5 * MM, z + 3.5 * MM))   # flats up and down
    return out

def block(bottom):
    top = bottom + 0.65
    back = TIP_X - DEPTH
    side = (cq.Workplane("XZ").polyline([(back, bottom), (back + DEPTH - FLAT, top), (TIP_X, top), (TIP_X, bottom)]).close()
            .extrude(ARC_R + 0.1, both=True))
    plan = cq.Workplane("XY").center(TIP_X - ARC_R, 0).circle(ARC_R).extrude(1).translate((0, 0, bottom))
    return side.intersect(plan).cut(ycyl(SHAFT_X, SHAFT_Z, -2, 2, 8.3 * MM))

def side_parts(s):
    """One side's parts, left (s = 1) or right (s = -1), as {name: (workplane, colour, kind)}. Built on the left, mirrored."""
    p = {}
    yA, yB = WEB_Y, WEB_Y + T
    p["plate_A (goBILDA 1116-0056-0056 grid plate, 7 x 7) on the upright's web"] = (plate(A_COLS, A_ROWS, yA), (0.75, 0.75, 0.78), "buy")
    p["plate_B (goBILDA 1116-0040-0136 grid plate, 5 x 17) outboard of A, to the hub"] = (plate(B_COLS, B_ROWS, yB), (0.75, 0.75, 0.78), "buy")
    p["hub (goBILDA 1310-0016-4008 Hyper Hub, 8mm REX, clamping) on B's inner face"] = (ycyl(SHAFT_X, SHAFT_Z, yB - HUB_L, yB, HUB_D), (0.85, 0.7, 0.2), "buy")
    sc = []
    for c in (0, 4):                         # A to the web: M4 x 12, nuts inside the channel, flats up (his Dual Block is
        sc.append(screw(gx(c), gz(0), yA + T, WEB_Y - 0.01 - 2.5 * MM - 5 * MM, nut_at=WEB_Y - 0.01 - 2.5 * MM))   # 0.3 mm under them)
    for c in (B_COLS[0], 6):                 # B to A: M4 x 12, nuts on A's inner face
        for r in (5, 6):
            sc.append(screw(gx(c), gz(r), yB + T, yA - 3 * MM, nut_at=yA))
    for c in (HUB_COL - 1, HUB_COL + 1):     # B into the hub's tapped 16 mm pattern: M4 x 8
        for r in (SHAFT_ROW - 1, SHAFT_ROW + 1):
            sc.append(screw(gx(c), gz(r), yB + T, yB - 5.5 * MM))
    w = sc[0]
    for x in sc[1:]: w = w.union(x)
    p["screws (goBILDA M4 socket heads, 2812-0004-0007 lock nuts)"] = (w, (0.1, 0.1, 0.1), "buy")
    p["collar (goBILDA 2910-0920-4008 clamping collar): a backstop for the block's set screws"] = (ycyl(SHAFT_X, SHAFT_Z, COLLAR_Y, COLLAR_Y + COLLAR_L, COLLAR_D), (0.85, 0.7, 0.2), "buy")
    if s < 0:
        p = {n: (wp.mirror("XZ"), c, k) for n, (wp, c, k) in p.items()}
    return {f"{n.split(' ')[0]}_{'LR'[s < 0]} {n.split(' ', 1)[1]}": v for n, v in p.items()}

def build(blk=BLOCK):
    p = {}
    for s in (1, -1): p.update(side_parts(s))
    p["shaft (goBILDA 2106-4008-2640 8mm REX shaft, 264 mm, uncut) hub to hub"] = (ycyl(SHAFT_X, SHAFT_Z, -SHAFT_L / 2, SHAFT_L / 2, 8 * MM), (0.6, 0.6, 0.62), "buy")
    p[f"block_{blk} (print, PETG: cad/ramp-hook's FLOWER block, bottom {BLOCKS[blk]:.2f} in, 2 x M3 set screws onto the shaft's flats)"] = (block(BLOCKS[blk]), (0.95, 0.55, 0.1), "print")
    return p
parts = build()

# The FLOWER seated on the block (tools/robot-cad/flower.py's FLOWER): the block's curved front, radius ARC_R, touches the
# grey uprights' inner corners (1.25 in beyond the FLOWER's centre, |Y| 0.62).
SEAT_D = TIP_X - ARC_R + math.sqrt(ARC_R ** 2 - 0.62 ** 2) - 1.25 - 7.56   # FLOWER centre, in ahead of his face

HIS_ALIGN_MM = (0.27, 35.04)        # his 9 Oct Robot.step -> our robot CAD frame (cad/full-robot placed_team)
def his_frame(wp):
    return TR.to_cad(wp).translate(cq.Vector(-HIS_ALIGN_MM[0], 0, -HIS_ALIGN_MM[1]))

if __name__ == "__main__":
    os.makedirs(os.path.join(HERE, "stl"), exist_ok=True)
    rh = load("ramp_parts", os.path.join(HERE, "..", "..", "ramp-hook", "parts.py"))
    IN = rh.IN
    for tag, bottom in BLOCKS.items():       # the printed blocks, flat on the bed: the shaft at z 0.96 in each
        rh.BOTTOM, rh.TOP, rh.ROD_Z = bottom * IN, (bottom + 0.65) * IN, SHAFT_Z * IN
        m = rh.ramp_block(); m.apply_translation([0, 0, -m.bounds[0][2]])
        m.export(os.path.join(HERE, "stl", f"block_{tag}.stl"))
        print(f"block_{tag}: bottom {bottom:.2f} in, top {bottom + 0.65:.2f}, wall under the shaft "
              f"{(SHAFT_Z - bottom) * IN - (rh.ROD_D + rh.FIT) / 2:.1f} mm, watertight {m.is_watertight}")
    assy = cq.Assembly(name="FLOWER stick prototype, fixed (on the mentor's robot)")
    for n, (wp, col, kind) in parts.items():
        assy.add(his_frame(wp), name=n.split(" ")[0], color=cq.Color(*col))
    assy.save(os.path.join(HERE, "flower-stick-proto.step"))
    bb = [wp.val().BoundingBox() for wp, _, _ in parts.values()]
    print(f"shaft X {SHAFT_X:.2f} z {SHAFT_Z:.2f}; block tip X {TIP_X:.2f}; front X {max(b.xmax for b in bb):.2f}; "
          f"|Y| {max(max(abs(b.ymin), abs(b.ymax)) for b in bb):.2f}; lowest z {min(b.zmin for b in bb):.2f}; "
          f"FLOWER centre seated {SEAT_D:.2f} in ahead of his face (X {7.56 + SEAT_D:.2f})")
