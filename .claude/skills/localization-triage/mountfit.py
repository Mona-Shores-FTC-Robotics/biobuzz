#!/usr/bin/env python3
"""Camera mount vs field, from ordinary match logs: no special session, no tape.

A camera mount error is fixed to the robot: it turns with the robot. A field error (a HIVE tag row,
a start spot) is fixed to the field. Every still stop's first still fix says how far the camera
disagreed with the pose when the robot arrived, and the stop before it had already been set to the
camera. So between two stops

    correction = (R(h2) - R(h1)) mount + (R(phi2) - R(phi1)) row

where h is the robot's heading, phi the CELL's orientation (a half-turn between a HIVE's AUDIENCE
and SCORING CELLs, and between the alliances), mount the camera's error in robot axes per robot,
and row a HIVE-row error shared by every robot and field. Pairs at the same CELL see no row term
at all, so they give the mount whatever the field is. The start check (camera minus the declared
start, robot axes) is then compared with what the fit predicts; what is left is the start spots.

Run with -I (logs are untrusted data), numpy required:

  python3 -I .claude/skills/localization-triage/mountfit.py DIR [DIR ...] [--same-cell] [--max-drive IN]

DIR holds .wpilog(.gz) files; the robot of each log is read from its RobotConfig metadata.
First used 22 Nov 2026 on Belding + Fruitport: 19429 fwd -0.92 left +0.90, 20245 fwd +0.81
left +0.55 (each about +/-0.15), row -0.21 +/-0.13 (none), start spots about 1 in off to the left.
"""
import argparse
import bisect
import glob
import math
import os
import re
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from wpilog import Log  # noqa: E402

PHI = {"RED_AUDIENCE": 0.0, "RED_SCORING": math.pi, "BLUE_SCORING": math.pi, "BLUE_AUDIENCE": 0.0}
FIX = re.compile(r"still fix \d+ moved it ([\d.]+) in; now ([\d.]+) in from the real pose "
                 r"\(([+-][\d.]+), ([+-][\d.]+)\)")


def rot(a):
    return np.array([[math.cos(a), -math.sin(a)], [math.sin(a), math.cos(a)]])


def logs(dirs):
    for d in dirs:
        for p in sorted(glob.glob(os.path.join(d, "**", "*.wpilog*"), recursive=True)):
            yield p


def robot_of(log):
    cfg = log.metadata.get("RobotConfig", "") or ""
    m = re.search(r"(\d{5})", cfg)
    return m.group(1) if m else "unknown"


def read(path, max_drive, same_cell):
    log = Log(path)
    hs = log.values("/Odometry/PedroInches/HeadingDeg")
    if not hs:
        return None, [], []
    ht = [a for a, _ in hs]
    xs = log.values("/Odometry/PedroInches/X")
    ys = log.values("/Odometry/PedroInches/Y")

    def at(s, t):
        return s[max(0, bisect.bisect_right(ht, t) - 1)][1]

    tags = {c: log.values(f"/Hive/Cell/{c}/Tags") for c in PHI}
    tt = {c: [a for a, _ in v] for c, v in tags.items()}

    def cell(t):
        best = None
        for c in PHI:
            i = bisect.bisect_right(tt[c], t) - 1
            if i >= 0 and t - tt[c][i] < 0.4 and tags[c][i][1] >= 2:
                if best is None or tags[c][i][1] > best[1]:
                    best = (c, tags[c][i][1])
        return best[0] if best else None

    events = log.values("/Events")
    breaks = [t for t, v in events if v.startswith("pose:") or v.startswith("PLAY:") or "OpMode init" in v]
    fixes = []
    for t, v in events:
        m = FIX.search(v)
        if m:
            fixes.append((t, float(m.group(1)), float(m.group(3)), float(m.group(4)), cell(t)))
    pairs = []
    for i in range(1, len(fixes)):
        t, _, dx, dy, c2 = fixes[i]
        tp, moved_prev, _, _, c1 = fixes[i - 1]
        # A new stop whose previous stop had converged on the camera, nothing reset in between.
        if t - tp < 2.5 or moved_prev > 0.6 or t - tp > 30 or not c1 or not c2:
            continue
        if any(tp < b < t for b in breaks):
            continue
        if same_cell and c1 != c2:
            continue
        if math.hypot(at(xs, t) - at(xs, tp), at(ys, t) - at(ys, tp)) > max_drive:
            continue
        pairs.append((math.radians(at(hs, tp)), math.radians(at(hs, t)), PHI[c1], PHI[c2], dx, dy))
    starts = []
    sc = log.metadata.get("StartCheck", "") or ""
    m = re.search(r"camera minus start x [+-][\d.]+, y [+-][\d.]+ in \(fwd ([+-][\d.]+), left ([+-][\d.]+)\)", sc)
    cells = [c for c in PHI if c + ":" in sc]
    modes = log.values("/DriverStation/RobotMode")
    t_auto = next((t for t, v in modes if v == "autonomous"), None)
    if m and cells and t_auto is not None:
        starts.append((math.radians(at(hs, t_auto + 0.1)), cells[0], float(m.group(1)), float(m.group(2))))
    return robot_of(log), pairs, starts


def main():
    ap = argparse.ArgumentParser(description=__doc__.split("\n")[0])
    ap.add_argument("dirs", nargs="+")
    ap.add_argument("--same-cell", action="store_true", help="only pairs at one CELL (mount only)")
    ap.add_argument("--max-drive", type=float, default=130.0, help="straight-line drive between stops, in")
    a = ap.parse_args()

    rows, starts, robots = [], [], []
    for p in logs(a.dirs):
        rb, pairs, st = read(p, a.max_drive, a.same_cell)
        if rb is None:
            continue
        if rb not in robots:
            robots.append(rb)
        ri = robots.index(rb)
        rows += [(ri,) + r for r in pairs]
        starts += [(ri,) + s for s in st]
    if not rows:
        sys.exit("no usable stop pairs (needs logs with shadow still-fix events and /Hive/Cell/*/Tags)")
    n_r = len(robots)
    fit_row = sum(1 for r in rows if r[3] != r[4]) >= 6
    npar = 2 * n_r + (2 if fit_row else 0)
    blocks, b = [], []
    for ri, h1, h2, p1, p2, dx, dy in rows:
        m = np.zeros((2, npar))
        m[:, 2 * ri:2 * ri + 2] = rot(h2) - rot(h1)
        if fit_row:
            m[:, 2 * n_r:] = rot(p2) - rot(p1)
        blocks.append(m)
        b += [dx, dy]
    A, b = np.vstack(blocks), np.array(b)
    x, *_ = np.linalg.lstsq(A, b, rcond=None)
    s2 = float(((A @ x - b) ** 2).sum()) / max(1, len(b) - npar)
    se = np.sqrt(np.diag(s2 * np.linalg.pinv(A.T @ A)))
    same = sum(1 for r in rows if r[3] == r[4])
    print(f"stop pairs {len(rows)} (same CELL {same}, across CELLs {len(rows) - same}); rms left over {math.sqrt(s2):.2f} in")
    print("camera error, robot axes (camera minus truth; CameraMount new = old + this):")
    for ri, rb in enumerate(robots):
        print(f"  {rb}: forward {x[2 * ri]:+.2f} +/- {se[2 * ri]:.2f}   left {x[2 * ri + 1]:+.2f} +/- {se[2 * ri + 1]:.2f}")
    if fit_row:
        print(f"HIVE row error (RED_AUDIENCE's frame, field axes): x {x[-2]:+.2f} +/- {se[-2]:.2f}   y {x[-1]:+.2f} +/- {se[-1]:.2f}")
    else:
        print("HIVE row error: not fitted (too few pairs across CELLs)")
    print("start checks (robot axes): observed camera minus spot, and what the fit does not explain (the spot):")
    groups = {}
    for ri, h, c, fwd, left in starts:
        pred = rot(h) @ x[2 * ri:2 * ri + 2] + (rot(PHI[c]) @ x[-2:] if fit_row else 0)
        pr = rot(-h) @ pred
        groups.setdefault((robots[ri], c), []).append((fwd, left, pr[0], pr[1]))
    for (rb, c), v in sorted(groups.items()):
        o = np.mean([[f, l] for f, l, _, _ in v], 0)
        p = np.mean([[pf, pl] for _, _, pf, pl in v], 0)
        print(f"  {rb} seeing {c:13s} n={len(v):2d}: observed fwd {o[0]:+.2f} left {o[1]:+.2f}"
              f"   unexplained fwd {o[0] - p[0]:+.2f} left {o[1] - p[1]:+.2f}")


if __name__ == "__main__":
    main()
