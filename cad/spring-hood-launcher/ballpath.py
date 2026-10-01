"""Sweeps POLLEN and NECTAR along the feed, the wrap and the exit; reports anything else they touch."""
import math, sys
import cadquery as cq
import launcher as L

def path(r_ball):
    rc = L.R_HOOD - r_ball if r_ball > 40 else 36 + r_ball      # NECTAR rides the opened hood, POLLEN the wheel
    rc = 36 + r_ball
    pts = []
    x0 = rc * math.cos(math.radians(L.A_ENTRY))
    for y in range(-260, int(rc * math.sin(math.radians(L.A_ENTRY))), 15):   # fed straight up from below
        pts.append((x0, y))
    for k in range(0, 31):
        pts.append(L.P(rc, L.A_ENTRY - 3 * k))
    ex = L.P(rc, L.A_EXIT); d = (math.cos(math.radians(75)), math.sin(math.radians(75)))
    for k in range(1, 12):
        pts.append(L.add(ex, d, 15 * k))
    return pts

def main():
    parts = [(n, w) for n, w, _ in L.fixed_printed()] + [(n, w) for n, _, w, _ in L.gobilda(None)] + [(n, w) for n, w, _ in L.hardware(0)]
    ok = True
    for name, r, frac in (("POLLEN", 35.6, 9 / 31), ("NECTAR", 46.0, 29 / 31)):
        moving = [(n, w) for n, w, _ in L.moving(frac)]
        hits = set()
        for c in path(r):
            ball = cq.Workplane("XY").sphere(r * 0.98).translate((c[0], c[1], 0)).val()
            for n, w in parts + moving:
                if any(k in n for k in ("Gecko", "Hood", "Wheel shaft")):
                    continue
                v = w.val()
                bb = v.BoundingBox()
                if bb.xmax < c[0] - r or bb.xmin > c[0] + r or bb.ymax < c[1] - r or bb.ymin > c[1] + r or bb.zmin > r or bb.zmax < -r:
                    continue
                if ball.intersect(v).Volume() > 1:
                    hits.add(n)
        print(name, "clear" if not hits else f"touches {sorted(hits)}")
        ok &= not hits
    sys.exit(0 if ok else 1)

if __name__ == "__main__":
    main()
