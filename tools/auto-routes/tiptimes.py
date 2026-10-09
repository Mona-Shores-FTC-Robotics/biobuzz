"""tiptimes.py <dir> <runs> [label ...]: each run's TIP times (match seconds) and when the Auto's steps whose label
contains any of the given labels started and finished, for both robots (sister_five.py)."""
import sys, struct
import wpilog

d, n, labels = sys.argv[1], int(sys.argv[2]), sys.argv[3:]
for i in range(1, n + 1):
    rows = wpilog.read(f"{d}/{i}.wpilog")
    ev = [(t - 1.0, p.decode()) for t, k, typ, p in rows if k == '/Events']
    tips = [f"{t:.1f}" for t, e in ev if 'tipped:' in e and 'RED' in e]
    cell = [struct.unpack('<q', p)[0] for t, k, typ, p in rows if k == '/Sim/Hive/Red/RaisedCellPieces' and t - 1.0 <= 30.5]
    steps = []
    for lab in labels:
        for who in ('auto:', 'auto2:'):
            st = [t for t, e in ev if e.startswith(f"{who} wait {lab}")]
            en = [(t, e.split(': ')[-1]) for t, e in ev if e.startswith(f"{who} {lab}") and ': ' in e[len(who) + 1:]]
            if st: steps.append(f"{'R' if who == 'auto:' else 'L'} {lab[:22]} {st[0]:.1f}-{(en[0][0] if en else float('nan')):.1f}")
    shots = [t for t, e in ev if 'launcher: shot' in e]
    print(f"{i:2d} TIPs {' '.join(tips):28s} last shot {max(shots) if shots else 0:5.1f}  raised CELL at 30 s: "
          f"{cell[-1] if cell else '?'}  | " + " | ".join(steps))
