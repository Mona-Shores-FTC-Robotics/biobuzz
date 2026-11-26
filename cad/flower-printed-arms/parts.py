"""Printed FLOWER stick arms (mentor's idea, 26 Nov 2026): the same arms in the same place, printed instead of the
goBILDA 11 Hole Mini U-Channel, with a hump that rides over the corner wheel's servo and mount.

    pip install cadquery numpy
    python3 cad/flower-printed-arms/parts.py     # writes the parts next to this file

The stick is a U: these two arms, the pivot axle through their top bosses (driven by the servo hub, as today), and
the 312 mm 8 mm REX shaft across their tips carrying the mountain, as today. The tip rollers come off; a foot takes
their place (README: why the shaft stays, and which foot).

What stays exactly as today (geometry.py): the pivot hole, the tip hole 240 mm from it (10 hole stations), the arm's
32 x 12 mm band, its Y position on each side, and the 0..162 deg swing. Each end now stops 16 mm past its hole (the
channel went 24): the arm fits a 250 x 210 bed corner to corner, and its tip no longer reaches past the roller.

What changes: around hole stations 5 to 7 the arm's centre line humps forward-up (seen with the stick down) by
`hump()`'s height, sized here from the corner parts in geometry.py so the arm's lower edge stays CLEARANCE (4 mm, half
a goBILDA hole) above them through the whole swing, on the worse of the two corners. Its centre line never bends
tighter than MIN_RADIUS, which sets how long the ramps up and down are. One part fits both sides.

The section is a ribbed I printed on its side: a 2.4 mm web in the middle of the arm's 12 mm (it bridges between the
ribs as it prints), 3 mm walls round the edge, diagonal ribs, all 12 mm tall. Both faces are alike, so it goes on
either side either way round. The pads round the pivot and tip holes are solid.
"""
import math
import os
import sys

import cadquery as cq
import numpy as np

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from geometry import (ARM_T, ARM_W, ARM_Y, CLEARANCE, END_PAST, GRID, PITCH, TIP_U, robot_to_arm,  # noqa: E402
                      sample_corner, SWING_DEG)

# ---- section and holes ----------------------------------------------------------------------------------------------
WEB = 2.4             # the web, in the middle of the arm's 12 mm (an I-section: both faces alike)
WALL = 3.0            # the edge walls and the hole rims
RIB = 2.5
RIB_PITCH = 24.0      # one diagonal per hole station
PAD = 20.0            # solid from each end to this far inside the pivot / tip hole
M4 = 4.3              # clearance; drill to 4.5 if a bolt binds
BEARING_BORE = 14.15  # 8x14x4 bearing, light press. Print the test coupon first (README)
HEX_AF = 12.7 + 0.25  # 1/2 in hex option, across flats
# goBILDA's hub patterns round a 14 mm hole: 4 holes on a 16 mm circle (diagonal) and 4 on a 16 mm square.
HUB_HOLES = [(8 * math.cos(math.radians(a)), 8 * math.sin(math.radians(a))) for a in (45, 135, 225, 315)] + \
            [(8.0, 8.0 * s) for s in (-1, 1)] + [(-8.0, 8.0 * s) for s in (-1, 1)]
# Plain 8 mm-grid holes along the centre line, in the solid pads, for the GARDEN guides and anything else bolted there.
PIVOT_LINE = [16.0]
TIP_LINE = [TIP_U - 16.0]

# ---- the hump --------------------------------------------------------------------------------------------------------
HUMP_FROM = 2 * PITCH            # u 48: the hump may leave the old line from hole station 3 ...
HUMP_TO = TIP_U - PAD - 2.0      # u 216: ... and is back on it before the tip pad, so the tip end stays straight
SIZING_MARGIN = 1.0             # mm on top of CLEARANCE: the sizing samples the parts, check.py measures them exactly
MIN_RADIUS = ARM_W / 2 + 6.0     # the centre line never bends tighter than this, so the inner edge stays smooth


def _bump(u, a, b, c, e):
    """0 before a, a cosine up to 1 at b, 1 to c, down to 0 at e."""
    u = np.asarray(u, float)
    s = np.zeros_like(u)
    up = (u > a) & (u < b)
    s[up] = 0.5 - 0.5 * np.cos(math.pi * (u[up] - a) / (b - a))
    s[(u >= b) & (u <= c)] = 1.0
    dn = (u > c) & (u < e)
    s[dn] = 0.5 + 0.5 * np.cos(math.pi * (u[dn] - c) / (e - c))
    return s


def obstacle_profile(step=1.5):
    """For every 2 mm along the arm: the highest v any corner part reaches inside the arm's band in Y (plus CLEARANCE)
    at any angle of the swing, worse side. Returns (u, need): the lowest the arm's lower edge may be there."""
    us = np.arange(-30, 270, 2.0)
    need = np.full(us.shape, -1e9)
    for side in ("left", "right"):
        p = sample_corner(side, step)
        y0, y1 = sorted(ARM_Y[side])
        p = p[(p[:, 1] > y0 - CLEARANCE) & (p[:, 1] < y1 + CLEARANCE)]
        for th in np.arange(0, SWING_DEG + 0.1, 2.0):
            a = robot_to_arm(p, th)
            idx = np.round((a[:, 0] - us[0]) / 2).astype(int)
            ok = (idx >= 0) & (idx < len(us))
            np.maximum.at(need, idx[ok], a[ok, 1] + CLEARANCE + SIZING_MARGIN)
    return us, need


_HUMP = None


def hump():
    """The hump (h, a, b, c, e): the lowest one, then the shortest, whose arm clears obstacle_profile() and whose
    centre line bends no tighter than MIN_RADIUS."""
    global _HUMP
    if _HUMP is not None:
        return _HUMP
    us, need = obstacle_profile()
    hit = us[need > -ARM_W / 2]
    if len(hit) == 0:
        _HUMP = (0.0, HUMP_FROM, HUMP_FROM + 1, HUMP_FROM + 2, HUMP_FROM + 3)
        return _HUMP
    def shortest(h):
        best = None
        for a in np.arange(hit[0] - 8, HUMP_FROM - 1e-6, -8):
            for e in np.arange(hit[-1] + 8, HUMP_TO + 1e-6, 8):
                if best is not None and e - a >= best[4] - best[1]:
                    continue
                for b in np.arange(a + 8, e, 8):
                    for c in np.arange(b, e - 7, 8):
                        hm = (h, a, b, c, e)
                        if _radius_ok(hm) and _clears(hm, us, need):
                            best = hm
                            break
                    if best is not None and best[1] == a and best[4] == e:
                        break
        return best

    hi = next((h for h in np.arange(2.0, 60.0, 2.0) if shortest(h) is not None), None)
    if hi is None:
        raise RuntimeError("no hump clears the corner parts without bending tighter than MIN_RADIUS")
    while hi - 0.5 > 0 and shortest(hi - 0.5) is not None:   # then down to the lowest, in 0.5 mm steps
        hi -= 0.5
    _HUMP = tuple(float(x) for x in shortest(hi))
    return _HUMP


def spine(hm, step=2.0):
    u = np.arange(-END_PAST + ARM_W / 2, TIP_U + END_PAST - ARM_W / 2 + 1e-6, step)   # the round ends add the rest
    return u, hm[0] * _bump(u, *hm[1:])


def _radius_ok(hm):
    u, v = spine(hm, 1.0)
    d1, d2 = np.gradient(v, u), np.gradient(np.gradient(v, u), u)
    k = np.abs(d2) / (1 + d1 ** 2) ** 1.5
    return bool(np.all(k <= 1.0 / MIN_RADIUS))


def edges(hm, half=ARM_W / 2, step=2.0):
    u, v = spine(hm, step)
    du, dv = np.gradient(u), np.gradient(v)
    n = np.c_[-dv, du] / np.hypot(du, dv)[:, None]
    return np.c_[u, v] - half * n, np.c_[u, v] + half * n


def _clears(hm, us, need):
    lo, _ = edges(hm)
    edge = np.interp(us, lo[:, 0], lo[:, 1])
    inside = (us >= lo[0, 0]) & (us <= lo[-1, 0])
    return bool(np.all(~inside | (edge >= need)))


# ---- building it -----------------------------------------------------------------------------------------------------
def _outline(hm, half):
    """The arm's outline, offset `half` either side of its centre line, with round ends (radius ARM_W/2) whose far
    points sit END_PAST past the pivot and tip holes. A closed list of (u, v)."""
    lo, hi = edges(hm, half)
    u0, u1 = lo[0, 0], lo[-1, 0]
    cap1 = [(u1 + half * math.sin(t), half * -math.cos(t)) for t in np.linspace(0, math.pi, 13)[1:-1]]
    cap0 = [(u0 - half * math.sin(t), half * math.cos(t)) for t in np.linspace(0, math.pi, 13)[1:-1]]
    return [tuple(p) for p in lo] + cap1 + [tuple(p) for p in hi[::-1]] + cap0


def _poly(pts, height, z0=0.0):
    return cq.Workplane("XY").workplane(offset=z0).polyline(pts).close().extrude(height)


def _cyl(r, h, u, v, z0=0.0):
    return cq.Workplane("XY").workplane(offset=z0).circle(r).extrude(h).translate((u, v, 0))


def spine_at(u):
    us, vs = spine(hump(), 1.0)
    return float(np.interp(u, us, vs))


def arm(pivot="bearing"):
    """One arm, either side (it is symmetric about its own mid-plane), in its own frame: u along X (pivot hole at the
    origin, tip hole at X 240), v along Y (+v forward-up once the stick is down), Z 0..12 its thickness, the web at
    Z 4.8..7.2.

    `pivot`: "bearing" keeps today's 14 mm hole and goBILDA hub patterns, so the axle, its hub and the servo hub
    bolt on as they do now; "hex" is a 1/2 in hex bore, keyed to a hex axle, with the same bolt patterns."""
    hm = hump()
    half = ARM_W / 2
    outline = _outline(hm, half)
    body = _poly(outline, ARM_T)
    inner = _outline(hm, half - WALL)
    pocket = _poly(inner, (ARM_T - WEB) / 2).union(_poly(inner, (ARM_T - WEB) / 2, (ARM_T + WEB) / 2))
    solid = cq.Workplane("XY").box(PAD + END_PAST + 20, 200, 3 * ARM_T, centered=(False, True, True)) \
        .translate((-END_PAST - 20, 0, 0))
    solid = solid.union(cq.Workplane("XY").box(PAD + END_PAST + 20, 200, 3 * ARM_T, centered=(False, True, True))
                        .translate((TIP_U - PAD, 0, 0)))
    body = body.cut(pocket.cut(solid))
    # diagonal ribs in the pockets, alternating, following the hump
    u_s, v_s = spine(hm, 1.0)
    ribs = None
    for ua_, ub_ in ((PAD, TIP_U - PAD),):
        n = max(1, int(round((ub_ - ua_) / (RIB_PITCH / 2))))
        rib_u = np.linspace(ua_, ub_, n + 1)
        for i in range(n):
            ua, ub = rib_u[i], rib_u[i + 1]
            va, vb = np.interp(ua, u_s, v_s), np.interp(ub, u_s, v_s)
            sa, sb = (-1, 1) if i % 2 == 0 else (1, -1)
            p0 = np.array([ua, va + sa * half])
            p1 = np.array([ub, vb + sb * half])
            L = float(np.linalg.norm(p1 - p0))
            ang = math.degrees(math.atan2(*(p1 - p0)[::-1]))
            r = cq.Workplane("XY").box(L + 6, RIB, ARM_T, centered=(True, True, False)) \
                .rotate((0, 0, 0), (0, 0, 1), ang).translate((*(p0 + p1) / 2, 0))
            ribs = r if ribs is None else ribs.union(r)
    body = body.union(ribs.intersect(_poly(outline, ARM_T)))
    # holes
    for u0, bore in ((0.0, pivot), (TIP_U, "bearing")):
        if bore == "hex":
            hole = cq.Workplane("XY").polygon(6, HEX_AF / math.cos(math.radians(30))).extrude(ARM_T) \
                .translate((u0, 0, 0))
        else:
            hole = _cyl(BEARING_BORE / 2, ARM_T, u0, 0)
        body = body.cut(hole)
        for du, dv in HUB_HOLES:
            body = body.cut(_cyl(M4 / 2, ARM_T, u0 + du, dv))
    for u0 in PIVOT_LINE + TIP_LINE:
        body = body.cut(_cyl(M4 / 2, ARM_T, u0, 0))
    return body


# ---- the tip feet -----------------------------------------------------------------------------------------------------
FOOT_R = 17.5          # today's tip roller (the CAD's 3400-0014-0032): its rim is the stick's floor contact
FOOT_T = 8.0           # across, inboard of the arm (the roller sits Y 129.4..135.5 on the left, the arm from 137.4)


def foot_skid():
    """Foot (a): a smooth puck in place of today's tip roller, with the roller's rim, so the ramp's nose rides at the
    same height. It slides onto the 8 mm REX shaft (8.4 mm bore) where the roller was and bolts to the arm's inner face
    on the tip hole's 16 mm square (4 x M4 x 20, counterbored, heads inboard), so it never turns. Own frame: the tip
    hole's axis on Z, the arm's face at Z 0, the puck Z 0..8 (print it arm-face down)."""
    puck = cq.Workplane("XY").circle(FOOT_R).extrude(FOOT_T).edges(">Z").fillet(3.0)
    puck = puck.cut(_cyl(8.4 / 2, FOOT_T, 0, 0))
    for du, dv in [(8.0, 8.0), (8.0, -8.0), (-8.0, 8.0), (-8.0, -8.0)]:
        puck = puck.cut(_cyl(M4 / 2, FOOT_T, du, dv)).cut(_cyl(4.0, 4.0, du, dv, FOOT_T - 4.0))
    return puck


def export(shape, name):
    for ext in ("step", "stl"):
        cq.exporters.export(shape, os.path.join(HERE, f"{name}.{ext}"), tolerance=0.05, angularTolerance=0.1)


def main():
    h, a, b, c, e = hump()
    print(f"hump: {h:.1f} mm; leaves the old line at u {a:.0f}, full from {b:.0f} to {c:.0f}, back on it at {e:.0f}")
    parts = {"arm": arm(), "arm_hex_pivot": arm("hex"), "foot_skid": foot_skid()}
    for name, shape in parts.items():
        export(shape, name)
        print(name, "volume", round(shape.val().Volume() / 1000, 1), "cm^3")
    # A more pronounced hump, for looking at in Onshape against the real corner (mentor, 26 Nov): 40 mm high, full
    # from hole 5.7 to 6.3, leaving the line at hole 2.5 and back on it at hole 9.5.
    global _HUMP
    sized = _HUMP
    _HUMP = PRONOUNCED
    export(arm(), "arm_hump40")
    _HUMP = sized


PRONOUNCED = (40.0, 36.0, 112.0, 128.0, 204.0)


if __name__ == "__main__":
    main()
