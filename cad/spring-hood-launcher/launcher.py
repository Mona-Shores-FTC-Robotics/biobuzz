"""Spring-hood launcher: printed parts, goBILDA assembly and interference check.

    pip install cadquery==2.8.0
    python launcher.py <goBILDA STEP folder> <output folder>
    python ballpath.py          # sweeps POLLEN and NECTAR along their path

The goBILDA folder holds goBILDA's own STEP files, unzipped one folder per SKU
(https://www.gobilda.com/content/step_files/<SKU>.zip). Without it the printed parts are still
written; the assembly uses simple stand-ins for the goBILDA parts.

Frame: X forward (toward the HIVE), Y up, Z across the robot (+Z is the motor side), millimetres.
The wheel shaft is the Z axis. Hood angles are measured counter-clockwise from +X, seen from +Z.
"""
import json, math, os, sys
import cadquery as cq

# ---- Geometry (the physics model's numbers live in LauncherDesignStudyTest) --------------------
R_HOOD = 97.0            # riding surface at the stop: 97 - 36 = 61 mm (2.4 in) gap to the Gecko wheel
SHELL = 3.0
R_PLATE = 116.6
A_EXIT, A_ENTRY, A_MID = 165.0, 255.0, 210.0
EAR_R, EAR_W = 167.0, 20.0
PIN1_R, PIN2_R = 109.0, 159.0
ARM = 80.0               # pivot to pin
OPEN = 31.0              # travel when NECTAR is in
PEGS = (24.0, 32.0, 40.0)  # spring pegs on the lower arm, from the pivot
SPRING_LEN = 52.1        # hook to hook at the stop (goBILDA 2915-0001-0002: 48 at rest)
STOP_D = 48.0
HOOD_HALF = 63.0         # shell runs Z -63..63; side plates 63..69
SEAM_Z = 35.0            # the hood prints in two halves split here, clear of the ball's contact
CHEEK_IN, CHEEK_T = 100.0, 5.0
TIE = (40.0, -96.0)
MOTOR = (24.0, 0.0)      # 30T:30T MOD 0.8 gears mesh at 24 mm
CHANNEL_X, CHANNEL_TOP = -128.0, -128.0

def P(r, a):
    return (r * math.cos(math.radians(a)), r * math.sin(math.radians(a)))

def add(a, b, s=1.0):
    return (a[0] + s * b[0], a[1] + s * b[1])

U = P(1, A_MID)                                       # hood opens this way
V = (-math.sin(math.radians(30)), math.cos(math.radians(30)))   # pin to pivot, perpendicular to U
P1, P2 = P(PIN1_R, A_MID), P(PIN2_R, A_MID)
F1, F2 = add(P1, V, ARM), add(P2, V, ARM)

def on_lower(d):
    return add(F2, V, -d)

ANCHOR = add(on_lower(32.0), U, -SPRING_LEN)
BUMP = 7.5 + 3.0 + 0.1                                # arm half-width + bumper radius
STOP = add(on_lower(STOP_D), U, -BUMP)                # bumper just in front of the lower arm

def rot(pt, c, ang):
    x, y = pt[0] - c[0], pt[1] - c[1]
    cs, sn = math.cos(ang), math.sin(ang)
    return (c[0] + x * cs - y * sn, c[1] + x * sn + y * cs)

PHI = OPEN / ARM
if (rot(P1, F1, PHI)[0] - P1[0]) * U[0] + (rot(P1, F1, PHI)[1] - P1[1]) * U[1] < 0:
    PHI = -PHI
SHIFT = (rot(P1, F1, PHI)[0] - P1[0], rot(P1, F1, PHI)[1] - P1[1])
BACKSTOP = add(rot(on_lower(STOP_D), F2, PHI * (33.0 / OPEN)), U, BUMP)

def arc_pts(r, a0, a1, n=48):
    return [P(r, a0 + (a1 - a0) * i / n) for i in range(n + 1)]

def poly(pts):
    return cq.Workplane("XY").polyline(pts).close()

def hull(points):
    pts = sorted(set((round(x, 3), round(y, 3)) for x, y in points))
    def cross(o, a, b):
        return (a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0])
    lo, hi = [], []
    for p in pts:
        while len(lo) >= 2 and cross(lo[-2], lo[-1], p) <= 0: lo.pop()
        lo.append(p)
    for p in reversed(pts):
        while len(hi) >= 2 and cross(hi[-2], hi[-1], p) <= 0: hi.pop()
        hi.append(p)
    return lo[:-1] + hi[:-1]

def ring(c, r, n=24):
    return [add(c, P(r, 360 * i / n)) for i in range(n)]

def cyl(c, r, z0, z1):
    return cq.Workplane("XY").workplane(offset=z0).center(*c).circle(r).extrude(z1 - z0)

def holes(solid, pts, d, z0=-300, z1=300):
    for c in pts:
        solid = solid.cut(cyl(c, d / 2, z0, z1))
    return solid

def mirror_z(w):
    return w.mirror("XY")

# ---- Printed parts ---------------------------------------------------------------------------------

def plate_profile():
    ea = math.degrees(math.asin(EAR_W / 2 / R_PLATE))
    out = arc_pts(R_HOOD, A_EXIT, A_ENTRY)
    out += arc_pts(R_PLATE, A_ENTRY, A_MID + ea, 24)
    c1, c2 = P(R_PLATE, A_MID + ea), P(R_PLATE, A_MID - ea)
    k = EAR_R - R_PLATE * math.cos(math.radians(ea))
    out += [add(c1, U, k), add(c2, U, k)]
    out += arc_pts(R_PLATE, A_MID - ea, A_EXIT, 24)
    return out

def hood():
    """The whole hood at its stop, before it is split for printing."""
    shell = poly(arc_pts(R_HOOD, A_EXIT, A_ENTRY) + arc_pts(R_HOOD + SHELL, A_ENTRY, A_EXIT)).extrude(2 * HOOD_HALF).translate((0, 0, -HOOD_HALF))
    for a in (188.0, 232.0):   # stiffening ribs along Z
        rib = poly([add(P(R_HOOD + 1, a), P(1.5, a + 90)), add(P(R_HOOD + 9, a), P(1.5, a + 90)),
                    add(P(R_HOOD + 9, a), P(1.5, a - 90)), add(P(R_HOOD + 1, a), P(1.5, a - 90))]).extrude(2 * HOOD_HALF).translate((0, 0, -HOOD_HALF))
        shell = shell.union(rib)
    plate = poly(plate_profile()).extrude(6)
    shell = shell.union(plate.translate((0, 0, HOOD_HALF))).union(plate.translate((0, 0, -HOOD_HALF - 6)))
    # seam tabs, either side of the split, with a 45° gusset under each so both halves print without support
    for a in (175.0, 210.0, 245.0):
        c = P(R_HOOD + 9, a)
        tab = poly(ring(c, 6.5)).extrude(12).translate((0, 0, SEAM_Z - 6))
        tab = tab.union(poly([add(P(R_HOOD + 1, a), P(6, a + 90)), add(P(R_HOOD + 15, a), P(6, a + 90)),
                              add(P(R_HOOD + 15, a), P(6, a - 90)), add(P(R_HOOD + 1, a), P(6, a - 90))]).extrude(12).translate((0, 0, SEAM_Z - 6)))
        gus = cq.Workplane("XY").polyline([(R_HOOD + 1, SEAM_Z - 6), (R_HOOD + 15, SEAM_Z - 6), (R_HOOD + 1, SEAM_Z - 20)]).close().extrude(6).translate((0, 0, -3))
        gus = gus.rotate((0, 0, 0), (1, 0, 0), 90).rotate((0, 0, 0), (0, 0, 1), a)
        gus2 = cq.Workplane("XY").polyline([(R_HOOD + 1, SEAM_Z + 6), (R_HOOD + 15, SEAM_Z + 6), (R_HOOD + 1, SEAM_Z + 20)]).close().extrude(6).translate((0, 0, -3))
        gus2 = gus2.rotate((0, 0, 0), (1, 0, 0), 90).rotate((0, 0, 0), (0, 0, 1), a)
        shell = shell.union(tab).union(gus).union(gus2)
        shell = shell.cut(cyl(add(c, (0, 0)), 2.15, SEAM_Z - 7, SEAM_Z + 7))
    # keep the riding surface clean: nothing inside R_HOOD
    shell = shell.cut(cyl((0, 0), R_HOOD, -200, 200))
    shell = holes(shell, (P1, P2), 4.3)
    return shell

_CACHE = {}

def cached(key, fn):
    if key not in _CACHE:
        _CACHE[key] = fn()
    return _CACHE[key]

def hood_halves():
    return cached("halves", _hood_halves)

def _hood_halves():
    h = hood()
    big = h.intersect(cq.Workplane("XY").box(600, 600, 300, centered=(True, True, False)).translate((0, 0, SEAM_Z - 300)))
    small = h.intersect(cq.Workplane("XY").box(600, 600, 300, centered=(True, True, False)).translate((0, 0, SEAM_Z)))
    return big, small

def arm(lower):
    """Arm at the +Z side, at its stop. Body Z 76..82; the pivot hub runs to the cheek, the pin hub to the side plate."""
    a, b = (F2, P2) if lower else (F1, P1)
    body = poly(hull(ring(a, 7.5) + ring(b, 7.5))).extrude(6).translate((0, 0, 76))
    body = body.union(cyl(a, 7.5, 76, CHEEK_IN - 0.25)).union(cyl(b, 7.5, 76, HOOD_HALF + 21.7))   # pin hub floats on its spacer; the pivot hub locates the arm
    if lower:
        for d in PEGS:
            c = on_lower(d)
            body = body.union(cyl(c, 4, 82, 96)).union(cyl(c, 5, 94, 96))
    body = holes(body, (a, b), 6.3)
    return body

def cheek(motor_side):
    """Cheek for the +Z side (motor side); the plain cheek is its mirror without the motor holes."""
    feats = (ring((0, 0), 16) + ring(MOTOR, 24) + ring(F1, 13) + ring(F2, 13) + ring(TIE, 12) + ring(ANCHOR, 12)
             + ring(add(STOP, U, 7), 10) + ring(add(STOP, U, -7), 10) + ring(BACKSTOP, 10)
             + [(-156.0, CHANNEL_TOP), (52.0, CHANNEL_TOP)])
    c = poly(hull(feats)).extrude(CHEEK_T).translate((0, 0, CHEEK_IN))
    # foot on the U-channel, with two gussets
    foot = cq.Workplane("XY").box(48, 6, CHEEK_IN - 76, centered=False).translate((CHANNEL_X - 24, CHANNEL_TOP, 76))
    for gx in (CHANNEL_X - 22, CHANNEL_X + 18):
        g = cq.Workplane("YZ").polyline([(CHANNEL_TOP + 6, 76), (CHANNEL_TOP + 6, CHEEK_IN), (CHANNEL_TOP + 30, CHEEK_IN)]).close().extrude(4).translate((gx, 0, 0))
        foot = foot.union(g)
    c = c.union(foot)
    c = c.union(cyl(ANCHOR, 5, 86, CHEEK_IN)).union(cyl(ANCHOR, 6, 86, 88))
    c = c.cut(cyl((0, 0), 7.05, 0, 300))                       # 14 mm bearing bore
    c = holes(c, (F1, F2, BACKSTOP), 4.3)
    c = c.cut(cyl(TIE, 4.15, 0, 300))
    slot = poly(hull(ring(add(STOP, U, 6), 2.2) + ring(add(STOP, U, -6), 2.2))).extrude(400).translate((0, 0, -200))
    c = c.cut(slot)
    for x in (CHANNEL_X - 16, CHANNEL_X + 16):
        for z in (84.0, 92.0):
            c = c.cut(cq.Workplane("XZ").center(x, z).circle(2.15).extrude(-20).translate((0, CHANNEL_TOP - 5, 0)))
    if motor_side:
        c = c.cut(cyl(MOTOR, 7.1, 0, 300))
        c = holes(c, [add(MOTOR, (sx * 8, sy * 8)) for sx in (-1, 1) for sy in (-1, 1)], 4.3)
    else:
        c = mirror_z(c)
    return c

def gauge():
    """61 mm block: sits between the Gecko tread and the hood to set the stop."""
    g = cq.Workplane("XY").box(61, 20, 8).edges("|Z").fillet(2)
    return g.faces(">Z").workplane().text("61 mm GAP", 6, -0.8, combine="cut")

# ---- goBILDA parts ---------------------------------------------------------------------------------

def step(lib, sku):
    if not lib:
        return None
    d = os.path.join(lib, sku)
    if not os.path.isdir(d):
        return None
    for root, _, files in os.walk(d):
        for f in files:
            if f.lower().endswith((".step", ".stp")):
                return cq.importers.importStep(os.path.join(root, f))
    return None

def place(w, rotx=0.0, roty=0.0, rotz=0.0, t=(0, 0, 0)):
    if rotx: w = w.rotate((0, 0, 0), (1, 0, 0), rotx)
    if roty: w = w.rotate((0, 0, 0), (0, 1, 0), roty)
    if rotz: w = w.rotate((0, 0, 0), (0, 0, 1), rotz)
    return w.translate(t)

def gobilda(lib):
    """Returns [(name, sku, workplane, colour)] for every bought part in the assembly."""
    parts = []
    def P_(name, sku, w, col):
        parts.append((name, sku, w, col))
    def zspan(sku, z0, z1, xy=(0, 0), r=5.0, col=(0.7, 0.7, 0.72)):
        return cyl(xy, r, z0, z1)
    gecko = step(lib, "3613-0014-0072")
    fly = step(lib, "3628-0032-0082")
    hub13 = step(lib, "1313-1632-4008")
    hub10 = step(lib, "1310-0016-4008")
    gear = step(lib, "2303-4008-0030")
    brg = step(lib, "1611-0514-4008")
    motor = step(lib, "5203-2402-0001")
    chan = step(lib, "1120-0008-0216")
    collar = step(lib, "2910-0920-4008")
    for s in (1, -1):
        g = place(gecko, rotx=-90, t=(0, 0, s * 12)) if gecko else cyl((0, 0), 36, s * 12 - 12, s * 12 + 12)
        P_(f"Gecko wheel {'+' if s > 0 else '-'}", "3613-0014-0072", g, (0.2, 0.2, 0.22))
        h = place(hub10, t=(15.42, 5.72, -1.03)) if hub10 else cyl((0, 0), 12, 0, 22)
        h = h.translate((0, 0, 24)) if s > 0 else mirror_z(h.translate((0, 0, 24)))
        P_("Hyper Hub, 16 mm pattern", "1310-0016-4008", h, (0.82, 0.82, 0.85))
        for k in range(2):
            z0 = 46 + 6 * k
            f = place(fly, rotx=90, t=(0, 0, z0 + 6)) if fly else cyl((0, 0), 41, z0, z0 + 6)
            f = f if s > 0 else mirror_z(f)
            P_("Steel flywheel", "3628-0032-0082", f, (0.45, 0.47, 0.5))
        h = place(hub13, t=(0, 0, 20)) if hub13 else cyl((0, 0), 20, 0, 22)
        h = h.translate((0, 0, 58)) if s > 0 else mirror_z(h.translate((0, 0, 58)))
        P_("Hyper Hub, 32 mm pattern", "1313-1632-4008", h, (0.82, 0.82, 0.85))
        b = place(brg, rotx=90, t=(0, 0, 0)) if brg else cyl((0, 0), 7, 0, 5)
        # flange (y 4..5 in the STEP) toward the inside: after rotx=90 y->z, so flange at z 4..5 -> put z 4..5 at 99..100 reversed
        b = (place(brg, rotx=-90, t=(0, 0, CHEEK_IN + 4)) if brg else cyl((0, 0), 7, CHEEK_IN - 1, CHEEK_IN + 4))
        b = b if s > 0 else mirror_z(b)
        P_("Bearing, 8 mm REX", "1611-0514-4008", b, (0.75, 0.75, 0.78))
    # spacers between the 32 mm hub and the bearing
    P_("8 mm ID spacer, 4 mm", "1522-0010-0040", cyl((0, 0), 5, 80, 84) .cut(cyl((0, 0), 4, 79, 85)), (0.3, 0.3, 0.32))
    P_("Shim, 1 mm", "2807-0811-1000", cyl((0, 0), 5.5, 84, 85).cut(cyl((0, 0), 4, 83, 86)), (0.8, 0.8, 0.8))
    for z0, z1, sku in ((-92, -80, "1522-0010-0120"), (-96, -92, "1522-0010-0040"), (-99, -96, "1522-0010-0030")):
        P_(f"8 mm ID spacer, {z1 - z0} mm", sku, cyl((0, 0), 5, z0, z1).cut(cyl((0, 0), 4, z0 - 1, z1 + 1)), (0.3, 0.3, 0.32))
    gw = place(gear, t=(-3.15, -15.75, -13.41)) if gear else cyl((0, 0), 12.8, 0, 14)
    P_("30T gear on wheel shaft", "2303-4008-0030", gw.translate((0, 0, 85)), (0.25, 0.25, 0.27))
    P_("30T gear on motor", "2303-4008-0030", gw.rotate((0, 0, 0), (0, 0, 1), 6).translate((MOTOR[0], MOTOR[1], 85)), (0.25, 0.25, 0.27))
    m = place(motor, t=(37.175, -92.86, 11.075)) if motor else cyl((0, 0), 18.75, -107.7, 0).union(cyl((0, 0), 4, 0, 23.5)).rotate((0, 0, 0), (1, 0, 0), -90)
    m = m.rotate((0, 0, 0), (1, 0, 0), -90).translate((MOTOR[0], MOTOR[1], CHEEK_IN + CHEEK_T))
    P_("Yellow Jacket motor, 1:1", "5203-2402-0001", m, (0.95, 0.78, 0.1))
    P_("Wheel shaft, 8 mm REX, 240 mm", "2106-4008-2400", cyl((0, 0), 4, CHEEK_IN + CHEEK_T - 1 - 240, CHEEK_IN + CHEEK_T - 1), (0.65, 0.65, 0.68))
    P_("Tie rod, 8 mm REX, 240 mm", "2106-4008-2400", cyl(TIE, 4, -120, 120), (0.65, 0.65, 0.68))
    for z0 in (CHEEK_IN - 9, CHEEK_IN + CHEEK_T, -CHEEK_IN, -CHEEK_IN - CHEEK_T - 9):
        cl = cyl(TIE, 10, z0, z0 + 9).cut(cyl(TIE, 4, z0 - 1, z0 + 10))
        P_("Clamping collar", "2910-0920-4008", cl, (0.85, 0.2, 0.2))
    c = place(chan, rotx=-90, t=(CHANNEL_X, CHANNEL_TOP - 2.5, 108)) if chan else cq.Workplane("XY").box(48, 48, 216).translate((CHANNEL_X, CHANNEL_TOP - 24, 0))
    P_("U-channel, 8 hole, 216 mm", "1120-0008-0216", c, (0.82, 0.82, 0.85))
    return parts

def hardware(open_frac=0.0):
    """Screws, spacers, bumpers and springs, as simple shapes (they are listed in the BOM)."""
    hw = []
    t = (SHIFT[0] * open_frac, SHIFT[1] * open_frac)
    ang = PHI * open_frac
    for s in (1, -1):
        def m(w):
            return w if s > 0 else mirror_z(w)
        for f in (F1, F2):     # pivots: M4x35 from outside, 24 mm spacer as the bushing, washer + nylock inside
            hw.append(("Pivot: M4x35 + 24 mm spacer + washer + nylock", m(cyl(f, 3, 76, 100).union(cyl(f, 3.5, 105, 109)).union(cyl(f, 2, 70, 105)).union(cyl(f, 3.5, 70.5, 76))), (0.15, 0.15, 0.15)))
        for p in (P1, P2):     # pins: M4x30 from inside the hood, 16 mm spacer
            q = add(p, t)
            z = HOOD_HALF + 6
            hw.append(("Pin: M4x30 + 16 mm spacer + washer + nylock", m(cyl(q, 3, z, z + 16).union(cyl(q, 3.5, HOOD_HALF - 4, HOOD_HALF)).union(cyl(q, 3.5, z + 16, z + 21.5))), (0.15, 0.15, 0.15)))
        for c in (STOP, BACKSTOP):
            hw.append(("Stop: M4x35 + 24 mm spacer", m(cyl(c, 3, 76, 100).union(cyl(c, 3.5, 105, 109)).union(cyl(c, 3.5, 70.5, 76))), (0.15, 0.15, 0.15)))
        peg = rot(on_lower(32.0), F2, ang)
        a, b = peg, ANCHOR
        L = math.hypot(b[0] - a[0], b[1] - a[1])
        spring = (cq.Workplane("XY").circle(4).circle(3).extrude(L - 12).translate((0, 0, 6))
                  .rotate((0, 0, 0), (0, 1, 0), 90).rotate((0, 0, 0), (0, 0, 1), math.degrees(math.atan2(b[1] - a[1], b[0] - a[0])))
                  .translate((a[0], a[1], 92)))
        hw.append(("Extension spring 8 mm OD", m(spring), (0.95, 0.75, 0.1)))
    return hw

def moving(open_frac=0.0):
    """Hood and arms, closed (0) or fully open (1)."""
    big, small = hood_halves()
    t = (SHIFT[0] * open_frac, SHIFT[1] * open_frac, 0)
    ang = math.degrees(PHI * open_frac)
    out = [("Hood, large half", big.translate(t), (0.93, 0.55, 0.15)), ("Hood, small half", small.translate(t), (0.93, 0.55, 0.15))]
    for s in (1, -1):
        for lower in (False, True):
            a = cached(("arm", lower), lambda: arm(lower))
            a = a.rotate((F2 if lower else F1) + (0,), (F2 if lower else F1) + (1,), ang)
            out.append((f"{'Lower' if lower else 'Upper'} arm", a if s > 0 else mirror_z(a), (0.1, 0.62, 0.55)))
    return out

def fixed_printed():
    return [("Cheek, motor side", cached("cm", lambda: cheek(True)), (0.15, 0.45, 0.8)),
            ("Cheek, plain side", cached("cp", lambda: cheek(False)), (0.15, 0.45, 0.8))]

# ---- Checks ------------------------------------------------------------------------------------------

def interference(items, skip=()):
    """Pairs whose solids overlap by more than a sliver. Fasteners are allowed to pass through their own holes."""
    shapes = [(n, w.val() if isinstance(w, cq.Workplane) else w) for n, w in items]
    hits = []
    for i in range(len(shapes)):
        for j in range(i + 1, len(shapes)):
            ni, a = shapes[i]; nj, b = shapes[j]
            if any((x in ni and y in nj) or (x in nj and y in ni) for x, y in skip):
                continue
            ba, bb = a.BoundingBox(), b.BoundingBox()
            if ba.xmax < bb.xmin or bb.xmax < ba.xmin or ba.ymax < bb.ymin or bb.ymax < ba.ymin or ba.zmax < bb.zmin or bb.zmax < ba.zmin:
                continue
            try:
                v = a.intersect(b).Volume()
            except Exception:
                continue
            if v > 1.0:
                hits.append((ni, nj, round(v, 1)))
    return hits

def main():
    lib = sys.argv[1] if len(sys.argv) > 1 and sys.argv[1] != "-" else None
    out = sys.argv[2] if len(sys.argv) > 2 else "out"
    os.makedirs(os.path.join(out, "stl"), exist_ok=True)
    big, small = hood_halves()
    # print orientation: each part flat on the bed, features up (turned over, never mirrored)
    printed = {
        "cheek-motor-side": cheek(True).rotate((0, 0, 0), (1, 0, 0), 180),
        "cheek-plain-side": cheek(False),
        "hood-large-half": big,
        "hood-small-half": small.rotate((0, 0, 0), (1, 0, 0), 180),
        "arm-upper-x2": arm(False),
        "arm-lower-x2": arm(True),
        "gap-gauge": gauge(),
    }
    for name in list(printed):
        w = printed[name]
        printed[name] = w.translate((0, 0, -w.val().BoundingBox().zmin))
    for name, w in printed.items():
        cq.exporters.export(w, os.path.join(out, "stl", name + ".stl"), tolerance=0.05, angularTolerance=0.1)
    info = {}
    for name, w in printed.items():
        bb = w.val().BoundingBox()
        info[name] = {"size_mm": [round(bb.xlen, 1), round(bb.ylen, 1), round(bb.zlen, 1)], "volume_cm3": round(w.val().Volume() / 1000, 1)}
    bought = gobilda(lib)
    closed = fixed_printed() + moving(0.0)
    report = {"printed": info, "geometry": {
        "F1": F1, "F2": F2, "P1": P1, "P2": P2, "anchor": ANCHOR, "stop": STOP, "backstop": BACKSTOP, "open_shift": SHIFT,
        "phi_deg": math.degrees(PHI)}}
    skip = [("Pivot", "arm"), ("Pivot", "Cheek"), ("Pin", "arm"), ("Pin", "Hood"), ("Stop", "Cheek"),
            ("Bearing", "Cheek"), ("Bearing", "Wheel shaft"), ("Wheel shaft", "Gecko"), ("Wheel shaft", "Hyper Hub"), ("Wheel shaft", "flywheel"),
            ("Wheel shaft", "spacer"), ("Wheel shaft", "Shim"), ("Wheel shaft", "gear on wheel"), ("Tie rod", "Cheek"), ("Tie rod", "collar"),
            ("Hyper Hub", "Gecko"), ("Hyper Hub", "flywheel"), ("motor", "gear on motor"), ("motor", "Cheek, motor"), ("spring", "arm"), ("spring", "Cheek"),
            ("gear on motor", "gear on wheel")]
    proxies = [(n, w) for n, _, w, _ in gobilda(None)]     # goBILDA envelopes: fast, and only their outsides matter here
    for frac, label in ((0.0, "closed"), (1.0, "open")):
        items = [(n, w) for n, w, _ in fixed_printed() + moving(frac)] + proxies + [(n, w) for n, w, _ in hardware(frac)]
        report[f"interference_{label}"] = interference(items, skip)
    # assembly: STEP of our parts in place (import goBILDA's own STEP files beside it), GLB of everything for the viewer
    assy = cq.Assembly(name="spring-hood-launcher")
    for i, (n, w, col) in enumerate(closed):
        assy.add(w, name=f"printed:{i}:{n}", color=cq.Color(*col))
    assy.export(os.path.join(out, "spring-hood-launcher-printed.step"))
    import glb
    glb.write_glb(os.path.join(out, "spring-hood-launcher.glb"),
                  [(f"printed:{i}:{n}", w, col, 0.0) for i, (n, w, col) in enumerate(closed)]
                  + [(f"gobilda:{i}:{sku}:{n}", w, col, 0.6) for i, (n, sku, w, col) in enumerate(bought)]
                  + [(f"hardware:{i}:{n}", w, col, 0.0) for i, (n, w, col) in enumerate(hardware(0.0))],
                  tolerance=0.2, angular=0.3)
    json.dump(report, open(os.path.join(out, "report.json"), "w"), indent=1)
    print(json.dumps(report, indent=1))

if __name__ == "__main__":
    main()
