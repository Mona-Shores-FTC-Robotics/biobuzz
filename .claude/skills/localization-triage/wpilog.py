#!/usr/bin/env python3
"""Reads a BIOBUZZ match log (.wpilog) and answers localization questions. Standard library only.

    python3 -I wpilog.py info   LOG                 # metadata, events, channels
    python3 -I wpilog.py triage LOG [--start S --end S]   # mount vs field point vs odometry
    python3 -I wpilog.py dump   LOG KEY [KEY...]    # time series, CSV on stdout

Written from the wpilog spec (https://github.com/wpilibsuite/allwpilib/blob/main/wpiutil/doc/datalog.adoc),
not from our writer, like the test reader in src/test/.../logging/WpiLogReader.java.
"""
import argparse
import bisect
import math
import struct
import sys
from collections import defaultdict

# Channel names, from HiveAndPose.logTags and MatchLog. If these change, change them here.
HEADING = "/Odometry/PedroInches/HeadingDeg"
POSE_X = "/Odometry/PedroInches/X"
POSE_Y = "/Odometry/PedroInches/Y"
TAG_FIRST, TAG_LAST = 0, 99  # tag ids are found by pattern, not range
REASONS = {0: "ACCEPTED", 1: "HEIGHT", 2: "RANGE", 3: "OUTLIER"}


class Log:
    def __init__(self, path):
        with open(path, "rb") as f:
            data = f.read()
        if data[:6] != b"WPILOG":
            raise SystemExit(f"{path}: not a wpilog file")
        extra = struct.unpack_from("<I", data, 8)[0]
        self.extra_header = data[12:12 + extra].decode("utf-8", "replace")
        pos = 12 + extra
        self.entries = {}      # id -> (name, type)
        self.metadata = {}     # name -> metadata string
        self.series = defaultdict(list)  # name -> [(t_seconds, value)]
        n = len(data)
        while pos < n:
            hdr = data[pos]
            id_len = (hdr & 3) + 1
            size_len = ((hdr >> 2) & 3) + 1
            ts_len = ((hdr >> 4) & 7) + 1
            if pos + 1 + id_len + size_len + ts_len > n:
                break  # a brownout can cut the last record
            p = pos + 1
            eid = int.from_bytes(data[p:p + id_len], "little"); p += id_len
            size = int.from_bytes(data[p:p + size_len], "little"); p += size_len
            ts = int.from_bytes(data[p:p + ts_len], "little"); p += ts_len
            if p + size > n:
                break
            payload = data[p:p + size]
            pos = p + size
            if eid == 0:
                self._control(payload)
            elif eid in self.entries:
                name, typ = self.entries[eid]
                v = self._value(typ, payload)
                if v is not None:
                    self.series[name].append((ts / 1e6, v))

    def _control(self, pl):
        kind = pl[0]
        if kind == 0:
            eid = struct.unpack_from("<I", pl, 1)[0]
            p = 5
            strs = []
            for _ in range(3):
                ln = struct.unpack_from("<I", pl, p)[0]; p += 4
                strs.append(pl[p:p + ln].decode("utf-8", "replace")); p += ln
            self.entries[eid] = (strs[0], strs[1])
            if strs[2]:
                self.metadata.setdefault(strs[0], strs[2])
        elif kind == 2:
            eid = struct.unpack_from("<I", pl, 1)[0]
            ln = struct.unpack_from("<I", pl, 5)[0]
            if eid in self.entries:
                self.metadata[self.entries[eid][0]] = pl[9:9 + ln].decode("utf-8", "replace")

    @staticmethod
    def _value(typ, pl):
        try:
            if typ == "double":
                return struct.unpack("<d", pl)[0]
            if typ == "float":
                return struct.unpack("<f", pl)[0]
            if typ == "int64":
                return struct.unpack("<q", pl)[0]
            if typ == "boolean":
                return pl[0] != 0
            if typ == "string":
                return pl.decode("utf-8", "replace")
        except struct.error:
            return None
        return None  # struct and array types are not needed here

    def values(self, name, start=None, end=None):
        return [(t, v) for t, v in self.series.get(name, [])
                if (start is None or t >= start) and (end is None or t <= end)]


def mean(xs):
    return sum(xs) / len(xs) if xs else float("nan")


def std(xs):
    if len(xs) < 2:
        return float("nan")
    m = mean(xs)
    return math.sqrt(sum((x - m) ** 2 for x in xs) / (len(xs) - 1))


def interp(times, vals, t):
    """Linear interpolation of a series at t; heading wraps, so callers pass unwrapped radians."""
    i = bisect.bisect_left(times, t)
    if i == 0:
        return vals[0]
    if i >= len(times):
        return vals[-1]
    t0, t1 = times[i - 1], times[i]
    if t1 == t0:
        return vals[i]
    a = (t - t0) / (t1 - t0)
    return vals[i - 1] + a * (vals[i] - vals[i - 1])


def unwrap(rads):
    out, off, prev = [], 0.0, None
    for r in rads:
        if prev is not None:
            d = r - prev
            if d > math.pi:
                off -= 2 * math.pi
            elif d < -math.pi:
                off += 2 * math.pi
        out.append(r + off)
        prev = r
    return out


def make_still(log, max_speed, max_turn_deg):
    """Returns still(t): True when the robot moved less than the limits over +/-0.15 s around t.

    A camera frame is ~20 ms old when it is read, and the log compares it with odometry now.
    At 60 in/s that is ~1.2 in; turning at 180 deg/s moves a tag 60 in away by ~4 in. So only
    samples taken while still say anything about the mount or the map.
    """
    if max_speed is None:
        return (lambda t: True), "no still filter (--all)"
    hd = log.values(HEADING)
    ht = [t for t, _ in hd]
    hr = unwrap([math.radians(v) for _, v in hd])
    px, py = log.values(POSE_X), log.values(POSE_Y)
    have_xy = len(px) > 2 and len(py) > 2
    if have_xy:
        xt, xv = [t for t, _ in px], [v for _, v in px]
        yt, yv = [t for t, _ in py], [v for _, v in py]
    w = 0.15

    def still(t):
        turn = abs(interp(ht, hr, t + w) - interp(ht, hr, t - w)) / (2 * w)
        if math.degrees(turn) > max_turn_deg:
            return False
        if have_xy:
            dx = interp(xt, xv, t + w) - interp(xt, xv, t - w)
            dy = interp(yt, yv, t + w) - interp(yt, yv, t - w)
            if math.hypot(dx, dy) / (2 * w) > max_speed:
                return False
        return True
    note = (f"still only: under {max_speed} in/s and {max_turn_deg} deg/s"
            + ("" if have_xy else " (no X/Y channels: heading rate only)"))
    return still, note


def cmd_info(log, _args):
    print("== Metadata (Build, Hardware, Tunables, ...) ==")
    for k in sorted(log.metadata):
        v = log.metadata[k]
        if v:
            print(f"  {k}: {v}")
    print("\n== Channels ==")
    for name in sorted(log.series):
        s = log.series[name]
        print(f"  {name}  n={len(s)}  t={s[0][0]:.1f}..{s[-1][0]:.1f}s")


def cmd_dump(log, args):
    names = args.keys
    rows = defaultdict(dict)
    for k in names:
        for t, v in log.values(k):
            rows[t][k] = v
    print("t," + ",".join(names))
    for t in sorted(rows):
        print(f"{t:.4f}," + ",".join(str(rows[t].get(k, "")) for k in names))


def tag_ids(log):
    ids = set()
    for name in log.series:
        parts = name.split("/")
        if len(parts) == 4 and parts[1] == "Vision" and parts[2].startswith("Tag") and parts[3] == "FieldDxIn":
            ids.add(int(parts[2][3:]))
    return sorted(ids)


def cmd_triage(log, args):
    t0, t1 = args.start, args.end
    hd = log.values(HEADING)
    if not hd:
        raise SystemExit(f"No {HEADING} channel: this log has no odometry. Is it a RobotOpMode log?")
    htimes = [t for t, _ in hd]
    hrad = unwrap([math.radians(v) for _, v in hd])
    ids = tag_ids(log)
    if not ids:
        raise SystemExit("No /Vision/Tag<N>/FieldDxIn channels. These only exist in a run of "
                         "'Vision: HIVE & Pose'. Ask for one; a match log cannot answer this.")

    # Which build and which robot produced it, because the answer is per robot.
    for k in ("RobotConfig", "Build", "GitCommit", "GitBranch", "StartCheck"):
        for mk, mv in log.metadata.items():
            if mk.endswith(k) and mv:
                print(f"{mk}: {mv}")
    for k in ("CameraMount.pitchDeg", "CameraMount.yawDeg", "CameraMount.mountForwardIn",
              "CameraMount.mountLeftIn", "CameraMount.mountUpIn",
              "LocalizationTuning.continuousFixes", "LocalizationTuning.stillFixes"):
        for mk, mv in log.metadata.items():
            if mk.endswith(k):
                print(f"{mk}: {mv}")
    print()

    still, still_note = make_still(log, None if args.all else args.max_speed, args.max_turn)
    print(f"Samples: {still_note}\n")
    dropped_moving = 0
    # Per tag: field-axis and robot-axis residuals, accepted samples only.
    per_tag = {}
    all_robot = []  # (rx, ry) pooled
    for tid in ids:
        base = f"/Vision/Tag{tid}/"
        dxs = log.values(base + "FieldDxIn", t0, t1)
        dys = dict(log.values(base + "FieldDyIn", t0, t1))
        reasons = log.values(base + "Reason", t0, t1)
        rtimes = [t for t, _ in reasons]
        rvals = [v for _, v in reasons]
        rejected = defaultdict(int)
        for _, v in reasons:
            if int(v) != 0:
                rejected[REASONS.get(int(v), str(int(v)))] += 1
        pts = []
        for t, dx in dxs:
            dy = dys.get(t)
            if dy is None:
                continue
            # skip a sample whose frame this tag was rejected in
            i = bisect.bisect_right(rtimes, t) - 1
            if i >= 0 and abs(rtimes[i] - t) < 0.05 and int(rvals[i]) != 0:
                continue
            if not still(t):
                dropped_moving += 1
                continue
            th = interp(htimes, hrad, t)
            c, s = math.cos(th), math.sin(th)
            rx = dx * c + dy * s
            ry = -dx * s + dy * c
            pts.append((t, dx, dy, rx, ry, th))
        per_tag[tid] = (pts, dict(rejected))
        all_robot += [(p[3], p[4]) for p in pts]

    print("== Per tag (inches; residual = camera-implied robot minus odometry) ==")
    print("tag   n   field mean (dx,dy)   field spread  | robot mean (fwd,left)  robot spread | heading range deg | rejected")
    verdict_rows = []
    for tid, (pts, rej) in per_tag.items():
        if len(pts) < 5:
            print(f"{tid:>3} {len(pts):>3}   too few samples" + (f"   rejected {rej}" if rej else ""))
            continue
        fdx, fdy = [p[1] for p in pts], [p[2] for p in pts]
        rfx, rfy = [p[3] for p in pts], [p[4] for p in pts]
        fspread = math.hypot(std(fdx), std(fdy))
        rspread = math.hypot(std(rfx), std(rfy))
        hdeg = [math.degrees(p[5]) for p in pts]
        hrange = max(hdeg) - min(hdeg)
        print(f"{tid:>3} {len(pts):>3}   ({mean(fdx):+5.2f},{mean(fdy):+5.2f})        {fspread:5.2f}       "
              f"| ({mean(rfx):+5.2f},{mean(rfy):+5.2f})          {rspread:5.2f}        | {hrange:6.0f}            | {rej or ''}")
        verdict_rows.append((tid, len(pts), (mean(fdx), mean(fdy)), fspread, (mean(rfx), mean(rfy)), rspread, hrange))

    if not verdict_rows:
        print("\nNot enough accepted samples to say anything. Need 5+ per tag; drive per the README's vision session.")
        return

    print(f"\n({dropped_moving} samples dropped for motion)")
    print("\n== Reading ==")
    hr = max(r[6] for r in verdict_rows)
    if hr < 60:
        print(f"! Robot headings only span {hr:.0f} deg. Mount and field point look the same unless the robot "
              "turns: ask for a log where the robot faces a CELL, then turns on the spot through 2 more headings.")
    pooled_f = mean([r[2][0] for r in verdict_rows]), mean([r[2][1] for r in verdict_rows])
    pooled_r = mean([r[4][0] for r in verdict_rows]), mean([r[4][1] for r in verdict_rows])
    print(f"Pooled across tags: field ({pooled_f[0]:+.2f},{pooled_f[1]:+.2f}) in, "
          f"robot ({pooled_r[0]:+.2f} fwd,{pooled_r[1]:+.2f} left) in.")

    # Per-tag RMS residual is the same in either frame, so it says how far off a tag is without
    # letting offsets in opposite directions average away.
    rms = {}
    for tid, (pts, _) in per_tag.items():
        if len(pts) >= 5:
            rms[tid] = math.sqrt(mean([p[1] ** 2 + p[2] ** 2 for p in pts]))
    # A mount error or a field-point error is inches. A residual this big means the pose itself
    # was wrong: the declared start did not match where the robot sat (wrong side, wrong tile,
    # facing the wrong way). Vision: HIVE & Pose always declares RED_AUDIENCE.
    WRONG_START_IN = 8.0
    typical = sorted(rms.values())[len(rms) // 2]
    if typical > WRONG_START_IN:
        print(f"Per-tag RMS residual: " + ", ".join(f"tag {t}: {v:.1f}" for t, v in sorted(rms.items())) + " in")
        print(f"\n!! Typical residual {typical:.0f} in is far too big for a calibration error "
              f"(the start check's limit is {6} in). The pose was wrong, almost certainly because the "
              "robot was not on the declared start (Vision: HIVE & Pose declares the RED audience start). "
              "Check the StartCheck line above. NOT a calibration log: do not change CameraMount or "
              "HiveFieldPoints from it. Re-run from the red start.")
        return
    BAD_IN = 1.0
    bad = [r for r in verdict_rows if rms[r[0]] >= BAD_IN]
    worst = max(rms.values())
    print("Per-tag RMS residual: " + ", ".join(f"tag {t}: {v:.2f}" for t, v in sorted(rms.items())) + " in")

    rob_means = [r[4] for r in verdict_rows]
    spread_between_tags = math.hypot(std([m[0] for m in rob_means]), std([m[1] for m in rob_means]))
    mount_like = [r for r in bad if r[5] < r[3] * 0.7]
    field_like = [r for r in bad if r[3] < r[5] * 0.7]
    print(f"Of {len(bad)} tags at or over {BAD_IN} in: {len(mount_like)} steadier in robot axes (mount-like), "
          f"{len(field_like)} steadier in field axes (map-like).")
    print(f"Between-tag disagreement in robot axes: {spread_between_tags:.2f} in.")

    if not bad:
        print(f"-> Every tag under {BAD_IN} in RMS (worst {worst:.2f}). Nothing to change.")
    elif len(mount_like) > len(field_like) and spread_between_tags < max(1.0, math.hypot(*pooled_r) / 2):
        fwd, left = pooled_r
        print("-> MOUNT. The same offset in robot axes on every tag, and it turns with the robot.")
        print(f"   Residual is {fwd:+.2f} in forward, {left:+.2f} in left. See SKILL.md for which "
              "CameraMount field it maps to (a constant offset is a translation; one that grows with range is pitch or yaw).")
    elif len(field_like) > len(mount_like):
        print("-> FIELD POINT or ODOMETRY. The residual stays put on the field as the robot turns.")
        print("   If every tag moves together over time: odometry (drift, Pinpoint offsets). "
              "If tags disagree with each other and each is steady: that tag's HiveFieldPoints entry.")
    else:
        print("-> MIXED or too noisy to call. Report the table, do not guess. Ask for a longer, still-then-turn log.")

    # Height error
    herr = []
    for tid in ids:
        v = [x for _, x in log.values(f"/Vision/Tag{tid}/HeightErrorIn", t0, t1)]
        if len(v) >= 5:
            herr.append((tid, mean(v), std(v)))
    if herr:
        print("\n== Height error per tag (measured minus map, in) ==")
        for tid, m, s in herr:
            print(f"  tag {tid}: {m:+.2f} +/- {s:.2f}")
        ms = [m for _, m, _ in herr]
        if len(ms) >= 3 and abs(mean(ms)) > 1.0 and std(ms) < 1.0:
            print("-> Same height error on every tag: CameraMount.pitchDeg or mountUpIn, not a field point.")

    jx = log.values("/Vision/JitterXIn", t0, t1)
    jy = log.values("/Vision/JitterYIn", t0, t1)
    if jx and jy:
        print(f"\n== Jitter (still, camera std dev) ==\n  X {jx[-1][1]:.2f} in, Y {jy[-1][1]:.2f} in "
              "(compare LocalizationTuning.fixSigmaIn; this is noise, not bias)")


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest="cmd", required=True)
    for name in ("info", "triage", "dump"):
        p = sub.add_parser(name)
        p.add_argument("log")
        if name == "triage":
            p.add_argument("--start", type=float, default=None, help="seconds into the log")
            p.add_argument("--end", type=float, default=None)
            p.add_argument("--max-speed", type=float, default=2.0, help="in/s; samples faster are dropped")
            p.add_argument("--max-turn", type=float, default=5.0, help="deg/s; samples turning faster are dropped")
            p.add_argument("--all", action="store_true", help="keep moving samples (camera latency will bias them)")
        if name == "dump":
            p.add_argument("keys", nargs="+")
    args = ap.parse_args()
    log = Log(args.log)
    {"info": cmd_info, "triage": cmd_triage, "dump": cmd_dump}[args.cmd](log, args)


if __name__ == "__main__":
    sys.exit(main())
