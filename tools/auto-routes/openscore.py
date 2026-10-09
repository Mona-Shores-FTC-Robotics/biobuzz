"""openscore.py <dir> <runs> [--at <label>]: for opening.py's routes, what robot 1 holds when TIP 2 raises the right
CELL (or at 9 s if it never does), TIP 1's time, and its G409 touches before then; one line for all runs. --at: when
robot 1's step <label> starts instead (tip2's "Holding 4?"), and the median time it got there."""
import sys, struct, collections, statistics as st
import wpilog

d, n = sys.argv[1], int(sys.argv[2])
at = sys.argv[sys.argv.index('--at') + 1] if '--at' in sys.argv else None
reached = []
held_at, tip1, g409 = collections.Counter(), [], 0
for i in range(1, n + 1):
    ev, held = [], []
    for t, name, typ, p in wpilog.read(f"{d}/{i}.wpilog"):
        if name == '/Events': ev.append((t - 1, p.decode()))
        elif name == '/Sim/Robot/Held': held.append((t - 1, struct.unpack('<q', p)[0]))
    t2 = next((t for t, e in ev if 'RED HIVE tipped: RIGHT_CELL_UP' in e), 9.0)
    if at:
        t2 = next((t for t, e in ev if e == 'auto: wait ' + at), 30.0)
        reached.append(t2)
    t1 = next((t for t, e in ev if 'RED HIVE tipped: LEFT_CELL_UP' in e), None)
    if t1: tip1.append(t1)
    held_at[([v for t, v in held if t <= t2] or [0])[-1]] += 1
    g409 += any(t <= t2 and 'G409: robot 1' in e for t, e in ev)
print(f"4 held at TIP 2 in {held_at[4]}/{n}  (3: {held_at[3]}, 2: {held_at[2]}, <=1: {held_at[1] + held_at[0]}); "
      f"mean held {sum(k * v for k, v in held_at.items()) / n:.2f}; TIP 1 median {st.median(tip1):.1f} s "
      f"({len(tip1)}/{n}); G409 {g409}/{n}" + (f"; there at {st.median(reached):.1f} s" if at else ""))
