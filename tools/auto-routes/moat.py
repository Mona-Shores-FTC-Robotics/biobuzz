"""moat.py <dir> <runs> [--fit]: how big a moat has to be to hold a whole spill, every time, without a G409
(doc/spill-first-robot.md, "Sizing the moat"). Mentor, 8 Oct 2026: "design a moat and figure out what the size of that
moat has to be in order to have no violations 100% of the time. And then back into, can our robot support that moat?"

A moat here is a rectangle on the tiles, in the right end's frame: the robot's face along its near edge (toward our end
wall, y = y0), its body behind that, a far wall toward the HIVE (y = y1) and a side wall each side (x = x0, x1). Read
from logs where our robot is away from the spill (sister5-right goes to the GARDEN); TIP 1 spills the right CELL at our
end, TIP 2 the left CELL at the other end, which is turned about the field's centre line (y -> 141.5 - y) to count too.
Each spilled piece is traced by nearest neighbour and judged against the moat:

  foul (G409)  before its first touch on the tiles, its centre passes a wall below that wall's top + 1.6 in (a piece's
               radius, between POLLEN's 1.4 and NECTAR's 1.8), or over the robot's body below 18 + 1.6 in;
  out          it first touches the tiles outside the moat (or within 1.6 in of a wall: it lands on it);
  hops         after touching, the first time it reaches a wall its centre is above that wall's top: it goes over;
  kept         otherwise (it reaches no wall, or reaches one low enough to be stopped by it).

What happens after a wall stops a piece (the rebound, pieces hitting pieces inside) is not judged: the logs have no moat
in them. The script prints, for each wall, how tall it must be to stop every first bounce and how short it must stay to
miss every falling piece, at each position out from the landing patch; then (--fit) how much of each spill the largest
moats that fit R105 with a robot body behind them keep. The body sits behind the near wall, BODY_TOP tall."""
import sys, math, statistics as st
import spilltrack as s
import wpilog

R, TOUCH_Z, BODY_TOP, BODY_LEN, BODY_W = 1.6, 2.2, 18.0, 15.12, 15.24
FIELD = 141.5


def first_touch(p):
    """Index of a piece's first touch on the tiles: the first frame below TOUCH_Z, or the first low point of its fall
    under 5 in (the log is 50 Hz; a piece falling at 100+ in/s can touch and bounce between two frames)."""
    for i in range(1, len(p) - 1):
        z = p[i][3]
        if z < TOUCH_Z or (z < 5 and p[i - 1][3] > z <= p[i + 1][3]): return i
    return None


def spills(f):
    """[(pieces' paths [[(t, x, y, z)]]), ...] for TIP 1 (our end) and TIP 2 (turned to our end), each traced 4 s."""
    snaps, _ = s.load(f)
    ev = [(t, p.decode()) for t, n, typ, p in wpilog.read(f) if n == '/Events']
    out = []
    for key, flip in (('tipped: LEFT_CELL_UP', False), ('tipped: RIGHT_CELL_UP', True)):
        tips = [t for t, e in ev if key in e and 'RED' in e]
        if not tips: continue
        tip = tips[0]
        t0, s0 = [x for x in snaps if x[0] <= tip + 0.2][-1]
        end = (lambda q: q[1] > 66) if flip else (lambda q: q[1] < 75)
        cur = [q for q in s0 if q[2] > 15 and end(q) and 35 < q[0] < 100]
        if not cur: continue
        paths = [[(t0,) + tuple(q)] for q in cur]
        for t, snap in snaps:
            if t <= t0 or t > tip + 4.0 or not snap: continue
            for p in paths:
                m = min(snap, key=lambda r: math.dist(r, p[-1][1:]))
                p.append((t,) + tuple(m) if math.dist(m, p[-1][1:]) < 8 else (t,) + p[-1][1:])
        if flip: paths = [[(t, x, FIELD - y, z) for t, x, y, z in p] for p in paths]
        out.append(paths)
    return out


def crossings(a, b, x0, x1, y0, y1):
    """Where the segment a-b crosses a wall line within its span: [(wall, z)]. Walls: 'far' y1, 'side' x0/x1, 'face' y0."""
    hits = []
    (_, ax, ay, az), (_, bx, by, bz) = a, b
    for wall, axis, v, lo, hi in (('far', 1, y1, x0, x1), ('face', 1, y0, x0, x1), ('side', 0, x0, y0, y1), ('side', 0, x1, y0, y1)):
        pa, pb = (ay, by) if axis else (ax, bx)
        if (pa - v) * (pb - v) > 0 or pa == pb: continue
        u = (v - pa) / (pb - pa)
        other = (ax + u * (bx - ax)) if axis else (ay + u * (by - ay))
        if lo <= other <= hi: hits.append((wall, az + u * (bz - az)))
    return hits


def judge(spill, x0, x1, y0, y1, h_far, h_side):
    top = {'far': h_far, 'side': h_side, 'face': BODY_TOP}
    bx0 = (x0 + x1) / 2 - BODY_W / 2
    res = {'kept': 0, 'out': 0, 'hops': 0, 'foul': 0}
    for p in spill:
        landed, verdict, ft = False, 'kept', first_touch(p)
        for i, (a, b) in enumerate(zip(p, p[1:])):
            if not landed:
                if i + 1 == ft:
                    landed = True
                    x, y = b[1], b[2]
                    if not (x0 + R <= x <= x1 - R and y0 + R <= y <= y1 - R): verdict = 'out'; break
                    continue
                if any(z < top[w] + R for w, z in crossings(a, b, x0, x1, y0, y1)): verdict = 'foul'; break
                if bx0 <= b[1] <= bx0 + BODY_W and y0 - BODY_LEN <= b[2] <= y0 and b[3] < BODY_TOP + R: verdict = 'foul'; break
            else:
                c = crossings(a, b, x0, x1, y0, y1)
                if c:
                    w, z = c[0]
                    if z >= top[w]: verdict = 'hops'
                    break
        res[verdict] += 1
    return res


def wall_table(all_spills, axis, v, lo, hi, side_of_moat):
    """One wall on the line (x if axis == 0 else y) = v, spanning lo..hi on the other axis. side_of_moat is -1 if the
    moat is on the lower side of the line, +1 the higher. Returns (lowest in-air crossing z, highest after-landing
    first crossing z from inside, landings on the wrong side or on the wall): a wall here must be taller than the
    second and shorter than the first minus R."""
    air, bounce, wrong = math.inf, -math.inf, 0
    for sp in all_spills:
        for p in sp:
            landed, ft = False, first_touch(p)
            for i, (a, b) in enumerate(zip(p, p[1:])):
                pa, pb = (a[2], b[2]) if axis else (a[1], b[1])
                crosses = (pa - v) * (pb - v) <= 0 and pa != pb
                z = None
                if crosses:
                    u = (v - pa) / (pb - pa)
                    other = (a[1] + u * (b[1] - a[1])) if axis else (a[2] + u * (b[2] - a[2]))
                    if lo <= other <= hi: z = a[3] + u * (b[3] - a[3])
                if not landed:
                    if z is not None: air = min(air, z)
                    if i + 1 == ft:
                        landed = True
                        c = b[2] if axis else b[1]
                        if (c - v) * side_of_moat < R: wrong += 1
                    continue
                if z is not None:
                    bounce = max(bounce, z); break
    return air, bounce, wrong


if __name__ == '__main__':
    d, n = sys.argv[1], int(sys.argv[2])
    fit = '--fit' in sys.argv
    all_spills = [sp for i in range(1, n + 1) for sp in spills(f"{d}/{i}.wpilog")]
    pieces = sum(len(sp) for sp in all_spills)
    land = [(p[i][1], p[i][2]) for sp in all_spills for p in sp for i in [first_touch(p)] if i is not None]
    xs, ys = sorted(x for x, _ in land), sorted(y for _, y in land)
    k = len(land)
    print(f"{len(all_spills)} spills, {pieces} pieces, {k} first touches. x {xs[0]:.1f}-{xs[-1]:.1f} (p5 {xs[k//20]:.1f}, "
          f"p95 {xs[-k//20]:.1f}); y {ys[0]:.1f}-{ys[-1]:.1f} (p5 {ys[k//20]:.1f}, p95 {ys[-k//20]:.1f})")
    X0, X1, Y0, Y1 = xs[0] - 6, xs[-1] + 6, ys[0] - 6, ys[-1] + 6
    print("Each wall: position, landings on the wrong side, the height it must reach to stop every bounce, the most it may"
          " be (lowest falling piece there, less 1.6 in). OK where the first is under the second.")
    for name, axis, side, rng, span in (
            ("face (near, toward our wall)", 1, +1, [ys[0] - 0.5 * j for j in range(0, 25)], (X0, X1)),
            ("far (toward the HIVE)", 1, -1, [ys[-1] + 0.5 * j for j in range(0, 25)], (X0, X1)),
            ("side toward the audience wall (low x)", 0, +1, [xs[0] - 0.5 * j for j in range(0, 25)], (Y0, Y1)),
            ("side toward the centre line (high x)", 0, -1, [xs[-1] + 0.5 * j for j in range(0, 25)], (Y0, Y1))):
        print("==", name)
        for v in rng:
            air, bounce, wrong = wall_table(all_spills, axis, v, *span, side)
            need = max(bounce, 0.0)
            ok = "OK" if wrong == 0 and need < air - R else ""
            print(f"  at {v:6.1f}: wrong side {wrong:3d}  needs > {need:5.1f} in  may be < {air - R:6.1f} in  {ok}")
    if fit:
        print("Largest moats that fit 18 x 24 in with a body behind (walls 8 in, the body 18 in), best place on our half:")
        for name, w, dp, body in (("today's 15.12 in body, 18 wide x 24 long", 17, 8.4, 15.12),
                                  ("a 9.5 in body, 18 wide x 24 long", 17, 14, 9.5),
                                  ("a 9 in body, 24 wide x 18 long", 23.5, 8.4, 9),
                                  ("no body (not a robot; the ceiling)", 23.5, 17.5, 0.01)):
            globals()['BODY_LEN'] = body
            best = None
            for x1 in [70.75 - 0.5 * i for i in range(8)]:
                for y0 in [30 + 0.5 * i for i in range(20)]:
                    kept, every, fouls = [], 0, 0
                    for sp in all_spills:
                        r = judge(sp, x1 - w, x1, y0, y0 + dp, 8, 8)
                        tot = sum(r.values())
                        kept.append(r['kept'] / tot); every += r['kept'] == tot; fouls += r['foul'] > 0
                    key = (st.mean(kept) - fouls / len(all_spills), st.mean(kept))
                    if best is None or key > best[0]: best = (key, x1 - w, x1, y0, every, fouls)
            (_, m), x0, x1, y0, every, fouls = best
            print(f"  {name}: inside {w} x {dp} at x {x0}-{x1}, y {y0}-{y0 + dp}: keeps {100 * m:.0f}% of a spill, "
                  f"all of it in {every}/{len(all_spills)}, G409 in {fouls}/{len(all_spills)}")

