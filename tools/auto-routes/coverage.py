"""coverage.py <dir> <runs>: the most of TIP 1's spill any 18 x 24 in robot could stop, wherever it stands
(doc/five-tip-flow-through.md, "A bigger change"). Run on logs where our robot is away from the spill (sister5-right
goes to the GARDEN). Each piece of the right CELL is traced through the spill by nearest neighbour; a footprint "takes" it if
the piece rolls into it after touching the tiles, and fouls (G409) if a piece is over it in the air, below the top of
a robot (z < 18 in), before touching. Every footprint is scanned on our half (x <= 70.75) at 2 in steps, both ways
round, and the best G409-free ones are printed."""
import sys, math, statistics as st
import spilltrack as s

TOP_IN, LAND_Z = 18.0, 3.0


def paths(f):
    snaps, tip = s.load(f)
    # The spill in the air 0.2 s after the TIP event (all of it is out by then, still 25-30 in up). Not the CELL's load
    # before the TIP, as spilltrack.py takes it: the CELL's starting NECTAR is logged only once it spills.
    t0, s0 = [x for x in snaps if x[0] <= tip + 0.2][-1]
    cur = [q for q in s0 if q[2] > 15 and q[1] < 75 and 35 < q[0] < 100]
    out = [[q] for q in cur]
    for t, snap in snaps:
        if t <= t0 or t > tip + 4.0 or not snap: continue
        for i, p in enumerate(out):
            m = min(snap, key=lambda r: math.dist(r, p[-1]))
            p.append(m if math.dist(m, p[-1]) < 8 else p[-1])
    return out


def score(runs, x0, y0, w, d):
    took, fouls = [], 0
    for ps in runs:
        n, foul = 0, False
        for p in ps:
            landed = False
            for q in p:
                inside = x0 <= q[0] <= x0 + w and y0 <= q[1] <= y0 + d
                if q[2] < LAND_Z: landed = True
                if inside and not landed and q[2] < TOP_IN: foul = True
                if inside and landed: n += 1; break
        took.append(n); fouls += foul
    return st.mean(took), fouls


if __name__ == "__main__":
    d, k = sys.argv[1], int(sys.argv[2])
    runs = [paths(f"{d}/{i}.wpilog") for i in range(1, k + 1)]
    print("pieces per spill", st.mean(len(r) for r in runs))
    rows = []
    for w, dp in ((24, 18), (18, 24)):
        for x0 in range(0, int(70.75 - w) + 1, 2):
            for y0 in range(0, 60, 2):
                m, f = score(runs, x0, y0, w, dp)
                rows.append((m, f, x0, y0, w, dp))
    clean = sorted([r for r in rows if r[1] == 0], reverse=True)[:6]
    any_ = sorted(rows, reverse=True)[:3]
    for m, f, x0, y0, w, dp in clean: print(f"G409-free  {w}x{dp} at x {x0}-{x0+w}, y {y0}-{y0+dp}: takes {m:.1f}")
    for m, f, x0, y0, w, dp in any_: print(f"any        {w}x{dp} at x {x0}-{x0+w}, y {y0}-{y0+dp}: takes {m:.1f}, G409 in {f}/{k}")
