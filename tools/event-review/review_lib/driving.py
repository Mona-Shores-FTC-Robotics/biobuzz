"""Driving: TeleOp speed from odometry (archive/belding-scripts/speed.py, drive2.py, trips2.py).

TeleOp here is the TeleOp OpMode's PLAY to its stop, at most 120 s. Speed is odometry position
resampled every 0.1 s; samples over 110 in/s (a pose jump) are dropped, and so is every sample
in a frozen-pose stretch from the event config.
"""
import collections
import math
import os
import re
import statistics as st

HERE = os.path.dirname(os.path.abspath(__file__))
ROBOTS = os.path.join(HERE, '..', '..', '..', 'TeamCode', 'src', 'main', 'java', 'org', 'firstinspires', 'ftc',
                      'teamcode', 'pedro', 'robots')


def window(m):
    if m.tele_play is None:
        return None, None
    return m.tele_play, min(m.tele_stop if m.tele_stop else 1e9, m.tele_play + 120)


def frozen(cfg, m):
    return [tuple(x) for x in cfg.get('frozen_pose', {}).get(m.team, {}).get(m.log, [])]


def tuned(team):
    """Pedro's measured top speeds for this robot (pedro/robots/Robot<team>.java), in/s."""
    try:
        s = open(os.path.join(ROBOTS, 'Robot%s.java' % team)).read()
    except OSError:
        return None, None
    f = re.search(r'maxAchievableForwardVelocity\.set\(([\d.]+)\)', s)
    g = re.search(r'maxAchievableStrafeVelocity\.set\(([\d.]+)\)', s)
    return (round(float(f.group(1)), 1) if f else None), (round(float(g.group(1)), 1) if g else None)


def q(a, p):
    a = sorted(a)
    return a[min(len(a) - 1, int(p * len(a)))] if a else float('nan')


def samples(m, cfg):
    t0, t1 = window(m)
    if t0 is None:
        return []
    X, Y, H = m['/Odometry/PedroInches/X'], m['/Odometry/PedroInches/Y'], m['/Odometry/PedroInches/HeadingDeg']
    F, Sx, T = m['/Drive/CommandForward'], m['/Drive/CommandStrafe'], m['/Drive/CommandTurn']
    L = m['/Lane/Count']
    fr = frozen(cfg, m)
    out, t = [], t0
    while t + 0.1 < t1:
        a, b = t, t + 0.1
        t = b
        if any(x <= b and a <= y for x, y in fr):
            continue
        xa, ya, xb, yb = X.at(a), Y.at(a), X.at(b), Y.at(b)
        if None in (xa, ya, xb, yb):
            continue
        vx, vy = (xb - xa) / 0.1, (yb - ya) / 0.1
        v = math.hypot(vx, vy)
        if v > 110:
            continue
        h = math.radians(H.at(b) or 0)
        f_, s_, tr = F.at(b), Sx.at(b), T.at(b)
        if f_ is None or f_ != f_:
            continue
        out.append(dict(v=v, fwd=vx * math.cos(h) + vy * math.sin(h), left=-vx * math.sin(h) + vy * math.cos(h),
                        cmd=math.hypot(f_, s_ or 0), turn=abs(tr or 0), lane=int(L.at(b) or 0)))
    return out


def top_speed(ms, cfg):
    sp_all = []
    for m in ms:
        t0, t1 = window(m)
        if t0 is None:
            continue
        X, Y = m['/Odometry/PedroInches/X'], m['/Odometry/PedroInches/Y']
        fr = frozen(cfg, m)
        grid = [t0 + k * 0.1 for k in range(int((t1 - t0) / 0.1))]
        pos = [(g, X.at(g), Y.at(g)) for g in grid]
        for (ta, xa, ya), (tb, xb, yb) in zip(pos, pos[1:]):
            if any(a <= tb and ta <= b for a, b in fr) or None in (xa, xb, ya, yb):
                continue
            v = math.hypot(xb - xa, yb - ya) / (tb - ta)
            if v <= 110:
                sp_all.append(v)
    s = sorted(sp_all)
    return round(s[int(.99 * len(s))], 1) if s else None


def speed(ms_by_team, cfg):
    out = []
    for team, ms in ms_by_team.items():
        S = [x for m in ms for x in samples(m, cfg)]
        if not S:
            continue
        full = [s for s in S if s['cmd'] >= 0.95 and s['turn'] < 0.2]
        dd = collections.defaultdict(list)
        for s in full:
            if s['v'] < 6:
                continue
            ang = abs(math.degrees(math.atan2(s['left'], s['fwd'])))
            ang = min(ang, 180 - ang)
            dd['fwd' if ang < 30 else 'strafe' if ang > 60 else 'diag'].append(s['v'])
        segs, cur = [], []
        for s in S + [None]:
            if s is not None and s['cmd'] >= 0.95 and s['turn'] < 0.2:
                cur.append(s)
            else:
                if cur:
                    segs.append(cur)
                cur = []
        lens = [len(c) * 0.1 for c in segs]
        v = [s['v'] for s in S]
        held = max((min(v[i:i + 5]) for i in range(len(v) - 5)), default=None)
        tf, ts = tuned(team)
        out.append(dict(robot=team, top_in_s=top_speed(ms, cfg),
                        fwd_full_in_s=round(st.median(dd['fwd']), 1) if dd['fwd'] else None,
                        strafe_full_in_s=round(st.median(dd['strafe']), 1) if dd['strafe'] else None,
                        bursts_under_1s=round(sum(x < 1 for x in lens) / len(lens), 2) if lens else None,
                        burst_median_s=round(st.median(lens), 1) if lens else None,
                        tuned_fwd_in_s=tf, tuned_strafe_in_s=ts,
                        held_half_s_in_s=round(held, 1) if held is not None else None))
    return out


def trips(ms_by_team, cfg):
    """drive_trips: driving to launch with a full lane (FULL to the gate opening) and heading out
    empty (gate shut on an empty lane to the first piece), each under 40 s and cut where auto-drive
    took over; speed is the median of 0.1 s samples over 6 in/s."""
    res = []
    for team, ms in ms_by_team.items():
        tr = {'full': [], 'empty': []}
        for m in ms:
            play, t1 = window(m)
            if play is None:
                continue
            L = [(t, v) for t, v in m['/Lane/Count'].between(play, t1)]
            fd = [(t, v) for t, v in m['/FeedGate/Feeding'].between(play, t1)]
            opens = [t for (a, x), (t, y) in zip(fd, fd[1:]) if y and not x]
            closes = [t for (a, x), (t, y) in zip(fd, fd[1:]) if x and not y]
            full_at = [t for (a, x), (t, y) in zip(L, L[1:]) if y >= 4 and x < 4]
            first_in = [t for (a, x), (t, y) in zip(L, L[1:]) if x == 0 and y >= 1]
            segs = []
            for t in full_at:
                o = next((g for g in opens if g > t), None)
                if o and o - t < 40:
                    segs.append(('full', t, o))
            for c in closes:
                if (m['/Lane/Count'].at(c + 0.05) or 0) > 0:
                    continue
                fi = next((g for g in first_in if g > c), None)
                if fi and fi - c < 40:
                    segs.append(('empty', c, fi))
            X, Y = m['/Odometry/PedroInches/X'], m['/Odometry/PedroInches/Y']
            ls = [t for t, v in m.events if v.startswith('launch spot: start')]
            fr = frozen(cfg, m)
            for kind, a, b in segs:
                cut = [t for t in ls if a < t < b]
                b = cut[0] if cut else b
                if any(x <= b and a <= y for x, y in fr):
                    continue
                vs, t = [], a
                while t + 0.1 <= b:
                    xa, ya, xb, yb = X.at(t), Y.at(t), X.at(t + 0.1), Y.at(t + 0.1)
                    t += 0.1
                    v = math.hypot(xb - xa, yb - ya) / 0.1
                    vs.append(float('nan') if v > 80 else v)
                vv = [v for v in vs if v == v]
                if len(vv) < 5:
                    continue
                mv = [v for v in vv if v > 6]
                tr[kind].append(st.median(mv) if mv else 0)
        if not tr['full'] and not tr['empty']:
            continue
        res.append(dict(robot=team, full_moving_in_s=round(st.median(tr['full']), 1) if tr['full'] else None,
                        empty_moving_in_s=round(st.median(tr['empty']), 1) if tr['empty'] else None,
                        full_trips=len(tr['full']), empty_trips=len(tr['empty'])))
    return res
