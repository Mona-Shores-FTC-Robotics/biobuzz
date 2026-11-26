"""Printed FLOWER stick arms (mentor's idea, 26 Nov 2026): the same arms in the same place, printed instead of the
goBILDA 11 Hole Mini U-Channel, with a hump that rides over the corner wheel and its servo.

    pip install cadquery numpy
    python3 cad/flower-printed-arms/parts.py     # writes the parts next to this file

What stays exactly as today (geometry.py): the pivot hole, the tip hole 240 mm from it (10 hole stations), the arm's
32 x 12 mm band, its Y position on each side, its ends 24 mm past both holes, and the 0..162 deg swing. The bearing,
the servo hub, the 312 mm REX shaft, the rollers, the ramp and the GARDEN guides bolt on unchanged.

What changes: between hole stations 4 and 9 the arm's centre line humps forward-up (seen with the stick down) by
`hump()`'s height, sized here from the corner parts in geometry.py so the arm's lower edge stays CLEARANCE (4 mm, half
a goBILDA hole) above them through the whole swing, on the worse of the two corners. One part fits both sides.

The section is a ribbed channel printed on its side: a 2.4 mm web on the bed, 3 mm walls round the edge and the
holes, diagonal ribs, all 12 mm tall (the arm's thickness in Y). The pads round the pivot and tip holes are solid.

Two layouts (the README compares them):
  (a) `arm`: arms only; the ramp you have stays on the shaft.          Print 2 per robot.
  (b) `arm_b` x 2 + `ramp_b`: the ramp printed too, bolted to each arm by a cheek behind the roller.
      As one piece it would be a 310 x 290 x 70 mm U standing on its arms (layers across the arms' bending): not
      offered. `u_assembly_b.step` is the three pieces together, for Onshape only.
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
WEB = 2.4             # the face on the bed (6 layers at 0.4)
WALL = 3.0            # the edge walls and the hole rims
RIB = 2.5
RIB_PITCH = 24.0      # one diagonal per hole station
PAD = 22.0            # solid from each end to this far inside the pivot / tip hole
M4 = 4.3              # clearance; drill to 4.5 if a bolt binds
BEARING_BORE = 14.15  # 8x14x4 bearing, light press. Print the test coupon first (README)
HEX_AF = 12.7 + 0.25  # 1/2 in hex option, across flats
# goBILDA's hub patterns round a 14 mm hole: 4 holes on a 16 mm circle (diagonal) and 4 on a 16 mm square.
HUB_HOLES = [(8 * math.cos(math.radians(a)), 8 * math.sin(math.radians(a))) for a in (45, 135, 225, 315)] + \
            [(8.0, 8.0 * s) for s in (-1, 1)] + [(-8.0, 8.0 * s) for s in (-1, 1)]
# Plain 8 mm-grid holes along the centre line, in the solid pads, for the GARDEN guides and anything else bolted there.
PIVOT_LINE = [-16.0, 16.0]
TIP_LINE = [TIP_U - 16.0, TIP_U + 16.0]

# ---- the hump --------------------------------------------------------------------------------------------------------
HUMP_FROM = 2 * PITCH            # u 48: the hump may leave the old line from hole station 3 ...
HUMP_TO = TIP_U - PAD - 2.0      # u 216: ... and is back on it before the tip pad, so the tip end stays straight
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
            np.maximum.at(need, idx[ok], a[ok, 1] + CLEARANCE)
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
    """The arm's outline, offset `half` either side of its centre line, with round ends (radius `half`) whose far
    points sit END_PAST past the pivot and tip holes. A closed list of (u, v)."""
    lo, hi = edges(hm, half)
    u0, u1 = lo[0, 0], lo[-1, 0]
    cap1 = [(u1 + half * math.sin(t), half * -math.cos(t)) for t in np.linspace(0, math.pi, 13)[1:-1]]
    cap0 = [(u0 - half * math.sin(t), half * math.cos(t)) for t in np.linspace(0, math.pi, 13)[1:-1]]
    pts = [tuple(p) for p in lo] + cap1 + [tuple(p) for p in hi[::-1]] + cap0
    return pts


def _poly(pts, height, z0=0.0):
    return cq.Workplane("XY").workplane(offset=z0).polyline(pts).close().extrude(height)


def arm(pivot="bearing", cheeks=False):
    """One arm in its own frame: u along X (pivot hole at the origin, tip hole at X 240), v along Y (+v forward-up once
    the stick is down), Z 0..12 its thickness (the web on the bed at Z 0..2.4). Robot frame: geometry.arm_to_robot,
    with the web against the arm's inner face (the side the servo hub bolts to)."""
    hm = hump()
    half = ARM_W / 2
    body = _poly(_outline(hm, half), ARM_T)
    # pocket the middle down to the web, leaving the edge walls and solid pads at both ends
    inner = _outline(hm, half - WALL)
    pocket = _poly(inner, ARM_T, WEB)
    keep = cq.Workplane("XY").box(PAD + END_PAST + 20, 200, 3 * ARM_T, centered=(False, True, True)) \
        .translate((-END_PAST - 20, 0, 0))
    keep = keep.union(cq.Workplane("XY").box(PAD + END_PAST + 20, 200, 3 * ARM_T, centered=(False, True, True))
                      .translate((TIP_U - PAD, 0, 0)))
    pocket = pocket.cut(keep)
    body = body.cut(pocket)
    # diagonal ribs between the pads, one per hole station, alternating, following the hump
    u_s, v_s = spine(hm, 1.0)
    rib_u = np.arange(PAD, TIP_U - PAD + 1e-6, RIB_PITCH / 2)
    ribs = None
    for i in range(len(rib_u) - 1):
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
    if ribs is not None:
        body = body.union(ribs.intersect(_poly(_outline(hm, half), ARM_T)))
    if cheeks:
        body = body.union(_cheek_boss())
    # holes
    for u0, bore in ((0.0, pivot), (TIP_U, "bearing")):
        if bore == "hex":
            hole = cq.Workplane("XY").polygon(6, HEX_AF / math.cos(math.radians(30))).extrude(ARM_T).translate((u0, 0, 0))
        else:
            hole = cq.Workplane("XY").circle(BEARING_BORE / 2).extrude(ARM_T).translate((u0, 0, 0))
        body = body.cut(hole)
        for du, dv in HUB_HOLES:
            body = body.cut(cq.Workplane("XY").circle(M4 / 2).extrude(ARM_T).translate((u0 + du, dv, 0)))
    for u0 in PIVOT_LINE + TIP_LINE:
        body = body.cut(cq.Workplane("XY").circle(M4 / 2).extrude(ARM_T).translate((u0, 0, 0)))
    if cheeks:
        for u0, v0 in CHEEK_HOLES:
            body = body.cut(cq.Workplane("XY").circle(M4 / 2).extrude(ARM_T).translate((u0, v0, 0)))
    return body


# ---- layout (b): the ramp, printed, on cheeks that bolt to the arms behind the rollers --------------------------------
# The ramp's section is the 31 Oct CAD's `Flower Ramp` (u 208..255, v -28.6..10.8 about the tip hole's line), traced.
RAMP_SECTION = [(212.0, -24.3), (215.6, -28.5), (228.6, -17.6), (237.8, -9.7), (242.8, -5.7), (249.3, -0.6),
                (254.5, 4.0), (249.3, 7.1), (239.7, 10.6), (235.0, 5.0), (230.0, -1.4), (220.1, -13.9)]
RAMP_HALF_W = 100.0          # the CAD ramp spans Y -105..95; the rollers and hubs sit between it and each arm
CHEEK_U = (196.0, 218.0)     # behind the rollers (about 36 mm across, centred on the tip hole: u 222..258), past the hump
CHEEK_V = (-14.0, 2.0)       # inside the arm's band
CHEEK_HOLES = [(201.0, -6.0), (213.0, -6.0)]
BAR_U, BAR_V = (204.0, 218.0), (-22.0, -6.0)   # cheek to the ramp's back


def _cheek_boss():
    """Solid where the cheek bolts on (layout b), so the two M4 holes have meat round them."""
    u0, u1 = CHEEK_U
    v0, v1 = CHEEK_V
    return cq.Workplane("XY").box(u1 - u0, v1 - v0, ARM_T, centered=False).translate((u0, v0, 0))


def ramp_b(gap_y):
    """Layout (b)'s ramp, in the arm frame of the left arm turned so Y is across the robot: X = u, Y = robot Y, Z = v.
    `gap_y`: (right arm's inner face, left arm's inner face) in robot Y. The CAD ramp's section, extruded over
    |Y| <= RAMP_HALF_W, plus a cheek at each end out to the arm's inner face, behind the rollers, 6 mm thick."""
    yl, yr = gap_y[1], gap_y[0]
    sec = cq.Workplane("XZ").polyline(RAMP_SECTION).close().extrude(-RAMP_HALF_W).translate((0, 0, 0))
    sec = sec.union(cq.Workplane("XZ").polyline(RAMP_SECTION).close().extrude(RAMP_HALF_W))
    body = sec
    u0, u1 = CHEEK_U
    v0, v1 = CHEEK_V
    for y_in, sgn in ((yl, 1), (yr, -1)):
        # a plate in the arm's plane against the arm, joined to the ramp's back by a bar
        y_face = y_in
        plate = cq.Workplane("XY").box(u1 - u0, 6.0, v1 - v0, centered=False) \
            .translate((u0, y_face - 6.0 if sgn > 0 else y_face, v0))
        bar_y0, bar_y1 = (RAMP_HALF_W - 2, y_face) if sgn > 0 else (y_face, -RAMP_HALF_W + 2)
        bar = cq.Workplane("XY").box(BAR_U[1] - BAR_U[0], bar_y1 - bar_y0, BAR_V[1] - BAR_V[0], centered=False) \
            .translate((BAR_U[0], bar_y0, BAR_V[0]))
        body = body.union(plate).union(bar)
        for hu, hv in CHEEK_HOLES:
            # heat-set M4 inserts (5.6 mm hole) from the arm side
            body = body.cut(cq.Workplane("XZ").circle(2.8).extrude(12 if sgn > 0 else -12)
                            .translate((hu, y_face, hv)))
    # the 8 mm REX shaft still passes through the ramp at the tip hole
    body = body.cut(cq.Workplane("XZ").circle(4.3).extrude(-300).translate((TIP_U, 150, 0)))
    return body


def export(shape, name):
    for ext in ("step", "stl"):
        cq.exporters.export(shape, os.path.join(HERE, f"{name}.{ext}"), tolerance=0.05, angularTolerance=0.1)


def main():
    h, a, b, c, e = hump()
    print(f"hump: {h:.1f} mm; leaves the old line at u {a:.0f}, full from {b:.0f} to {c:.0f}, back on it at {e:.0f}")
    a = arm()
    export(a, "arm")
    export(arm(pivot="hex"), "arm_hex_pivot")
    export(arm(cheeks=True), "arm_b")
    r = ramp_b((ARM_Y["right"][0], ARM_Y["left"][0]))
    export(r, "ramp_b")
    print("volumes, cm^3: arm", round(a.val().Volume() / 1000, 1), "arm_b", round(arm(cheeks=True).val().Volume() / 1000, 1),
          "ramp_b", round(r.val().Volume() / 1000, 1))


if __name__ == "__main__":
    main()
