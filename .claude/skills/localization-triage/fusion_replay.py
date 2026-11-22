#!/usr/bin/env python3
"""Would continuous camera fixes (LocalizationTuning.continuousFixes) have helped? Replay and see.

Every gated camera frame is logged as /Drift/GapXIn, /Drift/GapYIn (camera minus pose), moving or
still, so a match replays with continuous fixes on. The replay is the same arithmetic as
localization/FusionShadow on the robot, which is Pedro's FusionLocalizer in x and y:

- driving grows the covariance by processVarianceXY per inch, along the robot's own axes
  (FusionLocalizer.updateCovariance);
- each frame taken while moving is a Kalman measurement at its capture time (the log's latency,
  moved back along the logged path), with variance CellFix's still-fix variance times --scale,
  skipped if it is more than 6 in from the replayed pose (maxFixJumpIn), if a CELL it saw reads its
  row more than --max-row-error off the settled height (continuousFixMaxRowHeightErrorIn), or if the
  robot turns faster than --max-turn;
- each still fix is a measurement for both replays (still-fix variance), and moves the real pose
  onto the camera when the run had stillFixes on (read from the log's metadata).

The baseline is the same replay without continuous fixes, so runs with stillFixes off compare
like with like.

The score is the first still fix of each new stop: how far the camera disagreed with each pose on
arrival. Still frames are the camera's best reading, so a smaller arrival gap is a pose that stayed
nearer the camera while driving. It cannot judge the camera itself: calibrate the mount first
(mountfit.py).

  python3 -I .claude/skills/localization-triage/fusion_replay.py DIR [DIR ...] [--scales 1,10,40] [--max-turn 90]
"""
import argparse
import bisect
import glob
import math
import os
import re
import statistics as st
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from wpilog import Log  # noqa: E402

FIX = re.compile(r"still fix \d+ moved it ([\d.]+) in; now ([\d.]+) in from the real pose "
                 r"\(([+-][\d.]+), ([+-][\d.]+)\)")
Q_PER_IN = 1.0          # LocalizationTuning.processVarianceXY
STILL_VAR = (1.5 + 0.02 * 50) ** 2   # CellFix.variance at a typical 50 in, two or more tags
P0 = 0.25               # LocalizationTuning.initialVarianceXY
JUMP_IN = 6.0           # LocalizationTuning.maxFixJumpIn


def rot(h):
    c, s = math.cos(h), math.sin(h)
    return np.array([[c, -s], [s, c]])


def kalman(o, P, innov, r):
    S = P + np.eye(2) * r
    K = P @ np.linalg.inv(S)
    o = o + K @ innov
    IK = np.eye(2) - K
    P = IK @ P @ IK.T + K @ (np.eye(2) * r) @ K.T
    return o, P


def replay(path, scales, max_turn_dps, max_row_err=1.5):
    log = Log(path)
    xs, ys, hs = (log.values(k) for k in ("/Odometry/PedroInches/X", "/Odometry/PedroInches/Y",
                                          "/Odometry/PedroInches/HeadingDeg"))
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

    def series_at(key):
        s = log.values(key)
        ts = [a for a, _ in s]
        return lambda t: (s[bisect.bisect_right(ts, t) - 1][1] if ts and bisect.bisect_right(ts, t) > 0 else float("nan"))

    # Each frame's worst row height off its settled height (CellStateTracker: 50.2 UP, 35.0 DOWN),
    # from /Hive/Cell/*/RowHeightIn samples within 0.1 s: LocalizationTuning.continuousFixMaxRowHeightErrorIn.
    cells = ("RED_AUDIENCE", "RED_SCORING", "BLUE_AUDIENCE", "BLUE_SCORING")
    rows = {c: log.values(f"/Hive/Cell/{c}/RowHeightIn") for c in cells}
    row_t = {c: [a for a, _ in v] for c, v in rows.items()}

    def row_error(t):
        worst = 0.0
        for c in cells:
            j = bisect.bisect_right(row_t[c], t) - 1
            if j >= 0 and t - row_t[c][j] < 0.1:
                h = rows[c][j][1]
                worst = max(worst, min(abs(h - 50.2), abs(h - 35.0)))
        return worst

    latency = series_at("/Vision/LatencyMs")
    turn = series_at("/Drive/OdometryTurnRadPerSec")
    gx, gy, still = (log.values(k) for k in ("/Drift/GapXIn", "/Drift/GapYIn", "/Drift/Still"))
    events = log.values("/Events")
    fixes = [(t, float(m.group(3)), float(m.group(4))) for t, v in events for m in [FIX.search(v)] if m]
    breaks = [t for t, v in events if v.startswith("pose:") or v.startswith("PLAY:") or "OpMode init" in v]
    # One time-ordered stream: odometry steps, frames, still fixes, hard sets.
    stream = [(t_odo[i], 0, i) for i in range(1, len(t_odo))]
    stream += [(gx[i][0], 1, i) for i in range(min(len(gx), len(gy), len(still)))]
    stream += [(f[0], 2, k) for k, f in enumerate(fixes)]
    stream += [(b, 3, 0) for b in breaks]
    stream.sort()
    # Whether the real pose took the still fixes in this run (on from 20 Nov 2026; off at Belding).
    real_takes_fixes = str(log.metadata.get("Tunable/LocalizationTuning.stillFixes", "true")).lower() != "false"
    out = {}
    for scale in scales:
        o, P = np.zeros(2), np.eye(2) * P0
        moved_since_fix, last_fix_t, scores = 1e9, None, []
        for t, kind, i in stream:
            if kind == 0:
                dxw, dyw = xs[i][1] - xs[i - 1][1], ys[i][1] - ys[i - 1][1]
                h = math.radians(hs[i - 1][1])
                body = rot(-h) @ np.array([dxw, dyw])
                if abs(body[0]) + abs(body[1]) < 5:   # a jump is a hard set, not driving
                    P = P + rot(h) @ np.diag(np.abs(body) * Q_PER_IN) @ rot(h).T
                    moved_since_fix += abs(body[0]) + abs(body[1])
            elif kind == 1:
                if still[i][1] or scale is None:
                    continue
                if max_turn_dps and abs(math.degrees(turn(t))) > max_turn_dps:
                    continue
                if max_row_err and row_error(t) > max_row_err:
                    continue
                lat = latency(t)
                lat = (lat if lat == lat and 0 < lat < 200 else 30.0) / 1000.0
                a, b = pose(t), pose(t - lat)
                innov = np.array([gx[i][1] + a[0] - b[0], gy[i][1] + a[1] - b[1]]) - o
                if np.hypot(*innov) > JUMP_IN:
                    continue
                o, P = kalman(o, P, innov, STILL_VAR * scale)
            elif kind == 2:
                ft, cx, cy = fixes[i]
                cam = np.array([cx, cy])
                if last_fix_t is not None and ft - last_fix_t > 2.5 and moved_since_fix > 3:
                    scores.append((float(np.hypot(*(cam - o))), ft))
                # The replay takes the fix as a measurement; the real pose moved onto it if still fixes were on.
                o, P = kalman(o, P, cam - o, STILL_VAR)
                if real_takes_fixes:
                    o = o - cam
                last_fix_t, moved_since_fix = ft, 0.0
            else:
                o, P = np.zeros(2), np.eye(2) * P0
        out[scale] = scores
    base = out.get(None, [])
    return {s: [(b0, b, t) for (b0, _), (b, t) in zip(base, sc)] for s, sc in out.items()}


def main():
    ap = argparse.ArgumentParser(description=__doc__.split("\n")[0])
    ap.add_argument("dirs", nargs="+")
    ap.add_argument("--scales", default="1,4,10,25,50,100,200",
                    help="continuous-fix variance as a multiple of the still-fix variance")
    ap.add_argument("--max-turn", type=float, default=0.0, help="skip frames turning faster, deg/s (0: no limit)")
    ap.add_argument("--max-row-error", type=float, default=1.5,
                    help="skip frames whose CELL row is this far off its settled height, in (0: no limit)")
    a = ap.parse_args()
    scales = [None] + [float(s) for s in a.scales.split(",")]
    total = {s: [] for s in scales}
    for d in a.dirs:
        for p in sorted(glob.glob(os.path.join(d, "**", "*.wpilog*"), recursive=True)):
            for s, sc in replay(p, scales, a.max_turn, a.max_row_error).items():
                total[s] += sc
    off = [x for x, _, _ in total[None]]
    if not off:
        sys.exit("no scored stops (needs shadow still-fix events and /Drift channels)")
    print(f"stops scored {len(off)}; continuous fixes off: arrival median {st.median(off):.2f} in, "
          f"rms {math.sqrt(st.mean([v * v for v in off])):.2f}, worst {max(off):.1f}")
    for s in scales[1:]:
        on = [b for _, b, _ in total[s]]
        better = sum(1 for x, y, _ in total[s] if y < x)
        worse = sum(1 for x, y, _ in total[s] if y > x + 1.0)
        print(f"  variance x{s:<5g}: median {st.median(on):.2f} in, rms {math.sqrt(st.mean([v * v for v in on])):.2f},"
              f" worst {max(on):.1f}; smaller at {better}/{len(on)}, over 1 in worse at {worse}")


if __name__ == "__main__":
    main()
