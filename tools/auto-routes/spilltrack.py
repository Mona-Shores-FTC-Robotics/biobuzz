# spilltrack.py <dir> <runs>: follow the pieces that were in the right CELL just before TIP 1 to where they rest 3 s
# later (Pedro inches), by nearest-neighbour tracking frame to frame through a run's .wpilog (doc/five-tip-flow-through.md).
import sys, struct, math, statistics as st
from wpilog import read
C = 70.75
def poses(p):
    out = []
    for k in range(0, len(p), 56):
        tx, ty, tz = struct.unpack('<3d', p[k:k+24]); out.append((ty/0.0254 + C, C - tx/0.0254, tz/0.0254))
    return out
def load(f):
    frames = {}; tip = None
    for t, n, typ, p in read(f):
        if n in ('/Sim/GamePieces/Pollen', '/Sim/GamePieces/RedNectar'):
            frames.setdefault(t, {})[n] = poses(p)
        if n == '/Events' and tip is None and b'tipped: LEFT_CELL_UP' in p: tip = t
    ts = sorted(frames); snaps = []
    for t in ts:
        fr = frames[t]; snaps.append((t, fr.get('/Sim/GamePieces/RedNectar', []) + fr.get('/Sim/GamePieces/Pollen', [])))
    return snaps, tip
if __name__ == '__main__':
    d, runs = sys.argv[1], int(sys.argv[2])
    if len(sys.argv) > 3:  # dump CELL candidates of run 1
        snaps, tip = load(f"{d}/1.wpilog")
        s = [q for t, q in snaps if t <= tip - 1.0][-1]
        for q in sorted(s, key=lambda q: -q[2])[:14]: print([round(v, 1) for v in q])
        sys.exit()

def track(f):
    snaps, tip = load(f)
    # The right CELL's load once the rocker starts to move (the TIP event comes ~0.74 s later): pieces high up there.
    t0, s0 = [x for x in snaps if x[0] <= tip - 0.7][-1]
    cell = [q for q in s0 if q[2] > 15 and 40 < q[1] < 72 and 50 < q[0] < 92]
    cur = list(cell)
    for t, s in snaps:
        if t <= t0 or t > tip + 3.0: continue
        nxt = []
        for q in cur:
            if not s: nxt.append(q); continue
            m = min(s, key=lambda r: math.dist(r, q))
            nxt.append(m if math.dist(m, q) < 8 else q)  # a piece that vanished (intaken) keeps its last place
        cur = nxt
    return cell, cur
if __name__ == '__main__' and len(sys.argv) == 3:
    tally = {'in the CELL': [], 'right end, our half (y<51)': [], 'within 24 in of (57.5, 30)': [], 'under the HIVE (y 51-90)': [],
             "the other alliance's half (x>70.75)": [], 'left end (y>90)': []}
    for i in range(1, runs + 1):
        cell, rest = track(f"{d}/{i}.wpilog")
        tally['in the CELL'].append(len(cell))
        tally['right end, our half (y<51)'].append(sum(1 for q in rest if q[0] <= 70.75 and q[1] < 51))
        tally['within 24 in of (57.5, 30)'].append(sum(1 for q in rest if math.dist(q[:2], (57.5, 30)) <= 24))
        tally['under the HIVE (y 51-90)'].append(sum(1 for q in rest if q[0] <= 70.75 and 51 <= q[1] <= 90))
        tally["the other alliance's half (x>70.75)"].append(sum(1 for q in rest if q[0] > 70.75))
        tally['left end (y>90)'].append(sum(1 for q in rest if q[0] <= 70.75 and q[1] > 90))
    for k, v in tally.items(): print(f"{k:38s} mean {st.mean(v):.1f}  per run {v}")
