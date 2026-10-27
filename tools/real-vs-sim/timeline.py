#!/usr/bin/env python3
"""timeline.py: the card timeline of one Auto from a .wpilog, real or simulated, in the same columns, so the two sit side by side.

    py tools/real-vs-sim/timeline.py <log.wpilog> [--robot 2]

Prints one row per card of the Auto: match seconds, the card, the pose when it started, and for a path card the pose at the
next card, how long it took, and the peak speed (300 ms centred window: the robot's pose log is about 43 Hz, so shorter
windows are noise). Reads the Auto's own trace (/Events lines starting "auto: ", or "auto2: " for the second robot of a
simulated pair with --robot 2) and /Odometry/PedroInches (or /Odometry/PartnerPedroInches for robot 2). Match time starts at
the "AUTO starts" event of a simulated log, or the first "autonomous" in /DriverStation/RobotMode of a real one.
Plain Python 3, nothing to install. For the drawn-against-driven table of a real log use tools/log-review/review.py
(branch claude/sharp-euler-3t56jg); this one is for comparing with the simulator. See doc/real-vs-sim-25-oct.md, "Redo this".
"""
import math, os, struct, sys
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "auto-routes"))
import wpilog  # noqa: E402

def main():
    args = sys.argv[1:]
    robot2 = "--robot" in args and args[args.index("--robot") + 1] == "2"
    path = next(a for a in args if a.endswith(".wpilog"))
    rows = wpilog.read(path)
    pre = "auto2: " if robot2 else "auto: "
    okey = "/Odometry/" + ("PartnerPedroInches/" if robot2 else "PedroInches/")
    t0 = next((t for t, k, ty, p in rows if k == "/Events" and p.decode() == "AUTO starts"), None)
    if t0 is None:
        t0 = next((t for t, k, ty, p in rows if k == "/DriverStation/RobotMode" and p.decode() == "autonomous"), 0.0)
    def series(axis):
        return {t: struct.unpack("<d", p)[0] for t, k, ty, p in rows if k == okey + axis}
    xs, ys, hs = series("X"), series("Y"), series("HeadingDeg")
    poses = sorted((t - t0, xs[t], ys[t], hs.get(t, float("nan"))) for t in xs if t in ys)
    if not poses:
        sys.exit("no " + okey + " in this log")
    def at(t):
        return min(poses, key=lambda p: abs(p[0] - t))
    def peak(a, b):
        best = 0.0
        for i, p in enumerate(poses):
            if not a <= p[0] <= b:
                continue
            lo = next((q for q in poses if q[0] >= p[0] - 0.15), p)
            hi = next((q for q in reversed(poses) if q[0] <= p[0] + 0.15), p)
            if hi[0] > lo[0]:
                best = max(best, math.hypot(hi[1] - lo[1], hi[2] - lo[2]) / (hi[0] - lo[0]))
        return best
    ev = [(t - t0, p.decode()[len(pre):]) for t, k, ty, p in rows if k == "/Events" and p.decode().startswith(pre)]
    ev = [(t, s) for t, s in ev if t >= -0.01 and "turret" not in s and "extractor" not in s]
    end = max([poses[-1][0]] + [t for t, _ in ev])
    print(f"{os.path.basename(path)}  ({'robot 2' if robot2 else 'robot 1'})   match seconds from {'AUTO starts' if t0 else 'start'}")
    print(f"{'t':>6}  {'card':<52} {'at':>22}  {'at next card':>22} {'took':>5} {'peak':>5}")
    for i, (t, s) in enumerate(ev):
        p0 = at(t)
        line = f"{t:6.2f}  {s[:52]:<52} ({p0[1]:5.1f},{p0[2]:5.1f},{p0[3]:4.0f}°)"
        if s.startswith("path "):
            nxt = next((c[0] for c in ev[i + 1:] if c[0] > t + 1e-6 and (c[1].startswith(("path ", "wait ", "finished")) or "park needs" in c[1])), end)
            p1 = at(nxt)
            line += f"  ({p1[1]:5.1f},{p1[2]:5.1f},{p1[3]:4.0f}°) {nxt - t:5.2f} {peak(t, nxt):5.1f}"
        print(line)
    f = at(end)
    print(f"final pose ({f[1]:.1f}, {f[2]:.1f}, {f[3]:.0f}°) at {f[0]:.1f} s")

if __name__ == "__main__":
    main()
