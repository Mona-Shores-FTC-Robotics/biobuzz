"""Screws and nuts for the drawn assemblies: goBILDA's M4 and M3 socket head screws, M4 flat (countersunk) head
screws and M4 nylon-insert lock nuts.

A joint names its screw by where the head sits and which way the shank points. bolt() picks the shortest goBILDA
length that fits, drills the clearance holes, and adds the screw (and the nut, if there is one) as parts. The
envelopes are what the clash checks and the AdvantageScope model see; the full-robot STEP puts goBILDA's own models
where they are (SCREWS / NUTS say which and where).

Lengths are goBILDA's: under the head, 6 to 40 mm. A screw into a nut leaves at least 1 mm of thread past it; a
screw into a tapped hole (a goBILDA standoff, a shaft's end, a motor's face) engages at least 1.5 diameters and stops
0.5 mm short of the hole's bottom.
"""
import math
import cadquery as cq

M4_LENGTHS = (6, 8, 10, 12, 14, 16, 20, 25, 30, 35, 40)
M3_LENGTHS = (6, 8)
HEAD = {4: (7.0, 4.0), 3: (5.5, 3.0)}           # socket head: diameter, height
CLEAR = {4: 4.3, 3: 3.4}                         # clearance hole
NUT_H, NUT_AC = 4.85, 8.08                       # goBILDA 2812-0004-0007: height, across corners
FLAT = (8.0, 2.2)                                # goBILDA 2802 flat (countersunk, 90 deg) head: diameter, depth
SCREWS, NUTS = {}, {}                            # part name -> (goBILDA part, the head's top (or the nut's seat), axis)
PLACED = {}                                      # part name -> (vendor file, src origin, src axis, src ref, dst origin, dst axis, dst ref):
                                                 # a directional vendor part a build places itself (team CAD mm)
INFO = {}                                        # screw part name -> what the fastener check needs (below)
JOINTS = []                                      # (joint, what it holds, fastener, count, service note)

def _unit(v):
    n = math.sqrt(sum(c * c for c in v)); return tuple(c / n for c in v)

def _cyl(p0, axis, dia, length):
    """A cylinder from p0 along axis."""
    a = _unit(axis)
    return cq.Workplane(cq.Plane(origin=p0, xDir=_perp(a), normal=a)).circle(dia / 2).extrude(length)

def _cone(p0, axis, d0, d1, length):
    """A cone frustum from p0 (diameter d0) along axis to diameter d1."""
    a = _unit(axis)
    s = cq.Solid.makeCone(d0 / 2, d1 / 2, length, cq.Vector(*p0), cq.Vector(*a))
    return cq.Workplane().add(s)

def _perp(a):
    r = (1, 0, 0) if abs(a[0]) < 0.9 else (0, 1, 0)
    x = (r[1] * a[2] - r[2] * a[1], r[2] * a[0] - r[0] * a[2], r[0] * a[1] - r[1] * a[0])
    return _unit((x[1] * a[2] - x[2] * a[1], x[2] * a[0] - x[0] * a[2], x[0] * a[1] - x[1] * a[0]))

def length_for(grip, d=4, nut=True, tapped=None, min_engage=None):
    """The shortest standard length: through `grip` mm into a nut, or into a tapped hole `tapped` mm deep."""
    lengths = M4_LENGTHS if d == 4 else M3_LENGTHS
    if nut:
        need = grip + NUT_H + 1.0
        ok = [L for L in lengths if L >= need]
    else:
        lo, hi = grip + (1.5 * d if min_engage is None else min_engage), grip + tapped - 0.5
        ok = [L for L in lengths if lo <= L <= hi]
    if not ok: raise ValueError(f"no M{d} length for a {grip:.1f} mm grip" + (" and a nut" if nut else f" into {tapped} mm of thread"))
    return ok[0]

def bolt(parts, joint, holds, heads, axis, grip, d=4, nut=True, tapped=None, into=None, through=(), col=(0.25, 0.26, 0.28), label=None, service=None, min_engage=None, modelled=False, flat=False):
    """Screws at each head point (the head's underside, on the first part's surface), pointing along axis through
    `grip` mm of parts, into a nut or a tapped hole. Adds them to `parts` (a build's part dict) and returns the
    clearance-hole cutter to cut from the parts they pass through. `through` names (by their start) the parts it
    clamps, `into` the part (a regular expression on its name) whose thread it engages, for the fastener check.
    flat: a flat (countersunk) head, its top flush at the head point; the length is overall, as goBILDA gives it."""
    a = _unit(axis); L = length_for(grip, d, nut, tapped, min_engage)
    sku = (f"2802-0004-{L:04d}" if flat else f"2800-0004-{L:04d}" if d == 4 else f"2800-0003-{L:04d}")
    hd, hh = FLAT if flat else HEAD[d]; cut = None
    for i, p in enumerate(heads):
        top = p if flat else tuple(p[k] - a[k] * hh for k in range(3))
        env = (_cone(p, a, hd, d, hh) if flat else _cyl(top, a, hd, hh)).union(_cyl(p, a, d, L))
        n = f"screw_{joint}_{i} ({label})" if label else f"screw_{joint}_{i} (goBILDA {sku}, M{d} x {L} {'flat' if flat else 'socket'} head)"
        parts[n] = (env, col, "buy")
        if not label: SCREWS[n] = (sku, top, a)
        INFO[n] = dict(joint=joint, head=p, axis=a, d=d, L=L, grip=grip, nut=nut, into=into, through=tuple(through), service=service, modelled=modelled, flat=flat, min_engage=min_engage)
        if nut:
            seat = tuple(p[k] + a[k] * grip for k in range(3))
            nn = f"nut_{joint}_{i} (goBILDA 2812-0004-0007, M4 nylon-insert lock nut)"
            parts[nn] = (_cyl(seat, a, NUT_AC, NUT_H), col, "buy"); NUTS[nn] = ("2812-0004-0007", seat, a)
        h = _cyl(tuple(p[k] - a[k] * 0.5 for k in range(3)), a, CLEAR[d], grip + (0.5 if nut else 0.0) + 0.5)
        if flat: h = h.union(_cone(tuple(p[k] - a[k] * 0.3 for k in range(3)), a, hd + 0.6, CLEAR[d], hh + 0.3))
        cut = h if cut is None else cut.union(h)
    JOINTS.append((joint, holds, (label or f"M{d} x {L}" + (" flat head" if flat else "")) + (" + lock nut" if nut else f" into a tapped hole"), len(heads), service))
    return cut

def drill(parts, names, cutter):
    """Cut a joint's clearance holes from each named part (a name's start is enough)."""
    for n in list(parts):
        if any(n.startswith(k) for k in names):
            wp, col, kind = parts[n]; parts[n] = (wp.cut(cutter), col, kind)
