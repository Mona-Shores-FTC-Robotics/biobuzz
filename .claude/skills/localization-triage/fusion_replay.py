#!/usr/bin/env python3
"""Would continuous camera fixes (LocalizationTuning.continuousFixes) have helped? Replay and see.

Every gated camera frame is logged as /Drift/GapXIn, /Drift/GapYIn (camera minus pose) with
/Drift/Still, moving or not, so a match can be replayed with fusion on. Starting from the real pose,
each moving frame pulls a replayed pose `gain` of the way toward the camera (optionally moved back
by /Vision/LatencyMs along the logged path), with the still fix's 6 in jump gate. Both poses take
every real still fix. The score is the first still fix of each new stop: how far the camera
disagreed with each pose on arrival. Still frames are the camera's best reading, so a smaller
arrival gap is a pose that stayed nearer the camera while driving. It cannot judge the camera
itself: calibrate the mount first (mountfit.py).

  python3 -I .claude/skills/localization-triage/fusion_replay.py DIR [DIR ...] [--gains 0.02,0.05,0.1,0.5,0.95]

22 Nov 2026, Belding + Fruitport matches: fusion off, arrival median 1.6 in (19429) and 1.8 in
(20245), rms 1.8-2.1. Gain 0.05-0.1 per frame: median 0.2-0.4, rms 0.8-1.0, smaller at 40/56
and 40/46 stops. Gain 0.5-0.95 (about what Pedro's filter does with one fix at our variances: a
still fix moves the pose about 95 % of the way): rms 1.0-2.0, no better than off.
"""
import argparse
import bisect
import glob
import math
import os
import re
import statistics as st
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from wpilog import Log  # noqa: E402

FIX = re.compile(r"still fix \d+ moved it ([\d.]+) in; now ([\d.]+) in from the real pose "
                 r"\(([+-][\d.]+), ([+-][\d.]+)\)")


def replay(path, gains, latency_comp):
    log = Log(path)
    xs = log.values("/Odometry/PedroInches/X")
    ys = log.values("/Odometry/PedroInches/Y")
    if not xs:
        return {}
    t_odo = [a for a, _ in xs]

    def pose(t):
        i = bisect.bisect_right(t_odo, t) - 1
        if i < 0:
            return xs[0][1], ys[0][1]
        if i >= len(t_odo) - 1:
            return xs[-1][1], ys[-1][1]
        f = (t - t_odo[i]) / (t_odo[i + 1] - t_odo[i]) if t_odo[i + 1] > t_odo[i] else 0.0
        return xs[i][1] + f * (xs[i + 1][1] - xs[i][1]), ys[i][1] + f * (ys[i + 1][1] - ys[i][1])

    lat = log.values("/Vision/LatencyMs")
    t_lat = [a for a, _ in lat]

    def latency_s(t):
        i = bisect.bisect_right(t_lat, t) - 1
        v = lat[i][1] if i >= 0 else 30.0
        return (v if v == v and 0 < v < 200 else 30.0) / 1000.0

    gx, gy, still = (log.values(k) for k in ("/Drift/GapXIn", "/Drift/GapYIn", "/Drift/Still"))
    frames = [(gx[i][0], gx[i][1], gy[i][1], still[i][1]) for i in range(min(len(gx), len(gy), len(still)))]
    events = log.values("/Events")
    fixes = [(t, float(m.group(1)), float(m.group(3)), float(m.group(4)))
             for t, v in events for m in [FIX.search(v)] if m]
    breaks = sorted(t for t, v in events if v.startswith("pose:") or v.startswith("PLAY:") or "OpMode init" in v)
    out = {}
    for g in gains:
        scores, dx, dy, fi, bi, prev = [], 0.0, 0.0, 0, 0, None
        for t, fx, fy, is_still in frames:
            while fi < len(fixes) and fixes[fi][0] <= t:
                ft, moved, cx, cy = fixes[fi]
                if (prev is not None and 2.5 < ft - prev[0] <= 30 and prev[1] <= 0.6
                        and not any(prev[0] < b < ft for b in breaks)):
                    scores.append((math.hypot(cx, cy), math.hypot(cx - dx, cy - dy)))
                dx = dy = 0.0
                prev = (ft, moved)
                fi += 1
            while bi < len(breaks) and breaks[bi] <= t:
                dx = dy = 0.0
                bi += 1
            if is_still or g == 0:
                continue
            if latency_comp:
                a, b = pose(t), pose(t - latency_s(t))
                fx, fy = fx + a[0] - b[0], fy + a[1] - b[1]
            if math.hypot(fx - dx, fy - dy) > 6.0:
                continue
            dx += g * (fx - dx)
            dy += g * (fy - dy)
        out[g] = scores
    return out


def main():
    ap = argparse.ArgumentParser(description=__doc__.split("\n")[0])
    ap.add_argument("dirs", nargs="+")
    ap.add_argument("--gains", default="0.02,0.05,0.1,0.2,0.5,0.95")
    ap.add_argument("--no-latency", action="store_true", help="do not move frames back by their latency")
    a = ap.parse_args()
    gains = [float(g) for g in a.gains.split(",")]
    total = {g: [] for g in gains}
    for d in a.dirs:
        for p in sorted(glob.glob(os.path.join(d, "**", "*.wpilog*"), recursive=True)):
            for g, s in replay(p, gains, not a.no_latency).items():
                total[g] += s
    base = total[gains[0]]
    if not base:
        sys.exit("no scored stops (needs shadow still-fix events and /Drift channels)")
    off = [a for a, _ in base]
    print(f"stops scored {len(off)}; fusion off: arrival median {st.median(off):.2f} in, "
          f"rms {math.sqrt(st.mean([v * v for v in off])):.2f}")
    for g in gains:
        on = [b for _, b in total[g]]
        better = sum(1 for a_, b_ in total[g] if b_ < a_)
        print(f"  gain {g:<5}: median {st.median(on):.2f} in, rms {math.sqrt(st.mean([v * v for v in on])):.2f},"
              f" smaller at {better}/{len(on)} stops")


if __name__ == "__main__":
    main()
